<template>
  <PageContainer title="AI健康评估报告" subTitle="报告查看、发布与一键生成任务">
    <SearchForm :model="query" @search="loadReports" @reset="onReset">
      <a-form-item label="报告类型">
        <a-select v-model:value="query.type" allow-clear style="width: 160px" :options="typeOptions" />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 140px" :options="statusOptions" />
      </a-form-item>
      <a-form-item label="日期范围">
        <a-range-picker v-model:value="query.range" />
      </a-form-item>
      <template #extra>
        <a-space>
          <a-button @click="exportCsvData">导出CSV</a-button>
          <a-button @click="exportExcelData">导出Excel</a-button>
          <a-button type="primary" @click="openCreate">生成报告</a-button>
        </a-space>
      </template>
    </SearchForm>

    <DataTable rowKey="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" :row-class-name="resolveRowClassName" @change="onTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'typeLabel'">
          {{ typeLabel(record.type) }}
        </template>
        <template v-else-if="column.key === 'status'">
          <a-tag :color="statusColor(record.status)">{{ statusLabel(record.status) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openDetail(record)">详情</a-button>
            <a-button type="link" @click="publish(record)" :disabled="record.status === 'PUBLISHED'">发布</a-button>
            <a-button type="link" @click="generateTasks(record)">生成任务</a-button>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-drawer v-model:open="detailOpen" title="报告详情" width="760">
      <a-descriptions bordered :column="2" size="small">
        <a-descriptions-item label="报告类型">{{ typeLabel(current?.type) }}</a-descriptions-item>
        <a-descriptions-item label="状态">{{ statusLabel(current?.status) }}</a-descriptions-item>
        <a-descriptions-item label="关联长者">{{ current?.elderName || '机构级报告' }}</a-descriptions-item>
        <a-descriptions-item label="数据范围">{{ current?.rangeText }}</a-descriptions-item>
        <a-descriptions-item label="高风险提示">{{ current?.highRiskCount }}</a-descriptions-item>
      </a-descriptions>
      <a-card title="建议行动" class="card-elevated" style="margin-top: 12px" :bordered="false">
        <a-space wrap>
          <a-button type="primary" @click="go(generatedRoutes.inspectionRoute || '/medical-care/inspection?filter=generated_from_ai')">生成巡检计划</a-button>
          <a-button @click="go(generatedRoutes.medicalTodoRoute || '/medical-care/center?filter=generated_from_ai')">生成医护随访任务</a-button>
          <a-button @click="go(generatedRoutes.nursingTaskRoute || '/medical-care/care-task-board?filter=generated_from_ai')">生成护理干预任务</a-button>
          <a-button :disabled="!current?.elderId" @click="goFamilyCommunication">发起家属沟通记录</a-button>
        </a-space>
      </a-card>
    </a-drawer>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { exportCsv, exportExcel } from '../../utils/export'
import { aiHealthReportExportColumns, mapMedicalExportRows } from '../../constants/medicalExport'
import { resolveMedicalError } from './medicalError'
import {
  generateMedicalAiReport,
  generateMedicalAiReportTasks,
  getMedicalAiReportPage,
  publishMedicalAiReport
} from '../../api/medicalCare'
import type { MedicalAiReportItem, PageResult } from '../../types'

type ReportStatus = 'GENERATING' | 'GENERATED' | 'PUBLISHED'
type ReportType = 'WEEKLY' | 'MONTHLY' | 'CVD' | 'CHRONIC'

const router = useRouter()
const loading = ref(false)
const detailOpen = ref(false)
const current = ref<MedicalAiReportItem>()
const generatedRoutes = reactive({
  inspectionRoute: '',
  medicalTodoRoute: '',
  nursingTaskRoute: ''
})
const query = reactive({
  type: undefined as ReportType | undefined,
  status: undefined as ReportStatus | undefined,
  range: [] as any[],
  pageNo: 1,
  pageSize: 10
})
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '报告类型', key: 'typeLabel', width: 120 },
  { title: '状态', key: 'status', width: 120 },
  { title: '数据范围', dataIndex: 'rangeText', key: 'rangeText', width: 220 },
  { title: '风险摘要', dataIndex: 'highRiskCount', key: 'highRiskCount', width: 120 },
  { title: '生成时间', dataIndex: 'createdAt', key: 'createdAt', width: 180 },
  { title: '操作', key: 'action', width: 220 }
]

const typeOptions = [
  { label: '周报', value: 'WEEKLY' },
  { label: '月报', value: 'MONTHLY' },
  { label: '专项-心血管', value: 'CVD' },
  { label: '专项-慢病', value: 'CHRONIC' }
]
const statusOptions = [
  { label: '生成中', value: 'GENERATING' },
  { label: '已生成', value: 'GENERATED' },
  { label: '已发布', value: 'PUBLISHED' }
]

const rows = ref<MedicalAiReportItem[]>([])

