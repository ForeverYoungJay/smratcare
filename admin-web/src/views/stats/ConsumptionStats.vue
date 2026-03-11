<template>
  <PageContainer title="消费统计" subTitle="账单与商城消费分析">
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
        <a-form-item label="院内老人">
          <a-select
            v-model:value="query.elderId"
            show-search
            allow-clear
            :filter-option="false"
            :options="elderOptions"
            :loading="elderLoading"
            placeholder="按院内老人筛选"
            style="width: 180px"
            @search="searchElders"
            @focus="() => !elderOptions.length && searchElders('')"
            @change="onElderChange"
          />
        </a-form-item>
        <a-form-item label="打印备注">
          <a-input v-model:value="query.printRemark" allow-clear placeholder="例如：财务复核版" style="width: 200px" />
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
            <a-button @click="exportSummary">导出统计</a-button>
            <a-button @click="copyFilterLink">复制筛选链接</a-button>
            <a-button @click="openColumnSetting">列设置</a-button>
            <a-button :disabled="!displayTopRows.length" @click="printCurrent">打印当前列</a-button>
            <a-button :disabled="!query.elderId || !displayTopRows.length" @click="printSpecificElder">打印指定老人</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
      <StatsMetaHint
        scope-text="消费统计（总消费=账单+商城；排行口径为Top10）"
        :refreshed-at="refreshedAt"
        :note-text="metricTraceText"
      />
    </a-card>

    <a-row :gutter="16" style="margin-top: 16px;">
      <a-col :span="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="账单消费总额" :value="stats.totalBillConsumption || 0" :precision="2" />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="商城消费总额" :value="stats.totalStoreConsumption || 0" :precision="2" />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="消费合计" :value="stats.totalConsumption || 0" :precision="2" />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="月均消费" :value="stats.averageMonthlyConsumption || 0" :precision="2" />
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" style="margin-top: 16px;">
      <a-col :span="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="账单占比(%)" :value="stats.billConsumptionRatio || 0" :precision="2" />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="商城占比(%)" :value="stats.storeConsumptionRatio || 0" :precision="2" />
        </a-card>
      </a-col>
    </a-row>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="月度消费趋势">
      <div ref="trendRef" style="height: 320px;"></div>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="老人消费排行 Top10">
      <vxe-table border stripe show-overflow :data="displayTopRows" height="320">
        <vxe-column field="elderName" title="老人姓名" min-width="180">
          <template #default="{ row }">
            {{ row.elderName || '未知老人' }}
          </template>
        </vxe-column>
        <vxe-column field="amount" title="消费金额" width="180" />
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
import { exportConsumptionStatsCsv, getConsumptionStats } from '../../api/stats'
import type { ConsumptionStatsResponse, Id } from '../../types'
import { useECharts } from '../../plugins/echarts'
import { message } from 'ant-design-vue'
import { printTableReport } from '../../utils/print'
import { useElderOptions } from '../../composables/useElderOptions'
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
  elderId: undefined as Id | undefined,
  printRemark: ''
})

const stats = reactive<ConsumptionStatsResponse>({
  totalBillConsumption: 0,
  totalStoreConsumption: 0,
  totalConsumption: 0,
  billConsumptionRatio: 0,
  storeConsumptionRatio: 0,
  averageMonthlyConsumption: 0,
  monthlyBillConsumption: [],
  monthlyStoreConsumption: [],
  monthlyTotalConsumption: [],
  topConsumerElders: []
})

const { chartRef: trendRef, setOption } = useECharts()
const columnSettingOpen = ref(false)
const printColumnOptions = [
  { label: '老人姓名', value: 'elderName' },
  { label: '消费金额', value: 'amount' }
]
const selectedPrintColumns = ref<string[]>(['elderName', 'amount'])
const metricTraceText = computed(() => {
  const version = String(route.query.metricVersion || '').trim()
  const source = String(route.query.fromSource || '').trim()
  const notes: string[] = []
  if (version) notes.push(`口径版本：${version}`)
  if (source === 'dashboard') notes.push('来源：看板钻取')
  return notes.join('；')
})
const { elderOptions, elderLoading, searchElders } = useElderOptions({ pageSize: 100, inHospitalOnly: true, signedOnly: true })
const displayTopRows = computed(() => {
  const elderId = String(query.elderId || '').trim()
  const list = stats.topConsumerElders || []
  if (!elderId) return list
  return list.filter((item) => String(item.elderId || '').trim() === elderId)
})
const selectedElder = computed(() => elderOptions.value.find((item) => String(item.value || '').trim() === String(query.elderId || '').trim()))

