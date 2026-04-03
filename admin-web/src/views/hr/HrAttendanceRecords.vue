<template>
  <PageContainer title="考勤记录" subTitle="考勤与班组 / 考勤记录">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" placeholder="姓名/工号/手机号" allow-clear />
      </a-form-item>
      <a-form-item label="状态">
        <a-input v-model:value="query.status" placeholder="如 NORMAL/LATE/ABSENT" allow-clear />
      </a-form-item>
      <a-form-item label="日期区间">
        <a-range-picker v-model:value="dateRange" value-format="YYYY-MM-DD" />
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
        <template v-if="column.key === 'abnormal'">
          {{ record.abnormal ? '是' : '否' }}
        </template>
        <template v-else-if="column.key === 'reviewed'">
          {{ Number(record.reviewed) === 1 ? '已核验' : '待核验' }}
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
import { exportCsv, exportExcel } from '../../utils/export'
import { getHrAttendanceRecordPage } from '../../api/hr'
import type { HrAttendanceRecordItem, PageResult } from '../../types'
import { HR_ATTENDANCE_EXPORT_FIELDS, mapByDict } from './hrExportFields'

const query = reactive({
  keyword: undefined as string | undefined,
  status: undefined as string | undefined,
  dateFrom: undefined as string | undefined,
  dateTo: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})

const columns = [
  { title: '员工', dataIndex: 'staffName', key: 'staffName', width: 120 },
  { title: '签到时间', dataIndex: 'checkInTime', key: 'checkInTime', width: 180 },
  { title: '签退时间', dataIndex: 'checkOutTime', key: 'checkOutTime', width: 180 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '异常', dataIndex: 'abnormal', key: 'abnormal', width: 100 },
  { title: '核验', dataIndex: 'reviewed', key: 'reviewed', width: 100 }
]

const dateRange = ref<string[]>()
const rows = ref<HrAttendanceRecordItem[]>([])
const loading = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

async function fetchData() {
  query.dateFrom = dateRange.value?.[0]
  query.dateTo = dateRange.value?.[1]
  loading.value = true
  try {
    const res: PageResult<HrAttendanceRecordItem> = await getHrAttendanceRecordPage({ ...query })
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
  query.dateFrom = undefined
  query.dateTo = undefined
  dateRange.value = undefined
  query.pageNo = 1
  query.pageSize = 10
  pagination.current = 1
  pagination.pageSize = 10
  fetchData()
}

function rowClassName(record: HrAttendanceRecordItem) {
  if (record.abnormal) return 'hr-row-danger'
  return ''
}

function onExportCsv() {
  const exportRows = rows.value.map((item) => ({ ...item, abnormal: item.abnormal ? '是' : '否', reviewed: Number(item.reviewed) === 1 ? '已核验' : '待核验' }))
  exportCsv(mapByDict(exportRows as Record<string, any>[], HR_ATTENDANCE_EXPORT_FIELDS), '考勤记录-当前筛选')
}

function onExportExcel() {
  const exportRows = rows.value.map((item) => ({ ...item, abnormal: item.abnormal ? '是' : '否', reviewed: Number(item.reviewed) === 1 ? '已核验' : '待核验' }))
  exportExcel(mapByDict(exportRows as Record<string, any>[], HR_ATTENDANCE_EXPORT_FIELDS), '考勤记录-当前筛选')
}

onMounted(fetchData)
</script>

<style scoped>
:deep(.hr-row-danger) {
  background: #fff1f0 !important;
}
</style>
