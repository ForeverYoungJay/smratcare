<template>
  <PageContainer title="我的待办中心" subTitle="先处理最重要的协同事项，再下钻公告、审批与高频入口">
    <StatefulBlock :loading="loading" :error="errorText" :empty="false" @retry="load">
      <div class="portal-toolbar">
        <a-space wrap>
          <a-radio-group v-if="canSwitchScope" v-model:value="portalScope" button-style="solid" size="small">
            <a-radio-button value="PERSONAL">我的视角</a-radio-button>
            <a-radio-button value="ORG">机构视角</a-radio-button>
          </a-radio-group>
          <a-tag :color="portalScope === 'ORG' ? 'purple' : 'blue'">{{ scopeDescription }}</a-tag>
          <a-button size="small" type="primary" @click="openPath('/workbench/todo')">进入待办</a-button>
          <a-button size="small" @click="openPath('/oa/approval')">进入审批</a-button>
          <a-button size="small" @click="openPath('/workbench/reports')">写工作总结</a-button>
        </a-space>
      </div>
      <section class="portal-hero">
        <div class="hero-main">
          <div class="hero-label">当前主焦点</div>
          <div class="hero-value">{{ summary.openTodoCount || 0 }}</div>
          <div class="hero-title">待处理协同事项</div>
          <p class="hero-description">
            {{ heroDescription }}
          </p>
          <div class="hero-actions">
            <a-button type="primary" size="large" @click="openPath('/workbench/todo')">优先处理待办</a-button>
            <a-button size="large" @click="openPath('/oa/approval')">查看审批队列</a-button>
          </div>
        </div>
        <div class="hero-side">
          <div class="hero-alert" v-for="item in heroAlerts" :key="item.label" :class="`is-${item.tone}`">
            <div class="hero-alert-value">{{ item.value }}</div>
            <div class="hero-alert-label">{{ item.label }}</div>
          </div>
        </div>
      </section>

      <section class="snapshot-rail">
        <button
          v-for="metric in heroMetrics"
          :key="metric.label"
          type="button"
          class="snapshot-item"
          @click="metric.path && openPath(metric.path)"
        >
          <span class="snapshot-label">{{ metric.label }}</span>
          <strong class="snapshot-value">{{ metric.value }}</strong>
        </button>
      </section>

      <section class="portal-grid">
        <div class="panel panel-priority">
          <div class="panel-head">
            <div>
              <div class="panel-kicker">Today</div>
              <h3>优先处理</h3>
            </div>
            <a-button size="small" type="link" @click="openPath('/workbench/todo')">查看全部</a-button>
          </div>
          <div v-if="priorityTasks.length" class="priority-list">
            <button
              v-for="item in priorityTasks"
              :key="item.label"
              type="button"
              class="priority-item"
              @click="openPath(item.path)"
            >
              <span class="priority-label">{{ item.label }}</span>
              <strong class="priority-value">{{ item.value }}</strong>
            </button>
          </div>
          <a-empty v-else description="当前没有需要优先处理的事项" />
        </div>

        <div class="panel panel-actions">
          <div class="panel-head">
            <div>
              <div class="panel-kicker">Actions</div>
              <h3>高频入口</h3>
            </div>
          </div>
          <div class="action-list">
            <button
              v-for="action in commonActions"
              :key="action.path"
              type="button"
              class="action-item"
              @click="openPath(action.path)"
            >
              {{ action.label }}
            </button>
          </div>
        </div>

        <div class="panel panel-workflow">
          <div class="panel-head">
            <div>
              <div class="panel-kicker">Workflow</div>
              <h3>审批快捷入口</h3>
            </div>
          </div>
          <div v-if="workflowQuickActions.length" class="workflow-list">
            <button
              v-for="item in workflowQuickActions"
              :key="item.code"
              type="button"
              class="workflow-item"
              @click="openPath(item.route)"
            >
              <span>{{ item.name }}</span>
              <strong>{{ item.count }}</strong>
            </button>
          </div>
          <a-empty v-else description="当前没有需要优先处理的流程" />
        </div>

        <div class="panel panel-feed">
          <div class="panel-head">
            <div>
              <div class="panel-kicker">Feed</div>
              <h3>公告与待办流</h3>
            </div>
          </div>
          <div class="feed-columns">
            <div class="feed-column">
              <div class="feed-column-title">最新公告</div>
              <a-list :data-source="summary.notices" :render-item="renderNotice" :locale="{ emptyText: '暂无公告' }" />
            </div>
            <div class="feed-column">
              <div class="feed-column-title">{{ todoCardTitle }}</div>
              <a-list :data-source="summary.todos" :render-item="renderTodo" :locale="{ emptyText: '暂无待办' }" />
            </div>
          </div>
        </div>
      </section>
    </StatefulBlock>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, h, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
