<template>
  <PageContainer title="工作台" subTitle="个人待办、常用入口和业务中心在同一入口完成定位。" mode="showcase">
    <template #meta>
      <span class="status-pill">当前角色 {{ roleLabel }}</span>
      <span class="soft-pill">个人视角</span>
      <span class="soft-pill">最后刷新 {{ refreshedAt }}</span>
    </template>

    <template #extra>
      <a-space wrap>
        <a-button type="primary" @click="openPath('/workbench/todo')">查看我的待办</a-button>
        <a-button @click="openPath('/workbench/approvals')">处理审批</a-button>
        <a-button v-if="attendanceActionButtons.includes('IN')" @click="handleAttendanceAction('IN')">上班打卡</a-button>
        <a-button v-if="attendanceActionButtons.includes('OUT')" @click="handleAttendanceAction('OUT')">下班打卡</a-button>
      </a-space>
    </template>

    <section class="hero-shell card-elevated">
      <div class="hero-copy">
        <div class="hero-kicker">Personal Workspace</div>
        <h2>先扫重点，再处理待办，再进入业务中心。</h2>
        <p>这里保留员工日常高频事项、跨模块快捷入口和职责中心，帮助你在 3 秒内找到今天最该处理的工作。</p>
        <a-space wrap>
          <a-button type="primary" @click="openPath('/workbench/todo')">进入待办</a-button>
          <a-button @click="openPath('/workbench/my-info')">查看我的信息</a-button>
          <a-button @click="openPath('/oa/work-execution/task')">进入任务中心</a-button>
        </a-space>
      </div>

      <div class="hero-status">
        <div class="hero-status-head">
          <span>今日优先处理</span>
          <strong>{{ priorityItems.length }} 项重点</strong>
        </div>
        <button
          v-for="item in priorityItems"
          :key="item.label"
          type="button"
          class="priority-item"
          @click="openPath(item.path)"
        >
          <div>
            <span>{{ item.label }}</span>
            <small>{{ item.desc }}</small>
          </div>
          <strong>{{ item.value }}</strong>
        </button>
      </div>
    </section>

    <section class="metric-strip">
      <div
        v-for="item in metricItems"
        :key="item.label"
        class="metric-tile card-elevated"
        :class="`metric-tile--${item.tone}`"
        @click="openPath(item.path)"
      >
        <span class="metric-label">{{ item.label }}</span>
        <strong class="metric-value">{{ item.value }}</strong>
        <span class="metric-desc">{{ item.desc }}</span>
      </div>
    </section>

    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :xl="8">
        <section class="lane-shell card-elevated attendance-panel">
          <div class="lane-head">
            <div>
              <div class="lane-kicker">Attendance Today</div>
              <h3>今日出勤</h3>
            </div>
            <a-tag :color="attendanceTagColor">{{ attendanceOverview.todayStatusLabel || '未打卡' }}</a-tag>
          </div>
          <div class="attendance-summary">
            <div class="attendance-main">
              <strong>{{ attendanceOverview.todayStatusLabel || '未打卡' }}</strong>
              <p>{{ attendanceSeasonLabel }} {{ attendanceOverview.expectedWorkStart || '--:--' }} - {{ attendanceOverview.expectedWorkEnd || '--:--' }}</p>
            </div>
            <div class="attendance-sub">
              <span>当月在岗 {{ attendanceOverview.onDutyDays || 0 }} 天</span>
              <span>请假 {{ attendanceOverview.leaveDays || 0 }} 天</span>
              <span>异常 {{ attendanceOverview.abnormalCount || 0 }} 次</span>
            </div>
            <div class="attendance-actions">
              <a-button v-if="attendanceActionButtons.includes('IN')" type="primary" :loading="attendanceSubmitting" @click="handleAttendanceAction('IN')">上班打卡</a-button>
              <a-button v-if="attendanceActionButtons.includes('START_LUNCH')" :loading="attendanceSubmitting" @click="handleAttendanceAction('START_LUNCH')">开始午休</a-button>
              <a-button v-if="attendanceActionButtons.includes('END_LUNCH')" type="primary" ghost :loading="attendanceSubmitting" @click="handleAttendanceAction('END_LUNCH')">结束午休</a-button>
              <a-button v-if="attendanceActionButtons.includes('START_OUTING')" :loading="attendanceSubmitting" @click="handleAttendanceAction('START_OUTING')">开始外出</a-button>
              <a-button v-if="attendanceActionButtons.includes('END_OUTING')" type="primary" ghost :loading="attendanceSubmitting" @click="handleAttendanceAction('END_OUTING')">结束外出</a-button>
              <a-button v-if="attendanceActionButtons.includes('OUT')" :loading="attendanceSubmitting" @click="handleAttendanceAction('OUT')">下班打卡</a-button>
              <a-button @click="openPath('/workbench/attendance')">查看考勤与请假</a-button>
            </div>
          </div>
        </section>
      </a-col>

      <a-col :xs="24" :xl="8">
        <section class="lane-shell card-elevated">
          <div class="lane-head">
            <div>
              <div class="lane-kicker">My Actions</div>
              <h3>我的工作</h3>
            </div>
            <a-tag color="blue">{{ personalActions.length }} 项</a-tag>
          </div>
          <div class="action-grid">
            <button
              v-for="action in personalActions"
              :key="action.path"
              type="button"
              class="action-chip"
              @click="openPath(action.path)"
            >
              <span>{{ action.label }}</span>
              <small>{{ action.tip }}</small>
            </button>
          </div>
        </section>
      </a-col>

      <a-col :xs="24" :xl="8">
        <section class="lane-shell card-elevated">
          <div class="lane-head">
            <div>
              <div class="lane-kicker">Quick Launch</div>
              <h3>快捷发起</h3>
            </div>
            <a-tag color="cyan">跨模块</a-tag>
          </div>
          <div class="action-grid">
            <button
              v-for="action in quickLaunchActions"
              :key="action.path"
              type="button"
              class="action-chip action-chip--soft"
              @click="openPath(action.path)"
            >
              <span>{{ action.label }}</span>
              <small>{{ action.tip }}</small>
            </button>
          </div>
        </section>
      </a-col>

      <a-col :xs="24" :xl="8">
        <section class="lane-shell card-elevated">
          <div class="lane-head">
            <div>
              <div class="lane-kicker">Business Centers</div>
              <h3>业务中心</h3>
            </div>
            <a-tag color="purple">按职责划分</a-tag>
          </div>
          <div class="center-list">
            <button
              v-for="item in centerActions"
              :key="item.path"
              type="button"
              class="center-item"
              @click="openPath(item.path)"
            >
              <div>
                <strong>{{ item.label }}</strong>
                <p>{{ item.tip }}</p>
              </div>
              <span>进入</span>
            </button>
          </div>
        </section>
      </a-col>
    </a-row>

    <a-row :gutter="[16, 16]" style="margin-top: 16px">
      <a-col :xs="24" :xl="12">
        <section class="lane-shell card-elevated preview-panel">
          <div class="lane-head">
            <div>
              <div class="lane-kicker">Todo Preview</div>
              <h3>我的待办预览</h3>
            </div>
            <a-button type="link" @click="openPath('/workbench/todo')">查看全部</a-button>
          </div>
          <div v-if="portalTodos.length" class="preview-list">
            <button
              v-for="item in portalTodos"
              :key="String(item.id)"
              type="button"
              class="preview-item"
              @click="openPath('/workbench/todo')"
            >
              <div>
                <strong>{{ item.title }}</strong>
                <p>{{ item.assigneeName || '待处理' }}</p>
              </div>
              <span>{{ item.dueTime ? dayjs(item.dueTime).format('MM-DD HH:mm') : '无截止' }}</span>
            </button>
          </div>
          <a-empty v-else description="暂无待办" />
        </section>
      </a-col>
      <a-col :xs="24" :xl="12">
        <section class="lane-shell card-elevated preview-panel">
          <div class="lane-head">
            <div>
              <div class="lane-kicker">Notice Preview</div>
              <h3>近期公告</h3>
            </div>
            <a-button type="link" @click="openPath('/oa/notice')">查看全部</a-button>
          </div>
          <div v-if="portalNotices.length" class="preview-list">
            <button
              v-for="item in portalNotices"
              :key="String(item.id)"
              type="button"
              class="preview-item"
              @click="openPath('/oa/notice')"
            >
              <div>
                <strong>{{ item.title }}</strong>
                <p>{{ item.publisherName || '系统公告' }}</p>
              </div>
              <span>{{ item.publishTime ? dayjs(item.publishTime).format('MM-DD') : '-' }}</span>
            </button>
          </div>
          <a-empty v-else description="暂无公告" />
        </section>
      </a-col>
    </a-row>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import { getPortalSummary } from '../../api/oa'
