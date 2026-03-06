<template>
  <PageContainer title="月运营收入统计" subTitle="按月份查看运营收入">
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
        <a-form-item label="打印备注">
          <a-input v-model:value="query.printRemark" allow-clear placeholder="例如：财务复核版" style="width: 200px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="loadData">刷新</a-button>
            <a-button @click="exportTrend">导出趋势</a-button>
            <a-button @click="printReport">打印报告</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-row :gutter="16" style="margin-top: 16px;">
      <a-col :span="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="期间总收入" :value="stats.totalRevenue || 0" :precision="2" />
          <a-button type="link" size="small" @click="openMetricDetail('total')">查看明细</a-button>
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="月均收入" :value="stats.averageMonthlyRevenue || 0" :precision="2" />
          <a-button type="link" size="small" @click="openMetricDetail('average')">查看明细</a-button>
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="最近环比(%)" :value="stats.revenueGrowthRate || 0" :precision="2" />
          <a-button type="link" size="small" @click="openMetricDetail('growth')">查看明细</a-button>
        </a-card>
      </a-col>
    </a-row>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="收入趋势">
      <div ref="lineRef" style="height: 320px;"></div>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="收入明细">
      <vxe-table border stripe show-overflow :data="stats.monthlyRevenue || []" height="320">
        <vxe-column field="month" title="月份" width="140" />
        <vxe-column field="amount" title="收入金额" width="180" />
      </vxe-table>
    </a-card>

    <a-modal v-model:open="metricOpen" :title="metricTitle" @ok="metricOpen = false" cancel-text="关闭" ok-text="确定" width="680">
      <vxe-table border stripe show-overflow :data="metricRows" height="360">
        <vxe-column field="month" title="月份" width="160" />
        <vxe-column field="amount" title="收入金额" width="180" />
        <vxe-column field="tag" title="标签" min-width="180" />
      </vxe-table>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs, { type Dayjs } from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import { exportMonthlyRevenueStatsCsv, getMonthlyRevenueStats } from '../../api/stats'
import type { MonthlyRevenueStatsResponse } from '../../types'
import { useECharts } from '../../plugins/echarts'
import { message } from 'ant-design-vue'
import { printTableReport } from '../../utils/print'

const query = reactive({
  from: dayjs().subtract(5, 'month') as Dayjs,
  to: dayjs() as Dayjs,
  orgId: undefined as number | undefined,
  printRemark: ''
})
const stats = reactive<MonthlyRevenueStatsResponse>({
  totalRevenue: 0,
  averageMonthlyRevenue: 0,
  revenueGrowthRate: 0,
  monthlyRevenue: []
})
const { chartRef: lineRef, setOption } = useECharts()
const metricOpen = ref(false)
const metricMode = ref<'total' | 'average' | 'growth'>('total')
const metricTitle = computed(() => {
  if (metricMode.value === 'average') return '月均收入明细'
  if (metricMode.value === 'growth') return '最近环比明细'
  return '期间总收入明细'
})
const metricRows = computed(() => {
  const list = (stats.monthlyRevenue || []).map(item => ({ month: item.month, amount: item.amount, tag: '' }))
  if (metricMode.value === 'average') {
    return list.map(item => ({ ...item, tag: Number(item.amount || 0) >= Number(stats.averageMonthlyRevenue || 0) ? '高于月均' : '低于月均' }))
  }
  if (metricMode.value === 'growth') {
    return list.map((item, index) => {
      if (index === 0) return { ...item, tag: '基准月' }
      const prev = Number(list[index - 1].amount || 0)
      const cur = Number(item.amount || 0)
      const growth = prev > 0 ? ((cur - prev) / prev) * 100 : 0
      return { ...item, tag: `环比 ${growth.toFixed(2)}%` }
    })
  }
  return list.map(item => ({ ...item, tag: '期间收入' }))
})

async function loadData() {
  if (query.from.isAfter(query.to, 'month')) {
    message.warning('起始月份不能晚于结束月份')
    return
  }
  try {
    const data = await getMonthlyRevenueStats({
      from: dayjs(query.from).format('YYYY-MM'),
      to: dayjs(query.to).format('YYYY-MM'),
      orgId: query.orgId
    })
    Object.assign(stats, data)
    setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: data.monthlyRevenue.map(item => item.month) },
      yAxis: { type: 'value' },
      series: [{ name: '收入', type: 'line', smooth: true, data: data.monthlyRevenue.map(item => item.amount) }]
    })
  } catch (error: any) {
    message.error(error?.message || '加载收入统计失败')
  }
}

function reset() {
  query.from = dayjs().subtract(5, 'month')
  query.to = dayjs()
  query.orgId = undefined
  query.printRemark = ''
  loadData()
}

async function exportTrend() {
  try {
    await exportMonthlyRevenueStatsCsv({
      from: dayjs(query.from).format('YYYY-MM'),
      to: dayjs(query.to).format('YYYY-MM'),
      orgId: query.orgId
    })
    message.success('导出成功')
  } catch (error: any) {
    message.error(error?.message || '导出失败')
  }
}

function openMetricDetail(mode: 'total' | 'average' | 'growth') {
  metricMode.value = mode
  metricOpen.value = true
}

function printReport() {
  try {
    const orgText = query.orgId ? `机构ID：${query.orgId}` : '机构：当前'
    printTableReport({
      title: '月运营收入统计',
      subtitle: `${dayjs(query.from).format('YYYY-MM')} ~ ${dayjs(query.to).format('YYYY-MM')}；${orgText}；备注：${query.printRemark || '-'}；期间总收入：${stats.totalRevenue || 0}；月均收入：${stats.averageMonthlyRevenue || 0}；最近环比：${stats.revenueGrowthRate || 0}%`,
      columns: [
        { key: 'month', title: '月份' },
        { key: 'amount', title: '收入金额' }
      ],
      rows: (stats.monthlyRevenue || []).map(item => ({
        month: item.month,
        amount: item.amount
      }))
    })
  } catch (error: any) {
    message.error(error?.message || '打印失败')
  }
}

onMounted(loadData)
</script>
