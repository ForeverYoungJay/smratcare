<template>
  <PageContainer :title="pageMeta.title" :subTitle="pageMeta.subTitle">
    <a-card class="card-elevated" :bordered="false">
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
            <a-button @click="go('/finance/workbench')">返回工作台</a-button>
          </a-space>
        </a-form-item>
      </a-form>
      <a-tag v-if="activeCategory" style="margin-top: 8px;" color="blue">当前分类：{{ activeCategory }}</a-tag>
    </a-card>

    <StatefulBlock style="margin-top: 16px;" :loading="loading" :error="errorMessage" @retry="loadCharts">
      <a-row :gutter="[16, 16]">
        <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="总营收" :value="summary.totalRevenue" suffix="元" :precision="2" /></a-card></a-col>
        <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="商城销售" :value="summary.totalStoreSales" suffix="元" :precision="2" /></a-card></a-col>
        <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="欠费金额" :value="summary.arrearsTotal" suffix="元" :precision="2" /></a-card></a-col>
        <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="欠费长者" :value="summary.arrearsElderCount" /></a-card></a-col>
      </a-row>
      <a-alert v-if="summary.warningMessage" style="margin-top: 12px;" type="warning" show-icon :message="summary.warningMessage" />

      <a-row :gutter="16" style="margin-top: 16px;">
        <a-col :xs="24" :xl="12">
          <a-card class="card-elevated" :bordered="false" :title="pageMeta.leftChartTitle">
            <div ref="revenueRef" style="height: 280px;"></div>
          </a-card>
        </a-col>
        <a-col :xs="24" :xl="12">
          <a-card class="card-elevated" :bordered="false" :title="pageMeta.rightChartTitle">
            <div ref="storeRef" style="height: 280px;"></div>
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16" style="margin-top: 16px;">
        <a-col :xs="24" :xl="12">
          <a-card class="card-elevated" :bordered="false" :title="pageMeta.leftTableTitle">
            <vxe-table border stripe show-overflow :data="leftRows" height="260">
              <vxe-column field="label" :title="pageMeta.leftTableLabel" min-width="180" />
              <vxe-column field="amount" title="金额" width="140" />
              <vxe-column v-if="showExtra" field="extra" title="编码" width="120" />
            </vxe-table>
          </a-card>
        </a-col>
        <a-col :xs="24" :xl="12">
          <a-card class="card-elevated" :bordered="false" :title="pageMeta.rightTableTitle">
            <vxe-table border stripe show-overflow :data="rightRows" height="260">
              <vxe-column field="label" :title="pageMeta.rightTableLabel" min-width="180" />
              <vxe-column field="amount" title="金额" width="140" />
            </vxe-table>
          </a-card>
        </a-col>
      </a-row>

      <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" :title="pageMeta.arrearsTitle">
        <vxe-table border stripe show-overflow :data="arrears" height="260">
          <vxe-column field="elderName" title="长者" min-width="160">
            <template #default="{ row }">
              <span>{{ row.elderName || '未知长者' }}</span>
            </template>
          </vxe-column>
          <vxe-column field="outstandingAmount" title="欠费金额" width="140" />
        </vxe-table>
      </a-card>
    </StatefulBlock>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
import { getFinanceArrearsTop, getFinanceMonthlyRevenue, getFinanceReportEntrySummary, getFinanceStoreSales } from '../../api/finance'
import type { FinanceArrearsItem, FinanceNameAmountItem, FinanceReportEntrySummary, FinanceReportMonthlyItem, FinanceStoreSalesItem } from '../../types'
import { useECharts } from '../../plugins/echarts'

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
const summary = ref<FinanceReportEntrySummary>({
  reportKey: 'OVERALL',
  periodFrom: '',
  periodTo: '',
  totalRevenue: 0,
  totalStoreSales: 0,
  arrearsTotal: 0,
  arrearsElderCount: 0,
  warningMessage: '',
  topCategories: [],
  topRooms: []
})

const { chartRef: revenueRef, setOption: setRevenueOption } = useECharts()
const { chartRef: storeRef, setOption: setStoreOption } = useECharts()

const pageMeta = computed(() => {
  const key = reportKey.value
  if (key === 'REVENUE_STRUCTURE') {
    return {
      title: '费用结构/营收占比',
      subTitle: '按费用科目查看营收构成与占比变化',
      leftChartTitle: '月营收趋势',
      rightChartTitle: '商城销售趋势',
      leftTableTitle: '营收结构 Top',
      rightTableTitle: '房间收支 Top',
      leftTableLabel: '费用科目',
      rightTableLabel: '房间',
      arrearsTitle: '欠费长者排行'
    }
  }
  if (key === 'FLOOR_ROOM') {
    return {
      title: '楼层/房间收支情况',
      subTitle: '按房间经营表现识别高收益与低收益区域',
      leftChartTitle: '月营收趋势',
      rightChartTitle: '商城销售趋势',
      leftTableTitle: '房间收支 Top',
      rightTableTitle: '营收结构 Top',
      leftTableLabel: '房间',
      rightTableLabel: '费用科目',
      arrearsTitle: '欠费长者排行'
    }
  }
  if (key === 'OCCUPANCY_CONSUMPTION') {
    return {
      title: '入住/床位/消费统计',
      subTitle: '聚合入住消费、房间收支和欠费风险',
      leftChartTitle: '月营收趋势',
      rightChartTitle: '商城销售趋势',
      leftTableTitle: '房间收支 Top',
      rightTableTitle: '营收结构 Top',
      leftTableLabel: '房间',
      rightTableLabel: '费用科目',
      arrearsTitle: '欠费长者排行'
    }
  }
  if (key === 'MONTHLY_OPS') {
    return {
      title: '机构月运营详情',
      subTitle: '经营总览、风险预警与科目结构联动看板',
      leftChartTitle: '月营收趋势',
      rightChartTitle: '商城销售趋势',
      leftTableTitle: '营收结构 Top',
      rightTableTitle: '房间收支 Top',
      leftTableLabel: '费用科目',
      rightTableLabel: '房间',
      arrearsTitle: '欠费长者排行'
    }
  }
  return {
    title: '总收支报表/现金流',
    subTitle: '营收趋势、欠费排行与经营风险摘要',
    leftChartTitle: '月营收趋势',
    rightChartTitle: '商城销售趋势',
    leftTableTitle: '营收结构 Top',
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
  return sourceRows.filter(item => (item.extra || '').toUpperCase() === activeCategory.value.toUpperCase())
})

const rightRows = computed<FinanceNameAmountItem[]>(() => {
  if (reportKey.value === 'FLOOR_ROOM' || reportKey.value === 'OCCUPANCY_CONSUMPTION') {
    return summary.value.topCategories || []
  }
  return summary.value.topRooms || []
})

const showExtra = computed(() => reportKey.value !== 'FLOOR_ROOM' && reportKey.value !== 'OCCUPANCY_CONSUMPTION')

function go(path: string) {
  router.push(path)
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
    arrears.value = arrearsData || []
    summary.value = summaryData
    setRevenueOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: revenue.map(item => item.month) },
      yAxis: { type: 'value' },
      series: [{ name: '营收', type: 'line', data: revenue.map(item => item.amount) }]
    })

    setStoreOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: storeSales.map(item => item.month) },
      yAxis: { type: 'value' },
      series: [{ name: '销售额', type: 'bar', data: storeSales.map(item => item.amount) }]
    })
  } catch (error: any) {
    errorMessage.value = error?.message || '加载财务报表失败'
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
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
