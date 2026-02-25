<template>
  <PageContainer title="咨询统计" sub-title="近14天咨询线索新增趋势">
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline">
        <a-form-item label="天数">
          <a-input-number v-model:value="query.days" :min="7" :max="90" />
        </a-form-item>
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
    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px">
      <v-chart :option="lineOption" autoresize style="height: 280px" />
    </a-card>
    <a-card class="card-elevated" :bordered="false">
      <a-table :columns="columns" :data-source="rows" row-key="date" :pagination="false" />
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import VChart from 'vue-echarts'
import PageContainer from '../../../components/PageContainer.vue'
import { getMarketingConsultationTrend } from '../../../api/marketing'
import { exportCsv } from '../../../utils/export'
import type { MarketingConsultationTrendItem, MarketingReportQuery } from '../../../types'

const rows = ref<Array<{ date: string; total: number; consult: number; intent: number }>>([])
const query = reactive<MarketingReportQuery & { days: number }>({ days: 14 })
const columns = [
  { title: '日期', dataIndex: 'date', key: 'date', width: 140 },
  { title: '新增线索', dataIndex: 'total', key: 'total', width: 120 },
  { title: '咨询阶段', dataIndex: 'consult', key: 'consult', width: 120 },
  { title: '意向阶段', dataIndex: 'intent', key: 'intent', width: 120 }
]

const lineOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['新增线索', '咨询阶段', '意向阶段'] },
  xAxis: { type: 'category', data: rows.value.map((item) => item.date) },
  yAxis: { type: 'value' },
  series: [
    { name: '新增线索', type: 'line', data: rows.value.map((item) => item.total), smooth: true },
    { name: '咨询阶段', type: 'line', data: rows.value.map((item) => item.consult), smooth: true },
    { name: '意向阶段', type: 'line', data: rows.value.map((item) => item.intent), smooth: true }
  ]
}))

async function loadData() {
  const trend: MarketingConsultationTrendItem[] = await getMarketingConsultationTrend(query.days || 14, query)
  rows.value = trend.map((item) => ({
    date: item.date,
    total: item.total,
    consult: item.consultCount,
    intent: item.intentCount
  }))
}

function exportData() {
  exportCsv(rows.value.map((item) => ({
    日期: item.date,
    新增线索: item.total,
    咨询阶段: item.consult,
    意向阶段: item.intent
  })), `marketing-consultation-${Date.now()}.csv`)
}

onMounted(loadData)
</script>
