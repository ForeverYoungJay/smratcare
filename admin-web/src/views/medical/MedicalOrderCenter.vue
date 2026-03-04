<template>
  <PageContainer title="医嘱管理中心" subTitle="开立 / 审核 / 查对 / 执行 / 停嘱">
    <a-alert
      type="info"
      show-icon
      style="margin-bottom: 12px"
      :message="`今日医嘱应执行 ${summary.medicalOrderShouldCount}，已执行 ${summary.medicalOrderDoneCount}，待执行 ${summary.medicalOrderPendingCount}，异常 ${summary.medicalOrderAbnormalCount}。`"
    />

    <a-row :gutter="16" style="margin-bottom: 12px">
      <a-col :xs="24" :md="6"><a-card :bordered="false" class="card-elevated"><a-statistic title="应执行" :value="summary.medicalOrderShouldCount" /></a-card></a-col>
      <a-col :xs="24" :md="6"><a-card :bordered="false" class="card-elevated"><a-statistic title="已执行" :value="summary.medicalOrderDoneCount" /></a-card></a-col>
      <a-col :xs="24" :md="6"><a-card :bordered="false" class="card-elevated"><a-statistic title="待执行" :value="summary.medicalOrderPendingCount" /></a-card></a-col>
      <a-col :xs="24" :md="6"><a-card :bordered="false" class="card-elevated"><a-statistic title="执行异常" :value="summary.medicalOrderAbnormalCount" /></a-card></a-col>
    </a-row>

    <SearchForm :model="query" @search="fetchTaskData" @reset="onReset">
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="长者/药品" allow-clear />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 140px" :options="statusOptions" />
      </a-form-item>
      <a-form-item label="任务日期">
        <a-date-picker v-model:value="query.taskDate" value-format="YYYY-MM-DD" />
      </a-form-item>
      <template #extra>
        <a-space>
          <a-button @click="exportCsvData">导出CSV</a-button>
          <a-button @click="exportExcelData">导出Excel</a-button>
          <a-button type="primary" @click="go('/medical-care/medication-registration', { filter: 'to_execute', assignee: 'me', date: 'today' })">待执行医嘱</a-button>
          <a-button @click="go('/medical-care/medication-registration', { date: 'today', filter: 'pending_or_abnormal' })">待执行/异常</a-button>
        </a-space>
      </template>
    </SearchForm>

    <DataTable
      rowKey="id"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      :row-class-name="resolveRowClassName"
      @change="onTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'plannedTime'">
          {{ formatDateTime(record.plannedTime) }}
        </template>
        <template v-else-if="column.key === 'doneTime'">
          {{ formatDateTime(record.doneTime) }}
        </template>
        <template v-else-if="column.key === 'status'">
          <a-tag :color="statusColor(record.status)">{{ statusLabel(record.status) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="go('/medical-care/medication-registration', { elderId: record.elderId, keyword: record.drugName })">去登记</a-button>
            <a-button type="link" @click="go('/medical-care/medication-registration', { elderId: record.elderId, date: 'today', filter: 'pending_or_abnormal' })">查看执行</a-button>
          </a-space>
        </template>
      </template>
    </DataTable>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { exportCsv, exportExcel } from '../../utils/export'
import { mapMedicalExportRows, medicalOrderTaskExportColumns } from '../../constants/medicalExport'
import { resolveMedicalError } from './medicalError'
import { getMedicalHealthCenterSummary } from '../../api/medicalCare'
import { getHealthMedicationTaskPage } from '../../api/health'
import type { HealthMedicationTask, MedicalCareWorkbenchSummary, PageResult } from '../../types'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const rows = ref<HealthMedicationTask[]>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const query = reactive({
  keyword: '',
  status: '' as string | undefined,
  taskDate: dayjs().format('YYYY-MM-DD'),
  pageNo: 1,
  pageSize: 10
})
const statusOptions = [
  { label: '待执行', value: 'PENDING' },
  { label: '已执行', value: 'DONE' },
  { label: '已取消', value: 'CANCELLED' }
]
const columns = [
  { title: '长者', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '药品', dataIndex: 'drugName', key: 'drugName', width: 160 },
  { title: '计划时间', dataIndex: 'plannedTime', key: 'plannedTime', width: 180 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '完成时间', dataIndex: 'doneTime', key: 'doneTime', width: 180 },
  { title: '登记ID', dataIndex: 'registrationId', key: 'registrationId', width: 100 },
  { title: '操作', key: 'action', width: 170 }
]
const summary = reactive<MedicalCareWorkbenchSummary>({
  pendingMedicalOrderCount: 0,
  pendingReviewCount: 0,
  pendingAuditCount: 0,
  unclosedAbnormalCount: 0,
  todayInspectionTodoCount: 0,
  topRiskResidentCount: 0,
  abnormalVital24hCount: 0,
  abnormalEvent24hCount: 0,
  medicalOrderShouldCount: 0,
  medicalOrderDoneCount: 0,
  medicalOrderPendingCount: 0,
  medicalOrderAbnormalCount: 0,
  orderCheckRate: 0,
  medicationShouldCount: 0,
  medicationDoneCount: 0,
  medicationUndoneCount: 0,
  medicationLowStockCount: 0,
  medicationRequestPendingCount: 0,
  careTaskShouldCount: 0,
  careTaskDoneCount: 0,
  careTaskOverdueCount: 0,
  scanExecuteRate: 0,
  todayInspectionPlanCount: 0,
  nursingLogPendingCount: 0,
  handoverPendingCount: 0,
  handoverDoneCount: 0,
  handoverRiskCount: 0,
  handoverTodoCount: 0,
  incidentOpenCount: 0,
  incident30dCount: 0,
  incident30dRate: 0,
  lowScoreSurveyCount: 0,
  rectifyInProgressCount: 0,
  rectifyOverdueCount: 0,
  aiReportGeneratedCount: 0,
  aiReportPublishedCount: 0,
  pendingCareTaskCount: 0,
  overdueCareTaskCount: 0,
  todayInspectionPendingCount: 0,
  todayInspectionDoneCount: 0,
  abnormalInspectionCount: 0,
  todayMedicationPendingCount: 0,
  todayMedicationDoneCount: 0,
  tcmPublishedCount: 0,
  cvdHighRiskCount: 0,
  cvdNeedFollowupCount: 0,
  keyResidents: []
})

function go(path: string, queryParams?: Record<string, any>) {
  router.push({
    path,
    query: {
      ...route.query,
      ...(queryParams || {})
    }
  })
}

function parseFilterToStatus(filter?: string) {
  if (!filter) return undefined
  if (filter === 'to_execute' || filter === 'pending' || filter === 'pending_or_abnormal' || filter === 'check_pending') {
    return 'PENDING'
  }
  if (filter === 'done') return 'DONE'
  return undefined
}

function statusLabel(status?: string) {
  if (status === 'PENDING') return '待执行'
  if (status === 'DONE') return '已执行'
  if (status === 'CANCELLED') return '已取消'
  return status || '-'
}

function statusColor(status?: string) {
  if (status === 'DONE') return 'green'
  if (status === 'CANCELLED') return 'default'
  return 'orange'
}

function resolveRowClassName(record: HealthMedicationTask) {
  if (record.status === 'PENDING' && record.plannedTime && dayjs(record.plannedTime).isBefore(dayjs())) {
    return 'medical-row-danger'
  }
  if (record.status === 'PENDING') return 'medical-row-warning'
  return ''
}

function formatDateTime(value?: string) {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : '-'
}

async function load() {
  try {
    const data = await getMedicalHealthCenterSummary({
      elderId: route.query.elderId ? Number(route.query.elderId) : undefined,
      date: typeof route.query.date === 'string' ? route.query.date : undefined,
      status: typeof route.query.status === 'string' ? route.query.status : undefined
    })
    Object.assign(summary, data || {})
  } catch (error) {
    message.error(resolveMedicalError(error, '加载统计失败'))
  }
}

async function fetchTaskData() {
  loading.value = true
  try {
    const res: PageResult<HealthMedicationTask> = await getHealthMedicationTaskPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined,
      status: query.status || undefined,
      taskDate: query.taskDate || undefined,
      elderId: route.query.elderId ? Number(route.query.elderId) : undefined
    })
    rows.value = res.list || []
    pagination.total = res.total || rows.value.length
  } catch (error) {
    message.error(resolveMedicalError(error, '加载医嘱任务失败'))
    rows.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

function onReset() {
  query.keyword = ''
  query.status = parseFilterToStatus(typeof route.query.filter === 'string' ? route.query.filter : undefined)
  const routeDate = typeof route.query.date === 'string' ? route.query.date : ''
  query.taskDate = routeDate && routeDate !== 'today' ? routeDate : dayjs().format('YYYY-MM-DD')
  query.pageNo = 1
  query.pageSize = pagination.pageSize
  pagination.current = 1
  fetchTaskData()
}

function onTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  fetchTaskData()
}

function exportRows() {
  return mapMedicalExportRows(
    rows.value.map((item) => ({
      ...item,
      plannedTime: formatDateTime(item.plannedTime),
      status: statusLabel(item.status),
      doneTime: formatDateTime(item.doneTime)
    })),
    medicalOrderTaskExportColumns
  )
}

function exportCsvData() {
  const data = exportRows()
  if (!data.length) {
    message.warning('暂无可导出数据')
    return
  }
  exportCsv(data, `医嘱任务-${dayjs().format('YYYYMMDD-HHmmss')}.csv`)
  message.success('CSV导出成功')
}

function exportExcelData() {
  const data = exportRows()
  if (!data.length) {
    message.warning('暂无可导出数据')
    return
  }
  exportExcel(data, `医嘱任务-${dayjs().format('YYYYMMDD-HHmmss')}.xls`)
  message.success('Excel导出成功')
}

onMounted(() => {
  query.status = parseFilterToStatus(typeof route.query.filter === 'string' ? route.query.filter : undefined)
  load()
  fetchTaskData()
})

watch(
  () => route.query,
  () => {
    query.status = parseFilterToStatus(typeof route.query.filter === 'string' ? route.query.filter : undefined)
    query.pageNo = 1
    pagination.current = 1
    load()
    fetchTaskData()
  }
)
</script>

<style scoped>
:deep(.medical-row-danger > td) {
  background: #fff1f0 !important;
}

:deep(.medical-row-warning > td) {
  background: #fffbe6 !important;
}
</style>
