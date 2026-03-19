<template>
  <PageContainer title="月运营收入统计" subTitle="按月份查看运营收入">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline">
        <a-form-item label="起始月份">
          <a-date-picker v-model:value="query.from" picker="month" style="width: 160px" />
        </a-form-item>
        <a-form-item label="结束月份">
          <a-date-picker v-model:value="query.to" picker="month" style="width: 160px" />
        </a-form-item>
        <a-form-item label="机构ID">
          <a-input-number v-model:value="query.orgId" :min="1" placeholder="默认当前机构" style="width: 160px" />
        </a-form-item>
        <a-form-item label="打印备注">
          <a-input v-model:value="query.printRemark" allow-clear placeholder="例如：财务复核版" style="width: 200px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="loadData">刷新</a-button>
            <a-button @click="exportTrend">导出趋势</a-button>
            <a-button @click="printReport">打印报告</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-row :gutter="16" style="margin-top: 16px;">
      <a-col :span="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="期间总收入" :value="stats.totalRevenue || 0" :precision="2" />
          <a-button type="link" size="small" @click="openMetricDetail('total')">查看明细</a-button>
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="月均收入" :value="stats.averageMonthlyRevenue || 0" :precision="2" />
          <a-button type="link" size="small" @click="openMetricDetail('average')">查看明细</a-button>
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="最近环比(%)" :value="stats.revenueGrowthRate || 0" :precision="2" />
          <a-button type="link" size="small" @click="openMetricDetail('growth')">查看明细</a-button>
        </a-card>
      </a-col>
    </a-row>

    <StatsWorkspacePanel
      page-key="stats-monthly-revenue"
      title="收入策略区"
      :summary-text="workspaceSummary"
      :current-payload="workspacePayload"
      :target-fields="targetFields"
      :data-health="dataHealth"
      :empty-state="emptyState"
      @apply-preset="applyPreset"
      @targets-change="onTargetsChange"
    />

    <StatsInsightDeck :items="visibleInsightItems" />

    <StatsCommandCenter
      page-key="stats-monthly-revenue-command"
      title="收入协同台"
      share-title="月运营收入协同包"
      :summary-text="commandSummary"
      :current-payload="workspacePayload"
      :action-items="commandActionItems"
      :anomalies="commandAnomalies"
      :metric-notes="metricNotes"
      :panel-options="panelOptions"
      :selected-panel-keys="panelKeys"
      @trigger-action="onCommandAction"
      @panel-change="onPanelChange"
      @apply-template="applyReportTemplate"
    />

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="收入趋势">
      <div ref="lineRef" style="height: 320px;"></div>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="收入明细">
      <vxe-table border stripe show-overflow :data="stats.monthlyRevenue || []" height="320">
        <vxe-column field="month" title="月份" width="140" />
        <vxe-column field="amount" title="收入金额" width="180" />
      </vxe-table>
    </a-card>

    <a-modal v-model:open="metricOpen" :title="metricTitle" @ok="metricOpen = false" cancel-text="关闭" ok-text="确定" width="680">
      <vxe-table border stripe show-overflow :data="metricRows" height="360">
        <vxe-column field="month" title="月份" width="160" />
        <vxe-column field="amount" title="收入金额" width="180" />
        <vxe-column field="tag" title="标签" min-width="180" />
      </vxe-table>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs, { type Dayjs } from 'dayjs'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import StatsWorkspacePanel from '../../components/stats/StatsWorkspacePanel.vue'
import StatsInsightDeck from '../../components/stats/StatsInsightDeck.vue'
import StatsCommandCenter from '../../components/stats/StatsCommandCenter.vue'
import { exportMonthlyRevenueStatsCsv, getMonthlyRevenueStats } from '../../api/stats'
import type { MonthlyRevenueStatsResponse } from '../../types'
import { useECharts } from '../../plugins/echarts'
import { message } from 'ant-design-vue'
import { printTableReport } from '../../utils/print'
import { buildComparisonSummary, buildPeriodSeries } from '../../utils/statsInsight'