import { getAttendanceOverview, punchAttendance } from '../../api/schedule'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'
import { useUserStore } from '../../stores/user'
import { resolveRouteAccess } from '../../utils/routeAccess'
import type { AttendanceDashboardOverview, OaNotice, OaTodo } from '../../types'

type ActionItem = {
  label: string
  path: string
  tip: string
}

type AttendanceAction = 'IN' | 'OUT' | 'START_LUNCH' | 'END_LUNCH' | 'START_OUTING' | 'END_OUTING'

const router = useRouter()
const userStore = useUserStore()
const refreshedAt = ref('--')
const attendanceSubmitting = ref(false)
const attendanceOverview = reactive<AttendanceDashboardOverview>({ days: [] })
const portalTodos = ref<OaTodo[]>([])
const portalNotices = ref<OaNotice[]>([])
const summary = reactive({
  openTodoCount: 0,
  pendingApprovalCount: 0,
  ongoingTaskCount: 0,
  submittedReportCount: 0,
  birthdayTodoCount: 0,
  approvalTimeoutCount: 0
})

const roleLabel = computed(() => {
  const roles = userStore.roles || []
  if (roles.includes('HR_MINISTER')) return '行政人事部部长'
  if (roles.includes('HR_EMPLOYEE')) return '行政人事部员工'
  if (roles.includes('NURSING_MINISTER')) return '护理部长'
  if (roles.includes('NURSING_EMPLOYEE')) return '护理员工'
  if (roles.includes('DIRECTOR')) return '院长'
  return '员工账号'
})

