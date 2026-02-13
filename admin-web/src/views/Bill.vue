<template>
  <PageContainer title="月账单" subTitle="账单查询与支付登记">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="月份">
        <a-date-picker v-model:value="query.month" value-format="YYYY-MM" picker="month" />
      </a-form-item>
      <a-form-item label="老人">
        <a-input v-model:value="query.keyword" placeholder="输入老人姓名" allow-clear />
      </a-form-item>
    </SearchForm>

    <DataTable
      rowKey="id"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a @click="openPay(record)">支付登记</a>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="payOpen" title="支付登记" @ok="submitPay">
      <a-form layout="vertical">
        <a-form-item label="金额" required>
          <a-input-number v-model:value="payForm.amount" style="width: 100%" />
        </a-form-item>
        <a-form-item label="方式" required>
          <a-select v-model:value="payForm.method" :options="methodOptions" />
        </a-form-item>
        <a-form-item label="支付时间">
          <a-date-picker v-model:value="payForm.paidAt" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="payForm.remark" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../components/PageContainer.vue'
import SearchForm from '../components/SearchForm.vue'
import DataTable from '../components/DataTable.vue'
import { getBillPage, payBill } from '../api/bill'
import type { BillItem, PageResult } from '../types/api'

const query = reactive({ month: undefined as string | undefined, keyword: undefined as string | undefined, pageNo: 1, pageSize: 10 })
const rows = ref<BillItem[]>([])
const loading = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '月份', dataIndex: 'billMonth', key: 'billMonth', width: 120 },
  { title: '应收', dataIndex: 'totalAmount', key: 'totalAmount', width: 100 },
  { title: '已付', dataIndex: 'paidAmount', key: 'paidAmount', width: 100 },
  { title: '未付', dataIndex: 'outstandingAmount', key: 'outstandingAmount', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 120 }
]

const payOpen = ref(false)
const current = ref<BillItem | null>(null)
const payForm = reactive({ amount: undefined as number | undefined, method: 'CASH', paidAt: undefined as string | undefined, remark: '' })
const methodOptions = [
  { label: '现金', value: 'CASH' },
  { label: '银行转账', value: 'BANK' },
  { label: '线下微信', value: 'WECHAT_OFFLINE' }
]

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<BillItem> = await getBillPage(query)
    rows.value = res.list
    pagination.total = res.total || res.list.length
  } catch {
    rows.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

function handleTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  fetchData()
}

function onReset() {
  query.month = undefined
  query.keyword = undefined
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openPay(record: BillItem) {
  current.value = record
  payForm.amount = record.outstandingAmount || record.totalAmount
  payForm.method = 'CASH'
  payForm.paidAt = undefined
  payForm.remark = ''
  payOpen.value = true
}

async function submitPay() {
  if (!current.value || !payForm.amount) {
    message.warning('请输入金额')
    return
  }
  try {
    await payBill(current.value.id, payForm)
    message.success('支付登记成功')
    payOpen.value = false
    fetchData()
  } catch {
    message.error('支付登记失败')
  }
}

function statusText(status?: number) {
  if (status === 2) return '已支付'
  if (status === 1) return '部分支付'
  return '未支付'
}

function statusColor(status?: number) {
  if (status === 2) return 'green'
  if (status === 1) return 'gold'
  return 'red'
}

fetchData()
</script>
