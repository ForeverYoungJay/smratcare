<template>
  <PageContainer title="工作总结" subTitle="日/周/月/年总结统一管理">
    <template #extra>
      <a-button type="primary" @click="openCreate">新增总结</a-button>
      <a-button :disabled="!selectedSingleRecord" @click="editSelected">编辑</a-button>
      <a-button :disabled="!canSubmitSingle" @click="submitSelected">提交</a-button>
      <a-button :disabled="!selectedSingleRecord" danger @click="removeSelected">删除</a-button>
      <a-button :disabled="selectedRowKeys.length === 0" @click="batchSubmit">批量提交</a-button>
      <a-button :disabled="selectedRowKeys.length === 0" danger @click="batchRemove">批量删除</a-button>
      <span class="selection-tip">已勾选 {{ selectedRowKeys.length }} 条</span>
    </template>
    <a-radio-group v-if="isManager" v-model:value="viewScope" style="margin-bottom: 12px" @change="onScopeChange">
      <a-radio-button value="mine">我的总结</a-radio-button>
      <a-radio-button value="team">团队总结（点评）</a-radio-button>
    </a-radio-group>
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

    </SearchForm>

    <DataTable
      :filter-model="query"
      @clear-filters="fetchData"
      rowKey="id"
      :row-selection="rowSelection"
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
          <a-tag :color="record.reviewedAt ? 'purple' : record.status === 'SUBMITTED' ? 'green' : 'orange'">
            {{ record.reviewedAt ? '已点评' : record.status === 'SUBMITTED' ? '已提交' : '草稿' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'review'">
          <template v-if="record.reviewedAt">
            <a-rate v-if="record.reviewScore" :value="record.reviewScore" disabled style="font-size: 12px" />
            <div class="review-text">{{ record.reviewerName }}：{{ record.reviewComment }}</div>
          </template>
          <a v-else-if="canReview(record)" @click="openReview(record)">点评</a>
          <span v-else class="review-text">-</span>
        </template>
      </template>
    </DataTable>

    <a-modal :mask-closable="false" v-model:open="editOpen" :title="form.id ? '编辑总结' : '新增总结'" @ok="submit" :confirm-loading="saving" width="760px">
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
          <template #extra>
            <a-button size="small" :loading="drafting" @click="fillDraft">用本期工作数据生成草稿</a-button>
            <span class="review-text">只统计本人数据，填入后可修改，不会自动提交</span>
          </template>
          <a-textarea v-model:value="form.completedWork" :rows="4" />
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

    <a-modal v-model:open="reviewOpen" :title="`点评 · ${reviewTarget?.title || ''}`" :confirm-loading="saving" @ok="submitReview">
      <p class="review-text">{{ reviewTarget?.reporterName }} · {{ reviewTarget?.completedWork || reviewTarget?.contentSummary || '' }}</p>
      <a-form layout="vertical">
        <a-form-item label="评分"><a-rate v-model:value="reviewForm.score" /></a-form-item>
        <a-form-item label="点评意见" required><a-textarea v-model:value="reviewForm.comment" :rows="3" /></a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import dayjs, { Dayjs } from 'dayjs'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { getOaWorkReportPage, createOaWorkReport, updateOaWorkReport, submitOaWorkReport, deleteOaWorkReport, getOaWorkReportDraftStats, reviewOaWorkReport } from '../../api/oa'
import { useUserStore } from '../../stores/user'
import { hasMinisterOrHigher } from '../../utils/roleAccess'
import { defaultReportPeriod, draftFromStats } from './myOffice'
import type { OaWorkReport, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<OaWorkReport[]>([])
const selectedRowKeys = ref<string[]>([])
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
  { title: '主管点评', key: 'review', width: 240 }
]
const userStore = useUserStore()
const myStaffId = String(userStore.staffInfo?.id || '')
const isManager = hasMinisterOrHigher(userStore.roles || [])
const viewScope = ref<'mine' | 'team'>('mine')
const drafting = ref(false)
const reviewOpen = ref(false)
const reviewTarget = ref<OaWorkReport>()
const reviewForm = reactive({ comment: '', score: 4 })

const canReview = (record: OaWorkReport) => viewScope.value === 'team' && record.status === 'SUBMITTED' && String(record.reporterId) !== myStaffId

function onScopeChange() {
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openReview(record: OaWorkReport) {
  reviewTarget.value = record
  reviewForm.comment = ''
  reviewForm.score = 4
  reviewOpen.value = true
}

async function submitReview() {
  if (!reviewForm.comment.trim()) return message.warning('请填写点评意见')
  saving.value = true
  try {
    await reviewOaWorkReport(reviewTarget.value!.id, { comment: reviewForm.comment.trim(), score: reviewForm.score || undefined })
    message.success('已点评')
    reviewOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

/** 用本人本期数据生成草稿：只追加到「已完成工作」，由员工核对后自己提交 */
async function fillDraft() {
  const [start, end] = form.periodRange?.[0] && form.periodRange?.[1]
    ? [dayjs(form.periodRange[0]).format('YYYY-MM-DD'), dayjs(form.periodRange[1]).format('YYYY-MM-DD')]
    : defaultReportPeriod(form.reportType)
  drafting.value = true
  try {
    const stats = await getOaWorkReportDraftStats({ startDate: start, endDate: end })
    const draft = draftFromStats(stats, start, end)
    form.completedWork = form.completedWork ? `${form.completedWork}\n${draft}` : draft
    if (!form.periodRange) form.periodRange = [dayjs(start), dayjs(end)]
  } finally {
    drafting.value = false
  }
}

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
  id: undefined as string | number | undefined,
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
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: (string | number)[]) => {
    selectedRowKeys.value = keys.map((item) => String(item))
  }
}))
const selectedRecords = computed(() => rows.value.filter((item) => selectedRowKeys.value.includes(String(item.id))))
const selectedSingleRecord = computed(() => (selectedRecords.value.length === 1 ? selectedRecords.value[0] : null))
const canSubmitSingle = computed(() => !!selectedSingleRecord.value && selectedSingleRecord.value.status !== 'SUBMITTED')

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
      mineOnly: viewScope.value === 'mine',
      startDate: dateRange.value?.[0] ? dayjs(dateRange.value[0]).format('YYYY-MM-DD') : undefined,
      endDate: dateRange.value?.[1] ? dayjs(dateRange.value[1]).format('YYYY-MM-DD') : undefined
    })
    rows.value = (res.list || []).map((item) => ({
      ...item,
      id: String(item.id)
    }))
    pagination.total = res.total || res.list.length
    selectedRowKeys.value = []
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
  if (saving.value) return
  saving.value = true
  try {
    if (form.id) {
      await updateOaWorkReport(String(form.id), payload)
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
  await submitOaWorkReport(String(record.id))
  fetchData()
}

async function remove(record: OaWorkReport) {
  await deleteOaWorkReport(String(record.id))
  fetchData()
}

function requireSingleSelection(action: string) {
  if (!selectedSingleRecord.value) {
    message.info(`请先勾选 1 条总结后再${action}`)
    return null
  }
  return selectedSingleRecord.value
}

function editSelected() {
  const record = requireSingleSelection('编辑')
  if (!record) return
  openEdit(record)
}

async function submitSelected() {
  const record = requireSingleSelection('提交')
  if (!record) return
  if (record.status === 'SUBMITTED') {
    message.info('当前总结已提交')
    return
  }
  await submitReport(record)
}

async function removeSelected() {
  const record = requireSingleSelection('删除')
  if (!record) return
  await remove(record)
}

async function batchSubmit() {
  if (selectedRowKeys.value.length === 0) return
  const validRecords = selectedRecords.value.filter((item) => item.status !== 'SUBMITTED')
  if (validRecords.length === 0) {
    message.info('所选记录均为已提交，无需重复提交')
    return
  }
  await Promise.all(validRecords.map((item) => submitOaWorkReport(String(item.id))))
  message.success(`批量提交完成，共处理 ${validRecords.length} 条`)
  fetchData()
}

async function batchRemove() {
  if (selectedRowKeys.value.length === 0) return
  await Promise.all(selectedRecords.value.map((item) => deleteOaWorkReport(String(item.id))))
  message.success(`批量删除完成，共处理 ${selectedRecords.value.length} 条`)
  fetchData()
}

fetchData()
</script>

<style scoped>
.review-text { color: #6b7a74; font-size: 12px; }
.selection-tip {
  color: var(--muted);
  font-size: 12px;
}
</style>
