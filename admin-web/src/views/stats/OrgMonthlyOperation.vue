<template>
  <PageContainer title="机构月运营详情" subTitle="先看经营结论，再做横向比较和下钻明细">
    <template #stats>
      <div class="headline-stats">
        <div class="headline-stat">
          <span>记录条数</span>
          <strong>{{ displayRows.length }}</strong>
        </div>
        <div class="headline-stat">
          <span>最高营收机构</span>
          <strong>{{ topRevenueOrgs[0]?.orgName || '-' }}</strong>
        </div>
        <div class="headline-stat">
          <span>最高床位压力</span>
          <strong>{{ highPressureOrgs[0]?.orgName || '-' }}</strong>
        </div>
      </div>
    </template>

    <section class="stats-toolbar-shell">
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
        <a-form-item label="机构名称">
          <a-input v-model:value="query.orgNameKeyword" allow-clear placeholder="打印筛选" style="width: 160px" />
        </a-form-item>
        <a-form-item label="打印备注">
          <a-input v-model:value="query.printRemark" allow-clear placeholder="例如：运营周会版" style="width: 200px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="loadData">刷新</a-button>
            <a-button @click="exportCsvReport">导出报表</a-button>
            <a-button @click="openColumnSetting">列设置</a-button>
            <a-button @click="printCurrent">打印当前列</a-button>
            <a-button @click="printSpecificOrg">打印指定机构</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </section>

    <StatsWorkspacePanel
      page-key="stats-org-monthly-operation"
      title="机构运营策略区"
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
      page-key="stats-org-monthly-operation-command"
      title="机构经营协同台"
      share-title="机构月运营协同包"
      :summary-text="commandSummary"
      :current-payload="workspacePayload"
      :action-items="commandActionItems"
      :anomalies="commandAnomalies"
      :metric-notes="metricNotes"
      :panel-options="panelOptions"
      :selected-panel-keys="panelKeys"
      :template-column-options="printColumnOptions"
      :selected-template-columns="selectedPrintColumns"
      @trigger-action="onCommandAction"
      @panel-change="onPanelChange"
      @apply-template="applyReportTemplate"
    />

    <section class="stats-story-grid">
      <div class="story-panel">
        <div class="story-head">
          <div>
            <div class="story-kicker">Summary</div>
            <h3>机构营收对比</h3>
          </div>
        </div>
          <div class="compare-list" v-if="topRevenueOrgs.length">
            <div v-for="item in topRevenueOrgs" :key="`revenue-${item.orgName}`" class="compare-item">
              <div>
                <div class="compare-name">{{ item.orgName }}</div>
                <div class="compare-meta">累计入住 {{ item.admissions }} · 累计离院 {{ item.discharges }}</div>
              </div>
              <div class="compare-value">{{ item.revenue.toFixed(2) }} 元</div>
            </div>
          </div>
          <a-empty v-else description="暂无机构对比数据" />
      </div>
      <div class="story-panel">
        <div class="story-head">
          <div>
            <div class="story-kicker">Pressure</div>
            <h3>容量压力机构</h3>
          </div>
        </div>
          <div class="compare-list" v-if="highPressureOrgs.length">
            <div v-for="item in highPressureOrgs" :key="`pressure-${item.orgName}`" class="compare-item">
              <div>
                <div class="compare-name">{{ item.orgName }}</div>
                <div class="compare-meta">总床位 {{ item.totalBeds }} · 已使用 {{ item.occupiedBeds }}</div>
              </div>
              <div class="compare-value">{{ item.occupancyRate.toFixed(2) }}%</div>
            </div>
          </div>
          <a-empty v-else description="暂无床位压力对比" />
      </div>
    </section>

    <section class="story-panel story-panel--chart">
      <div class="story-head">
        <div>
          <div class="story-kicker">Trend</div>
          <h3>机构月运营趋势</h3>
        </div>
      </div>
      <div ref="trendRef" style="height: 320px;"></div>
    </section>

    <section class="story-panel story-panel--table">
      <div class="story-head">
        <div>
          <div class="story-kicker">Detail</div>
          <h3>机构月运营明细</h3>
        </div>
      </div>
      <vxe-table border stripe show-overflow :data="displayRows" height="320">
        <vxe-column field="orgName" title="机构" min-width="140" />
        <vxe-column field="month" title="月份" width="120" />
        <vxe-column field="admissions" title="入住" width="100" />
        <vxe-column field="discharges" title="离院" width="100" />
        <vxe-column field="revenue" title="营收" width="150" />
        <vxe-column field="occupiedBeds" title="已使用床位" width="120" />
        <vxe-column field="totalBeds" title="总床位" width="100" />
        <vxe-column field="occupancyRate" title="床位使用率(%)" width="120" />
      </vxe-table>
    </section>

    <a-modal v-model:open="columnSettingOpen" title="打印列设置" @ok="columnSettingOpen = false" cancel-text="关闭" ok-text="确定">
      <a-checkbox-group v-model:value="selectedPrintColumns" :options="printColumnOptions" />
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import dayjs, { type Dayjs } from 'dayjs'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import StatsWorkspacePanel from '../../components/stats/StatsWorkspacePanel.vue'
import StatsInsightDeck from '../../components/stats/StatsInsightDeck.vue'
import StatsCommandCenter from '../../components/stats/StatsCommandCenter.vue'
import { exportOrgMonthlyOperationCsv, getOrgMonthlyOperation } from '../../api/stats'
import type { OrgMonthlyOperationItem } from '../../types'
import { useECharts } from '../../plugins/echarts'
import { message } from 'ant-design-vue'
import { printTableReport } from '../../utils/print'
import { buildComparisonSummary, buildPeriodSeries } from '../../utils/statsInsight'

