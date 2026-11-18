<template>
  <PageContainer title="考勤记录" subTitle="考勤与班组 / 考勤记录">
    <template #extra>
      <a-space>
        <a-button @click="onExportCsv">导出 CSV</a-button>
        <a-button @click="onExportExcel">导出 Excel</a-button>
      </a-space>
    </template>
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

    </SearchForm>
    <DataTable
      :filter-model="query"
      @clear-filters="fetchData"
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
        <template v-else-if="column.key === 'adjust'">
          <a @click="openAdjust(record)">申请调整</a>
        </template>
      </template>
    </DataTable>
    <a-modal v-model:open="adjustOpen" :title="`考勤调整单 · ${adjustTarget?.staffName || ''}`" ok-text="提交审批" @ok="submitAdjust">
      <a-alert type="info" show-icon style="margin-bottom: 12px" message="调整单经人事主管审批后才写回考勤；已核定锁定的月份只能通过调整单修改。" />
      <a-form layout="vertical">
        <a-form-item label="更正签到时间"><a-date-picker v-model:value="adjustForm.newCheckIn" show-time value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" /></a-form-item>
        <a-form-item label="更正签退时间（夜班可选次日）"><a-date-picker v-model:value="adjustForm.newCheckOut" show-time value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" /></a-form-item>
        <a-form-item label="更正状态">
          <a-select v-model:value="adjustForm.newStatus" allow-clear :options="[{ value: 'NORMAL', label: '正常' }, { value: 'LATE', label: '迟到' }, { value: 'EARLY_LEAVE', label: '早退' }, { value: 'ABSENT', label: '缺勤' }]" />
        </a-form-item>
        <a-form-item label="调整原因" required><a-textarea v-model:value="adjustForm.reason" :rows="2" placeholder="如：打卡设备故障，班组长确认实际到岗时间" /></a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { message } from 'ant-design-vue'
import { requestAttendanceAdjustment } from '../../api/hrOps'
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
  { title: '核验', dataIndex: 'reviewed', key: 'reviewed', width: 100 },
  { title: '', key: 'adjust', width: 90 }
]
const adjustOpen = ref(false)
const adjustTarget = ref<HrAttendanceRecordItem>()
const adjustForm = reactive({ newCheckIn: undefined as string | undefined, newCheckOut: undefined as string | undefined, newStatus: undefined as string | undefined, reason: '' })

function openAdjust(record: HrAttendanceRecordItem) {
  adjustTarget.value = record
  Object.assign(adjustForm, { newCheckIn: undefined, newCheckOut: undefined, newStatus: undefined, reason: '' })
  adjustOpen.value = true
}
async function submitAdjust() {
  if (!adjustForm.reason.trim()) return message.warning('请填写调整原因')
  await requestAttendanceAdjustment({ recordId: String(adjustTarget.value!.id), ...adjustForm })
  message.success('已提交调整单，待人事主管审批后生效')
  adjustOpen.value = false
}

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
  background: rgba(var(--danger-rgb), 0.1) !important;
}
</style>
