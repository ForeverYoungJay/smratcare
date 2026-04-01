<template>
  <PageContainer title="护理与质量中心" subTitle="用药 / 巡查 / 护理计划 / 交接 / 质量治理一体化">
    <template #extra>
      <a-space wrap>
        <a-tag color="blue">业务日期 {{ summary.snapshotDate || '-' }}</a-tag>
        <a-tag color="geekblue">事件窗口 {{ configuredQuery.incidentWindowDays }} 天</a-tag>
        <a-tag color="orange">超时阈值 {{ configuredQuery.overdueHours }} 小时</a-tag>
        <a-button type="primary" @click="reloadData">刷新</a-button>
      </a-space>
    </template>

    <a-card class="card-elevated quality-filter" :bordered="false">
      <a-form layout="inline" @submit.prevent>
        <a-form-item label="统计日期">
          <a-date-picker v-model:value="draftQuery.date" value-format="YYYY-MM-DD" allow-clear style="width: 140px" />
        </a-form-item>
        <a-form-item label="事件窗口">
          <a-select v-model:value="draftQuery.incidentWindowDays" :options="incidentWindowOptions" style="width: 120px" />
        </a-form-item>
        <a-form-item label="超时阈值">
          <a-select v-model:value="draftQuery.overdueHours" :options="overdueHourOptions" style="width: 120px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button @click="applyPreset('stable')">稳健</a-button>
            <a-button @click="applyPreset('balanced')">平衡</a-button>
            <a-button @click="applyPreset('sensitive')">敏感</a-button>
            <a-button type="primary" @click="applyFilters">应用参数</a-button>
            <a-button @click="resetFilters">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated quality-hero" :bordered="false" style="margin-top: 12px">
      <a-row :gutter="[14, 14]" align="middle">
        <a-col :xs="24" :xl="8">
          <div class="quality-hero__title">护理质量风险指数</div>
          <div class="quality-hero__score-row">
            <span class="quality-hero__score">{{ riskIndex }}</span>
            <a-tag :color="riskTagColor">{{ riskLabel }}</a-tag>
          </div>
          <a-progress :percent="riskIndex" :status="riskIndex >= 75 ? 'exception' : riskIndex >= 55 ? 'active' : 'normal'" />
          <div class="quality-hero__meta">
            风险信号 {{ riskSignals.length }} 项 · 近{{ configuredQuery.incidentWindowDays }}天事件率 {{ formatPercent(summary.incident30dRate, 2) }}%
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

    <a-card class="card-elevated trend-card" :bordered="false" style="margin-top: 12px" title="风险日趋势（窗口内）">
      <template #extra>
        <a-space>
          <a-tag color="blue">趋势判断：{{ trendSummary }}</a-tag>
          <a-tag color="purple">样本 {{ timelineRows.length }} 天</a-tag>
        </a-space>
      </template>
      <a-spin :spinning="timelineLoading">
        <a-empty v-if="!timelineRows.length" description="暂无趋势数据" />
        <div v-else class="trend-list">
          <div class="trend-item" v-for="item in timelineRows" :key="item.date">
            <div class="trend-date">{{ shortDate(item.date) }}</div>
            <div class="trend-bar-track">
              <div class="trend-bar-fill" :style="{ width: `${trendWidth(item.riskScore)}%`, background: trendColor(item.riskScore) }" />
            </div>
            <a-tag :color="trendTagColor(item.riskScore)">{{ Number(item.riskScore || 0) }}</a-tag>
            <div class="trend-meta">
              待执行 {{ item.medicationPendingCount || 0 }} · 巡检 {{ item.inspectionOpenCount || 0 }} · 超时 {{ item.careOverdueCount || 0 }} · 事件 {{ item.incidentCount || 0 }}
            </div>
          </div>
        </div>
      </a-spin>
    </a-card>

    <a-row :gutter="16" style="margin-top: 12px; margin-bottom: 12px">
      <a-col :xs="24" :xl="14">
        <a-card class="card-elevated" :bordered="false" title="推荐治理动作">
          <a-empty v-if="!focusActions.length" description="当前无高风险治理动作" />
          <a-list v-else :data-source="focusActions" :pagination="false">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta :title="item.title" :description="item.description" />
                <template #actions>
                  <a-tag :color="severityColor(item.severity)">{{ severityLabel(item.severity) }}</a-tag>
                  <a-button type="link" @click="go(item.route || '/medical-care/unified-task-center')">去处理</a-button>
                </template>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>
      <a-col :xs="24" :xl="10">
        <a-card class="card-elevated" :bordered="false" title="风险信号明细">
          <a-list size="small" :data-source="riskSignals" :pagination="false" :locale="{ emptyText: '暂无显著风险信号' }">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-space>
                  <a-tag :color="riskTagColor">{{ riskLabel }}</a-tag>
                  <span>{{ item }}</span>
                </a-space>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="12" style="margin-bottom: 12px">
      <a-col :xs="12" :lg="4"><a-card :bordered="false" class="card-elevated"><a-statistic title="用药待执行" :value="summary.todayMedicationPendingCount || 0" /></a-card></a-col>
      <a-col :xs="12" :lg="4"><a-card :bordered="false" class="card-elevated"><a-statistic title="巡检待闭环" :value="summary.abnormalInspectionOpenCount || 0" /></a-card></a-col>
      <a-col :xs="12" :lg="4"><a-card :bordered="false" class="card-elevated"><a-statistic title="日志待补录" :value="summary.nursingLogPendingCount || 0" /></a-card></a-col>
      <a-col :xs="12" :lg="4"><a-card :bordered="false" class="card-elevated"><a-statistic title="交接待完成" :value="summary.handoverPendingCount || 0" /></a-card></a-col>
      <a-col :xs="12" :lg="4"><a-card :bordered="false" class="card-elevated"><a-statistic title="整改逾期" :value="summary.rectifyOverdueCount || 0" /></a-card></a-col>
      <a-col :xs="12" :lg="4"><a-card :bordered="false" class="card-elevated"><a-statistic title="异常未闭环" :value="summary.unclosedAbnormalCount || 0" /></a-card></a-col>
    </a-row>

    <a-tabs v-model:activeKey="activeTab" type="card">
      <a-tab-pane key="medication" tab="用药管理">
        <a-space wrap>
          <a-button type="primary" @click="go('/health/medication/medication-setting')">用药设置</a-button>
          <a-button @click="go('/medical-care/medication-registration')">用药登记</a-button>
          <a-button @click="go('/health/medication/medication-remaining')">剩余用药</a-button>
          <a-button @click="go('/health/medication/drug-deposit')">带药外出/缴存</a-button>
          <a-button @click="go('/health/medication/drug-dictionary')">过敏/禁忌提醒规则</a-button>
        </a-space>
      </a-tab-pane>
      <a-tab-pane key="vital" tab="健康数据与巡查">
        <a-space wrap>
          <a-button type="primary" @click="go('/health/management/data?date=today')">生命体征采集</a-button>
          <a-button @click="go('/medical-care/inspection?date=today')">健康巡检</a-button>
          <a-button @click="go('/medical-care/nursing-log?filter=pending')">护理日志</a-button>
          <a-button @click="go('/medical-care/inspection?filter=abnormal&date=today')">异常预警清单</a-button>
        </a-space>
      </a-tab-pane>
      <a-tab-pane key="plan" tab="护理计划与等级">
        <a-space wrap>
          <a-button type="primary" @click="go('/care/service/care-levels')">护理等级规则</a-button>
          <a-button @click="go('/care/service/service-items')">护理项目库</a-button>
          <a-button @click="go('/care/service/service-plans')">服务计划（周/月）</a-button>
          <a-button @click="go('/care/care-packages')">护理套餐库</a-button>
        </a-space>
      </a-tab-pane>
      <a-tab-pane key="task" tab="护理任务与扫码执行">
        <a-space wrap>
          <a-button type="primary" @click="go('/medical-care/care-task-board?date=today&filter=all')">今日任务清单</a-button>
          <a-button type="primary" @click="go('/medical-care/unified-task-center')">统一任务中心</a-button>
          <a-button @click="go('/care/workbench/qr?mode=scan')">扫码执行</a-button>
          <a-button @click="go('/medical-care/care-task-board?date=today&filter=overdue')">超时与补录</a-button>
          <a-button @click="go('/medical-care/care-task-board?date=today&filter=overdue_or_missed')">任务异常入口</a-button>
        </a-space>
      </a-tab-pane>
      <a-tab-pane key="handover" tab="交接班与班组">
        <a-space wrap>
          <a-button type="primary" @click="go('/medical-care/handovers?shift=当前班次')">交接清单</a-button>
          <a-button @click="go('/medical-care/handovers?tab=records')">交接记录</a-button>
          <a-button @click="go('/care/staff/caregiver-groups')">护士/护工分组</a-button>
          <a-button @click="go('/care/scheduling/shift-calendar')">排班查看与调整申请</a-button>
        </a-space>
      </a-tab-pane>
      <a-tab-pane key="pharmacy" tab="药库与药品主数据">
        <a-space wrap>
          <a-button type="primary" @click="go('/health/medication/drug-dictionary')">药品字典</a-button>
          <a-button @click="go('/logistics/storage/warehouse')">药库库存</a-button>
          <a-button @click="go('/logistics/storage/inbound')">入库</a-button>
          <a-button @click="go('/logistics/storage/outbound')">出库</a-button>
          <a-button @click="go('/logistics/storage/supplier')">供应商</a-button>
          <a-button @click="go('/logistics/storage/purchase?filter=pending')">领药申请/发药记录</a-button>
        </a-space>
      </a-tab-pane>
      <a-tab-pane key="quality" tab="质量与报表">
        <a-space wrap>
          <a-button type="primary" @click="go('/care/service/nursing-reports')">护理报表</a-button>
          <a-button @click="go('/medical-care/alert-rules')">异常规则配置</a-button>
          <a-button @click="go('/stats/org/monthly-operation')">执行率/覆盖率</a-button>
          <a-button @click="go('/survey/stats?filter=low_score_or_complaint')">投诉问卷分析</a-button>
          <a-button @click="go('/oa/work-execution/task?filter=overdue_rectify')">整改任务闭环</a-button>
          <a-button @click="go('/hr/performance')">绩效数据输出</a-button>
        </a-space>
      </a-tab-pane>
    </a-tabs>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { getMedicalHealthCenterSummary, getMedicalRiskTimeline } from '../../api/medicalCare'
