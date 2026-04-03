<template>
  <PageContainer title="收银台" subTitle="把收费、票据、修正和交班放进一条顺手的操作链。">
    <div class="cashier-shell">
      <section class="cashier-top fade-up">
        <div class="cashier-top__copy">
          <div class="cashier-kicker">Cashier Flow</div>
          <h2>一线财务只需要盯住今天这四步。</h2>
          <p>先收款，再补票据，再复核修正，最后打印交班单。页面不再让你来回找模块。</p>
        </div>
        <div class="cashier-top__controls">
          <a-form layout="inline" :model="query" class="search-form">
            <a-form-item label="业务日期">
              <a-date-picker v-model:value="query.date" />
            </a-form-item>
            <a-form-item label="收款方式">
              <a-select v-model:value="query.method" allow-clear style="width: 160px" placeholder="全部方式">
                <a-select-option v-for="item in methodOptions" :key="item.value" :value="item.value">
                  {{ item.label }}
                </a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item>
              <a-space>
                <a-button type="primary" @click="loadData">刷新</a-button>
                <a-button @click="reset">重置</a-button>
              </a-space>
            </a-form-item>
          </a-form>
          <div class="preset-strip">
            <button
              v-for="preset in datePresets"
              :key="preset.key"
              class="preset-chip"
              type="button"
              @click="applyDatePreset(preset.key)"
            >
              {{ preset.label }}
            </button>
            <button class="preset-chip preset-chip--ghost" type="button" @click="query.method = undefined">
              全部收款方式
            </button>
          </div>
          <div class="cashier-top__actions">
            <a-button type="primary" @click="go('/finance/payments/register?from=cashier_desk')">收费登记</a-button>
            <a-button @click="go('/finance/fees/payment-and-invoice?from=cashier_desk')">票据处理</a-button>
            <a-button @click="go('/finance/payments/refund-reversal?from=cashier_desk')">退款/冲正</a-button>
            <a-button @click="printShiftSummary">打印交班单</a-button>
          </div>
        </div>
      </section>

      <section class="step-strip fade-up">
        <article v-for="step in processSteps" :key="step.key" class="step-card">
          <div class="step-card__index">{{ step.index }}</div>
          <div class="step-card__copy">
            <strong>{{ step.title }}</strong>
            <span>{{ step.hint }}</span>
          </div>
          <a-tag :color="step.done ? 'green' : 'orange'">{{ step.status }}</a-tag>
        </article>
      </section>

      <StatefulBlock :loading="loading" :error="errorMessage" @retry="loadData">
        <section class="summary-strip fade-up">
          <div class="summary-tile summary-tile--ink">
            <span>当日收款</span>
            <strong>{{ money(totalAmount) }}</strong>
            <small>{{ filteredPayments.length }} 笔流水</small>
          </div>
          <div class="summary-tile summary-tile--amber">
            <span>待补票据</span>
            <strong>{{ pendingInvoices.length }}</strong>
            <small>{{ money(pendingInvoiceAmount) }} 待补齐</small>
          </div>
          <div class="summary-tile summary-tile--rose">
            <span>待复核修正</span>
            <strong>{{ todayAdjustments.length }}</strong>
            <small>{{ money(adjustmentAmount) }} 需确认</small>
          </div>
          <div class="summary-tile summary-tile--green">
            <span>交班状态</span>
            <strong>{{ closingStatus }}</strong>
            <small>{{ closingHint }}</small>
          </div>
        </section>

        <section class="workspace-grid">
          <article class="workspace-main fade-up">
            <div class="panel-head">
              <div>
                <div class="panel-kicker">Payment Stream</div>
                <h3>当日收款流水</h3>
              </div>
              <a-button type="link" @click="go('/finance/payments/records')">全部流水</a-button>
            </div>
            <div class="method-strip">
              <button
                v-for="method in paymentMethodSummary"
                :key="method.method"
                class="method-pill"
                type="button"
                @click="query.method = method.method"
              >
                <span>{{ method.label }}</span>
                <strong>{{ money(method.amount) }}</strong>
              </button>
              <button v-if="query.method" class="method-pill method-pill--clear" type="button" @click="query.method = undefined">
                <span>清除筛选</span>
                <strong>全部方式</strong>
              </button>
            </div>
            <vxe-table border stripe show-overflow :loading="loading" :data="filteredPayments" height="560">
              <vxe-column field="billMonthlyId" title="账单ID" width="120" />
              <vxe-column field="amount" title="金额" width="110">
                <template #default="{ row }">{{ money(row.amount) }}</template>
              </vxe-column>
              <vxe-column field="payMethod" title="方式" width="120">
                <template #default="{ row }">{{ payMethodLabel(row.payMethod) }}</template>
              </vxe-column>
              <vxe-column field="operatorStaffName" title="操作人" width="120" />
              <vxe-column field="paidAt" title="收款时间" width="180" />
              <vxe-column field="externalTxnId" title="外部流水号" min-width="180" />
              <vxe-column field="remark" title="备注" min-width="220" />
              <vxe-column title="操作" width="130" fixed="right">
                <template #default="{ row }">
                  <a-button type="link" @click="go(`/finance/bill/${row.billMonthlyId}`)">查看账单</a-button>
                </template>
              </vxe-column>
            </vxe-table>
          </article>

          <aside class="workspace-side">
            <article class="side-panel fade-up">
              <div class="panel-head">
                <div>
                  <div class="panel-kicker">Invoice Queue</div>
                  <h3>票据待办</h3>
                </div>
                <a-button type="link" @click="exportPendingInvoices">导出</a-button>
              </div>
              <a-alert
                show-icon
                :type="pendingInvoices.length ? 'warning' : 'success'"
                :message="pendingInvoices.length ? `还有 ${pendingInvoices.length} 笔收款未补齐票据` : '票据关联已完整'"
                :description="pendingInvoices.length ? '建议交班前补齐收据号或发票关联，避免对账继续挂起。' : '当前查询日期未发现票据缺口。'"
              />
              <div class="side-list">
                <button
                  v-for="item in pendingInvoices.slice(0, 6)"
                  :key="`${item.billId}-${item.paidAt}`"
                  class="side-row"
                  type="button"
                  @click="go('/finance/fees/payment-and-invoice?from=cashier_desk')"
                >
                  <div>
                    <strong>{{ item.elderName || '未绑定长者' }} · {{ money(item.amount) }}</strong>
                    <span>{{ item.remark || '未写备注' }}</span>
                  </div>
                  <a-tag color="orange">去补齐</a-tag>
                </button>
              </div>
            </article>

            <article class="side-panel fade-up">
              <div class="panel-head">
                <div>
                  <div class="panel-kicker">Adjustments</div>
                  <h3>退款与修正</h3>
                </div>
                <a-button type="link" @click="go('/finance/payments/refund-reversal?from=cashier_desk')">全部修正</a-button>
              </div>
              <div class="side-list">
                <button
                  v-for="item in todayAdjustments.slice(0, 6)"
                  :key="`${item.type}-${item.billId}-${item.occurredAt}`"
                  class="side-row"
                  type="button"
                  @click="go('/finance/payments/refund-reversal?from=cashier_desk')"
                >
                  <div>
                    <strong>{{ item.typeLabel }} · {{ money(item.amount) }}</strong>
                    <span>{{ item.detail || item.remark || '待复核' }}</span>
                  </div>
                  <a-tag :color="item.approvalStatus === 'APPROVED' ? 'green' : item.approvalStatus === 'PENDING' ? 'gold' : 'red'">
                    {{ item.approvalStatusLabel || '待处理' }}
                  </a-tag>
                </button>
                <div v-if="!todayAdjustments.length" class="side-empty">今天没有新增修正记录。</div>
              </div>
            </article>

            <article class="side-panel fade-up">
              <div class="panel-head">
                <div>
                  <div class="panel-kicker">Shift Checklist</div>
                  <h3>交班检查单</h3>
                </div>
              </div>
              <div class="checklist">
                <div v-for="item in closingChecklist" :key="item.label" class="check-row">
                  <div>
                    <strong>{{ item.label }}</strong>
                    <span>{{ item.hint }}</span>
                  </div>
                  <a-tag :color="item.done ? 'green' : 'orange'">{{ item.done ? '完成' : '待处理' }}</a-tag>
                </div>
              </div>
            </article>
          </aside>
        </section>
      </StatefulBlock>
    </div>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import type { Dayjs } from 'dayjs'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
