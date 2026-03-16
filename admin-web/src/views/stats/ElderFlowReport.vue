<template>
  <PageContainer title="老人出入报表" subTitle="老人入院/离院明细查询">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline">
        <a-form-item label="开始日期">
          <a-date-picker v-model:value="query.fromDate" style="width: 150px" />
        </a-form-item>
        <a-form-item label="结束日期">
          <a-date-picker v-model:value="query.toDate" style="width: 150px" />
        </a-form-item>
        <a-form-item label="事件类型">
          <a-select v-model:value="query.eventType" allow-clear style="width: 140px">
            <a-select-option value="ADMISSION">入院</a-select-option>
            <a-select-option value="DISCHARGE">离院</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="机构ID">
          <a-input v-model:value="query.orgId" allow-clear placeholder="默认当前机构" style="width: 160px" />
        </a-form-item>
        <a-form-item label="老人姓名">
          <a-input v-model:value="query.keyword" allow-clear placeholder="按姓名关键字检索" style="width: 180px" />
        </a-form-item>
        <a-form-item label="院内老人">
          <a-select
            v-model:value="printElderId"
            show-search
            allow-clear
            :filter-option="false"
            :options="elderOptions"
            :loading="elderLoading"
            placeholder="请选择院内老人"
            style="width: 180px"
            @search="searchElders"
            @focus="() => !elderOptions.length && searchElders('')"
            @change="onPrintElderChange"
          />
        </a-form-item>
        <a-form-item label="快捷区间">
          <a-space>
            <a-button size="small" @click="setDayPreset(7)">近7天</a-button>
            <a-button size="small" @click="setDayPreset(30)">近30天</a-button>
            <a-button size="small" @click="setDayPreset(90)">近90天</a-button>
          </a-space>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="search">查询</a-button>
            <a-button @click="exportCsvReport">导出报表</a-button>
            <a-button @click="copyFilterLink">复制筛选链接</a-button>
            <a-button @click="openColumnSetting">列设置</a-button>
            <a-button :disabled="!printRows.length" @click="printCurrent">打印当前列</a-button>
            <a-button :disabled="!canPrintSpecific" @click="printSpecificElder">打印指定老人</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
      <StatsMetaHint
        scope-text="老人出入报表（支持按院内老人过滤）"
        :refreshed-at="refreshedAt"
        :note-text="metricTraceText"
      />
    </a-card>

    <StatsWorkspacePanel
      page-key="stats-elder-flow-report"
      title="出入报表策略区"
      :summary-text="workspaceSummary"
      :current-payload="workspacePayload"
      :target-fields="targetFields"
      :data-health="dataHealth"
      :empty-state="emptyState"
      @apply-preset="applyPreset"
      @targets-change="onTargetsChange"
    />

    <StatsInsightDeck :items="visibleInsightItems" />

    <StatsCommandCenter
      page-key="stats-elder-flow-report-command"
      title="出入报表协同台"
      share-title="老人出入报表协同包"
      :summary-text="commandSummary"
      :current-payload="workspacePayload"
      :action-items="commandActionItems"
      :anomalies="commandAnomalies"
      :metric-notes="metricNotes"
      :panel-options="panelOptions"
      :selected-panel-keys="panelKeys"
      :template-column-options="printColumnOptions"
      :selected-template-columns="selectedPrintColumns"
      @trigger-action="onCommandAction"
      @panel-change="onPanelChange"
      @apply-template="applyReportTemplate"
    />

    <a-row :gutter="16" style="margin-top: 16px;">
      <a-col :span="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="匹配总记录" :value="summary.total" />
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="入院记录" :value="summary.admissionCount" />
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="离院记录" :value="summary.dischargeCount" />
        </a-card>
      </a-col>
    </a-row>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <div class="batch-action-bar">
        <a-space wrap>
          <a-tag color="blue">已选 {{ selectedRows.length }} 条</a-tag>
          <a-button size="small" :disabled="!rows.length" @click="selectAllCurrentPage">全选本页</a-button>
          <a-button size="small" :disabled="!rows.length" @click="selectByEventType('ADMISSION')">勾选全部入院</a-button>
          <a-button size="small" :disabled="!rows.length" @click="selectByEventType('DISCHARGE')">勾选全部离院</a-button>
          <a-button size="small" :disabled="!selectedRows.length" @click="clearSelection">清空勾选</a-button>
          <a-button size="small" type="primary" :disabled="!selectedRows.length" @click="printSelectedRows">批量打印勾选</a-button>
          <a-button size="small" :disabled="!selectedRows.length" @click="exportSelectedRowsCsv">批量导出勾选</a-button>
        </a-space>
      </div>

      <vxe-table
        ref="tableRef"
        border
        stripe
        show-overflow
        :row-id="'rowKey'"
        :data="rows"
        height="420"
        @checkbox-change="refreshSelectedRows"
        @checkbox-all="refreshSelectedRows"
      >
        <vxe-column type="checkbox" width="56" />
        <vxe-column type="seq" title="#" width="60" />
        <vxe-column field="eventDate" title="日期" width="140" />
        <vxe-column field="eventType" title="事件类型" width="120">
          <template #default="{ row }">
            <a-tag :color="row.eventType === 'ADMISSION' ? 'blue' : 'orange'">
              {{ row.eventType === 'ADMISSION' ? '入院' : '离院' }}
            </a-tag>
          </template>
        </vxe-column>
        <vxe-column field="elderName" title="老人姓名" min-width="160">
          <template #default="{ row }">
            {{ row.elderName || '未知老人' }}
          </template>
        </vxe-column>
        <vxe-column field="remark" title="备注" min-width="260" />
      </vxe-table>
      <div style="margin-top: 12px; text-align: right;">
        <a-pagination
          v-model:current="query.pageNo"
          v-model:page-size="query.pageSize"
          :total="total"
          show-size-changer
          :page-size-options="['10', '20', '50', '100']"
          @change="loadData"
          @show-size-change="onPageSizeChange"
        />
      </div>
    </a-card>

    <a-modal v-model:open="columnSettingOpen" title="打印列设置" @ok="columnSettingOpen = false" cancel-text="关闭" ok-text="确定">
      <a-checkbox-group v-model:value="selectedPrintColumns" :options="printColumnOptions" />
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue'
import dayjs, { type Dayjs } from 'dayjs'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import StatsMetaHint from '../../components/stats/StatsMetaHint.vue'
import StatsWorkspacePanel from '../../components/stats/StatsWorkspacePanel.vue'
import StatsInsightDeck from '../../components/stats/StatsInsightDeck.vue'
import StatsCommandCenter from '../../components/stats/StatsCommandCenter.vue'
import { exportElderFlowReportCsv, getElderFlowReport } from '../../api/stats'
import type { FlowReportItem, Id } from '../../types'
import { message, Modal } from 'ant-design-vue'
import { printTableReport } from '../../utils/print'
import { useElderOptions } from '../../composables/useElderOptions'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'
import { copyText } from '../../utils/clipboard'
import { normalizeId } from '../../utils/id'
import { buildComparisonSummary, buildPeriodSeries } from '../../utils/statsInsight'

