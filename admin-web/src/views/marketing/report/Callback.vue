<template>
  <PageContainer title="回访统计" sub-title="今日待回访、逾期回访与完成情况">
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline">
        <a-form-item label="开始日期">
          <a-date-picker v-model:value="query.dateFrom" value-format="YYYY-MM-DD" />
        </a-form-item>
        <a-form-item label="结束日期">
          <a-date-picker v-model:value="query.dateTo" value-format="YYYY-MM-DD" />
        </a-form-item>
        <a-form-item label="渠道">
          <a-input v-model:value="query.source" placeholder="渠道" allow-clear />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="loadData">查询</a-button>
            <a-button @click="exportData">导出CSV</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>
    <a-row :gutter="16">
      <a-col :xs="24" :sm="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="今日待回访" :value="todayDue" />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="逾期回访" :value="overdue" />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="已完成（预订）" :value="completed" />
        </a-card>
      </a-col>
    </a-row>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px">
      <a-table :columns="columns" :data-source="rows" :pagination="false" row-key="id">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.nextFollowDate < today ? 'red' : 'orange'">
              {{ record.nextFollowDate < today ? '逾期' : '今日' }}
            </a-tag>
          </template>
        </template>
      </a-table>
      <a-pagination
        style="margin-top: 16px; text-align: right;"
        :current="query.pageNo"
        :page-size="query.pageSize"
        :total="total"
        show-size-changer
        @change="onPageChange"
        @showSizeChange="onPageSizeChange"
      />
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import PageContainer from '../../../components/PageContainer.vue'
import { getMarketingCallbackReport } from '../../../api/marketing'
import { exportCsv } from '../../../utils/export'
import type { MarketingCallbackItem, MarketingReportQuery } from '../../../types'

const today = dayjs().format('YYYY-MM-DD')
const rows = ref<MarketingCallbackItem[]>([])
const total = ref(0)
const todayDue = ref(0)
const overdue = ref(0)
const completed = ref(0)

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  dateFrom: undefined as MarketingReportQuery['dateFrom'],
  dateTo: undefined as MarketingReportQuery['dateTo'],
  source: undefined as MarketingReportQuery['source'],
  staffId: undefined as MarketingReportQuery['staffId']
})

const columns = [
  { title: '姓名', dataIndex: 'name', key: 'name', width: 140 },
  { title: '电话', dataIndex: 'phone', key: 'phone', width: 140 },
  { title: '来源', dataIndex: 'source', key: 'source', width: 140 },
  { title: '回访日期', dataIndex: 'nextFollowDate', key: 'nextFollowDate', width: 140 },
  { title: '状态', key: 'status', width: 100 },
  { title: '备注', dataIndex: 'remark', key: 'remark' }
]

async function loadData() {
  const report = await getMarketingCallbackReport(query)
  rows.value = report.records || []
  total.value = report.total || 0
  todayDue.value = report.todayDue || 0
  overdue.value = report.overdue || 0
  completed.value = report.completed || 0
}

function exportData() {
  exportCsv(rows.value.map((item) => ({
    姓名: item.name,
    电话: item.phone || '',
    来源: item.source || '',
    回访日期: item.nextFollowDate || '',
    备注: item.remark || ''
  })), `marketing-callback-${Date.now()}.csv`)
}

function onPageChange(page: number) {
  query.pageNo = page
  loadData()
}

function onPageSizeChange(_current: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
  loadData()
}

onMounted(loadData)
</script>
