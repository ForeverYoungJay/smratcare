<template>
  <PageContainer title="财务工作台" subTitle="把今天要处理的收费、欠费、退款、对账和经营观察收在一个入口里。">
    <StatefulBlock :loading="loading" :error="errorMessage" @retry="loadData">
      <section class="finance-stage">
        <section class="command-deck fade-up">
          <div class="command-deck__main">
            <div class="command-kicker">Finance Operations Center</div>
            <h2>{{ roleHeadline }}</h2>
            <p>{{ roleHint }}</p>
            <div class="command-search">
              <a-input
                v-model:value="searchKeyword"
                allow-clear
                placeholder="搜索长者、账单号、外部流水号、合同号"
                @pressEnter="openFinanceSearch"
              />
              <a-button type="primary" @click="openFinanceSearch">财务搜索</a-button>
              <a-button @click="loadData">刷新数据</a-button>
            </div>
            <div class="command-actions">
              <a-button type="primary" @click="go('/finance/payments/cashier-desk')">进入收银台</a-button>
              <a-button @click="go('/finance/bills/in-resident')">查看在住账单</a-button>
              <a-button @click="go('/finance/reconcile/issue-center')">处理异常</a-button>
              <a-button v-if="isManagerOrAbove" @click="go('/finance/reconcile/month-close')">查看月结</a-button>
            </div>
          </div>
          <div class="command-deck__side">
            <div class="command-side__meta">
              <span>业务日期</span>
              <strong>{{ overview?.bizDate || '-' }}</strong>
            </div>
            <div class="command-side__meta">
              <span>当前视角</span>
              <strong>{{ roleTone }}</strong>
            </div>
            <div class="signal-grid">
              <div v-for="item in headlineSignals" :key="item.label" class="signal-tile">
                <span>{{ item.label }}</span>
                <strong>{{ item.value }}</strong>
                <small>{{ item.hint }}</small>
              </div>
            </div>
          </div>
        </section>

        <section class="lane-grid">
          <article
            v-for="lane in operationLanes"
            :key="lane.key"
            class="task-lane fade-up"
            :class="`task-lane--${lane.tone}`"
          >
            <div class="task-lane__head">
              <div>
                <div class="task-lane__eyebrow">{{ lane.eyebrow }}</div>
                <h3>{{ lane.title }}</h3>
              </div>
              <div class="task-lane__hero">
                <strong>{{ lane.heroValue }}</strong>
                <span>{{ lane.heroHint }}</span>
              </div>
            </div>
            <p class="task-lane__caption">{{ lane.caption }}</p>
            <div class="task-metrics">
              <div v-for="metric in lane.metrics" :key="metric.label" class="task-metric">
                <span>{{ metric.label }}</span>
                <strong>{{ metric.value }}</strong>
              </div>
            </div>
            <div class="task-lane__actions">
              <a-button
                v-for="action in lane.actions"
                :key="action.label"
                :type="action.primary ? 'primary' : 'default'"
                @click="go(action.path)"
              >
                {{ action.label }}
              </a-button>
            </div>
            <div class="task-lane__list">
              <div v-for="item in lane.checks" :key="item.label" class="task-check">
                <div>
                  <div class="task-check__label">{{ item.label }}</div>
                  <div class="task-check__hint">{{ item.hint }}</div>
                </div>
                <a-tag :color="item.good ? 'green' : 'orange'">{{ item.status }}</a-tag>
              </div>
            </div>
          </article>
        </section>

        <section class="journey-grid">
          <article class="journey-panel fade-up">
            <div class="panel-head">
              <div>
                <div class="panel-kicker">Task Groups</div>
                <h3>按业务阶段进入功能组</h3>
              </div>
            </div>
            <div class="journey-list">
              <button
                v-for="group in financeGroups"
                :key="group.key"
                class="journey-row"
                type="button"
                @click="go(group.path)"
              >
                <div class="journey-row__copy">
                  <strong>{{ group.title }}</strong>
                  <span>{{ group.desc }}</span>
                </div>
                <div class="journey-row__meta">
                  <strong>{{ group.metric }}</strong>
                  <small>{{ group.metricLabel }}</small>
                </div>
              </button>
            </div>
          </article>

          <article class="journey-panel fade-up">
            <div class="panel-head">
              <div>
                <div class="panel-kicker">Suggested Route</div>
                <h3>今天建议这样处理</h3>
              </div>
            </div>
            <div class="journey-steps">
              <div v-for="item in suggestedSteps" :key="item.title" class="journey-step">
                <div class="journey-step__index">{{ item.index }}</div>
                <div class="journey-step__copy">
                  <strong>{{ item.title }}</strong>
                  <span>{{ item.desc }}</span>
                </div>
                <a-button type="link" @click="go(item.path)">{{ item.action }}</a-button>
              </div>
            </div>
          </article>
        </section>

        <section class="insight-grid">
          <article class="insight-panel insight-panel--dark fade-up">
            <div class="panel-head">
              <div>
                <div class="panel-kicker">Revenue Snapshot</div>
                <h3>本月营收结构</h3>
              </div>
              <a-button type="link" @click="go('/finance/reports/revenue-structure?period=this_month')">查看报表</a-button>
            </div>
            <div class="panel-stat-row">
              <div>
                <span>净营收</span>
                <strong>{{ money(overview?.revenueStructure?.monthRevenueTotal) }}</strong>
              </div>
              <div>
                <span>Top 房间</span>
                <strong>{{ topRoomLabel }}</strong>
              </div>
            </div>
            <div ref="revenueChartRef" class="revenue-chart"></div>
            <div class="chip-list">
              <button
                v-for="category in (overview?.revenueStructure?.categories || []).slice(0, 6)"
                :key="category.code"
                class="chip-link"
                type="button"
                @click="openRevenueCategory(category.code)"
              >
                <span>{{ category.name }}</span>
                <strong>{{ category.ratio }}%</strong>
              </button>
            </div>
          </article>

          <article class="insight-panel fade-up">
            <div class="panel-head">
              <div>
                <div class="panel-kicker">Integrity Watch</div>
                <h3>财务一致性与风险信号</h3>
              </div>
              <a-button type="link" @click="go('/finance/reconcile/ledger-health')">巡检明细</a-button>
            </div>
            <div class="watch-grid">
              <div class="watch-item">
                <span>巡检问题</span>
                <strong>{{ ledgerHealth?.totalIssueCount || 0 }}</strong>
              </div>
              <div class="watch-item">
                <span>高优先级洞察</span>
                <strong>{{ opsInsight?.highPriorityCount || 0 }}</strong>
              </div>
              <div class="watch-item">
                <span>待审批事项</span>
                <strong>{{ pendingCount }}</strong>
              </div>
            </div>
            <div class="watch-list">
              <button
                v-for="item in watchItems"
                :key="item.title"
                class="watch-row"
                type="button"
                @click="item.path ? go(item.path) : undefined"
              >
                <div class="watch-row__copy">
                  <strong>{{ item.title }}</strong>
                  <span>{{ item.detail }}</span>
                </div>
                <a-tag :color="item.level === 'HIGH' ? 'red' : item.level === 'MEDIUM' ? 'orange' : 'blue'">
                  {{ item.levelLabel }}
                </a-tag>
              </button>
            </div>
          </article>
        </section>

        <section class="support-grid">
          <article class="support-panel fade-up">
            <div class="panel-head">
              <div>
                <div class="panel-kicker">Focus List</div>
                <h3>今日待办与主管关注项</h3>
              </div>
            </div>
            <div class="focus-list">
              <div v-for="item in roleFocusItems" :key="item.title" class="focus-row">
                <strong>{{ item.title }}</strong>
                <span>{{ item.desc }}</span>
              </div>
            </div>
            <div class="mini-list">
              <div v-for="item in pendingList" :key="item.label" class="mini-list__row">
                <span>{{ item.label }}</span>
                <strong>{{ item.value }}</strong>
              </div>
            </div>
          </article>

          <article class="support-panel fade-up">
            <div class="panel-head">
              <div>
                <div class="panel-kicker">Quick Access</div>
                <h3>高频动作与配置入口</h3>
              </div>
              <a-button type="link" @click="go('/finance/config/master-data')">配置中心</a-button>
            </div>
            <div class="quick-grid">
              <button
                v-for="item in visibleQuickEntries.slice(0, 8)"
                :key="item.key"
                class="quick-card"
                type="button"
                @click="go(item.path)"
              >
                <strong>{{ item.label }}</strong>
                <span>{{ quickEntryHint(item.key) }}</span>
              </button>
            </div>
            <div class="config-strip">
              <div class="config-strip__metric">
                <span>费用科目</span>
                <strong>{{ masterOverview?.feeSubjectCount || 0 }}</strong>
              </div>
              <div class="config-strip__metric">
                <span>计费规则</span>
                <strong>{{ masterOverview?.billingRuleCount || 0 }}</strong>
              </div>
              <div class="config-strip__metric">
                <span>支付渠道</span>
                <strong>{{ masterOverview?.paymentChannelCount || 0 }}</strong>
              </div>
              <div class="config-strip__metric">
                <span>待审批配置</span>
                <strong>{{ masterOverview?.pendingApprovalCount || 0 }}</strong>
              </div>
            </div>
            <div class="config-note">最近配置变更：{{ latestConfigChangeText }}</div>
          </article>
        </section>
      </section>
    </StatefulBlock>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
