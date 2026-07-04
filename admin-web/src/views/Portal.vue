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

    <section class="portal-entry-strip">
      <button class="entry-card entry-card--employee" type="button" @click="router.push('/workbench/overview')">
        <div>
          <span>员工入口</span>
          <strong>先进入个人工作台</strong>
          <small>打卡、待办、审批、日程和岗位任务都从个人视角开始处理。</small>
        </div>
        <StatusTag text="个人优先" tone="normal" />
      </button>
      <button class="entry-card entry-card--manager" type="button" @click="router.push('/stats/org/monthly-operation')">
        <div>
          <span>管理入口</span>
          <strong>再看机构经营与风险</strong>
          <small>院长、主管和管理员适合从经营首页或月报继续下钻。</small>
        </div>
        <StatusTag text="经营总览" tone="pending" />
      </button>
    </section>

    <section class="portal-grid portal-grid--metrics">
      <OverviewMetricCard
        v-for="item in primaryMetrics"
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

    <section class="portal-grid portal-grid--command">
      <SectionPanel
        title="院长运营指挥台"
        description="把经营结果、服务风险、家属体验和现金流放在一个可追责的月度闭环里，先看是否偏离，再点进对应业务处理。"
      >
        <template #extra>
          <a-space wrap>
            <StatusTag :text="commandCenterStatus.text" :tone="commandCenterStatus.tone" />
            <a-button size="small" @click="router.push('/stats/org/monthly-operation')">经营月报</a-button>
          </a-space>
        </template>

        <div class="command-scoreboard">
          <button
            v-for="item in commandScoreCards"
            :key="item.key"
            type="button"
            class="command-score-card"
            @click="router.push(item.path)"
          >
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
            <small>{{ item.helper }}</small>
            <i :class="`command-score-card__bar command-score-card__bar--${item.tone}`" />
          </button>
        </div>

        <div class="command-split">
          <div class="command-column">
            <div class="command-column__head">
              <strong>本月经营闭环</strong>
              <span>入住、回款、人效与客户转化</span>
            </div>
            <button
              v-for="item in operatingLoopItems"
              :key="item.title"
              type="button"
              class="command-list-item"
              @click="router.push(item.path)"
            >
              <div>
                <strong>{{ item.title }}</strong>
                <span>{{ item.desc }}</span>
              </div>
              <StatusTag :text="item.tag" :tone="item.tone" />
            </button>
          </div>

          <div class="command-column">
            <div class="command-column__head">
              <strong>今日风险闭环</strong>
              <span>护理、健康、后勤与审批 SLA</span>
            </div>
            <button
              v-for="item in riskClosureItems"
              :key="item.title"
              type="button"
              class="command-list-item"
              @click="router.push(item.path)"
            >
              <div>
                <strong>{{ item.title }}</strong>
                <span>{{ item.desc }}</span>
              </div>
              <StatusTag :text="item.tag" :tone="item.tone" />
            </button>
          </div>
        </div>
      </SectionPanel>

      <SectionPanel
        title="业务落地清单"
        description="按养老机构真实运营顺序串起七个闭环：咨询入住、照护执行、健康用药、安全事件、家属服务、收费合同、后勤人力。"
      >
        <template #extra>
          <a-space wrap>
            <StatusTag :text="capabilitySummaryText" :tone="capabilitySummaryTone" />
            <a-button size="small" @click="router.push('/stats/operations?tab=quality')">服务月报</a-button>
            <a-button size="small" @click="router.push('/workbench/todo')">转为待办</a-button>
          </a-space>
        </template>

        <div class="landing-flow">
          <button
            v-for="item in businessLandingFlow"
            :key="item.title"
            type="button"
            class="landing-flow-step"
            @click="router.push(item.path)"
          >
            <b>{{ item.index }}</b>
            <div>
              <strong>{{ item.title }}</strong>
              <span>{{ item.desc }}</span>
            </div>
            <StatusTag :text="item.tag" :tone="item.tone" />
          </button>
        </div>

        <div class="capability-lane">
          <div class="capability-lane__head">
            <strong>智能化落地路线</strong>
            <span>先接入数据和规则，再进入 AI 摘要、IoT 告警、智能排班和家属问答。</span>
          </div>
          <a-button size="small" class="capability-lane__entry" @click="router.push('/stats/operations?tab=intelligence')">查看智能化落地</a-button>
          <button
            v-for="item in intelligenceRoadmap"
            :key="item.title"
            type="button"
            class="capability-chip"
            @click="router.push(item.path)"
          >
            <strong>{{ item.title }}</strong>
            <span>{{ item.desc }}</span>
            <StatusTag :text="item.tag" :tone="item.tone" />
          </button>
        </div>
      </SectionPanel>
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
            @click="router.push('/stats/operations?tab=marketing')"
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

    <section class="portal-grid portal-grid--more">
      <SectionPanel
        title="更多机构概览"
        description="护理执行、人力在岗与设备接入等运行指标，作为二级概览随时下钻。"
      >
        <div class="portal-grid portal-grid--more-metrics">
          <OverviewMetricCard
            v-for="item in secondaryMetrics"
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
        </div>
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

    <section class="portal-grid portal-grid--support">
      <SectionPanel
        title="老人生日计划"
        description="把今日关怀、7 天内提醒和本月名单前置到首页，方便活动准备、物资安排和家属关怀一起闭环。"
      >
        <template #extra>
          <a-space wrap>
            <a-button size="small" @click="router.push('/oa/life/birthday')">查看全部</a-button>
            <a-button size="small" @click="router.push('/workbench/schedule')">同步到我的日程</a-button>
          </a-space>
        </template>

        <div class="portal-grid portal-grid--birthday-metrics">
          <OverviewMetricCard
            clickable
            label="今日生日"
            :value="displayNumber(birthdayStats.today)"
            helper="优先安排祝福、探访与活动"
            :tone="Number(birthdayStats.today) > 0 ? 'brand' : 'default'"
            @click="router.push('/oa/life/birthday?scope=today')"
          />
          <OverviewMetricCard
            clickable
            label="未来 7 天"
            :value="displayNumber(birthdayStats.next7Days)"
            helper="提前准备活动与物资"
            tone="brand"
            @click="router.push('/oa/life/birthday?scope=next7')"
          />
          <OverviewMetricCard
            clickable
            label="本月生日"
            :value="displayNumber(birthdayStats.thisMonth)"
            helper="适合统筹月度集体庆生"
            tone="success"
            @click="router.push('/oa/life/birthday?scope=month')"
          />
          <OverviewMetricCard
            clickable
            label="80+ / 90+"
            :value="birthdayAgeBandLabel"
            helper="高龄长者关怀优先级"
            tone="default"
            @click="router.push('/oa/life/birthday')"
          />
        </div>

        <div class="birthday-highlight-row">
          <div class="birthday-highlight-copy">
            <strong>{{ birthdayHeadline.title }}</strong>
            <p>{{ birthdayHeadline.description }}</p>
          </div>
          <a-space wrap>
            <StatusTag :text="`今日 ${birthdayStats.today}`" :tone="Number(birthdayStats.today) > 0 ? 'pending' : 'normal'" />
            <StatusTag :text="`7天内 ${birthdayStats.next7Days}`" tone="pending" />
            <StatusTag :text="`本月 ${birthdayStats.thisMonth}`" tone="normal" />
          </a-space>
        </div>

        <div class="birthday-action-row">
          <button type="button" class="birthday-action-card" @click="openBirthdayModal('today')">
            <strong>今日生日弹窗名单</strong>
            <span>现场确认今天需要祝福、探访和家属联络的长者名单。</span>
          </button>
          <button type="button" class="birthday-action-card" @click="launchMonthlyBirthdayActivity">
            <strong>本月集体庆生快捷发起</strong>
            <span>自动带入当前月份和预计人数，直接进入活动创建。</span>
          </button>
          <button type="button" class="birthday-action-card" @click="openBirthdayMaterialPrep">
            <strong>生日物资准备快捷入口</strong>
            <span>直接进入出库/备料场景，减少活动当天临时找物资。</span>
          </button>
        </div>

        <div class="birthday-list">
          <button
            v-for="item in birthdayPreviewList"
            :key="`${item.elderId}_${item.nextBirthday}`"
            type="button"
            class="birthday-item"
            @click="router.push(`/oa/life/birthday?elderName=${encodeURIComponent(item.elderName || '')}`)"
          >
            <div>
              <strong>{{ item.elderName }}</strong>
              <span>{{ birthdayMetaText(item) }}</span>
            </div>
            <StatusTag :text="birthdayDueText(item.daysUntil)" :tone="birthdayDueTone(item.daysUntil)" />
          </button>
          <a-empty v-if="!birthdayPreviewList.length" description="近期暂无生日提醒" />
        </div>

        <div class="birthday-footer-actions">
          <a-space wrap>
            <a-button size="small" @click="router.push('/oa/life/birthday?minAge=80')">查看 80+ 生日名单</a-button>
            <a-button size="small" @click="router.push('/oa/life/birthday?minAge=90')">查看 90+ 生日名单</a-button>
            <a-button size="small" @click="router.push('/workbench/todo?keyword=生日提醒')">生日待办</a-button>
            <a-button size="small" @click="printBirthdayPreview">打印近期名单</a-button>
          </a-space>
        </div>
      </SectionPanel>

      <SectionPanel
        title="协同与待办速览"
        description="首页保留最值得院长和主管第一眼看到的协同信号：待办、审批、今日日程、生日关怀和超时事项。"
      >
        <template #extra>
          <a-space wrap>
            <a-button size="small" @click="router.push('/workbench/todo')">待办中心</a-button>
            <a-button size="small" @click="router.push('/workbench/schedule')">我的日程</a-button>
          </a-space>
        </template>

        <div class="portal-grid portal-grid--collab-metrics">
          <OverviewMetricCard
            v-for="item in collaborationMetrics"
            :key="item.label"
            clickable
            :label="item.label"
            :value="item.value"
            :helper="item.helper"
            :tone="item.tone"
            @click="router.push(item.path)"
          />
        </div>

        <div class="collab-list">
          <button
            v-for="item in collaborationFocusList"
            :key="item.title"
            type="button"
            class="collab-list-item"
            @click="router.push(item.path)"
          >
            <div>
              <strong>{{ item.title }}</strong>
              <span>{{ item.desc }}</span>
            </div>
            <StatusTag :text="item.tag" :tone="item.tone" />
          </button>
          <a-empty v-if="!collaborationFocusList.length" description="暂无需要特别关注的协同事项" />
        </div>
      </SectionPanel>
    </section>

    <a-modal
      v-model:open="birthdayModalOpen"
      :title="birthdayModalTitle"
      width="860px"
      :footer="null"
      destroy-on-close
    >
      <a-table
        :columns="birthdayColumns"
        :data-source="birthdayModalRows"
        row-key="elderId"
        size="small"
        :pagination="false"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'actions'">
            <a-space size="small">
              <a @click="openBirthdayProfile(record)">长者档案</a>
              <a @click="openBirthdayMaterialForRecord(record)">物资准备</a>
              <a @click="openBirthdayActivityForRecord(record)">创建活动</a>
            </a-space>
          </template>
        </template>
      </a-table>
      <div class="birthday-modal-actions">
        <a-space wrap>
          <a-button @click="printBirthdayModalRows">打印名单</a-button>
          <a-button @click="router.push('/oa/life/birthday?scope=today')">进入完整生日页面</a-button>
        </a-space>
      </div>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import VChart from 'vue-echarts'
