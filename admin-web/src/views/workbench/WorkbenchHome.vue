<template>
  <PageContainer title="工作台" subTitle="个人待办、常用入口和业务中心在同一入口完成定位。">
    <section class="hero-shell card-elevated">
      <div class="hero-copy">
        <div class="hero-kicker">Personal Workspace</div>
        <h2>先处理我的事项，再进入业务中心。</h2>
        <p>工作台只保留个人入口和跨模块快捷入口，不再混放行政、人资、配置类菜单。</p>
        <a-space wrap>
          <a-button type="primary" @click="openPath('/workbench/todo')">查看我的待办</a-button>
          <a-button @click="openPath('/workbench/my-info')">查看我的信息</a-button>
          <a-button @click="openPath('/workbench/reports')">填写我的总结</a-button>
        </a-space>
      </div>

      <div class="hero-status">
        <div class="status-chip">
          <span>当前角色</span>
          <strong>{{ roleLabel }}</strong>
        </div>
        <div class="status-chip">
          <span>统计口径</span>
          <strong>个人视角</strong>
        </div>
        <div class="status-chip">
          <span>最后刷新</span>
          <strong>{{ refreshedAt }}</strong>
        </div>
      </div>
    </section>

    <section class="metric-strip">
      <div v-for="item in metricItems" :key="item.label" class="metric-tile card-elevated" @click="openPath(item.path)">
        <span class="metric-label">{{ item.label }}</span>
        <strong class="metric-value">{{ item.value }}</strong>
        <span class="metric-desc">{{ item.desc }}</span>
      </div>
    </section>

    <a-row :gutter="[16, 16]">
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
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import { getPortalSummary } from '../../api/oa'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'
import { useUserStore } from '../../stores/user'
import { resolveRouteAccess } from '../../utils/routeAccess'

type ActionItem = {
  label: string
  path: string
  tip: string
}

const router = useRouter()
const userStore = useUserStore()
const refreshedAt = ref('--')
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
  { label: '我的待办', value: summary.openTodoCount || 0, desc: '需要我处理的协同事项', path: '/workbench/todo' },
  { label: '待我审批', value: summary.pendingApprovalCount || 0, desc: '需要我确认的流程', path: '/workbench/approvals' },
  { label: '进行中任务', value: summary.ongoingTaskCount || 0, desc: '仍在推进的任务', path: '/oa/work-execution/task' },
  { label: '已交总结', value: summary.submittedReportCount || 0, desc: '近30天已提交数量', path: '/workbench/reports' },
  { label: '生日提醒', value: summary.birthdayTodoCount || 0, desc: '与我相关的生日待办', path: '/oa/todo?keyword=生日提醒' },
  { label: '审批超时', value: summary.approvalTimeoutCount || 0, desc: '需要催办或升级处理', path: '/workbench/approvals' }
].filter((item) => canAccess(item.path)))

const personalActions = computed<ActionItem[]>(() => filterActions([
  { label: '我的待办', path: '/workbench/todo', tip: '查看今天需要处理的事项' },
  { label: '我的信息', path: '/workbench/my-info', tip: '查看薪资、绩效、报销和提醒' },
  { label: '我的考勤与请假', path: '/workbench/attendance', tip: '查看出勤、请假和审批进度' },
  { label: '我的总结', path: '/workbench/reports', tip: '填写日报、周报、月报和年报' },
  { label: '我的审批', path: '/workbench/approvals', tip: '查看我发起和我处理的审批' }
]))

const quickLaunchActions = computed<ActionItem[]>(() => filterActions([
  { label: '发起审批', path: '/oa/approval', tip: '请假、报销、采购等流程' },
  { label: '创建待办', path: '/oa/todo', tip: '记录提醒、生日、协作事项' },
  { label: '任务管理', path: '/oa/work-execution/task', tip: '新增或跟进任务' },
  { label: '文档中心', path: '/oa/document', tip: '上传、查找和归档文件' },
  { label: '活动中心', path: '/oa/activity-center/plan', tip: '活动计划、方案、执行和复盘' }
]))

const centerActions = computed<ActionItem[]>(() => filterActions([
  { label: '行政中心', path: '/oa/overview', tip: '协同办公、活动文化、员工服务' },
  { label: '人力资源中心', path: '/hr/overview', tip: '招聘、档案、考勤、费用、培训绩效' },
  { label: '长者管理', path: '/elder/in-hospital-overview', tip: '长者档案、评估、入住和在院服务' },
  { label: '统计分析', path: '/stats', tip: '指标监控与经营分析' }
]))

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
  const res = await getPortalSummary({ params: { scope: 'PERSONAL' }, silent403: true })
  summary.openTodoCount = Number(res?.openTodoCount || 0)
  summary.pendingApprovalCount = Number(res?.pendingApprovalCount || 0)
  summary.ongoingTaskCount = Number(res?.ongoingTaskCount || 0)
  summary.submittedReportCount = Number(res?.submittedReportCount || 0)
  summary.birthdayTodoCount = Number(res?.birthdayTodoCount || 0)
  summary.approvalTimeoutCount = Number(res?.approvalTimeoutCount || 0)
  refreshedAt.value = dayjs().format('MM-DD HH:mm')
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
    linear-gradient(135deg, rgba(255, 255, 255, 0.95), rgba(244, 250, 255, 0.9)),
    radial-gradient(circle at top right, rgba(53, 156, 220, 0.18), transparent 38%);
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
  font-size: 28px;
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

.status-chip {
  padding: 16px 18px;
  border-radius: 18px;
  background: rgba(16, 55, 88, 0.9);
  color: rgba(255, 255, 255, 0.74);
}

.status-chip strong {
  display: block;
  margin-top: 6px;
  font-size: 18px;
  color: #fff;
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
