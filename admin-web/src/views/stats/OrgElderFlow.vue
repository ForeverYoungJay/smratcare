<template>
  <PageContainer title="老人出入统计" subTitle="机构维度老人出入院趋势">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline">
        <a-form-item label="起始月份">
          <a-date-picker v-model:value="query.from" picker="month" style="width: 160px" />
        </a-form-item>
        <a-form-item label="结束月份">
          <a-date-picker v-model:value="query.to" picker="month" style="width: 160px" />
        </a-form-item>
        <a-form-item label="机构ID">
          <a-input-number v-model:value="query.orgId" :min="1" placeholder="默认当前机构" style="width: 160px" />
        </a-form-item>
        <a-form-item label="月份筛选">
          <a-input v-model:value="query.monthKeyword" allow-clear placeholder="如 2026-03" style="width: 140px" />
        </a-form-item>
        <a-form-item label="打印备注">
          <a-input v-model:value="query.printRemark" allow-clear placeholder="例如：经营例会版" style="width: 200px" />
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
            <a-button @click="exportCsvReport">导出报表</a-button>
            <a-button @click="copyFilterLink">复制筛选链接</a-button>
            <a-button @click="openColumnSetting">列设置</a-button>
            <a-button :disabled="!displayRows.length" @click="printCurrent">打印当前列</a-button>
            <a-button :disabled="!query.monthKeyword.trim() || !displayRows.length" @click="printSpecificMonth">打印指定月份</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
      <StatsMetaHint
        scope-text="老人出入统计（按月）"
        :refreshed-at="refreshedAt"
        :note-text="metricTraceText"
      />
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="老人出入趋势">
      <div ref="trendRef" style="height: 320px;"></div>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="老人出入明细">
      <vxe-table border stripe show-overflow :data="displayRows" height="320">
        <vxe-column field="month" title="月份" width="140" />
        <vxe-column field="admissions" title="入住人数" width="120" />
        <vxe-column field="discharges" title="离院人数" width="120" />
      </vxe-table>
    </a-card>

    <a-modal v-model:open="columnSettingOpen" title="打印列设置" @ok="columnSettingOpen = false" cancel-text="关闭" ok-text="确定">
      <a-checkbox-group v-model:value="selectedPrintColumns" :options="printColumnOptions" />
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import dayjs, { type Dayjs } from 'dayjs'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import StatsMetaHint from '../../components/stats/StatsMetaHint.vue'
import { exportOrgElderFlowCsv, getOrgElderFlow } from '../../api/stats'
import type { MonthFlowItem } from '../../types'
import { useECharts } from '../../plugins/echarts'
import { message } from 'ant-design-vue'
import { printTableReport } from '../../utils/print'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'
import { copyText } from '../../utils/clipboard'

const route = useRoute()
const router = useRouter()
const refreshedAt = ref('')

const query = ref({
  from: dayjs().subtract(5, 'month') as Dayjs,
  to: dayjs() as Dayjs,
  orgId: undefined as number | undefined,
  monthKeyword: '',
  printRemark: ''
})

const rows = ref<MonthFlowItem[]>([])
const { chartRef: trendRef, setOption } = useECharts()
const columnSettingOpen = ref(false)
const printColumnOptions = [
  { label: '月份', value: 'month' },
  { label: '入住人数', value: 'admissions' },
  { label: '离院人数', value: 'discharges' }
]
const selectedPrintColumns = ref<string[]>(['month', 'admissions', 'discharges'])
const metricTraceText = computed(() => {
  const version = String(route.query.metricVersion || '').trim()
  const source = String(route.query.fromSource || '').trim()
  const notes: string[] = []
  if (version) notes.push(`口径版本：${version}`)
  if (source === 'dashboard') notes.push('来源：看板钻取')
  return notes.join('；')
})
const printableRows = computed(() => (rows.value || []).map(item => ({
  month: item.month,
  admissions: item.admissions,
  discharges: item.discharges
})))
const displayRows = computed(() => {
  const keyword = String(query.value.monthKeyword || '').trim()
  if (!keyword) return rows.value
  return rows.value.filter(item => String(item.month || '').includes(keyword))
})

