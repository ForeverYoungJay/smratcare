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
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="loadData">刷新</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="老人出入趋势">
      <div ref="trendRef" style="height: 320px;"></div>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="老人出入明细">
      <vxe-table border stripe show-overflow :data="rows" height="320">
        <vxe-column field="month" title="月份" width="140" />
        <vxe-column field="admissions" title="入住人数" width="120" />
        <vxe-column field="discharges" title="离院人数" width="120" />
      </vxe-table>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import dayjs, { type Dayjs } from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import { getOrgElderFlow } from '../../api/stats'
import type { MonthFlowItem } from '../../types'
import { useECharts } from '../../plugins/echarts'

const query = ref({
  from: dayjs().subtract(5, 'month') as Dayjs,
  to: dayjs() as Dayjs,
  orgId: undefined as number | undefined
})

const rows = ref<MonthFlowItem[]>([])
const { chartRef: trendRef, setOption } = useECharts()

async function loadData() {
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
}

function reset() {
  query.value.from = dayjs().subtract(5, 'month')
  query.value.to = dayjs()
  query.value.orgId = undefined
  loadData()
}

onMounted(loadData)
</script>
