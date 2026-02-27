<template>
  <div class="portal-page">
    <div class="portal-hero card-elevated">
      <div>
        <div class="hero-title">智慧养老 OA 工作台</div>
        <div class="hero-subtitle">提醒中心、协同任务、行政审批、经营看板统一入口</div>
        <div class="hero-search">
          <a-auto-complete
            v-model:value="searchKeyword"
            :options="searchOptions"
            style="width: 420px"
            placeholder="一键搜索页面（如：请假审批、退住结算、消防月报）"
            @search="onSearchChange"
            @select="onSearchSelect"
          >
            <a-input-search enter-button="搜索" @search="submitSearch" />
          </a-auto-complete>
        </div>
      </div>
      <div class="hero-meta">
        <div class="meta-item">
          <div class="meta-label">当前用户</div>
          <div class="meta-value">{{ userStore.staffInfo?.realName || '管理员' }}</div>
        </div>
        <div class="meta-item">
          <div class="meta-label">刷新时间</div>
          <div class="meta-value">{{ refreshedAt || '--' }}</div>
        </div>
      </div>
    </div>

    <a-row :gutter="[10, 10]" class="summary-strip">
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card :bordered="false" class="card-elevated mini-card" hoverable @click="go('/hr/performance')">
          <a-statistic title="我的工资" :value="summary.mySalaryAmount || 0" suffix="元" :precision="2" />
          <div class="stat-desc">发放日 {{ summary.salaryPayDate || '每月固定日期' }}</div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card :bordered="false" class="card-elevated mini-card" hoverable @click="go('/hr/performance')">
          <a-statistic title="我的绩效" :value="summary.myPerformanceScore || 0" suffix="分" :precision="1" />
          <div class="stat-desc">等级 {{ summary.myPerformanceLevel || '--' }}</div>
        </a-card>
      </a-col>
      <a-col :xs="12" :sm="6" :lg="3" v-for="item in topStats.slice(0, 4)" :key="item.title">
        <a-card :bordered="false" class="card-elevated micro-card" hoverable @click="go(item.route)">
          <a-statistic :title="item.title" :value="item.value" />
          <div class="stat-desc">{{ item.desc }}</div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="[10, 10]" class="compact-main-row">
      <a-col :xs="24" :lg="15" class="stretch-col">
        <a-card :bordered="false" class="card-elevated compact-calendar-card full-height-card">
          <template #title>
            <a-button type="link" class="calendar-title-link" @click="goToOaCalendar">行政日历（协同日历）</a-button>
          </template>
          <template #extra>
            <a-space size="small">
              <a-button size="small" @click="openCreateSchedule()">新增日程</a-button>
              <a-button size="small" type="link" @click="goToOaCalendar">进入协同日历</a-button>
            </a-space>
          </template>
          <StatefulBlock :loading="calendarLoading || loading" :error="pageError" :empty="!calendarRows.length" empty-text="暂无日历事项" @retry="init">
            <FullCalendar :options="calendarOptions" />
          </StatefulBlock>
        </a-card>
      </a-col>
      <a-col :xs="24" :lg="9" class="stretch-col">
        <a-card title="协同总览与快捷入口" :bordered="false" class="card-elevated full-height-card side-card">
          <StatefulBlock :loading="loading" :error="pageError" @retry="init">
            <div class="panel-section">
              <div class="section-head">
                <span>提醒中心</span>
                <a-button type="link" size="small" @click="go('/oa/todo')">更多</a-button>
              </div>
              <div class="scroll-list">
                <a-list size="small" :data-source="reminderItems.slice(0, 5)" :locale="{ emptyText: '暂无提醒' }">
                  <template #renderItem="{ item }">
                    <a-list-item>
                      <a-list-item-meta :title="item.title" :description="item.desc" />
                      <template #actions>
                        <a-button type="link" @click="go(item.route)">处理</a-button>
                      </template>
                    </a-list-item>
                  </template>
                </a-list>
              </div>
            </div>

            <div class="panel-section">
              <div class="section-head">
                <span>流程待办</span>
                <a-button type="link" size="small" @click="go('/oa/approval')">进入审批</a-button>
              </div>
              <a-space wrap>
                <a-tag color="processing">进行中任务 {{ summary.ongoingTaskCount || 0 }}</a-tag>
                <a-tag color="success">总结 {{ summary.submittedReportCount || 0 }}</a-tag>
                <a-tag color="warning">超时 {{ summary.approvalTimeoutCount || 0 }}</a-tag>
              </a-space>
              <div class="workflow-line">
                <div v-for="item in (summary.workflowTodos || []).slice(0, 4)" :key="item.name" class="workflow-item">
                  <span class="workflow-name">{{ item.name }}</span>
                  <a-badge :count="item.count || 0" />
                </div>
              </div>
            </div>

            <div class="panel-section">
              <div class="section-head">
                <span>异常提醒</span>
                <a-button type="link" size="small" @click="go('/health/inspection')">查看详情</a-button>
              </div>
              <a-space wrap>
                <a-tag v-for="item in abnormalItems" :key="item.title" color="orange">{{ item.title }}：{{ item.desc }}</a-tag>
              </a-space>
            </div>

            <div class="panel-section">
              <div class="section-head">
                <span>常用功能</span>
              </div>
              <a-space wrap>
                <a-button v-for="item in quickLaunches.slice(0, 6)" :key="item.label" size="small" @click="go(item.route)">{{ item.label }}</a-button>
                <a-button size="small" @click="go('/oa/attendance-leave')">考勤管理</a-button>
                <a-button size="small" @click="go('/finance/account')">费用管理</a-button>
                <a-button size="small" @click="go('/oa/document')">文档中心</a-button>
                <a-button size="small" @click="go('/stats/org/monthly-operation')">院长总览</a-button>
                <a-button size="small" @click="go('/marketing/reports/channel')">渠道统计</a-button>
              </a-space>
            </div>
          </StatefulBlock>
        </a-card>
      </a-col>
    </a-row>

    <a-modal
      v-model:open="scheduleOpen"
      title="新增行政日程"
      width="620px"
      :confirm-loading="scheduleSaving"
      @ok="submitSchedule"
    >
      <a-form layout="vertical">
        <a-form-item label="标题" required>
          <a-input v-model:value="scheduleForm.title" placeholder="例如：月度行政例会" />
        </a-form-item>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="开始时间" required>
              <a-date-picker v-model:value="scheduleForm.startTime" show-time style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="结束时间">
              <a-date-picker v-model:value="scheduleForm.endTime" show-time style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="负责人">
              <a-input v-model:value="scheduleForm.assigneeName" placeholder="例如：行政部" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="优先级">
              <a-select v-model:value="scheduleForm.priority" :options="priorityOptions" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="备注">
          <a-textarea v-model:value="scheduleForm.description" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>

  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import type { Dayjs } from 'dayjs'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import interactionPlugin from '@fullcalendar/interaction'
