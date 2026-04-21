<template>
  <PageContainer title="人力资源中心" subTitle="兼容入口会自动回到新的人力资源业务中心结构。">
    <a-card :bordered="false" class="card-elevated" style="margin-bottom: 12px" title="卡片1：今日人力概况">
      <a-row :gutter="16">
        <a-col :xs="12" :md="4">
          <div class="stat-clickable" @click="go('/hr/profile/basic?status=1')">
            <a-statistic title="在职人数" :value="summary.onJobCount" />
          </div>
        </a-col>
        <a-col :xs="12" :md="4">
          <div class="stat-clickable" @click="go('/hr/profile/basic?status=0')">
            <a-statistic title="离职人数" :value="summary.leftCount" />
          </div>
        </a-col>
        <a-col :xs="12" :md="4">
          <div class="stat-clickable" @click="go('/hr/development/records?date=today')">
            <a-statistic title="今日培训" :value="summary.todayTrainingCount" />
          </div>
        </a-col>
        <a-col :xs="12" :md="4">
          <div class="stat-clickable" @click="go('/hr/attendance/leave-approval?type=LEAVE&status=PENDING')">
            <a-statistic title="请假审批待办" :value="summary.pendingLeaveApprovalCount" />
          </div>
        </a-col>
        <a-col :xs="12" :md="4">
          <div class="stat-clickable" @click="go('/hr/attendance/abnormal?date=today')">
            <a-statistic title="考勤异常" :value="summary.attendanceAbnormalCount" />
          </div>
        </a-col>
        <a-col :xs="12" :md="4">
          <div class="stat-clickable" @click="go('/hr/profile/contract-reminders?range=30d')">
            <a-statistic title="合同到期预警" :value="summary.contractExpiringCount" />
          </div>
        </a-col>
        <a-col :xs="12" :md="4">
          <div class="stat-clickable" @click="openBirthdayModal('TODAY')">
            <a-statistic title="今日生日" :value="summary.birthdayTodayCount" />
          </div>
        </a-col>
        <a-col :xs="12" :md="4">
          <div class="stat-clickable" @click="openBirthdayModal('NEXT7')">
            <a-statistic title="7天内生日" :value="summary.birthdayUpcomingCount" />
          </div>
        </a-col>
        <a-col :xs="12" :md="4">
          <div class="stat-clickable" @click="go('/workbench/todo?keyword=生日提醒')">
            <a-statistic title="生日待办" :value="summary.birthdayTodoCount" :value-style="{ color: (summary.birthdayTodoCount || 0) > 0 ? '#cf1322' : undefined }" />
          </div>
        </a-col>
      </a-row>
    </a-card>

    <a-card :bordered="false" class="card-elevated" style="margin-bottom: 12px" title="卡片2：快速查找与常用入口">
      <a-space wrap style="margin-bottom: 12px">
        <a-input-search v-model:value="actionKeyword" placeholder="搜索入口，如 合同 / 生日 / 报销 / 培训" allow-clear style="width: 320px" />
        <a-tag color="blue">匹配 {{ matchedActionCount }} 项</a-tag>
        <a-tag color="gold">已收藏 {{ favoriteActions.length }} 项</a-tag>
      </a-space>
      <div v-if="favoriteActions.length" class="favorite-actions">
        <div v-for="action in favoriteActions" :key="`favorite-${action.path}`" class="favorite-action-item">
          <a-button type="primary" @click="go(action.path)">{{ action.label }}</a-button>
          <a-tooltip title="移除常用">
            <a-button type="text" size="small" class="favorite-toggle" @click.stop="toggleFavorite(action.path)">
              <StarFilled />
            </a-button>
          </a-tooltip>
        </div>
      </div>
      <a-empty v-else description="先把高频入口收藏到这里，后续会一直保留在当前浏览器中" />
    </a-card>

    <a-row :gutter="12">
      <a-col v-for="section in visibleSections" :key="section.title" :xs="24" :xl="8" style="margin-bottom: 12px">
        <a-card :bordered="false" class="card-elevated" :title="section.title">
          <a-space wrap>
            <template v-for="action in section.actions" :key="action.path">
              <div class="action-entry">
                <a-badge v-if="action.path === '/workbench/todo?keyword=生日提醒'" :count="summary.birthdayTodoCount || 0" :offset="[2, -2]">
                  <a-button :type="action.primary ? 'primary' : 'default'" @click="go(action.path)">{{ action.label }}</a-button>
                </a-badge>
                <a-button v-else :type="action.primary ? 'primary' : 'default'" @click="go(action.path)">{{ action.label }}</a-button>
                <a-tooltip :title="isFavorite(action.path) ? '移出常用入口' : '加入常用入口'">
                  <a-button type="text" size="small" class="favorite-toggle" @click.stop="toggleFavorite(action.path)">
                    <StarFilled v-if="isFavorite(action.path)" />
                    <StarOutlined v-else />
                  </a-button>
                </a-tooltip>
              </div>
            </template>
          </a-space>
        </a-card>
      </a-col>
    </a-row>

    <a-modal
      v-model:open="birthdayModalOpen"
      :title="birthdayModalTitle"
      width="880px"
      :footer="null"
      destroy-on-close
    >
      <a-table
        :columns="birthdayColumns"
        :data-source="birthdayRows"
        :loading="birthdayLoading"
        row-key="staffId"
        :pagination="false"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="Number(record.status) === 1 ? 'green' : 'default'">{{ Number(record.status) === 1 ? '在职' : '离职' }}</a-tag>
          </template>
        </template>
      </a-table>
      <div style="margin-top: 12px; display: flex; justify-content: flex-end">
        <a-button @click="printBirthdayRows">打印名单</a-button>
      </div>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { StarFilled, StarOutlined } from '@ant-design/icons-vue'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { getHrStaffBirthdayPage, getHrWorkbenchSummary } from '../../api/hr'
