<template>
  <PageContainer class="rm-page mk-ops" title="市场运营" sub-title="经营分析与营销方案共用一个主页面：先看转化与渠道，再定方案与预算。">
    <template #extra>
      <a-space wrap>
        <a-date-picker v-if="tab === 'analysis'" v-model:value="month" picker="month" :allow-clear="false" :disabled-date="futureMonth" @change="loadAnalysis" />
        <a-dropdown v-if="tab === 'analysis'" :trigger="['click']">
          <a-button><FileSearchOutlined />详细报表</a-button>
          <template #overlay>
            <a-menu @click="openReport">
              <a-menu-item v-for="r in reports" :key="r.path">{{ r.label }}</a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
        <a-button v-if="tab === 'analysis'" @click="exportAnalysis"><DownloadOutlined />导出分析</a-button>
      </a-space>
    </template>

    <a-tabs :active-key="tab" @change="switchTab">
      <a-tab-pane key="analysis"><template #tab><BarChartOutlined />经营分析</template></a-tab-pane>
      <a-tab-pane key="plans"><template #tab><SolutionOutlined />营销方案与审批</template></a-tab-pane>
    </a-tabs>

    <template v-if="tab === 'analysis'">
      <a-alert v-if="analysisError" type="error" show-icon :message="analysisError" style="margin-bottom: 16px" />
      <a-spin :spinning="loading">
        <section class="rm-kpis mk-kpis" :aria-label="`${range.label}经营指标`">
          <div v-for="card in kpis" :key="card.label" class="rm-kpi">
            <span class="rm-kpi__label">{{ card.label }}
              <a-tooltip :title="card.formula"><QuestionCircleOutlined class="mk-help" /></a-tooltip>
            </span>
            <strong class="rm-kpi__value">{{ card.value }}</strong>
            <span class="rm-kpi__hint">{{ card.hint }}</span>
          </div>
        </section>

        <div class="mk-grid">
          <section class="rm-card">
            <div class="rm-card__head">
              <h2 class="rm-card__title">{{ range.label }}获客转化漏斗</h2>
              <span class="rm-card__sub">点击阶段查看客户清单</span>
            </div>
            <button v-for="stage in stages" :key="stage.key" type="button" class="mk-funnel" @click="router.push({ path: stage.path, query: stage.query })">
              <span class="mk-funnel__label">{{ stage.label }}</span>
              <span class="mk-funnel__track"><span class="mk-funnel__fill" :style="{ width: `${Math.max(stage.width, 6)}%` }">{{ stage.count }} 人</span></span>
              <span class="mk-funnel__share">占线索 {{ stage.share }}</span>
            </button>
            <p class="rm-card__sub mk-foot">客户按线索登记日期归属当月去重统计；到访取参观预约中的已到访/已完成记录。整链路转化 {{ formatRate(report?.contractCount, report?.totalLeads) }}。</p>
          </section>

          <section class="rm-card">
            <div class="rm-card__head"><h2 class="rm-card__title">渠道获客效果</h2></div>
            <a-table size="small" row-key="source" :columns="channelColumns" :data-source="channels" :pagination="false" :scroll="{ x: 'max-content' }">
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'rate'">{{ formatRate(record.contractCount, record.leadCount) }}</template>
                <template v-else-if="column.key === 'source'">{{ marketingInfoSourceLabel(record.source) || record.source || '未标注' }}</template>
              </template>
            </a-table>
            <p class="rm-card__sub mk-foot">渠道签约率 = 该渠道签约客户 ÷ 该渠道新增线索；投入与 ROI 需关联营销活动的实际支出。</p>
          </section>

          <section class="rm-card">
            <div class="rm-card__head"><h2 class="rm-card__title">最近 6 个月签约趋势</h2></div>
            <TrendChart :labels="trend.map((m) => m.label)" :series="trendSeries" />
          </section>

          <section class="rm-card">
            <div class="rm-card__head"><h2 class="rm-card__title">市场人员协作与跟进质量</h2></div>
            <a-table size="small" row-key="name" :columns="staffColumns" :data-source="staffRows" :pagination="false" :scroll="{ x: 'max-content' }">
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'overdue'"><span :class="{ 'mk-danger': record.overdue > 0 }">{{ record.overdue }}</span></template>
                <template v-else-if="column.key === 'name'"><button type="button" class="rm-link" @click="router.push({ path: '/marketing/customers', query: { marketerName: record.name } })">{{ record.name }}</button></template>
              </template>
            </a-table>
            <p class="rm-card__sub mk-foot">同时看跟进及时与到访转化，不只按签约数考核个人；超期跟进 = 下次回访日早于今天且未回访。</p>
          </section>
        </div>
      </a-spin>
    </template>

    <template v-else>
      <section class="rm-kpis mk-kpis" aria-label="营销方案概况">
        <div class="rm-kpi"><span class="rm-kpi__label">本月营销活动</span><strong class="rm-kpi__value">{{ planStats.month }}</strong><span class="rm-kpi__hint">本月生效的方案</span></div>
        <div class="rm-kpi"><span class="rm-kpi__label">待审批方案</span><span class="rm-kpi__icon rm-kpi__icon--warning"><ClockCircleOutlined /></span><strong class="rm-kpi__value">{{ planStats.pending }}</strong><span class="rm-kpi__hint">提交后等待审批</span></div>
        <div class="rm-kpi"><span class="rm-kpi__label">本月计划预算</span><strong class="rm-kpi__value">¥{{ planStats.budget.toLocaleString() }}</strong><span class="rm-kpi__hint">本月生效方案的预算合计</span></div>
        <div class="rm-kpi"><span class="rm-kpi__label">执行中活动</span><strong class="rm-kpi__value">{{ planStats.active }}</strong><span class="rm-kpi__hint">已发布或执行中</span></div>
      </section>
      <div class="rm-split">
        <EmbeddedPage><MarketingPlan /></EmbeddedPage>
        <div class="mk-side">
          <section class="rm-card">
            <h2 class="rm-section-title">方案审批流程</h2>
            <a-steps direction="vertical" size="small" :current="-1" :items="approvalSteps" />
          </section>
          <section class="rm-card">
            <h2 class="rm-section-title">素材与发布规范</h2>
            <ul class="mk-rules">
              <li v-for="rule in publishRules" :key="rule"><CheckCircleOutlined />{{ rule }}</li>
            </ul>
          </section>
        </div>
      </div>
    </template>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import dayjs, { type Dayjs } from 'dayjs'
