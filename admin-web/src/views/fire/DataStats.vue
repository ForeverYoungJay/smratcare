<template>
  <PageContainer title="数据报表统计" subTitle="消防安全管理数据总览、日报/月报与打印版">
    <div class="no-print">
      <SearchForm :model="query" @search="fetchAll" @reset="onReset">
        <a-form-item label="统计日期">
          <a-range-picker v-model:value="query.dateRange" value-format="YYYY-MM-DD" />
        </a-form-item>
        <template #extra>
          <a-space>
            <a-button :loading="loading" @click="fetchAll">一键生成巡查记录</a-button>
            <a-button type="primary" :loading="exporting" @click="downloadReport">下载报告</a-button>
            <a-button @click="printReport">打印版</a-button>
          </a-space>
        </template>
      </SearchForm>

      <a-row :gutter="16" style="margin-bottom: 16px">
        <a-col :span="6"><a-card><a-statistic title="记录总数" :value="summary.totalCount" /></a-card></a-col>
        <a-col :span="6"><a-card><a-statistic title="处理中" :value="summary.openCount" /></a-card></a-col>
        <a-col :span="6"><a-card><a-statistic title="已关闭" :value="summary.closedCount" /></a-card></a-col>
        <a-col :span="6"><a-card><a-statistic title="逾期项" :value="summary.overdueCount" /></a-card></a-col>
      </a-row>

      <a-row :gutter="16" style="margin-bottom: 16px">
        <a-col :span="4"><a-card><a-statistic title="日巡完成" :value="summary.dailyCompletedCount" /></a-card></a-col>
        <a-col :span="4"><a-card><a-statistic title="月检完成" :value="summary.monthlyCompletedCount" /></a-card></a-col>
        <a-col :span="4"><a-card><a-statistic title="值班记录" :value="summary.dutyRecordCount" /></a-card></a-col>
        <a-col :span="4"><a-card><a-statistic title="交接班打卡" :value="summary.handoverPunchCount" /></a-card></a-col>
        <a-col :span="4"><a-card><a-statistic title="设备批号更新" :value="summary.equipmentUpdateCount" /></a-card></a-col>
        <a-col :span="4"><a-card><a-statistic title="设备老化处置" :value="summary.equipmentAgingDisposalCount" /></a-card></a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="8">
          <a-card title="分类统计" style="margin-bottom: 16px">
            <a-table :columns="typeColumns" :data-source="summary.typeStats" :pagination="false" row-key="recordType" size="small">
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'recordType'">{{ typeLabel(record.recordType) }}</template>
              </template>
            </a-table>
          </a-card>
        </a-col>
        <a-col :span="16">
          <a-card title="日报/月报分栏视图">
            <a-tabs v-model:activeKey="activeTab">
              <a-tab-pane key="daily" tab="日报">
                <a-table :columns="dailyColumns" :data-source="dailyRows" row-key="date" size="small" :pagination="false" />
              </a-tab-pane>
              <a-tab-pane key="monthly" tab="月报">
                <a-table :columns="monthlyColumns" :data-source="monthlyRows" row-key="month" size="small" :pagination="false" />
              </a-tab-pane>
              <a-tab-pane key="detail" tab="巡查明细">
                <a-table :columns="detailColumns" :data-source="detail.records" :pagination="false" row-key="id" size="small" :scroll="{ x: 1320 }">
                  <template #bodyCell="{ column, record }">
                    <template v-if="column.key === 'recordType'">{{ typeLabel(record.recordType) }}</template>
                    <template v-else-if="column.key === 'status'">
                      <a-tag :color="record.status === 'CLOSED' ? 'green' : 'orange'">{{ record.status === 'CLOSED' ? '已关闭' : '处理中' }}</a-tag>
                    </template>
                  </template>
                </a-table>
              </a-tab-pane>
            </a-tabs>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <div class="print-area" id="fire-print-area">
      <h2>消防安全管理报告</h2>
      <div class="print-meta">统计区间：{{ printRangeText }}</div>
      <div class="print-meta">生成时间：{{ generatedAt }}</div>
      <table class="print-table" cellspacing="0" cellpadding="0">
        <thead>
          <tr>
            <th>总数</th><th>处理中</th><th>已关闭</th><th>逾期</th><th>日巡完成</th><th>月检完成</th><th>值班记录</th><th>交接打卡</th><th>设备更新</th><th>老化处置</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>{{ summary.totalCount }}</td>
            <td>{{ summary.openCount }}</td>
            <td>{{ summary.closedCount }}</td>
            <td>{{ summary.overdueCount }}</td>
            <td>{{ summary.dailyCompletedCount }}</td>
            <td>{{ summary.monthlyCompletedCount }}</td>
            <td>{{ summary.dutyRecordCount }}</td>
            <td>{{ summary.handoverPunchCount }}</td>
            <td>{{ summary.equipmentUpdateCount }}</td>
            <td>{{ summary.equipmentAgingDisposalCount }}</td>
          </tr>
        </tbody>
      </table>

      <h3>日报</h3>
      <table class="print-table" cellspacing="0" cellpadding="0">
        <thead>
          <tr>
            <th>日期</th><th>记录数</th><th>已关闭</th><th>日巡完成</th><th>值班记录</th><th>交接打卡</th><th>设备更新</th><th>老化处置</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in dailyRows" :key="row.date">
            <td>{{ row.date }}</td>
            <td>{{ row.totalCount }}</td>
            <td>{{ row.closedCount }}</td>
            <td>{{ row.dailyCompletedCount }}</td>
            <td>{{ row.dutyRecordCount }}</td>
            <td>{{ row.handoverPunchCount }}</td>
            <td>{{ row.equipmentUpdateCount }}</td>
            <td>{{ row.equipmentAgingDisposalCount }}</td>
          </tr>
        </tbody>
      </table>

      <h3>月报</h3>
      <table class="print-table" cellspacing="0" cellpadding="0">
        <thead>
          <tr>
            <th>月份</th><th>记录数</th><th>已关闭</th><th>月检完成</th><th>值班记录</th><th>交接打卡</th><th>设备更新</th><th>老化处置</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in monthlyRows" :key="row.month">
            <td>{{ row.month }}</td>
            <td>{{ row.totalCount }}</td>
            <td>{{ row.closedCount }}</td>
            <td>{{ row.monthlyCompletedCount }}</td>
            <td>{{ row.dutyRecordCount }}</td>
            <td>{{ row.handoverPunchCount }}</td>
            <td>{{ row.equipmentUpdateCount }}</td>
            <td>{{ row.equipmentAgingDisposalCount }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import { exportFireSafetyReport, getFireSafetyReportDetail, getFireSafetySummary } from '../../api/fire'
import type { FireSafetyRecordType, FireSafetyReportDetail, FireSafetyReportRecordItem, FireSafetyReportSummary } from '../../types'

const query = reactive({
  dateRange: undefined as [string, string] | undefined
})

const activeTab = ref('daily')
const loading = ref(false)
const exporting = ref(false)

const summary = reactive<FireSafetyReportSummary>({
  totalCount: 0,
  openCount: 0,
  closedCount: 0,
  overdueCount: 0,
  dailyCompletedCount: 0,
  monthlyCompletedCount: 0,
  dutyRecordCount: 0,
  handoverPunchCount: 0,
  equipmentUpdateCount: 0,
  equipmentAgingDisposalCount: 0,
  typeStats: []
})

const detail = reactive<FireSafetyReportDetail>({
  totalCount: 0,
  dailyCompletedCount: 0,
  monthlyCompletedCount: 0,
  dutyRecordCount: 0,
  handoverPunchCount: 0,
  equipmentUpdateCount: 0,
  equipmentAgingDisposalCount: 0,
  records: []
})

const typeColumns = [
  { title: '分类', dataIndex: 'recordType', key: 'recordType' },
  { title: '数量', dataIndex: 'count', key: 'count', width: 120 }
]

const detailColumns = [
  { title: '类型', dataIndex: 'recordType', key: 'recordType', width: 120 },
  { title: '标题', dataIndex: 'title', key: 'title', width: 180 },
  { title: '位置', dataIndex: 'location', key: 'location', width: 120 },
  { title: '负责人', dataIndex: 'inspectorName', key: 'inspectorName', width: 100 },
  { title: '检查时间', dataIndex: 'checkTime', key: 'checkTime', width: 160 },
  { title: '扫码完成', dataIndex: 'scanCompletedAt', key: 'scanCompletedAt', width: 160 },
  { title: '值班记录', dataIndex: 'dutyRecord', key: 'dutyRecord', width: 180 },
  { title: '交接班打卡', dataIndex: 'handoverPunchTime', key: 'handoverPunchTime', width: 160 },
  { title: '设备批号', dataIndex: 'equipmentBatchNo', key: 'equipmentBatchNo', width: 130 },
  { title: '设备老化处置', dataIndex: 'equipmentAgingDisposal', key: 'equipmentAgingDisposal', width: 180 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90 }
]

const dailyColumns = [
  { title: '日期', dataIndex: 'date', key: 'date', width: 120 },
  { title: '记录数', dataIndex: 'totalCount', key: 'totalCount', width: 90 },
  { title: '已关闭', dataIndex: 'closedCount', key: 'closedCount', width: 90 },
  { title: '日巡完成', dataIndex: 'dailyCompletedCount', key: 'dailyCompletedCount', width: 100 },
  { title: '值班记录', dataIndex: 'dutyRecordCount', key: 'dutyRecordCount', width: 100 },
  { title: '交接班打卡', dataIndex: 'handoverPunchCount', key: 'handoverPunchCount', width: 110 },
  { title: '设备更新', dataIndex: 'equipmentUpdateCount', key: 'equipmentUpdateCount', width: 100 },
  { title: '老化处置', dataIndex: 'equipmentAgingDisposalCount', key: 'equipmentAgingDisposalCount', width: 100 }
]

const monthlyColumns = [
  { title: '月份', dataIndex: 'month', key: 'month', width: 120 },
  { title: '记录数', dataIndex: 'totalCount', key: 'totalCount', width: 90 },
  { title: '已关闭', dataIndex: 'closedCount', key: 'closedCount', width: 90 },
  { title: '月检完成', dataIndex: 'monthlyCompletedCount', key: 'monthlyCompletedCount', width: 100 },
  { title: '值班记录', dataIndex: 'dutyRecordCount', key: 'dutyRecordCount', width: 100 },
  { title: '交接班打卡', dataIndex: 'handoverPunchCount', key: 'handoverPunchCount', width: 110 },
  { title: '设备更新', dataIndex: 'equipmentUpdateCount', key: 'equipmentUpdateCount', width: 100 },
  { title: '老化处置', dataIndex: 'equipmentAgingDisposalCount', key: 'equipmentAgingDisposalCount', width: 100 }
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

function hasText(value?: string) {
  return !!value && !!String(value).trim()
}

function buildGroupedRows(records: FireSafetyReportRecordItem[], mode: 'day' | 'month') {
  const map = new Map<string, any>()
  records.forEach((record) => {
    const timeValue = record.checkTime || record.scanCompletedAt || ''
    const key = mode === 'day' ? dayjs(timeValue).format('YYYY-MM-DD') : dayjs(timeValue).format('YYYY-MM')
    if (!key || key === 'Invalid Date') return
    const existing = map.get(key) || {
      totalCount: 0,
      closedCount: 0,
      dailyCompletedCount: 0,
      monthlyCompletedCount: 0,
      dutyRecordCount: 0,
      handoverPunchCount: 0,
      equipmentUpdateCount: 0,
      equipmentAgingDisposalCount: 0
    }
    existing.totalCount += 1
    if (record.status === 'CLOSED') existing.closedCount += 1
    if (record.recordType === 'DAY_PATROL' && hasText(record.scanCompletedAt)) existing.dailyCompletedCount += 1
    if (record.recordType === 'MONTHLY_CHECK' && hasText(record.scanCompletedAt)) existing.monthlyCompletedCount += 1
    if (hasText(record.dutyRecord)) existing.dutyRecordCount += 1
    if (hasText(record.handoverPunchTime)) existing.handoverPunchCount += 1
    if (hasText(record.equipmentBatchNo)) existing.equipmentUpdateCount += 1
    if (hasText(record.equipmentAgingDisposal)) existing.equipmentAgingDisposalCount += 1
    map.set(key, existing)
  })

  return Array.from(map.entries())
    .sort((a, b) => (a[0] < b[0] ? 1 : -1))
    .map(([key, value]) => {
      if (mode === 'day') {
        return { date: key, ...value }
      }
      return { month: key, ...value }
    })
}

const dailyRows = computed(() => buildGroupedRows(detail.records, 'day'))
const monthlyRows = computed(() => buildGroupedRows(detail.records, 'month'))

const printRangeText = computed(() => {
  const [from, to] = query.dateRange || []
  if (from && to) return `${from} 至 ${to}`
  return '全部数据'
})

const generatedAt = computed(() => dayjs().format('YYYY-MM-DD HH:mm:ss'))

async function fetchAll() {
  const [dateFrom, dateTo] = query.dateRange || []
  loading.value = true
  try {
    const [summaryData, detailData] = await Promise.all([
      getFireSafetySummary({ dateFrom, dateTo }),
      getFireSafetyReportDetail({ dateFrom, dateTo, limit: 500 })
    ])
    Object.assign(summary, summaryData)
    Object.assign(detail, detailData)
  } finally {
    loading.value = false
  }
}

async function downloadReport() {
  const [dateFrom, dateTo] = query.dateRange || []
  exporting.value = true
  try {
    await exportFireSafetyReport({ dateFrom, dateTo })
    message.success('报表下载成功')
  } finally {
    exporting.value = false
  }
}

function printReport() {
  window.print()
}

function onReset() {
  query.dateRange = undefined
  fetchAll()
}

fetchAll()
</script>

<style scoped>
.print-area {
  display: none;
  margin-top: 16px;
  color: #000;
}

.print-meta {
  margin-bottom: 6px;
  font-size: 12px;
}

.print-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 16px;
}

.print-table th,
.print-table td {
  border: 1px solid #222;
  padding: 6px;
  font-size: 12px;
}

@media print {
  .no-print {
    display: none !important;
  }

  .print-area {
    display: block !important;
  }
}
</style>