import { openPrintTableReport } from '../../utils/print'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'
import { resolveRouteAccess } from '../../utils/routeAccess'
import { useUserStore } from '../../stores/user'
import type { HrStaffBirthdayItem } from '../../types'

type WorkbenchAction = { label: string; path: string; primary?: boolean }
type WorkbenchSection = { title: string; actions: readonly WorkbenchAction[] }

const router = useRouter()
const userStore = useUserStore()
const FAVORITE_STORAGE_KEY = 'hr-workbench-favorites'
const summary = reactive({
  onJobCount: 0,
  leftCount: 0,
  todayTrainingCount: 0,
  pendingLeaveApprovalCount: 0,
  attendanceAbnormalCount: 0,
  contractExpiringCount: 0,
  birthdayTodayCount: 0,
  birthdayUpcomingCount: 0,
  birthdayTodoCount: 0
})

const birthdayModalOpen = ref(false)
const birthdayScope = ref<'TODAY' | 'NEXT7'>('TODAY')
const birthdayRows = ref<HrStaffBirthdayItem[]>([])
const birthdayLoading = ref(false)
const actionKeyword = ref('')
const favoritePaths = ref<string[]>(readFavoritePaths())
const birthdayColumns = [
  { title: '工号', dataIndex: 'staffNo', key: 'staffNo', width: 110 },
  { title: '姓名', dataIndex: 'realName', key: 'realName', width: 120 },
  { title: '生日', dataIndex: 'birthday', key: 'birthday', width: 120 },
  { title: '下次生日', dataIndex: 'nextBirthday', key: 'nextBirthday', width: 120 },
  { title: '剩余天数', dataIndex: 'daysUntil', key: 'daysUntil', width: 100 },
  { title: '手机号', dataIndex: 'phone', key: 'phone', width: 150 },
  { title: '岗位', dataIndex: 'jobTitle', key: 'jobTitle', width: 140 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90 }
]
const birthdayModalTitle = computed(() => (birthdayScope.value === 'TODAY' ? '今日生日名单' : '近7天生日名单'))

