<template>
  <div class="bi-screen">
    <!-- 顶部标题栏 -->
    <header class="bi-header">
      <div class="bi-title">
        <span class="bi-title-main">智养云 · 经营驾驶舱</span>
        <span class="bi-title-sub" v-if="summary.month">
          统计月 {{ summary.month }}
          <template v-if="summary.latestStatDate"> · 数据截至 {{ summary.latestStatDate }}</template>
        </span>
      </div>
      <a-space wrap>
        <a-radio-group v-model:value="trendDays" button-style="solid" size="small" @change="loadTrend">
          <a-radio-button :value="30">近 30 天</a-radio-button>
          <a-radio-button :value="90">近 90 天</a-radio-button>
        </a-radio-group>
        <a-date-picker
          v-model:value="recalcDate"
          size="small"
          :allow-clear="true"
          placeholder="重算日期(默认昨日)"
          value-format="YYYY-MM-DD"
        />
        <a-button size="small" :loading="recalcing" @click="recalc">重算汇总</a-button>
        <a-button size="small" type="primary" :loading="loading" @click="loadAll">刷新</a-button>
        <a-button size="small" @click="definitionsVisible = true">指标口径</a-button>
      </a-space>
    </header>

    <a-spin :spinning="loading">
      <!-- 顶部关键指标卡（本月 + 环比） -->
      <section class="bi-cards">
        <div v-for="card in topCards" :key="card.metricCode" class="bi-card">
          <div class="bi-card-name">{{ card.metricName }}</div>
          <div class="bi-card-value">
            {{ formatValue(card.value) }}
            <span class="bi-card-unit" v-if="card.unit">{{ card.unit }}</span>
          </div>
          <div class="bi-card-mom" v-if="card.momRate !== null && card.momRate !== undefined">
            环比
            <span :class="card.momRate >= 0 ? 'up' : 'down'">
              {{ card.momRate >= 0 ? '▲' : '▼' }} {{ Math.abs(card.momRate).toFixed(2) }}%
            </span>
          </div>
          <div class="bi-card-mom muted" v-else>环比 --</div>
        </div>
      </section>

      <!-- 中部趋势 -->
      <section class="bi-row">
        <div class="bi-panel">
          <div class="bi-panel-title">入住率与在住人数趋势</div>
          <v-chart class="bi-chart" :option="occupancyTrendOption" autoresize />
        </div>
        <div class="bi-panel">
          <div class="bi-panel-title">营收与回款趋势</div>
          <v-chart class="bi-chart" :option="financeTrendOption" autoresize />
        </div>
      </section>

      <!-- 下部分布与达标 -->
      <section class="bi-row bi-row-3">
        <div class="bi-panel">
          <div class="bi-panel-title">在住长者护理等级分布</div>
          <v-chart v-if="hasCareLevelData" class="bi-chart" :option="careLevelOption" autoresize />
          <a-empty v-else class="bi-empty" description="暂无分布数据" />
        </div>
        <div class="bi-panel">
          <div class="bi-panel-title">当月费用科目结构</div>
          <v-chart v-if="hasCostData" class="bi-chart" :option="costStructureOption" autoresize />
          <a-empty v-else class="bi-empty" description="暂无科目数据" />
        </div>
        <div class="bi-panel">
          <div class="bi-panel-title">护理完成率 / 告警响应达标率</div>
          <v-chart class="bi-chart" :option="qualityTrendOption" autoresize />
        </div>
      </section>

      <!-- 机构对比（仅超管可见） -->
      <section class="bi-row" v-if="isSysAdmin">
        <div class="bi-panel bi-panel-full">
          <div class="bi-panel-title">机构对比（本月）</div>
          <a-table
            class="bi-table"
            :columns="compareColumns"
            :data-source="orgCompare"
            :pagination="false"
            row-key="orgId"
            size="small"
          />
        </div>
      </section>
    </a-spin>

    <!-- 指标口径抽屉 -->
    <a-drawer v-model:open="definitionsVisible" title="指标口径登记" width="640">
      <a-table
        :columns="definitionColumns"
        :data-source="definitions"
        :pagination="false"
        row-key="id"
        size="small"
      />
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import VChart from 'vue-echarts'
import { useUserStore } from '../../stores/user'
import {
  getCockpitBiSummary,
  getCockpitBiTrend,
  getCockpitBiDistribution,
  getCockpitBiOrgCompare,
  getCockpitMetricDefinitions,
  recalcCockpitBiStats,
  type CockpitBiSummaryResponse,
  type CockpitBiTrendItem,
  type CockpitBiDistributionResponse,
  type CockpitBiOrgCompareItem,
  type CockpitMetricDefinition
} from '../../api/cockpit'

