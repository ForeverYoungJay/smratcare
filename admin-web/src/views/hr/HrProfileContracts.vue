<template>
  <PageContainer title="劳动合同管理" subTitle="员工档案中心 / 劳动合同管理">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" placeholder="合同号/长者/联系人/电话" allow-clear />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 180px" :options="statusOptions" />
      </a-form-item>
      <a-form-item label="到期范围">
        <a-range-picker v-model:value="query.range" />
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
        <template v-if="column.key === 'remainingDays'">
          <a-tag v-if="(record.remainingDays || 0) < 0" color="red">已过期 {{ Math.abs(record.remainingDays || 0) }} 天</a-tag>
          <a-tag v-else-if="(record.remainingDays || 0) <= 30" color="orange">{{ record.remainingDays }} 天</a-tag>
          <span v-else>{{ record.remainingDays ?? '-' }} 天</span>
        </template>
      </template>
    </DataTable>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { Dayjs } from 'dayjs'
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
  range: undefined as [Dayjs, Dayjs] | undefined,
  pageNo: 1,
  pageSize: 10
})
const statusOptions = [
  { label: '待审批', value: 'PENDING_APPROVAL' },
  { label: '已审批', value: 'APPROVED' },
  { label: '已签署', value: 'SIGNED' },
  { label: '生效中', value: 'EFFECTIVE' },
  { label: '作废', value: 'VOID' }
]
const columns = [
  { title: '合同号', dataIndex: 'contractNo', key: 'contractNo', width: 170 },
  { title: '长者', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '联系人', dataIndex: 'contactName', key: 'contactName', width: 120 },
  { title: '联系电话', dataIndex: 'contactPhone', key: 'contactPhone', width: 140 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '业务状态', dataIndex: 'contractStatus', key: 'contractStatus', width: 150 },
  { title: '开始日期', dataIndex: 'effectiveFrom', key: 'effectiveFrom', width: 120 },
  { title: '到期日期', dataIndex: 'effectiveTo', key: 'effectiveTo', width: 120 },
  { title: '剩余天数', dataIndex: 'remainingDays', key: 'remainingDays', width: 120 }
]
const rows = ref<HrProfileContractItem[]>([])
const loading = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

async function fetchData() {
  loading.value = true
  try {
    const params: any = {
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword,
      status: query.status
    }
    if (query.range) {
      params.dateFrom = query.range[0].format('YYYY-MM-DD')
      params.dateTo = query.range[1].format('YYYY-MM-DD')
    }
    const res: PageResult<HrProfileContractItem> = await getHrProfileContractPage(params)
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
  query.range = undefined
  query.pageNo = 1
  query.pageSize = 10
  pagination.current = 1
  pagination.pageSize = 10
  fetchData()
}

function rowClassName(record: HrProfileContractItem) {
  const days = Number(record.remainingDays ?? 0)
  if (days < 0) return 'hr-row-danger'
  if (days <= 30) return 'hr-row-warning'
  return ''
}

const exportFields = [
  { key: 'contractNo', label: '合同号' },
  { key: 'elderName', label: '长者' },
  { key: 'contactName', label: '联系人' },
  { key: 'contactPhone', label: '联系电话' },
  { key: 'status', label: '状态' },
  { key: 'contractStatus', label: '业务状态' },
  { key: 'effectiveFrom', label: '开始日期' },
  { key: 'effectiveTo', label: '到期日期' },
  { key: 'remainingDays', label: '剩余天数' }
]

function onExportCsv() {
  exportCsv(mapByDict(rows.value as Record<string, any>[], exportFields), '劳动合同管理-当前筛选')
}

function onExportExcel() {
  exportExcel(mapByDict(rows.value as Record<string, any>[], exportFields), '劳动合同管理-当前筛选')
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