import PageContainer from '../components/PageContainer.vue'
import { getBirthdayAll } from '../api/life'
import EntitySummaryCard from '../components/smartcare/EntitySummaryCard.vue'
import OverviewMetricCard from '../components/smartcare/OverviewMetricCard.vue'
import QuickActionTile from '../components/smartcare/QuickActionTile.vue'
import RiskList from '../components/smartcare/RiskList.vue'
import SectionPanel from '../components/smartcare/SectionPanel.vue'
import StatusTag from '../components/smartcare/StatusTag.vue'
import { getHomeOverviewBundle, type HomeOverviewBundle } from '../api/home'
import type { BirthdayReminder } from '../types'
import { openPrintTableReport } from '../utils/print'

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
const birthdayRows = ref<BirthdayReminder[]>([])
const birthdayModalOpen = ref(false)
const birthdayModalScope = ref<'today' | 'next7'>('today')
const refreshedAt = ref('--')

const dashboard = computed(() => bundle.value?.dashboard || null)
const portal = computed(() => bundle.value?.portal || null)
const hr = computed(() => bundle.value?.hr || null)
const logistics = computed(() => bundle.value?.logistics || null)
const operations = computed(() => bundle.value?.operations || null)
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

function normalizeBirthdayDate(value?: string | null) {
  if (!value) return null
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? null : date
}