import { useUserStore } from '../stores/user'
import { createOaTask, getOaTaskCalendar, getPortalSummary } from '../api/oa'
import { useLiveSyncRefresh } from '../composables/useLiveSyncRefresh'
import type { OaPortalSummary, OaTask } from '../types'
import StatefulBlock from '../components/StatefulBlock.vue'

const router = useRouter()
const userStore = useUserStore()
const refreshedAt = ref('')
const calendarLoading = ref(false)
const scheduleOpen = ref(false)
const scheduleSaving = ref(false)
const loading = ref(false)
const pageError = ref('')
const calendarRows = ref<OaTask[]>([])
const scheduleForm = reactive({
  title: '',
  startTime: undefined as Dayjs | undefined,
  endTime: undefined as Dayjs | undefined,
  assigneeName: '',
  priority: 'NORMAL',
  description: ''
})

const summary = reactive<OaPortalSummary>({
  notices: [],
  todos: [],
  workflowTodos: [],
  marketingChannels: [],
  collaborationGantt: [],
  latestSuggestions: []
})

const quickLaunches = [
  { label: '签到/签退', route: '/oa/attendance-leave?action=clock' },
  { label: '请假', route: '/oa/approval?type=LEAVE&quick=1' },
  { label: '加班', route: '/oa/approval?type=OVERTIME&quick=1' },
  { label: '报销', route: '/oa/approval?type=REIMBURSE&quick=1' },
  { label: '采购', route: '/oa/approval?type=PURCHASE&quick=1' },
  { label: '用章', route: '/oa/approval?type=OFFICIAL_SEAL&quick=1' },
  { label: '收入证明', route: '/oa/approval?type=INCOME_PROOF&quick=1' },
  { label: '物资申请', route: '/oa/approval?type=MATERIAL_APPLY&quick=1' }
]