const route = useRoute()
const router = useRouter()
const refreshedAt = ref('')
const query = ref({
  fromDate: dayjs().subtract(29, 'day') as Dayjs,
  toDate: dayjs() as Dayjs,
  eventType: undefined as 'ADMISSION' | 'DISCHARGE' | undefined,
  keyword: '',
  orgId: undefined as Id | undefined,
  pageNo: 1,
  pageSize: 20
})

type FlowReportTableRow = FlowReportItem & {
  rowKey: string
}

const tableRef = ref<any>(null)
const rows = ref<FlowReportTableRow[]>([])
const selectedRows = ref<FlowReportTableRow[]>([])
const total = ref(0)
const summary = ref({
  total: 0,
  admissionCount: 0,
  dischargeCount: 0
})
const printElderId = ref<Id | undefined>(undefined)
const columnSettingOpen = ref(false)
const printColumnOptions = [
  { label: '日期', value: 'eventDate' },
  { label: '事件类型', value: 'eventTypeLabel' },
  { label: '老人姓名', value: 'elderName' },
  { label: '备注', value: 'remark' }
]
const selectedPrintColumns = ref<string[]>(['eventDate', 'eventTypeLabel', 'elderName', 'remark'])
const panelKeys = ref<string[]>([])
const targets = ref<Record<string, number>>({
  totalRecords: 0,
  dischargeShare: 50
})
const { elderOptions, elderLoading, searchElders } = useElderOptions({ pageSize: 100, inHospitalOnly: true, signedOnly: false })
const LARGE_PRINT_ROW_THRESHOLD = 200
const metricTraceText = computed(() => {
  const version = String(route.query.metricVersion || '').trim()
  const source = String(route.query.fromSource || '').trim()
  const notes: string[] = []
  if (version) notes.push(`口径版本：${version}`)
  if (source === 'dashboard') notes.push('来源：看板钻取')
  return notes.join('；')
})
const printRows = computed(() => rows.value.map(item => ({
  rowKey: item.rowKey,
  elderId: item.elderId,
  eventDate: item.eventDate || '',
  eventTypeLabel: item.eventType === 'ADMISSION' ? '入院' : '离院',
  elderName: item.elderName || '未知老人',
  remark: item.remark || ''
})))
const selectedPrintElder = computed(() => elderOptions.value.find((item) => String(item.value || '').trim() === String(printElderId.value || '').trim()))
const canPrintSpecific = computed(() => !!printRows.value.length && !!printElderId.value)
const workspaceSummary = computed(() => `对老人出入院明细做分页检索、批量操作和固定筛选保存。当前命中 ${summary.value.total} 条记录。`)
const workspacePayload = computed(() => ({
  fromDate: dayjs(query.value.fromDate).format('YYYY-MM-DD'),
  toDate: dayjs(query.value.toDate).format('YYYY-MM-DD'),
  eventType: query.value.eventType || '',
  keyword: query.value.keyword || '',
  orgId: query.value.orgId ? String(query.value.orgId) : '',
  elderId: printElderId.value ? String(printElderId.value) : ''
}))
const targetFields = computed(() => ([
  { key: 'totalRecords', label: '匹配记录目标', value: targets.value.totalRecords, min: 0, max: 999999, step: 10 },
  { key: 'dischargeShare', label: '离院占比关注线(%)', value: targets.value.dischargeShare, min: 0, max: 100, step: 1 }
]))
const dataHealth = computed(() => {
  const dischargeShare = Number(summary.value.total || 0) > 0
    ? Number(((Number(summary.value.dischargeCount || 0) / Number(summary.value.total || 1)) * 100).toFixed(2))
    : 0
  return {
    freshness: refreshedAt.value || '--',
    completeness: `${rows.value.length}/${summary.value.total || 0} 条已加载`,
    issues: [
      query.value.keyword ? `姓名关键字：${query.value.keyword}` : '',
      dischargeShare > Number(targets.value.dischargeShare || 0) ? '离院占比高于关注线' : ''
    ].filter(Boolean)
  }
})
const emptyState = computed(() => ({
  visible: !rows.value.length,
  title: '当前筛选下没有出入院记录',
  description: '建议先移除姓名关键字或院内老人筛选，再扩大日期范围。',
  hints: [
    '如果仅筛某位老人，先确认该老人是否存在出入院记录',
    '若只看离院，可尝试切换全部事件确认是否为单侧无数据',
    '支持直接复制当前筛选链接给业务同事复核'
  ]
}))
const insightItems = computed(() => {
  const dailySeriesMap = new Map<string, number>()
  rows.value.forEach((item) => {
    const key = String(item.eventDate || '')
    dailySeriesMap.set(key, Number(dailySeriesMap.get(key) || 0) + 1)
  })
  const series = Array.from(dailySeriesMap.entries()).map(([period, value]) => ({ period, value }))
  const seriesSummary = buildComparisonSummary(series, targets.value.totalRecords)
  const dischargeShare = Number(summary.value.total || 0) > 0
    ? Number(((Number(summary.value.dischargeCount || 0) / Number(summary.value.total || 1)) * 100).toFixed(2))
    : 0
  return [
    {
      key: 'total',
      label: '匹配总记录',
      valueText: `${summary.value.total || 0}`,
      trendText: `目标差 ${Number((summary.value.total || 0) - Number(targets.value.totalRecords || 0)) >= 0 ? '+' : ''}${Number((summary.value.total || 0) - Number(targets.value.totalRecords || 0))}`,
      detail: seriesSummary.anomalyText,
      tone: Number(summary.value.total || 0) < Number(targets.value.totalRecords || 0) ? 'warning' : 'good'
    },
    {
      key: 'admission',
      label: '入院记录',
      valueText: `${summary.value.admissionCount || 0}`,
      trendText: `占比 ${summary.value.total ? ((Number(summary.value.admissionCount || 0) / Number(summary.value.total || 1)) * 100).toFixed(2) : '0.00'}%`,
      detail: '适合排查新入住高峰与批量办理周期',
      tone: 'neutral'
    },
    {
      key: 'discharge',
      label: '离院记录',
      valueText: `${summary.value.dischargeCount || 0}`,
      trendText: `关注线 ${Number(targets.value.dischargeShare || 0).toFixed(2)}%`,
      detail: dischargeShare > Number(targets.value.dischargeShare || 0) ? '当前离院占比偏高，建议联动离院原因排查' : '离院占比处于关注线内',
      tone: dischargeShare > Number(targets.value.dischargeShare || 0) ? 'danger' : 'good'
    },
    {
      key: 'loaded',
      label: '当前页加载',
      valueText: `${rows.value.length}`,
      trendText: `页码 ${query.value.pageNo}/${Math.max(1, Math.ceil(Number(summary.value.total || 0) / Number(query.value.pageSize || 20)))}`,
      detail: printElderId.value ? `已锁定老人 ${selectedPrintElder.value?.name || printElderId.value}` : '当前为全量老人视角',
      tone: 'neutral'
    }
  ]
})
const visibleInsightItems = computed(() => {
  const selected = panelKeys.value.length ? new Set(panelKeys.value) : null
  return insightItems.value.filter((item) => !selected || selected.has(item.key))
})
const commandSummary = computed(() => '把离院异常、批量打印模板、协作链接和纠错入口合并到出入报表工作台。')
const commandActionItems = computed(() => ([
  { key: 'open-checkin', label: '查看入住统计', description: '回到月度入住趋势判断波峰', route: '/stats/check-in', tone: 'warning' as const },
  { key: 'open-elder-info', label: '查看老人画像', description: '核对出入老人结构', route: '/stats/elder-info', tone: 'danger' as const },
  { key: 'open-org-flow', label: '查看机构出入趋势', description: '切到机构维度看月度波动', route: '/stats/org/elder-flow', tone: 'neutral' as const }
]))
const commandAnomalies = computed(() =>
  insightItems.value
    .filter((item) => item.tone === 'danger' || item.tone === 'warning')
    .map((item) => ({ key: item.key, label: item.label, detail: item.detail, tone: item.tone }))
)
const metricNotes = computed(() => ([
  { key: 'keyword', label: '姓名关键字', note: '姓名关键字检索已直通后端分页与导出，不再只是前端过滤。' },
  { key: 'discharge', label: '离院占比', note: '离院占比偏高时，建议联动离院原因、床位周转和收入变化一起判断。' },
  { key: 'batch', label: '批量操作', note: '当前页支持勾选批量打印与导出，适合业务复核。' }
]))
const panelOptions = computed(() =>
  insightItems.value.map((item) => ({
    key: item.key,
    label: item.label,
    description: item.detail || item.trendText
  }))
)