const router = useRouter()
const query = reactive({
  from: dayjs().subtract(5, 'month') as Dayjs,
  to: dayjs() as Dayjs,
  orgId: undefined as number | undefined,
  printRemark: ''
})
const stats = reactive<MonthlyRevenueStatsResponse>({
  totalRevenue: 0,
  averageMonthlyRevenue: 0,
  revenueGrowthRate: 0,
  monthlyRevenue: []
})
const { chartRef: lineRef, setOption } = useECharts()
const targets = ref<Record<string, number>>({
  totalRevenue: 0,
  averageMonthlyRevenue: 0,
  growthFloor: 0
})
const panelKeys = ref<string[]>([])
const metricOpen = ref(false)
const metricMode = ref<'total' | 'average' | 'growth'>('total')
const metricTitle = computed(() => {
  if (metricMode.value === 'average') return '月均收入明细'
  if (metricMode.value === 'growth') return '最近环比明细'
  return '期间总收入明细'
})
const metricRows = computed(() => {
  const list = (stats.monthlyRevenue || []).map(item => ({ month: item.month, amount: item.amount, tag: '' }))
  if (metricMode.value === 'average') {
    return list.map(item => ({ ...item, tag: Number(item.amount || 0) >= Number(stats.averageMonthlyRevenue || 0) ? '高于月均' : '低于月均' }))
  }
  if (metricMode.value === 'growth') {
    return list.map((item, index) => {
      if (index === 0) return { ...item, tag: '基准月' }
      const prev = Number(list[index - 1].amount || 0)
      const cur = Number(item.amount || 0)
      const growth = prev > 0 ? ((cur - prev) / prev) * 100 : 0
      return { ...item, tag: `环比 ${growth.toFixed(2)}%` }
    })
  }
  return list.map(item => ({ ...item, tag: '期间收入' }))
})
const workspaceSummary = computed(() => `将总收入、月均收入与最近环比联动观察，并可保存常用月份方案。当前共有 ${(stats.monthlyRevenue || []).length} 个月份样本。`)
const workspacePayload = computed(() => ({
  from: dayjs(query.from).format('YYYY-MM'),
  to: dayjs(query.to).format('YYYY-MM'),
  orgId: query.orgId ? String(query.orgId) : ''
}))
const targetFields = computed(() => ([
  { key: 'totalRevenue', label: '期间总收入目标(元)', value: targets.value.totalRevenue, min: 0, max: 999999999, step: 1000 },
  { key: 'averageMonthlyRevenue', label: '月均收入目标(元)', value: targets.value.averageMonthlyRevenue, min: 0, max: 999999999, step: 1000 },
  { key: 'growthFloor', label: '最近环比底线(%)', value: targets.value.growthFloor, min: -100, max: 1000, step: 1 }
]))
const dataHealth = computed(() => ({
  freshness: dayjs().format('YYYY-MM-DD HH:mm:ss'),
  completeness: `${(stats.monthlyRevenue || []).length} 月样本`,
  issues: [
    Number(stats.revenueGrowthRate || 0) < Number(targets.value.growthFloor || 0) ? '最近环比低于底线' : ''
  ].filter(Boolean)
}))
const emptyState = computed(() => ({
  visible: !(stats.monthlyRevenue || []).length,
  title: '当前窗口没有收入样本',
  description: '建议拉长月份范围或确认账单月结是否完成。',
  hints: [
    '优先检查账单月数据是否已生成',
    '若是新机构，可先切换到近12个月确认累计趋势',
    '对照看板总收入口径，确认当前筛选与看板是否一致'
  ]
}))
const insightItems = computed(() => {
  const revenueSeries = buildPeriodSeries(stats.monthlyRevenue || [], 'month', 'amount')
  const revenueSummary = buildComparisonSummary(revenueSeries, targets.value.totalRevenue)
  const averageGap = Number(stats.averageMonthlyRevenue || 0) - Number(targets.value.averageMonthlyRevenue || 0)
  return [
    {
      key: 'total',
      label: '期间总收入',
      valueText: `${Number(stats.totalRevenue || 0).toFixed(2)} 元`,
      trendText: revenueSummary.target != null ? `目标差 ${Number(revenueSummary.targetGap || 0).toFixed(2)} 元` : '等待配置目标',
      detail: revenueSummary.anomalyText,
      tone: revenueSummary.targetGap != null && revenueSummary.targetGap < 0 ? 'warning' : (revenueSummary.anomalyLevel || 'good')
    },
    {
      key: 'average',
      label: '月均收入',
      valueText: `${Number(stats.averageMonthlyRevenue || 0).toFixed(2)} 元`,
      trendText: `目标差 ${averageGap >= 0 ? '+' : ''}${averageGap.toFixed(2)} 元`,
      detail: averageGap < 0 ? '月均收入尚未达到目标' : '月均收入已达到目标',
      tone: averageGap < 0 ? 'warning' : 'good'
    },
    {
      key: 'growth',
      label: '最近环比',
      valueText: `${Number(stats.revenueGrowthRate || 0).toFixed(2)}%`,
      trendText: `底线 ${Number(targets.value.growthFloor || 0).toFixed(2)}%`,
      detail: revenueSummary.yoyRate == null ? '同比待累积' : `同比 ${revenueSummary.yoyRate >= 0 ? '+' : ''}${revenueSummary.yoyRate}%`,
      tone: Number(stats.revenueGrowthRate || 0) < Number(targets.value.growthFloor || 0) ? 'danger' : 'good'
    },
    {
      key: 'latest',
      label: '最新月份收入',
      valueText: `${Number(revenueSummary.latestValue || 0).toFixed(2)} 元`,
      trendText: revenueSummary.momRate == null ? '暂无环比' : `较上月 ${revenueSummary.momRate >= 0 ? '+' : ''}${revenueSummary.momRate}%`,
      detail: `样本期 ${(stats.monthlyRevenue || []).slice(-1)[0]?.month || '--'}`,
      tone: 'neutral'
    }
  ]
})
const visibleInsightItems = computed(() => {
  const selected = panelKeys.value.length ? new Set(panelKeys.value) : null
  return insightItems.value.filter((item) => !selected || selected.has(item.key))
})
const commandSummary = computed(() => '把收入异常、跨页钻取和模板化报表入口合在一起，适合财务和经营复盘。')
const commandActionItems = computed(() => ([
  { key: 'open-org-operation', label: '查看机构经营', description: '联动机构月运营横向对比', route: '/stats/org/monthly-operation', tone: 'danger' as const },
  { key: 'open-consumption', label: '查看消费结构', description: '核对消费与收入同步关系', route: '/stats/consumption', tone: 'warning' as const },
  { key: 'open-dashboard', label: '返回看板', description: '回到经营总览', route: '/dashboard', tone: 'neutral' as const }
]))
const commandAnomalies = computed(() =>
  insightItems.value
    .filter((item) => item.tone === 'danger' || item.tone === 'warning')
    .map((item) => ({ key: item.key, label: item.label, detail: item.detail, tone: item.tone }))
)
const metricNotes = computed(() => ([
  { key: 'total', label: '期间总收入', note: '按当前月份窗口累计账单金额，不含窗口外账单。' },
  { key: 'average', label: '月均收入', note: '月均收入=期间总收入/样本月份数。' },
  { key: 'growth', label: '最近环比', note: '最近环比比较最后两个月收入变化，适合联动异常判断。' }
]))
const panelOptions = computed(() =>
  insightItems.value.map((item) => ({
    key: item.key,
    label: item.label,
    description: item.detail || item.trendText
  }))
)

