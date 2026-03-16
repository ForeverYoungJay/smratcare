<template>
  <PageContainer title="床位使用统计" subTitle="机构床位使用概况">
    <a-card class="card-elevated" :bordered="false" style="margin-bottom: 16px;">
      <a-form layout="inline">
        <a-form-item label="机构ID">
          <a-input-number v-model:value="query.orgId" :min="1" placeholder="默认当前机构" style="width: 160px" />
        </a-form-item>
        <a-form-item label="打印对象">
          <a-select v-model:value="query.targetType" style="width: 140px">
            <a-select-option value="ALL">全部</a-select-option>
            <a-select-option value="OCCUPIED">已使用</a-select-option>
            <a-select-option value="AVAILABLE">空闲</a-select-option>
            <a-select-option value="MAINTENANCE">维护</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="打印备注">
          <a-input v-model:value="query.printRemark" allow-clear placeholder="例如：床态巡检版" style="width: 200px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="loadData">刷新</a-button>
            <a-button @click="exportSummary">导出汇总</a-button>
            <a-button @click="openColumnSetting">列设置</a-button>
            <a-button @click="printCurrent">打印</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-row :gutter="16">
      <a-col :span="4">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="总床位" :value="stats.totalBeds || 0" />
        </a-card>
      </a-col>
      <a-col :span="4">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="已使用床位" :value="stats.occupiedBeds || 0" />
        </a-card>
      </a-col>
      <a-col :span="4">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="空闲床位" :value="stats.availableBeds || 0" />
        </a-card>
      </a-col>
      <a-col :span="4">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="维护床位" :value="stats.maintenanceBeds || 0" />
        </a-card>
      </a-col>
      <a-col :span="4">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="使用率(%)" :value="stats.occupancyRate || 0" :precision="2" />
        </a-card>
      </a-col>
      <a-col :span="4">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="空闲率(%)" :value="stats.availableRate || 0" :precision="2" />
        </a-card>
      </a-col>
    </a-row>

    <StatsWorkspacePanel
      page-key="stats-org-bed-usage"
      title="床态策略区"
      :summary-text="workspaceSummary"
      :current-payload="workspacePayload"
      :target-fields="targetFields"
      :data-health="dataHealth"
      @apply-preset="applyPreset"
      @targets-change="onTargetsChange"
    />

    <StatsInsightDeck :items="visibleInsightItems" />

    <StatsCommandCenter
      page-key="stats-org-bed-usage-command"
      title="床态协同台"
      share-title="床位使用统计协同包"
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

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="床位状态分布">
      <div ref="pieRef" style="height: 360px;"></div>
    </a-card>

    <a-modal v-model:open="columnSettingOpen" title="打印列设置" @ok="columnSettingOpen = false" cancel-text="关闭" ok-text="确定">
      <a-checkbox-group v-model:value="selectedPrintColumns" :options="printColumnOptions" />
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import StatsWorkspacePanel from '../../components/stats/StatsWorkspacePanel.vue'
import StatsInsightDeck from '../../components/stats/StatsInsightDeck.vue'
import StatsCommandCenter from '../../components/stats/StatsCommandCenter.vue'
import { exportOrgBedUsageCsv, getOrgBedUsage } from '../../api/stats'
import type { BedUsageStatsResponse } from '../../types'
import { useECharts } from '../../plugins/echarts'
import { message } from 'ant-design-vue'
import { printTableReport } from '../../utils/print'