async function loadData() {
  if (query.value.fromDate.isAfter(query.value.toDate, 'day')) {
    message.warning('开始日期不能晚于结束日期')
    return
  }
  try {
    const res = await getElderFlowReport({
      fromDate: dayjs(query.value.fromDate).format('YYYY-MM-DD'),
      toDate: dayjs(query.value.toDate).format('YYYY-MM-DD'),
      eventType: query.value.eventType,
      elderId: printElderId.value,
      keyword: query.value.keyword,
      pageNo: query.value.pageNo,
      pageSize: query.value.pageSize,
      orgId: query.value.orgId
    })
    rows.value = (res.list || []).map((item, index) => ({
      ...item,
      rowKey: buildRowKey(item, index)
    }))
    total.value = Number(res.total || 0)
    summary.value.total = Number(res.total || 0)
    summary.value.admissionCount = Number(res.admissionCount || 0)
    summary.value.dischargeCount = Number(res.dischargeCount || 0)
    refreshedAt.value = dayjs().format('YYYY-MM-DD HH:mm:ss')
    selectedRows.value = []
    await nextTick()
    tableRef.value?.clearCheckboxRow?.()
    syncRouteQuery()
  } catch (error: any) {
    message.error(error?.message || '加载老人出入报表失败')
  }
}

