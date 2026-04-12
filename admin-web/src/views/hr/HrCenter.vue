<template>
  <PageContainer title="人力资源中心" subTitle="招聘、档案、考勤、合规、费用、培训绩效与激励按业务域统一组织。">
    <template #meta>
      <a-space wrap size="small">
        <span class="soft-pill">业务分区：{{ visibleSections.length }}</span>
        <span class="soft-pill">可达动作：{{ visibleActionCount }}</span>
        <span class="selection-pill">合同预警：{{ summary.contractExpiringCount || 0 }}</span>
        <span class="soft-pill">考勤异常：{{ summary.attendanceAbnormalCount || 0 }}</span>
      </a-space>
    </template>

    <section class="hero-shell card-elevated">
      <div class="hero-main">
        <div class="hero-kicker">Human Resources</div>
        <h2>人资入口回到“业务中心”，不再伪装成工作台。</h2>
        <p>这里是人力资源业务导航页，集中承接招聘、档案、考勤、费用和绩效激励。个人事项已经回到工作台。</p>
        <a-space wrap>
          <a-button type="primary" @click="openFirstAvailable(['/hr/profile/basic', '/hr/profile/social-security-reminders'])">进入员工档案</a-button>
          <a-button @click="openPath('/hr/attendance/schemes')">进入考勤与班组</a-button>
          <a-button @click="openPath('/workbench')">返回工作台</a-button>
        </a-space>
      </div>
      <div class="hero-metrics">
        <div class="metric-box">
          <span>在职人数</span>
          <strong>{{ summary.onJobCount || 0 }}</strong>
        </div>
        <div class="metric-box metric-box--warning">
          <span>合同到期预警</span>
          <strong>{{ summary.contractExpiringCount || 0 }}</strong>
        </div>
        <div class="metric-box">
          <span>考勤异常</span>
          <strong>{{ summary.attendanceAbnormalCount || 0 }}</strong>
        </div>
        <div class="metric-box metric-box--accent">
          <span>请假审批待办</span>
          <strong>{{ summary.pendingLeaveApprovalCount || 0 }}</strong>
        </div>
      </div>
    </section>

    <section class="summary-rail">
      <div class="summary-pill card-elevated" @click="openPath('/hr/profile/social-security-reminders')">
        <span>生日提醒</span>
        <strong>{{ summary.birthdayTodayCount || 0 }} / {{ summary.birthdayUpcomingCount || 0 }}</strong>
        <small>今日 / 7天内</small>
      </div>
      <div class="summary-pill card-elevated" @click="openPath('/hr/development/records')">
        <span>今日培训</span>
        <strong>{{ summary.todayTrainingCount || 0 }}</strong>
        <small>待执行培训安排</small>
      </div>
      <div class="summary-pill card-elevated" @click="openPath('/workbench/todo?keyword=生日提醒')">
        <span>生日待办</span>
        <strong>{{ summary.birthdayTodoCount || 0 }}</strong>
        <small>关联 OA 协同待办</small>
      </div>
      <div class="summary-pill card-elevated" @click="openPath('/hr/profile/contract-reminders')">
        <span>合同预警</span>
        <strong>{{ summary.contractExpiringCount || 0 }}</strong>
        <small>临近到期员工合同</small>
      </div>
    </section>

    <a-row :gutter="[16, 16]">
      <a-col v-for="section in visibleSections" :key="section.title" :xs="24" :xl="12">
        <section class="section-shell card-elevated">
          <div class="section-head">
            <div>
              <div class="section-kicker">{{ section.kicker }}</div>
              <h3>{{ section.title }}</h3>
            </div>
            <a-tag :color="section.color">{{ section.actions.length }} 项</a-tag>
          </div>
          <p class="section-desc">{{ section.desc }}</p>
          <div class="action-grid">
            <button
              v-for="action in section.actions"
              :key="action.path"
              type="button"
              class="action-link"
              @click="openPath(action.path)"
            >
              <span>{{ action.label }}</span>
              <small>{{ action.tip }}</small>
            </button>
          </div>
        </section>
      </a-col>
    </a-row>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { getHrWorkbenchSummary } from '../../api/hr'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'
