<template>
  <PageContainer title="工作台" subTitle="个人待办中心" mode="showcase" kicker="个人工作区">
    <template #meta>
      <a-space wrap>
        <StatusTag :text="roleLabel" tone="pending" />
        <StatusTag text="个人优先视角" tone="normal" />
        <StatusTag :text="`最近刷新 ${refreshedAt}`" tone="offline" />
      </a-space>
    </template>

    <template #extra>
      <a-space wrap>
        <a-button v-if="attendanceActionButtons.includes('IN')" @click="handleAttendanceAction('IN')">上班打卡</a-button>
        <a-button v-if="attendanceActionButtons.includes('OUT')" @click="handleAttendanceAction('OUT')">下班打卡</a-button>
        <a-button type="primary" :loading="loading" @click="loadWorkbench">刷新工作台</a-button>
      </a-space>
    </template>

    <section class="workbench-summary-grid">
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

    <section class="workbench-shell">
      <WorkbenchModuleCard title="我的日程" eyebrow="Today">
        <div class="schedule-head">
          <div>
            <strong>{{ attendanceOverview.todayStatusLabel || '未打卡' }}</strong>
            <p>{{ attendanceWindowLabel }}</p>
          </div>
          <StatusTag :text="attendanceOverview.todayStatusLabel || '待打卡'" :tone="attendanceTone" />
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

      <WorkbenchModuleCard title="最近访问" eyebrow="History">
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

      <WorkbenchModuleCard title="常用功能" eyebrow="Common">
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
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import OverviewMetricCard from '../../components/smartcare/OverviewMetricCard.vue'
import QuickActionTile from '../../components/smartcare/QuickActionTile.vue'
import StatusTag from '../../components/smartcare/StatusTag.vue'
import WorkbenchModuleCard from '../../components/smartcare/WorkbenchModuleCard.vue'
import { getLogisticsWorkbenchSummary } from '../../api/logistics'
import { getPortalSummary } from '../../api/oa'
import { getAttendanceOverview, punchAttendance } from '../../api/schedule'
import type { AttendanceDashboardOverview, OaPortalSummary, LogisticsWorkbenchSummary } from '../../types'
import { useUserStore } from '../../stores/user'
import { useRouter } from 'vue-router'
import { resolveRouteAccess } from '../../utils/routeAccess'
import { loadRecentVisits, type RecentVisitItem } from '../../utils/recentVisits'

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

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const attendanceSubmitting = ref(false)
const refreshedAt = ref('--')
const portalSummary = ref<OaPortalSummary | null>(null)
const logisticsSummary = ref<LogisticsWorkbenchSummary | null>(null)
const attendanceOverview = reactive<AttendanceDashboardOverview>({
  todayStatusLabel: '',
  expectedWorkStart: '',
  expectedWorkEnd: '',
  onDutyDays: 0,
  leaveDays: 0,
  abnormalCount: 0
})

function numberValue(value?: number | null, fallback = 0) {
  return Number.isFinite(Number(value)) ? Number(value) : fallback
}

