<template>
  <PageContainer title="医护健康服务中心" subTitle="医嘱、护理、巡检与风险处置一体化入口">
    <template #extra>
      <a-space wrap>
        <a-tag color="blue">业务日期 {{ summary.snapshotDate || '-' }}</a-tag>
        <a-tag color="geekblue">窗口 {{ configuredQuery.incidentWindowDays }} 天</a-tag>
        <a-tag :color="riskTagColor">{{ riskLabel }}</a-tag>
        <a-button @click="copyShareLink">复制分享链接</a-button>
        <a-button type="primary" ghost @click="loadSummary">刷新</a-button>
      </a-space>
    </template>

    <a-card class="card-elevated medical-filter" :bordered="false">
      <div class="medical-filter__head">
        <a-space wrap>
          <a-tag color="cyan">超时阈值 {{ configuredQuery.overdueHours }} 小时</a-tag>
          <a-tag color="purple">重点长者 Top{{ configuredQuery.topResidentLimit }}</a-tag>
          <a-tag color="gold">回溯 {{ configuredQuery.riskResidentLookbackDays }} 天</a-tag>
        </a-space>
        <a-space wrap>
          <a-button size="small" @click="applyPreset('stable')">稳健</a-button>
          <a-button size="small" @click="applyPreset('balanced')">平衡</a-button>
          <a-button size="small" @click="applyPreset('sensitive')">敏感</a-button>
          <a-button size="small" type="primary" @click="applyFilters">应用参数</a-button>
          <a-button size="small" @click="resetFilters">重置</a-button>
        </a-space>
      </div>
      <a-form layout="inline" class="medical-filter__form" @submit.prevent>
        <a-form-item label="统计日期">
          <a-date-picker v-model:value="draftQuery.date" value-format="YYYY-MM-DD" allow-clear style="width: 144px" />
        </a-form-item>
        <a-form-item label="事件窗口">
          <a-select v-model:value="draftQuery.incidentWindowDays" :options="incidentWindowOptions" style="width: 128px" />
        </a-form-item>
        <a-form-item label="超时判定">
          <a-select v-model:value="draftQuery.overdueHours" :options="overdueHourOptions" style="width: 128px" />
        </a-form-item>
        <a-form-item label="重点居民">
          <a-select v-model:value="draftQuery.topResidentLimit" :options="topResidentOptions" style="width: 128px" />
        </a-form-item>
        <a-form-item label="回溯天数">
          <a-select v-model:value="draftQuery.riskResidentLookbackDays" :options="lookbackOptions" style="width: 128px" />
        </a-form-item>
      </a-form>
    </a-card>

    <StatefulBlock :loading="loading" :error="errorText" :empty="false" @retry="loadSummary">
      <a-card class="card-elevated risk-hero" :bordered="false">
        <a-row :gutter="[14, 14]" align="middle">
          <a-col :xs="24" :xl="8">
            <div class="risk-hero__title">医疗执行风险指数</div>
            <div class="risk-hero__score-row">
              <span class="risk-hero__score">{{ riskIndex }}</span>
              <a-tag :color="riskTagColor">{{ riskLabel }}</a-tag>
            </div>
            <a-progress :percent="riskIndex" :status="progressStatus" :stroke-color="progressGradient" />
            <div class="risk-hero__meta">
              触发信号 {{ Number(summary.riskTriggeredCount || riskSignals.length) }} 项 · 生成 {{ generatedAtText }}
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

      <a-alert
        v-if="summary.overdueCareTaskCount > 0 || summary.unclosedAbnormalCount > 0 || summary.todayMedicationPendingCount > 0"
        type="warning"
        show-icon
        style="margin-top: 12px"
        :message="alertText"
      />

      <a-row :gutter="16" style="margin-top: 12px">
        <a-col :xs="24" :xl="14">
          <a-card class="card-elevated" :bordered="false" title="推荐处置动作">
            <a-empty v-if="!focusActions.length" description="暂无推荐动作" />
            <a-list v-else :data-source="focusActions" :pagination="false">
              <template #renderItem="{ item }">
                <a-list-item>
                  <a-list-item-meta :title="item.title" :description="item.description" />
                  <template #actions>
                    <a-tag :color="severityColor(item.severity)">{{ severityLabel(item.severity) }}</a-tag>
                    <a-tag v-if="Number(item.count || 0) > 0" color="default">{{ item.count }}</a-tag>
                    <a-button type="link" @click="go(item.route || '/medical-care/unified-task-center')">去处理</a-button>
                  </template>
                </a-list-item>
              </template>
            </a-list>
          </a-card>
        </a-col>
        <a-col :xs="24" :xl="10">
          <a-card class="card-elevated" :bordered="false" title="风险信号明细">
            <a-list size="small" :data-source="riskSignals" :pagination="false" :locale="{ emptyText: '当前参数下暂无显著风险信号' }">
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

      <a-row :gutter="16" style="margin-top: 12px">
        <a-col :xs="24" :sm="12" :xl="6" v-for="stat in keyStats" :key="stat.key" style="margin-bottom: 12px">
          <a-card :bordered="false" class="card-elevated stat-card" @click="go(stat.route)">
            <div class="stat-card__label">{{ stat.label }}</div>
            <div class="stat-card__value">{{ stat.value }}</div>
            <div class="stat-card__hint">{{ stat.hint }}</div>
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16" style="margin-top: 4px">
        <a-col :xs="24" :xl="12" style="margin-bottom: 12px">
          <a-card class="card-elevated" :bordered="false" title="执行任务总览">
            <a-space wrap>
              <a-button @click="go('/medical-care/orders', { status: 'PENDING' })">待执行医嘱 {{ summary.medicalOrderPendingCount || 0 }}</a-button>
              <a-button @click="go('/medical-care/care-task-board', { status: 'OVERDUE' })">超时护理 {{ summary.overdueCareTaskCount || 0 }}</a-button>
              <a-button @click="go('/medical-care/inspection', { status: 'ABNORMAL' })">异常巡检 {{ summary.abnormalInspectionOpenCount || 0 }}</a-button>
              <a-button @click="go('/medical-care/nursing-log', { status: 'PENDING' })">日志待补录 {{ summary.nursingLogPendingCount || 0 }}</a-button>
            </a-space>
            <a-descriptions :column="2" size="small" bordered style="margin-top: 12px">
              <a-descriptions-item label="医嘱完成率">{{ formatPercent(summary.medicalOrderExecutionRate, 1) }}%</a-descriptions-item>
              <a-descriptions-item label="护理完成率">{{ formatPercent(summary.careTaskCompletionRate, 1) }}%</a-descriptions-item>
              <a-descriptions-item label="巡检完成率">{{ formatPercent(summary.inspectionCompletionRate, 1) }}%</a-descriptions-item>
              <a-descriptions-item label="整改逾期">{{ summary.rectifyOverdueCount || 0 }}</a-descriptions-item>
            </a-descriptions>
          </a-card>
        </a-col>
        <a-col :xs="24" :xl="12" style="margin-bottom: 12px">
          <a-card class="card-elevated" :bordered="false" title="重点长者关注">
            <a-empty v-if="!summary.keyResidents?.length" description="暂无高风险长者" />
            <a-list v-else size="small" :data-source="summary.keyResidents.slice(0, configuredQuery.topResidentLimit)">
              <template #renderItem="{ item }">
                <a-list-item>
                  <a-list-item-meta :title="item.elderName || '-'" :description="item.keyRiskFactors || '暂无关键风险因子'" />
                  <template #actions>
                    <a-tag :color="residentRiskColor(item.riskLevel)">{{ item.riskLevel || '-' }}</a-tag>
                    <a-button type="link" @click="go('/elder/resident-360', { residentId: item.elderId })">长者360</a-button>
                  </template>
                </a-list-item>
              </template>
            </a-list>
          </a-card>
        </a-col>
      </a-row>

      <a-card class="card-elevated" :bordered="false" title="模块快捷入口" style="margin-top: 4px">
        <a-space wrap>
          <a-button type="primary" @click="go('/medical-care/orders')">医嘱管理</a-button>
          <a-button type="primary" @click="go('/medical-care/unified-task-center')">统一任务中心</a-button>
          <a-button @click="go('/medical-care/nursing-quality?tab=medication')">用药管理</a-button>
          <a-button @click="go('/medical-care/inspection')">健康巡检</a-button>
          <a-button @click="go('/medical-care/care-task-board')">护理任务看板</a-button>
          <a-button @click="go('/medical-care/handovers')">交接班</a-button>
          <a-button @click="go('/medical-care/nursing-quality?tab=quality')">质量与报表</a-button>
          <a-button @click="go('/medical-care/ai-reports')">AI 健康报告</a-button>
          <a-button @click="go('/medical-care/workbench')">高级工作台</a-button>
        </a-space>
      </a-card>
    </StatefulBlock>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
