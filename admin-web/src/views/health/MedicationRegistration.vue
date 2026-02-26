<template>
  <PageContainer title="用药登记" subTitle="记录每次执行用药">
    <a-row :gutter="16" class="summary-row">
      <a-col :xs="24" :md="6">
        <a-card :bordered="false"><a-statistic title="登记总数" :value="summary.totalCount" /></a-card>
      </a-col>
      <a-col :xs="24" :md="6">
        <a-card :bordered="false"><a-statistic title="今日登记" :value="summary.todayCount" /></a-card>
      </a-col>
      <a-col :xs="24" :md="6">
        <a-card :bordered="false"><a-statistic title="总用药量" :value="summary.totalDosage" :precision="2" /></a-card>
      </a-col>
      <a-col :xs="24" :md="6">
        <a-card :bordered="false"><a-statistic title="待完成任务" :value="summary.pendingTaskCount" /></a-card>
      </a-col>
    </a-row>

    <a-card :bordered="false" class="summary-row">
      <a-space wrap>
        <a-tag color="blue">已联动任务：{{ summary.doneTaskCount }}</a-tag>
        <a-tag v-for="item in summary.nurseStats" :key="item.name" color="purple">
          {{ item.name }}：{{ item.totalCount }}
        </a-tag>
        <a-tag v-if="!summary.nurseStats.length" color="default">暂无护士统计</a-tag>
      </a-space>
    </a-card>

    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="老人/药品/护士" allow-clear />
      </a-form-item>
      <a-form-item label="药品">
        <a-input v-model:value="query.drugName" placeholder="药品名" allow-clear style="width: 160px" />
      </a-form-item>
      <a-form-item label="护士">
        <a-input v-model:value="query.nurseName" placeholder="护士名" allow-clear style="width: 140px" />
      </a-form-item>
      <a-form-item label="时间">
        <a-range-picker v-model:value="query.registerRange" show-time style="width: 320px" />
      </a-form-item>
      <template #extra>
        <a-space>
          <a-button @click="exportCsvData" :loading="exporting">导出CSV</a-button>
          <a-button @click="exportExcelData" :loading="exporting">导出Excel</a-button>
          <a-button type="primary" @click="openCreate">新增登记</a-button>
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
        <template v-if="column.key === 'registerTime'">
          {{ formatDateTime(record.registerTime) }}
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

    <a-modal v-model:open="editOpen" title="用药登记" @ok="submit" :confirm-loading="saving" width="680px">
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
          <a-col :span="12"><a-form-item label="药品名称" required><a-input v-model:value="form.drugName" /></a-form-item></a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12"><a-form-item label="登记时间" required><a-date-picker v-model:value="form.registerTime" show-time style="width: 100%" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="执行护士"><a-input v-model:value="form.nurseName" /></a-form-item></a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12"><a-form-item label="用药量" required><a-input-number v-model:value="form.dosageTaken" :min="0" style="width: 100%" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="单位"><a-input v-model:value="form.unit" /></a-form-item></a-col>
        </a-row>
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
import { mapHealthExportRows, medicationRegistrationExportColumns } from '../../constants/healthExport'
import { exportCsv, exportExcel } from '../../utils/export'
import {
  getHealthMedicationRegistrationPage,
  getHealthMedicationRegistrationSummary,
  createHealthMedicationRegistration,
  updateHealthMedicationRegistration,
  deleteHealthMedicationRegistration
} from '../../api/health'
import type { HealthMedicationRegistration, HealthMedicationRegistrationSummary, PageResult } from '../../types'

const loading = ref(false)
const exporting = ref(false)
const rows = ref<HealthMedicationRegistration[]>([])
const query = reactive({
  keyword: '',
  drugName: '',
  nurseName: '',
  registerRange: [] as any[],
  pageNo: 1,
  pageSize: 10
})
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const summary = reactive<HealthMedicationRegistrationSummary>({
  totalCount: 0,
  todayCount: 0,
  totalDosage: 0,
  doneTaskCount: 0,
  pendingTaskCount: 0,
  nurseStats: []
})

const columns = [
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '药品', dataIndex: 'drugName', key: 'drugName', width: 150 },
  { title: '登记时间', dataIndex: 'registerTime', key: 'registerTime', width: 180 },
  { title: '用药量', dataIndex: 'dosageTaken', key: 'dosageTaken', width: 100 },
  { title: '单位', dataIndex: 'unit', key: 'unit', width: 80 },
  { title: '护士', dataIndex: 'nurseName', key: 'nurseName', width: 120 },
  { title: '备注', dataIndex: 'remark', key: 'remark' },
  { title: '操作', key: 'action', width: 120 }
]

const editOpen = ref(false)
const saving = ref(false)
const { elderOptions, searchElders, findElderName, ensureSelectedElder } = useElderOptions({ pageSize: 50 })
const form = reactive({
  id: undefined as number | undefined,
  elderId: undefined as number | undefined,
  elderName: '',
  drugName: '',
  registerTime: dayjs(),
  dosageTaken: 0 as number | undefined,
  unit: '',
  nurseName: '',
  remark: ''
})

