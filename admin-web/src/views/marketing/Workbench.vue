<template>
  <PageContainer title="销售运营工作台" sub-title="运营总览、风险预警、快捷动作与营销任务集中处理" mode="showcase" kicker="经营运营">
    <template #meta>
      <span class="soft-pill">统计范围 {{ quickRangeLabel }}</span>
      <span class="soft-pill">自动刷新 {{ autoRefresh ? '已开启' : '已关闭' }}</span>
      <span class="soft-pill">快照 {{ snapshotHints.length }} 条</span>
      <span class="soft-pill">更新于 {{ lastUpdated || '-' }}</span>
    </template>

    <template #stats>
      <div class="marketing-overview-grid">
        <div
          v-for="item in overviewTiles"
          :key="item.label"
          class="overview-tile"
          :class="`overview-tile--${item.tone}`"
        >
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
          <small>{{ item.hint }}</small>
        </div>
      </div>
    </template>

    <section class="workbench-shell">
      <a-card class="card-elevated dashboard-control" :bordered="false">
        <div class="dashboard-control-bar">
          <div class="dashboard-control-copy">
            <span class="section-kicker">Marketing Operations</span>
            <strong>销售运营工作台</strong>
            <span>围绕漏斗、风险、床位、合同和回访，把当下最该处理的销售动作集中到一屏。</span>
          </div>
          <div class="dashboard-control-actions">
            <a-radio-group v-model:value="quickRange" size="small" @change="onQuickRangeChange">
              <a-radio-button value="TODAY">今日</a-radio-button>
              <a-radio-button value="7D">近7天</a-radio-button>
              <a-radio-button value="30D">近30天</a-radio-button>
              <a-radio-button value="MONTH">本月</a-radio-button>
            </a-radio-group>
            <a-range-picker
              v-model:value="dateRange"
              value-format="YYYY-MM-DD"
              :allow-clear="false"
              @change="onRangeChange"
            />
            <span class="control-switch">
              <a-switch v-model:checked="autoRefresh" size="small" />
              <span>自动刷新</span>
            </span>
            <a-button :loading="dashboardLoading" @click="loadOverview">立即刷新</a-button>
            <a-button @click="goSnapshots">查看快照</a-button>
          </div>
        </div>
        <a-alert
          v-if="dashboardError"
          class="dashboard-alert"
          type="warning"
          show-icon
          :message="dashboardError"
        />
      </a-card>

      <div class="workbench-top-grid">
        <a-card class="card-elevated hero-panel" :bordered="false">
          <div class="panel-head">
            <div>
              <span class="section-kicker">本月</span>
              <strong>线索漏斗</strong>
            </div>
            <span class="panel-link" @click="goReport('conversion')">查看转化率</span>
          </div>
          <div class="hero-panel-body">
            <div class="hero-stage-list">
              <div v-for="item in funnelStages" :key="item.label" class="hero-stage-item">
                <span>{{ item.label }}</span>
                <strong>{{ item.value }}</strong>
                <small>{{ item.hint }}</small>
              </div>
            </div>
            <div class="hero-chart-wrap">
              <v-chart :option="funnelOption" autoresize class="hero-funnel-chart" />
            </div>
          </div>
        </a-card>

        <a-card class="card-elevated risk-panel" :bordered="false">
          <div class="panel-head">
            <div>
              <span class="section-kicker">Risk</span>
              <strong>风险预警</strong>
            </div>
            <span class="panel-link" @click="goReport('followup')">查看明细</span>
          </div>
          <div class="risk-list">
            <button
              v-for="item in riskAlerts"
              :key="item.title"
              type="button"
              class="risk-item"
              :class="`risk-item--${item.tone}`"
              @click="item.action()"
            >
              <span class="risk-icon">{{ item.icon }}</span>
              <div class="risk-copy">
                <strong>{{ item.title }}</strong>
                <small>{{ item.desc }}</small>
              </div>
              <span class="risk-action">{{ item.cta }}</span>
            </button>
          </div>
        </a-card>

        <a-card class="card-elevated quick-panel" :bordered="false">
          <div class="panel-head">
            <div>
              <span class="section-kicker">Actions</span>
              <strong>快捷操作</strong>
            </div>
            <span class="panel-link" @click="goPlan({ status: 'PENDING_APPROVAL' })">查看方案审批</span>
          </div>
          <div class="quick-action-grid">
            <button
              v-for="item in quickActions"
              :key="item.label"
              type="button"
              class="quick-action"
              @click="item.action()"
            >
              <span class="quick-action-icon" :class="`quick-action-icon--${item.tone}`">{{ item.short }}</span>
              <span>{{ item.label }}</span>
            </button>
          </div>
        </a-card>
      </div>

      <div class="signal-grid">
        <div v-for="item in signalCards" :key="item.label" class="signal-card">
          <div class="signal-copy">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
            <small>{{ item.hint }}</small>
          </div>
          <span class="signal-badge" :class="`signal-badge--${item.tone}`">{{ item.badge }}</span>
        </div>

        <a-card class="card-elevated bed-overview-card" :bordered="false">
          <div class="panel-head">
            <div>
              <span class="section-kicker">Bed & Reservation</span>
              <strong>床位与预定概况</strong>
            </div>
            <span class="panel-link" @click="goReservation('panorama')">进入床态看板</span>
          </div>
          <div class="bed-overview-body">
            <v-chart :option="bedOverviewOption" autoresize class="bed-overview-chart" />
            <div class="bed-overview-legend">
              <div v-for="item in bedOverviewItems" :key="item.label" class="bed-legend-item">
                <span class="bed-legend-dot" :style="{ background: item.color }"></span>
                <span>{{ item.label }}</span>
                <strong>{{ item.value }}</strong>
              </div>
            </div>
          </div>
        </a-card>
      </div>

      <div class="operations-grid">
        <a-card class="card-elevated operations-card" :bordered="false">
          <div class="panel-head panel-head--stack">
            <div>
              <span class="section-kicker">Task Desk</span>
              <strong>运营任务台</strong>
              <small>把今天最需要推进的营销动作收在一起，优先级从高到低处理。</small>
            </div>
            <div class="report-chip-row">
              <button
                v-for="item in quickReportButtons"
                :key="item.key"
                type="button"
                class="report-chip"
                @click="goReport(item.entry)"
              >
                {{ quickLabel(item.entry, item.label) }}
              </button>
            </div>
          </div>

          <div class="task-table">
            <div class="task-table-head">
              <span>任务内容</span>
              <span>业务阶段</span>
              <span>提醒口径</span>
              <span>优先级</span>
              <span>处理动作</span>
            </div>
            <div v-for="item in taskRows" :key="item.key" class="task-row">
              <div class="task-main">
                <strong>{{ item.title }}</strong>
                <small>{{ item.desc }}</small>
              </div>
              <span class="task-stage">{{ item.stage }}</span>
              <span class="task-metric">{{ item.metric }}</span>
              <span class="task-priority" :class="`task-priority--${item.tone}`">{{ item.priority }}</span>
              <a-button type="link" @click="item.action()">{{ item.actionLabel }}</a-button>
            </div>
          </div>
        </a-card>

        <div class="sidebar-stack">
          <a-card class="card-elevated rail-card" :bordered="false">
            <div class="panel-head">
              <div>
                <span class="section-kicker">Reminder</span>
                <strong>跟进任务提醒</strong>
              </div>
              <span class="panel-link" @click="goFollowup('today')">查看全部</span>
            </div>
            <div class="reminder-list">
              <div v-for="item in reminderItems" :key="item.label" class="reminder-item">
                <span class="reminder-dot" :class="`reminder-dot--${item.tone}`"></span>
                <span>{{ item.label }}</span>
                <strong>{{ item.value }}</strong>
              </div>
            </div>
          </a-card>

          <a-card class="card-elevated rail-card" :bordered="false">
            <div class="panel-head">
              <div>
                <span class="section-kicker">Channel</span>
                <strong>渠道表现</strong>
              </div>
              <span class="panel-link" @click="goReport('channel-rank')">查看全部</span>
            </div>
            <div class="progress-list">
              <div v-for="item in channelProgressItems" :key="item.label" class="progress-item">
                <div class="progress-copy">
                  <strong>{{ item.label }}</strong>
                  <small>{{ item.subLabel }}</small>
                </div>
                <a-progress :percent="item.percent" :show-info="false" :stroke-color="item.color" />
                <span class="progress-value">{{ item.percent }}%</span>
              </div>
            </div>
          </a-card>

          <a-card class="card-elevated rail-card" :bordered="false">
            <div class="panel-head">
              <div>
                <span class="section-kicker">Campaign</span>
                <strong>营销方案进度</strong>
              </div>
              <span class="panel-link" @click="goPlan({ status: 'PUBLISHED' })">查看全部</span>
            </div>
            <div class="progress-list">
              <div v-for="item in planProgressItems" :key="item.label" class="progress-item">
                <div class="progress-copy">
                  <strong>{{ item.label }}</strong>
                  <small>{{ item.subLabel }}</small>
                </div>
                <a-progress :percent="item.percent" :show-info="false" :stroke-color="item.color" />
                <span class="progress-value">{{ item.percent }}%</span>
              </div>
            </div>
          </a-card>

          <a-card class="card-elevated rail-card" :bordered="false">
            <div class="panel-head">
              <div>
                <span class="section-kicker">Snapshot</span>
                <strong>经营快照</strong>
              </div>
              <span class="panel-link" @click="goSnapshots">更多</span>
            </div>
            <div class="snapshot-list">
              <div v-for="item in snapshotDisplay" :key="item.id" class="snapshot-item">
                <strong>{{ item.snapshotDate || '-' }}</strong>
                <small>{{ item.generatedAt || '-' }}</small>
              </div>
            </div>
          </a-card>
        </div>
      </div>
    </section>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import VChart from 'vue-echarts'
