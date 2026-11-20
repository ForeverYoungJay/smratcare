<template>
  <PageContainer title="工作总结" sub-title="日报、周报、月报：撰写草稿、提交、主管点评；点评后不能再修改">
    <div class="of-page">
      <a-radio-group v-if="isManager" v-model:value="viewScope" button-style="solid" @change="onScopeChange">
        <a-radio-button value="mine">我的总结</a-radio-button>
        <a-radio-button value="team">团队总结（点评）</a-radio-button>
      </a-radio-group>

      <template v-if="viewScope === 'mine'">
        <div class="of-kpis">
          <button v-for="card in statusCards" :key="card.label" type="button" class="of-kpi" @click="card.action()">
            <span class="of-kpi__icon" :class="card.tone"><component :is="card.icon" /></span>
            <div class="of-kpi__body"><span>{{ card.label }}</span><b>{{ card.value }}</b><em>{{ card.hint }}</em></div>
          </button>
        </div>

        <div class="wr-grid">
          <article class="of-card">
            <div class="of-card__head"><h3><FileTextOutlined />我的总结</h3></div>
            <a-button type="primary" block @click="startNew('WEEK')"><PlusOutlined /> 新建工作总结</a-button>
            <a-radio-group v-model:value="listFilter" size="small" class="wr-filter">
              <a-radio-button value="ALL">全部</a-radio-button>
              <a-radio-button value="DRAFT">草稿</a-radio-button>
              <a-radio-button value="SUBMITTED">已提交</a-radio-button>
              <a-radio-button value="REVIEWED">已点评</a-radio-button>
            </a-radio-group>
            <a-spin :spinning="loading">
              <div v-if="!pagedList.length" class="of-empty">还没有总结，点击「新建工作总结」开始撰写</div>
              <button v-for="r in pagedList" :key="String(r.id)" type="button" class="wr-item" :class="{ 'wr-item--active': String(r.id) === String(form.id) }" @click="openReport(r)">
                <FileTextOutlined />
                <div class="of-row__main"><b>{{ r.title }}</b><small>{{ periodText(r) }}</small></div>
                <span class="of-pill" :class="statusOf(r).tone">{{ statusOf(r).label }}</span>
              </button>
            </a-spin>
            <a-pagination v-if="filteredList.length > PAGE" v-model:current="listPage" size="small" :page-size="PAGE" :total="filteredList.length" class="wr-pager" />
          </article>

          <article class="of-card">
            <div class="of-card__head">
              <h3><EditOutlined />{{ form.id ? (locked ? '查看工作总结' : '编辑工作总结') : '撰写工作总结' }}</h3>
              <span v-if="locked" class="of-muted">{{ form.reviewedAt ? '已点评，不能修改' : '已提交，等待主管点评' }}</span>
            </div>
            <a-form layout="vertical" :disabled="locked">
              <a-row :gutter="12">
                <a-col :xs="24" :md="14">
                  <a-form-item label="总结周期" required><a-range-picker v-model:value="form.range" style="width: 100%" /></a-form-item>
                </a-col>
                <a-col :xs="24" :md="10">
                  <a-form-item label="总结类型" required><a-select v-model:value="form.reportType" :options="reportTypeOptions" @change="onTypeChange" /></a-form-item>
                </a-col>
              </a-row>
              <a-form-item label="标题" required><a-input v-model:value="form.title" /></a-form-item>
              <a-form-item label="一、本期完成工作"><a-textarea v-model:value="form.completedWork" :rows="4" placeholder="完成的服务、任务和亮点" /></a-form-item>
              <a-form-item label="二、问题与改进"><a-textarea v-model:value="form.riskIssue" :rows="3" placeholder="存在的问题与改进措施" /></a-form-item>
              <a-form-item label="三、下期计划"><a-textarea v-model:value="form.nextPlan" :rows="3" /></a-form-item>
              <a-form-item label="四、心得体会"><a-textarea v-model:value="form.contentSummary" :rows="2" /></a-form-item>
            </a-form>
            <div v-if="form.reviewedAt" class="wr-review">
              <b>{{ form.reviewerName }} 点评</b><a-rate v-if="form.reviewScore" :value="form.reviewScore" disabled />
              <p>{{ form.reviewComment }}</p>
            </div>
            <div v-if="!locked" class="wr-actions">
              <a-button v-if="form.id" danger @click="removeCurrent">删除</a-button>
              <span class="of-muted">提交前需本人确认，系统不会自动提交</span>
              <a-button :loading="saving" @click="save(false)">保存草稿</a-button>
              <a-button type="primary" :loading="saving" @click="save(true)"><SendOutlined /> 提交总结</a-button>
            </div>
          </article>

          <article class="of-card">
            <div class="of-card__head"><h3><BarChartOutlined />本期工作数据</h3><small class="of-muted">{{ statsRange[0] }} ~ {{ statsRange[1] }}</small></div>
            <div v-for="item in statItems" :key="item.label" class="wr-stat">
              <span class="of-kpi__icon" :class="item.tone"><component :is="item.icon" /></span>
              <div class="of-row__main"><b>{{ item.label }}</b><small>{{ item.hint }}</small></div>
              <strong>{{ item.value }}<small> {{ item.unit }}</small></strong>
            </div>
            <a-button block :disabled="locked" :loading="drafting" @click="insertDraft">把这些数据插入「本期完成工作」</a-button>
            <p class="of-muted">只统计本人数据，插入后可修改。</p>
          </article>
        </div>

        <article class="of-card">
          <div class="of-card__head"><h3><CommentOutlined />历史总结与领导点评</h3></div>
          <a-table size="small" row-key="id" :data-source="reviewedList" :columns="historyColumns" :pagination="{ pageSize: 5, hideOnSinglePage: true }">
            <template #emptyText><div class="of-empty">还没有被点评的总结</div></template>
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'period'">{{ periodText(record) }}</template>
              <template v-else-if="column.key === 'type'"><span class="of-pill tone-green">{{ typeLabel(record.reportType) }}</span></template>
              <template v-else-if="column.key === 'review'"><b>{{ record.reviewerName }}</b> <small class="of-muted">{{ fmt(record.reviewedAt) }}</small><div class="wr-comment">{{ record.reviewComment }}</div></template>
              <template v-else-if="column.key === 'score'"><a-rate v-if="record.reviewScore" :value="record.reviewScore" disabled style="font-size: 13px" /><span v-else>-</span></template>
              <template v-else-if="column.key === 'action'"><a @click="openReport(record)">查看详情</a></template>
            </template>
          </a-table>
        </article>
      </template>

      <article v-else class="of-card">
        <div class="of-card__head"><h3><TeamOutlined />本部门已提交的总结</h3><span class="of-muted">只能点评本部门、他人已提交的总结；点评后员工不能再修改</span></div>
        <a-table size="small" row-key="id" :data-source="teamList" :columns="teamColumns" :loading="loading" :pagination="{ pageSize: 10 }">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'period'">{{ periodText(record) }}</template>
            <template v-else-if="column.key === 'type'">{{ typeLabel(record.reportType) }}</template>
            <template v-else-if="column.key === 'review'">
              <template v-if="record.reviewedAt"><a-rate v-if="record.reviewScore" :value="record.reviewScore" disabled style="font-size: 12px" /><div class="wr-comment">{{ record.reviewComment }}</div></template>
              <a v-else-if="String(record.reporterId) !== myStaffId" @click="openReview(record)">点评</a>
              <span v-else class="of-muted">本人</span>
            </template>
          </template>
        </a-table>
      </article>
    </div>

    <a-modal v-model:open="reviewOpen" :title="`点评 · ${reviewTarget?.title || ''}`" :confirm-loading="saving" width="640px" @ok="submitReview">
      <div class="wr-preview">
        <p><b>{{ reviewTarget?.reporterName }}</b> · {{ reviewTarget ? periodText(reviewTarget) : '' }}</p>
        <p v-if="reviewTarget?.completedWork"><b>完成工作：</b>{{ reviewTarget.completedWork }}</p>
        <p v-if="reviewTarget?.riskIssue"><b>问题与改进：</b>{{ reviewTarget.riskIssue }}</p>
        <p v-if="reviewTarget?.nextPlan"><b>下期计划：</b>{{ reviewTarget.nextPlan }}</p>
      </div>
      <a-form layout="vertical">
        <a-form-item label="评分"><a-rate v-model:value="reviewForm.score" /></a-form-item>
        <a-form-item label="点评意见" required><a-textarea v-model:value="reviewForm.comment" :rows="3" /></a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import dayjs, { type Dayjs } from 'dayjs'