const sectionConfigs: readonly WorkbenchSection[] = [
  {
    title: '招聘管理',
    actions: [
      { label: '招聘需求申请', path: '/hr/recruitment/needs', primary: true },
      { label: '岗位发布', path: '/hr/recruitment/needs?scene=job-posting' },
      { label: '候选人库', path: '/hr/recruitment/needs?scene=candidates' },
      { label: '面试管理', path: '/hr/recruitment/needs?scene=interviews' },
      { label: '入职办理', path: '/hr/recruitment/needs?scene=onboarding' },
      { label: '入职资料收集', path: '/hr/recruitment/needs?scene=materials' }
    ]
  },
  {
    title: '员工档案中心',
    actions: [
      { label: '入职向导', path: '/hr/profile/onboarding', primary: true },
      { label: '员工基本信息', path: '/hr/profile/basic' },
      { label: '账号与领导设置', path: '/hr/profile/account-access' },
      { label: '劳动合同管理', path: '/hr/profile/contracts' },
      { label: '合同模板库', path: '/hr/profile/contract-templates' },
      { label: '合同打印', path: '/hr/profile/contract-print' },
      { label: '证书上传', path: '/hr/profile/certificates' },
      { label: '培训记录', path: '/hr/development/records?scene=records' },
      { label: '档案附件', path: '/hr/profile/attachments' },
      { label: '合同到期提醒', path: '/hr/profile/contract-reminders' }
    ]
  },
  {
    title: '考勤与班组',
    actions: [
      { label: '排班方案', path: '/hr/attendance/schemes', primary: true },
      { label: '班组设置', path: '/hr/attendance/groups' },
      { label: '排班日历', path: '/hr/attendance/calendar' },
      { label: '请假审批', path: '/hr/attendance/leave-approval' },
      { label: '调班申请', path: '/hr/attendance/leave-approval?scene=shift-change' },
      { label: '加班申请', path: '/hr/attendance/leave-approval?scene=overtime' },
      { label: '考勤记录', path: '/hr/attendance/records' },
      { label: '考勤异常', path: '/hr/attendance/abnormal' },
      { label: '门禁对接', path: '/hr/attendance/access-control' },
      { label: '一卡通数据对接', path: '/hr/attendance/card-sync' }
    ]
  },
  {
    title: '行政中心（兼容）',
    actions: [
      { label: '通知管理', path: '/oa/notice', primary: true },
      { label: '任务管理', path: '/oa/work-execution/task' },
      { label: '我的待办', path: '/workbench/todo' },
      { label: '活动中心', path: '/oa/activity-center/plan' },
      { label: '文档中心', path: '/oa/document' },
      { label: '知识库', path: '/oa/knowledge' },
      { label: '规章制度库', path: '/hr/compliance/policies' },
      { label: '制度更新预警', path: '/hr/compliance/policy-alerts' },
      { label: '生日提醒待办', path: '/workbench/todo?keyword=生日提醒' },
      { label: '分组设置', path: '/oa/group-settings' }
    ]
  },
  {
    title: '业务协同入口',
    actions: [
      { label: '营销合同闭环', path: '/marketing/contracts/pending?flowStage=PENDING_ASSESSMENT', primary: true },
      { label: '入住评估', path: '/elder/assessment/ability/admission' },
      { label: '入住办理', path: '/elder/admission-processing' },
      { label: '在院总览', path: '/elder/in-hospital-overview' },
      { label: '床位房态图', path: '/logistics/assets/room-state-map' },
      { label: '工作台', path: '/workbench' }
    ]
  },
  {
    title: '薪酬与费用',
    actions: [
      { label: '外出培训费用报销', path: '/hr/expense/records?scene=training-reimburse', primary: true },
      { label: '补贴申请', path: '/hr/expense/records?scene=subsidy' },
      { label: '员工宿舍', path: '/hr/expense/dormitory' },
      { label: '宿舍房态图', path: '/hr/expense/dormitory-map' },
      { label: '员工餐费', path: '/hr/expense/meal-fee' },
      { label: '员工电费', path: '/hr/expense/electricity-fee' },
      { label: '工资补贴记录', path: '/hr/expense/records?scene=salary-subsidy' },
      { label: '报销审批流', path: '/hr/expense/approval-flow' }
    ]
  },
  {
    title: '培训发展 / 绩效管理',
    actions: [
      { label: '培训计划', path: '/hr/development/records?scene=plans', primary: true },
      { label: '培训报名', path: '/hr/development/records?scene=enrollments' },
      { label: '培训签到', path: '/hr/development/records?scene=signin' },
      { label: '培训记录', path: '/hr/development/records?scene=records' },
      { label: '证书管理', path: '/hr/development/certificates' },
      { label: '证书到期提醒', path: '/hr/development/certificate-reminders' },
      { label: '护理绩效', path: '/hr/performance/reports?scene=nursing' },
      { label: '销售绩效', path: '/hr/performance/reports?scene=sales' },
      { label: '行政绩效', path: '/hr/performance/reports?scene=admin' },
      { label: '评分规则配置', path: '/hr/performance/scoring-rules' },
      { label: '绩效生成', path: '/hr/performance/reports?scene=generation' },
      { label: '绩效报表', path: '/hr/performance/reports?scene=reports' },
      { label: '奖惩记录', path: '/hr/performance/reward-punishment' }
    ]
  }
] as const

const normalizedActionKeyword = computed(() => actionKeyword.value.trim().toLowerCase())
const allAccessibleActions = computed(() =>
  sectionConfigs.flatMap((section) =>
    section.actions
      .filter((action) => canAccess(action.path))
      .map((action) => ({ ...action, sectionTitle: section.title }))
  )
)
const favoriteActions = computed(() =>
  favoritePaths.value
    .map((path) => allAccessibleActions.value.find((action) => action.path === path))
    .filter((action): action is WorkbenchAction & { sectionTitle: string } => Boolean(action))
)
const visibleSections = computed(() =>
  sectionConfigs
    .map((section) => ({
      ...section,
      actions: section.actions.filter((action) => canAccess(action.path) && matchesAction(action, section.title))
    }))
    .filter((section) => section.actions.length > 0)
)
const matchedActionCount = computed(() => visibleSections.value.reduce((total, section) => total + section.actions.length, 0))

