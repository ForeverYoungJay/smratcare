<template>
  <PageContainer title="健康巡检" subTitle="日常巡检项目与整改闭环">
    <a-row :gutter="16" class="summary-row">
      <a-col :xs="24" :md="6"><a-card :bordered="false"><a-statistic title="巡检总数" :value="summary.totalCount" /></a-card></a-col>
      <a-col :xs="24" :md="6"><a-card :bordered="false"><a-statistic title="异常数" :value="summary.abnormalCount" /></a-card></a-col>
      <a-col :xs="24" :md="6"><a-card :bordered="false"><a-statistic title="跟进中" :value="summary.followingCount" /></a-card></a-col>
      <a-col :xs="24" :md="6"><a-card :bordered="false"><a-statistic title="已关闭" :value="summary.closedCount" /></a-card></a-col>
    </a-row>

    <a-card :bordered="false" class="summary-row">
      <a-space wrap>
        <a-tag color="blue">关联护理日志：{{ summary.linkedLogCount }}</a-tag>
        <a-tag v-for="item in summary.statusStats" :key="item.name" :color="statusColor(item.name)">
          {{ statusLabel(item.name) }}：{{ item.totalCount }}
        </a-tag>
        <a-tag v-if="!summary.statusStats.length" color="default">暂无状态统计</a-tag>
      </a-space>
    </a-card>

    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键词"><a-input v-model:value="query.keyword" placeholder="老人/项目/巡检人" allow-clear /></a-form-item>
      <a-form-item label="状态"><a-select v-model:value="query.status" :options="statusOptions" allow-clear style="width: 180px" /></a-form-item>
      <a-form-item label="日期">
        <a-range-picker v-model:value="query.inspectionRange" style="width: 280px" />
      </a-form-item>
      <template #extra>
        <a-space>
          <a-button @click="exportCsvData" :loading="exporting">导出CSV</a-button>
          <a-button @click="exportExcelData" :loading="exporting">导出Excel</a-button>
          <a-button type="primary" @click="openCreate">新增巡检</a-button>
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
        <template v-if="column.key === 'inspectionDate'">
          {{ formatDate(record.inspectionDate) }}
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

    <a-modal v-model:open="editOpen" title="健康巡检" @ok="submit" :confirm-loading="saving" width="760px">
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
          <a-col :span="12"><a-form-item label="巡检日期" required><a-date-picker v-model:value="form.inspectionDate" style="width: 100%" /></a-form-item></a-col>
        </a-row>
        <a-form-item label="巡检项目" required><a-input v-model:value="form.inspectionItem" /></a-form-item>
        <a-form-item label="结果"><a-textarea v-model:value="form.result" :rows="2" /></a-form-item>
        <a-row :gutter="16">
          <a-col :span="12"><a-form-item label="状态"><a-select v-model:value="form.status" :options="statusOptions" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="巡检人"><a-input v-model:value="form.inspectorName" /></a-form-item></a-col>
        </a-row>
        <a-form-item label="跟进措施"><a-input v-model:value="form.followUpAction" /></a-form-item>
        <a-form-item label="备注"><a-input v-model:value="form.remark" /></a-form-item>
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
import { inspectionExportColumns, mapHealthExportRows } from '../../constants/healthExport'
import { exportCsv, exportExcel } from '../../utils/export'
import {
  getHealthInspectionPage,
  getHealthInspectionSummary,
  createHealthInspection,
  updateHealthInspection,
  deleteHealthInspection
} from '../../api/health'
import type { HealthInspection, HealthInspectionSummary, PageResult } from '../../types'