function birthdayMonthMatches(item?: BirthdayReminder | null, month = dayjs().month() + 1) {
  if (!item?.birthDate) return false
  const parsed = dayjs(item.birthDate)
  return parsed.isValid() && parsed.month() + 1 === month
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

const occupancyHealth = computed(() => {
  const rate = numberValue(dashboard.value?.bedOccupancyRate)
  if (rate >= 92) return { text: '高入住', tone: 'normal' as const }
  if (rate >= 80) return { text: '稳定', tone: 'pending' as const }
  return { text: '需转化', tone: 'warning' as const }
})

const commandCenterStatus = computed(() => {
  if (todayAlertCount.value > 0 || numberValue(portal.value?.overdueTodoCount) > 0) {
    return { text: '今日需闭环', tone: 'warning' as const }
  }
  return { text: '运行平稳', tone: 'normal' as const }
})

function capabilityTone(status?: string) {
  if (status === 'READY') return 'normal' as const
  if (status === 'PARTIAL') return 'pending' as const
  if (status === 'PLANNED') return 'offline' as const
  return 'offline' as const
}

const capabilitySummaryText = computed(() => {
  const summary = operations.value?.summary?.[0]
  if (!summary) return '能力清单待同步'
  return `已落地 ${summary.ready}/${summary.total}`
})

const capabilitySummaryTone = computed(() => {
  const summary = operations.value?.summary?.[0]
  if (!summary) return 'offline' as const
  if (summary.partial > 0 || summary.planned > 0) return 'pending' as const
  return 'normal' as const
})

const commandScoreCards = computed(() => ([
  {
    key: 'occupancy',
    label: '入住率',
    value: percentText(dashboard.value?.bedOccupancyRate),
    helper: `空床 ${displayNumber(dashboard.value?.availableBeds)} 张`,
    tone: numberValue(dashboard.value?.bedOccupancyRate) >= 85 ? 'success' as const : 'warning' as const,
    path: '/elder/bed-panorama'
  },
  {
    key: 'service',
    label: '服务完成',
    value: percentText(serviceCompletionRate.value),
    helper: `异常任务 ${displayNumber(dashboard.value?.abnormalTasksToday)}`,
    tone: serviceCompletionRate.value >= 95 ? 'success' as const : 'danger' as const,
    path: '/medical-care/care-task-board'
  },
  {
    key: 'cash',
    label: '本月回款',
    value: displayCurrency(revenue.value?.totalRevenue ?? dashboard.value?.totalRevenue),
    helper: '账单、押金、护理费统一跟踪',
    tone: 'success' as const,
    path: '/finance/workbench'
  },
  {
    key: 'risk',
    label: '风险事件',
    value: displayNumber(todayAlertCount.value),
    helper: '健康异常、长者异常、逾期工单',
    tone: todayAlertCount.value > 0 ? 'danger' as const : 'success' as const,
    path: '/stats/operations?tab=safety'
  }
]))

const operatingLoopItems = computed(() => ([
  {
    title: '入住转化',
    desc: `空置床位 ${displayNumber(dashboard.value?.availableBeds)} 张，咨询/净增 ${consultationGrowthLabel.value}`,
    tag: occupancyHealth.value.text,
    tone: occupancyHealth.value.tone,
    path: '/stats/operations?tab=marketing'
  },
  {
    title: '合同收费',
    desc: `本月收入 ${displayCurrency(revenue.value?.totalRevenue ?? dashboard.value?.totalRevenue)}，可继续核对欠费、退住结算与票据。`,
    tag: '月结',
    tone: 'normal' as const,
    path: '/finance/workbench'
  },
  {
    title: '人力排班',
    desc: `在岗 ${displayNumber(hr.value?.onJobCount)} 人，联动护理任务、考勤异常与班次覆盖。`,
    tag: '排班',
    tone: 'pending' as const,
    path: '/stats/operations?tab=workforce'
  },
  {
    title: '家属体验',
    desc: `待回访/建议 ${displayNumber(portal.value?.suggestionCount)} 项，关联探访、缴费、投诉建议和生日关怀。`,
    tag: numberValue(portal.value?.suggestionCount) > 0 ? '待回访' : '平稳',
    tone: numberValue(portal.value?.suggestionCount) > 0 ? 'warning' as const : 'offline' as const,
    path: '/oa/family-service'
  }
]))

const riskClosureItems = computed(() => ([
  {
    title: '照护执行 SLA',
    desc: `今日护理任务 ${displayNumber(dashboard.value?.careTasksToday)} 项，异常 ${displayNumber(dashboard.value?.abnormalTasksToday)} 项。`,
    tag: serviceCompletionRate.value >= 95 ? '达标' : '追踪',
    tone: serviceCompletionRate.value >= 95 ? 'normal' as const : 'warning' as const,
    path: '/medical-care/care-task-board'
  },
  {
    title: '健康复核',
    desc: `异常生命体征 ${displayNumber(portal.value?.healthAbnormalCount)} 条，需要医护复核与交班记录。`,
    tag: numberValue(portal.value?.healthAbnormalCount) > 0 ? '待复核' : '已清空',
    tone: numberValue(portal.value?.healthAbnormalCount) > 0 ? 'warning' as const : 'offline' as const,
    path: '/medical-care/handovers'
  },
  {
    title: '安全事件',
    desc: `高风险长者 ${displayNumber(portal.value?.elderAbnormalCount)} 位，联动巡房、跌倒、离床和应急处理。`,
    tag: numberValue(portal.value?.elderAbnormalCount) > 0 ? '高优先' : '平稳',
    tone: numberValue(portal.value?.elderAbnormalCount) > 0 ? 'danger' as const : 'offline' as const,
    path: '/stats/operations?tab=safety'
  },
  {
    title: '后勤保障',
    desc: `待处理工单 ${displayNumber(logistics.value?.maintenancePendingCount)}，逾期 ${displayNumber(logistics.value?.maintenanceOverdueCount)}。`,
    tag: numberValue(logistics.value?.maintenanceOverdueCount) > 0 ? '逾期' : '处理中',
    tone: numberValue(logistics.value?.maintenanceOverdueCount) > 0 ? 'danger' as const : 'pending' as const,
    path: '/stats/operations?tab=logistics'
  }
]))

const fallbackBusinessLandingFlow = computed(() => ([
  { index: '01', title: '咨询到入住', desc: '线索、参观、评估、合同、床位一条线推进。', tag: `${displayNumber(portal.value?.suggestionCount)} 条`, tone: numberValue(portal.value?.suggestionCount) > 0 ? 'pending' as const : 'offline' as const, path: '/stats/operations?tab=marketing' },
  { index: '02', title: '能力评估', desc: 'ADL、认知、自理和持续评估驱动护理等级。', tag: '评估', tone: 'normal' as const, path: '/elder/assessment' },
  { index: '03', title: '护理计划执行', desc: '计划、任务、打卡、异常、质检和交接班闭环。', tag: `${displayNumber(dashboard.value?.careTasksToday)} 项`, tone: 'pending' as const, path: '/medical-care/care-task-board' },
  { index: '04', title: '健康用药', desc: '生命体征、医嘱、发药、漏服提醒和慢病趋势。', tag: `${displayNumber(portal.value?.healthAbnormalCount)} 异常`, tone: numberValue(portal.value?.healthAbnormalCount) > 0 ? 'warning' as const : 'offline' as const, path: '/health/management/data' },
  { index: '05', title: '安全事件', desc: '跌倒、离床、走失、压疮、感染和应急预案。', tag: `${displayNumber(todayAlertCount.value)} 件`, tone: todayAlertCount.value > 0 ? 'danger' as const : 'offline' as const, path: '/stats/operations?tab=safety' },
  { index: '06', title: '家属连接', desc: '探访、缴费、动态、投诉建议和生日关怀。', tag: `${displayNumber(birthdayStats.value.next7Days)} 生日`, tone: birthdayStats.value.next7Days > 0 ? 'pending' as const : 'offline' as const, path: '/oa/life/birthday' },
  { index: '07', title: '经营复盘', desc: '床位、收入、服务质量、人效和风险月报。', tag: '复盘', tone: 'normal' as const, path: '/stats/org/monthly-operation' }
]))

const businessLandingFlow = computed(() => {
  const domains = operations.value?.domains || []
  if (!domains.length) return fallbackBusinessLandingFlow.value
  return domains.slice(0, 7).map((item, index) => ({
    index: String(index + 1).padStart(2, '0'),
    title: item.title,
    desc: item.description,
    tag: item.statusText || item.status,
    tone: capabilityTone(item.status),
    path: item.routePath || '/portal'
  }))
})

const intelligenceRoadmap = computed(() => {
  const items = operations.value?.intelligence || []
  if (!items.length) {
    return [
      { title: '智能风险预警', desc: '连续异常趋势、夜间呼叫和活动下降进入统一任务中心。', tag: '增强中', tone: 'pending' as const, path: '/medical-care/alert-rules' },
      { title: 'AI护理摘要', desc: '基于护理记录、质检和异常闭环生成月度照护报告。', tag: '已接入', tone: 'normal' as const, path: '/care/service/nursing-reports' },
      { title: 'IoT设备接入', desc: '床垫、手环、门禁、呼叫器接入实时告警。', tag: '增强中', tone: 'pending' as const, path: '/medical-care/smart-alerts' }
    ]
  }
  return items.slice(0, 4).map((item) => ({
    title: item.title,
    desc: item.landingSteps?.[0] || '按数据、规则、任务闭环逐步落地。',
    tag: item.status === 'READY' ? '已落地' : item.status === 'PARTIAL' ? '增强中' : '待建设',
    tone: capabilityTone(item.status),
    path: item.routePath || '/portal'
  }))
})

const pendingTotal = computed(() =>
  numberValue(portal.value?.openTodoCount) +
  numberValue(portal.value?.pendingApprovalCount) +
  numberValue(logistics.value?.maintenancePendingCount)
)

// 首屏只突出四组最关键的经营与安全指标，其余下沉到「更多机构概览」。
const primaryMetrics = computed(() => ([
  {
    label: '今日风险',
    value: displayNumber(todayAlertCount.value),
    helper: '生命体征异常 · 长者异常 · 逾期工单',
    tone: todayAlertCount.value > 0 ? 'danger' as const : 'success' as const,
    path: '/stats/operations?tab=safety',
    statusText: todayAlertCount.value > 0 ? '需立即处理' : '平稳',
    statusTone: todayAlertCount.value > 0 ? 'danger' as const : 'normal' as const
  },
  {
    label: '待处理事项',
    value: displayNumber(pendingTotal.value),
    helper: '待办 · 审批 · 后勤工单',
    tone: pendingTotal.value > 0 ? 'warning' as const : 'success' as const,
    path: '/workbench/todo',
    statusText: pendingTotal.value > 0 ? '待处理' : '已清空',
    statusTone: pendingTotal.value > 0 ? 'warning' as const : 'normal' as const
  },
  {
    label: '在住 / 空床',
    value: `${displayNumber(dashboard.value?.inHospitalCount)} / ${displayNumber(dashboard.value?.availableBeds)}`,
    helper: '在住长者 / 空置床位',
    tone: 'brand' as const,
    path: '/elder/in-hospital-overview',
    statusText: '核心',
    statusTone: 'pending' as const
  },
  {
    label: '本月收入 / 欠费',
    value: displayCurrency(revenue.value?.totalRevenue ?? dashboard.value?.totalRevenue),
    helper: '本月收费与回款，可下钻欠费与结算',
    tone: 'success' as const,
    path: '/finance/workbench',
    statusText: '经营',
    statusTone: 'normal' as const
  }
]))

const secondaryMetrics = computed(() => ([
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
    label: '员工在岗数',
    value: displayNumber(hr.value?.onJobCount),
    helper: '排班与人力在岗',
    tone: 'brand' as const,
    path: '/stats/operations?tab=workforce',
    statusText: '人力',
    statusTone: 'pending' as const
  },
  {
    label: '设备在线率',
    value: '--',
    helper: '待设备遥测接入',
    tone: 'default' as const,
    path: '/logistics/equipment',
    statusText: '待接入',
    statusTone: 'offline' as const
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
    path: '/stats/operations?tab=safety'
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
    actionLabel: '立即复核'
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
    actionLabel: '派单处理'
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
    path: '/stats/operations?tab=logistics'
  },
  {
    label: '待回访客户',
    value: displayNumber(portal.value?.suggestionCount),
    helper: '咨询线索与营销回访',
    tone: 'success' as const,
    path: '/stats/operations?tab=marketing'
  }
]))

