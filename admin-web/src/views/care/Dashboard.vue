<template>
  <PageContainer title="护理看板" subTitle="任务完成与质量概览">
    <a-row :gutter="16">
      <a-col :xs="24" :md="6">
        <a-card class="stat-card" :bordered="false">
          <a-statistic title="今日任务" :value="stats.total" />
        </a-card>
      </a-col>
      <a-col :xs="24" :md="6">
        <a-card class="stat-card" :bordered="false">
          <a-statistic title="完成率" :value="stats.completionRate" suffix="%" />
        </a-card>
      </a-col>
      <a-col :xs="24" :md="6">
        <a-card class="stat-card" :bordered="false">
          <a-statistic title="异常数" :value="stats.exception" />
        </a-card>
      </a-col>
      <a-col :xs="24" :md="6">
        <a-card class="stat-card" :bordered="false">
          <a-statistic title="可疑数" :value="stats.suspicious" />
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" class="chart-row">
      <a-col :xs="24" :lg="16">
        <a-card :bordered="false" class="chart-card">
          <div class="card-title">任务完成趋势（按小时）</div>
          <div ref="trendRef" class="chart-box" />
        </a-card>
      </a-col>
      <a-col :xs="24" :lg="8">
        <a-card :bordered="false" class="chart-card">
          <div class="card-title">护工完成量排行</div>
          <div ref="rankRef" class="chart-box" />
        </a-card>
      </a-col>
    </a-row>

    <a-card :bordered="false" class="list-card">
      <div class="card-title">今日异常任务</div>
      <a-table
        row-key="taskDailyId"
        :columns="columns"
        :data-source="exceptionList"
        :pagination="false"
        :loading="loading"
      />
      <a-empty v-if="!loading && exceptionList.length === 0" description="暂无异常任务" />
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { getTodayTasks } from '../../api/care'
import { useStaffOptions } from '../../composables/useStaffOptions'
import type { CareTaskItem } from '../../types'
import { useECharts } from '../../plugins/echarts'
import { resolveCareError } from './careError'

const loading = ref(false)
const tasks = ref<CareTaskItem[]>([])
const staffLoaded = ref(false)
const { searchStaff, findStaffName } = useStaffOptions({ pageSize: 260, preloadSize: 500 })

const stats = computed(() => {
  const total = tasks.value.length
  const done = tasks.value.filter((t) => t.status === 'DONE').length
  const exception = tasks.value.filter((t) => t.status === 'EXCEPTION').length
  const suspicious = tasks.value.filter((t) => t.suspiciousFlag).length
  return {
    total,
    completionRate: total === 0 ? 0 : Math.round((done / total) * 100),
    exception,
    suspicious
  }
})

const exceptionList = computed(() => tasks.value.filter((t) => t.status === 'EXCEPTION').slice(0, 6))

const columns = [
  { title: '老人', dataIndex: 'elderName' },
  { title: '房间', dataIndex: 'roomNo' },
  { title: '任务', dataIndex: 'taskName' },
  { title: '计划时间', dataIndex: 'planTime' }
]

const { chartRef: trendRef, setOption: setTrend } = useECharts()
const { chartRef: rankRef, setOption: setRank } = useECharts()

function buildCharts(list: CareTaskItem[]) {
  const hours = Array.from({ length: 24 }, (_, i) => i.toString().padStart(2, '0'))
  const doneByHour = new Array(24).fill(0)
  const totalByHour = new Array(24).fill(0)
  list.forEach((task) => {
    if (!task.planTime) return
    const hour = new Date(task.planTime).getHours()
    totalByHour[hour] += 1
    if (task.status === 'DONE') doneByHour[hour] += 1
  })

  setTrend({
    grid: { left: 24, right: 24, top: 30, bottom: 24 },
    tooltip: { trigger: 'axis' },
    legend: { data: ['完成', '总量'] },
    xAxis: { type: 'category', data: hours },
    yAxis: { type: 'value' },
    series: [
      { name: '完成', type: 'line', data: doneByHour, smooth: true },
      { name: '总量', type: 'line', data: totalByHour, smooth: true }
    ]
  })

  const staffCountMap = new Map<string, number>()
  list.filter((t) => t.status === 'DONE').forEach((task) => {
    const key = task.staffId ? (findStaffName(task.staffId) || `员工#${task.staffId}`) : '未分配'
    staffCountMap.set(key, (staffCountMap.get(key) || 0) + 1)
  })
  const rank = Array.from(staffCountMap.entries())
    .sort((a, b) => b[1] - a[1])
    .slice(0, 6)

  setRank({
    grid: { left: 10, right: 10, top: 30, bottom: 24, containLabel: true },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: rank.map((r) => r[0]) },
    series: [{ type: 'bar', data: rank.map((r) => r[1]) }]
  })
}

async function load() {
  loading.value = true
  try {
    const page = await getTodayTasks({ pageNo: 1, pageSize: 200 })
    tasks.value = page.list || []
    await ensureStaffLoaded()
    buildCharts(tasks.value)
  } catch (error) {
    message.error(resolveCareError(error, '加载护理看板失败'))
    tasks.value = []
    buildCharts([])
  } finally {
    loading.value = false
  }
}

async function ensureStaffLoaded() {
  if (staffLoaded.value) return
  try {
    await searchStaff('')
    staffLoaded.value = true
  } catch (error) {
    message.error(resolveCareError(error, '加载护工信息失败'))
  }
}

onMounted(load)
</script>

<style scoped>
.stat-card {
  border-radius: 12px;
}
.chart-row {
  margin-top: 16px;
}
.chart-card {
  border-radius: 12px;
}
.chart-box {
  height: 320px;
}
.list-card {
  margin-top: 16px;
  border-radius: 12px;
}
.card-title {
  font-weight: 600;
  margin-bottom: 12px;
}
</style>