const priorityOptions = [
  { label: '低', value: 'LOW' },
  { label: '中', value: 'NORMAL' },
  { label: '高', value: 'HIGH' }
]

const searchKeyword = ref('')
const searchOptions = ref<{ value: string; label: string; route: string }[]>([])
const searchRouteMap = [
  { label: '签到签退', route: '/oa/attendance-leave?action=clock' },
  { label: '请假审批', route: '/oa/approval?type=LEAVE&quick=1' },
  { label: '加班审批', route: '/oa/approval?type=OVERTIME&quick=1' },
  { label: '报销审批', route: '/oa/approval?type=REIMBURSE&quick=1' },
  { label: '采购审批', route: '/oa/approval?type=PURCHASE&quick=1' },
  { label: '收入证明审批', route: '/oa/approval?type=INCOME_PROOF&quick=1' },
  { label: '物资申请审批', route: '/oa/approval?type=MATERIAL_APPLY&quick=1' },
  { label: '退住结算', route: '/finance/discharge-settlement' },
  { label: '在住账单缴费', route: '/finance/resident-bill-payment' },
  { label: '健康服务与医护账户一体化', route: '/medical-care/integrated-account' },
  { label: '消防报表统计', route: '/fire/data-stats' },
  { label: '消防日间巡查', route: '/fire/day-patrol' },
  { label: 'Resident 360', route: '/elder/resident-360' }
]

const topStats = computed(() => [
  {
    title: '我的待办',
    value: summary.openTodoCount || 0,
    desc: `超期 ${summary.overdueTodoCount || 0} 条`,
    route: '/oa/todo'
  },
  {
    title: '流程待审批',
    value: summary.pendingApprovalCount || 0,
    desc: `超时 ${summary.approvalTimeoutCount || 0} 条`,
    route: '/oa/approval'
  },
  {
    title: '库存预警',
    value: summary.inventoryLowStockCount || 0,
    desc: `采购草稿 ${summary.materialPurchaseDraftCount || 0} 条`,
    route: '/material/alerts'
  },
  {
    title: '长者状态异常',
    value: summary.elderAbnormalCount || 0,
    desc: `在住长者 ${summary.inHospitalElderCount || 0} 人`,
    route: '/health/inspection'
  },
  {
    title: '事故未闭环',
    value: summary.incidentOpenCount || 0,
    desc: `调拨草稿 ${summary.materialTransferDraftCount || 0} 条`,
    route: '/life/incident'
  },
  {
    title: '文档归档',
    value: summary.documentCount || 0,
    desc: `发票夹 ${summary.invoiceFolderCount || 0} 个`,
    route: '/oa/document'
  }
])

const reminderItems = computed(() => {
  const noticeItems = (summary.notices || []).map((notice) => ({
    title: `公告：${notice.title}`,
    desc: `${notice.publisherName || '系统'} · ${formatTime(notice.publishTime)}`,
    route: '/oa/notice'
  }))
  const todoItems = (summary.todos || []).map((todo) => ({
    title: `待办：${todo.title}`,
    desc: `截止时间 ${formatTime(todo.dueTime)}`,
    route: '/oa/todo'
  }))
  return [...todoItems, ...noticeItems].slice(0, 10)
})

