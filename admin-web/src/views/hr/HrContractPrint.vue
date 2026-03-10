<template>
  <PageContainer title="合同打印" subTitle="员工档案中心 / 合同打印">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键字">
        <ElderNameAutocomplete v-model:value="query.keyword" placeholder="长者姓名(编号)" width="220px" />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 180px" :options="statusOptions" />
      </a-form-item>
      <template #extra>
        <a-space>
          <a-button @click="onExportCsv">导出 CSV</a-button>
          <a-button @click="onExportExcel">导出 Excel</a-button>
        </a-space>
      </template>
    </SearchForm>

    <DataTable
      rowKey="contractId"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      :row-class-name="rowClassName"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <a-button type="link" @click="printContract(record)">打印</a-button>
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
import ElderNameAutocomplete from '../../components/ElderNameAutocomplete.vue'
import { getHrProfileContractPage } from '../../api/hr'
import type { HrProfileContractItem, PageResult } from '../../types'
import { exportCsv, exportExcel } from '../../utils/export'
import { mapByDict } from './hrExportFields'

const query = reactive({
  keyword: undefined as string | undefined,
  status: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})
const statusOptions = [
  { label: '待审批', value: 'PENDING_APPROVAL' },
  { label: '已审批', value: 'APPROVED' },
  { label: '已签署', value: 'SIGNED' },
  { label: '生效中', value: 'EFFECTIVE' }
]
const columns = [
  { title: '合同号', dataIndex: 'contractNo', key: 'contractNo', width: 180 },
  { title: '长者', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '联系人', dataIndex: 'contactName', key: 'contactName', width: 120 },
  { title: '联系电话', dataIndex: 'contactPhone', key: 'contactPhone', width: 140 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '开始日期', dataIndex: 'effectiveFrom', key: 'effectiveFrom', width: 120 },
  { title: '到期日期', dataIndex: 'effectiveTo', key: 'effectiveTo', width: 120 },
  { title: '操作', key: 'action', width: 100 }
]
const rows = ref<HrProfileContractItem[]>([])
const loading = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<HrProfileContractItem> = await getHrProfileContractPage({
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

function rowClassName(record: HrProfileContractItem) {
  const days = Number(record.remainingDays ?? 0)
  if (days < 0 || record.status === 'VOID') return 'hr-row-danger'
  if (days > 0 && days <= 30) return 'hr-row-warning'
  return ''
}

const exportFields = [
  { key: 'contractNo', label: '合同号' },
  { key: 'elderName', label: '长者' },
  { key: 'contactName', label: '联系人' },
  { key: 'contactPhone', label: '联系电话' },
  { key: 'status', label: '状态' },
  { key: 'effectiveFrom', label: '开始日期' },
  { key: 'effectiveTo', label: '到期日期' }
]

function onExportCsv() {
  exportCsv(mapByDict(rows.value as Record<string, any>[], exportFields), '合同打印-当前筛选')
}

function onExportExcel() {
  exportExcel(mapByDict(rows.value as Record<string, any>[], exportFields), '合同打印-当前筛选')
}

function printContract(record: HrProfileContractItem) {
  const win = window.open('', '_blank')
  if (!win) return
  const html = `
    <html><head><title>合同打印</title></head>
    <body style="font-family: sans-serif; padding: 24px;">
      <h2>合同打印单</h2>
      <p>合同号：${record.contractNo || '-'}</p>
      <p>长者：${record.elderName || '-'}</p>
      <p>联系人：${record.contactName || '-'} / ${record.contactPhone || '-'}</p>
      <p>状态：${record.status || '-'}（${record.contractStatus || '-'}）</p>
      <p>合同周期：${record.effectiveFrom || '-'} ~ ${record.effectiveTo || '-'}</p>
    </body></html>
  `
  win.document.write(html)
  win.document.close()
  win.focus()
  win.print()
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
