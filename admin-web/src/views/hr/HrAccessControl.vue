<template>
  <PageContainer title="门禁对接" subTitle="考勤与班组 / 门禁对接">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" placeholder="姓名/工号/手机号" allow-clear />
      </a-form-item>
      <a-form-item label="状态">
        <a-input v-model:value="query.status" placeholder="如 NORMAL/LATE/ABSENT" allow-clear />
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

    <DataTable rowKey="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" :row-class-name="rowClassName" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'accessResult'">
          <a-tag :color="record.accessResult === 'PASS' ? 'green' : record.accessResult === 'EXCEPTION' ? 'red' : 'default'">
            {{ record.accessResult || '-' }}
          </a-tag>
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
import { getHrAccessControlPage } from '../../api/hr'
import type { HrAccessControlRecordItem, PageResult } from '../../types'
import { exportCsv, exportExcel } from '../../utils/export'
import { mapByDict } from './hrExportFields'

const query = reactive({
  keyword: undefined as string | undefined,
  status: undefined as string | undefined,
  range: undefined as [Dayjs, Dayjs] | undefined,
  pageNo: 1,
  pageSize: 10
})
const columns = [
  { title: '员工', dataIndex: 'staffName', key: 'staffName', width: 140 },
  { title: '签到时间', dataIndex: 'checkInTime', key: 'checkInTime', width: 180 },
  { title: '签退时间', dataIndex: 'checkOutTime', key: 'checkOutTime', width: 180 },
  { title: '考勤状态', dataIndex: 'attendanceStatus', key: 'attendanceStatus', width: 120 },
  { title: '门禁结果', dataIndex: 'accessResult', key: 'accessResult', width: 110 }
]
const rows = ref<HrAccessControlRecordItem[]>([])
const loading = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

async function fetchData() {
  loading.value = true
  try {
    const params: any = { pageNo: query.pageNo, pageSize: query.pageSize, keyword: query.keyword, status: query.status }
    if (query.range) {
      params.dateFrom = query.range[0].format('YYYY-MM-DD')
      params.dateTo = query.range[1].format('YYYY-MM-DD')
    }
    const res: PageResult<HrAccessControlRecordItem> = await getHrAccessControlPage(params)
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

function rowClassName(record: HrAccessControlRecordItem) {
  if (record.accessResult === 'EXCEPTION') return 'hr-row-danger'
  if (record.accessResult === 'UNKNOWN') return 'hr-row-warning'
  return ''
}

const exportFields = [
  { key: 'staffName', label: '员工' },
  { key: 'checkInTime', label: '签到时间' },
  { key: 'checkOutTime', label: '签退时间' },
  { key: 'attendanceStatus', label: '考勤状态' },
  { key: 'accessResult', label: '门禁结果' }
]

function onExportCsv() {
  exportCsv(mapByDict(rows.value as Record<string, any>[], exportFields), '门禁对接-当前筛选')
}

function onExportExcel() {
  exportExcel(mapByDict(rows.value as Record<string, any>[], exportFields), '门禁对接-当前筛选')
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