const quickActions = [
  { title: '新增长者', description: '进入入住办理，补充档案与合同。', icon: '长', path: '/elder/admission-processing' },
  { title: '新增护理记录', description: '进入医护任务板补充护理执行。', icon: '护', path: '/medical-care/care-task-board' },
  { title: '处理预警', description: '打开管理驾驶舱处理安全异常。', icon: '警', path: '/stats/operations?tab=safety' },
  { title: '创建工单', description: '进入后勤任务中心发起维修或巡检。', icon: '工', path: '/logistics/task-center' },
  { title: '收费登记', description: '进入财务运营中心处理收费与账单。', icon: '费', path: '/finance/workbench' },
  { title: '数据报表', description: '查看入住、收入与服务质量报表。', icon: '报', path: '/stats/org/monthly-operation' }
]

const birthdayStats = computed(() => {
  return birthdayRows.value.reduce((acc, item) => {
    const daysUntil = Number(item.daysUntil ?? 9999)
    if (daysUntil === 0) acc.today += 1
    if (daysUntil >= 0 && daysUntil <= 7) acc.next7Days += 1
    if (birthdayMonthMatches(item)) {
      acc.thisMonth += 1
    }
    if (Number(item.ageOnNextBirthday || 0) >= 80) acc.age80Plus += 1
    if (Number(item.ageOnNextBirthday || 0) >= 90) acc.age90Plus += 1
    return acc
  }, { today: 0, next7Days: 0, thisMonth: 0, age80Plus: 0, age90Plus: 0 })
})

