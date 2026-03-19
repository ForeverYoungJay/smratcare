<template>
  <PageContainer title="医护照护工作台" subTitle="Medical & Care 一体化管理">
    <template #extra>
      <a-space>
        <a-tag color="blue">业务日期 {{ summary.snapshotDate || '-' }}</a-tag>
        <a-tag :color="compactMode ? 'gold' : 'default'">{{ compactMode ? '交班紧凑模式' : '标准模式' }}</a-tag>
        <a-button type="primary" ghost @click="load">刷新数据</a-button>
        <a-switch v-model:checked="compactMode" checked-children="紧凑" un-checked-children="标准" @change="handleModeChange" />
      </a-space>
    </template>

    <a-card class="card-elevated filter-panel" :bordered="false" style="margin-bottom: 12px">
      <div class="filter-head">
        <a-space wrap>
          <a-tag color="cyan">事件窗口 {{ configuredQuery.incidentWindowDays }} 天</a-tag>
          <a-tag color="orange">超时判定 {{ configuredQuery.overdueHours }} 小时</a-tag>
          <a-tag color="purple">重点居民 Top{{ configuredQuery.topResidentLimit }}</a-tag>
          <a-tag color="geekblue">风险评估回溯 {{ configuredQuery.riskResidentLookbackDays }} 天</a-tag>
        </a-space>
        <a-space wrap>
          <a-button size="small" @click="applyPreset('stable')">稳健</a-button>
          <a-button size="small" @click="applyPreset('balanced')">平衡</a-button>
          <a-button size="small" @click="applyPreset('sensitive')">敏感</a-button>
          <a-button size="small" type="primary" @click="applyFilters">应用参数</a-button>
          <a-button size="small" @click="resetFilters">恢复默认</a-button>
        </a-space>
      </div>
      <a-form layout="inline" class="filter-form" @submit.prevent>
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

    <StatefulBlock :loading="loading" :error="errorText" :empty="false" @retry="load">
      <a-card class="card-elevated hero-panel" :bordered="false" style="margin-bottom: 12px">
        <a-row :gutter="[12, 12]" align="middle">
          <a-col :xs="24" :xl="9">
            <div class="hero-title">医护风险指数</div>
            <div class="hero-score-wrap">
              <span class="hero-score">{{ riskIndex }}</span>
              <a-tag :color="riskTagColor">{{ riskLevelLabel }}</a-tag>
            </div>
            <a-progress
              :percent="riskIndex"
              :status="riskIndex >= 75 ? 'exception' : riskIndex >= 55 ? 'active' : 'normal'"
              :stroke-color="riskProgressGradient"
            />
            <div class="hero-subline">
              触发信号 {{ Number(summary.riskTriggeredCount || riskSignals.length) }} 项 · 生成时间 {{ generatedAtText }}
            </div>
          </a-col>
          <a-col :xs="24" :xl="15">
            <a-row :gutter="[12, 12]">
              <a-col :xs="12" :md="6" v-for="tile in heroTiles" :key="tile.key">
                <div class="metric-tile">
                  <div class="metric-label">{{ tile.label }}</div>
                  <div class="metric-value">{{ tile.value }}</div>
                  <div class="metric-hint">{{ tile.hint }}</div>
                </div>
              </a-col>
            </a-row>
          </a-col>
        </a-row>
      </a-card>

      <a-alert
        v-if="summary.overdueCareTaskCount > 0 || summary.unclosedAbnormalCount > 0 || summary.abnormalInspectionCount > 0"
        type="warning"
        show-icon
        style="margin-bottom: 12px"
        :message="`异常提醒：超时任务 ${summary.overdueCareTaskCount} 条，未闭环异常 ${summary.unclosedAbnormalCount} 条，异常巡检 ${summary.abnormalInspectionCount} 条。`"
      />

      <a-card class="card-elevated" :bordered="false" style="margin-bottom: 12px" title="风险触发详情">
        <a-list size="small" :data-source="riskSignals" :pagination="false" :locale="{ emptyText: '当前参数下暂无显著触发信号' }">
          <template #renderItem="{ item }">
            <a-list-item>
              <a-space>
                <a-tag :color="riskTagColor">{{ riskLevelLabel }}</a-tag>
                <span>{{ item }}</span>
              </a-space>
            </a-list-item>
          </template>
        </a-list>
      </a-card>

      <a-row :gutter="16">
        <a-col :xs="24" :md="12" :xl="8" v-for="card in cards" :key="card.key" style="margin-bottom: 16px">
          <a-card :title="card.title" :bordered="false" class="card-elevated card-click card-panel" @click="go(card.route)">
            <template #extra>
              <a-tag color="blue">{{ card.badge }}</a-tag>
            </template>
            <div v-for="line in visibleLines(card.lines)" :key="line" class="line">{{ line }}</div>
            <a-space wrap style="margin-top: 12px">
              <a-button
                v-for="action in visibleActions(card.actions)"
                :key="action.label"
                size="small"
                @click.stop="go(action.route)"
              >
                {{ action.label }}
              </a-button>
            </a-space>
          </a-card>
        </a-col>
      </a-row>

      <a-card :title="`今日长者重点关注 Top${configuredQuery.topResidentLimit}`" :bordered="false" class="card-elevated">
        <a-empty v-if="!summary.keyResidents?.length" description="暂无高风险长者" />
        <a-list v-else :data-source="summary.keyResidents">
          <template #renderItem="{ item }">
            <a-list-item>
              <a-list-item-meta
                :title="`${item.elderName || '-'}（${riskLabel(item.riskLevel)}）`"
                :description="`${item.keyRiskFactors || '暂无因子说明'} · ${item.assessmentDate || '-'}`"
              />
              <template #actions>
                <a-button type="link" @click="go(`/elder/resident-360?residentId=${item.elderId}&from=medicalCare`)">长者总览</a-button>
              </template>
            </a-list-item>
          </template>
        </a-list>
      </a-card>
    </StatefulBlock>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import dayjs from 'dayjs'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
