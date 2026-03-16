<template>
  <PageContainer title="日结/交班" subTitle="按业务日期确认收款、票据、修正和异常是否可以完成交班">
    <div class="shift-shell">
      <a-card class="shift-hero card-elevated" :bordered="false">
        <div>
          <div class="shift-hero__eyebrow">Shift Close</div>
          <h2>交班前只看今天该收口的四件事。</h2>
          <p>收款流水、票据关联、退款修正和对账异常缺一不可，避免第二天继续挂账。</p>
        </div>
        <a-form layout="inline" :model="query" class="search-form">
          <a-form-item label="业务日期">
            <a-date-picker v-model:value="query.date" />
          </a-form-item>
          <a-form-item>
            <a-space>
              <a-button type="primary" @click="loadData">刷新交班摘要</a-button>
              <a-button @click="runReconcile">重新生成对账</a-button>
              <a-button @click="go('/finance/payments/cashier-desk')">收银台</a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </a-card>

      <StatefulBlock style="margin-top: 16px;" :loading="loading" :error="errorMessage" @retry="loadData">
        <a-row :gutter="[14, 14]">
          <a-col :xs="24" :xl="6"><a-card class="card-elevated shift-stat shift-stat--ink" :bordered="false"><a-statistic title="收款笔数" :value="payments.length" /></a-card></a-col>
          <a-col :xs="24" :xl="6"><a-card class="card-elevated shift-stat shift-stat--amber" :bordered="false"><a-statistic title="待补票据" :value="pendingInvoices.length" /></a-card></a-col>
          <a-col :xs="24" :xl="6"><a-card class="card-elevated shift-stat shift-stat--red" :bordered="false"><a-statistic title="修正记录" :value="sameDayAdjustments.length" /></a-card></a-col>
          <a-col :xs="24" :xl="6"><a-card class="card-elevated shift-stat shift-stat--green" :bordered="false"><a-statistic title="对账异常" :value="exceptions.length" /></a-card></a-col>
        </a-row>

        <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
          <a-alert
            :type="canClose ? 'success' : 'warning'"
            show-icon
            :message="canClose ? '当前日期可以交班' : '当前日期仍有待收口事项'"
            :description="closeDescription"
          />
          <div class="close-checks">
            <div v-for="item in closeChecks" :key="item.label" class="close-checks__item">
              <div>
                <div class="close-checks__title">{{ item.label }}</div>
                <div class="close-checks__hint">{{ item.hint }}</div>
              </div>
              <a-tag :color="item.done ? 'green' : 'orange'">{{ item.done ? '已完成' : '待处理' }}</a-tag>
            </div>
          </div>
        </a-card>

        <a-row :gutter="[16, 16]" style="margin-top: 16px;">
          <a-col :xs="24" :xl="14">
            <a-card class="card-elevated" :bordered="false" title="收款流水">
              <vxe-table border stripe show-overflow :loading="loading" :data="payments" height="380">
                <vxe-column field="billMonthlyId" title="账单ID" width="120" />
                <vxe-column field="amount" title="金额" width="120" />
                <vxe-column field="payMethod" title="方式" width="120" />
                <vxe-column field="paidAt" title="收款时间" width="180" />
                <vxe-column field="operatorStaffName" title="操作人" width="120" />
                <vxe-column field="remark" title="备注" min-width="220" />
              </vxe-table>
            </a-card>
          </a-col>
          <a-col :xs="24" :xl="10">
            <a-card class="card-elevated" :bordered="false" title="待补票据与异常">
              <a-list class="issue-list" :data-source="issueRows" item-layout="horizontal">
                <template #renderItem="{ item }">
                  <a-list-item>
                    <a-list-item-meta :title="item.title" :description="item.description" />
                    <template #actions>
                      <a-button type="link" @click="go(item.path)">{{ item.action }}</a-button>
                    </template>
                  </a-list-item>
                </template>
              </a-list>
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
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
import {
  getFinanceInvoiceReceiptPage,
  getFinancePaymentAdjustmentPage,
  getFinanceReconcileExceptions,
  getPaymentRecordPage,
  reconcileDaily
} from '../../api/finance'
import type {
  FinanceInvoiceReceiptItem,
  FinancePaymentAdjustmentItem,
  FinanceReconcileExceptionItem,
  PageResult,
  PaymentRecordItem
} from '../../types'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const errorMessage = ref('')
const payments = ref<PaymentRecordItem[]>([])
const invoices = ref<FinanceInvoiceReceiptItem[]>([])
const adjustments = ref<FinancePaymentAdjustmentItem[]>([])
const exceptions = ref<FinanceReconcileExceptionItem[]>([])
const query = reactive({
  date: route.query.date === 'today' ? dayjs() : (typeof route.query.date === 'string' ? dayjs(route.query.date) : dayjs())
})

const pendingInvoices = computed(() => invoices.value.filter(item => item.invoiceStatus === 'UNLINKED'))
const sameDayAdjustments = computed(() => adjustments.value.filter(item => item.occurredAt && dayjs(item.occurredAt).isSame(query.date, 'day')))
const canClose = computed(() => pendingInvoices.value.length === 0 && sameDayAdjustments.value.length === 0 && exceptions.value.length === 0 && payments.value.length > 0)
const closeDescription = computed(() => canClose.value
  ? '票据、修正和异常均已收口，可以执行交班。'
  : `待补票据 ${pendingInvoices.value.length}；修正 ${sameDayAdjustments.value.length}；异常 ${exceptions.value.length}。`)
