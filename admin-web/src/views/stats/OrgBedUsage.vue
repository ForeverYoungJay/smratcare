<template>
  <PageContainer title="床位使用统计" subTitle="机构床位使用概况">
    <a-card class="card-elevated" :bordered="false" style="margin-bottom: 16px;">
      <a-form layout="inline">
        <a-form-item label="机构ID">
          <a-input-number v-model:value="query.orgId" :min="1" placeholder="默认当前机构" style="width: 160px" />
        </a-form-item>
        <a-form-item label="打印对象">
          <a-select v-model:value="query.targetType" style="width: 140px">
            <a-select-option value="ALL">全部</a-select-option>
            <a-select-option value="OCCUPIED">已使用</a-select-option>
            <a-select-option value="AVAILABLE">空闲</a-select-option>
            <a-select-option value="MAINTENANCE">维护</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="打印备注">
          <a-input v-model:value="query.printRemark" allow-clear placeholder="例如：床态巡检版" style="width: 200px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="loadData">刷新</a-button>
            <a-button @click="exportSummary">导出汇总</a-button>
            <a-button @click="openColumnSetting">列设置</a-button>
            <a-button @click="printCurrent">打印</a-button>
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

    <a-modal v-model:open="columnSettingOpen" title="打印列设置" @ok="columnSettingOpen = false" cancel-text="关闭" ok-text="确定">
      <a-checkbox-group v-model:value="selectedPrintColumns" :options="printColumnOptions" />
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import PageContainer from '../../components/PageContainer.vue'
import { exportOrgBedUsageCsv, getOrgBedUsage } from '../../api/stats'
import type { BedUsageStatsResponse } from '../../types'
import { useECharts } from '../../plugins/echarts'
import { message } from 'ant-design-vue'
import { printTableReport } from '../../utils/print'

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
  orgId: undefined as number | undefined,
  targetType: 'ALL' as 'ALL' | 'OCCUPIED' | 'AVAILABLE' | 'MAINTENANCE',
  printRemark: ''
})
const { chartRef: pieRef, setOption } = useECharts()
const columnSettingOpen = ref(false)
const printColumnOptions = [
  { label: '总床位', value: 'totalBeds' },
  { label: '已使用床位', value: 'occupiedBeds' },
  { label: '空闲床位', value: 'availableBeds' },
  { label: '维护床位', value: 'maintenanceBeds' },
  { label: '使用率(%)', value: 'occupancyRate' },
  { label: '空闲率(%)', value: 'availableRate' }
]
const selectedPrintColumns = ref<string[]>(['totalBeds', 'occupiedBeds', 'availableBeds', 'maintenanceBeds', 'occupancyRate', 'availableRate'])

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
  query.targetType = 'ALL'
  query.printRemark = ''
  loadData()
}

function openColumnSetting() {
  columnSettingOpen.value = true
}

async function exportSummary() {
  try {
    await exportOrgBedUsageCsv({ orgId: query.orgId })
    message.success('导出成功')
  } catch (error: any) {
    message.error(error?.message || '导出失败')
  }
}

function printCurrent() {
  if (!selectedPrintColumns.value.length) {
    message.warning('请至少选择一列打印')
    return
  }
  const source = {
    totalBeds: stats.totalBeds || 0,
    occupiedBeds: stats.occupiedBeds || 0,
    availableBeds: stats.availableBeds || 0,
    maintenanceBeds: stats.maintenanceBeds || 0,
    occupancyRate: `${Number(stats.occupancyRate || 0).toFixed(2)}%`,
    availableRate: `${Number(stats.availableRate || 0).toFixed(2)}%`
  }
  const targetRows: Array<Record<string, any>> = []
  if (query.targetType === 'ALL') {
    targetRows.push(source)
  } else if (query.targetType === 'OCCUPIED') {
    targetRows.push({ ...source, totalBeds: source.occupiedBeds, availableBeds: '-', maintenanceBeds: '-', occupancyRate: source.occupancyRate, availableRate: '-' })
  } else if (query.targetType === 'AVAILABLE') {
    targetRows.push({ ...source, totalBeds: source.availableBeds, occupiedBeds: '-', maintenanceBeds: '-', occupancyRate: '-', availableRate: source.availableRate })
  } else {
    targetRows.push({ ...source, totalBeds: source.maintenanceBeds, occupiedBeds: '-', availableBeds: '-', occupancyRate: '-', availableRate: '-' })
  }
  try {
    const orgText = query.orgId ? `机构ID：${query.orgId}` : '机构：当前'
    const targetLabelMap: Record<string, string> = {
      ALL: '全部',
      OCCUPIED: '已使用',
      AVAILABLE: '空闲',
      MAINTENANCE: '维护'
    }
    printTableReport({
      title: '床位使用统计',
      subtitle: `${orgText}；对象：${targetLabelMap[query.targetType]}；备注：${query.printRemark || '-'}`,
      columns: printColumnOptions.filter(item => selectedPrintColumns.value.includes(item.value)).map(item => ({ key: item.value, title: item.label })),
      rows: targetRows
    })
  } catch (error: any) {
    message.error(error?.message || '打印失败')
  }
}

onMounted(loadData)
</script>
