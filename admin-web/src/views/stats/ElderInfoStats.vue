<template>
  <PageContainer title="老人信息统计" subTitle="老人结构、护理等级与状态分布">
    <a-card class="card-elevated" :bordered="false" style="margin-bottom: 16px;">
      <a-form layout="inline">
        <a-form-item label="机构ID">
          <a-input-number v-model:value="query.orgId" :min="1" placeholder="默认当前机构" style="width: 160px" />
        </a-form-item>
        <a-form-item label="打印维度">
          <a-select v-model:value="query.printSection" style="width: 150px">
            <a-select-option value="all">全部</a-select-option>
            <a-select-option value="gender">性别</a-select-option>
            <a-select-option value="age">年龄</a-select-option>
            <a-select-option value="care">护理等级</a-select-option>
            <a-select-option value="status">状态</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="对象筛选">
          <a-input v-model:value="query.keyword" allow-clear placeholder="例如：男/80-89岁/在院" style="width: 180px" />
        </a-form-item>
        <a-form-item label="打印备注">
          <a-input v-model:value="query.printRemark" allow-clear placeholder="例如：月度画像版" style="width: 180px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="loadData">刷新</a-button>
            <a-button @click="exportSummary">导出汇总</a-button>
            <a-button @click="openColumnSetting">列设置</a-button>
            <a-button @click="printCurrent">打印当前列</a-button>
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
          <a-statistic title="在院人数" :value="stats.inHospitalCount || 0" />
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="离院人数" :value="stats.dischargedCount || 0" />
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

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="画像明细">
      <vxe-table border stripe show-overflow :data="displayRows" height="300">
        <vxe-column field="section" title="维度" width="120" />
        <vxe-column field="name" title="项" min-width="180" />
        <vxe-column field="count" title="人数" width="120" />
      </vxe-table>
    </a-card>

    <a-modal v-model:open="columnSettingOpen" title="打印列设置" @ok="columnSettingOpen = false" cancel-text="关闭" ok-text="确定">
      <a-checkbox-group v-model:value="selectedPrintColumns" :options="printColumnOptions" />
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import PageContainer from '../../components/PageContainer.vue'
import { exportElderInfoStatsCsv, getElderInfoStats } from '../../api/stats'
import type { ElderInfoStatsResponse, NameCountItem } from '../../types'
import { useECharts } from '../../plugins/echarts'
import { message } from 'ant-design-vue'
import { printTableReport } from '../../utils/print'

const stats = reactive<ElderInfoStatsResponse>({
  totalElders: 0,
  inHospitalCount: 0,
  dischargedCount: 0,
  genderDistribution: [],
  ageDistribution: [],
  careLevelDistribution: [],
  statusDistribution: []
})
const query = reactive({
  orgId: undefined as number | undefined,
  printSection: 'all' as 'all' | 'gender' | 'age' | 'care' | 'status',
  keyword: '',
  printRemark: ''
})

const { chartRef: genderRef, setOption: setGenderOption } = useECharts()
const { chartRef: ageRef, setOption: setAgeOption } = useECharts()
const { chartRef: careLevelRef, setOption: setCareLevelOption } = useECharts()
const { chartRef: statusRef, setOption: setStatusOption } = useECharts()
const columnSettingOpen = ref(false)
const printColumnOptions = [
  { label: '维度', value: 'section' },
  { label: '项', value: 'name' },
  { label: '人数', value: 'count' }
]
const selectedPrintColumns = ref<string[]>(['section', 'name', 'count'])
const printableRows = computed(() => {
  const rows: Array<{ section: string; name: string; count: number }> = []
  for (const item of stats.genderDistribution || []) rows.push({ section: '性别', name: item.name, count: item.count })
  for (const item of stats.ageDistribution || []) rows.push({ section: '年龄', name: item.name, count: item.count })
  for (const item of stats.careLevelDistribution || []) rows.push({ section: '护理等级', name: item.name, count: item.count })
  for (const item of stats.statusDistribution || []) rows.push({ section: '状态', name: item.name, count: item.count })
  return rows
})
const displayRows = computed(() => {
  const section = query.printSection
  const keyword = String(query.keyword || '').trim()
  return printableRows.value.filter(item => {
    const sectionMatched = section === 'all'
      || (section === 'gender' && item.section === '性别')
      || (section === 'age' && item.section === '年龄')
      || (section === 'care' && item.section === '护理等级')
      || (section === 'status' && item.section === '状态')
    const keywordMatched = !keyword || item.name.includes(keyword)
    return sectionMatched && keywordMatched
  })
})

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
  try {
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
  } catch (error: any) {
    message.error(error?.message || '加载老人信息统计失败')
  }
}

function reset() {
  query.orgId = undefined
  query.printSection = 'all'
  query.keyword = ''
  query.printRemark = ''
  loadData()
}

function openColumnSetting() {
  columnSettingOpen.value = true
}

async function exportSummary() {
  try {
    await exportElderInfoStatsCsv({ orgId: query.orgId })
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
  try {
    const orgText = query.orgId ? `机构ID：${query.orgId}` : '机构：当前'
    printTableReport({
      title: '老人信息统计',
      subtitle: `${orgText}；维度：${query.printSection}；筛选：${query.keyword || '-'}；备注：${query.printRemark || '-'}`,
      columns: printColumnOptions.filter(item => selectedPrintColumns.value.includes(item.value)).map(item => ({ key: item.value, title: item.label })),
      rows: displayRows.value
    })
  } catch (error: any) {
    message.error(error?.message || '打印失败')
  }
}

onMounted(loadData)
</script>
