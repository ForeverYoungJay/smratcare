<template>
  <PageContainer :title="pageMeta.title" :subTitle="pageMeta.subTitle">
    <div class="report-shell">
      <section class="report-nav fade-up">
        <div class="report-nav__tabs">
          <button
            v-for="tab in reportTabs"
            :key="tab.name"
            class="report-tab"
            :class="{ 'report-tab--active': route.name === tab.name }"
            type="button"
            @click="go(tab.path)"
          >
            <strong>{{ tab.label }}</strong>
            <span>{{ tab.hint }}</span>
          </button>
        </div>
        <div class="report-nav__controls">
          <a-form layout="inline" :model="query" class="search-form">
            <a-form-item label="起始月份">
              <a-date-picker v-model:value="query.from" picker="month" style="width: 160px" />
            </a-form-item>
            <a-form-item label="结束月份">
              <a-date-picker v-model:value="query.to" picker="month" style="width: 160px" />
            </a-form-item>
            <a-form-item>
              <a-space>
                <a-button type="primary" @click="loadCharts">刷新</a-button>
                <a-button @click="reset">重置</a-button>
              </a-space>
            </a-form-item>
          </a-form>
          <div class="range-strip">
            <button
              v-for="preset in rangePresets"
              :key="preset.key"
              class="range-chip"
              type="button"
              @click="applyRangePreset(preset.key)"
            >
              {{ preset.label }}
            </button>
            <button v-if="activeCategory" class="range-chip range-chip--ghost" type="button" @click="activeCategory = ''">
              清除类目筛选
            </button>
          </div>
          <div class="report-nav__summary">
            <span>当前范围</span>
            <strong>{{ dayjs(query.from).format('YYYY-MM') }} 至 {{ dayjs(query.to).format('YYYY-MM') }}</strong>
            <small v-if="activeCategory">当前类目筛选：{{ activeCategory }}</small>
          </div>
        </div>
      </section>

      <StatefulBlock :loading="loading" :error="errorMessage" @retry="loadCharts">
        <section class="metric-strip fade-up">
          <div v-for="item in metricCards" :key="item.label" class="metric-tile">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
            <small>{{ item.hint }}</small>
          </div>
        </section>

        <a-alert v-if="summary.warningMessage" type="warning" show-icon :message="summary.warningMessage" />

        <section class="report-stage">
          <article class="chart-stage chart-stage--dark fade-up">
            <div class="stage-head">
              <div>
                <div class="stage-kicker">Primary Trend</div>
                <h3>{{ pageMeta.leftChartTitle }}</h3>
              </div>
            </div>
            <div ref="revenueRef" class="chart-box"></div>
            <div class="stage-tags" v-if="(summary.topCategories || []).length && reportKey !== 'FLOOR_ROOM'">
              <button
                v-for="item in (summary.topCategories || []).slice(0, 6)"
                :key="`${item.label}-${item.extra}`"
                class="stage-chip"
                type="button"
                @click="activeCategory = item.extra || item.label"
              >
                <span>{{ item.label }}</span>
                <strong>{{ money(item.amount) }}</strong>
              </button>
            </div>
          </article>

          <article class="chart-stage fade-up">
            <div class="stage-head">
              <div>
                <div class="stage-kicker">Support View</div>
                <h3>{{ pageMeta.rightChartTitle }}</h3>
              </div>
            </div>
            <div ref="storeRef" class="chart-box chart-box--light"></div>
            <div class="snapshot-list">
              <div class="snapshot-row">
                <span>净营收</span>
                <strong>{{ money(summary.netRevenue ?? summary.totalRevenue) }}</strong>
              </div>
              <div class="snapshot-row">
                <span>开账金额</span>
                <strong>{{ money(summary.billedRevenue) }}</strong>
              </div>
              <div class="snapshot-row">
                <span>实收金额</span>
                <strong>{{ money(summary.totalReceived) }}</strong>
              </div>
              <div class="snapshot-row">
                <span>退款金额</span>
                <strong>{{ money(summary.refundTotal) }}</strong>
              </div>
            </div>
          </article>
        </section>

        <section class="table-stage">
          <article class="table-panel fade-up">
            <div class="stage-head">
              <div>
                <div class="stage-kicker">Primary Ranking</div>
                <h3>{{ pageMeta.leftTableTitle }}</h3>
              </div>
            </div>
            <vxe-table border stripe show-overflow :data="leftRows" height="280">
              <vxe-column field="label" :title="pageMeta.leftTableLabel" min-width="180" />
              <vxe-column field="amount" title="金额" width="140">
                <template #default="{ row }">{{ money(row.amount) }}</template>
              </vxe-column>
              <vxe-column v-if="showExtra" field="extra" title="编码" width="120" />
            </vxe-table>
          </article>

          <article class="table-panel fade-up">
            <div class="stage-head">
              <div>
                <div class="stage-kicker">Support Ranking</div>
                <h3>{{ pageMeta.rightTableTitle }}</h3>
              </div>
            </div>
            <vxe-table border stripe show-overflow :data="rightRows" height="280">
              <vxe-column field="label" :title="pageMeta.rightTableLabel" min-width="180" />
              <vxe-column field="amount" title="金额" width="140">
                <template #default="{ row }">{{ money(row.amount) }}</template>
              </vxe-column>
            </vxe-table>
          </article>
        </section>

        <section class="support-stage">
          <article class="table-panel fade-up">
            <div class="stage-head">
              <div>
                <div class="stage-kicker">Receivable Risk</div>
                <h3>{{ pageMeta.arrearsTitle }}</h3>
              </div>
            </div>
            <vxe-table border stripe show-overflow :data="arrears" height="280">
              <vxe-column field="elderName" title="长者" min-width="160">
                <template #default="{ row }">
                  <span>{{ row.elderName || '未知长者' }}</span>
                </template>
              </vxe-column>
              <vxe-column field="outstandingAmount" title="欠费金额" width="140">
                <template #default="{ row }">{{ money(row.outstandingAmount) }}</template>
              </vxe-column>
            </vxe-table>
          </article>

          <article class="table-panel fade-up">
            <div class="stage-head">
              <div>
                <div class="stage-kicker">Report Actions</div>
                <h3>常用跳转</h3>
              </div>
            </div>
            <div class="action-grid">
              <button class="action-card" type="button" @click="go('/finance/workbench')">
                <strong>返回工作台</strong>
                <span>回到今日任务首页继续处理收费与对账。</span>
              </button>
              <button class="action-card" type="button" @click="go('/finance/reconcile/issue-center')">
                <strong>查看异常修复</strong>
                <span>从报表直接跳去处理差异和挂账问题。</span>
              </button>
              <button class="action-card" type="button" @click="go('/finance/bills/follow-up')">
                <strong>欠费催缴</strong>
                <span>把欠费排行直接转成催缴跟进动作。</span>
              </button>
              <button class="action-card" type="button" @click="go('/finance/reports/room-ops-detail?period=this_month')">
                <strong>房间经营详情</strong>
                <span>继续下钻到房间、楼栋和床位表现。</span>
              </button>
            </div>
          </article>
        </section>

        <section class="category-stage fade-up">
          <div class="category-stage__head">
            <div>
              <div class="stage-kicker">Category Analysis</div>
              <h3>类目消费比例与盈利分析</h3>
            </div>
            <a-space wrap>
              <a-button type="primary" @click="loadCategoryAnalysis">查询</a-button>
              <a-button @click="queryAllCategories">查看全部类目</a-button>
              <a-button @click="exportCategoryProfit">导出盈利报表</a-button>
              <a-button @click="exportCategoryTrend">导出趋势</a-button>
              <a-button @click="printCategoryAnalysis">打印</a-button>
            </a-space>
          </div>

          <a-form layout="inline" :model="categoryQuery" class="search-form">
            <a-form-item label="开始日期">
              <a-date-picker v-model:value="categoryQuery.from" />
            </a-form-item>
            <a-form-item label="结束日期">
              <a-date-picker v-model:value="categoryQuery.to" />
            </a-form-item>
            <a-form-item label="物品关键字">
              <a-input v-model:value="categoryQuery.itemKeyword" allow-clear placeholder="例如：鸡蛋" style="width: 180px" />
            </a-form-item>
            <a-form-item label="楼栋">
              <a-input v-model:value="categoryQuery.building" allow-clear placeholder="例如：A栋" style="width: 140px" />
            </a-form-item>
            <a-form-item label="楼层">
              <a-input v-model:value="categoryQuery.floorNo" allow-clear placeholder="例如：2F" style="width: 120px" />
            </a-form-item>
          </a-form>

          <div class="category-metrics">
            <div class="category-metric">
              <span>区间总消费</span>
              <strong>{{ money(categoryAnalysis.totalAmount) }}</strong>
            </div>
            <div class="category-metric">
              <span>关键字消费额</span>
              <strong>{{ money(categoryAnalysis.itemAmount) }}</strong>
            </div>
            <div class="category-metric">
              <span>关键字消费占比</span>
              <strong>{{ (Number(categoryAnalysis.itemRatio || 0) * 100).toFixed(2) }}%</strong>
            </div>
          </div>

          <div class="category-grid">
            <article class="table-panel table-panel--plain">
              <div class="stage-head">
                <div>
                  <div class="stage-kicker">Trend</div>
                  <h3>关键字消费波动线</h3>
                </div>
              </div>
              <div ref="categoryTrendRef" class="chart-box chart-box--light"></div>
            </article>

            <article class="table-panel table-panel--plain">
              <div class="stage-head">
                <div>
                  <div class="stage-kicker">Profitability</div>
                  <h3>全类目盈利情况</h3>
                </div>
              </div>
              <vxe-table border stripe show-overflow :data="categoryAnalysis.categoryProfit || []" height="320">
                <vxe-column field="category" title="类目" min-width="120" />
                <vxe-column field="totalAmount" title="消费额" width="110">
                  <template #default="{ row }">{{ money(row.totalAmount) }}</template>
                </vxe-column>
                <vxe-column field="totalCost" title="成本" width="110">
                  <template #default="{ row }">{{ money(row.totalCost) }}</template>
                </vxe-column>
                <vxe-column field="totalProfit" title="利润" width="110">
                  <template #default="{ row }">{{ money(row.totalProfit) }}</template>
                </vxe-column>
                <vxe-column field="profitRate" title="利润率" width="110">
                  <template #default="{ row }">
                    {{ (Number(row.profitRate || 0) * 100).toFixed(2) }}%
                  </template>
                </vxe-column>
              </vxe-table>
            </article>
          </div>
        </section>
      </StatefulBlock>
    </div>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
