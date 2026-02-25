<template>
  <PageContainer title="机构月运营详情" subTitle="机构月度运营与床位利用情况">
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
            <a-button @click="exportCsvReport">导出报表</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="机构月运营趋势">
      <div ref="trendRef" style="height: 320px;"></div>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="机构月运营明细">
      <vxe-table border stripe show-overflow :data="rows" height="320">
        <vxe-column field="month" title="月份" width="120" />
        <vxe-column field="admissions" title="入住" width="100" />
        <vxe-column field="discharges" title="离院" width="100" />
        <vxe-column field="revenue" title="营收" width="150" />
        <vxe-column field="occupiedBeds" title="已使用床位" width="120" />
        <vxe-column field="totalBeds" title="总床位" width="100" />
        <vxe-column field="occupancyRate" title="床位使用率(%)" width="120" />
      </vxe-table>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import dayjs, { type Dayjs } from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import { exportOrgMonthlyOperationCsv, getOrgMonthlyOperation } from '../../api/stats'
import type { OrgMonthlyOperationItem } from '../../types'
import { useECharts } from '../../plugins/echarts'
import { message } from 'ant-design-vue'

const query = ref({
  from: dayjs().subtract(5, 'month') as Dayjs,
  to: dayjs() as Dayjs,
  orgId: undefined as number | undefined
})
const rows = ref<OrgMonthlyOperationItem[]>([])
const { chartRef: trendRef, setOption } = useECharts()

async function loadData() {
  rows.value = await getOrgMonthlyOperation({
    from: dayjs(query.value.from).format('YYYY-MM'),
    to: dayjs(query.value.to).format('YYYY-MM'),
    orgId: query.value.orgId
  })
  setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['入住', '离院', '营收'] },
    xAxis: { type: 'category', data: rows.value.map(item => item.month) },
    yAxis: [{ type: 'value' }, { type: 'value' }],
    series: [
      { name: '入住', type: 'bar', data: rows.value.map(item => item.admissions) },
      { name: '离院', type: 'bar', data: rows.value.map(item => item.discharges) },
      { name: '营收', type: 'line', yAxisIndex: 1, smooth: true, data: rows.value.map(item => item.revenue) }
    ]
  })
}

async function exportCsvReport() {
  try {
    await exportOrgMonthlyOperationCsv({
      from: dayjs(query.value.from).format('YYYY-MM'),
      to: dayjs(query.value.to).format('YYYY-MM'),
      orgId: query.value.orgId
    })
    message.success('报表导出成功')
  } catch (error: any) {
    message.error(error?.message || '报表导出失败')
  }
}

function reset() {
  query.value.from = dayjs().subtract(5, 'month')
  query.value.to = dayjs()
  query.value.orgId = undefined
  loadData()
}

onMounted(loadData)
</script>
