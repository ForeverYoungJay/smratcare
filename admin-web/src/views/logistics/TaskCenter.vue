<template>
  <PageContainer
    title="后勤任务中心"
    subTitle="按清洁、维修、送餐、盘点进行任务分流与执行跟踪"
    :class="{ 'duty-dense': densityMode === 'dense' }"
  >
    <StatefulBlock :loading="loading" :error="errorMessage" :empty="false" @retry="loadData">
      <a-row :gutter="12" style="margin-bottom: 12px">
        <a-col :xs="24" :md="12" :xl="6">
          <a-card size="small" title="清洁任务">
            <div class="metric-value">{{ summary?.todayCleaningTaskCount || 0 }}</div>
            <div class="metric-line">待完成：{{ pendingCleaningCount }}</div>
            <a-space style="margin-top: 8px">
              <a-button type="primary" @click="go('/life/room-cleaning?status=PENDING')">待清洁</a-button>
              <a-button @click="go('/life/room-cleaning')">全部</a-button>
            </a-space>
          </a-card>
        </a-col>
        <a-col :xs="24" :md="12" :xl="6">
          <a-card size="small" title="维修任务">
            <div class="metric-value">{{ summary?.todayMaintenanceTaskCount || 0 }}</div>
            <div class="metric-line">待处理：{{ pendingMaintenanceCount }}</div>
            <a-space style="margin-top: 8px">
              <a-button type="primary" @click="go('/logistics/assets/maintenance-record?status=OPEN')">待派单</a-button>
              <a-button @click="go('/logistics/assets/maintenance-record?status=PROCESSING')">处理中</a-button>
            </a-space>
          </a-card>
        </a-col>
        <a-col :xs="24" :md="12" :xl="6">
          <a-card size="small" title="送餐任务">
            <div class="metric-value">{{ summary?.todayDeliveryTaskCount || 0 }}</div>
            <div class="metric-line">未完成：{{ pendingDeliveryCount }}</div>
            <a-space style="margin-top: 8px">
              <a-button type="primary" @click="go('/logistics/dining/delivery-plan?filter=undelivered')">未送达</a-button>
              <a-button @click="go('/logistics/dining/delivery-plan')">送餐计划</a-button>
            </a-space>
          </a-card>
        </a-col>
        <a-col :xs="24" :md="12" :xl="6">
          <a-card size="small" title="库存盘点">
            <div class="metric-value">{{ summary?.todayInventoryCheckTaskCount || 0 }}</div>
            <div class="metric-line">盘亏记录：{{ lossAdjustmentCount }}</div>
            <a-space style="margin-top: 8px">
              <a-button type="primary" @click="go('/logistics/storage/stock-check?adjustType=LOSS')">盘亏记录</a-button>
              <a-button @click="go('/logistics/storage/stock-check')">全部盘点</a-button>
            </a-space>
          </a-card>
        </a-col>
      </a-row>

      <a-card size="small" style="margin-bottom: 12px">
        <a-space wrap>
          <a-radio-group v-model:value="viewMode" button-style="solid">
            <a-radio-button value="ALL">全量视图</a-radio-button>
            <a-radio-button value="DUTY">值班模式</a-radio-button>
          </a-radio-group>
          <a-radio-group v-model:value="densityMode" button-style="solid">
            <a-radio-button value="normal">标准密度</a-radio-button>
            <a-radio-button value="dense">值班大屏</a-radio-button>
          </a-radio-group>
          <a-button @click="go('/oa/work-execution/task?department=logistics')">OA任务中心</a-button>
          <a-button @click="go('/logistics/reports/maintenance-todo-log')">维保执行日志</a-button>
          <a-button @click="exportCurrentTab">导出当前视图</a-button>
          <a-button @click="exportActionLogs" :disabled="actionLogs.length === 0">导出操作日志</a-button>
          <BatchActionBar :actions="tabBatchActions" />
          <a-button type="primary" @click="loadData">刷新任务数据</a-button>
        </a-space>
      </a-card>
      <a-alert
        v-if="lastBatchReceipt"
        type="info"
        show-icon
        style="margin-bottom: 8px"
        :message="`最近回执：${lastBatchReceipt.action}｜开始 ${lastBatchReceipt.startAt}｜结束 ${lastBatchReceipt.finishAt}｜成功 ${lastBatchReceipt.success}/${lastBatchReceipt.total}｜失败 ${lastBatchReceipt.failed}`"
      />
      <a-row v-if="batchFailures.length > 0" :gutter="12" style="margin-bottom: 8px">
        <a-col :span="8"><a-statistic title="失败总数" :value="batchFailures.length" /></a-col>
        <a-col :span="8"><a-statistic title="可重试失败" :value="retryableBatchFailures.length" /></a-col>
        <a-col :span="8"><a-statistic title="错误码种类" :value="batchFailureCodeSummary.length" /></a-col>
      </a-row>
      <a-alert
        v-if="batchFailureCodeSummary.length > 0"
        style="margin-bottom: 8px"
        type="warning"
        show-icon
        :message="`错误码聚合：${batchFailureCodeSummary.map((item) => `${item.code}(${item.count})`).join('，')}`"
      />

      <a-card v-if="actionLogs.length > 0" size="small" style="margin-bottom: 12px" title="本机操作日志（最近20条）">
        <a-timeline>
          <a-timeline-item v-for="item in latestActionLogs" :key="item.id" :color="item.success ? 'green' : 'red'">
            {{ item.time }}｜{{ item.action }}｜{{ item.detail }}
          </a-timeline-item>
        </a-timeline>
      </a-card>

      <a-alert
        v-if="overdueTotal > 0"
        style="margin-bottom: 12px"
        type="warning"
        show-icon
        :message="`当前超时任务共 ${overdueTotal} 项（清洁 ${overdueCleaningCount}、维修 ${overdueMaintenanceCount}、送餐 ${overdueDeliveryCount}）`"
      />

      <a-tabs v-model:activeKey="activeTab">
        <a-tab-pane key="cleaning" tab="清洁任务">
          <a-table
            :columns="cleaningColumns"
            :data-source="displayCleaningRows"
            :size="tableSize"
            :pagination="false"
            row-key="id"
            :row-selection="cleaningRowSelection"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <a-tag :color="cleaningStatusColor(record)">{{ cleaningStatusLabel(record) }}</a-tag>
              </template>
              <template v-if="column.key === 'action'">
                <a-button type="link" :disabled="isCleaningDone(record.status)" @click="finishCleaning(record)">完成</a-button>
              </template>
            </template>
          </a-table>
        </a-tab-pane>
        <a-tab-pane key="maintenance" tab="维修任务">
          <a-table
            :columns="maintenanceColumns"
            :data-source="displayMaintenanceRows"
            :size="tableSize"
            :pagination="false"
            row-key="id"
            :row-selection="maintenanceRowSelection"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <a-tag :color="maintenanceStatusColor(record)">{{ maintenanceStatusLabel(record) }}</a-tag>
              </template>
              <template v-if="column.key === 'action'">
                <a-button type="link" :disabled="record.status === 'COMPLETED'" @click="finishMaintenance(record)">完成</a-button>
              </template>
            </template>
          </a-table>
        </a-tab-pane>
        <a-tab-pane key="delivery" tab="送餐任务">
          <a-table
            :columns="deliveryColumns"
            :data-source="displayDeliveryRows"
            :size="tableSize"
            :pagination="false"
            row-key="id"
            :row-selection="deliveryRowSelection"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <a-tag :color="deliveryStatusColor(record)">{{ deliveryStatusLabel(record) }}</a-tag>
              </template>
              <template v-if="column.key === 'action'">
                <a-space>
                  <a-button type="link" :disabled="record.status === 'DELIVERED'" @click="finishDelivery(record)">送达</a-button>
                  <a-button type="link" danger :disabled="record.status === 'FAILED'" @click="markDeliveryFailed(record)">失败</a-button>
                  <a-button type="link" :disabled="record.status === 'DELIVERED'" @click="redispatchDelivery(record)">重派</a-button>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-tab-pane>
        <a-tab-pane key="inventory" tab="盘点任务">
          <a-table
            :columns="inventoryColumns"
            :data-source="displayAdjustmentRows"
            :size="tableSize"
            :pagination="false"
            row-key="id"
          />
        </a-tab-pane>
      </a-tabs>
    </StatefulBlock>

    <a-drawer v-model:open="batchFailureDrawerOpen" title="批量失败明细" width="760">
      <a-form layout="inline" style="margin-bottom: 8px">
        <a-form-item label="关键字">
          <a-input v-model:value="batchFailureKeyword" allow-clear placeholder="动作/原因/错误码/路径/对象ID" style="width: 280px" />
        </a-form-item>
        <a-form-item>
          <a-checkbox v-model:checked="batchFailureRetryableOnly">仅可重试</a-checkbox>
        </a-form-item>
      </a-form>
      <a-alert
        v-if="batchFailureActionSummary.length > 0"
        type="warning"
        show-icon
        style="margin-bottom: 8px"
        :message="`动作聚合：${batchFailureActionSummary.map((item) => `${item.action}(${item.count})`).join('，')}`"
      />
      <a-table :data-source="filteredBatchFailures" :pagination="false" row-key="at" size="small">
        <a-table-column title="时间" data-index="at" key="at" width="170" />
        <a-table-column title="动作" data-index="action" key="action" width="150" />
        <a-table-column title="对象ID" data-index="itemId" key="itemId" width="120" />
        <a-table-column title="错误码" data-index="code" key="code" width="120" />
        <a-table-column title="接口路径" data-index="path" key="path" width="200" />
        <a-table-column title="可重试" key="retryable" width="90">
          <template #default="{ record }">
            <a-tag :color="record.retryable ? 'green' : 'red'">{{ record.retryable ? '是' : '否' }}</a-tag>
          </template>
        </a-table-column>
        <a-table-column title="失败原因" data-index="reason" key="reason" />
      </a-table>
    </a-drawer>

    <a-modal v-model:open="batchProgress.open" :title="batchProgress.title" :footer="null" :mask-closable="false" :closable="!batchProgress.running" @cancel="closeBatchProgress">
      <a-space direction="vertical" style="width: 100%">
        <a-progress :percent="batchProgress.percent" :status="batchProgress.running ? 'active' : 'normal'" />
        <a-statistic title="总数" :value="batchProgress.total" />
        <a-statistic title="已处理" :value="batchProgress.current" />
        <a-statistic title="成功" :value="batchProgress.success" />
        <a-statistic title="失败" :value="batchProgress.failed" />
      </a-space>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
