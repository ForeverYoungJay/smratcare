<template>
  <PageContainer :title="pageTitle" :subTitle="`考勤与班组 / ${pageTitle}`">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" placeholder="标题/申请人/备注" allow-clear />
      </a-form-item>
      <a-form-item label="审批状态">
        <a-select v-model:value="query.status" allow-clear style="width: 180px" :options="statusOptions" />
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
    />
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { exportCsv, exportExcel } from '../../utils/export'
import { getHrAttendanceLeavePage, getHrAttendanceOvertimePage, getHrAttendanceShiftChangePage } from '../../api/hr'
import type { HrGenericApprovalItem, PageResult } from '../../types'
import { HR_APPROVAL_EXPORT_FIELDS, mapByDict } from './hrExportFields'

const route = useRoute()

const query = reactive({
  keyword: undefined as string | undefined,
  status: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})

const statusOptions = [
  { label: '待审批', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' }
]

const columns = [
  { title: '标题', dataIndex: 'title', key: 'title', width: 260 },
  { title: '审批类型', dataIndex: 'approvalType', key: 'approvalType', width: 120 },
  { title: '场景', dataIndex: 'scene', key: 'scene', width: 140 },
  { title: '申请人', dataIndex: 'applicantName', key: 'applicantName', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '开始时间', dataIndex: 'startTime', key: 'startTime', width: 180 },
  { title: '结束时间', dataIndex: 'endTime', key: 'endTime', width: 180 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '备注', dataIndex: 'remark', key: 'remark', width: 220 }
]

const rows = ref<HrGenericApprovalItem[]>([])
const loading = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const approvalScene = computed<'leave' | 'shift-change' | 'overtime'>(() => {
  const scene = String(route.query.scene || '').trim()
  if (scene === 'shift-change') return 'shift-change'
  if (scene === 'overtime') return 'overtime'
  if (scene === 'leave') return 'leave'
  if (route.name === 'HrAttendanceShiftChange') return 'shift-change'
  if (route.name === 'HrAttendanceOvertime') return 'overtime'
  return 'leave'
})

const pageTitle = computed(() => {
  if (approvalScene.value === 'shift-change') return '调班申请'
  if (approvalScene.value === 'overtime') return '加班申请'
  return '请假审批'
})

const fetcher = computed(() => {
  if (approvalScene.value === 'shift-change') return getHrAttendanceShiftChangePage
  if (approvalScene.value === 'overtime') return getHrAttendanceOvertimePage
  return getHrAttendanceLeavePage
})

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<HrGenericApprovalItem> = await fetcher.value({ ...query })
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
  query.pageNo = 1
  query.pageSize = 10
  pagination.current = 1
  pagination.pageSize = 10
  fetchData()
}

function rowClassName(record: HrGenericApprovalItem) {
  if (record.status === 'REJECTED') return 'hr-row-danger'
  if (record.status === 'PENDING') return 'hr-row-warning'
  return ''
}

function onExportCsv() {
  exportCsv(mapByDict(rows.value as Record<string, any>[], HR_APPROVAL_EXPORT_FIELDS), `${pageTitle.value}-当前筛选`)
}

function onExportExcel() {
  exportExcel(mapByDict(rows.value as Record<string, any>[], HR_APPROVAL_EXPORT_FIELDS), `${pageTitle.value}-当前筛选`)
}

onMounted(fetchData)

watch(
  () => route.fullPath,
  () => {
    onReset()
  }
)
</script>

<style scoped>
:deep(.hr-row-danger) {
  background: #fff1f0 !important;
}

:deep(.hr-row-warning) {
  background: #fffbe6 !important;
}
</style>