function search() {
  query.value.pageNo = 1
  loadData()
}

function applyPreset(payload: Record<string, any>) {
  if (payload.fromDate && dayjs(String(payload.fromDate)).isValid()) query.value.fromDate = dayjs(String(payload.fromDate))
  if (payload.toDate && dayjs(String(payload.toDate)).isValid()) query.value.toDate = dayjs(String(payload.toDate))
  const eventType = String(payload.eventType || '')
  query.value.eventType = eventType === 'ADMISSION' || eventType === 'DISCHARGE' ? eventType : undefined
  query.value.keyword = String(payload.keyword || '')
  query.value.orgId = normalizeId(payload.orgId)
  printElderId.value = normalizeId(payload.elderId)
  query.value.pageNo = 1
  loadData()
}

function onTargetsChange(payload: Record<string, number>) {
  targets.value = {
    ...targets.value,
    ...(payload || {})
  }
}

function onPanelChange(keys: string[]) {
  panelKeys.value = keys.length ? [...keys] : insightItems.value.map((item) => item.key)
}

function onCommandAction(item: { route?: string }) {
  if (item.route) {
    router.push(item.route)
  }
}

function applyReportTemplate(payload: { payload: Record<string, any>; columns: string[] }) {
  if (payload.columns?.length) {
    selectedPrintColumns.value = [...payload.columns]
  }
  applyPreset(payload.payload || {})
}

