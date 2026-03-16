<template>
  <PageContainer title="老人信息统计" subTitle="老人结构、护理等级与状态分布">
    <a-card class="card-elevated" :bordered="false" style="margin-bottom: 16px;">
      <a-form layout="inline">
        <a-form-item label="机构ID">
          <a-input-number v-model:value="query.orgId" :min="1" placeholder="默认当前机构" style="width: 160px" />
        </a-form-item>
        <a-form-item label="打印维度">
          <a-select v-model:value="query.printSection" style="width: 150px">
            <a-select-option value="all">全部</a-select-option>
            <a-select-option value="gender">性别</a-select-option>
            <a-select-option value="age">年龄</a-select-option>
            <a-select-option value="care">护理等级</a-select-option>
            <a-select-option value="status">状态</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="对象筛选">
          <a-input v-model:value="query.keyword" allow-clear placeholder="例如：男/80-89岁/在院" style="width: 180px" />
        </a-form-item>
        <a-form-item label="打印备注">
          <a-input v-model:value="query.printRemark" allow-clear placeholder="例如：月度画像版" style="width: 180px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="loadData">刷新</a-button>
            <a-button @click="exportSummary">导出汇总</a-button>
            <a-button @click="openColumnSetting">列设置</a-button>
            <a-button @click="printCurrent">打印当前列</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-row :gutter="16">
      <a-col :span="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="老人总数" :value="stats.totalElders || 0" />
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="在院人数" :value="stats.inHospitalCount || 0" />
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="离院人数" :value="stats.dischargedCount || 0" />
        </a-card>
      </a-col>
    </a-row>

    <StatsWorkspacePanel
      page-key="stats-elder-info"
      title="画像策略区"
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
      page-key="stats-elder-info-command"
      title="画像协同台"
      share-title="老人画像统计协同包"
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

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="重点分群观察">
      <a-row :gutter="[12, 12]">
        <a-col :xs="24" :sm="12" :lg="6" v-for="item in segmentCards" :key="item.key">
          <div class="segment-card">
            <div class="segment-label">{{ item.label }}</div>
            <div class="segment-value">{{ item.valueText }}</div>
            <div class="segment-detail">{{ item.detail }}</div>
          </div>
        </a-col>
      </a-row>
    </a-card>

    <a-row :gutter="16" style="margin-top: 16px;">
      <a-col :span="12">
        <a-card class="card-elevated" :bordered="false" title="性别分布">
          <div ref="genderRef" style="height: 280px;"></div>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card class="card-elevated" :bordered="false" title="年龄分布">
          <div ref="ageRef" style="height: 280px;"></div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" style="margin-top: 16px;">
      <a-col :span="12">
        <a-card class="card-elevated" :bordered="false" title="护理等级分布">
          <div ref="careLevelRef" style="height: 280px;"></div>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card class="card-elevated" :bordered="false" title="状态分布">
          <div ref="statusRef" style="height: 280px;"></div>
        </a-card>
      </a-col>
    </a-row>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="画像明细">
      <vxe-table border stripe show-overflow :data="displayRows" height="300">
        <vxe-column field="section" title="维度" width="120" />
        <vxe-column field="name" title="项" min-width="180" />
        <vxe-column field="count" title="人数" width="120" />
      </vxe-table>
    </a-card>

    <a-modal v-model:open="columnSettingOpen" title="打印列设置" @ok="columnSettingOpen = false" cancel-text="关闭" ok-text="确定">
      <a-checkbox-group v-model:value="selectedPrintColumns" :options="printColumnOptions" />
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import StatsWorkspacePanel from '../../components/stats/StatsWorkspacePanel.vue'
import StatsInsightDeck from '../../components/stats/StatsInsightDeck.vue'
import StatsCommandCenter from '../../components/stats/StatsCommandCenter.vue'
import { exportElderInfoStatsCsv, getElderInfoStats } from '../../api/stats'
import type { ElderInfoStatsResponse, NameCountItem } from '../../types'
import { useECharts } from '../../plugins/echarts'
import { message } from 'ant-design-vue'
import { printTableReport } from '../../utils/print'

