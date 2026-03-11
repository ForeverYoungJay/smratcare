<template>
  <PageContainer title="合同到期提醒" subTitle="员工劳动合同按到期天数预警">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" placeholder="姓名/工号/手机号/合同编号" allow-clear style="width: 240px" />
      </a-form-item>
      <a-form-item label="预警天数">
        <a-input-number v-model:value="query.warningDays" :min="0" :max="365" style="width: 140px" />
      </a-form-item>
      <a-form-item label="包含已过期">
        <a-switch v-model:checked="query.includeExpired" />
      </a-form-item>
      <template #extra>
        <a-space>
          <a-button @click="onExportCsv">导出 CSV</a-button>
          <a-button @click="onExportExcel">导出 Excel</a-button>
          <a-tag color="orange">当前口径：到期≤{{ query.warningDays }}天{{ query.includeExpired ? '（含已过期）' : '' }}</a-tag>
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
          <a-tag v-else-if="(record.remainingDays || 0) <= 7" color="volcano">{{ record.remainingDays }} 天</a-tag>
          <a-tag v-else-if="(record.remainingDays || 0) <= 30" color="orange">{{ record.remainingDays }} 天</a-tag>
          <span v-else>{{ record.remainingDays ?? '-' }} 天</span>
        </template>
        <template v-else-if="column.key === 'contractStatus'">
          <a-tag :color="contractStatusColor(record.contractStatus)">{{ contractStatusText(record.contractStatus) }}</a-tag>
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
import { getHrContractReminderPage } from '../../api/hr'
import type { HrContractReminderItem, PageResult } from '../../types'
import { exportCsv, exportExcel } from '../../utils/export'
import { mapByDict } from './hrExportFields'

const query = reactive({
  keyword: undefined as string | undefined,
  warningDays: 30,
  includeExpired: false,
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
const rows = ref<HrContractReminderItem[]>([])
const loading = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '合同编号', dataIndex: 'contractNo', key: 'contractNo', width: 170 },
  { title: '工号', dataIndex: 'staffNo', key: 'staffNo', width: 120 },
  { title: '员工姓名', dataIndex: 'staffName', key: 'staffName', width: 120 },
  { title: '手机号', dataIndex: 'phone', key: 'phone', width: 140 },
  { title: '岗位', dataIndex: 'jobTitle', key: 'jobTitle', width: 130 },
  { title: '合同类型', dataIndex: 'contractType', key: 'contractType', width: 120 },
  { title: '合同状态', dataIndex: 'contractStatus', key: 'contractStatus', width: 120 },
  { title: '生效开始', dataIndex: 'contractStartDate', key: 'contractStartDate', width: 120 },
  { title: '到期日期', dataIndex: 'contractEndDate', key: 'contractEndDate', width: 120 },
  { title: '剩余天数', dataIndex: 'remainingDays', key: 'remainingDays', width: 130 }
]

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
    const res: PageResult<HrContractReminderItem> = await getHrContractReminderPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword,
      warningDays: query.warningDays,
      includeExpired: query.includeExpired
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

function onReset() {
  query.keyword = undefined
  query.warningDays = 30
  query.includeExpired = false
  query.pageNo = 1
  query.pageSize = 10
  pagination.current = 1
  pagination.pageSize = 10
  fetchData()
}

function handleTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  fetchData()
}

function rowClassName(record: HrContractReminderItem) {
  const days = Number(record.remainingDays ?? 0)
  if (days < 0) return 'hr-row-danger'
  if (days <= 30) return 'hr-row-warning'
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
  { key: 'contractStartDate', label: '生效开始' },
  { key: 'contractEndDate', label: '到期日期' },
  { key: 'remainingDays', label: '剩余天数' }
]

function onExportCsv() {
  exportCsv(mapByDict(rows.value.map((item) => ({
    ...item,
    contractStatusText: contractStatusText(item.contractStatus)
  })) as Record<string, any>[], exportFields), '合同到期提醒-当前筛选')
}

function onExportExcel() {
  exportExcel(mapByDict(rows.value.map((item) => ({
    ...item,
    contractStatusText: contractStatusText(item.contractStatus)
  })) as Record<string, any>[], exportFields), '合同到期提醒-当前筛选')
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