import { message, Modal } from 'ant-design-vue'
import {
  BarChartOutlined, CalendarOutlined, CheckSquareOutlined, CommentOutlined, EditOutlined, FileDoneOutlined, FileTextOutlined,
  FormOutlined, PlusOutlined, SendOutlined, StarOutlined, TeamOutlined
} from '@ant-design/icons-vue'
import PageContainer from '../../components/PageContainer.vue'
import {
  createOaWorkReport, deleteOaWorkReport, getOaWorkReportDraftStats, getOaWorkReportPage, reviewOaWorkReport, submitOaWorkReport, updateOaWorkReport
} from '../../api/oa'
import type { OaWorkReport } from '../../types'
import { useUserStore } from '../../stores/user'
import { hasMinisterOrHigher } from '../../utils/roleAccess'
import { defaultReportPeriod, draftFromStats } from './myOffice'
import './office/office.css'

const PAGE = 6
const reportTypeOptions = [
  { label: '日报', value: 'DAY' }, { label: '周报', value: 'WEEK' }, { label: '月报', value: 'MONTH' }, { label: '年度总结', value: 'YEAR' }
]
const historyColumns = [
  { title: '总结标题', dataIndex: 'title' }, { title: '周期', key: 'period', width: 190 }, { title: '类型', key: 'type', width: 90 },
  { title: '领导点评', key: 'review' }, { title: '评分', key: 'score', width: 150 }, { title: '', key: 'action', width: 90 }
]
const teamColumns = [
  { title: '员工', dataIndex: 'reporterName', width: 100 }, { title: '总结标题', dataIndex: 'title' }, { title: '周期', key: 'period', width: 190 },
  { title: '类型', key: 'type', width: 90 }, { title: '点评', key: 'review', width: 260 }
]

