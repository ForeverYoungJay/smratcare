<template>
  <PageContainer title="回访质量评分" sub-title="回访评分、低分预警与复访处理闭环">
    <MarketingQuickNav parent-path="/marketing/callback" />
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline">
        <a-form-item label="姓名">
          <a-input v-model:value="query.keyword" allow-clear placeholder="姓名/电话" />
        </a-form-item>
        <a-form-item label="开始日期">
          <a-date-picker v-model:value="query.dateFrom" value-format="YYYY-MM-DD" />
        </a-form-item>
        <a-form-item label="结束日期">
          <a-date-picker v-model:value="query.dateTo" value-format="YYYY-MM-DD" />
        </a-form-item>
        <a-form-item label="仅低分">
          <a-switch v-model:checked="query.lowScoreOnly" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="onSearch">查询</a-button>
            <a-button @click="onReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="平均回访评分" :value="averageScore" :precision="1" suffix="/5" />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="低分预警数量（＜3分）" :value="lowScoreCount" />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="待复访数量" :value="revisitCount" />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="已完成回访" :value="completedCount" />
        </a-card>
      </a-col>
    </a-row>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px">
      <MarketingListToolbar :tip="`当前筛选 ${filteredRows.length} 条`">
        <a-space>
          <a-button @click="go('/marketing/callback/checkin')">入住后回访</a-button>
          <a-button @click="go('/marketing/callback/trial')">试住回访</a-button>
          <a-button @click="go('/marketing/callback/discharge')">退住回访</a-button>
          <a-button danger @click="go('/marketing/followup/overdue')">逾期未跟进</a-button>
        </a-space>
      </MarketingListToolbar>
      <a-table :columns="columns" :data-source="pagedRows" :pagination="false" row-key="id">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'score'">
            <a-tag :color="scoreColor(record._score)">{{ record._score.toFixed(1) }}</a-tag>
          </template>
          <template v-if="column.key === 'risk'">
            <a-tag :color="record._riskLevel === '高风险' ? 'red' : record._riskLevel === '中风险' ? 'gold' : 'green'">
              {{ record._riskLevel }}
            </a-tag>
          </template>
        </template>
      </a-table>
      <a-pagination
        style="margin-top: 16px; text-align: right"
        :current="query.pageNo"
        :page-size="query.pageSize"
        :total="filteredRows.length"
        show-size-changer
        @change="onPageChange"
        @showSizeChange="onPageSizeChange"
      />
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import MarketingQuickNav from './components/MarketingQuickNav.vue'
import MarketingListToolbar from './components/MarketingListToolbar.vue'
import { getMarketingCallbackReport } from '../../api/marketing'
import type { MarketingCallbackItem } from '../../types'

type CallbackQualityRow = MarketingCallbackItem & {
  _score: number
  _riskLevel: '高风险' | '中风险' | '低风险'
}

const router = useRouter()
const today = dayjs().format('YYYY-MM-DD')
const completedCount = ref(0)
const allRows = ref<CallbackQualityRow[]>([])

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  keyword: '',
  dateFrom: undefined as string | undefined,
  dateTo: undefined as string | undefined,
  lowScoreOnly: false
})

const columns = [
  { title: '姓名', dataIndex: 'name', key: 'name', width: 120 },
  { title: '电话', dataIndex: 'phone', key: 'phone', width: 140 },
  { title: '来源', dataIndex: 'source', key: 'source', width: 120 },
  { title: '回访日期', dataIndex: 'nextFollowDate', key: 'nextFollowDate', width: 140 },
  { title: '评分', key: 'score', width: 100 },
  { title: '风险等级', key: 'risk', width: 110 },
  { title: '备注', dataIndex: 'remark', key: 'remark' }
]

const filteredRows = computed(() => {
  const keyword = query.keyword.trim()
  return allRows.value.filter((item) => {
    if (keyword) {
      const haystack = `${item.name || ''} ${item.phone || ''}`
      if (!haystack.includes(keyword)) return false
    }
    if (query.dateFrom && item.nextFollowDate && item.nextFollowDate < query.dateFrom) return false
    if (query.dateTo && item.nextFollowDate && item.nextFollowDate > query.dateTo) return false
    if (query.lowScoreOnly && item._score >= 3) return false
    return true
  })
})

const pagedRows = computed(() => {
  const start = (query.pageNo - 1) * query.pageSize
  return filteredRows.value.slice(start, start + query.pageSize)
})

const averageScore = computed(() => {
  if (!filteredRows.value.length) return 0
  const sum = filteredRows.value.reduce((acc, item) => acc + item._score, 0)
  return Number((sum / filteredRows.value.length).toFixed(1))
})
const lowScoreCount = computed(() => filteredRows.value.filter((item) => item._score < 3).length)
const revisitCount = computed(() => filteredRows.value.filter((item) => item._riskLevel !== '低风险').length)

function scoreColor(score: number) {
  if (score < 3) return 'red'
  if (score < 4) return 'gold'
  return 'green'
}

function parseScore(item: MarketingCallbackItem): number {
  const remark = String(item.remark || '')
  const matched = remark.match(/([1-5](?:\.\d)?)\s*(?:分|星)/)
  if (matched?.[1]) return Number(matched[1])
  if (item.nextFollowDate && item.nextFollowDate < today) return 2.4
  return 4.2
}

function buildRiskLevel(item: MarketingCallbackItem, score: number): '高风险' | '中风险' | '低风险' {
  if (score < 3 || (item.nextFollowDate && item.nextFollowDate < today)) return '高风险'
  if (score < 4) return '中风险'
  return '低风险'
}

async function loadData() {
  const report = await getMarketingCallbackReport({ pageNo: 1, pageSize: 500 })
  completedCount.value = report.completed || 0
  allRows.value = (report.records || []).map((item) => {
    const score = parseScore(item)
    return {
      ...item,
      id: String(item.id),
      _score: score,
      _riskLevel: buildRiskLevel(item, score)
    }
  })
}

function onSearch() {
  query.pageNo = 1
}

function onReset() {
  query.pageNo = 1
  query.pageSize = 10
  query.keyword = ''
  query.dateFrom = undefined
  query.dateTo = undefined
  query.lowScoreOnly = false
}

function onPageChange(page: number) {
  query.pageNo = page
}

function onPageSizeChange(_current: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
}

function go(path: string) {
  router.push(path)
}

onMounted(loadData)
</script>
