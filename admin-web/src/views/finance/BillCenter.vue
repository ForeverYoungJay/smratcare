<template>
  <PageContainer title="月账单中心" subTitle="按月查询、生成与收款登记">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline" :model="query" class="search-form">
        <a-form-item label="月份">
          <a-date-picker v-model:value="query.month" picker="month" allow-clear style="width: 160px" />
        </a-form-item>
        <a-form-item label="老人姓名">
          <a-input v-model:value="query.keyword" style="width: 160px" allow-clear />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchData">查询</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <div class="table-actions">
        <a-space>
          <a-button @click="exportCsvData">导出</a-button>
          <a-button type="primary" @click="openGenerate">生成账单</a-button>
        </a-space>
      </div>

      <vxe-toolbar custom export></vxe-toolbar>
      <vxe-table
        border
        stripe
        show-overflow
        height="520"
        :loading="loading"
        :data="rows"
        :column-config="{ resizable: true }"
      >
        <vxe-column field="billMonth" title="账单月份" width="120" />
        <vxe-column field="elderName" title="老人" min-width="140">
          <template #default="{ row }">
            <span>{{ row.elderName || '未知老人' }}</span>
          </template>
        </vxe-column>
        <vxe-column field="totalAmount" title="总额" width="120" />
        <vxe-column field="paidAmount" title="已付" width="120" />
        <vxe-column field="outstandingAmount" title="欠费" width="120">
          <template #default="{ row }">
            <a-tag :color="row.outstandingAmount > 0 ? 'red' : 'green'">{{ row.outstandingAmount ?? 0 }}</a-tag>
          </template>
        </vxe-column>
        <vxe-column field="status" title="状态" width="140">
          <template #default="{ row }">
            <a-tag :color="statusColor(row.status)">{{ statusText(row.status) }}</a-tag>
          </template>
        </vxe-column>
        <vxe-column title="操作" width="240" fixed="right">
          <template #default="{ row }">
            <a-space>
              <a-button type="link" @click="openDetail(row)">查看</a-button>
              <a-button type="link" @click="openPay(row)">登记收款</a-button>
            </a-space>
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

    <a-modal v-model:open="generateOpen" title="生成账单" @ok="submitGenerate" :confirm-loading="generating">
      <a-form layout="vertical" :model="generateForm">
        <a-form-item label="账单月份">
          <a-date-picker v-model:value="generateForm.month" picker="month" style="width: 100%" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="payOpen" title="登记收款" @ok="submitPay" :confirm-loading="paying">
      <a-form layout="vertical" :model="payForm" :rules="payRules" ref="payFormRef">
        <a-form-item label="金额" name="amount">
          <a-input-number v-model:value="payForm.amount" style="width: 100%" />
        </a-form-item>
        <a-form-item label="方式" name="method">
          <a-select v-model:value="payForm.method">
            <a-select-option value="CASH">现金</a-select-option>
            <a-select-option value="BANK">转账</a-select-option>
            <a-select-option value="WECHAT_OFFLINE">微信线下</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="收款时间" name="paidAt">
          <a-date-picker v-model:value="payForm.paidAt" show-time style="width: 100%" />
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="payForm.remark" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import { exportCsv } from '../../utils/export'
import { getBillPage, generateBill, payBill } from '../../api/bill'
import type { BillItem, PageResult } from '../../types'
import router from '../../router'

const loading = ref(false)
const rows = ref<BillItem[]>([])
const total = ref(0)

const query = reactive({
  month: undefined as any,
  keyword: '',
  pageNo: 1,
  pageSize: 10
})

const generateOpen = ref(false)
const generating = ref(false)
const generateForm = reactive({
  month: dayjs().startOf('month')
})

const payOpen = ref(false)
const paying = ref(false)
const payFormRef = ref()
type PayForm = {
  amount: number
  method: string
  paidAt: any
  remark: string
}

const payForm = reactive<PayForm>({
  amount: 0,
  method: 'CASH',
  paidAt: dayjs(),
  remark: ''
})
const payRules = {
  amount: [{ required: true, message: '请输入金额' }],
  method: [{ required: true, message: '请选择方式' }],
  paidAt: [{ required: true, message: '请选择时间' }]
}

const activeBillId = ref<number | null>(null)

function statusText(status?: number) {
  if (status === 2) return '已付'
  if (status === 1) return '部分已付'
  return '未付'
}

function statusColor(status?: number) {
  if (status === 2) return 'green'
  if (status === 1) return 'orange'
  return 'red'
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<BillItem> = await getBillPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      month: query.month ? dayjs(query.month).format('YYYY-MM') : undefined,
      keyword: query.keyword
    })
    rows.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function reset() {
  query.month = undefined
  query.keyword = ''
  query.pageNo = 1
  fetchData()
}

function onPageChange(page: number) {
  query.pageNo = page
  fetchData()
}

function onPageSizeChange(current: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
  fetchData()
}

function exportCsvData() {
  exportCsv(rows.value, `bill-${dayjs().format('YYYYMMDD')}.csv`)
}

function openGenerate() {
  generateOpen.value = true
}

async function submitGenerate() {
  if (!generateForm.month) {
    message.warning('请选择月份')
    return
  }
  generating.value = true
  try {
    await generateBill(dayjs(generateForm.month).format('YYYY-MM'))
    message.success('生成成功')
    generateOpen.value = false
    fetchData()
  } finally {
    generating.value = false
  }
}

function openPay(row: BillItem) {
  activeBillId.value = row.id
  payForm.amount = Number(row.outstandingAmount || 0)
  payForm.method = 'CASH'
  payForm.paidAt = dayjs()
  payForm.remark = ''
  payOpen.value = true
}

async function submitPay() {
  await payFormRef.value?.validate?.()
  if (!activeBillId.value) return
  paying.value = true
  try {
    await payBill(activeBillId.value, {
      amount: payForm.amount,
      method: payForm.method,
      paidAt: dayjs(payForm.paidAt).format('YYYY-MM-DD HH:mm:ss'),
      remark: payForm.remark
    })
    message.success('收款登记成功')
    payOpen.value = false
    fetchData()
  } finally {
    paying.value = false
  }
}

function openDetail(row: BillItem) {
  router.push(`/finance/bill/${row.id}`)
}

onMounted(fetchData)
</script>
