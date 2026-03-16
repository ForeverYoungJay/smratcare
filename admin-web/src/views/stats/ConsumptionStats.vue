<template>
  <PageContainer title="消费统计" subTitle="账单与商城消费分析">
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
        <a-form-item label="院内老人">
          <a-select
            v-model:value="query.elderId"
            show-search
            allow-clear
            :filter-option="false"
            :options="elderOptions"
            :loading="elderLoading"
            placeholder="按院内老人筛选"
            style="width: 180px"
            @search="searchElders"
            @focus="() => !elderOptions.length && searchElders('')"
            @change="onElderChange"
          />
        </a-form-item>
        <a-form-item label="打印备注">
          <a-input v-model:value="query.printRemark" allow-clear placeholder="例如：财务复核版" style="width: 200px" />
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
            <a-button @click="exportSummary">导出统计</a-button>
            <a-button @click="copyFilterLink">复制筛选链接</a-button>
            <a-button @click="openColumnSetting">列设置</a-button>
            <a-button :disabled="!displayTopRows.length" @click="printCurrent">打印当前列</a-button>
            <a-button :disabled="!query.elderId || !displayTopRows.length" @click="printSpecificElder">打印指定老人</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
      <StatsMetaHint
        scope-text="消费统计（总消费=账单+商城；排行口径为Top10）"
        :refreshed-at="refreshedAt"
        :note-text="metricTraceText"
      />
    </a-card>

    <StatsWorkspacePanel
      page-key="stats-consumption"
      title="消费策略区"
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
      page-key="stats-consumption-command"
      title="消费协同台"
      share-title="消费统计协同包"
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

    <a-row :gutter="16" style="margin-top: 16px;">
      <a-col :span="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="账单消费总额" :value="stats.totalBillConsumption || 0" :precision="2" />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="商城消费总额" :value="stats.totalStoreConsumption || 0" :precision="2" />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="消费合计" :value="stats.totalConsumption || 0" :precision="2" />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="月均消费" :value="stats.averageMonthlyConsumption || 0" :precision="2" />
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" style="margin-top: 16px;">
      <a-col :span="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="账单占比(%)" :value="stats.billConsumptionRatio || 0" :precision="2" />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="商城占比(%)" :value="stats.storeConsumptionRatio || 0" :precision="2" />
        </a-card>
      </a-col>
    </a-row>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="月度消费趋势">
      <div ref="trendRef" style="height: 320px;"></div>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="老人消费排行 Top10">
      <vxe-table border stripe show-overflow :data="displayTopRows" height="320">
        <vxe-column field="elderName" title="老人姓名" min-width="180">
          <template #default="{ row }">
            {{ row.elderName || '未知老人' }}
          </template>
        </vxe-column>
        <vxe-column field="amount" title="消费金额" width="180" />
      </vxe-table>
    </a-card>

    <a-modal v-model:open="columnSettingOpen" title="打印列设置" @ok="columnSettingOpen = false" cancel-text="关闭" ok-text="确定">
      <a-checkbox-group v-model:value="selectedPrintColumns" :options="printColumnOptions" />
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs, { type Dayjs } from 'dayjs'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import StatsMetaHint from '../../components/stats/StatsMetaHint.vue'
import StatsWorkspacePanel from '../../components/stats/StatsWorkspacePanel.vue'
import StatsInsightDeck from '../../components/stats/StatsInsightDeck.vue'
import StatsCommandCenter from '../../components/stats/StatsCommandCenter.vue'
import { exportConsumptionStatsCsv, getConsumptionStats } from '../../api/stats'
import type { ConsumptionStatsResponse, Id } from '../../types'
import { useECharts } from '../../plugins/echarts'
import { message } from 'ant-design-vue'
import { printTableReport } from '../../utils/print'
import { useElderOptions } from '../../composables/useElderOptions'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'
import { copyText } from '../../utils/clipboard'
import { normalizeId } from '../../utils/id'
import { buildComparisonSummary, buildPeriodSeries } from '../../utils/statsInsight'

const route = useRoute()
const router = useRouter()
const refreshedAt = ref('')

const query = reactive({
  from: dayjs().subtract(5, 'month') as Dayjs,
  to: dayjs() as Dayjs,
  orgId: undefined as Id | undefined,
  elderId: undefined as Id | undefined,
  printRemark: ''
})

