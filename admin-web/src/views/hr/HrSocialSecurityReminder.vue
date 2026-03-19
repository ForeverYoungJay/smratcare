<template>
  <PageContainer title="社保到期/未办理明细" subTitle="按提醒日期跟进待办理、即将提醒和已参保员工">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" placeholder="姓名/工号/手机号" allow-clear style="width: 240px" />
      </a-form-item>
      <a-form-item label="提醒范围">
        <a-select v-model:value="query.scope" :options="scopeOptions" style="width: 180px" />
      </a-form-item>
      <a-form-item label="社保状态">
        <a-select v-model:value="query.status" :options="statusOptions" allow-clear style="width: 180px" />
      </a-form-item>
      <template #extra>
        <a-space>
          <a-button @click="onExportCsv">导出 CSV</a-button>
          <a-button @click="onExportExcel">导出 Excel</a-button>
          <a-tag color="blue">当前范围：{{ scopeText(query.scope) }}</a-tag>
        </a-space>
      </template>
    </SearchForm>

    <DataTable
      rowKey="staffId"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      :row-class-name="rowClassName"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'socialSecurityStatus'">
          <a-tag :color="statusColor(record.socialSecurityStatus)">{{ statusText(record.socialSecurityStatus) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'remainingDays'">
          <a-tag v-if="(record.remainingDays || 0) < 0" color="red">已逾期 {{ Math.abs(record.remainingDays || 0) }} 天</a-tag>
          <a-tag v-else-if="(record.remainingDays || 0) === 0" color="volcano">今天提醒</a-tag>
          <a-tag v-else-if="(record.remainingDays || 0) <= 7" color="orange">{{ record.remainingDays }} 天后提醒</a-tag>
          <span v-else>{{ record.remainingDays ?? '-' }} 天</span>
        </template>
        <template v-else-if="column.key === 'reminderScope'">
          <a-tag :color="scopeColor(record.reminderScope)">{{ scopeText(record.reminderScope) }}</a-tag>
        </template>
      </template>
    </DataTable>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { getHrSocialSecurityReminderPage } from '../../api/hr'
import type { HrSocialSecurityReminderItem, PageResult } from '../../types'
import { exportCsv, exportExcel } from '../../utils/export'
import { mapByDict } from './hrExportFields'

const route = useRoute()
const query = reactive({
  keyword: undefined as string | undefined,
  scope: 'ALL',
  status: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})

const scopeOptions = [
  { label: '全部', value: 'ALL' },
  { label: '已到提醒日', value: 'DUE' },
  { label: '7日内将提醒', value: 'UPCOMING' },
  { label: '未办理', value: 'PENDING' },
  { label: '已参保', value: 'COMPLETED' }
]
const statusOptions = [
  { label: '待办理', value: 'PENDING' },
  { label: '办理中', value: 'PROCESSING' },
  { label: '已参保', value: 'COMPLETED' },
  { label: '暂停缴纳', value: 'STOPPED' }
]
const columns = [
  { title: '工号', dataIndex: 'staffNo', key: 'staffNo', width: 120 },
  { title: '员工姓名', dataIndex: 'staffName', key: 'staffName', width: 120 },
  { title: '手机号', dataIndex: 'phone', key: 'phone', width: 140 },
  { title: '岗位', dataIndex: 'jobTitle', key: 'jobTitle', width: 140 },
  { title: '入职日期', dataIndex: 'hireDate', key: 'hireDate', width: 120 },
  { title: '社保状态', dataIndex: 'socialSecurityStatus', key: 'socialSecurityStatus', width: 120 },
  { title: '参保开始', dataIndex: 'socialSecurityStartDate', key: 'socialSecurityStartDate', width: 120 },
  { title: '提醒天数', dataIndex: 'socialSecurityReminderDays', key: 'socialSecurityReminderDays', width: 110 },
  { title: '提醒日期', dataIndex: 'reminderDate', key: 'reminderDate', width: 120 },
  { title: '提醒范围', dataIndex: 'reminderScope', key: 'reminderScope', width: 120 },
  { title: '剩余天数', dataIndex: 'remainingDays', key: 'remainingDays', width: 120 },
  { title: '备注', dataIndex: 'socialSecurityRemark', key: 'socialSecurityRemark', width: 220 }
]

const rows = ref<HrSocialSecurityReminderItem[]>([])
const loading = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

function statusText(status?: string) {
  return statusOptions.find((item) => item.value === status)?.label || status || '待办理'
}

function statusColor(status?: string) {
  switch (status) {
    case 'COMPLETED':
      return 'green'
    case 'PROCESSING':
      return 'blue'
    case 'STOPPED':
      return 'default'
    default:
      return 'orange'
  }
}

function scopeText(scope?: string) {
  return scopeOptions.find((item) => item.value === scope)?.label || scope || '全部'
}

function scopeColor(scope?: string) {
  switch (scope) {
    case 'DUE':
      return 'red'
    case 'UPCOMING':
      return 'orange'
    case 'COMPLETED':
      return 'green'
    default:
      return 'blue'
  }
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<HrSocialSecurityReminderItem> = await getHrSocialSecurityReminderPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword,
      scope: query.scope,
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

function onReset() {
  query.keyword = undefined
  query.scope = 'ALL'
  query.status = undefined
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

function rowClassName(record: HrSocialSecurityReminderItem) {
  const days = Number(record.remainingDays ?? 9999)
  if ((record.reminderScope || '') === 'DUE' || days < 0) return 'hr-row-danger'
  if ((record.reminderScope || '') === 'UPCOMING' || days <= 7) return 'hr-row-warning'
  return ''
}

const exportFields = [
  { key: 'staffNo', label: '工号' },
  { key: 'staffName', label: '员工姓名' },
  { key: 'phone', label: '手机号' },
  { key: 'jobTitle', label: '岗位' },
  { key: 'hireDate', label: '入职日期' },
  { key: 'socialSecurityStatusText', label: '社保状态' },
  { key: 'socialSecurityStartDate', label: '参保开始' },
  { key: 'socialSecurityReminderDays', label: '提醒天数' },
  { key: 'reminderDate', label: '提醒日期' },
  { key: 'reminderScopeText', label: '提醒范围' },
  { key: 'remainingDays', label: '剩余天数' },
  { key: 'socialSecurityRemark', label: '备注' }
]

function onExportCsv() {
  exportCsv(mapByDict(rows.value.map((item) => ({
    ...item,
    socialSecurityStatusText: statusText(item.socialSecurityStatus),
    reminderScopeText: scopeText(item.reminderScope)
  })) as Record<string, any>[], exportFields), '社保到期未办理明细-当前筛选')
}

function onExportExcel() {
  exportExcel(mapByDict(rows.value.map((item) => ({
    ...item,
    socialSecurityStatusText: statusText(item.socialSecurityStatus),
    reminderScopeText: scopeText(item.reminderScope)
  })) as Record<string, any>[], exportFields), '社保到期未办理明细-当前筛选')
}

onMounted(() => {
  const scope = String(route.query.scope || '').trim().toUpperCase()
  if (scope) {
    query.scope = scope
  }
  fetchData()
})
</script>

<style scoped>
:deep(.hr-row-danger) {
  background: #fff1f0 !important;
}

:deep(.hr-row-warning) {
  background: #fffbe6 !important;
}
</style>
