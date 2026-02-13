<template>
  <PageContainer title="绩效看板" subTitle="任务量、评价与积分排名">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="员工">
        <a-select
          v-model:value="query.staffId"
          allow-clear
          show-search
          :filter-option="false"
          placeholder="选择员工"
          style="width: 200px"
          @search="searchStaff"
        >
          <a-select-option v-for="item in staffOptions" :key="item.value" :value="item.value">
            {{ item.label }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="日期范围">
        <a-range-picker v-model:value="query.range" />
      </a-form-item>
      <a-form-item label="排序">
        <a-select v-model:value="query.sortBy" :options="sortOptions" style="width: 140px" />
      </a-form-item>
    </SearchForm>

    <a-card :bordered="false" class="card-elevated" style="margin-bottom: 16px">
      <a-row :gutter="16">
        <a-col :span="4"><a-statistic title="任务量" :value="summary.taskCount" /></a-col>
        <a-col :span="4"><a-statistic title="完成" :value="summary.successCount" /></a-col>
        <a-col :span="4"><a-statistic title="失败" :value="summary.failCount" /></a-col>
        <a-col :span="4"><a-statistic title="可疑" :value="summary.suspiciousCount" /></a-col>
        <a-col :span="4"><a-statistic title="平均评分" :value="summary.avgScore || 0" :precision="1" /></a-col>
        <a-col :span="4"><a-statistic title="积分余额" :value="summary.pointsBalance" /></a-col>
      </a-row>
    </a-card>

    <DataTable
      rowKey="rankNo"
      :columns="columns"
      :data-source="ranks"
      :loading="loading"
      :pagination="false"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'rankNo'">
          <a-tag :color="record.rankNo <= 3 ? 'gold' : 'default'">#{{ record.rankNo }}</a-tag>
        </template>
      </template>
    </DataTable>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import type { Dayjs } from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { getPerformanceSummary, getPerformanceRanking } from '../../api/hr'
import { getStaffPage } from '../../api/rbac'
import type { StaffPerformanceRankItem, StaffPerformanceSummary, PageResult, StaffItem } from '../../types'

const query = reactive({
  staffId: undefined as number | undefined,
  range: undefined as [Dayjs, Dayjs] | undefined,
  sortBy: 'points'
})

const sortOptions = [
  { label: '按积分', value: 'points' },
  { label: '按任务量', value: 'tasks' }
]

const summary = reactive<StaffPerformanceSummary>({
  staffId: 0,
  staffName: '',
  taskCount: 0,
  successCount: 0,
  failCount: 0,
  suspiciousCount: 0,
  avgScore: 0,
  pointsBalance: 0,
  pointsEarned: 0,
  pointsDeducted: 0
})

const ranks = ref<StaffPerformanceRankItem[]>([])
const loading = ref(false)
const staffOptions = ref<Array<{ label: string; value: number }>>([])

const columns = [
  { title: '排名', dataIndex: 'rankNo', key: 'rankNo', width: 80 },
  { title: '员工', dataIndex: 'staffName', key: 'staffName', width: 120 },
  { title: '任务量', dataIndex: 'taskCount', key: 'taskCount', width: 100 },
  { title: '完成', dataIndex: 'successCount', key: 'successCount', width: 100 },
  { title: '平均评分', dataIndex: 'avgScore', key: 'avgScore', width: 120 },
  { title: '周期积分', dataIndex: 'periodPoints', key: 'periodPoints', width: 120 },
  { title: '积分余额', dataIndex: 'pointsBalance', key: 'pointsBalance', width: 120 }
]

async function fetchData() {
  loading.value = true
  try {
    const params: any = {}
    if (query.range) {
      params.dateFrom = query.range[0].format('YYYY-MM-DD')
      params.dateTo = query.range[1].format('YYYY-MM-DD')
    }
    params.sortBy = query.sortBy
    const rankRes = await getPerformanceRanking(params)
    ranks.value = rankRes

    if (query.staffId) {
      const summaryRes = await getPerformanceSummary({
        staffId: Number(query.staffId),
        dateFrom: params.dateFrom,
        dateTo: params.dateTo
      })
      Object.assign(summary, summaryRes)
    } else {
      summary.taskCount = 0
      summary.successCount = 0
      summary.failCount = 0
      summary.suspiciousCount = 0
      summary.avgScore = 0
      summary.pointsBalance = 0
      summary.pointsEarned = 0
      summary.pointsDeducted = 0
    }
  } finally {
    loading.value = false
  }
}

function onReset() {
  query.staffId = undefined
  query.range = undefined
  query.sortBy = 'points'
  fetchData()
}

async function loadStaffOptions(keyword?: string) {
  const res: PageResult<StaffItem> = await getStaffPage({ pageNo: 1, pageSize: 50, keyword })
  staffOptions.value = res.list.map((item) => ({ label: item.realName || item.username, value: item.id }))
}

async function searchStaff(val: string) {
  await loadStaffOptions(val)
}

loadStaffOptions()
fetchData()
</script>
