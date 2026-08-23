<template>
  <PageContainer title="我的工作台" subTitle="集中查看今天要完成的任务、风险提醒和常用操作" mode="showcase" kicker="今日工作">
    <template #meta>
      <a-space wrap>
        <StatusTag :text="roleLabel" tone="pending" />
        <StatusTag :text="audienceLabel" tone="normal" />
        <StatusTag :text="`最近刷新 ${refreshedAt}`" tone="offline" />
      </a-space>
    </template>

    <template #extra>
      <a-space wrap>
        <a-button
          v-for="action in attendanceActionButtons"
          :key="action"
          :loading="attendancePending === action"
          :disabled="attendanceSubmitting && attendancePending !== action"
          @click="handleAttendanceAction(action)"
        >{{ attendanceActionLabel(action) }}</a-button>
        <a-button type="primary" :loading="loading" @click="loadWorkbench()">刷新工作台</a-button>
      </a-space>
    </template>

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

    <section class="workbench-hero">
      <div class="workbench-hero-copy">
        <span class="workbench-hero-kicker">Hi, {{ staffDisplayName }}</span>
        <h2>{{ workbenchHeadline }}</h2>
        <p>{{ workbenchSubline }}</p>
      </div>
      <div class="workbench-focus-grid">
        <button
          v-for="item in todayFocusCards"
          :key="item.label"
          type="button"
          class="workbench-focus-card"
          @click="openPath(item.path)"
        >
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
          <small>{{ item.helper }}</small>
        </button>
      </div>
    </section>

    <div v-if="initialLoading" class="workbench-skeleton">
      <a-skeleton active :title="false" :paragraph="{ rows: 3 }" />
      <a-skeleton active :title="false" :paragraph="{ rows: 6 }" />
    </div>

    <section v-if="!initialLoading" class="workbench-summary-grid">
      <OverviewMetricCard
        v-for="item in summaryMetrics"
        :key="item.label"
        clickable
        :helper="item.helper"
        :label="item.label"
        :tone="item.tone"
        :value="item.value"
        @click="openPath(item.path)"
      />
    </section>

    <section v-if="!initialLoading" class="workbench-shell">
      <NursingWorkbenchPanel
        v-if="workbenchProfile.department === 'NURSING' && departmentDataStatus === 'ready' && departmentSnapshot?.nursing"
        :detail="departmentSnapshot.nursing"
        :management-view="workbenchProfile.hasManagementView"
        :can-access="canAccess"
        @open="openPath"
      />

      <MedicalWorkbenchPanel
        v-else-if="workbenchProfile.department === 'MEDICAL' && departmentDataStatus === 'ready' && departmentSnapshot?.medical"
        :detail="departmentSnapshot.medical"
        :management-view="workbenchProfile.hasManagementView"
        :can-access="canAccess"
        @open="openPath"
      />

      <FinanceWorkbenchPanel
        v-else-if="workbenchProfile.department === 'FINANCE' && departmentDataStatus === 'ready' && departmentSnapshot?.finance"
        :detail="departmentSnapshot.finance"
        :management-view="workbenchProfile.hasManagementView"
        :can-access="canAccess"
        @open="openPath"
      />

      <WorkbenchModuleCard
        v-else-if="workbenchProfile.department"
        class="department-summary-card"
        :title="`${workbenchProfile.departmentLabel}今日工作`"
        eyebrow="岗位数据"
      >
        <template #extra>
          <a-space size="small">
            <StatusTag :text="departmentSourceStatusText" :tone="departmentSourceStatusTone" />
            <a-button type="link" @click="openPath(workbenchProfile.primaryEntry)">进入部门业务</a-button>
          </a-space>
        </template>

        <div v-if="departmentDataStatus === 'ready'" class="department-metric-grid">
          <OverviewMetricCard
            v-for="item in departmentSnapshot?.metrics"
            :key="item.key"
            clickable
            :helper="item.helper"
            :label="item.label"
            :tone="item.risk ? 'warning' : 'brand'"
            :value="departmentMetricValue(item)"
            @click="openPath(item.path)"
          />
        </div>
        <a-skeleton v-else-if="departmentDataStatus === 'loading'" active :title="false" :paragraph="{ rows: 2 }" />
        <a-empty v-else :description="departmentDataMessage || '当前暂无需要处理的部门事项'">
          <a-button v-if="departmentDataStatus === 'error'" @click="loadWorkbench()">重新加载</a-button>
        </a-empty>
      </WorkbenchModuleCard>

      <WorkbenchModuleCard title="我的日程" eyebrow="今日">
        <div class="schedule-head">
          <div>
            <strong>{{ selectedDateLabel }}</strong>
            <p>{{ attendanceWindowLabel }}</p>
          </div>
          <a-space size="small">
            <StatusTag :text="attendanceOverview.todayStatusLabel || '待打卡'" :tone="attendanceTone" />
            <a-button type="link" @click="openPath('/workbench/schedule')">查看完整日历</a-button>
          </a-space>
        </div>

        <div class="schedule-calendar-shell">
          <div class="schedule-calendar-panel">
            <div class="schedule-calendar-toolbar">
              <a-button size="small" @click="moveCalendar(-1)">上月</a-button>
              <strong>{{ calendarMonthLabel }}</strong>
              <a-space size="small">
                <a-button size="small" @click="jumpToToday">今天</a-button>
                <a-button size="small" @click="moveCalendar(1)">下月</a-button>
              </a-space>
            </div>
            <FullCalendar ref="calendarRef" :options="calendarOptions" />
          </div>

          <div class="schedule-side-panel">
            <div class="schedule-status-card">
              <div class="schedule-status-copy">
                <strong>{{ attendanceOverview.todayStatusLabel || '未打卡' }}</strong>
                <span>{{ selectedDateHint }}</span>
              </div>
              <StatusTag :text="todayScheduleTagText" :tone="todayScheduleTagTone" />
            </div>

            <div class="workbench-chip-grid">
              <button
                v-for="item in scheduleItems"
                :key="item.label"
                type="button"
                class="workbench-chip"
                @click="openPath(item.path)"
              >
                <strong>{{ item.value }}</strong>
                <span>{{ item.label }}</span>
              </button>
            </div>

            <div class="schedule-agenda">
              <div class="schedule-agenda-head">
                <strong>当日安排</strong>
                <span>{{ selectedDateAgenda.length }} 项</span>
              </div>
              <div v-if="selectedDateAgenda.length" class="schedule-agenda-list">
                <button
                  v-for="item in selectedDateAgenda.slice(0, 4)"
                  :key="item.id"
                  type="button"
                  class="schedule-agenda-item"
                  @click="openPath('/workbench/schedule')"
                >
                  <div>
                    <strong>{{ item.title }}</strong>
                    <span>{{ agendaTimeText(item.startTime, item.endTime) }}</span>
                  </div>
                  <StatusTag :text="agendaTypeText(item.calendarType)" :tone="agendaTone(item)" />
                </button>
              </div>
              <a-empty v-else description="这一天暂无安排" />
            </div>
          </div>
        </div>
      </WorkbenchModuleCard>

      <WorkbenchModuleCard
        v-for="module in orderedPrimaryModules"
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

      <WorkbenchModuleCard v-if="!orderedPrimaryModules.length" title="业务模块" eyebrow="提示">
        <a-empty description="当前角色暂无可展示的业务模块，可前往「全部功能」查看有权限的入口">
          <a-button type="primary" @click="openPath('/function-map')">前往全部功能</a-button>
        </a-empty>
      </WorkbenchModuleCard>

      <WorkbenchModuleCard title="最近访问" eyebrow="最近">
        <div class="recent-visit-list">
          <button
            v-for="item in recentVisits"
            :key="item.key"
            type="button"
            class="recent-visit-item"
            @click="openPath(item.path)"
          >
            <div>
              <strong>{{ item.title }}</strong>
              <span>{{ item.path }}</span>
            </div>
            <small>{{ formatVisitTime(item.visitedAt) }}</small>
          </button>
          <a-empty v-if="!recentVisits.length" description="最近访问会从导航、标签页和搜索中自动沉淀" />
        </div>
      </WorkbenchModuleCard>

      <WorkbenchModuleCard title="常用功能" eyebrow="常用">
        <div class="common-actions-grid">
          <QuickActionTile
            v-for="item in commonActions"
            :key="item.title"
            :description="item.description"
            :icon="item.icon"
            :title="item.title"
            @click="openPath(item.path)"
          />
        </div>
      </WorkbenchModuleCard>
    </section>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import dayjs, { type Dayjs } from 'dayjs'
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import interactionPlugin from '@fullcalendar/interaction'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import OverviewMetricCard from '../../components/smartcare/OverviewMetricCard.vue'
import QuickActionTile from '../../components/smartcare/QuickActionTile.vue'
import StatusTag from '../../components/smartcare/StatusTag.vue'
import WorkbenchModuleCard from '../../components/smartcare/WorkbenchModuleCard.vue'
import NursingWorkbenchPanel from '../../components/workbench/NursingWorkbenchPanel.vue'
import MedicalWorkbenchPanel from '../../components/workbench/MedicalWorkbenchPanel.vue'
import FinanceWorkbenchPanel from '../../components/workbench/FinanceWorkbenchPanel.vue'
import { getOaTaskCalendar, getPortalSummary } from '../../api/oa'
import { getAttendanceOverview, punchAttendance } from '../../api/schedule'
import type { AttendanceDashboardOverview, OaPortalSummary, OaTask } from '../../types'
import { useUserStore } from '../../stores/user'
import { useRouter } from 'vue-router'
import { resolveRouteAccess } from '../../utils/routeAccess'
import { loadRecentVisits, type RecentVisitItem } from '../../utils/recentVisits'
import { resolveWorkbenchProfile, workbenchAudienceLabel } from '../../workbench/model'
import {
  loadDepartmentWorkbenchSnapshot,
  type DepartmentWorkbenchSnapshot,
  type WorkbenchDataStatus,
  type WorkbenchMetricSnapshot
} from '../../workbench/dataSources'

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
  tone: 'brand' | 'warning' | 'success'
  path: string
}

