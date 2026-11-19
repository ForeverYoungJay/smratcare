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

    <header class="workbench-greeting">
      <div class="workbench-greeting__copy">
        <h1>{{ workbenchHeadline }}</h1>
        <p :title="workbenchSubline">{{ workbenchSubline }}</p>
      </div>
      <div class="workbench-greeting__actions">
        <small>{{ todayDateLabel }} · 更新于 {{ refreshedAt }}</small>
        <a-button
          v-for="action in attendanceActionButtons"
          :key="action"
          :loading="attendancePending === action"
          :disabled="attendanceSubmitting && attendancePending !== action"
          @click="handleAttendanceAction(action)"
        >{{ attendanceActionLabel(action) }}</a-button>
        <a-button :loading="loading" @click="loadWorkbench()">刷新</a-button>
      </div>
    </header>

    <div v-if="initialLoading" class="workbench-skeleton">
      <a-skeleton active :title="false" :paragraph="{ rows: 3 }" />
      <a-skeleton active :title="false" :paragraph="{ rows: 6 }" />
    </div>

    <section v-if="!initialLoading" class="workbench-kpis" aria-label="今日关键指标">
      <button
        v-for="item in todayFocusCards"
        :key="item.key"
        type="button"
        class="workbench-kpi"
        :class="`workbench-kpi--${item.tone}`"
        @click="openPath(item.path)"
      >
        <span class="workbench-kpi__icon" aria-hidden="true"><component :is="item.icon" /></span>
        <span class="workbench-kpi__body"><span>{{ item.label }}</span><strong>{{ item.value }}</strong><small>{{ item.helper }}</small></span>
        <RightOutlined class="workbench-kpi__arrow" aria-hidden="true" />
      </button>
    </section>

    <section v-if="!initialLoading" class="workbench-main" aria-label="工作台总览">
      <div class="workbench-column">
        <article class="workbench-panel workbench-panel--todos">
          <div class="workbench-panel__head"><h2>我的待办 <small>({{ portalSummary?.openTodoCount ?? 0 }})</small></h2><button type="button" @click="openPath('/workbench/todo')">查看全部 <RightOutlined aria-hidden="true" /></button></div>
          <div class="workbench-filter" role="group" aria-label="待办筛选">
            <button v-for="filter in todoFilterOptions" :key="filter.key" type="button" :class="{ active: todoFilter === filter.key }" :aria-pressed="todoFilter === filter.key" @click="todoFilter = filter.key">{{ filter.label }}</button>
          </div>
          <div v-if="visibleTodos.length" class="workbench-todo-list">
            <button v-for="todo in visibleTodos" :key="todo.id" type="button" class="workbench-todo-row" @click="openPath('/workbench/todo')">
              <span class="workbench-todo-row__mark" aria-hidden="true"></span>
              <span class="workbench-todo-row__title">{{ todo.title }}</span>
              <span class="workbench-todo-row__status" :class="{ overdue: isTodoOverdue(todo) }">{{ isTodoOverdue(todo) ? '已逾期' : '待处理' }}</span>
              <time v-if="todo.dueTime" :datetime="todo.dueTime">{{ dayjs(todo.dueTime).format('MM-DD HH:mm') }}</time>
            </button>
          </div>
          <div v-else class="workbench-empty">{{ portalSummary?.openTodoCount ? '当前筛选暂无待办，查看全部可继续处理。' : '今天没有待处理事项。' }}</div>
          <button type="button" class="workbench-coordination-link" @click="openPath('/oa/staff-collaboration')">
            <span>跨部门协同</span><strong>{{ portalSummary?.ongoingTaskCount ?? 0 }}</strong><span>项进行中</span><RightOutlined aria-hidden="true" />
          </button>
        </article>
        <article class="workbench-panel workbench-panel--quick">
          <div class="workbench-panel__head"><h2>常用入口</h2><button type="button" @click="openPath('/function-map')">全部功能 <RightOutlined aria-hidden="true" /></button></div>
          <div class="workbench-shortcuts">
            <button v-for="action in visibleQuickActions" :key="action.path" type="button" class="workbench-shortcut" :class="`workbench-shortcut--${action.tone}`" @click="openPath(action.path)">
              <component :is="action.icon" aria-hidden="true" /><span>{{ action.label }}</span>
            </button>
          </div>
        </article>
      </div>
      <div class="workbench-column">
        <article class="workbench-panel workbench-panel--agenda">
          <div class="workbench-panel__head"><h2>今日工作日程</h2><button type="button" @click="openPath('/workbench/schedule')">完整日历 <RightOutlined aria-hidden="true" /></button></div>
          <div class="workbench-agenda">
            <div v-for="item in selectedDateAgenda.slice(0, 5)" :key="item.id" class="workbench-agenda__row">
              <time :datetime="item.startTime || item.endTime">{{ item.startTime ? dayjs(item.startTime).format('HH:mm') : '全天' }}</time>
              <span>{{ item.title }}</span><StatusTag :text="agendaTypeText(item.calendarType)" :tone="agendaTone(item)" />
            </div>
            <div v-if="!selectedDateAgenda.length" class="workbench-empty">今日暂无日程安排，可在日历中添加。</div>
          </div>
        </article>
        <article class="workbench-panel workbench-panel--inbox">
          <div class="workbench-panel__head">
            <h2>消息与公告</h2>
            <button type="button" @click="inboxTab === 'messages' ? openAllMessages() : openPath('/oa/notice')">全部 <RightOutlined aria-hidden="true" /></button>
          </div>
          <div class="workbench-filter" role="group" aria-label="消息与公告切换">
            <button type="button" :class="{ active: inboxTab === 'messages' }" :aria-pressed="inboxTab === 'messages'" @click="inboxTab = 'messages'">消息<template v-if="unreadMessageCount"> · {{ unreadMessageCount }} 未读</template></button>
            <button type="button" :class="{ active: inboxTab === 'notices' }" :aria-pressed="inboxTab === 'notices'" @click="inboxTab = 'notices'">公告</button>
          </div>
          <template v-if="inboxTab === 'messages'">
            <div v-if="messagePreviews.length" class="workbench-message-list" role="list">
              <button
                v-for="item in messagePreviews"
                :key="item.roomId"
                type="button"
                class="workbench-message-row"
                :aria-label="`${item.roomName}，${item.senderName}：${item.content}${item.unreadCount ? `，${item.unreadCount}条未读` : ''}`"
                @click="openMessage(item)"
              >
                <span class="workbench-message-row__avatar" aria-hidden="true">{{ item.senderName.slice(0, 1) }}</span>
                <span class="workbench-message-row__body">
                  <span class="workbench-message-row__meta"><strong>{{ item.roomName }}</strong><time :datetime="item.createdAt">{{ messageTimeText(item.createdAt) }}</time></span>
                  <span class="workbench-message-row__preview">{{ item.senderName }}：{{ item.content }}</span>
                </span>
                <span v-if="item.unreadCount" class="workbench-message-row__unread" role="status" aria-atomic="true">{{ item.unreadCount }}</span>
              </button>
            </div>
            <div v-else class="workbench-empty">暂无消息，新的协作消息会显示在这里。</div>
          </template>
          <template v-else>
            <div v-if="visibleNotices.length" class="workbench-notice-list">
              <button v-for="notice in visibleNotices" :key="notice.id" type="button" class="workbench-notice-row" @click="openPath('/oa/notice')">
                <span class="workbench-notice-row__dot" aria-hidden="true"></span><span>{{ notice.title }}</span><time v-if="notice.publishTime" :datetime="notice.publishTime">{{ dayjs(notice.publishTime).format('MM-DD') }}</time>
              </button>
            </div>
            <div v-else class="workbench-empty">暂无已发布公告。</div>
          </template>
        </article>
      </div>
    </section>

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
import { computed, onMounted, onUnmounted, reactive, ref, type Component } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import {
  AlertOutlined, CalendarOutlined, ClockCircleOutlined,
  FileTextOutlined, HeartOutlined, MedicineBoxOutlined, RightOutlined,
  ScheduleOutlined, TeamOutlined, ToolOutlined, UnorderedListOutlined
} from '@ant-design/icons-vue'
import OverviewMetricCard from '../../components/smartcare/OverviewMetricCard.vue'
import StatusTag from '../../components/smartcare/StatusTag.vue'
import WorkbenchModuleCard from '../../components/smartcare/WorkbenchModuleCard.vue'
import DepartmentHomeOverview from '../../components/workbench/DepartmentHomeOverview.vue'
import { getOaTaskCalendar, getPortalSummary, getQuickChatState } from '../../api/oa'
import { getAttendanceOverview, punchAttendance } from '../../api/schedule'
import type { AttendanceDashboardOverview, OaPortalSummary, OaTask } from '../../types'
import { useUserStore } from '../../stores/user'
import { useRouter } from 'vue-router'
import { resolveRouteAccess } from '../../utils/routeAccess'
import { buildWorkbenchKpis, resolveWorkbenchProfile, type WorkbenchKpiTone } from '../../workbench/model'
import {
  loadDepartmentWorkbenchSnapshot,
  type DepartmentWorkbenchSnapshot,
  type WorkbenchDataStatus
} from '../../workbench/dataSources'
import type { DepartmentHomeDataState, DepartmentHomeMetric } from '../../workbench/departmentHome'
import { buildQuickChatPreviews, QUICK_CHAT_OPEN_EVENT, type QuickChatPreviewItem } from '../../utils/quickChatPreview'
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

