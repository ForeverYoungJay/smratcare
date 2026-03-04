<template>
  <PageContainer title="规章制度库" subTitle="制度文档归档与检索">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" placeholder="文档名/上传人/备注" allow-clear />
      </a-form-item>
      <a-form-item label="目录">
        <a-input v-model:value="query.folder" placeholder="默认检索制度相关目录" allow-clear />
      </a-form-item>
      <template #extra>
        <a-space>
          <a-button @click="onExportCsv">导出 CSV</a-button>
          <a-button @click="onExportExcel">导出 Excel</a-button>
          <a-button @click="fetchData">刷新</a-button>
        </a-space>
      </template>
    </SearchForm>

    <DataTable
      rowKey="id"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      :row-class-name="rowClassName"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'url'">
          <a v-if="record.url" :href="record.url" target="_blank">查看</a>
          <span v-else>-</span>
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
import { getHrPolicyPage } from '../../api/hr'
import type { OaDocument, PageResult } from '../../types'
import { exportCsv, exportExcel } from '../../utils/export'
import { mapByDict } from './hrExportFields'

const query = reactive({
  keyword: undefined as string | undefined,
  folder: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})
const columns = [
  { title: '文档名称', dataIndex: 'name', key: 'name', width: 240 },
  { title: '目录', dataIndex: 'folder', key: 'folder', width: 180 },
  { title: '上传人', dataIndex: 'uploaderName', key: 'uploaderName', width: 120 },
  { title: '上传时间', dataIndex: 'uploadedAt', key: 'uploadedAt', width: 180 },
  { title: '链接', dataIndex: 'url', key: 'url', width: 100 }
]
const rows = ref<OaDocument[]>([])
const loading = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<OaDocument> = await getHrPolicyPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword,
      folder: query.folder
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
  query.folder = undefined
  query.pageNo = 1
  query.pageSize = 10
  pagination.current = 1
  pagination.pageSize = 10
  fetchData()
}

function rowClassName(record: OaDocument) {
  if (!record.url) return 'hr-row-warning'
  return ''
}

const exportFields = [
  { key: 'name', label: '文档名称' },
  { key: 'folder', label: '目录' },
  { key: 'uploaderName', label: '上传人' },
  { key: 'uploadedAt', label: '上传时间' },
  { key: 'url', label: '链接' }
]

function onExportCsv() {
  exportCsv(mapByDict(rows.value as Record<string, any>[], exportFields), '规章制度库-当前筛选')
}

function onExportExcel() {
  exportExcel(mapByDict(rows.value as Record<string, any>[], exportFields), '规章制度库-当前筛选')
}

onMounted(fetchData)
</script>

<style scoped>
:deep(.hr-row-warning) {
  background: #fffbe6 !important;
}
</style>