import type { MedicalCareWorkbenchSummary, MedicalCareWorkbenchSummaryQuery, MedicalRiskTimelinePoint } from '../../types'
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
import { resolveMedicalError } from './medicalError'
import { isAutoCarryResidentContextEnabled, syncMedicalAlertRules } from '../../utils/medicalAlertRule'

type PresetKey = 'stable' | 'balanced' | 'sensitive'

const QUERY_PRESETS: Record<PresetKey, Required<Pick<MedicalCareWorkbenchSummaryQuery, 'incidentWindowDays' | 'overdueHours'>>> = {
  stable: { incidentWindowDays: 45, overdueHours: 18 },
  balanced: { incidentWindowDays: 30, overdueHours: 12 },
  sensitive: { incidentWindowDays: 14, overdueHours: 6 }
}

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

const FILTER_KEYS = new Set(['date', 'status', 'elderId', 'incidentWindowDays', 'overdueHours', 'topResidentLimit', 'riskResidentLookbackDays'])

const router = useRouter()
const route = useRoute()

const summary = ref<MedicalCareWorkbenchSummary>(createMedicalWorkbenchSummaryDefaults())
const timelineRows = ref<MedicalRiskTimelinePoint[]>([])
const timelineLoading = ref(false)
const activeQuery = ref<MedicalCareWorkbenchSummaryQuery>(normalizeMedicalWorkbenchQuery(MEDICAL_WORKBENCH_QUERY_DEFAULTS))
const draftQuery = ref<MedicalCareWorkbenchSummaryQuery>(normalizeMedicalWorkbenchQuery(MEDICAL_WORKBENCH_QUERY_DEFAULTS))