import { getFinanceArrearsTop, getFinanceCategoryConsumptionAnalysis, getFinanceMonthlyRevenue, getFinanceReportEntrySummary, getFinanceStoreSales } from '../../api/finance'
import type {
  FinanceArrearsItem,
  FinanceCategoryConsumptionAnalysis,
  FinanceNameAmountItem,
  FinanceReportEntrySummary,
  FinanceReportMonthlyItem,
  FinanceStoreSalesItem
} from '../../types'
import { useECharts } from '../../plugins/echarts'
import { exportCsv } from '../../utils/export'

const route = useRoute()
const router = useRouter()
const query = ref({
  from: dayjs().subtract(5, 'month'),
  to: dayjs()
})
const activeCategory = ref('')

const loading = ref(false)
const errorMessage = ref('')
const arrears = ref<FinanceArrearsItem[]>([])
const monthlyRevenueRows = ref<FinanceReportMonthlyItem[]>([])
const storeSalesRows = ref<FinanceStoreSalesItem[]>([])
const summary = ref<FinanceReportEntrySummary>({
  reportKey: 'OVERALL',
  periodFrom: '',
  periodTo: '',
  totalRevenue: 0,
  billedRevenue: 0,
  totalReceived: 0,
  refundTotal: 0,
  netRevenue: 0,
  totalStoreSales: 0,
  arrearsTotal: 0,
  arrearsElderCount: 0,
  warningMessage: '',
  topCategories: [],
  topRooms: []
})

