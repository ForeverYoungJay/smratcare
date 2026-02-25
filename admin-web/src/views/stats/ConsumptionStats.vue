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
      <vxe-table border stripe show-overflow :data="stats.topConsumerElders || []" height="320">
        <vxe-column field="elderName" title="老人姓名" min-width="180">
          <template #default="{ row }">
            {{ row.elderName || '未知老人' }}
          </template>
        </vxe-column>
        <vxe-column field="amount" title="消费金额" width="180" />
      </vxe-table>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import dayjs, { type Dayjs } from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import { getConsumptionStats } from '../../api/stats'
import type { ConsumptionStatsResponse } from '../../types'
import { useECharts } from '../../plugins/echarts'
import { message } from 'ant-design-vue'

const query = reactive({
  from: dayjs().subtract(5, 'month') as Dayjs,
  to: dayjs() as Dayjs,
  orgId: undefined as number | undefined
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
  } catch (error: any) {
    message.error(error?.message || '加载消费统计失败')
  }
}

function reset() {
  query.from = dayjs().subtract(5, 'month')
  query.to = dayjs()
  query.orgId = undefined
  loadData()
}

onMounted(loadData)
</script>