import BatchActionBar, { type BatchActionItem } from '../../components/BatchActionBar.vue'
import { exportCsv } from '../../utils/export'
import { useUserStore } from '../../stores/user'
import { getLogisticsWorkbenchSummary } from '../../api/logistics'
import { completeMaintenance, completeRoomCleaning, getMaintenancePage, getRoomCleaningPage } from '../../api/life'
import { getDiningDeliveryRecordPage, redispatchDiningDeliveryRecord, updateDiningDeliveryRecord } from '../../api/dining'
import { getInventoryAdjustmentPage } from '../../api/materialCenter'
import type { DiningDeliveryRecord, InventoryAdjustmentItem, LogisticsWorkbenchSummary, MaintenanceRequest, RoomCleaningTask } from '../../types'

type ActionLogEntry = {
  id: string
  time: string
  action: string
  detail: string
  success: boolean
}

type TaskBatchFailure = {
  at: string
  action: string
  itemId: string | number
  reason: string
  code: string
  path: string
  retryable: boolean
}

type TaskBatchReceipt = {
  action: string
  startAt: string
  finishAt: string
  total: number
  success: number
  failed: number
}

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()
const loading = ref(false)
const errorMessage = ref('')
const activeTab = ref('cleaning')
const viewMode = ref<'ALL' | 'DUTY'>('ALL')
const densityMode = ref<'normal' | 'dense'>('normal')
const tableSize = computed(() => (densityMode.value === 'dense' ? 'small' : 'middle'))

