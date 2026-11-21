<template>
  <PageContainer class="rm-page" title="行政工作台" :sub-title="`${today} · 全局待办、家属反馈、跨部门任务和运营提醒`">
    <template #extra>
      <a-space wrap>
        <a-button @click="go('/oa/services?tab=maintenance')">新建工单</a-button>
        <a-button type="primary" @click="go('/oa/collaboration?tab=approvals')">发起审批</a-button>
      </a-space>
    </template>

    <div class="rm-kpis">
      <button v-for="card in kpis" :key="card.key" type="button" class="rm-kpi" @click="go(card.path)">
        <span class="rm-kpi__icon" :class="card.tone && `rm-kpi__icon--${card.tone}`"><component :is="card.icon" /></span>
        <span class="rm-kpi__label">{{ card.label }}</span>
        <span class="rm-kpi__value">{{ loaded ? card.value : '--' }}<small>{{ card.unit }}</small></span>
        <span class="rm-kpi__hint">{{ card.hint }}</span>
      </button>
    </div>

    <div class="rm-split aw-grid">
      <section class="rm-card">
        <div class="rm-card__head">
          <span class="rm-card__title">今日待办 <small class="rm-card__sub">我的待办事项</small></span>
          <a class="rm-link" @click="go('/workbench/todo')">查看全部 →</a>
        </div>
        <div v-if="!todos.length" class="aw-empty">{{ loaded ? '今天没有待办事项' : '暂无数据' }}</div>
        <div v-for="todo in todos" :key="String(todo.id)" class="rm-row">
          <span class="aw-time">{{ todo.dueTime ? dayjs(todo.dueTime).format('HH:mm') : '--' }}</span>
          <div class="rm-row__main">
            <span class="rm-row__title">{{ todo.title }}</span>
            <span class="rm-row__meta">{{ todo.content || '' }}{{ todo.assigneeName ? ` · ${todo.assigneeName}` : '' }}</span>
          </div>
          <span class="rm-pill" :class="isOverdue(todo.dueTime) ? 'rm-pill--danger' : 'rm-pill--warning'">{{ isOverdue(todo.dueTime) ? '已超时' : '待处理' }}</span>
        </div>
      </section>

      <section class="rm-card">
        <div class="rm-card__head"><span class="rm-card__title">重点提醒 <small class="rm-card__sub">及时闭环</small></span></div>
        <div v-if="!alerts.length" class="aw-empty">暂无需要提醒的事项</div>
        <button v-for="item in alerts" :key="item.label" type="button" class="rm-row aw-alert" @click="go(item.path)">
          <span class="aw-dot" :class="`aw-dot--${item.tone}`" />
          <div class="rm-row__main"><span class="rm-row__title">{{ item.label }}</span><span class="rm-row__meta">{{ item.hint }}</span></div>
          <span class="rm-pill" :class="item.tone === 'danger' ? 'rm-pill--danger' : 'rm-pill--warning'">{{ item.count }}</span>
        </button>
      </section>
    </div>

    <div class="rm-split aw-grid">
      <section class="rm-card">
        <div class="rm-card__head"><span class="rm-card__title">待处理流程 <small class="rm-card__sub">按类型汇总</small></span><a class="rm-link" @click="go('/oa/collaboration?tab=approvals')">审批办理 →</a></div>
        <div v-if="!workflows.length" class="aw-empty">没有待处理的流程</div>
        <div class="aw-flows">
          <button v-for="flow in workflows" :key="flow.code" type="button" @click="go(flow.route || '/oa/collaboration?tab=approvals')">
            <b>{{ flow.count }}</b><span>{{ flow.name }}</span>
          </button>
        </div>
      </section>
      <section class="rm-card">
        <div class="rm-card__head"><span class="rm-card__title">快捷办理</span></div>
        <div class="aw-quick">
          <button v-for="item in QUICK" :key="item.label" type="button" @click="go(item.path)"><component :is="item.icon" /><span>{{ item.label }}</span></button>
        </div>
        <div class="aw-care">
          <b>今日关怀</b>
          <span v-if="!birthdays.length">本周没有长者过生日</span>
          <span v-else>本周 {{ birthdays.length }} 位长者过生日：{{ birthdays.slice(0, 3).map((b) => b.elderName).join('、') }}{{ birthdays.length > 3 ? ' 等' : '' }}</span>
          <a class="rm-link" @click="go('/oa/culture?tab=birthdays')">安排生日关怀 →</a>
        </div>
      </section>
    </div>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'
