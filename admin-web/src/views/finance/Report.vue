<template>
  <PageContainer title="财务报表" subTitle="营收趋势、欠费排行与商城销售">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline" :model="query" class="search-form">
        <a-form-item label="起始月份">
          <a-date-picker v-model:value="query.from" picker="month" style="width: 160px" />
        </a-form-item>
        <a-form-item label="结束月份">
          <a-date-picker v-model:value="query.to" picker="month" style="width: 160px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="loadCharts">刷新</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-row :gutter="16" style="margin-top: 16px;">
      <a-col :span="12">
        <a-card class="card-elevated" :bordered="false" title="月营收趋势">
          <div ref="revenueRef" style="height: 280px;"></div>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card class="card-elevated" :bordered="false" title="商城销售统计">
          <div ref="storeRef" style="height: 280px;"></div>
        </a-card>
      </a-col>
    </a-row>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="欠费金额排行">
      <vxe-table border stripe show-overflow :data="arrears" height="260">
        <vxe-column field="elderName" title="老人" min-width="160">
          <template #default="{ row }">
            <span>{{ row.elderName || '未知老人' }}</span>
          </template>
        </vxe-column>
        <vxe-column field="outstandingAmount" title="欠费金额" width="140" />
      </vxe-table>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import { getFinanceArrearsTop, getFinanceMonthlyRevenue, getFinanceStoreSales } from '../../api/finance'
import type { FinanceArrearsItem, FinanceReportMonthlyItem, FinanceStoreSalesItem } from '../../types'
import { useECharts } from '../../plugins/echarts'

const query = ref({
  from: dayjs().subtract(5, 'month'),
  to: dayjs()
})

const arrears = ref<FinanceArrearsItem[]>([])

const { chartRef: revenueRef, setOption: setRevenueOption } = useECharts()
const { chartRef: storeRef, setOption: setStoreOption } = useECharts()

async function loadCharts() {
  const params = {
    from: dayjs(query.value.from).format('YYYY-MM'),
    to: dayjs(query.value.to).format('YYYY-MM')
  }
  const revenue: FinanceReportMonthlyItem[] = await getFinanceMonthlyRevenue(params)
  const storeSales: FinanceStoreSalesItem[] = await getFinanceStoreSales(params)
  arrears.value = await getFinanceArrearsTop({ limit: 10 })

  setRevenueOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: revenue.map(item => item.month) },
    yAxis: { type: 'value' },
    series: [{ name: '营收', type: 'line', data: revenue.map(item => item.amount) }]
  })

  setStoreOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: storeSales.map(item => item.month) },
    yAxis: { type: 'value' },
    series: [{ name: '销售额', type: 'bar', data: storeSales.map(item => item.amount) }]
  })
}

function reset() {
  query.value.from = dayjs().subtract(5, 'month')
  query.value.to = dayjs()
  loadCharts()
}

onMounted(loadCharts)
</script>