import { getMedicalCareWorkbenchSummary } from '../../api/medicalCare'
import type { MedicalCareWorkbenchSummary, MedicalCareWorkbenchSummaryQuery } from '../../types'
import {
  MEDICAL_WORKBENCH_QUERY_DEFAULTS,
  buildMedicalWorkbenchRouteQuery,
  normalizeMedicalWorkbenchQuery,
  parseMedicalWorkbenchQueryPatch
} from '../../utils/medicalWorkbenchQuery'
import { resolveMedicalError } from './medicalError'

interface WorkbenchAction {
  label: string
  route: string
}

interface WorkbenchCard {
  key: string
  title: string
  badge: string
  route: string
  lines: string[]
  actions: WorkbenchAction[]
}

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

const RISK_LEVEL_LABEL_MAP: Record<string, string> = {
  LOW: '低风险',
  MEDIUM: '中风险',
  HIGH: '高风险',
  CRITICAL: '紧急风险'
}

const RISK_LEVEL_COLOR_MAP: Record<string, string> = {
  LOW: 'green',
  MEDIUM: 'blue',
  HIGH: 'orange',
  CRITICAL: 'red'
}

const DEFAULT_SUMMARY: MedicalCareWorkbenchSummary = {
  pendingMedicalOrderCount: 0,
  pendingReviewCount: 0,
  pendingAuditCount: 0,
  unclosedAbnormalCount: 0,
  todayInspectionTodoCount: 0,
  topRiskResidentCount: 0,
  abnormalVital24hCount: 0,
  abnormalEvent24hCount: 0,
  medicalOrderShouldCount: 0,
  medicalOrderDoneCount: 0,
  medicalOrderPendingCount: 0,
  medicalOrderAbnormalCount: 0,
  orderCheckRate: 0,
  medicationShouldCount: 0,
  medicationDoneCount: 0,
  medicationUndoneCount: 0,
  medicationLowStockCount: 0,
  medicationRequestPendingCount: 0,
  careTaskShouldCount: 0,
  careTaskDoneCount: 0,
  careTaskOverdueCount: 0,
  scanExecuteRate: 0,
  todayInspectionPlanCount: 0,
  nursingLogPendingCount: 0,
  handoverPendingCount: 0,
  handoverDoneCount: 0,
  handoverRiskCount: 0,
  handoverTodoCount: 0,
  incidentOpenCount: 0,
  incident30dCount: 0,
  incident30dRate: 0,
  lowScoreSurveyCount: 0,
  rectifyInProgressCount: 0,
  rectifyOverdueCount: 0,
  aiReportGeneratedCount: 0,
  aiReportPublishedCount: 0,
  pendingCareTaskCount: 0,
  overdueCareTaskCount: 0,
  todayInspectionPendingCount: 0,
  todayInspectionDoneCount: 0,
  abnormalInspectionCount: 0,
  todayMedicationPendingCount: 0,
  todayMedicationDoneCount: 0,
  tcmPublishedCount: 0,
  cvdHighRiskCount: 0,
  cvdNeedFollowupCount: 0,
  keyResidents: []
}

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const errorText = ref('')
const summary = ref<MedicalCareWorkbenchSummary>({ ...DEFAULT_SUMMARY })
const compactMode = ref(false)
const activeQuery = ref<MedicalCareWorkbenchSummaryQuery>({})
const draftQuery = ref<MedicalCareWorkbenchSummaryQuery>({ ...MEDICAL_WORKBENCH_QUERY_DEFAULTS })

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

