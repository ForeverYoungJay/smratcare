<template>
  <PageContainer title="工作总结" subTitle="日/周/月/年总结统一管理">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="类型">
        <a-select v-model:value="query.reportType" :options="reportTypeOptions" allow-clear style="width: 160px" />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" :options="statusOptions" allow-clear style="width: 160px" />
      </a-form-item>
      <a-form-item label="日期范围">
        <a-range-picker v-model:value="dateRange" style="width: 260px" />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openCreate">新增总结</a-button>
      </template>
    </SearchForm>

    <DataTable
      rowKey="id"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'reportType'">
          <a-tag color="blue">{{ reportTypeLabel(record.reportType) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'status'">
          <a-tag :color="record.status === 'SUBMITTED' ? 'green' : 'orange'">
            {{ record.status === 'SUBMITTED' ? '已提交' : '草稿' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openEdit(record)">编辑</a-button>
            <a-button type="link" @click="submitReport(record)">提交</a-button>
            <a-button type="link" danger @click="remove(record)">删除</a-button>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="editOpen" :title="form.id ? '编辑总结' : '新增总结'" @ok="submit" :confirm-loading="saving" width="760px">
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="标题" required>
              <a-input v-model:value="form.title" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="总结类型" required>
              <a-select v-model:value="form.reportType" :options="reportTypeOptions" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="总结日期">
              <a-date-picker v-model:value="form.reportDate" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="16">
            <a-form-item label="周期范围">
              <a-range-picker v-model:value="form.periodRange" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="工作概述">
          <a-textarea v-model:value="form.contentSummary" :rows="2" />
        </a-form-item>
        <a-form-item label="已完成工作">
          <a-textarea v-model:value="form.completedWork" :rows="3" />
        </a-form-item>
        <a-form-item label="问题与风险">
          <a-textarea v-model:value="form.riskIssue" :rows="3" />
        </a-form-item>
        <a-form-item label="下一步计划">
          <a-textarea v-model:value="form.nextPlan" :rows="3" />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="总结人">
              <a-input v-model:value="form.reporterName" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="状态">
              <a-select v-model:value="form.status" :options="statusOptions" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import dayjs, { Dayjs } from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { getOaWorkReportPage, createOaWorkReport, updateOaWorkReport, submitOaWorkReport, deleteOaWorkReport } from '../../api/oa'
import type { OaWorkReport, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<OaWorkReport[]>([])
const dateRange = ref<[Dayjs, Dayjs] | undefined>()
const query = reactive({
  reportType: undefined as string | undefined,
  status: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '标题', dataIndex: 'title', key: 'title', width: 240 },
  { title: '类型', dataIndex: 'reportType', key: 'reportType', width: 100 },
  { title: '总结日期', dataIndex: 'reportDate', key: 'reportDate', width: 120 },
  { title: '总结人', dataIndex: 'reporterName', key: 'reporterName', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 180 }
]

const reportTypeOptions = [
  { label: '日总结', value: 'DAY' },
  { label: '周总结', value: 'WEEK' },
  { label: '月总结', value: 'MONTH' },
  { label: '年总结', value: 'YEAR' }
]

const statusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已提交', value: 'SUBMITTED' }
]

const editOpen = ref(false)
const saving = ref(false)
const form = reactive({
  id: undefined as number | undefined,
  title: '',
  reportType: 'DAY',
  reportDate: undefined as Dayjs | undefined,
  periodRange: undefined as [Dayjs, Dayjs] | undefined,
  contentSummary: '',
  completedWork: '',
  riskIssue: '',
  nextPlan: '',
  reporterName: '',
  status: 'DRAFT'
})

function reportTypeLabel(reportType?: string) {
  if (reportType === 'WEEK') return '周总结'
  if (reportType === 'MONTH') return '月总结'
  if (reportType === 'YEAR') return '年总结'
  return '日总结'
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<OaWorkReport> = await getOaWorkReportPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      reportType: query.reportType,
      status: query.status,
      startDate: dateRange.value?.[0] ? dayjs(dateRange.value[0]).format('YYYY-MM-DD') : undefined,
      endDate: dateRange.value?.[1] ? dayjs(dateRange.value[1]).format('YYYY-MM-DD') : undefined
    })
    rows.value = res.list
    pagination.total = res.total || res.list.length
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
  query.reportType = undefined
  query.status = undefined
  query.pageNo = 1
  pagination.current = 1
  dateRange.value = undefined
  fetchData()
}

function openCreate() {
  form.id = undefined
  form.title = ''
  form.reportType = 'DAY'
  form.reportDate = dayjs()
  form.periodRange = undefined
  form.contentSummary = ''
  form.completedWork = ''
  form.riskIssue = ''
  form.nextPlan = ''
  form.reporterName = ''
  form.status = 'DRAFT'
  editOpen.value = true
}

function openEdit(record: OaWorkReport) {
  form.id = record.id
  form.title = record.title
  form.reportType = record.reportType || 'DAY'
  form.reportDate = record.reportDate ? dayjs(record.reportDate) : undefined
  form.periodRange = record.periodStartDate && record.periodEndDate
    ? [dayjs(record.periodStartDate), dayjs(record.periodEndDate)]
    : undefined
  form.contentSummary = record.contentSummary || ''
  form.completedWork = record.completedWork || ''
  form.riskIssue = record.riskIssue || ''
  form.nextPlan = record.nextPlan || ''
  form.reporterName = record.reporterName || ''
  form.status = record.status || 'DRAFT'
  editOpen.value = true
}

async function submit() {
  const payload = {
    title: form.title,
    reportType: form.reportType,
    reportDate: form.reportDate ? dayjs(form.reportDate).format('YYYY-MM-DD') : undefined,
    periodStartDate: form.periodRange?.[0] ? dayjs(form.periodRange[0]).format('YYYY-MM-DD') : undefined,
    periodEndDate: form.periodRange?.[1] ? dayjs(form.periodRange[1]).format('YYYY-MM-DD') : undefined,
    contentSummary: form.contentSummary,
    completedWork: form.completedWork,
    riskIssue: form.riskIssue,
    nextPlan: form.nextPlan,
    reporterName: form.reporterName,
    status: form.status
  }
  saving.value = true
  try {
    if (form.id) {
      await updateOaWorkReport(form.id, payload)
    } else {
      await createOaWorkReport(payload)
    }
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function submitReport(record: OaWorkReport) {
  await submitOaWorkReport(record.id)
  fetchData()
}

async function remove(record: OaWorkReport) {
  await deleteOaWorkReport(record.id)
  fetchData()
}

fetchData()
</script>