const abnormalItems = computed(() => [
  {
    title: '考勤异常',
    desc: `今日异常 ${summary.attendanceAbnormalCount || 0} 条`
  },
  {
    title: '审批超时',
    desc: `超 48 小时未处理 ${summary.approvalTimeoutCount || 0} 条`
  },
  {
    title: '库存不足',
    desc: `低于安全库存 ${summary.inventoryLowStockCount || 0} 项`
  },
  {
    title: '长者状态异常',
    desc: `健康巡检异常 ${summary.healthAbnormalCount || 0} 条`
  }
])

const calendarOptions = computed(() => ({
  plugins: [dayGridPlugin, interactionPlugin],
  initialView: 'dayGridMonth',
  locale: 'zh-cn',
  height: 320,
  headerToolbar: {
    left: 'prev,next today',
    center: 'title',
    right: 'dayGridMonth'
  },
  buttonText: {
    today: '今天',
    month: '月视图',
    week: '周视图'
  },
  dateClick: (arg: any) => {
    openCreateSchedule(dayjs(arg.dateStr))
  },
  eventClick: () => {
    goToOaCalendar()
  },
  events: calendarRows.value.map((task) => ({
    id: String(task.id),
    title: `${task.title}${task.assigneeName ? `（${task.assigneeName}）` : ''}`,
    start: task.startTime || task.endTime,
    end: task.endTime || task.startTime,
    color: task.status === 'DONE' ? '#52c41a' : '#1677ff'
  }))
}))

function go(path: string) {
  router.push(path)
}

function goToOaCalendar() {
  go('/oa/work-execution/calendar')
}

function openCreateSchedule(date?: Dayjs) {
  scheduleForm.title = ''
  scheduleForm.startTime = date ? date.hour(9).minute(0).second(0) : undefined
  scheduleForm.endTime = date ? date.hour(10).minute(0).second(0) : undefined
  scheduleForm.assigneeName = ''
  scheduleForm.priority = 'NORMAL'
  scheduleForm.description = ''
  scheduleOpen.value = true
}

async function submitSchedule() {
  if (!scheduleForm.title.trim()) {
    message.warning('请填写标题')
    return
  }
  if (!scheduleForm.startTime) {
    message.warning('请选择开始时间')
    return
  }
  scheduleSaving.value = true
  try {
    await createOaTask({
      title: scheduleForm.title.trim(),
      description: scheduleForm.description || undefined,
      startTime: dayjs(scheduleForm.startTime).format('YYYY-MM-DDTHH:mm:ss'),
      endTime: scheduleForm.endTime ? dayjs(scheduleForm.endTime).format('YYYY-MM-DDTHH:mm:ss') : undefined,
      priority: scheduleForm.priority,
      status: 'OPEN',
      assigneeName: scheduleForm.assigneeName || undefined
    })
    scheduleOpen.value = false
    message.success('日程已新增')
    await loadCalendar()
  } finally {
    scheduleSaving.value = false
  }
}

function onSearchChange(keyword: string) {
  const text = String(keyword || '').trim()
  if (!text) {
    searchOptions.value = searchRouteMap.slice(0, 8).map((item) => ({
      value: item.label,
      label: `${item.label} - ${item.route}`,
      route: item.route
    }))
    return
  }
  const lower = text.toLowerCase()
  searchOptions.value = searchRouteMap
    .filter((item) => item.label.includes(text) || item.route.toLowerCase().includes(lower))
    .slice(0, 12)
    .map((item) => ({
      value: item.label,
      label: `${item.label} - ${item.route}`,
      route: item.route
    }))
}

function onSearchSelect(_value: string, option: any) {
  if (option?.route) {
    go(option.route)
  }
}

function submitSearch(value: string) {
  const text = String(value || searchKeyword.value || '').trim()
  if (!text) return
  const match = searchRouteMap.find((item) => item.label.includes(text) || item.route.includes(text))
  if (match) {
    go(match.route)
    return
  }
  message.warning('未匹配到页面，请换个关键词')
}

function formatTime(value?: string) {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm') : '--'
}