import { getPortalSummary } from '../../api/oa'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'
import { useUserStore } from '../../stores/user'
import { hasMinisterOrHigher } from '../../utils/roleAccess'
import { resolveRouteAccess } from '../../utils/routeAccess'
import type { OaPortalSummary, OaNotice, OaTodo, OaWorkflowTodoItem } from '../../types'

const summary = reactive<OaPortalSummary>({ notices: [], todos: [] })
const loading = ref(false)
const errorText = ref('')
const router = useRouter()
const userStore = useUserStore()
const PORTAL_SCOPE_STORAGE_KEY = 'oa-portal-scope'
const canSwitchScope = computed(() => hasMinisterOrHigher(userStore.roles || []))
const portalScope = ref<'PERSONAL' | 'ORG'>(readInitialPortalScope())
const pendingApprovalTitle = computed(() =>
  portalScope.value === 'ORG'
    ? '机构待审'
    : hasMinisterOrHigher(userStore.roles || [])
      ? '待我审批'
      : '审批处理中'
)
const scopeDescription = computed(() =>
  portalScope.value === 'ORG' ? '统计口径：机构整体协同情况' : '统计口径：只看我相关的待办、任务和审批'
)
const todoCardTitle = computed(() =>
  portalScope.value === 'ORG'
    ? `机构待办（近30天机构已提交总结 ${summary.submittedReportCount || 0} 条）`
    : `我的待办（近30天我已提交总结 ${summary.submittedReportCount || 0} 条）`
)
const workflowQuickActions = computed<OaWorkflowTodoItem[]>(() =>
  (summary.workflowTodos || []).filter((item) => Number(item.count || 0) > 0).slice(0, 6)
)
const heroDescription = computed(() =>
  `当前${portalScope.value === 'ORG' ? '机构侧' : '个人侧'}共有 ${summary.openTodoCount || 0} 条待办，其中超期 ${summary.overdueTodoCount || 0} 条、审批待处理 ${summary.pendingApprovalCount || 0} 条。`
)
const heroAlerts = computed(() => ([
  { label: '超期待办', value: summary.overdueTodoCount || 0, tone: (summary.overdueTodoCount || 0) > 0 ? 'danger' : 'calm' },
  { label: pendingApprovalTitle.value, value: summary.pendingApprovalCount || 0, tone: (summary.pendingApprovalCount || 0) > 0 ? 'focus' : 'calm' },
  { label: '健康异常', value: summary.healthAbnormalCount || 0, tone: (summary.healthAbnormalCount || 0) > 0 ? 'warning' : 'calm' }
]))
const heroMetrics = computed(() => ([
  { label: '生日待办', value: summary.birthdayTodoCount || 0, path: '/workbench/todo?keyword=生日提醒' },
  { label: '进行中任务', value: summary.ongoingTaskCount || 0, path: '/oa/work-execution/task' },
  { label: '今日日程', value: summary.todayScheduleCount || 0, path: '/oa/work-execution/calendar' },
  { label: '问卷草稿', value: summary.surveyDraftCount || 0, path: '/survey/template' },
  { label: '已发布问卷', value: summary.surveyPublishedCount || 0, path: '/survey/template?status=1' },
  { label: '今日回收', value: summary.surveyTodaySubmissionCount || 0, path: '/survey/stats' }
]))
const priorityTasks = computed(() => ([
  { label: '超期处理', value: summary.overdueTodoCount || 0, path: '/workbench/todo?status=OVERDUE' },
  { label: '审批排队', value: summary.pendingApprovalCount || 0, path: '/oa/approval' },
  { label: '健康异常', value: summary.healthAbnormalCount || 0, path: '/health/management/archive' },
  { label: '生日关怀', value: summary.birthdayTodoCount || 0, path: '/workbench/todo?keyword=生日提醒' }
]).filter((item) => Number(item.value || 0) > 0))
const commonActions = computed(() => ([
  { label: '审批流程', path: '/oa/approval' },
  { label: '我的待办', path: '/workbench/todo' },
  { label: '任务管理', path: '/oa/work-execution/task' },
  { label: '文档管理', path: '/oa/document' },
  { label: '通知管理', path: '/oa/notice' },
  { label: '我的总结', path: '/workbench/reports' },
  { label: '入住评估', path: '/elder/assessment/ability/admission' },
  { label: '入住办理', path: '/elder/admission-processing' },
  { label: '营销合同闭环', path: '/marketing/contracts/pending?flowStage=PENDING_ASSESSMENT' },
  { label: '人力资源中心', path: '/hr/overview' }
]).filter((item) => canAccess(item.path)))

