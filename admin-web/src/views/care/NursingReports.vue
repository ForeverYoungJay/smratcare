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
          <div class="metric-label">执行完成率</div>
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

    <a-card class="card-elevated report-card" :bordered="false" title="月度服务报告摘要">
      <div class="report-summary">{{ summary.monthlyReportSummary || '暂无护理服务报告摘要' }}</div>
      <a-alert
        class="ai-summary"
        type="info"
        show-icon
        :message="summary.aiCareSummary || '暂无AI护理摘要'"
        :description="summary.familyReadableSummary || '暂无家属可读摘要'"
      />
      <a-row :gutter="16" class="quality-row">
        <a-col :span="6">
          <a-statistic title="计划达成率" :value="pct(summary.planAchievementRate)" />
        </a-col>
        <a-col :span="6">
          <a-statistic title="质检次数" :value="summary.qualityReviewCount || 0" />
        </a-col>
        <a-col :span="6">
          <a-statistic title="平均评分" :value="scoreText(summary.averageReviewScore)" />
        </a-col>
        <a-col :span="6">
          <a-statistic title="可疑执行" :value="summary.suspiciousExecutionCount || 0" />
        </a-col>
      </a-row>
      <a-row :gutter="16" class="quality-row">
        <a-col :span="6">
          <a-statistic title="异常任务" :value="summary.exceptionTaskCount || 0" />
        </a-col>
        <a-col :span="6">
          <a-statistic title="异常已闭环" :value="summary.exceptionResolvedCount || 0" />
        </a-col>
        <a-col :span="6">
          <a-statistic title="异常闭环率" :value="pct(summary.exceptionClosureRate)" />
        </a-col>
        <a-col :span="6">
          <a-statistic title="交接确认率" :value="pct(summary.handoverConfirmRate)" />
        </a-col>
      </a-row>
      <a-list :data-source="summary.reviewActionItems || []" size="small" class="action-list">
        <template #renderItem="{ item }">
          <a-list-item>{{ item }}</a-list-item>
        </template>
      </a-list>
    </a-card>

    <a-row :gutter="16" class="report-insights">
      <a-col :xs="24" :lg="8">
        <a-card title="照护亮点" :bordered="false" class="card-elevated">
          <a-list :data-source="summary.careHighlights || []" size="small">
            <template #renderItem="{ item }"><a-list-item>{{ item }}</a-list-item></template>
          </a-list>
        </a-card>
      </a-col>
      <a-col :xs="24" :lg="8">
        <a-card title="风险提醒" :bordered="false" class="card-elevated">
          <a-list :data-source="summary.riskSignals || []" size="small">
            <template #renderItem="{ item }"><a-list-item>{{ item }}</a-list-item></template>
          </a-list>
        </a-card>
      </a-col>
      <a-col :xs="24" :lg="8">
        <a-card title="下月建议" :bordered="false" class="card-elevated">
          <a-list :data-source="summary.nextMonthSuggestions || []" size="small">
            <template #renderItem="{ item }"><a-list-item>{{ item }}</a-list-item></template>
          </a-list>
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
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import { getNursingReportSummary } from '../../api/nursing'
import type { NursingReportSummary } from '../../types'
import { resolveCareError } from './careError'

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
  qualityReviewCount: 0,
  averageReviewScore: 0,
  exceptionTaskCount: 0,
  exceptionResolvedCount: 0,
  exceptionClosureRate: 0,
  suspiciousExecutionCount: 0,
  handoverCount: 0,
  handoverConfirmedCount: 0,
  handoverConfirmRate: 0,
  monthlyReportSummary: '',
  aiCareSummary: '',
  familyReadableSummary: '',
  reviewActionItems: [],
  careHighlights: [],
  riskSignals: [],
  nextMonthSuggestions: [],
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

function scoreText(value?: number | null) {
  if (value == null) return '0.0'
  return Number(value).toFixed(1)
}

async function fetchData() {
  try {
    const res = await getNursingReportSummary({
      timeFrom: query.range?.[0] ? dayjs(query.range[0]).format('YYYY-MM-DDT00:00:00') : undefined,
      timeTo: query.range?.[1] ? dayjs(query.range[1]).format('YYYY-MM-DDT23:59:59') : undefined
    })
    Object.assign(summary, res || {})
  } catch (error) {
    message.error(resolveCareError(error, '加载护理报表失败'))
    Object.assign(summary, {
      totalServices: 0,
      completedServices: 0,
      completionRate: 0,
      overdueCount: 0,
      overdueRate: 0,
      planBookingCount: 0,
      planCompletedCount: 0,
      planAchievementRate: 0,
      qualityReviewCount: 0,
      averageReviewScore: 0,
      exceptionTaskCount: 0,
      exceptionResolvedCount: 0,
      exceptionClosureRate: 0,
      suspiciousExecutionCount: 0,
      handoverCount: 0,
      handoverConfirmedCount: 0,
      handoverConfirmRate: 0,
      monthlyReportSummary: '',
      aiCareSummary: '',
      familyReadableSummary: '',
      reviewActionItems: [],
      careHighlights: [],
      riskSignals: [],
      nextMonthSuggestions: [],
      staffWorkloads: []
    })
  }
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
  color: var(--muted);
  font-size: 13px;
}

.metric-value {
  font-size: 24px;
  font-weight: 700;
  margin-top: 6px;
}

.report-card {
  margin-bottom: 16px;
}

.report-summary {
  color: var(--ink);
  font-size: 15px;
  margin-bottom: 16px;
}

.ai-summary {
  margin-bottom: 16px;
}

.quality-row {
  margin-bottom: 12px;
}

.report-insights {
  margin-bottom: 16px;
}

.action-list {
  margin-top: 4px;
}
</style>