type QuickActionItem = {
  title: string
  description: string
  icon: string
  path: string
}

type WorkbenchPersona = {
  key: 'nursing' | 'medical' | 'logistics' | 'finance' | 'hr' | 'marketing' | 'manager'
  roleLabel: string
  subline: string
  modulePriority: string[]
  primaryEntry: {
    label: string
    value: string
    helper: string
    path: string
  }
  summaryMetrics: SummaryMetricItem[]
  preferredActions: string[]
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
let refreshInFlight = false
const calendarRef = ref<InstanceType<typeof FullCalendar> | null>(null)
const selectedDate = ref(dayjs())
const calendarCursor = ref(dayjs().startOf('month'))
const calendarRows = ref<OaTask[]>([])
const portalSummary = ref<OaPortalSummary | null>(null)
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

function departmentMetricValue(item: WorkbenchMetricSnapshot) {
  const value = displayNumber(item.value)
  return item.suffix && value !== '--' ? `${value} ${item.suffix}` : value
}

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
const audienceLabel = computed(() => workbenchAudienceLabel(workbenchProfile.value))

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
      primaryEntry: {
        label: '护理执行',
        value: displayNumber(departmentMetricNumber('pending')),
        helper: `超时 ${displayNumber(departmentMetricNumber('overdue'))}，先清护理任务与交接风险`,
        path: '/care/today'
      },
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
      ],
      preferredActions: ['/care/today', '/care/service/nursing-records', '/workbench/attendance']
    }
  }
  if (workbenchProfile.value.department === 'MEDICAL') {
    const managementView = workbenchProfile.value.hasManagementView
    return {
      key: 'medical',
      roleLabel: '医务处置角色',
      subline: '优先复核健康异常、医嘱执行和巡诊安排，避免问题跨班次积压。',
      modulePriority: ['todo', 'clinical', 'approval', 'customer', 'workorder', 'finance', 'hr'],
      primaryEntry: {
        label: managementView ? '未闭环异常' : '医务处置',
        value: managementView ? displayNumber(departmentMetricNumber('abnormal')) : '--',
        helper: managementView
          ? `待执行医嘱 ${displayNumber(departmentMetricNumber('orders'))}，先看异常与医嘱闭环`
          : '个人待办口径待后端接入，请进入巡诊或医嘱列表处理',
        path: managementView ? '/medical-care/unified-task-center' : '/medical-care/rounds'
      },
      summaryMetrics: [
        {
          label: managementView ? '未闭环异常' : '我的巡诊待办',
          value: managementView ? displayNumber(departmentMetricNumber('abnormal')) : '--',
          helper: managementView ? '异常复核与交班跟进' : '个人统计接口尚未接入',
          tone: 'warning',
          path: managementView ? '/medical-care/unified-task-center' : '/medical-care/rounds'
        },
        {
          label: managementView ? '待执行医嘱' : '我的今日用药登记',
          value: displayNumber(departmentMetricNumber(managementView ? 'orders' : 'medicationDone')),
          helper: managementView ? '巡诊、医嘱与医护任务' : '按当前登录账号登记人统计',
          tone: 'success',
          path: managementView ? '/medical-care/orders' : '/medical-care/medication-registration'
        }
      ],
      preferredActions: ['/medical-care/rounds', '/medical-care/orders', '/medical-care/medication-registration', '/workbench/attendance']
    }
  }
  if (workbenchProfile.value.department === 'LOGISTICS' || hasRoleFragment('GUARD')) {
    return {
      key: 'logistics',
      roleLabel: '后勤保障角色',
      subline: '先处理逾期维修、巡检和保障工单，再补充现场回执。',
      modulePriority: ['todo', 'workorder', 'approval', 'hr', 'customer', 'clinical', 'finance'],
      primaryEntry: {
        label: '保障工单',
        value: displayNumber(departmentMetricNumber('maintenance')),
        helper: `逾期 ${displayNumber(departmentMetricNumber('overdue'))}，优先清设备与保障异常`,
        path: '/logistics/task-center'
      },
      summaryMetrics: [
        {
          label: '待处理工单',
          value: displayNumber(departmentMetricNumber('maintenance')),
          helper: '维修、巡检与保障任务',
          tone: 'warning',
          path: '/logistics/task-center'
        },
        {
          label: '逾期工单',
          value: displayNumber(departmentMetricNumber('overdue')),
          helper: '超时项优先催办',
          tone: 'warning',
          path: '/logistics/task-center'
        }
      ],
      preferredActions: ['/logistics/task-center', '/workbench/attendance', '/workbench/todo']
    }
  }
  if (workbenchProfile.value.department === 'FINANCE') {
    const managementView = workbenchProfile.value.hasManagementView
    return {
      key: 'finance',
      roleLabel: '财务运营角色',
      subline: '优先核对收费、账单和对账例外，避免回款与审批卡住。',
      modulePriority: ['todo', 'finance', 'approval', 'hr', 'customer', 'clinical', 'workorder'],
      primaryEntry: {
        label: managementView ? '待审核费用' : '机构今日实收',
        value: managementView ? displayNumber(departmentMetricNumber('pending')) : displayAmount(departmentMetricNumber('collected')),
        helper: managementView ? '先处理折扣、退款和退住结算审批' : '机构当日口径，完成收款后及时核对票据',
        path: managementView ? '/workbench/approvals' : '/finance/payments/cashier-desk'
      },
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
      ],
      preferredActions: managementView
        ? ['/workbench/approvals', '/finance/bills/follow-up', '/finance/reconcile/issue-center']
        : ['/finance/payments/cashier-desk', '/finance/payments/records', '/finance/payments/shift-close']
    }
  }
  if (workbenchProfile.value.department === 'HR') {
    return {
      key: 'hr',
      roleLabel: '人事行政角色',
      subline: '先看考勤异常、班组排班和档案动作，再推进招聘与入转调离。',
      modulePriority: ['todo', 'hr', 'approval', 'finance', 'customer', 'clinical', 'workorder'],
      primaryEntry: {
        label: '人资中心',
        value: displayNumber(attendanceOverview.abnormalCount),
        helper: `今日日程 ${displayNumber(portalSummary.value?.todayScheduleCount)}，优先处理考勤和档案动作`,
        path: '/hr/overview'
      },
      summaryMetrics: [
        {
          label: '考勤异常',
          value: displayNumber(attendanceOverview.abnormalCount),
          helper: '打卡、请假与班组排班',
          tone: 'warning',
          path: '/workbench/attendance'
        },
        {
          label: '今日日程',
          value: displayNumber(portalSummary.value?.todayScheduleCount),
          helper: '招聘、面试与行政协同',
          tone: 'brand',
          path: '/hr/overview'
        }
      ],
      preferredActions: ['/hr/overview', '/workbench/attendance', '/workbench/approvals']
    }
  }
  if (workbenchProfile.value.department === 'MARKETING') {
    return {
      key: 'marketing',
      roleLabel: '营销转化角色',
      subline: '先推进线索回访、参观和试住转化，再补营销任务和计划。',
      modulePriority: ['todo', 'customer', 'approval', 'finance', 'clinical', 'workorder', 'hr'],
      primaryEntry: {
        label: '客户跟进',
        value: displayNumber(portalSummary.value?.suggestionCount),
        helper: '优先跟进咨询、预约参观和试住转化',
        path: '/marketing/workbench'
      },
      summaryMetrics: [
        {
          label: '待回访客户',
          value: displayNumber(portalSummary.value?.suggestionCount),
          helper: '咨询、参观与试住推进',
          tone: 'success',
          path: '/marketing/workbench'
        },
        {
          label: '协同待办',
          value: displayNumber(portalSummary.value?.openTodoCount),
          helper: '合同、床位与跨部门衔接',
          tone: 'brand',
          path: '/workbench/todo'
        }
      ],
      preferredActions: ['/marketing/workbench', '/workbench/todo', '/workbench/approvals']
    }
  }
  return {
    key: 'manager',
    roleLabel: '综合管理角色',
    subline: '先处理审批、协同和关键待办，再下钻到具体业务模块。',
    modulePriority: ['todo', 'approval', 'clinical', 'customer', 'workorder', 'finance', 'hr'],
    primaryEntry: {
      label: '协同事项',
      value: displayNumber(portalSummary.value?.pendingApprovalCount),
      helper: '先处理审批、对账、排班和跨部门协同',
      path: '/workbench/approvals'
    },
    summaryMetrics: [
      {
        label: '我的护理任务',
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
    ],
    preferredActions: ['/workbench/approvals', '/workbench/todo', '/finance/workbench']
  }
})