const userStore = useUserStore()
const myStaffId = String(userStore.staffInfo?.id || '')
const isManager = hasMinisterOrHigher(userStore.roles || [])
const viewScope = ref<'mine' | 'team'>('mine')
const loading = ref(false)
const saving = ref(false)
const drafting = ref(false)
const myList = ref<OaWorkReport[]>([])
const teamList = ref<OaWorkReport[]>([])
const listFilter = ref<'ALL' | 'DRAFT' | 'SUBMITTED' | 'REVIEWED'>('ALL')
const listPage = ref(1)
const stats = ref<{ tasksDone?: number | string; todosDone?: number | string; attendanceDays?: number | string }>({})
const reviewOpen = ref(false)
const reviewTarget = ref<OaWorkReport>()
const reviewForm = reactive({ comment: '', score: 4 })

const emptyForm = () => ({
  id: undefined as string | number | undefined, title: '', reportType: 'WEEK', range: undefined as [Dayjs, Dayjs] | undefined,
  completedWork: '', riskIssue: '', nextPlan: '', contentSummary: '', status: 'DRAFT',
  reviewedAt: undefined as string | undefined, reviewerName: '', reviewComment: '', reviewScore: undefined as number | undefined
})
const form = reactive(emptyForm())
const locked = computed(() => form.status === 'SUBMITTED' || Boolean(form.reviewedAt))

const typeLabel = (t?: string) => reportTypeOptions.find((o) => o.value === t)?.label || '总结'
const fmt = (v?: string) => (v ? dayjs(v).format('MM-DD HH:mm') : '')
const periodText = (r: OaWorkReport) => (r.periodStartDate ? `${r.periodStartDate} ~ ${r.periodEndDate || ''}` : r.reportDate || '')
function statusOf(r: OaWorkReport) {
  if (r.reviewedAt) return { label: '已点评', tone: 'tone-purple' }
  return r.status === 'SUBMITTED' ? { label: '已提交', tone: 'tone-green' } : { label: '草稿', tone: 'tone-orange' }
}
const filteredList = computed(() => myList.value.filter((r) => listFilter.value === 'ALL'
  || (listFilter.value === 'REVIEWED' ? Boolean(r.reviewedAt) : r.status === listFilter.value && !r.reviewedAt)))
const pagedList = computed(() => filteredList.value.slice((listPage.value - 1) * PAGE, listPage.value * PAGE))
const reviewedList = computed(() => myList.value.filter((r) => r.reviewedAt))
watch(listFilter, () => { listPage.value = 1 })

