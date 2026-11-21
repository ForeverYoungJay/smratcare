<template>
  <WorkbenchModuleCard
    :title="managementView ? '财务部今日运行' : '今日收费与交班'"
    :eyebrow="managementView ? '部长视图' : '员工视图'"
    class="finance-workbench"
  >
    <template #extra>
      <a-space size="small" wrap>
        <StatusTag :text="detail.scopeLabel" :tone="managementView ? 'normal' : 'pending'" />
        <a-button type="link" @click="open(managementView ? '/finance/workbench' : '/finance/payments/cashier-desk')">
          {{ managementView ? '进入财务管理' : '进入收银台' }}
        </a-button>
      </a-space>
    </template>

    <div class="finance-command-bar">
      <div>
        <span>{{ managementView ? 'RISK BEFORE CLOSE' : 'CASHIER FLOW' }}</span>
        <strong>{{ managementView ? '先收口欠费、审批和对账风险，再推进月结。' : '先完成收款，再核对票据、修正和交班。' }}</strong>
        <p>{{ managementView ? '经营风险来自财务汇总接口，金额与数量均为机构口径。' : '当日数字是机构收银口径，不作为个人工作量或绩效。' }}</p>
      </div>
      <div class="finance-actions" aria-label="财务常用操作">
        <button v-for="action in visibleActions" :key="action.path" type="button" @click="open(action.path)">
          <span aria-hidden="true">{{ action.icon }}</span><strong>{{ action.label }}</strong>
        </button>
      </div>
    </div>

    <template v-if="managementView">
      <div class="finance-risk-grid" aria-label="财务风险概览">
        <button v-for="risk in detail.risks" :key="risk.key" type="button" @click="open(risk.path)">
          <span>{{ risk.label }}</span>
          <strong>{{ risk.key === 'arrearsAmount' ? money(risk.value) : number(risk.value) }}<small>{{ risk.suffix }}</small></strong>
          <em>{{ risk.value > 0 ? '需要跟进 →' : '当前正常' }}</em>
        </button>
      </div>

      <div class="finance-columns">
        <section class="finance-panel" aria-labelledby="finance-approval-heading">
          <div class="finance-panel__head">
            <div><span>审批队列</span><h4 id="finance-approval-heading">待审核与待确认</h4></div>
            <StatusTag :text="`${pendingTotal} 项`" :tone="pendingTotal ? 'warning' : 'offline'" />
          </div>
          <div class="finance-review-list">
            <button type="button" @click="open('/workbench/approvals')"><span>折扣审批</span><strong>{{ number(detail.overview.pending?.pendingDiscountCount) }}</strong><em>进入审批</em></button>
            <button type="button" @click="open('/workbench/approvals')"><span>退款审批</span><strong>{{ number(detail.overview.pending?.pendingRefundCount) }}</strong><em>进入审批</em></button>
            <button type="button" @click="open('/finance/discharge/review')"><span>退住结算</span><strong>{{ number(detail.overview.pending?.pendingDischargeSettlementCount) }}</strong><em>进入审核</em></button>
          </div>
        </section>

        <section class="finance-panel" aria-labelledby="finance-method-heading">
          <div class="finance-panel__head">
            <div><span>收款结构</span><h4 id="finance-method-heading">今日收款方式</h4></div>
            <StatusTag :text="`${detail.paymentMethods.length} 种`" :tone="detail.paymentMethods.length ? 'normal' : 'offline'" />
          </div>
          <div v-if="detail.paymentMethods.length" class="finance-methods">
            <div v-for="method in detail.paymentMethods" :key="method.method"><span>{{ method.methodLabel }}</span><strong>¥ {{ money(method.amount) }}</strong></div>
          </div>
          <a-empty v-else description="今天暂无已确认收款">
            <a-button @click="open('/finance/payments/records')">查看收款流水</a-button>
          </a-empty>
        </section>
      </div>
    </template>

    <template v-else>
      <a-alert
        class="finance-scope-alert"
        type="info"
        show-icon
        message="当前后端暂无按收银员统计的个人收款和处理记录接口"
        description="下方金额与异常均为机构当日口径，仅用于当班业务闭环，不展示为个人业绩。"
      />
      <div class="finance-columns finance-columns--employee">
        <section class="finance-panel" aria-labelledby="finance-sequence-heading">
          <div class="finance-panel__head"><div><span>工作顺序</span><h4 id="finance-sequence-heading">今天建议这样处理</h4></div></div>
          <ol class="finance-steps">
            <li><strong>01</strong><div><b>收款与票据</b><span>核对长者、账期、金额和支付方式，及时补齐票据。</span></div></li>
            <li><strong>02</strong><div><b>退款与异常</b><span>检查修正记录和账单差异，危险操作必须走审批。</span></div></li>
            <li><strong>03</strong><div><b>日结与交班</b><span>确认收款、票据和异常均已核对，再完成交班。</span></div></li>
          </ol>
        </section>

        <section class="finance-panel" aria-labelledby="finance-cashier-heading">
          <div class="finance-panel__head">
            <div><span>当班参考</span><h4 id="finance-cashier-heading">机构今日收银</h4></div>
            <StatusTag text="非个人业绩" tone="pending" />
          </div>
          <div class="finance-cashier-total"><span>今日实收</span><strong>¥ {{ money(detail.overview.cashier?.todayCollectedTotal) }}</strong><small>今日退款 ¥ {{ money(detail.overview.cashier?.todayRefundAmount) }}</small></div>
          <div v-if="detail.paymentMethods.length" class="finance-methods finance-methods--compact">
            <div v-for="method in detail.paymentMethods" :key="method.method"><span>{{ method.methodLabel }}</span><strong>¥ {{ money(method.amount) }}</strong></div>
          </div>
          <a-empty v-else description="今天暂无已确认收款"><a-button @click="open('/finance/payments/cashier-desk')">开始收费</a-button></a-empty>
        </section>
      </div>
    </template>

    <section class="finance-panel finance-panel--close" aria-labelledby="finance-close-heading">
      <div class="finance-panel__head">
        <div><span>闭环检查</span><h4 id="finance-close-heading">{{ managementView ? '今日账务异常' : '交班前必须核对' }}</h4></div>
        <StatusTag :text="closeIssueCount ? `待处理 ${closeIssueCount}` : '全部清零'" :tone="closeIssueCount ? 'danger' : 'normal'" />
      </div>
      <div class="finance-checks">
        <button v-for="check in detail.closeChecks" :key="check.key" type="button" @click="open(check.path)">
          <span class="finance-checks__mark" aria-hidden="true">{{ check.count ? '!' : '✓' }}</span>
          <div><strong>{{ check.label }}</strong><span>{{ check.statusLabel }}</span></div>
          <em>查看 →</em>
        </button>
      </div>
      <div class="finance-close-actions">
        <a-button type="primary" @click="open('/finance/payments/shift-close')">进入日结与交班</a-button>
        <a-button v-if="managementView" @click="open('/finance/reconcile/month-close')">查看月结进度</a-button>
      </div>
    </section>
  </WorkbenchModuleCard>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import StatusTag from '../smartcare/StatusTag.vue'