const riskIndex = computed(() => Math.max(0, Math.min(100, Number(summary.value.riskIndex || 0))))
const riskLevel = computed(() => String(summary.value.riskLevel || 'LOW'))
const riskLevelLabel = computed(() => RISK_LEVEL_LABEL_MAP[riskLevel.value] || '低风险')
const riskTagColor = computed(() => RISK_LEVEL_COLOR_MAP[riskLevel.value] || 'default')
const generatedAtText = computed(() => (summary.value.generatedAt ? dayjs(summary.value.generatedAt).format('MM-DD HH:mm:ss') : '-'))

const riskProgressGradient = computed(() => {
  if (riskIndex.value >= 75) {
    return { '0%': '#ff4d4f', '100%': '#cf1322' }
  }
  if (riskIndex.value >= 55) {
    return { '0%': '#fa8c16', '100%': '#d46b08' }
  }
  if (riskIndex.value >= 30) {
    return { '0%': '#1677ff', '100%': '#0958d9' }
  }
  return { '0%': '#52c41a', '100%': '#389e0d' }
})

const heroTiles = computed(() => [
  {
    key: 'care-overdue',
    label: '护理超时率',
    value: `${Number(summary.value.careTaskOverdueRate || 0).toFixed(2)}%`,
    hint: `超时 ${summary.value.careTaskOverdueCount || 0} / 应做 ${summary.value.careTaskShouldCount || 0}`
  },
  {
    key: 'med-pending',
    label: '医嘱待执行率',
    value: `${Number(summary.value.medicationPendingRate || 0).toFixed(2)}%`,
    hint: `待执行 ${summary.value.medicationUndoneCount || 0} / 应执行 ${summary.value.medicationShouldCount || 0}`
  },
  {
    key: 'incident-window',
    label: `近${configuredQuery.value.incidentWindowDays}天事件率`,
    value: `${Number(summary.value.incident30dRate || 0).toFixed(2)}%`,
    hint: `事件总数 ${summary.value.incident30dCount || 0}`
  },
  {
    key: 'risk-resident',
    label: '高风险居民',
    value: `${summary.value.topRiskResidentCount || 0}`,
    hint: `回溯 ${configuredQuery.value.riskResidentLookbackDays} 天`
  }
])

const riskSignals = computed(() => {
  const serverSignals = summary.value.riskSignals || []
  if (serverSignals.length > 0) {
    return serverSignals
  }
  const rows: string[] = []
  if (Number(summary.value.overdueCareTaskCount || 0) > 0) {
    rows.push(`护理任务超时 ${summary.value.overdueCareTaskCount} 条`)
  }
  if (Number(summary.value.unclosedAbnormalCount || 0) > 0) {
    rows.push(`未闭环异常 ${summary.value.unclosedAbnormalCount} 条`)
  }
  if (Number(summary.value.rectifyOverdueCount || 0) > 0) {
    rows.push(`整改任务逾期 ${summary.value.rectifyOverdueCount} 条`)
  }
  return rows
})

