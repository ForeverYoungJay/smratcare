<template>
  <PageContainer title="考勤异常" subTitle="考勤异常记录与追踪">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="员工关键字">
        <a-input v-model:value="query.keyword" placeholder="姓名/工号/手机号" allow-clear />
      </a-form-item>
      <a-form-item label="异常状态">
        <a-select v-model:value="query.status" allow-clear style="width: 180px" :options="statusOptions" />
      </a-form-item>
      <a-form-item label="日期范围">
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
      rowKey="id"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      :row-class-name="rowClassName"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag color="red">{{ record.status || '-' }}</a-tag>
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
import { exportCsv, exportExcel } from '../../utils/export'
import { getHrAttendanceAbnormalPage } from '../../api/hr'
import type { HrAttendanceAbnormalItem, PageResult } from '../../types'
import { mapByDict } from './hrExportFields'

const query = reactive({
  keyword: undefined as string | undefined,
  status: undefined as string | undefined,
  range: undefined as [Dayjs, Dayjs] | undefined,
  pageNo: 1,
  pageSize: 10
})

const statusOptions = [
  { label: '迟到', value: 'LATE' },
  { label: '早退', value: 'EARLY_LEAVE' },
  { label: '缺卡', value: 'MISSING_PUNCH' },
  { label: '缺勤', value: 'ABSENT' }
]
const columns = [
  { title: '员工', dataIndex: 'staffName', key: 'staffName', width: 140 },
  { title: '签到时间', dataIndex: 'checkInTime', key: 'checkInTime', width: 180 },
  { title: '签退时间', dataIndex: 'checkOutTime', key: 'checkOutTime', width: 180 },
  { title: '异常状态', dataIndex: 'status', key: 'status', width: 120 }
]
const rows = ref<HrAttendanceAbnormalItem[]>([])
const loading = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const abnormalExportFields = [
  { key: 'staffName', label: '员工姓名' },
  { key: 'checkInTime', label: '签到时间' },
  { key: 'checkOutTime', label: '签退时间' },
  { key: 'status', label: '异常状态' }
]

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
    const res: PageResult<HrAttendanceAbnormalItem> = await getHrAttendanceAbnormalPage(params)
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

function rowClassName() {
  return 'hr-row-danger'
}

function onExportCsv() {
  exportCsv(mapByDict(rows.value as Record<string, any>[], abnormalExportFields), '考勤异常-当前筛选')
}

function onExportExcel() {
  exportExcel(mapByDict(rows.value as Record<string, any>[], abnormalExportFields), '考勤异常-当前筛选')
}

onMounted(fetchData)
</script>

<style scoped>
:deep(.hr-row-danger) {
  background: #fff1f0 !important;
}
</style>
