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
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import dayjs, { type Dayjs } from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import { getCheckInStats } from '../../api/stats'
import type { CheckInStatsResponse } from '../../types'
import { useECharts } from '../../plugins/echarts'
import { message } from 'ant-design-vue'

const query = reactive({
  from: dayjs().subtract(5, 'month') as Dayjs,
  to: dayjs() as Dayjs,
  orgId: undefined as number | undefined
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
  } catch (error: any) {
    message.error(error?.message || '加载入住统计失败')
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