const roleLabel = computed(() => workbenchProfile.value.roleLabel)

const staffDisplayName = computed(() =>
  String(userStore.staffInfo?.realName || userStore.staffInfo?.username || '当前员工')
)

const attendanceWindowLabel = computed(() => {
  const start = attendanceOverview.expectedWorkStart || '--:--'
  const end = attendanceOverview.expectedWorkEnd || '--:--'
  return `今日班次 ${start} - ${end}`
})

const todayDateLabel = computed(() => dayjs().format('YYYY年M月D日'))
const workbenchHeadline = computed(() => {
  const hour = dayjs().hour()
  return `${staffDisplayName.value}，${hour < 11 ? '早上' : hour < 18 ? '下午' : '晚上'}好！`
})

const workbenchSubline = computed(() =>
  `${roleLabel.value} · ${attendanceWindowLabel.value} · ${workbenchPersona.value.subline}`
)

const kpiIcons: Record<string, Component> = { overdue: ClockCircleOutlined, health: AlertOutlined, approvals: ScheduleOutlined }

const todayFocusCards = computed(() => buildWorkbenchKpis(workbenchPersona.value.key, workbenchPersona.value.summaryMetrics, {
  overdue: displayNumber(portalSummary.value?.overdueTodoCount),
  healthRisk: displayNumber(portalSummary.value?.healthAbnormalCount),
  healthPath: canAccess('/medical-care/unified-task-center') ? '/medical-care/unified-task-center' : '/workbench/todo',
  approvals: displayNumber(portalSummary.value?.pendingApprovalCount)
}).map((item) => ({ ...item, icon: kpiIcons[item.key] ?? UnorderedListOutlined })))

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