const { chartRef: revenueRef, setOption: setRevenueOption } = useECharts()
const { chartRef: storeRef, setOption: setStoreOption } = useECharts()
const { chartRef: categoryTrendRef, setOption: setCategoryTrendOption } = useECharts()
const categoryQuery = ref({
  from: dayjs().subtract(30, 'day'),
  to: dayjs(),
  itemKeyword: '鸡蛋',
  building: '',
  floorNo: ''
})
const categoryAnalysis = ref<FinanceCategoryConsumptionAnalysis>({
  itemKeyword: '',
  from: '',
  to: '',
  totalAmount: 0,
  itemAmount: 0,
  itemRatio: 0,
  trend: [],
  categoryProfit: []
})

const reportTabs = [
  { name: 'FinanceReportsOverall', path: '/finance/reports/overall', label: '总收支', hint: '净营收、现金流、欠费风险' },
  { name: 'FinanceReportsRevenueStructure', path: '/finance/reports/revenue-structure', label: '营收结构', hint: '费用类目、营收占比、退款影响' },
  { name: 'FinanceReportsFloorRoom', path: '/finance/reports/floor-room-profit', label: '楼层房间', hint: '楼层、房间、空床损失和收支表现' },
  { name: 'FinanceReportsOccupancyConsumption', path: '/finance/reports/occupancy-consumption', label: '入住消费', hint: '入住、床位和消费联动情况' },
  { name: 'FinanceReportsMonthlyOps', path: '/finance/reports/monthly-ops', label: '月运营', hint: '机构经营趋势和本月运营摘要' }
]
const rangePresets = [
  { key: 'thisMonth', label: '本月' },
  { key: 'last3', label: '近3个月' },
  { key: 'last6', label: '近6个月' },
  { key: 'last12', label: '近12个月' }
] as const