const roleLabel = computed(() => workbenchProfile.value.roleLabel)

const departmentSourceStatusText = computed(() => {
  if (departmentDataStatus.value === 'loading') return '正在加载'
  if (departmentDataStatus.value === 'ready') return '真实业务数据'
  if (departmentDataStatus.value === 'error') return '加载失败'
  if (departmentDataStatus.value === 'forbidden') return '无数据权限'
  return '暂无数据'
})

const departmentSourceStatusTone = computed(() => {
  if (departmentDataStatus.value === 'ready') return 'normal'
  if (departmentDataStatus.value === 'error') return 'danger'
  if (departmentDataStatus.value === 'loading') return 'pending'
  return 'offline'
})

const staffDisplayName = computed(() =>
  String(userStore.staffInfo?.realName || userStore.staffInfo?.username || '当前员工')
)

const attendanceTone = computed(() => {
  const status = String(attendanceOverview.todayStatus || '').toUpperCase()
  if (status === 'ON_DUTY' || status === 'OUTING' || status === 'LUNCH_BREAK') return 'normal'
  if (status === 'OFF_DUTY') return 'offline'
  if (status === 'ON_LEAVE') return 'warning'
  return 'pending'
})

const attendanceWindowLabel = computed(() => {
  const start = attendanceOverview.expectedWorkStart || '--:--'
  const end = attendanceOverview.expectedWorkEnd || '--:--'
  return `今日班次 ${start} - ${end}`
})