const metricItems = computed(() => [
  { label: '我的待办', value: summary.openTodoCount || 0, desc: '需要我处理的协同事项', path: '/workbench/todo', tone: 'primary' },
  { label: '待我审批', value: summary.pendingApprovalCount || 0, desc: '需要我确认的流程', path: '/workbench/approvals', tone: 'warning' },
  { label: '进行中任务', value: summary.ongoingTaskCount || 0, desc: '仍在推进的任务', path: '/oa/work-execution/task', tone: 'success' },
  { label: '已交总结', value: summary.submittedReportCount || 0, desc: '近30天已提交数量', path: '/workbench/reports', tone: 'neutral' },
  { label: '生日提醒', value: summary.birthdayTodoCount || 0, desc: '与我相关的生日待办', path: '/workbench/todo?keyword=生日提醒', tone: 'success' },
  { label: '审批超时', value: summary.approvalTimeoutCount || 0, desc: '需要催办或升级处理', path: '/workbench/approvals', tone: 'danger' }
].filter((item) => canAccess(item.path)))

const priorityItems = computed(() =>
  [
    { label: '待办事项', value: summary.openTodoCount || 0, desc: '先清空今天必须处理的协同任务', path: '/workbench/todo' },
    { label: '待我审批', value: summary.pendingApprovalCount || 0, desc: '优先处理影响他人推进的审批流', path: '/workbench/approvals' },
    { label: '超时关注', value: summary.approvalTimeoutCount || 0, desc: '存在催办或升级处理风险', path: '/workbench/approvals' }
  ]
    .filter((item) => canAccess(item.path))
    .sort((a, b) => Number(b.value) - Number(a.value))
)