import PageContainer from '../../components/PageContainer.vue'
import { buildReportRoute, hasReportCache } from '../../utils/marketingReportNav'
import { getMarketingReportRoute, type MarketingReportEntry } from '../../utils/marketingReportRegistry'
import {
  buildCallbackRoute,
  buildContractRoute,
  buildFollowupRoute,
  buildFunnelRoute,
  buildLeadRoute,
  buildReservationRoute,
  routeToUnknownSourceOrAll
} from '../../utils/marketingNav'
import {
  getMarketingReportSnapshots,
  getMarketingWorkbenchSummary
} from '../../api/marketing'
import type { CrmContractItem, CrmSalesReportSnapshotItem, MarketingWorkbenchSummary } from '../../types'

const router = useRouter()
const dashboardLoading = ref(false)
const dashboardError = ref('')
const lastUpdated = ref('')
const snapshotHints = ref<CrmSalesReportSnapshotItem[]>([])
const quickRange = ref<'TODAY' | '7D' | '30D' | 'MONTH'>('MONTH')
const dateRange = ref<any[]>([dayjs().startOf('month').format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')])
const autoRefresh = ref(true)
let refreshTimer: number | undefined
const AUTO_REFRESH_INTERVAL_MS = 60 * 1000

const funnel = reactive({
  todayConsultCount: 0,
  evaluationCount: 0,
  pendingSignCount: 0,
  pendingAdmissionCount: 0,
  monthDealCount: 0,
  monthConversionRate: 0
})
const followup = reactive({
  todayDue: 0,
  overdue: 0,
  highIntentCount: 0,
  lockExpiringCount: 0
})
const bedSales = reactive({
  emptyCount: 0,
  lockCount: 0,
  reservedUnsignedCount: 0,
  premiumEmptyCount: 0
})
const contract = reactive({
  pendingSignCount: 0,
  renewalDueCount: 0,
  changePendingCount: 0,
  monthAmount: 0
})
const callback = reactive({
  checkinCount: 0,
  trialCount: 0,
  dischargeCount: 0,
  score: 0
})
const performance = reactive({
  monthDealCount: 0,
  monthAmount: 0,
  rankNo: 0,
  timelyRate: 0
})
const medical = reactive({
  todayCount: 0,
  referCount: 0,
  unassignedCount: 0
})
const plan = reactive({
  speechCount: 0,
  policyCount: 0,
  pendingApprovalCount: 0,
  rejectedCount: 0
})
const risk = reactive({
  overdueFollowupCount: 0,
  lockUnsignedCount: 0,
  highIntentNoEvalCount: 0,
  channelDropCount: 0
})

const channelTop5 = ref<Array<{ source: string; leadCount: number; contractRate: string }>>([])
const channelUnknownCount = ref(0)
const channelMonthDeals = ref(0)

const riskCount = computed(() =>
  Number(risk.overdueFollowupCount || 0)
  + Number(risk.lockUnsignedCount || 0)
  + Number(risk.highIntentNoEvalCount || 0)
  + Number(risk.channelDropCount || 0)
)
const opportunityCount = computed(() =>
  Number(funnel.evaluationCount || 0)
  + Number(funnel.pendingSignCount || 0)
  + Number(funnel.pendingAdmissionCount || 0)
  + Number(medical.unassignedCount || 0)
)

const quickReportButtons = computed(() => {
  return [
    { key: 'conversion', label: '转化率统计', entry: 'conversion' as MarketingReportEntry },
    { key: 'followup', label: '跟进统计', entry: 'followup' as MarketingReportEntry },
    { key: 'channel', label: '渠道评估', entry: 'channel' as MarketingReportEntry },
    { key: 'consultation', label: '咨询统计', entry: 'consultation' as MarketingReportEntry },
    { key: 'callback', label: '回访统计', entry: 'callback' as MarketingReportEntry },
    { key: 'snapshots', label: '经营快照', entry: 'snapshots' as MarketingReportEntry }
  ]
})

const quickRangeLabel = computed(() => {
  if (quickRange.value === 'TODAY') return '今日'
  if (quickRange.value === '7D') return '近7天'
  if (quickRange.value === '30D') return '近30天'
  return '本月'
})

const overviewTiles = computed(() => [
  { label: '机会池', value: opportunityCount.value, hint: '待推进评估、签约与入住', tone: 'primary' },
  { label: '风险项', value: riskCount.value, hint: '超时跟进、锁床未签与异常波动', tone: riskCount.value > 0 ? 'danger' : 'success' },
  { label: '今日待跟进', value: followup.todayDue || 0, hint: '优先处理今天必须回访的客户', tone: 'warning' },
  { label: '本月成交', value: funnel.monthDealCount || 0, hint: '用于观察当前转化推进节奏', tone: 'success' }
])

const funnelStages = computed(() => [
  { label: '线索新增', value: funnel.todayConsultCount || 0, hint: '今日新增咨询' },
  { label: '初步沟通', value: followup.highIntentCount || 0, hint: '高意向客户' },
  { label: '到访评估', value: funnel.evaluationCount || 0, hint: '待评估推进' },
  { label: '预定锁床', value: bedSales.lockCount || 0, hint: '锁床与预订证据' },
  { label: '签约入住', value: funnel.monthDealCount || 0, hint: `转化率 ${Number(funnel.monthConversionRate || 0).toFixed(1)}%` }
])

const riskAlerts = computed(() => [
  {
    title: '待回访超期',
    desc: `有 ${followup.overdue || 0} 位客户回访已超时，建议优先处理。`,
    cta: '去处理',
    icon: '!',
    tone: 'danger',
    action: () => goFollowup('overdue')
  },
  {
    title: '预定即将到期',
    desc: `有 ${followup.lockExpiringCount || 0} 份锁床记录 3 天内到期。`,
    cta: '去查看',
    icon: '!',
    tone: 'warning',
    action: () => goReservation('expiring')
  },
  {
    title: '锁床未签约',
    desc: `还有 ${risk.lockUnsignedCount || 0} 位客户已锁床但合同未闭环。`,
    cta: '去推进',
    icon: '!',
    tone: 'danger',
    action: () => goContract('pending')
  }
])

const quickActions = computed(() => [
  { label: '新增线索', short: '新', tone: 'blue', action: () => goLead('all', { quick: '1', tab: 'consultation' }) },
  { label: '记录跟进', short: '跟', tone: 'cyan', action: () => goFollowup('today') },
  { label: '安排回访', short: '回', tone: 'green', action: () => goCallback('checkin') },
  { label: '预定床位', short: '订', tone: 'teal', action: () => goReservation('records') },
  { label: '锁定床位', short: '锁', tone: 'orange', action: () => goReservation('lock') },
  { label: '发起合同', short: '签', tone: 'indigo', action: () => goContract('pending') },
  { label: '合同变更', short: '变', tone: 'violet', action: () => goContract('change') },
  { label: '签后回访', short: '访', tone: 'amber', action: () => goCallback('score') }
])

const signalCards = computed(() => [
  { label: '线索总量', value: opportunityCount.value, hint: '机会池总量', badge: '线索', tone: 'blue' },
  { label: '到访评估', value: funnel.evaluationCount || 0, hint: '待评估客户', badge: '评估', tone: 'cyan' },
  { label: '预定中', value: bedSales.reservedUnsignedCount || 0, hint: '待签约预订', badge: '预订', tone: 'orange' },
  { label: '锁床中', value: bedSales.lockCount || 0, hint: '锁床客户', badge: '锁床', tone: 'amber' },
  { label: '签约中', value: contract.pendingSignCount || 0, hint: '待签合同', badge: '合同', tone: 'indigo' },
  { label: '本月入住', value: funnel.monthDealCount || 0, hint: `较上期 ${Number(funnel.monthConversionRate || 0).toFixed(1)}%`, badge: '入住', tone: 'teal' }
])

const bedOverviewItems = computed(() => [
  { label: '可用', value: bedSales.emptyCount || 0, color: '#37b7d8' },
  { label: '预定中', value: bedSales.reservedUnsignedCount || 0, color: '#ffb04d' },
  { label: '锁床中', value: bedSales.lockCount || 0, color: '#ff8a3d' },
  { label: '入住中', value: funnel.monthDealCount || 0, color: '#67c598' }
])

const bedOverviewOption = computed(() => ({
  tooltip: { trigger: 'item' },
  color: bedOverviewItems.value.map((item) => item.color),
  series: [
    {
      type: 'pie',
      radius: ['58%', '78%'],
      center: ['42%', '50%'],
      label: { show: false },
      data: bedOverviewItems.value.map((item) => ({
        name: item.label,
        value: item.value
      }))
    }
  ],
  graphic: [
    {
      type: 'text',
      left: '33%',
      top: '40%',
      style: {
        text: `总床位\n${bedOverviewItems.value.reduce((sum, item) => sum + Number(item.value || 0), 0)}`,
        textAlign: 'center',
        fill: '#173854',
        fontSize: 15,
        fontWeight: 700
      }
    }
  ]
}))

const taskRows = computed(() => [
  {
    key: 'follow-today',
    title: '今日待回访客户',
    desc: '客户互动中心今日应执行的跟进与回访任务。',
    stage: '客户互动',
    metric: `${followup.todayDue || 0} 条`,
    priority: '高',
    tone: 'warning',
    actionLabel: '跟进',
    action: () => goFollowup('today')
  },
  {
    key: 'follow-overdue',
    title: '逾期未跟进客户',
    desc: '超过计划日期仍未处理的客户，需要优先清理。',
    stage: '风险处理',
    metric: `${followup.overdue || 0} 条`,
    priority: '紧急',
    tone: 'danger',
    actionLabel: '处理',
    action: () => goFollowup('overdue')
  },
  {
    key: 'evaluation',
    title: '待评估客户',
    desc: '签约前评估尚未完成，影响后续床位和合同推进。',
    stage: '合同前置',
    metric: `${funnel.evaluationCount || 0} 人`,
    priority: '高',
    tone: 'cyan',
    actionLabel: '推进',
    action: () => goContractLifecycle('PENDING_ASSESSMENT')
  },
  {
    key: 'contract',
    title: '待签合同',
    desc: '预订已完成但合同仍未最终签署。',
    stage: '合同闭环',
    metric: `${contract.pendingSignCount || 0} 份`,
    priority: '高',
    tone: 'indigo',
    actionLabel: '签署',
    action: () => goContract('pending')
  },
  {
    key: 'lock-expiring',
    title: '锁床即将到期',
    desc: '锁床资源将到期，需确认是否继续保留或释放。',
    stage: '床位联动',
    metric: `${followup.lockExpiringCount || 0} 条`,
    priority: '中',
    tone: 'orange',
    actionLabel: '查看',
    action: () => goReservation('expiring')
  }
])

const reminderItems = computed(() => [
  { label: '待跟进（今天）', value: followup.todayDue || 0, tone: 'blue' },
  { label: '回访超期', value: followup.overdue || 0, tone: 'orange' },
  { label: '3天内需回访', value: callback.checkinCount || 0, tone: 'red' },
  { label: '本周到访安排', value: funnel.evaluationCount || 0, tone: 'green' }
])

const channelProgressItems = computed(() => {
  const maxCount = Math.max(...channelTop5.value.map((item) => Number(item.leadCount || 0)), 1)
  const colors = ['#37b7d8', '#4ea3ff', '#67c598']
  return channelTop5.value.slice(0, 3).map((item, index) => ({
    label: item.source,
    subLabel: `线索 ${item.leadCount} · 转化率 ${item.contractRate}`,
    percent: Math.max(6, Math.round((Number(item.leadCount || 0) / maxCount) * 100)),
    color: colors[index] || '#37b7d8'
  }))
})

const planProgressItems = computed(() => {
  const total = Math.max(
    Number(plan.speechCount || 0) + Number(plan.policyCount || 0),
    Number(plan.pendingApprovalCount || 0) + Number(plan.rejectedCount || 0),
    1
  )
  return [
    {
      label: '待审批方案',
      subLabel: '等待业务或院长审批',
      percent: Math.min(100, Math.round((Number(plan.pendingApprovalCount || 0) / total) * 100)),
      color: '#37b7d8'
    },
    {
      label: '话术库维护',
      subLabel: `当前话术 ${plan.speechCount || 0} 条`,
      percent: Math.min(100, Math.round((Number(plan.speechCount || 0) / total) * 100)),
      color: '#67c598'
    },
    {
      label: '季度政策沉淀',
      subLabel: `当前政策 ${plan.policyCount || 0} 条`,
      percent: Math.min(100, Math.round((Number(plan.policyCount || 0) / total) * 100)),
      color: '#ffb04d'
    }
  ]
})

const snapshotDisplay = computed(() => (
  snapshotHints.value.slice(0, 3).map((item) => ({
    ...item,
    snapshotDate: item.snapshotDate || item.generatedAt || '-',
    generatedAt: item.generatedAt ? dayjs(item.generatedAt).format('YYYY-MM-DD HH:mm:ss') : '-'
  }))
))

const funnelOption = computed(() => ({
  tooltip: { trigger: 'item' },
  color: ['#4ea3ff', '#3fb4c4', '#7cc8d7', '#ffb454'],
  series: [
    {
      type: 'funnel',
      left: '8%',
      top: 10,
      bottom: 10,
      width: '84%',
      minSize: '24%',
      maxSize: '100%',
      sort: 'descending',
      gap: 2,
      label: {
        show: true,
        position: 'inside',
        color: '#ffffff',
        fontSize: 12,
        formatter: '{b}'
      },
      itemStyle: {
        borderColor: '#ffffff',
        borderWidth: 1
      },
      data: [
        { name: '咨询', value: funnel.todayConsultCount || 0 },
        { name: '评估', value: funnel.evaluationCount || 0 },
        { name: '预定', value: bedSales.reservedUnsignedCount || 0 },
        { name: '锁床', value: bedSales.lockCount || 0 },
        { name: '签约入住', value: funnel.monthDealCount || 0 }
      ]
    }
  ]
}))

function goWithReportCache(path: string, cacheKey: string) {
  router.push(buildReportRoute(path, cacheKey))
}

function goReport(entry: MarketingReportEntry) {
  const config = getMarketingReportRoute(entry, hasCache)
  goWithReportCache(config.path, config.cacheKey)
}

function goLead(entry: 'all' | 'intent' | 'invalid' | 'blacklist' | 'unknown-source' | 'medical-transfer', query?: Record<string, string>) {
  router.push(buildLeadRoute(entry, query))
}

function goFollowup(entry: 'records' | 'due' | 'today' | 'overdue', query?: Record<string, string>) {
  router.push(buildFollowupRoute(entry, query))
}

function goReservation(entry: 'records' | 'lock' | 'expiring' | 'panorama', query?: Record<string, string>) {
  router.push(buildReservationRoute(entry, query))
}

function goContract(entry: 'pending' | 'signed' | 'renewal' | 'change' | 'attachments', query?: Record<string, string>) {
  router.push(buildContractRoute(entry, query))
}

function goContractLifecycle(stage: CrmContractItem['flowStage']) {
  router.push({
    path: '/marketing/contracts/pending',
    query: stage ? { flowStage: stage } : {}
  })
}

function goFunnel(entry: 'consultation' | 'evaluation' | 'signing' | 'admission' | 'lost', query?: Record<string, string>) {
  router.push(buildFunnelRoute(entry, query))
}

function goCallback(entry: 'checkin' | 'trial' | 'discharge' | 'score', query?: Record<string, string>) {
  router.push(buildCallbackRoute(entry, query))
}

function goChannelSource(source: string) {
  router.push(routeToUnknownSourceOrAll(source))
}

function goPlan(query?: Record<string, string>) {
  router.push({
    path: '/marketing/plan',
    query: query || {}
  })
}

function goApproval(type: string) {
  router.push({
    path: '/oa/approval',
    query: { type, status: 'PENDING' }
  })
}

function goSnapshots() {
  router.push('/marketing/reports/snapshots')
}

function hasCache(cacheKey: string) {
  return hasReportCache(cacheKey)
}

function quickLabel(entry: MarketingReportEntry, base: string) {
  const config = getMarketingReportRoute(entry, hasCache)
  return hasCache(config.cacheKey) ? `${base}（上次筛选）` : base
}

async function loadOverview() {
  const rangeStart = String(dateRange.value?.[0] || dayjs().startOf('month').format('YYYY-MM-DD'))
  const rangeEnd = String(dateRange.value?.[1] || dayjs().format('YYYY-MM-DD'))
  dashboardLoading.value = true
  dashboardError.value = ''
  try {
    const summary = await getMarketingWorkbenchSummary({
      dateFrom: rangeStart,
      dateTo: rangeEnd
    }) as Promise<MarketingWorkbenchSummary>
    Object.assign(funnel, summary.funnel || {})
    Object.assign(followup, summary.followup || {})
    Object.assign(bedSales, summary.bedSales || {})
    Object.assign(contract, {
      pendingSignCount: Number(summary.contract?.pendingSignCount || 0),
      renewalDueCount: Number(summary.contract?.renewalDueCount || 0),
      changePendingCount: Number(summary.contract?.changePendingCount || 0),
      monthAmount: Number(summary.contract?.monthAmount || 0)
    })
    Object.assign(callback, summary.callback || {})
    Object.assign(performance, {
      monthDealCount: Number(summary.performance?.monthDealCount || 0),
      monthAmount: Number(summary.performance?.monthAmount || 0),
      rankNo: Number(summary.performance?.rankNo || 0),
      timelyRate: Number(summary.performance?.timelyRate || 0)
    })
    Object.assign(medical, summary.medical || {})
    Object.assign(plan, summary.plan || {})
    Object.assign(risk, summary.risk || {})
    channelTop5.value = Array.isArray(summary.channelTop5) ? summary.channelTop5 : []
    channelUnknownCount.value = Number(summary.channelUnknownCount || 0)
    channelMonthDeals.value = Number(summary.channelMonthDeals || 0)
    lastUpdated.value = summary.generatedAt ? dayjs(summary.generatedAt).format('YYYY-MM-DD HH:mm:ss') : dayjs().format('YYYY-MM-DD HH:mm:ss')
    try {
      const snapshots = await getMarketingReportSnapshots({ snapshotType: 'MARKETING_WORKBENCH', limit: 3 })
      snapshotHints.value = Array.isArray(snapshots) ? snapshots : []
    } catch {
      snapshotHints.value = []
    }
  } catch (error: any) {
    dashboardError.value = error?.message || '营销工作台数据加载失败'
  } finally {
    dashboardLoading.value = false
  }
}

function applyQuickRange(range: 'TODAY' | '7D' | '30D' | 'MONTH') {
  const end = dayjs()
  if (range === 'TODAY') {
    dateRange.value = [end.format('YYYY-MM-DD'), end.format('YYYY-MM-DD')]
    return
  }
  if (range === '7D') {
    dateRange.value = [end.subtract(6, 'day').format('YYYY-MM-DD'), end.format('YYYY-MM-DD')]
    return
  }
  if (range === '30D') {
    dateRange.value = [end.subtract(29, 'day').format('YYYY-MM-DD'), end.format('YYYY-MM-DD')]
    return
  }
  dateRange.value = [end.startOf('month').format('YYYY-MM-DD'), end.format('YYYY-MM-DD')]
}

function onQuickRangeChange() {
  applyQuickRange(quickRange.value)
  loadOverview()
}

function onRangeChange() {
  loadOverview()
}

function startAutoRefresh() {
  stopAutoRefresh()
  if (!autoRefresh.value) return
  refreshTimer = window.setInterval(() => {
    if (dashboardLoading.value || document.visibilityState !== 'visible') return
    loadOverview()
  }, AUTO_REFRESH_INTERVAL_MS)
}

function stopAutoRefresh() {
  if (!refreshTimer) return
  window.clearInterval(refreshTimer)
  refreshTimer = undefined
}

watch(autoRefresh, () => {
  startAutoRefresh()
})

onMounted(() => {
  applyQuickRange(quickRange.value)
  loadOverview()
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style scoped>
.workbench-shell {
  display: grid;
  gap: 16px;
}

.marketing-overview-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.overview-tile {
  display: grid;
  gap: 8px;
  min-height: 112px;
  padding: 18px;
  border-radius: 22px;
  border: 1px solid rgba(216, 229, 239, 0.92);
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 14px 32px rgba(17, 60, 93, 0.05);
}

.overview-tile span {
  color: #648097;
  font-size: 12px;
}

.overview-tile strong {
  color: #12314d;
  font-size: 30px;
  line-height: 1;
}

.overview-tile small {
  color: #7f96aa;
  font-size: 12px;
  line-height: 1.55;
}

.overview-tile--primary {
  background: linear-gradient(180deg, rgba(68, 163, 255, 0.11) 0%, rgba(255, 255, 255, 0.98) 100%);
}

.overview-tile--warning {
  background: linear-gradient(180deg, rgba(255, 176, 77, 0.14) 0%, rgba(255, 255, 255, 0.98) 100%);
}

.overview-tile--success {
  background: linear-gradient(180deg, rgba(103, 197, 152, 0.12) 0%, rgba(255, 255, 255, 0.98) 100%);
}

.overview-tile--danger {
  background: linear-gradient(180deg, rgba(255, 108, 116, 0.12) 0%, rgba(255, 255, 255, 0.98) 100%);
}

.dashboard-control {
  border: 1px solid rgba(210, 225, 235, 0.92);
  background:
    radial-gradient(circle at top right, rgba(68, 163, 255, 0.12), transparent 24%),
    radial-gradient(circle at left bottom, rgba(103, 197, 152, 0.12), transparent 28%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(247, 251, 253, 0.98) 100%);
}

.dashboard-control-bar {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.dashboard-control-copy {
  display: grid;
  gap: 8px;
  max-width: 560px;
}

.section-kicker {
  color: #7a95ac;
  font-size: 11px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.dashboard-control-copy strong {
  color: #133854;
  font-size: 34px;
  line-height: 1.08;
}

.dashboard-control-copy span:last-child {
  color: #5d7890;
  font-size: 14px;
  line-height: 1.7;
}

.dashboard-control-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 10px;
}

.control-switch {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #5f7b92;
  font-size: 12px;
}

.dashboard-alert {
  margin-top: 14px;
}

.workbench-top-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.7fr) minmax(320px, 1.15fr) minmax(300px, 1fr);
  gap: 16px;
}

.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.panel-head strong {
  display: block;
  color: #173854;
  font-size: 16px;
}

.panel-head small {
  display: block;
  margin-top: 6px;
  color: #7b94a9;
  font-size: 12px;
  line-height: 1.5;
}

.panel-link {
  color: #1f8bc4;
  font-size: 12px;
  cursor: pointer;
  white-space: nowrap;
}

.hero-panel {
  min-height: 338px;
}

.hero-panel-body {
  display: grid;
  grid-template-columns: minmax(220px, 0.95fr) minmax(0, 1.35fr);
  gap: 16px;
  align-items: center;
}

.hero-stage-list {
  display: grid;
  gap: 10px;
}

.hero-stage-item {
  display: grid;
  gap: 4px;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(224, 233, 239, 0.9);
}

.hero-stage-item:last-child {
  padding-bottom: 0;
  border-bottom: 0;
}

.hero-stage-item span {
  color: #5a748b;
  font-size: 13px;
}

.hero-stage-item strong {
  color: #1774b2;
  font-size: 32px;
  line-height: 1;
}

.hero-stage-item small {
  color: #8a9caf;
  font-size: 12px;
}

.hero-chart-wrap {
  min-height: 250px;
}

.hero-funnel-chart {
  height: 250px;
}

.risk-panel,
.quick-panel {
  min-height: 338px;
}

.risk-list,
.quick-action-grid {
  display: grid;
  gap: 12px;
}

.risk-item {
  display: grid;
  grid-template-columns: 36px minmax(0, 1fr) auto;
  gap: 12px;
  align-items: center;
  width: 100%;
  padding: 16px;
  border-radius: 18px;
  border: 1px solid rgba(226, 233, 239, 0.95);
  background: #fff;
  text-align: left;
}

.risk-item--danger {
  border-color: rgba(255, 143, 130, 0.65);
  background: linear-gradient(180deg, rgba(255, 245, 243, 0.98) 0%, rgba(255, 255, 255, 0.98) 100%);
}

.risk-item--warning {
  border-color: rgba(255, 188, 92, 0.72);
  background: linear-gradient(180deg, rgba(255, 250, 240, 0.98) 0%, rgba(255, 255, 255, 0.98) 100%);
}

.risk-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 12px;
  background: linear-gradient(180deg, #ff8a3d 0%, #ff5d5d 100%);
  color: #fff;
  font-weight: 700;
  font-size: 18px;
}

.risk-copy {
  display: grid;
  gap: 4px;
}

.risk-copy strong {
  color: #173854;
  font-size: 14px;
}

.risk-copy small {
  color: #70869a;
  font-size: 12px;
  line-height: 1.5;
}

.risk-action {
  color: #1f8bc4;
  font-size: 12px;
}

.quick-action-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.quick-action {
  display: grid;
  justify-items: center;
  gap: 10px;
  min-height: 102px;
  padding: 14px 10px;
  border-radius: 18px;
  border: 1px solid rgba(224, 233, 239, 0.95);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.99) 0%, rgba(247, 250, 252, 0.99) 100%);
  color: #294256;
  font-size: 13px;
}

.quick-action-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 16px;
  color: #fff;
  font-size: 16px;
  font-weight: 700;
}