import { useUserStore } from '../../stores/user'
import { resolveRouteAccess } from '../../utils/routeAccess'

type HrAction = {
  label: string
  path: string
  tip: string
}

type HrSection = {
  title: string
  kicker: string
  desc: string
  color: string
  actions: HrAction[]
}

const router = useRouter()
const userStore = useUserStore()
const summary = reactive({
  onJobCount: 0,
  todayTrainingCount: 0,
  pendingLeaveApprovalCount: 0,
  attendanceAbnormalCount: 0,
  contractExpiringCount: 0,
  birthdayTodayCount: 0,
  birthdayUpcomingCount: 0,
  birthdayTodoCount: 0
})

const sections = computed<HrSection[]>(() => [
  {
    title: '招聘与入转调离',
    kicker: 'Recruitment',
    desc: '覆盖招聘需求、岗位发布、候选人处理、入职与离职办理。',
    color: 'blue',
    actions: filterActions([
      { label: '招聘管理', path: '/hr/recruitment/needs', tip: '统一处理招聘流程和入职环节' },
      { label: '入职向导', path: '/hr/profile/onboarding', tip: '新员工入职和资料收集' }
    ])
  },
  {
    title: '员工档案',
    kicker: 'Employee Archive',
    desc: '承接员工基础信息、账号领导关系、合同、证书和附件归档。',
    color: 'cyan',
    actions: filterActions([
      { label: '员工基本信息', path: '/hr/profile/basic', tip: '主档、部门、任职状态、社保' },
      { label: '账号与领导设置', path: '/hr/profile/account-access', tip: '账号初始化、角色与领导链' },
      { label: '劳动合同管理', path: '/hr/profile/contracts', tip: '合同签订、续签和提醒' },
      { label: '证书管理', path: '/hr/development/certificates', tip: '证书和到期提醒' },
      { label: '社保提醒', path: '/hr/profile/social-security-reminders', tip: '到期、待办理与本月新增参保' }
    ])
  },
  {
    title: '考勤与班组',
    kicker: 'Attendance',
    desc: '排班、班组、考勤记录、异常和请假审批统一归口。',
    color: 'purple',
    actions: filterActions([
      { label: '排班管理', path: '/hr/attendance/schemes', tip: '班次方案和自动排班' },
      { label: '班组设置', path: '/hr/attendance/groups', tip: '班组结构和快捷打印' },
      { label: '请假审批', path: '/hr/attendance/leave-approval', tip: '请假、调班、加班处理' },
      { label: '考勤记录', path: '/hr/attendance/records', tip: '原始记录和考勤台账' }
    ])
  },
  {
    title: '制度、费用与绩效',
    kicker: 'Policy & Rewards',
    desc: '把规章制度、报销费用、培训发展、绩效与积分激励统一归到人资域。',
    color: 'gold',
    actions: filterActions([
      { label: '制度与合规', path: '/hr/compliance/policies', tip: '制度库和更新预警' },
      { label: '薪酬与费用', path: '/hr/expense/records?scene=training-reimburse', tip: '报销、补贴、餐费和电费' },
      { label: '培训发展', path: '/hr/development/records', tip: '培训计划、签到、记录和证书' },
      { label: '绩效管理', path: '/hr/performance/reports', tip: '绩效报表、评分规则和奖惩' },
      { label: '积分激励', path: '/hr/incentive/ledger', tip: '积分台账和积分规则' }
    ])
  }
])

const visibleSections = computed(() => sections.value.filter((item) => item.actions.length > 0))
const visibleActionCount = computed(() => visibleSections.value.reduce((sum, item) => sum + item.actions.length, 0))

function canAccess(path: string) {
  return resolveRouteAccess(router, userStore.roles || [], path, userStore.pagePermissions || []).canAccess
}

function filterActions(items: HrAction[]) {
  return items.filter((item) => canAccess(item.path))
}

function openPath(path: string) {
  if (!canAccess(path)) {
    message.warning('当前账号暂无权限访问该页面')
    return
  }
  router.push(path)
}

function openFirstAvailable(paths: string[]) {
  const target = paths.find((item) => canAccess(item))
  if (!target) {
    message.warning('当前账号暂无权限访问该页面')
    return
  }
  router.push(target)
}

