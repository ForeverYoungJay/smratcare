<template>
  <PageContainer title="入住状态变更" subTitle="外出/来访/试住/退住/外出就医/死亡统一入口，自动触发跨部门任务包">
    <template #extra>
      <a-space wrap>
        <a-tag v-if="lastUpdatedAt" color="blue">更新时间 {{ lastUpdatedAt }}</a-tag>
        <a-switch v-model:checked="alertOnly" checked-children="仅异常状态" un-checked-children="全部状态" />
        <a-switch v-model:checked="autoRefresh" checked-children="自动刷新" un-checked-children="手动刷新" />
        <a-button @click="fetchRealStats">刷新</a-button>
      </a-space>
    </template>
    <a-alert type="error" show-icon :message="pendingAlertMessage" style="margin-bottom: 16px" />
    <LifecycleStageBar
      title="状态变更闭环阶段"
      :subject="statusLifecycleSubject"
      :stage="statusLifecycleStage"
      :steps="statusLifecycleSteps"
      :generated-at="lastUpdatedAtRaw"
      :hint="statusLifecycleHint"
      style="margin-bottom: 16px"
    />

    <StatefulBlock :loading="loading" :error="errorMessage" @retry="fetchRealStats">
      <a-row :gutter="16" style="margin-bottom: 16px">
        <a-col v-for="item in visibleStatusItems" :key="item.status" :xs="12" :sm="8" :md="6" :lg="4" style="margin-bottom: 12px">
          <a-card class="card-elevated" :bordered="false" hoverable @click="go(item.path)">
            <a-statistic :title="item.status" :value="item.count" />
          </a-card>
        </a-col>
      </a-row>
    </StatefulBlock>

    <a-card title="跨部门联动回执" class="card-elevated" :bordered="false" style="margin-bottom: 16px">
      <StatefulBlock :loading="linkageLoading" :error="linkageErrorMessage" @retry="fetchLinkageReceipts">
        <a-alert
          v-if="linkageNoticeMessage"
          type="warning"
          show-icon
          style="margin-bottom: 10px"
          :message="linkageNoticeMessage"
        />
        <div class="receipt-head">
          <span>当前联动待办总量 {{ totalLinkagePendingCount }}</span>
          <span class="receipt-meta">回执更新时间 {{ linkageUpdatedAtText }}</span>
        </div>
        <a-row :gutter="[12, 12]">
          <a-col v-for="item in linkageReceiptCards" :key="item.key" :xs="24" :md="8">
            <div class="receipt-tile receipt-clickable" @click="openReceiptDrawer(item.key)">
              <div class="receipt-title-row">
                <span class="receipt-title">{{ item.title }}</span>
                <a-tag :color="item.riskCount > 0 ? 'red' : 'green'">
                  {{ item.riskCount > 0 ? `风险 ${item.riskCount}` : '已平稳' }}
                </a-tag>
              </div>
              <div class="receipt-main-value">{{ item.pendingCount }}</div>
              <div class="receipt-desc">{{ item.description }}</div>
              <div class="receipt-mini-metrics">
                <span v-for="metric in item.metrics" :key="`${item.key}-${metric.label}`">{{ metric.label }} {{ metric.value }}</span>
              </div>
            </div>
          </a-col>
        </a-row>
      </StatefulBlock>
    </a-card>

    <a-card title="状态变更动作" class="card-elevated" :bordered="false">
      <a-space wrap>
        <a-button type="primary" @click="go('/elder/outing')">外出登记</a-button>
        <a-button @click="go('/elder/visit-register')">来访登记</a-button>
        <a-button @click="go('/elder/trial-stay')">试住登记</a-button>
        <a-button danger @click="go('/elder/discharge-apply')">退住申请</a-button>
        <a-button @click="go('/elder/medical-outing')">外出就医登记</a-button>
        <a-button danger @click="go('/elder/death-register')">死亡登记</a-button>
      </a-space>

      <a-divider />
      <a-typography-paragraph>
        触发规则：提交状态变更后自动推送护工/医护/财务/后勤消息，生成任务包（退住交接、事故处置等），并按规则调整费用、床位与餐饮计划。
      </a-typography-paragraph>
    </a-card>

    <a-card title="跨部门联动入口" class="card-elevated" :bordered="false" style="margin-top: 16px">
      <a-row :gutter="[12, 12]">
        <a-col :xs="24" :md="8">
          <a-card
            size="small"
            class="linkage-tile"
            @click="go('/oa/work-execution/task?source=lifecycle&scene=status-change&category=alert&quick=overdue&status=OPEN')"
          >
            <div class="linkage-title">综合任务中心</div>
            <div class="linkage-desc">查看状态变更触发的待办与告警任务</div>
          </a-card>
        </a-col>
        <a-col :xs="24" :md="8">
          <a-card
            size="small"
            class="linkage-tile"
            @click="go('/medical-care/unified-task-center?source=lifecycle&scene=status-change&onlyOverdue=1&sortBy=RISK_SCORE&sortDirection=DESC')"
          >
            <div class="linkage-title">医护任务中心</div>
            <div class="linkage-desc">就医外出、死亡登记后自动联动医护处置</div>
          </a-card>
        </a-col>
        <a-col :xs="24" :md="8">
          <a-card
            size="small"
            class="linkage-tile"
            @click="go('/logistics/task-center?source=lifecycle&scene=status-change&view=DUTY&tab=maintenance&lifecycleFocus=maintenance&overdueOnly=1')"
          >
            <div class="linkage-title">后勤任务中心</div>
            <div class="linkage-desc">退住交接、床位清洁与维修任务自动同步</div>
          </a-card>
        </a-col>
      </a-row>
    </a-card>

    <a-drawer
      v-model:open="receiptDrawerOpen"
      :title="activeReceiptTitle"
      :width="460"
      placement="right"
      destroy-on-close
    >
      <a-space direction="vertical" style="width: 100%">
        <a-alert :type="activeReceiptRiskCount > 0 ? 'warning' : 'success'" show-icon :message="activeReceiptDescription" />
        <a-list bordered size="small" :data-source="activeReceiptDetailMetrics">
          <template #renderItem="{ item }">
            <a-list-item>
              <span>{{ item.label }}</span>
              <a-tag :color="item.highlight ? 'red' : 'blue'">{{ item.value }}</a-tag>
            </a-list-item>
          </template>
        </a-list>
        <div class="receipt-drawer-actions">
          <a-button
            v-for="action in activeReceiptActions"
            :key="action.label"
            block
            @click="go(action.path)"
          >
            {{ action.label }}
          </a-button>
        </div>
      </a-space>
    </a-drawer>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import PageContainer from '../../../components/PageContainer.vue'
