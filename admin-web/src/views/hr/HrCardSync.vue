<template>
  <PageContainer title="一卡通数据对接" subTitle="排班与考勤管理 / 一卡通数据对接">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" placeholder="卡号/长者姓名" allow-clear />
      </a-form-item>
      <a-form-item label="卡状态">
        <a-input v-model:value="query.status" placeholder="如 ACTIVE/INACTIVE" allow-clear />
      </a-form-item>
      <template #extra>
        <a-space>
          <a-button @click="onExportCsv">导出 CSV</a-button>
          <a-button @click="onExportExcel">导出 Excel</a-button>
        </a-space>
      </template>
    </SearchForm>

    <DataTable rowKey="cardAccountId" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" :row-class-name="rowClassName" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'syncStatus'">
          <a-tag :color="record.syncStatus === 'SYNCED' ? 'green' : record.syncStatus === 'CARD_LOST' ? 'red' : 'orange'">
            {{ record.syncStatus || '-' }}
          </a-tag>
        </template>
      </template>
    </DataTable>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { getHrCardSyncPage } from '../../api/hr'
import type { HrCardSyncItem, PageResult } from '../../types'
import { exportCsv, exportExcel } from '../../utils/export'
import { mapByDict } from './hrExportFields'

const query = reactive({
  keyword: undefined as string | undefined,
  status: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})
const columns = [
  { title: '长者', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '卡号', dataIndex: 'cardNo', key: 'cardNo', width: 180 },
  { title: '卡状态', dataIndex: 'cardStatus', key: 'cardStatus', width: 110 },
  { title: '挂失', dataIndex: 'lossFlag', key: 'lossFlag', width: 80 },
  { title: '开卡时间', dataIndex: 'openTime', key: 'openTime', width: 180 },
  { title: '最近充值', dataIndex: 'lastRechargeTime', key: 'lastRechargeTime', width: 180 },
  { title: '同步状态', dataIndex: 'syncStatus', key: 'syncStatus', width: 120 }
]
const rows = ref<HrCardSyncItem[]>([])
const loading = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<HrCardSyncItem> = await getHrCardSyncPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword,
      status: query.status
    })
    rows.value = res.list || []
    pagination.total = res.total || rows.value.length
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
  query.keyword = undefined
  query.status = undefined
  query.pageNo = 1
  query.pageSize = 10
  pagination.current = 1
  pagination.pageSize = 10
  fetchData()
}

function rowClassName(record: HrCardSyncItem) {
  if (record.syncStatus === 'CARD_LOST') return 'hr-row-danger'
  if (record.syncStatus === 'PENDING') return 'hr-row-warning'
  return ''
}

const exportFields = [
  { key: 'elderName', label: '长者' },
  { key: 'cardNo', label: '卡号' },
  { key: 'cardStatus', label: '卡状态' },
  { key: 'lossFlag', label: '挂失' },
  { key: 'openTime', label: '开卡时间' },
  { key: 'lastRechargeTime', label: '最近充值' },
  { key: 'syncStatus', label: '同步状态' }
]

function onExportCsv() {
  exportCsv(mapByDict(rows.value as Record<string, any>[], exportFields), '一卡通数据对接-当前筛选')
}

function onExportExcel() {
  exportExcel(mapByDict(rows.value as Record<string, any>[], exportFields), '一卡通数据对接-当前筛选')
}

onMounted(fetchData)
</script>

<style scoped>
:deep(.hr-row-danger) {
  background: #fff1f0 !important;
}

:deep(.hr-row-warning) {
  background: #fffbe6 !important;
}
</style>