.quick-action-icon--blue {
  background: linear-gradient(180deg, #45a5ff 0%, #3285f5 100%);
}

.quick-action-icon--cyan {
  background: linear-gradient(180deg, #3ec1db 0%, #1f9ab5 100%);
}

.quick-action-icon--green {
  background: linear-gradient(180deg, #61c596 0%, #2ea86b 100%);
}

.quick-action-icon--teal {
  background: linear-gradient(180deg, #57c8b0 0%, #27998b 100%);
}

.quick-action-icon--orange {
  background: linear-gradient(180deg, #ffb04d 0%, #ff8a3d 100%);
}

.quick-action-icon--indigo {
  background: linear-gradient(180deg, #6e89ff 0%, #5067df 100%);
}

.quick-action-icon--violet {
  background: linear-gradient(180deg, #9a7dff 0%, #7657e8 100%);
}

.quick-action-icon--amber {
  background: linear-gradient(180deg, #f6c258 0%, #e49b2a 100%);
}

.signal-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 14px;
}

.signal-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-height: 116px;
  padding: 18px;
  border-radius: 22px;
  border: 1px solid rgba(218, 229, 236, 0.95);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.99) 0%, rgba(247, 250, 252, 0.99) 100%);
  box-shadow: 0 14px 32px rgba(17, 60, 93, 0.05);
}

.signal-copy {
  display: grid;
  gap: 8px;
}

.signal-copy span {
  color: #658298;
  font-size: 13px;
}

.signal-copy strong {
  color: #173854;
  font-size: 38px;
  line-height: 1;
}

.signal-copy small {
  color: #8a9eb0;
  font-size: 12px;
}

.signal-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 18px;
  font-size: 13px;
  font-weight: 700;
}

.signal-badge--blue {
  background: rgba(68, 163, 255, 0.14);
  color: #2d86db;
}

.signal-badge--cyan {
  background: rgba(63, 193, 219, 0.14);
  color: #1f94b8;
}

.signal-badge--orange,
.signal-badge--amber {
  background: rgba(255, 176, 77, 0.16);
  color: #dc8d25;
}

.signal-badge--indigo {
  background: rgba(110, 137, 255, 0.14);
  color: #566fe5;
}

.signal-badge--teal {
  background: rgba(87, 200, 176, 0.14);
  color: #289486;
}

.bed-overview-card {
  grid-column: span 2;
}

.bed-overview-body {
  display: grid;
  grid-template-columns: minmax(180px, 1.05fr) minmax(0, 0.95fr);
  gap: 8px;
  align-items: center;
}

.bed-overview-chart {
  height: 190px;
}

.bed-overview-legend {
  display: grid;
  gap: 10px;
}

.bed-legend-item {
  display: grid;
  grid-template-columns: 10px 1fr auto;
  gap: 10px;
  align-items: center;
  color: #617b91;
  font-size: 13px;
}

.bed-legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
}

.bed-legend-item strong {
  color: #173854;
}

.operations-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.8fr) minmax(290px, 0.9fr);
  gap: 16px;
}

