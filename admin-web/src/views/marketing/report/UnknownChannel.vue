<template>
  <PageContainer title="不明渠道统计" sub-title="渠道缺失与非标准来源监控">
    <MarketingQuickNav parent-path="/marketing/reports" />
    <a-row :gutter="16">
      <a-col :xs="24" :sm="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="缺失来源线索数" :value="quality.missingSourceCount" />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="缺失下次回访日期" :value="quality.missingNextFollowDateCount" />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="非标准来源线索数" :value="quality.nonStandardSourceCount" />
        </a-card>
      </a-col>
    </a-row>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px">
      <MarketingListToolbar :tip="`不明渠道共 ${quality.missingSourceCount} 条`">
        <a-space>
          <a-button type="primary" @click="loadData">刷新</a-button>
          <a-button @click="goUnknownLeads">去渠道不明客户池</a-button>
          <a-button @click="goChannelReport">去渠道评估</a-button>
        </a-space>
      </MarketingListToolbar>
      <a-table :columns="columns" :data-source="rows" :pagination="false" row-key="source">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'source'">
            <a @click="goSource(record.source)">{{ record.source }}</a>
          </template>
        </template>
      </a-table>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../../components/PageContainer.vue'
import MarketingQuickNav from '../components/MarketingQuickNav.vue'
import MarketingListToolbar from '../components/MarketingListToolbar.vue'
import { getMarketingChannelReport, getMarketingDataQualityReport } from '../../../api/marketing'
import { buildLeadRoute, routeToUnknownSourceOrAll } from '../../../utils/marketingNav'
import type { MarketingChannelReportItem, MarketingDataQualityReport } from '../../../types'

const router = useRouter()
const quality = ref<MarketingDataQualityReport>({
  missingSourceCount: 0,
  missingNextFollowDateCount: 0,
  nonStandardSourceCount: 0
})
const rows = ref<Array<{ source: string; leadCount: number; contractCount: number; conversionRate: string }>>([])

const columns = [
  { title: '渠道名称', dataIndex: 'source', key: 'source', width: 180 },
  { title: '线索数', dataIndex: 'leadCount', key: 'leadCount', width: 120 },
  { title: '签约数', dataIndex: 'contractCount', key: 'contractCount', width: 120 },
  { title: '转化率', dataIndex: 'conversionRate', key: 'conversionRate', width: 120 }
]

async function loadData() {
  const [qualityData, channelData] = await Promise.all([
    getMarketingDataQualityReport(),
    getMarketingChannelReport()
  ])
  quality.value = qualityData
  rows.value = (channelData || [])
    .filter((item: MarketingChannelReportItem) => !String(item.source || '').trim() || String(item.source || '').includes('不明'))
    .map((item: MarketingChannelReportItem) => ({
      source: item.source || '未标记渠道',
      leadCount: item.leadCount || 0,
      contractCount: item.contractCount || 0,
      conversionRate: item.leadCount ? `${(((item.contractCount || 0) / item.leadCount) * 100).toFixed(1)}%` : '0%'
    }))
}

function goUnknownLeads() {
  router.push(buildLeadRoute('unknown-source'))
}

function goChannelReport() {
  router.push('/marketing/reports/channel')
}

function goSource(source: string) {
  router.push(routeToUnknownSourceOrAll(source))
}

onMounted(loadData)
</script>
