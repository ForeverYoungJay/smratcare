<template>
  <PageContainer title="营销跟进统计" sub-title="按状态查看跟进推进情况">
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline">
        <a-form-item label="开始日期">
          <a-date-picker v-model:value="query.dateFrom" value-format="YYYY-MM-DD" />
        </a-form-item>
        <a-form-item label="结束日期">
          <a-date-picker v-model:value="query.dateTo" value-format="YYYY-MM-DD" />
        </a-form-item>
        <a-form-item label="渠道">
          <a-input v-model:value="query.source" placeholder="渠道" allow-clear />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="loadData">查询</a-button>
            <a-button @click="exportData">导出CSV</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>
    <a-row :gutter="16">
      <a-col :xs="24" :sm="6" v-for="item in cards" :key="item.title">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic :title="item.title" :value="item.value" />
        </a-card>
      </a-col>
    </a-row>
    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px">
      <a-row :gutter="16">
        <a-col :span="8">
          <a-statistic title="缺失渠道" :value="quality.missingSourceCount" />
        </a-col>
        <a-col :span="8">
          <a-statistic title="缺失回访日期" :value="quality.missingNextFollowDateCount" />
        </a-col>
        <a-col :span="8">
          <a-statistic title="非标准渠道" :value="quality.nonStandardSourceCount" />
        </a-col>
      </a-row>
      <div style="margin-top: 12px; text-align: right">
        <a-button @click="normalizeSource">标准化渠道数据</a-button>
      </div>
    </a-card>
    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px">
      <a-table :columns="columns" :data-source="rows" :pagination="false" row-key="name">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'name'">
            <a @click="drillDown(record.name)">{{ record.name }}</a>
          </template>
        </template>
      </a-table>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../../components/PageContainer.vue'
import {
  getMarketingDataQualityReport,
  getMarketingFollowupReport,
  normalizeMarketingSources
} from '../../../api/marketing'
import { exportCsv } from '../../../utils/export'
import type { MarketingDataQualityReport, MarketingFollowupReport, MarketingReportQuery } from '../../../types'

const router = useRouter()
const report = ref<MarketingFollowupReport>({
  totalLeads: 0,
  consultCount: 0,
  intentCount: 0,
  reservationCount: 0,
  invalidCount: 0,
  overdueCount: 0,
  stages: []
})
const quality = ref<MarketingDataQualityReport>({
  missingSourceCount: 0,
  missingNextFollowDateCount: 0,
  nonStandardSourceCount: 0
})
const query = ref<MarketingReportQuery>({})

const cards = computed(() => [
  { title: '总线索', value: report.value.totalLeads },
  { title: '咨询中', value: report.value.consultCount },
  { title: '意向中', value: report.value.intentCount },
  { title: '逾期回访', value: report.value.overdueCount }
])

const columns = [
  { title: '阶段', dataIndex: 'name', key: 'name' },
  { title: '数量', dataIndex: 'count', key: 'count' },
  { title: '占比', dataIndex: 'rate', key: 'rate' }
]

const rows = computed(() => {
  return (report.value.stages || []).map((item) => ({
    name: item.name,
    count: item.count,
    rate: `${item.rate}%`
  }))
})

async function loadData() {
  const [followup, qualityData] = await Promise.all([
    getMarketingFollowupReport(query.value),
    getMarketingDataQualityReport()
  ])
  report.value = followup
  quality.value = qualityData
}

function exportData() {
  exportCsv(rows.value.map((item) => ({
    阶段: item.name,
    数量: item.count,
    占比: item.rate
  })), `marketing-followup-${Date.now()}.csv`)
}

function drillDown(stage: string) {
  if (stage === '咨询管理') router.push({ path: '/marketing/sales/pipeline', query: { tab: 'consultation' } })
  if (stage === '意向客户') router.push({ path: '/marketing/sales/pipeline', query: { tab: 'intent' } })
  if (stage === '预订管理') router.push('/marketing/sales/reservation')
  if (stage === '失效用户') router.push('/marketing/sales/invalid')
}

async function normalizeSource() {
  const updated = await normalizeMarketingSources()
  message.success(`已标准化 ${updated} 条渠道数据`)
  await loadData()
}

onMounted(loadData)
</script>