.operations-card {
  min-height: 620px;
}

.panel-head--stack {
  display: grid;
  gap: 14px;
}

.report-chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.report-chip {
  min-height: 38px;
  padding: 0 14px;
  border-radius: 999px;
  border: 1px solid rgba(220, 229, 236, 0.95);
  background: linear-gradient(180deg, rgba(248, 250, 252, 0.98) 0%, rgba(255, 255, 255, 0.98) 100%);
  color: #466178;
  font-size: 12px;
}

.task-table {
  margin-top: 8px;
  border: 1px solid rgba(223, 232, 238, 0.94);
  border-radius: 20px;
  overflow: hidden;
}

.task-table-head,
.task-row {
  display: grid;
  grid-template-columns: minmax(220px, 1.8fr) minmax(110px, 0.8fr) minmax(110px, 0.8fr) minmax(90px, 0.6fr) minmax(90px, 0.6fr);
  gap: 12px;
  align-items: center;
  padding: 16px 18px;
}

.task-table-head {
  color: #648097;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  background: #f7fafc;
}

.task-row {
  border-top: 1px solid rgba(228, 235, 240, 0.92);
}

.task-main {
  display: grid;
  gap: 6px;
}

.task-main strong {
  color: #173854;
  font-size: 14px;
}

.task-main small,
.task-stage,
.task-metric {
  color: #72889d;
  font-size: 12px;
  line-height: 1.55;
}