function readInitialPortalScope(): 'PERSONAL' | 'ORG' {
  if (typeof window === 'undefined') {
    return 'PERSONAL'
  }
  return window.localStorage.getItem(PORTAL_SCOPE_STORAGE_KEY) === 'ORG' ? 'ORG' : 'PERSONAL'
}

function renderNotice(item: OaNotice) {
  return h('div', { class: 'list-item' }, [
    h('div', { class: 'title' }, item.title),
    h('div', { class: 'meta' }, `${item.publisherName || ''} ${item.publishTime || ''}`)
  ])
}

function renderTodo(item: OaTodo) {
  return h('div', { class: 'list-item' }, [
    h('div', { class: 'title' }, item.title),
    h('div', { class: 'meta' }, item.dueTime || '')
  ])
}

function canAccess(path: string) {
  return resolveRouteAccess(router, userStore.roles || [], path, userStore.pagePermissions || []).canAccess
}

function openPath(path: string) {
  if (!canAccess(path)) {
    message.warning('当前账号暂无权限访问该页面')
    return
  }
  router.push(path)
}

async function load() {
  loading.value = true
  errorText.value = ''
  try {
    const res = await getPortalSummary({ params: { scope: portalScope.value } })
    Object.assign(summary, res || {})
    summary.notices = res.notices || []
    summary.todos = res.todos || []
  } catch (error: any) {
    errorText.value = error?.message || '加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

watch(canSwitchScope, (value) => {
  if (!value && portalScope.value !== 'PERSONAL') {
    portalScope.value = 'PERSONAL'
  }
}, { immediate: true })

watch(portalScope, (value) => {
  if (typeof window !== 'undefined') {
    window.localStorage.setItem(PORTAL_SCOPE_STORAGE_KEY, value)
  }
  load().catch(() => {})
})

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
.portal-toolbar {
  margin-bottom: 14px;
}

.portal-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(280px, 0.65fr);
  gap: 16px;
  padding: 24px;
  border: 1px solid rgba(195, 217, 231, 0.82);
  border-radius: 22px;
  background:
    linear-gradient(135deg, rgba(248, 251, 255, 0.98) 0%, rgba(236, 244, 251, 0.96) 100%);
}

.hero-main {
  display: grid;
  gap: 10px;
}

.hero-label,
.panel-kicker {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #7290a8;
}

.hero-value {
  font-size: clamp(42px, 6vw, 68px);
  line-height: 0.95;
  font-weight: 800;
  color: #143654;
}

.hero-title {
  font-size: 20px;
  font-weight: 700;
  color: #173854;
}

.hero-description {
  margin: 0;
  max-width: 640px;
  font-size: 14px;
  line-height: 1.7;
  color: #5f7b92;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 6px;
}

.hero-side {
  display: grid;
  gap: 12px;
}

.hero-alert {
  padding: 14px 16px;
  border-radius: 18px;
  border: 1px solid rgba(195, 217, 231, 0.9);
  background: rgba(255, 255, 255, 0.86);
}

.hero-alert.is-danger {
  border-color: rgba(222, 91, 99, 0.22);
  background: rgba(255, 244, 244, 0.92);
}

.hero-alert.is-warning {
  border-color: rgba(240, 165, 74, 0.24);
  background: rgba(255, 248, 238, 0.92);
}

.hero-alert.is-focus {
  border-color: rgba(27, 107, 207, 0.2);
  background: rgba(242, 247, 255, 0.94);
}

.hero-alert-value {
  font-size: 28px;
  font-weight: 800;
  color: #173854;
}

.hero-alert-label {
  margin-top: 4px;
  font-size: 12px;
  color: #6a8398;
}

.snapshot-rail {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 10px;
  margin-top: 14px;
}

.snapshot-item {
  border: 1px solid rgba(195, 217, 231, 0.82);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.78);
  padding: 14px;
  text-align: left;
  cursor: pointer;
  transition: transform 0.18s ease, border-color 0.18s ease, background 0.18s ease;
}