const calendarMonthLabel = computed(() => calendarCursor.value.format('YYYY 年 M 月'))

const selectedDateLabel = computed(() => selectedDate.value.format('M 月 D 日 dddd'))

const selectedDateHint = computed(() => {
  if (selectedDate.value.isSame(dayjs(), 'day')) return '今天的日程和打卡状态会优先显示在这里'
  return `${selectedDate.value.format('M 月 D 日')} 的安排摘要`
})

const rolePrimaryEntry = computed(() => workbenchPersona.value.primaryEntry)

const workbenchHeadline = computed(() => `${staffDisplayName.value}，先把今天要闭环的事项处理掉。`)

const workbenchSubline = computed(() =>
  `${roleLabel.value} · ${attendanceWindowLabel.value} · ${workbenchPersona.value.subline}`
)

const todayFocusCards = computed(() => [
  {
    label: '班次状态',
    value: attendanceOverview.todayStatusLabel || '待打卡',
    helper: attendanceWindowLabel.value,
    path: '/workbench/attendance'
  },
  {
    label: '今日待办',
    value: displayNumber(portalSummary.value?.openTodoCount),
    helper: `逾期 ${displayNumber(portalSummary.value?.overdueTodoCount)}，建议先清高优先事项`,
    path: '/workbench/todo'
  },
  rolePrimaryEntry.value
])

