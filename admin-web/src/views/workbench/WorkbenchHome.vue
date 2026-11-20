<template>
  <div class="unified-workbench">

    <a-alert
      v-if="loadFailures.length"
      class="workbench-load-alert"
      type="warning"
      show-icon
      :message="`部分数据加载失败：${loadFailures.join('、')}，可能显示为占位符`"
    >
      <template #action>
        <a-button size="small" type="link" :loading="loading" @click="loadWorkbench()">重试</a-button>
      </template>
    </a-alert>

    <div class="of-page-title"><h1>我的工作台</h1><span>欢迎使用弋阳龟峰颐养中心员工自助办公平台 · 更新于 {{ refreshedAt }}</span>
      <a-button size="small" :loading="loading" style="margin-left: auto" @click="loadWorkbench()">刷新</a-button>
    </div>
    <OfficeHero :shift="attendanceOverview.expectedWorkStart ? `${attendanceOverview.expectedWorkStart}–${attendanceOverview.expectedWorkEnd}` : ''">
      {{ workbenchPersona.subline }}
    </OfficeHero>

    <div v-if="initialLoading" class="workbench-skeleton">
      <a-skeleton active :title="false" :paragraph="{ rows: 3 }" />
      <a-skeleton active :title="false" :paragraph="{ rows: 6 }" />
    </div>

    <template v-if="!initialLoading">
      <div class="of-grid-3">
        <article class="of-card">
          <div class="of-card__head"><h3><AppstoreOutlined />今日工作概览</h3></div>
          <div class="wb-overview">
            <button v-for="item in overviewNumbers" :key="item.label" type="button" :class="item.tone" @click="openPath(item.path)">
              <b>{{ item.value }}</b><span>{{ item.label }}</span>
            </button>
          </div>
        </article>
        <article class="of-card">
          <div class="of-card__head"><h3><ClockCircleOutlined />考勤状态</h3><button type="button" class="of-link" @click="openPath('/workbench/attendance')">考勤详情 ›</button></div>
          <div class="wb-attendance" :class="{ 'wb-attendance--warn': !todayRecord?.checkInTime }">
            <b>今日：{{ attendanceOverview.todayStatusLabel || '未打卡' }}</b>
            <div>上班 {{ attendanceOverview.expectedWorkStart || '--:--' }}
              <em>{{ todayRecord?.checkInTime ? `已打卡 ${dayjs(todayRecord.checkInTime).format('HH:mm')}` : '待打卡' }}</em></div>
            <div>下班 {{ attendanceOverview.expectedWorkEnd || '--:--' }}
              <em>{{ todayRecord?.checkOutTime ? `已打卡 ${dayjs(todayRecord.checkOutTime).format('HH:mm')}` : '待打卡' }}</em></div>
            <a-space v-if="attendanceActionButtons.length" wrap style="margin-top: 8px">
              <a-button
                v-for="action in attendanceActionButtons"
                :key="action"
                size="small"
                :type="action === 'IN' || action === 'OUT' ? 'primary' : 'default'"
                :loading="attendancePending === action"
                :disabled="attendanceSubmitting && attendancePending !== action"
                @click="handleAttendanceAction(action)"
              >{{ attendanceActionLabel(action) }}</a-button>
            </a-space>
          </div>
        </article>
        <article class="of-card">
          <div class="of-card__head"><h3><TrophyOutlined />我的积分</h3><button type="button" class="of-link" @click="openPath('/workbench/payslips')">积分明细 ›</button></div>
          <div class="wb-points">
            <div><small>积分余额</small><b>{{ pointsSummary ? pointsSummary.balance : '--' }}<small> 分</small></b></div>
            <div><small>全院排名</small><b>{{ pointsSummary?.rank ? `第 ${pointsSummary.rank} 名` : '--' }}</b><small v-if="pointsSummary?.rankTotal">共 {{ pointsSummary.rankTotal }} 人</small></div>
          </div>
        </article>
      </div>

      <div class="of-grid-3">
        <article class="of-card">
          <div class="of-card__head"><h3><CheckSquareOutlined />我的待办 <small>（共 {{ portalSummary?.openTodoCount ?? 0 }} 项）</small></h3><button type="button" class="of-link" @click="openPath('/workbench/todo')">全部待办 ›</button></div>
          <div class="workbench-filter" role="group" aria-label="待办筛选">
            <button v-for="filter in todoFilterOptions" :key="filter.key" type="button" :class="{ active: todoFilter === filter.key }" :aria-pressed="todoFilter === filter.key" @click="todoFilter = filter.key">{{ filter.label }}</button>
          </div>
          <div v-if="!visibleTodos.length" class="of-empty">{{ portalSummary?.openTodoCount ? '当前筛选暂无待办' : '今天没有待处理事项' }}</div>
          <button v-for="todo in visibleTodos.slice(0, 4)" :key="todo.id" type="button" class="of-row wb-row-btn" @click="openPath('/workbench/todo')">
            <span class="of-kpi__icon tone-green wb-row-icon"><FileTextOutlined /></span>
            <div class="of-row__main"><b>{{ todo.title }}</b><small>{{ todo.dueTime ? `截止 ${dayjs(todo.dueTime).format('MM-DD HH:mm')}` : '无截止时间' }}</small></div>
            <span class="of-pill" :class="isTodoOverdue(todo) ? 'tone-red' : 'tone-orange'">{{ isTodoOverdue(todo) ? '已逾期' : '待处理' }}</span>
          </button>
        </article>
        <article class="of-card">
          <div class="of-card__head"><h3><CalendarOutlined />今日日程 <small>（{{ selectedDateAgenda.length }} 项）</small></h3><button type="button" class="of-link" @click="openPath('/workbench/schedule')">查看日程 ›</button></div>
          <div v-if="!selectedDateAgenda.length" class="of-empty">今日暂无日程安排</div>
          <div class="wb-timeline">
            <div v-for="item in selectedDateAgenda.slice(0, 4)" :key="item.id" class="wb-timeline__row">
              <time>{{ item.startTime ? dayjs(item.startTime).format('HH:mm') : '全天' }}{{ item.endTime ? ` – ${dayjs(item.endTime).format('HH:mm')}` : '' }}</time>
              <div class="of-row__main"><b>{{ item.title }}</b><small>{{ agendaTypeText(item.calendarType) }}</small></div>
              <StatusTag :text="agendaStatusText(item)" :tone="agendaTone(item)" />
            </div>
          </div>
        </article>
        <article class="of-card">
          <div class="of-card__head"><h3><BarChartOutlined />本月考勤统计 <small>（{{ dayjs().format('M') }}月）</small></h3><button type="button" class="of-link" @click="openPath('/workbench/attendance')">详细数据 ›</button></div>
          <div class="wb-donut-row">
            <div class="of-donut" :style="{ background: attendanceDonut }"><div><small>出勤天数</small><b>{{ attendanceOverview.onDutyDays ?? 0 }}</b></div></div>
            <div class="of-legend">
              <div v-for="item in attendanceLegend" :key="item.label"><i :style="{ background: item.color }"></i>{{ item.label }}<b>{{ item.value }} {{ item.unit }}</b></div>
            </div>
          </div>
        </article>
      </div>

      <div class="of-grid-3">
        <article class="of-card">
          <div class="of-card__head">
            <h3><NotificationOutlined />近期公告</h3>
            <button type="button" class="of-link" @click="inboxTab === 'messages' ? openAllMessages() : openPath('/oa/notice')">查看更多 ›</button>
          </div>
          <div class="workbench-filter" role="group" aria-label="公告与消息切换">
            <button type="button" :class="{ active: inboxTab === 'notices' }" :aria-pressed="inboxTab === 'notices'" @click="inboxTab = 'notices'">公告</button>
            <button type="button" :class="{ active: inboxTab === 'messages' }" :aria-pressed="inboxTab === 'messages'" @click="inboxTab = 'messages'">消息<template v-if="unreadMessageCount"> · {{ unreadMessageCount }} 未读</template></button>
          </div>
          <template v-if="inboxTab === 'notices'">
            <div v-if="!visibleNotices.length" class="of-empty">暂无已发布公告</div>
            <button v-for="notice in visibleNotices" :key="notice.id" type="button" class="of-row wb-row-btn" @click="openPath('/oa/notice')">
              <span class="of-pill tone-green">公告</span>
              <div class="of-row__main"><b>{{ notice.title }}</b></div>
              <time class="of-muted">{{ notice.publishTime ? dayjs(notice.publishTime).format('MM-DD') : '' }}</time>
            </button>
          </template>
          <template v-else>
            <div v-if="!messagePreviews.length" class="of-empty">暂无消息</div>
            <button v-for="item in messagePreviews" :key="item.roomId" type="button" class="of-row wb-row-btn" @click="openMessage(item)">
              <div class="of-row__main"><b>{{ item.roomName }}</b><small>{{ item.content }}</small></div>
              <span v-if="item.unreadCount" class="of-pill tone-red">{{ item.unreadCount }}</span>
            </button>
          </template>
        </article>
        <article class="of-card">
          <div class="of-card__head"><h3><ThunderboltOutlined />快捷操作</h3><button type="button" class="of-link" @click="openPath('/function-map')">更多应用 ›</button></div>
          <div class="of-tile-grid">
            <button v-for="action in visibleQuickActions" :key="action.path" type="button" class="of-tile" :class="TILE_TONE[action.tone] || 'tone-green'" @click="openPath(action.path)">
              <component :is="action.icon" aria-hidden="true" /><span>{{ action.label }}</span>
            </button>
          </div>
        </article>
        <article class="of-card">
          <div class="of-card__head"><h3><FormOutlined />工作总结</h3><button type="button" class="of-link" @click="openPath('/workbench/reports')">全部总结 ›</button></div>
          <div class="wb-report">
            <div class="wb-report__cta">
              <b>本月工作总结</b>
              <small>总结完成情况、服务亮点与改进建议</small>
              <a-button type="primary" size="small" @click="openPath('/workbench/reports')">撰写总结 ›</a-button>
            </div>
            <div class="of-legend">
              <div>本月已提交<b>{{ reportSummary?.monthSubmittedCount ?? '--' }} 篇</b></div>
              <div>草稿待提交<b>{{ reportSummary?.draftCount ?? '--' }} 篇</b></div>
              <div>本月出勤<b>{{ attendanceOverview.onDutyDays ?? 0 }} 天</b></div>
            </div>
          </div>
        </article>
      </div>
    </template>

    <div v-if="!initialLoading" class="workbench-details-title"><span>岗位业务明细</span><h2>继续处理今天的工作</h2></div>

    <section v-if="!initialLoading" class="workbench-shell">
      <DepartmentHomeOverview
        v-if="workbenchProfile.department"
        class="workbench-shell__department"
        :show-header="false"
        :department="workbenchProfile.department"
        :metrics="departmentHomeMetrics"
        :data-state="departmentHomeDataState"
        :data-message="departmentDataMessage"
        :scope-label="workbenchProfile.hasManagementView ? '部门业务范围' : '当前岗位可用范围'"
        period-label="今日"
        :updated-at="refreshedAt === '--' ? '' : refreshedAt"
        :refreshing="loading"
        show-refresh
        @refresh="loadWorkbench()"
      />

      <WorkbenchModuleCard
        v-for="module in workbenchProfile.department ? [] : orderedPrimaryModules"
        :key="module.key"
        :title="module.title"
        :eyebrow="module.eyebrow"
      >
        <template #extra>
          <a-button type="link" @click="openPath(module.path)">查看全部</a-button>
        </template>

        <div class="module-summary-row">
          <OverviewMetricCard
            clickable
            :label="module.metricLabel"
            :value="module.metricValue"
            :helper="module.metricHelper"
            :tone="module.tone"
            @click="openPath(module.path)"
          />
        </div>

        <div class="workbench-list">
          <button
            v-for="item in module.items"
            :key="item.title"
            type="button"
            class="workbench-list-item"
            @click="openPath(item.path)"
          >
            <div>
              <strong>{{ item.title }}</strong>
              <span>{{ item.desc }}</span>
            </div>
            <StatusTag :text="item.tag" :tone="item.tone" />
          </button>
        </div>
      </WorkbenchModuleCard>

      <WorkbenchModuleCard v-if="!workbenchProfile.department && !orderedPrimaryModules.length" title="业务模块" eyebrow="提示">
        <a-empty description="当前角色暂无可展示的业务模块，可前往「全部功能」查看有权限的入口">
          <a-button type="primary" @click="openPath('/function-map')">前往全部功能</a-button>
        </a-empty>
      </WorkbenchModuleCard>

    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import {
  AppstoreOutlined, BarChartOutlined, BookOutlined, CalendarOutlined, CheckSquareOutlined, ClockCircleOutlined,
  FileTextOutlined, FormOutlined, HeartOutlined, MedicineBoxOutlined, NotificationOutlined,
  ScheduleOutlined, SendOutlined, TeamOutlined, ThunderboltOutlined, ToolOutlined, TrophyOutlined, UnorderedListOutlined
} from '@ant-design/icons-vue'
import OfficeHero from '../oa/office/OfficeHero.vue'
import '../oa/office/office.css'
import { getPointsSummary } from '../../api/me'
import { getOaWorkReportSummary } from '../../api/oa'
import type { OaWorkReportSummary } from '../../types'
import OverviewMetricCard from '../../components/smartcare/OverviewMetricCard.vue'
import StatusTag from '../../components/smartcare/StatusTag.vue'
import WorkbenchModuleCard from '../../components/smartcare/WorkbenchModuleCard.vue'
import DepartmentHomeOverview from '../../components/workbench/DepartmentHomeOverview.vue'
import { getOaTaskCalendar, getPortalSummary } from '../../api/oa'
import { getLeadPage } from '../../api/marketing'
import { getConversations } from '../../api/chat'
import { getAttendanceOverview, punchAttendance } from '../../api/schedule'
import type { AttendanceDashboardOverview, OaPortalSummary, OaTask } from '../../types'
import { useUserStore } from '../../stores/user'
import { useRouter } from 'vue-router'
import { resolveRouteAccess } from '../../utils/routeAccess'
import { resolveWorkbenchProfile, type WorkbenchKpiTone } from '../../workbench/model'
import {
  loadDepartmentWorkbenchSnapshot,
  type DepartmentWorkbenchSnapshot,
  type WorkbenchDataStatus
} from '../../workbench/dataSources'
import type { DepartmentHomeDataState, DepartmentHomeMetric } from '../../workbench/departmentHome'
import { QUICK_CHAT_OPEN_EVENT, type QuickChatPreviewItem } from '../../utils/quickChatPreview'
import { subscribeLiveSync } from '../../utils/liveSync'

