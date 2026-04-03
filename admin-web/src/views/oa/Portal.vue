<template>
  <PageContainer title="我的待办中心" subTitle="公告、待办和审批提醒统一在这里查看">
    <StatefulBlock :loading="loading" :error="errorText" :empty="false" @retry="load">
      <div class="portal-toolbar">
        <a-space wrap>
          <a-radio-group v-if="canSwitchScope" v-model:value="portalScope" button-style="solid" size="small">
            <a-radio-button value="PERSONAL">我的视角</a-radio-button>
            <a-radio-button value="ORG">机构视角</a-radio-button>
          </a-radio-group>
          <a-tag :color="portalScope === 'ORG' ? 'purple' : 'blue'">{{ scopeDescription }}</a-tag>
          <a-button size="small" type="primary" @click="openPath('/oa/todo')">进入待办</a-button>
          <a-button size="small" @click="openPath('/oa/approval')">进入审批</a-button>
          <a-button size="small" @click="openPath('/oa/work-report')">写工作总结</a-button>
        </a-space>
      </div>
      <a-alert
        v-if="(summary.overdueTodoCount || 0) > 0 || (summary.approvalTimeoutCount || 0) > 0 || (summary.healthAbnormalCount || 0) > 0 || (summary.birthdayTodoCount || 0) > 0"
        type="warning"
        show-icon
        style="margin-bottom: 12px"
        :message="`协同提醒：超期待办 ${summary.overdueTodoCount || 0} 条，审批超时 ${summary.approvalTimeoutCount || 0} 条，健康异常 ${summary.healthAbnormalCount || 0} 条，生日待办 ${summary.birthdayTodoCount || 0} 条。`"
      />
      <a-row :gutter="16" class="overview-row">
        <a-col :xs="12" :md="4">
          <a-card :bordered="false" class="card-elevated stat-card">
            <a-statistic title="待办总数" :value="summary.openTodoCount || 0" />
          </a-card>
        </a-col>
        <a-col :xs="12" :md="4">
          <a-card :bordered="false" class="card-elevated stat-card clickable" @click="openPath('/oa/todo?keyword=生日提醒')">
            <a-statistic title="生日待办" :value="summary.birthdayTodoCount || 0" />
          </a-card>
        </a-col>
        <a-col :xs="12" :md="4">
          <a-card :bordered="false" class="card-elevated stat-card">
            <a-statistic title="超期待办" :value="summary.overdueTodoCount || 0" :value-style="{ color: '#cf1322' }" />
          </a-card>
        </a-col>
        <a-col :xs="12" :md="4">
          <a-card :bordered="false" class="card-elevated stat-card">
            <a-statistic :title="pendingApprovalTitle" :value="summary.pendingApprovalCount || 0" />
          </a-card>
        </a-col>
        <a-col :xs="12" :md="4">
          <a-card :bordered="false" class="card-elevated stat-card">
            <a-statistic title="进行中任务" :value="summary.ongoingTaskCount || 0" />
          </a-card>
        </a-col>
        <a-col :xs="12" :md="4">
          <a-card :bordered="false" class="card-elevated stat-card">
            <a-statistic title="今日日程" :value="summary.todayScheduleCount || 0" />
          </a-card>
        </a-col>
      </a-row>
      <a-row :gutter="16" class="overview-row">
        <a-col :span="6">
          <a-card :bordered="false" class="card-elevated stat-card clickable" @click="openPath('/survey/template')">
            <a-statistic title="问卷草稿" :value="summary.surveyDraftCount || 0" />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card :bordered="false" class="card-elevated stat-card clickable" @click="openPath('/survey/template?status=1')">
            <a-statistic title="已发布问卷" :value="summary.surveyPublishedCount || 0" />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card :bordered="false" class="card-elevated stat-card clickable" @click="openPath('/survey/stats')">
            <a-statistic title="今日问卷回收" :value="summary.surveyTodaySubmissionCount || 0" />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card :bordered="false" class="card-elevated stat-card clickable" @click="openPath('/survey/template?targetType=FAMILY&status=1')">
            <a-statistic title="家属端问卷" :value="summary.surveyFamilyPublishedCount || 0" />
          </a-card>
        </a-col>
      </a-row>
      <a-row :gutter="16" class="overview-row">
        <a-col :span="14">
          <a-card title="审批快捷入口" :bordered="false" class="card-elevated">
            <a-space v-if="workflowQuickActions.length" wrap>
              <a-badge v-for="item in workflowQuickActions" :key="item.code" :count="item.count" :offset="[4, -2]">
                <a-button @click="openPath(item.route)">{{ item.name }}</a-button>
              </a-badge>
            </a-space>
            <a-empty v-else description="当前没有需要优先处理的流程" />
          </a-card>
        </a-col>
        <a-col :span="10">
          <a-card title="高频功能" :bordered="false" class="card-elevated">
            <a-space wrap>
              <a-button v-for="action in commonActions" :key="action.path" @click="openPath(action.path)">
                {{ action.label }}
              </a-button>
            </a-space>
          </a-card>
        </a-col>
      </a-row>
      <a-row :gutter="16">
        <a-col :span="14">
          <a-card title="最新公告" :bordered="false" class="card-elevated">
            <a-list :data-source="summary.notices" :render-item="renderNotice" :locale="{ emptyText: '暂无公告' }" />
          </a-card>
        </a-col>
        <a-col :span="10">
          <a-card :title="todoCardTitle" :bordered="false" class="card-elevated">
            <a-list :data-source="summary.todos" :render-item="renderTodo" :locale="{ emptyText: '暂无待办' }" />
          </a-card>
        </a-col>
      </a-row>
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
const commonActions = computed(() => ([
  { label: '审批流程', path: '/oa/approval' },
  { label: '待办事项', path: '/oa/todo' },
  { label: '任务管理', path: '/oa/work-execution/task' },
  { label: '文档管理', path: '/oa/document' },
  { label: '通知管理', path: '/oa/notice' },
  { label: '工作总结', path: '/oa/work-report' },
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
  margin-bottom: 12px;
}
.overview-row {
  margin-bottom: 16px;
}
.stat-card {
  min-height: 120px;
}
.clickable {
  cursor: pointer;
}
.list-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 8px 0;
}
.title {
  font-weight: 600;
}
.meta {
  color: #888;
  font-size: 12px;
}
</style>