const cards = computed<WorkbenchCard[]>(() => {
  const incidentWindowDays = configuredQuery.value.incidentWindowDays || MEDICAL_WORKBENCH_QUERY_DEFAULTS.incidentWindowDays
  const lookbackDays = configuredQuery.value.riskResidentLookbackDays || MEDICAL_WORKBENCH_QUERY_DEFAULTS.riskResidentLookbackDays
  const summaryDate = summary.value.snapshotDate || configuredQuery.value.date || dayjs().format('YYYY-MM-DD')
  return [
    {
      key: 'A',
      title: '我的待办',
      badge: `${summary.value.pendingCareTaskCount}`,
      route: `/medical-care/orders?date=${summaryDate}&scope=todo`,
      lines: [
        `待执行医嘱 ${summary.value.pendingMedicalOrderCount} · 待查对 ${summary.value.pendingReviewCount} · 待审核 ${summary.value.pendingAuditCount}`,
        `超时任务 ${summary.value.overdueCareTaskCount} · 未闭环异常 ${summary.value.unclosedAbnormalCount}`,
        `今日巡查待完成 ${summary.value.todayInspectionTodoCount}`
      ],
      actions: [
        { label: '待执行医嘱', route: `/medical-care/orders?filter=to_execute&assignee=me&date=${summaryDate}` },
        { label: '超时任务', route: `/medical-care/care-task-board?date=${summaryDate}&filter=overdue&assignee=me` },
        { label: '巡查待完成', route: `/medical-care/inspection?filter=pending&assignee=me&date=${summaryDate}` },
        { label: '统一任务中心', route: '/medical-care/unified-task-center' }
      ]
    },
    {
      key: 'C',
      title: '医嘱执行概览',
      badge: `${summary.value.medicalOrderPendingCount}`,
      route: `/medical-care/orders?date=${summaryDate}&filter=pending_or_abnormal`,
      lines: [
        `今日医嘱 应执行 ${summary.value.medicalOrderShouldCount} / 已执行 ${summary.value.medicalOrderDoneCount}`,
        `待执行 ${summary.value.medicalOrderPendingCount} · 异常 ${summary.value.medicalOrderAbnormalCount}`,
        `查对完成率 ${summary.value.orderCheckRate.toFixed(1)}%`
      ],
      actions: [
        { label: '待执行/异常', route: `/medical-care/orders?date=${summaryDate}&filter=pending_or_abnormal` },
        { label: '医嘱执行单', route: `/medical-care/orders?date=${summaryDate}&view=ward` }
      ]
    },
    {
      key: 'D',
      title: '用药与药品预警',
      badge: `${summary.value.medicationUndoneCount}`,
      route: `/medical-care/nursing-quality?tab=medication&date=${summaryDate}`,
      lines: [
        `今日应服 ${summary.value.medicationShouldCount} · 已服 ${summary.value.medicationDoneCount} · 未服 ${summary.value.medicationUndoneCount}`,
        `待执行率 ${Number(summary.value.medicationPendingRate || 0).toFixed(2)}% · 缺药/临期 ${summary.value.medicationLowStockCount}`,
        `领药申请待处理 ${summary.value.medicationRequestPendingCount}`
      ],
      actions: [
        { label: '用药登记', route: `/medical-care/medication-registration?date=${summaryDate}&filter=pending` },
        { label: '库存预警', route: '/logistics/storage/alerts?filter=low_or_expiring' },
        { label: '领药申请', route: '/logistics/storage/purchase?filter=pending' }
      ]
    },
    {
      key: 'E',
      title: '护理任务执行',
      badge: `${summary.value.careTaskOverdueCount}`,
      route: `/care/workbench/task-board?date=${summaryDate}`,
      lines: [
        `今日护理任务 应做 ${summary.value.careTaskShouldCount} · 已做 ${summary.value.careTaskDoneCount} · 超时 ${summary.value.careTaskOverdueCount}`,
        `超时率 ${Number(summary.value.careTaskOverdueRate || 0).toFixed(2)}% · 超时判定 ${configuredQuery.value.overdueHours} 小时`,
        `扫码执行率 ${summary.value.scanExecuteRate.toFixed(1)}%`
      ],
      actions: [
        { label: '任务总览', route: `/care/workbench/task-board?date=${summaryDate}&filter=all` },
        { label: '扫码执行', route: '/care/workbench/qr?mode=scan' },
        { label: '异常任务', route: `/care/workbench/task-board?date=${summaryDate}&filter=overdue_or_missed` }
      ]
    },
    {
      key: 'F',
      title: '巡查与生命体征',
      badge: `${summary.value.abnormalInspectionCount}`,
      route: `/medical-care/nursing-quality?tab=vital&date=${summaryDate}`,
      lines: [
        `巡查计划 应巡查 ${summary.value.todayInspectionPlanCount} · 已巡查 ${summary.value.todayInspectionDoneCount} · 未巡查 ${summary.value.todayInspectionPendingCount}`,
        `当日异常巡检 ${summary.value.abnormalInspectionCount}（异常率 ${Number(summary.value.inspectionAbnormalRate || 0).toFixed(2)}%）`,
        `生命体征异常 ${summary.value.abnormalVital24hCount} · 护理日志待补录 ${summary.value.nursingLogPendingCount}`
      ],
      actions: [
        { label: '健康巡查', route: `/medical-care/inspection?date=${summaryDate}` },
        { label: '健康数据', route: `/health/management/data?date=${summaryDate}` },
        { label: '护理日志', route: '/medical-care/nursing-log?filter=pending' }
      ]
    },
    {
      key: 'G',
      title: '交接班',
      badge: `${summary.value.handoverPendingCount}`,
      route: '/care/scheduling/handovers?shift=today',
      lines: [
        `当前班次 待交接 ${summary.value.handoverPendingCount} · 已交接 ${summary.value.handoverDoneCount}`,
        `重点事项 风险 ${summary.value.handoverRiskCount} · 未完成事项 ${summary.value.handoverTodoCount}`,
        `未完成交接将计入待办`
      ],
      actions: [
        { label: '交接班', route: '/care/scheduling/handovers?shift=today' },
        { label: '交接记录', route: '/care/scheduling/handovers?tab=records' }
      ]
    },
    {
      key: 'H',
      title: '质量与安全',
      badge: `${summary.value.incidentOpenCount}`,
      route: '/life/incident?filter=open_cases',
      lines: [
        `事故事件 未结案 ${summary.value.incidentOpenCount} · 近${incidentWindowDays}天发生 ${summary.value.incident30dCount}（${summary.value.incident30dRate.toFixed(1)}%）`,
        `投诉/问卷低分待整改 ${summary.value.lowScoreSurveyCount}`,
        `整改任务 进行中 ${summary.value.rectifyInProgressCount} · 逾期 ${summary.value.rectifyOverdueCount}`
      ],
      actions: [
        { label: '安全事件', route: `/life/incident?filter=open_cases&period=${incidentWindowDays}d` },
        { label: '问卷低分', route: '/survey/stats?filter=low_score_or_complaint' },
        { label: '整改任务', route: '/oa/work-execution/task?filter=overdue_rectify' }
      ]
    },
    {
      key: 'I',
      title: '报表速览',
      badge: `${summary.value.aiReportGeneratedCount}`,
      route: '/medical-care/ai-reports',
      lines: [
        `AI健康评估报告 生成 ${summary.value.aiReportGeneratedCount} · 已发布 ${summary.value.aiReportPublishedCount}`,
        `高风险心血管提示 ${summary.value.cvdHighRiskCount} · 需随访 ${summary.value.cvdNeedFollowupCount}`,
        `高风险居民 ${summary.value.topRiskResidentCount} 人（近${lookbackDays}天）`
      ],
      actions: [
        { label: 'AI报告', route: '/medical-care/ai-reports' },
        { label: '异常规则', route: '/medical-care/alert-rules' },
        { label: '护理任务', route: '/care/workbench/task-board?filter=generated_from_ai' }
      ]
    }
  ]
})

