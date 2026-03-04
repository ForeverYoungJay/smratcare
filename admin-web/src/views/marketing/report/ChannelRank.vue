<template>
  <PageContainer title="渠道贡献排名" sub-title="按签约贡献与转化率进行渠道排行">
    <MarketingQuickNav parent-path="/marketing/reports" />
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
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px">
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
import { onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../../components/PageContainer.vue'
import MarketingQuickNav from '../components/MarketingQuickNav.vue'
import { getMarketingChannelReport } from '../../../api/marketing'
import { useReportQueryCache } from '../../../composables/useReportQueryCache'
import { routeToUnknownSourceOrAll } from '../../../utils/marketingNav'
import type { MarketingChannelReportItem, MarketingReportQuery } from '../../../types'

const router = useRouter()
const query = reactive<MarketingReportQuery>({
  dateFrom: undefined,
  dateTo: undefined
})
const queryCache = useReportQueryCache<MarketingReportQuery>('channel-rank')

const rows = ref<Array<{
  rankNo: number
  source: string
  leadCount: number
  contractCount: number
  contributionRate: string
  conversionRate: string
}>>([])

const columns = [
  { title: '排名', dataIndex: 'rankNo', key: 'rankNo', width: 80 },
  { title: '渠道', dataIndex: 'source', key: 'source', width: 180 },
  { title: '线索数', dataIndex: 'leadCount', key: 'leadCount', width: 120 },
  { title: '签约数', dataIndex: 'contractCount', key: 'contractCount', width: 120 },
  { title: '签约贡献率', dataIndex: 'contributionRate', key: 'contributionRate', width: 140 },
  { title: '渠道转化率', dataIndex: 'conversionRate', key: 'conversionRate', width: 140 }
]

async function loadData() {
  const list = (await getMarketingChannelReport(query)) as MarketingChannelReportItem[]
  const totalContract = list.reduce((acc, item) => acc + Number(item.contractCount || 0), 0) || 1
  rows.value = (list || [])
    .map((item) => {
      const leadCount = Number(item.leadCount || 0)
      const contractCount = Number(item.contractCount || 0)
      return {
        source: item.source || '未标记渠道',
        leadCount,
        contractCount,
        contributionRate: `${((contractCount / totalContract) * 100).toFixed(1)}%`,
        conversionRate: leadCount ? `${((contractCount / leadCount) * 100).toFixed(1)}%` : '0%'
      }
    })
    .sort((a, b) => b.contractCount - a.contractCount || b.leadCount - a.leadCount)
    .map((item, index) => ({ ...item, rankNo: index + 1 }))
}

function goSource(source: string) {
  router.push(routeToUnknownSourceOrAll(source))
}

onMounted(() => {
  Object.assign(query, queryCache.restore())
  loadData()
})

watch(
  () => ({ ...query }),
  (value) => {
    queryCache.persist(value)
  },
  { deep: true }
)
</script>
