<template>
  <PageContainer title="首页" subTitle="机构经营与安全总览" mode="showcase" kicker="机构总览">
    <template #meta>
      <a-space wrap>
        <StatusTag text="经营总览" tone="pending" />
        <StatusTag text="安全关注" tone="warning" />
        <StatusTag :text="`最近刷新 ${refreshedAt}`" tone="offline" />
      </a-space>
    </template>

    <template #extra>
      <a-space wrap>
        <a-button @click="router.push('/workbench/overview')">进入工作台</a-button>
        <a-button type="primary" :loading="loading" @click="loadOverview">刷新首页</a-button>
      </a-space>
    </template>

    <section class="portal-grid portal-grid--metrics">
      <OverviewMetricCard
        v-for="item in topMetrics"
        :key="item.label"
        clickable
        :helper="item.helper"
        :label="item.label"
        :status-text="item.statusText"
        :status-tone="item.statusTone"
        :tone="item.tone"
        :value="item.value"
        @click="router.push(item.path)"
      />
    </section>

    <section class="portal-grid portal-grid--main">
      <SectionPanel
        title="运营概览"
        description="把入住、收入、咨询转化和服务完成率放在同一视图里，方便先看经营，再判断需要调度的业务。"
      >
        <div class="portal-grid portal-grid--operating">
          <OverviewMetricCard
            clickable
            label="床位入住率"
            :value="percentText(dashboard?.bedOccupancyRate)"
            helper="在住 / 总床位"
            tone="brand"
            @click="router.push('/elder/bed-panorama')"
          />
          <OverviewMetricCard
            clickable
            label="新增咨询 / 新增长者"
            :value="consultationGrowthLabel"
            helper="营销线索与入住净增"
            tone="success"
            @click="router.push('/marketing/workbench')"
          />
          <OverviewMetricCard
            clickable
            label="服务完成率"
            :value="percentText(serviceCompletionRate)"
            helper="护理任务完成度"
            tone="success"
            @click="router.push('/medical-care/care-task-board')"
          />
          <SectionPanel dense title="本月收入趋势" description="最近 6 个月收费收入走势">
            <v-chart class="trend-chart" :option="revenueTrendOption" autoresize />
          </SectionPanel>
        </div>
      </SectionPanel>

      <SectionPanel
        title="安全与健康预警"
        description="高风险长者、异常生命体征和行为预警需要优先闭环，所有缺明细的数据都保持可见占位。"
      >
        <div class="portal-grid portal-grid--risk-cards">
          <EntitySummaryCard
            v-for="item in alertFocusCards"
            :key="item.title"
            :avatar-text="item.avatar"
            :meta="item.meta"
            :subtitle="item.subtitle"
            :title="item.title"
            @click="router.push(item.path)"
          >
            <template #tag>
              <StatusTag :text="item.tagText" :tone="item.tagTone" />
            </template>
          </EntitySummaryCard>
        </div>
        <RiskList :items="riskItems" @select="router.push($event.path)" />
      </SectionPanel>
    </section>

    <section class="portal-grid portal-grid--secondary">
      <SectionPanel
        title="今日工作"
        description="以今日护理任务、审批、后勤工单和客户回访作为四个工作优先面板。"
      >
        <div class="portal-grid portal-grid--work">
          <OverviewMetricCard
            v-for="item in todayWorkItems"
            :key="item.label"
            clickable
            :helper="item.helper"
            :label="item.label"
            :tone="item.tone"
            :value="item.value"
            @click="router.push(item.path)"
          />
        </div>
      </SectionPanel>

      <SectionPanel
        title="快捷入口"
        description="面向首页的高频动作，不额外创造新权限，只导向现有模块与页面。"
      >
        <div class="portal-grid portal-grid--quick-actions">
          <QuickActionTile
            v-for="item in quickActions"
            :key="item.title"
            :description="item.description"
            :icon="item.icon"
            :title="item.title"
            @click="router.push(item.path)"
          />
        </div>
      </SectionPanel>
    </section>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import VChart from 'vue-echarts'