async function loadSummary() {
  const data = await getPortalSummary()
  Object.assign(summary, data)
  refreshedAt.value = dayjs().format('YYYY-MM-DD HH:mm:ss')
}

async function loadCalendar() {
  calendarLoading.value = true
  try {
    calendarRows.value = await getOaTaskCalendar({
      startDate: dayjs().startOf('month').format('YYYY-MM-DD'),
      endDate: dayjs().endOf('month').format('YYYY-MM-DD')
    })
  } finally {
    calendarLoading.value = false
  }
}

useLiveSyncRefresh({
  topics: ['elder', 'bed', 'lifecycle', 'finance', 'care', 'health', 'dining', 'marketing', 'oa'],
  refresh: () => {
    loadSummary()
    loadCalendar()
  },
  debounceMs: 800
})

async function init() {
  loading.value = true
  pageError.value = ''
  try {
    await Promise.all([loadSummary(), loadCalendar()])
  } catch (error: any) {
    pageError.value = error?.message || '加载首页失败'
    message.error(pageError.value)
  } finally {
    loading.value = false
  }
}

onMounted(init)
</script>

<style scoped>
.portal-page {
  display: flex;
  flex-direction: column;
  gap: 10px;
  height: calc(100vh - 132px);
  min-height: calc(100vh - 132px);
  overflow: hidden;
}

.portal-hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
}

.hero-title {
  font-size: 18px;
  font-weight: 700;
}

.hero-subtitle {
  margin-top: 4px;
  color: var(--muted);
  font-size: 12px;
}

.hero-search {
  margin-top: 8px;
}

.hero-meta {
  display: flex;
  gap: 24px;
}

.meta-item {
  text-align: right;
}

.meta-label {
  font-size: 12px;
  color: var(--muted);
}

.meta-value {
  font-size: 13px;
  font-weight: 600;
}

.summary-strip {
  flex-shrink: 0;
}

.mini-card,
.micro-card {
  cursor: pointer;
  min-height: 86px;
}

.micro-card {
  min-height: 86px;
}

.stat-desc {
  margin-top: 6px;
  color: var(--muted);
  font-size: 11px;
}

.compact-main-row {
  flex: 1;
  min-height: 0;
}

.stretch-col {
  display: flex;
  min-height: 0;
}

.full-height-card {
  width: 100%;
  height: 100%;
}

.full-height-card :deep(.ant-card-head) {
  min-height: 48px;
  padding: 0 12px;
}

.full-height-card :deep(.ant-card-body) {
  padding: 10px 12px;
}

.side-card :deep(.ant-card-body) {
  max-height: calc(100% - 48px);
  overflow: auto;
}

.panel-section + .panel-section {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
  font-size: 13px;
  font-weight: 600;
}

.scroll-list {
  max-height: 124px;
  overflow: auto;
}

.workflow-line {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-top: 8px;
}

.workflow-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.workflow-name {
  font-size: 12px;
  color: #334155;
}

.compact-calendar-card :deep(.fc .fc-daygrid-day-frame) {
  min-height: 40px;
}

.compact-calendar-card :deep(.fc .fc-toolbar.fc-header-toolbar) {
  margin-bottom: 0.35em;
}

.compact-calendar-card :deep(.fc .fc-toolbar-title) {
  font-size: 16px;
}

.compact-calendar-card :deep(.fc .fc-button) {
  padding: 0.2em 0.45em;
}

.compact-calendar-card :deep(.fc .fc-daygrid-day-number) {
  font-size: 12px;
}

.compact-calendar-card :deep(.fc .fc-daygrid-event) {
  font-size: 11px;
}

.compact-calendar-card :deep(.fc .fc-view-harness) {
  min-height: 310px;
}

@media (max-width: 768px) {
  .portal-page {
    height: auto;
    min-height: 0;
    overflow: visible;
  }

  .portal-hero {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .hero-meta {
    width: 100%;
    justify-content: space-between;
  }

  .hero-search :deep(.ant-select),
  .hero-search :deep(.ant-input-search) {
    width: 100% !important;
  }
}
</style>
