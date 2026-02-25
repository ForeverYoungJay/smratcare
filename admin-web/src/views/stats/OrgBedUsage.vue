<template>
  <PageContainer title="床位使用统计" subTitle="机构床位使用概况">
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
      <a-col :span="4">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="总床位" :value="stats.totalBeds || 0" />
        </a-card>
      </a-col>
      <a-col :span="4">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="已使用床位" :value="stats.occupiedBeds || 0" />
        </a-card>
      </a-col>
      <a-col :span="4">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="空闲床位" :value="stats.availableBeds || 0" />
        </a-card>
      </a-col>
      <a-col :span="4">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="维护床位" :value="stats.maintenanceBeds || 0" />
        </a-card>
      </a-col>
      <a-col :span="4">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="使用率(%)" :value="stats.occupancyRate || 0" :precision="2" />
        </a-card>
      </a-col>
      <a-col :span="4">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="空闲率(%)" :value="stats.availableRate || 0" :precision="2" />
        </a-card>
      </a-col>
    </a-row>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="床位状态分布">
      <div ref="pieRef" style="height: 360px;"></div>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import PageContainer from '../../components/PageContainer.vue'
import { getOrgBedUsage } from '../../api/stats'
import type { BedUsageStatsResponse } from '../../types'
import { useECharts } from '../../plugins/echarts'
import { message } from 'ant-design-vue'

const stats = reactive<BedUsageStatsResponse>({
  totalBeds: 0,
  occupiedBeds: 0,
  availableBeds: 0,
  maintenanceBeds: 0,
  occupancyRate: 0,
  maintenanceRate: 0,
  availableRate: 0
})
const query = reactive({
  orgId: undefined as number | undefined
})
const { chartRef: pieRef, setOption } = useECharts()

async function loadData() {
  try {
    const data = await getOrgBedUsage({ orgId: query.orgId })
    Object.assign(stats, data)
    setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: 0 },
      series: [
        {
          type: 'pie',
          radius: ['36%', '66%'],
          data: [
            { name: '已使用', value: data.occupiedBeds || 0 },
            { name: '空闲', value: data.availableBeds || 0 },
            { name: '维护', value: data.maintenanceBeds || 0 }
          ]
        }
      ]
    })
  } catch (error: any) {
    message.error(error?.message || '加载床位统计失败')
  }
}

function reset() {
  query.orgId = undefined
  loadData()
}

onMounted(loadData)
</script>