async function loadData() {
  if (query.from.isAfter(query.to, 'month')) {
    message.warning('起始月份不能晚于结束月份')
    return
  }
  try {
    const data = await getMonthlyRevenueStats({
      from: dayjs(query.from).format('YYYY-MM'),
      to: dayjs(query.to).format('YYYY-MM'),
      orgId: query.orgId
    })
    Object.assign(stats, data)
    setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: data.monthlyRevenue.map(item => item.month) },
      yAxis: { type: 'value' },
      series: [{ name: '收入', type: 'line', smooth: true, data: data.monthlyRevenue.map(item => item.amount) }]
    })
  } catch (error: any) {
    message.error(error?.message || '加载收入统计失败')
  }
}

function reset() {
  query.from = dayjs().subtract(5, 'month')
  query.to = dayjs()
  query.orgId = undefined
  query.printRemark = ''
  loadData()
}

function applyPreset(payload: Record<string, any>) {
  if (payload.from && dayjs(String(payload.from)).isValid()) query.from = dayjs(String(payload.from))
  if (payload.to && dayjs(String(payload.to)).isValid()) query.to = dayjs(String(payload.to))
  query.orgId = payload.orgId ? Number(payload.orgId) : undefined
  loadData()
}

function onTargetsChange(payload: Record<string, number>) {
  targets.value = {
    ...targets.value,
    ...(payload || {})
  }
}

function onPanelChange(keys: string[]) {
  panelKeys.value = keys.length ? [...keys] : insightItems.value.map((item) => item.key)
}

function onCommandAction(item: { route?: string }) {
  if (item.route) {
    router.push(item.route)
  }
}

function applyReportTemplate(payload: { payload: Record<string, any> }) {
  applyPreset(payload.payload || {})
}

async function exportTrend() {
  try {
    await exportMonthlyRevenueStatsCsv({
      from: dayjs(query.from).format('YYYY-MM'),
      to: dayjs(query.to).format('YYYY-MM'),
      orgId: query.orgId
    })
    message.success('导出成功')
  } catch (error: any) {
    message.error(error?.message || '导出失败')
  }
}

function openMetricDetail(mode: 'total' | 'average' | 'growth') {
  metricMode.value = mode
  metricOpen.value = true
}

function printReport() {
  try {
    const orgText = query.orgId ? `机构ID：${query.orgId}` : '机构：当前'
    printTableReport({
      title: '月运营收入统计',
      subtitle: `${dayjs(query.from).format('YYYY-MM')} ~ ${dayjs(query.to).format('YYYY-MM')}；${orgText}；备注：${query.printRemark || '-'}；期间总收入：${stats.totalRevenue || 0}；月均收入：${stats.averageMonthlyRevenue || 0}；最近环比：${stats.revenueGrowthRate || 0}%`,
      columns: [
        { key: 'month', title: '月份' },
        { key: 'amount', title: '收入金额' }
      ],
      rows: (stats.monthlyRevenue || []).map(item => ({
        month: item.month,
        amount: item.amount
      }))
    })
  } catch (error: any) {
    message.error(error?.message || '打印失败')
  }
}

onMounted(loadData)
</script>