import { getFinanceConfigChangeLogPage, getFinanceLedgerHealth, getFinanceMasterDataOverview, getFinanceOpsInsights, getFinanceWorkbenchOverview } from '../../api/finance'
import { useUserStore } from '../../stores/user'
import type {
  FinanceConfigChangeLogItem,
  FinanceLedgerHealth,
  FinanceQuickEntry,
  FinanceMasterDataOverview,
  FinanceOpsInsight,
  FinanceWorkbenchOverview
} from '../../types'
import { useECharts } from '../../plugins/echarts'

interface ActionLink {
  label: string
  path: string
  primary?: boolean
}

interface LaneCheck {
  label: string
  hint: string
  status: string
  good: boolean
}

interface OperationLane {
  key: string
  eyebrow: string
  title: string
  caption: string
  heroValue: string
  heroHint: string
  tone: 'navy' | 'green' | 'amber' | 'rose'
  metrics: Array<{ label: string; value: string }>
  actions: ActionLink[]
  checks: LaneCheck[]
}

interface FinanceGroupNavItem {
  key: string
  title: string
  desc: string
  path: string
  metric: string
  metricLabel: string
}

interface SuggestedStep {
  index: string
  title: string
  desc: string
  action: string
  path: string
}

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const errorMessage = ref('')
const searchKeyword = ref('')
const overview = ref<FinanceWorkbenchOverview | null>(null)
const masterOverview = ref<FinanceMasterDataOverview | null>(null)
const latestConfigChange = ref<FinanceConfigChangeLogItem | null>(null)
const ledgerHealth = ref<FinanceLedgerHealth | null>(null)
const opsInsight = ref<FinanceOpsInsight | null>(null)