import StatefulBlock from '../../../components/StatefulBlock.vue'
import LifecycleStageBar from '../../../components/LifecycleStageBar.vue'
import { getResidenceStatusSummary } from '../../../api/elderResidence'
import { getPortalSummary } from '../../../api/oa'
import { getMedicalCareWorkbenchSummary } from '../../../api/medicalCare'
import { getLogisticsWorkbenchSummary } from '../../../api/logistics'
import { useLiveSyncRefresh } from '../../../composables/useLiveSyncRefresh'
import { formatChineseDateTime } from '../../../utils/dateLocale'
import type {
  LogisticsWorkbenchSummary,
  MedicalCareWorkbenchSummary,
  OaPortalSummary,
  ResidenceStatusSummary
} from '../../../types'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const errorMessage = ref('')
const alertOnly = ref(false)
const autoRefresh = ref(true)
const lastUpdatedAt = ref('')
const lastUpdatedAtRaw = ref('')
const statusRouteSignature = ref('')
const skipNextStatusRouteWatch = ref(false)
const STATUS_ROUTE_KEYS = ['statusAlertOnly', 'statusAuto'] as const
const statusLifecycleSteps: Array<{
  key: 'PENDING_ASSESSMENT' | 'PENDING_BED_SELECT' | 'PENDING_SIGN' | 'SIGNED'
  label: string
  description: string
}> = [
  { key: 'PENDING_ASSESSMENT', label: '状态监测', description: '监测外出、退住、异常事件变化' },
  { key: 'PENDING_BED_SELECT', label: '任务触发', description: '状态变更后推送OA/医护/后勤任务' },
  { key: 'PENDING_SIGN', label: '跨部门处置', description: '处理逾期与风险任务，避免服务中断' },
  { key: 'SIGNED', label: '闭环完成', description: '风险清零并同步账单、床位与服务计划' }
]
const statusItems = ref([
  { status: '意向', count: 0, path: '/elder/list' },
  { status: '试住', count: 0, path: '/elder/trial-stay' },
  { status: '在住', count: 0, path: '/elder/list?status=1' },
  { status: '外出', count: 0, path: '/elder/outing?status=OUT' },
  { status: '外出就医', count: 0, path: '/elder/medical-outing' },
  { status: '退住办理中', count: 0, path: '/elder/discharge-apply?status=PENDING' },
  { status: '已退住', count: 0, path: '/elder/list?status=3' },
  { status: '死亡', count: 0, path: '/elder/death-register' }
])