/** 本周 / 本月对应的总结（按周期起始日匹配） */
function reportFor(type: string) {
  const [start] = defaultReportPeriod(type)
  return myList.value.find((r) => r.reportType === type && r.periodStartDate === start)
}
const statusCards = computed(() => {
  const week = reportFor('WEEK')
  const month = reportFor('MONTH')
  const [ws, we] = defaultReportPeriod('WEEK')
  return [
    { label: '本周总结', value: week ? statusOf(week).label : '未撰写', hint: `${ws.slice(5)} ~ ${we.slice(5)}`, icon: CalendarOutlined, tone: 'tone-green', action: () => (week ? openReport(week) : startNew('WEEK')) },
    { label: '本月总结', value: month ? statusOf(month).label : '未撰写', hint: dayjs().format('YYYY年M月'), icon: FileDoneOutlined, tone: 'tone-blue', action: () => (month ? openReport(month) : startNew('MONTH')) },
    { label: '待提交', value: `${myList.value.filter((r) => r.status !== 'SUBMITTED').length} 篇`, hint: '草稿需本人提交', icon: FormOutlined, tone: 'tone-orange', action: () => { listFilter.value = 'DRAFT' } },
    { label: '已点评', value: `${reviewedList.value.length} 篇`, hint: '查看主管点评', icon: StarOutlined, tone: 'tone-purple', action: () => { listFilter.value = 'REVIEWED' } }
  ]
})
const statsRange = computed<[string, string]>(() => (form.range?.[0] && form.range?.[1]
  ? [form.range[0].format('YYYY-MM-DD'), form.range[1].format('YYYY-MM-DD')] : defaultReportPeriod(form.reportType)))
const statItems = computed(() => [
  { label: '完成协同任务', hint: '本人负责、已完成', value: stats.value.tasksDone ?? '--', unit: '项', icon: CheckSquareOutlined, tone: 'tone-green' },
  { label: '完成待办事项', hint: '本人待办已完成', value: stats.value.todosDone ?? '--', unit: '项', icon: FileDoneOutlined, tone: 'tone-blue' },
  { label: '出勤', hint: '有上班打卡的天数', value: stats.value.attendanceDays ?? '--', unit: '天', icon: CalendarOutlined, tone: 'tone-orange' }
])

async function loadMine() {
  loading.value = true
  try {
    // shortcut: 一次取本人最近 100 篇在前端分页，篇数更多时改为服务端分页
    myList.value = (await getOaWorkReportPage({ pageNo: 1, pageSize: 100, mineOnly: true })).list || []
  } finally {
    loading.value = false
  }
}
async function loadTeam() {
  loading.value = true
  try {
    teamList.value = (await getOaWorkReportPage({ pageNo: 1, pageSize: 100, mineOnly: false, status: 'SUBMITTED' })).list || []
  } finally {
    loading.value = false
  }
}
async function loadStats() {
  const [startDate, endDate] = statsRange.value
  stats.value = await getOaWorkReportDraftStats({ startDate, endDate }).catch(() => ({}))
}
watch(statsRange, loadStats)

function onScopeChange() { (viewScope.value === 'team' ? loadTeam : loadMine)() }

function startNew(type: string) {
  Object.assign(form, emptyForm(), { reportType: type })
  const [s, e] = defaultReportPeriod(type)
  form.range = [dayjs(s), dayjs(e)]
  const titles: Record<string, string> = {
    DAY: `${dayjs(s).format('YYYY年M月D日')}日报`,
    WEEK: `${dayjs(s).format('M月D日')}–${dayjs(e).format('M月D日')}周工作总结`,
    MONTH: `${dayjs(s).format('YYYY年M月')}月度工作总结`,
    YEAR: `${dayjs(s).format('YYYY')}年度工作总结`
  }
  form.title = titles[type] || '工作总结'
}
function onTypeChange(type: string) {
  if (!form.id) startNew(type)
}
function openReport(r: OaWorkReport) {
  Object.assign(form, emptyForm(), {
    id: r.id, title: r.title, reportType: r.reportType || 'WEEK', status: r.status || 'DRAFT',
    range: r.periodStartDate && r.periodEndDate ? [dayjs(r.periodStartDate), dayjs(r.periodEndDate)] : undefined,
    completedWork: r.completedWork || '', riskIssue: r.riskIssue || '', nextPlan: r.nextPlan || '', contentSummary: r.contentSummary || '',
    reviewedAt: r.reviewedAt, reviewerName: r.reviewerName || '', reviewComment: r.reviewComment || '', reviewScore: r.reviewScore
  })
}

