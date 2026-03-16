<template>
  <PageContainer :title="title" :sub-title="subTitle">
    <MarketingQuickNav parent-path="/marketing/callback" />
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline">
        <a-form-item label="关键词">
          <a-input v-model:value="query.keyword" allow-clear placeholder="姓名/电话/备注" />
        </a-form-item>
        <a-form-item label="来源">
          <a-input v-model:value="query.source" allow-clear placeholder="渠道/来源" />
        </a-form-item>
        <a-form-item label="开始日期">
          <a-date-picker v-model:value="query.dateFrom" value-format="YYYY-MM-DD" />
        </a-form-item>
        <a-form-item label="结束日期">
          <a-date-picker v-model:value="query.dateTo" value-format="YYYY-MM-DD" />
        </a-form-item>
        <a-form-item label="仅逾期">
          <a-switch v-model:checked="query.overdueOnly" />
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
      <a-col :xs="24" :sm="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="本场景总回访" :value="filteredRows.length" />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="今日待回访" :value="todayDueCount" />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="逾期未回访" :value="overdueCount" />
        </a-card>
      </a-col>
    </a-row>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px">
      <MarketingListToolbar :tip="`当前筛选 ${filteredRows.length} 条`">
        <a-space>
          <a-button @click="goFollowup('today')">今日跟进计划</a-button>
          <a-button danger @click="goFollowup('overdue')">逾期未跟进</a-button>
          <a-button @click="goCallback('score')">回访质量评分</a-button>
        </a-space>
      </MarketingListToolbar>
      <a-table :columns="columns" :data-source="pagedRows" :pagination="false" row-key="id">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record._statusColor">{{ record._statusText }}</a-tag>
          </template>
          <template v-if="column.key === 'risk'">
            <a-tag :color="record._riskColor">{{ record._riskText }}</a-tag>
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
import { computed, onMounted, reactive, ref, watch } from 'vue'
import dayjs from 'dayjs'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import MarketingQuickNav from './components/MarketingQuickNav.vue'
import MarketingListToolbar from './components/MarketingListToolbar.vue'
import { getMarketingCallbackReport } from '../../api/marketing'
import { buildCallbackRoute, buildFollowupRoute } from '../../utils/marketingNav'
import type { MarketingCallbackItem } from '../../types'

const props = withDefaults(defineProps<{
  type: 'checkin' | 'trial' | 'discharge'
  title?: string
  subTitle?: string
}>(), {
  title: '回访列表',
  subTitle: '回访任务与执行情况'
})

type CallbackScenarioRow = MarketingCallbackItem & {
  _statusText: '今日待回访' | '逾期未回访' | '计划中'
  _statusColor: 'orange' | 'red' | 'blue'
  _riskText: '高风险' | '中风险' | '低风险'
  _riskColor: 'red' | 'gold' | 'green'
}

const router = useRouter()
const route = useRoute()
const today = dayjs().format('YYYY-MM-DD')
const allRows = ref<CallbackScenarioRow[]>([])

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  keyword: '',
  source: '',
  dateFrom: undefined as string | undefined,
  dateTo: undefined as string | undefined,
  overdueOnly: false
})

const columns = [
  { title: '姓名', dataIndex: 'name', key: 'name', width: 120 },
  { title: '电话', dataIndex: 'phone', key: 'phone', width: 140 },
  { title: '回访日期', dataIndex: 'nextFollowDate', key: 'nextFollowDate', width: 140 },
  { title: '回访状态', key: 'status', width: 120 },
  { title: '风险等级', key: 'risk', width: 120 },
  { title: '来源', dataIndex: 'source', key: 'source', width: 140 },
  { title: '备注', dataIndex: 'remark', key: 'remark' }
]

const filteredRows = computed(() => {
  const keyword = query.keyword.trim()
  return allRows.value.filter((item) => {
    if (keyword) {
      const haystack = `${item.name || ''} ${item.phone || ''} ${item.remark || ''}`
      if (!haystack.includes(keyword)) return false
    }
    if (query.source && !String(item.source || '').includes(query.source)) return false
    if (query.dateFrom && item.nextFollowDate && item.nextFollowDate < query.dateFrom) return false
    if (query.dateTo && item.nextFollowDate && item.nextFollowDate > query.dateTo) return false
    if (query.overdueOnly && item._statusText !== '逾期未回访') return false
    return true
  })
})

const pagedRows = computed(() => {
  const start = (query.pageNo - 1) * query.pageSize
  return filteredRows.value.slice(start, start + query.pageSize)
})

const todayDueCount = computed(() => filteredRows.value.filter((item) => item._statusText === '今日待回访').length)
const overdueCount = computed(() => filteredRows.value.filter((item) => item._statusText === '逾期未回访').length)

function buildStatus(nextFollowDate?: string) {
  if (!nextFollowDate) {
    return { text: '计划中', color: 'blue' as const }
  }
  if (nextFollowDate < today) {
    return { text: '逾期未回访', color: 'red' as const }
  }
  if (nextFollowDate === today) {
    return { text: '今日待回访', color: 'orange' as const }
  }
  return { text: '计划中', color: 'blue' as const }
}

function buildRisk(statusText: CallbackScenarioRow['_statusText']) {
  if (statusText === '逾期未回访') return { text: '高风险', color: 'red' as const }
  if (statusText === '今日待回访') return { text: '中风险', color: 'gold' as const }
  return { text: '低风险', color: 'green' as const }
}

async function loadData() {
  const report = await getMarketingCallbackReport({ pageNo: 1, pageSize: 500, type: props.type })
  allRows.value = (report.records || [])
    .map((item) => {
      const status = buildStatus(item.nextFollowDate)
      const risk = buildRisk(status.text)
      return {
        ...item,
        id: String(item.id),
        _statusText: status.text,
        _statusColor: status.color,
        _riskText: risk.text,
        _riskColor: risk.color
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
  query.source = ''
  query.dateFrom = undefined
  query.dateTo = undefined
  query.overdueOnly = false
}

function onPageChange(page: number) {
  query.pageNo = page
}

function onPageSizeChange(_current: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
}

function goFollowup(entry: 'records' | 'due' | 'today' | 'overdue') {
  router.push(buildFollowupRoute(entry))
}

function goCallback(entry: 'checkin' | 'trial' | 'discharge' | 'score') {
  router.push(buildCallbackRoute(entry))
}

function syncQueryByRoute() {
  const routeFilter = String(route.query.filter || '').trim()
  const routeSource = String(route.query.source || '').trim()
  const routeDateFrom = String(route.query.dateFrom || '').trim()
  const routeDateTo = String(route.query.dateTo || '').trim()
  const routeKeyword = String(route.query.keyword || '').trim()
  query.overdueOnly = routeFilter === 'overdue'
  if (routeFilter === 'today') {
    query.dateFrom = today
    query.dateTo = today
  } else {
    query.dateFrom = routeDateFrom || undefined
    query.dateTo = routeDateTo || undefined
  }
  query.source = routeSource
  query.keyword = routeKeyword
}

onMounted(loadData)
syncQueryByRoute()

watch(
  () => route.query,
  () => {
    syncQueryByRoute()
    query.pageNo = 1
  }
)
</script>