const pendingStats = ref({
  openIncidentCount: 0,
  pendingDischargeCount: 0,
  overdueOutingCount: 0
})
const linkageLoading = ref(false)
const linkageErrorMessage = ref('')
const linkageNoticeMessage = ref('')
const linkageUpdatedAt = ref('')
const receiptDrawerOpen = ref(false)
const activeReceiptKey = ref<'oa' | 'medical' | 'logistics'>('oa')
const linkageSnapshot = ref({
  oa: {
    openTodoCount: 0,
    pendingApprovalCount: 0,
    ongoingTaskCount: 0,
    overdueTodoCount: 0,
    incidentOpenCount: 0
  },
  medical: {
    pendingCareTaskCount: 0,
    pendingMedicalOrderCount: 0,
    todayInspectionPendingCount: 0,
    overdueCareTaskCount: 0,
    abnormalInspectionOpenCount: 0,
    incidentOpenCount: 0
  },
  logistics: {
    todayTaskCount: 0,
    maintenancePendingCount: 0,
    maintenanceOverdueCount: 0,
    undeliveredCount: 0
  }
})

const pendingAlertMessage = computed(
  () =>
    `待处理事件：未闭环事故 ${pendingStats.value.openIncidentCount}、待审批退住 ${pendingStats.value.pendingDischargeCount}、外出超时未返院 ${pendingStats.value.overdueOutingCount}`
)
const visibleStatusItems = computed(() => {
  if (!alertOnly.value) return statusItems.value
  const preferred = new Set(['外出', '外出就医', '退住办理中', '死亡'])
  const filtered = statusItems.value.filter((item) => Number(item.count || 0) > 0 || preferred.has(item.status))
  return filtered.length ? filtered : statusItems.value
})
const linkageUpdatedAtText = computed(() => {
  const raw = String(linkageUpdatedAt.value || '').trim()
  if (!raw) return '-'
  return formatChineseDateTime(raw, '-')
})
const linkageReceiptCards = computed(() => {
  const oaPending = linkageSnapshot.value.oa.openTodoCount + linkageSnapshot.value.oa.pendingApprovalCount + linkageSnapshot.value.oa.ongoingTaskCount
  const oaRisk = linkageSnapshot.value.oa.overdueTodoCount + linkageSnapshot.value.oa.incidentOpenCount
  const medicalPending = linkageSnapshot.value.medical.pendingCareTaskCount
    + linkageSnapshot.value.medical.pendingMedicalOrderCount
    + linkageSnapshot.value.medical.todayInspectionPendingCount
  const medicalRisk = linkageSnapshot.value.medical.overdueCareTaskCount
    + linkageSnapshot.value.medical.abnormalInspectionOpenCount
    + linkageSnapshot.value.medical.incidentOpenCount
  const logisticsPending = linkageSnapshot.value.logistics.todayTaskCount + linkageSnapshot.value.logistics.maintenancePendingCount
  const logisticsRisk = linkageSnapshot.value.logistics.maintenanceOverdueCount + linkageSnapshot.value.logistics.undeliveredCount
  return [
    {
      key: 'oa',
      title: 'OA协同',
      pendingCount: oaPending,
      riskCount: oaRisk,
      description: '任务待办、审批与工单协同',
      metrics: [
        { label: '待办', value: linkageSnapshot.value.oa.openTodoCount },
        { label: '待审批', value: linkageSnapshot.value.oa.pendingApprovalCount },
        { label: '进行中', value: linkageSnapshot.value.oa.ongoingTaskCount }
      ]
    },
    {
      key: 'medical',
      title: '医护处置',
      pendingCount: medicalPending,
      riskCount: medicalRisk,
      description: '护理、巡检、用药任务处置',
      metrics: [
        { label: '护理待办', value: linkageSnapshot.value.medical.pendingCareTaskCount },
        { label: '用药待审', value: linkageSnapshot.value.medical.pendingMedicalOrderCount },
        { label: '巡检待办', value: linkageSnapshot.value.medical.todayInspectionPendingCount }
      ]
    },
    {
      key: 'logistics',
      title: '后勤执行',
      pendingCount: logisticsPending,
      riskCount: logisticsRisk,
      description: '清洁、维修、配送任务执行',
      metrics: [
        { label: '今日任务', value: linkageSnapshot.value.logistics.todayTaskCount },
        { label: '维修待办', value: linkageSnapshot.value.logistics.maintenancePendingCount },
        { label: '配送未完成', value: linkageSnapshot.value.logistics.undeliveredCount }
      ]
    }
  ]
})
const totalLinkagePendingCount = computed(() =>
  linkageReceiptCards.value.reduce((sum, item) => sum + item.pendingCount, 0)
)
const totalLinkageRiskCount = computed(() =>
  linkageReceiptCards.value.reduce((sum, item) => sum + item.riskCount, 0)
)
const statusLifecycleStage = computed(() => {
  const hasAnyCount = statusItems.value.some((item) => Number(item.count || 0) > 0)
  if (!hasAnyCount && !lastUpdatedAtRaw.value) return 'PENDING_ASSESSMENT'
  if (totalLinkageRiskCount.value > 0 || pendingStats.value.openIncidentCount > 0 || pendingStats.value.overdueOutingCount > 0) {
    return 'PENDING_SIGN'
  }
  if (totalLinkagePendingCount.value > 0 || pendingStats.value.pendingDischargeCount > 0) {
    return 'PENDING_BED_SELECT'
  }
  return 'SIGNED'
})
const statusLifecycleSubject = computed(
  () => `当前状态池：在住 ${findStatusCount('在住')} / 外出 ${findStatusCount('外出')} / 退住办理中 ${findStatusCount('退住办理中')}`
)
const statusLifecycleHint = computed(() => {
  if (statusLifecycleStage.value === 'PENDING_SIGN') {
    return `当前存在高优风险 ${totalLinkageRiskCount.value} 项，建议优先处理跨部门逾期任务。`
  }
  if (statusLifecycleStage.value === 'PENDING_BED_SELECT') {
    return `当前仍有待办 ${totalLinkagePendingCount.value} 项，建议同步查看OA/医护/后勤任务中心。`
  }
  if (statusLifecycleStage.value === 'SIGNED') {
    return '当前状态变更任务已基本闭环，可持续监测新增事件。'
  }
  return '等待状态数据加载完成后自动评估闭环阶段。'
})
const activeReceiptRecord = computed(() =>
  linkageReceiptCards.value.find((item) => item.key === activeReceiptKey.value) || linkageReceiptCards.value[0]
)
const activeReceiptTitle = computed(() =>
  activeReceiptRecord.value ? `${activeReceiptRecord.value.title} - 联动明细` : '联动明细'
)
const activeReceiptDescription = computed(() =>
  activeReceiptRecord.value?.description || '查看跨部门联动回执明细'
)
const activeReceiptRiskCount = computed(() => Number(activeReceiptRecord.value?.riskCount || 0))
const activeReceiptDetailMetrics = computed(() => {
  if (!activeReceiptRecord.value) return []
  const base = activeReceiptRecord.value.metrics.map((item) => ({ label: item.label, value: item.value, highlight: false }))
  if (activeReceiptRecord.value.key === 'oa') {
    return [
      ...base,
      { label: '逾期待办', value: linkageSnapshot.value.oa.overdueTodoCount, highlight: linkageSnapshot.value.oa.overdueTodoCount > 0 },
      { label: '未闭环事故', value: linkageSnapshot.value.oa.incidentOpenCount, highlight: linkageSnapshot.value.oa.incidentOpenCount > 0 }
    ]
  }
  if (activeReceiptRecord.value.key === 'medical') {
    return [
      ...base,
      { label: '护理超时', value: linkageSnapshot.value.medical.overdueCareTaskCount, highlight: linkageSnapshot.value.medical.overdueCareTaskCount > 0 },
      { label: '异常巡检未闭环', value: linkageSnapshot.value.medical.abnormalInspectionOpenCount, highlight: linkageSnapshot.value.medical.abnormalInspectionOpenCount > 0 },
      { label: '事故处理中', value: linkageSnapshot.value.medical.incidentOpenCount, highlight: linkageSnapshot.value.medical.incidentOpenCount > 0 }
    ]
  }
  return [
    ...base,
    { label: '维修逾期', value: linkageSnapshot.value.logistics.maintenanceOverdueCount, highlight: linkageSnapshot.value.logistics.maintenanceOverdueCount > 0 },
    { label: '配送未完成', value: linkageSnapshot.value.logistics.undeliveredCount, highlight: linkageSnapshot.value.logistics.undeliveredCount > 0 }
  ]
})
const activeReceiptActions = computed(() => {
  if (activeReceiptKey.value === 'oa') {
    return [
      {
        label: '打开OA任务中心',
        path: '/oa/work-execution/task?source=lifecycle&scene=status-change&status=OPEN&category=alert&quick=overdue'
      },
      {
        label: '打开OA审批中心',
        path: '/oa/approval?source=lifecycle&scene=status-change&scope=PENDING_REVIEW&status=PENDING&overdue=1'
      },
      {
        label: '打开OA待办中心',
        path: '/oa/todo?source=lifecycle&scene=status-change&status=OPEN&quick=overdue'
      }
    ]
  }
  if (activeReceiptKey.value === 'medical') {
    return [
      {
        label: '打开医护统一任务',
        path: '/medical-care/unified-task-center?source=lifecycle&scene=status-change&module=INSPECTION&onlyOverdue=1&sortBy=RISK_SCORE&sortDirection=DESC'
      },
      { label: '打开医护工作台', path: '/medical-care/workbench?source=lifecycle&scene=status-change' },
      { label: '打开医护服务中心', path: '/medical-care/center?source=lifecycle&scene=status-change' }
    ]
  }
  return [
    {
      label: '打开后勤任务中心',
      path: '/logistics/task-center?source=lifecycle&scene=status-change&view=DUTY&tab=maintenance&lifecycleFocus=maintenance&overdueOnly=1'
    },
    {
      label: '打开后勤工作台',
      path: '/logistics/workbench?source=lifecycle&scene=status-change&focus=maintenance'
    },
    { label: '打开房态图', path: '/logistics/assets/room-state-map?source=lifecycle&scene=status-change' }
  ]
})
let refreshTimer: number | null = null