const pageMeta = computed(() => {
  const key = reportKey.value
  if (key === 'REVENUE_STRUCTURE') {
    return {
      title: '营收结构',
      subTitle: '把净营收、开账、实收和退款放到同一张结构化视图里。',
      leftChartTitle: '营收结构 Top',
      rightChartTitle: '净营收与退款趋势',
      leftTableTitle: '重点费用科目',
      rightTableTitle: '房间收支 Top',
      leftTableLabel: '费用科目',
      rightTableLabel: '房间',
      arrearsTitle: '欠费长者排行'
    }
  }
  if (key === 'FLOOR_ROOM') {
    return {
      title: '楼层房间收支',
      subTitle: '从楼层、房间和欠费视角看经营表现，快速识别高收益和低收益区域。',
      leftChartTitle: '房间收支 Top',
      rightChartTitle: '欠费风险分布',
      leftTableTitle: '房间收支 Top',
      rightTableTitle: '营收结构 Top',
      leftTableLabel: '房间',
      rightTableLabel: '费用科目',
      arrearsTitle: '欠费长者排行'
    }
  }
  if (key === 'OCCUPANCY_CONSUMPTION') {
    return {
      title: '入住与消费',
      subTitle: '聚合房间营收、消费类目和欠费情况，方便看入住经营质量。',
      leftChartTitle: '房间营收 Top',
      rightChartTitle: '消费类目结构',
      leftTableTitle: '房间收支 Top',
      rightTableTitle: '营收结构 Top',
      leftTableLabel: '房间',
      rightTableLabel: '费用科目',
      arrearsTitle: '欠费长者排行'
    }
  }
  if (key === 'MONTHLY_OPS') {
    return {
      title: '机构月运营',
      subTitle: '经营总览、结构变化和欠费风险放在一个月运营视图里。',
      leftChartTitle: '净营收与实收趋势',
      rightChartTitle: '营收结构',
      leftTableTitle: '重点费用科目',
      rightTableTitle: '房间收支 Top',
      leftTableLabel: '费用科目',
      rightTableLabel: '房间',
      arrearsTitle: '欠费长者排行'
    }
  }
  return {
    title: '总收支与现金流',
    subTitle: '默认先看净营收、实收、退款和欠费，判断本期经营是否健康。',
    leftChartTitle: '净营收趋势',
    rightChartTitle: '商城销售趋势',
    leftTableTitle: '重点费用科目',
    rightTableTitle: '房间收支 Top',
    leftTableLabel: '费用科目',
    rightTableLabel: '房间',
    arrearsTitle: '欠费长者排行'
  }
})