const { chartRef: revenueChartRef, setOption: setRevenueOption } = useECharts()

const userRoles = computed(() => userStore.roles || [])
const isManagerOrAbove = computed(() => userRoles.value.some(role => ['FINANCE_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'].includes(role)))
const roleTone = computed(() => isManagerOrAbove.value ? '主管 / 结账视角' : '一线 / 收银视角')
const roleHeadline = computed(() => isManagerOrAbove.value ? '先收口风险，再推进审批、催缴和月结。' : '先完成收费闭环，再补齐票据、修正和交班。')
const roleHint = computed(() => isManagerOrAbove.value
  ? '首页先把异常、欠费、月结阻塞项排在前面，方便主管快速判断今天哪里会拖慢结账。'
  : '首页先给收银、票据、退款和交班，让一线财务少找菜单、少切页面。')
const roleFocusItems = computed(() => isManagerOrAbove.value
  ? [
      { title: '异常修复', desc: '账单、收款、票据和对账差异统一回到一个问题池。' },
      { title: '欠费推进', desc: '先看高风险欠费和续约临近人群，避免月底集中堆积。' },
      { title: '月结收口', desc: '锁账前直接看阻塞步骤和跨期动作申请。' }
    ]
  : [
      { title: '收款闭环', desc: '收款登记、票据补齐、修正复核、交班打印放在同一条链上。' },
      { title: '票据补齐', desc: '当天就把未关联收据和发票补完整，避免次日继续挂起。' },
      { title: '异常回捞', desc: '重复收款、作废账单和修正记录当天处理，不留到月底。' }
    ])

const latestConfigChangeText = computed(() => {
  if (latestConfigChange.value) {
    const actor = latestConfigChange.value.actorName || '系统'
    const entity = latestConfigChange.value.entityType || '配置项'
    const detail = latestConfigChange.value.detail || latestConfigChange.value.actionType || '已更新'
    return `${actor} · ${entity} · ${detail}`
  }
  const recentConfig = masterOverview.value?.recentConfigs?.[0]
  if (recentConfig) {
    return `${recentConfig.configKey} 自 ${recentConfig.effectiveMonth} 生效`
  }
  return '最近没有新的配置变更'
})

const headlineSignals = computed(() => [
  {
    label: '今日入账',
    value: money(overview.value?.cashier?.todayCollectedTotal),
    hint: `${overview.value?.cashier?.paymentMethods?.length || 0} 种方式`
  },
  {
    label: '欠费规模',
    value: money(overview.value?.risk?.overdueAmount),
    hint: `${overview.value?.risk?.overdueElderCount || 0} 位长者`
  },
  {
    label: '待审批',
    value: String(pendingCount.value),
    hint: '退款、减免、退住结算'
  },
  {
    label: '巡检问题',
    value: String(ledgerHealth.value?.totalIssueCount || 0),
    hint: ledgerHealth.value?.checkedAt || '等待巡检'
  }
])