.snapshot-item:hover {
  transform: translateY(-1px);
  border-color: rgba(27, 107, 207, 0.22);
  background: rgba(255, 255, 255, 0.94);
}

.snapshot-label {
  display: block;
  font-size: 12px;
  color: #7590a5;
}

.snapshot-value {
  display: block;
  margin-top: 10px;
  font-size: 24px;
  color: #173854;
}

.portal-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(320px, 0.95fr);
  gap: 16px;
  margin-top: 16px;
}

.panel {
  border: 1px solid rgba(195, 217, 231, 0.82);
  border-radius: 20px;
  padding: 18px;
  background: rgba(255, 255, 255, 0.82);
}

.panel-priority,
.panel-feed {
  min-height: 100%;
}

.panel-feed {
  grid-column: 1 / -1;
}

.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.panel-head h3 {
  margin: 4px 0 0;
  font-size: 18px;
  color: #173854;
}

.priority-list,
.workflow-list,
.action-list {
  display: grid;
  gap: 10px;
}

.priority-list {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.priority-item,
.workflow-item,
.action-item {
  width: 100%;
  text-align: left;
  border: 1px solid rgba(208, 223, 234, 0.9);
  background: rgba(248, 251, 254, 0.9);
  border-radius: 16px;
  padding: 14px;
  cursor: pointer;
  transition: border-color 0.18s ease, transform 0.18s ease, background 0.18s ease;
}

.priority-item:hover,
.workflow-item:hover,
.action-item:hover {
  transform: translateY(-1px);
  border-color: rgba(27, 107, 207, 0.24);
  background: rgba(255, 255, 255, 0.96);
}

.priority-label {
  display: block;
  font-size: 12px;
  color: #6f8ba2;
}

.priority-value {
  display: block;
  margin-top: 8px;
  font-size: 26px;
  color: #173854;
}

.workflow-item,
.action-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.feed-columns {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.feed-column-title {
  margin-bottom: 10px;
  font-size: 13px;
  font-weight: 700;
  color: #5b7891;
}

.list-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 0;
}

.title {
  font-weight: 600;
  color: #173854;
}

.meta {
  color: #71879b;
  font-size: 12px;
}

@media (max-width: 1200px) {
  .snapshot-rail {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 992px) {
  .portal-hero,
  .portal-grid,
  .feed-columns {
    grid-template-columns: 1fr;
  }

  .priority-list,
  .snapshot-rail {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .portal-hero {
    padding: 18px;
  }
}

@media (max-width: 640px) {
  .priority-list,
  .snapshot-rail {
    grid-template-columns: 1fr;
  }
}
</style>