function buildRowKey(row: FlowReportItem, index: number) {
  return [
    row.eventDate || 'NA',
    row.eventType || 'NA',
    row.elderId || 'NA',
    index
  ].join('_')
}

function reset() {
  query.value.fromDate = dayjs().subtract(29, 'day')
  query.value.toDate = dayjs()
  query.value.eventType = undefined
  query.value.keyword = ''
  query.value.orgId = undefined
  query.value.pageNo = 1
  query.value.pageSize = 20
  printElderId.value = undefined
  loadData()
}

function onPageSizeChange(_: number, pageSize: number) {
  query.value.pageNo = 1
  query.value.pageSize = pageSize
  loadData()
}

function openColumnSetting() {
  columnSettingOpen.value = true
}

async function exportCsvReport() {
  if (query.value.fromDate.isAfter(query.value.toDate, 'day')) {
    message.warning('开始日期不能晚于结束日期')
    return
  }
  try {
    await exportElderFlowReportCsv({
      fromDate: dayjs(query.value.fromDate).format('YYYY-MM-DD'),
      toDate: dayjs(query.value.toDate).format('YYYY-MM-DD'),
      eventType: query.value.eventType,
      elderId: printElderId.value,
      keyword: query.value.keyword,
      orgId: query.value.orgId
    })
    message.success('报表导出成功')
  } catch (error: any) {
    message.error(error?.message || '报表导出失败')
  }
}

