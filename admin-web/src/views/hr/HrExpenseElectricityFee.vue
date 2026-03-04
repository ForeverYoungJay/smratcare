<template>
  <PageContainer title="员工电费" subTitle="费用与报销 / 员工电费">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" placeholder="标题/申请人/备注" allow-clear />
      </a-form-item>
      <a-form-item label="审批状态">
        <a-input v-model:value="query.status" placeholder="如 PENDING/APPROVED/REJECTED" allow-clear />
      </a-form-item>
      <template #extra>
        <a-space>
          <a-button @click="onExportCsv">导出 CSV</a-button>
          <a-button @click="onExportExcel">导出 Excel</a-button>
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
    />
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { exportCsv, exportExcel } from '../../utils/export'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { getHrElectricityFeePage } from '../../api/hr'
import type { HrExpenseItem, PageResult } from '../../types'
import { HR_EXPENSE_EXPORT_FIELDS, mapByDict } from './hrExportFields'

const query = reactive({ keyword: undefined as string | undefined, status: undefined as string | undefined, pageNo: 1, pageSize: 10 })
const columns = [
  { title: '标题', dataIndex: 'title', key: 'title', width: 240 },
  { title: '申请人', dataIndex: 'applicantName', key: 'applicantName', width: 120 },
  { title: '金额', dataIndex: 'amount', key: 'amount', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 110 },
  { title: '申请时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '备注', dataIndex: 'remark', key: 'remark', width: 240 }
]
const rows = ref<HrExpenseItem[]>([])
const loading = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<HrExpenseItem> = await getHrElectricityFeePage({ ...query })
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

function rowClassName(record: HrExpenseItem) {
  if (record.status === 'REJECTED') return 'hr-row-danger'
  if (record.status === 'PENDING') return 'hr-row-warning'
  return ''
}

function onExportCsv() {
  exportCsv(mapByDict(rows.value as Record<string, any>[], HR_EXPENSE_EXPORT_FIELDS), '员工电费-当前筛选')
}

function onExportExcel() {
  exportExcel(mapByDict(rows.value as Record<string, any>[], HR_EXPENSE_EXPORT_FIELDS), '员工电费-当前筛选')
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