const activeTab = computed({
  get: () => (typeof route.query.tab === 'string' ? route.query.tab : 'medication'),
  set: (tab: string) => {
    router.replace({ query: { ...route.query, tab } })
  }
})

const configuredQuery = computed(() =>
  normalizeMedicalWorkbenchQuery({
    date: summary.value.snapshotDate || activeQuery.value.date,
    status: activeQuery.value.status,
    elderId: activeQuery.value.elderId,
    incidentWindowDays: summary.value.configuredIncidentWindowDays ?? activeQuery.value.incidentWindowDays,
    overdueHours: summary.value.configuredOverdueHours ?? activeQuery.value.overdueHours,
    topResidentLimit: summary.value.configuredTopResidentLimit ?? activeQuery.value.topResidentLimit,
    riskResidentLookbackDays: summary.value.configuredRiskResidentLookbackDays ?? activeQuery.value.riskResidentLookbackDays
  })
)

const riskIndex = computed(() => normalizeRiskIndex(summary.value.riskIndex))
const riskLabel = computed(() => resolveMedicalRiskLabel(summary.value.riskLevel))
const riskTagColor = computed(() => resolveMedicalRiskColor(summary.value.riskLevel))

const focusActions = computed(() => summary.value.focusActions || [])
const riskSignals = computed(() => {
  const rows = summary.value.riskSignals || []
  if (rows.length > 0) {
    return rows
  }
  const fallback: string[] = []
  if (Number(summary.value.todayMedicationPendingCount || 0) > 0) {
    fallback.push(`用药待执行 ${summary.value.todayMedicationPendingCount} 条`)
  }
  if (Number(summary.value.abnormalInspectionOpenCount || 0) > 0) {
    fallback.push(`开放异常巡检 ${summary.value.abnormalInspectionOpenCount} 条`)
  }
  if (Number(summary.value.rectifyOverdueCount || 0) > 0) {
    fallback.push(`整改逾期 ${summary.value.rectifyOverdueCount} 条`)
  }
  return fallback
})