const router = useRouter()
const query = ref({
  from: dayjs().subtract(5, 'month') as Dayjs,
  to: dayjs() as Dayjs,
  orgId: undefined as number | undefined,
  orgNameKeyword: '',
  printRemark: ''
})
const rows = ref<OrgMonthlyOperationItem[]>([])
const { chartRef: trendRef, setOption } = useECharts()
const targets = ref<Record<string, number>>({
  revenue: 0,
  occupancyRate: 90
})
const columnSettingOpen = ref(false)
const printColumnOptions = [
  { label: '机构', value: 'orgName' },
  { label: '月份', value: 'month' },
  { label: '入住', value: 'admissions' },
  { label: '离院', value: 'discharges' },
  { label: '营收', value: 'revenue' },
  { label: '已使用床位', value: 'occupiedBeds' },
  { label: '总床位', value: 'totalBeds' },
  { label: '床位使用率(%)', value: 'occupancyRate' }
]
const selectedPrintColumns = ref<string[]>(['orgName', 'month', 'admissions', 'discharges', 'revenue', 'occupiedBeds', 'totalBeds', 'occupancyRate'])
const panelKeys = ref<string[]>([])
const printableRows = computed(() => (rows.value || []).map(item => ({
  orgName: item.orgName || `机构#${item.orgId || '-'}`,
  month: item.month,
  admissions: item.admissions,
  discharges: item.discharges,
  revenue: item.revenue,
  occupiedBeds: item.occupiedBeds,
  totalBeds: item.totalBeds,
  occupancyRate: item.occupancyRate
})))
const displayRows = computed(() => {
  const keyword = String(query.value.orgNameKeyword || '').trim()
  if (!keyword) return rows.value
  return rows.value.filter(item => String(item.orgName || '').includes(keyword))
})
const workspaceSummary = computed(() => `围绕营收、床位占用和入住离院联动观察机构月经营表现。当前展示 ${displayRows.value.length} 条记录。`)
const workspacePayload = computed(() => ({
  from: dayjs(query.value.from).format('YYYY-MM'),
  to: dayjs(query.value.to).format('YYYY-MM'),
  orgId: query.value.orgId ? String(query.value.orgId) : '',
  orgNameKeyword: query.value.orgNameKeyword || ''
}))
const targetFields = computed(() => ([
  { key: 'revenue', label: '月营收目标(元)', value: targets.value.revenue, min: 0, max: 999999999, step: 1000 },
  { key: 'occupancyRate', label: '床位使用率目标(%)', value: targets.value.occupancyRate, min: 0, max: 100, step: 1 }
]))
const dataHealth = computed(() => ({
  freshness: dayjs().format('YYYY-MM-DD HH:mm:ss'),
  completeness: `${displayRows.value.length}/${rows.value.length || 0} 条记录可视`,
  issues: [
    query.value.orgNameKeyword ? `机构名称关键字：${query.value.orgNameKeyword}` : ''
  ].filter(Boolean)
}))
const emptyState = computed(() => ({
  visible: !displayRows.value.length,
  title: '当前筛选下没有机构运营数据',
  description: '建议清空机构名称关键字或扩大月份范围。',
  hints: [
    '机构月运营明细适合做横向比对和经营周报',
    '若只看单机构，建议同时对照床位占用与营收目标',
    '可以保存成常用周会方案'
  ]
}))
const insightItems = computed(() => {
  const revenueSeries = buildPeriodSeries(displayRows.value, 'month', 'revenue')
  const occupancySeries = buildPeriodSeries(displayRows.value, 'month', 'occupancyRate')
  const revenueSummary = buildComparisonSummary(revenueSeries, targets.value.revenue)
  const occupancySummary = buildComparisonSummary(occupancySeries, targets.value.occupancyRate)
  return [
    {
      key: 'revenue',
      label: '营收走势',
      valueText: `${Number(revenueSummary.latestValue || 0).toFixed(2)} 元`,
      trendText: revenueSummary.momRate == null ? '暂无环比' : `较上期 ${revenueSummary.momRate >= 0 ? '+' : ''}${revenueSummary.momRate}%`,
      detail: revenueSummary.target != null ? `目标差 ${Number(revenueSummary.targetGap || 0).toFixed(2)} 元` : revenueSummary.anomalyText,
      tone: revenueSummary.targetGap != null && revenueSummary.targetGap < 0 ? 'warning' : (revenueSummary.anomalyLevel || 'good')
    },
    {
      key: 'occupancy',
      label: '床位使用率',
      valueText: `${Number(occupancySummary.latestValue || 0).toFixed(2)}%`,
      trendText: `目标 ${Number(targets.value.occupancyRate || 0).toFixed(2)}%`,
      detail: occupancySummary.anomalyText,
      tone: Number(occupancySummary.latestValue || 0) > Number(targets.value.occupancyRate || 0) ? 'warning' : 'good'
    },
    {
      key: 'admission',
      label: '入住动能',
      valueText: `${Number(displayRows.value.reduce((sum, item) => sum + Number(item.admissions || 0), 0))}`,
      trendText: `离院 ${Number(displayRows.value.reduce((sum, item) => sum + Number(item.discharges || 0), 0))}`,
      detail: '适合结合营收一起判断经营节奏',
      tone: 'neutral'
    },
    {
      key: 'rows',
      label: '机构样本',
      valueText: `${displayRows.value.length}`,
      trendText: query.value.orgId ? `机构ID ${query.value.orgId}` : '当前机构视角',
      detail: '如需周会使用，建议保存当前机构与月份组合',
      tone: 'neutral'
    }
  ]
})
const visibleInsightItems = computed(() => {
  const selected = panelKeys.value.length ? new Set(panelKeys.value) : null
  return insightItems.value.filter((item) => !selected || selected.has(item.key))
})
const commandSummary = computed(() => '把机构横向经营对比、模板化报表和跨页钻取收口到经营协同台。')
const commandActionItems = computed(() => ([
  { key: 'open-revenue', label: '查看收入趋势', description: '回到月收入统计核对整体趋势', route: '/stats/monthly-revenue', tone: 'danger' as const },
  { key: 'open-bed-usage', label: '查看床位使用', description: '联动床位容量判断经营压力', route: '/stats/org/bed-usage', tone: 'warning' as const },
  { key: 'open-org-flow', label: '查看机构出入', description: '联动入住离院波动', route: '/stats/org/elder-flow', tone: 'neutral' as const }
]))
const commandAnomalies = computed(() =>
  insightItems.value
    .filter((item) => item.tone === 'danger' || item.tone === 'warning')
    .map((item) => ({ key: item.key, label: item.label, detail: item.detail, tone: item.tone }))
)
const metricNotes = computed(() => ([
  { key: 'revenue', label: '营收走势', note: '可结合机构维度横向对比找出强弱机构。' },
  { key: 'occupancy', label: '床位使用率', note: '床位使用率偏高时建议继续联动床位和出入统计。' },
  { key: 'admission', label: '入住动能', note: '入住与离院一起看，才能判断经营增长是否健康。' }
]))
const panelOptions = computed(() =>
  insightItems.value.map((item) => ({
    key: item.key,
    label: item.label,
    description: item.detail || item.trendText
  }))
)
const orgCompareRows = computed(() => {
  const map = new Map<string, {
    orgName: string
    revenue: number
    admissions: number
    discharges: number
    occupiedBeds: number
    totalBeds: number
    occupancyRate: number
  }>()
  displayRows.value.forEach((item) => {
    const orgName = String(item.orgName || `机构#${item.orgId || '-'}`)
    const current = map.get(orgName) || {
      orgName,
      revenue: 0,
      admissions: 0,
      discharges: 0,
      occupiedBeds: 0,
      totalBeds: 0,
      occupancyRate: 0
    }
    current.revenue += Number(item.revenue || 0)
    current.admissions += Number(item.admissions || 0)
    current.discharges += Number(item.discharges || 0)
    current.occupiedBeds = Math.max(current.occupiedBeds, Number(item.occupiedBeds || 0))
    current.totalBeds = Math.max(current.totalBeds, Number(item.totalBeds || 0))
    current.occupancyRate = Math.max(current.occupancyRate, Number(item.occupancyRate || 0))
    map.set(orgName, current)
  })
  return Array.from(map.values())
})
const topRevenueOrgs = computed(() =>
  [...orgCompareRows.value]
    .sort((left, right) => right.revenue - left.revenue)
    .slice(0, 3)
)
const highPressureOrgs = computed(() =>
  [...orgCompareRows.value]
    .sort((left, right) => right.occupancyRate - left.occupancyRate)
    .slice(0, 3)
)

