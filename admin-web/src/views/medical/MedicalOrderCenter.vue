<template>
  <PageContainer title="医嘱管理中心" subTitle="执行效率、风险信号与任务明细统一视图">
    <template #extra>
      <a-space wrap>
        <a-tag color="blue">统计日期 {{ summary.snapshotDate || query.taskDate || '-' }}</a-tag>
        <a-tag color="geekblue">窗口 {{ summaryQuery.incidentWindowDays }} 天</a-tag>
        <a-tag :color="riskTagColor">{{ riskLabel }}</a-tag>
        <a-button type="primary" ghost @click="reloadAll">刷新</a-button>
      </a-space>
    </template>

    <a-card class="card-elevated order-hero" :bordered="false">
      <a-row :gutter="[14, 14]" align="middle">
        <a-col :xs="24" :xl="7">
          <div class="order-hero__title">医嘱执行总览</div>
          <div class="order-hero__value-row">
            <span class="order-hero__value">{{ formatPercent(executionRate, 1) }}%</span>
            <a-tag :color="riskTagColor">{{ riskLabel }}</a-tag>
          </div>
          <div class="order-hero__meta">
            应执行 {{ summary.medicalOrderShouldCount || 0 }} · 已执行 {{ summary.medicalOrderDoneCount || 0 }} · 待执行 {{ summary.medicalOrderPendingCount || 0 }}
          </div>
          <a-progress :percent="Number(formatPercent(executionRate, 2))" :status="executionRate >= 90 ? 'success' : executionRate >= 70 ? 'active' : 'exception'" />
        </a-col>
        <a-col :xs="24" :xl="17">
          <a-row :gutter="[12, 12]">
            <a-col :xs="12" :md="6" v-for="tile in heroTiles" :key="tile.key">
              <div class="order-tile">
                <div class="order-tile__label">{{ tile.label }}</div>
                <div class="order-tile__value">{{ tile.value }}</div>
                <div class="order-tile__hint">{{ tile.hint }}</div>
              </div>
            </a-col>
          </a-row>
        </a-col>
      </a-row>
    </a-card>

    <a-alert
      v-if="summary.medicalOrderPendingCount > 0 || summary.overdueCareTaskCount > 0 || summary.unclosedAbnormalCount > 0"
      style="margin-top: 12px"
      type="warning"
      show-icon
      :message="`执行提醒：医嘱待执行 ${summary.medicalOrderPendingCount || 0} 条，护理超时 ${summary.overdueCareTaskCount || 0} 条，未闭环异常 ${summary.unclosedAbnormalCount || 0} 条。`"
    />

    <a-row :gutter="16" style="margin-top: 12px; margin-bottom: 12px">
      <a-col :xs="24" :xl="16">
        <SearchForm :model="query" @search="onSearch" @reset="onReset">
          <a-form-item label="关键词">
            <a-input v-model:value="query.keyword" placeholder="长者 / 药品" allow-clear />
          </a-form-item>
          <a-form-item label="状态">
            <a-select v-model:value="query.status" allow-clear style="width: 140px" :options="statusOptions" />
          </a-form-item>
          <a-form-item label="任务日期">
            <a-date-picker v-model:value="query.taskDate" value-format="YYYY-MM-DD" allow-clear />
          </a-form-item>
          <a-form-item label="风险策略">
            <a-select v-model:value="draftSummaryQuery.overdueHours" :options="overdueHourOptions" style="width: 132px" />
          </a-form-item>
          <a-form-item label="事件窗口">
            <a-select v-model:value="draftSummaryQuery.incidentWindowDays" :options="incidentWindowOptions" style="width: 132px" />
          </a-form-item>
          <template #extra>
            <a-space wrap>
              <a-button @click="applySummaryFilters">应用策略</a-button>
              <a-button :loading="exporting" @click="exportCsvData">导出CSV</a-button>
              <a-button :loading="exporting" @click="exportExcelData">导出Excel</a-button>
              <a-button type="primary" @click="quickToPending">仅看待执行</a-button>
              <a-button @click="quickToAbnormal">待执行/异常</a-button>
            </a-space>
          </template>
        </SearchForm>
      </a-col>
      <a-col :xs="24" :xl="8">
        <a-card class="card-elevated order-actions" :bordered="false" title="批量治理队列">
          <a-empty v-if="!focusActions.length" description="暂无推荐动作" />
          <a-list v-else size="small" :data-source="focusActions" :pagination="false">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-space>
                  <a-tag :color="severityColor(item.severity)">{{ severityLabel(item.severity) }}</a-tag>
                  <a @click="go(item.route || '/medical-care/unified-task-center')">{{ item.title || '前往处理' }}</a>
                </a-space>
              </a-list-item>
            </template>
          </a-list>
          <div class="risk-queue-title">高风险待办（Top{{ highRiskQueue.length >= 5 ? 5 : highRiskQueue.length }})</div>
          <a-empty v-if="!highRiskQueue.length" description="暂无高风险任务" />
          <a-list v-else size="small" :data-source="highRiskQueue.slice(0, 5)" :pagination="false">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-space direction="vertical" size="small" style="width: 100%">
                  <a-space>
                    <a-tag :color="riskLevelColor(item.riskMeta.level)">{{ riskLevelLabel(item.riskMeta.level) }}</a-tag>
                    <a-tag color="volcano">{{ item.riskMeta.score }}</a-tag>
                    <span>{{ item.elderName || '-' }} · {{ item.drugName || '-' }}</span>
                  </a-space>
                  <div class="risk-queue-meta">{{ item.riskMeta.reason }}</div>
                  <a-space>
                    <a-button type="link" size="small" @click="go('/medical-care/medication-registration', { elderId: item.elderId, keyword: item.drugName })">
                      去登记
                    </a-button>
                    <a-button type="link" size="small" @click="go('/medical-care/medication-registration', { elderId: item.elderId, date: query.taskDate, filter: 'pending_or_abnormal' })">
                      查看执行
                    </a-button>
                  </a-space>
                </a-space>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>
    </a-row>

    <a-space wrap style="margin-bottom: 12px">
      <a-checkable-tag :checked="activeQuickFilter === 'ALL'" @change="() => applyQuickFilter('ALL')">全部</a-checkable-tag>
      <a-checkable-tag :checked="activeQuickFilter === 'PENDING'" @change="() => applyQuickFilter('PENDING')">待执行</a-checkable-tag>
      <a-checkable-tag :checked="activeQuickFilter === 'DONE'" @change="() => applyQuickFilter('DONE')">已执行</a-checkable-tag>
      <a-checkable-tag :checked="activeQuickFilter === 'CANCELLED'" @change="() => applyQuickFilter('CANCELLED')">已取消</a-checkable-tag>
      <a-checkable-tag :checked="activeQuickFilter === 'OVERDUE_PENDING'" @change="() => applyQuickFilter('OVERDUE_PENDING')">逾期待执行</a-checkable-tag>
      <a-checkable-tag :checked="riskFilterMode === 'HIGH'" @change="(checked) => applyRiskFilter(checked ? 'HIGH' : 'ALL')">高风险优先</a-checkable-tag>
      <a-checkable-tag :checked="riskFilterMode === 'IMMINENT'" @change="(checked) => applyRiskFilter(checked ? 'IMMINENT' : 'ALL')">即将超时</a-checkable-tag>
      <a-button size="small" @click="applyRiskFilter('ALL')">清空风险筛选</a-button>
      <a-button size="small" :loading="exporting" @click="exportHighRiskCsvData">高风险CSV</a-button>
      <a-button size="small" :loading="exporting" @click="exportHighRiskExcelData">高风险Excel</a-button>
      <a-tag color="blue">当前列表 {{ filteredRows.length }} 条</a-tag>
      <a-tag color="orange">逾期待执行 {{ overduePendingCount }} 条</a-tag>
      <a-tag color="volcano">高风险 {{ highRiskCount }} 条</a-tag>
      <a-tag color="gold">即将超时 {{ imminentCount }} 条</a-tag>
    </a-space>

    <DataTable
      rowKey="id"
      :columns="columns"
      :data-source="filteredRows"
      :loading="loading"
      :pagination="pagination"
      :row-class-name="resolveRowClassName"
      @change="onTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'plannedTime'">
          {{ formatDateTime(record.plannedTime) }}
        </template>
        <template v-else-if="column.key === 'doneTime'">
          {{ formatDateTime(record.doneTime) }}
        </template>
        <template v-else-if="column.key === 'status'">
          <a-tag :color="statusColor(record.status)">{{ statusLabel(record.status) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'plannedTimeGap'">
          <a-tag :color="plannedGapColor(record)">{{ plannedGapText(record) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'riskLevel'">
          <a-tag :color="riskLevelColor(record.riskMeta.level)">{{ riskLevelLabel(record.riskMeta.level) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'riskScore'">
          <a-tag :color="riskScoreColor(record.riskMeta.score)">{{ record.riskMeta.score }}</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="go('/medical-care/medication-registration', { elderId: record.elderId, keyword: record.drugName })">去登记</a-button>
            <a-button type="link" @click="go('/medical-care/medication-registration', { elderId: record.elderId, date: query.taskDate, filter: 'pending_or_abnormal' })">查看执行</a-button>
          </a-space>
        </template>
      </template>
    </DataTable>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import dayjs, { type Dayjs } from 'dayjs'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { exportCsv, exportExcel } from '../../utils/export'
import { mapMedicalExportRows, medicalOrderTaskExportColumns } from '../../constants/medicalExport'
import { resolveMedicalError } from './medicalError'
import { getMedicalHealthCenterSummary } from '../../api/medicalCare'
import { getHealthMedicationTaskPage } from '../../api/health'
import type { HealthMedicationTask, MedicalCareWorkbenchSummary, MedicalCareWorkbenchSummaryQuery, PageResult } from '../../types'
import {
  MEDICAL_WORKBENCH_QUERY_DEFAULTS,
  buildMedicalWorkbenchRouteQuery,
  normalizeMedicalWorkbenchQuery,
  parseMedicalWorkbenchQueryPatch
} from '../../utils/medicalWorkbenchQuery'
import {
  createMedicalWorkbenchSummaryDefaults,
  formatPercent,
  normalizeRiskIndex,
  resolveMedicalRiskColor,
  resolveMedicalRiskLabel
} from '../../utils/medicalSummary'

type QuickFilter = 'ALL' | 'PENDING' | 'DONE' | 'CANCELLED' | 'OVERDUE_PENDING'
type RiskFilterMode = 'ALL' | 'HIGH' | 'IMMINENT'
type TaskRiskLevel = 'HIGH' | 'IMMINENT' | 'NORMAL'

interface MedicationTaskRiskMeta {
  level: TaskRiskLevel
  score: number
  reason: string
  overdueMinutes: number
  remainMinutes: number
}

interface MedicationTaskRow extends HealthMedicationTask {
  riskMeta: MedicationTaskRiskMeta
}

const EXPORT_PAGE_SIZE = 200
const EXPORT_PAGE_LIMIT = 30

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const exporting = ref(false)
const rows = ref<HealthMedicationTask[]>([])
const summary = ref<MedicalCareWorkbenchSummary>(createMedicalWorkbenchSummaryDefaults())
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const query = reactive({
  keyword: '',
  status: '' as string | undefined,
  taskDate: dayjs().format('YYYY-MM-DD'),
  pageNo: 1,
  pageSize: 10
})

const summaryQuery = ref<MedicalCareWorkbenchSummaryQuery>(normalizeMedicalWorkbenchQuery(MEDICAL_WORKBENCH_QUERY_DEFAULTS))
const draftSummaryQuery = ref<MedicalCareWorkbenchSummaryQuery>(normalizeMedicalWorkbenchQuery(MEDICAL_WORKBENCH_QUERY_DEFAULTS))

const statusOptions = [
  { label: '待执行', value: 'PENDING' },
  { label: '已执行', value: 'DONE' },
  { label: '已取消', value: 'CANCELLED' }
]
const incidentWindowOptions = [
  { label: '近7天', value: 7 },
  { label: '近14天', value: 14 },
  { label: '近30天', value: 30 },
  { label: '近45天', value: 45 },
  { label: '近60天', value: 60 }
]
const overdueHourOptions = [
  { label: '6小时', value: 6 },
  { label: '8小时', value: 8 },
  { label: '12小时', value: 12 },
  { label: '18小时', value: 18 },
  { label: '24小时', value: 24 }
]

const columns = [
  { title: '长者', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '药品', dataIndex: 'drugName', key: 'drugName', width: 160 },
  { title: '计划时间', dataIndex: 'plannedTime', key: 'plannedTime', width: 170 },
  { title: '时间状态', key: 'plannedTimeGap', width: 140 },
  { title: '风险等级', key: 'riskLevel', width: 110 },
  { title: '风险评分', key: 'riskScore', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '完成时间', dataIndex: 'doneTime', key: 'doneTime', width: 170 },
  { title: '登记ID', dataIndex: 'registrationId', key: 'registrationId', width: 100 },
  { title: '操作', key: 'action', width: 180 }
]

const activeQuickFilter = ref<QuickFilter>('ALL')
const riskFilterMode = ref<RiskFilterMode>('ALL')

const executionRate = computed(() => Number(summary.value.medicalOrderExecutionRate || summary.value.orderCheckRate || 0))
const riskIndex = computed(() => normalizeRiskIndex(summary.value.riskIndex))
const riskLabel = computed(() => resolveMedicalRiskLabel(summary.value.riskLevel))
const riskTagColor = computed(() => resolveMedicalRiskColor(summary.value.riskLevel))

const focusActions = computed(() => summary.value.focusActions || [])
const enrichedRows = computed<MedicationTaskRow[]>(() => {
  const now = dayjs()
  return rows.value.map((item) => ({
    ...item,
    riskMeta: resolveTaskRisk(item, now)
  }))
})

function filterByQuickMode(list: MedicationTaskRow[], mode: QuickFilter) {
  if (mode === 'ALL') {
    return list
  }
  if (mode === 'OVERDUE_PENDING') {
    return list.filter((item) => item.status === 'PENDING' && item.riskMeta.overdueMinutes > 0)
  }
  return list.filter((item) => item.status === mode)
}

function filterByRiskMode(list: MedicationTaskRow[], mode: RiskFilterMode) {
  if (mode === 'HIGH') {
    return list.filter((item) => item.riskMeta.level === 'HIGH')
  }
  if (mode === 'IMMINENT') {
    return list.filter((item) => item.riskMeta.level === 'IMMINENT')
  }
  return list
}

function applyClientFilters(list: MedicationTaskRow[]) {
  return filterByRiskMode(filterByQuickMode(list, activeQuickFilter.value), riskFilterMode.value)
}

const filteredRows = computed(() => applyClientFilters(enrichedRows.value))
const overduePendingCount = computed(() => enrichedRows.value.filter((item) => item.status === 'PENDING' && item.riskMeta.overdueMinutes > 0).length)
const highRiskCount = computed(() => enrichedRows.value.filter((item) => item.riskMeta.level === 'HIGH').length)
const imminentCount = computed(() => enrichedRows.value.filter((item) => item.riskMeta.level === 'IMMINENT').length)
const highRiskQueue = computed(() =>
  [...enrichedRows.value]
    .filter((item) => item.riskMeta.level === 'HIGH')
    .sort((a, b) => {
      const scoreDiff = b.riskMeta.score - a.riskMeta.score
      if (scoreDiff !== 0) {
        return scoreDiff
      }
      const aTime = a.plannedTime ? dayjs(a.plannedTime).valueOf() : Number.MAX_SAFE_INTEGER
      const bTime = b.plannedTime ? dayjs(b.plannedTime).valueOf() : Number.MAX_SAFE_INTEGER
      return aTime - bTime
    })
)

const heroTiles = computed(() => [
  {
    key: 'pending',
    label: '待执行',
    value: summary.value.medicalOrderPendingCount || 0,
    hint: `异常 ${summary.value.medicalOrderAbnormalCount || 0}`
  },
  {
    key: 'done',
    label: '已执行',
    value: summary.value.medicalOrderDoneCount || 0,
    hint: `应执行 ${summary.value.medicalOrderShouldCount || 0}`
  },
  {
    key: 'pending-rate',
    label: '待执行率',
    value: `${formatPercent(summary.value.medicationPendingRate, 2)}%`,
    hint: `策略阈值 ${summaryQuery.value.overdueHours} 小时`
  },
  {
    key: 'risk-index',
    label: '风险指数',
    value: riskIndex.value,
    hint: `${riskLabel.value} · 信号 ${summary.value.riskTriggeredCount || 0}`
  }
])

function parsePositiveNumber(value: unknown) {
  const parsed = Number(value)
  if (!Number.isFinite(parsed) || parsed <= 0) {
    return undefined
  }
  return Math.round(parsed)
}

function parseFilterToStatus(filter?: string) {
  if (!filter) return undefined
  if (filter === 'to_execute' || filter === 'pending' || filter === 'pending_or_abnormal' || filter === 'check_pending') {
    return 'PENDING'
  }
  if (filter === 'done') return 'DONE'
  return undefined
}

function statusLabel(status?: string) {
  if (status === 'PENDING') return '待执行'
  if (status === 'DONE') return '已执行'
  if (status === 'CANCELLED') return '已取消'
  return status || '-'
}

function statusColor(status?: string) {
  if (status === 'DONE') return 'green'
  if (status === 'CANCELLED') return 'default'
  return 'orange'
}

function resolveTaskRisk(task: HealthMedicationTask, now: Dayjs): MedicationTaskRiskMeta {
  if (task.status !== 'PENDING') {
    return {
      level: 'NORMAL',
      score: task.status === 'DONE' ? 8 : 5,
      reason: task.status === 'DONE' ? '任务已执行' : '任务已关闭',
      overdueMinutes: 0,
      remainMinutes: 0
    }
  }

  if (!task.plannedTime) {
    return {
      level: 'IMMINENT',
      score: 56,
      reason: '缺少计划时间，建议尽快补齐',
      overdueMinutes: 0,
      remainMinutes: 0
    }
  }

  const planned = dayjs(task.plannedTime)
  if (!planned.isValid()) {
    return {
      level: 'IMMINENT',
      score: 52,
      reason: '计划时间格式异常，建议校验源数据',
      overdueMinutes: 0,
      remainMinutes: 0
    }
  }

  const diffMinutes = now.diff(planned, 'minute')
  if (diffMinutes >= 180) {
    return {
      level: 'HIGH',
      score: 96,
      reason: `已超时 ${diffMinutes} 分钟（超过 3 小时）`,
      overdueMinutes: diffMinutes,
      remainMinutes: 0
    }
  }
  if (diffMinutes >= 60) {
    return {
      level: 'HIGH',
      score: 88,
      reason: `已超时 ${diffMinutes} 分钟`,
      overdueMinutes: diffMinutes,
      remainMinutes: 0
    }
  }
  if (diffMinutes >= 0) {
    return {
      level: 'HIGH',
      score: 76,
      reason: `刚超时 ${diffMinutes} 分钟`,
      overdueMinutes: diffMinutes,
      remainMinutes: 0
    }
  }

  const remainMinutes = Math.abs(diffMinutes)
  if (remainMinutes <= 30) {
    return {
      level: 'IMMINENT',
      score: 64,
      reason: `30 分钟内到期（剩余 ${remainMinutes} 分钟）`,
      overdueMinutes: 0,
      remainMinutes
    }
  }
  if (remainMinutes <= 120) {
    return {
      level: 'IMMINENT',
      score: 48,
      reason: `2 小时内到期（剩余 ${remainMinutes} 分钟）`,
      overdueMinutes: 0,
      remainMinutes
    }
  }
  return {
    level: 'NORMAL',
    score: 26,
    reason: `计划中（剩余 ${remainMinutes} 分钟）`,
    overdueMinutes: 0,
    remainMinutes
  }
}

function riskLevelLabel(level: TaskRiskLevel) {
  if (level === 'HIGH') return '高风险'
  if (level === 'IMMINENT') return '临近风险'
  return '常规'
}

function riskLevelColor(level: TaskRiskLevel) {
  if (level === 'HIGH') return 'red'
  if (level === 'IMMINENT') return 'orange'
  return 'blue'
}

function riskScoreColor(score: number) {
  if (score >= 90) return 'volcano'
  if (score >= 70) return 'red'
  if (score >= 45) return 'orange'
  return 'blue'
}

function plannedGapText(record: MedicationTaskRow) {
  if (!record.plannedTime || record.status === 'DONE') return '按计划'
  if (record.riskMeta.overdueMinutes > 0) {
    return `逾期 ${record.riskMeta.overdueMinutes} 分钟`
  }
  if (record.riskMeta.remainMinutes > 0) {
    return `剩余 ${record.riskMeta.remainMinutes} 分钟`
  }
  return '临近执行'
}

function plannedGapColor(record: MedicationTaskRow) {
  if (record.status !== 'PENDING') return 'default'
  if (record.riskMeta.level === 'HIGH') return 'red'
  if (record.riskMeta.level === 'IMMINENT') return 'orange'
  return 'blue'
}

function resolveRowClassName(record: MedicationTaskRow) {
  if (record.riskMeta.level === 'HIGH') {
    return 'medical-row-danger'
  }
  if (record.riskMeta.level === 'IMMINENT') {
    return 'medical-row-warning'
  }
  return ''
}

function formatDateTime(value?: string) {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : '-'
}

function severityLabel(level?: string) {
  const normalized = String(level || '').toUpperCase()
  if (normalized === 'HIGH') return '高优'
  if (normalized === 'MEDIUM') return '中优'
  return '常规'
}

function severityColor(level?: string) {
  const normalized = String(level || '').toUpperCase()
  if (normalized === 'HIGH') return 'red'
  if (normalized === 'MEDIUM') return 'orange'
  return 'default'
}

function go(path: string, queryParams?: Record<string, any>) {
  const [pathname, rawQuery] = path.split('?')
  const queryFromPath: Record<string, string> = {}
  if (rawQuery) {
    const searchParams = new URLSearchParams(rawQuery)
    searchParams.forEach((value, key) => {
      if (value) {
        queryFromPath[key] = value
      }
    })
  }
  router.push({
    path: pathname,
    query: {
      ...buildMedicalWorkbenchRouteQuery(summaryQuery.value, {
        ...queryFromPath,
        ...(queryParams || {})
      }),
      keyword: query.keyword || undefined,
      taskDate: query.taskDate || undefined
    }
  })
}

function syncQueryFromRoute() {
  const routeFilterStatus = parseFilterToStatus(typeof route.query.filter === 'string' ? route.query.filter : undefined)
  query.keyword = typeof route.query.keyword === 'string' ? route.query.keyword : ''
  query.status = (typeof route.query.status === 'string' ? route.query.status : routeFilterStatus) || undefined
  const routeDate = typeof route.query.taskDate === 'string' ? route.query.taskDate : typeof route.query.date === 'string' ? route.query.date : ''
  query.taskDate = routeDate && routeDate !== 'today' ? routeDate : dayjs().format('YYYY-MM-DD')
  query.pageNo = 1
  query.pageSize = pagination.pageSize
  pagination.current = 1
  activeQuickFilter.value = 'ALL'
  riskFilterMode.value = 'ALL'

  summaryQuery.value = normalizeMedicalWorkbenchQuery({
    ...MEDICAL_WORKBENCH_QUERY_DEFAULTS,
    ...parseMedicalWorkbenchQueryPatch(route.query),
    date: typeof route.query.date === 'string' ? route.query.date : undefined,
    status: typeof route.query.status === 'string' ? route.query.status : undefined,
    elderId: parsePositiveNumber(route.query.elderId)
  })
  draftSummaryQuery.value = { ...summaryQuery.value }
}

async function loadSummary() {
  try {
    const data = await getMedicalHealthCenterSummary(summaryQuery.value)
    summary.value = {
      ...createMedicalWorkbenchSummaryDefaults(),
      ...(data || {})
    }
  } catch (error) {
    message.error(resolveMedicalError(error, '加载统计失败'))
  }
}

function buildTaskRequestParams(pageNo: number, pageSize: number) {
  const elderId = parsePositiveNumber(summaryQuery.value.elderId)
  return {
    pageNo,
    pageSize,
    keyword: query.keyword || undefined,
    status: query.status || undefined,
    taskDate: query.taskDate || undefined,
    elderId
  }
}

async function fetchTaskData() {
  loading.value = true
  try {
    const res: PageResult<HealthMedicationTask> = await getHealthMedicationTaskPage(buildTaskRequestParams(query.pageNo, query.pageSize))
    rows.value = res.list || []
    pagination.total = res.total || rows.value.length
  } catch (error) {
    message.error(resolveMedicalError(error, '加载医嘱任务失败'))
    rows.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

async function loadAllRowsForExport() {
  exporting.value = true
  try {
    let pageNo = 1
    let total = 0
    const allRows: HealthMedicationTask[] = []
    do {
      const page = await getHealthMedicationTaskPage(buildTaskRequestParams(pageNo, EXPORT_PAGE_SIZE))
      const list = page.list || []
      total = Number(page.total || 0)
      allRows.push(...list)
      pageNo += 1
      if (list.length < EXPORT_PAGE_SIZE) {
        break
      }
    } while (allRows.length < total && pageNo <= EXPORT_PAGE_LIMIT)

    if (allRows.length < total) {
      message.warning(`导出数据过多，已截取前 ${allRows.length} 条`)
    }

    const now = dayjs()
    return allRows.map((item) => ({
      ...item,
      riskMeta: resolveTaskRisk(item, now)
    }))
  } catch (error) {
    message.error(resolveMedicalError(error, '加载导出数据失败'))
    return []
  } finally {
    exporting.value = false
  }
}

function mapExportRows(records: MedicationTaskRow[]) {
  return mapMedicalExportRows(
    records.map((item) => ({
      ...item,
      plannedTime: formatDateTime(item.plannedTime),
      status: statusLabel(item.status),
      doneTime: formatDateTime(item.doneTime),
      riskLevel: riskLevelLabel(item.riskMeta.level),
      riskScore: item.riskMeta.score
    })),
    medicalOrderTaskExportColumns
  )
}

async function exportRows(scope: 'ALL' | 'HIGH') {
  const sourceRows = await loadAllRowsForExport()
  const rowsAfterQuickFilter = filterByQuickMode(sourceRows, activeQuickFilter.value)
  const rowsAfterRiskScope = scope === 'HIGH'
    ? rowsAfterQuickFilter.filter((item) => item.riskMeta.level === 'HIGH')
    : filterByRiskMode(rowsAfterQuickFilter, riskFilterMode.value)
  return mapExportRows(rowsAfterRiskScope)
}

async function reloadAll() {
  await Promise.all([loadSummary(), fetchTaskData()])
}

function onSearch() {
  query.pageNo = 1
  pagination.current = 1
  fetchTaskData()
}

function onReset() {
  query.keyword = ''
  query.status = undefined
  query.taskDate = dayjs().format('YYYY-MM-DD')
  query.pageNo = 1
  query.pageSize = pagination.pageSize
  pagination.current = 1
  activeQuickFilter.value = 'ALL'
  riskFilterMode.value = 'ALL'
  fetchTaskData()
}

function onTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  fetchTaskData()
}

async function exportCsvData() {
  const data = await exportRows('ALL')
  if (!data.length) {
    message.warning('暂无可导出数据')
    return
  }
  exportCsv(data, `医嘱任务-${dayjs().format('YYYYMMDD-HHmmss')}.csv`)
  message.success('CSV导出成功')
}

async function exportExcelData() {
  const data = await exportRows('ALL')
  if (!data.length) {
    message.warning('暂无可导出数据')
    return
  }
  exportExcel(data, `医嘱任务-${dayjs().format('YYYYMMDD-HHmmss')}.xls`)
  message.success('Excel导出成功')
}

async function exportHighRiskCsvData() {
  const data = await exportRows('HIGH')
  if (!data.length) {
    message.warning('当前筛选下暂无高风险任务')
    return
  }
  exportCsv(data, `医嘱任务-高风险-${dayjs().format('YYYYMMDD-HHmmss')}.csv`)
  message.success('高风险CSV导出成功')
}

async function exportHighRiskExcelData() {
  const data = await exportRows('HIGH')
  if (!data.length) {
    message.warning('当前筛选下暂无高风险任务')
    return
  }
  exportExcel(data, `医嘱任务-高风险-${dayjs().format('YYYYMMDD-HHmmss')}.xls`)
  message.success('高风险Excel导出成功')
}

function quickToPending() {
  query.status = 'PENDING'
  activeQuickFilter.value = 'PENDING'
  query.pageNo = 1
  pagination.current = 1
  fetchTaskData()
}

function quickToAbnormal() {
  go('/medical-care/medication-registration', {
    date: query.taskDate,
    filter: 'pending_or_abnormal'
  })
}

function applySummaryFilters() {
  summaryQuery.value = normalizeMedicalWorkbenchQuery({
    ...summaryQuery.value,
    ...draftSummaryQuery.value
  })
  router.replace({
    path: route.path,
    query: {
      ...buildMedicalWorkbenchRouteQuery(summaryQuery.value),
      keyword: query.keyword || undefined,
      taskDate: query.taskDate || undefined
    }
  })
}

function applyQuickFilter(next: QuickFilter) {
  activeQuickFilter.value = next
}

function applyRiskFilter(next: RiskFilterMode) {
  riskFilterMode.value = next
}

watch(
  () => route.query,
  () => {
    syncQueryFromRoute()
    reloadAll()
  },
  { immediate: true }
)
</script>

<style scoped>
.order-hero {
  border: 1px solid #d4e6ff;
  background:
    radial-gradient(130% 110% at 100% 0%, rgba(39, 121, 255, 0.18) 0%, rgba(39, 121, 255, 0) 58%),
    linear-gradient(132deg, #f7fbff 0%, #edf5ff 45%, #f8fbff 100%);
}

.order-hero__title {
  font-size: 14px;
  color: #1f2f45;
  font-weight: 600;
}

.order-hero__value-row {
  margin-top: 8px;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.order-hero__value {
  font-size: 38px;
  line-height: 1;
  font-weight: 700;
  color: #0f1f34;
}

.order-hero__meta {
  margin-bottom: 8px;
  font-size: 12px;
  color: #5f7087;
}

.order-tile {
  border-radius: 10px;
  border: 1px solid #dce9ff;
  background: rgba(255, 255, 255, 0.86);
  padding: 10px;
}

.order-tile__label {
  font-size: 12px;
  color: #66798e;
}

.order-tile__value {
  margin-top: 6px;
  font-size: 24px;
  line-height: 1;
  font-weight: 700;
  color: #10243f;
}

.order-tile__hint {
  margin-top: 6px;
  font-size: 11px;
  color: #73849b;
}

.order-actions {
  min-height: 138px;
  border: 1px solid #dce8ff;
  background:
    radial-gradient(120% 100% at 100% 0%, rgba(67, 143, 255, 0.12) 0%, rgba(67, 143, 255, 0) 55%),
    linear-gradient(145deg, #f8fbff 0%, #f2f7ff 45%, #ffffff 100%);
}

.risk-queue-title {
  margin-top: 10px;
  margin-bottom: 6px;
  padding-top: 8px;
  border-top: 1px dashed #dce8ff;
  font-size: 12px;
  color: #4b5f79;
  font-weight: 600;
}

.risk-queue-meta {
  color: #5f7087;
  font-size: 12px;
}

:deep(.medical-row-danger > td) {
  background: #fff1f0 !important;
}

:deep(.medical-row-warning > td) {
  background: #fffbe6 !important;
}

@media (max-width: 992px) {
  .order-actions {
    min-height: auto;
  }
}
</style>
