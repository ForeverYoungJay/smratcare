<template>
  <PageContainer title="合同打印" subTitle="员工档案中心 / 合同打印">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" placeholder="姓名/工号/手机号/合同编号" allow-clear style="width: 240px" />
      </a-form-item>
      <a-form-item label="合同状态">
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
        <template v-if="column.key === 'contractStatus'">
          <a-tag :color="contractStatusColor(record.contractStatus)">{{ contractStatusText(record.contractStatus) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
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
  { label: '待签署', value: 'PENDING' },
  { label: '已生效', value: 'ACTIVE' },
  { label: '续签处理中', value: 'RENEWAL_PENDING' },
  { label: '已到期', value: 'EXPIRED' },
  { label: '已终止', value: 'TERMINATED' }
]
const columns = [
  { title: '合同编号', dataIndex: 'contractNo', key: 'contractNo', width: 180 },
  { title: '工号', dataIndex: 'staffNo', key: 'staffNo', width: 120 },
  { title: '员工姓名', dataIndex: 'staffName', key: 'staffName', width: 120 },
  { title: '手机号', dataIndex: 'phone', key: 'phone', width: 140 },
  { title: '岗位', dataIndex: 'jobTitle', key: 'jobTitle', width: 120 },
  { title: '合同类型', dataIndex: 'contractType', key: 'contractType', width: 120 },
  { title: '合同状态', dataIndex: 'contractStatus', key: 'contractStatus', width: 120 },
  { title: '开始日期', dataIndex: 'contractStartDate', key: 'contractStartDate', width: 120 },
  { title: '到期日期', dataIndex: 'contractEndDate', key: 'contractEndDate', width: 120 },
  { title: '操作', key: 'action', width: 100 }
]
const rows = ref<HrProfileContractItem[]>([])
const loading = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

function contractStatusText(status?: string) {
  return statusOptions.find((item) => item.value === status)?.label || status || '-'
}

function contractStatusColor(status?: string) {
  switch (status) {
    case 'ACTIVE':
      return 'green'
    case 'RENEWAL_PENDING':
      return 'orange'
    case 'EXPIRED':
    case 'TERMINATED':
      return 'red'
    default:
      return 'default'
  }
}

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
  if (days < 0 || record.contractStatus === 'TERMINATED') return 'hr-row-danger'
  if (days > 0 && days <= 30) return 'hr-row-warning'
  return ''
}

const exportFields = [
  { key: 'contractNo', label: '合同编号' },
  { key: 'staffNo', label: '工号' },
  { key: 'staffName', label: '员工姓名' },
  { key: 'phone', label: '手机号' },
  { key: 'jobTitle', label: '岗位' },
  { key: 'contractType', label: '合同类型' },
  { key: 'contractStatusText', label: '合同状态' },
  { key: 'contractStartDate', label: '开始日期' },
  { key: 'contractEndDate', label: '到期日期' }
]

function onExportCsv() {
  exportCsv(mapByDict(rows.value.map((item) => ({
    ...item,
    contractStatusText: contractStatusText(item.contractStatus)
  })) as Record<string, any>[], exportFields), '合同打印-当前筛选')
}

function onExportExcel() {
  exportExcel(mapByDict(rows.value.map((item) => ({
    ...item,
    contractStatusText: contractStatusText(item.contractStatus)
  })) as Record<string, any>[], exportFields), '合同打印-当前筛选')
}

function printContract(record: HrProfileContractItem) {
  const win = window.open('', '_blank')
  if (!win) return
  const html = `
    <html><head><title>劳动合同打印</title></head>
    <body style="font-family: sans-serif; padding: 24px;">
      <h2>劳动合同打印单</h2>
      <p>合同编号：${record.contractNo || '-'}</p>
      <p>员工：${record.staffName || '-'}${record.staffNo ? `（${record.staffNo}）` : ''}</p>
      <p>手机号：${record.phone || '-'}</p>
      <p>岗位：${record.jobTitle || '-'}</p>
      <p>合同类型：${record.contractType || '-'}</p>
      <p>合同状态：${contractStatusText(record.contractStatus)}</p>
      <p>合同周期：${record.contractStartDate || '-'} ~ ${record.contractEndDate || '-'}</p>
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