const summary = ref<LogisticsWorkbenchSummary>()
const cleaningRows = ref<RoomCleaningTask[]>([])
const maintenanceRows = ref<MaintenanceRequest[]>([])
const deliveryRows = ref<DiningDeliveryRecord[]>([])
const adjustmentRows = ref<InventoryAdjustmentItem[]>([])
const selectedCleaningKeys = ref<Array<number | string>>([])
const selectedMaintenanceKeys = ref<Array<number | string>>([])
const selectedDeliveryKeys = ref<Array<number | string>>([])
const actionLogs = ref<ActionLogEntry[]>([])
const batchFailures = ref<TaskBatchFailure[]>([])
const batchFailureDrawerOpen = ref(false)
const lastBatchReceipt = ref<TaskBatchReceipt | null>(null)
const batchFailureKeyword = ref('')
const batchFailureRetryableOnly = ref(false)
const batchProgress = ref({
  open: false,
  running: false,
  title: '批量处理中',
  total: 0,
  current: 0,
  success: 0,
  failed: 0,
  percent: 0
})
const latestActionLogs = computed(() => actionLogs.value.slice(0, 20))

const pendingCleaningCount = computed(() => cleaningRows.value.filter((item) => !isCleaningDone(item.status)).length)
const pendingMaintenanceCount = computed(() =>
  maintenanceRows.value.filter((item) => {
    const status = normalizeStatus(item.status)
    return status === 'OPEN' || status === 'PROCESSING'
  }).length
)
const pendingDeliveryCount = computed(() => deliveryRows.value.filter((item) => normalizeStatus(item.status) !== 'DELIVERED').length)
const lossAdjustmentCount = computed(() => adjustmentRows.value.filter((item) => item.adjustType === 'LOSS').length)
const overdueCleaningCount = computed(() =>
  cleaningRows.value.filter((item) => !isCleaningDone(item.status) && item.planDate && dayjs(item.planDate).isBefore(dayjs(), 'day')).length
)
const overdueMaintenanceCount = computed(() =>
  maintenanceRows.value.filter((item) => normalizeStatus(item.status) !== 'COMPLETED' && item.reportedAt && dayjs(item.reportedAt).isBefore(dayjs().subtract(2, 'day'))).length
)
const overdueDeliveryCount = computed(() =>
  deliveryRows.value.filter((item) => {
    if (normalizeStatus(item.status) === 'DELIVERED') return false
    const refTime = item.deliveredAt || item.createTime
    return Boolean(refTime && dayjs(refTime).isBefore(dayjs().subtract(2, 'hour')))
  }).length
)
const overdueTotal = computed(() => overdueCleaningCount.value + overdueMaintenanceCount.value + overdueDeliveryCount.value)

const displayCleaningRows = computed(() => {
  if (viewMode.value === 'ALL') return cleaningRows.value
  return cleaningRows.value.filter((item) => !isCleaningDone(item.status))
})
const displayMaintenanceRows = computed(() => {
  if (viewMode.value === 'ALL') return maintenanceRows.value
  return maintenanceRows.value.filter((item) => {
    const status = normalizeStatus(item.status)
    return status === 'OPEN' || status === 'PROCESSING'
  })
})
const displayDeliveryRows = computed(() => {
  if (viewMode.value === 'ALL') return deliveryRows.value
  return deliveryRows.value.filter((item) => normalizeStatus(item.status) !== 'DELIVERED')
})
const displayAdjustmentRows = computed(() => {
  if (viewMode.value === 'ALL') return adjustmentRows.value
  return adjustmentRows.value.filter((item) => item.adjustType === 'LOSS')
})
const retryableBatchFailures = computed(() => batchFailures.value.filter((item) => item.retryable))
const filteredBatchFailures = computed(() => {
  const keyword = batchFailureKeyword.value.trim().toLowerCase()
  return batchFailures.value.filter((item) => {
    if (batchFailureRetryableOnly.value && !item.retryable) return false
    if (!keyword) return true
    const haystack = [item.action, item.reason, item.code, item.path, String(item.itemId)].join(' ').toLowerCase()
    return haystack.includes(keyword)
  })
})
const batchFailureActionSummary = computed(() => {
  const map = new Map<string, number>()
  for (const row of filteredBatchFailures.value) {
    const key = String(row.action || '-')
    map.set(key, (map.get(key) || 0) + 1)
  }
  return Array.from(map.entries())
    .map(([action, count]) => ({ action, count }))
    .sort((a, b) => b.count - a.count)
})
const batchFailureCodeSummary = computed(() => {
  const map = new Map<string, number>()
  for (const row of batchFailures.value) {
    const key = String(row.code || '-')
    map.set(key, (map.get(key) || 0) + 1)
  }
  return Array.from(map.entries())
    .map(([code, count]) => ({ code, count }))
    .sort((a, b) => b.count - a.count)
    .slice(0, 6)
})

const cleaningRowSelection = computed(() => ({
  selectedRowKeys: selectedCleaningKeys.value,
  onChange: (keys: Array<number | string>) => {
    selectedCleaningKeys.value = keys
  }
}))
const maintenanceRowSelection = computed(() => ({
  selectedRowKeys: selectedMaintenanceKeys.value,
  onChange: (keys: Array<number | string>) => {
    selectedMaintenanceKeys.value = keys
  }
}))
const deliveryRowSelection = computed(() => ({
  selectedRowKeys: selectedDeliveryKeys.value,
  onChange: (keys: Array<number | string>) => {
    selectedDeliveryKeys.value = keys
  }
}))

function clearActiveTabSelection() {
  if (activeTab.value === 'cleaning') {
    selectedCleaningKeys.value = []
    return
  }
  if (activeTab.value === 'maintenance') {
    selectedMaintenanceKeys.value = []
    return
  }
  if (activeTab.value === 'delivery') {
    selectedDeliveryKeys.value = []
  }
}