async function loadData() {
  if (query.value.from.isAfter(query.value.to, 'month')) {
    message.warning('起始月份不能晚于结束月份')
    return
  }
  try {
    rows.value = await getOrgMonthlyOperation({
      from: dayjs(query.value.from).format('YYYY-MM'),
      to: dayjs(query.value.to).format('YYYY-MM'),
      orgId: query.value.orgId
    })
    setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['入住', '离院', '营收'] },
      xAxis: { type: 'category', data: rows.value.map(item => item.month) },
      yAxis: [{ type: 'value' }, { type: 'value' }],
      series: [
        { name: '入住', type: 'bar', data: rows.value.map(item => item.admissions) },
        { name: '离院', type: 'bar', data: rows.value.map(item => item.discharges) },
        { name: '营收', type: 'line', yAxisIndex: 1, smooth: true, data: rows.value.map(item => item.revenue) }
      ]
    })
  } catch (error: any) {
    message.error(error?.message || '加载机构月运营统计失败')
  }
}

async function exportCsvReport() {
  if (query.value.from.isAfter(query.value.to, 'month')) {
    message.warning('起始月份不能晚于结束月份')
    return
  }
  try {
    await exportOrgMonthlyOperationCsv({
      from: dayjs(query.value.from).format('YYYY-MM'),
      to: dayjs(query.value.to).format('YYYY-MM'),
      orgId: query.value.orgId
    })
    message.success('报表导出成功')
  } catch (error: any) {
    message.error(error?.message || '报表导出失败')
  }
}

