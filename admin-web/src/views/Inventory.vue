<template>
  <PageContainer title="库存预警" subTitle="低库存与临期提醒">
    <SearchForm :model="query" @search="fetchData" @reset="fetchData">
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="商品名称/编码" />
      </a-form-item>
      <a-form-item label="类型">
        <a-select v-model:value="query.type" allow-clear style="width: 160px">
          <a-select-option value="LOW">低库存</a-select-option>
          <a-select-option value="EXPIRY">临期</a-select-option>
        </a-select>
      </a-form-item>
    </SearchForm>

    <DataTable
      rowKey="id"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
    />
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import PageContainer from '../components/PageContainer.vue'
import SearchForm from '../components/SearchForm.vue'
import DataTable from '../components/DataTable.vue'
import { getInventoryAlerts, getInventoryExpiryAlerts } from '../api/inventory'
import type { InventoryAlertItem } from '../types/api'

const query = reactive({
  keyword: undefined as string | undefined,
  type: undefined as 'LOW' | 'EXPIRY' | undefined,
  pageNo: 1,
  pageSize: 10
})

const loading = ref(false)
const rows = ref<InventoryAlertItem[]>([])
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true
})

const columns = [
  { title: '商品', dataIndex: 'productName', key: 'productName', width: 180 },
  { title: '编码', dataIndex: 'productCode', key: 'productCode', width: 120 },
  { title: '预警类型', dataIndex: 'alertType', key: 'alertType', width: 100 },
  { title: '当前库存', dataIndex: 'stock', key: 'stock', width: 100 },
  { title: '安全库存', dataIndex: 'safetyStock', key: 'safetyStock', width: 100 },
  { title: '备注', dataIndex: 'remark', key: 'remark' }
]

async function fetchData() {
  loading.value = true
  try {
    let data: InventoryAlertItem[] = []
    if (query.type === 'EXPIRY') {
      data = await getInventoryExpiryAlerts()
    } else {
      data = await getInventoryAlerts()
    }

    const keyword = query.keyword?.trim()
    if (keyword) {
      data = data.filter((d) =>
        String(d.productName || '').includes(keyword) || String(d.productCode || '').includes(keyword)
      )
    }

    pagination.total = data.length
    const start = (pagination.current - 1) * pagination.pageSize
    rows.value = data.slice(start, start + pagination.pageSize)
  } finally {
    loading.value = false
  }
}

function handleTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

onMounted(fetchData)
</script>
