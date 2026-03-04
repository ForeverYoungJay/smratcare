<template>
  <PageContainer title="制度更新预警" subTitle="超期未更新制度识别与提醒">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" placeholder="文档名/目录/上传人" allow-clear />
      </a-form-item>
      <a-form-item label="超期天数">
        <a-input-number v-model:value="query.staleDays" :min="30" :max="2000" style="width: 160px" />
      </a-form-item>
      <template #extra>
        <a-space>
          <a-button @click="onExportCsv">导出 CSV</a-button>
          <a-button @click="onExportExcel">导出 Excel</a-button>
        </a-space>
      </template>
    </SearchForm>

    <DataTable
      rowKey="documentId"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      :row-class-name="rowClassName"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'alertLevel'">
          <a-tag :color="levelColor(record.alertLevel)">{{ record.alertLevel || '-' }}</a-tag>
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
import { getHrPolicyAlertPage } from '../../api/hr'
import type { HrPolicyAlertItem, PageResult } from '../../types'
import { exportCsv, exportExcel } from '../../utils/export'
import { mapByDict } from './hrExportFields'

const query = reactive({
  keyword: undefined as string | undefined,
  staleDays: 180,
  pageNo: 1,
  pageSize: 10
})
const columns = [
  { title: '制度文档', dataIndex: 'name', key: 'name', width: 260 },
  { title: '目录', dataIndex: 'folder', key: 'folder', width: 180 },
  { title: '上传人', dataIndex: 'uploaderName', key: 'uploaderName', width: 120 },
  { title: '上传时间', dataIndex: 'uploadedAt', key: 'uploadedAt', width: 180 },
  { title: '未更新天数', dataIndex: 'lastUpdatedDays', key: 'lastUpdatedDays', width: 120 },
  { title: '预警等级', dataIndex: 'alertLevel', key: 'alertLevel', width: 120 }
]
const rows = ref<HrPolicyAlertItem[]>([])
const loading = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

function levelColor(level?: string) {
  if (level === 'HIGH') return 'red'
  if (level === 'MEDIUM') return 'orange'
  return 'blue'
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<HrPolicyAlertItem> = await getHrPolicyAlertPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword,
      staleDays: query.staleDays
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
  query.staleDays = 180
  query.pageNo = 1
  query.pageSize = 10
  pagination.current = 1
  pagination.pageSize = 10
  fetchData()
}

function rowClassName(record: HrPolicyAlertItem) {
  if (record.alertLevel === 'HIGH') return 'hr-row-danger'
  if (record.alertLevel === 'MEDIUM') return 'hr-row-warning'
  return ''
}

const exportFields = [
  { key: 'name', label: '制度文档' },
  { key: 'folder', label: '目录' },
  { key: 'uploaderName', label: '上传人' },
  { key: 'uploadedAt', label: '上传时间' },
  { key: 'lastUpdatedDays', label: '未更新天数' },
  { key: 'alertLevel', label: '预警等级' }
]

function onExportCsv() {
  exportCsv(mapByDict(rows.value as Record<string, any>[], exportFields), '制度更新预警-当前筛选')
}

function onExportExcel() {
  exportExcel(mapByDict(rows.value as Record<string, any>[], exportFields), '制度更新预警-当前筛选')
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
