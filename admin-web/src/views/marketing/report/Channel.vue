<template>
  <PageContainer title="来源渠道统计" sub-title="各渠道线索贡献与签约贡献">
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline">
        <a-form-item label="开始日期">
          <a-date-picker v-model:value="query.dateFrom" value-format="YYYY-MM-DD" />
        </a-form-item>
        <a-form-item label="结束日期">
          <a-date-picker v-model:value="query.dateTo" value-format="YYYY-MM-DD" />
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
      <v-chart :option="pieOption" autoresize style="height: 280px" />
    </a-card>
    <a-card class="card-elevated" :bordered="false">
      <a-table :columns="columns" :data-source="rows" row-key="source" :pagination="false" />
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import VChart from 'vue-echarts'
import PageContainer from '../../../components/PageContainer.vue'
import { getMarketingChannelReport } from '../../../api/marketing'
import { exportCsv } from '../../../utils/export'
import type { MarketingChannelReportItem, MarketingReportQuery } from '../../../types'

const rows = ref<Array<{ source: string; leadCount: number; reservationCount: number; contractCount: number }>>([])
const query = reactive<MarketingReportQuery>({})

const columns = [
  { title: '来源渠道', dataIndex: 'source', key: 'source' },
  { title: '线索数', dataIndex: 'leadCount', key: 'leadCount', width: 120 },
  { title: '预订数', dataIndex: 'reservationCount', key: 'reservationCount', width: 120 },
  { title: '签约数', dataIndex: 'contractCount', key: 'contractCount', width: 120 }
]

const pieOption = computed(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: 0 },
  series: [
    {
      type: 'pie',
      radius: ['40%', '70%'],
      data: rows.value.map((item) => ({ name: item.source, value: item.leadCount }))
    }
  ]
}))

async function loadData() {
  const report: MarketingChannelReportItem[] = await getMarketingChannelReport(query)
  rows.value = report.map((item) => ({
    source: item.source,
    leadCount: item.leadCount,
    reservationCount: item.reservationCount,
    contractCount: item.contractCount
  }))
}

function exportData() {
  exportCsv(rows.value.map((item) => ({
    渠道: item.source,
    线索数: item.leadCount,
    预订数: item.reservationCount,
    签约数: item.contractCount
  })), `marketing-channel-${Date.now()}.csv`)
}

onMounted(loadData)
</script>