import PageContainer from '../components/PageContainer.vue'
import EntitySummaryCard from '../components/smartcare/EntitySummaryCard.vue'
import OverviewMetricCard from '../components/smartcare/OverviewMetricCard.vue'
import QuickActionTile from '../components/smartcare/QuickActionTile.vue'
import RiskList from '../components/smartcare/RiskList.vue'
import SectionPanel from '../components/smartcare/SectionPanel.vue'
import StatusTag from '../components/smartcare/StatusTag.vue'
import { getHomeOverviewBundle, type HomeOverviewBundle } from '../api/home'

type RiskItemWithPath = {
  actionLabel?: string
  description: string
  key: string
  levelText: string
  path: string
  title: string
  tone: 'normal' | 'pending' | 'warning' | 'danger' | 'offline'
  value: number | string
}

const router = useRouter()
const loading = ref(false)
const bundle = ref<HomeOverviewBundle | null>(null)
const refreshedAt = ref('--')

const dashboard = computed(() => bundle.value?.dashboard || null)
const portal = computed(() => bundle.value?.portal || null)
const hr = computed(() => bundle.value?.hr || null)
const logistics = computed(() => bundle.value?.logistics || null)
const revenue = computed(() => bundle.value?.revenue || null)

function numberValue(value?: number | null, fallback = 0) {
  return Number.isFinite(Number(value)) ? Number(value) : fallback
}

function displayNumber(value?: number | null) {
  if (value === null || value === undefined || Number.isNaN(Number(value))) return '--'
  return Number(value).toLocaleString('zh-CN')
}

function displayCurrency(value?: number | null) {
  if (value === null || value === undefined || Number.isNaN(Number(value))) return '--'
  return `¥${Number(value).toLocaleString('zh-CN')}`
}

function percentText(value?: number | null) {
  if (value === null || value === undefined || Number.isNaN(Number(value))) return '--'
  return `${Math.round(Number(value) * 100) / 100}%`
}

const todayAlertCount = computed(() => {
  return numberValue(portal.value?.healthAbnormalCount) +
    numberValue(portal.value?.elderAbnormalCount) +
    numberValue(logistics.value?.maintenanceOverdueCount)
})

const serviceCompletionRate = computed(() => {
  const total = numberValue(dashboard.value?.careTasksToday)
  if (!total) return 0
  const abnormal = numberValue(dashboard.value?.abnormalTasksToday)
  return Math.max(0, ((total - abnormal) / total) * 100)
})

const consultationGrowthLabel = computed(() => {
  const leads = numberValue(portal.value?.suggestionCount)
  const growth = numberValue(dashboard.value?.checkInNetIncrease)
  return `${leads} / ${growth}`
})