const stats = reactive<ConsumptionStatsResponse>({
  totalBillConsumption: 0,
  totalStoreConsumption: 0,
  totalConsumption: 0,
  billConsumptionRatio: 0,
  storeConsumptionRatio: 0,
  averageMonthlyConsumption: 0,
  monthlyBillConsumption: [],
  monthlyStoreConsumption: [],
  monthlyTotalConsumption: [],
  topConsumerElders: []
})

const { chartRef: trendRef, setOption } = useECharts()
const columnSettingOpen = ref(false)
const printColumnOptions = [
  { label: '老人姓名', value: 'elderName' },
  { label: '消费金额', value: 'amount' }
]
const selectedPrintColumns = ref<string[]>(['elderName', 'amount'])
const panelKeys = ref<string[]>([])
const targets = ref<Record<string, number>>({
  totalConsumption: 0,
  billRatio: 70,
  topConsumerAmount: 0
})
const metricTraceText = computed(() => {
  const version = String(route.query.metricVersion || '').trim()
  const source = String(route.query.fromSource || '').trim()
  const notes: string[] = []
  if (version) notes.push(`口径版本：${version}`)
  if (source === 'dashboard') notes.push('来源：看板钻取')
  return notes.join('；')
})
const { elderOptions, elderLoading, searchElders } = useElderOptions({ pageSize: 100, inHospitalOnly: true, signedOnly: false })
const displayTopRows = computed(() => {
  const elderId = String(query.elderId || '').trim()
  const list = stats.topConsumerElders || []
  if (!elderId) return list
  return list.filter((item) => String(item.elderId || '').trim() === elderId)
})
const selectedElder = computed(() => elderOptions.value.find((item) => String(item.value || '').trim() === String(query.elderId || '').trim()))
const workspaceSummary = computed(() => `把总消费、账单占比与重点老人消费排行结合查看，并沉淀固定筛选方案。当前展示 ${displayTopRows.value.length} 位老人。`)
const workspacePayload = computed(() => ({
  from: dayjs(query.from).format('YYYY-MM'),
  to: dayjs(query.to).format('YYYY-MM'),
  orgId: query.orgId ? String(query.orgId) : '',
  elderId: query.elderId ? String(query.elderId) : ''
}))
const targetFields = computed(() => ([
  { key: 'totalConsumption', label: '总消费目标(元)', value: targets.value.totalConsumption, min: 0, max: 999999999, step: 1000 },
  { key: 'billRatio', label: '账单消费占比目标(%)', value: targets.value.billRatio, min: 0, max: 100, step: 1 },
  { key: 'topConsumerAmount', label: 'Top1 消费提醒线(元)', value: targets.value.topConsumerAmount, min: 0, max: 999999999, step: 100 }
]))
const dataHealth = computed(() => ({
  freshness: refreshedAt.value || '--',
  completeness: `${displayTopRows.value.length}/${(stats.topConsumerElders || []).length || 0} 人已展示`,
  issues: [
    query.elderId ? `当前已锁定院内老人：${selectedElder.value?.name || query.elderId}` : '',
    Number(stats.billConsumptionRatio || 0) > Number(targets.value.billRatio || 0) ? '账单消费占比高于目标' : ''
  ].filter(Boolean)
}))
const emptyState = computed(() => ({
  visible: !(stats.monthlyTotalConsumption || []).length,
  title: '当前时间范围内暂无消费数据',
  description: '建议检查账单月结、商城订单或切换到更长的月份范围。',
  hints: [
    '确认财务账单已生成且未被软删除',
    '若按老人筛选，先恢复为全部再判断是否为单体问题',
    '对比账单消费与商城消费占比，确认数据是否偏科'
  ]
}))
const insightItems = computed(() => {
  const totalSeries = buildPeriodSeries(stats.monthlyTotalConsumption || [], 'month', 'amount')
  const billSeries = buildPeriodSeries(stats.monthlyBillConsumption || [], 'month', 'amount')
  const storeSeries = buildPeriodSeries(stats.monthlyStoreConsumption || [], 'month', 'amount')
  const totalSummary = buildComparisonSummary(totalSeries, targets.value.totalConsumption)
  const topAmount = Number((stats.topConsumerElders || [])[0]?.amount || 0)
  return [
    {
      key: 'total',
      label: '总消费走势',
      valueText: `${Number(stats.totalConsumption || 0).toFixed(2)} 元`,
      trendText: totalSummary.momRate == null ? '暂无环比' : `较上期 ${totalSummary.momRate >= 0 ? '+' : ''}${totalSummary.momRate}%`,
      detail: totalSummary.target != null ? `目标差 ${Number(totalSummary.targetGap || 0).toFixed(2)} 元` : totalSummary.anomalyText,
      tone: totalSummary.targetGap != null && totalSummary.targetGap < 0 ? 'warning' : (totalSummary.anomalyLevel || 'good')
    },
    {
      key: 'bill-ratio',
      label: '账单消费占比',
      valueText: `${Number(stats.billConsumptionRatio || 0).toFixed(2)}%`,
      trendText: `目标 ${Number(targets.value.billRatio || 0).toFixed(2)}%`,
      detail: `商城占比 ${Number(stats.storeConsumptionRatio || 0).toFixed(2)}%`,
      tone: Number(stats.billConsumptionRatio || 0) > Number(targets.value.billRatio || 0) ? 'warning' : 'good'
    },
    {
      key: 'bill',
      label: '账单与商城结构',
      valueText: `${Number((billSeries[billSeries.length - 1]?.value || 0)).toFixed(2)} / ${Number((storeSeries[storeSeries.length - 1]?.value || 0)).toFixed(2)}`,
      trendText: '最新月账单/商城',
      detail: totalSummary.anomalyText,
      tone: 'neutral'
    },
    {
      key: 'top',
      label: 'Top1 消费提醒',
      valueText: `${topAmount.toFixed(2)} 元`,
      trendText: `提醒线 ${Number(targets.value.topConsumerAmount || 0).toFixed(2)} 元`,
      detail: topAmount > Number(targets.value.topConsumerAmount || 0) ? '重点老人消费已超过提醒线' : 'Top1 消费仍处于提醒线内',
      tone: topAmount > Number(targets.value.topConsumerAmount || 0) ? 'warning' : 'good'
    }
  ]
})
const visibleInsightItems = computed(() => {
  const selected = panelKeys.value.length ? new Set(panelKeys.value) : null
  return insightItems.value.filter((item) => !selected || selected.has(item.key))
})
const commandSummary = computed(() => '把消费异常、重点老人、模板列和协作说明合并到一个操作入口，方便财务复核。')
const commandActionItems = computed(() => ([
  { key: 'open-revenue', label: '查看收入统计', description: '联动收入趋势判断经营结果', route: '/stats/monthly-revenue', tone: 'warning' as const },
  { key: 'open-elder-info', label: '查看老人画像', description: '结合老人结构判断消费分层', route: '/stats/elder-info', tone: 'danger' as const },
  { key: 'open-dashboard', label: '返回看板', description: '回到看板查看整体口径', route: '/dashboard', tone: 'neutral' as const }
]))
const commandAnomalies = computed(() =>
  insightItems.value
    .filter((item) => item.tone === 'danger' || item.tone === 'warning')
    .map((item) => ({ key: item.key, label: item.label, detail: item.detail, tone: item.tone }))
)
const metricNotes = computed(() => ([
  { key: 'total', label: '总消费', note: '总消费=账单消费+商城消费，老人筛选会同步作用于汇总和导出。' },
  { key: 'bill', label: '账单占比', note: '账单占比过高时，建议对比商城消费和线下账单结构。' },
  { key: 'top', label: 'Top10 排行', note: '排行口径默认取当前筛选下的前 10 位老人。' }
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
    const data = await getConsumptionStats({
      from: dayjs(query.from).format('YYYY-MM'),
      to: dayjs(query.to).format('YYYY-MM'),
      elderId: query.elderId,
      orgId: query.orgId
    })
    Object.assign(stats, data)
    setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['账单消费', '商城消费', '总消费'] },
      xAxis: { type: 'category', data: data.monthlyBillConsumption.map(item => item.month) },
      yAxis: { type: 'value' },
      series: [
        { name: '账单消费', type: 'bar', data: data.monthlyBillConsumption.map(item => item.amount) },
        { name: '商城消费', type: 'line', smooth: true, data: data.monthlyStoreConsumption.map(item => item.amount) },
        { name: '总消费', type: 'line', smooth: true, data: data.monthlyTotalConsumption.map(item => item.amount) }
      ]
    })
    refreshedAt.value = dayjs().format('YYYY-MM-DD HH:mm:ss')
    syncRouteQuery()
  } catch (error: any) {
    message.error(error?.message || '加载消费统计失败')
  }
}

