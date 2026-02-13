<template>
  <PageContainer title="报表图表" subTitle="ECharts 示例">
    <a-row :gutter="16">
      <a-col :xs="24" :lg="12">
        <a-card class="card-elevated" :bordered="false">
          <div ref="lineRef" style="height: 300px;"></div>
        </a-card>
      </a-col>
      <a-col :xs="24" :lg="12">
        <a-card class="card-elevated" :bordered="false">
          <div ref="pieRef" style="height: 300px;"></div>
        </a-card>
      </a-col>
    </a-row>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import PageContainer from '../../components/PageContainer.vue'
import { useECharts } from '../../plugins/echarts'

const { chartRef: lineRef, setOption: setLine } = useECharts()
const { chartRef: pieRef, setOption: setPie } = useECharts()

onMounted(() => {
  setLine({
    xAxis: { type: 'category', data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri'] },
    yAxis: { type: 'value' },
    series: [{ data: [120, 200, 150, 80, 70], type: 'line', smooth: true }]
  })
  setPie({
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        data: [
          { value: 40, name: '护理' },
          { value: 20, name: '餐饮' },
          { value: 30, name: '床位' }
        ]
      }
    ]
  })
})
</script>