const topMetrics = computed(() => ([
  {
    label: '在住长者数',
    value: displayNumber(dashboard.value?.inHospitalCount),
    helper: '当前在住规模',
    tone: 'brand' as const,
    path: '/elder/in-hospital-overview',
    statusText: '核心',
    statusTone: 'pending' as const
  },
  {
    label: '空置床位数',
    value: displayNumber(dashboard.value?.availableBeds),
    helper: '空置床位可用于转化',
    tone: 'success' as const,
    path: '/elder/bed-panorama',
    statusText: '可调度',
    statusTone: 'normal' as const
  },
  {
    label: '今日预警数',
    value: displayNumber(todayAlertCount.value),
    helper: '生命体征 + 长者异常 + 工单逾期',
    tone: todayAlertCount.value > 0 ? 'danger' as const : 'success' as const,
    path: '/portal',
    statusText: todayAlertCount.value > 0 ? '需关注' : '平稳',
    statusTone: todayAlertCount.value > 0 ? 'danger' as const : 'normal' as const
  },
  {
    label: '待处理工单',
    value: displayNumber(logistics.value?.maintenancePendingCount),
    helper: '后勤维修与保障处理',
    tone: 'warning' as const,
    path: '/logistics/task-center',
    statusText: '后勤',
    statusTone: 'warning' as const
  },
  {
    label: '今日护理任务',
    value: displayNumber(dashboard.value?.careTasksToday),
    helper: '护理执行与巡诊跟踪',
    tone: 'brand' as const,
    path: '/medical-care/care-task-board',
    statusText: '任务',
    statusTone: 'pending' as const
  },
  {
    label: '本月收入',
    value: displayCurrency(revenue.value?.totalRevenue ?? dashboard.value?.totalRevenue),
    helper: '收费、账单与回款',
    tone: 'success' as const,
    path: '/finance/workbench',
    statusText: '经营',
    statusTone: 'normal' as const
  },
  {
    label: '设备在线率',
    value: '--',
    helper: '待设备遥测接入',
    tone: 'default' as const,
    path: '/logistics/equipment',
    statusText: '待接入',
    statusTone: 'offline' as const
  },
  {
    label: '员工在岗数',
    value: displayNumber(hr.value?.onJobCount),
    helper: '排班与人力在岗',
    tone: 'brand' as const,
    path: '/hr/workbench',
    statusText: '人力',
    statusTone: 'pending' as const
  }
]))

const alertFocusCards = computed(() => ([
  {
    avatar: '高',
    title: '高风险长者',
    subtitle: '重点关注跌倒、离床、异常波动等高风险对象。',
    meta: [`当前关注 ${displayNumber(portal.value?.elderAbnormalCount)}`, '名单待具体接口接入'],
    tagText: numberValue(portal.value?.elderAbnormalCount) > 0 ? '高优先级' : '暂平稳',
    tagTone: numberValue(portal.value?.elderAbnormalCount) > 0 ? 'danger' as const : 'normal' as const,
    path: '/elder/resident-360'
  },
  {
    avatar: '体',
    title: '今日异常生命体征',
    subtitle: '聚焦今日体温、血压、血氧等异常记录。',
    meta: [`异常 ${displayNumber(portal.value?.healthAbnormalCount)}`, '需护理与医生复核'],
    tagText: numberValue(portal.value?.healthAbnormalCount) > 0 ? '待复核' : '正常',
    tagTone: numberValue(portal.value?.healthAbnormalCount) > 0 ? 'warning' as const : 'normal' as const,
    path: '/medical-care/handovers'
  },
  {
    avatar: '护',
    title: '跌倒 / 离床 / 呼叫预警',
    subtitle: '行为类预警统一收束到处置视线，不再散落在不同模块。',
    meta: [`预警 ${displayNumber(todayAlertCount.value)}`, '缺少名单时保持占位显示'],
    tagText: todayAlertCount.value > 0 ? '待处置' : '已清空',
    tagTone: todayAlertCount.value > 0 ? 'danger' as const : 'normal' as const,
    path: '/medical-care/unified-task-center'
  }
]))

const riskItems = computed<RiskItemWithPath[]>(() => ([
  {
    key: 'risk-elder',
    title: '高风险长者',
    description: '需要重点巡视与连续观察的长者',
    levelText: '危险',
    tone: 'danger',
    value: displayNumber(portal.value?.elderAbnormalCount),
    path: '/elder/resident-360',
    actionLabel: '进入档案'
  },
  {
    key: 'risk-health',
    title: '异常生命体征',
    description: '今日采集到的异常生命体征记录',
    levelText: '警告',
    tone: 'warning',
    value: displayNumber(portal.value?.healthAbnormalCount),
    path: '/medical-care/handovers',
    actionLabel: '查看交班'
  },
  {
    key: 'risk-maintenance',
    title: '待处理预警列表',
    description: '后勤维保逾期与设备异常清单',
    levelText: '待处理',
    tone: 'pending',
    value: displayNumber(logistics.value?.maintenanceOverdueCount),
    path: '/logistics/task-center',
    actionLabel: '进入工单'
  }
]))

