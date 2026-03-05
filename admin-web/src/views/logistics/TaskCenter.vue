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
          <a-button
            v-if="activeTab === 'cleaning'"
            :disabled="selectedCleaningKeys.length === 0"
            @click="batchFinishCleaning"
          >
            批量完成清洁（{{ selectedCleaningKeys.length }}）
          </a-button>
          <a-button
            v-if="activeTab === 'maintenance'"
            :disabled="selectedMaintenanceKeys.length === 0"
            @click="batchFinishMaintenance"
          >
            批量完成维修（{{ selectedMaintenanceKeys.length }}）
          </a-button>
          <a-button
            v-if="activeTab === 'delivery'"
            :disabled="selectedDeliveryKeys.length === 0"
            @click="batchRedispatchDelivery"
          >
            批量重派送餐（{{ selectedDeliveryKeys.length }}）
          </a-button>
          <a-button type="primary" @click="loadData">刷新任务数据</a-button>
        </a-space>
      </a-card>

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
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
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
  try {
    await Promise.all(rows.map((item) => completeRoomCleaning(item.id)))
    pushActionLog('批量完成清洁', `处理 ${rows.length} 条`, true)
    message.success(`已批量完成${rows.length}条清洁任务`)
    selectedCleaningKeys.value = []
    await loadData()
  } catch (error: any) {
    pushActionLog('批量完成清洁', `失败：${error?.message || '未知错误'}`, false)
    message.error(error?.message || '批量完成清洁失败')
  }
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
  try {
    await Promise.all(rows.map((item) => completeMaintenance(item.id)))
    pushActionLog('批量完成维修', `处理 ${rows.length} 条`, true)
    message.success(`已批量完成${rows.length}条维修任务`)
    selectedMaintenanceKeys.value = []
    await loadData()
  } catch (error: any) {
    pushActionLog('批量完成维修', `失败：${error?.message || '未知错误'}`, false)
    message.error(error?.message || '批量完成维修失败')
  }
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
  try {
    await Promise.all(rows.map((item) => redispatchDiningDeliveryRecord(item.id, {})))
    pushActionLog('批量重派送餐', `处理 ${rows.length} 条`, true)
    message.success(`已批量重派${rows.length}条送餐任务`)
    selectedDeliveryKeys.value = []
    await loadData()
  } catch (error: any) {
    pushActionLog('批量重派送餐', `失败：${error?.message || '未知错误'}`, false)
    message.error(error?.message || '批量重派失败')
  }
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

async function loadData() {
  loading.value = true
  errorMessage.value = ''
  const today = dayjs().format('YYYY-MM-DD')
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
    selectedCleaningKeys.value = []
    selectedMaintenanceKeys.value = []
    selectedDeliveryKeys.value = []
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