const pendingCount = computed(() => (
  Number(overview.value?.pending?.pendingDiscountCount || 0)
  + Number(overview.value?.pending?.pendingRefundCount || 0)
  + Number(overview.value?.pending?.pendingDischargeSettlementCount || 0)
))

const visibleQuickEntries = computed(() => {
  const entries = overview.value?.quickEntries || []
  const preferredKeys = isManagerOrAbove.value
    ? ['issue_center', 'collection_follow_up', 'month_close', 'invoice_receipt', 'fee_adjustment', 'payment_register']
    : ['cashier_desk', 'payment_register', 'invoice_receipt', 'fee_adjustment', 'prepaid_recharge', 'issue_center']
  const score = new Map(preferredKeys.map((key, index) => [key, index]))
  return [...entries]
    .filter((item: FinanceQuickEntry) => isManagerOrAbove.value || item.key !== 'month_close')
    .sort((a: FinanceQuickEntry, b: FinanceQuickEntry) => (score.get(a.key) ?? 99) - (score.get(b.key) ?? 99))
})

const pendingList = computed(() => [
  { label: '待审批减免单', value: overview.value?.pending?.pendingDiscountCount || 0 },
  { label: '待审批退款', value: overview.value?.pending?.pendingRefundCount || 0 },
  { label: '待审核退住结算', value: overview.value?.pending?.pendingDischargeSettlementCount || 0 },
  { label: '异常工单待处理', value: overview.value?.pending?.issueTodoCount || 0 },
  { label: '催缴提醒到期', value: overview.value?.pending?.collectionReminderCount || 0 },
  { label: '已锁账月份', value: overview.value?.pending?.lockedMonthCount || 0 }
])

const financeGroups = computed<FinanceGroupNavItem[]>(() => [
  {
    key: 'collect',
    title: '收费、票据与交班',
    desc: '适合前台或一线财务，从收款登记开始一路走到交班打印。',
    path: '/finance/payments/cashier-desk',
    metric: money(overview.value?.cashier?.todayCollectedTotal),
    metricLabel: '今日收款'
  },
  {
    key: 'bills',
    title: '账单、欠费与余额',
    desc: '查看在住账单、欠费跟进、预存和押金，适合日常催缴与续费。',
    path: '/finance/bills/in-resident',
    metric: String(overview.value?.risk?.overdueElderCount || 0),
    metricLabel: '欠费人数'
  },
  {
    key: 'fix',
    title: '退款、修正与异常',
    desc: '把退款、冲正、重复收款和账票不一致问题放到一组处理。',
    path: '/finance/reconcile/issue-center',
    metric: String(ledgerHealth.value?.totalIssueCount || 0),
    metricLabel: '待修复问题'
  },
  {
    key: 'close',
    title: '对账、报表与月结',
    desc: '主管可以从这里进入经营看板、对账中心和月结锁账。',
    path: '/finance/reconcile/month-close',
    metric: String(pendingCount.value),
    metricLabel: '待审批事项'
  }
])

const suggestedSteps = computed<SuggestedStep[]>(() => [
  {
    index: '01',
    title: '先处理今日收款',
    desc: '把账单收款和票据补齐放在同一步，减少来回跳页。',
    action: '进入收银台',
    path: '/finance/payments/cashier-desk'
  },
  {
    index: '02',
    title: '再看欠费与余额',
    desc: '如果今天要催缴、续费或查余额，从在住账单和账户列表进入最快。',
    action: '查看账单',
    path: '/finance/bills/in-resident'
  },
  {
    index: '03',
    title: '当天异常当天清',
    desc: '把退款、冲正和一致性问题当天收口，月底就不会堆在一起。',
    action: '处理异常',
    path: '/finance/reconcile/issue-center'
  },
  {
    index: '04',
    title: '最后做对账和月结',
    desc: '主管在日终或月末再统一看锁账、报表和跨期申请。',
    action: '查看月结',
    path: '/finance/reconcile/month-close'
  }
])

const topRoomLabel = computed(() => {
  const room = overview.value?.roomOps?.roomTop10?.[0]
  if (!room) return '暂无'
  return `${room.building || ''}${room.roomNo || ''}`
})

const watchItems = computed(() => {
  const issueRows = (ledgerHealth.value?.issues || []).slice(0, 2).map(item => ({
    title: item.issueTypeLabel,
    detail: item.detail,
    path: item.billId ? `/finance/bill/${item.billId}` : '/finance/reconcile/issue-center',
    level: 'HIGH',
    levelLabel: '高优先'
  }))
  const insightRows = (opsInsight.value?.items || []).slice(0, 3).map(item => ({
    title: item.title,
    detail: item.detail,
    path: item.actionPath,
    level: item.level,
    levelLabel: item.level === 'HIGH' ? '高优先' : item.level === 'MEDIUM' ? '需跟进' : '观察'
  }))
  return [...issueRows, ...insightRows].slice(0, 5)
})