.task-priority {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 30px;
  padding: 0 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.task-priority--danger {
  background: rgba(255, 108, 116, 0.14);
  color: #d04b52;
}

.task-priority--warning,
.task-priority--orange {
  background: rgba(255, 176, 77, 0.16);
  color: #d98b23;
}

.task-priority--cyan {
  background: rgba(63, 193, 219, 0.14);
  color: #1f94b8;
}

.task-priority--indigo {
  background: rgba(110, 137, 255, 0.14);
  color: #566fe5;
}

.sidebar-stack {
  display: grid;
  gap: 16px;
}

.rail-card {
  min-height: 0;
}

.reminder-list,
.snapshot-list {
  display: grid;
  gap: 12px;
}

.reminder-item {
  display: grid;
  grid-template-columns: 10px 1fr auto;
  gap: 10px;
  align-items: center;
  color: #617b91;
  font-size: 13px;
}

.reminder-item strong {
  color: #173854;
}

.reminder-dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
}

.reminder-dot--blue {
  background: #37b7d8;
}

.reminder-dot--orange {
  background: #ffb04d;
}

.reminder-dot--red {
  background: #ff6c74;
}

.reminder-dot--green {
  background: #67c598;
}

.progress-list {
  display: grid;
  gap: 14px;
}

.progress-item {
  display: grid;
  gap: 8px;
}

