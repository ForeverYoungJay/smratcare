<template>
  <div class="portal-page">
    <div class="portal-hero card-elevated">
      <div>
        <div class="hero-title">智慧养老 OA 工作台</div>
        <div class="hero-subtitle">提醒中心、协同任务、行政审批、经营看板统一入口</div>
      </div>
      <div class="hero-meta">
        <div class="meta-item">
          <div class="meta-label">当前用户</div>
          <div class="meta-value">{{ userStore.staffInfo?.realName || '管理员' }}</div>
        </div>
        <div class="meta-item">
          <div class="meta-label">刷新时间</div>
          <div class="meta-value">{{ refreshedAt || '--' }}</div>
        </div>
      </div>
    </div>

    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :sm="12" :lg="6" v-for="item in topStats" :key="item.title">
        <a-card :bordered="false" class="card-elevated stat-card" hoverable @click="go(item.route)">
          <a-statistic :title="item.title" :value="item.value" />
          <div class="stat-desc">{{ item.desc }}</div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :lg="14">
        <a-card title="提醒中心" :bordered="false" class="card-elevated">
          <a-list :data-source="reminderItems" :locale="{ emptyText: '暂无提醒' }">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta :title="item.title" :description="item.desc" />
                <template #actions>
                  <a-button type="link" @click="go(item.route)">处理</a-button>
                </template>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>
      <a-col :xs="24" :lg="10">
        <a-card title="异常提醒" :bordered="false" class="card-elevated">
          <a-space direction="vertical" style="width: 100%">
            <a-alert v-for="item in abnormalItems" :key="item.title" :message="item.title" :description="item.desc" type="warning" show-icon />
          </a-space>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :lg="12">
        <a-card title="我的协同（任务/项目/甘特图/工作小结）" :bordered="false" class="card-elevated">
          <div class="collab-meta">
            <a-tag color="processing">进行中任务 {{ summary.ongoingTaskCount || 0 }}</a-tag>
            <a-tag color="success">近30天总结 {{ summary.submittedReportCount || 0 }}</a-tag>
            <a-button size="small" type="link" @click="go('/oa/work-execution/task')">查看任务</a-button>
          </div>
          <div v-if="(summary.collaborationGantt || []).length" class="gantt-wrap">
            <div v-for="item in summary.collaborationGantt" :key="item.taskId" class="gantt-row">
              <div class="gantt-title">{{ item.title }}</div>
              <div class="gantt-bar">
                <div class="gantt-progress" :style="{ width: `${item.progress || 0}%` }"></div>
              </div>
              <div class="gantt-percent">{{ item.progress || 0 }}%</div>
            </div>
          </div>
          <a-empty v-else description="暂无甘特任务" />
        </a-card>
      </a-col>
      <a-col :xs="24" :lg="12">
        <a-card title="待办与流程入口（工作流引擎）" :bordered="false" class="card-elevated">
          <a-list :data-source="summary.workflowTodos || []" :locale="{ emptyText: '暂无流程待办' }">
            <template #renderItem="{ item }">
              <a-list-item>
                <div>{{ item.name }}</div>
                <a-space>
                  <a-badge :count="item.count || 0" />
                  <a-button type="link" @click="go(item.route)">进入</a-button>
                </a-space>
              </a-list-item>
            </template>
          </a-list>
          <div class="quick-launch">
            <div class="sub-title">快捷发起</div>
            <a-space wrap>
              <a-button v-for="item in quickLaunches" :key="item.label" size="small" @click="go(item.route)">{{ item.label }}</a-button>
            </a-space>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :lg="8">
        <a-card title="行政日程" :bordered="false" class="card-elevated module-card">
          <div class="module-main">{{ summary.todayScheduleCount || 0 }}</div>
          <div class="module-sub">今日排班任务</div>
          <a-button type="link" @click="go('/care/scheduling/shift-calendar')">进入行政日程</a-button>
        </a-card>
      </a-col>
      <a-col :xs="24" :lg="8">
        <a-card title="考勤与请假" :bordered="false" class="card-elevated module-card">
          <div class="module-main">{{ summary.attendanceAbnormalCount || 0 }}</div>
          <div class="module-sub">今日考勤异常</div>
          <a-space>
            <a-button size="small" @click="go('/hr/staff')">考勤管理</a-button>
            <a-button size="small" @click="go('/oa/approval')">请假审批</a-button>
          </a-space>
        </a-card>
      </a-col>
      <a-col :xs="24" :lg="8">
        <a-card title="费用管理（我的/部门/发票夹）" :bordered="false" class="card-elevated module-card">
          <div class="module-main">{{ summary.myExpenseCount || 0 }}</div>
          <div class="module-sub">账户数量 / 30天流水 {{ summary.deptExpenseCount || 0 }} / 发票夹 {{ summary.invoiceFolderCount || 0 }}</div>
          <a-button type="link" @click="go('/finance/account')">进入费用管理</a-button>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :lg="12">
        <a-card title="营销绩效（客户渠道+业绩完成度甘特图）" :bordered="false" class="card-elevated">
          <a-list :data-source="summary.marketingChannels || []" :locale="{ emptyText: '暂无渠道数据' }">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta :title="item.source" :description="`线索 ${item.leadCount || 0} / 签约 ${item.contractCount || 0}`" />
              </a-list-item>
            </template>
          </a-list>
          <a-space>
            <a-button size="small" @click="go('/marketing/reports/channel')">来源渠道</a-button>
            <a-button size="small" @click="go('/marketing/reports/followup')">业绩完成度</a-button>
          </a-space>
        </a-card>
      </a-col>
      <a-col :xs="24" :lg="12">
        <a-card title="文档中心（制度/项目/合同扫描件）" :bordered="false" class="card-elevated">
          <div class="module-main">{{ summary.documentCount || 0 }}</div>
          <div class="module-sub">发票夹 {{ summary.invoiceFolderCount || 0 }}，可集中管理制度、项目和合同扫描件</div>
          <a-button type="link" @click="go('/oa/document')">进入文档中心</a-button>
        </a-card>
        <a-card title="数据看板（院长/部门报表入口）" :bordered="false" class="card-elevated board-card">
          <a-space wrap>
            <a-button size="small" @click="go('/stats/org/monthly-operation')">院长总览</a-button>
            <a-button size="small" @click="go('/stats/consumption')">部门消费</a-button>
            <a-button size="small" @click="go('/finance/report')">财务报表</a-button>
            <a-button size="small" @click="go('/stats/check-in')">入住报表</a-button>
          </a-space>
        </a-card>
      </a-col>
    </a-row>

  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { useUserStore } from '../stores/user'