const inboxTab = ref<'messages' | 'notices'>('messages')
const unreadMessageCount = computed(() => messagePreviews.value.reduce((sum, item) => sum + (item.unreadCount || 0), 0))

const visibleNotices = computed(() => (portalSummary.value?.notices || []).slice(0, 5))

function quickChatIdentityAliases() {
  return [userStore.staffInfo?.id, userStore.staffInfo?.username, userStore.staffInfo?.staffNo, 'me']
}

function openAllMessages() {
  window.dispatchEvent(new CustomEvent(QUICK_CHAT_OPEN_EVENT))
}

function openMessage(item: QuickChatPreviewItem) {
  item.unreadCount = 0
  window.dispatchEvent(new CustomEvent(QUICK_CHAT_OPEN_EVENT, { detail: { roomId: item.roomId } }))
}

function messageTimeText(value: string) {
  const parsed = dayjs(value)
  if (!parsed.isValid()) return ''
  return parsed.isSame(dayjs(), 'day') ? parsed.format('HH:mm') : parsed.format('MM-DD')
}

const quickActions = [
  { label: '长者档案', path: '/elder/resident-360', icon: TeamOutlined, tone: 'green' },
  { label: '护理记录', path: '/care/service/nursing-records', icon: HeartOutlined, tone: 'coral' },
  { label: '健康监测', path: '/health/management/data', icon: MedicineBoxOutlined, tone: 'blue' },
  { label: '排班管理', path: '/care/scheduling/shift-calendar', icon: CalendarOutlined, tone: 'blue' },
  { label: '服务工单', path: '/logistics/task-center', icon: FileTextOutlined, tone: 'blue' },
  { label: '物资申领', path: '/oa/approval', icon: UnorderedListOutlined, tone: 'amber' },
  { label: '审批中心', path: '/workbench/approvals', icon: ScheduleOutlined, tone: 'green' },
  { label: '报修维护', path: '/logistics/assets/maintenance-record', icon: ToolOutlined, tone: 'blue' }
]
const visibleQuickActions = computed(() => quickActions.filter((action) => canAccess(action.path)))

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
    metricValue: displayNumber(portalSummary.value?.suggestionCount),
    tone: 'success',
    items: [
      { title: '待回访客户', desc: '咨询、试住和参观回访统一管理。', path: '/marketing/workbench', tag: `跟进 ${displayNumber(portalSummary.value?.suggestionCount)}`, tone: 'pending' },
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
    staffId: (userStore.staffInfo?.id as string | number | undefined) || undefined
  })
  Object.assign(attendanceOverview, data || {})
}

async function refreshMessagePreviews() {
  const quickChat = await getQuickChatState()
  messagePreviews.value = buildQuickChatPreviews(quickChat?.stateJson || '', quickChatIdentityAliases(), 5)
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
      refreshMessagePreviews().catch(capture('消息'))
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