import { exportFinanceInvoiceReceiptCsv, getFinanceInvoiceReceiptPage, getFinancePaymentAdjustmentPage, getPaymentRecordPage } from '../../api/finance'
import type { FinanceInvoiceReceiptItem, FinancePaymentAdjustmentItem, PageResult, PaymentRecordItem } from '../../types'
import { printTableReport } from '../../utils/print'

const router = useRouter()
const loading = ref(false)
const errorMessage = ref('')
const payments = ref<PaymentRecordItem[]>([])
const invoices = ref<FinanceInvoiceReceiptItem[]>([])
const adjustments = ref<FinancePaymentAdjustmentItem[]>([])
const query = reactive({
  date: dayjs(),
  method: undefined as string | undefined
})

const filteredPayments = computed(() => (
  query.method ? payments.value.filter(item => item.payMethod === query.method) : payments.value
))
const totalAmount = computed(() => filteredPayments.value.reduce((sum, item) => sum + Number(item.amount || 0), 0))
const pendingInvoices = computed(() => invoices.value.filter(item => item.invoiceStatus === 'UNLINKED'))
const pendingInvoiceAmount = computed(() => pendingInvoices.value.reduce((sum, item) => sum + Number(item.amount || 0), 0))
const todayAdjustments = computed(() => adjustments.value.filter(item => sameDate(item.occurredAt, query.date)))
const adjustmentAmount = computed(() => todayAdjustments.value.reduce((sum, item) => sum + Number(item.amount || 0), 0))
const closingStatus = computed(() => pendingInvoices.value.length || todayAdjustments.value.length ? '待收口' : '可交班')
const closingHint = computed(() => pendingInvoices.value.length || todayAdjustments.value.length ? '票据或修正仍需处理' : '当前日期的收银动作已基本闭环')
const methodOptions = computed(() => paymentMethodSummary.value.map(item => ({ value: item.method, label: item.label })))
const paymentMethodSummary = computed(() => {
  const bucket = new Map<string, number>()
  payments.value.forEach(item => {
    const key = String(item.payMethod || 'UNKNOWN')
    bucket.set(key, Number(bucket.get(key) || 0) + Number(item.amount || 0))
  })
  return Array.from(bucket.entries()).map(([method, amount]) => ({
    method,
    label: payMethodLabel(method),
    amount
  }))
})
const datePresets = [
  { key: 'today', label: '今天' },
  { key: 'yesterday', label: '昨天' },
  { key: 'monthStart', label: '月初' }
] as const

