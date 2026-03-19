<template>
  <PageContainer title="老人出入统计" subTitle="机构维度老人出入院趋势">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline">
        <a-form-item label="起始月份">
          <a-date-picker v-model:value="query.from" picker="month" style="width: 160px" />
        </a-form-item>
        <a-form-item label="结束月份">
          <a-date-picker v-model:value="query.to" picker="month" style="width: 160px" />
        </a-form-item>
        <a-form-item label="机构ID">
          <a-input v-model:value="query.orgId" allow-clear placeholder="默认当前机构" style="width: 160px" />
        </a-form-item>
        <a-form-item label="月份筛选">
          <a-input v-model:value="query.monthKeyword" allow-clear placeholder="如 2026-03" style="width: 140px" />
        </a-form-item>
        <a-form-item label="打印备注">
          <a-input v-model:value="query.printRemark" allow-clear placeholder="例如：经营例会版" style="width: 200px" />
        </a-form-item>
        <a-form-item label="快捷区间">
          <a-space>
            <a-button size="small" @click="setMonthPreset(3)">近3月</a-button>
            <a-button size="small" @click="setMonthPreset(6)">近6月</a-button>
            <a-button size="small" @click="setThisMonth">本月</a-button>
          </a-space>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="loadData">刷新</a-button>
            <a-button @click="exportCsvReport">导出报表</a-button>
            <a-button @click="openColumnSetting">列设置</a-button>
            <a-button :disabled="!displayRows.length" @click="printCurrent">打印当前列</a-button>
            <a-button :disabled="!query.monthKeyword.trim() || !displayRows.length" @click="printSpecificMonth">打印指定月份</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
      <StatsMetaHint
        scope-text="老人出入统计（按月）"
        :refreshed-at="refreshedAt"
        :note-text="metricTraceText"
      />
    </a-card>

    <StatsWorkspacePanel
      page-key="stats-org-elder-flow"
      title="出入趋势策略区"
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
      page-key="stats-org-elder-flow-command"
      title="机构出入协同台"
      share-title="机构老人出入协同包"
      :summary-text="commandSummary"
      :current-payload="workspacePayload"
      :metric-version="String(route.query.metricVersion || '')"
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

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="老人出入趋势">
      <div ref="trendRef" style="height: 320px;"></div>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="老人出入明细">
      <vxe-table border stripe show-overflow :data="displayRows" height="320">
        <vxe-column field="month" title="月份" width="140" />
        <vxe-column field="admissions" title="入住人数" width="120" />
        <vxe-column field="discharges" title="离院人数" width="120" />
      </vxe-table>
    </a-card>

    <a-modal v-model:open="columnSettingOpen" title="打印列设置" @ok="columnSettingOpen = false" cancel-text="关闭" ok-text="确定">
      <a-checkbox-group v-model:value="selectedPrintColumns" :options="printColumnOptions" />
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import dayjs, { type Dayjs } from 'dayjs'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import StatsMetaHint from '../../components/stats/StatsMetaHint.vue'
import StatsWorkspacePanel from '../../components/stats/StatsWorkspacePanel.vue'
import StatsInsightDeck from '../../components/stats/StatsInsightDeck.vue'
import StatsCommandCenter from '../../components/stats/StatsCommandCenter.vue'
import { exportOrgElderFlowCsv, getOrgElderFlow } from '../../api/stats'
import type { Id, MonthFlowItem } from '../../types'
import { useECharts } from '../../plugins/echarts'
import { message } from 'ant-design-vue'
import { printTableReport } from '../../utils/print'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'
import { normalizeId } from '../../utils/id'
import { buildComparisonSummary, buildPeriodSeries } from '../../utils/statsInsight'

const route = useRoute()
const router = useRouter()
const refreshedAt = ref('')

const query = ref({
  from: dayjs().subtract(5, 'month') as Dayjs,
  to: dayjs() as Dayjs,
  orgId: undefined as Id | undefined,
  monthKeyword: '',
  printRemark: ''
})