const operationLanes = computed<OperationLane[]>(() => {
  const todayCollected = overview.value?.cashier?.todayCollectedTotal || 0
  const todayRefund = overview.value?.cashier?.todayRefundAmount || 0
  const overdueAmount = overview.value?.risk?.overdueAmount || 0
  const overdueCount = overview.value?.risk?.overdueElderCount || 0
  const issueCount = ledgerHealth.value?.totalIssueCount || 0
  const lockedMonths = overview.value?.pending?.lockedMonthCount || 0

  return [
    {
      key: 'cashier',
      eyebrow: 'Today Cashier',
      title: '今日收款与票据',
      caption: '先看今天的收款、票据缺口和交班动作，保证一线财务闭环。',
      heroValue: money(todayCollected),
      heroHint: `今日退款 ${money(todayRefund)}`,
      tone: 'navy',
      metrics: [
        { label: '收款方式', value: String(overview.value?.cashier?.paymentMethods?.length || 0) },
        { label: '开票金额', value: money(overview.value?.cashier?.todayInvoiceAmount) },
        { label: '票据未关联', value: String(overview.value?.reconcile?.invoiceUnlinkedCount || 0) }
      ],
      actions: [
        { label: '进入收银台', path: '/finance/payments/cashier-desk', primary: true },
        { label: '收费登记', path: '/finance/payments/register?from=finance_workbench' },
        { label: '票据处理', path: '/finance/fees/payment-and-invoice?from=finance_workbench' }
      ],
      checks: [
        {
          label: '票据补齐',
          hint: overview.value?.reconcile?.invoiceUnlinkedCount ? '仍有收款未补齐收据或发票关联。' : '当前未发现票据挂起。',
          status: overview.value?.reconcile?.invoiceUnlinkedCount ? '待处理' : '已收口',
          good: !overview.value?.reconcile?.invoiceUnlinkedCount
        },
        {
          label: '日结交班',
          hint: todayCollected > 0 ? '建议收银员在当日收款完成后打印交班单。' : '今天暂未产生收款流水。',
          status: todayCollected > 0 ? '待交班' : '无流水',
          good: todayCollected === 0
        }
      ]
    },
    {
      key: 'bills',
      eyebrow: 'Bills & Arrears',
      title: '账单与欠费',
      caption: '把在住账单、欠费催缴、低余额预警和续约风险放在一起看。',
      heroValue: money(overdueAmount),
      heroHint: `${overdueCount} 位长者存在欠费`,
      tone: 'green',
      metrics: [
        { label: '低余额账户', value: String(overview.value?.risk?.lowBalanceCount || 0) },
        { label: '即将到期合同', value: String(overview.value?.risk?.expiringContractCount || 0) },
        { label: '自动扣费失败', value: String(overview.value?.autoDebit?.failedCount || 0) }
      ],
      actions: [
        { label: '在住账单', path: '/finance/bills/in-resident', primary: true },
        { label: '欠费跟进', path: '/finance/bills/follow-up?riskLevel=HIGH' },
        { label: '余额预警', path: '/finance/accounts/list?filter=low_balance' }
      ],
      checks: [
        {
          label: '高风险催缴',
          hint: overdueCount > 0 ? '优先跟进高风险欠费和合同即将到期长者。' : '当前未发现催缴高风险人群。',
          status: overdueCount > 0 ? '需推进' : '正常',
          good: overdueCount === 0
        },
        {
          label: '自动扣费异常',
          hint: overview.value?.autoDebit?.failedCount ? '建议先处理扣费失败原因，再安排补缴。' : '本期自动扣费未发现失败。',
          status: overview.value?.autoDebit?.failedCount ? '待处理' : '正常',
          good: !overview.value?.autoDebit?.failedCount
        }
      ]
    },
    {
      key: 'exceptions',
      eyebrow: 'Refunds & Fixes',
      title: '退款、冲正与异常',
      caption: '把退款审批、作废修正和一致性问题放到一条处理链里。',
      heroValue: String(issueCount),
      heroHint: `待审批退款 ${overview.value?.pending?.pendingRefundCount || 0} 笔`,
      tone: 'rose',
      metrics: [
        { label: '重复/冲正待完成', value: String(overview.value?.reconcile?.duplicatedOrReversalPendingCount || 0) },
        { label: '巡检问题', value: String(issueCount) },
        { label: '异常工单', value: String(overview.value?.pending?.issueTodoCount || 0) }
      ],
      actions: [
        { label: '退款/冲正', path: '/finance/payments/refund-reversal', primary: true },
        { label: '异常修复中心', path: '/finance/reconcile/issue-center' },
        { label: '一致性巡检', path: '/finance/reconcile/ledger-health' }
      ],
      checks: [
        {
          label: '退款复核',
          hint: overview.value?.pending?.pendingRefundCount ? '退款申请和执行记录需要同日复核。' : '当前无待审批退款。',
          status: overview.value?.pending?.pendingRefundCount ? '待审批' : '正常',
          good: !overview.value?.pending?.pendingRefundCount
        },
        {
          label: '账单修正',
          hint: issueCount ? '账单、收款、票据存在差异，建议优先修复。' : '未发现明显账务差异。',
          status: issueCount ? '待修复' : '正常',
          good: issueCount === 0
        }
      ]
    },
    {
      key: 'close',
      eyebrow: 'Reconcile & Close',
      title: '对账与月结',
      caption: '把日对账、发票核对、锁账状态和月结推进集中到一个入口。',
      heroValue: String(overview.value?.reconcile?.billPaidUnmatchedCount || 0),
      heroHint: `已锁账月份 ${lockedMonths} 个`,
      tone: 'amber',
      metrics: [
        { label: '发票未关联', value: String(overview.value?.reconcile?.invoiceUnlinkedCount || 0) },
        { label: '异常处理', value: String(overview.value?.pending?.issueTodoCount || 0) },
        { label: '月结提醒', value: String(overview.value?.pending?.collectionReminderCount || 0) }
      ],
      actions: [
        { label: '对账中心', path: '/finance/reconcile/center', primary: true },
        { label: '月结进度', path: '/finance/reconcile/month-close' },
        { label: '发票对账', path: '/finance/reconcile/invoice?filter=unlinked' }
      ],
      checks: [
        {
          label: '账单已收未核销',
          hint: overview.value?.reconcile?.billPaidUnmatchedCount ? '存在收款与账单核销不一致。' : '当前未发现收款核销差异。',
          status: overview.value?.reconcile?.billPaidUnmatchedCount ? '待对账' : '正常',
          good: !overview.value?.reconcile?.billPaidUnmatchedCount
        },
        {
          label: '月结阻塞',
          hint: lockedMonths ? '存在已锁账月份，跨期操作需要审批。' : '当前无锁账阻塞提醒。',
          status: lockedMonths ? '需关注' : '正常',
          good: lockedMonths === 0
        }
      ]
    }
  ]
})