const personalActions = computed<ActionItem[]>(() => filterActions([
  { label: '我的待办', path: '/workbench/todo', tip: '查看今天需要处理的事项' },
  { label: '我的信息', path: '/workbench/my-info', tip: '查看薪资、绩效、报销和提醒' },
  { label: '我的考勤与请假', path: '/workbench/attendance', tip: '查看出勤、请假和审批进度' },
  { label: '我的总结', path: '/workbench/reports', tip: '填写日报、周报、月报和年报' },
  { label: '我的审批', path: '/workbench/approvals', tip: '查看我发起和我处理的审批' }
]))

const quickLaunchActions = computed<ActionItem[]>(() => filterActions([
  { label: '发起审批', path: '/oa/approval', tip: '请假、报销、采购等流程' },
  { label: '创建待办', path: '/workbench/todo', tip: '记录提醒、生日、协作事项' },
  { label: '任务管理', path: '/oa/work-execution/task', tip: '新增或跟进任务' },
  { label: '文档中心', path: '/oa/document', tip: '上传、查找和归档文件' },
  { label: '活动中心', path: '/oa/activity-center/plan', tip: '活动策划、方案、执行和复盘' }
]))

const centerActions = computed<ActionItem[]>(() => filterActions([
  { label: '行政中心', path: '/oa/overview', tip: '协同办公、活动文化、员工服务' },
  { label: '人力资源中心', path: '/hr/overview', tip: '招聘、档案、考勤、费用、培训绩效' },
  { label: '长者管理', path: '/elder/in-hospital-overview', tip: '长者档案、评估、入住和在院服务' },
  { label: '统计分析', path: '/stats', tip: '指标监控与经营分析' }
]))
const attendanceTodayRecord = computed(() => {
  const today = dayjs().format('YYYY-MM-DD')
  return (attendanceOverview.days || []).find((item) => String(item?.date || '') === today)
})
const attendanceSeasonLabel = computed(() => attendanceOverview.seasonType === 'WINTER' ? '冬季' : '夏季')
const attendanceTagColor = computed(() => {
  const status = String(attendanceOverview.todayStatus || '').toUpperCase()
  if (status === 'ON_DUTY') return 'green'
  if (status === 'LUNCH_BREAK') return 'blue'
  if (status === 'OUTING') return 'orange'
  if (status === 'OFF_DUTY') return 'purple'
  if (status === 'ON_LEAVE') return 'cyan'
  return 'default'
})
const attendanceActionButtons = computed<AttendanceAction[]>(() => {
  const status = String(attendanceOverview.todayStatus || '').toUpperCase()
  const hasLunchStarted = !!attendanceTodayRecord.value?.lunchBreakStartTime
  const hasOutingStarted = !!attendanceTodayRecord.value?.outingStartTime
  if (!status || status === 'NOT_CHECKED_IN') return ['IN']
  if (status === 'LUNCH_BREAK') return ['END_LUNCH']
  if (status === 'OUTING') return ['END_OUTING']
  if (status === 'ON_DUTY') {
    const actions: AttendanceAction[] = []
    if (!hasLunchStarted) actions.push('START_LUNCH')
    if (!hasOutingStarted) actions.push('START_OUTING')
    actions.push('OUT')
    return actions
  }
  return []
})

function filterActions(items: ActionItem[]) {
  return items.filter((item) => canAccess(item.path))
}

function canAccess(path: string) {
  return resolveRouteAccess(router, userStore.roles || [], path, userStore.pagePermissions || []).canAccess
}

function openPath(path: string) {
  if (!canAccess(path)) {
    message.warning('当前账号暂无权限访问该入口')
    return
  }
  router.push(path)
}

