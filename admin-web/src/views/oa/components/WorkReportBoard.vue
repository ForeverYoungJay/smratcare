<template>
  <PageContainer :title="title" :subTitle="subTitle">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="状态">
        <a-select v-model:value="query.status" :options="statusOptions" allow-clear style="width: 160px" />
      </a-form-item>
      <a-form-item label="日期范围">
        <a-range-picker v-model:value="dateRange" style="width: 260px" />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openCreate">新增{{ title }}</a-button>
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
        <template v-if="column.key === 'status'">
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

    <a-modal v-model:open="editOpen" :title="form.id ? `编辑${title}` : `新增${title}`" @ok="submit" :confirm-loading="saving" width="760px">
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="标题" required>
              <a-input v-model:value="form.title" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="总结日期">
              <a-date-picker v-model:value="form.reportDate" style="width: 100%" />
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
import PageContainer from '../../../components/PageContainer.vue'
import SearchForm from '../../../components/SearchForm.vue'
import DataTable from '../../../components/DataTable.vue'
import {
  createOaWorkReport,
  deleteOaWorkReport,
  getDailyWorkReportPage,
  getMonthlyWorkReportPage,
  getWeeklyWorkReportPage,
  getYearlyWorkReportPage,
  submitOaWorkReport,
  updateOaWorkReport
} from '../../../api/oa'
import type { OaWorkReport, PageResult } from '../../../types'

const props = withDefaults(defineProps<{
  title: string
  reportType: 'DAY' | 'WEEK' | 'MONTH' | 'YEAR'
}>(), {
  title: '工作总结',
  reportType: 'DAY'
})

const subTitle = '统一模板，按维度沉淀执行复盘'
const loading = ref(false)
const rows = ref<OaWorkReport[]>([])
const dateRange = ref<[Dayjs, Dayjs] | undefined>()
const query = reactive({ status: undefined as string | undefined, pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '标题', dataIndex: 'title', key: 'title', width: 260 },
  { title: '总结日期', dataIndex: 'reportDate', key: 'reportDate', width: 120 },
  { title: '总结人', dataIndex: 'reporterName', key: 'reporterName', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 180 }
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
  reportDate: undefined as Dayjs | undefined,
  contentSummary: '',
  completedWork: '',
  riskIssue: '',
  nextPlan: '',
  reporterName: '',
  status: 'DRAFT'
})

async function fetchData() {
  loading.value = true
  try {
    const params = {
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      status: query.status,
      startDate: dateRange.value?.[0] ? dayjs(dateRange.value[0]).format('YYYY-MM-DD') : undefined,
      endDate: dateRange.value?.[1] ? dayjs(dateRange.value[1]).format('YYYY-MM-DD') : undefined
    }
    let res: PageResult<OaWorkReport>
    if (props.reportType === 'WEEK') {
      res = await getWeeklyWorkReportPage(params)
    } else if (props.reportType === 'MONTH') {
      res = await getMonthlyWorkReportPage(params)
    } else if (props.reportType === 'YEAR') {
      res = await getYearlyWorkReportPage(params)
    } else {
      res = await getDailyWorkReportPage(params)
    }
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
  query.status = undefined
  query.pageNo = 1
  pagination.current = 1
  dateRange.value = undefined
  fetchData()
}

function openCreate() {
  form.id = undefined
  form.title = ''
  form.reportDate = dayjs()
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
  form.reportDate = record.reportDate ? dayjs(record.reportDate) : undefined
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
    reportType: props.reportType,
    reportDate: form.reportDate ? dayjs(form.reportDate).format('YYYY-MM-DD') : undefined,
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