const birthdayAgeBandLabel = computed(() => `${birthdayStats.value.age80Plus} / ${birthdayStats.value.age90Plus}`)

const birthdayPreviewList = computed(() => [...birthdayRows.value]
  .sort((left, right) => Number(left.daysUntil ?? 9999) - Number(right.daysUntil ?? 9999))
  .slice(0, 6))

const birthdayMonthList = computed(() => birthdayRows.value.filter((item) => birthdayMonthMatches(item)))

const birthdayModalRows = computed(() => {
  if (birthdayModalScope.value === 'today') {
    return birthdayRows.value.filter((item) => Number(item.daysUntil ?? 9999) === 0)
  }
  return birthdayRows.value.filter((item) => {
    const days = Number(item.daysUntil ?? 9999)
    return days >= 0 && days <= 7
  })
})

const birthdayModalTitle = computed(() => (birthdayModalScope.value === 'today' ? '今日生日名单' : '未来 7 天生日名单'))

const birthdayColumns = [
  { title: '长者姓名', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '下次生日', dataIndex: 'nextBirthday', key: 'nextBirthday', width: 120 },
  { title: '剩余天数', dataIndex: 'daysUntil', key: 'daysUntil', width: 100 },
  { title: '届时年龄', dataIndex: 'ageOnNextBirthday', key: 'ageOnNextBirthday', width: 100 },
  { title: '房间 / 床位', key: 'room', width: 180, customRender: ({ record }: { record: BirthdayReminder }) => [record.roomNo, record.bedNo].filter(Boolean).join(' / ') || '-' },
  { title: '操作', key: 'actions', width: 220 }
] as const