async function copyFilterLink() {
  const resolved = router.resolve({
    path: route.path,
    query: {
      fromDate: dayjs(query.value.fromDate).format('YYYY-MM-DD'),
      toDate: dayjs(query.value.toDate).format('YYYY-MM-DD'),
      eventType: query.value.eventType || '',
      keyword: query.value.keyword || '',
      orgId: query.value.orgId ? String(query.value.orgId) : '',
      elderId: printElderId.value ? String(printElderId.value) : '',
      pageNo: String(query.value.pageNo),
      pageSize: String(query.value.pageSize)
    }
  })
  const ok = await copyText(`${window.location.origin}${resolved.fullPath}`)
  if (ok) {
    message.success('筛选链接已复制')
  } else {
    message.warning('复制失败，请手动复制地址栏链接')
  }
}

function refreshSelectedRows() {
  const records = tableRef.value?.getCheckboxRecords?.() || []
  selectedRows.value = records
}

function selectAllCurrentPage() {
  tableRef.value?.setAllCheckboxRow?.(true)
  refreshSelectedRows()
}

function clearSelection() {
  tableRef.value?.clearCheckboxRow?.()
  selectedRows.value = []
}

function selectByEventType(eventType: 'ADMISSION' | 'DISCHARGE') {
  tableRef.value?.clearCheckboxRow?.()
  const targets = rows.value.filter(item => item.eventType === eventType)
  tableRef.value?.setCheckboxRow?.(targets, true)
  refreshSelectedRows()
}

function printSelectedRows() {
  if (!selectedRows.value.length) {
    message.warning('请先勾选需要操作的记录')
    return
  }
  const data = selectedRows.value.map(item => ({
    elderId: item.elderId,
    eventDate: item.eventDate || '',
    eventTypeLabel: item.eventType === 'ADMISSION' ? '入院' : '离院',
    elderName: item.elderName || '未知老人',
    remark: item.remark || ''
  }))
  renderPrint(
    `老人出入报表（批量勾选 ${data.length} 条）`,
    data,
    buildPrintScopeText(`批量勾选 ${data.length} 条`)
  )
}

function exportSelectedRowsCsv() {
  if (!selectedRows.value.length) {
    message.warning('请先勾选需要操作的记录')
    return
  }
  const headers = ['日期', '事件类型', '老人姓名', '备注']
  const lines = selectedRows.value.map(item => [
    csvSafe(item.eventDate || ''),
    csvSafe(item.eventType === 'ADMISSION' ? '入院' : '离院'),
    csvSafe(item.elderName || '未知老人'),
    csvSafe(item.remark || '')
  ].join(','))
  const csv = [headers.join(','), ...lines].join('\n')
  const blob = new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `elder-flow-selected-${dayjs().format('YYYYMMDD-HHmmss')}.csv`
  link.click()
  URL.revokeObjectURL(link.href)
  message.success(`已导出 ${selectedRows.value.length} 条勾选记录`)
}

function csvSafe(value: string) {
  const next = String(value || '')
  return `"${next.replace(/"/g, '""')}"`
}

function printCurrent() {
  renderPrint('老人出入报表（当前结果）', printRows.value, buildPrintScopeText('当前查询结果'))
}

function printSpecificElder() {
  let filtered = printRows.value
  let title = '老人出入报表（指定老人）'
  let printScope = '当前查询结果'

  if (printElderId.value) {
    filtered = filtered.filter((item) => String(item.elderId || '').trim() === String(printElderId.value || '').trim())
    const selectedName = selectedPrintElder.value?.name || '未命名长者'
    title = `老人出入报表（${selectedName}）`
    printScope = buildPrintScopeText(`指定老人：${selectedName}`)
  } else {
    message.warning('请选择院内老人')
    return
  }

  if (!filtered.length) {
    message.warning('未找到匹配老人记录')
    return
  }
  renderPrint(title, filtered, printScope)
}

function onPrintElderChange(value?: Id) {
  printElderId.value = value
  query.value.pageNo = 1
  loadData()
}

function setDayPreset(days: number) {
  query.value.toDate = dayjs()
  query.value.fromDate = dayjs().subtract(days - 1, 'day')
  query.value.pageNo = 1
  loadData()
}