async function load() {
  const [portalResult, attendanceResult] = await Promise.allSettled([
    getPortalSummary({ params: { scope: 'PERSONAL' }, silent403: true }),
    getAttendanceOverview({ month: dayjs().format('YYYY-MM') })
  ])

  if (portalResult.status === 'fulfilled') {
    const res = portalResult.value
    summary.openTodoCount = Number(res?.openTodoCount || 0)
    summary.pendingApprovalCount = Number(res?.pendingApprovalCount || 0)
    summary.ongoingTaskCount = Number(res?.ongoingTaskCount || 0)
    summary.submittedReportCount = Number(res?.submittedReportCount || 0)
    summary.birthdayTodoCount = Number(res?.birthdayTodoCount || 0)
    summary.approvalTimeoutCount = Number(res?.approvalTimeoutCount || 0)
    portalTodos.value = Array.isArray(res?.todos) ? res.todos : []
    portalNotices.value = Array.isArray(res?.notices) ? res.notices : []
  }

  if (attendanceResult.status === 'fulfilled') {
    Object.assign(attendanceOverview, attendanceResult.value || { days: [] })
  }

  refreshedAt.value = dayjs().format('MM-DD HH:mm')
}

async function handleAttendanceAction(action: AttendanceAction) {
  attendanceSubmitting.value = true
  try {
    await punchAttendance(action)
    message.success(resolveAttendanceActionText(action))
    await load()
  } catch (error: any) {
    message.error(error?.message || `${resolveAttendanceActionText(action)}失败`)
  } finally {
    attendanceSubmitting.value = false
  }
}

function resolveAttendanceActionText(action: AttendanceAction) {
  if (action === 'IN') return '上班打卡成功'
  if (action === 'OUT') return '下班打卡成功'
  if (action === 'START_LUNCH') return '开始午休成功'
  if (action === 'END_LUNCH') return '结束午休成功'
  if (action === 'START_OUTING') return '开始外出成功'
  return '结束外出成功'
}

onMounted(() => {
  load().catch(() => {})
})

useLiveSyncRefresh({
  topics: ['oa', 'hr', 'system'],
  refresh: () => {
    load().catch(() => {})
  },
  debounceMs: 900
})
</script>

<style scoped>
.hero-shell {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(280px, 0.9fr);
  gap: 18px;
  padding: 24px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.98), rgba(244, 250, 255, 0.94)),
    radial-gradient(circle at top right, rgba(53, 156, 220, 0.16), transparent 38%);
}

.hero-kicker,
.lane-kicker {
  font-size: 11px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--muted);
  font-weight: 700;
}

.hero-copy h2 {
  margin: 10px 0 8px;
  font-size: 30px;
  line-height: 1.18;
  color: var(--ink);
}

.hero-copy p {
  max-width: 560px;
  margin: 0 0 16px;
  color: var(--muted);
}

.hero-status {
  display: grid;
  gap: 10px;
}

.hero-status-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 10px;
  padding: 4px 4px 2px;
}

