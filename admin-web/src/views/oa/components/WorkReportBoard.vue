<template>
  <PageContainer :title="title" :subTitle="subTitle">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" placeholder="标题/概述/完成工作/风险/计划/总结人" allow-clear style="width: 280px" />
      </a-form-item>
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

    <StatefulBlock :loading="summaryLoading" :error="summaryError" :empty="false" @retry="fetchData">
      <a-row :gutter="[12, 12]" style="margin-bottom: 12px">
        <a-col :xs="12" :lg="4"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="总数" :value="summary.totalCount || 0" /></a-card></a-col>
        <a-col :xs="12" :lg="4"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="已提交" :value="summary.submittedCount || 0" /></a-card></a-col>
        <a-col :xs="12" :lg="4"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="草稿" :value="summary.draftCount || 0" /></a-card></a-col>
        <a-col :xs="12" :lg="4"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="今日提交" :value="summary.todaySubmittedCount || 0" /></a-card></a-col>
        <a-col :xs="12" :lg="4"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="本周提交" :value="summary.weekSubmittedCount || 0" /></a-card></a-col>
        <a-col :xs="12" :lg="4"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="缺少概述" :value="summary.missingSummaryCount || 0" /></a-card></a-col>
      </a-row>
      <a-alert
        v-if="(summary.draftCount || 0) > 0 || (summary.missingSummaryCount || 0) > 0"
        type="warning"
        show-icon
        style="margin-bottom: 12px"
        :message="`提交提醒：草稿 ${summary.draftCount || 0} 条，内容缺少概述 ${summary.missingSummaryCount || 0} 条。`"
      />
    </StatefulBlock>

    <StatefulBlock
      :loading="loading"
      :error="tableError"
      :empty="!loading && !tableError && rows.length === 0"
      empty-text="暂无总结记录"
      @retry="fetchData"
    >
      <DataTable
        rowKey="id"
        :columns="columns"
        :data-source="rows"
        :loading="false"
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
              <a-button type="link" :disabled="record.status === 'SUBMITTED'" @click="submitReport(record)">提交</a-button>
              <a-button type="link" danger @click="remove(record)">删除</a-button>
            </a-space>
          </template>
        </template>
      </DataTable>
    </StatefulBlock>

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
import StatefulBlock from '../../../components/StatefulBlock.vue'
import {
  createOaWorkReport,
  deleteOaWorkReport,
  getDailyWorkReportPage,
  getMonthlyWorkReportPage,
  getOaWorkReportSummary,
  getWeeklyWorkReportPage,
  getYearlyWorkReportPage,
  submitOaWorkReport,
  updateOaWorkReport
} from '../../../api/oa'
import type { OaWorkReport, OaWorkReportSummary, PageResult } from '../../../types'

const props = withDefaults(defineProps<{
  title: string
  reportType: 'DAY' | 'WEEK' | 'MONTH' | 'YEAR'
}>(), {
  title: '工作总结',
  reportType: 'DAY'
})

const subTitle = '统一模板，按维度沉淀执行复盘'
const loading = ref(false)
const tableError = ref('')
const summaryLoading = ref(false)
const summaryError = ref('')
const rows = ref<OaWorkReport[]>([])
const dateRange = ref<[Dayjs, Dayjs] | undefined>()
const query = reactive({ keyword: '', status: undefined as string | undefined, pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const summary = reactive<OaWorkReportSummary>({
  totalCount: 0,
  draftCount: 0,
  submittedCount: 0,
  todaySubmittedCount: 0,
  weekSubmittedCount: 0,
  monthSubmittedCount: 0,
  dayTypeCount: 0,
  weekTypeCount: 0,
  monthTypeCount: 0,
  yearTypeCount: 0,
  missingSummaryCount: 0
})

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
  summaryLoading.value = true
  tableError.value = ''
  summaryError.value = ''
  try {
    const params = {
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined,
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
    const summaryRes = await getOaWorkReportSummary({
      reportType: props.reportType,
      status: query.status,
      keyword: query.keyword || undefined,
      startDate: params.startDate,
      endDate: params.endDate
    })
    Object.assign(summary, summaryRes || {})
  } catch (error: any) {
    const text = error?.message || '加载失败，请稍后重试'
    tableError.value = text
    summaryError.value = text
  } finally {
    loading.value = false
    summaryLoading.value = false
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