const loading = ref(false)
const exporting = ref(false)
const rows = ref<HealthInspection[]>([])
const query = reactive({ keyword: '', status: '', inspectionRange: [] as any[], pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const summary = reactive<HealthInspectionSummary>({
  totalCount: 0,
  abnormalCount: 0,
  followingCount: 0,
  closedCount: 0,
  linkedLogCount: 0,
  statusStats: []
})

const columns = [
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '日期', dataIndex: 'inspectionDate', key: 'inspectionDate', width: 120 },
  { title: '项目', dataIndex: 'inspectionItem', key: 'inspectionItem', width: 150 },
  { title: '结果', dataIndex: 'result', key: 'result', width: 200 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '巡检人', dataIndex: 'inspectorName', key: 'inspectorName', width: 120 },
  { title: '跟进措施', dataIndex: 'followUpAction', key: 'followUpAction' },
  { title: '操作', key: 'action', width: 120 }
]

const editOpen = ref(false)
const saving = ref(false)
const { elderOptions, searchElders, findElderName, ensureSelectedElder } = useElderOptions({ pageSize: 50 })
const form = reactive({
  id: undefined as number | undefined,
  elderId: undefined as number | undefined,
  elderName: '',
  inspectionDate: dayjs(),
  inspectionItem: '',
  result: '',
  status: '',
  inspectorName: '',
  followUpAction: '',
  remark: ''
})
const statusOptions = [
  { label: '正常', value: 'NORMAL' },
  { label: '异常', value: 'ABNORMAL' },
  { label: '跟进中', value: 'FOLLOWING' },
  { label: '已关闭', value: 'CLOSED' }
]

async function fetchData() {
  loading.value = true
  try {
    const params = buildQueryParams()
    const [res, summaryRes] = await Promise.all([
      getHealthInspectionPage(params) as Promise<PageResult<HealthInspection>>,
      getHealthInspectionSummary(params) as Promise<HealthInspectionSummary>
    ])
    rows.value = res.list
    pagination.total = res.total || 0
    summary.totalCount = summaryRes.totalCount || 0
    summary.abnormalCount = summaryRes.abnormalCount || 0
    summary.followingCount = summaryRes.followingCount || 0
    summary.closedCount = summaryRes.closedCount || 0
    summary.linkedLogCount = summaryRes.linkedLogCount || 0
    summary.statusStats = summaryRes.statusStats || []
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
  query.status = ''
  query.inspectionRange = []
  query.pageNo = 1
  query.pageSize = pagination.pageSize
  pagination.current = 1
  fetchData()
}

function openCreate() {
  form.id = undefined
  form.elderId = undefined
  form.elderName = ''
  form.inspectionDate = dayjs()
  form.inspectionItem = ''
  form.result = ''
  form.status = 'NORMAL'
  form.inspectorName = ''
  form.followUpAction = ''
  form.remark = ''
  editOpen.value = true
}

function openEdit(record: HealthInspection) {
  form.id = record.id
  form.elderId = record.elderId
  form.elderName = record.elderName || ''
  ensureSelectedElder(record.elderId, record.elderName)
  form.inspectionDate = record.inspectionDate ? dayjs(record.inspectionDate) : dayjs()
  form.inspectionItem = record.inspectionItem
  form.result = record.result || ''
  form.status = record.status || 'NORMAL'
  form.inspectorName = record.inspectorName || ''
  form.followUpAction = record.followUpAction || ''
  form.remark = record.remark || ''
  editOpen.value = true
}

function onElderChange(elderId?: number) {
  form.elderName = findElderName(elderId)
}

async function submit() {
  if (!form.elderId || !form.inspectionItem) {
    message.error('请补全必填项')
    return
  }
  if ((!form.status || form.status === 'NORMAL') && form.result && form.result.includes('异常')) {
    form.status = 'ABNORMAL'
  }
  if (form.status === 'ABNORMAL' && !form.followUpAction) {
    message.error('异常巡检请填写跟进措施')
    return
  }
  saving.value = true
  try {
    const payload = {
      elderId: form.elderId,
      elderName: findElderName(form.elderId) || form.elderName,
      inspectionDate: dayjs(form.inspectionDate).format('YYYY-MM-DD'),
      inspectionItem: form.inspectionItem,
      result: form.result,
      status: form.status,
      inspectorName: form.inspectorName,
      followUpAction: form.followUpAction,
      remark: form.remark
    }
    if (form.id) {
      await updateHealthInspection(form.id, payload)
    } else {
      await createHealthInspection(payload)
    }
    message.success('保存成功')
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function remove(record: HealthInspection) {
  await deleteHealthInspection(record.id)
  message.success('删除成功')
  fetchData()
}

async function exportCsvData() {
  const records = await loadExportRecords()
  if (!records.length) {
    message.warning('暂无可导出数据')
    return
  }
  exportCsv(records, `健康巡检-${dayjs().format('YYYYMMDD-HHmmss')}.csv`)
  message.success('CSV导出成功')
}

async function exportExcelData() {
  const records = await loadExportRecords()
  if (!records.length) {
    message.warning('暂无可导出数据')
    return
  }
  exportExcel(records, `健康巡检-${dayjs().format('YYYYMMDD-HHmmss')}.xls`)
  message.success('Excel导出成功')
}

function buildQueryParams() {
  const params: Record<string, any> = {
    keyword: query.keyword || undefined,
    status: query.status || undefined,
    pageNo: query.pageNo,
    pageSize: query.pageSize
  }
  if (Array.isArray(query.inspectionRange) && query.inspectionRange.length === 2) {
    params.inspectionFrom = dayjs(query.inspectionRange[0]).format('YYYY-MM-DD')
    params.inspectionTo = dayjs(query.inspectionRange[1]).format('YYYY-MM-DD')
  }
  return params
}

function statusLabel(status?: string) {
  const item = statusOptions.find((option) => option.value === status)
  return item?.label || status || '-'
}

function statusColor(status?: string) {
  if (status === 'ABNORMAL') return 'red'
  if (status === 'FOLLOWING') return 'orange'
  if (status === 'CLOSED') return 'green'
  return 'blue'
}

function formatDate(value?: string) {
  return value ? dayjs(value).format('YYYY-MM-DD') : '-'
}

function resolveRowClassName(record: HealthInspection) {
  if (record.status === 'ABNORMAL' || record.status === 'FOLLOWING') return 'health-row-danger'
  return ''
}

async function loadExportRecords() {
  exporting.value = true
  try {
    const params = buildQueryParams()
    const pageSize = 500
    let pageNo = 1
    let total = 0
    const list: HealthInspection[] = []
    do {
      const page = await getHealthInspectionPage({
        ...params,
        pageNo,
        pageSize
      }) as PageResult<HealthInspection>
      total = page.total || 0
      list.push(...(page.list || []))
      pageNo += 1
      if (!page.list || page.list.length < pageSize) break
    } while (list.length < total && pageNo <= 20)
    return mapHealthExportRows(
      list.map((item) => ({
        ...item,
        inspectionDate: formatDate(item.inspectionDate),
        status: statusLabel(item.status)
      })),
      inspectionExportColumns
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
:deep(.health-row-danger > td) {
  background: #fff1f0 !important;
}
</style>