const birthdayHeadline = computed(() => {
  if (birthdayStats.value.today > 0) {
    return {
      title: `今天有 ${birthdayStats.value.today} 位长者过生日`,
      description: '建议优先安排探访、祝福、餐食加菜或小型集体庆生活动，避免关怀遗漏。'
    }
  }
  if (birthdayStats.value.next7Days > 0) {
    return {
      title: `未来 7 天有 ${birthdayStats.value.next7Days} 位长者生日`,
      description: '现在就可以开始安排家属通知、物资准备和本月集体活动排期。'
    }
  }
  return {
    title: '近期生日关怀节奏平稳',
    description: '本月生日名单仍然保留在首页，方便提前规划关怀活动和物资。'
  }
})

function birthdayMetaText(item: BirthdayReminder) {
  const age = item.ageOnNextBirthday ? `${item.ageOnNextBirthday}岁` : '年龄待补充'
  const room = [item.roomNo, item.bedNo].filter(Boolean).join(' / ') || '房间床位待补充'
  const date = item.nextBirthday || '--'
  return `${date} · ${age} · ${room}`
}

function openBirthdayModal(scope: 'today' | 'next7') {
  birthdayModalScope.value = scope
  birthdayModalOpen.value = true
}

function openBirthdayProfile(record: BirthdayReminder) {
  if (record.elderId != null) {
    router.push(`/elder/detail/${record.elderId}`)
    return
  }
  router.push(`/elder/list?keyword=${encodeURIComponent(record.elderName || '')}`)
}

function openBirthdayMaterialForRecord(record: BirthdayReminder) {
  router.push(`/inventory/outbound?scene=birthday&elderName=${encodeURIComponent(record.elderName || '')}`)
}

function openBirthdayActivityForRecord(record: BirthdayReminder) {
  router.push(`/life/activity?quick=create&elderName=${encodeURIComponent(record.elderName || '')}`)
}

function launchMonthlyBirthdayActivity() {
  if (!birthdayMonthList.value.length) {
    message.info('本月暂无生日长者')
    return
  }
  const month = dayjs().format('YYYY-MM')
  router.push(`/life/activity?quick=create&scope=monthly-birthday&month=${month}&count=${birthdayMonthList.value.length}`)
}

function openBirthdayMaterialPrep() {
  const firstName = birthdayMonthList.value[0]?.elderName || birthdayPreviewList.value[0]?.elderName
  const query = firstName ? `&elderName=${encodeURIComponent(firstName)}` : ''
  router.push(`/inventory/outbound?scene=birthday${query}`)
}

function printBirthdayRows(title: string, rows: BirthdayReminder[]) {
  if (!rows.length) {
    message.info('暂无可打印名单')
    return
  }
  openPrintTableReport({
    title,
    subtitle: `打印时间：${dayjs().format('YYYY-MM-DD HH:mm:ss')}`,
    columns: [
      { key: 'elderName', title: '长者姓名' },
      { key: 'nextBirthday', title: '下次生日' },
      { key: 'daysUntil', title: '剩余天数' },
      { key: 'ageOnNextBirthday', title: '届时年龄' },
      { key: 'roomNo', title: '房间号' },
      { key: 'bedNo', title: '床位号' }
    ],
    rows: rows as any
  })
}

function printBirthdayPreview() {
  printBirthdayRows('近期生日名单', birthdayPreviewList.value)
}

function printBirthdayModalRows() {
  printBirthdayRows(birthdayModalTitle.value, birthdayModalRows.value)
}