const reportKey = computed(() => {
  const name = String(route.name || '')
  if (name === 'FinanceReportsRevenueStructure') return 'REVENUE_STRUCTURE'
  if (name === 'FinanceReportsFloorRoom') return 'FLOOR_ROOM'
  if (name === 'FinanceReportsOccupancyConsumption') return 'OCCUPANCY_CONSUMPTION'
  if (name === 'FinanceReportsMonthlyOps') return 'MONTHLY_OPS'
  return 'OVERALL'
})

const leftRows = computed<FinanceNameAmountItem[]>(() => {
  const sourceRows = reportKey.value === 'FLOOR_ROOM' || reportKey.value === 'OCCUPANCY_CONSUMPTION'
    ? summary.value.topRooms || []
    : summary.value.topCategories || []
  if (!activeCategory.value || reportKey.value === 'FLOOR_ROOM' || reportKey.value === 'OCCUPANCY_CONSUMPTION') {
    return sourceRows
  }
  return sourceRows.filter(item => `${item.extra || item.label}`.toUpperCase().includes(activeCategory.value.toUpperCase()))
})

const rightRows = computed<FinanceNameAmountItem[]>(() => {
  if (reportKey.value === 'FLOOR_ROOM' || reportKey.value === 'OCCUPANCY_CONSUMPTION') {
    return summary.value.topCategories || []
  }
  return summary.value.topRooms || []
})

const showExtra = computed(() => reportKey.value !== 'FLOOR_ROOM' && reportKey.value !== 'OCCUPANCY_CONSUMPTION')

const metricCards = computed(() => [
  {
    label: '净营收',
    value: money(summary.value.netRevenue ?? summary.value.totalRevenue),
    hint: '实收减退款后的实际收入'
  },
  {
    label: '开账金额',
    value: money(summary.value.billedRevenue),
    hint: '本期账单应收合计'
  },
  {
    label: '实收金额',
    value: money(summary.value.totalReceived),
    hint: '支付成功并已入账金额'
  },
  {
    label: '退款金额',
    value: money(summary.value.refundTotal),
    hint: '已执行退款金额'
  },
  {
    label: '商城销售',
    value: money(summary.value.totalStoreSales),
    hint: '附加销售收入'
  },
  {
    label: '欠费金额',
    value: money(summary.value.arrearsTotal),
    hint: `欠费长者 ${summary.value.arrearsElderCount || 0} 人`
  }
])

function go(path: string) {
  router.push(path)
}

function money(value?: number) {
  return `¥${Number(value || 0).toFixed(2)}`
}

function applyRangePreset(preset: typeof rangePresets[number]['key']) {
  if (preset === 'thisMonth') {
    query.value.from = dayjs().startOf('month')
    query.value.to = dayjs().endOf('month')
  } else if (preset === 'last3') {
    query.value.from = dayjs().subtract(2, 'month').startOf('month')
    query.value.to = dayjs().endOf('month')
  } else if (preset === 'last12') {
    query.value.from = dayjs().subtract(11, 'month').startOf('month')
    query.value.to = dayjs().endOf('month')
  } else {
    query.value.from = dayjs().subtract(5, 'month').startOf('month')
    query.value.to = dayjs().endOf('month')
  }
  loadCharts()
}

function monthNetValue(item: FinanceReportMonthlyItem) {
  return Number(item.netAmount ?? item.amount ?? 0)
}

function monthReceivedValue(item: FinanceReportMonthlyItem) {
  return Number(item.receivedAmount ?? item.amount ?? 0)
}

function monthRefundValue(item: FinanceReportMonthlyItem) {
  return Number(item.refundAmount ?? 0)
}

