<template>
  <PageContainer title="数据报表统计" subTitle="消防安全管理数据总览、日报/月报与打印版">
    <div class="no-print">
      <SearchForm :model="query" @search="fetchAll" @reset="onReset">
        <a-form-item label="统计日期">
          <a-range-picker v-model:value="query.dateRange" value-format="YYYY-MM-DD" />
        </a-form-item>
        <a-form-item label="明细筛选">
          <a-select v-model:value="selectedTypeFilter" style="width: 220px" @change="handleTypeFilterChange">
            <a-select-option value="ALL">全部分类</a-select-option>
            <a-select-option v-for="item in availableTypeOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <template #extra>
          <a-space>
            <a-button :loading="loading" @click="fetchAll">刷新统计</a-button>
            <a-button type="primary" :loading="exporting" @click="openReportConfig('download')">下载报告</a-button>
            <a-button @click="openReportConfig('print')">打印版</a-button>
          </a-space>
        </template>
      </SearchForm>

      <StatefulBlock :loading="loading" :error="errorMessage" :empty="!detail.records.length" empty-text="暂无消防统计数据" @retry="fetchAll">
        <a-row :gutter="16" style="margin-bottom: 16px">
          <a-col :span="6"><a-card><a-statistic title="记录总数" :value="summary.totalCount" /></a-card></a-col>
          <a-col :span="6"><a-card><a-statistic title="未关闭项" :value="summary.openCount" /></a-card></a-col>
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
                  <template v-if="column.key === 'recordType'">
                    <a-button type="link" size="small" @click="openTypeDetail(record.recordType)">{{ typeLabel(record.recordType) }}</a-button>
                  </template>
                  <template v-else-if="column.key === 'count'">
                    <a-button type="link" size="small" @click="openTypeDetail(record.recordType)">{{ record.count }}</a-button>
                  </template>
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
                <a-tab-pane key="detail" :tab="detailTabTitle">
                  <a-table :columns="detailColumns" :data-source="filteredDetailRecords" :pagination="false" row-key="id" size="small" :scroll="{ x: 1320 }">
                    <template #bodyCell="{ column, record }">
                      <template v-if="column.key === 'recordType'">{{ typeLabel(record.recordType) }}</template>
                      <template v-else-if="column.key === 'status'">
                        <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
                      </template>
                    </template>
                  </a-table>
                </a-tab-pane>
              </a-tabs>
            </a-card>
          </a-col>
        </a-row>
      </StatefulBlock>
    </div>

    <div class="print-area" id="fire-print-area">
      <h2>消防安全管理报告</h2>
      <div class="print-meta">统计区间：{{ printRangeText }}</div>
      <div class="print-meta">生成时间：{{ generatedAt }}</div>
      <table class="print-table" cellspacing="0" cellpadding="0">
        <thead>
          <tr>
            <th>总数</th><th>未关闭项</th><th>已关闭</th><th>逾期</th><th>日巡完成</th><th>月检完成</th><th>值班记录</th><th>交接打卡</th><th>设备更新</th><th>老化处置</th>
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

    <a-modal
      v-model:open="reportConfigOpen"
      :title="reportAction === 'print' ? '打印内容设置' : '下载内容设置'"
      @ok="submitReportAction"
      :confirm-loading="exporting"
      width="760px"
    >
      <a-form layout="vertical">
        <a-form-item label="报告区块">
          <a-checkbox-group v-model:value="reportSections">
            <a-row :gutter="[12, 12]">
              <a-col :span="8"><a-checkbox value="summary">汇总统计</a-checkbox></a-col>
              <a-col :span="8"><a-checkbox value="category">分类统计</a-checkbox></a-col>
              <a-col :span="8"><a-checkbox value="daily">日报</a-checkbox></a-col>
              <a-col :span="8"><a-checkbox value="monthly">月报</a-checkbox></a-col>
              <a-col :span="8"><a-checkbox value="detail">巡查明细</a-checkbox></a-col>
            </a-row>
          </a-checkbox-group>
        </a-form-item>
        <a-form-item label="明细打印字段">
          <a-checkbox-group v-model:value="detailPrintFields">
            <a-row :gutter="[12, 12]">
              <a-col v-for="item in detailFieldOptions" :key="item.key" :span="8">
                <a-checkbox :value="item.key">{{ item.label }}</a-checkbox>
              </a-col>
            </a-row>
          </a-checkbox-group>
        </a-form-item>
        <a-alert
          type="info"
          show-icon
          :message="`当前明细范围：${selectedTypeFilter === 'ALL' ? '全部分类' : typeLabel(selectedTypeFilter as FireSafetyRecordType)}；共 ${filteredDetailRecords.length} 条。`"
        />
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
import { getFireSafetyReportDetail, getFireSafetySummary } from '../../api/fire'
import type { FireSafetyRecordType, FireSafetyReportDetail, FireSafetyReportRecordItem, FireSafetyReportSummary } from '../../types'

const query = reactive({
  dateRange: undefined as [string, string] | undefined
})