const closeChecks = computed(() => [
  { label: '收款流水已核对', hint: payments.value.length ? `已加载 ${payments.value.length} 笔当日流水` : '当前日期暂无收款记录', done: payments.value.length > 0 },
  { label: '票据关联完整', hint: pendingInvoices.value.length ? `仍有 ${pendingInvoices.value.length} 笔未补票据` : '票据关联已补齐', done: pendingInvoices.value.length === 0 },
  { label: '退款/冲正已复核', hint: sameDayAdjustments.value.length ? `仍有 ${sameDayAdjustments.value.length} 笔修正待处理` : '当日无待复核修正', done: sameDayAdjustments.value.length === 0 },
  { label: '对账异常已清零', hint: exceptions.value.length ? `仍有 ${exceptions.value.length} 条异常` : '未发现对账异常', done: exceptions.value.length === 0 }
])
const issueRows = computed(() => [
  ...pendingInvoices.value.slice(0, 4).map(item => ({
    title: `票据待补 · ${item.elderName || '未绑定长者'} · ${Number(item.amount || 0).toFixed(2)} 元`,
    description: item.remark || '收款未补齐发票/收据关联',
    path: '/finance/fees/payment-and-invoice?from=shift_close',
    action: '补票据'
  })),
  ...sameDayAdjustments.value.slice(0, 4).map(item => ({
    title: `${item.typeLabel} · ${Number(item.amount || 0).toFixed(2)} 元`,
    description: item.detail || item.remark || '收款修正待复核',
    path: '/finance/payments/refund-reversal?from=shift_close',
    action: '去复核'
  })),
  ...exceptions.value.slice(0, 4).map(item => ({
    title: item.exceptionTypeLabel || '对账异常',
    description: item.detail || item.suggestion || '待处理',
    path: '/finance/reconcile/issue-center?from=shift_close',
    action: '去处理'
  }))
])

function go(path: string) {
  router.push(path)
}

async function loadData() {
  loading.value = true
  errorMessage.value = ''
  try {
    const dateText = dayjs(query.date).format('YYYY-MM-DD')
    const monthText = dayjs(query.date).format('YYYY-MM')
    const [paymentPage, invoicePage, adjustmentPage, exceptionRows] = await Promise.all([
      getPaymentRecordPage({ pageNo: 1, pageSize: 200, from: dateText, to: dateText }),
      getFinanceInvoiceReceiptPage({ pageNo: 1, pageSize: 200, date: dateText }),
      getFinancePaymentAdjustmentPage({ pageNo: 1, pageSize: 200, month: monthText }),
      getFinanceReconcileExceptions({ date: dateText })
    ])
    payments.value = (paymentPage as PageResult<PaymentRecordItem>).list || []
    invoices.value = (invoicePage as PageResult<FinanceInvoiceReceiptItem>).list || []
    adjustments.value = (adjustmentPage as PageResult<FinancePaymentAdjustmentItem>).list || []
    exceptions.value = exceptionRows || []
  } catch (error: any) {
    errorMessage.value = error?.message || '加载交班摘要失败'
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

async function runReconcile() {
  try {
    await reconcileDaily({ date: dayjs(query.date).format('YYYY-MM-DD') })
    message.success('已重新生成对账结果')
    await loadData()
  } catch (error: any) {
    message.error(error?.message || '重新对账失败')
  }
}

onMounted(loadData)
</script>

<style scoped>
.shift-hero {
  display: grid;
  gap: 18px;
  grid-template-columns: 1.1fr 1fr;
  background:
    radial-gradient(circle at top right, rgba(250, 204, 21, 0.22), transparent 28%),
    linear-gradient(135deg, #111827 0%, #1d4ed8 46%, #0f766e 100%);
  color: #fff;
}

.shift-hero__eyebrow {
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.72);
}

.shift-hero h2 {
  margin: 10px 0;
  font-size: 28px;
  line-height: 1.18;
}

.shift-hero p {
  margin: 0;
  color: rgba(255, 255, 255, 0.76);
}

.shift-stat {
  border-radius: 18px;
}

.shift-stat--ink {
  background: linear-gradient(135deg, #111827, #1f2937);
  color: #fff;
}

.shift-stat--amber {
  background: linear-gradient(135deg, #854d0e, #f59e0b);
  color: #fff;
}

.shift-stat--red {
  background: linear-gradient(135deg, #7f1d1d, #dc2626);
  color: #fff;
}

.shift-stat--green {
  background: linear-gradient(135deg, #14532d, #16a34a);
  color: #fff;
}

.close-checks {
  display: grid;
  gap: 12px;
  margin-top: 14px;
}

.close-checks__item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px dashed #e2e8f0;
}

.close-checks__item:last-child {
  border-bottom: none;
}

.close-checks__title {
  font-weight: 700;
  color: #0f172a;
}

.close-checks__hint {
  margin-top: 4px;
  color: #64748b;
}

.issue-list {
  margin-top: 4px;
}

@media (max-width: 1100px) {
  .shift-hero {
    grid-template-columns: 1fr;
  }
}
</style>