type AttendanceAction = 'IN' | 'OUT' | 'START_LUNCH' | 'END_LUNCH' | 'START_OUTING' | 'END_OUTING'

type ModuleListItem = {
  title: string
  desc: string
  path: string
  tag: string
  tone: 'normal' | 'pending' | 'warning' | 'danger' | 'offline'
}

type PrimaryModule = {
  key: string
  title: string
  eyebrow: string
  path: string
  metricLabel: string
  metricHelper: string
  metricValue: string
  tone: 'default' | 'brand' | 'warning' | 'danger' | 'success'
  items: ModuleListItem[]
}

type SummaryMetricItem = {
  label: string
  value: string
  helper: string
  tone: WorkbenchKpiTone
  path: string
}

type WorkbenchPersona = {
  key: 'nursing' | 'medical' | 'logistics' | 'finance' | 'hr' | 'marketing' | 'manager'
  roleLabel: string
  subline: string
  modulePriority: string[]
  summaryMetrics: SummaryMetricItem[]
}

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const initialLoading = ref(true)
const loadFailures = ref<string[]>([])
const attendanceSubmitting = ref(false)
const attendancePending = ref<AttendanceAction | null>(null)
const refreshedAt = ref('--')
const REFRESH_INTERVAL_MS = 60_000
let refreshTimer: ReturnType<typeof setInterval> | null = null
let messageSyncUnsubscribe = () => {}
let refreshInFlight = false
const calendarRows = ref<OaTask[]>([])
const portalSummary = ref<OaPortalSummary | null>(null)
// 与市场工作台「待跟进」同口径：跟进日期已到的客户；无市场权限时为 null（显示 --）
const followupDueCount = ref<number | null>(null)
const messagePreviews = ref<QuickChatPreviewItem[]>([])
const departmentSnapshot = ref<DepartmentWorkbenchSnapshot | null>(null)
const departmentDataStatus = ref<WorkbenchDataStatus>('idle')
const departmentDataMessage = ref('')
const attendanceOverview = reactive<AttendanceDashboardOverview>({
  todayStatusLabel: '',
  expectedWorkStart: '',
  expectedWorkEnd: '',
  onDutyDays: 0,
  leaveDays: 0,
  abnormalCount: 0
})