import WorkbenchModuleCard from '../smartcare/WorkbenchModuleCard.vue'
import type { FinanceWorkbenchDetail } from '../../workbench/finance'

const props = defineProps<{
  detail: FinanceWorkbenchDetail
  managementView: boolean
  canAccess: (path: string) => boolean
}>()

const emit = defineEmits<{ open: [path: string] }>()

const actions = [
  { label: '收银台', icon: '收', path: '/finance/payments/cashier-desk', managementOnly: false },
  { label: '收费登记', icon: '费', path: '/finance/payments/register', managementOnly: false },
  { label: '收款流水', icon: '流', path: '/finance/payments/records', managementOnly: false },
  { label: '发票收据', icon: '票', path: '/finance/fees/payment-and-invoice', managementOnly: false },
  { label: '退款冲正', icon: '退', path: '/finance/payments/refund-reversal', managementOnly: false },
  { label: '欠费跟进', icon: '欠', path: '/finance/bills/follow-up', managementOnly: true },
  { label: '待我审批', icon: '审', path: '/workbench/approvals', managementOnly: true },
  { label: '月结进度', icon: '结', path: '/finance/reconcile/month-close', managementOnly: true }
]

const visibleActions = computed(() => actions.filter((item) => (!item.managementOnly || props.managementView) && props.canAccess(item.path)))
const pendingTotal = computed(() => Number(props.detail.overview.pending?.pendingDiscountCount || 0) + Number(props.detail.overview.pending?.pendingRefundCount || 0) + Number(props.detail.overview.pending?.pendingDischargeSettlementCount || 0))
const closeIssueCount = computed(() => props.detail.closeChecks.reduce((total, item) => total + item.count, 0))

function open(path: string) { emit('open', path) }
function number(value?: number) { return Number(value || 0).toLocaleString('zh-CN') }
function money(value?: number) { return Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) }
</script>