const heroTiles = computed(() => [
  {
    key: 'medication-pending',
    label: '用药待执行',
    value: summary.value.todayMedicationPendingCount || 0,
    hint: `待执行率 ${formatPercent(summary.value.medicationPendingRate, 2)}%`
  },
  {
    key: 'inspection-open',
    label: '巡检待闭环',
    value: summary.value.abnormalInspectionOpenCount || 0,
    hint: `完成率 ${formatPercent(summary.value.inspectionCompletionRate, 1)}%`
  },
  {
    key: 'care-overdue',
    label: '护理超时',
    value: summary.value.overdueCareTaskCount || 0,
    hint: `超时率 ${formatPercent(summary.value.careTaskOverdueRate, 2)}%`
  },
  {
    key: 'rectify-overdue',
    label: '整改逾期',
    value: summary.value.rectifyOverdueCount || 0,
    hint: `未闭环异常 ${summary.value.unclosedAbnormalCount || 0}`
  }
])

const trendSummary = computed(() => {
  if (!timelineRows.value.length) {
    return '暂无数据'
  }
  const first = Number(timelineRows.value[0]?.riskScore || 0)
  const last = Number(timelineRows.value[timelineRows.value.length - 1]?.riskScore || 0)
  const delta = last - first
  if (delta >= 10) {
    return `风险上行 +${delta}`
  }
  if (delta <= -10) {
    return `风险下行 ${delta}`
  }
  return `风险平稳 ${delta >= 0 ? '+' : ''}${delta}`
})

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

function pickStableQueryContext() {
  const passThrough: Record<string, string> = {}
  Object.entries(route.query || {}).forEach(([key, value]) => {
    if (FILTER_KEYS.has(key)) {
      return
    }
    if (Array.isArray(value)) {
      if (value.length > 0 && value[0]) {
        passThrough[key] = String(value[0])
      }
      return
    }
    if (value != null && value !== '') {
      passThrough[key] = String(value)
    }
  })
  return passThrough
}

function routeFilters() {
  const routePatch = parseMedicalWorkbenchQueryPatch(route.query)
  const carryResident = isAutoCarryResidentContextEnabled()
  const elderIdRaw = route.query.elderId ?? route.query.residentId
  const elderId = elderIdRaw == null ? undefined : String(Array.isArray(elderIdRaw) ? elderIdRaw[0] || '' : elderIdRaw).trim() || undefined
  return normalizeMedicalWorkbenchQuery({
    ...MEDICAL_WORKBENCH_QUERY_DEFAULTS,
    ...routePatch,
    elderId: carryResident ? elderId : routePatch.elderId,
    date: typeof route.query.date === 'string' ? route.query.date : routePatch.date,
    status: typeof route.query.status === 'string' ? route.query.status : routePatch.status
  })
}

function go(path: string) {
  const [pathname, search] = path.split('?')
  const parsed = new URLSearchParams(search || '')
  const query: Record<string, any> = {}
  parsed.forEach((value, key) => {
    query[key] = value
  })
  const residentId = route.query.residentId ?? route.query.elderId
  const carryResident = isAutoCarryResidentContextEnabled()
  if (carryResident && residentId != null && query.residentId == null && query.elderId == null) {
    query.residentId = residentId
    query.elderId = residentId
  }
  const normalizedRouteQuery = buildMedicalWorkbenchRouteQuery(configuredQuery.value, query)
  router.push({
    path: pathname,
    query: {
      ...pickStableQueryContext(),
      ...normalizedRouteQuery
    }
  })
}

async function loadSummary() {
  try {
    const data = await getMedicalHealthCenterSummary(activeQuery.value)
    summary.value = {
      ...createMedicalWorkbenchSummaryDefaults(),
      ...(data || {})
    }
  } catch (error) {
    message.error(resolveMedicalError(error, '加载护理与质量中心失败'))
  }
}