function readFavoritePaths(): string[] {
  if (typeof window === 'undefined') {
    return []
  }
  try {
    const parsed = JSON.parse(window.localStorage.getItem(FAVORITE_STORAGE_KEY) || '[]')
    return Array.isArray(parsed) ? parsed.map((item) => String(item || '')).filter(Boolean) : []
  } catch {
    return []
  }
}

function canAccess(path: string) {
  return resolveRouteAccess(router, userStore.roles || [], path, userStore.pagePermissions || []).canAccess
}

function matchesAction(action: WorkbenchAction, sectionTitle: string) {
  const keyword = normalizedActionKeyword.value
  if (!keyword) {
    return true
  }
  return [action.label, sectionTitle, action.path].some((item) => String(item || '').toLowerCase().includes(keyword))
}

function isFavorite(path: string) {
  return favoritePaths.value.includes(path)
}

function toggleFavorite(path: string) {
  if (isFavorite(path)) {
    favoritePaths.value = favoritePaths.value.filter((item) => item !== path)
    return
  }
  favoritePaths.value = [path, ...favoritePaths.value].slice(0, 12)
}

function go(path: string) {
  if (!canAccess(path)) {
    message.warning('当前账号暂无权限访问该页面')
    return
  }
  router.push(path)
}

watch(favoritePaths, (value) => {
  if (typeof window !== 'undefined') {
    window.localStorage.setItem(FAVORITE_STORAGE_KEY, JSON.stringify(value))
  }
})

async function loadSummary() {
  try {
    const res = await getHrWorkbenchSummary({ warningDays: 30 })
    summary.onJobCount = res.onJobCount || 0
    summary.leftCount = res.leftCount || 0
    summary.todayTrainingCount = res.todayTrainingCount || 0
    summary.pendingLeaveApprovalCount = res.pendingLeaveApprovalCount || 0
    summary.attendanceAbnormalCount = res.attendanceAbnormalCount || 0
    summary.contractExpiringCount = res.contractExpiringCount || 0
    summary.birthdayTodayCount = res.birthdayTodayCount || 0
    summary.birthdayUpcomingCount = res.birthdayUpcomingCount || 0
    summary.birthdayTodoCount = res.birthdayTodoCount || 0
  } catch (_error) {
    summary.onJobCount = 0
    summary.leftCount = 0
    summary.todayTrainingCount = 0
    summary.pendingLeaveApprovalCount = 0
    summary.attendanceAbnormalCount = 0
    summary.contractExpiringCount = 0
    summary.birthdayTodayCount = 0
    summary.birthdayUpcomingCount = 0
    summary.birthdayTodoCount = 0
  }
}

async function openBirthdayModal(scope: 'TODAY' | 'NEXT7') {
  birthdayScope.value = scope
  birthdayModalOpen.value = true
  birthdayLoading.value = true
  try {
    const page = await getHrStaffBirthdayPage({
      pageNo: 1,
      pageSize: 200,
      scope,
      onJobOnly: true
    })
    birthdayRows.value = page.list || []
  } catch {
    birthdayRows.value = []
  } finally {
    birthdayLoading.value = false
  }
}

function printBirthdayRows() {
  if (!birthdayRows.value.length) {
    return
  }
  openPrintTableReport({
    title: birthdayModalTitle.value,
    subtitle: `打印时间：${dayjs().format('YYYY-MM-DD HH:mm:ss')}`,
    columns: [
      { key: 'staffNo', title: '工号' },
      { key: 'realName', title: '姓名' },
      { key: 'birthday', title: '生日' },
      { key: 'nextBirthday', title: '下次生日' },
      { key: 'daysUntil', title: '剩余天数' },
      { key: 'phone', title: '手机号' },
      { key: 'jobTitle', title: '岗位' }
    ],
    rows: birthdayRows.value as any
  })
}

onMounted(loadSummary)

useLiveSyncRefresh({
  topics: ['hr', 'oa', 'system'],
  refresh: () => {
    loadSummary().catch(() => {})
  },
  debounceMs: 900
})
</script>

<style scoped>
.favorite-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.favorite-action-item,
.action-entry {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.favorite-toggle {
  color: #d48806;
}

.stat-clickable {
  border-radius: 8px;
  padding: 8px;
  cursor: pointer;
}

.stat-clickable:hover {
  background: rgba(24, 144, 255, 0.06);
}
</style>