function displayNumber(value?: number | null) {
  if (value === null || value === undefined || Number.isNaN(Number(value))) return '--'
  return Number(value).toLocaleString('zh-CN')
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

const roleLabel = computed(() => {
  const roles = userStore.roles || []
  if (roles.some((role) => role.includes('NURSING') || role.includes('MEDICAL'))) return '医护 / 护理角色'
  if (roles.some((role) => role.includes('MARKETING'))) return '营销 / 客服角色'
  if (roles.some((role) => role.includes('LOGISTICS'))) return '后勤 / 保障角色'
  if (roles.some((role) => role.includes('FINANCE') || role.includes('HR') || role.includes('OA'))) return '经营支持角色'
  return '综合管理角色'
})

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

const attendanceActionButtons = computed<AttendanceAction[]>(() => {
  const status = String(attendanceOverview.todayStatus || '').toUpperCase()
  if (!status || status === 'NOT_CHECKED_IN') return ['IN']
  if (status === 'ON_DUTY') return ['START_LUNCH', 'START_OUTING', 'OUT']
  if (status === 'LUNCH_BREAK') return ['END_LUNCH']
  if (status === 'OUTING') return ['END_OUTING']
  return []
})

const summaryMetrics = computed(() => ([
  {
    label: '我的待办',
    value: displayNumber(portalSummary.value?.openTodoCount),
    helper: `逾期 ${displayNumber(portalSummary.value?.overdueTodoCount)}`,
    tone: 'brand' as const,
    path: '/workbench/todo'
  },
  {
    label: '我的审批',
    value: displayNumber(portalSummary.value?.pendingApprovalCount),
    helper: '待审批与待确认',
    tone: 'warning' as const,
    path: '/workbench/approvals'
  },
  {
    label: '我的护理任务',
    value: displayNumber(portalSummary.value?.pendingMedicalOrderCount),
    helper: '医护任务与用药提醒',
    tone: 'success' as const,
    path: '/medical-care/care-task-board'
  },
  {
    label: '我的工单',
    value: displayNumber(logisticsSummary.value?.maintenancePendingCount),
    helper: '维修、巡检与保障任务',
    tone: 'warning' as const,
    path: '/logistics/task-center'
  }
]))

const scheduleItems = computed(() => ([
  { label: '当月在岗', value: `${displayNumber(attendanceOverview.onDutyDays)} 天`, path: '/workbench/attendance' },
  { label: '请假天数', value: `${displayNumber(attendanceOverview.leaveDays)} 天`, path: '/workbench/attendance' },
  { label: '考勤异常', value: `${displayNumber(attendanceOverview.abnormalCount)} 次`, path: '/workbench/attendance' },
  { label: '今日日程', value: `${displayNumber(portalSummary.value?.todayScheduleCount)} 项`, path: '/workbench/my-info' }
]))

const allModules = computed<PrimaryModule[]>(() => ([
  {
    key: 'todo',
    title: '我的待办',
    eyebrow: 'Priority',
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
    eyebrow: 'Approve',
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
  {
    key: 'nursing',
    title: '我的护理任务',
    eyebrow: 'Clinical',
    path: '/medical-care/care-task-board',
    metricLabel: '护理与医护任务',
    metricHelper: '巡诊、护理计划、医嘱执行',
    metricValue: displayNumber(portalSummary.value?.pendingMedicalOrderCount),
    tone: 'success',
    items: [
      { title: '待执行护理任务', desc: '结合任务板统一处理。', path: '/medical-care/care-task-board', tag: `任务 ${displayNumber(portalSummary.value?.pendingMedicalOrderCount)}`, tone: 'pending' },
      { title: '健康异常提醒', desc: '优先处理生命体征和长者异常。', path: '/medical-care/unified-task-center', tag: `异常 ${displayNumber(portalSummary.value?.healthAbnormalCount)}`, tone: 'danger' }
    ]
  },
  {
    key: 'customer',
    title: '我的客户跟进',
    eyebrow: 'Marketing',
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
    eyebrow: 'Logistics',
    path: '/logistics/task-center',
    metricLabel: '工单与保障',
    metricHelper: '维修、巡检、后勤保障',
    metricValue: displayNumber(logisticsSummary.value?.maintenancePendingCount),
    tone: 'warning',
    items: [
      { title: '待处理工单', desc: '当前需要响应的维修与保障任务。', path: '/logistics/task-center', tag: `待办 ${displayNumber(logisticsSummary.value?.maintenancePendingCount)}`, tone: 'warning' },
      { title: '逾期工单', desc: '逾期项需要优先催办。', path: '/logistics/task-center', tag: `逾期 ${displayNumber(logisticsSummary.value?.maintenanceOverdueCount)}`, tone: 'danger' }
    ]
  }
]))

const orderedPrimaryModules = computed(() => {
  const roles = userStore.roles || []
  const priority: string[] = ['todo', 'approval', 'nursing', 'customer', 'workorder']
  if (roles.some((role) => role.includes('MEDICAL') || role.includes('NURSING'))) {
    priority.splice(priority.indexOf('nursing'), 1)
    priority.splice(1, 0, 'nursing')
  } else if (roles.some((role) => role.includes('MARKETING'))) {
    priority.splice(priority.indexOf('customer'), 1)
    priority.splice(1, 0, 'customer')
  } else if (roles.some((role) => role.includes('LOGISTICS'))) {
    priority.splice(priority.indexOf('workorder'), 1)
    priority.splice(1, 0, 'workorder')
  } else if (roles.some((role) => role.includes('FINANCE') || role.includes('HR') || role.includes('OA'))) {
    priority.splice(priority.indexOf('approval'), 1)
    priority.splice(1, 0, 'approval')
  }

  const rank = new Map(priority.map((key, index) => [key, index]))
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
  const actions = [
    { title: '我的待办', description: '查看所有待办、提醒和逾期项。', icon: '办', path: '/workbench/todo' },
    { title: '我的审批', description: '处理审批流程和待确认事项。', icon: '审', path: '/workbench/approvals' },
    { title: '我的考勤', description: '进入考勤、请假和打卡视图。', icon: '勤', path: '/workbench/attendance' },
    { title: '客户跟进', description: '查看待跟进客户和预约参观。', icon: '客', path: '/marketing/workbench' },
    { title: '护理任务', description: '进入医护任务板与预警处理。', icon: '护', path: '/medical-care/care-task-board' },
    { title: '后勤工单', description: '查看维修、巡检与保障任务。', icon: '工', path: '/logistics/task-center' }
  ]
  return actions.filter((item) => canAccess(item.path))
})

async function refreshAttendance() {
  const data = await getAttendanceOverview({
    staffId: (userStore.staffInfo?.id as string | number | undefined) || undefined
  })
  Object.assign(attendanceOverview, data || {})
}

async function loadWorkbench() {
  loading.value = true
  try {
    const [portal, logistics] = await Promise.all([
      getPortalSummary({ silent403: true, silentError: true }).catch(() => null),
      getLogisticsWorkbenchSummary(undefined, { silent403: true, silentError: true }).catch(() => null),
      refreshAttendance().catch(() => undefined)
    ])
    portalSummary.value = portal
    logisticsSummary.value = logistics
    refreshedAt.value = new Date().toLocaleString('zh-CN', {
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } finally {
    loading.value = false
  }
}

async function handleAttendanceAction(action: AttendanceAction) {
  attendanceSubmitting.value = true
  try {
    await punchAttendance(action)
    message.success(action === 'IN' ? '上班打卡成功' : action === 'OUT' ? '下班打卡成功' : '考勤状态已更新')
    await refreshAttendance()
  } catch (error: any) {
    message.error(error?.message || '打卡失败，请稍后重试')
  } finally {
    attendanceSubmitting.value = false
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

onMounted(() => {
  loadWorkbench()
})
</script>

<style scoped>
.workbench-summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.workbench-shell {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
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

.workbench-chip-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.workbench-chip,
.workbench-list-item,
.recent-visit-item {
  width: 100%;
  border: 1px solid rgba(212, 224, 232, 0.9);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.94);
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

@media (max-width: 1280px) {
  .workbench-summary-grid,
  .workbench-shell,
  .workbench-chip-grid,
  .common-actions-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .workbench-summary-grid,
  .workbench-shell,
  .workbench-chip-grid,
  .common-actions-grid {
    grid-template-columns: 1fr;
  }

  .schedule-head {
    grid-template-columns: 1fr;
  }
}
</style>
