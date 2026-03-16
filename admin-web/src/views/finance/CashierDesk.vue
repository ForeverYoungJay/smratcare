<template>
  <PageContainer title="收银台" subTitle="收费登记、票据处理、退款修正与日结动作放在同一页">
    <div class="desk-shell">
      <a-card class="desk-hero card-elevated" :bordered="false">
        <div class="desk-hero__copy">
          <div class="desk-kicker">Cashier Desk</div>
          <h2>前台收费流程在一个操作面里闭环。</h2>
          <p>适合一线财务按业务日期处理收款、票据待办和当日修正，减少菜单来回切换。</p>
        </div>
        <div class="desk-hero__panel">
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
                <a-button type="primary" @click="loadData">刷新看板</a-button>
                <a-button @click="reset">重置</a-button>
              </a-space>
            </a-form-item>
          </a-form>
          <div class="desk-actions">
            <a-button type="primary" @click="go('/finance/payments/register?from=cashier_desk')">收费登记</a-button>
            <a-button @click="go('/finance/fees/payment-and-invoice?from=cashier_desk')">开票/收据</a-button>
            <a-button @click="go('/finance/payments/refund-reversal?from=cashier_desk')">退款/冲正</a-button>
            <a-button @click="go('/finance/reconcile/month-close?from=cashier_desk')">月结进度</a-button>
            <a-button @click="exportPendingInvoices">导出票据待办</a-button>
            <a-button @click="printShiftSummary">打印交班单</a-button>
          </div>
        </div>
      </a-card>

      <StatefulBlock style="margin-top: 16px;" :loading="loading" :error="errorMessage" @retry="loadData">
        <a-row :gutter="[14, 14]">
          <a-col :xs="24" :xl="6">
            <a-card class="metric-card metric-card--ink" :bordered="false">
              <div class="metric-card__label">当日收款</div>
              <div class="metric-card__value">{{ money(totalAmount) }}</div>
              <div class="metric-card__hint">{{ filteredPayments.length }} 笔实际入账</div>
            </a-card>
          </a-col>
          <a-col :xs="24" :xl="6">
            <a-card class="metric-card metric-card--amber" :bordered="false">
              <div class="metric-card__label">待补票据</div>
              <div class="metric-card__value">{{ pendingInvoices.length }}</div>
              <div class="metric-card__hint">{{ money(pendingInvoiceAmount) }} 待补关联</div>
            </a-card>
          </a-col>
          <a-col :xs="24" :xl="6">
            <a-card class="metric-card metric-card--red" :bordered="false">
              <div class="metric-card__label">当日修正</div>
              <div class="metric-card__value">{{ todayAdjustments.length }}</div>
              <div class="metric-card__hint">{{ money(adjustmentAmount) }} 需复核</div>
            </a-card>
          </a-col>
          <a-col :xs="24" :xl="6">
            <a-card class="metric-card metric-card--green" :bordered="false">
              <div class="metric-card__label">日结状态</div>
              <div class="metric-card__value">{{ closingStatus }}</div>
              <div class="metric-card__hint">{{ closingHint }}</div>
            </a-card>
          </a-col>
        </a-row>

        <a-row :gutter="[16, 16]" style="margin-top: 16px;">
          <a-col :xs="24" :xl="16">
            <a-card class="card-elevated" :bordered="false" title="当日收款流水">
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
              </div>
              <vxe-table border stripe show-overflow :loading="loading" :data="filteredPayments" height="520">
                <vxe-column field="billMonthlyId" title="账单ID" width="120" />
                <vxe-column field="amount" title="金额" width="110">
                  <template #default="{ row }">{{ money(row.amount) }}</template>
                </vxe-column>
                <vxe-column field="payMethod" title="方式" width="120" />
                <vxe-column field="operatorStaffName" title="操作人" width="120" />
                <vxe-column field="paidAt" title="收款时间" width="180" />
                <vxe-column field="externalTxnId" title="外部流水号" min-width="180" />
                <vxe-column field="remark" title="备注" min-width="220" />
                <vxe-column title="操作" width="140" fixed="right">
                  <template #default="{ row }">
                    <a-button type="link" @click="go(`/finance/bill/${row.billMonthlyId}`)">查看账单</a-button>
                  </template>
                </vxe-column>
              </vxe-table>
            </a-card>
          </a-col>

          <a-col :xs="24" :xl="8">
            <a-card class="card-elevated side-card" :bordered="false" title="票据待办">
              <a-alert
                show-icon
                type="warning"
                :message="pendingInvoices.length ? `还有 ${pendingInvoices.length} 笔收款未补齐票据关联` : '票据关联完整'"
                :description="pendingInvoices.length ? '建议在交班前补齐收据号或发票关联，避免次日对账继续挂起。' : '当前查询日期未发现票据缺口。'"
              />
              <a-list class="side-list" :data-source="pendingInvoices.slice(0, 8)" item-layout="horizontal">
                <template #renderItem="{ item }">
                  <a-list-item>
                    <a-list-item-meta :title="`${item.elderName || '未绑定长者'} · ${money(item.amount)}`" :description="item.remark || '未写备注'" />
                    <template #actions>
                      <a-button type="link" @click="go('/finance/fees/payment-and-invoice?from=cashier_desk')">补票据</a-button>
                    </template>
                  </a-list-item>
                </template>
              </a-list>
            </a-card>

            <a-card class="card-elevated side-card" :bordered="false" title="收款修正" style="margin-top: 16px;">
              <a-list class="side-list" :data-source="todayAdjustments.slice(0, 8)" item-layout="horizontal">
                <template #renderItem="{ item }">
                  <a-list-item>
                    <a-list-item-meta :title="`${item.typeLabel} · ${money(item.amount)}`" :description="item.detail || item.remark || '待复核'" />
                    <template #actions>
                      <a-button type="link" @click="go('/finance/payments/refund-reversal?from=cashier_desk')">去复核</a-button>
                    </template>
                  </a-list-item>
                </template>
              </a-list>
            </a-card>

            <a-card class="card-elevated side-card" :bordered="false" title="交班检查单" style="margin-top: 16px;">
              <div v-for="item in closingChecklist" :key="item.label" class="check-row">
                <div>
                  <div class="check-row__label">{{ item.label }}</div>
                  <div class="check-row__hint">{{ item.hint }}</div>
                </div>
                <a-tag :color="item.done ? 'green' : 'orange'">{{ item.done ? '完成' : '待处理' }}</a-tag>
              </div>
            </a-card>
          </a-col>
        </a-row>
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
const closingHint = computed(() => pendingInvoices.value.length || todayAdjustments.value.length ? '票据或修正事项仍需处理' : '当日收银动作已基本闭环')
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
.desk-shell {
  --desk-ink: #14213d;
  --desk-sand: #f4efe6;
  --desk-amber: #f7b538;
  --desk-red: #d64933;
  --desk-green: #285943;
}