import { AuditOutlined, CalendarOutlined, CheckSquareOutlined, CommentOutlined, HomeOutlined, NotificationOutlined, TeamOutlined, ToolOutlined } from '@ant-design/icons-vue'
import PageContainer from '../../../components/PageContainer.vue'
import { getPortalSummary, getSuggestionPage } from '../../../api/oa'
import { getBirthdayAll, getMaintenancePage, getRoomCleaningPage } from '../../../api/life'
import { useUserStore } from '../../../stores/user'
import { hasMinisterOrHigher } from '../../../utils/roleAccess'
import { useLiveSyncRefresh } from '../../../composables/useLiveSyncRefresh'
import type { BirthdayReminder, OaPortalSummary } from '../../../types'
import { upcomingBirthdays } from './adminTabs'
import '../../elder/residentUi.css'

const QUICK = [
  { label: '家属服务', icon: TeamOutlined, path: '/oa/family?tab=service' },
  { label: '审批办理', icon: AuditOutlined, path: '/oa/collaboration?tab=approvals' },
  { label: '服务工单', icon: ToolOutlined, path: '/oa/services?tab=maintenance' },
  { label: '活动组织', icon: CalendarOutlined, path: '/oa/culture?tab=activities' },
  { label: '通知公告', icon: NotificationOutlined, path: '/oa/library?tab=notices' },
  { label: '家属反馈', icon: CommentOutlined, path: '/oa/family?tab=feedback' }
]

const router = useRouter()
const user = useUserStore()
const today = dayjs().format('YYYY年M月D日')
const loaded = ref(false)
const summary = ref<OaPortalSummary | null>(null)
const feedbackPending = ref(0)
const repairsOpen = ref(0)
const cleaningOpen = ref(0)
const birthdayList = ref<BirthdayReminder[]>([])

const n = (value: unknown) => Number(value ?? 0)
const isOverdue = (value?: string) => Boolean(value && dayjs(value).isBefore(dayjs()))
const todos = computed(() => (summary.value?.todos || []).slice(0, 6))
const workflows = computed(() => (summary.value?.workflowTodos || []).filter((w) => n(w.count) > 0))
const birthdays = computed(() => upcomingBirthdays(birthdayList.value, 7))
const kpis = computed(() => {
  const s = summary.value
  return [
    { key: 'todo', label: '今日待办', value: n(s?.openTodoCount), unit: '项', hint: `已超时 ${n(s?.overdueTodoCount)} 项`, icon: CheckSquareOutlined, path: '/workbench/todo' },
    { key: 'approval', label: '待我审批', value: n(s?.pendingApprovalCount), unit: '项', hint: `审批超时 ${n(s?.approvalTimeoutCount)} 项`, icon: AuditOutlined, tone: 'warning', path: '/oa/collaboration?tab=approvals' },
    { key: 'family', label: '家属待回复', value: feedbackPending.value, unit: '条', hint: '家属反馈与沟通', icon: CommentOutlined, tone: 'danger', path: '/oa/family?tab=feedback' },
    { key: 'orders', label: '在办服务工单', value: repairsOpen.value + cleaningOpen.value, unit: '单', hint: `维修 ${repairsOpen.value} · 保洁 ${cleaningOpen.value}`, icon: HomeOutlined, tone: 'info', path: '/oa/services?tab=maintenance' },
    { key: 'tasks', label: '进行中协同任务', value: n(s?.ongoingTaskCount), unit: '项', hint: '跨部门任务', icon: TeamOutlined, path: '/oa/collaboration?tab=tasks' }
  ]
})
const alerts = computed(() => {
  const s = summary.value
  return [
    { label: '超时待办', count: n(s?.overdueTodoCount), hint: '已超过截止时间的待办', path: '/workbench/todo', tone: 'danger' },
    { label: '审批即将超时', count: n(s?.approvalTimeoutCount), hint: '超过时限仍未处理的审批', path: '/oa/collaboration?tab=approvals', tone: 'danger' },
    { label: '家属反馈待回复', count: feedbackPending.value, hint: '涉及安全的反馈请立即转责任部门', path: '/oa/family?tab=feedback', tone: 'warning' },
    { label: '维修待处理', count: repairsOpen.value, hint: '后勤执行，行政跟踪回访', path: '/oa/services?tab=maintenance', tone: 'warning' }
  ].filter((item) => item.count > 0)
})

