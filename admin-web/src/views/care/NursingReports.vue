<template>
  <PageContainer title="护理报表" subTitle="护理执行总览与人员工作量分析">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="时间范围">
        <a-range-picker v-model:value="query.range" />
      </a-form-item>
    </SearchForm>

    <a-row :gutter="16" class="metrics-row">
      <a-col :span="6">
        <a-card class="metric-card" :bordered="false">
          <div class="metric-label">服务总数</div>
          <div class="metric-value">{{ summary.totalServices || 0 }}</div>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="metric-card" :bordered="false">
          <div class="metric-label">已完成</div>
          <div class="metric-value">{{ summary.completedServices || 0 }}</div>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="metric-card" :bordered="false">
          <div class="metric-label">完成率</div>
          <div class="metric-value">{{ pct(summary.completionRate) }}</div>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="metric-card" :bordered="false">
          <div class="metric-label">超时数</div>
          <div class="metric-value">{{ summary.overdueCount || 0 }}</div>
        </a-card>
      </a-col>
    </a-row>

    <a-card class="card-elevated" :bordered="false">
      <a-table :data-source="summary.staffWorkloads || []" :columns="columns" row-key="staffId" :pagination="false" />
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import dayjs, { type Dayjs } from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import { getNursingReportSummary } from '../../api/nursing'
import type { NursingReportSummary } from '../../types'

const query = reactive({
  range: undefined as [Dayjs, Dayjs] | undefined
})

const summary = reactive<NursingReportSummary>({
  totalServices: 0,
  completedServices: 0,
  completionRate: 0,
  overdueCount: 0,
  overdueRate: 0,
  planBookingCount: 0,
  planCompletedCount: 0,
  planAchievementRate: 0,
  staffWorkloads: []
})

const columns = [
  { title: '护理人员', dataIndex: 'staffName', key: 'staffName', width: 160 },
  { title: '预约总数', dataIndex: 'bookingCount', key: 'bookingCount', width: 120 },
  { title: '预约完成', dataIndex: 'completedBookingCount', key: 'completedBookingCount', width: 120 },
  { title: '执行总数', dataIndex: 'executeCount', key: 'executeCount', width: 120 },
  { title: '执行完成', dataIndex: 'completedExecuteCount', key: 'completedExecuteCount', width: 120 },
  { title: '综合负荷', dataIndex: 'totalLoad', key: 'totalLoad', width: 120 }
]

function pct(value?: number | null) {
  if (value == null) return '0%'
  return `${(value * 100).toFixed(1)}%`
}

async function fetchData() {
  const res = await getNursingReportSummary({
    timeFrom: query.range?.[0] ? dayjs(query.range[0]).format('YYYY-MM-DD 00:00:00') : undefined,
    timeTo: query.range?.[1] ? dayjs(query.range[1]).format('YYYY-MM-DD 23:59:59') : undefined
  })
  Object.assign(summary, res || {})
}

function onReset() {
  query.range = undefined
  fetchData()
}

fetchData()
</script>

<style scoped>
.metrics-row {
  margin-bottom: 16px;
}

.metric-card {
  border-radius: 12px;
}

.metric-label {
  color: #64748b;
  font-size: 13px;
}

.metric-value {
  font-size: 24px;
  font-weight: 700;
  margin-top: 6px;
}
</style>