const router = useRouter()
const stats = reactive<BedUsageStatsResponse>({
  totalBeds: 0,
  occupiedBeds: 0,
  availableBeds: 0,
  maintenanceBeds: 0,
  occupancyRate: 0,
  maintenanceRate: 0,
  availableRate: 0
})
const query = reactive({
  orgId: undefined as number | undefined,
  targetType: 'ALL' as 'ALL' | 'OCCUPIED' | 'AVAILABLE' | 'MAINTENANCE',
  printRemark: ''
})
const { chartRef: pieRef, setOption } = useECharts()
const columnSettingOpen = ref(false)
const printColumnOptions = [
  { label: '总床位', value: 'totalBeds' },
  { label: '已使用床位', value: 'occupiedBeds' },
  { label: '空闲床位', value: 'availableBeds' },
  { label: '维护床位', value: 'maintenanceBeds' },
  { label: '使用率(%)', value: 'occupancyRate' },
  { label: '空闲率(%)', value: 'availableRate' }
]
const selectedPrintColumns = ref<string[]>(['totalBeds', 'occupiedBeds', 'availableBeds', 'maintenanceBeds', 'occupancyRate', 'availableRate'])
const panelKeys = ref<string[]>([])
const targets = ref<Record<string, number>>({
  occupancyRate: 95,
  availableRate: 10,
  maintenanceBeds: 5
})
const workspaceSummary = computed(() => `聚焦床位使用率、空闲率和维护床位数量，支持固定巡检策略。当前总床位 ${stats.totalBeds || 0} 张。`)
const workspacePayload = computed(() => ({
  orgId: query.orgId ? String(query.orgId) : '',
  targetType: query.targetType
}))
const targetFields = computed(() => ([
  { key: 'occupancyRate', label: '床位使用率上限(%)', value: targets.value.occupancyRate, min: 1, max: 100, step: 1 },
  { key: 'availableRate', label: '空闲率目标(%)', value: targets.value.availableRate, min: 0, max: 100, step: 1 },
  { key: 'maintenanceBeds', label: '维护床位提醒线', value: targets.value.maintenanceBeds, min: 0, max: 9999, step: 1 }
]))
const dataHealth = computed(() => ({
  freshness: '实时',
  completeness: `${stats.totalBeds || 0} 床位已汇总`,
  issues: [
    Number(stats.occupancyRate || 0) > Number(targets.value.occupancyRate || 0) ? '床位使用率超过上限' : '',
    Number(stats.maintenanceBeds || 0) > Number(targets.value.maintenanceBeds || 0) ? '维护床位超过提醒线' : ''
  ].filter(Boolean)
}))
const insightItems = computed(() => [
  {
    key: 'occupancy',
    label: '床位使用率',
    valueText: `${Number(stats.occupancyRate || 0).toFixed(2)}%`,
    trendText: `上限 ${Number(targets.value.occupancyRate || 0).toFixed(2)}%`,
    detail: Number(stats.occupancyRate || 0) > Number(targets.value.occupancyRate || 0) ? '已进入高占用区，建议提前协调床位' : '使用率处于目标区间',
    tone: Number(stats.occupancyRate || 0) > Number(targets.value.occupancyRate || 0) ? 'danger' : 'good'
  },
  {
    key: 'available',
    label: '空闲率',
    valueText: `${Number(stats.availableRate || 0).toFixed(2)}%`,
    trendText: `目标 ${Number(targets.value.availableRate || 0).toFixed(2)}%`,
    detail: `空闲床位 ${stats.availableBeds || 0} 张`,
    tone: Number(stats.availableRate || 0) < Number(targets.value.availableRate || 0) ? 'warning' : 'good'
  },
  {
    key: 'maintenance',
    label: '维护床位',
    valueText: `${stats.maintenanceBeds || 0}`,
    trendText: `提醒线 ${targets.value.maintenanceBeds || 0}`,
    detail: `维护率 ${Number(stats.maintenanceRate || 0).toFixed(2)}%`,
    tone: Number(stats.maintenanceBeds || 0) > Number(targets.value.maintenanceBeds || 0) ? 'warning' : 'good'
  },
  {
    key: 'capacity',
    label: '容量概览',
    valueText: `${stats.occupiedBeds || 0}/${stats.totalBeds || 0}`,
    trendText: '已使用 / 总床位',
    detail: `空闲 ${stats.availableBeds || 0} · 维护 ${stats.maintenanceBeds || 0}`,
    tone: 'neutral'
  }
])
const visibleInsightItems = computed(() => {
  const selected = panelKeys.value.length ? new Set(panelKeys.value) : null
  return insightItems.value.filter((item) => !selected || selected.has(item.key))
})
const commandSummary = computed(() => '把高占用告警、模板化打印和后续钻取收口到床态协同台，适合床位巡检。')
const commandActionItems = computed(() => ([
  { key: 'open-flow-report', label: '查看出入报表', description: '高占用时继续排查入住离院明细', route: '/stats/elder-flow-report', tone: 'danger' as const },
  { key: 'open-operation', label: '查看机构经营', description: '结合营收与入住判断床位压力', route: '/stats/org/monthly-operation', tone: 'warning' as const },
  { key: 'open-dashboard', label: '返回看板', description: '回到经营总览', route: '/dashboard', tone: 'neutral' as const }
]))
const commandAnomalies = computed(() =>
  insightItems.value
    .filter((item) => item.tone === 'danger' || item.tone === 'warning')
    .map((item) => ({ key: item.key, label: item.label, detail: item.detail, tone: item.tone }))
)
const metricNotes = computed(() => ([
  { key: 'occupancy', label: '床位使用率', note: '已使用床位 / 总床位，用于判断高占用风险。' },
  { key: 'available', label: '空闲率', note: '空闲率过低时应结合入住净增长和离院计划一起看。' },
  { key: 'maintenance', label: '维护床位', note: '维护床位偏高会直接影响可售床位容量。' }
]))
const panelOptions = computed(() =>
  insightItems.value.map((item) => ({
    key: item.key,
    label: item.label,
    description: item.detail || item.trendText
  }))
)

