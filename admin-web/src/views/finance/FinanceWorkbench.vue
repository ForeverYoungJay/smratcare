<template>
  <PageContainer class="rm-page" title="财务工作台" sub-title="今日收款、待办审批、逾期账单与资金风险集中处理">
    <template #extra>
      <a-space wrap>
        <a-button :loading="loading" @click="load">刷新</a-button>
        <a-button @click="go('/finance/bills/detail-query')">生成账单</a-button>
        <a-button type="primary" @click="go('/finance/payments/cashier-desk')">快捷收银</a-button>
      </a-space>
    </template>

    <a-alert v-if="errorMessage" class="fw-alert" type="error" show-icon
      :message="forbidden ? '当前账号没有财务数据权限' : errorMessage" />

    <div class="rm-kpis">
      <button v-for="card in kpis" :key="card.key" type="button" class="rm-kpi" @click="go(card.path)">
        <span class="rm-kpi__icon" :class="card.tone && `rm-kpi__icon--${card.tone}`"><component :is="card.icon" /></span>
        <span class="rm-kpi__label">{{ card.label }}</span>
        <span class="rm-kpi__value">{{ overview ? card.value : '--' }}</span>
        <span class="rm-kpi__hint">{{ card.hint }}</span>
      </button>
    </div>

    <div class="rm-split fw-grid">
      <section class="rm-card">
        <div class="rm-card__head">
          <span class="rm-card__title">今日财务待办 <small class="rm-card__sub">{{ overview?.bizDate ? `业务日期 ${overview.bizDate}` : '' }}</small></span>
          <a class="rm-link" @click="go('/workbench/todo')">全部待办 →</a>
        </div>
        <div v-if="!todos.length" class="fw-empty">{{ overview ? '今日没有待处理的财务事项' : '暂无数据' }}</div>
        <button v-for="item in todos" :key="item.label" type="button" class="rm-row fw-todo" @click="go(item.path)">
          <span class="fw-dot" :class="`fw-dot--${item.tone}`" />
          <div class="rm-row__main">
            <span class="rm-row__title">{{ item.label }} · {{ item.count }}{{ item.unit }}</span>
            <span class="rm-row__meta">{{ item.hint }}</span>
          </div>
          <span class="rm-link">办理 →</span>
        </button>
      </section>

      <section class="rm-card">
        <div class="rm-card__head"><span class="rm-card__title">近 6 个月收款与应收趋势</span><a class="rm-link" @click="go('/finance/reports/overall')">经营分析 →</a></div>
        <TrendChart v-if="trend.length" :labels="trend.map((p) => p.label)" :series="[
          { name: '实收金额', color: '#1f9d74', values: trend.map((p) => p.received) },
          { name: '账单应收', color: '#7aa7c7', values: trend.map((p) => p.billed) }]" />
        <div v-else class="fw-empty">暂无趋势数据</div>
        <p class="fw-muted">实收按收款时间、应收按账单月汇总，两者口径不同，不能直接相减作为欠费。</p>
      </section>
    </div>

    <div class="rm-split fw-grid">
      <section class="rm-card">
        <div class="rm-card__head"><span class="rm-card__title">最近收款记录</span><a class="rm-link" @click="go('/finance/payments/records')">前往收款流水 →</a></div>
        <a-table size="small" :data-source="payments" :columns="columns" :pagination="false" :loading="paymentLoading" row-key="id">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'paidAt'">{{ record.paidAt ? dayjs(record.paidAt).format('MM-DD HH:mm') : '--' }}</template>
            <template v-else-if="column.key === 'elder'">{{ record.elderName || '--' }}<small v-if="record.billMonth" class="fw-sub"> {{ record.billMonth }}</small></template>
            <template v-else-if="column.key === 'amount'">{{ money(record.amount) }}</template>
            <template v-else-if="column.key === 'payMethod'">{{ METHOD[record.payMethod] || record.payMethod }}</template>
            <template v-else-if="column.key === 'bill'"><a class="rm-link" @click="go(`/finance/bill/${record.billMonthlyId}`)">查看账单</a></template>
          </template>
        </a-table>
      </section>

      <section class="rm-card">
        <div class="rm-card__head"><span class="rm-card__title">欠费账龄分布</span><a class="rm-link" @click="go('/finance/bills/follow-up')">欠费与催缴 →</a></div>
        <div v-if="!aging.length" class="fw-empty">暂无欠费数据</div>
        <div v-else class="fw-bars">
          <div v-for="row in aging" :key="row.key" class="fw-bar fw-bar--aging">
            <span>{{ row.label }}</span>
            <a-progress :percent="row.percent" :show-info="false" :stroke-color="AGING_COLOR[row.key]" size="small" />
            <em>{{ money(row.amount) }}</em>
          </div>
        </div>
        <p class="fw-muted">统计口径：未结清账单以账单月最后一天为应缴日计算逾期天数；当月账单计为未到期。</p>
      </section>
    </div>

    <div class="rm-split fw-grid">
      <section class="rm-card">
        <div class="rm-card__head"><span class="rm-card__title">快捷办理</span></div>
        <div class="fw-quick">
          <button v-for="item in QUICK" :key="item.label" type="button" @click="go(item.path)"><component :is="item.icon" /><span>{{ item.label }}</span></button>
        </div>
        <div class="fw-terms">
          <div><b>应收账单</b><span>已确认收费义务，不等于实际收款</span></div>
          <div><b>预收服务费</b><span>尚未提供服务的预收款，单独跟踪</span></div>
          <div><b>押金</b><span>受用途与存管约束，不作为通用余额</span></div>
        </div>
      </section>
      <section class="rm-card">
        <div class="rm-card__head"><span class="rm-card__title">本月收入结构</span><a class="rm-link" @click="go('/finance/reports/revenue-structure')">经营分析 →</a></div>
        <div v-if="!revenue.length" class="fw-empty">本月暂无收入数据</div>
        <div v-else class="fw-bars">
          <div v-for="row in revenue" :key="row.code" class="fw-bar">
            <span>{{ row.name }}</span>
            <a-progress :percent="row.percent" :show-info="false" stroke-color="#1f9d74" size="small" />
            <em>{{ money(row.amount) }}</em>
          </div>
        </div>
        <p class="fw-muted">本月收入合计 {{ money(overview?.revenueStructure?.monthRevenueTotal) }}；收入确认不等于现金到账。</p>
      </section>
    </div>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'