async function loadRiskTimeline() {
  timelineLoading.value = true
  try {
    const data = await getMedicalRiskTimeline({
      elderId: activeQuery.value.elderId,
      date: activeQuery.value.date,
      overdueHours: activeQuery.value.overdueHours,
      windowDays: activeQuery.value.incidentWindowDays
    })
    timelineRows.value = data || []
  } catch (error) {
    message.error(resolveMedicalError(error, '加载风险趋势失败'))
    timelineRows.value = []
  } finally {
    timelineLoading.value = false
  }
}

async function reloadData() {
  await Promise.all([loadSummary(), loadRiskTimeline()])
}

function applyPreset(preset: PresetKey) {
  draftQuery.value = {
    ...draftQuery.value,
    ...QUERY_PRESETS[preset]
  }
}

function applyFilters() {
  const next = normalizeMedicalWorkbenchQuery({ ...activeQuery.value, ...draftQuery.value })
  router.replace({
    path: route.path,
    query: {
      ...pickStableQueryContext(),
      ...buildMedicalWorkbenchRouteQuery(next, { tab: activeTab.value })
    }
  })
}

function resetFilters() {
  draftQuery.value = normalizeMedicalWorkbenchQuery(MEDICAL_WORKBENCH_QUERY_DEFAULTS)
  applyFilters()
}

function shortDate(value?: string) {
  if (!value) return '-'
  return String(value).slice(5)
}

function trendWidth(score?: number) {
  return Math.max(6, Math.min(100, Number(score || 0)))
}

function trendColor(score?: number) {
  const current = Number(score || 0)
  if (current >= 75) return 'linear-gradient(90deg, #ff6b6b, #cf1322)'
  if (current >= 55) return 'linear-gradient(90deg, #ffb84d, #d46b08)'
  if (current >= 30) return 'linear-gradient(90deg, #4c9dff, #125dd7)'
  return 'linear-gradient(90deg, #86d96f, #389e0d)'
}

function trendTagColor(score?: number) {
  const current = Number(score || 0)
  if (current >= 75) return 'red'
  if (current >= 55) return 'orange'
  if (current >= 30) return 'blue'
  return 'green'
}

watch(
  () => route.query,
  () => {
    const query = routeFilters()
    activeQuery.value = { ...query }
    draftQuery.value = { ...query }
    reloadData().catch(() => {})
  },
  { immediate: true }
)

onMounted(() => {
  syncMedicalAlertRules().catch(() => {})
})
</script>

<style scoped>
.quality-filter {
  border: 1px solid #d7e9ff;
  background:
    radial-gradient(130% 120% at 0% 0%, rgba(40, 126, 255, 0.12) 0%, rgba(40, 126, 255, 0) 56%),
    linear-gradient(135deg, #f5faff 0%, #edf5ff 45%, #f8fbff 100%);
}

.quality-hero {
  border: 1px solid #d4e6ff;
  background:
    radial-gradient(130% 110% at 100% 0%, rgba(39, 121, 255, 0.18) 0%, rgba(39, 121, 255, 0) 58%),
    linear-gradient(132deg, #f7fbff 0%, #edf5ff 45%, #f8fbff 100%);
}

.quality-hero__title {
  font-size: 14px;
  color: #1f2f45;
  font-weight: 600;
}

.quality-hero__score-row {
  margin-top: 8px;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.quality-hero__score {
  font-size: 38px;
  line-height: 1;
  font-weight: 700;
  color: #0f1f34;
}

.quality-hero__meta {
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

.trend-card {
  border: 1px solid #dae7ff;
  background: linear-gradient(165deg, #ffffff 0%, #f5f9ff 100%);
}

.trend-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.trend-item {
  display: grid;
  grid-template-columns: 58px 1fr 62px 340px;
  gap: 8px;
  align-items: center;
}

.trend-date {
  font-size: 12px;
  color: #64748b;
}

.trend-bar-track {
  width: 100%;
  height: 10px;
  border-radius: 999px;
  background: #eaf1ff;
  overflow: hidden;
}

.trend-bar-fill {
  height: 100%;
  border-radius: 999px;
  transition: width 0.25s ease;
}

.trend-meta {
  font-size: 12px;
  color: #5f7087;
}

@media (max-width: 992px) {
  .quality-filter :deep(.ant-form-item) {
    width: 100%;
    margin-inline-end: 0;
  }

  .trend-item {
    grid-template-columns: 52px 1fr 56px;
    grid-template-rows: auto auto;
  }

  .trend-meta {
    grid-column: 1 / 4;
  }
}
</style>