const router = useRouter()
const stats = reactive<ElderInfoStatsResponse>({
  totalElders: 0,
  inHospitalCount: 0,
  dischargedCount: 0,
  genderDistribution: [],
  ageDistribution: [],
  careLevelDistribution: [],
  statusDistribution: []
})
const query = reactive({
  orgId: undefined as number | undefined,
  printSection: 'all' as 'all' | 'gender' | 'age' | 'care' | 'status',
  keyword: '',
  printRemark: ''
})

const { chartRef: genderRef, setOption: setGenderOption } = useECharts()
const { chartRef: ageRef, setOption: setAgeOption } = useECharts()
const { chartRef: careLevelRef, setOption: setCareLevelOption } = useECharts()
const { chartRef: statusRef, setOption: setStatusOption } = useECharts()
const columnSettingOpen = ref(false)
const printColumnOptions = [
  { label: '维度', value: 'section' },
  { label: '项', value: 'name' },
  { label: '人数', value: 'count' }
]
const selectedPrintColumns = ref<string[]>(['section', 'name', 'count'])
const panelKeys = ref<string[]>([])
const targets = ref<Record<string, number>>({
  inHospitalCount: 0,
  dischargedCount: 0,
  highAgeShare: 30
})
const printableRows = computed(() => {
  const rows: Array<{ section: string; name: string; count: number }> = []
  for (const item of stats.genderDistribution || []) rows.push({ section: '性别', name: item.name, count: item.count })
  for (const item of stats.ageDistribution || []) rows.push({ section: '年龄', name: item.name, count: item.count })
  for (const item of stats.careLevelDistribution || []) rows.push({ section: '护理等级', name: item.name, count: item.count })
  for (const item of stats.statusDistribution || []) rows.push({ section: '状态', name: item.name, count: item.count })
  return rows
})
const displayRows = computed(() => {
  const section = query.printSection
  const keyword = String(query.keyword || '').trim()
  return printableRows.value.filter(item => {
    const sectionMatched = section === 'all'
      || (section === 'gender' && item.section === '性别')
      || (section === 'age' && item.section === '年龄')
      || (section === 'care' && item.section === '护理等级')
      || (section === 'status' && item.section === '状态')
    const keywordMatched = !keyword || item.name.includes(keyword)
    return sectionMatched && keywordMatched
  })
})
const highAgeCount = computed(() =>
  (stats.ageDistribution || [])
    .filter((item) => ['80-89岁', '90岁及以上'].includes(String(item.name || '')))
    .reduce((sum, item) => sum + Number(item.count || 0), 0)
)
const highAgeShare = computed(() =>
  Number(stats.totalElders || 0) > 0 ? Number(((highAgeCount.value / Number(stats.totalElders || 1)) * 100).toFixed(2)) : 0
)
const workspaceSummary = computed(() => `聚焦老人总数、在院/离院和高龄结构，支持保存常用画像筛选。当前总人数 ${stats.totalElders || 0}。`)
const workspacePayload = computed(() => ({
  orgId: query.orgId ? String(query.orgId) : '',
  printSection: query.printSection,
  keyword: query.keyword || ''
}))
const targetFields = computed(() => ([
  { key: 'inHospitalCount', label: '在院人数目标', value: targets.value.inHospitalCount, min: 0, max: 999999, step: 1 },
  { key: 'dischargedCount', label: '离院人数关注线', value: targets.value.dischargedCount, min: 0, max: 999999, step: 1 },
  { key: 'highAgeShare', label: '高龄占比提醒线(%)', value: targets.value.highAgeShare, min: 0, max: 100, step: 1 }
]))
const dataHealth = computed(() => ({
  freshness: '实时',
  completeness: `${displayRows.value.length}/${printableRows.value.length || 0} 画像已显示`,
  issues: [
    query.keyword ? `当前已按对象关键字筛选：${query.keyword}` : '',
    highAgeShare.value > Number(targets.value.highAgeShare || 0) ? '高龄老人占比高于提醒线' : ''
  ].filter(Boolean)
}))
const emptyState = computed(() => ({
  visible: !displayRows.value.length,
  title: '当前维度下没有画像结果',
  description: '建议切换到全部维度或清空对象筛选后再查看。',
  hints: [
    '对象筛选会同时作用于性别、年龄、护理等级和状态',
    '高龄结构建议结合护理等级一起看',
    '如果用于月报，建议先保存一套固定方案'
  ]
}))
const insightItems = computed(() => {
  const inHospitalGap = Number(stats.inHospitalCount || 0) - Number(targets.value.inHospitalCount || 0)
  const dischargedGap = Number(stats.dischargedCount || 0) - Number(targets.value.dischargedCount || 0)
  return [
    {
      key: 'total',
      label: '老人总数',
      valueText: `${stats.totalElders || 0}`,
      trendText: `在院 ${stats.inHospitalCount || 0} / 离院 ${stats.dischargedCount || 0}`,
      detail: `高龄占比 ${highAgeShare.value.toFixed(2)}%`,
      tone: 'neutral'
    },
    {
      key: 'inhospital',
      label: '在院达成',
      valueText: `${stats.inHospitalCount || 0}`,
      trendText: `目标差 ${inHospitalGap >= 0 ? '+' : ''}${inHospitalGap}`,
      detail: inHospitalGap < 0 ? '在院人数低于目标，建议结合入住净增长一起看' : '在院人数达到目标区间',
      tone: inHospitalGap < 0 ? 'warning' : 'good'
    },
    {
      key: 'discharged',
      label: '离院关注',
      valueText: `${stats.dischargedCount || 0}`,
      trendText: `关注线 ${targets.value.dischargedCount || 0}`,
      detail: dischargedGap > 0 ? '离院人数高于关注线，建议核查近期离院原因' : '离院人数处于关注线内',
      tone: dischargedGap > 0 ? 'warning' : 'good'
    },
    {
      key: 'aged',
      label: '高龄结构',
      valueText: `${highAgeShare.value.toFixed(2)}%`,
      trendText: `提醒线 ${Number(targets.value.highAgeShare || 0).toFixed(2)}%`,
      detail: `80岁及以上 ${highAgeCount.value} 人`,
      tone: highAgeShare.value > Number(targets.value.highAgeShare || 0) ? 'warning' : 'good'
    }
  ]
})
const visibleInsightItems = computed(() => {
  const selected = panelKeys.value.length ? new Set(panelKeys.value) : null
  return insightItems.value.filter((item) => !selected || selected.has(item.key))
})
const commandSummary = computed(() => '把老人分群观察、模板化打印、协作分享和纠错反馈集中到画像协同台。')
const commandActionItems = computed(() => ([
  { key: 'open-checkin', label: '查看入住统计', description: '结合在院规模和净增长判断画像变化', route: '/stats/check-in', tone: 'warning' as const },
  { key: 'open-flow-report', label: '查看出入报表', description: '排查离院偏高的具体老人', route: '/stats/elder-flow-report', tone: 'danger' as const },
  { key: 'open-dashboard', label: '返回看板', description: '回到经营总览', route: '/dashboard', tone: 'neutral' as const }
]))
const commandAnomalies = computed(() =>
  insightItems.value
    .filter((item) => item.tone === 'danger' || item.tone === 'warning')
    .map((item) => ({ key: item.key, label: item.label, detail: item.detail, tone: item.tone }))
)
const metricNotes = computed(() => ([
  { key: 'high-age', label: '高龄占比', note: '高龄占比用于判断护理和照护资源压力。' },
  { key: 'inhospital', label: '在院达成', note: '在院人数建议结合入住净增长和床位使用率一起看。' },
  { key: 'discharged', label: '离院关注', note: '离院人数高于关注线时，建议继续钻取老人出入报表。' }
]))
const panelOptions = computed(() =>
  insightItems.value.map((item) => ({
    key: item.key,
    label: item.label,
    description: item.detail || item.trendText
  }))
)
const heavyCareCount = computed(() =>
  (stats.careLevelDistribution || [])
    .filter((item) => /3|4|重|失能/.test(String(item.name || '')))
    .reduce((sum, item) => sum + Number(item.count || 0), 0)
)
const femaleCount = computed(() =>
  (stats.genderDistribution || [])
    .filter((item) => String(item.name || '').includes('女'))
    .reduce((sum, item) => sum + Number(item.count || 0), 0)
)
const segmentCards = computed(() => ([
  {
    key: 'high-age',
    label: '高龄分群',
    valueText: `${highAgeCount.value} 人`,
    detail: `占比 ${highAgeShare.value.toFixed(2)}%`
  },
  {
    key: 'heavy-care',
    label: '重度护理分群',
    valueText: `${heavyCareCount.value} 人`,
    detail: '建议结合护理等级与高龄结构一起看'
  },
  {
    key: 'female',
    label: '女性老人分群',
    valueText: `${femaleCount.value} 人`,
    detail: stats.totalElders ? `占比 ${((femaleCount.value / Number(stats.totalElders || 1)) * 100).toFixed(2)}%` : '占比 0.00%'
  },
  {
    key: 'resident',
    label: '在院活跃分群',
    valueText: `${stats.inHospitalCount || 0} 人`,
    detail: `离院 ${stats.dischargedCount || 0} 人`
  }
]))