async function loadData() {
  if (query.value.from.isAfter(query.value.to, 'month')) {
    message.warning('起始月份不能晚于结束月份')
    return
  }
  try {
    rows.value = await getOrgElderFlow({
      from: dayjs(query.value.from).format('YYYY-MM'),
      to: dayjs(query.value.to).format('YYYY-MM'),
      orgId: query.value.orgId
    })
    setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['入住', '离院'] },
      xAxis: { type: 'category', data: rows.value.map(item => item.month) },
      yAxis: { type: 'value' },
      series: [
        { name: '入住', type: 'bar', data: rows.value.map(item => item.admissions) },
        { name: '离院', type: 'bar', data: rows.value.map(item => item.discharges) }
      ]
    })
    refreshedAt.value = dayjs().format('YYYY-MM-DD HH:mm:ss')
    syncRouteQuery()
  } catch (error: any) {
    message.error(error?.message || '加载老人出入统计失败')
  }
}

function reset() {
  query.value.from = dayjs().subtract(5, 'month')
  query.value.to = dayjs()
  query.value.orgId = undefined
  query.value.monthKeyword = ''
  query.value.printRemark = ''
  syncRouteQuery()
  loadData()
}

async function exportCsvReport() {
  if (query.value.from.isAfter(query.value.to, 'month')) {
    message.warning('起始月份不能晚于结束月份')
    return
  }
  try {
    await exportOrgElderFlowCsv({
      from: dayjs(query.value.from).format('YYYY-MM'),
      to: dayjs(query.value.to).format('YYYY-MM'),
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
      from: dayjs(query.value.from).format('YYYY-MM'),
      to: dayjs(query.value.to).format('YYYY-MM'),
      orgId: query.value.orgId ? String(query.value.orgId) : '',
      monthKeyword: query.value.monthKeyword || ''
    }
  })
  const ok = await copyText(`${window.location.origin}${resolved.fullPath}`)
  if (ok) {
    message.success('筛选链接已复制')
  } else {
    message.warning('复制失败，请手动复制地址栏链接')
  }
}

function openColumnSetting() {
  columnSettingOpen.value = true
}

function setMonthPreset(months: number) {
  query.value.to = dayjs()
  query.value.from = dayjs().subtract(months - 1, 'month')
  loadData()
}

function setThisMonth() {
  query.value.from = dayjs().startOf('month')
  query.value.to = dayjs()
  loadData()
}

function printCurrent() {
  const keyword = String(query.value.monthKeyword || '').trim()
  const data = !keyword ? printableRows.value : printableRows.value.filter(item => item.month.includes(keyword))
  renderPrint('老人出入统计（当前结果）', data)
}

function printSpecificMonth() {
  const keyword = String(query.value.monthKeyword || '').trim()
  if (!keyword) {
    message.warning('请输入月份关键字')
    return
  }
  const filtered = printableRows.value.filter(item => item.month.includes(keyword))
  if (!filtered.length) {
    message.warning('未找到匹配月份记录')
    return
  }
  renderPrint(`老人出入统计（${keyword}）`, filtered)
}

function renderPrint(title: string, data: Array<Record<string, any>>) {
  if (!selectedPrintColumns.value.length) {
    message.warning('请至少选择一列打印')
    return
  }
  try {
    const orgText = query.value.orgId ? `机构ID：${query.value.orgId}` : '机构：当前'
    const monthKeywordText = query.value.monthKeyword ? `月份筛选：${query.value.monthKeyword}` : '月份筛选：全部'
    printTableReport({
      title,
      subtitle: `${dayjs(query.value.from).format('YYYY-MM')} ~ ${dayjs(query.value.to).format('YYYY-MM')}；${orgText}；打印范围：${monthKeywordText}；记录数：${data.length}；备注：${query.value.printRemark || '-'}`,
      columns: printColumnOptions.filter(item => selectedPrintColumns.value.includes(item.value)).map(item => ({ key: item.value, title: item.label })),
      rows: data
    })
  } catch (error: any) {
    message.error(error?.message || '打印失败')
  }
}

function syncRouteQuery() {
  const nextQuery: Record<string, string> = {
    from: dayjs(query.value.from).format('YYYY-MM'),
    to: dayjs(query.value.to).format('YYYY-MM')
  }
  if (query.value.orgId) nextQuery.orgId = String(query.value.orgId)
  if (query.value.monthKeyword) nextQuery.monthKeyword = query.value.monthKeyword
  router.replace({ path: route.path, query: nextQuery }).catch(() => {})
}

function initFromRouteQuery() {
  const from = String(route.query.from || '')
  const to = String(route.query.to || '')
  if (dayjs(from).isValid()) query.value.from = dayjs(from)
  if (dayjs(to).isValid()) query.value.to = dayjs(to)
  const orgId = Number(route.query.orgId)
  if (Number.isFinite(orgId) && orgId > 0) query.value.orgId = orgId
  const monthKeyword = String(route.query.monthKeyword || '')
  if (monthKeyword) query.value.monthKeyword = monthKeyword
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