.hero-status-head span {
  color: var(--muted);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero-status-head strong {
  color: var(--ink);
  font-size: 18px;
}

.priority-item {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 16px 18px;
  border: 1px solid rgba(19, 108, 181, 0.1);
  border-radius: 18px;
  background: rgba(248, 252, 255, 0.9);
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.priority-item:hover {
  transform: translateY(-1px);
  border-color: rgba(19, 108, 181, 0.24);
  box-shadow: 0 14px 28px rgba(18, 49, 77, 0.08);
}

.priority-item span,
.priority-item small {
  display: block;
}

.priority-item span {
  color: var(--ink);
  font-size: 14px;
  font-weight: 700;
}

.priority-item small {
  margin-top: 6px;
  color: var(--muted);
  font-size: 12px;
  line-height: 1.55;
}

.priority-item strong {
  min-width: 52px;
  text-align: right;
  color: var(--primary);
  font-size: 28px;
  line-height: 1;
}

.metric-strip {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 12px;
}

.metric-tile {
  min-height: 122px;
  padding: 16px 18px;
  cursor: pointer;
  display: grid;
  align-content: space-between;
}

.metric-tile--primary {
  background: linear-gradient(180deg, rgba(19, 108, 181, 0.1) 0%, rgba(255, 255, 255, 0.96) 100%);
}

.metric-tile--warning {
  background: linear-gradient(180deg, rgba(217, 137, 34, 0.1) 0%, rgba(255, 255, 255, 0.96) 100%);
}

.metric-tile--success {
  background: linear-gradient(180deg, rgba(47, 155, 102, 0.1) 0%, rgba(255, 255, 255, 0.96) 100%);
}

.metric-tile--danger {
  background: linear-gradient(180deg, rgba(214, 76, 95, 0.1) 0%, rgba(255, 255, 255, 0.96) 100%);
}

.metric-label {
  color: var(--muted);
  font-size: 12px;
}

.metric-value {
  font-size: 30px;
  line-height: 1;
  color: var(--ink);
}

.metric-desc {
  color: var(--muted);
  font-size: 12px;
  line-height: 1.5;
}

.lane-shell {
  height: 100%;
  padding: 20px;
}

.attendance-panel,
.preview-panel {
  height: 100%;
}

.lane-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.lane-head h3 {
  margin: 6px 0 0;
  font-size: 20px;
  color: var(--ink);
}

.action-grid {
  display: grid;
  gap: 10px;
}

.action-chip,
.center-item {
  width: 100%;
  border: 1px solid rgba(20, 70, 110, 0.1);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.84);
  text-align: left;
  padding: 14px 16px;
  cursor: pointer;
  transition: transform 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease;
}

.action-chip:hover,
.center-item:hover {
  transform: translateY(-1px);
  border-color: rgba(31, 143, 190, 0.28);
  box-shadow: 0 12px 28px rgba(44, 104, 152, 0.12);
}

.action-chip span,
.center-item strong {
  display: block;
  color: var(--ink);
  font-size: 15px;
  font-weight: 700;
}

.action-chip small,
.center-item p {
  display: block;
  margin-top: 6px;
  color: var(--muted);
  font-size: 12px;
  line-height: 1.55;
}

.action-chip--soft {
  background: linear-gradient(135deg, rgba(239, 248, 252, 0.96), rgba(255, 255, 255, 0.9));
}

.center-list {
  display: grid;
  gap: 10px;
}

.center-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.center-item p {
  margin: 6px 0 0;
}

.center-item span {
  color: var(--primary);
  font-size: 12px;
  font-weight: 700;
}

.attendance-summary {
  display: grid;
  gap: 14px;
}

.attendance-main strong {
  display: block;
  font-size: 24px;
  color: var(--ink);
}

.attendance-main p {
  margin: 6px 0 0;
  color: var(--muted);
}

.attendance-sub {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 16px;
  color: var(--muted);
  font-size: 13px;
}

.attendance-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.preview-list {
  display: grid;
  gap: 10px;
}

.preview-item {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 16px;
  border: 1px solid rgba(20, 70, 110, 0.1);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.84);
  cursor: pointer;
  text-align: left;
  transition: transform 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease;
}

.preview-item:hover {
  transform: translateY(-1px);
  border-color: rgba(31, 143, 190, 0.28);
  box-shadow: 0 12px 28px rgba(44, 104, 152, 0.12);
}

.preview-item strong {
  display: block;
  color: var(--ink);
  font-size: 15px;
  font-weight: 700;
}

.preview-item p {
  margin: 6px 0 0;
  color: var(--muted);
  font-size: 12px;
  line-height: 1.55;
}

.preview-item span {
  color: var(--muted);
  font-size: 12px;
  white-space: nowrap;
}

@media (max-width: 1400px) {
  .metric-strip {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 992px) {
  .hero-shell {
    grid-template-columns: 1fr;
  }

  .metric-strip {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .metric-strip {
    grid-template-columns: 1fr;
  }

  .hero-copy h2 {
    font-size: 24px;
  }
}
</style>
