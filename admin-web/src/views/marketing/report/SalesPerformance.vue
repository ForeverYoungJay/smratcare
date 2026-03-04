<template>
  <PageContainer title="销售人员业绩" sub-title="按签约数与签约金额进行排行">
    <MarketingQuickNav parent-path="/marketing/reports" />
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline">
        <a-form-item label="营销人员">
          <a-input v-model:value="query.marketerName" placeholder="姓名关键字" allow-clear />
        </a-form-item>
        <a-form-item label="流程阶段">
          <a-select v-model:value="query.flowStage" allow-clear style="width: 160px">
            <a-select-option value="PENDING_SIGN">待签署</a-select-option>
            <a-select-option value="SIGNED">已签署</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="loadData">查询</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px">
      <a-table :columns="columns" :data-source="rows" :pagination="false" row-key="marketerName">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'marketerName'">
            <a @click="goMarketerLeads(record.marketerName)">{{ record.marketerName }}</a>
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
import { getContractPage, getMarketingFollowupReport } from '../../../api/marketing'
import { useReportQueryCache } from '../../../composables/useReportQueryCache'
import { buildLeadRoute } from '../../../utils/marketingNav'
import type { CrmContractItem, MarketingFollowupReport, PageResult } from '../../../types'

const router = useRouter()
const query = reactive({
  marketerName: '',
  flowStage: undefined as string | undefined
})
const queryCache = useReportQueryCache<typeof query>('sales-performance')
const rows = ref<Array<{
  rankNo: number
  marketerName: string
  contractCount: number
  contractAmount: number
  conversionRate: string
  timelyRate: string
}>>([])

const columns = [
  { title: '排名', dataIndex: 'rankNo', key: 'rankNo', width: 80 },
  { title: '营销人员', dataIndex: 'marketerName', key: 'marketerName', width: 160 },
  { title: '签约数', dataIndex: 'contractCount', key: 'contractCount', width: 120 },
  { title: '签约金额', dataIndex: 'contractAmount', key: 'contractAmount', width: 140 },
  { title: '转化率', dataIndex: 'conversionRate', key: 'conversionRate', width: 120 },
  { title: '跟进及时率', dataIndex: 'timelyRate', key: 'timelyRate', width: 120 }
]

function toMoney(value: number) {
  return Number(value || 0).toFixed(2)
}

async function loadData() {
  const [contractPage, followup] = await Promise.all([
    getContractPage({ pageNo: 1, pageSize: 300, marketerName: query.marketerName || undefined, flowStage: query.flowStage || undefined }),
    getMarketingFollowupReport() as Promise<MarketingFollowupReport>
  ])
  const list = (contractPage as PageResult<CrmContractItem>).list || []
  const map = new Map<string, { count: number; amount: number }>()
  list.forEach((item) => {
    const key = String(item.marketerName || '未分配')
    const current = map.get(key) || { count: 0, amount: 0 }
    current.count += 1
    current.amount += Number(item.amount || item.contractAmount || 0)
    map.set(key, current)
  })
  const total = list.length || 1
  const timely = Number((((followup.totalLeads || 0) - (followup.overdueCount || 0)) / Math.max(followup.totalLeads || 1, 1) * 100).toFixed(1))
  rows.value = Array.from(map.entries())
    .map(([marketerName, item]) => ({
      marketerName,
      contractCount: item.count,
      contractAmount: Number(toMoney(item.amount)),
      conversionRate: `${((item.count / total) * 100).toFixed(1)}%`,
      timelyRate: `${timely}%`
    }))
    .sort((a, b) => b.contractCount - a.contractCount || b.contractAmount - a.contractAmount)
    .map((item, index) => ({ ...item, rankNo: index + 1 }))
}

function reset() {
  query.marketerName = ''
  query.flowStage = undefined
  loadData()
}

function goMarketerLeads(marketerName: string) {
  if (!marketerName || marketerName === '未分配') return
  router.push(buildLeadRoute('all', { marketerName }))
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