const userStore = useUserStore()
const isSysAdmin = computed(() => (userStore.roles || []).includes('SYS_ADMIN'))

const loading = ref(false)
const recalcing = ref(false)
const trendDays = ref(30)
const recalcDate = ref<string | undefined>(undefined)
const definitionsVisible = ref(false)

const summary = ref<Partial<CockpitBiSummaryResponse>>({})
const trend = ref<CockpitBiTrendItem[]>([])
const distribution = ref<Partial<CockpitBiDistributionResponse>>({})
const orgCompare = ref<CockpitBiOrgCompareItem[]>([])
const definitions = ref<CockpitMetricDefinition[]>([])

const TOP_CARD_CODES = [
  'OCCUPANCY_RATE',
  'RESIDENT_COUNT',
  'ADMISSIONS',
  'DISCHARGES',
  'REVENUE_AMOUNT',
  'COLLECTION_RATE',
  'CARE_TASK_COMPLETION_RATE',
  'ALERT_ON_TIME_RATE'
]

const topCards = computed(() => {
  const cards = summary.value.cards || []
  const picked = TOP_CARD_CODES
    .map((code) => cards.find((c) => c.metricCode === code))
    .filter((c): c is NonNullable<typeof c> => !!c)
  return picked.length ? picked : cards
})

function formatValue(value: number | null | undefined) {
  if (value === null || value === undefined) return '--'
  const num = Number(value)
  if (Math.abs(num) >= 10000) return (num / 10000).toFixed(2) + ' 万'
  return Number.isInteger(num) ? String(num) : num.toFixed(2)
}

// ==================== 图表配置（深色大屏风格） ====================

const AXIS_STYLE = {
  axisLine: { lineStyle: { color: '#3d4a6b' } },
  axisLabel: { color: '#8fa3c8' },
  splitLine: { lineStyle: { color: 'rgba(61, 74, 107, 0.35)' } }
}

const dates = computed(() => trend.value.map((t) => t.statDate))

const occupancyTrendOption = computed(() => ({
  backgroundColor: 'transparent',
  tooltip: { trigger: 'axis' },
  legend: { textStyle: { color: '#8fa3c8' }, top: 0 },
  grid: { left: 48, right: 48, top: 36, bottom: 28 },
  xAxis: { type: 'category', data: dates.value, ...AXIS_STYLE },
  yAxis: [
    { type: 'value', name: '入住率%', max: 100, ...AXIS_STYLE },
    { type: 'value', name: '人数', ...AXIS_STYLE, splitLine: { show: false } }
  ],
  series: [
    {
      name: '入住率(%)',
      type: 'line',
      smooth: true,
      showSymbol: false,
      data: trend.value.map((t) => t.occupancyRate),
      lineStyle: { color: '#36c6ff', width: 2 },
      itemStyle: { color: '#36c6ff' },
      areaStyle: { color: 'rgba(54, 198, 255, 0.12)' }
    },
    {
      name: '在住人数',
      type: 'line',
      smooth: true,
      showSymbol: false,
      yAxisIndex: 1,
      data: trend.value.map((t) => t.residentCount),
      lineStyle: { color: '#7c8cff', width: 2 },
      itemStyle: { color: '#7c8cff' }
    }
  ]
}))

const financeTrendOption = computed(() => ({
  backgroundColor: 'transparent',
  tooltip: { trigger: 'axis' },
  legend: { textStyle: { color: '#8fa3c8' }, top: 0 },
  grid: { left: 64, right: 48, top: 36, bottom: 28 },
  xAxis: { type: 'category', data: dates.value, ...AXIS_STYLE },
  yAxis: [
    { type: 'value', name: '元', ...AXIS_STYLE },
    { type: 'value', name: '回款率%', max: 100, ...AXIS_STYLE, splitLine: { show: false } }
  ],
  series: [
    {
      name: '当月营收累计(元)',
      type: 'line',
      smooth: true,
      showSymbol: false,
      data: trend.value.map((t) => t.revenueAmount),
      lineStyle: { color: '#ffc53d', width: 2 },
      itemStyle: { color: '#ffc53d' }
    },
    {
      name: '当日回款(元)',
      type: 'bar',
      barMaxWidth: 10,
      data: trend.value.map((t) => t.paidDailyAmount),
      itemStyle: { color: 'rgba(54, 198, 255, 0.65)' }
    },
    {
      name: '回款率(%)',
      type: 'line',
      smooth: true,
      showSymbol: false,
      yAxisIndex: 1,
      data: trend.value.map((t) => t.collectionRate),
      lineStyle: { color: '#5ad8a6', width: 2 },
      itemStyle: { color: '#5ad8a6' }
    }
  ]
}))