function money(value?: number) {
  return `¥${Number(value || 0).toFixed(2)}`
}

function go(path: string) {
  router.push(path)
}

function openFinanceSearch() {
  const keyword = searchKeyword.value.trim()
  if (!keyword) {
    message.warning('请输入搜索关键词')
    return
  }
  go(`/finance/search?keyword=${encodeURIComponent(keyword)}`)
}

function openRevenueCategory(categoryCode: string) {
  if (categoryCode === 'UTILITY') {
    go('/finance/allocation/public-cost?category=utility&period=this_month')
    return
  }
  go(`/finance/reports/revenue-structure?period=this_month&category=${encodeURIComponent(categoryCode)}`)
}

function quickEntryHint(key: string) {
  const hintMap: Record<string, string> = {
    cashier_desk: '按业务日期处理收款、票据、修正和交班',
    payment_register: '适合直接登记账单收款',
    invoice_receipt: '集中处理收据、发票与打印',
    fee_adjustment: '减免、补录、改价统一提交审批',
    issue_center: '账单、收款、对账差异集中修复',
    month_close: '查看锁账状态、阻塞步骤与跨期申请',
    collection_follow_up: '按风险推进催缴与承诺回款',
    prepaid_recharge: '处理预存与押金充值'
  }
  return hintMap[key] || '进入对应模块继续处理'
}

function renderRevenueChart() {
  const categories = overview.value?.revenueStructure?.categories || []
  setRevenueOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, textStyle: { color: '#dbe7f5' } },
    series: [
      {
        type: 'pie',
        radius: ['46%', '72%'],
        center: ['50%', '44%'],
        avoidLabelOverlap: false,
        label: { color: '#dbe7f5' },
        data: categories.map(item => ({ name: item.name, value: item.amount }))
      }
    ]
  })
}