import { getPortalSummary } from '../api/oa'
import { useLiveSyncRefresh } from '../composables/useLiveSyncRefresh'
import type { OaPortalSummary } from '../types'

const router = useRouter()
const userStore = useUserStore()
const refreshedAt = ref('')

const summary = reactive<OaPortalSummary>({
  notices: [],
  todos: [],
  workflowTodos: [],
  marketingChannels: [],
  collaborationGantt: [],
  latestSuggestions: []
})

const quickLaunches = [
  { label: '签到/签退', route: '/hr/staff' },
  { label: '请假', route: '/oa/approval' },
  { label: '加班', route: '/oa/approval' },
  { label: '报销', route: '/oa/approval' },
  { label: '采购', route: '/material/purchase' },
  { label: '用章', route: '/oa/approval' },
  { label: '收入证明', route: '/oa/approval' },
  { label: '物资申领', route: '/material/transfer' }
]

const topStats = computed(() => [
  {
    title: '我的待办',
    value: summary.openTodoCount || 0,
    desc: `超期 ${summary.overdueTodoCount || 0} 条`,
    route: '/oa/todo'
  },
  {
    title: '流程待审批',
    value: summary.pendingApprovalCount || 0,
    desc: `超时 ${summary.approvalTimeoutCount || 0} 条`,
    route: '/oa/approval'
  },
  {
    title: '库存预警',
    value: summary.inventoryLowStockCount || 0,
    desc: `采购草稿 ${summary.materialPurchaseDraftCount || 0} 条`,
    route: '/inventory/alerts'
  },
  {
    title: '长者状态异常',
    value: summary.elderAbnormalCount || 0,
    desc: `在住长者 ${summary.inHospitalElderCount || 0} 人`,
    route: '/health/inspection'
  },
  {
    title: '事故未闭环',
    value: summary.incidentOpenCount || 0,
    desc: `调拨草稿 ${summary.materialTransferDraftCount || 0} 条`,
    route: '/life/incident'
  },
  {
    title: '文档归档',
    value: summary.documentCount || 0,
    desc: `发票夹 ${summary.invoiceFolderCount || 0} 个`,
    route: '/oa/document'
  }
])