function statusLabel(value?: string) {
  if (value === 'GENERATING') return '生成中'
  if (value === 'GENERATED') return '已生成'
  if (value === 'PUBLISHED') return '已发布'
  return '-'
}

function statusColor(value?: string) {
  if (value === 'GENERATING') return 'processing'
  if (value === 'GENERATED') return 'blue'
  if (value === 'PUBLISHED') return 'green'
  return 'default'
}

function typeLabel(type?: string) {
  const value = (type || '').toUpperCase()
  if (value === 'WEEKLY') return '周报'
  if (value === 'MONTHLY') return '月报'
  if (value === 'CVD') return '专项-心血管'
  if (value === 'CHRONIC') return '专项-慢病'
  return value || '-'
}

async function loadReports() {
  loading.value = true
  try {
    const params: any = {
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      type: query.type,
      status: query.status
    }
    if (Array.isArray(query.range) && query.range.length === 2) {
      params.dateFrom = dayjs(query.range[0]).format('YYYY-MM-DD')
      params.dateTo = dayjs(query.range[1]).format('YYYY-MM-DD')
    }
    const res: PageResult<MedicalAiReportItem> = await getMedicalAiReportPage(params)
    rows.value = res.list || []
    pagination.total = res.total || 0
  } catch (error) {
    message.error(resolveMedicalError(error, '加载报告失败'))
    rows.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

function onReset() {
  query.type = undefined
  query.status = undefined
  query.range = []
  query.pageNo = 1
  query.pageSize = pagination.pageSize
  pagination.current = 1
  loadReports()
}

async function openCreate() {
  const payload: any = { type: query.type || 'WEEKLY' }
  if (Array.isArray(query.range) && query.range.length === 2) {
    payload.dateFrom = dayjs(query.range[0]).format('YYYY-MM-DD')
    payload.dateTo = dayjs(query.range[1]).format('YYYY-MM-DD')
  }
  try {
    await generateMedicalAiReport(payload)
    message.success('已创建生成任务')
    loadReports()
  } catch (error) {
    message.error(resolveMedicalError(error, '生成报告失败'))
  }
}

function openDetail(record: MedicalAiReportItem) {
  current.value = record
  generatedRoutes.inspectionRoute = ''
  generatedRoutes.medicalTodoRoute = ''
  generatedRoutes.nursingTaskRoute = ''
  detailOpen.value = true
}

async function publish(record: MedicalAiReportItem) {
  try {
    await publishMedicalAiReport(record.id)
    message.success('报告已发布并可在长者总览查看摘要')
    loadReports()
  } catch (error) {
    message.error(resolveMedicalError(error, '发布失败'))
  }
}

async function generateTasks(record: MedicalAiReportItem) {
  try {
    const resp = await generateMedicalAiReportTasks(record.id)
    current.value = record
    detailOpen.value = true
    generatedRoutes.inspectionRoute = resp?.inspectionRoute || ''
    generatedRoutes.medicalTodoRoute = resp?.medicalTodoRoute || ''
    generatedRoutes.nursingTaskRoute = resp?.nursingTaskRoute || ''
    if (resp?.inspectionRoute || resp?.medicalTodoRoute || resp?.nursingTaskRoute) {
      message.success('任务路由已生成，可在详情面板直接跳转')
    }
  } catch (error) {
    message.error(resolveMedicalError(error, '生成任务失败'))
  }
}

function onTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  loadReports()
}

function resolveRowClassName(record: MedicalAiReportItem) {
  if (record.status === 'GENERATING') return 'ai-row-warning'
  return ''
}

function exportRows() {
  return mapMedicalExportRows(
    rows.value.map((item) => ({
      ...item,
      type: typeLabel(item.type),
      status: statusLabel(item.status as ReportStatus),
      rangeText: item.rangeText || `${item.dateFrom || '-'} ~ ${item.dateTo || '-'}`
    })),
    aiHealthReportExportColumns
  )
}

function exportCsvData() {
  const data = exportRows()
  if (!data.length) {
    message.warning('暂无可导出数据')
    return
  }
  exportCsv(data, `AI健康评估报告-${dayjs().format('YYYYMMDD-HHmmss')}.csv`)
  message.success('CSV导出成功')
}

function exportExcelData() {
  const data = exportRows()
  if (!data.length) {
    message.warning('暂无可导出数据')
    return
  }
  exportExcel(data, `AI健康评估报告-${dayjs().format('YYYYMMDD-HHmmss')}.xls`)
  message.success('Excel导出成功')
}

function go(path: string) {
  router.push(path)
}

function goFamilyCommunication() {
  if (!current.value?.elderId) {
    message.warning('当前报告为机构级汇总报告，暂不支持直接发起家属沟通记录')
    return
  }
  router.push({
    path: '/elder/resident-360',
    query: {
      residentId: current.value.elderId,
      residentName: current.value.elderName || undefined,
      from: 'ai_report'
    }
  })
}

loadReports()
</script>

<style scoped>
:deep(.ai-row-warning) {
  background: #fffbe6 !important;
}
</style>
