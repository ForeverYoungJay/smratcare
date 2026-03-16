<template>
  <PageContainer title="销售运营工作台" sub-title="营销管理首页：漏斗、跟进、床态、合同、回访、业绩、风险一屏总览">
    <a-card class="card-elevated dashboard-control" :bordered="false" style="margin-bottom: 12px">
      <a-space wrap>
        <a-tag color="blue">机会指数：{{ opportunityCount }}</a-tag>
        <a-tag :color="riskCount > 0 ? 'red' : 'green'">风险指数：{{ riskCount }}</a-tag>
        <span class="quick-label">统计区间：</span>
        <a-radio-group v-model:value="quickRange" size="small" @change="onQuickRangeChange">
          <a-radio-button value="TODAY">今日</a-radio-button>
          <a-radio-button value="7D">近7天</a-radio-button>
          <a-radio-button value="30D">近30天</a-radio-button>
          <a-radio-button value="MONTH">本月</a-radio-button>
        </a-radio-group>
        <a-range-picker
          v-model:value="dateRange"
          value-format="YYYY-MM-DD"
          :allow-clear="false"
          @change="onRangeChange"
        />
        <a-switch v-model:checked="autoRefresh" />
        <span class="quick-label">自动刷新</span>
        <a-button size="small" :loading="dashboardLoading" @click="loadOverview">立即刷新</a-button>
        <span class="quick-label">更新于：{{ lastUpdated || '-' }}</span>
      </a-space>
      <a-alert
        v-if="dashboardError"
        style="margin-top: 10px"
        type="warning"
        show-icon
        :message="dashboardError"
      />
    </a-card>
    <a-card class="card-elevated" :bordered="false" style="margin-bottom: 16px">
      <a-space wrap>
        <span class="quick-label">快速返回上次筛选视图：</span>
        <a-button
          v-for="item in quickReportButtons"
          :key="item.key"
          size="small"
          @click="goReport(item.entry)"
        >
          {{ quickLabel(item.entry, item.label) }}
        </a-button>
      </a-space>
    </a-card>

    <a-card class="card-elevated action-board" :bordered="false" style="margin-bottom: 16px">
      <a-space wrap>
        <span class="quick-label">今日建议动作：</span>
        <a-button type="primary" ghost @click="goFollowup('overdue')">
          处理逾期跟进（{{ followup.overdue }}）
        </a-button>
        <a-button type="primary" ghost @click="goContract('pending')">
          推进待签署（{{ contract.pendingSignCount }}）
        </a-button>
        <a-button type="primary" ghost @click="goReservation('expiring')">
          处理锁床到期（{{ followup.lockExpiringCount }}）
        </a-button>
        <a-button type="primary" ghost @click="goPlan({ status: 'PENDING_APPROVAL' })">
          审批营销方案（{{ plan.pendingApprovalCount }}）
        </a-button>
      </a-space>
    </a-card>
    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :lg="12">
        <a-card class="card-elevated" :bordered="false" title="卡片1：销售漏斗总览（核心卡）">
          <a-row :gutter="[12, 12]">
            <a-col :span="12"><div class="stat-link" @click="goLead('all', { tab: 'consultation' })"><a-statistic title="今日新增咨询数" :value="funnel.todayConsultCount" /></div></a-col>
            <a-col :span="12"><div class="stat-link" @click="goFunnel('evaluation')"><a-statistic title="待评估人数" :value="funnel.evaluationCount" /></div></a-col>
            <a-col :span="12"><div class="stat-link" @click="goContract('pending')"><a-statistic title="待签约人数" :value="funnel.pendingSignCount" /></div></a-col>
            <a-col :span="12"><div class="stat-link" @click="goFunnel('admission')"><a-statistic title="待入住人数" :value="funnel.pendingAdmissionCount" /></div></a-col>
            <a-col :span="12"><div class="stat-link" @click="goContract('signed')"><a-statistic title="本月成交数" :value="funnel.monthDealCount" /></div></a-col>
            <a-col :span="12"><div class="stat-link" @click="goReport('conversion')"><a-statistic title="本月转化率" :value="funnel.monthConversionRate" suffix="%" /></div></a-col>
          </a-row>
          <v-chart :option="funnelOption" autoresize style="height: 240px; margin-top: 12px" />
          <a-space wrap>
            <a-button size="small" @click="goFunnel('evaluation')">待评估</a-button>
            <a-button size="small" @click="goContract('pending')">待签约</a-button>
            <a-button size="small" @click="goReport('conversion')">转化率</a-button>
          </a-space>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="12">
        <a-card class="card-elevated" :bordered="false" title="卡片2：今日待跟进">
          <a-row :gutter="[12, 12]">
            <a-col :span="12"><div class="stat-link" @click="goFollowup('today')"><a-statistic title="今日待回访数量" :value="followup.todayDue" /></div></a-col>
            <a-col :span="12"><div class="stat-link" @click="goFollowup('overdue')"><a-statistic title="逾期未跟进数量" :value="followup.overdue" /></div></a-col>
            <a-col :span="12"><div class="stat-link" @click="goLead('intent')"><a-statistic title="高意向客户数量" :value="followup.highIntentCount" /></div></a-col>
            <a-col :span="12"><div class="stat-link" @click="goReservation('expiring')"><a-statistic title="锁床即将到期数量" :value="followup.lockExpiringCount" /></div></a-col>
          </a-row>
          <a-space wrap style="margin-top: 12px">
            <a-button size="small" @click="goFollowup('today')">今日跟进计划</a-button>
            <a-button size="small" @click="goFollowup('overdue')">逾期未跟进</a-button>
            <a-button size="small" @click="goReservation('expiring')">预定到期提醒</a-button>
          </a-space>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="12">
        <a-card class="card-elevated" :bordered="false" title="卡片3：渠道表现">
          <a-table
            :columns="channelColumns"
            :data-source="channelTop5"
            :pagination="false"
            size="small"
            row-key="source"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'source'">
                <a @click="goChannelSource(record.source)">{{ record.source }}</a>
              </template>
            </template>
          </a-table>
          <a-row :gutter="12" style="margin-top: 12px">
            <a-col :span="12"><div class="stat-link" @click="goReport('unknown-channel')"><a-statistic title="不明渠道数量" :value="channelUnknownCount" /></div></a-col>
            <a-col :span="12"><div class="stat-link" @click="goReport('channel-rank')"><a-statistic title="本月渠道成交贡献" :value="channelMonthDeals" /></div></a-col>
          </a-row>
          <a-space wrap style="margin-top: 12px">
            <a-button size="small" @click="goReport('channel')">渠道评估</a-button>
            <a-button size="small" @click="goReport('unknown-channel')">不明渠道统计</a-button>
          </a-space>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="12">
        <a-card class="card-elevated" :bordered="false" title="卡片4：床位销售视图（销售版床态）">
          <a-row :gutter="[12, 12]">
            <a-col :span="12"><div class="stat-link" @click="goReservation('panorama')"><a-statistic title="空床数量" :value="bedSales.emptyCount" /></div></a-col>
            <a-col :span="12"><div class="stat-link" @click="goReservation('lock')"><a-statistic title="锁床数量" :value="bedSales.lockCount" /></div></a-col>
            <a-col :span="12"><div class="stat-link" @click="goContract('pending')"><a-statistic title="预定未签数量" :value="bedSales.reservedUnsignedCount" /></div></a-col>
            <a-col :span="12"><div class="stat-link" @click="goReservation('panorama', { roomType: 'premium' })"><a-statistic title="高端房型空床数量" :value="bedSales.premiumEmptyCount" /></div></a-col>
          </a-row>
          <a-space wrap style="margin-top: 12px">
            <a-button size="small" @click="goReservation('panorama')">床态全景（销售）</a-button>
            <a-button size="small" @click="goReservation('records')">床位预定管理</a-button>
          </a-space>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="12">
        <a-card class="card-elevated" :bordered="false" title="卡片5：合同进度">
          <a-row :gutter="[12, 12]">
            <a-col :span="12"><div class="stat-link" @click="goContract('pending')"><a-statistic title="待签合同" :value="contract.pendingSignCount" /></div></a-col>
            <a-col :span="12"><div class="stat-link" @click="goContract('renewal')"><a-statistic title="续签提醒" :value="contract.renewalDueCount" /></div></a-col>
            <a-col :span="12"><div class="stat-link" @click="goContract('change')"><a-statistic title="合同变更待审批" :value="contract.changePendingCount" /></div></a-col>
            <a-col :span="12"><div class="stat-link" @click="goReport('sales-performance')"><a-statistic title="本月合同金额总计" :value="contract.monthAmount" precision="2" prefix="¥" /></div></a-col>
          </a-row>
          <a-space wrap style="margin-top: 12px">
            <a-button size="small" @click="goContract('pending')">待签合同</a-button>
            <a-button size="small" @click="goContract('renewal')">续签提醒</a-button>
            <a-button size="small" @click="goContract('change')">合同变更</a-button>
            <a-button size="small" @click="goContract('attachments')">合同附件</a-button>
          </a-space>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="12">
        <a-card class="card-elevated" :bordered="false" title="卡片6：回访与满意度">
          <a-row :gutter="[12, 12]">
            <a-col :span="12"><div class="stat-link" @click="goCallback('checkin')"><a-statistic title="入住7天回访数量" :value="callback.checkinCount" /></div></a-col>
            <a-col :span="12"><div class="stat-link" @click="goCallback('trial')"><a-statistic title="试住回访数量" :value="callback.trialCount" /></div></a-col>
            <a-col :span="12"><div class="stat-link" @click="goCallback('discharge')"><a-statistic title="退住回访数量" :value="callback.dischargeCount" /></div></a-col>
            <a-col :span="12"><div class="stat-link" @click="goCallback('score')"><a-statistic title="回访评分" :value="callback.score" suffix="/5" /></div></a-col>
          </a-row>
          <a-space wrap style="margin-top: 12px">
            <a-button size="small" @click="goCallback('checkin')">入住后回访</a-button>
            <a-button size="small" @click="goCallback('trial')">试住回访</a-button>
            <a-button size="small" @click="goCallback('discharge')">退住回访</a-button>
            <a-button size="small" @click="goCallback('score')">回访质量评分</a-button>
          </a-space>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="12">
        <a-card class="card-elevated" :bordered="false" title="卡片7：销售人员业绩">
          <a-row :gutter="[12, 12]">
            <a-col :span="12"><div class="stat-link" @click="goReport('sales-performance')"><a-statistic title="本月个人成交数" :value="performance.monthDealCount" /></div></a-col>
            <a-col :span="12"><div class="stat-link" @click="goReport('sales-performance')"><a-statistic title="本月签约金额" :value="performance.monthAmount" precision="2" prefix="¥" /></div></a-col>
            <a-col :span="12"><div class="stat-link" @click="goReport('sales-performance')"><a-statistic title="转化率排名" :value="performance.rankNo" suffix="名" /></div></a-col>
            <a-col :span="12"><div class="stat-link" @click="goReport('followup')"><a-statistic title="跟进及时率" :value="performance.timelyRate" suffix="%" /></div></a-col>
          </a-row>
          <a-space wrap style="margin-top: 12px">
            <a-button size="small" @click="goReport('sales-performance')">销售人员业绩</a-button>
          </a-space>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="12">
        <a-card class="card-elevated" :bordered="false" title="卡片8：外来就医转线索">
          <a-row :gutter="[12, 12]">
            <a-col :span="12"><div class="stat-link" @click="goLead('medical-transfer', { source: 'medical' })"><a-statistic title="今日新增医疗转线索" :value="medical.todayCount" /></div></a-col>
            <a-col :span="12"><div class="stat-link" @click="goLead('medical-transfer', { source: 'medical' })"><a-statistic title="医护推荐潜客" :value="medical.referCount" /></div></a-col>
            <a-col :span="12"><div class="stat-link" @click="goLead('medical-transfer', { source: 'medical', filter: 'unassigned' })"><a-statistic title="未分配线索数量" :value="medical.unassignedCount" /></div></a-col>
          </a-row>
          <a-space wrap style="margin-top: 12px">
            <a-button size="small" @click="goLead('medical-transfer', { source: 'medical' })">医疗转线索</a-button>
          </a-space>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="12">
        <a-card class="card-elevated" :bordered="false" title="卡片9：营销方案与审批">
          <a-row :gutter="[12, 12]">
            <a-col :span="12"><div class="stat-link" @click="goPlan({ moduleType: 'SPEECH' })"><a-statistic title="话术库总数" :value="plan.speechCount" /></div></a-col>
            <a-col :span="12"><div class="stat-link" @click="goPlan({ moduleType: 'POLICY' })"><a-statistic title="季度政策总数" :value="plan.policyCount" /></div></a-col>
            <a-col :span="12"><div class="stat-link" @click="goPlan({ status: 'PENDING_APPROVAL' })"><a-statistic title="待审批方案" :value="plan.pendingApprovalCount" /></div></a-col>
            <a-col :span="12"><div class="stat-link" @click="goPlan({ status: 'REJECTED' })"><a-statistic title="已驳回方案" :value="plan.rejectedCount" /></div></a-col>
          </a-row>
          <a-space wrap style="margin-top: 12px">
            <a-button size="small" @click="goPlan({ status: 'PUBLISHED' })">已发布方案</a-button>
            <a-button size="small" @click="goPlan({ status: 'INACTIVE' })">停用方案</a-button>
            <a-button size="small" @click="goApproval('MARKETING_PLAN')">院长审批栏</a-button>
          </a-space>
        </a-card>
      </a-col>

      <a-col :xs="24">
        <a-card class="card-elevated" :bordered="false" title="卡片10：销售风险预警">
          <a-row :gutter="[12, 12]">
            <a-col :xs="12" :lg="6"><div class="stat-link" @click="goFollowup('overdue')"><a-statistic title="逾期跟进客户" :value="risk.overdueFollowupCount" /></div></a-col>
            <a-col :xs="12" :lg="6"><div class="stat-link" @click="goReservation('lock', { filter: 'unsigned_lock' })"><a-statistic title="锁床未签约客户" :value="risk.lockUnsignedCount" /></div></a-col>
            <a-col :xs="12" :lg="6"><div class="stat-link" @click="goLead('intent', { filter: 'missing_followup' })"><a-statistic title="高意向未评估客户" :value="risk.highIntentNoEvalCount" /></div></a-col>
            <a-col :xs="12" :lg="6"><div class="stat-link" @click="goReport('channel-rank')"><a-statistic title="渠道异常下降数" :value="risk.channelDropCount" /></div></a-col>
          </a-row>
        </a-card>
      </a-col>
    </a-row>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import VChart from 'vue-echarts'