async function loadCharts() {
  loading.value = true
  errorMessage.value = ''
  const params = {
    from: dayjs(query.value.from).format('YYYY-MM'),
    to: dayjs(query.value.to).format('YYYY-MM')
  }
  try {
    const [revenue, storeSales, arrearsData, summaryData] = await Promise.all([
      getFinanceMonthlyRevenue(params) as Promise<FinanceReportMonthlyItem[]>,
      getFinanceStoreSales(params) as Promise<FinanceStoreSalesItem[]>,
      getFinanceArrearsTop({ limit: 10 }) as Promise<FinanceArrearsItem[]>,
      getFinanceReportEntrySummary({
        reportKey: reportKey.value,
        from: params.from,
        to: params.to,
        top: 8
      })
    ])
    monthlyRevenueRows.value = revenue || []
    storeSalesRows.value = storeSales || []
    arrears.value = arrearsData || []
    summary.value = summaryData
    renderCharts()
    await loadCategoryAnalysis()
  } catch (error: any) {
    errorMessage.value = error?.message || '加载财务报表失败'
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

function renderCharts() {
  if (reportKey.value === 'REVENUE_STRUCTURE') {
    setRevenueOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: (summary.value.topCategories || []).map(item => item.label) },
      yAxis: { type: 'value' },
      series: [{ name: '营收结构', type: 'bar', data: (summary.value.topCategories || []).map(item => item.amount) }]
    })
    setStoreOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: monthlyRevenueRows.value.map(item => item.month) },
      yAxis: { type: 'value' },
      series: [
        { name: '净营收', type: 'line', smooth: true, data: monthlyRevenueRows.value.map(monthNetValue) },
        { name: '退款', type: 'bar', data: monthlyRevenueRows.value.map(monthRefundValue) }
      ]
    })
    return
  }
  if (reportKey.value === 'FLOOR_ROOM') {
    setRevenueOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: (summary.value.topRooms || []).map(item => item.label) },
      yAxis: { type: 'value' },
      series: [{ name: '房间收支', type: 'bar', data: (summary.value.topRooms || []).map(item => item.amount) }]
    })
    setStoreOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: arrears.value.map(item => item.elderName || '未知长者') },
      yAxis: { type: 'value' },
      series: [{ name: '欠费金额', type: 'bar', data: arrears.value.map(item => item.outstandingAmount) }]
    })
    return
  }
  if (reportKey.value === 'OCCUPANCY_CONSUMPTION') {
    setRevenueOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: (summary.value.topRooms || []).map(item => item.label) },
      yAxis: { type: 'value' },
      series: [{ name: '房间营收', type: 'bar', data: (summary.value.topRooms || []).map(item => item.amount) }]
    })
    setStoreOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: (summary.value.topCategories || []).map(item => item.label) },
      yAxis: { type: 'value' },
      series: [{ name: '费用结构', type: 'bar', data: (summary.value.topCategories || []).map(item => item.amount) }]
    })
    return
  }
  if (reportKey.value === 'MONTHLY_OPS') {
    setRevenueOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: monthlyRevenueRows.value.map(item => item.month) },
      yAxis: { type: 'value' },
      series: [
        { name: '净营收', type: 'line', smooth: true, data: monthlyRevenueRows.value.map(monthNetValue) },
        { name: '实收', type: 'bar', data: monthlyRevenueRows.value.map(monthReceivedValue) }
      ]
    })
    setStoreOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: (summary.value.topCategories || []).map(item => item.label) },
      yAxis: { type: 'value' },
      series: [{ name: '营收结构', type: 'bar', data: (summary.value.topCategories || []).map(item => item.amount) }]
    })
    return
  }
  setRevenueOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: monthlyRevenueRows.value.map(item => item.month) },
    yAxis: { type: 'value' },
    series: [
      { name: '净营收', type: 'line', smooth: true, data: monthlyRevenueRows.value.map(monthNetValue) },
      { name: '退款', type: 'bar', data: monthlyRevenueRows.value.map(monthRefundValue) }
    ]
  })
  setStoreOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: storeSalesRows.value.map(item => item.month) },
    yAxis: { type: 'value' },
    series: [{ name: '销售额', type: 'bar', data: storeSalesRows.value.map(item => item.amount) }]
  })
}