function go(path: string) {
  receiptDrawerOpen.value = false
  router.push(path)
}

function findStatusCount(status: string) {
  const matched = statusItems.value.find((item) => item.status === status)
  return Number(matched?.count || 0)
}

function openReceiptDrawer(key: 'oa' | 'medical' | 'logistics') {
  activeReceiptKey.value = key
  receiptDrawerOpen.value = true
}

function firstRouteQueryText(value: unknown) {
  if (Array.isArray(value)) return firstRouteQueryText(value[0])
  if (value == null) return ''
  return String(value).trim()
}

function flattenRouteQuery(source: Record<string, unknown>) {
  return Object.entries(source || {}).reduce<Record<string, string>>((acc, [key, value]) => {
    const text = firstRouteQueryText(value)
    if (!text) return acc
    acc[key] = text
    return acc
  }, {})
}

function buildStatusRouteSignature(source: Record<string, unknown>) {
  return [...STATUS_ROUTE_KEYS]
    .sort()
    .map((key) => `${key}:${firstRouteQueryText(source[key])}`)
    .join('|')
}

function applyStatusQueryFromRoute() {
  alertOnly.value = firstRouteQueryText(route.query.statusAlertOnly) === '1'
  autoRefresh.value = firstRouteQueryText(route.query.statusAuto) !== '0'
}