const rows = ref<MonthFlowItem[]>([])
const { chartRef: trendRef, setOption } = useECharts()
const columnSettingOpen = ref(false)
const printColumnOptions = [
  { label: '月份', value: 'month' },
  { label: '入住人数', value: 'admissions' },
  { label: '离院人数', value: 'discharges' }
]
const selectedPrintColumns = ref<string[]>(['month', 'admissions', 'discharges'])
const panelKeys = ref<string[]>([])
const targets = ref<Record<string, number>>({
  admissions: 0,
  dischargeShare: 50
})
const metricTraceText = computed(() => {
  const version = String(route.query.metricVersion || '').trim()
  const source = String(route.query.fromSource || '').trim()
  const notes: string[] = []
  if (version) notes.push(`口径版本：${version}`)
  if (source === 'dashboard') notes.push('来源：看板钻取')
  return notes.join('；')
})
const printableRows = computed(() => (rows.value || []).map(item => ({
  month: item.month,
  admissions: item.admissions,
  discharges: item.discharges
})))
const displayRows = computed(() => {
  const keyword = String(query.value.monthKeyword || '').trim()
  if (!keyword) return rows.value
  return rows.value.filter(item => String(item.month || '').includes(keyword))
})
const workspaceSummary = computed(() => `观察机构维度下的月度入住与离院波动，并沉淀常用筛选方案。当前可见 ${displayRows.value.length} 个月。`)
const workspacePayload = computed(() => ({
  from: dayjs(query.value.from).format('YYYY-MM'),
  to: dayjs(query.value.to).format('YYYY-MM'),
  orgId: query.value.orgId ? String(query.value.orgId) : '',
  monthKeyword: query.value.monthKeyword || ''
}))
const targetFields = computed(() => ([
  { key: 'admissions', label: '月入住目标', value: targets.value.admissions, min: 0, max: 99999, step: 1 },
  { key: 'dischargeShare', label: '离院占比关注线(%)', value: targets.value.dischargeShare, min: 0, max: 100, step: 1 }
]))
const dataHealth = computed(() => ({
  freshness: refreshedAt.value || '--',
  completeness: `${displayRows.value.length}/${rows.value.length || 0} 月可视`,
  issues: [
    query.value.monthKeyword ? `月份关键字：${query.value.monthKeyword}` : ''
  ].filter(Boolean)
}))
const emptyState = computed(() => ({
  visible: !displayRows.value.length,
  title: '当前时间范围没有出入趋势数据',
  description: '建议扩大月份范围或清空月份关键字。',
  hints: [
    '趋势页适合与入住统计、月运营收入一起看',
    '若离院偏高，可进一步钻取老人出入报表',
    '建议保存季度版和半年版两套方案'
  ]
}))
const insightItems = computed(() => {
  const admissionSeries = buildPeriodSeries(displayRows.value, 'month', 'admissions')
  const dischargeSeries = buildPeriodSeries(displayRows.value, 'month', 'discharges')
  const admissionSummary = buildComparisonSummary(admissionSeries, targets.value.admissions)
  const latestAdmission = Number(admissionSummary.latestValue || 0)
  const latestDischarge = Number(dischargeSeries[dischargeSeries.length - 1]?.value || 0)
  const dischargeShare = latestAdmission + latestDischarge > 0
    ? Number(((latestDischarge / (latestAdmission + latestDischarge)) * 100).toFixed(2))
    : 0
  return [
    {
      key: 'admission',
      label: '最新月入住',
      valueText: `${latestAdmission}`,
      trendText: admissionSummary.momRate == null ? '暂无环比' : `较上月 ${admissionSummary.momRate >= 0 ? '+' : ''}${admissionSummary.momRate}%`,
      detail: admissionSummary.target != null ? `目标差 ${Number(admissionSummary.targetGap || 0)}` : admissionSummary.anomalyText,
      tone: admissionSummary.targetGap != null && admissionSummary.targetGap < 0 ? 'warning' : (admissionSummary.anomalyLevel || 'good')
    },
    {
      key: 'discharge',
      label: '最新月离院',
      valueText: `${latestDischarge}`,
      trendText: `占比 ${dischargeShare.toFixed(2)}%`,
      detail: dischargeShare > Number(targets.value.dischargeShare || 0) ? '离院占比高于关注线' : '离院占比处于关注线内',
      tone: dischargeShare > Number(targets.value.dischargeShare || 0) ? 'danger' : 'good'
    },
    {
      key: 'window',
      label: '观察窗口',
      valueText: `${displayRows.value.length} 月`,
      trendText: `${dayjs(query.value.from).format('YYYY-MM')} ~ ${dayjs(query.value.to).format('YYYY-MM')}`,
      detail: '建议结合季度节奏观察入住高峰',
      tone: 'neutral'
    },
    {
      key: 'balance',
      label: '累计出入平衡',
      valueText: `${displayRows.value.reduce((sum, item) => sum + Number(item.admissions || 0) - Number(item.discharges || 0), 0)}`,
      trendText: '累计入住 - 累计离院',
      detail: '可用于判断当前阶段净流入/净流出',
      tone: 'neutral'
    }
  ]
})
const visibleInsightItems = computed(() => {
  const selected = panelKeys.value.length ? new Set(panelKeys.value) : null
  return insightItems.value.filter((item) => !selected || selected.has(item.key))
})
const commandSummary = computed(() => '把机构出入趋势、月度打印模板和异常联动入口放到统一协同台，方便经营例会使用。')
const commandActionItems = computed(() => ([
  { key: 'open-flow-report', label: '查看出入明细', description: '从月趋势继续钻取到明细报表', route: '/stats/elder-flow-report', tone: 'danger' as const },
  { key: 'open-checkin', label: '查看入住统计', description: '对照入住净增长趋势', route: '/stats/check-in', tone: 'warning' as const },
  { key: 'open-dashboard', label: '返回看板', description: '回到经营总览', route: '/dashboard', tone: 'neutral' as const }
]))
const commandAnomalies = computed(() =>
  insightItems.value
    .filter((item) => item.tone === 'danger' || item.tone === 'warning')
    .map((item) => ({ key: item.key, label: item.label, detail: item.detail, tone: item.tone }))
)
const metricNotes = computed(() => ([
  { key: 'admission', label: '最新月入住', note: '用于判断当前窗口内最新一个月的入住动能。' },
  { key: 'discharge', label: '最新月离院', note: '离院占比偏高时建议直接钻取明细报表。' },
  { key: 'balance', label: '累计出入平衡', note: '累计入住-累计离院可反映阶段性净流入或净流出。' }
]))
const panelOptions = computed(() =>
  insightItems.value.map((item) => ({
    key: item.key,
    label: item.label,
    description: item.detail || item.trendText
  }))
)