.progress-copy {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  align-items: baseline;
}

.progress-copy strong {
  color: #173854;
  font-size: 13px;
}

.progress-copy small {
  color: #8298ab;
  font-size: 12px;
}

.progress-value {
  color: #5f7b92;
  font-size: 12px;
}

.snapshot-item {
  display: grid;
  gap: 4px;
  padding: 12px 14px;
  border-radius: 16px;
  background: linear-gradient(180deg, rgba(248, 250, 252, 0.98) 0%, rgba(255, 255, 255, 0.98) 100%);
  border: 1px solid rgba(223, 232, 238, 0.94);
}

.snapshot-item strong {
  color: #173854;
  font-size: 13px;
}

.snapshot-item small {
  color: #8298ab;
  font-size: 12px;
}

@media (max-width: 1400px) {
  .workbench-top-grid {
    grid-template-columns: 1fr;
  }

  .signal-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .bed-overview-card {
    grid-column: span 3;
  }

  .operations-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 1100px) {
  .dashboard-control-bar,
  .hero-panel-body,
  .bed-overview-body {
    grid-template-columns: 1fr;
    display: grid;
  }

  .dashboard-control-actions {
    justify-content: flex-start;
  }

  .signal-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .bed-overview-card {
    grid-column: span 2;
  }
}

@media (max-width: 820px) {
  .marketing-overview-grid,
  .signal-grid,
  .quick-action-grid {
    grid-template-columns: 1fr;
  }

  .bed-overview-card {
    grid-column: auto;
  }

  .task-table-head {
    display: none;
  }

  .task-row {
    grid-template-columns: 1fr;
    align-items: flex-start;
  }
}
</style>