import {
  BarChartOutlined, CheckCircleOutlined, ClockCircleOutlined, DownloadOutlined, FileSearchOutlined,
  QuestionCircleOutlined, SolutionOutlined
} from '@ant-design/icons-vue'
import PageContainer from '../../../components/PageContainer.vue'
import EmbeddedPage from './EmbeddedPage.vue'
import TrendChart from './TrendChart.vue'
import MarketingPlan from '../MarketingPlan.vue'
import { getMarketingChannelReport, getMarketingConversionReport, getMarketingPlanPage, getMarketingStaffActivity, type MarketingStaffActivity } from '../../../api/marketing'
import { marketingInfoSourceLabel } from '../../../utils/marketingEnums'
import { formatRate, funnelStages, monthRange, recentMonths, toCsv } from './marketingMetrics'
import type { MarketingChannelReportItem, MarketingConversionReport, MarketingPlanItem } from '../../../types'
import '../../elder/residentUi.css'

type Tab = 'analysis' | 'plans'
const route = useRoute()
const router = useRouter()
const tab = computed<Tab>(() => (route.query.tab === 'plans' ? 'plans' : 'analysis'))
const month = ref<Dayjs>(dayjs())
const range = computed(() => monthRange(month.value))
const loading = ref(false)
const analysisError = ref('')
const report = ref<MarketingConversionReport | null>(null)
const visitCount = ref(0)
const channels = ref<MarketingChannelReportItem[]>([])
const trend = ref(recentMonths(6).map((m) => ({ ...m, contracts: 0, reservations: 0 })))
const staffRows = ref<Array<{ name: string; leads: number; visits: number; contracts: number; overdue: number }>>([])
const plans = ref<MarketingPlanItem[]>([])