const todayWorkItems = computed(() => ([
  {
    label: '今日护理任务',
    value: displayNumber(dashboard.value?.careTasksToday),
    helper: '护理记录、计划与执行',
    tone: 'brand' as const,
    path: '/medical-care/care-task-board'
  },
  {
    label: '待审批事项',
    value: displayNumber(portal.value?.pendingApprovalCount),
    helper: '行政、财务、人事审批',
    tone: 'warning' as const,
    path: '/workbench/approvals'
  },
  {
    label: '待处理维修 / 后勤工单',
    value: displayNumber(logistics.value?.maintenancePendingCount),
    helper: '维修、巡检与后勤保障',
    tone: 'warning' as const,
    path: '/logistics/task-center'
  },
  {
    label: '待回访客户',
    value: displayNumber(portal.value?.suggestionCount),
    helper: '咨询线索与营销回访',
    tone: 'success' as const,
    path: '/marketing/workbench'
  }
]))

const quickActions = [
  { title: '新增长者', description: '进入入住办理，补充档案与合同。', icon: '长', path: '/elder/admission-processing' },
  { title: '新增护理记录', description: '进入医护任务板补充护理执行。', icon: '护', path: '/medical-care/care-task-board' },
  { title: '处理预警', description: '打开医护统一任务中心处理异常。', icon: '警', path: '/medical-care/unified-task-center' },
  { title: '创建工单', description: '进入后勤任务中心发起维修或巡检。', icon: '工', path: '/logistics/task-center' },
  { title: '收费登记', description: '进入财务运营中心处理收费与账单。', icon: '费', path: '/finance/workbench' },
  { title: '数据报表', description: '查看入住、收入与服务质量报表。', icon: '报', path: '/stats/org/monthly-operation' }
]

const revenueTrendOption = computed(() => {
  const revenueList = revenue.value?.monthlyRevenue || []
  return {
    color: ['#1d8cb4'],
    grid: { left: 10, right: 10, top: 20, bottom: 0, containLabel: true },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: revenueList.map((item: any) => item.month || item.label || '--'),
      axisLine: { lineStyle: { color: '#c8d8e5' } },
      axisLabel: { color: '#6d879c' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: 'rgba(211, 224, 232, 0.7)' } },
      axisLabel: {
        color: '#6d879c',
        formatter: (value: number) => `${Math.round(value / 1000)}k`
      }
    },
    series: [
      {
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 7,
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(29, 140, 180, 0.28)' },
              { offset: 1, color: 'rgba(29, 140, 180, 0.02)' }
            ]
          }
        },
        lineStyle: { width: 3, color: '#1d8cb4' },
        data: revenueList.map((item: any) => Number(item.amount || item.value || 0))
      }
    ]
  }
})

async function loadOverview() {
  loading.value = true
  try {
    bundle.value = await getHomeOverviewBundle()
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

onMounted(() => {
  loadOverview()
})
</script>

<style scoped>
.portal-grid {
  display: grid;
  gap: 18px;
}

.portal-grid--metrics {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.portal-grid--main,
.portal-grid--secondary {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.portal-grid--operating {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.portal-grid--risk-cards,
.portal-grid--work,
.portal-grid--quick-actions {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.trend-chart {
  height: 240px;
}

@media (max-width: 1280px) {
  .portal-grid--metrics,
  .portal-grid--main,
  .portal-grid--secondary,
  .portal-grid--operating,
  .portal-grid--risk-cards,
  .portal-grid--work,
  .portal-grid--quick-actions {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .portal-grid--metrics,
  .portal-grid--main,
  .portal-grid--secondary,
  .portal-grid--operating,
  .portal-grid--risk-cards,
  .portal-grid--work,
  .portal-grid--quick-actions {
    grid-template-columns: 1fr;
  }
}
</style>
