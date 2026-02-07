<template>
  <PageContainer title="订单管理" subTitle="订单与积分结算">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="订单号">
        <a-input v-model:value="query.keyword" placeholder="订单号/老人ID" />
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
        <template v-if="column.key === 'orderStatus'">
          <a-tag :color="statusColor(record.orderStatus)">{{ statusText(record.orderStatus) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a @click="openDetail(record)">详情</a>
        </template>
      </template>
    </DataTable>

    <a-drawer v-model:open="detailOpen" title="订单详情" width="420">
      <a-descriptions bordered :column="1" size="small">
        <a-descriptions-item label="订单号">{{ current?.orderNo || current?.id }}</a-descriptions-item>
        <a-descriptions-item label="老人ID">{{ current?.elderId }}</a-descriptions-item>
        <a-descriptions-item label="积分">{{ current?.pointsUsed ?? '-' }}</a-descriptions-item>
        <a-descriptions-item label="状态">{{ statusText(current?.orderStatus) }}</a-descriptions-item>
      </a-descriptions>
    </a-drawer>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import PageContainer from '../components/PageContainer.vue'
import SearchForm from '../components/SearchForm.vue'
import DataTable from '../components/DataTable.vue'
import { getOrderPage } from '../api/store'
import type { OrderItem, ApiResult, PageResult } from '../types/api'

const query = reactive({ keyword: undefined as string | undefined, pageNo: 1, pageSize: 10 })
const rows = ref<OrderItem[]>([])
const loading = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '订单号', dataIndex: 'orderNo', key: 'orderNo', width: 160 },
  { title: '老人ID', dataIndex: 'elderId', key: 'elderId', width: 100 },
  { title: '积分', dataIndex: 'pointsUsed', key: 'pointsUsed', width: 100 },
  { title: '状态', dataIndex: 'orderStatus', key: 'orderStatus', width: 100 },
  { title: '操作', key: 'action', width: 100 }
]

const detailOpen = ref(false)
const current = ref<OrderItem | null>(null)

async function fetchData() {
  loading.value = true
  try {
    const res: ApiResult<PageResult<OrderItem>> = await getOrderPage(query)
    if (res.code === 0) {
      let data = res.data.records || []
      if (query.keyword) {
        data = data.filter((d) =>
          String(d.orderNo || '').includes(query.keyword!) || String(d.elderId).includes(query.keyword!)
        )
      }
      rows.value = data
      pagination.total = res.data.total || data.length
    }
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
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openDetail(record: OrderItem) {
  current.value = record
  detailOpen.value = true
}

function statusText(status?: number) {
  if (status === 1) return '已支付'
  if (status === 2) return '已完成'
  if (status === 3) return '已取消'
  return '新建'
}

function statusColor(status?: number) {
  if (status === 1) return 'green'
  if (status === 2) return 'blue'
  if (status === 3) return 'default'
  return 'gold'
}

fetchData()
</script>