async function loadData() {
  try {
    const data = await getOrgBedUsage({ orgId: query.orgId })
    Object.assign(stats, data)
    setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: 0 },
      series: [
        {
          type: 'pie',
          radius: ['36%', '66%'],
          data: [
            { name: '已使用', value: data.occupiedBeds || 0 },
            { name: '空闲', value: data.availableBeds || 0 },
            { name: '维护', value: data.maintenanceBeds || 0 }
          ]
        }
      ]
    })
  } catch (error: any) {
    message.error(error?.message || '加载床位统计失败')
  }
}

function reset() {
  query.orgId = undefined
  query.targetType = 'ALL'
  query.printRemark = ''
  loadData()
}

function applyPreset(payload: Record<string, any>) {
  query.orgId = payload.orgId ? Number(payload.orgId) : undefined
  const targetType = String(payload.targetType || 'ALL')
  query.targetType = ['ALL', 'OCCUPIED', 'AVAILABLE', 'MAINTENANCE'].includes(targetType) ? targetType as any : 'ALL'
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

function openColumnSetting() {
  columnSettingOpen.value = true
}

async function exportSummary() {
  try {
    await exportOrgBedUsageCsv({ orgId: query.orgId })
    message.success('导出成功')
  } catch (error: any) {
    message.error(error?.message || '导出失败')
  }
}

function printCurrent() {
  if (!selectedPrintColumns.value.length) {
    message.warning('请至少选择一列打印')
    return
  }
  const source = {
    totalBeds: stats.totalBeds || 0,
    occupiedBeds: stats.occupiedBeds || 0,
    availableBeds: stats.availableBeds || 0,
    maintenanceBeds: stats.maintenanceBeds || 0,
    occupancyRate: `${Number(stats.occupancyRate || 0).toFixed(2)}%`,
    availableRate: `${Number(stats.availableRate || 0).toFixed(2)}%`
  }
  const targetRows: Array<Record<string, any>> = []
  if (query.targetType === 'ALL') {
    targetRows.push(source)
  } else if (query.targetType === 'OCCUPIED') {
    targetRows.push({ ...source, totalBeds: source.occupiedBeds, availableBeds: '-', maintenanceBeds: '-', occupancyRate: source.occupancyRate, availableRate: '-' })
  } else if (query.targetType === 'AVAILABLE') {
    targetRows.push({ ...source, totalBeds: source.availableBeds, occupiedBeds: '-', maintenanceBeds: '-', occupancyRate: '-', availableRate: source.availableRate })
  } else {
    targetRows.push({ ...source, totalBeds: source.maintenanceBeds, occupiedBeds: '-', availableBeds: '-', occupancyRate: '-', availableRate: '-' })
  }
  try {
    const orgText = query.orgId ? `机构ID：${query.orgId}` : '机构：当前'
    const targetLabelMap: Record<string, string> = {
      ALL: '全部',
      OCCUPIED: '已使用',
      AVAILABLE: '空闲',
      MAINTENANCE: '维护'
    }
    printTableReport({
      title: '床位使用统计',
      subtitle: `${orgText}；对象：${targetLabelMap[query.targetType]}；备注：${query.printRemark || '-'}`,
      columns: printColumnOptions.filter(item => selectedPrintColumns.value.includes(item.value)).map(item => ({ key: item.value, title: item.label })),
      rows: targetRows
    })
  } catch (error: any) {
    message.error(error?.message || '打印失败')
  }
}

onMounted(loadData)
</script>
