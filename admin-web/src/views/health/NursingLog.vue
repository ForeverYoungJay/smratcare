<template>
  <PageContainer title="护理日志" subTitle="记录护理执行过程与质量">
    <a-row :gutter="16" class="summary-row">
      <a-col :xs="24" :md="6"><a-card :bordered="false"><a-statistic title="日志总数" :value="summary.totalCount" /></a-card></a-col>
      <a-col :xs="24" :md="6"><a-card :bordered="false"><a-statistic title="待处理" :value="summary.pendingCount" /></a-card></a-col>
      <a-col :xs="24" :md="6"><a-card :bordered="false"><a-statistic title="已完成" :value="summary.doneCount" /></a-card></a-col>
      <a-col :xs="24" :md="6"><a-card :bordered="false"><a-statistic title="已关闭" :value="summary.closedCount" /></a-card></a-col>
    </a-row>

    <a-card :bordered="false" class="summary-row">
      <a-space wrap>
        <a-tag color="blue">关联巡检：{{ summary.linkedInspectionCount }}</a-tag>
        <a-tag v-for="item in summary.logTypeStats" :key="item.name" color="purple">
          {{ logTypeLabel(item.name) }}：{{ item.totalCount }}
        </a-tag>
        <a-tag v-if="!summary.logTypeStats.length" color="default">暂无类型统计</a-tag>
      </a-space>
    </a-card>

    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键词"><a-input v-model:value="query.keyword" placeholder="老人/内容/记录人" allow-clear /></a-form-item>
      <a-form-item label="类型">
        <a-select v-model:value="query.logType" :options="logTypeOptions" allow-clear style="width: 180px" />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" :options="statusOptions" allow-clear style="width: 160px" />
      </a-form-item>
      <a-form-item label="时间">
        <a-range-picker v-model:value="query.logRange" show-time style="width: 320px" />
      </a-form-item>
      <template #extra>
        <a-space>
          <a-button @click="exportCsvData" :loading="exporting">导出CSV</a-button>
          <a-button @click="exportExcelData" :loading="exporting">导出Excel</a-button>
          <a-button type="primary" @click="openCreate">新增日志</a-button>
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
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'logTime'">
          {{ formatDateTime(record.logTime) }}
        </template>
        <template v-else-if="column.key === 'logType'">
          {{ logTypeLabel(record.logType) }}
        </template>
        <template v-else-if="column.key === 'status'">
          <a-tag :color="statusColor(record.status)">{{ statusLabel(record.status) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openEdit(record)">编辑</a-button>
            <a-popconfirm title="确认删除该记录吗？" ok-text="确认" cancel-text="取消" @confirm="remove(record)">
              <a-button type="link" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="editOpen" title="护理日志" @ok="submit" :confirm-loading="saving" width="760px">
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="老人" required>
              <a-select
                v-model:value="form.elderId"
                show-search
                :filter-option="false"
                :options="elderOptions"
                placeholder="请输入姓名搜索"
                @search="searchElders"
                @focus="() => !elderOptions.length && searchElders('')"
                @change="onElderChange"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12"><a-form-item label="日志时间" required><a-date-picker v-model:value="form.logTime" show-time style="width: 100%" /></a-form-item></a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12"><a-form-item label="日志类型" required><a-select v-model:value="form.logType" :options="logTypeOptions" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="记录人"><a-input v-model:value="form.staffName" /></a-form-item></a-col>
        </a-row>
        <a-form-item label="日志内容" required><a-textarea v-model:value="form.content" :rows="4" /></a-form-item>
        <a-row :gutter="16">
          <a-col :span="12"><a-form-item label="状态"><a-select v-model:value="form.status" :options="statusOptions" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="备注"><a-input v-model:value="form.remark" /></a-form-item></a-col>
        </a-row>
        <a-form-item label="来源巡检ID"><a-input-number v-model:value="form.sourceInspectionId" :min="1" style="width: 100%" /></a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { useElderOptions } from '../../composables/useElderOptions'
import { mapHealthExportRows, nursingLogExportColumns } from '../../constants/healthExport'
import { exportCsv, exportExcel } from '../../utils/export'
import {
  getHealthNursingLogPage,
  getHealthNursingLogSummary,
  createHealthNursingLog,
  updateHealthNursingLog,
  deleteHealthNursingLog
} from '../../api/health'
import type { HealthNursingLog, HealthNursingLogSummary, PageResult } from '../../types'

const loading = ref(false)
const exporting = ref(false)
const rows = ref<HealthNursingLog[]>([])
const query = reactive({
  keyword: '',
  logType: '',
  status: '',
  logRange: [] as any[],
  pageNo: 1,
  pageSize: 10
})
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const summary = reactive<HealthNursingLogSummary>({
  totalCount: 0,
  pendingCount: 0,
  doneCount: 0,
  closedCount: 0,
  linkedInspectionCount: 0,
  logTypeStats: []
})

const columns = [
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '时间', dataIndex: 'logTime', key: 'logTime', width: 180 },
  { title: '类型', dataIndex: 'logType', key: 'logType', width: 120 },
  { title: '内容', dataIndex: 'content', key: 'content', width: 260 },
  { title: '记录人', dataIndex: 'staffName', key: 'staffName', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 120 }
]

const editOpen = ref(false)
const saving = ref(false)
const { elderOptions, searchElders, findElderName, ensureSelectedElder } = useElderOptions({ pageSize: 50 })
const form = reactive({
  id: undefined as number | undefined,
  elderId: undefined as number | undefined,
  elderName: '',
  sourceInspectionId: undefined as number | undefined,
  logTime: dayjs(),
  logType: 'ROUTINE',
  content: '',
  staffName: '',
  status: 'PENDING',
  remark: ''
})
const logTypeOptions = [
  { label: '日常护理', value: 'ROUTINE' },
  { label: '巡检跟进', value: 'INSPECTION_FOLLOW_UP' },
  { label: '异常处理', value: 'INCIDENT' }
]
const statusOptions = [
  { label: '待处理', value: 'PENDING' },
  { label: '已完成', value: 'DONE' },
  { label: '已关闭', value: 'CLOSED' }
]

async function fetchData() {
  loading.value = true
  try {
    const params = buildQueryParams()
    const [res, summaryRes] = await Promise.all([
      getHealthNursingLogPage(params) as Promise<PageResult<HealthNursingLog>>,
      getHealthNursingLogSummary(params) as Promise<HealthNursingLogSummary>
    ])
    rows.value = res.list
    pagination.total = res.total || 0
    summary.totalCount = summaryRes.totalCount || 0
    summary.pendingCount = summaryRes.pendingCount || 0
    summary.doneCount = summaryRes.doneCount || 0
    summary.closedCount = summaryRes.closedCount || 0
    summary.linkedInspectionCount = summaryRes.linkedInspectionCount || 0
    summary.logTypeStats = summaryRes.logTypeStats || []
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
  query.keyword = ''
  query.logType = ''
  query.status = ''
  query.logRange = []
  query.pageNo = 1
  query.pageSize = pagination.pageSize
  pagination.current = 1
  fetchData()
}

function openCreate() {
  form.id = undefined
  form.elderId = undefined
  form.elderName = ''
  form.sourceInspectionId = undefined
  form.logTime = dayjs()
  form.logType = 'ROUTINE'
  form.content = ''
  form.staffName = ''
  form.status = 'PENDING'
  form.remark = ''
  editOpen.value = true
}

function openEdit(record: HealthNursingLog) {
  form.id = record.id
  form.elderId = record.elderId
  form.elderName = record.elderName || ''
  ensureSelectedElder(record.elderId, record.elderName)
  form.sourceInspectionId = record.sourceInspectionId
  form.logTime = record.logTime ? dayjs(record.logTime) : dayjs()
  form.logType = record.logType
  form.content = record.content
  form.staffName = record.staffName || ''
  form.status = record.status || 'PENDING'
  form.remark = record.remark || ''
  editOpen.value = true
}

function onElderChange(elderId?: number) {
  form.elderName = findElderName(elderId)
}

async function submit() {
  if (!form.elderId || !form.logType || !form.content) {
    message.error('请补全必填项')
    return
  }
  if (form.logType === 'INSPECTION_FOLLOW_UP' && !form.sourceInspectionId) {
    message.error('巡检跟进日志需关联来源巡检ID')
    return
  }
  if (dayjs(form.logTime).isAfter(dayjs())) {
    message.error('日志时间不能晚于当前时间')
    return
  }
  saving.value = true
  try {
    const payload = {
      elderId: form.elderId,
      elderName: findElderName(form.elderId) || form.elderName,
      sourceInspectionId: form.sourceInspectionId,
      logTime: dayjs(form.logTime).format('YYYY-MM-DD HH:mm:ss'),
      logType: form.logType,
      content: form.content,
      staffName: form.staffName,
      status: form.status,
      remark: form.remark
    }
    if (form.id) {
      await updateHealthNursingLog(form.id, payload)
    } else {
      await createHealthNursingLog(payload)
    }
    message.success('保存成功')
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function remove(record: HealthNursingLog) {
  await deleteHealthNursingLog(record.id)
  message.success('删除成功')
  fetchData()
}

async function exportCsvData() {
  const records = await loadExportRecords()
  if (!records.length) {
    message.warning('暂无可导出数据')
    return
  }
  exportCsv(records, `护理日志-${dayjs().format('YYYYMMDD-HHmmss')}.csv`)
  message.success('CSV导出成功')
}

async function exportExcelData() {
  const records = await loadExportRecords()
  if (!records.length) {
    message.warning('暂无可导出数据')
    return
  }
  exportExcel(records, `护理日志-${dayjs().format('YYYYMMDD-HHmmss')}.xls`)
  message.success('Excel导出成功')
}

function buildQueryParams() {
  const params: Record<string, any> = {
    keyword: query.keyword || undefined,
    logType: query.logType || undefined,
    status: query.status || undefined,
    pageNo: query.pageNo,
    pageSize: query.pageSize
  }
  if (Array.isArray(query.logRange) && query.logRange.length === 2) {
    params.logFrom = dayjs(query.logRange[0]).format('YYYY-MM-DD HH:mm:ss')
    params.logTo = dayjs(query.logRange[1]).format('YYYY-MM-DD HH:mm:ss')
  }
  return params
}

function formatDateTime(value?: string) {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : '-'
}

function logTypeLabel(value?: string) {
  const item = logTypeOptions.find((option) => option.value === value)
  return item?.label || value || '-'
}

function statusLabel(value?: string) {
  const item = statusOptions.find((option) => option.value === value)
  return item?.label || value || '-'
}

function statusColor(value?: string) {
  if (value === 'DONE') return 'green'
  if (value === 'CLOSED') return 'blue'
  return 'orange'
}

function resolveRowClassName(record: HealthNursingLog) {
  if (record.logType === 'INSPECTION_FOLLOW_UP' && !record.sourceInspectionId) return 'health-row-warning'
  if (record.status === 'PENDING' && record.logType === 'INCIDENT') return 'health-row-warning'
  if (record.logTime && dayjs(record.logTime).isAfter(dayjs())) return 'health-row-warning'
  return ''
}

async function loadExportRecords() {
  exporting.value = true
  try {
    const params = buildQueryParams()
    const pageSize = 500
    let pageNo = 1
    let total = 0
    const list: HealthNursingLog[] = []
    do {
      const page = await getHealthNursingLogPage({
        ...params,
        pageNo,
        pageSize
      }) as PageResult<HealthNursingLog>
      total = page.total || 0
      list.push(...(page.list || []))
      pageNo += 1
      if (!page.list || page.list.length < pageSize) break
    } while (list.length < total && pageNo <= 20)
    return mapHealthExportRows(
      list.map((item) => ({
        ...item,
        logTime: formatDateTime(item.logTime),
        logType: logTypeLabel(item.logType),
        status: statusLabel(item.status)
      })),
      nursingLogExportColumns
    )
  } finally {
    exporting.value = false
  }
}

fetchData()
searchElders('')
</script>

<style scoped>
.summary-row {
  margin-bottom: 12px;
}
:deep(.health-row-warning > td) {
  background: #fff7e6 !important;
}
</style>