const qualityTrendOption = computed(() => ({
  backgroundColor: 'transparent',
  tooltip: { trigger: 'axis' },
  legend: { textStyle: { color: '#8fa3c8' }, top: 0 },
  grid: { left: 48, right: 24, top: 36, bottom: 28 },
  xAxis: { type: 'category', data: dates.value, ...AXIS_STYLE },
  yAxis: { type: 'value', name: '%', max: 100, ...AXIS_STYLE },
  series: [
    {
      name: '护理工单完成率(%)',
      type: 'line',
      smooth: true,
      showSymbol: false,
      data: trend.value.map((t) => t.careTaskCompletionRate),
      lineStyle: { color: '#5ad8a6', width: 2 },
      itemStyle: { color: '#5ad8a6' }
    },
    {
      name: '告警响应达标率(%)',
      type: 'line',
      smooth: true,
      showSymbol: false,
      data: trend.value.map((t) => t.alertOnTimeRate),
      lineStyle: { color: '#ff7875', width: 2 },
      itemStyle: { color: '#ff7875' }
    }
  ]
}))

const PIE_COLORS = ['#36c6ff', '#7c8cff', '#5ad8a6', '#ffc53d', '#ff7875', '#b37feb', '#5cdbd3', '#ffa940']

const hasCareLevelData = computed(() => Object.keys(distribution.value.careLevelDist || {}).length > 0)
const hasCostData = computed(() => Object.keys(distribution.value.costStructure || {}).length > 0)

const careLevelOption = computed(() => ({
  backgroundColor: 'transparent',
  color: PIE_COLORS,
  tooltip: { trigger: 'item', formatter: '{b}: {c} 人 ({d}%)' },
  legend: { textStyle: { color: '#8fa3c8' }, bottom: 0 },
  series: [
    {
      type: 'pie',
      radius: ['42%', '68%'],
      center: ['50%', '46%'],
      label: { color: '#c7d3ec' },
      data: Object.entries(distribution.value.careLevelDist || {}).map(([name, value]) => ({ name, value }))
    }
  ]
}))

const costStructureOption = computed(() => ({
  backgroundColor: 'transparent',
  color: PIE_COLORS,
  tooltip: { trigger: 'item', formatter: '{b}: {c} 元 ({d}%)' },
  legend: { textStyle: { color: '#8fa3c8' }, bottom: 0 },
  series: [
    {
      type: 'pie',
      radius: ['42%', '68%'],
      center: ['50%', '46%'],
      label: { color: '#c7d3ec' },
      data: Object.entries(distribution.value.costStructure || {}).map(([name, value]) => ({ name, value }))
    }
  ]
}))

// ==================== 表格 ====================

const compareColumns = [
  { title: '机构', dataIndex: 'orgName', key: 'orgName' },
  { title: '入住率(%)', dataIndex: 'occupancyRate', key: 'occupancyRate' },
  { title: '在住人数', dataIndex: 'residentCount', key: 'residentCount' },
  { title: '本月营收(元)', dataIndex: 'revenueAmount', key: 'revenueAmount' },
  { title: '回款率(%)', dataIndex: 'collectionRate', key: 'collectionRate' },
  { title: '护理完成率(%)', dataIndex: 'careTaskCompletionRate', key: 'careTaskCompletionRate' },
  { title: '告警达标率(%)', dataIndex: 'alertOnTimeRate', key: 'alertOnTimeRate' },
  { title: '家属满意度(分)', dataIndex: 'satisfactionScore', key: 'satisfactionScore' }
]

const definitionColumns = [
  { title: '指标', dataIndex: 'metricName', key: 'metricName', width: 130 },
  { title: '分类', dataIndex: 'metricCategory', key: 'metricCategory', width: 90 },
  { title: '单位', dataIndex: 'unit', key: 'unit', width: 60 },
  { title: '口径说明', dataIndex: 'caliberDesc', key: 'caliberDesc' }
]

