<template>
  <PageContainer title="转换率统计" sub-title="线索到签约的漏斗转化分析">
    <MarketingQuickNav parent-path="/marketing/reports" />
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline">
        <a-form-item label="开始日期">
          <a-date-picker v-model:value="query.dateFrom" value-format="YYYY-MM-DD" />
        </a-form-item>
        <a-form-item label="结束日期">
          <a-date-picker v-model:value="query.dateTo" value-format="YYYY-MM-DD" />
        </a-form-item>
        <a-form-item label="渠道">
          <a-select v-model:value="query.source" allow-clear style="width: 140px">
            <a-select-option value="抖音">抖音</a-select-option>
            <a-select-option value="微信">微信</a-select-option>
            <a-select-option value="线上咨询">线上咨询</a-select-option>
            <a-select-option value="自然到访">自然到访</a-select-option>
            <a-select-option value="转介绍">转介绍</a-select-option>
            <a-select-option value="社区活动">社区活动</a-select-option>
            <a-select-option value="其他">其他</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="跟进人">
          <a-select
            v-model:value="query.staffId"
            allow-clear
            style="width: 220px"
            show-search
            :filter-option="false"
            :options="staffOptions"
            placeholder="输入员工姓名/拼音首字母"
            @search="searchStaff"
            @focus="() => !staffOptions.length && searchStaff('')"
          />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="loadData">查询</a-button>
            <a-button @click="reset">重置</a-button>
            <a-button @click="exportData">导出CSV</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-row :gutter="16">
      <a-col :xs="12" :sm="6" v-for="item in cards" :key="item.title">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic :title="item.title" :value="item.value" />
          <a style="font-size: 12px;" @click="drillDown(item.stage)">查看明细</a>
        </a-card>
      </a-col>
    </a-row>
    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px">
      <v-chart :option="funnelOption" autoresize style="height: 280px" />
    </a-card>
    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px">
      <a-table :columns="columns" :data-source="rows" :pagination="false" row-key="stage">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'stage'">
            <a @click="drillDown(record.stage)">{{ record.stage }}</a>
          </template>
        </template>
      </a-table>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import VChart from 'vue-echarts'
import PageContainer from '../../../components/PageContainer.vue'
import MarketingQuickNav from '../components/MarketingQuickNav.vue'
import { getMarketingConversionReport } from '../../../api/marketing'
import { useStaffOptions } from '../../../composables/useStaffOptions'
import { useReportQueryCache } from '../../../composables/useReportQueryCache'
import { buildContractRoute, buildLeadRoute, buildReservationRoute } from '../../../utils/marketingNav'
import { exportCsv } from '../../../utils/export'
import type { MarketingConversionReport, MarketingReportQuery } from '../../../types'

const router = useRouter()
const report = ref<MarketingConversionReport>({
  totalLeads: 0,
  consultCount: 0,
  intentCount: 0,
  reservationCount: 0,
  invalidCount: 0,
  contractCount: 0,
  intentRate: 0,
  reservationRate: 0,
  contractRate: 0
})
const query = reactive<MarketingReportQuery>({})
const queryCache = useReportQueryCache<MarketingReportQuery>('conversion')
const staffOptions = ref<Array<{ label: string; value: number }>>([])
const { staffOptions: staffOptionPool, searchStaff: searchStaffPool } = useStaffOptions({ pageSize: 220, preloadSize: 500 })

const cards = computed(() => [
  { title: '咨询总量', value: report.value.consultCount, stage: '咨询' },
  { title: '意向总量', value: report.value.intentCount, stage: '意向' },
  { title: '预订总量', value: report.value.reservationCount, stage: '预订' },
  { title: '签约总量', value: report.value.contractCount, stage: '签约' }
])

const columns = [
  { title: '阶段', dataIndex: 'stage', key: 'stage' },
  { title: '数量', dataIndex: 'count', key: 'count' },
  { title: '相对总线索占比', dataIndex: 'rate', key: 'rate' }
]

function ratio(count: number, total: number) {
  if (!total) return '0.0%'
  return `${((count / total) * 100).toFixed(1)}%`
}

const rows = computed(() => [
  { stage: '咨询', count: report.value.consultCount, rate: ratio(report.value.consultCount, report.value.totalLeads) },
  { stage: '意向', count: report.value.intentCount, rate: ratio(report.value.intentCount, report.value.totalLeads) },
  { stage: '预订', count: report.value.reservationCount, rate: ratio(report.value.reservationCount, report.value.totalLeads) },
  { stage: '失效', count: report.value.invalidCount, rate: ratio(report.value.invalidCount, report.value.totalLeads) },
  { stage: '签约', count: report.value.contractCount, rate: ratio(report.value.contractCount, report.value.totalLeads) }
])

const funnelOption = computed(() => ({
  tooltip: { trigger: 'item' },
  series: [
    {
      type: 'funnel',
      left: '10%',
      top: 20,
      bottom: 20,
      width: '80%',
      minSize: '20%',
      maxSize: '100%',
      label: { show: true, position: 'inside' },
      data: [
        { name: '咨询', value: report.value.consultCount },
        { name: '意向', value: report.value.intentCount },
        { name: '预订', value: report.value.reservationCount },
        { name: '签约', value: report.value.contractCount }
      ]
    }
  ]
}))

function drillDown(stage: string) {
  const commonQuery = {
    source: query.source,
    consultDateFrom: query.dateFrom,
    consultDateTo: query.dateTo
  }
  if (stage === '咨询') router.push(buildLeadRoute('all', { tab: 'consultation', ...commonQuery }))
  if (stage === '意向') router.push(buildLeadRoute('intent', commonQuery))
  if (stage === '预订') router.push(buildReservationRoute('records', commonQuery))
  if (stage === '失效') router.push(buildLeadRoute('invalid', commonQuery))
  if (stage === '签约') router.push(buildContractRoute('signed'))
}

function exportData() {
  exportCsv(rows.value.map((item) => ({
    阶段: item.stage,
    数量: item.count,
    占比: item.rate
  })), `marketing-conversion-${Date.now()}.csv`)
}

function reset() {
  query.dateFrom = undefined
  query.dateTo = undefined
  query.source = undefined
  query.staffId = undefined
  queryCache.clear()
  loadData()
}

async function loadData() {
  report.value = await getMarketingConversionReport(query)
}

async function searchStaff(keyword = '') {
  await searchStaffPool(keyword)
  staffOptions.value = staffOptionPool.value
    .map((item) => ({
      label: item.label,
      value: Number(item.value)
    }))
    .filter((item) => Number.isFinite(item.value))
}

onMounted(async () => {
  Object.assign(query, queryCache.restore())
  await searchStaff('')
  await loadData()
})

watch(
  () => ({ ...query }),
  (value) => {
    queryCache.persist(value)
  },
  { deep: true }
)
</script>