function startBatchProgress(title: string, total: number) {
  batchProgress.value = {
    open: true,
    running: true,
    title,
    total,
    current: 0,
    success: 0,
    failed: 0,
    percent: total > 0 ? 0 : 100
  }
}

function stepBatchProgress(success: boolean) {
  batchProgress.value.current += 1
  if (success) batchProgress.value.success += 1
  else batchProgress.value.failed += 1
  batchProgress.value.percent = batchProgress.value.total > 0
    ? Math.min(100, Math.round((batchProgress.value.current / batchProgress.value.total) * 100))
    : 100
}

function finishBatchProgress() {
  batchProgress.value.running = false
  window.setTimeout(() => {
    if (!batchProgress.value.running) batchProgress.value.open = false
  }, 800)
}

function closeBatchProgress() {
  if (batchProgress.value.running) return
  batchProgress.value.open = false
}

function normalizeErrorMessage(error: unknown) {
  const raw = error as any
  return String(raw?.msg || raw?.message || raw?.response?.data?.msg || '未知错误')
}

function parseErrorDetail(error: unknown) {
  const raw = error as any
  const status = raw?.response?.status
  const codeRaw = raw?.response?.data?.code ?? status ?? raw?.code
  const code = codeRaw == null ? '' : String(codeRaw)
  const path = String(raw?.config?.url || raw?.response?.config?.url || '').split('?')[0]
  const retryable = !status || status >= 500 || status === 429 || status === 408 || code === 'ECONNABORTED' || code === 'ERR_NETWORK'
  return {
    reason: normalizeErrorMessage(error),
    code: code || '-',
    path: path || '-',
    retryable
  }
}

function exportBatchFailures() {
  if (!batchFailures.value.length) {
    message.info('暂无失败明细')
    return
  }
  exportCsv(
    batchFailures.value.map((item) => ({
      时间: item.at,
      动作: item.action,
      对象ID: item.itemId,
      错误码: item.code,
      接口路径: item.path,
      可重试: item.retryable ? '是' : '否',
      失败原因: item.reason
    })),
    `后勤批量失败明细-${dayjs().format('YYYYMMDD-HHmm')}.csv`
  )
}

function exportBatchReceipt() {
  const receipt = lastBatchReceipt.value
  if (!receipt) {
    message.info('暂无可导出的执行回执')
    return
  }
  exportCsv(
    [{
      动作: receipt.action,
      开始时间: receipt.startAt,
      结束时间: receipt.finishAt,
      总数: receipt.total,
      成功: receipt.success,
      失败: receipt.failed,
      失败率: receipt.total > 0 ? `${Math.round((receipt.failed / receipt.total) * 100)}%` : '0%'
    }],
    `后勤批量执行回执-${dayjs().format('YYYYMMDD-HHmm')}.csv`
  )
}

async function copyBatchFailureDigest() {
  if (!batchFailures.value.length) {
    message.info('暂无失败明细')
    return
  }
  const lines = batchFailures.value.slice(0, 40).map((item) =>
    `[${item.at}] action=${item.action} itemId=${item.itemId} code=${item.code} retryable=${item.retryable ? 'Y' : 'N'} path=${item.path} reason=${item.reason}`
  )
  const text = [`后勤批量失败摘要 total=${batchFailures.value.length}`, ...lines].join('\n')
  try {
    if (navigator?.clipboard?.writeText) {
      await navigator.clipboard.writeText(text)
      message.success('排障摘要已复制')
      return
    }
  } catch {}
  message.warning('当前环境不支持剪贴板自动复制，请导出失败明细')
}

function latestBatchFailureAction() {
  return batchFailures.value[0]?.action || ''
}

function failedBatchIds() {
  return Array.from(new Set(batchFailures.value.map((item) => item.itemId).filter((id) => id !== null && id !== undefined && String(id) !== '')))
}

async function retryBatchFailures() {
  const ids = failedBatchIds()
  if (!ids.length) {
    message.info('暂无可重试失败项')
    return
  }
  const action = latestBatchFailureAction()
  if (action.includes('清洁')) {
    selectedCleaningKeys.value = ids
    await batchFinishCleaning()
    return
  }
  if (action.includes('维修')) {
    selectedMaintenanceKeys.value = ids
    await batchFinishMaintenance()
    return
  }
  selectedDeliveryKeys.value = ids
  await batchRedispatchDelivery()
}

async function retryRetryableBatchFailures() {
  const ids = Array.from(new Set(retryableBatchFailures.value.map((item) => item.itemId)))
  if (!ids.length) {
    message.info('暂无可重试失败项')
    return
  }
  const action = latestBatchFailureAction()
  if (action.includes('清洁')) {
    selectedCleaningKeys.value = ids
    await batchFinishCleaning()
    return
  }
  if (action.includes('维修')) {
    selectedMaintenanceKeys.value = ids
    await batchFinishMaintenance()
    return
  }
  selectedDeliveryKeys.value = ids
  await batchRedispatchDelivery()
}