import PageContainer from '../../components/PageContainer.vue'
import { buildReportRoute, hasReportCache } from '../../utils/marketingReportNav'
import { getMarketingReportRoute, type MarketingReportEntry } from '../../utils/marketingReportRegistry'
import {
  buildCallbackRoute,
  buildContractRoute,
  buildFollowupRoute,
  buildFunnelRoute,
  buildLeadRoute,
  buildReservationRoute,
  routeToUnknownSourceOrAll
} from '../../utils/marketingNav'
import { getBedMap } from '../../api/bed'
import {
  getContractPage,
  getLeadPage,
  getMarketingCallbackReport,
  getMarketingChannelReport,
  getMarketingConversionReport,
  getMarketingFollowupReport,
  getMarketingPlanPage
} from '../../api/marketing'
import type { BedItem, CrmContractItem, CrmLeadItem, MarketingChannelReportItem, MarketingConversionReport, MarketingFollowupReport, PageResult } from '../../types'

const router = useRouter()
const dashboardLoading = ref(false)
const dashboardError = ref('')
const lastUpdated = ref('')
const quickRange = ref<'TODAY' | '7D' | '30D' | 'MONTH'>('MONTH')
const dateRange = ref<any[]>([dayjs().startOf('month').format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')])
const autoRefresh = ref(true)
let refreshTimer: number | undefined
const AUTO_REFRESH_INTERVAL_MS = 60 * 1000

const funnel = reactive({
  todayConsultCount: 0,
  evaluationCount: 0,
  pendingSignCount: 0,
  pendingAdmissionCount: 0,
  monthDealCount: 0,
  monthConversionRate: 0
})
const followup = reactive({
  todayDue: 0,
  overdue: 0,
  highIntentCount: 0,
  lockExpiringCount: 0
})
const bedSales = reactive({
  emptyCount: 0,
  lockCount: 0,
  reservedUnsignedCount: 0,
  premiumEmptyCount: 0
})
const contract = reactive({
  pendingSignCount: 0,
  renewalDueCount: 0,
  changePendingCount: 0,
  monthAmount: 0
})
const callback = reactive({
  checkinCount: 0,
  trialCount: 0,
  dischargeCount: 0,
  score: 0
})
const performance = reactive({
  monthDealCount: 0,
  monthAmount: 0,
  rankNo: 0,
  timelyRate: 0
})
const medical = reactive({
  todayCount: 0,
  referCount: 0,
  unassignedCount: 0
})
const plan = reactive({
  speechCount: 0,
  policyCount: 0,
  pendingApprovalCount: 0,
  rejectedCount: 0
})
const risk = reactive({
  overdueFollowupCount: 0,
  lockUnsignedCount: 0,
  highIntentNoEvalCount: 0,
  channelDropCount: 0
})

const channelTop5 = ref<Array<{ source: string; leadCount: number; contractRate: string }>>([])
const channelUnknownCount = ref(0)
const channelMonthDeals = ref(0)

const channelColumns = [
  { title: '渠道', dataIndex: 'source', key: 'source', width: 160 },
  { title: '线索数', dataIndex: 'leadCount', key: 'leadCount', width: 120 },
  { title: '转化率', dataIndex: 'contractRate', key: 'contractRate', width: 120 }
]
const riskCount = computed(() =>
  Number(risk.overdueFollowupCount || 0)
  + Number(risk.lockUnsignedCount || 0)
  + Number(risk.highIntentNoEvalCount || 0)
  + Number(risk.channelDropCount || 0)
)
const opportunityCount = computed(() =>
  Number(funnel.evaluationCount || 0)
  + Number(funnel.pendingSignCount || 0)
  + Number(funnel.pendingAdmissionCount || 0)
  + Number(medical.unassignedCount || 0)
)

const quickReportButtons = computed(() => {
  return [
    { key: 'conversion', label: '转化率统计', entry: 'conversion' as MarketingReportEntry },
    { key: 'followup', label: '跟进统计', entry: 'followup' as MarketingReportEntry },
    { key: 'channel', label: '渠道评估', entry: 'channel' as MarketingReportEntry },
    { key: 'consultation', label: '咨询统计', entry: 'consultation' as MarketingReportEntry },
    { key: 'callback', label: '回访统计', entry: 'callback' as MarketingReportEntry }
  ]
})

const funnelOption = computed(() => ({
  tooltip: { trigger: 'item' },
  series: [
    {
      type: 'funnel',
      left: '12%',
      width: '76%',
      data: [
        { name: '咨询', value: funnel.todayConsultCount || 0 },
        { name: '评估', value: funnel.evaluationCount || 0 },
        { name: '签约', value: funnel.pendingSignCount || 0 },
        { name: '入住', value: funnel.monthDealCount || 0 }
      ]
    }
  ]
}))

function goWithReportCache(path: string, cacheKey: string) {
  router.push(buildReportRoute(path, cacheKey))
}

function goReport(entry: MarketingReportEntry) {
  const config = getMarketingReportRoute(entry, hasCache)
  goWithReportCache(config.path, config.cacheKey)
}

function goLead(entry: 'all' | 'intent' | 'invalid' | 'blacklist' | 'unknown-source' | 'medical-transfer', query?: Record<string, string>) {
  router.push(buildLeadRoute(entry, query))
}

function goFollowup(entry: 'records' | 'due' | 'today' | 'overdue', query?: Record<string, string>) {
  router.push(buildFollowupRoute(entry, query))
}

function goReservation(entry: 'records' | 'lock' | 'expiring' | 'panorama', query?: Record<string, string>) {
  router.push(buildReservationRoute(entry, query))
}

function goContract(entry: 'pending' | 'signed' | 'renewal' | 'change' | 'attachments', query?: Record<string, string>) {
  router.push(buildContractRoute(entry, query))
}

function goFunnel(entry: 'consultation' | 'evaluation' | 'signing' | 'admission' | 'lost', query?: Record<string, string>) {
  router.push(buildFunnelRoute(entry, query))
}

function goCallback(entry: 'checkin' | 'trial' | 'discharge' | 'score', query?: Record<string, string>) {
  router.push(buildCallbackRoute(entry, query))
}

function goChannelSource(source: string) {
  router.push(routeToUnknownSourceOrAll(source))
}

function goPlan(query?: Record<string, string>) {
  router.push({
    path: '/marketing/plan',
    query: query || {}
  })
}

function goApproval(type: string) {
  router.push({
    path: '/oa/approval',
    query: { type, status: 'PENDING' }
  })
}

function hasCache(cacheKey: string) {
  return hasReportCache(cacheKey)
}

function quickLabel(entry: MarketingReportEntry, base: string) {
  const config = getMarketingReportRoute(entry, hasCache)
  return hasCache(config.cacheKey) ? `${base}（上次筛选）` : base
}

function isSignedContract(item: CrmContractItem) {
  const flowStage = String(item.flowStage || '').toUpperCase()
  const status = String(item.status || '').toUpperCase()
  return flowStage === 'SIGNED' || status === 'SIGNED' || status === 'EFFECTIVE'
}

async function loadOverview() {
  const rangeStart = String(dateRange.value?.[0] || dayjs().startOf('month').format('YYYY-MM-DD'))
  const rangeEnd = String(dateRange.value?.[1] || dayjs().format('YYYY-MM-DD'))
  const today = dayjs().format('YYYY-MM-DD')
  dashboardLoading.value = true
  dashboardError.value = ''
  try {
    const [
      conversion,
      followupReport,
      callbackReport,
      checkinCallbackReport,
      trialCallbackReport,
      dischargeCallbackReport,
      scoreCallbackReport,
      channelReport,
      leadPage,
      contractPage,
      bedMap,
      speechPlanPage,
      policyPlanPage
    ] = await Promise.all([
      getMarketingConversionReport({ dateFrom: rangeStart, dateTo: rangeEnd }),
      getMarketingFollowupReport({ dateFrom: rangeStart, dateTo: rangeEnd }) as Promise<MarketingFollowupReport>,
      getMarketingCallbackReport({ pageNo: 1, pageSize: 200, dateFrom: rangeStart, dateTo: rangeEnd }),
      getMarketingCallbackReport({ pageNo: 1, pageSize: 1, dateFrom: rangeStart, dateTo: rangeEnd, type: 'checkin' }),
      getMarketingCallbackReport({ pageNo: 1, pageSize: 1, dateFrom: rangeStart, dateTo: rangeEnd, type: 'trial' }),
      getMarketingCallbackReport({ pageNo: 1, pageSize: 1, dateFrom: rangeStart, dateTo: rangeEnd, type: 'discharge' }),
      getMarketingCallbackReport({ pageNo: 1, pageSize: 500, dateFrom: rangeStart, dateTo: rangeEnd, type: 'score' }),
      getMarketingChannelReport({ dateFrom: rangeStart, dateTo: rangeEnd }) as Promise<MarketingChannelReportItem[]>,
      getLeadPage({ pageNo: 1, pageSize: 300 }),
      getContractPage({ pageNo: 1, pageSize: 300 }),
      getBedMap(),
      getMarketingPlanPage({ pageNo: 1, pageSize: 500, moduleType: 'SPEECH' }),
      getMarketingPlanPage({ pageNo: 1, pageSize: 500, moduleType: 'POLICY' })
    ])

    const leads = (leadPage as PageResult<CrmLeadItem>).list || []
    const contracts = (contractPage as PageResult<CrmContractItem>).list || []
    const beds = (bedMap || []) as BedItem[]
    const speechPlans = speechPlanPage.list || []
    const policyPlans = policyPlanPage.list || []
    const conv = conversion as MarketingConversionReport

  funnel.todayConsultCount = conv.consultCount || 0
  funnel.evaluationCount = leads.filter((item) => Number(item.status) === 1).length
  funnel.pendingSignCount = contracts.filter((item) => String(item.flowStage || '') === 'PENDING_SIGN').length
  funnel.pendingAdmissionCount = contracts.filter((item) => String(item.flowStage || '') === 'PENDING_BED_SELECT').length
  funnel.monthDealCount = conv.contractCount || 0
  funnel.monthConversionRate = Number(conv.contractRate || 0)

  followup.todayDue = callbackReport.todayDue || 0
  followup.overdue = callbackReport.overdue || 0
  followup.highIntentCount = leads.filter((item) => Number(item.status) === 1).length
  followup.lockExpiringCount = leads.filter((item) => String(item.reservationStatus || '').includes('锁') && item.nextFollowDate).length

  const emptyBeds = beds.filter((item) => Number(item.status) === 1)
  bedSales.emptyCount = emptyBeds.length
  bedSales.lockCount = leads.filter((item) => String(item.reservationStatus || '').includes('锁')).length
  bedSales.reservedUnsignedCount = leads.filter((item) => Number(item.status) === 2 && !item.contractSignedFlag).length
  bedSales.premiumEmptyCount = emptyBeds.filter((item) => String(item.roomType || '').includes('高') || String(item.roomNo || '').startsWith('V')).length

  contract.pendingSignCount = contracts.filter((item) => String(item.flowStage || '') === 'PENDING_SIGN').length
  contract.renewalDueCount = contracts.filter((item) => {
    if (!isSignedContract(item)) return false
    if (!item.contractExpiryDate) return false
    const diff = dayjs(item.contractExpiryDate).diff(dayjs(today), 'day')
    return diff >= 0 && diff <= 30
  }).length
  contract.changePendingCount = contracts.filter((item) => String(item.changeWorkflowStatus || '') === 'PENDING_APPROVAL').length
  contract.monthAmount = contracts.reduce((acc, item) => acc + Number(item.amount || item.contractAmount || 0), 0)

  callback.checkinCount = checkinCallbackReport.total || 0
  callback.trialCount = trialCallbackReport.total || 0
  callback.dischargeCount = dischargeCallbackReport.total || 0
  const scoreRows = scoreCallbackReport.records || []
  callback.score = scoreRows.length
    ? Number((scoreRows.reduce((acc, item) => acc + Number(item.score || 0), 0) / scoreRows.length).toFixed(1))
    : 0

  const marketerDealMap = new Map<string, { count: number; amount: number }>()
  contracts.forEach((item) => {
    const key = String(item.marketerName || '未分配')
    const current = marketerDealMap.get(key) || { count: 0, amount: 0 }
    current.count += 1
    current.amount += Number(item.amount || item.contractAmount || 0)
    marketerDealMap.set(key, current)
  })
  const sortedMarketers = Array.from(marketerDealMap.values()).sort((a, b) => b.count - a.count)
  performance.monthDealCount = sortedMarketers[0]?.count || 0
  performance.monthAmount = sortedMarketers[0]?.amount || 0
  performance.rankNo = sortedMarketers.length ? 1 : 0
  performance.timelyRate = Number((((followupReport.totalLeads || 0) - (followupReport.overdueCount || 0)) / Math.max(followupReport.totalLeads || 1, 1) * 100).toFixed(1))

  const medicalLeads = leads.filter((item) =>
    String(item.infoSource || '').includes('医') || String(item.mediaChannel || '').includes('医')
  )
  medical.todayCount = medicalLeads.filter((item) => String(item.createTime || '').slice(0, 10) === today).length
  medical.referCount = medicalLeads.length
  medical.unassignedCount = medicalLeads.filter((item) => !item.marketerName).length

  const allPlans = [...speechPlans, ...policyPlans]
  plan.speechCount = speechPlans.length
  plan.policyCount = policyPlans.length
  plan.pendingApprovalCount = allPlans.filter((item) => String(item.status || '') === 'PENDING_APPROVAL').length
  plan.rejectedCount = allPlans.filter((item) => String(item.status || '') === 'REJECTED').length

  risk.overdueFollowupCount = callbackReport.overdue || 0
  risk.lockUnsignedCount = leads.filter((item) => String(item.reservationStatus || '').includes('锁') && !item.contractSignedFlag).length
  risk.highIntentNoEvalCount = leads.filter((item) => Number(item.status) === 1 && !item.followupStatus).length
  risk.channelDropCount = channelReport.filter((item) => (item.leadCount || 0) > 0 && ((item.contractCount || 0) / item.leadCount) < 0.1).length

  const top = [...channelReport]
    .sort((a, b) => (b.contractCount || 0) - (a.contractCount || 0))
    .slice(0, 5)
  channelTop5.value = top.map((item) => ({
    source: item.source || '未标记渠道',
    leadCount: item.leadCount || 0,
    contractRate: item.leadCount ? `${(((item.contractCount || 0) / item.leadCount) * 100).toFixed(1)}%` : '0%'
  }))
  channelUnknownCount.value = channelReport
    .filter((item) => !String(item.source || '').trim() || String(item.source || '').includes('不明'))
    .reduce((acc, item) => acc + Number(item.leadCount || 0), 0)
  channelMonthDeals.value = channelReport.reduce((acc, item) => acc + Number(item.contractCount || 0), 0)
  lastUpdated.value = dayjs().format('YYYY-MM-DD HH:mm:ss')
  } catch (error: any) {
    dashboardError.value = error?.message || '营销工作台数据加载失败'
  } finally {
    dashboardLoading.value = false
  }
}

function applyQuickRange(range: 'TODAY' | '7D' | '30D' | 'MONTH') {
  const end = dayjs()
  if (range === 'TODAY') {
    dateRange.value = [end.format('YYYY-MM-DD'), end.format('YYYY-MM-DD')]
    return
  }
  if (range === '7D') {
    dateRange.value = [end.subtract(6, 'day').format('YYYY-MM-DD'), end.format('YYYY-MM-DD')]
    return
  }
  if (range === '30D') {
    dateRange.value = [end.subtract(29, 'day').format('YYYY-MM-DD'), end.format('YYYY-MM-DD')]
    return
  }
  dateRange.value = [end.startOf('month').format('YYYY-MM-DD'), end.format('YYYY-MM-DD')]
}

function onQuickRangeChange() {
  applyQuickRange(quickRange.value)
  loadOverview()
}

function onRangeChange() {
  loadOverview()
}

function startAutoRefresh() {
  stopAutoRefresh()
  if (!autoRefresh.value) return
  refreshTimer = window.setInterval(() => {
    if (dashboardLoading.value) return
    loadOverview()
  }, AUTO_REFRESH_INTERVAL_MS)
}

function stopAutoRefresh() {
  if (!refreshTimer) return
  window.clearInterval(refreshTimer)
  refreshTimer = undefined
}

watch(autoRefresh, () => {
  startAutoRefresh()
})

onMounted(() => {
  applyQuickRange(quickRange.value)
  loadOverview()
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style scoped>
.dashboard-control {
  border: 1px solid rgba(45, 108, 223, 0.16);
  background: linear-gradient(135deg, rgba(45, 108, 223, 0.08), rgba(24, 144, 255, 0.02));
}

.action-board {
  border: 1px dashed rgba(24, 144, 255, 0.3);
}

.quick-label {
  color: rgba(0, 0, 0, 0.65);
  font-size: 12px;
}

.stat-link {
  cursor: pointer;
  border-radius: 8px;
  transition: background-color 0.2s ease;
  padding: 4px 6px;
}

.stat-link:hover {
  background-color: rgba(24, 144, 255, 0.08);
}
</style>