async function renderPrint(title: string, data: Array<Record<string, any>>, printScope: string) {
  if (!selectedPrintColumns.value.length) {
    message.warning('请至少选择一列打印')
    return
  }
  if (data.length > LARGE_PRINT_ROW_THRESHOLD) {
    const confirmed = await confirmLargePrint(data.length)
    if (!confirmed) {
      return
    }
  }
  const headers = printColumnOptions.filter(item => selectedPrintColumns.value.includes(item.value))
  try {
    printTableReport({
      title,
      subtitle: `${dayjs(query.value.fromDate).format('YYYY-MM-DD')} ~ ${dayjs(query.value.toDate).format('YYYY-MM-DD')}；打印范围：${printScope}；记录数：${data.length}`,
      columns: headers.map(item => ({ key: item.value, title: item.label })),
      rows: data
    })
  } catch (error: any) {
    message.error(error?.message || '打印失败')
  }
}

function confirmLargePrint(rowCount: number) {
  return new Promise<boolean>((resolve) => {
    Modal.confirm({
      title: '打印确认',
      content: `本次将打印 ${rowCount} 条记录，可能耗时较长，是否继续？`,
      okText: '继续打印',
      cancelText: '取消',
      onOk: () => resolve(true),
      onCancel: () => resolve(false)
    })
  })
}

function buildPrintScopeText(baseScope: string) {
  const eventTypeText = query.value.eventType === 'ADMISSION'
    ? '仅入院'
    : query.value.eventType === 'DISCHARGE'
      ? '仅离院'
      : '全部事件'
  const keywordText = query.value.keyword ? `；姓名关键字：${query.value.keyword}` : ''
  return `${baseScope}；事件类型：${eventTypeText}${keywordText}`
}

function syncRouteQuery() {
  const nextQuery: Record<string, string> = {
    fromDate: dayjs(query.value.fromDate).format('YYYY-MM-DD'),
    toDate: dayjs(query.value.toDate).format('YYYY-MM-DD'),
    pageNo: String(query.value.pageNo),
    pageSize: String(query.value.pageSize)
  }
  if (query.value.eventType) nextQuery.eventType = query.value.eventType
  if (query.value.keyword) nextQuery.keyword = query.value.keyword
  if (query.value.orgId) nextQuery.orgId = String(query.value.orgId)
  if (printElderId.value) nextQuery.elderId = String(printElderId.value)

  router.replace({ path: route.path, query: nextQuery }).catch(() => {})
}

function initFromRouteQuery() {
  const fromDate = String(route.query.fromDate || '')
  const toDate = String(route.query.toDate || '')
  if (dayjs(fromDate).isValid()) {
    query.value.fromDate = dayjs(fromDate)
  }
  if (dayjs(toDate).isValid()) {
    query.value.toDate = dayjs(toDate)
  }
  const eventType = String(route.query.eventType || '')
  if (eventType === 'ADMISSION' || eventType === 'DISCHARGE') {
    query.value.eventType = eventType
  }
  query.value.keyword = String(route.query.keyword || '').trim()
  query.value.orgId = normalizeId(route.query.orgId)
  printElderId.value = normalizeId(route.query.elderId)
  const pageNo = Number(route.query.pageNo)
  if (Number.isFinite(pageNo) && pageNo > 0) {
    query.value.pageNo = pageNo
  }
  const pageSize = Number(route.query.pageSize)
  if (Number.isFinite(pageSize) && pageSize > 0) {
    query.value.pageSize = pageSize
  }
}

useLiveSyncRefresh({
  topics: ['elder', 'lifecycle', 'bed'],
  refresh: () => {
    loadData()
  },
  debounceMs: 800
})

onMounted(() => {
  initFromRouteQuery()
  loadData()
})
</script>

<style scoped>
.batch-action-bar {
  margin-bottom: 10px;
  padding: 10px 12px;
  background: var(--ant-color-fill-tertiary, #fafafa);
  border-radius: 8px;
}
</style>