.desk-hero {
  display: grid;
  gap: 20px;
  grid-template-columns: 1.3fr 1fr;
  background:
    radial-gradient(circle at top left, rgba(247, 181, 56, 0.28), transparent 38%),
    linear-gradient(140deg, #0f172a 0%, #14213d 52%, #1d3557 100%);
  color: #fff;
  overflow: hidden;
}

.desk-kicker {
  display: inline-flex;
  padding: 4px 10px;
  border: 1px solid rgba(255, 255, 255, 0.24);
  border-radius: 999px;
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.desk-hero h2 {
  margin: 14px 0 10px;
  font-size: 28px;
  line-height: 1.2;
}

.desk-hero p {
  margin: 0;
  max-width: 560px;
  color: rgba(255, 255, 255, 0.76);
}

.desk-hero__panel {
  padding: 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(8px);
}

.desk-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 14px;
}

.metric-card {
  min-height: 138px;
  color: #fff;
  border-radius: 18px;
}

.metric-card__label {
  font-size: 13px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  opacity: 0.8;
}

.metric-card__value {
  margin-top: 14px;
  font-size: 32px;
  font-weight: 700;
}

.metric-card__hint {
  margin-top: 12px;
  opacity: 0.8;
}

.metric-card--ink {
  background: linear-gradient(135deg, #111827, #1f2937);
}

.metric-card--amber {
  background: linear-gradient(135deg, #a16207, #f59e0b);
}

.metric-card--red {
  background: linear-gradient(135deg, #7f1d1d, #dc2626);
}

.metric-card--green {
  background: linear-gradient(135deg, #14532d, #15803d);
}

.method-strip {
  display: grid;
  gap: 10px;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  margin-bottom: 14px;
}

.method-pill {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #dbe4f0;
  border-radius: 14px;
  background: linear-gradient(180deg, #ffffff, #f8fafc);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.method-pill:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 24px rgba(20, 33, 61, 0.08);
}

.side-card {
  border-radius: 18px;
}

.side-list {
  margin-top: 14px;
}

.check-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px dashed #e5e7eb;
}

.check-row:last-child {
  border-bottom: none;
}

.check-row__label {
  font-weight: 600;
  color: #0f172a;
}

.check-row__hint {
  margin-top: 4px;
  color: #64748b;
  font-size: 13px;
}

@media (max-width: 1100px) {
  .desk-hero {
    grid-template-columns: 1fr;
  }
}
</style>