function birthdayDueText(daysUntil?: number | null) {
  const value = Number(daysUntil ?? 9999)
  if (value <= 0) return '今天'
  if (value <= 7) return `${value}天内`
  return `${value}天后`
}

function birthdayDueTone(daysUntil?: number | null) {
  const value = Number(daysUntil ?? 9999)
  if (value <= 0) return 'warning'
  if (value <= 7) return 'pending'
  return 'offline'
}

const collaborationMetrics = computed(() => ([
  {
    label: '开放待办',
    value: displayNumber(portal.value?.openTodoCount),
    helper: `逾期 ${displayNumber(portal.value?.overdueTodoCount)}`,
    tone: 'brand' as const,
    path: '/workbench/todo'
  },
  {
    label: '进行中任务',
    value: displayNumber(portal.value?.ongoingTaskCount),
    helper: '任务推进与跨部门执行',
    tone: 'success' as const,
    path: '/oa/work-execution/task'
  },
  {
    label: '今日日程',
    value: displayNumber(portal.value?.todayScheduleCount),
    helper: '个人、部门与协同安排',
    tone: 'warning' as const,
    path: '/workbench/schedule'
  },
  {
    label: '生日待办',
    value: displayNumber(portal.value?.birthdayTodoCount),
    helper: '关怀类提醒与后续处理',
    tone: 'default' as const,
    path: '/workbench/todo?keyword=生日提醒'
  }
]))

const collaborationFocusList = computed(() => ([
  {
    title: '逾期待办',
    desc: '优先处理已经超期的事项，避免继续积压到跨班次。',
    tag: `逾期 ${displayNumber(portal.value?.overdueTodoCount)}`,
    tone: numberValue(portal.value?.overdueTodoCount) > 0 ? 'danger' as const : 'offline' as const,
    path: '/workbench/todo?status=OVERDUE'
  },
  {
    title: '待我审批',
    desc: '财务、人事、行政等流程审批集中处理。',
    tag: `待审 ${displayNumber(portal.value?.pendingApprovalCount)}`,
    tone: numberValue(portal.value?.pendingApprovalCount) > 0 ? 'warning' as const : 'offline' as const,
    path: '/workbench/approvals'
  },
  {
    title: '生日关怀待办',
    desc: '把生日祝福、活动与物资准备转成具体可追踪动作。',
    tag: `提醒 ${displayNumber(portal.value?.birthdayTodoCount)}`,
    tone: numberValue(portal.value?.birthdayTodoCount) > 0 ? 'pending' as const : 'offline' as const,
    path: '/workbench/todo?keyword=生日提醒'
  },
  {
    title: '协同日程',
    desc: '查看今日与本周跨部门安排，避免执行撞车。',
    tag: `日程 ${displayNumber(portal.value?.todayScheduleCount)}`,
    tone: numberValue(portal.value?.todayScheduleCount) > 0 ? 'normal' as const : 'offline' as const,
    path: '/workbench/schedule'
  }
]).filter((item) => !item.tag.includes('--')))

const revenueTrendOption = computed(() => {
  const revenueList = revenue.value?.monthlyRevenue || []
  return {
    color: ['#21705F'],
    grid: { left: 10, right: 10, top: 20, bottom: 0, containLabel: true },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: revenueList.map((item: any) => item.month || item.label || '--'),
      axisLine: { lineStyle: { color: '#d3d8cf' } },
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
              { offset: 0, color: 'rgba(33, 112, 95, 0.26)' },
              { offset: 1, color: 'rgba(33, 112, 95, 0.02)' }
            ]
          }
        },
        lineStyle: { width: 3, color: '#21705F' },
        data: revenueList.map((item: any) => Number(item.amount || item.value || 0))
      }
    ]
  }
})