const reports = [
  { label: '转化率统计', path: '/marketing/reports/conversion' }, { label: '咨询统计', path: '/marketing/reports/consultation' },
  { label: '跟进统计', path: '/marketing/reports/followup' }, { label: '回访统计', path: '/marketing/reports/callback' },
  { label: '渠道评估', path: '/marketing/reports/channel' }, { label: '渠道贡献排名', path: '/marketing/reports/channel-rank' },
  { label: '不明渠道统计', path: '/marketing/reports/unknown-channel' }, { label: '销售人员业绩', path: '/marketing/reports/sales-performance' },
  { label: '经营快照', path: '/marketing/reports/snapshots' }, { label: '回访质量评分', path: '/marketing/callback/score' }
]

const kpis = computed(() => {
  const r = report.value
  const v = visitCount.value
  return [
    { label: '新增线索', value: r ? r.totalLeads : '--', hint: range.value.label, formula: '统计期内登记的线索数（去重客户）' },
    { label: '有效意向率', value: formatRate(r?.intentCount, r?.totalLeads), hint: `${r?.intentCount ?? '--'} / ${r?.totalLeads ?? '--'}`, formula: '有效意向客户 ÷ 同口径新增线索' },
    { label: '意向客户到访率', value: formatRate(v, r?.intentCount), hint: `${v} / ${r?.intentCount ?? '--'}`, formula: '实际到访客户 ÷ 有效意向客户（到访取参观预约到访记录）' },
    { label: '到访后预订率', value: formatRate(r?.reservationCount, v), hint: `${r?.reservationCount ?? '--'} / ${v}`, formula: '床位预订客户 ÷ 实际到访客户' },
    { label: '线索签约率', value: formatRate(r?.contractCount, r?.totalLeads), hint: `${r?.contractCount ?? '--'} / ${r?.totalLeads ?? '--'}`, formula: '签约去重客户 ÷ 新增线索；回款以财务确认为准' }
  ]
})
const stages = computed(() => funnelStages({ ...report.value, visitCount: visitCount.value }))
const trendSeries = computed(() => [
  { name: '合同签约', color: '#21705f', values: trend.value.map((m) => m.contracts) },
  { name: '床位预订', color: '#6b9bd2', values: trend.value.map((m) => m.reservations) }
])
const channelColumns = [
  { title: '获客渠道', key: 'source', dataIndex: 'source' }, { title: '线索数', dataIndex: 'leadCount' },
  { title: '预订', dataIndex: 'reservationCount' }, { title: '签约', dataIndex: 'contractCount' }, { title: '渠道签约率', key: 'rate' }
]
const staffColumns = [
  { title: '负责人', key: 'name', dataIndex: 'name' }, { title: '有效线索', dataIndex: 'leads' }, { title: '到访', dataIndex: 'visits' },
  { title: '签约', dataIndex: 'contracts' }, { title: '超期跟进', key: 'overdue', dataIndex: 'overdue' }
]

const approvalSteps = [
  { title: '市场专员提交方案', description: '活动主题、受众、渠道、预算、素材与目标客户数' },
  { title: '市场负责人审核', description: '活动必要性、内容合规、排期与人员安排' },
  { title: '财务预算审核', description: '费用科目、预算额度与付款依据' },
  { title: '授权负责人审批并执行', description: '发布前核对宣传内容；执行后回填实际效果' }
]
const publishRules = [
  '宣传素材、活动预算均有版本与审批记录',
  '未经授权不得发布长者照片、视频及私人信息',
  '宣传价格需与已审批的收费标准一致',
  '审核通过后执行，执行后回填真实效果'
]
const planStats = computed(() => {
  const inMonth = plans.value.filter((p) => p.effectiveDate && dayjs(p.effectiveDate).isSame(dayjs(), 'month'))
  return {
    month: inMonth.length,
    pending: plans.value.filter((p) => p.status === 'PENDING_APPROVAL').length,
    budget: inMonth.reduce((sum, p) => sum + Number(p.budgetAmount || 0), 0),
    active: plans.value.filter((p) => p.status === 'ACTIVE' || p.status === 'PUBLISHED').length
  }
})

const silent = { silent403: true }
const futureMonth = (d: Dayjs) => d.isAfter(dayjs(), 'month')

function switchTab(key: string | number) {
  router.replace({ path: route.path, query: { ...route.query, tab: String(key) } })
}

function openReport({ key }: { key: string | number }) {
  router.push(String(key))
}

