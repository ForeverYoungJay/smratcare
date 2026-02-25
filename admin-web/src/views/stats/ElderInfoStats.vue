<template>
  <PageContainer title="老人信息统计" subTitle="老人结构、护理等级与状态分布">
    <a-card class="card-elevated" :bordered="false" style="margin-bottom: 16px;">
      <a-form layout="inline">
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

    <a-row :gutter="16">
      <a-col :span="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="老人总数" :value="stats.totalElders || 0" />
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="在院人数" :value="statusCount('在院')" />
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="离院人数" :value="statusCount('离院')" />
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" style="margin-top: 16px;">
      <a-col :span="12">
        <a-card class="card-elevated" :bordered="false" title="性别分布">
          <div ref="genderRef" style="height: 280px;"></div>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card class="card-elevated" :bordered="false" title="年龄分布">
          <div ref="ageRef" style="height: 280px;"></div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" style="margin-top: 16px;">
      <a-col :span="12">
        <a-card class="card-elevated" :bordered="false" title="护理等级分布">
          <div ref="careLevelRef" style="height: 280px;"></div>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card class="card-elevated" :bordered="false" title="状态分布">
          <div ref="statusRef" style="height: 280px;"></div>
        </a-card>
      </a-col>
    </a-row>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive } from 'vue'
import PageContainer from '../../components/PageContainer.vue'
import { getElderInfoStats } from '../../api/stats'
import type { ElderInfoStatsResponse, NameCountItem } from '../../types'
import { useECharts } from '../../plugins/echarts'

const stats = reactive<ElderInfoStatsResponse>({
  totalElders: 0,
  genderDistribution: [],
  ageDistribution: [],
  careLevelDistribution: [],
  statusDistribution: []
})
const query = reactive({
  orgId: undefined as number | undefined
})

const { chartRef: genderRef, setOption: setGenderOption } = useECharts()
const { chartRef: ageRef, setOption: setAgeOption } = useECharts()
const { chartRef: careLevelRef, setOption: setCareLevelOption } = useECharts()
const { chartRef: statusRef, setOption: setStatusOption } = useECharts()

const statusMap = computed(() =>
  (stats.statusDistribution || []).reduce<Record<string, number>>((acc, item) => {
    acc[item.name] = item.count
    return acc
  }, {})
)

function statusCount(name: string) {
  return statusMap.value[name] || 0
}

function toPieData(data: NameCountItem[]) {
  return (data || []).map(item => ({ name: item.name, value: item.count }))
}

function toBarOption(data: NameCountItem[], color: string) {
  return {
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: data.map(item => item.name) },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', itemStyle: { color }, data: data.map(item => item.count) }]
  }
}

async function loadData() {
  const data = await getElderInfoStats({ orgId: query.orgId })
  Object.assign(stats, data)
  setGenderOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{ type: 'pie', radius: ['36%', '66%'], data: toPieData(data.genderDistribution || []) }]
  })
  setAgeOption(toBarOption(data.ageDistribution || [], '#13c2c2'))
  setCareLevelOption(toBarOption(data.careLevelDistribution || [], '#1677ff'))
  setStatusOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{ type: 'pie', radius: '65%', data: toPieData(data.statusDistribution || []) }]
  })
}

function reset() {
  query.orgId = undefined
  loadData()
}

onMounted(loadData)
</script>