async function loadData() {
  if (query.from.isAfter(query.to, 'month')) {
    message.warning('起始月份不能晚于结束月份')
    return
  }
  try {
    const data = await getConsumptionStats({
      from: dayjs(query.from).format('YYYY-MM'),
      to: dayjs(query.to).format('YYYY-MM'),
      orgId: query.orgId
    })
    Object.assign(stats, data)
    setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['账单消费', '商城消费', '总消费'] },
      xAxis: { type: 'category', data: data.monthlyBillConsumption.map(item => item.month) },
      yAxis: { type: 'value' },
      series: [
        { name: '账单消费', type: 'bar', data: data.monthlyBillConsumption.map(item => item.amount) },
        { name: '商城消费', type: 'line', smooth: true, data: data.monthlyStoreConsumption.map(item => item.amount) },
        { name: '总消费', type: 'line', smooth: true, data: data.monthlyTotalConsumption.map(item => item.amount) }
      ]
    })
    refreshedAt.value = dayjs().format('YYYY-MM-DD HH:mm:ss')
    syncRouteQuery()
  } catch (error: any) {
    message.error(error?.message || '加载消费统计失败')
  }
}

function reset() {
  query.from = dayjs().subtract(5, 'month')
  query.to = dayjs()
  query.orgId = undefined
  query.elderId = undefined
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
    await exportConsumptionStatsCsv({
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
      elderId: query.elderId ? String(query.elderId) : ''
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
  renderPrint('消费统计（当前结果）', displayTopRows.value.map(item => ({
    elderName: item.elderName || '未知老人',
    amount: item.amount
  })))
}

function printSpecificElder() {
  if (!query.elderId) {
    message.warning('请选择院内老人')
    return
  }
  const filtered = (stats.topConsumerElders || [])
    .filter((item) => String(item.elderId || '').trim() === String(query.elderId || '').trim())
    .map(item => ({ elderName: item.elderName || '未知老人', amount: item.amount }))
  if (!filtered.length) {
    message.warning('未找到匹配老人')
    return
  }
  const elderText = selectedElder.value ? `${selectedElder.value.name}` : '未命名长者'
  renderPrint(`消费统计（${elderText}）`, filtered)
}

function renderPrint(title: string, rows: Array<Record<string, any>>) {
  if (!selectedPrintColumns.value.length) {
    message.warning('请至少选择一列打印')
    return
  }
  try {
    const orgText = query.orgId ? `机构ID：${query.orgId}` : '机构：当前'
  const elderText = selectedElder.value
      ? `院内老人：${selectedElder.value.name}`
      : '院内老人：全部'
    printTableReport({
      title,
      subtitle: `${dayjs(query.from).format('YYYY-MM')} ~ ${dayjs(query.to).format('YYYY-MM')}；${orgText}；打印范围：${elderText}；记录数：${rows.length}；备注：${query.printRemark || '-'}`,
      columns: printColumnOptions.filter(item => selectedPrintColumns.value.includes(item.value)).map(item => ({ key: item.value, title: item.label })),
      rows
    })
  } catch (error: any) {
    message.error(error?.message || '打印失败')
  }
}

function onElderChange(value?: Id) {
  query.elderId = value
  syncRouteQuery()
}

function syncRouteQuery() {
  const nextQuery: Record<string, string> = {
    from: dayjs(query.from).format('YYYY-MM'),
    to: dayjs(query.to).format('YYYY-MM')
  }
  if (query.orgId) nextQuery.orgId = String(query.orgId)
  if (query.elderId) nextQuery.elderId = String(query.elderId)
  router.replace({ path: route.path, query: nextQuery }).catch(() => {})
}

function initFromRouteQuery() {
  const from = String(route.query.from || '')
  const to = String(route.query.to || '')
  if (dayjs(from).isValid()) query.from = dayjs(from)
  if (dayjs(to).isValid()) query.to = dayjs(to)
  query.orgId = normalizeId(route.query.orgId)
  query.elderId = normalizeId(route.query.elderId)
}

useLiveSyncRefresh({
  topics: ['finance', 'elder', 'lifecycle'],
  refresh: () => {
    loadData()
  },
  debounceMs: 900
})

onMounted(() => {
  initFromRouteQuery()
  loadData()
})
</script>
