<template>
  <PageContainer title="转化率统计" sub-title="线索到签约的漏斗转化分析与阶段诊断">
    <MarketingQuickNav parent-path="/marketing/reports" />
    <section class="conversion-hero">
      <div class="conversion-hero-copy">
        <small>Conversion Intelligence</small>
        <h2>把咨询、意向、预订到签约的每一段损耗都看清楚</h2>
        <p>统一查看阶段数量、转化率和当前筛选口径，方便从渠道、时间和负责人三个维度定位问题。</p>
      </div>
      <div class="conversion-hero-side">
        <div class="hero-stat">
          <span>总线索</span>
          <strong>{{ report.totalLeads }}</strong>
        </div>
        <div class="hero-stat">
          <span>签约转化率</span>
          <strong>{{ report.contractRate.toFixed(1) }}%</strong>
        </div>
      </div>
    </section>

    <a-card class="card-elevated conversion-filter-card" :bordered="false">
      <a-form :model="query" layout="inline" class="conversion-filter-form">
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

    <a-row :gutter="[16, 16]" class="conversion-card-row">
      <a-col :xs="12" :sm="6" v-for="item in cards" :key="item.title">
        <a-card class="card-elevated conversion-stat-card" :bordered="false">
          <span class="conversion-stat-label">{{ item.title }}</span>
          <strong class="conversion-stat-value">{{ item.value }}</strong>
          <div class="conversion-stat-meta">
            <span>{{ item.rate }}</span>
            <a @click="drillDown(item.stage)">查看明细</a>
          </div>
        </a-card>
      </a-col>
    </a-row>
    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :xl="14">
        <a-card class="card-elevated conversion-chart-card" :bordered="false">
          <template #title>阶段漏斗</template>
          <v-chart :option="funnelOption" autoresize style="height: 320px" />
        </a-card>
      </a-col>
      <a-col :xs="24" :xl="10">
        <a-card class="card-elevated conversion-insight-card" :bordered="false">
          <template #title>阶段观察</template>
          <div class="insight-item">
            <span>咨询转意向</span>
            <strong>{{ report.intentRate.toFixed(1) }}%</strong>
            <small>判断咨询接待与资格筛选质量</small>
          </div>
          <div class="insight-item">
            <span>意向转预订</span>
            <strong>{{ report.reservationRate.toFixed(1) }}%</strong>
            <small>观察评估推进、锁床动作与回访执行</small>
          </div>
          <div class="insight-item">
            <span>预订转签约</span>
            <strong>{{ reservationToContractRate }}</strong>
            <small>反映合同推进、床位确认与签约闭环效率</small>
          </div>
        </a-card>
      </a-col>
    </a-row>
    <a-card class="card-elevated conversion-table-card" :bordered="false">
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
  { title: '咨询总量', value: report.value.consultCount, rate: ratio(report.value.consultCount, report.value.totalLeads), stage: '咨询' },
  { title: '意向总量', value: report.value.intentCount, rate: ratio(report.value.intentCount, report.value.totalLeads), stage: '意向' },
  { title: '预订总量', value: report.value.reservationCount, rate: ratio(report.value.reservationCount, report.value.totalLeads), stage: '预订' },
  { title: '签约总量', value: report.value.contractCount, rate: ratio(report.value.contractCount, report.value.totalLeads), stage: '签约' }
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

const reservationToContractRate = computed(() => {
  if (!report.value.reservationCount) return '0.0%'
  return `${((report.value.contractCount / report.value.reservationCount) * 100).toFixed(1)}%`
})

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

<style scoped>
.conversion-hero {
  display: flex;
  align-items: stretch;
  justify-content: space-between;
  gap: 18px;
  padding: 24px;
  border-radius: 26px;
  border: 1px solid rgba(26, 97, 133, 0.12);
  background:
    radial-gradient(circle at top right, rgba(44, 172, 214, 0.14), transparent 28%),
    radial-gradient(circle at left bottom, rgba(255, 193, 94, 0.16), transparent 26%),
    linear-gradient(135deg, #f8fcff 0%, #edf6fb 48%, #fffaf1 100%);
  box-shadow: 0 18px 42px rgba(13, 70, 108, 0.08);
}

.conversion-hero-copy {
  max-width: 720px;
}

.conversion-hero-copy small {
  display: block;
  margin-bottom: 10px;
  color: #6f869a;
  font-size: 11px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.conversion-hero-copy h2 {
  margin: 0;
  color: #14324d;
  font-size: 28px;
  line-height: 1.2;
}

.conversion-hero-copy p {
  margin: 12px 0 0;
  color: #607d95;
  line-height: 1.8;
}

.conversion-hero-side {
  display: grid;
  grid-template-columns: repeat(2, minmax(148px, 1fr));
  gap: 12px;
}

.hero-stat {
  display: grid;
  gap: 10px;
  padding: 16px 18px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(212, 225, 235, 0.82);
}

.hero-stat span {
  color: #67839b;
  font-size: 12px;
}

.hero-stat strong {
  color: #11314d;
  font-size: 30px;
  line-height: 1;
}

.conversion-filter-card,
.conversion-table-card {
  margin-top: 16px;
}

.conversion-filter-form {
  row-gap: 12px;
}

.conversion-card-row {
  margin-top: 16px;
}

.conversion-stat-card {
  min-height: 134px;
  display: grid;
  gap: 10px;
  align-content: space-between;
}

.conversion-stat-label {
  color: #617e96;
  font-size: 13px;
}

.conversion-stat-value {
  color: #14324d;
  font-size: 34px;
  line-height: 1;
  letter-spacing: -0.03em;
}

.conversion-stat-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: #6f869a;
  font-size: 12px;
}

.conversion-chart-card,
.conversion-insight-card {
  margin-top: 16px;
  height: 100%;
}

.conversion-insight-card {
  min-height: 100%;
}

.insight-item {
  display: grid;
  gap: 8px;
  padding: 16px 0;
  border-bottom: 1px solid rgba(224, 233, 239, 0.82);
}

.insight-item:last-child {
  border-bottom: 0;
}

.insight-item span {
  color: #5d7891;
  font-size: 13px;
}

.insight-item strong {
  color: #12314d;
  font-size: 28px;
  line-height: 1;
}

.insight-item small {
  color: #7a91a5;
  line-height: 1.6;
}

@media (max-width: 992px) {
  .conversion-hero {
    flex-direction: column;
  }

  .conversion-hero-side {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 768px) {
  .conversion-hero {
    padding: 18px;
  }

  .conversion-hero-copy h2 {
    font-size: 24px;
  }

  .conversion-hero-side {
    grid-template-columns: 1fr;
  }
}
</style>