function buildStatusRouteQuery() {
  const nextQuery: Record<string, string> = {}
  Object.entries(flattenRouteQuery(route.query as Record<string, unknown>)).forEach(([key, value]) => {
    if ((STATUS_ROUTE_KEYS as readonly string[]).includes(key)) return
    nextQuery[key] = value
  })
  nextQuery.statusAuto = autoRefresh.value ? '1' : '0'
  if (alertOnly.value) nextQuery.statusAlertOnly = '1'
  return nextQuery
}

function hasSameStatusRouteQuery(nextQuery: Record<string, string>) {
  const currentQuery = flattenRouteQuery(route.query as Record<string, unknown>)
  const currentKeys = Object.keys(currentQuery)
  const nextKeys = Object.keys(nextQuery)
  if (currentKeys.length !== nextKeys.length) return false
  return nextKeys.every((key) => currentQuery[key] === nextQuery[key])
}

async function syncStatusQueryToRoute() {
  const nextQuery = buildStatusRouteQuery()
  if (hasSameStatusRouteQuery(nextQuery)) return
  skipNextStatusRouteWatch.value = true
  statusRouteSignature.value = buildStatusRouteSignature(nextQuery)
  await router.replace({ path: route.path, query: nextQuery })
}

function clearAutoRefresh() {
  if (refreshTimer !== null) {
    window.clearInterval(refreshTimer)
    refreshTimer = null
  }
}