async function loadCategoryAnalysis() {
  if (dayjs(categoryQuery.value.from).isAfter(dayjs(categoryQuery.value.to), 'day')) {
    message.warning('类目分析开始日期不能晚于结束日期')
    return
  }
  const data = await getFinanceCategoryConsumptionAnalysis({
    from: dayjs(categoryQuery.value.from).format('YYYY-MM-DD'),
    to: dayjs(categoryQuery.value.to).format('YYYY-MM-DD'),
    itemKeyword: categoryQuery.value.itemKeyword || undefined,
    building: categoryQuery.value.building || undefined,
    floorNo: categoryQuery.value.floorNo || undefined
  })
  categoryAnalysis.value = data
  setCategoryTrendOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: (data.trend || []).map(item => item.period) },
    yAxis: [
      { type: 'value', name: '消费额' },
      { type: 'value', name: '占比(%)' }
    ],
    series: [
      { name: '关键字消费额', type: 'line', smooth: true, data: (data.trend || []).map(item => item.amount) },
      { name: '关键字占比', type: 'line', smooth: true, yAxisIndex: 1, data: (data.trend || []).map(item => Number(item.ratio || 0) * 100) }
    ]
  })
}

function exportCategoryProfit() {
  exportCsv(
    (categoryAnalysis.value.categoryProfit || []).map(item => ({
      类目: item.category,
      消费额: item.totalAmount,
      成本: item.totalCost,
      利润: item.totalProfit,
      利润率: `${(Number(item.profitRate || 0) * 100).toFixed(2)}%`
    })),
    `类目消费盈利-${dayjs().format('YYYYMMDD-HHmmss')}.csv`
  )
}

function exportCategoryTrend() {
  exportCsv(
    (categoryAnalysis.value.trend || []).map(item => ({
      日期: item.period,
      关键字消费额: item.amount,
      关键字占比: `${(Number(item.ratio || 0) * 100).toFixed(2)}%`
    })),
    `类目消费波动-${dayjs().format('YYYYMMDD-HHmmss')}.csv`
  )
}

function queryAllCategories() {
  categoryQuery.value.itemKeyword = ''
  loadCategoryAnalysis()
}

function printCategoryAnalysis() {
  const rows = categoryAnalysis.value.categoryProfit || []
  const win = window.open('', '_blank')
  if (!win) {
    message.error('请允许弹窗后重试打印')
    return
  }
  const html = `
    <html>
      <head><meta charset="utf-8"><title>类目消费盈利分析</title></head>
      <body>
        <h3>类目消费盈利分析（${categoryAnalysis.value.from} ~ ${categoryAnalysis.value.to}）</h3>
        <p>关键字：${categoryAnalysis.value.itemKeyword || '-'}</p>
        <p>楼栋/楼层筛选：${categoryQuery.value.building || '全部'} / ${categoryQuery.value.floorNo || '全部'}</p>
        <p>总消费：${categoryAnalysis.value.totalAmount}；关键字消费：${categoryAnalysis.value.itemAmount}；占比：${(Number(categoryAnalysis.value.itemRatio || 0) * 100).toFixed(2)}%</p>
        <table border="1" cellspacing="0" cellpadding="6">
          <thead><tr><th>类目</th><th>消费额</th><th>成本</th><th>利润</th><th>利润率</th></tr></thead>
          <tbody>
            ${rows.map(item => `<tr><td>${item.category}</td><td>${item.totalAmount}</td><td>${item.totalCost}</td><td>${item.totalProfit}</td><td>${(Number(item.profitRate || 0) * 100).toFixed(2)}%</td></tr>`).join('')}
          </tbody>
        </table>
      </body>
    </html>
  `
  win.document.write(html)
  win.document.close()
  win.focus()
  win.print()
}

function reset() {
  query.value.from = dayjs().subtract(5, 'month')
  query.value.to = dayjs()
  activeCategory.value = ''
  loadCharts()
}

onMounted(loadCharts)
watch(() => route.name, () => {
  loadCharts()
})
watch(() => route.query.category, (category) => {
  activeCategory.value = String(category || '').trim()
}, { immediate: true })
</script>

<style scoped>
.report-shell {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.fade-up {
  animation: fadeUp 0.42s ease both;
}

.report-nav {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(320px, 0.9fr);
  gap: 16px;
}

.report-nav__tabs {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(170px, 1fr));
  gap: 10px;
}

.report-tab {
  width: 100%;
  padding: 16px;
  text-align: left;
  border: 1px solid #dbe7f2;
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff 0%, #f7fafc 100%);
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
  cursor: pointer;
}

.report-tab:hover,
.action-card:hover,
.stage-chip:hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 34px rgba(15, 23, 42, 0.06);
}