async function insertDraft() {
  drafting.value = true
  try {
    await loadStats()
    const [s, e] = statsRange.value
    const draft = draftFromStats(stats.value, s, e)
    form.completedWork = form.completedWork ? `${form.completedWork}\n${draft}` : draft
  } finally {
    drafting.value = false
  }
}

async function save(submit: boolean) {
  if (!form.title.trim()) return message.warning('请填写标题')
  if (!form.range) return message.warning('请选择总结周期')
  const payload = {
    title: form.title.trim(), reportType: form.reportType, reportDate: dayjs().format('YYYY-MM-DD'),
    periodStartDate: form.range[0].format('YYYY-MM-DD'), periodEndDate: form.range[1].format('YYYY-MM-DD'),
    completedWork: form.completedWork, riskIssue: form.riskIssue, nextPlan: form.nextPlan, contentSummary: form.contentSummary,
    reporterName: userStore.staffInfo?.realName || userStore.staffInfo?.username, status: 'DRAFT'
  }
  const doSave = async () => {
    saving.value = true
    try {
      const saved = form.id ? await updateOaWorkReport(form.id, payload) : await createOaWorkReport(payload)
      form.id = saved?.id ?? form.id
      if (submit && form.id) await submitOaWorkReport(form.id)
      message.success(submit ? '已提交，等待主管点评' : '草稿已保存')
      await loadMine()
      const fresh = myList.value.find((r) => String(r.id) === String(form.id))
      if (fresh) openReport(fresh)
    } finally {
      saving.value = false
    }
  }
  if (!submit) return doSave()
  Modal.confirm({ title: '确认提交这篇总结？', content: '提交后由主管点评，点评后不能再修改。', okText: '确认提交', onOk: doSave })
}

function removeCurrent() {
  Modal.confirm({
    title: '删除这篇总结？', okType: 'danger', okText: '删除',
    async onOk() {
      await deleteOaWorkReport(form.id!)
      message.success('已删除')
      Object.assign(form, emptyForm())
      startNew('WEEK')
      await loadMine()
    }
  })
}

function openReview(r: OaWorkReport) {
  reviewTarget.value = r
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
    await loadTeam()
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  startNew('WEEK')
  await loadMine()
  loadStats()
})
</script>

<style scoped>
.wr-grid { display: grid; grid-template-columns: minmax(260px, 0.9fr) minmax(0, 1.8fr) minmax(260px, 0.9fr); gap: 16px; align-items: start; }
.wr-filter { display: flex; margin: 12px 0 8px; }
.wr-item { display: flex; align-items: center; gap: 10px; width: 100%; margin-bottom: 8px; padding: 10px 12px; border: 1px solid #e8eee9; border-radius: 12px; background: #fff; text-align: left; cursor: pointer; color: #17734f; }
.wr-item--active { border-color: #1f9d74; background: #f2faf5; }
.wr-pager { margin-top: 8px; text-align: center; }
.wr-actions { display: flex; flex-wrap: wrap; align-items: center; justify-content: flex-end; gap: 10px; margin-top: 8px; }
.wr-actions .of-muted { margin-right: auto; }
.wr-review { margin-top: 8px; padding: 12px 14px; border-radius: 12px; background: #fdf7ec; }
.wr-review p { margin: 6px 0 0; color: #4f6258; }
.wr-stat { display: flex; align-items: center; gap: 12px; padding: 12px; margin-bottom: 10px; border: 1px solid #eef2ef; border-radius: 12px; }
.wr-stat strong { font-size: 22px; color: #1d3a2f; }
.wr-stat strong small { font-size: 12px; font-weight: 400; color: #6b7a74; }
.wr-comment { max-width: 520px; padding: 6px 10px; margin-top: 4px; border-radius: 8px; background: #fdf7ec; color: #4f6258; font-size: 12px; }
.wr-preview { max-height: 240px; overflow: auto; padding: 10px 12px; margin-bottom: 12px; border-radius: 10px; background: #f6faf7; }
.wr-preview p { margin: 0 0 6px; white-space: pre-wrap; }
@media (max-width: 1200px) { .wr-grid { grid-template-columns: 1fr 1fr; } .wr-grid > :nth-child(2) { grid-column: 1 / -1; order: -1; } }
@media (max-width: 860px) { .wr-grid { grid-template-columns: 1fr; } }
</style>
