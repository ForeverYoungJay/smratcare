<template>
  <PageContainer title="统一任务中心" subTitle="跨模块任务聚合、风险排序与闭环处理">
    <template #extra>
      <a-space wrap>
        <a-tag color="blue">任务总数 {{ summary.totalPending }}</a-tag>
        <a-tag color="orange">高风险 {{ summary.highRiskCount }}</a-tag>
        <a-tag color="purple">超时 {{ summary.overdueCount }}</a-tag>
        <a-button @click="copyShareLink">复制分享链接</a-button>
        <a-button type="primary" ghost :loading="loading" @click="loadData">刷新</a-button>
      </a-space>
    </template>

    <a-card v-if="residentContext.active" :bordered="false" class="card-elevated resident-context">
      <a-space wrap>
        <a-tag color="processing">当前长者：{{ residentContext.name }}</a-tag>
        <a-button size="small" @click="clearResidentContext">清除长者上下文</a-button>
      </a-space>
    </a-card>

    <a-alert
      v-if="lifecycleContext.active"
      type="info"
      show-icon
      style="margin-bottom: 12px"
      :message="lifecycleContext.message"
    >
      <template #description>
        <a-space wrap>
          <a-tag color="blue">场景：入住状态变更联动</a-tag>
          <a-button type="link" size="small" @click="setLifecycleQuick('risk')">聚焦高风险任务</a-button>
          <a-button type="link" size="small" @click="setLifecycleQuick('overdue')">仅看超时任务</a-button>
        </a-space>
      </template>
    </a-alert>

    <a-card class="card-elevated task-hero" :bordered="false">
      <a-row :gutter="[12, 12]" align="middle">
        <a-col :xs="24" :xl="8">
          <div class="task-hero__title">任务风险热度</div>
          <div class="task-hero__score-row">
            <span class="task-hero__score">{{ summary.avgRiskScore }}</span>
            <a-tag :color="riskTagColor">{{ riskLevelLabel }}</a-tag>
          </div>
          <a-progress :percent="summary.avgRiskScore" :status="summary.avgRiskScore >= 75 ? 'exception' : summary.avgRiskScore >= 55 ? 'active' : 'normal'" />
          <div class="task-hero__meta">
            严重风险 {{ summary.criticalRiskCount }} 项 · 长者覆盖 {{ summary.residentCount }} 人
          </div>
        </a-col>
        <a-col :xs="24" :xl="16">
          <a-row :gutter="[12, 12]">
            <a-col :xs="12" :md="6" v-for="tile in heroTiles" :key="tile.key">
              <div class="metric-tile">
                <div class="metric-tile__label">{{ tile.label }}</div>
                <div class="metric-tile__value">{{ tile.value }}</div>
                <div class="metric-tile__hint">{{ tile.hint }}</div>
              </div>
            </a-col>
          </a-row>
        </a-col>
      </a-row>
    </a-card>

    <a-row :gutter="16" style="margin-top: 12px; margin-bottom: 12px">
      <a-col :xs="24" :xl="16">
        <SearchForm :model="query" @search="applyFilters" @reset="resetFilters">
          <a-form-item label="关键词">
            <a-input v-model:value="query.keyword" placeholder="长者/事项/责任人" allow-clear />
          </a-form-item>
          <a-form-item label="模块">
            <a-select v-model:value="query.module" :options="moduleOptions" allow-clear style="width: 150px" />
          </a-form-item>
          <a-form-item label="优先级">
            <a-select v-model:value="query.priority" :options="priorityOptions" allow-clear style="width: 140px" />
          </a-form-item>
          <a-form-item label="状态">
            <a-select v-model:value="query.status" :options="statusOptions" allow-clear style="width: 140px" />
          </a-form-item>
          <a-form-item label="超时阈值">
            <a-select v-model:value="query.overdueHours" :options="overdueOptions" style="width: 130px" />
          </a-form-item>
          <a-form-item label="排序">
            <a-select v-model:value="query.sortBy" :options="sortByOptions" style="width: 160px" />
          </a-form-item>
          <a-form-item label="方向">
            <a-radio-group v-model:value="query.sortDirection" size="small" button-style="solid">
              <a-radio-button value="DESC">降序</a-radio-button>
              <a-radio-button value="ASC">升序</a-radio-button>
            </a-radio-group>
          </a-form-item>
          <a-form-item>
            <a-switch v-model:checked="query.onlyOverdue" checked-children="仅超时" un-checked-children="全部" />
          </a-form-item>
          <template #extra>
            <a-space wrap>
              <a-button :loading="loading" @click="loadData">刷新</a-button>
              <a-button :loading="exporting" @click="exportCsvData">导出CSV</a-button>
              <a-button :loading="exporting" @click="exportExcelData">导出Excel</a-button>
            </a-space>
          </template>
        </SearchForm>
      </a-col>
      <a-col :xs="24" :xl="8">
        <a-card class="card-elevated action-queue" :bordered="false" title="推荐处理队列">
          <a-empty v-if="!focusQueue.length" description="暂无高风险任务" />
          <a-list v-else size="small" :data-source="focusQueue" :pagination="false">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-space direction="vertical" size="small" style="width: 100%">
                  <a-space>
                    <a-tag :color="riskScoreColor(item.riskScore)">{{ item.riskScore || 0 }}</a-tag>
                    <span class="queue-title">{{ item.taskTitle || '-' }}</span>
                  </a-space>
                  <span class="queue-meta">{{ item.riskReason || '常规待办' }}</span>
                  <a-space>
                    <a-button type="link" size="small" @click="goDetail(item)">去处理</a-button>
                    <a-button type="link" size="small" @click="goResident(item)">长者视图</a-button>
                  </a-space>
                </a-space>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>
    </a-row>

    <a-space wrap style="margin-bottom: 12px">
      <a-checkable-tag :checked="activeQuickModule === 'ALL'" @change="() => applyQuickModule('ALL')">全部模块</a-checkable-tag>
      <a-checkable-tag :checked="activeQuickModule === 'ORDER'" @change="() => applyQuickModule('ORDER')">医嘱执行</a-checkable-tag>
      <a-checkable-tag :checked="activeQuickModule === 'INSPECTION'" @change="() => applyQuickModule('INSPECTION')">健康巡检</a-checkable-tag>
      <a-checkable-tag :checked="activeQuickModule === 'NURSING_LOG'" @change="() => applyQuickModule('NURSING_LOG')">护理日志</a-checkable-tag>
      <a-checkable-tag :checked="activeQuickModule === 'HANDOVER'" @change="() => applyQuickModule('HANDOVER')">交接班</a-checkable-tag>
    </a-space>

    <DataTable
      rowKey="id"
      :columns="columns"
      :data-source="visibleRows"
      :loading="loading"
      :pagination="pagination"
      :row-class-name="resolveRowClassName"
      @change="onTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'module'">
          <a-tag :color="moduleColor(record.module)">{{ moduleLabel(record.module) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'priority'">
          <a-tag :color="priorityColor(record.priority)">{{ priorityLabel(record.priority) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'plannedTime'">
          {{ formatDateTime(record.plannedTime) }}
        </template>
        <template v-else-if="column.key === 'overdueMinutes'">
          <a-tag :color="record.overdue ? 'red' : 'default'">{{ formatOverdue(record) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'riskScore'">
          <a-tag :color="riskScoreColor(record.riskScore)">{{ record.riskScore || 0 }}</a-tag>
        </template>
        <template v-else-if="column.key === 'riskReason'">
          <span class="risk-reason">{{ record.riskReason || '-' }}</span>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="goDetail(record)">处理</a-button>
            <a-button type="link" @click="goResident(record)">长者视图</a-button>
          </a-space>
        </template>
      </template>
    </DataTable>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { mapMedicalExportRows, unifiedTaskExportColumns } from '../../constants/medicalExport'
import { exportCsv, exportExcel } from '../../utils/export'
import { resolveMedicalError } from './medicalError'
import { getMedicalUnifiedTaskPage } from '../../api/medicalCare'
import {
  MEDICAL_UNIFIED_TASK_QUERY_DEFAULTS,
  buildMedicalUnifiedTaskRouteQuery,
  normalizeMedicalUnifiedTaskQuery,
  parseMedicalUnifiedTaskRouteQuery
} from '../../utils/unifiedTaskQuery'
import type { MedicalUnifiedTaskItem, MedicalUnifiedTaskQuery, PageResult } from '../../types'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const exporting = ref(false)
const rows = ref<MedicalUnifiedTaskItem[]>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const MEDICAL_MANAGED_ROUTE_KEYS = new Set([
  'residentId',
  'residentName',
  'elderId',
  'module',
  'priority',
  'status',
  'keyword',
  'overdueHours',
  'onlyOverdue',
  'sortBy',
  'sortDirection',
  'pageNo',
  'pageSize'
])

const query = reactive<Required<Pick<MedicalUnifiedTaskQuery, 'keyword' | 'sortBy' | 'sortDirection' | 'overdueHours'>> & {
  module?: string
  priority?: string
  status?: string
  onlyOverdue: boolean
}>({
  keyword: '',
  module: undefined,
  priority: undefined,
  status: undefined,
  overdueHours: MEDICAL_UNIFIED_TASK_QUERY_DEFAULTS.overdueHours,
  sortBy: MEDICAL_UNIFIED_TASK_QUERY_DEFAULTS.sortBy,
  sortDirection: MEDICAL_UNIFIED_TASK_QUERY_DEFAULTS.sortDirection,
  onlyOverdue: MEDICAL_UNIFIED_TASK_QUERY_DEFAULTS.onlyOverdue
})

const activeQuickModule = ref<'ALL' | 'ORDER' | 'INSPECTION' | 'NURSING_LOG' | 'HANDOVER'>('ALL')

const residentContext = computed(() => {
  const residentIdRaw = route.query.residentId ?? route.query.elderId
  const residentName = typeof route.query.residentName === 'string' ? route.query.residentName : ''
  return {
    active: !!residentIdRaw,
    residentId: residentIdRaw ? Number(residentIdRaw) : undefined,
    name: residentName || (residentIdRaw ? `长者#${residentIdRaw}` : '')
  }
})
const lifecycleContext = computed(() => {
  const source = String(route.query.source || '').trim().toLowerCase()
  const scene = String(route.query.scene || '').trim().toLowerCase()
  const active = source === 'lifecycle' || scene === 'status-change'
  return {
    active,
    message: active ? '已自动按状态变更场景启用风险排序，可直接处理跨模块高风险任务。' : ''
  }
})

const moduleOptions = [
  { label: '医嘱执行', value: 'ORDER' },
  { label: '健康巡检', value: 'INSPECTION' },
  { label: '护理日志', value: 'NURSING_LOG' },
  { label: '交接班', value: 'HANDOVER' }
]
const priorityOptions = [
  { label: '高', value: 'HIGH' },
  { label: '中', value: 'MEDIUM' },
  { label: '低', value: 'LOW' }
]
const statusOptions = [
  { label: '待执行', value: 'PENDING' },
  { label: '异常', value: 'ABNORMAL' },
  { label: '跟进中', value: 'FOLLOWING' },
  { label: '草稿交接', value: 'DRAFT' },
  { label: '已交接', value: 'HANDED_OVER' }
]
const overdueOptions = [
  { label: '4小时', value: 4 },
  { label: '6小时', value: 6 },
  { label: '8小时', value: 8 },
  { label: '12小时', value: 12 },
  { label: '18小时', value: 18 },
  { label: '24小时', value: 24 }
]
const sortByOptions = [
  { label: '风险分数', value: 'RISK_SCORE' },
  { label: '计划时间', value: 'PLANNED_TIME' },
  { label: '优先级', value: 'PRIORITY' }
]

const columns = [
  { title: '模块', key: 'module', width: 110 },
  { title: '长者', dataIndex: 'residentName', key: 'residentName', width: 120 },
  { title: '事项', dataIndex: 'taskTitle', key: 'taskTitle', width: 220 },
  { title: '计划时间', dataIndex: 'plannedTime', key: 'plannedTime', width: 170 },
  { title: '超时状态', key: 'overdueMinutes', width: 120 },
  { title: '风险分', key: 'riskScore', width: 90 },
  { title: '优先级', key: 'priority', width: 100 },
  { title: '风险说明', key: 'riskReason', width: 260 },
  { title: '操作', key: 'action', width: 140 }
]

const summary = computed(() => {
  const totalPending = Number(pagination.total || 0)
  const highPriorityCount = rows.value.filter((item) => item.priority === 'HIGH').length
  const overdueCount = rows.value.filter((item) => item.overdue).length
  const residentCount = new Set(rows.value.map((item) => item.residentId).filter((id) => !!id)).size
  const scores = rows.value.map((item) => Number(item.riskScore || 0)).filter((score) => score > 0)
  const avgRiskScore = scores.length ? Math.round(scores.reduce((sum, score) => sum + score, 0) / scores.length) : 0
  const criticalRiskCount = rows.value.filter((item) => Number(item.riskScore || 0) >= 80).length
  const highRiskCount = rows.value.filter((item) => Number(item.riskScore || 0) >= 60).length
  return {
    totalPending,
    highPriorityCount,
    overdueCount,
    residentCount,
    avgRiskScore,
    criticalRiskCount,
    highRiskCount
  }
})

const riskLevelLabel = computed(() => {
  if (summary.value.avgRiskScore >= 75) return '紧急风险'
  if (summary.value.avgRiskScore >= 55) return '高风险'
  if (summary.value.avgRiskScore >= 30) return '中风险'
  return '低风险'
})

const riskTagColor = computed(() => {
  if (summary.value.avgRiskScore >= 75) return 'red'
  if (summary.value.avgRiskScore >= 55) return 'orange'
  if (summary.value.avgRiskScore >= 30) return 'blue'
  return 'green'
})

const heroTiles = computed(() => [
  {
    key: 'total',
    label: '总待办',
    value: summary.value.totalPending,
    hint: `当前页 ${rows.value.length} 条`
  },
  {
    key: 'high-priority',
    label: '高优先级',
    value: summary.value.highPriorityCount,
    hint: `风险分>=60 ${summary.value.highRiskCount} 条`
  },
  {
    key: 'overdue',
    label: '超时风险',
    value: summary.value.overdueCount,
    hint: `阈值 ${query.overdueHours} 小时`
  },
  {
    key: 'critical',
    label: '严重风险',
    value: summary.value.criticalRiskCount,
    hint: `长者覆盖 ${summary.value.residentCount} 人`
  }
])

const visibleRows = computed(() => {
  if (activeQuickModule.value === 'ALL') {
    return rows.value
  }
  return rows.value.filter((item) => item.module === activeQuickModule.value)
})

const focusQueue = computed(() => {
  return [...rows.value]
    .sort((a, b) => Number(b.riskScore || 0) - Number(a.riskScore || 0))
    .slice(0, 5)
})

function moduleLabel(module?: string) {
  return moduleOptions.find((item) => item.value === module)?.label || module
}

function moduleColor(module?: string) {
  if (module === 'ORDER') return 'blue'
  if (module === 'INSPECTION') return 'orange'
  if (module === 'NURSING_LOG') return 'purple'
  return 'green'
}

function priorityLabel(priority?: string) {
  return priorityOptions.find((item) => item.value === priority)?.label || priority
}

function priorityColor(priority?: string) {
  if (priority === 'HIGH') return 'red'
  if (priority === 'MEDIUM') return 'orange'
  return 'default'
}

function riskScoreColor(score?: number) {
  const current = Number(score || 0)
  if (current >= 80) return 'red'
  if (current >= 60) return 'orange'
  if (current >= 35) return 'blue'
  return 'default'
}

function formatDateTime(value?: string) {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : '-'
}

function formatOverdue(record: MedicalUnifiedTaskItem) {
  if (!record.overdue) {
    return '按计划'
  }
  return `超时 ${record.overdueMinutes || 0} 分钟`
}

function resolveRowClassName(record: MedicalUnifiedTaskItem) {
  const score = Number(record.riskScore || 0)
  if (score >= 80) return 'task-row-danger'
  if (record.overdue || score >= 60) return 'task-row-warning'
  return ''
}

function parseRoutePath(path: string) {
  const [pathname, search] = path.split('?')
  const parsed = new URLSearchParams(search || '')
  const queryObject: Record<string, string> = {}
  parsed.forEach((value, key) => {
    if (value) {
      queryObject[key] = value
    }
  })
  return {
    pathname,
    queryObject
  }
}

function goDetail(record: MedicalUnifiedTaskItem) {
  if (record.suggestedRoute) {
    const target = parseRoutePath(record.suggestedRoute)
    router.push({ path: target.pathname, query: target.queryObject })
    return
  }
  if (record.module === 'ORDER') {
    router.push({ path: '/medical-care/orders', query: { elderId: record.residentId, filter: 'to_execute' } })
    return
  }
  if (record.module === 'INSPECTION') {
    router.push({ path: '/medical-care/inspection', query: { inspectionId: record.sourceId, residentId: record.residentId } })
    return
  }
  if (record.module === 'NURSING_LOG') {
    router.push({ path: '/medical-care/nursing-log', query: { sourceInspectionId: record.sourceId, residentId: record.residentId } })
    return
  }
  router.push({ path: '/medical-care/handovers', query: { id: record.sourceId, status: record.status || 'DRAFT' } })
}

function goResident(record: MedicalUnifiedTaskItem) {
  router.push({
    path: '/elder/resident-360',
    query: {
      residentId: record.residentId ? String(record.residentId) : undefined,
      from: 'unified-task-center'
    }
  })
}

function clearResidentContext() {
  const nextQuery: Record<string, any> = { ...route.query }
  delete nextQuery.residentId
  delete nextQuery.elderId
  delete nextQuery.residentName
  router.replace({ path: route.path, query: nextQuery })
}

function applyQuickModule(module: 'ALL' | 'ORDER' | 'INSPECTION' | 'NURSING_LOG' | 'HANDOVER') {
  activeQuickModule.value = module
}

function firstRouteQueryText(value: unknown) {
  if (Array.isArray(value)) return firstRouteQueryText(value[0])
  if (value == null) return ''
  return String(value).trim()
}

function unmanagedRouteQuery() {
  const unmanaged: Record<string, string> = {}
  Object.entries(route.query).forEach(([key, value]) => {
    if (MEDICAL_MANAGED_ROUTE_KEYS.has(key)) return
    const text = firstRouteQueryText(value)
    if (!text) return
    unmanaged[key] = text
  })
  return unmanaged
}

function syncStateFromRoute() {
  const routeQuery = parseMedicalUnifiedTaskRouteQuery(route.query)
  query.keyword = routeQuery.keyword || ''
  query.module = routeQuery.module
  query.priority = routeQuery.priority
  query.status = routeQuery.status
  query.overdueHours = Number(routeQuery.overdueHours || MEDICAL_UNIFIED_TASK_QUERY_DEFAULTS.overdueHours)
  query.onlyOverdue = Boolean(routeQuery.onlyOverdue)
  query.sortBy = String(routeQuery.sortBy || MEDICAL_UNIFIED_TASK_QUERY_DEFAULTS.sortBy)
  query.sortDirection = String(routeQuery.sortDirection || MEDICAL_UNIFIED_TASK_QUERY_DEFAULTS.sortDirection)
  pagination.current = Number(routeQuery.pageNo || 1)
  pagination.pageSize = Number(routeQuery.pageSize || 10)
  const source = firstRouteQueryText(route.query.source).toLowerCase()
  const scene = firstRouteQueryText(route.query.scene).toLowerCase()
  const category = firstRouteQueryText(route.query.category).toLowerCase()
  const quick = firstRouteQueryText(route.query.quick).toLowerCase()
  const focus = firstRouteQueryText(route.query.lifecycleFocus).toLowerCase()
  const lifecycleScene = source === 'lifecycle' || scene === 'status-change'
  if (!lifecycleScene) {
    return
  }

  const hasExplicitModule = Boolean(firstRouteQueryText(route.query.module))
  if (!hasExplicitModule) {
    if (focus === 'order') query.module = 'ORDER'
    if (focus === 'inspection' || focus === 'medical') query.module = 'INSPECTION'
    if (focus === 'nursing' || focus === 'nursing_log') query.module = 'NURSING_LOG'
    if (focus === 'handover') query.module = 'HANDOVER'
  }

  const hasExplicitStatus = Boolean(firstRouteQueryText(route.query.status))
  const riskyQuick = quick === 'overdue' || quick === 'risk' || quick === 'alert' || category === 'alert'
  if (riskyQuick) {
    query.onlyOverdue = true
    if (!hasExplicitStatus) {
      query.status = 'ABNORMAL'
    }
  }
  if (!firstRouteQueryText(route.query.sortBy)) {
    query.sortBy = 'RISK_SCORE'
  }
  if (!firstRouteQueryText(route.query.sortDirection)) {
    query.sortDirection = 'DESC'
  }
}

function setLifecycleQuick(mode: 'risk' | 'overdue') {
  if (mode === 'risk') {
    query.onlyOverdue = false
    query.sortBy = 'RISK_SCORE'
    query.sortDirection = 'DESC'
    query.status = undefined
  } else {
    query.onlyOverdue = true
    query.status = query.status || 'ABNORMAL'
  }
  applyRouteQuery(true)
}

function applyRouteQuery(resetPage = true) {
  const normalized = normalizeMedicalUnifiedTaskQuery({
    keyword: query.keyword,
    module: query.module,
    priority: query.priority,
    status: query.status,
    overdueHours: query.overdueHours,
    onlyOverdue: query.onlyOverdue,
    sortBy: query.sortBy,
    sortDirection: query.sortDirection,
    pageNo: resetPage ? 1 : pagination.current,
    pageSize: pagination.pageSize,
    elderId: residentContext.value.residentId
  })
  const unmanaged = unmanagedRouteQuery()
  const routeQuery = buildMedicalUnifiedTaskRouteQuery(normalized, {
    ...unmanaged,
    residentName: typeof route.query.residentName === 'string' ? route.query.residentName : undefined
  })
  router.replace({
    path: route.path,
    query: routeQuery
  })
}

function applyFilters() {
  applyRouteQuery(true)
}

function resetFilters() {
  query.keyword = ''
  query.module = undefined
  query.priority = undefined
  query.status = undefined
  query.overdueHours = MEDICAL_UNIFIED_TASK_QUERY_DEFAULTS.overdueHours
  query.onlyOverdue = false
  query.sortBy = MEDICAL_UNIFIED_TASK_QUERY_DEFAULTS.sortBy
  query.sortDirection = MEDICAL_UNIFIED_TASK_QUERY_DEFAULTS.sortDirection
  activeQuickModule.value = 'ALL'
  applyRouteQuery(true)
}

function onTableChange(pag: { current?: number; pageSize?: number }) {
  pagination.current = pag.current || 1
  pagination.pageSize = pag.pageSize || 10
  applyRouteQuery(false)
}

async function loadData() {
  loading.value = true
  try {
    const params = normalizeMedicalUnifiedTaskQuery({
      pageNo: pagination.current,
      pageSize: pagination.pageSize,
      elderId: residentContext.value.residentId,
      module: query.module,
      priority: query.priority,
      status: query.status,
      keyword: query.keyword,
      overdueHours: query.overdueHours,
      onlyOverdue: query.onlyOverdue,
      sortBy: query.sortBy,
      sortDirection: query.sortDirection
    })
    const page: PageResult<MedicalUnifiedTaskItem> = await getMedicalUnifiedTaskPage(params)
    rows.value = page.list || []
    pagination.total = Number(page.total || 0)
  } catch (error) {
    message.error(resolveMedicalError(error, '加载统一任务失败'))
    rows.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

async function loadExportRecords() {
  exporting.value = true
  try {
    const pageSize = 500
    let pageNo = 1
    let total = 0
    const list: MedicalUnifiedTaskItem[] = []
    do {
      const page: PageResult<MedicalUnifiedTaskItem> = await getMedicalUnifiedTaskPage(
        normalizeMedicalUnifiedTaskQuery({
          pageNo,
          pageSize,
          elderId: residentContext.value.residentId,
          module: query.module,
          priority: query.priority,
          status: query.status,
          keyword: query.keyword,
          overdueHours: query.overdueHours,
          onlyOverdue: query.onlyOverdue,
          sortBy: query.sortBy,
          sortDirection: query.sortDirection
        })
      )
      total = Number(page.total || 0)
      list.push(...(page.list || []))
      pageNo += 1
      if (!page.list || page.list.length < pageSize) break
    } while (list.length < total && pageNo <= 20)

    return mapMedicalExportRows(
      list.map((item) => ({
        ...item,
        module: moduleLabel(item.module),
        plannedTime: formatDateTime(item.plannedTime),
        priority: priorityLabel(item.priority),
        status: item.status || '-',
        riskScore: item.riskScore || 0,
        riskReason: item.riskReason || '-',
        overdueMinutes: formatOverdue(item)
      })),
      unifiedTaskExportColumns
    )
  } catch (error) {
    message.error(resolveMedicalError(error, '加载导出数据失败'))
    return []
  } finally {
    exporting.value = false
  }
}

function exportCsvData() {
  loadExportRecords().then((data) => {
    if (!data.length) {
      message.warning('暂无可导出任务')
      return
    }
    exportCsv(data, `统一任务中心-${dayjs().format('YYYYMMDD-HHmmss')}.csv`)
    message.success('CSV导出成功')
  })
}

function exportExcelData() {
  loadExportRecords().then((data) => {
    if (!data.length) {
      message.warning('暂无可导出任务')
      return
    }
    exportExcel(data, `统一任务中心-${dayjs().format('YYYYMMDD-HHmmss')}.xls`)
    message.success('Excel导出成功')
  })
}

async function copyShareLink() {
  const url = `${window.location.origin}${route.fullPath}`
  try {
    await navigator.clipboard.writeText(url)
    message.success('分享链接已复制')
  } catch (error) {
    message.warning('复制失败，请手动复制地址栏链接')
  }
}

watch(
  () => route.query,
  () => {
    syncStateFromRoute()
    loadData()
  },
  { immediate: true }
)
</script>

<style scoped>
.resident-context {
  margin-bottom: 12px;
  border: 1px solid #d5ebff;
  background: linear-gradient(130deg, #f5fbff 0%, #ebf5ff 100%);
}

.task-hero {
  border: 1px solid #d4e6ff;
  background:
    radial-gradient(130% 110% at 100% 0%, rgba(39, 121, 255, 0.18) 0%, rgba(39, 121, 255, 0) 58%),
    linear-gradient(132deg, #f7fbff 0%, #edf5ff 45%, #f8fbff 100%);
}

.task-hero__title {
  font-size: 14px;
  color: #1f2f45;
  font-weight: 600;
}

.task-hero__score-row {
  margin-top: 8px;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.task-hero__score {
  font-size: 38px;
  line-height: 1;
  font-weight: 700;
  color: #0f1f34;
}

.task-hero__meta {
  margin-top: 8px;
  font-size: 12px;
  color: #5f7087;
}

.metric-tile {
  border-radius: 10px;
  border: 1px solid #dce9ff;
  background: rgba(255, 255, 255, 0.86);
  padding: 10px;
}

.metric-tile__label {
  font-size: 12px;
  color: #66798e;
}

.metric-tile__value {
  margin-top: 6px;
  font-size: 24px;
  line-height: 1;
  font-weight: 700;
  color: #10243f;
}

.metric-tile__hint {
  margin-top: 6px;
  font-size: 11px;
  color: #73849b;
}

.action-queue {
  min-height: 206px;
}

.queue-title {
  font-weight: 600;
  color: #15243d;
}

.queue-meta {
  color: #64748b;
  font-size: 12px;
}

.risk-reason {
  color: #4b5563;
  font-size: 12px;
}

:deep(.task-row-danger > td) {
  background: #fff1f0 !important;
}

:deep(.task-row-warning > td) {
  background: #fff7e6 !important;
}

@media (max-width: 992px) {
  .action-queue {
    min-height: auto;
  }
}
</style>