import { AlertOutlined, AuditOutlined, FileTextOutlined, LogoutOutlined, SafetyCertificateOutlined, TeamOutlined, WalletOutlined } from '@ant-design/icons-vue'
import PageContainer from '../../components/PageContainer.vue'
import { getFinanceArrearsAging, getFinanceMonthlyRevenue, getFinanceWorkbenchOverview, getPaymentRecordPage } from '../../api/finance'
import TrendChart from '../marketing/center/TrendChart.vue'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'
import type { FinanceArrearsAgingItem, FinanceReportMonthlyItem, FinanceWorkbenchOverview, PaymentRecordItem } from '../../types'
import { agingRows, financeTodos } from './workbenchTodos'
import '../elder/residentUi.css'

const METHOD: Record<string, string> = { CASH: '现金', CARD: '刷卡', BANK: '银行转账', WECHAT_OFFLINE: '微信（线下）', ALIPAY: '支付宝', WECHAT: '微信', QR_CODE: '扫码' }
const AGING_COLOR: Record<string, string> = { CURRENT: '#1f9d74', D30: '#e3b55c', D60: '#e09a3e', D90: '#e07a6a', D90_PLUS: '#d9534f' }
const QUICK = [
  { label: '收银开单', icon: WalletOutlined, path: '/finance/payments/cashier-desk' },
  { label: '票据管理', icon: FileTextOutlined, path: '/finance/fees/payment-and-invoice' },
  { label: '长者账户', icon: TeamOutlined, path: '/finance/accounts/list' },
  { label: '退住结算', icon: LogoutOutlined, path: '/finance/discharge/settlement' }
]
const columns = [
  { title: '时间', key: 'paidAt', width: 110 },
  { title: '长者', key: 'elder' },
  { title: '金额', key: 'amount' },
  { title: '渠道', key: 'payMethod' },
  { title: '登记人', dataIndex: 'operatorStaffName' },
  { title: '', key: 'bill', width: 90 }
]

const router = useRouter()
const overview = ref<FinanceWorkbenchOverview | null>(null)
const payments = ref<PaymentRecordItem[]>([])
const monthly = ref<FinanceReportMonthlyItem[]>([])
const agingRaw = ref<FinanceArrearsAgingItem[]>([])
const loading = ref(false)
const paymentLoading = ref(false)
const errorMessage = ref('')
const forbidden = ref(false)

const n = (value: unknown) => Number(value ?? 0)
const money = (value: unknown) => `¥${n(value).toLocaleString('zh-CN', { maximumFractionDigits: 2 })}`