const attendanceActionButtons = computed<AttendanceAction[]>(() => {
  const status = String(attendanceOverview.todayStatus || '').toUpperCase()
  if (!status || status === 'NOT_CHECKED_IN') return ['IN']
  if (status === 'ON_DUTY') return ['START_LUNCH', 'START_OUTING', 'OUT']
  if (status === 'LUNCH_BREAK') return ['END_LUNCH']
  if (status === 'OUTING') return ['END_OUTING']
  return []
})

// 通用的“我的待办/我的审批”已在 hero 焦点卡与下方 module 卡呈现，这里只保留角色专属指标，避免同一数字重复计数
const summaryMetrics = computed(() => workbenchPersona.value.summaryMetrics)

const scheduleItems = computed(() => ([
  { label: '当月在岗', value: `${displayNumber(attendanceOverview.onDutyDays)} 天`, path: '/workbench/attendance' },
  { label: '请假天数', value: `${displayNumber(attendanceOverview.leaveDays)} 天`, path: '/workbench/attendance' },
  { label: '考勤异常', value: `${displayNumber(attendanceOverview.abnormalCount)} 次`, path: '/workbench/attendance' },
  { label: '今日日程', value: `${displayNumber(portalSummary.value?.todayScheduleCount)} 项`, path: '/workbench/schedule' }
]))

const selectedDateAgenda = computed(() => {
  const target = selectedDate.value.format('YYYY-MM-DD')
  return calendarRows.value.filter((item) => {
    const start = item.startTime || item.endTime
    return start ? dayjs(start).format('YYYY-MM-DD') === target : false
  })
})

const todayScheduleTagText = computed(() => {
  const count = selectedDateAgenda.value.length
  return count ? `${count} 项待查看` : '暂无安排'
})