function setupAutoRefresh() {
  clearAutoRefresh()
  if (!autoRefresh.value) return
  refreshTimer = window.setInterval(() => {
    if (loading.value || document.visibilityState !== 'visible') return
    fetchRealStats()
  }, 60000)
}

function toNumber(value: unknown) {
  const num = Number(value || 0)
  return Number.isFinite(num) ? num : 0
}

function pickLatestTimestamp(candidates: Array<string | undefined>) {
  return candidates.reduce((latest, current) => {
    const latestTime = latest ? new Date(latest).getTime() : 0
    const currentTime = current ? new Date(current).getTime() : 0
    if (!Number.isFinite(currentTime) || currentTime <= 0) {
      return latest
    }
    if (!Number.isFinite(latestTime) || currentTime > latestTime) {
      return current || latest
    }
    return latest
  }, '')
}

async function fetchLinkageReceipts(options?: { silent?: boolean }) {
  const silent = Boolean(options?.silent)
  if (!silent) {
    linkageLoading.value = true
  }
  linkageErrorMessage.value = ''
  linkageNoticeMessage.value = ''
  try {
    const [oaResult, medicalResult, logisticsResult] = await Promise.allSettled([
      getPortalSummary(),
      getMedicalCareWorkbenchSummary({ topResidentLimit: 5 }),
      getLogisticsWorkbenchSummary({ lite: true })
    ])

    const errors: string[] = []
    if (oaResult.status === 'fulfilled') {
      const data = oaResult.value as OaPortalSummary
      linkageSnapshot.value.oa = {
        openTodoCount: toNumber(data?.openTodoCount),
        pendingApprovalCount: toNumber(data?.pendingApprovalCount),
        ongoingTaskCount: toNumber(data?.ongoingTaskCount),
        overdueTodoCount: toNumber(data?.overdueTodoCount),
        incidentOpenCount: toNumber(data?.incidentOpenCount)
      }
    } else {
      errors.push('OA回执失败')
    }

    if (medicalResult.status === 'fulfilled') {
      const data = medicalResult.value as MedicalCareWorkbenchSummary
      linkageSnapshot.value.medical = {
        pendingCareTaskCount: toNumber(data?.pendingCareTaskCount),
        pendingMedicalOrderCount: toNumber(data?.pendingMedicalOrderCount),
        todayInspectionPendingCount: toNumber(data?.todayInspectionPendingCount),
        overdueCareTaskCount: toNumber(data?.overdueCareTaskCount),
        abnormalInspectionOpenCount: toNumber(data?.abnormalInspectionOpenCount),
        incidentOpenCount: toNumber(data?.incidentOpenCount)
      }
    } else {
      errors.push('医护回执失败')
    }

    if (logisticsResult.status === 'fulfilled') {
      const data = logisticsResult.value as LogisticsWorkbenchSummary
      linkageSnapshot.value.logistics = {
        todayTaskCount: toNumber(data?.todayCleaningTaskCount) + toNumber(data?.todayMaintenanceTaskCount)
          + toNumber(data?.todayDeliveryTaskCount) + toNumber(data?.todayInventoryCheckTaskCount),
        maintenancePendingCount: toNumber(data?.maintenancePendingCount),
        maintenanceOverdueCount: toNumber(data?.maintenanceOverdueCount),
        undeliveredCount: toNumber(data?.undeliveredCount)
      }
    } else {
      errors.push('后勤回执失败')
    }

    const latest = pickLatestTimestamp([
      medicalResult.status === 'fulfilled' ? String((medicalResult.value as MedicalCareWorkbenchSummary)?.generatedAt || '') : '',
      logisticsResult.status === 'fulfilled' ? String((logisticsResult.value as LogisticsWorkbenchSummary)?.generatedAt || '') : ''
    ])
    linkageUpdatedAt.value = latest || new Date().toISOString()
    if (errors.length >= 3) {
      linkageErrorMessage.value = '跨部门回执加载失败，请稍后重试'
    } else if (errors.length > 0) {
      linkageNoticeMessage.value = `${errors.join('、')}，其余模块已更新`
    }
  } finally {
    if (!silent) {
      linkageLoading.value = false
    }
  }
}

