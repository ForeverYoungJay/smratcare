<template>
  <PageContainer title="入住统计" subTitle="按月份查看入住与离院趋势">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline">
        <a-form-item label="起始月份">
          <a-date-picker v-model:value="query.from" picker="month" style="width: 160px" />
        </a-form-item>
        <a-form-item label="结束月份">
          <a-date-picker v-model:value="query.to" picker="month" style="width: 160px" />
        </a-form-item>
        <a-form-item label="机构ID">
          <a-input v-model:value="query.orgId" allow-clear placeholder="默认当前机构" style="width: 160px" />
        </a-form-item>
        <a-form-item label="月份筛选">
          <a-input v-model:value="query.monthKeyword" allow-clear placeholder="如 2026-03" style="width: 140px" />
        </a-form-item>
        <a-form-item label="打印备注">
          <a-input v-model:value="query.printRemark" allow-clear placeholder="例如：运营例会版" style="width: 200px" />
        </a-form-item>
        <a-form-item label="快捷区间">
          <a-space>
            <a-button size="small" @click="setMonthPreset(3)">近3月</a-button>
            <a-button size="small" @click="setMonthPreset(6)">近6月</a-button>
            <a-button size="small" @click="setThisMonth">本月</a-button>
          </a-space>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="loadData">刷新</a-button>
            <a-button @click="exportSummary">导出汇总</a-button>
            <a-button @click="copyFilterLink">复制筛选链接</a-button>
            <a-button @click="openColumnSetting">列设置</a-button>
            <a-button :disabled="!displayRows.length" @click="printCurrent">打印当前列</a-button>
            <a-button :disabled="!query.monthKeyword.trim() || !displayRows.length" @click="printSpecificMonth">打印指定月份</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
      <StatsMetaHint
        scope-text="入住统计（按月，净增长=入住-离院）"
        :refreshed-at="refreshedAt"
        :note-text="metricTraceText"
      />
    </a-card>

    <a-row :gutter="16" style="margin-top: 16px;">
      <a-col :span="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="期间入住人数" :value="stats.totalAdmissions" />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="期间离院人数" :value="stats.totalDischarges" />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="净增长人数" :value="stats.netIncrease" />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="当前在院人数" :value="stats.currentResidents" />
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" style="margin-top: 16px;">
      <a-col :span="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="离院/入住比(%)" :value="stats.dischargeToAdmissionRate" :precision="2" />
        </a-card>
      </a-col>
    </a-row>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="入住/离院趋势">
      <div ref="trendRef" style="height: 320px;"></div>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="月度明细">
      <vxe-table border stripe show-overflow :data="displayRows" height="300">
        <vxe-column field="month" title="月份" width="140" />
        <vxe-column field="admissions" title="入住" width="120" />
        <vxe-column field="discharges" title="离院" width="120" />
        <vxe-column field="netIncrease" title="净增长" width="120" />
      </vxe-table>
    </a-card>

    <a-modal v-model:open="columnSettingOpen" title="打印列设置" @ok="columnSettingOpen = false" cancel-text="关闭" ok-text="确定">
      <a-checkbox-group v-model:value="selectedPrintColumns" :options="printColumnOptions" />
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs, { type Dayjs } from 'dayjs'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import StatsMetaHint from '../../components/stats/StatsMetaHint.vue'
import { exportCheckInStatsCsv, getCheckInStats } from '../../api/stats'
import type { CheckInStatsResponse, Id } from '../../types'
import { useECharts } from '../../plugins/echarts'
import { message } from 'ant-design-vue'
import { printTableReport } from '../../utils/print'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'
import { copyText } from '../../utils/clipboard'
import { normalizeId } from '../../utils/id'

const route = useRoute()
const router = useRouter()
const refreshedAt = ref('')

const query = reactive({
  from: dayjs().subtract(5, 'month') as Dayjs,
  to: dayjs() as Dayjs,
  orgId: undefined as Id | undefined,
  monthKeyword: '',
  printRemark: ''
})

const stats = reactive<CheckInStatsResponse>({
  totalAdmissions: 0,
  totalDischarges: 0,
  netIncrease: 0,
  dischargeToAdmissionRate: 0,
  currentResidents: 0,
  monthlyAdmissions: [],
  monthlyDischarges: [],
  monthlyNetIncrease: []
})

const { chartRef: trendRef, setOption } = useECharts()
const columnSettingOpen = ref(false)
const printColumnOptions = [
  { label: '月份', value: 'month' },
  { label: '入住', value: 'admissions' },
  { label: '离院', value: 'discharges' },
  { label: '净增长', value: 'netIncrease' }
]
const selectedPrintColumns = ref<string[]>(['month', 'admissions', 'discharges', 'netIncrease'])
const metricTraceText = computed(() => {
  const version = String(route.query.metricVersion || '').trim()
  const source = String(route.query.fromSource || '').trim()
  const notes: string[] = []
  if (version) notes.push(`口径版本：${version}`)
  if (source === 'dashboard') notes.push('来源：看板钻取')
  return notes.join('；')
})
const printableRows = computed(() => {
  const admissionsMap = new Map((stats.monthlyAdmissions || []).map(item => [item.month, item.count]))
  const dischargeMap = new Map((stats.monthlyDischarges || []).map(item => [item.month, item.count]))
  return (stats.monthlyNetIncrease || []).map(item => ({
    month: item.month,
    admissions: admissionsMap.get(item.month) || 0,
    discharges: dischargeMap.get(item.month) || 0,
    netIncrease: item.count || 0
  }))
})
const displayRows = computed(() => {
  const keyword = String(query.monthKeyword || '').trim()
  if (!keyword) return printableRows.value
  return printableRows.value.filter(item => item.month.includes(keyword))
})