function go(path: string) { router.push(path) }

async function load() {
  const scope = hasMinisterOrHigher(user.roles || []) ? 'ORG' : 'PERSONAL'
  const quiet = { silent403: true }
  const [s, f, r1, r2, c, b] = await Promise.allSettled([
    getPortalSummary({ params: { scope }, ...quiet }),
    getSuggestionPage({ pageNo: 1, pageSize: 1, status: 'PENDING', familyCommunicationOnly: true }),
    getMaintenancePage({ pageNo: 1, pageSize: 1, status: 'OPEN' }),
    getMaintenancePage({ pageNo: 1, pageSize: 1, status: 'PROCESSING' }),
    getRoomCleaningPage({ pageNo: 1, pageSize: 1, status: 'PENDING' }),
    getBirthdayAll({}, quiet)
  ])
  summary.value = s.status === 'fulfilled' ? s.value : null
  feedbackPending.value = f.status === 'fulfilled' ? n(f.value.total) : 0
  repairsOpen.value = (r1.status === 'fulfilled' ? n(r1.value.total) : 0) + (r2.status === 'fulfilled' ? n(r2.value.total) : 0)
  cleaningOpen.value = c.status === 'fulfilled' ? n(c.value.total) : 0
  birthdayList.value = b.status === 'fulfilled' ? b.value || [] : []
  loaded.value = true
}

onMounted(load)
useLiveSyncRefresh({ topics: ['oa', 'logistics'], refresh: load, debounceMs: 900 })
</script>

<style scoped>
.aw-grid { grid-template-columns: minmax(0, 1.6fr) minmax(300px, 1fr); margin-top: 16px; }
.aw-empty { padding: 24px 8px; color: #7b877e; text-align: center; }
.aw-time { width: 44px; color: #6b7a74; font-variant-numeric: tabular-nums; }
.aw-alert { width: 100%; border: 0; background: none; text-align: left; cursor: pointer; }
.aw-dot { flex: 0 0 8px; height: 8px; border-radius: 50%; }
.aw-dot--warning { background: #d9a441; }
.aw-dot--danger { background: #d9534f; }
.aw-flows { display: grid; grid-template-columns: repeat(auto-fill, minmax(120px, 1fr)); gap: 10px; }
.aw-flows button { display: grid; gap: 4px; padding: 12px; border: 1px solid #e3ebe6; border-radius: 10px; background: #fff; text-align: left; cursor: pointer; }
.aw-flows b { color: #1f6f55; font-size: 20px; }
.aw-flows span { color: #4f6258; font-size: 13px; }
.aw-quick { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; }
.aw-quick button { display: grid; justify-items: center; gap: 8px; padding: 16px 4px; border: 1px solid #e3ebe6; border-radius: 10px; background: #fff; color: #1f6f55; font-size: 22px; cursor: pointer; }
.aw-quick button span { color: #33433a; font-size: 13px; }
.aw-quick button:hover { border-color: #1f9d74; background: #f3faf6; }
.aw-care { display: grid; gap: 4px; margin-top: 14px; padding: 12px; border-radius: 10px; background: #f3faf6; font-size: 13px; color: #4f6258; }
@media (max-width: 1100px) { .aw-grid { grid-template-columns: 1fr; } }
</style>