function reset() {
  query.from = dayjs().subtract(5, 'month')
  query.to = dayjs()
  query.orgId = undefined
  query.elderId = undefined
  query.printRemark = ''
  syncRouteQuery()
  loadData()
}

function applyPreset(payload: Record<string, any>) {
  if (payload.from && dayjs(String(payload.from)).isValid()) query.from = dayjs(String(payload.from))
  if (payload.to && dayjs(String(payload.to)).isValid()) query.to = dayjs(String(payload.to))
  query.orgId = normalizeId(payload.orgId)
  query.elderId = normalizeId(payload.elderId)
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

function setMonthPreset(months: number) {
  query.to = dayjs()
  query.from = dayjs().subtract(months - 1, 'month')
  loadData()
}

function setThisMonth() {
  query.from = dayjs().startOf('month')
  query.to = dayjs()
  loadData()
}

async function exportSummary() {
  try {
    await exportConsumptionStatsCsv({
      from: dayjs(query.from).format('YYYY-MM'),
      to: dayjs(query.to).format('YYYY-MM'),
      elderId: query.elderId,
      orgId: query.orgId
    })
    message.success('导出成功')
  } catch (error: any) {
    message.error(error?.message || '导出失败')
  }
}

async function copyFilterLink() {
  const resolved = router.resolve({
    path: route.path,
    query: {
      from: dayjs(query.from).format('YYYY-MM'),
      to: dayjs(query.to).format('YYYY-MM'),
      orgId: query.orgId ? String(query.orgId) : '',
      elderId: query.elderId ? String(query.elderId) : ''
    }
  })
  const ok = await copyText(`${window.location.origin}${resolved.fullPath}`)
  if (ok) {
    message.success('筛选链接已复制')
  } else {
    message.warning('复制失败，请手动复制地址栏链接')
  }
}

function printCurrent() {
  renderPrint('消费统计（当前结果）', displayTopRows.value.map(item => ({
    elderName: item.elderName || '未知老人',
    amount: item.amount
  })))
}

function printSpecificElder() {
  if (!query.elderId) {
    message.warning('请选择院内老人')
    return
  }
  const filtered = (stats.topConsumerElders || [])
    .filter((item) => String(item.elderId || '').trim() === String(query.elderId || '').trim())
    .map(item => ({ elderName: item.elderName || '未知老人', amount: item.amount }))
  if (!filtered.length) {
    message.warning('未找到匹配老人')
    return
  }
  const elderText = selectedElder.value ? `${selectedElder.value.name}` : '未命名长者'
  renderPrint(`消费统计（${elderText}）`, filtered)
}

function renderPrint(title: string, rows: Array<Record<string, any>>) {
  if (!selectedPrintColumns.value.length) {
    message.warning('请至少选择一列打印')
    return
  }
  try {
    const orgText = query.orgId ? `机构ID：${query.orgId}` : '机构：当前'
  const elderText = selectedElder.value
      ? `院内老人：${selectedElder.value.name}`
      : '院内老人：全部'
    printTableReport({
      title,
      subtitle: `${dayjs(query.from).format('YYYY-MM')} ~ ${dayjs(query.to).format('YYYY-MM')}；${orgText}；打印范围：${elderText}；记录数：${rows.length}；备注：${query.printRemark || '-'}`,
      columns: printColumnOptions.filter(item => selectedPrintColumns.value.includes(item.value)).map(item => ({ key: item.value, title: item.label })),
      rows
    })
  } catch (error: any) {
    message.error(error?.message || '打印失败')
  }
}

function onElderChange(value?: Id) {
  query.elderId = value
  loadData()
}

function syncRouteQuery() {
  const nextQuery: Record<string, string> = {
    from: dayjs(query.from).format('YYYY-MM'),
    to: dayjs(query.to).format('YYYY-MM')
  }
  if (query.orgId) nextQuery.orgId = String(query.orgId)
  if (query.elderId) nextQuery.elderId = String(query.elderId)
  router.replace({ path: route.path, query: nextQuery }).catch(() => {})
}

function initFromRouteQuery() {
  const from = String(route.query.from || '')
  const to = String(route.query.to || '')
  if (dayjs(from).isValid()) query.from = dayjs(from)
  if (dayjs(to).isValid()) query.to = dayjs(to)
  query.orgId = normalizeId(route.query.orgId)
  query.elderId = normalizeId(route.query.elderId)
}

useLiveSyncRefresh({
  topics: ['finance', 'elder', 'lifecycle'],
  refresh: () => {
    loadData()
  },
  debounceMs: 900
})

onMounted(() => {
  initFromRouteQuery()
  loadData()
})
</script>
