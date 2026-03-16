<template>
  <PageContainer title="收款流水" subTitle="按时间与账单查看实际收款记录">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline" :model="query" class="search-form">
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
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchData">查询</a-button>
            <a-button @click="reset">重置</a-button>
            <a-button @click="exportCurrent">导出CSV</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-row :gutter="[12, 12]" style="margin-top: 16px;">
      <a-col :xs="24" :xl="8"><a-card class="card-elevated" :bordered="false"><a-statistic title="收款笔数" :value="rows.length" /></a-card></a-col>
      <a-col :xs="24" :xl="8"><a-card class="card-elevated" :bordered="false"><a-statistic title="当前页金额" :value="pageAmount" suffix="元" :precision="2" /></a-card></a-col>
      <a-col :xs="24" :xl="8"><a-card class="card-elevated" :bordered="false"><a-statistic title="记录总数" :value="total" /></a-card></a-col>
    </a-row>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
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

      <a-pagination
        style="margin-top: 16px; text-align: right;"
        :current="query.pageNo"
        :page-size="query.pageSize"
        :total="total"
        show-size-changer
        @change="onPageChange"
        @showSizeChange="onPageSizeChange"
      />
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
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

onMounted(fetchData)
</script>