async function loadData() {
  loading.value = true
  errorMessage.value = ''
  try {
    const [workbenchData, configData, ledgerData, opsInsightData] = await Promise.all([
      getFinanceWorkbenchOverview(),
      getFinanceMasterDataOverview(),
      getFinanceLedgerHealth({ limit: 12 }),
      getFinanceOpsInsights()
    ])
    overview.value = workbenchData
    masterOverview.value = configData
    ledgerHealth.value = ledgerData
    opsInsight.value = opsInsightData
    const changeLogPage = await getFinanceConfigChangeLogPage({ pageNo: 1, pageSize: 1 })
    latestConfigChange.value = changeLogPage?.list?.[0] || null
    renderRevenueChart()
  } catch (error: any) {
    errorMessage.value = error?.message || '加载财务工作台失败'
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.finance-stage {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.fade-up {
  animation: fadeUp 0.45s ease both;
}

.fade-up:nth-child(2) {
  animation-delay: 0.05s;
}

.fade-up:nth-child(3) {
  animation-delay: 0.1s;
}

.command-deck {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(320px, 0.9fr);
  gap: 20px;
  padding: 24px;
  border-radius: 26px;
  background:
    radial-gradient(circle at left top, rgba(56, 189, 248, 0.22), transparent 30%),
    radial-gradient(circle at right bottom, rgba(249, 115, 22, 0.18), transparent 24%),
    linear-gradient(135deg, #08111f 0%, #122238 48%, #1f3b5b 100%);
  color: #f8fbff;
}

.command-kicker,
.task-lane__eyebrow,
.panel-kicker {
  font-size: 11px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: rgba(227, 238, 249, 0.64);
}

.command-deck h2 {
  margin: 10px 0 12px;
  font-size: 34px;
  line-height: 1.12;
  color: #fff;
}

.command-deck p {
  margin: 0;
  max-width: 620px;
  color: rgba(227, 238, 249, 0.76);
  line-height: 1.7;
}

.command-search {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-top: 18px;
}

.command-search :deep(.ant-input-affix-wrapper) {
  max-width: 360px;
  border: none;
  border-radius: 14px;
  background: rgba(248, 251, 255, 0.12);
  color: #fff;
}

.command-search :deep(.ant-input) {
  color: #fff;
  background: transparent;
}

.command-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 16px;
}

.command-deck__side {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 18px;
  border-radius: 22px;
  background: rgba(248, 251, 255, 0.08);
  backdrop-filter: blur(10px);
}

.command-side__meta {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(226, 232, 240, 0.14);
}

.command-side__meta span {
  color: rgba(227, 238, 249, 0.68);
}

.command-side__meta strong {
  color: #fff;
}

.signal-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.signal-tile {
  padding: 14px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.06);
}

.signal-tile span,
.task-metric span,
.panel-stat-row span,
.watch-item span,
.config-strip__metric span,
.mini-list__row span {
  display: block;
  font-size: 12px;
  color: #6f8298;
}

.signal-tile strong,
.task-metric strong,
.panel-stat-row strong,
.watch-item strong,
.config-strip__metric strong,
.mini-list__row strong {
  display: block;
  margin-top: 8px;
  font-size: 22px;
  color: #13263b;
}

.signal-tile strong {
  color: #fff;
}

.signal-tile small {
  display: block;
  margin-top: 8px;
  color: rgba(227, 238, 249, 0.66);
}

.lane-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.task-lane,
.insight-panel,
.support-panel,
.journey-panel {
  padding: 22px;
  border-radius: 24px;
  background: linear-gradient(180deg, #ffffff 0%, #f7fafc 100%);
  border: 1px solid rgba(215, 226, 237, 0.84);
  box-shadow: 0 18px 42px rgba(15, 23, 42, 0.05);
}

.task-lane {
  transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease;
}

.task-lane:hover,
.quick-card:hover,
.watch-row:hover,
.chip-link:hover {
  transform: translateY(-2px);
}

.task-lane:hover {
  box-shadow: 0 26px 54px rgba(15, 23, 42, 0.08);
}

.task-lane--navy {
  border-top: 4px solid #2157d5;
}

.task-lane--green {
  border-top: 4px solid #1f8f63;
}

.task-lane--rose {
  border-top: 4px solid #df5f72;
}

.task-lane--amber {
  border-top: 4px solid #d7921a;
}

.task-lane__head,
.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.task-lane__head h3,
.panel-head h3 {
  margin: 8px 0 0;
  font-size: 24px;
  line-height: 1.2;
  color: #13263b;
}

.task-lane__hero {
  min-width: 138px;
  text-align: right;
}

.task-lane__hero strong {
  display: block;
  font-size: 28px;
  line-height: 1.1;
  color: #13263b;
}

.task-lane__hero span,
.task-lane__caption,
.watch-row__copy span,
.focus-row span,
.config-note {
  color: #66788c;
  line-height: 1.7;
}

.task-lane__caption {
  margin: 12px 0 0;
}

.task-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.task-metric {
  padding: 12px 14px;
  border-radius: 16px;
  background: #f2f6fa;
}

.task-lane__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 16px;
}

.task-lane__list,
.focus-list,
.watch-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 16px;
}

.task-check,
.focus-row,
.watch-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 16px;
  border-radius: 16px;
  border: 1px solid #e3ecf5;
  background: rgba(255, 255, 255, 0.78);
}

