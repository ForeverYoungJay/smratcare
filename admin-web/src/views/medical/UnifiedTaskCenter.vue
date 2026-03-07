<template>
  <PageContainer title="统一任务中心" subTitle="医嘱/巡检/护理日志/交接班统一待办闭环">
    <a-card v-if="residentContext.active" :bordered="false" style="margin-bottom: 12px; background: #f0f9ff">
      <a-space wrap>
        <a-tag color="processing">当前长者：{{ residentContext.name }}</a-tag>
        <a-button size="small" @click="clearResidentContext">清除长者上下文</a-button>
      </a-space>
    </a-card>
    <a-alert
      type="info"
      show-icon
      style="margin-bottom: 12px"
      :message="`待办总数 ${summary.totalPending}，高优先级 ${summary.highPriorityCount}，超时风险 ${summary.overdueRiskCount}。`"
    />

    <a-row :gutter="12" style="margin-bottom: 12px">
      <a-col :xs="12" :md="6"><a-card :bordered="false"><a-statistic title="总待办" :value="summary.totalPending" /></a-card></a-col>
      <a-col :xs="12" :md="6"><a-card :bordered="false"><a-statistic title="高优先级" :value="summary.highPriorityCount" /></a-card></a-col>
      <a-col :xs="12" :md="6"><a-card :bordered="false"><a-statistic title="超时风险" :value="summary.overdueRiskCount" /></a-card></a-col>
      <a-col :xs="12" :md="6"><a-card :bordered="false"><a-statistic title="已联动长者" :value="summary.residentCount" /></a-card></a-col>
    </a-row>

    <SearchForm :model="query" @search="applyFilters" @reset="resetFilters">
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="长者/事项/责任人" allow-clear />
      </a-form-item>
      <a-form-item label="模块">
        <a-select v-model:value="query.module" :options="moduleOptions" allow-clear style="width: 150px" />
      </a-form-item>
      <a-form-item label="优先级">
        <a-select v-model:value="query.priority" :options="priorityOptions" allow-clear style="width: 140px" />
      </a-form-item>
      <template #extra>
        <a-space>
          <a-button :loading="loading" @click="loadData">刷新</a-button>
          <a-button :loading="exporting" @click="exportCsvData">导出CSV</a-button>
          <a-button :loading="exporting" @click="exportExcelData">导出Excel</a-button>
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
        <template v-if="column.key === 'module'">
          <a-tag :color="moduleColor(record.module)">{{ moduleLabel(record.module) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'priority'">
          <a-tag :color="priorityColor(record.priority)">{{ priorityLabel(record.priority) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'plannedTime'">
          {{ formatDateTime(record.plannedTime) }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="goDetail(record)">处理</a-button>
            <a-button type="link" @click="goResident(record)">长者视图</a-button>
          </a-space>
        </template>
      </template>
    </DataTable>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { mapMedicalExportRows, unifiedTaskExportColumns } from '../../constants/medicalExport'
import { exportCsv, exportExcel } from '../../utils/export'
import { resolveMedicalError } from './medicalError'
import { getMedicalUnifiedTaskPage } from '../../api/medicalCare'
import type { MedicalUnifiedTaskItem, PageResult } from '../../types'

type UnifiedTaskItem = MedicalUnifiedTaskItem

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const exporting = ref(false)
const rows = ref<UnifiedTaskItem[]>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const query = reactive({
  keyword: '',
  module: undefined as string | undefined,
  priority: undefined as string | undefined
})
const residentContext = computed(() => {
  const residentIdRaw = route.query.residentId ?? route.query.elderId
  const residentName = typeof route.query.residentName === 'string' ? route.query.residentName : ''
  return {
    active: !!residentIdRaw,
    residentId: residentIdRaw ? Number(residentIdRaw) : undefined,
    name: residentName || (residentIdRaw ? `长者#${residentIdRaw}` : '')
  }
})

const moduleOptions = [
  { label: '医嘱执行', value: 'ORDER' },
  { label: '健康巡检', value: 'INSPECTION' },
  { label: '护理日志', value: 'NURSING_LOG' },
  { label: '交接班', value: 'HANDOVER' }
]
const priorityOptions = [
  { label: '高', value: 'HIGH' },
  { label: '中', value: 'MEDIUM' },
  { label: '低', value: 'LOW' }
]

const columns = [
  { title: '模块', key: 'module', width: 120 },
  { title: '长者', dataIndex: 'residentName', key: 'residentName', width: 120 },
  { title: '事项', dataIndex: 'taskTitle', key: 'taskTitle', width: 240 },
  { title: '责任人', dataIndex: 'assignee', key: 'assignee', width: 120 },
  { title: '计划时间', dataIndex: 'plannedTime', key: 'plannedTime', width: 170 },
  { title: '优先级', key: 'priority', width: 100 },
  { title: '操作', key: 'action', width: 140 }
]

const summary = computed(() => {
  const totalPending = Number(pagination.total || 0)
  const highPriorityCount = rows.value.filter((item) => item.priority === 'HIGH').length
  const overdueRiskCount = rows.value.filter((item) => item.plannedTime && dayjs(item.plannedTime).isBefore(dayjs())).length
  const residentCount = new Set(rows.value.map((item) => item.residentId).filter((id) => !!id)).size
  return {
    totalPending,
    highPriorityCount,
    overdueRiskCount,
    residentCount
  }
})

function moduleLabel(module?: string) {
  return moduleOptions.find((item) => item.value === module)?.label || module
}

function moduleColor(module?: string) {
  if (module === 'ORDER') return 'blue'
  if (module === 'INSPECTION') return 'orange'
  if (module === 'NURSING_LOG') return 'purple'
  return 'green'
}

function priorityLabel(priority?: string) {
  return priorityOptions.find((item) => item.value === priority)?.label || priority
}

function priorityColor(priority?: string) {
  if (priority === 'HIGH') return 'red'
  if (priority === 'MEDIUM') return 'orange'
  return 'default'
}

function formatDateTime(value?: string) {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : '-'
}

function resolveRowClassName(record: UnifiedTaskItem) {
  if (record.priority === 'HIGH') return 'task-row-danger'
  if (record.plannedTime && dayjs(record.plannedTime).isBefore(dayjs())) return 'task-row-warning'
  return ''
}

function applyFilters() {
  pagination.current = 1
  loadData()
}

function resetFilters() {
  query.keyword = ''
  query.module = undefined
  query.priority = undefined
  pagination.current = 1
  loadData()
}

function onTableChange(pag: { current?: number; pageSize?: number }) {
  pagination.current = pag.current || 1
  pagination.pageSize = pag.pageSize || 10
  loadData()
}

function exportCsvData() {
  loadExportRecords().then((data) => {
    if (!data.length) {
      message.warning('暂无可导出任务')
      return
    }
    exportCsv(data, `统一任务中心-${dayjs().format('YYYYMMDD-HHmmss')}.csv`)
    message.success('CSV导出成功')
  })
}

function exportExcelData() {
  loadExportRecords().then((data) => {
    if (!data.length) {
      message.warning('暂无可导出任务')
      return
    }
    exportExcel(data, `统一任务中心-${dayjs().format('YYYYMMDD-HHmmss')}.xls`)
    message.success('Excel导出成功')
  })
}

function goDetail(record: UnifiedTaskItem) {
  if (record.module === 'ORDER') {
    router.push({ path: '/medical-care/orders', query: { elderId: record.residentId, filter: 'to_execute' } })
    return
  }
  if (record.module === 'INSPECTION') {
    router.push({ path: '/medical-care/inspection', query: { inspectionId: record.sourceId, residentId: record.residentId } })
    return
  }
  if (record.module === 'NURSING_LOG') {
    router.push({ path: '/medical-care/nursing-log', query: { sourceInspectionId: record.sourceId, residentId: record.residentId } })
    return
  }
  router.push({ path: '/medical-care/handovers', query: { id: record.sourceId, status: record.status || 'DRAFT' } })
}

function goResident(record: UnifiedTaskItem) {
  router.push({
    path: '/medical-care/residents',
    query: {
      residentId: record.residentId ? String(record.residentId) : undefined,
      residentName: record.residentName || undefined,
      from: 'unified-task-center'
    }
  })
}

function clearResidentContext() {
  const nextQuery: Record<string, any> = { ...route.query }
  delete nextQuery.residentId
  delete nextQuery.elderId
  delete nextQuery.residentName
  router.push({ path: route.path, query: nextQuery })
}

async function loadData() {
  loading.value = true
  try {
    const residentIdRaw = route.query.residentId ?? route.query.elderId
    const residentId = residentIdRaw ? Number(residentIdRaw) : undefined
    const page: PageResult<UnifiedTaskItem> = await getMedicalUnifiedTaskPage({
      pageNo: pagination.current,
      pageSize: pagination.pageSize,
      elderId: residentId,
      module: query.module || undefined,
      priority: query.priority || undefined,
      keyword: query.keyword || undefined
    })
    rows.value = page.list || []
    pagination.total = Number(page.total || 0)
  } catch (error) {
    message.error(resolveMedicalError(error, '加载统一任务失败'))
    rows.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

async function loadExportRecords() {
  exporting.value = true
  try {
    const residentIdRaw = route.query.residentId ?? route.query.elderId
    const residentId = residentIdRaw ? Number(residentIdRaw) : undefined
    const pageSize = 500
    let pageNo = 1
    let total = 0
    const list: UnifiedTaskItem[] = []
    do {
      const page: PageResult<UnifiedTaskItem> = await getMedicalUnifiedTaskPage({
        pageNo,
        pageSize,
        elderId: residentId,
        module: query.module || undefined,
        priority: query.priority || undefined,
        keyword: query.keyword || undefined
      })
      total = Number(page.total || 0)
      list.push(...(page.list || []))
      pageNo += 1
      if (!page.list || page.list.length < pageSize) break
    } while (list.length < total && pageNo <= 20)
    return mapMedicalExportRows(
      list.map((item) => ({
        ...item,
        module: moduleLabel(item.module),
        plannedTime: formatDateTime(item.plannedTime),
        priority: priorityLabel(item.priority),
        status: item.status || '-'
      })),
      unifiedTaskExportColumns
    )
  } catch (error) {
    message.error(resolveMedicalError(error, '加载导出数据失败'))
    return []
  } finally {
    exporting.value = false
  }
}

onMounted(loadData)
watch(() => route.query, loadData)
</script>

<style scoped>
:deep(.task-row-danger > td) {
  background: #fff1f0 !important;
}
:deep(.task-row-warning > td) {
  background: #fff7e6 !important;
}
</style>
