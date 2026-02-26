<template>
  <PageContainer title="数据报表统计" subTitle="消防安全管理数据总览与分类统计">
    <SearchForm :model="query" @search="fetchSummary" @reset="onReset">
      <a-form-item label="统计日期">
        <a-range-picker v-model:value="query.dateRange" value-format="YYYY-MM-DD" />
      </a-form-item>
    </SearchForm>

    <a-row :gutter="16" style="margin-bottom: 16px">
      <a-col :span="6">
        <a-card>
          <a-statistic title="记录总数" :value="summary.totalCount" />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic title="处理中" :value="summary.openCount" />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic title="已关闭" :value="summary.closedCount" />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic title="逾期项" :value="summary.overdueCount" />
        </a-card>
      </a-col>
    </a-row>

    <a-card title="分类统计">
      <a-table :columns="columns" :data-source="summary.typeStats" :pagination="false" row-key="recordType">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'recordType'">
            {{ typeLabel(record.recordType) }}
          </template>
        </template>
      </a-table>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import { getFireSafetySummary } from '../../api/fire'
import type { FireSafetyRecordType, FireSafetyReportSummary } from '../../types'

const query = reactive({
  dateRange: undefined as [string, string] | undefined
})

const summary = reactive<FireSafetyReportSummary>({
  totalCount: 0,
  openCount: 0,
  closedCount: 0,
  overdueCount: 0,
  typeStats: []
})

const columns = [
  { title: '分类', dataIndex: 'recordType', key: 'recordType' },
  { title: '数量', dataIndex: 'count', key: 'count', width: 120 }
]

const typeLabelMap: Record<FireSafetyRecordType, string> = {
  FACILITY: '消防设施管理',
  CONTROL_ROOM_DUTY: '控制室值班记录',
  MONTHLY_CHECK: '每月防火检查',
  DAY_PATROL: '日间防火巡查',
  NIGHT_PATROL: '夜间防火巡查',
  MAINTENANCE_REPORT: '消防设施维护保养报告',
  FAULT_MAINTENANCE: '建筑消防设施故障维护'
}

function typeLabel(type: FireSafetyRecordType) {
  return typeLabelMap[type] || type
}

async function fetchSummary() {
  const [dateFrom, dateTo] = query.dateRange || []
  const res = await getFireSafetySummary({ dateFrom, dateTo })
  summary.totalCount = res.totalCount || 0
  summary.openCount = res.openCount || 0
  summary.closedCount = res.closedCount || 0
  summary.overdueCount = res.overdueCount || 0
  summary.typeStats = res.typeStats || []
}

function onReset() {
  query.dateRange = undefined
  fetchSummary()
}

fetchSummary()
</script>