const tabBatchActions = computed<BatchActionItem[]>(() => {
  if (activeTab.value === 'cleaning') {
    return [
      {
        key: 'batch-cleaning',
        type: 'primary',
        label: `批量完成清洁（${selectedCleaningKeys.value.length}）`,
        disabled: selectedCleaningKeys.value.length === 0,
        onClick: batchFinishCleaning
      },
      {
        key: 'clear',
        label: '清空勾选',
        disabled: selectedCleaningKeys.value.length === 0,
        onClick: clearActiveTabSelection
      },
      {
        key: 'retry-failures',
        type: 'dashed',
        label: `重试失败项（${batchFailures.value.length}）`,
        disabled: batchFailures.value.length === 0,
        onClick: retryBatchFailures
      },
      {
        key: 'retry-retryable',
        type: 'dashed',
        label: `仅重试可重试（${retryableBatchFailures.value.length}）`,
        disabled: retryableBatchFailures.value.length === 0,
        onClick: retryRetryableBatchFailures
      },
      {
        key: 'view-failures',
        label: '查看失败明细',
        disabled: batchFailures.value.length === 0,
        onClick: () => { batchFailureDrawerOpen.value = true }
      },
      {
        key: 'export-failures',
        label: `导出失败明细（${batchFailures.value.length}）`,
        disabled: batchFailures.value.length === 0,
        onClick: exportBatchFailures
      },
      {
        key: 'export-receipt',
        label: '导出执行回执',
        disabled: !lastBatchReceipt.value,
        onClick: exportBatchReceipt
      },
      {
        key: 'copy-digest',
        label: '复制排障摘要',
        disabled: batchFailures.value.length === 0,
        onClick: copyBatchFailureDigest
      }
    ]
  }
  if (activeTab.value === 'maintenance') {
    return [
      {
        key: 'batch-maintenance',
        type: 'primary',
        label: `批量完成维修（${selectedMaintenanceKeys.value.length}）`,
        disabled: selectedMaintenanceKeys.value.length === 0,
        onClick: batchFinishMaintenance
      },
      {
        key: 'clear',
        label: '清空勾选',
        disabled: selectedMaintenanceKeys.value.length === 0,
        onClick: clearActiveTabSelection
      },
      {
        key: 'retry-failures',
        type: 'dashed',
        label: `重试失败项（${batchFailures.value.length}）`,
        disabled: batchFailures.value.length === 0,
        onClick: retryBatchFailures
      },
      {
        key: 'retry-retryable',
        type: 'dashed',
        label: `仅重试可重试（${retryableBatchFailures.value.length}）`,
        disabled: retryableBatchFailures.value.length === 0,
        onClick: retryRetryableBatchFailures
      },
      {
        key: 'view-failures',
        label: '查看失败明细',
        disabled: batchFailures.value.length === 0,
        onClick: () => { batchFailureDrawerOpen.value = true }
      },
      {
        key: 'export-failures',
        label: `导出失败明细（${batchFailures.value.length}）`,
        disabled: batchFailures.value.length === 0,
        onClick: exportBatchFailures
      },
      {
        key: 'export-receipt',
        label: '导出执行回执',
        disabled: !lastBatchReceipt.value,
        onClick: exportBatchReceipt
      },
      {
        key: 'copy-digest',
        label: '复制排障摘要',
        disabled: batchFailures.value.length === 0,
        onClick: copyBatchFailureDigest
      }
    ]
  }
  if (activeTab.value === 'delivery') {
    return [
      {
        key: 'batch-delivery',
        type: 'primary',
        label: `批量重派送餐（${selectedDeliveryKeys.value.length}）`,
        disabled: selectedDeliveryKeys.value.length === 0,
        onClick: batchRedispatchDelivery
      },
      {
        key: 'clear',
        label: '清空勾选',
        disabled: selectedDeliveryKeys.value.length === 0,
        onClick: clearActiveTabSelection
      },
      {
        key: 'retry-failures',
        type: 'dashed',
        label: `重试失败项（${batchFailures.value.length}）`,
        disabled: batchFailures.value.length === 0,
        onClick: retryBatchFailures
      },
      {
        key: 'retry-retryable',
        type: 'dashed',
        label: `仅重试可重试（${retryableBatchFailures.value.length}）`,
        disabled: retryableBatchFailures.value.length === 0,
        onClick: retryRetryableBatchFailures
      },
      {
        key: 'view-failures',
        label: '查看失败明细',
        disabled: batchFailures.value.length === 0,
        onClick: () => { batchFailureDrawerOpen.value = true }
      },
      {
        key: 'export-failures',
        label: `导出失败明细（${batchFailures.value.length}）`,
        disabled: batchFailures.value.length === 0,
        onClick: exportBatchFailures
      },
      {
        key: 'export-receipt',
        label: '导出执行回执',
        disabled: !lastBatchReceipt.value,
        onClick: exportBatchReceipt
      },
      {
        key: 'copy-digest',
        label: '复制排障摘要',
        disabled: batchFailures.value.length === 0,
        onClick: copyBatchFailureDigest
      }
    ]
  }
  return []
})

const cleaningColumns = [
  { title: '房间', dataIndex: 'roomNo', key: 'roomNo', width: 120 },
  { title: '保洁员', dataIndex: 'cleanerName', key: 'cleanerName', width: 120 },
  { title: '计划日期', dataIndex: 'planDate', key: 'planDate', width: 140 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '备注', dataIndex: 'remark', key: 'remark' },
  { title: '操作', key: 'action', width: 90 }
]

const maintenanceColumns = [
  { title: '房间', dataIndex: 'roomNo', key: 'roomNo', width: 120 },
  { title: '问题类型', dataIndex: 'issueType', key: 'issueType', width: 140 },
  { title: '报修人', dataIndex: 'reporterName', key: 'reporterName', width: 120 },
  { title: '处理人', dataIndex: 'assigneeName', key: 'assigneeName', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 110 },
  { title: '报修时间', dataIndex: 'reportedAt', key: 'reportedAt', width: 180 },
  { title: '操作', key: 'action', width: 90 }
]

const deliveryColumns = [
  { title: '订单号', dataIndex: 'orderNo', key: 'orderNo', width: 180 },
  { title: '送餐区域', dataIndex: 'deliveryAreaName', key: 'deliveryAreaName', width: 140 },
  { title: '配送员', dataIndex: 'deliveredByName', key: 'deliveredByName', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 110 },
  { title: '失败原因', dataIndex: 'failureReason', key: 'failureReason' },
  { title: '操作', key: 'action', width: 220 }
]

const inventoryColumns = [
  { title: '物资', dataIndex: 'productName', key: 'productName', width: 180 },
  { title: '仓库', dataIndex: 'warehouseName', key: 'warehouseName', width: 140 },
  { title: '盘点类型', dataIndex: 'inventoryType', key: 'inventoryType', width: 120 },
  { title: '调整类型', dataIndex: 'adjustType', key: 'adjustType', width: 110 },
  { title: '调整数量', dataIndex: 'adjustQty', key: 'adjustQty', width: 110 },
  { title: '时间', dataIndex: 'createTime', key: 'createTime', width: 180 }
]

function go(path: string) {
  router.push(path)
}