const processSteps = computed(() => [
  {
    key: 'collect',
    index: '01',
    title: '登记收款',
    hint: filteredPayments.value.length ? `已加载 ${filteredPayments.value.length} 笔流水供核对` : '当前筛选条件下暂无流水',
    status: filteredPayments.value.length ? '进行中' : '待开始',
    done: filteredPayments.value.length > 0
  },
  {
    key: 'invoice',
    index: '02',
    title: '补齐票据',
    hint: pendingInvoices.value.length ? `还有 ${pendingInvoices.value.length} 笔待补票据` : '收据和发票关联已完整',
    status: pendingInvoices.value.length ? '待处理' : '完成',
    done: pendingInvoices.value.length === 0
  },
  {
    key: 'adjust',
    index: '03',
    title: '复核修正',
    hint: todayAdjustments.value.length ? `今天有 ${todayAdjustments.value.length} 笔修正待复核` : '今日无新增修正',
    status: todayAdjustments.value.length ? '待复核' : '完成',
    done: todayAdjustments.value.length === 0
  },
  {
    key: 'close',
    index: '04',
    title: '打印交班',
    hint: closingHint.value,
    status: closingStatus.value,
    done: pendingInvoices.value.length === 0 && todayAdjustments.value.length === 0
  }
])

const closingChecklist = computed(() => [
  {
    label: '票据关联',
    hint: pendingInvoices.value.length ? `仍有 ${pendingInvoices.value.length} 笔待补票据` : '当前日期未发现票据缺口',
    done: pendingInvoices.value.length === 0
  },
  {
    label: '退款/冲正复核',
    hint: todayAdjustments.value.length ? `仍有 ${todayAdjustments.value.length} 笔修正待确认` : '当日无新增修正记录',
    done: todayAdjustments.value.length === 0
  },
  {
    label: '收款流水检查',
    hint: filteredPayments.value.length ? `已加载 ${filteredPayments.value.length} 笔流水供交班核对` : '当前筛选条件下暂无流水',
    done: filteredPayments.value.length > 0
  }
])