async function load() {
  const res = await getHrWorkbenchSummary(undefined, { silent403: true })
  summary.onJobCount = Number(res?.onJobCount || 0)
  summary.todayTrainingCount = Number(res?.todayTrainingCount || 0)
  summary.pendingLeaveApprovalCount = Number(res?.pendingLeaveApprovalCount || 0)
  summary.attendanceAbnormalCount = Number(res?.attendanceAbnormalCount || 0)
  summary.contractExpiringCount = Number(res?.contractExpiringCount || 0)
  summary.birthdayTodayCount = Number(res?.birthdayTodayCount || 0)
  summary.birthdayUpcomingCount = Number(res?.birthdayUpcomingCount || 0)
  summary.birthdayTodoCount = Number(res?.birthdayTodoCount || 0)
}

onMounted(() => {
  load().catch(() => {})
})

useLiveSyncRefresh({
  topics: ['hr', 'oa', 'system'],
  refresh: () => {
    load().catch(() => {})
  },
  debounceMs: 900
})
</script>

<style scoped>
.hero-shell {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(320px, 0.95fr);
  gap: 18px;
  padding: 24px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.96), rgba(245, 250, 255, 0.92)),
    radial-gradient(circle at top right, rgba(84, 150, 225, 0.18), transparent 38%);
}

.hero-kicker,
.section-kicker {
  font-size: 11px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--muted);
  font-weight: 700;
}

.hero-main h2 {
  margin: 10px 0 8px;
  font-size: 28px;
  line-height: 1.18;
  color: var(--ink);
}

.hero-main p {
  margin: 0 0 16px;
  max-width: 600px;
  color: var(--muted);
}

.hero-metrics {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.metric-box {
  padding: 16px 18px;
  border-radius: 18px;
  background: rgba(13, 49, 83, 0.92);
  color: rgba(255, 255, 255, 0.72);
}

.metric-box strong {
  display: block;
  margin-top: 6px;
  color: #fff;
  font-size: 26px;
  line-height: 1;
}

.metric-box--warning {
  background: rgba(99, 74, 20, 0.9);
}

.metric-box--accent {
  background: linear-gradient(135deg, rgba(31, 143, 190, 0.95), rgba(100, 192, 226, 0.96));
}

.summary-rail {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.summary-pill {
  min-height: 116px;
  padding: 16px 18px;
  cursor: pointer;
  display: grid;
  align-content: space-between;
}

.summary-pill span {
  color: var(--muted);
  font-size: 12px;
}

.summary-pill strong {
  font-size: 28px;
  line-height: 1;
  color: var(--ink);
}

.summary-pill small {
  color: var(--muted);
  font-size: 12px;
}

.section-shell {
  height: 100%;
  padding: 20px;
}

.section-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.section-head h3 {
  margin: 6px 0 0;
  font-size: 22px;
  color: var(--ink);
}

.section-desc {
  margin: 12px 0 14px;
  color: var(--muted);
  line-height: 1.65;
}

.action-grid {
  display: grid;
  gap: 10px;
}

.action-link {
  width: 100%;
  padding: 14px 16px;
  border: 1px solid rgba(16, 55, 88, 0.1);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.86);
  text-align: left;
  cursor: pointer;
  transition: transform 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease;
}

.action-link:hover {
  transform: translateY(-1px);
  border-color: rgba(31, 143, 190, 0.26);
  box-shadow: 0 12px 28px rgba(44, 104, 152, 0.12);
}

.action-link span {
  display: block;
  color: var(--ink);
  font-size: 15px;
  font-weight: 700;
}

.action-link small {
  display: block;
  margin-top: 6px;
  color: var(--muted);
  font-size: 12px;
  line-height: 1.55;
}

@media (max-width: 1200px) {
  .summary-rail {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 992px) {
  .hero-shell {
    grid-template-columns: 1fr;
  }

  .hero-metrics {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .hero-main h2 {
    font-size: 24px;
  }
}

@media (max-width: 640px) {
  .summary-rail,
  .hero-metrics {
    grid-template-columns: 1fr;
  }
}
</style>