function normalizeStatus(status?: string) {
  return String(status || '').trim().toUpperCase()
}

function actionLogStorageKey() {
  const orgId = userStore.staffInfo?.orgId || 'unknown-org'
  const staffId = userStore.staffInfo?.id || 'unknown-staff'
  return `logistics-task-center-action-logs:${orgId}:${staffId}`
}

function loadActionLogs() {
  try {
    const raw = window.localStorage.getItem(actionLogStorageKey())
    const parsed = raw ? JSON.parse(raw) : []
    actionLogs.value = Array.isArray(parsed) ? parsed.slice(0, 20) : []
  } catch {
    actionLogs.value = []
  }
}

function saveActionLogs() {
  try {
    window.localStorage.setItem(actionLogStorageKey(), JSON.stringify(actionLogs.value.slice(0, 20)))
  } catch {}
}

function pushActionLog(action: string, detail: string, success: boolean) {
  actionLogs.value = [
    {
      id: `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
      time: dayjs().format('YYYY-MM-DD HH:mm:ss'),
      action,
      detail,
      success
    },
    ...actionLogs.value
  ].slice(0, 20)
  saveActionLogs()
}

async function finishCleaning(record: RoomCleaningTask) {
  try {
    await completeRoomCleaning(record.id)
    pushActionLog('清洁任务完成', `房间 ${record.roomNo || '-'}，任务ID ${record.id}`, true)
    message.success('清洁任务已完成')
    await loadData()
  } catch (error: any) {
    pushActionLog('清洁任务完成', `任务ID ${record.id}，失败：${error?.message || '未知错误'}`, false)
    message.error(error?.message || '清洁任务完成失败')
  }
}

async function batchFinishCleaning() {
  const rows = displayCleaningRows.value.filter((item) => selectedCleaningKeys.value.includes(item.id) && !isCleaningDone(item.status))
  if (rows.length === 0) {
    message.info('没有可批量完成的清洁任务')
    return
  }
  const startedAt = dayjs().format('YYYY-MM-DD HH:mm:ss')
  batchFailures.value = []
  startBatchProgress('批量完成清洁处理中', rows.length)
  const successIds: Array<number | string> = []
  const failedIds: Array<number | string> = []
  for (const row of rows) {
    try {
      await completeRoomCleaning(row.id)
      successIds.push(row.id)
      stepBatchProgress(true)
    } catch (error) {
      failedIds.push(row.id)
      stepBatchProgress(false)
      const detail = parseErrorDetail(error)
      batchFailures.value.push({
        at: dayjs().format('YYYY-MM-DD HH:mm:ss'),
        action: '批量完成清洁',
        itemId: row.id,
        reason: detail.reason,
        code: detail.code,
        path: detail.path,
        retryable: detail.retryable
      })
    }
  }
  finishBatchProgress()
  lastBatchReceipt.value = {
    action: '批量完成清洁',
    startAt: startedAt,
    finishAt: dayjs().format('YYYY-MM-DD HH:mm:ss'),
    total: rows.length,
    success: successIds.length,
    failed: failedIds.length
  }
  selectedCleaningKeys.value = failedIds
  await loadData({ preserveSelection: true })
  if (failedIds.length > 0) {
    pushActionLog('批量完成清洁', `成功 ${successIds.length} 条，失败 ${failedIds.length} 条`, false)
    message.warning(`批量完成清洁：成功 ${successIds.length}，失败 ${failedIds.length}（失败项已保留勾选）`)
    return
  }
  pushActionLog('批量完成清洁', `处理 ${successIds.length} 条`, true)
  message.success(`已批量完成${successIds.length}条清洁任务`)
}

async function finishMaintenance(record: MaintenanceRequest) {
  try {
    await completeMaintenance(record.id)
    pushActionLog('维修任务完成', `房间 ${record.roomNo || '-'}，任务ID ${record.id}`, true)
    message.success('维修任务已完成')
    await loadData()
  } catch (error: any) {
    pushActionLog('维修任务完成', `任务ID ${record.id}，失败：${error?.message || '未知错误'}`, false)
    message.error(error?.message || '维修任务完成失败')
  }
}

async function batchFinishMaintenance() {
  const rows = displayMaintenanceRows.value.filter((item) =>
    selectedMaintenanceKeys.value.includes(item.id) && normalizeStatus(item.status) !== 'COMPLETED'
  )
  if (rows.length === 0) {
    message.info('没有可批量完成的维修任务')
    return
  }
  const startedAt = dayjs().format('YYYY-MM-DD HH:mm:ss')
  batchFailures.value = []
  startBatchProgress('批量完成维修处理中', rows.length)
  const successIds: Array<number | string> = []
  const failedIds: Array<number | string> = []
  for (const row of rows) {
    try {
      await completeMaintenance(row.id)
      successIds.push(row.id)
      stepBatchProgress(true)
    } catch (error) {
      failedIds.push(row.id)
      stepBatchProgress(false)
      const detail = parseErrorDetail(error)
      batchFailures.value.push({
        at: dayjs().format('YYYY-MM-DD HH:mm:ss'),
        action: '批量完成维修',
        itemId: row.id,
        reason: detail.reason,
        code: detail.code,
        path: detail.path,
        retryable: detail.retryable
      })
    }
  }
  finishBatchProgress()
  lastBatchReceipt.value = {
    action: '批量完成维修',
    startAt: startedAt,
    finishAt: dayjs().format('YYYY-MM-DD HH:mm:ss'),
    total: rows.length,
    success: successIds.length,
    failed: failedIds.length
  }
  selectedMaintenanceKeys.value = failedIds
  await loadData({ preserveSelection: true })
  if (failedIds.length > 0) {
    pushActionLog('批量完成维修', `成功 ${successIds.length} 条，失败 ${failedIds.length} 条`, false)
    message.warning(`批量完成维修：成功 ${successIds.length}，失败 ${failedIds.length}（失败项已保留勾选）`)
    return
  }
  pushActionLog('批量完成维修', `处理 ${successIds.length} 条`, true)
  message.success(`已批量完成${successIds.length}条维修任务`)
}

function cleaningStatusLabel(record: RoomCleaningTask) {
  if (isCleaningDone(record.status)) return '已完成'
  const overdue = record.planDate && dayjs(record.planDate).isBefore(dayjs(), 'day')
  return overdue ? '待清洁（超时）' : '待清洁'
}

function cleaningStatusColor(record: RoomCleaningTask) {
  if (isCleaningDone(record.status)) return 'green'
  const overdue = record.planDate && dayjs(record.planDate).isBefore(dayjs(), 'day')
  return overdue ? 'red' : 'orange'
}

function isCleaningDone(status?: string) {
  const normalized = normalizeStatus(status)
  return normalized === 'DONE' || normalized === 'COMPLETED'
}

function maintenanceStatusLabel(record: MaintenanceRequest) {
  const status = normalizeStatus(record.status)
  if (status === 'COMPLETED') return '已完成'
  const overdue = record.reportedAt && dayjs(record.reportedAt).isBefore(dayjs().subtract(2, 'day'))
  if (status === 'PROCESSING') {
    return overdue ? '处理中（超时）' : '处理中'
  }
  return overdue ? '待处理（超时）' : '待处理'
}

function maintenanceStatusColor(record: MaintenanceRequest) {
  const status = normalizeStatus(record.status)
  if (status === 'COMPLETED') return 'green'
  const overdue = record.reportedAt && dayjs(record.reportedAt).isBefore(dayjs().subtract(2, 'day'))
  return overdue ? 'red' : (status === 'PROCESSING' ? 'blue' : 'orange')
}

function deliveryStatusLabel(record: DiningDeliveryRecord) {
  const status = normalizeStatus(record.status)
  if (status === 'DELIVERED') return '已送达'
  if (status === 'FAILED') return '送达失败'
  const refTime = record.deliveredAt || record.createTime
  const overdue = refTime && dayjs(refTime).isBefore(dayjs().subtract(2, 'hour'))
  return overdue ? '待送达（超时）' : '待送达'
}

function deliveryStatusColor(record: DiningDeliveryRecord) {
  const status = normalizeStatus(record.status)
  if (status === 'DELIVERED') return 'green'
  if (status === 'FAILED') return 'red'
  const refTime = record.deliveredAt || record.createTime
  const overdue = refTime && dayjs(refTime).isBefore(dayjs().subtract(2, 'hour'))
  return overdue ? 'volcano' : 'gold'
}

async function finishDelivery(record: DiningDeliveryRecord) {
  try {
    await updateDiningDeliveryRecord(record.id, {
      mealOrderId: record.mealOrderId,
      orderNo: record.orderNo,
      deliveryAreaId: record.deliveryAreaId,
      deliveryAreaName: record.deliveryAreaName,
      deliveredByName: record.deliveredByName,
      status: 'DELIVERED',
      failureReason: undefined,
      remark: record.remark
    })
    pushActionLog('送餐标记送达', `订单 ${record.orderNo || '-'}，记录ID ${record.id}`, true)
    message.success('送餐任务已标记送达')
    await loadData()
  } catch (error: any) {
    pushActionLog('送餐标记送达', `记录ID ${record.id}，失败：${error?.message || '未知错误'}`, false)
    message.error(error?.message || '标记送达失败')
  }
}

async function markDeliveryFailed(record: DiningDeliveryRecord) {
  try {
    const reason = window.prompt('请输入失败原因', record.failureReason || '')
    if (reason === null) {
      return
    }
    const failureReason = reason.trim()
    if (!failureReason) {
      message.warning('失败原因不能为空')
      return
    }
    await updateDiningDeliveryRecord(record.id, {
      mealOrderId: record.mealOrderId,
      orderNo: record.orderNo,
      deliveryAreaId: record.deliveryAreaId,
      deliveryAreaName: record.deliveryAreaName,
      deliveredByName: record.deliveredByName,
      status: 'FAILED',
      failureReason,
      remark: record.remark
    })
    pushActionLog('送餐标记失败', `订单 ${record.orderNo || '-'}，原因：${failureReason}`, true)
    message.success('送餐任务已标记失败')
    await loadData()
  } catch (error: any) {
    pushActionLog('送餐标记失败', `记录ID ${record.id}，失败：${error?.message || '未知错误'}`, false)
    message.error(error?.message || '标记失败失败')
  }
}

async function redispatchDelivery(record: DiningDeliveryRecord) {
  try {
    await redispatchDiningDeliveryRecord(record.id, {})
    pushActionLog('送餐重派', `订单 ${record.orderNo || '-'}，记录ID ${record.id}`, true)
    message.success('送餐任务已重派')
    await loadData()
  } catch (error: any) {
    pushActionLog('送餐重派', `记录ID ${record.id}，失败：${error?.message || '未知错误'}`, false)
    message.error(error?.message || '重派失败')
  }
}

async function batchRedispatchDelivery() {
  const rows = displayDeliveryRows.value.filter((item) =>
    selectedDeliveryKeys.value.includes(item.id) && normalizeStatus(item.status) !== 'DELIVERED'
  )
  if (rows.length === 0) {
    message.info('没有可批量重派的送餐任务')
    return
  }
  const startedAt = dayjs().format('YYYY-MM-DD HH:mm:ss')
  batchFailures.value = []
  startBatchProgress('批量重派送餐处理中', rows.length)
  const successIds: Array<number | string> = []
  const failedIds: Array<number | string> = []
  for (const row of rows) {
    try {
      await redispatchDiningDeliveryRecord(row.id, {})
      successIds.push(row.id)
      stepBatchProgress(true)
    } catch (error) {
      failedIds.push(row.id)
      stepBatchProgress(false)
      const detail = parseErrorDetail(error)
      batchFailures.value.push({
        at: dayjs().format('YYYY-MM-DD HH:mm:ss'),
        action: '批量重派送餐',
        itemId: row.id,
        reason: detail.reason,
        code: detail.code,
        path: detail.path,
        retryable: detail.retryable
      })
    }
  }
  finishBatchProgress()
  lastBatchReceipt.value = {
    action: '批量重派送餐',
    startAt: startedAt,
    finishAt: dayjs().format('YYYY-MM-DD HH:mm:ss'),
    total: rows.length,
    success: successIds.length,
    failed: failedIds.length
  }
  selectedDeliveryKeys.value = failedIds
  await loadData({ preserveSelection: true })
  if (failedIds.length > 0) {
    pushActionLog('批量重派送餐', `成功 ${successIds.length} 条，失败 ${failedIds.length} 条`, false)
    message.warning(`批量重派送餐：成功 ${successIds.length}，失败 ${failedIds.length}（失败项已保留勾选）`)
    return
  }
  pushActionLog('批量重派送餐', `处理 ${successIds.length} 条`, true)
  message.success(`已批量重派${successIds.length}条送餐任务`)
}

function exportCurrentTab() {
  if (activeTab.value === 'cleaning') {
    exportCsv(
      displayCleaningRows.value.map((item) => ({
        房间: item.roomNo || '',
        保洁员: item.cleanerName || '',
        计划日期: item.planDate || '',
        状态: item.status || '',
        备注: item.remark || ''
      })),
      `后勤任务-清洁-${dayjs().format('YYYYMMDD-HHmm')}.csv`
    )
    return
  }
  if (activeTab.value === 'maintenance') {
    exportCsv(
      displayMaintenanceRows.value.map((item) => ({
        房间: item.roomNo || '',
        问题类型: item.issueType || '',
        报修人: item.reporterName || '',
        处理人: item.assigneeName || '',
        状态: item.status || '',
        报修时间: item.reportedAt || ''
      })),
      `后勤任务-维修-${dayjs().format('YYYYMMDD-HHmm')}.csv`
    )
    return
  }
  if (activeTab.value === 'delivery') {
    exportCsv(
      displayDeliveryRows.value.map((item) => ({
        订单号: item.orderNo || '',
        送餐区域: item.deliveryAreaName || '',
        配送员: item.deliveredByName || '',
        状态: item.status || '',
        失败原因: item.failureReason || ''
      })),
      `后勤任务-送餐-${dayjs().format('YYYYMMDD-HHmm')}.csv`
    )
    return
  }
  exportCsv(
    displayAdjustmentRows.value.map((item) => ({
      物资: item.productName || '',
      仓库: item.warehouseName || '',
      盘点类型: item.inventoryType || '',
      调整类型: item.adjustType || '',
      调整数量: item.adjustQty || 0,
      时间: item.createTime || ''
    })),
    `后勤任务-盘点-${dayjs().format('YYYYMMDD-HHmm')}.csv`
  )
}

function exportActionLogs() {
  exportCsv(
    actionLogs.value.map((item) => ({
      时间: item.time,
      动作: item.action,
      详情: item.detail,
      结果: item.success ? '成功' : '失败'
    })),
    `后勤任务-操作日志-${dayjs().format('YYYYMMDD-HHmm')}.csv`
  )
}

async function loadData(options?: { preserveSelection?: boolean }) {
  loading.value = true
  errorMessage.value = ''
  const today = dayjs().format('YYYY-MM-DD')
  const preserveSelection = Boolean(options?.preserveSelection)
  try {
    const [summaryData, cleaningPage, maintenancePage, deliveryPage, adjustmentPage] = await Promise.all([
      getLogisticsWorkbenchSummary(),
      getRoomCleaningPage({ pageNo: 1, pageSize: 500 }),
      getMaintenancePage({ pageNo: 1, pageSize: 500, dateFrom: today, dateTo: today }),
      getDiningDeliveryRecordPage({ pageNo: 1, pageSize: 500, dateFrom: today, dateTo: today }),
      getInventoryAdjustmentPage({ pageNo: 1, pageSize: 200, dateFrom: today, dateTo: today })
    ])
    summary.value = summaryData
    cleaningRows.value = cleaningPage?.list || []
    maintenanceRows.value = maintenancePage?.list || []
    deliveryRows.value = deliveryPage?.list || []
    adjustmentRows.value = adjustmentPage?.list || []
    if (preserveSelection) {
      const cleaningSet = new Set(cleaningRows.value.map((item) => String(item.id)))
      selectedCleaningKeys.value = selectedCleaningKeys.value.filter((id) => cleaningSet.has(String(id)))
      const maintenanceSet = new Set(maintenanceRows.value.map((item) => String(item.id)))
      selectedMaintenanceKeys.value = selectedMaintenanceKeys.value.filter((id) => maintenanceSet.has(String(id)))
      const deliverySet = new Set(deliveryRows.value.map((item) => String(item.id)))
      selectedDeliveryKeys.value = selectedDeliveryKeys.value.filter((id) => deliverySet.has(String(id)))
    } else {
      selectedCleaningKeys.value = []
      selectedMaintenanceKeys.value = []
      selectedDeliveryKeys.value = []
    }
  } catch (error: any) {
    errorMessage.value = error?.message || '加载后勤任务中心失败'
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

let refreshTimer: number | undefined
onMounted(() => {
  const tab = String(route.query.tab || '')
  if (['cleaning', 'maintenance', 'delivery', 'inventory'].includes(tab)) {
    activeTab.value = tab
  }
  loadActionLogs()
  loadData()
  refreshTimer = window.setInterval(loadData, 60000)
})
onUnmounted(() => {
  if (refreshTimer) {
    window.clearInterval(refreshTimer)
  }
})
</script>

<style scoped>
.metric-value {
  font-size: 24px;
  font-weight: 600;
  color: #262626;
  line-height: 1.4;
}

.metric-line {
  margin-top: 4px;
  color: #595959;
}

.duty-dense :deep(.ant-card-body) {
  padding: 10px 12px;
}

.duty-dense .metric-value {
  font-size: 20px;
}

.duty-dense :deep(.ant-tabs-nav) {
  margin-bottom: 8px;
}

.duty-dense :deep(.ant-table-cell) {
  padding-top: 6px !important;
  padding-bottom: 6px !important;
}
</style>