async function loadData() {
  if (query.value.from.isAfter(query.value.to, 'month')) {
    message.warning('起始月份不能晚于结束月份')
    return
  }
  try {
    rows.value = await getOrgElderFlow({
      from: dayjs(query.value.from).format('YYYY-MM'),
      to: dayjs(query.value.to).format('YYYY-MM'),
      orgId: query.value.orgId
    })
    setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['入住', '离院'] },
      xAxis: { type: 'category', data: rows.value.map(item => item.month) },
      yAxis: { type: 'value' },
      series: [
        { name: '入住', type: 'bar', data: rows.value.map(item => item.admissions) },
        { name: '离院', type: 'bar', data: rows.value.map(item => item.discharges) }
      ]
    })
    refreshedAt.value = dayjs().format('YYYY-MM-DD HH:mm:ss')
    syncRouteQuery()
  } catch (error: any) {
    message.error(error?.message || '加载老人出入统计失败')
  }
}

function reset() {
  query.value.from = dayjs().subtract(5, 'month')
  query.value.to = dayjs()
  query.value.orgId = undefined
  query.value.monthKeyword = ''
  query.value.printRemark = ''
  syncRouteQuery()
  loadData()
}

function applyPreset(payload: Record<string, any>) {
  if (payload.from && dayjs(String(payload.from)).isValid()) query.value.from = dayjs(String(payload.from))
  if (payload.to && dayjs(String(payload.to)).isValid()) query.value.to = dayjs(String(payload.to))
  query.value.orgId = normalizeId(payload.orgId)
  query.value.monthKeyword = String(payload.monthKeyword || '')
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

async function exportCsvReport() {
  if (query.value.from.isAfter(query.value.to, 'month')) {
    message.warning('起始月份不能晚于结束月份')
    return
  }
  try {
    await exportOrgElderFlowCsv({
      from: dayjs(query.value.from).format('YYYY-MM'),
      to: dayjs(query.value.to).format('YYYY-MM'),
      orgId: query.value.orgId
    })
    message.success('报表导出成功')
  } catch (error: any) {
    message.error(error?.message || '报表导出失败')
  }
}

function openColumnSetting() {
  columnSettingOpen.value = true
}

function setMonthPreset(months: number) {
  query.value.to = dayjs()
  query.value.from = dayjs().subtract(months - 1, 'month')
  loadData()
}

function setThisMonth() {
  query.value.from = dayjs().startOf('month')
  query.value.to = dayjs()
  loadData()
}

function printCurrent() {
  const keyword = String(query.value.monthKeyword || '').trim()
  const data = !keyword ? printableRows.value : printableRows.value.filter(item => item.month.includes(keyword))
  renderPrint('老人出入统计（当前结果）', data)
}

function printSpecificMonth() {
  const keyword = String(query.value.monthKeyword || '').trim()
  if (!keyword) {
    message.warning('请输入月份关键字')
    return
  }
  const filtered = printableRows.value.filter(item => item.month.includes(keyword))
  if (!filtered.length) {
    message.warning('未找到匹配月份记录')
    return
  }
  renderPrint(`老人出入统计（${keyword}）`, filtered)
}

function renderPrint(title: string, data: Array<Record<string, any>>) {
  if (!selectedPrintColumns.value.length) {
    message.warning('请至少选择一列打印')
    return
  }
  try {
    const orgText = query.value.orgId ? `机构ID：${query.value.orgId}` : '机构：当前'
    const monthKeywordText = query.value.monthKeyword ? `月份筛选：${query.value.monthKeyword}` : '月份筛选：全部'
    printTableReport({
      title,
      subtitle: `${dayjs(query.value.from).format('YYYY-MM')} ~ ${dayjs(query.value.to).format('YYYY-MM')}；${orgText}；打印范围：${monthKeywordText}；记录数：${data.length}；备注：${query.value.printRemark || '-'}`,
      columns: printColumnOptions.filter(item => selectedPrintColumns.value.includes(item.value)).map(item => ({ key: item.value, title: item.label })),
      rows: data
    })
  } catch (error: any) {
    message.error(error?.message || '打印失败')
  }
}

function syncRouteQuery() {
  const nextQuery: Record<string, string> = {
    from: dayjs(query.value.from).format('YYYY-MM'),
    to: dayjs(query.value.to).format('YYYY-MM')
  }
  if (query.value.orgId) nextQuery.orgId = String(query.value.orgId)
  if (query.value.monthKeyword) nextQuery.monthKeyword = query.value.monthKeyword
  router.replace({ path: route.path, query: nextQuery }).catch(() => {})
}

function initFromRouteQuery() {
  const from = String(route.query.from || '')
  const to = String(route.query.to || '')
  if (dayjs(from).isValid()) query.value.from = dayjs(from)
  if (dayjs(to).isValid()) query.value.to = dayjs(to)
  query.value.orgId = normalizeId(route.query.orgId)
  const monthKeyword = String(route.query.monthKeyword || '')
  if (monthKeyword) query.value.monthKeyword = monthKeyword
}

useLiveSyncRefresh({
  topics: ['elder', 'lifecycle', 'bed'],
  refresh: () => {
    loadData()
  },
  debounceMs: 800
})

onMounted(() => {
  initFromRouteQuery()
  loadData()
})
</script>