const kpis = computed(() => {
  const o = overview.value
  const approvals = n(o?.pending?.pendingRefundCount) + n(o?.pending?.pendingDiscountCount) + n(o?.pending?.pendingDischargeSettlementCount)
  return [
    { key: 'received', label: '今日已收款', value: money(o?.cashier?.todayCollectedTotal), hint: `今日退款 ${money(o?.cashier?.todayRefundAmount)}`, icon: WalletOutlined, path: '/finance/payments/records' },
    { key: 'invoice', label: '今日开票', value: money(o?.cashier?.todayInvoiceAmount), hint: `${n(o?.reconcile?.invoiceUnlinkedCount)} 张票据未关联收款`, icon: FileTextOutlined, tone: 'info', path: '/finance/fees/payment-and-invoice' },
    { key: 'arrears', label: '逾期欠费', value: money(o?.risk?.overdueAmount), hint: `${n(o?.risk?.overdueElderCount)} 位长者需要跟进`, icon: AlertOutlined, tone: 'danger', path: '/finance/bills/follow-up' },
    { key: 'approval', label: '待审批事项', value: `${approvals} 项`, hint: `${n(o?.pending?.pendingRefundCount)} 笔退款 · ${n(o?.pending?.pendingDiscountCount)} 笔减免`, icon: SafetyCertificateOutlined, tone: 'warning', path: '/oa/approval' },
    { key: 'issues', label: '待处理异常', value: `${n(o?.pending?.issueTodoCount)} 项`, hint: `对账未匹配 ${n(o?.reconcile?.billPaidUnmatchedCount)} 笔`, icon: AuditOutlined, tone: 'danger', path: '/finance/reconcile/issue-center' }
  ]
})
const todos = computed(() => financeTodos(overview.value))
const revenue = computed(() => (overview.value?.revenueStructure?.categories || []).map((c) => ({
  ...c, percent: Math.round(n(c.ratio) <= 1 ? n(c.ratio) * 100 : n(c.ratio))
})))

const trend = computed(() => monthly.value.map((m) => ({ label: dayjs(`${m.month}-01`).format('M月'), received: n(m.receivedAmount), billed: n(m.billedAmount) })))
const aging = computed(() => agingRows(agingRaw.value))

function go(path: string) { router.push(path) }

async function loadPayments() {
  paymentLoading.value = true
  try {
    const page = await getPaymentRecordPage({ pageNo: 1, pageSize: 5, from: dayjs().subtract(30, 'day').format('YYYY-MM-DD'), to: dayjs().format('YYYY-MM-DD') })
    payments.value = page.list || []
  } catch {
    payments.value = []
  } finally {
    paymentLoading.value = false
  }
}

async function load() {
  if (loading.value) return
  loading.value = true; errorMessage.value = ''; forbidden.value = false
  try {
    overview.value = await getFinanceWorkbenchOverview() as unknown as FinanceWorkbenchOverview
  } catch (error: any) {
    overview.value = null; forbidden.value = Number(error?.response?.status) === 403
    errorMessage.value = error?.message || '财务工作台加载失败，请重试'
  } finally { loading.value = false }
  loadPayments()
  const quiet = { silent403: true }
  const [m, a] = await Promise.allSettled([getFinanceMonthlyRevenue({ months: 6 }), getFinanceArrearsAging(quiet)])
  monthly.value = m.status === 'fulfilled' ? m.value || [] : []
  agingRaw.value = a.status === 'fulfilled' ? a.value || [] : []
}
onMounted(load)
useLiveSyncRefresh({ topics: ['finance'], refresh: load, debounceMs: 900 })
</script>

<style scoped>
.fw-alert { margin-bottom: 12px; }
.fw-grid { grid-template-columns: minmax(0, 1.6fr) minmax(300px, 1fr); margin-top: 16px; }
.fw-empty { padding: 24px 8px; color: #7b877e; text-align: center; }
.fw-muted { margin: 10px 0 0; color: #8a968f; font-size: 12px; }
.fw-todo { width: 100%; border: 0; background: none; text-align: left; cursor: pointer; }
.fw-dot { flex: 0 0 8px; height: 8px; border-radius: 50%; background: #1f9d74; }
.fw-dot--warning { background: #d9a441; }
.fw-dot--danger { background: #d9534f; }
.fw-bars { display: grid; gap: 10px; }
.fw-bar { display: grid; grid-template-columns: 80px 1fr 100px; align-items: center; gap: 8px; font-size: 13px; }
.fw-bar--aging { grid-template-columns: 96px 1fr 100px; }
.fw-sub { color: #8a968f; }
.fw-bar em { color: #6b7a74; font-style: normal; text-align: right; }
.fw-quick { display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; }
.fw-quick button { display: grid; justify-items: center; gap: 8px; padding: 18px 4px; border: 1px solid #e3ebe6; border-radius: 10px; background: #fff; color: #1f6f55; font-size: 22px; cursor: pointer; }
.fw-quick button span { color: #33433a; font-size: 13px; }
.fw-quick button:hover { border-color: #1f9d74; background: #f3faf6; }
.fw-terms { display: grid; gap: 8px; margin-top: 14px; }
.fw-terms div { display: grid; gap: 2px; padding: 8px 10px; border-radius: 8px; background: #f5f8f6; font-size: 12px; }
.fw-terms span { color: #7b877e; }
@media (max-width: 1100px) { .fw-grid { grid-template-columns: 1fr; } }
</style>