const todayScheduleTagTone = computed(() => (selectedDateAgenda.value.length ? 'pending' : 'offline'))

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

function agendaTimeText(startTime?: string, endTime?: string) {
  if (!startTime && !endTime) return '全天安排'
  const start = startTime ? dayjs(startTime).format('HH:mm') : '--:--'
  const end = endTime ? dayjs(endTime).format('HH:mm') : '--:--'
  return `${start} - ${end}`
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

function recentVisitScope() {
  if (userStore.staffInfo?.id) return `id_${String(userStore.staffInfo.id)}`
  if (userStore.staffInfo?.username) return `u_${String(userStore.staffInfo.username)}`
  return 'default'
}

const recentVisits = computed<RecentVisitItem[]>(() => loadRecentVisits(recentVisitScope()).slice(0, 6))

const commonActions = computed(() => {
  const medicalEmployee = workbenchPersona.value.key === 'medical' && !hasManagement.value
  const financeEmployee = workbenchPersona.value.key === 'finance' && !hasManagement.value
  const actions: QuickActionItem[] = [
    { title: '我的待办', description: '查看所有待办、提醒和逾期项。', icon: '办', path: '/workbench/todo' },
    { title: '我的审批', description: '处理审批流程和待确认事项。', icon: '审', path: '/workbench/approvals' },
    { title: '我的考勤', description: '进入考勤、请假和打卡视图。', icon: '勤', path: '/workbench/attendance' },
    { title: '客户跟进', description: '查看待跟进客户和预约参观。', icon: '客', path: '/marketing/workbench' },
    { title: '护理任务', description: '查看今日护理执行、超时和异常事项。', icon: '护', path: '/care/today' },
    { title: '护理记录', description: '补充服务执行记录与结果留痕。', icon: '录', path: '/care/service/nursing-records' },
    {
      title: medicalEmployee ? '今日巡诊' : '医护处置',
      description: medicalEmployee ? '进入巡诊与巡检列表处理本人工作。' : '查看健康异常、医嘱和巡诊待办。',
      icon: '医',
      path: medicalEmployee ? '/medical-care/rounds' : '/medical-care/unified-task-center'
    },
    { title: '后勤工单', description: '查看维修、巡检与保障任务。', icon: '工', path: '/logistics/task-center' },
    { title: financeEmployee ? '进入收银台' : '财务工作台', description: financeEmployee ? '登记收费并核对当天收款。' : '进入收费、账单和对账总览。', icon: '费', path: financeEmployee ? '/finance/payments/cashier-desk' : '/finance/workbench' },
    { title: '人资中心', description: '进入档案、招聘、考勤与班组。', icon: '人', path: '/hr/overview' }
  ]
  const preferredOrder = new Map(workbenchPersona.value.preferredActions.map((path, index) => [path, index]))
  return actions
    .filter((item) => canAccess(item.path))
    .sort((left, right) => (preferredOrder.get(left.path) ?? 99) - (preferredOrder.get(right.path) ?? 99))
})

const calendarOptions = computed(() => ({
  plugins: [dayGridPlugin, interactionPlugin],
  initialView: 'dayGridMonth',
  locale: 'zh-cn',
  height: 'auto',
  fixedWeekCount: false,
  headerToolbar: false,
  dayMaxEventRows: 2,
  events: calendarRows.value.map((task) => ({
    id: String(task.id),
    title: task.title,
    start: task.startTime || task.endTime,
    end: task.endTime || task.startTime,
    color: task.urgency === 'EMERGENCY' ? 'var(--danger)' : task.calendarType === 'COLLAB' ? 'var(--warning)' : 'var(--primary)',
    classNames: [
      selectedDate.value.isSame(task.startTime || task.endTime || '', 'day') ? 'workbench-calendar-event--focus' : '',
      task.status === 'DONE' ? 'workbench-calendar-event--done' : ''
    ].filter(Boolean)
  })),
  datesSet: (arg: { start: Date; end: Date; view: { currentStart?: Date } }) => {
    const currentStart = dayjs(arg.view?.currentStart || arg.start)
    if (!currentStart.isSame(calendarCursor.value, 'month')) {
      calendarCursor.value = currentStart.startOf('month')
      loadCalendar(currentStart)
    }
  },
  dateClick: (arg: { dateStr: string }) => {
    selectedDate.value = dayjs(arg.dateStr)
  },
  dayCellClassNames: (arg: { date: Date }) => {
    const cellDate = dayjs(arg.date)
    return [
      cellDate.isSame(dayjs(), 'day') ? 'workbench-calendar-day--today' : '',
      cellDate.isSame(selectedDate.value, 'day') ? 'workbench-calendar-day--selected' : ''
    ].filter(Boolean)
  }
}))

async function loadCalendar(base = calendarCursor.value): Promise<boolean> {
  if (!canAccess('/workbench/schedule')) {
    calendarRows.value = []
    return true
  }
  const monthStart = dayjs(base).startOf('month')
  const monthEnd = monthStart.endOf('month')
  try {
    const rows = await getOaTaskCalendar({
      startDate: monthStart.format('YYYY-MM-DD'),
      endDate: monthEnd.format('YYYY-MM-DD')
    })
    calendarRows.value = Array.isArray(rows) ? rows : []
    return true
  } catch {
    calendarRows.value = []
    return false
  }
}

function moveCalendar(offset: number) {
  const api = calendarRef.value?.getApi?.()
  if (!api) {
    const next = calendarCursor.value.add(offset, 'month')
    calendarCursor.value = next.startOf('month')
    loadCalendar(next)
    return
  }
  if (offset > 0) api.next()
  else api.prev()
}

function jumpToToday() {
  selectedDate.value = dayjs()
  const api = calendarRef.value?.getApi?.()
  if (api) {
    api.today()
    return
  }
  calendarCursor.value = dayjs().startOf('month')
  loadCalendar(dayjs())
}

async function refreshAttendance() {
  const data = await getAttendanceOverview({
    staffId: (userStore.staffInfo?.id as string | number | undefined) || undefined
  })
  Object.assign(attendanceOverview, data || {})
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
      getPortalSummary({ silent403: true, silentError: true }).catch(capture('工作待办')),
      loadDepartmentWorkbenchSnapshot(workbenchProfile.value, canAccess, {
        staffId: userStore.staffInfo?.id,
        username: userStore.staffInfo?.username
      }),
      refreshAttendance().catch(capture('考勤')),
      loadCalendar(calendarCursor.value)
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

function formatVisitTime(timestamp: number) {
  if (!timestamp) return '--'
  return new Date(timestamp).toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function handleVisibilityChange() {
  // 标签页重新可见时立即补一次刷新，隐藏时不做无谓请求
  if (document.visibilityState === 'visible') loadWorkbench({ silent: true })
}

onMounted(() => {
  loadWorkbench()
  refreshTimer = setInterval(() => {
    if (document.visibilityState === 'visible') loadWorkbench({ silent: true })
  }, REFRESH_INTERVAL_MS)
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})
</script>

<style scoped>
.workbench-load-alert {
  margin-bottom: 18px;
  border-radius: 14px;
}

.workbench-skeleton {
  display: grid;
  gap: 18px;
}

.workbench-hero,
.workbench-summary-grid {
  display: grid;
  gap: 18px;
}

.workbench-hero {
  grid-template-columns: minmax(0, 1.1fr) minmax(420px, 0.9fr);
  margin-bottom: 18px;
  padding: 24px;
  border-radius: 24px;
  background:
    radial-gradient(circle at 100% 0%, rgba(229, 138, 58, 0.16), transparent 42%),
    linear-gradient(135deg, #14483d 0%, #185c4e 45%, #21705f 100%);
  color: #ecf5f1;
}

.workbench-summary-grid {
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
}

.workbench-hero-copy {
  display: grid;
  gap: 10px;
  align-content: start;
}

.workbench-hero-kicker {
  color: rgba(220, 237, 230, 0.85);
  font-size: 13px;
  letter-spacing: 0.06em;
}

.workbench-hero-copy h2 {
  margin: 0;
  color: #ffffff;
  font-size: clamp(26px, 3vw, 34px);
  line-height: 1.2;
}

.workbench-hero-copy p {
  margin: 0;
  color: rgba(236, 245, 241, 0.82);
  font-size: 14px;
  line-height: 1.8;
}

.workbench-focus-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.workbench-focus-card {
  width: 100%;
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.08);
  padding: 16px;
  text-align: left;
  cursor: pointer;
  transition: background 0.2s ease, border-color 0.2s ease;
}

.workbench-focus-card:hover {
  border-color: rgba(255, 255, 255, 0.34);
  background: rgba(255, 255, 255, 0.14);
}

.workbench-focus-card span,
.workbench-focus-card small {
  display: block;
  color: rgba(236, 245, 241, 0.78);
}

.workbench-focus-card strong {
  display: block;
  margin: 10px 0 8px;
  color: #ffffff;
  font-size: 26px;
  line-height: 1.05;
}

.workbench-focus-card small {
  font-size: 12px;
  line-height: 1.7;
}

.workbench-shell {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.department-summary-card {
  grid-column: 1 / -1;
}

.department-metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.schedule-head,
.module-summary-row,
.workbench-chip-grid,
.workbench-list,
.recent-visit-list,
.common-actions-grid {
  display: grid;
  gap: 12px;
}

.schedule-head {
  grid-template-columns: 1fr auto;
  align-items: center;
}

.schedule-head strong {
  display: block;
  color: var(--ink);
  font-size: 22px;
}

.schedule-head p {
  margin: 4px 0 0;
  color: var(--muted);
  font-size: 13px;
}

.schedule-calendar-shell {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(280px, 0.95fr);
  gap: 16px;
}

.schedule-calendar-panel,
.schedule-side-panel {
  display: grid;
  gap: 12px;
}

.schedule-calendar-toolbar,
.schedule-status-card,
.schedule-agenda-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.schedule-calendar-toolbar strong,
.schedule-status-copy strong,
.schedule-agenda-head strong {
  color: var(--ink);
}

.schedule-status-card,
.schedule-agenda,
.workbench-chip,
.workbench-list-item,
.recent-visit-item {
  border: 1px solid var(--border);
  border-radius: 18px;
  background: #ffffff;
}

.workbench-chip:hover,
.workbench-list-item:hover,
.recent-visit-item:hover,
.schedule-agenda-item:hover {
  border-color: rgba(var(--primary-rgb), 0.36);
  box-shadow: var(--shadow-sm);
}

.schedule-status-card {
  padding: 14px 16px;
}

.schedule-status-copy {
  display: grid;
  gap: 4px;
}

.schedule-status-copy span,
.schedule-agenda-head span {
  color: var(--muted);
  font-size: 12px;
}

.schedule-agenda {
  padding: 14px 16px;
  display: grid;
  gap: 12px;
}

.schedule-agenda-list {
  display: grid;
  gap: 10px;
}

.schedule-agenda-item {
  width: 100%;
  border: 1px solid var(--border-soft);
  border-radius: 16px;
  background: var(--surface-3);
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
  padding: 12px 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  text-align: left;
  cursor: pointer;
}

.schedule-agenda-item strong {
  display: block;
  color: var(--ink);
}

.schedule-agenda-item span {
  color: var(--muted);
  font-size: 12px;
}

.workbench-chip-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.workbench-chip,
.workbench-list-item,
.recent-visit-item {
  width: 100%;
  text-align: left;
  cursor: pointer;
}

.workbench-chip {
  padding: 14px 16px;
}

.workbench-chip strong {
  display: block;
  color: var(--ink);
  font-size: 18px;
}

.workbench-chip span {
  color: var(--muted);
  font-size: 12px;
}

.workbench-list-item,
.recent-visit-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 15px 16px;
}

.workbench-list-item strong,
.recent-visit-item strong {
  color: var(--ink);
}

.workbench-list-item span,
.recent-visit-item span,
.recent-visit-item small {
  color: var(--muted);
  font-size: 12px;
}

.common-actions-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

:deep(.fc) {
  --fc-border-color: rgba(222, 232, 236, 0.9);
  --fc-event-border-color: transparent;
  --fc-page-bg-color: transparent;
  --fc-neutral-bg-color: rgba(243, 248, 249, 0.9);
  --fc-today-bg-color: rgba(var(--primary-rgb), 0.08);
  font-size: 13px;
}

:deep(.fc-theme-standard td),
:deep(.fc-theme-standard th),
:deep(.fc-theme-standard .fc-scrollgrid) {
  border-color: rgba(222, 232, 236, 0.9);
}

:deep(.fc .fc-col-header-cell-cushion) {
  padding: 10px 0;
  color: var(--muted);
  font-weight: 600;
}

:deep(.fc .fc-daygrid-day-number) {
  color: var(--ink);
  padding: 8px 10px 0;
}

:deep(.fc .workbench-calendar-day--selected) {
  background: rgba(var(--primary-rgb), 0.12);
}

:deep(.fc .workbench-calendar-day--today) .fc-daygrid-day-number {
  color: var(--primary);
  font-weight: 700;
}

:deep(.fc .fc-daygrid-event) {
  border-radius: 999px;
  padding: 1px 6px;
  font-size: 11px;
}

:deep(.fc .workbench-calendar-event--done) {
  opacity: 0.55;
}

:deep(.fc .workbench-calendar-event--focus) {
  box-shadow: 0 0 0 1px rgba(var(--primary-rgb), 0.18);
}

@media (max-width: 1280px) {
  .workbench-hero,
  .workbench-summary-grid,
  .workbench-shell,
  .department-metric-grid,
  .common-actions-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .schedule-calendar-shell {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .workbench-hero,
  .workbench-summary-grid,
  .workbench-shell,
  .department-metric-grid,
  .workbench-chip-grid,
  .workbench-focus-grid,
  .common-actions-grid {
    grid-template-columns: 1fr;
  }

  .schedule-head {
    grid-template-columns: 1fr;
  }
}
</style>