async function fetchRealStats() {
  loading.value = true
  errorMessage.value = ''
  const linkageRefreshPromise = fetchLinkageReceipts({ silent: true })
  try {
    const data: ResidenceStatusSummary = await getResidenceStatusSummary()
    statusItems.value = [
      { status: '意向', count: Number(data.intentCount || 0), path: '/elder/list' },
      { status: '试住', count: Number(data.trialCount || 0), path: '/elder/trial-stay' },
      { status: '在住', count: Number(data.inHospitalCount || 0), path: '/elder/list?status=1' },
      { status: '外出', count: Number(data.outingCount || 0), path: '/elder/outing?status=OUT' },
      { status: '外出就医', count: Number(data.medicalOutingCount || 0), path: '/elder/medical-outing' },
      { status: '退住办理中', count: Number(data.dischargePendingCount || 0), path: '/elder/discharge-apply?status=PENDING' },
      { status: '已退住', count: Number(data.dischargedCount || 0), path: '/elder/list?status=3' },
      { status: '死亡', count: Number(data.deathCount || 0), path: '/elder/death-register' }
    ]
    pendingStats.value = {
      openIncidentCount: Number(data.openIncidentCount || 0),
      pendingDischargeCount: Number(data.dischargePendingCount || 0),
      overdueOutingCount: Number(data.overdueOutingCount || 0)
    }
    const generatedAt = String(data.generatedAt || '').trim()
    lastUpdatedAtRaw.value = generatedAt || new Date().toISOString()
    lastUpdatedAt.value = generatedAt ? formatChineseDateTime(generatedAt) : formatChineseDateTime(new Date())
  } catch (error: any) {
    errorMessage.value = error?.message || '加载状态统计失败'
    message.error(errorMessage.value)
  } finally {
    await linkageRefreshPromise.catch(() => {})
    loading.value = false
  }
}