function resolveCompactMode(raw: unknown) {
  return String(Array.isArray(raw) ? raw[0] : raw || '').toLowerCase() === 'compact'
}

function visibleLines(lines: string[]) {
  if (!compactMode.value) {
    return lines
  }
  return lines.slice(0, 2)
}

function visibleActions(actions: WorkbenchAction[]) {
  if (!compactMode.value) {
    return actions
  }
  return actions.slice(0, 3)
}

function go(path: string) {
  router.push(path)
}

function riskLabel(value?: string) {
  if (value === 'VERY_HIGH') return '极高风险'
  if (value === 'HIGH') return '高风险'
  if (value === 'MEDIUM') return '中风险'
  if (value === 'LOW') return '低风险'
  return value || '未分层'
}

function handleModeChange(checked: boolean) {
  const query: Record<string, string> = {}
  if (Object.keys(activeQuery.value).length > 0) {
    Object.assign(query, buildMedicalWorkbenchRouteQuery(configuredQuery.value))
  }
  if (checked) {
    query.mode = 'compact'
  }
  router.replace({ query })
}

function applyPreset(preset: PresetKey) {
  draftQuery.value = {
    ...draftQuery.value,
    ...QUERY_PRESETS[preset]
  }
  message.success('已应用预设，点击“应用参数”生效')
}

