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
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="loadData">刷新</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <a-statistic title="期间总收入" :value="stats.totalRevenue || 0" :precision="2" />
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="收入趋势">
      <div ref="lineRef" style="height: 320px;"></div>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="收入明细">
      <vxe-table border stripe show-overflow :data="stats.monthlyRevenue || []" height="320">
        <vxe-column field="month" title="月份" width="140" />
        <vxe-column field="amount" title="收入金额" width="180" />
      </vxe-table>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import dayjs, { type Dayjs } from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import { getMonthlyRevenueStats } from '../../api/stats'
import type { MonthlyRevenueStatsResponse } from '../../types'
import { useECharts } from '../../plugins/echarts'

const query = reactive({
  from: dayjs().subtract(5, 'month') as Dayjs,
  to: dayjs() as Dayjs,
  orgId: undefined as number | undefined
})
const stats = reactive<MonthlyRevenueStatsResponse>({
  totalRevenue: 0,
  monthlyRevenue: []
})
const { chartRef: lineRef, setOption } = useECharts()

async function loadData() {
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
}

function reset() {
  query.from = dayjs().subtract(5, 'month')
  query.to = dayjs()
  query.orgId = undefined
  loadData()
}

onMounted(loadData)
</script>
