<template>
  <PageContainer title="转换率统计" sub-title="线索到签约的漏斗转化分析">
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
          <a-select v-model:value="query.staffId" allow-clear style="width: 180px" show-search option-filter-prop="label">
            <a-select-option v-for="item in staffOptions" :key="item.value" :value="item.value" :label="item.label">
              {{ item.label }}
            </a-select-option>
          </a-select>
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
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import VChart from 'vue-echarts'
import PageContainer from '../../../components/PageContainer.vue'
import { getMarketingConversionReport } from '../../../api/marketing'
import { getStaffPage } from '../../../api/staff'
import { exportCsv } from '../../../utils/export'
import type { MarketingConversionReport, MarketingReportQuery, PageResult, StaffItem } from '../../../types'

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
const staffOptions = ref<Array<{ label: string; value: number }>>([])

const cards = computed(() => [
  { title: '咨询总量', value: report.value.consultCount },
  { title: '意向总量', value: report.value.intentCount },
  { title: '预订总量', value: report.value.reservationCount },
  { title: '签约总量', value: report.value.contractCount }
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
  if (stage === '咨询') router.push({ path: '/marketing/sales/consultation' })
  if (stage === '意向') router.push({ path: '/marketing/sales/intent' })
  if (stage === '预订') router.push({ path: '/marketing/sales/reservation' })
  if (stage === '失效') router.push({ path: '/marketing/sales/invalid' })
  if (stage === '签约') router.push({ path: '/marketing/contract-management' })
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
  loadData()
}

async function loadData() {
  report.value = await getMarketingConversionReport(query)
}

async function loadStaff() {
  try {
    const page: PageResult<StaffItem> = await getStaffPage({ pageNo: 1, pageSize: 200 })
    staffOptions.value = (page.list || []).map((item) => ({
      label: item.realName || item.username || String(item.id),
      value: item.id
    }))
  } catch {
    staffOptions.value = []
  }
}

onMounted(async () => {
  await loadStaff()
  await loadData()
})
</script>