async function loadOverview() {
  loading.value = true
  try {
    const [homeBundle, birthdays] = await Promise.all([
      getHomeOverviewBundle(),
      getBirthdayAll({}, { silent403: true, silentError: true }).catch(() => [])
    ])
    bundle.value = homeBundle
    birthdayRows.value = birthdays || []
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
.portal-entry-strip,
.portal-grid {
  display: grid;
  gap: 18px;
}

.portal-entry-strip {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.portal-grid--metrics {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.portal-grid--command {
  grid-template-columns: minmax(0, 1.25fr) minmax(360px, 0.75fr);
}

.portal-grid--main,
.portal-grid--secondary {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.portal-grid--support {
  grid-template-columns: minmax(0, 1.15fr) minmax(0, 0.85fr);
}

.portal-grid--operating {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.portal-grid--more-metrics {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.portal-grid--risk-cards,
.portal-grid--work,
.portal-grid--quick-actions,
.portal-grid--birthday-metrics,
.portal-grid--collab-metrics {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.birthday-highlight-row,
.birthday-item,
.collab-list-item,
.command-list-item,
.landing-flow-step,
.entry-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.entry-card {
  width: 100%;
  padding: 18px 20px;
  border: 1px solid var(--border);
  border-radius: 14px;
  box-shadow: var(--shadow-xs);
  text-align: left;
  cursor: pointer;
}

.entry-card--employee {
  border-color: rgba(var(--primary-rgb), 0.18);
  background: linear-gradient(135deg, #e8f3ef, #ffffff);
}

.entry-card--manager {
  border-color: rgba(var(--accent-rgb), 0.22);
  background: linear-gradient(135deg, #fbf0e2, #ffffff);
}

.entry-card span,
.entry-card small {
  display: block;
  color: var(--muted);
  line-height: 1.7;
}

.entry-card span {
  font-size: 12px;
}

.entry-card strong {
  display: block;
  margin: 6px 0;
  color: var(--ink);
  font-size: 22px;
  line-height: 1.2;
}

.entry-card small {
  font-size: 12px;
}

.command-scoreboard {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.command-score-card {
  position: relative;
  display: grid;
  gap: 8px;
  min-height: 132px;
  padding: 16px;
  overflow: hidden;
  border: 1px solid var(--border);
  border-radius: 12px;
  background: #ffffff;
  text-align: left;
  cursor: pointer;
}

.command-score-card span,
.command-score-card small,
.command-column__head span,
.landing-flow-step span,
.command-list-item span {
  color: var(--muted);
  font-size: 12px;
  line-height: 1.6;
}

.command-score-card strong {
  color: var(--ink);
  font-size: 28px;
  line-height: 1;
}

.command-score-card__bar {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  height: 4px;
  background: rgba(118, 139, 154, 0.5);
}

.command-score-card__bar--success {
  background: rgb(var(--success-rgb));
}

.command-score-card__bar--warning {
  background: rgb(var(--warning-rgb));
}

.command-score-card__bar--danger {
  background: rgb(var(--danger-rgb));
}

.command-split {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.command-column,
.landing-flow,
.capability-lane {
  display: grid;
  gap: 10px;
}

.command-column__head,
.capability-lane__head {
  padding: 4px 2px;
}

.command-column__head strong,
.capability-lane__head strong,
.capability-chip strong,
.landing-flow-step strong,
.command-list-item strong {
  display: block;
  color: var(--ink);
}

.command-list-item,
.landing-flow-step,
.capability-chip {
  width: 100%;
  padding: 14px 15px;
  border: 1px solid var(--border);
  border-radius: 12px;
  background: #ffffff;
  text-align: left;
  cursor: pointer;
}

.capability-lane {
  margin-top: 4px;
  padding-top: 14px;
  border-top: 1px solid var(--border-soft);
}

.capability-lane__entry {
  justify-self: start;
}

.capability-lane__head span,
.capability-chip span {
  color: var(--muted);
  font-size: 12px;
  line-height: 1.6;
}

.capability-chip {
  display: grid;
  grid-template-columns: minmax(0, 0.82fr) minmax(0, 1.18fr) auto;
  align-items: center;
  gap: 12px;
}

.landing-flow-step {
  align-items: flex-start;
}

.landing-flow-step b {
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 999px;
  background: rgba(33, 112, 95, 0.12);
  color: var(--primary-strong);
  font-size: 12px;
}

.landing-flow-step div,
.command-list-item div {
  min-width: 0;
}

.birthday-highlight-row {
  padding: 14px 16px;
  border: 1px solid rgba(var(--accent-rgb), 0.24);
  border-radius: 12px;
  background: linear-gradient(135deg, var(--accent-soft), #ffffff);
}

.birthday-highlight-copy strong,
.birthday-item strong,
.collab-list-item strong {
  display: block;
  color: var(--ink);
}

.birthday-highlight-copy p,
.birthday-item span,
.collab-list-item span {
  margin: 4px 0 0;
  color: var(--muted);
  font-size: 12px;
  line-height: 1.6;
}

.birthday-list,
.collab-list {
  display: grid;
  gap: 12px;
}

.birthday-action-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.birthday-action-card {
  padding: 14px 16px;
  border: 1px solid var(--border);
  border-radius: 12px;
  background: #ffffff;
  text-align: left;
  cursor: pointer;
}

.birthday-action-card strong {
  display: block;
  color: var(--ink);
}

.birthday-action-card span {
  display: block;
  margin-top: 6px;
  color: var(--muted);
  font-size: 12px;
  line-height: 1.6;
}

.birthday-item,
.collab-list-item {
  width: 100%;
  padding: 14px 16px;
  border: 1px solid var(--border);
  border-radius: 12px;
  background: #ffffff;
  text-align: left;
  cursor: pointer;
}

.birthday-footer-actions,
.birthday-modal-actions {
  margin-top: 12px;
}

.trend-chart {
  height: 240px;
}

@media (max-width: 1280px) {
  .portal-entry-strip,
  .portal-grid--metrics,
  .portal-grid--command,
  .portal-grid--main,
  .portal-grid--secondary,
  .portal-grid--support,
  .portal-grid--operating,
  .portal-grid--risk-cards,
  .portal-grid--work,
  .portal-grid--quick-actions,
  .portal-grid--birthday-metrics,
  .portal-grid--collab-metrics,
  .portal-grid--more-metrics {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .command-scoreboard,
  .command-split {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .birthday-action-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .portal-entry-strip,
  .portal-grid--metrics,
  .portal-grid--command,
  .portal-grid--main,
  .portal-grid--secondary,
  .portal-grid--support,
  .portal-grid--operating,
  .portal-grid--risk-cards,
  .portal-grid--work,
  .portal-grid--quick-actions,
  .portal-grid--birthday-metrics,
  .portal-grid--collab-metrics,
  .portal-grid--more-metrics {
    grid-template-columns: 1fr;
  }

  .command-scoreboard,
  .command-split,
  .capability-chip {
    grid-template-columns: 1fr;
  }

  .birthday-highlight-row,
  .birthday-item,
  .collab-list-item,
  .command-list-item,
  .landing-flow-step {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