const activeTab = ref('daily')
const loading = ref(false)
const exporting = ref(false)
const errorMessage = ref('')
const selectedTypeFilter = ref<'ALL' | FireSafetyRecordType>('ALL')
const reportConfigOpen = ref(false)
const reportAction = ref<'print' | 'download'>('print')
const reportSections = ref<Array<'summary' | 'category' | 'daily' | 'monthly' | 'detail'>>(['summary', 'category', 'daily', 'monthly', 'detail'])
const detailPrintFields = ref<string[]>([
  'recordType',
  'title',
  'location',
  'inspectorName',
  'checkTime',
  'dutyRecord',
  'handoverPunchTime',
  'status',
  'issueDescription',
  'actionTaken'
])

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
  expiringSoonCount: 0,
  nextCheckDueSoonCount: 0,
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

const detailFieldOptions = [
  { key: 'recordType', label: '类型' },
  { key: 'title', label: '标题' },
  { key: 'location', label: '位置' },
  { key: 'inspectorName', label: '负责人' },
  { key: 'checkTime', label: '检查时间' },
  { key: 'scanCompletedAt', label: '扫码完成' },
  { key: 'dutyRecord', label: '值班记录' },
  { key: 'handoverPunchTime', label: '交接班打卡' },
  { key: 'equipmentBatchNo', label: '设备批号' },
  { key: 'equipmentAgingDisposal', label: '老化处置' },
  { key: 'status', label: '状态' },
  { key: 'issueDescription', label: '问题描述' },
  { key: 'actionTaken', label: '处置措施' }
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

function statusText(status?: FireSafetyReportRecordItem['status']) {
  if (status === 'CLOSED') return '已关闭'
  if (status === 'RUNNING') return '运行中'
  return '处理中'
}

function statusColor(status?: FireSafetyReportRecordItem['status']) {
  if (status === 'CLOSED') return 'green'
  if (status === 'RUNNING') return 'blue'
  return 'orange'
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

const availableTypeOptions = computed(() =>
  (summary.typeStats || [])
    .filter((item) => Number(item.count || 0) > 0)
    .map((item) => ({ value: item.recordType, label: typeLabel(item.recordType) }))
)

const filteredDetailRecords = computed(() => {
  if (selectedTypeFilter.value === 'ALL') {
    return detail.records
  }
  return detail.records.filter((item) => item.recordType === selectedTypeFilter.value)
})

const dailyRows = computed(() => buildGroupedRows(detail.records, 'day'))
const monthlyRows = computed(() => buildGroupedRows(detail.records, 'month'))
const detailTabTitle = computed(() =>
  selectedTypeFilter.value === 'ALL'
    ? `巡查明细（${filteredDetailRecords.value.length}）`
    : `${typeLabel(selectedTypeFilter.value)}明细（${filteredDetailRecords.value.length}）`
)

const printRangeText = computed(() => {
  const [from, to] = query.dateRange || []
  if (from && to) return `${from} 至 ${to}`
  return '全部数据'
})

const generatedAt = computed(() => dayjs().format('YYYY-MM-DD HH:mm:ss'))

function handleTypeFilterChange() {
  if (activeTab.value !== 'detail') {
    activeTab.value = 'detail'
  }
}

function openTypeDetail(recordType: FireSafetyRecordType) {
  selectedTypeFilter.value = recordType
  activeTab.value = 'detail'
}

function openReportConfig(action: 'print' | 'download') {
  reportAction.value = action
  reportConfigOpen.value = true
}

function safeHtml(value: unknown) {
  return String(value ?? '')
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;')
}

function detailFieldLabel(key: string) {
  return detailFieldOptions.find((item) => item.key === key)?.label || key
}

function detailFieldValue(record: FireSafetyReportRecordItem, key: string) {
  if (key === 'recordType') return typeLabel(record.recordType)
  if (key === 'status') return statusText(record.status)
  return (record as Record<string, any>)[key] || '-'
}

function buildSectionTable(title: string, headers: string[], rows: Array<Array<string | number>>) {
  return `
    <section class="report-section">
      <h3>${safeHtml(title)}</h3>
      <table class="report-table">
        <thead><tr>${headers.map((item) => `<th>${safeHtml(item)}</th>`).join('')}</tr></thead>
        <tbody>${rows.map((row) => `<tr>${row.map((cell) => `<td>${safeHtml(cell)}</td>`).join('')}</tr>`).join('')}</tbody>
      </table>
    </section>
  `
}

function buildReportDocumentHtml() {
  const sections: string[] = []
  if (reportSections.value.includes('summary')) {
    sections.push(buildSectionTable('汇总统计', ['总数', '未关闭项', '已关闭', '逾期', '日巡完成', '月检完成', '值班记录', '交接打卡', '设备更新', '老化处置'], [[
      summary.totalCount,
      summary.openCount,
      summary.closedCount,
      summary.overdueCount,
      summary.dailyCompletedCount,
      summary.monthlyCompletedCount,
      summary.dutyRecordCount,
      summary.handoverPunchCount,
      summary.equipmentUpdateCount,
      summary.equipmentAgingDisposalCount
    ]]))
  }
  if (reportSections.value.includes('category')) {
    sections.push(buildSectionTable('分类统计', ['分类', '数量'], (summary.typeStats || []).map((item) => [typeLabel(item.recordType), item.count || 0])))
  }
  if (reportSections.value.includes('daily')) {
    sections.push(buildSectionTable('日报', ['日期', '记录数', '已关闭', '日巡完成', '值班记录', '交接班打卡', '设备更新', '老化处置'], dailyRows.value.map((row) => [
      row.date,
      row.totalCount,
      row.closedCount,
      row.dailyCompletedCount,
      row.dutyRecordCount,
      row.handoverPunchCount,
      row.equipmentUpdateCount,
      row.equipmentAgingDisposalCount
    ])))
  }
  if (reportSections.value.includes('monthly')) {
    sections.push(buildSectionTable('月报', ['月份', '记录数', '已关闭', '月检完成', '值班记录', '交接班打卡', '设备更新', '老化处置'], monthlyRows.value.map((row) => [
      row.month,
      row.totalCount,
      row.closedCount,
      row.monthlyCompletedCount,
      row.dutyRecordCount,
      row.handoverPunchCount,
      row.equipmentUpdateCount,
      row.equipmentAgingDisposalCount
    ])))
  }
  if (reportSections.value.includes('detail')) {
    const headers = detailPrintFields.value.map((item) => detailFieldLabel(item))
    const rows = filteredDetailRecords.value.map((record) => detailPrintFields.value.map((field) => detailFieldValue(record, field)))
    sections.push(buildSectionTable('巡查明细', headers, rows))
  }

  return `
    <!DOCTYPE html>
    <html>
      <head>
        <meta charset="UTF-8" />
        <title>消防安全管理报告</title>
        <style>
          body { font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Microsoft YaHei', sans-serif; padding: 24px; color: #111827; }
          h1 { margin: 0 0 8px; font-size: 24px; }
          h3 { margin: 0 0 10px; font-size: 16px; }
          .meta { margin-bottom: 6px; color: #4b5563; font-size: 12px; }
          .report-section { margin-top: 18px; }
          .report-table { width: 100%; border-collapse: collapse; }
          .report-table th, .report-table td { border: 1px solid #d1d5db; padding: 6px 8px; font-size: 12px; text-align: left; vertical-align: top; }
          .report-table th { background: #f3f4f6; }
        </style>
      </head>
      <body>
        <h1>消防安全管理报告</h1>
        <div class="meta">统计区间：${safeHtml(printRangeText.value)}</div>
        <div class="meta">生成时间：${safeHtml(dayjs().format('YYYY-MM-DD HH:mm:ss'))}</div>
        <div class="meta">明细范围：${safeHtml(selectedTypeFilter.value === 'ALL' ? '全部分类' : typeLabel(selectedTypeFilter.value))}</div>
        ${sections.join('')}
      </body>
    </html>
  `
}

function downloadHtmlReport(html: string) {
  const blob = new Blob([html], { type: 'application/vnd.ms-excel;charset=utf-8;' })
  const link = document.createElement('a')
  const objectUrl = URL.createObjectURL(blob)
  link.href = objectUrl
  link.download = `消防安全管理报告-${dayjs().format('YYYYMMDD-HHmmss')}.xls`
  link.click()
  URL.revokeObjectURL(objectUrl)
}

function printHtmlReport(html: string) {
  const popup = window.open('', '_blank')
  if (!popup) {
    throw new Error('请允许弹窗后重试打印')
  }
  popup.document.write(html)
  popup.document.close()
  popup.focus()
  popup.print()
}

async function submitReportAction() {
  if (!reportSections.value.length) {
    message.warning('请至少选择一个报告区块')
    return
  }
  if (reportSections.value.includes('detail') && !detailPrintFields.value.length) {
    message.warning('请至少选择一个明细字段')
    return
  }
  const html = buildReportDocumentHtml()
  reportConfigOpen.value = false
  exporting.value = true
  try {
    if (reportAction.value === 'download') {
      downloadHtmlReport(html)
      message.success('报告下载成功')
    } else {
      printHtmlReport(html)
    }
  } catch (error: any) {
    message.error(error?.message || (reportAction.value === 'download' ? '报表下载失败' : '打印失败'))
  } finally {
    exporting.value = false
  }
}

async function fetchAll() {
  const [dateFrom, dateTo] = query.dateRange || []
  loading.value = true
  errorMessage.value = ''
  try {
    const [summaryData, detailData] = await Promise.all([
      getFireSafetySummary({ dateFrom, dateTo }),
      getFireSafetyReportDetail({ dateFrom, dateTo, limit: 500 })
    ])
    Object.assign(summary, summaryData)
    Object.assign(detail, detailData)
  } catch (error: any) {
    errorMessage.value = error?.message || '加载消防统计失败'
  } finally {
    loading.value = false
  }
}

function onReset() {
  query.dateRange = undefined
  selectedTypeFilter.value = 'ALL'
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