function applyFilters() {
  const normalized = normalizeMedicalWorkbenchQuery(draftQuery.value)
  if (draftQuery.value.date) {
    normalized.date = draftQuery.value.date
  }
  activeQuery.value = { ...normalized }
  const query = buildMedicalWorkbenchRouteQuery(normalized, { mode: compactMode.value ? 'compact' : undefined })
  router.replace({ query })
}

function resetFilters() {
  activeQuery.value = {}
  draftQuery.value = { ...MEDICAL_WORKBENCH_QUERY_DEFAULTS }
  const query: Record<string, string> = compactMode.value ? { mode: 'compact' } : {}
  router.replace({ query })
  message.success('已恢复默认参数')
}

async function load() {
  loading.value = true
  errorText.value = ''
  try {
    const params = Object.keys(activeQuery.value).length > 0
      ? normalizeMedicalWorkbenchQuery(activeQuery.value)
      : undefined
    const data = await getMedicalCareWorkbenchSummary(params)
    summary.value = {
      ...DEFAULT_SUMMARY,
      ...(data || {})
    }

    const serverQuery = normalizeMedicalWorkbenchQuery({
      date: summary.value.snapshotDate || activeQuery.value.date,
      status: activeQuery.value.status,
      elderId: activeQuery.value.elderId,
      incidentWindowDays: summary.value.configuredIncidentWindowDays,
      overdueHours: summary.value.configuredOverdueHours,
      topResidentLimit: summary.value.configuredTopResidentLimit,
      riskResidentLookbackDays: summary.value.configuredRiskResidentLookbackDays
    })
    draftQuery.value = {
      ...serverQuery,
      date: summary.value.snapshotDate || serverQuery.date
    }
  } catch (error: any) {
    errorText.value = resolveMedicalError(error, '加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

watch(
  () => route.query,
  () => {
    compactMode.value = resolveCompactMode(route.query.mode)
    activeQuery.value = parseMedicalWorkbenchQueryPatch(route.query)
    draftQuery.value = normalizeMedicalWorkbenchQuery(activeQuery.value)
    if (activeQuery.value.date) {
      draftQuery.value.date = activeQuery.value.date
    }
    load()
  },
  { immediate: true, deep: true }
)
</script>

<style scoped>
.filter-panel {
  border: 1px solid #e0ecff;
  background: linear-gradient(130deg, #f6fbff 0%, #f2f8ff 45%, #f9fdff 100%);
}

.filter-head {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.filter-form {
  row-gap: 8px;
}

.hero-panel {
  overflow: hidden;
  border: 1px solid #d9ebff;
  background:
    radial-gradient(150% 110% at 0% 0%, rgba(22, 119, 255, 0.09) 0%, rgba(22, 119, 255, 0) 58%),
    linear-gradient(140deg, #f8fcff 0%, #eef6ff 42%, #f8fdff 100%);
}

.hero-title {
  font-size: 15px;
  color: #1f2937;
  font-weight: 600;
}

.hero-score-wrap {
  margin-top: 8px;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.hero-score {
  font-size: 42px;
  line-height: 1;
  font-weight: 700;
  color: #0f172a;
}

.hero-subline {
  margin-top: 8px;
  color: #64748b;
  font-size: 12px;
}

.metric-tile {
  border-radius: 10px;
  border: 1px solid rgba(148, 163, 184, 0.24);
  background: rgba(255, 255, 255, 0.8);
  padding: 10px;
  min-height: 94px;
  animation: rise-in 0.45s ease both;
}

.metric-label {
  color: #64748b;
  font-size: 12px;
}

.metric-value {
  margin-top: 6px;
  font-size: 24px;
  line-height: 1.1;
  color: #0f172a;
  font-weight: 700;
}

.metric-hint {
  margin-top: 6px;
  font-size: 12px;
  color: #475569;
}

.card-click {
  cursor: pointer;
}

.card-panel {
  border: 1px solid #edf2f7;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.card-panel:hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 32px rgba(15, 23, 42, 0.08);
}

.line {
  margin-bottom: 6px;
  color: #334155;
  font-size: 13px;
}

@keyframes rise-in {
  from {
    transform: translateY(6px);
    opacity: 0;
  }

  to {
    transform: translateY(0);
    opacity: 1;
  }
}

@media (max-width: 900px) {
  .hero-score {
    font-size: 34px;
  }

  .metric-tile {
    min-height: 84px;
  }
}
</style>