// ==================== 数据加载 ====================

async function loadTrend() {
  try {
    trend.value = await getCockpitBiTrend({ days: trendDays.value })
  } catch (e) {
    message.error('加载趋势数据失败')
  }
}

async function loadAll() {
  loading.value = true
  try {
    const tasks: Promise<unknown>[] = [
      getCockpitBiSummary().then((data) => (summary.value = data || {})),
      loadTrend(),
      getCockpitBiDistribution().then((data) => (distribution.value = data || {}))
    ]
    if (isSysAdmin.value) {
      tasks.push(
        getCockpitBiOrgCompare()
          .then((data) => (orgCompare.value = data || []))
          .catch(() => (orgCompare.value = []))
      )
    }
    await Promise.all(tasks)
  } catch (e) {
    message.error('加载经营驾驶舱数据失败')
  } finally {
    loading.value = false
  }
}

async function recalc() {
  recalcing.value = true
  try {
    await recalcCockpitBiStats({ date: recalcDate.value })
    message.success('汇总重算完成')
    await loadAll()
  } catch (e) {
    message.error('汇总重算失败')
  } finally {
    recalcing.value = false
  }
}

watch(definitionsVisible, async (open) => {
  if (open && definitions.value.length === 0) {
    try {
      definitions.value = await getCockpitMetricDefinitions()
    } catch (e) {
      message.error('加载指标口径失败')
    }
  }
})

onMounted(loadAll)
</script>

<style scoped>
.bi-screen {
  min-height: 100%;
  padding: 16px 20px 24px;
  background: radial-gradient(circle at 20% 0%, #16213f 0%, #0b1226 55%, #080e1e 100%);
  color: #c7d3ec;
}

.bi-header {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.bi-title-main {
  font-size: 22px;
  font-weight: 600;
  letter-spacing: 2px;
  color: #eaf2ff;
  text-shadow: 0 0 12px rgba(54, 198, 255, 0.45);
}

.bi-title-sub {
  margin-left: 12px;
  font-size: 12px;
  color: #8fa3c8;
}

.bi-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.bi-card {
  padding: 14px 16px;
  border: 1px solid rgba(54, 198, 255, 0.22);
  border-radius: 8px;
  background: linear-gradient(160deg, rgba(54, 198, 255, 0.08), rgba(124, 140, 255, 0.05));
}

.bi-card-name {
  font-size: 13px;
  color: #8fa3c8;
}

.bi-card-value {
  margin-top: 6px;
  font-size: 26px;
  font-weight: 600;
  color: #36c6ff;
}

.bi-card-unit {
  font-size: 13px;
  font-weight: 400;
  color: #8fa3c8;
}

.bi-card-mom {
  margin-top: 4px;
  font-size: 12px;
  color: #8fa3c8;
}

.bi-card-mom .up {
  color: #5ad8a6;
}

.bi-card-mom .down {
  color: #ff7875;
}

.bi-card-mom.muted {
  opacity: 0.6;
}

.bi-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(360px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.bi-row-3 {
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
}

.bi-panel {
  padding: 12px 16px 8px;
  border: 1px solid rgba(61, 74, 107, 0.6);
  border-radius: 8px;
  background: rgba(13, 21, 44, 0.75);
}

.bi-panel-full {
  grid-column: 1 / -1;
}

.bi-panel-title {
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #eaf2ff;
}

.bi-panel-title::before {
  content: '';
  display: inline-block;
  width: 4px;
  height: 12px;
  margin-right: 8px;
  border-radius: 2px;
  background: #36c6ff;
  vertical-align: -1px;
}

.bi-chart {
  height: 280px;
}

.bi-empty {
  padding: 60px 0;
}

.bi-empty :deep(.ant-empty-description) {
  color: #8fa3c8;
}

.bi-table :deep(.ant-table) {
  background: transparent;
  color: #c7d3ec;
}

.bi-table :deep(.ant-table-thead > tr > th) {
  background: rgba(54, 198, 255, 0.08);
  color: #8fa3c8;
  border-bottom-color: rgba(61, 74, 107, 0.6);
}

.bi-table :deep(.ant-table-tbody > tr > td) {
  border-bottom-color: rgba(61, 74, 107, 0.4);
}

.bi-table :deep(.ant-table-tbody > tr:hover > td) {
  background: rgba(54, 198, 255, 0.06);
}
</style>