onMounted(() => {
  applyStatusQueryFromRoute()
  statusRouteSignature.value = buildStatusRouteSignature(route.query as Record<string, unknown>)
  syncStatusQueryToRoute().catch(() => {})
  fetchRealStats()
  setupAutoRefresh()
})

watch(alertOnly, () => {
  syncStatusQueryToRoute().catch(() => {})
})

watch(autoRefresh, () => {
  syncStatusQueryToRoute().catch(() => {})
  setupAutoRefresh()
})

watch(
  () => route.query,
  (queryMap) => {
    const nextSignature = buildStatusRouteSignature(queryMap as Record<string, unknown>)
    if (skipNextStatusRouteWatch.value) {
      skipNextStatusRouteWatch.value = false
      statusRouteSignature.value = nextSignature
      return
    }
    if (nextSignature === statusRouteSignature.value) return
    statusRouteSignature.value = nextSignature
    applyStatusQueryFromRoute()
  },
  { deep: true }
)

onBeforeUnmount(() => {
  clearAutoRefresh()
})

useLiveSyncRefresh({
  topics: ['elder', 'lifecycle', 'system'],
  refresh: fetchRealStats,
  debounceMs: 800
})
</script>

<style scoped>
.receipt-head {
  margin-bottom: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #334155;
  font-size: 12px;
  font-weight: 600;
}

.receipt-meta {
  color: #64748b;
  font-weight: 500;
}

.receipt-tile {
  min-height: 136px;
  border-radius: 12px;
  border: 1px solid rgba(148, 163, 184, 0.2);
  padding: 10px 12px;
  background: linear-gradient(126deg, rgba(248, 250, 252, 0.94) 0%, rgba(241, 245, 249, 0.84) 100%);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.receipt-tile:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 20px rgba(15, 23, 42, 0.08);
}

.receipt-clickable {
  cursor: pointer;
}

.receipt-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.receipt-title {
  color: #0f172a;
  font-size: 13px;
  font-weight: 700;
}

.receipt-main-value {
  margin-top: 6px;
  color: #0f172a;
  font-size: 30px;
  line-height: 1;
  font-weight: 700;
}

.receipt-desc {
  margin-top: 6px;
  color: #64748b;
  font-size: 12px;
}

.receipt-mini-metrics {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.receipt-mini-metrics span {
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(148, 163, 184, 0.12);
  color: #334155;
  font-size: 11px;
}

.receipt-drawer-actions {
  margin-top: 6px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.linkage-tile {
  border: 1px solid rgba(148, 163, 184, 0.24);
  background: linear-gradient(128deg, rgba(248, 250, 252, 0.95) 0%, rgba(241, 245, 249, 0.85) 100%);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.linkage-tile:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.08);
}

.linkage-title {
  color: #0f172a;
  font-size: 13px;
  font-weight: 600;
}

.linkage-desc {
  margin-top: 6px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.5;
}
</style>