function displayNumber(value?: number | null) {
  if (value === null || value === undefined || Number.isNaN(Number(value))) return '--'
  return Number(value).toLocaleString('zh-CN')
}

function displayAmount(value?: number | null) {
  if (value === null || value === undefined || Number.isNaN(Number(value))) return '--'
  return `¥ ${Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

const departmentHomeMetrics = computed<DepartmentHomeMetric[]>(() =>
  (departmentSnapshot.value?.metrics || []).map((item) => ({
    key: item.key,
    label: item.label,
    value: item.value,
    suffix: item.suffix ? ` ${item.suffix}` : '',
    helper: item.helper,
    path: item.path,
    tone: item.risk ? 'warning' : 'normal'
  }))
)

const departmentHomeDataState = computed<DepartmentHomeDataState>(() => {
  if (departmentDataStatus.value === 'idle') return 'loading'
  return departmentDataStatus.value
})

function departmentMetricNumber(key: string) {
  return departmentSnapshot.value?.metrics.find((item) => item.key === key)?.value
}

function canAccess(path: string) {
  return resolveRouteAccess(router, userStore.roles || [], path, userStore.pagePermissions || []).canAccess
}

function openPath(path: string) {
  if (!canAccess(path)) {
    message.warning('当前角色暂无该页面权限')
    return
  }
  router.push(path)
}

const normalizedRoles = computed(() => (userStore.roles || []).map((role) => String(role || '').toUpperCase()))
const workbenchProfile = computed(() => resolveWorkbenchProfile(userStore.roles || []))

function hasRoleFragment(fragment: string) {
  return normalizedRoles.value.some((role) => role.includes(fragment))
}

const workbenchPersona = computed<WorkbenchPersona>(() => {
  if (workbenchProfile.value.department === 'NURSING') {
    return {
      key: 'nursing',
      roleLabel: '护理执行角色',
      subline: '先完成护理执行、巡视交接和异常处置，再补充护理记录。',
      modulePriority: ['todo', 'clinical', 'approval', 'workorder', 'hr', 'customer', 'finance'],
      summaryMetrics: [
        {
          label: workbenchProfile.value.hasManagementView ? '部门待执行' : '我的待执行',
          value: displayNumber(departmentMetricNumber('pending')),
          helper: workbenchProfile.value.hasManagementView ? '护理部今日尚未完成的任务' : '今天分配给我的护理任务',
          tone: 'success',
          path: '/care/today'
        },
        {
          label: workbenchProfile.value.hasManagementView ? '待分配任务' : '异常任务',
          value: displayNumber(departmentMetricNumber(workbenchProfile.value.hasManagementView ? 'unassigned' : 'exception')),
          helper: workbenchProfile.value.hasManagementView ? '需要安排护理人员的任务' : '需要说明或协同处理的异常',
          tone: 'warning',
          path: '/care/today'
        }
      ]
    }
  }
  if (workbenchProfile.value.department === 'MEDICAL') {
    const managementView = workbenchProfile.value.hasManagementView
    return {
      key: 'medical',
      roleLabel: '医务处置角色',
      subline: '优先复核健康异常、医嘱执行和巡诊安排，避免问题跨班次积压。',
      modulePriority: ['todo', 'clinical', 'approval', 'customer', 'workorder', 'finance', 'hr'],
      summaryMetrics: [
        // 医务员工的个人巡诊统计接口尚未接入，不放占位卡
        ...(managementView
          ? [{
              label: '未闭环异常',
              value: displayNumber(departmentMetricNumber('abnormal')),
              helper: '异常复核与交班跟进',
              tone: 'warning' as const,
              path: '/medical-care/unified-task-center'
            }]
          : []),
        {
          label: managementView ? '待执行医嘱' : '我的今日用药登记',
          value: displayNumber(departmentMetricNumber(managementView ? 'orders' : 'medicationDone')),
          helper: managementView ? '巡诊、医嘱与医护任务' : '按当前登录账号登记人统计',
          tone: 'success',
          path: managementView ? '/medical-care/orders' : '/medical-care/medication-registration'
        }
      ]
    }
  }
  if (workbenchProfile.value.department === 'LOGISTICS' || hasRoleFragment('GUARD')) {
    const managementView = workbenchProfile.value.hasManagementView
    return {
      key: 'logistics',
      roleLabel: '后勤保障角色',
      subline: '先处理逾期维修、巡检和保障工单，再补充现场回执。',
      modulePriority: ['todo', 'workorder', 'approval', 'hr', 'customer', 'clinical', 'finance'],
      summaryMetrics: [
        {
          label: managementView ? '库存预警' : '今日出库',
          value: displayNumber(departmentMetricNumber(managementView ? 'stock' : 'outbound')),
          helper: managementView ? '低于安全库存的物资' : '机构当日出库数量，非个人绩效',
          tone: 'warning',
          path: managementView ? '/logistics/storage/alerts' : '/logistics/storage/outbound'
        },
        {
          label: managementView ? '设备临期' : '待送餐',
          value: displayNumber(departmentMetricNumber(managementView ? 'equipment' : 'delivery')),
          helper: managementView ? '需要安排保养的设备' : '机构当班协作任务',
          tone: managementView ? 'warning' : 'success',
          path: managementView ? '/logistics/maintenance/assets' : '/logistics/dining/delivery-plan'
        }
      ]
    }
  }
  if (workbenchProfile.value.department === 'FINANCE') {
    const managementView = workbenchProfile.value.hasManagementView
    return {
      key: 'finance',
      roleLabel: '财务运营角色',
      subline: '优先核对收费、账单和对账例外，避免回款与审批卡住。',
      modulePriority: ['todo', 'finance', 'approval', 'hr', 'customer', 'clinical', 'workorder'],
      summaryMetrics: [
        {
          label: managementView ? '欠费长者' : '票据未关联',
          value: displayNumber(departmentMetricNumber(managementView ? 'arrears' : 'invoiceGap')),
          helper: managementView ? '催缴和账户风险' : '交班前需要补齐',
          tone: 'warning',
          path: managementView ? '/finance/bills/follow-up' : '/finance/fees/payment-and-invoice'
        },
        {
          label: managementView ? '对账异常' : '账务异常',
          value: displayNumber(departmentMetricNumber('reconcile')),
          helper: '收款、退款与账单差异',
          tone: 'warning',
          path: '/finance/reconcile/issue-center'
        }
      ]
    }
  }
  if (workbenchProfile.value.department === 'HR') {
    const managementView = workbenchProfile.value.hasManagementView
    return {
      key: 'hr',
      roleLabel: '人事行政角色',
      subline: '先看考勤异常、班组排班和档案动作，再推进招聘与入转调离。',
      modulePriority: ['todo', 'hr', 'approval', 'finance', 'customer', 'clinical', 'workorder'],
      summaryMetrics: [
        {
          label: managementView ? '合同临期' : '机构今日培训',
          value: displayNumber(departmentMetricNumber(managementView ? 'contract' : 'training')),
          helper: managementView ? '未来预警周期内到期' : '机构协作参考，非本人绩效',
          tone: 'warning',
          path: managementView ? '/hr/profile/contract-reminders' : '/hr/overview'
        },
        {
          label: managementView ? '今日培训' : '社保事项',
          value: displayNumber(departmentMetricNumber(managementView ? 'training' : 'social')),
          helper: managementView ? '部门培训安排' : '进入已授权页面查看',
          tone: 'brand',
          path: managementView ? '/hr/development/records' : '/hr/profile/social-security-reminders'
        }
      ]
    }
  }
  if (workbenchProfile.value.department === 'MARKETING') {
    const managementView = workbenchProfile.value.hasManagementView
    return {
      key: 'marketing',
      roleLabel: '营销转化角色',
      subline: '先推进线索回访、参观和试住转化，再补营销任务和计划。',
      modulePriority: ['todo', 'customer', 'approval', 'finance', 'clinical', 'workorder', 'hr'],
      summaryMetrics: [
        {
          label: managementView ? '待签约客户' : '今日新增咨询',
          value: displayNumber(departmentMetricNumber(managementView ? 'sign' : 'consult')),
          helper: managementView ? '需要推进合同签署' : '机构新增咨询，非个人业绩',
          tone: 'success',
          path: managementView ? '/marketing/contracts/pending' : '/marketing/leads/all'
        },
        {
          label: managementView ? '本月成交' : '团队逾期回访',
          value: displayNumber(departmentMetricNumber(managementView ? 'deal' : 'overdue')),
          helper: managementView ? '部门成交口径' : '机构客户池风险',
          tone: managementView ? 'brand' : 'warning',
          path: managementView ? '/marketing/reports/conversion' : '/marketing/interactions/overdue'
        }
      ]
    }
  }
  return {
    key: 'manager',
    roleLabel: '综合管理角色',
    subline: '先处理审批、协同和关键待办，再下钻到具体业务模块。',
    modulePriority: ['todo', 'approval', 'clinical', 'customer', 'workorder', 'finance', 'hr'],
    summaryMetrics: [
      {
        label: '待执行医嘱',
        value: displayNumber(portalSummary.value?.pendingMedicalOrderCount),
        helper: '医护任务与用药提醒',
        tone: 'success',
        path: '/medical-care/care-task-board'
      },
      {
        label: '我的工单',
        value: displayNumber(departmentMetricNumber('maintenance')),
        helper: '维修、巡检与保障任务',
        tone: 'warning',
        path: '/logistics/task-center'
      }
    ]
  }
})








const todoFilter = ref<'all' | 'today' | 'overdue'>('all')
const todoFilterOptions = [
  { key: 'all', label: '全部' },
  { key: 'today', label: '今日到期' },
  { key: 'overdue', label: '已逾期' }
] as const

function isTodoOverdue(todo: OaPortalSummary['todos'][number]) {
  return todo.status !== 'DONE' && !!todo.dueTime && dayjs(todo.dueTime).isBefore(dayjs())
}

const visibleTodos = computed(() => (portalSummary.value?.todos || [])
  .filter((todo) => todo.status !== 'DONE')
  .filter((todo) => todoFilter.value === 'all' || (todoFilter.value === 'overdue'
    ? isTodoOverdue(todo)
    : !!todo.dueTime && dayjs(todo.dueTime).isSame(dayjs(), 'day')))
  .slice(0, 6))

const inboxTab = ref<'messages' | 'notices'>('notices')
const unreadMessageCount = computed(() => messagePreviews.value.reduce((sum, item) => sum + (item.unreadCount || 0), 0))

const visibleNotices = computed(() => (portalSummary.value?.notices || []).slice(0, 5))

function openAllMessages() {
  window.dispatchEvent(new CustomEvent(QUICK_CHAT_OPEN_EVENT))
}

function openMessage(item: QuickChatPreviewItem) {
  router.push({ path: '/workbench/chat', query: { c: item.roomId } })
}

function messageTimeText(value: string) {
  const parsed = dayjs(value)
  if (!parsed.isValid()) return ''
  return parsed.isSame(dayjs(), 'day') ? parsed.format('HH:mm') : parsed.format('MM-DD')
}

const quickActions = [
  { label: '发起申请', path: '/oa/approval', icon: SendOutlined, tone: 'green' },
  { label: '我的考勤', path: '/workbench/attendance', icon: ClockCircleOutlined, tone: 'blue' },
  { label: '规章制度', path: '/oa/document', icon: BookOutlined, tone: 'purple' },
  { label: '长者档案', path: '/elder/resident-360', icon: TeamOutlined, tone: 'green' },
  { label: '护理记录', path: '/care/service/nursing-records', icon: HeartOutlined, tone: 'coral' },
  { label: '健康监测', path: '/health/management/data', icon: MedicineBoxOutlined, tone: 'blue' },
  { label: '排班管理', path: '/care/scheduling/shift-calendar', icon: CalendarOutlined, tone: 'blue' },
  { label: '服务工单', path: '/logistics/task-center', icon: FileTextOutlined, tone: 'blue' },
  { label: '审批中心', path: '/workbench/approvals', icon: ScheduleOutlined, tone: 'green' },
  { label: '报修维护', path: '/logistics/assets/maintenance-record', icon: ToolOutlined, tone: 'blue' }
]
const visibleQuickActions = computed(() => quickActions.filter((action) => canAccess(action.path)).slice(0, 8))
const TILE_TONE: Record<string, string> = { green: 'tone-green', coral: 'tone-red', blue: 'tone-blue', amber: 'tone-orange', purple: 'tone-purple' }

const pointsSummary = ref<{ balance: number; rank?: number | string | null; rankTotal?: number | string } | null>(null)
const reportSummary = ref<OaWorkReportSummary | null>(null)
const todayRecord = computed(() => (attendanceOverview.days || []).find((d) => d.date === dayjs().format('YYYY-MM-DD')))
const overviewNumbers = computed(() => [
  { label: '待办事项', value: displayNumber(portalSummary.value?.openTodoCount), path: '/workbench/todo', tone: 'tone-green' },
  { label: '待我审批', value: displayNumber(portalSummary.value?.pendingApprovalCount), path: '/oa/approval?scope=PENDING_REVIEW', tone: 'tone-orange' },
  { label: '今日日程', value: selectedDateAgenda.value.length, path: '/workbench/schedule', tone: 'tone-blue' },
  { label: '考勤异常', value: attendanceOverview.abnormalCount ?? 0, path: '/workbench/attendance', tone: 'tone-purple' }
])
const attendanceLegend = computed(() => [
  { label: '正常出勤', value: attendanceOverview.onDutyDays ?? 0, unit: '天', color: '#1f9d74' },
  { label: '请假', value: attendanceOverview.leaveDays ?? 0, unit: '天', color: '#f0b429' },
  { label: '迟到', value: attendanceOverview.lateCount ?? 0, unit: '次', color: '#e5675a' },
  { label: '早退', value: attendanceOverview.earlyLeaveCount ?? 0, unit: '次', color: '#9aa5a0' },
  { label: '异常', value: attendanceOverview.abnormalCount ?? 0, unit: '次', color: '#7a55c7' }
])
/** 圆环：按天数/次数占比画扇区（没有数据时整圈灰色） */
const attendanceDonut = computed(() => {
  const parts = attendanceLegend.value.filter((p) => Number(p.value) > 0)
  const total = parts.reduce((sum, p) => sum + Number(p.value), 0)
  if (!total) return '#e9eeeb'
  let acc = 0
  return `conic-gradient(${parts.map((p) => { const from = acc; acc += (Number(p.value) / total) * 360; return `${p.color} ${from}deg ${acc}deg` }).join(', ')})`
})
function agendaStatusText(item: OaTask) {
  if (item.status === 'DONE') return '已完成'
  const start = item.startTime ? dayjs(item.startTime) : null
  const end = item.endTime ? dayjs(item.endTime) : null
  if (start && end && dayjs().isAfter(start) && dayjs().isBefore(end)) return '进行中'
  return start && dayjs().isBefore(start) ? '待开始' : '待完成'
}

const attendanceActionButtons = computed<AttendanceAction[]>(() => {
  const status = String(attendanceOverview.todayStatus || '').toUpperCase()
  if (!status || status === 'NOT_CHECKED_IN') return ['IN']
  if (status === 'ON_DUTY') return ['START_LUNCH', 'START_OUTING', 'OUT']
  if (status === 'LUNCH_BREAK') return ['END_LUNCH']
  if (status === 'OUTING') return ['END_OUTING']
  return []
})

const selectedDateAgenda = computed(() => {
  const target = dayjs().format('YYYY-MM-DD')
  return calendarRows.value.filter((item) => {
    const start = item.startTime || item.endTime
    return start ? dayjs(start).format('YYYY-MM-DD') === target : false
  })
})

function agendaTypeText(type?: string) {
  if (type === 'PERSONAL') return '个人'
  if (type === 'DAILY') return '日常'
  if (type === 'COLLAB') return '协同'
  return '工作'
}

function agendaTone(item: OaTask) {
  if (item.urgency === 'EMERGENCY') return 'danger'
  if (item.status === 'DONE') return 'normal'
  if (item.calendarType === 'COLLAB') return 'warning'
  return 'pending'
}

const clinicalModule = computed<PrimaryModule>(() => {
  if (workbenchPersona.value.key === 'nursing') {
    return {
      key: 'clinical',
      title: workbenchProfile.value.hasManagementView ? '护理部任务与风险' : '我的护理执行',
      eyebrow: '护理',
      path: '/care/today',
      metricLabel: workbenchProfile.value.hasManagementView ? '部门待执行任务' : '我的待执行任务',
      metricHelper: '护理计划、执行记录、交接与异常跟踪',
      metricValue: displayNumber(departmentMetricNumber('pending')),
      tone: 'success',
      items: [
        { title: '超时任务', desc: '优先处理并补充超时原因。', path: '/care/today?metric=overdue', tag: `超时 ${displayNumber(departmentMetricNumber('overdue'))}`, tone: 'danger' },
        { title: workbenchProfile.value.hasManagementView ? '待分配任务' : '异常任务', desc: workbenchProfile.value.hasManagementView ? '及时安排护理人员，避免任务滞留。' : '复核异常并完成交接。', path: workbenchProfile.value.hasManagementView ? '/care/today' : '/care/today?metric=exception', tag: `${workbenchProfile.value.hasManagementView ? '待分配' : '异常'} ${displayNumber(departmentMetricNumber(workbenchProfile.value.hasManagementView ? 'unassigned' : 'exception'))}`, tone: 'warning' }
      ]
    }
  }
  if (workbenchPersona.value.key === 'medical') {
    const managementView = workbenchProfile.value.hasManagementView
    return {
      key: 'clinical',
      title: managementView ? '医务部任务与风险' : '我的医务处置',
      eyebrow: '医务',
      path: managementView ? '/medical-care/unified-task-center' : '/medical-care/rounds',
      metricLabel: managementView ? '未闭环异常' : '个人待办数据',
      metricHelper: managementView ? '异常复核、医嘱执行和巡诊安排' : '个人任务统计接口尚未接入',
      metricValue: managementView ? displayNumber(departmentMetricNumber('abnormal')) : '--',
      tone: 'success',
      items: managementView ? [
        { title: '异常复核', desc: '优先处理生命体征与长者异常。', path: '/medical-care/unified-task-center', tag: `异常 ${displayNumber(departmentMetricNumber('abnormal'))}`, tone: 'danger' },
        { title: '待执行医嘱', desc: '协调医嘱、用药和巡诊动作。', path: '/medical-care/orders', tag: `待执行 ${displayNumber(departmentMetricNumber('orders'))}`, tone: 'pending' }
      ] : [
        { title: '今日巡诊', desc: '进入巡诊列表查看本人需要处理的事项。', path: '/medical-care/rounds', tag: '进入处理', tone: 'pending' },
        { title: '医嘱与用药', desc: '核对医嘱并完成用药登记。', path: '/medical-care/medication-registration', tag: `已登记 ${displayNumber(departmentMetricNumber('medicationDone'))}`, tone: 'normal' }
      ]
    }
  }
  return {
    key: 'clinical',
    title: '我的护理执行',
    eyebrow: '护理',
    path: '/medical-care/care-task-board',
    metricLabel: '护理与医护任务',
    metricHelper: '护理计划、巡视执行、交接与异常跟踪',
    metricValue: displayNumber(portalSummary.value?.pendingMedicalOrderCount),
    tone: 'success',
    items: [
      { title: '待执行护理任务', desc: '结合任务板统一处理。', path: '/medical-care/care-task-board', tag: `任务 ${displayNumber(portalSummary.value?.pendingMedicalOrderCount)}`, tone: 'pending' },
      { title: '健康异常提醒', desc: '优先处理生命体征和长者异常。', path: '/medical-care/unified-task-center', tag: `异常 ${displayNumber(portalSummary.value?.healthAbnormalCount)}`, tone: 'danger' }
    ]
  }
})

const financeModule = computed<PrimaryModule>(() => {
  const financePersona = workbenchPersona.value.key === 'finance'
  const managementView = workbenchProfile.value.hasManagementView
  return {
    key: 'finance',
    title: financePersona && managementView ? '财务部风险与审批' : '我的收费与账务',
    eyebrow: '财务',
    path: financePersona && !managementView ? '/finance/payments/cashier-desk' : '/finance/workbench',
    metricLabel: managementView ? '待审核费用' : '机构今日实收',
    metricHelper: managementView ? '折扣、退款与退住结算' : '当日机构收银口径，非个人业绩',
    metricValue: managementView ? displayNumber(departmentMetricNumber('pending')) : displayAmount(departmentMetricNumber('collected')),
    tone: 'warning',
    items: managementView ? [
      { title: '待审核费用', desc: '处理折扣、退款与退住结算审批。', path: '/workbench/approvals', tag: `待审 ${displayNumber(departmentMetricNumber('pending'))}`, tone: 'warning' },
      { title: '欠费与对账', desc: '先收口欠费和账务异常，再推进月结。', path: '/finance/reconcile/issue-center', tag: `异常 ${displayNumber(departmentMetricNumber('reconcile'))}`, tone: 'danger' }
    ] : [
      { title: '收银与收费', desc: '核对长者、账期和金额后完成收款。', path: '/finance/payments/cashier-desk', tag: '开始处理', tone: 'normal' },
      { title: '票据与交班', desc: '补齐票据并处理当日账务差异。', path: '/finance/payments/shift-close', tag: `异常 ${displayNumber(departmentMetricNumber('reconcile'))}`, tone: 'warning' }
    ]
  }
})

const hrModule = computed<PrimaryModule>(() => ({
  key: 'hr',
  title: '我的人事行政',
  eyebrow: '行政人事',
  path: '/hr/overview',
  metricLabel: '考勤与档案',
  metricHelper: '班组排班、员工档案与行政协同',
  metricValue: displayNumber(attendanceOverview.abnormalCount),
  tone: 'warning',
  items: [
    { title: '人力资源中心', desc: '招聘、档案、考勤和制度入口统一收口。', path: '/hr/overview', tag: '业务中心', tone: 'normal' },
    { title: '考勤与班组', desc: '处理打卡异常、班次调整和请假安排。', path: '/workbench/attendance', tag: `异常 ${displayNumber(attendanceOverview.abnormalCount)}`, tone: 'warning' }
  ]
}))

const allModules = computed<PrimaryModule[]>(() => ([
  {
    key: 'todo',
    title: '我的待办',
    eyebrow: '优先处理',
    path: '/workbench/todo',
    metricLabel: '待办总览',
    metricHelper: '开放待办与逾期事项',
    metricValue: displayNumber(portalSummary.value?.openTodoCount),
    tone: 'brand',
    items: [
      { title: '开放待办', desc: '先处理今天必须闭环的任务。', path: '/workbench/todo', tag: `待办 ${displayNumber(portalSummary.value?.openTodoCount)}`, tone: 'pending' },
      { title: '逾期提醒', desc: '逾期事项优先级更高。', path: '/workbench/todo', tag: `逾期 ${displayNumber(portalSummary.value?.overdueTodoCount)}`, tone: 'danger' }
    ]
  },
  {
    key: 'approval',
    title: '我的审批',
    eyebrow: '审批',
    path: '/workbench/approvals',
    metricLabel: '审批中心',
    metricHelper: '行政、财务、人事等审批',
    metricValue: displayNumber(portalSummary.value?.pendingApprovalCount),
    tone: 'warning',
    items: [
      { title: '待审批事项', desc: '涉及流程审批、报销与采购。', path: '/workbench/approvals', tag: `待审 ${displayNumber(portalSummary.value?.pendingApprovalCount)}`, tone: 'warning' },
      { title: '超时审批', desc: '关注长期未处理的流程。', path: '/workbench/approvals', tag: `超时 ${displayNumber(portalSummary.value?.approvalTimeoutCount)}`, tone: 'danger' }
    ]
  },
  clinicalModule.value,
  {
    key: 'customer',
    title: '我的客户跟进',
    eyebrow: '市场',
    path: '/marketing/workbench',
    metricLabel: '客户跟进',
    metricHelper: '咨询线索、预约参观与转化',
    metricValue: displayNumber(followupDueCount.value),
    tone: 'success',
    items: [
      { title: '待回访客户', desc: '咨询、试住和参观回访统一管理。', path: '/marketing/workbench', tag: `跟进 ${displayNumber(followupDueCount.value)}`, tone: 'pending' },
      { title: '营销渠道概览', desc: '查看来源和转化效率。', path: '/marketing/workbench', tag: '营销视图', tone: 'normal' }
    ]
  },
  {
    key: 'workorder',
    title: '我的工单',
    eyebrow: '后勤',
    path: '/logistics/task-center',
    metricLabel: '工单与保障',
    metricHelper: '维修、巡检、后勤保障',
    metricValue: displayNumber(departmentMetricNumber('maintenance')),
    tone: 'warning',
    items: [
      { title: '待处理工单', desc: '当前需要响应的维修与保障任务。', path: '/logistics/task-center', tag: `待办 ${displayNumber(departmentMetricNumber('maintenance'))}`, tone: 'warning' },
      { title: '逾期工单', desc: '逾期项需要优先催办。', path: '/logistics/task-center', tag: `逾期 ${displayNumber(departmentMetricNumber('overdue'))}`, tone: 'danger' }
    ]
  },
  financeModule.value,
  hrModule.value
]))

const orderedPrimaryModules = computed(() => {
  const rank = new Map(workbenchPersona.value.modulePriority.map((key, index) => [key, index]))
  return allModules.value
    .filter((module) => canAccess(module.path))
    .sort((left, right) => (rank.get(left.key) ?? 99) - (rank.get(right.key) ?? 99))
})

async function loadCalendar(): Promise<boolean> {
  if (!canAccess('/workbench/schedule')) {
    calendarRows.value = []
    return true
  }
  const today = dayjs().format('YYYY-MM-DD')
  try {
    const rows = await getOaTaskCalendar({ startDate: today, endDate: today })
    calendarRows.value = Array.isArray(rows) ? rows : []
    return true
  } catch {
    calendarRows.value = []
    return false
  }
}

async function refreshAttendance() {
  const data = await getAttendanceOverview({
    staffId: userStore.staffInfo?.id ? String(userStore.staffInfo.id) : undefined
  })
  Object.assign(attendanceOverview, data || {})
}

async function refreshMessagePreviews() {
  // 最近沟通来自服务端会话（只含我所在的会话），未读按已读游标计算
  messagePreviews.value = (await getConversations()).slice(0, 5).map((c) => ({
    roomId: c.id, roomName: c.title, senderName: '', content: c.lastMessagePreview || '暂无消息',
    createdAt: c.lastMessageAt || '', unreadCount: Number(c.unread || 0)
  }))
}

async function loadWorkbench(options: { silent?: boolean } = {}) {
  if (refreshInFlight) return
  refreshInFlight = true
  if (!options.silent) loading.value = true
  if (workbenchProfile.value.department) departmentDataStatus.value = 'loading'
  const failures: string[] = []
  // 403 表示当前角色无该数据权限，属于正常裁剪，不计入“加载失败”；其余（网络/5xx）才提示
  const capture = (label: string) => (error: any) => {
    if (error?.response?.status !== 403) failures.push(label)
    return null
  }
  try {
    const [portal, departmentResult, , calendarOk] = await Promise.all([
      getPortalSummary({ silent403: true, silentError: true, params: { scope: 'PERSONAL' } }).catch(capture('工作待办')),
      loadDepartmentWorkbenchSnapshot(workbenchProfile.value, canAccess, {
        staffId: userStore.staffInfo?.id,
        username: userStore.staffInfo?.username
      }),
      refreshAttendance().catch(capture('考勤')),
      loadCalendar(),
      refreshMessagePreviews().catch(capture('消息')),
      getPointsSummary().then((data) => { pointsSummary.value = data }).catch(() => { pointsSummary.value = null }),
      getOaWorkReportSummary({ mineOnly: true }).then((data) => { reportSummary.value = data }).catch(() => { reportSummary.value = null }),
      canAccess('/marketing/workbench')
        ? getLeadPage({ pageNo: 1, pageSize: 1, followupDueOnly: true, followupDateTo: dayjs().format('YYYY-MM-DD') }, { silent403: true, silentError: true })
          .then((page) => { followupDueCount.value = Number(page.total ?? 0) })
          .catch(() => { followupDueCount.value = null })
        : Promise.resolve()
    ])
    portalSummary.value = portal
    departmentDataStatus.value = departmentResult.status
    departmentSnapshot.value = departmentResult.snapshot || null
    departmentDataMessage.value = departmentResult.message || ''
    if (departmentResult.status === 'error') failures.push(`${workbenchProfile.value.departmentLabel}业务数据`)
    if (calendarOk === false) failures.push('日程')
    loadFailures.value = failures
    refreshedAt.value = new Date().toLocaleString('zh-CN', {
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } finally {
    refreshInFlight = false
    if (!options.silent) loading.value = false
    initialLoading.value = false
  }
}

function attendanceActionLabel(action: AttendanceAction) {
  switch (action) {
    case 'IN': return '上班打卡'
    case 'OUT': return '下班打卡'
    case 'START_LUNCH': return '开始午休'
    case 'END_LUNCH': return '结束午休'
    case 'START_OUTING': return '外出登记'
    case 'END_OUTING': return '外出返回'
  }
}

async function handleAttendanceAction(action: AttendanceAction) {
  if (attendanceSubmitting.value) return
  attendanceSubmitting.value = true
  attendancePending.value = action
  try {
    await punchAttendance(action)
    message.success(action === 'IN' ? '上班打卡成功' : action === 'OUT' ? '下班打卡成功' : '考勤状态已更新')
    await refreshAttendance()
  } catch (error: any) {
    message.error(error?.message || '打卡失败，请稍后重试')
  } finally {
    attendanceSubmitting.value = false
    attendancePending.value = null
  }
}

function handleVisibilityChange() {
  // 标签页重新可见时立即补一次刷新，隐藏时不做无谓请求
  if (document.visibilityState === 'visible') loadWorkbench({ silent: true })
}

onMounted(() => {
  loadWorkbench()
  messageSyncUnsubscribe = subscribeLiveSync((payload) => {
    if (payload.url === '/local/quick-chat') refreshMessagePreviews().catch(() => {})
  })
  refreshTimer = setInterval(() => {
    if (document.visibilityState === 'visible') loadWorkbench({ silent: true })
  }, REFRESH_INTERVAL_MS)
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

onUnmounted(() => {
  messageSyncUnsubscribe()
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})
</script>

<style scoped>
.unified-workbench { display: grid; gap: 16px; }
.wb-overview { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 10px; }
.wb-overview button { display: grid; justify-items: center; gap: 4px; padding: 14px 4px; border: 0; border-radius: 12px; cursor: pointer; }
.wb-overview b { font-size: 28px; line-height: 1.1; }
.wb-overview span { color: #33433a; font-size: 13px; }
.wb-attendance { display: grid; gap: 6px; padding: 14px 16px; border-radius: 12px; background: #eef8f2; color: #33433a; }
.wb-attendance--warn { background: #fff6ea; }
.wb-attendance > b { font-size: 18px; color: #17734f; }
.wb-attendance--warn > b { color: #c27c1a; }
.wb-attendance em { margin-left: 8px; font-style: normal; color: #6b7a74; }
.wb-points { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.wb-points > div { display: grid; gap: 4px; padding: 14px; border-radius: 12px; background: #fdf7ec; }
.wb-points > div + div { background: #f4f1fb; }
.wb-points b { font-size: 24px; color: #1d3a2f; }
.wb-points small { color: #6b7a74; font-weight: 400; font-size: 12px; }
.wb-row-btn { width: 100%; border: 0; border-bottom: 1px solid #f0f3f1; background: none; text-align: left; cursor: pointer; }
.wb-row-icon { width: 34px; height: 34px; font-size: 16px; }
.wb-timeline__row { display: flex; align-items: center; gap: 12px; padding: 9px 0 9px 14px; border-left: 2px solid #d7ebe0; }
.wb-timeline__row time { flex: none; width: 96px; color: #33433a; font-size: 13px; font-variant-numeric: tabular-nums; }
.wb-donut-row { display: flex; align-items: center; gap: 18px; }
.wb-report { display: grid; grid-template-columns: minmax(0, 1fr) minmax(0, 1fr); gap: 14px; align-items: center; }
.wb-report__cta { display: grid; gap: 6px; padding: 14px; border-radius: 12px; background: #fdf7ec; }
.wb-report__cta small { color: #6b7a74; }
.unified-workbench {
  display: grid;
  gap: 16px;
  min-width: 0;
  color: var(--ink);
}

.workbench-load-alert {
  border-radius: 14px;
}

.workbench-skeleton {
  display: grid;
  gap: 18px;
}

.workbench-greeting { display: flex; align-items: center; justify-content: space-between; gap: 16px; padding: 14px 18px; border: 1px solid #e8ece8; border-radius: 12px; background: var(--surface); }
.workbench-greeting__copy { min-width: 0; }
.workbench-greeting h1 { margin: 0; color: var(--ink); font-size: 20px; font-weight: 700; line-height: 1.35; }
.workbench-greeting p { margin: 2px 0 0; overflow: hidden; color: var(--muted); font-size: 13px; text-overflow: ellipsis; white-space: nowrap; }
.workbench-greeting__actions { display: flex; flex: 0 0 auto; flex-wrap: wrap; align-items: center; justify-content: flex-end; gap: 8px; }
.workbench-greeting__actions small { color: var(--muted); font-size: 12px; font-variant-numeric: tabular-nums; }

.workbench-kpis { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 12px; }
.workbench-kpi { display: flex; align-items: flex-start; gap: 14px; min-width: 0; min-height: 104px; padding: 16px; border: 1px solid #e6ebe7; border-radius: 12px; background: var(--surface); box-shadow: 0 5px 18px rgba(35, 51, 43, .045); text-align: left; cursor: pointer; transition: border-color .18s ease, box-shadow .18s ease; }
.workbench-kpi:hover { border-color: #9bcdbd; box-shadow: 0 12px 30px rgba(36, 48, 39, .13); }
.workbench-kpi__icon { display: grid; place-items: center; flex: 0 0 50px; height: 50px; border-radius: 13px; background: #e4f3ed; color: #137e60; font-size: 25px; }
.workbench-kpi--warning .workbench-kpi__icon { background: #fff0dc; color: #ca7523; }
.workbench-kpi--danger .workbench-kpi__icon { background: #fde9e7; color: #d74343; }
.workbench-kpi__body { display: grid; gap: 3px; min-width: 0; flex: 1; color: #44534b; font-size: 14px; }
.workbench-kpi__body strong { color: #192c24; font: 700 29px/1.12 "Noto Serif SC", "Songti SC", serif; font-variant-numeric: tabular-nums; }
.workbench-kpi--danger .workbench-kpi__body strong { color: #c83232; }
.workbench-kpi__body small { color: #69766e; font-size: 12px; line-height: 1.45; }
.workbench-kpi__arrow { align-self: center; color: #4a8f77; }

.workbench-main { display: grid; grid-template-columns: minmax(0, 2fr) minmax(300px, 1fr); align-items: start; gap: 12px; }
.workbench-column { display: grid; gap: 12px; min-width: 0; }
.workbench-panel { min-width: 0; padding: 16px; border: 1px solid #e8ece8; border-radius: 12px; background: var(--surface); box-shadow: 0 5px 18px rgba(35, 51, 43, .045); }
.workbench-panel__head { display: flex; align-items: center; justify-content: space-between; gap: 10px; margin-bottom: 13px; }
.workbench-panel__head h2 { margin: 0; padding-left: 10px; border-left: 3px solid #16866b; color: var(--ink); font-size: 17px; line-height: 1.25; }
.workbench-panel__head h2 small { color: var(--muted); font-size: 13px; font-weight: 400; }
.workbench-panel__head > button { display: inline-flex; align-items: center; gap: 4px; flex: 0 0 auto; padding: 5px 0; border: 0; background: none; color: #326c5d; font-size: 12px; cursor: pointer; }
.workbench-filter { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 8px; }
.workbench-filter button { min-height: 30px; padding: 4px 11px; border: 1px solid transparent; border-radius: 7px; background: #f4f6f4; color: #536158; font-size: 12px; cursor: pointer; }
.workbench-filter button.active { background: #dcefe7; color: #176b52; font-weight: 700; }
.workbench-todo-list,
.workbench-notice-list,
.workbench-agenda { display: grid; }
.workbench-todo-row,
.workbench-notice-row { display: flex; align-items: center; gap: 10px; width: 100%; min-width: 0; min-height: 43px; padding: 7px 3px; border: 0; border-bottom: 1px solid #eef0ec; background: transparent; text-align: left; cursor: pointer; }
.workbench-todo-row:last-child,
.workbench-notice-row:last-child { border-bottom: 0; }
.workbench-todo-row__mark { flex: 0 0 16px; height: 16px; border: 1px solid #bccbc1; border-radius: 4px; }
.workbench-todo-row__title,
.workbench-notice-row > span:nth-child(2) { flex: 1; min-width: 0; overflow: hidden; color: #26382f; font-size: 13px; text-overflow: ellipsis; white-space: nowrap; }
.workbench-todo-row__status { flex: 0 0 auto; padding: 3px 7px; border-radius: 5px; background: #e3f2eb; color: #167453; font-size: 11px; }
.workbench-todo-row__status.overdue { background: #fde8e5; color: #c43737; }
.workbench-todo-row time,
.workbench-notice-row time { flex: 0 0 auto; color: #738077; font-size: 11px; font-variant-numeric: tabular-nums; }
.workbench-shortcuts { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 9px; }
.workbench-shortcut { display: grid; place-items: center; align-content: center; gap: 10px; min-width: 0; min-height: 96px; padding: 9px 4px; border: 1px solid #eef2ee; border-radius: 10px; background: #f1f8f5; color: #11765a; font-size: 13px; cursor: pointer; transition: background .18s ease, border-color .18s ease; }
.workbench-shortcut:hover { border-color: #abd4c3; background: #e9f5ee; }
.workbench-shortcut .anticon { font-size: 25px; }
.workbench-shortcut--coral { background: #fff4ef; color: #c65b4e; }
.workbench-shortcut--blue { background: #eff6fb; color: #236fa9; }
.workbench-shortcut--amber { background: #fff6e8; color: #bb752d; }
.workbench-notice-row__dot { flex: 0 0 6px; height: 6px; border-radius: 50%; background: #e69751; }
.workbench-message-list { display: grid; }
.workbench-message-row { display: flex; align-items: center; gap: 9px; width: 100%; min-width: 0; min-height: 54px; padding: 7px 2px; border: 0; border-bottom: 1px solid #eef0ec; background: transparent; text-align: left; cursor: pointer; }
.workbench-message-row:last-child { border-bottom: 0; }
.workbench-message-row__avatar { display: grid; place-items: center; flex: 0 0 34px; height: 34px; border-radius: 50%; background: #e6f1fb; color: #2871aa; font-size: 13px; font-weight: 700; }
.workbench-message-row__body { display: grid; gap: 3px; flex: 1; min-width: 0; }
.workbench-message-row__meta { display: flex; align-items: center; gap: 8px; min-width: 0; }
.workbench-message-row__meta strong { flex: 1; min-width: 0; overflow: hidden; color: #26382f; font-size: 13px; text-overflow: ellipsis; white-space: nowrap; }
.workbench-message-row__meta time { flex: 0 0 auto; color: #809087; font-size: 10px; font-variant-numeric: tabular-nums; }
.workbench-message-row__preview { overflow: hidden; color: #718078; font-size: 11px; text-overflow: ellipsis; white-space: nowrap; }
.workbench-message-row__unread { flex: 0 0 auto; min-width: 18px; padding: 1px 5px; border-radius: 999px; background: #ef4444; color: #fff; font-size: 10px; line-height: 16px; text-align: center; white-space: nowrap; }
.workbench-agenda__row { display: grid; grid-template-columns: 54px minmax(0, 1fr) auto; align-items: center; gap: 10px; min-height: 42px; border-bottom: 1px solid #eef0ec; font-size: 12px; }
.workbench-agenda__row:last-child { border-bottom: 0; }
.workbench-agenda__row time { color: #707e72; font-variant-numeric: tabular-nums; }
.workbench-agenda__row > span:nth-child(2) { min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.workbench-coordination-link { display: flex; align-items: baseline; gap: 6px; width: 100%; margin-top: 10px; padding: 10px 3px 0; border: 0; border-top: 1px solid #eef0ec; background: transparent; color: #536158; font-size: 13px; text-align: left; cursor: pointer; }
.workbench-coordination-link strong { color: #187c60; font-size: 17px; font-variant-numeric: tabular-nums; }
.workbench-coordination-link .anticon { margin-left: auto; color: #a5b3a8; font-size: 11px; }
.workbench-empty { padding: 22px 8px; color: #68766c; font-size: 13px; }
.workbench-details-title { padding: 10px 2px 0; }
.workbench-details-title span { color: #18705d; font-size: 12px; font-weight: 700; letter-spacing: .1em; }
.workbench-details-title h2 { margin: 5px 0 0; color: var(--ink); font-size: 20px; }

.unified-workbench button:focus-visible { outline: 3px solid #25836c; outline-offset: 2px; }

:global([data-theme='dark']) .workbench-kpi,
:global([data-theme='dark']) .workbench-panel { background: var(--surface); border-color: var(--border); }
:global([data-theme='dark']) .workbench-kpi__body,
:global([data-theme='dark']) .workbench-kpi__body strong,
:global([data-theme='dark']) .workbench-todo-row__title,
:global([data-theme='dark']) .workbench-notice-row > span:nth-child(2),
:global([data-theme='dark']) .workbench-message-row__meta strong { color: var(--ink); }

.workbench-shell__department {
  grid-column: 1 / -1;
}

.workbench-shell {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.module-summary-row,
.workbench-list {
  display: grid;
  gap: 12px;
}

.workbench-list-item {
  border: 1px solid var(--border);
  border-radius: 18px;
  background: #ffffff;
}

.workbench-list-item:hover {
  border-color: rgba(var(--primary-rgb), 0.36);
  box-shadow: var(--shadow-sm);
}

.workbench-list-item {
  width: 100%;
  text-align: left;
  cursor: pointer;
}

.workbench-list-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 15px 16px;
}

.workbench-list-item strong {
  color: var(--ink);
}

.workbench-list-item span {
  color: var(--muted);
  font-size: 12px;
}

@media (max-width: 1280px) {
  .workbench-main { grid-template-columns: minmax(0, 1fr); }
  .workbench-shell {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .workbench-greeting { flex-direction: column; align-items: flex-start; }
  .workbench-greeting__actions { justify-content: flex-start; }
  .workbench-greeting p { white-space: normal; }
  .workbench-shell {
    grid-template-columns: 1fr;
  }

  .workbench-shortcuts { grid-template-columns: repeat(4, minmax(0, 1fr)); }
}

@media (max-width: 520px) {
  .workbench-kpis { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .workbench-kpi { gap: 8px; min-height: 96px; padding: 12px; }
  .workbench-kpi__icon { flex-basis: 35px; height: 35px; font-size: 18px; }
  .workbench-kpi__body { font-size: 12px; }
  .workbench-kpi__body strong { font-size: 24px; }
  .workbench-kpi__body small { font-size: 10px; }
  .workbench-kpi__arrow { display: none; }
  .workbench-shortcuts { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}

@media (prefers-reduced-motion: reduce) {
  .workbench-kpi,
.workbench-shortcut { transition: none; }
}
</style>