async function loadData() {
  if (query.from.isAfter(query.to, 'month')) {
    message.warning('起始月份不能晚于结束月份')
    return
  }
  try {
    const data = await getCheckInStats({
      from: dayjs(query.from).format('YYYY-MM'),
      to: dayjs(query.to).format('YYYY-MM'),
      orgId: query.orgId
    })
    Object.assign(stats, data)
    setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['入住', '离院', '净增长'] },
      xAxis: { type: 'category', data: data.monthlyAdmissions.map(item => item.month) },
      yAxis: { type: 'value' },
      series: [
        { name: '入住', type: 'line', smooth: true, data: data.monthlyAdmissions.map(item => item.count) },
        { name: '离院', type: 'line', smooth: true, data: data.monthlyDischarges.map(item => item.count) },
        { name: '净增长', type: 'bar', data: data.monthlyNetIncrease.map(item => item.count) }
      ]
    })
    refreshedAt.value = dayjs().format('YYYY-MM-DD HH:mm:ss')
    syncRouteQuery()
  } catch (error: any) {
    message.error(error?.message || '加载入住统计失败')
  }
}

function reset() {
  query.from = dayjs().subtract(5, 'month')
  query.to = dayjs()
  query.orgId = undefined
  query.monthKeyword = ''
  query.printRemark = ''
  syncRouteQuery()
  loadData()
}

function openColumnSetting() {
  columnSettingOpen.value = true
}

function setMonthPreset(months: number) {
  query.to = dayjs()
  query.from = dayjs().subtract(months - 1, 'month')
  loadData()
}

function setThisMonth() {
  query.from = dayjs().startOf('month')
  query.to = dayjs()
  loadData()
}

async function exportSummary() {
  try {
    await exportCheckInStatsCsv({
      from: dayjs(query.from).format('YYYY-MM'),
      to: dayjs(query.to).format('YYYY-MM'),
      orgId: query.orgId
    })
    message.success('导出成功')
  } catch (error: any) {
    message.error(error?.message || '导出失败')
  }
}

async function copyFilterLink() {
  const resolved = router.resolve({
    path: route.path,
    query: {
      from: dayjs(query.from).format('YYYY-MM'),
      to: dayjs(query.to).format('YYYY-MM'),
      orgId: query.orgId ? String(query.orgId) : '',
      monthKeyword: query.monthKeyword || ''
    }
  })
  const ok = await copyText(`${window.location.origin}${resolved.fullPath}`)
  if (ok) {
    message.success('筛选链接已复制')
  } else {
    message.warning('复制失败，请手动复制地址栏链接')
  }
}

function printCurrent() {
  renderPrint('入住统计（当前结果）', displayRows.value)
}

function printSpecificMonth() {
  const keyword = String(query.monthKeyword || '').trim()
  if (!keyword) {
    message.warning('请输入月份关键字')
    return
  }
  const filtered = printableRows.value.filter(item => item.month.includes(keyword))
  if (!filtered.length) {
    message.warning('未找到匹配月份')
    return
  }
  renderPrint(`入住统计（${keyword}）`, filtered)
}

function renderPrint(title: string, rows: Array<Record<string, any>>) {
  if (!selectedPrintColumns.value.length) {
    message.warning('请至少选择一列打印')
    return
  }
  try {
    const orgText = query.orgId ? `机构ID：${query.orgId}` : '机构：当前'
    const monthKeywordText = query.monthKeyword ? `月份筛选：${query.monthKeyword}` : '月份筛选：全部'
    printTableReport({
      title,
      subtitle: `${dayjs(query.from).format('YYYY-MM')} ~ ${dayjs(query.to).format('YYYY-MM')}；${orgText}；打印范围：${monthKeywordText}；记录数：${rows.length}；备注：${query.printRemark || '-'}`,
      columns: printColumnOptions.filter(item => selectedPrintColumns.value.includes(item.value)).map(item => ({ key: item.value, title: item.label })),
      rows
    })
  } catch (error: any) {
    message.error(error?.message || '打印失败')
  }
}

function syncRouteQuery() {
  const nextQuery: Record<string, string> = {
    from: dayjs(query.from).format('YYYY-MM'),
    to: dayjs(query.to).format('YYYY-MM')
  }
  if (query.orgId) nextQuery.orgId = String(query.orgId)
  if (query.monthKeyword) nextQuery.monthKeyword = query.monthKeyword
  router.replace({ path: route.path, query: nextQuery }).catch(() => {})
}

function initFromRouteQuery() {
  const from = String(route.query.from || '')
  const to = String(route.query.to || '')
  if (dayjs(from).isValid()) query.from = dayjs(from)
  if (dayjs(to).isValid()) query.to = dayjs(to)
  query.orgId = normalizeId(route.query.orgId)
  const monthKeyword = String(route.query.monthKeyword || '')
  if (monthKeyword) query.monthKeyword = monthKeyword
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
