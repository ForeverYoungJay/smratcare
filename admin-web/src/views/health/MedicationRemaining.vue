<template>
  <PageContainer title="剩余用药" subTitle="按老人和药品统计剩余数量与预警">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="老人/药品" allow-clear />
      </a-form-item>
    </SearchForm>

    <DataTable rowKey="drugName" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'lowStock'">
          <a-tag :color="record.lowStock ? 'red' : 'green'">{{ record.lowStock ? '预警' : '正常' }}</a-tag>
        </template>
      </template>
    </DataTable>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { getHealthMedicationRemainingPage } from '../../api/health'
import type { HealthMedicationRemainingItem, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<HealthMedicationRemainingItem[]>([])
const query = reactive({ keyword: '', pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '药品', dataIndex: 'drugName', key: 'drugName', width: 160 },
  { title: '缴存总量', dataIndex: 'depositQty', key: 'depositQty', width: 110 },
  { title: '已用总量', dataIndex: 'usedQty', key: 'usedQty', width: 110 },
  { title: '剩余用量', dataIndex: 'remainQty', key: 'remainQty', width: 110 },
  { title: '最小阈值', dataIndex: 'minRemainQty', key: 'minRemainQty', width: 110 },
  { title: '状态', key: 'lowStock', width: 100 }
]

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<HealthMedicationRemainingItem> = await getHealthMedicationRemainingPage(query)
    rows.value = res.list
    pagination.total = res.total || res.list.length
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
  query.keyword = ''
  query.pageNo = 1
  query.pageSize = pagination.pageSize
  pagination.current = 1
  fetchData()
}

fetchData()
</script>