function toPieData(data: NameCountItem[]) {
  return (data || []).map(item => ({ name: item.name, value: item.count }))
}

function toBarOption(data: NameCountItem[], color: string) {
  return {
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: data.map(item => item.name) },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', itemStyle: { color }, data: data.map(item => item.count) }]
  }
}

async function loadData() {
  try {
    const data = await getElderInfoStats({ orgId: query.orgId })
    Object.assign(stats, data)
    setGenderOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: 0 },
      series: [{ type: 'pie', radius: ['36%', '66%'], data: toPieData(data.genderDistribution || []) }]
    })
    setAgeOption(toBarOption(data.ageDistribution || [], '#13c2c2'))
    setCareLevelOption(toBarOption(data.careLevelDistribution || [], '#1677ff'))
    setStatusOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: 0 },
      series: [{ type: 'pie', radius: '65%', data: toPieData(data.statusDistribution || []) }]
    })
  } catch (error: any) {
    message.error(error?.message || '加载老人信息统计失败')
  }
}

function reset() {
  query.orgId = undefined
  query.printSection = 'all'
  query.keyword = ''
  query.printRemark = ''
  loadData()
}

function applyPreset(payload: Record<string, any>) {
  query.orgId = payload.orgId ? Number(payload.orgId) : undefined
  query.keyword = String(payload.keyword || '')
  const printSection = String(payload.printSection || 'all')
  query.printSection = ['all', 'gender', 'age', 'care', 'status'].includes(printSection) ? printSection as any : 'all'
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

async function exportSummary() {
  try {
    await exportElderInfoStatsCsv({ orgId: query.orgId })
    message.success('导出成功')
  } catch (error: any) {
    message.error(error?.message || '导出失败')
  }
}

function printCurrent() {
  if (!selectedPrintColumns.value.length) {
    message.warning('请至少选择一列打印')
    return
  }
  try {
    const orgText = query.orgId ? `机构ID：${query.orgId}` : '机构：当前'
    printTableReport({
      title: '老人信息统计',
      subtitle: `${orgText}；维度：${query.printSection}；筛选：${query.keyword || '-'}；备注：${query.printRemark || '-'}`,
      columns: printColumnOptions.filter(item => selectedPrintColumns.value.includes(item.value)).map(item => ({ key: item.value, title: item.label })),
      rows: displayRows.value
    })
  } catch (error: any) {
    message.error(error?.message || '打印失败')
  }
}

onMounted(loadData)
</script>

<style scoped>
.segment-card {
  min-height: 128px;
  padding: 16px;
  border-radius: 18px;
  background:
    linear-gradient(145deg, rgba(255, 255, 255, 0.96), rgba(245, 248, 252, 0.96)),
    radial-gradient(circle at top right, rgba(22, 119, 255, 0.1), transparent 48%);
  border: 1px solid rgba(15, 23, 42, 0.08);
}

.segment-label {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: rgba(15, 23, 42, 0.5);
}

.segment-value {
  margin-top: 10px;
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
}

.segment-detail {
  margin-top: 8px;
  font-size: 12px;
  line-height: 1.6;
  color: rgba(15, 23, 42, 0.66);
}
</style>