.report-tab--active {
  border-color: #8bb9ff;
  background: linear-gradient(180deg, #eff6ff 0%, #f8fbff 100%);
}

.report-tab strong,
.action-card strong {
  display: block;
  color: #13263b;
}

.report-tab span,
.action-card span {
  display: block;
  margin-top: 6px;
  color: #66788c;
  line-height: 1.6;
}

.report-nav__controls {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 18px;
  border-radius: 22px;
  border: 1px solid #dbe7f2;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
}

.range-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.range-chip {
  padding: 8px 12px;
  border-radius: 999px;
  border: 1px solid #dbe7f2;
  background: #f7fafc;
  color: #26415f;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.range-chip:hover {
  transform: translateY(-1px);
  border-color: #bfd4ea;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.05);
}

.range-chip--ghost {
  color: #66788c;
}

.report-nav__summary span,
.metric-tile span,
.stage-kicker,
.category-metric span,
.snapshot-row span {
  display: block;
  font-size: 12px;
  color: #6f8298;
}

.report-nav__summary strong,
.metric-tile strong,
.category-metric strong,
.snapshot-row strong {
  display: block;
  margin-top: 8px;
  color: #13263b;
}

.report-nav__summary small {
  display: block;
  margin-top: 8px;
  color: #66788c;
}

.metric-strip {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 12px;
}

.metric-tile {
  min-height: 126px;
  padding: 18px;
  border-radius: 18px;
  border: 1px solid #dbe7f2;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
}

.metric-tile strong {
  font-size: 28px;
  line-height: 1.12;
}

.metric-tile small {
  display: block;
  margin-top: 12px;
  color: #66788c;
  line-height: 1.6;
}

.report-stage,
.table-stage,
.support-stage,
.category-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.chart-stage,
.table-panel,
.category-stage {
  padding: 22px;
  border-radius: 22px;
  border: 1px solid #dbe7f2;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  box-shadow: 0 18px 44px rgba(15, 23, 42, 0.05);
}

.chart-stage--dark {
  background:
    radial-gradient(circle at right top, rgba(59, 130, 246, 0.2), transparent 26%),
    linear-gradient(135deg, #0f172a 0%, #17263c 48%, #1d3656 100%);
  border-color: rgba(148, 163, 184, 0.18);
}

.chart-stage--dark h3,
.chart-stage--dark .stage-kicker,
.chart-stage--dark .stage-chip strong,
.chart-stage--dark .stage-chip span {
  color: #edf4fb;
}

.stage-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.stage-head h3 {
  margin: 8px 0 0;
  font-size: 24px;
  line-height: 1.2;
  color: #13263b;
}

.chart-box {
  height: 320px;
  margin-top: 12px;
}

.chart-box--light {
  height: 280px;
}

.stage-tags,
.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 10px;
  margin-top: 14px;
}

.stage-chip,
.action-card {
  width: 100%;
  padding: 12px 14px;
  text-align: left;
  border: 1px solid #dbe7f2;
  border-radius: 16px;
  background: linear-gradient(180deg, #ffffff 0%, #f7fafc 100%);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  cursor: pointer;
}

.chart-stage--dark .stage-chip {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(191, 212, 234, 0.18);
}

.snapshot-list,
.category-metrics {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 14px;
}

.snapshot-row,
.category-metric {
  padding: 12px 14px;
  border-radius: 14px;
  background: #f2f6fa;
}

.category-stage {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.category-stage__head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.category-stage__head h3 {
  margin: 8px 0 0;
  font-size: 24px;
  color: #13263b;
}

.table-panel--plain {
  box-shadow: none;
}

@keyframes fadeUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 1200px) {
  .report-nav,
  .report-stage,
  .table-stage,
  .support-stage,
  .category-grid {
    grid-template-columns: 1fr;
  }

  .metric-strip {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .metric-strip,
  .snapshot-list,
  .category-metrics {
    grid-template-columns: 1fr;
  }

  .report-nav__tabs {
    grid-template-columns: 1fr;
  }

  .chart-stage,
  .table-panel,
  .category-stage,
  .report-nav__controls {
    padding: 18px;
  }

  .category-stage__head,
  .stage-head {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
