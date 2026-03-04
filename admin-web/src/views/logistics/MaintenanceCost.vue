<template>
  <PageContainer title="维修成本记录" subTitle="维修工单成本汇总（实付优先，缺失时使用估算）">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="状态">
        <a-select v-model:value="query.status" :options="statusOptions" allow-clear style="width: 160px" />
      </a-form-item>
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="房间/类型/报修人" allow-clear />
      </a-form-item>
    </SearchForm>

    <a-card class="card-elevated" :bordered="false" style="margin-bottom: 12px">
      <a-row :gutter="12">
        <a-col :span="6"><a-statistic title="工单数" :value="summary.totalCount" /></a-col>
        <a-col :span="6"><a-statistic title="高优先级" :value="summary.highPriorityCount" /></a-col>
        <a-col :span="6"><a-statistic title="超时工单" :value="summary.overdueCount" /></a-col>
        <a-col :span="6"><a-statistic title="成本合计" :value="summary.costAmount" suffix="元" /></a-col>
      </a-row>
    </a-card>

    <DataTable rowKey="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'costAmount'">
          {{ resolveCost(record).toFixed(2) }} 元
        </template>
      </template>
    </DataTable>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { getMaintenancePage } from '../../api/life'
import type { MaintenanceRequest, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<MaintenanceRequest[]>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const summary = reactive({ totalCount: 0, highPriorityCount: 0, overdueCount: 0, costAmount: 0 })

const query = reactive({
  status: undefined as string | undefined,
  keyword: '',
  pageNo: 1,
  pageSize: 10
})

const statusOptions = [
  { label: '待处理', value: 'OPEN' },
  { label: '处理中', value: 'PROCESSING' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' }
]

const columns = [
  { title: '房间号', dataIndex: 'roomNo', key: 'roomNo', width: 110 },
  { title: '故障类型', dataIndex: 'issueType', key: 'issueType', width: 150 },
  { title: '优先级', dataIndex: 'priority', key: 'priority', width: 90 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 110 },
  { title: '报修时间', dataIndex: 'reportedAt', key: 'reportedAt', width: 170 },
  { title: '完成时间', dataIndex: 'completedAt', key: 'completedAt', width: 170 },
  { title: '成本(元)', key: 'costAmount', width: 120 }
]

function estimateCost(record: MaintenanceRequest) {
  if (record.priority === 'HIGH') return 500
  if (record.priority === 'LOW') return 120
  return 280
}

function normalizeAmount(value?: number | null) {
  const amount = Number(value || 0)
  if (!Number.isFinite(amount) || amount < 0) return 0
  return amount
}

function resolveCost(record: MaintenanceRequest) {
  const totalCost = normalizeAmount(record.totalCost)
  if (totalCost > 0) return totalCost
  const splitCost = normalizeAmount(record.laborCost) + normalizeAmount(record.materialCost)
  if (splitCost > 0) return splitCost
  return estimateCost(record)
}

function calcSummary() {
  summary.totalCount = rows.value.length
  summary.highPriorityCount = rows.value.filter((item) => item.priority === 'HIGH').length
  summary.overdueCount = rows.value.filter((item) => {
    if (!item.reportedAt) return false
    if (item.status === 'COMPLETED' || item.status === 'CANCELLED') return false
    return dayjs().diff(dayjs(item.reportedAt), 'day') >= 2
  }).length
  summary.costAmount = Number(rows.value.reduce((sum, item) => sum + resolveCost(item), 0).toFixed(2))
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<MaintenanceRequest> = await getMaintenancePage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      status: query.status || undefined,
      keyword: query.keyword || undefined
    })
    rows.value = res.list || []
    pagination.total = res.total || 0
    calcSummary()
  } finally {
    loading.value = false
  }
}

function handleTableChange(pag: any) {
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

function onReset() {
  query.status = undefined
  query.keyword = ''
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

onMounted(fetchData)
</script>
