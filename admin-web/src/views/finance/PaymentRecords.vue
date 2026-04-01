<template>
  <PageContainer title="收款流水" subTitle="按时间与账单查看实际收款记录">
    <template #stats>
      <div class="payment-overview-grid">
        <div class="overview-card">
          <span>收款笔数</span>
          <strong>{{ rows.length }}</strong>
          <small>当前页实际收款记录条数</small>
        </div>
        <div class="overview-card">
          <span>当前页金额</span>
          <strong>{{ pageAmount.toFixed(2) }}</strong>
          <small>便于财务核对当页流水</small>
        </div>
        <div class="overview-card">
          <span>记录总数</span>
          <strong>{{ total }}</strong>
          <small>支持跨账期持续追溯</small>
        </div>
      </div>
    </template>

    <SearchForm :model="query" @search="fetchData" @reset="reset">
      <a-form-item label="开始日期">
        <a-date-picker v-model:value="query.from" />
      </a-form-item>
      <a-form-item label="结束日期">
        <a-date-picker v-model:value="query.to" />
      </a-form-item>
      <a-form-item label="收款方式">
        <a-select v-model:value="query.method" allow-clear style="width: 160px" placeholder="全部方式">
          <a-select-option value="CASH">现金</a-select-option>
          <a-select-option value="CARD">刷卡</a-select-option>
          <a-select-option value="BANK">转账</a-select-option>
          <a-select-option value="WECHAT">微信</a-select-option>
          <a-select-option value="WECHAT_OFFLINE">微信线下</a-select-option>
          <a-select-option value="ALIPAY">支付宝</a-select-option>
          <a-select-option value="QR_CODE">扫码</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="账单ID">
        <a-input-number v-model:value="query.billId" :min="1" style="width: 160px" />
      </a-form-item>
    </SearchForm>

    <section class="surface-toolbar">
      <div class="surface-toolbar-title">
        <strong>收款流水工作台</strong>
        <span>按时间、方式与账单编号核对实际收款记录，支持快速回到对应账单继续排查。</span>
      </div>
      <a-space wrap>
        <a-tag color="processing">{{ dayjs(query.from).format('MM-DD') }} 至 {{ dayjs(query.to).format('MM-DD') }}</a-tag>
        <a-tag color="blue">方式 {{ methodLabel(query.method) }}</a-tag>
        <a-tag color="green">当页金额 {{ pageAmount.toFixed(2) }}</a-tag>
      </a-space>
    </section>

    <section class="card-elevated payment-workspace">
      <div class="table-actions">
        <div class="table-head">
          <div>
            <strong>收款记录列表</strong>
            <span>支持按账单回看收款动作、操作人和外部流水号，便于财务核账与追踪。</span>
          </div>
          <a-space wrap>
            <a-tag color="default">共 {{ total }} 条</a-tag>
            <a-tag color="purple">当前页 {{ rows.length }} 条</a-tag>
          </a-space>
        </div>
        <a-button @click="exportCurrent">导出CSV</a-button>
      </div>

      <div class="table-frame">
        <vxe-table border stripe show-overflow :loading="loading" :data="rows" height="520">
        <vxe-column field="billMonthlyId" title="账单ID" width="120" />
        <vxe-column field="amount" title="金额" width="120" />
        <vxe-column field="payMethod" title="方式" width="120" />
        <vxe-column field="paidAt" title="收款时间" width="180" />
        <vxe-column field="operatorStaffName" title="操作人" width="140" />
        <vxe-column field="externalTxnId" title="外部流水号" min-width="180" />
        <vxe-column field="remark" title="备注" min-width="220" />
        <vxe-column title="操作" width="140" fixed="right">
          <template #default="{ row }">
            <a-button type="link" @click="go(`/finance/bill/${row.billMonthlyId}`)">查看账单</a-button>
          </template>
        </vxe-column>
        </vxe-table>
      </div>

      <div class="pager-row">
        <a-pagination
          :current="query.pageNo"
          :page-size="query.pageSize"
          :total="total"
          show-size-changer
          @change="onPageChange"
          @showSizeChange="onPageSizeChange"
        />
      </div>
    </section>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import { getPaymentRecordPage } from '../../api/finance'
import type { PageResult, PaymentRecordItem } from '../../types'
import { exportCsv } from '../../utils/export'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const rows = ref<PaymentRecordItem[]>([])
const total = ref(0)
const pageAmount = computed(() => rows.value.reduce((sum, item) => sum + Number(item.amount || 0), 0))

const query = reactive({
  from: route.query.date === 'today' ? dayjs() : dayjs().startOf('month'),
  to: route.query.date === 'today' ? dayjs() : dayjs(),
  method: (typeof route.query.method === 'string' ? route.query.method : undefined) as string | undefined,
  billId: undefined as number | undefined,
  pageNo: 1,
  pageSize: 20
})

function go(path: string) {
  router.push(path)
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<PaymentRecordItem> = await getPaymentRecordPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      billId: query.billId,
      method: query.method,
      from: dayjs(query.from).format('YYYY-MM-DD'),
      to: dayjs(query.to).format('YYYY-MM-DD')
    })
    rows.value = res.list || []
    total.value = Number(res.total || 0)
  } finally {
    loading.value = false
  }
}

function reset() {
  query.from = dayjs().startOf('month')
  query.to = dayjs()
  query.method = undefined
  query.billId = undefined
  query.pageNo = 1
  fetchData()
}

function onPageChange(page: number) {
  query.pageNo = page
  fetchData()
}

function onPageSizeChange(_: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
  fetchData()
}

function exportCurrent() {
  exportCsv(
    rows.value.map(item => ({
      账单ID: item.billMonthlyId,
      金额: item.amount,
      收款方式: item.payMethod,
      收款时间: item.paidAt,
      操作人: item.operatorStaffName || '',
      外部流水号: item.externalTxnId || '',
      备注: item.remark || ''
    })),
    `收款流水-${dayjs().format('YYYYMMDD-HHmmss')}.csv`
  )
}

function methodLabel(value?: string) {
  if (!value) return '全部方式'
  if (value === 'CASH') return '现金'
  if (value === 'CARD') return '刷卡'
  if (value === 'BANK') return '转账'
  if (value === 'WECHAT') return '微信'
  if (value === 'WECHAT_OFFLINE') return '微信线下'
  if (value === 'ALIPAY') return '支付宝'
  if (value === 'QR_CODE') return '扫码'
  return value
}

onMounted(fetchData)
</script>

<style scoped>
.payment-overview-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.overview-card {
  display: grid;
  gap: 6px;
  padding: 14px 16px;
  border-radius: 16px;
  border: 1px solid #dce9f2;
  background: rgba(255, 255, 255, 0.86);
}

.overview-card span,
.table-head span {
  color: #6d8aa3;
  font-size: 12px;
}

.overview-card strong {
  color: #173854;
  font-size: 24px;
}

.overview-card small {
  color: #7a97b0;
}

.payment-workspace {
  padding: 16px;
}

.table-actions,
.table-head,
.pager-row {
  display: flex;
  align-items: center;
}

.table-actions {
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}

.table-head {
  justify-content: space-between;
  gap: 12px;
  flex: 1;
  flex-wrap: wrap;
}

.table-head strong {
  display: block;
  color: #173854;
  font-size: 16px;
}

.table-frame {
  border: 1px solid #e4edf4;
  border-radius: 18px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.95);
}

.pager-row {
  justify-content: flex-end;
  margin-top: 16px;
}

@media (max-width: 768px) {
  .payment-overview-grid {
    grid-template-columns: 1fr;
  }

  .table-actions,
  .table-head {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