function go(path: string) {
  router.push(path)
}

function money(value?: number) {
  return `¥${Number(value || 0).toFixed(2)}`
}

function payMethodLabel(method?: string) {
  const map: Record<string, string> = {
    CASH: '现金',
    CARD: '刷卡',
    BANK: '转账',
    WECHAT: '微信',
    WECHAT_OFFLINE: '微信线下',
    ALIPAY: '支付宝',
    QR_CODE: '扫码'
  }
  return map[String(method || '')] || String(method || '其他')
}

function sameDate(value: string | undefined, target: Dayjs) {
  return value ? dayjs(value).isSame(target, 'day') : false
}

function applyDatePreset(preset: typeof datePresets[number]['key']) {
  if (preset === 'yesterday') {
    query.date = dayjs().subtract(1, 'day')
  } else if (preset === 'monthStart') {
    query.date = dayjs().startOf('month')
  } else {
    query.date = dayjs()
  }
  loadData()
}

async function loadData() {
  loading.value = true
  errorMessage.value = ''
  try {
    const dateText = dayjs(query.date).format('YYYY-MM-DD')
    const monthText = dayjs(query.date).format('YYYY-MM')
    const [paymentPage, invoicePage, adjustmentPage] = await Promise.all([
      getPaymentRecordPage({
        pageNo: 1,
        pageSize: 200,
        from: dateText,
        to: dateText
      }),
      getFinanceInvoiceReceiptPage({
        pageNo: 1,
        pageSize: 200,
        date: dateText
      }),
      getFinancePaymentAdjustmentPage({
        pageNo: 1,
        pageSize: 200,
        month: monthText
      })
    ])
    payments.value = (paymentPage as PageResult<PaymentRecordItem>).list || []
    invoices.value = (invoicePage as PageResult<FinanceInvoiceReceiptItem>).list || []
    adjustments.value = (adjustmentPage as PageResult<FinancePaymentAdjustmentItem>).list || []
  } catch (error: any) {
    errorMessage.value = error?.message || '加载收银台失败'
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

function reset() {
  query.date = dayjs()
  query.method = undefined
  loadData()
}

async function exportPendingInvoices() {
  try {
    await exportFinanceInvoiceReceiptCsv({
      date: dayjs(query.date).format('YYYY-MM-DD'),
      method: query.method,
      invoiceStatus: 'UNLINKED'
    })
    message.success('票据待办已导出')
  } catch (error: any) {
    message.error(error?.message || '导出失败')
  }
}

function printShiftSummary() {
  printTableReport({
    title: '收银交班单',
    subtitle: `业务日期：${dayjs(query.date).format('YYYY-MM-DD')}；收款金额：${money(totalAmount.value)}；待补票据：${pendingInvoices.value.length}；修正记录：${todayAdjustments.value.length}`,
    columns: [
      { key: 'billMonthlyId', title: '账单ID' },
      { key: 'amount', title: '金额' },
      { key: 'payMethod', title: '方式' },
      { key: 'paidAt', title: '收款时间' },
      { key: 'operatorStaffName', title: '操作人' },
      { key: 'remark', title: '备注' }
    ],
    rows: filteredPayments.value.map(item => ({
      billMonthlyId: item.billMonthlyId,
      amount: item.amount,
      payMethod: payMethodLabel(item.payMethod),
      paidAt: item.paidAt,
      operatorStaffName: item.operatorStaffName || '-',
      remark: item.remark || ''
    })),
    signatures: ['收银员', '复核人']
  })
}

onMounted(loadData)
</script>

<style scoped>
.cashier-shell {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.fade-up {
  animation: fadeUp 0.42s ease both;
}

.cashier-top {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(320px, 1fr);
  gap: 20px;
  padding: 24px;
  border-radius: 24px;
  background:
    radial-gradient(circle at left top, rgba(245, 158, 11, 0.22), transparent 30%),
    linear-gradient(135deg, #0f172a 0%, #12263f 46%, #173251 100%);
  color: #f8fbff;
}

.cashier-kicker,
.panel-kicker {
  font-size: 11px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: rgba(227, 238, 249, 0.64);
}

.cashier-top h2 {
  margin: 10px 0 12px;
  font-size: 34px;
  line-height: 1.12;
  color: #fff;
}

.cashier-top p {
  margin: 0;
  max-width: 560px;
  color: rgba(227, 238, 249, 0.74);
  line-height: 1.7;
}

.cashier-top__controls {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 18px;
  border-radius: 20px;
  background: rgba(248, 251, 255, 0.08);
  backdrop-filter: blur(8px);
}

.cashier-top__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.preset-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.preset-chip {
  padding: 8px 12px;
  border-radius: 999px;
  border: 1px solid rgba(219, 231, 242, 0.24);
  background: rgba(248, 251, 255, 0.1);
  color: #eef5fb;
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease, background 0.2s ease;
}

.preset-chip:hover {
  transform: translateY(-1px);
  border-color: rgba(255, 255, 255, 0.4);
  background: rgba(248, 251, 255, 0.16);
}

.preset-chip--ghost {
  color: rgba(238, 245, 251, 0.74);
}

.step-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.step-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff 0%, #f7fafc 100%);
  border: 1px solid #dbe7f2;
}

.step-card__index {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #eaf2fb;
  color: #1d4ed8;
  font-weight: 700;
}

.step-card__copy {
  min-width: 0;
  flex: 1;
}

.step-card__copy strong,
.check-row strong,
.side-row strong {
  display: block;
  color: #13263b;
}

.step-card__copy span,
.check-row span,
.side-row span {
  display: block;
  margin-top: 4px;
  color: #66788c;
  line-height: 1.6;
}

.summary-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.summary-tile {
  min-height: 132px;
  padding: 18px;
  border-radius: 18px;
  color: #fff;
}

.summary-tile span {
  display: block;
  font-size: 12px;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  opacity: 0.82;
}

.summary-tile strong {
  display: block;
  margin-top: 14px;
  font-size: 32px;
  line-height: 1.1;
}

.summary-tile small {
  display: block;
  margin-top: 12px;
  opacity: 0.82;
}

.summary-tile--ink {
  background: linear-gradient(135deg, #111827, #1f2937);
}

.summary-tile--amber {
  background: linear-gradient(135deg, #9a6700, #f59e0b);
}

.summary-tile--rose {
  background: linear-gradient(135deg, #7f1d3f, #e11d48);
}

.summary-tile--green {
  background: linear-gradient(135deg, #14532d, #15803d);
}

.workspace-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(320px, 0.82fr);
  gap: 16px;
}

.workspace-main,
.side-panel {
  padding: 22px;
  border-radius: 22px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  border: 1px solid #dbe7f2;
  box-shadow: 0 18px 44px rgba(15, 23, 42, 0.05);
}

.workspace-side {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.panel-head h3 {
  margin: 8px 0 0;
  font-size: 24px;
  line-height: 1.2;
  color: #13263b;
}

.method-strip {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 10px;
  margin: 16px 0;
}

.method-pill {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #dbe7f2;
  border-radius: 14px;
  background: linear-gradient(180deg, #ffffff 0%, #f7fafc 100%);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.method-pill strong {
  color: #13263b;
}

.method-pill:hover,
.side-row:hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 34px rgba(15, 23, 42, 0.06);
}

.method-pill--clear {
  border-style: dashed;
  color: #64748b;
}

.side-list,
.checklist {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 14px;
}

.side-row,
.check-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 16px;
  border: 1px solid #e3ecf5;
  background: rgba(255, 255, 255, 0.85);
}

.side-row {
  width: 100%;
  text-align: left;
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}

.side-empty {
  padding: 14px 0 2px;
  color: #66788c;
}

@keyframes fadeUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 1200px) {
  .cashier-top,
  .workspace-grid {
    grid-template-columns: 1fr;
  }

  .step-strip,
  .summary-strip {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .cashier-top,
  .workspace-main,
  .side-panel {
    padding: 18px;
  }

  .cashier-top h2 {
    font-size: 28px;
  }

  .step-strip,
  .summary-strip {
    grid-template-columns: 1fr;
  }

  .cashier-top__actions,
  .panel-head {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