.watch-row {
  width: 100%;
  text-align: left;
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}

.watch-row:hover {
  border-color: #bfd4ea;
  box-shadow: 0 14px 32px rgba(15, 23, 42, 0.05);
}

.task-check__label,
.watch-row__copy strong,
.focus-row strong {
  display: block;
  color: #13263b;
  font-weight: 700;
}

.task-check__hint {
  margin-top: 4px;
  color: #66788c;
  line-height: 1.6;
}

.journey-grid,
.insight-grid,
.support-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(0, 0.9fr);
  gap: 16px;
}

.journey-list,
.journey-steps {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 16px;
}

.journey-row,
.journey-step {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 16px 18px;
  border-radius: 18px;
  border: 1px solid #e3ecf5;
  background: rgba(255, 255, 255, 0.82);
}

.journey-row {
  width: 100%;
  text-align: left;
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}

.journey-row:hover {
  transform: translateY(-2px);
  border-color: #bfd4ea;
  box-shadow: 0 14px 32px rgba(15, 23, 42, 0.05);
}

.journey-row__copy,
.journey-step__copy {
  min-width: 0;
  flex: 1;
}

.journey-row__copy strong,
.journey-step__copy strong {
  display: block;
  color: #13263b;
}

.journey-row__copy span,
.journey-step__copy span,
.journey-row__meta small {
  display: block;
  margin-top: 4px;
  color: #66788c;
  line-height: 1.6;
}

.journey-row__meta {
  min-width: 136px;
  text-align: right;
}

.journey-row__meta strong,
.journey-step__index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 46px;
  min-height: 46px;
  padding: 0 14px;
  border-radius: 999px;
  background: #ecf3fb;
  color: #1d4ed8;
  font-weight: 700;
}

.journey-step {
  align-items: flex-start;
}

.insight-panel--dark {
  background:
    radial-gradient(circle at right top, rgba(59, 130, 246, 0.18), transparent 28%),
    linear-gradient(145deg, #0f172a 0%, #16263d 52%, #1a3552 100%);
  border-color: rgba(148, 163, 184, 0.18);
}

.insight-panel--dark h3,
.insight-panel--dark .panel-kicker,
.insight-panel--dark .panel-stat-row strong,
.insight-panel--dark .chip-link strong,
.insight-panel--dark .chip-link span {
  color: #edf4fb;
}

.insight-panel--dark .panel-stat-row span {
  color: rgba(227, 238, 249, 0.68);
}

.panel-stat-row,
.watch-grid,
.config-strip,
.mini-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.revenue-chart {
  height: 280px;
  margin-top: 8px;
}

.chip-list,
.quick-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 10px;
  margin-top: 16px;
}

.chip-link,
.quick-card {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid rgba(191, 212, 234, 0.22);
  border-radius: 16px;
  text-align: left;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.chip-link {
  background: rgba(255, 255, 255, 0.06);
}

.quick-card {
  background: linear-gradient(180deg, #ffffff 0%, #f7fafc 100%);
  border-color: #d8e4ef;
}

.quick-card strong,
.chip-link strong {
  display: block;
}

.quick-card span,
.chip-link span {
  display: block;
  margin-top: 6px;
  line-height: 1.5;
}

.quick-card span {
  color: #6f8298;
}

.config-note {
  margin-top: 14px;
}

@keyframes fadeUp {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 1200px) {
  .command-deck,
  .journey-grid,
  .insight-grid,
  .support-grid {
    grid-template-columns: 1fr;
  }

  .lane-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .command-deck,
  .task-lane,
  .insight-panel,
  .support-panel,
  .journey-panel {
    padding: 18px;
    border-radius: 20px;
  }

  .command-deck h2 {
    font-size: 28px;
  }

  .command-search,
  .task-lane__head,
  .panel-head,
  .journey-row,
  .journey-step {
    flex-direction: column;
    align-items: stretch;
  }

  .signal-grid,
  .task-metrics,
  .panel-stat-row,
  .watch-grid,
  .config-strip,
  .mini-list {
    grid-template-columns: 1fr;
  }

  .task-lane__hero {
    text-align: left;
  }

  .journey-row__meta {
    text-align: left;
  }
}
</style>