<style scoped>
.finance-command-bar { display: grid; grid-template-columns: minmax(260px, 1fr) minmax(420px, 1.35fr); gap: 20px; padding: 20px; border: 1px solid #d9e2dc; border-left: 5px solid #176b4d; background: #f6faf7; }
.finance-command-bar > div:first-child { display: grid; align-content: center; gap: 5px; }
.finance-command-bar span, .finance-panel__head span { color: #607068; font-size: 12px; font-weight: 700; letter-spacing: .08em; }
.finance-command-bar strong { color: #163d30; font-size: 18px; line-height: 1.5; }
.finance-command-bar p { margin: 0; color: #58655f; font-size: 14px; }
.finance-actions { display: grid; grid-template-columns: repeat(4, minmax(82px, 1fr)); gap: 8px; }
.finance-actions button { min-height: 72px; padding: 10px 8px; border: 1px solid #d7e1da; border-radius: 4px; background: #fff; color: #263c33; cursor: pointer; }
.finance-actions button span { display: block; margin-bottom: 5px; color: #176b4d; font-size: 16px; letter-spacing: 0; }
.finance-actions button strong { font-size: 14px; }
.finance-actions button:hover { border-color: #176b4d; background: #f2f8f4; }
.finance-risk-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; margin-top: 16px; }
.finance-risk-grid button { min-height: 120px; padding: 16px; text-align: left; border: 1px solid #e1d7c7; border-top: 4px solid #9b5a1a; border-radius: 4px; background: #fffaf2; cursor: pointer; }
.finance-risk-grid span { display: block; color: #6e6253; font-size: 14px; }
.finance-risk-grid strong { display: block; margin: 8px 0; color: #512f14; font-size: 25px; }
.finance-risk-grid small { margin-left: 4px; font-size: 13px; }
.finance-risk-grid em, .finance-review-list em, .finance-checks em { color: #80684f; font-size: 12px; font-style: normal; }
.finance-columns { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-top: 16px; }
.finance-panel { padding: 18px; border: 1px solid #dfe5e1; border-radius: 4px; background: #fff; }
.finance-panel__head { display: flex; align-items: center; justify-content: space-between; gap: 12px; margin-bottom: 14px; }
.finance-panel__head h4 { margin: 2px 0 0; color: #24372f; font-size: 17px; }
.finance-review-list, .finance-methods, .finance-checks { display: grid; gap: 8px; }
.finance-review-list { grid-template-columns: repeat(3, 1fr); }
.finance-review-list button { min-height: 94px; padding: 12px; text-align: left; border: 1px solid #e1e5e2; border-radius: 3px; background: #fafbfa; cursor: pointer; }
.finance-review-list span, .finance-review-list strong, .finance-review-list em { display: block; }
.finance-review-list strong { margin: 6px 0; color: #7a4211; font-size: 24px; }
.finance-methods { grid-template-columns: repeat(2, 1fr); }
.finance-methods div { display: flex; justify-content: space-between; gap: 10px; padding: 12px; border-bottom: 1px solid #e8ece9; font-size: 14px; }
.finance-methods strong { color: #176b4d; }
.finance-scope-alert { margin-top: 16px; }
.finance-columns--employee { align-items: stretch; }
.finance-steps { display: grid; gap: 14px; margin: 0; padding: 0; list-style: none; }
.finance-steps li { display: grid; grid-template-columns: 40px 1fr; gap: 10px; align-items: start; }
.finance-steps li > strong { color: #176b4d; font-size: 20px; }
.finance-steps b, .finance-steps span { display: block; }
.finance-steps b { color: #273b32; font-size: 15px; }
.finance-steps span { margin-top: 3px; color: #66736d; font-size: 14px; line-height: 1.6; }
.finance-cashier-total { display: grid; padding: 16px; border-left: 4px solid #176b4d; background: #f5f9f6; }
.finance-cashier-total span, .finance-cashier-total small { color: #617068; font-size: 13px; }
.finance-cashier-total strong { margin: 4px 0; color: #164b37; font-size: 28px; }
.finance-methods--compact { margin-top: 8px; }
.finance-panel--close { margin-top: 16px; }
.finance-checks { grid-template-columns: repeat(3, 1fr); }
.finance-checks button { display: grid; grid-template-columns: 34px 1fr auto; gap: 10px; align-items: center; min-height: 72px; padding: 12px; text-align: left; border: 1px solid #e2e6e3; border-radius: 3px; background: #fafbfa; cursor: pointer; }
.finance-checks__mark { display: grid; place-items: center; width: 30px; height: 30px; border-radius: 50%; background: #eaf3ed; color: #176b4d; font-size: 15px; }
.finance-checks button div strong, .finance-checks button div span { display: block; }
.finance-checks button div strong { color: #2d3e36; font-size: 14px; }
.finance-checks button div span { margin-top: 3px; color: #69756f; font-size: 13px; }
.finance-close-actions { display: flex; gap: 10px; margin-top: 14px; }
.finance-actions button:focus-visible, .finance-risk-grid button:focus-visible, .finance-review-list button:focus-visible, .finance-checks button:focus-visible { outline: 3px solid rgba(23, 107, 77, .28); outline-offset: 2px; }
@media (max-width: 1100px) { .finance-command-bar { grid-template-columns: 1fr; } .finance-risk-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 760px) { .finance-actions { grid-template-columns: repeat(2, 1fr); } .finance-columns, .finance-risk-grid, .finance-checks { grid-template-columns: 1fr; } .finance-review-list { grid-template-columns: 1fr; } .finance-close-actions { flex-direction: column; } }
</style>