async function loadAnalysis() {
  loading.value = true
  analysisError.value = ''
  const { dateFrom, dateTo } = range.value
  const months = recentMonths(6, month.value)
  try {
    const [conv, activity, chan, ...trendResults] = await Promise.allSettled([
      getMarketingConversionReport({ dateFrom, dateTo }),
      getMarketingStaffActivity({ dateFrom, dateTo }, silent),
      getMarketingChannelReport({ dateFrom, dateTo }),
      ...months.map((m) => getMarketingConversionReport({ dateFrom: m.dateFrom, dateTo: m.dateTo }, silent))
    ])
    if (conv.status === 'rejected') throw conv.reason
    report.value = conv.value as unknown as MarketingConversionReport
    const stats = activity.status === 'fulfilled' ? activity.value as unknown as MarketingStaffActivity : null
    visitCount.value = Number(stats?.arrivedLeadCount || 0)
    channels.value = chan.status === 'fulfilled' ? (chan.value as unknown as MarketingChannelReportItem[]) || [] : []
    trend.value = months.map((m, i) => {
      const r = trendResults[i]
      const value = r.status === 'fulfilled' ? r.value as unknown as MarketingConversionReport : null
      return { ...m, contracts: Number(value?.contractCount || 0), reservations: Number(value?.reservationCount || 0) }
    })
    staffRows.value = stats?.staff || []
  } catch (error: any) {
    report.value = null
    analysisError.value = Number(error?.response?.status) === 403 ? '当前账号无权查看经营分析' : (error?.message || '经营分析加载失败，请重试')
  } finally {
    loading.value = false
  }
}

async function loadPlans() {
  try {
    const page = await getMarketingPlanPage({ pageNo: 1, pageSize: 200 })
    plans.value = (page as PageResult<MarketingPlanItem>).list || []
  } catch {
    plans.value = []
  }
}

function exportAnalysis() {
  const csv = toCsv(['分类', '名称', '线索/有效线索', '到访/预订', '签约', '比率/超期跟进'], [
    ...stages.value.map((s) => ['转化漏斗', s.label, s.count, '', '', s.share]),
    ...channels.value.map((c) => ['渠道', marketingInfoSourceLabel(c.source) || c.source, c.leadCount, c.reservationCount, c.contractCount, formatRate(c.contractCount, c.leadCount)]),
    ...staffRows.value.map((s) => ['人员', s.name, s.leads, s.visits, s.contracts, s.overdue])
  ])
  const link = document.createElement('a')
  link.href = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8' }))
  link.download = `市场经营分析-${range.value.month}.csv`
  link.click()
  URL.revokeObjectURL(link.href)
}

watch(tab, (value) => { if (value === 'plans') loadPlans(); else if (!report.value) loadAnalysis() })
onMounted(() => (tab.value === 'plans' ? loadPlans() : loadAnalysis()))
</script>

<style scoped>
.mk-kpis { grid-template-columns: repeat(auto-fit, minmax(170px, 1fr)); }
.mk-help { margin-left: 4px; color: #a3b1ab; cursor: help; }
.mk-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; }
.mk-funnel { display: grid; grid-template-columns: 72px 1fr 96px; align-items: center; gap: 12px; width: 100%; padding: 6px 0; border: 0; background: none; font: inherit; font-size: 13px; color: var(--rm-text); text-align: left; cursor: pointer; }
.mk-funnel__track { min-width: 0; }
.mk-funnel__fill { display: block; padding: 3px 8px; border-radius: 4px; background: linear-gradient(90deg, #2f8a70, #8cc7a8); color: #fff; font-size: 12px; font-weight: 600; white-space: nowrap; }
.mk-funnel:hover .mk-funnel__fill { background: var(--rm-green); }
.mk-funnel__share { color: var(--rm-muted); font-size: 12px; }
.mk-foot { margin: 10px 0 0; }
.mk-danger { color: var(--rm-danger); font-weight: 600; }
.mk-side { display: flex; flex-direction: column; gap: 16px; }
.mk-rules { margin: 0; padding: 0; list-style: none; }
.mk-rules li { display: flex; gap: 8px; padding: 6px 0; font-size: 13px; color: var(--rm-text); }
.mk-rules .anticon { margin-top: 3px; color: var(--rm-green); }
@media (max-width: 1100px) { .mk-grid { grid-template-columns: minmax(0, 1fr); } }
</style>