async function fetchData() {
  loading.value = true
  try {
    const params = buildQueryParams()
    const [res, summaryRes] = await Promise.all([
      getHealthMedicationRegistrationPage(params) as Promise<PageResult<HealthMedicationRegistration>>,
      getHealthMedicationRegistrationSummary(params) as Promise<HealthMedicationRegistrationSummary>
    ])
    rows.value = res.list
    pagination.total = res.total || 0
    summary.totalCount = summaryRes.totalCount || 0
    summary.todayCount = summaryRes.todayCount || 0
    summary.totalDosage = summaryRes.totalDosage || 0
    summary.doneTaskCount = summaryRes.doneTaskCount || 0
    summary.pendingTaskCount = summaryRes.pendingTaskCount || 0
    summary.nurseStats = summaryRes.nurseStats || []
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
  query.drugName = ''
  query.nurseName = ''
  query.registerRange = []
  query.pageNo = 1
  query.pageSize = pagination.pageSize
  pagination.current = 1
  fetchData()
}

function openCreate() {
  form.id = undefined
  form.elderId = undefined
  form.elderName = ''
  form.drugName = ''
  form.registerTime = dayjs()
  form.dosageTaken = 0
  form.unit = ''
  form.nurseName = ''
  form.remark = ''
  editOpen.value = true
}

function openEdit(record: HealthMedicationRegistration) {
  form.id = record.id
  form.elderId = record.elderId
  form.elderName = record.elderName || ''
  ensureSelectedElder(record.elderId, record.elderName)
  form.drugName = record.drugName
  form.registerTime = record.registerTime ? dayjs(record.registerTime) : dayjs()
  form.dosageTaken = record.dosageTaken
  form.unit = record.unit || ''
  form.nurseName = record.nurseName || ''
  form.remark = record.remark || ''
  editOpen.value = true
}

function onElderChange(elderId?: number) {
  form.elderName = findElderName(elderId)
}

async function submit() {
  if (!form.elderId || !form.drugName) {
    message.error('请填写老人和药品')
    return
  }
  if (!form.dosageTaken || form.dosageTaken <= 0) {
    message.error('用药量必须大于0')
    return
  }
  if (dayjs(form.registerTime).isAfter(dayjs())) {
    message.error('登记时间不能晚于当前时间')
    return
  }
  saving.value = true
  try {
    const payload = {
      elderId: form.elderId,
      elderName: findElderName(form.elderId) || form.elderName,
      drugName: form.drugName,
      registerTime: dayjs(form.registerTime).format('YYYY-MM-DD HH:mm:ss'),
      dosageTaken: form.dosageTaken,
      unit: form.unit,
      nurseName: form.nurseName || '未填写',
      remark: form.remark
    }
    if (form.id) {
      await updateHealthMedicationRegistration(form.id, payload)
    } else {
      await createHealthMedicationRegistration(payload)
    }
    message.success('保存成功')
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function remove(record: HealthMedicationRegistration) {
  await deleteHealthMedicationRegistration(record.id)
  message.success('删除成功')
  fetchData()
}

async function exportCsvData() {
  const records = await loadExportRecords()
  if (!records.length) {
    message.warning('暂无可导出数据')
    return
  }
  exportCsv(records, `用药登记-${dayjs().format('YYYYMMDD-HHmmss')}.csv`)
  message.success('CSV导出成功')
}

async function exportExcelData() {
  const records = await loadExportRecords()
  if (!records.length) {
    message.warning('暂无可导出数据')
    return
  }
  exportExcel(records, `用药登记-${dayjs().format('YYYYMMDD-HHmmss')}.xls`)
  message.success('Excel导出成功')
}

function buildQueryParams() {
  const params: Record<string, any> = {
    keyword: query.keyword || undefined,
    drugName: query.drugName || undefined,
    nurseName: query.nurseName || undefined,
    pageNo: query.pageNo,
    pageSize: query.pageSize
  }
  if (Array.isArray(query.registerRange) && query.registerRange.length === 2) {
    params.registerFrom = dayjs(query.registerRange[0]).format('YYYY-MM-DD HH:mm:ss')
    params.registerTo = dayjs(query.registerRange[1]).format('YYYY-MM-DD HH:mm:ss')
  }
  return params
}

function formatDateTime(value?: string) {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : '-'
}

function resolveRowClassName(record: HealthMedicationRegistration) {
  if (!record.nurseName) return 'health-row-warning'
  if (Number(record.dosageTaken || 0) > 10) return 'health-row-warning'
  if (record.registerTime && dayjs(record.registerTime).isAfter(dayjs())) return 'health-row-warning'
  return ''
}

async function loadExportRecords() {
  exporting.value = true
  try {
    const params = buildQueryParams()
    const pageSize = 500
    let pageNo = 1
    let total = 0
    const list: HealthMedicationRegistration[] = []
    do {
      const page = await getHealthMedicationRegistrationPage({
        ...params,
        pageNo,
        pageSize
      }) as PageResult<HealthMedicationRegistration>
      total = page.total || 0
      list.push(...(page.list || []))
      pageNo += 1
      if (!page.list || page.list.length < pageSize) break
    } while (list.length < total && pageNo <= 20)
    return mapHealthExportRows(
      list.map((item) => ({
        ...item,
        registerTime: formatDateTime(item.registerTime)
      })),
      medicationRegistrationExportColumns
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