const reminderItems = computed(() => {
  const noticeItems = (summary.notices || []).map((notice) => ({
    title: `公告：${notice.title}`,
    desc: `${notice.publisherName || '系统'} · ${formatTime(notice.publishTime)}`,
    route: '/oa/notice'
  }))
  const todoItems = (summary.todos || []).map((todo) => ({
    title: `待办：${todo.title}`,
    desc: `截止时间 ${formatTime(todo.dueTime)}`,
    route: '/oa/todo'
  }))
  return [...todoItems, ...noticeItems].slice(0, 10)
})

const abnormalItems = computed(() => [
  {
    title: '考勤异常',
    desc: `今日异常 ${summary.attendanceAbnormalCount || 0} 条`
  },
  {
    title: '审批超时',
    desc: `超 48 小时未处理 ${summary.approvalTimeoutCount || 0} 条`
  },
  {
    title: '库存不足',
    desc: `低于安全库存 ${summary.inventoryLowStockCount || 0} 项`
  },
  {
    title: '长者状态异常',
    desc: `健康巡检异常 ${summary.healthAbnormalCount || 0} 条`
  }
])

function go(path: string) {
  router.push(path)
}

function formatTime(value?: string) {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm') : '--'
}

async function loadSummary() {
  const data = await getPortalSummary()
  Object.assign(summary, data)
  refreshedAt.value = dayjs().format('YYYY-MM-DD HH:mm:ss')
}

useLiveSyncRefresh({
  topics: ['elder', 'bed', 'lifecycle', 'finance', 'care', 'health', 'dining', 'marketing', 'oa'],
  refresh: () => {
    loadSummary()
  },
  debounceMs: 800
})

async function init() {
  try {
    await loadSummary()
  } catch (error: any) {
    message.error(error?.message || '加载首页失败')
  }
}

onMounted(init)
</script>

<style scoped>
.portal-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.portal-hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
}

.hero-title {
  font-size: 20px;
  font-weight: 700;
}

.hero-subtitle {
  margin-top: 6px;
  color: var(--muted);
  font-size: 13px;
}

.hero-meta {
  display: flex;
  gap: 24px;
}

.meta-item {
  text-align: right;
}

.meta-label {
  font-size: 12px;
  color: var(--muted);
}

.meta-value {
  font-size: 14px;
  font-weight: 600;
}

.stat-card {
  cursor: pointer;
  min-height: 132px;
}

.stat-desc {
  margin-top: 8px;
  color: var(--muted);
  font-size: 12px;
}

.collab-meta {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.gantt-wrap {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.gantt-row {
  display: grid;
  grid-template-columns: 120px 1fr 48px;
  gap: 8px;
  align-items: center;
}

.gantt-title {
  font-size: 12px;
  color: #334155;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.gantt-bar {
  height: 10px;
  border-radius: 6px;
  background: #eef2ff;
}

.gantt-progress {
  height: 10px;
  border-radius: 6px;
  background: linear-gradient(90deg, #2b5db8, #22c55e);
}

.gantt-percent {
  text-align: right;
  font-size: 12px;
  color: #64748b;
}

.quick-launch {
  margin-top: 12px;
}

.sub-title {
  margin-bottom: 8px;
  font-weight: 600;
}

.module-card {
  min-height: 204px;
}

.module-main {
  font-size: 30px;
  font-weight: 700;
  color: #1d4ed8;
}

.module-sub {
  margin: 8px 0 12px;
  color: #64748b;
  font-size: 12px;
}

.board-card {
  margin-top: 16px;
}

@media (max-width: 768px) {
  .portal-hero {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .hero-meta {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