import { getMedicalHealthCenterSummary } from '../../api/medicalCare'
import type { MedicalCareWorkbenchSummary, MedicalCareWorkbenchSummaryQuery } from '../../types'
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

const QUERY_PRESETS: Record<PresetKey, Required<Pick<MedicalCareWorkbenchSummaryQuery, 'incidentWindowDays' | 'overdueHours' | 'topResidentLimit' | 'riskResidentLookbackDays'>>> = {
  stable: { incidentWindowDays: 45, overdueHours: 18, topResidentLimit: 5, riskResidentLookbackDays: 240 },
  balanced: { incidentWindowDays: 30, overdueHours: 12, topResidentLimit: 8, riskResidentLookbackDays: 180 },
  sensitive: { incidentWindowDays: 14, overdueHours: 6, topResidentLimit: 10, riskResidentLookbackDays: 120 }
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
const topResidentOptions = [
  { label: 'Top3', value: 3 },
  { label: 'Top5', value: 5 },
  { label: 'Top8', value: 8 },
  { label: 'Top10', value: 10 },
  { label: 'Top15', value: 15 }
]
const lookbackOptions = [
  { label: '90天', value: 90 },
  { label: '120天', value: 120 },
  { label: '180天', value: 180 },
  { label: '240天', value: 240 },
  { label: '365天', value: 365 }
]

const FILTER_KEYS = new Set(['date', 'status', 'elderId', 'incidentWindowDays', 'overdueHours', 'topResidentLimit', 'riskResidentLookbackDays'])

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const errorText = ref('')
const summary = ref<MedicalCareWorkbenchSummary>(createMedicalWorkbenchSummaryDefaults())

const activeQuery = ref<MedicalCareWorkbenchSummaryQuery>(normalizeMedicalWorkbenchQuery(MEDICAL_WORKBENCH_QUERY_DEFAULTS))
const draftQuery = ref<MedicalCareWorkbenchSummaryQuery>(normalizeMedicalWorkbenchQuery(MEDICAL_WORKBENCH_QUERY_DEFAULTS))

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
const generatedAtText = computed(() => (summary.value.generatedAt ? dayjs(summary.value.generatedAt).format('MM-DD HH:mm:ss') : '-'))

const progressStatus = computed(() => {
  if (riskIndex.value >= 75) return 'exception'
  if (riskIndex.value >= 55) return 'active'
  return 'normal'
})

const progressGradient = computed(() => {
  if (riskIndex.value >= 75) return { '0%': '#ff5b67', '100%': '#c6172c' }
  if (riskIndex.value >= 55) return { '0%': '#ffb23d', '100%': '#d46b08' }
  if (riskIndex.value >= 30) return { '0%': '#3c8bff', '100%': '#125dd7' }
  return { '0%': '#52c41a', '100%': '#389e0d' }
})

const heroTiles = computed(() => [
  {
    key: 'medical-order-exec',
    label: '医嘱完成率',
    value: `${formatPercent(summary.value.medicalOrderExecutionRate, 1)}%`,
    hint: `已执行 ${summary.value.medicalOrderDoneCount || 0} / 应执行 ${summary.value.medicalOrderShouldCount || 0}`
  },
  {
    key: 'care-completion',
    label: '护理完成率',
    value: `${formatPercent(summary.value.careTaskCompletionRate, 1)}%`,
    hint: `已完成 ${summary.value.careTaskDoneCount || 0} / 应做 ${summary.value.careTaskShouldCount || 0}`
  },
  {
    key: 'care-overdue-rate',
    label: '护理超时率',
    value: `${formatPercent(summary.value.careTaskOverdueRate, 2)}%`,
    hint: `超时阈值 ${configuredQuery.value.overdueHours} 小时`
  },
  {
    key: 'incident-window-rate',
    label: `近${configuredQuery.value.incidentWindowDays}天事件率`,
    value: `${formatPercent(summary.value.incident30dRate, 2)}%`,
    hint: `${summary.value.incidentWindowStartDate || '-'} 至 ${summary.value.incidentWindowEndDate || '-'}`
  }
])

const alertText = computed(
  () =>
    `执行提醒：护理超时 ${summary.value.overdueCareTaskCount || 0} 条，未闭环异常 ${summary.value.unclosedAbnormalCount || 0} 条，用药待执行 ${summary.value.todayMedicationPendingCount || 0} 条。`
)

const riskSignals = computed(() => {
  const serverSignals = summary.value.riskSignals || []
  if (serverSignals.length > 0) {
    return serverSignals
  }
  const rows: string[] = []
  if (Number(summary.value.overdueCareTaskCount || 0) > 0) {
    rows.push(`护理超时 ${summary.value.overdueCareTaskCount} 条`)
  }
  if (Number(summary.value.abnormalInspectionOpenCount || 0) > 0) {
    rows.push(`开放异常巡检 ${summary.value.abnormalInspectionOpenCount} 条`)
  }
  if (Number(summary.value.rectifyOverdueCount || 0) > 0) {
    rows.push(`整改任务逾期 ${summary.value.rectifyOverdueCount} 条`)
  }
  return rows
})

const focusActions = computed(() => {
  const actions = summary.value.focusActions || []
  if (actions.length > 0) {
    return actions
  }
  return [
    {
      key: 'fallback',
      title: '查看统一任务中心',
      description: '当前风险信号较少，建议按班次核对例行任务。',
      route: '/medical-care/unified-task-center',
      severity: 'LOW',
      count: 0
    }
  ]
})

const keyStats = computed(() => [
  {
    key: 'medical-order-pending',
    label: '医嘱待执行',
    value: summary.value.medicalOrderPendingCount || 0,
    hint: `异常 ${summary.value.medicalOrderAbnormalCount || 0}`,
    route: '/medical-care/orders?status=PENDING'
  },
  {
    key: 'care-overdue',
    label: '护理超时',
    value: summary.value.overdueCareTaskCount || 0,
    hint: `超时率 ${formatPercent(summary.value.careTaskOverdueRate, 2)}%`,
    route: '/medical-care/care-task-board?status=OVERDUE'
  },
  {
    key: 'inspection-open',
    label: '开放巡检异常',
    value: summary.value.abnormalInspectionOpenCount || 0,
    hint: `今日计划 ${summary.value.todayInspectionPlanCount || 0}`,
    route: '/medical-care/inspection?status=ABNORMAL'
  },
  {
    key: 'high-risk',
    label: '高风险长者',
    value: summary.value.topRiskResidentCount || 0,
    hint: `回溯 ${configuredQuery.value.riskResidentLookbackDays} 天`,
    route: '/medical-care/residents?status=HIGH_RISK'
  }
])

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

function residentRiskColor(level?: string) {
  return resolveMedicalRiskColor(level)
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
  const elderId = elderIdRaw == null ? undefined : Number(elderIdRaw)
  const merged = normalizeMedicalWorkbenchQuery({
    ...MEDICAL_WORKBENCH_QUERY_DEFAULTS,
    ...routePatch,
    elderId: carryResident && Number.isFinite(elderId) ? elderId : routePatch.elderId,
    date: typeof route.query.date === 'string' ? route.query.date : routePatch.date,
    status: typeof route.query.status === 'string' ? route.query.status : routePatch.status
  })
  return merged
}

function go(path: string, extra?: Record<string, string | number | undefined>) {
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
  const routeQuery = buildMedicalWorkbenchRouteQuery(configuredQuery.value, {
    ...queryFromPath,
    ...(extra || {})
  })
  router.push({
    path: pathname,
    query: {
      ...pickStableQueryContext(),
      ...routeQuery
    }
  })
}

async function loadSummary() {
  loading.value = true
  errorText.value = ''
  try {
    const data = await getMedicalHealthCenterSummary(activeQuery.value)
    summary.value = {
      ...createMedicalWorkbenchSummaryDefaults(),
      ...(data || {})
    }
  } catch (error: any) {
    errorText.value = resolveMedicalError(error, '加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

function applyPreset(preset: PresetKey) {
  draftQuery.value = {
    ...draftQuery.value,
    ...QUERY_PRESETS[preset]
  }
}

function pushQuery(query: Partial<MedicalCareWorkbenchSummaryQuery>) {
  const next = normalizeMedicalWorkbenchQuery({ ...activeQuery.value, ...query })
  const normalizedRouteQuery = buildMedicalWorkbenchRouteQuery(next)
  router.replace({
    path: route.path,
    query: {
      ...pickStableQueryContext(),
      ...normalizedRouteQuery
    }
  })
}

function applyFilters() {
  pushQuery(draftQuery.value)
}

function resetFilters() {
  const defaults = normalizeMedicalWorkbenchQuery(MEDICAL_WORKBENCH_QUERY_DEFAULTS)
  draftQuery.value = { ...defaults }
  pushQuery(defaults)
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
  async () => {
    const nextQuery = routeFilters()
    activeQuery.value = { ...nextQuery }
    draftQuery.value = { ...nextQuery }
    await loadSummary()
  },
  { immediate: true }
)

onMounted(() => {
  syncMedicalAlertRules().catch(() => {})
})
</script>

<style scoped>
.medical-filter {
  margin-bottom: 12px;
  border: 1px solid #d9e9ff;
  background:
    radial-gradient(140% 120% at 0% 0%, rgba(44, 128, 255, 0.11) 0%, rgba(44, 128, 255, 0) 58%),
    linear-gradient(135deg, #f4faff 0%, #edf5ff 46%, #f8fbff 100%);
}

.medical-filter__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 8px;
}

.medical-filter__form {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.risk-hero {
  border: 1px solid #cfe4ff;
  background:
    radial-gradient(120% 110% at 100% 0%, rgba(52, 115, 255, 0.17) 0%, rgba(52, 115, 255, 0) 58%),
    linear-gradient(132deg, #f7fbff 0%, #ecf4ff 44%, #f8fcff 100%);
}

.risk-hero__title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.risk-hero__score-row {
  margin-top: 8px;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.risk-hero__score {
  font-size: 40px;
  line-height: 1;
  font-weight: 700;
  color: #0f172a;
}

.risk-hero__meta {
  margin-top: 8px;
  font-size: 12px;
  color: #5f6f82;
}

.metric-tile {
  border-radius: 10px;
  border: 1px solid #d8e7ff;
  background: rgba(255, 255, 255, 0.82);
  padding: 10px;
}

.metric-tile__label {
  font-size: 12px;
  color: #63758b;
}

.metric-tile__value {
  margin-top: 6px;
  font-size: 22px;
  line-height: 1.2;
  font-weight: 700;
  color: #10243f;
}

.metric-tile__hint {
  margin-top: 4px;
  font-size: 11px;
  color: #677489;
}

.stat-card {
  min-height: 114px;
  cursor: pointer;
  border: 1px solid #e7efff;
  background: linear-gradient(170deg, #ffffff 0%, #f6f9ff 100%);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(20, 61, 130, 0.12);
}

.stat-card__label {
  font-size: 13px;
  color: #617286;
}

.stat-card__value {
  margin-top: 6px;
  font-size: 30px;
  line-height: 1;
  font-weight: 700;
  color: #0f1f34;
}

.stat-card__hint {
  margin-top: 8px;
  font-size: 12px;
  color: #78869a;
}

@media (max-width: 992px) {
  .medical-filter__head {
    flex-direction: column;
  }

  .medical-filter__form :deep(.ant-form-item) {
    width: 100%;
    margin-inline-end: 0;
  }
}
</style>