function reset() {
  query.value.from = dayjs().subtract(5, 'month')
  query.value.to = dayjs()
  query.value.orgId = undefined
  query.value.orgNameKeyword = ''
  query.value.printRemark = ''
  loadData()
}

function applyPreset(payload: Record<string, any>) {
  if (payload.from && dayjs(String(payload.from)).isValid()) query.value.from = dayjs(String(payload.from))
  if (payload.to && dayjs(String(payload.to)).isValid()) query.value.to = dayjs(String(payload.to))
  query.value.orgId = payload.orgId ? Number(payload.orgId) : undefined
  query.value.orgNameKeyword = String(payload.orgNameKeyword || '')
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

function applyReportTemplate(payload: { payload: Record<string, any>; columns: string[] }) {
  if (payload.columns?.length) {
    selectedPrintColumns.value = [...payload.columns]
  }
  applyPreset(payload.payload || {})
}

function openColumnSetting() {
  columnSettingOpen.value = true
}

function printCurrent() {
  renderPrint('机构月运营详情（当前结果）', printableRows.value.filter(item => {
    const keyword = String(query.value.orgNameKeyword || '').trim()
    return !keyword || item.orgName.includes(keyword)
  }))
}

function printSpecificOrg() {
  const keyword = String(query.value.orgNameKeyword || '').trim()
  if (!keyword) {
    message.warning('请输入机构名称关键字')
    return
  }
  const filtered = printableRows.value.filter(item => item.orgName.includes(keyword))
  if (!filtered.length) {
    message.warning('未找到匹配机构记录')
    return
  }
  renderPrint(`机构月运营详情（${keyword}）`, filtered)
}

function renderPrint(title: string, data: Array<Record<string, any>>) {
  if (!selectedPrintColumns.value.length) {
    message.warning('请至少选择一列打印')
    return
  }
  try {
    printTableReport({
      title,
      subtitle: `${dayjs(query.value.from).format('YYYY-MM')} ~ ${dayjs(query.value.to).format('YYYY-MM')}；备注：${query.value.printRemark || '-'}`,
      columns: printColumnOptions.filter(item => selectedPrintColumns.value.includes(item.value)).map(item => ({ key: item.value, title: item.label })),
      rows: data
    })
  } catch (error: any) {
    message.error(error?.message || '打印失败')
  }
}

onMounted(loadData)
</script>

<style scoped>
.headline-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.headline-stat {
  padding: 14px 16px;
  border-radius: 16px;
  border: 1px solid rgba(208, 223, 234, 0.88);
  background: rgba(255, 255, 255, 0.78);
}

.headline-stat span {
  display: block;
  font-size: 12px;
  color: #6f8ba2;
}

.headline-stat strong {
  display: block;
  margin-top: 8px;
  font-size: 22px;
  color: #173854;
}

.stats-toolbar-shell,
.story-panel {
  padding: 16px 18px;
  border-radius: 20px;
  border: 1px solid rgba(208, 223, 234, 0.88);
  background: rgba(255, 255, 255, 0.82);
}

.stats-story-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-top: 16px;
}

.story-panel--chart,
.story-panel--table {
  margin-top: 16px;
}

.story-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.story-kicker {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #7290a8;
}

.story-head h3 {
  margin: 4px 0 0;
  font-size: 18px;
  color: #173854;
}

.compare-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.compare-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  padding: 12px 14px;
  border-radius: 16px;
  background: rgba(248, 250, 252, 0.86);
}

.compare-name {
  font-weight: 700;
  color: #0f172a;
}

.compare-meta {
  margin-top: 4px;
  font-size: 12px;
  color: rgba(15, 23, 42, 0.62);
}

.compare-value {
  font-size: 18px;
  font-weight: 700;
  color: #111827;
}

@media (max-width: 992px) {
  .headline-stats,
  .stats-story-grid {
    grid-template-columns: 1fr;
  }
}
</style>
