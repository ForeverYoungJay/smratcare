<template>
  <PageContainer title="行政中心" subTitle="按职责进入协同办公、活动文化和员工服务，不再把个人入口混在业务域里。">
    <section class="center-hero card-elevated">
      <div class="hero-main">
        <div class="hero-kicker">Administrative Domain</div>
        <h2>行政管理只保留业务能力，不再承担个人工作台。</h2>
        <p>审批、通知、任务、文档、知识、活动和员工服务都按职责分区。个人待办、我的信息、我的考勤已移到工作台。</p>
        <a-space wrap>
          <a-button type="primary" @click="openPath('/oa/approval')">进入审批中心</a-button>
          <a-button @click="openPath('/oa/document')">进入文档中心</a-button>
          <a-button @click="openPath('/workbench')">返回工作台</a-button>
        </a-space>
      </div>
      <div class="hero-aside">
        <div class="aside-item">
          <span>未完成待办</span>
          <strong>{{ summary.openTodoCount || 0 }}</strong>
        </div>
        <div class="aside-item">
          <span>审批处理中</span>
          <strong>{{ summary.pendingApprovalCount || 0 }}</strong>
        </div>
        <div class="aside-item">
          <span>进行中任务</span>
          <strong>{{ summary.ongoingTaskCount || 0 }}</strong>
        </div>
      </div>
    </section>

    <a-row :gutter="[16, 16]">
      <a-col v-for="section in visibleSections" :key="section.title" :xs="24" :xl="8">
        <section class="section-shell card-elevated">
          <div class="section-head">
            <div>
              <div class="section-kicker">{{ section.kicker }}</div>
              <h3>{{ section.title }}</h3>
            </div>
            <a-tag :color="section.color">{{ section.actions.length }} 项</a-tag>
          </div>
          <p class="section-desc">{{ section.desc }}</p>
          <div class="section-actions">
            <button
              v-for="action in section.actions"
              :key="action.path"
              type="button"
              class="section-link"
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
import { getPortalSummary } from '../../api/oa'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'
import { useUserStore } from '../../stores/user'
import { hasMinisterOrHigher } from '../../utils/roleAccess'
import { resolveRouteAccess } from '../../utils/routeAccess'

type CenterAction = {
  label: string
  path: string
  tip: string
}

type CenterSection = {
  title: string
  kicker: string
  desc: string
  color: string
  actions: CenterAction[]
}

const router = useRouter()
const userStore = useUserStore()
const summary = reactive({
  openTodoCount: 0,
  pendingApprovalCount: 0,
  ongoingTaskCount: 0
})

const sections = computed<CenterSection[]>(() => [
  {
    title: '协同办公',
    kicker: 'Collaboration',
    desc: '处理通知、审批、任务、文档和知识，面向日常运营协作。',
    color: 'blue',
    actions: filterActions([
      { label: '任务管理', path: '/oa/work-execution/task', tip: '新增、跟进和归档协作任务' },
      { label: '协同日历', path: '/oa/work-execution/calendar', tip: '查看行政与协作日程' },
      { label: '通知公告', path: '/oa/notice', tip: '发布内部通知和公告' },
      { label: '审批中心', path: '/oa/approval', tip: '处理固定审批和业务审批' },
      { label: '文档中心', path: '/oa/document', tip: '统一管理制度、资料和归档文件' },
      { label: '知识库', path: '/oa/knowledge', tip: '沉淀制度、经验和流程文档' }
    ])
  },
  {
    title: '活动与文化',
    kicker: 'Culture & Events',
    desc: '把活动策划、执行记录、满意度调查和文化运营集中到一个分区。',
    color: 'purple',
    actions: filterActions([
      { label: '活动中心', path: '/oa/activity-center/plan', tip: '计划、方案、审批、执行和复盘' },
      { label: '活动记录', path: '/oa/activity-center/records', tip: '查看历史活动和执行记录' },
      { label: '满意度调查', path: '/oa/activity-center/survey-manage', tip: '维护调查模板和发放计划' },
      { label: '调查统计', path: '/oa/activity-center/survey-stats', tip: '查看回收情况和满意度结果' }
    ])
  },
  {
    title: '员工服务',
    kicker: 'Employee Services',
    desc: '承接员工生活服务、一卡通等服务型能力，不再混入人资管理。',
    color: 'cyan',
    actions: filterActions([
      { label: '生活服务', path: '/oa/life/birthday', tip: '生日提醒、房间打扫、维修管理' },
      { label: '一卡通', path: '/oa/card/account', tip: '卡务、充值和消费记录' }
    ])
  }
])

const visibleSections = computed(() => sections.value.filter((item) => item.actions.length > 0))

function canAccess(path: string) {
  return resolveRouteAccess(router, userStore.roles || [], path, userStore.pagePermissions || []).canAccess
}

function filterActions(items: CenterAction[]) {
  return items.filter((item) => canAccess(item.path))
}

function openPath(path: string) {
  if (!canAccess(path)) {
    message.warning('当前账号暂无权限访问该页面')
    return
  }
  router.push(path)
}

async function load() {
  const scope = hasMinisterOrHigher(userStore.roles || []) ? 'ORG' : 'PERSONAL'
  const res = await getPortalSummary({ params: { scope }, silent403: true })
  summary.openTodoCount = Number(res?.openTodoCount || 0)
  summary.pendingApprovalCount = Number(res?.pendingApprovalCount || 0)
  summary.ongoingTaskCount = Number(res?.ongoingTaskCount || 0)
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
.center-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.45fr) minmax(280px, 0.85fr);
  gap: 18px;
  padding: 24px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.96), rgba(243, 249, 255, 0.92)),
    radial-gradient(circle at top right, rgba(72, 139, 233, 0.18), transparent 40%);
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
  max-width: 580px;
  color: var(--muted);
}

.hero-aside {
  display: grid;
  gap: 10px;
}

.aside-item {
  padding: 16px 18px;
  border-radius: 18px;
  background: rgba(20, 53, 90, 0.94);
  color: rgba(255, 255, 255, 0.74);
}

.aside-item strong {
  display: block;
  margin-top: 6px;
  color: #fff;
  font-size: 28px;
  line-height: 1;
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

.section-actions {
  display: grid;
  gap: 10px;
}

.section-link {
  width: 100%;
  padding: 14px 16px;
  border: 1px solid rgba(16, 55, 88, 0.1);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.86);
  text-align: left;
  cursor: pointer;
  transition: transform 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease;
}

.section-link:hover {
  transform: translateY(-1px);
  border-color: rgba(31, 143, 190, 0.26);
  box-shadow: 0 12px 28px rgba(44, 104, 152, 0.12);
}

.section-link span {
  display: block;
  color: var(--ink);
  font-size: 15px;
  font-weight: 700;
}

.section-link small {
  display: block;
  margin-top: 6px;
  color: var(--muted);
  font-size: 12px;
  line-height: 1.55;
}

@media (max-width: 992px) {
  .center-hero {
    grid-template-columns: 1fr;
  }

  .hero-main h2 {
    font-size: 24px;
  }
}
</style>
