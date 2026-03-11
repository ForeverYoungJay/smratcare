<template>
  <PageContainer title="消费登记" subTitle="手工登记消费流水，支持按日期筛选">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="开始日期">
        <a-date-picker v-model:value="query.from" />
      </a-form-item>
      <a-form-item label="结束日期">
        <a-date-picker v-model:value="query.to" />
      </a-form-item>
      <a-form-item label="类别">
        <a-select v-model:value="query.category" :options="categoryOptions" style="width: 180px" allow-clear />
      </a-form-item>
      <a-form-item label="风险等级">
        <a-select v-model:value="query.riskLevel" allow-clear style="width: 140px">
          <a-select-option value="HIGH">高风险</a-select-option>
          <a-select-option value="MEDIUM">中风险</a-select-option>
          <a-select-option value="LOW">低风险</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="老人">
        <ElderNameAutocomplete v-model:value="query.keyword" placeholder="老人姓名(编号)" width="220px" />
      </a-form-item>
      <template #extra>
        <a-button @click="quickFilterHighRisk">仅看高风险</a-button>
        <a-button @click="exportData">导出</a-button>
        <a-button @click="printCurrent">打印当前结果</a-button>
        <a-button type="primary" @click="openCreate">新增消费</a-button>
      </template>
    </SearchForm>

    <a-row :gutter="[12, 12]" style="margin-bottom: 12px;">
      <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="流水数" :value="filteredRows.length" /></a-card></a-col>
      <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="消费总额" :value="totalAmount" suffix="元" :precision="2" /></a-card></a-col>
      <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="高风险数" :value="highRiskCount" /></a-card></a-col>
      <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="来源缺失" :value="missingSourceCount" /></a-card></a-col>
      <a-col :span="24" v-if="missingSourceCount > 0">
        <a-alert type="warning" show-icon :message="`存在 ${missingSourceCount} 条来源缺失流水，建议补齐 sourceType / sourceId`" />
      </a-col>
    </a-row>

    <DataTable rowKey="id" :columns="columns" :data-source="filteredRows" :loading="loading" :pagination="pagination" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'riskLevel'">
          <a-tag :color="riskColor(record)">{{ riskLevel(record) }}</a-tag>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="createOpen" title="新增消费登记" :confirm-loading="creating" @ok="submitCreate">
      <a-form layout="vertical">
        <a-form-item label="老人" required>
          <a-select v-model:value="createForm.elderId" show-search :filter-option="false" :options="elderOptions" @search="searchElders" @focus="() => !elderOptions.length && searchElders('')" />
        </a-form-item>
        <a-form-item label="消费日期" required>
          <a-date-picker v-model:value="createForm.consumeDate" style="width: 100%" />
        </a-form-item>
        <a-form-item label="金额" required>
          <a-input-number v-model:value="createForm.amount" :min="0.01" :precision="2" style="width: 100%" />
        </a-form-item>
        <a-form-item label="类别">
          <a-select v-model:value="createForm.category" :options="categoryOptions" allow-clear />
        </a-form-item>
        <a-form-item label="来源类型">
          <a-select v-model:value="createForm.sourceType" allow-clear :options="sourceTypeOptions" />
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="createForm.remark" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { useRoute } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import ElderNameAutocomplete from '../../components/ElderNameAutocomplete.vue'
import { useElderOptions } from '../../composables/useElderOptions'
import { createConsumption, getConsumptionPage } from '../../api/financeFee'
import type { ConsumptionRecordItem, Id, PageResult } from '../../types'
import { exportCsv } from '../../utils/export'
import { normalizeResidentId } from '../../utils/id'

const loading = ref(false)
const rows = ref<ConsumptionRecordItem[]>([])
const route = useRoute()
const query = reactive({
  pageNo: 1,
  pageSize: 10,
  from: undefined as any,
  to: undefined as any,
  category: '',
  riskLevel: undefined as 'HIGH' | 'MEDIUM' | 'LOW' | undefined,
  keyword: '',
  elderId: normalizeResidentId(route.query as Record<string, unknown>) as Id | undefined
})
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 140 },
  { title: '消费日期', dataIndex: 'consumeDate', key: 'consumeDate', width: 120 },
  { title: '金额', dataIndex: 'amount', key: 'amount', width: 120 },
  { title: '类别', dataIndex: 'category', key: 'category', width: 120 },
  { title: '来源类型', dataIndex: 'sourceType', key: 'sourceType', width: 120 },
  { title: '风险等级', key: 'riskLevel', width: 100 },
  { title: '备注', dataIndex: 'remark', key: 'remark' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 }
]
const filteredRows = computed(() => (rows.value || [])
  .filter(item => !query.riskLevel || riskLevel(item) === (query.riskLevel === 'HIGH' ? '高' : query.riskLevel === 'MEDIUM' ? '中' : '低')))
const totalAmount = computed(() => filteredRows.value.reduce((sum, item) => sum + Number(item.amount || 0), 0))
const highRiskCount = computed(() => filteredRows.value.filter(item => riskLevel(item) === '高').length)
const missingSourceCount = computed(() => filteredRows.value.filter(item => !item.sourceType).length)

const createOpen = ref(false)
const creating = ref(false)
const { elderOptions, searchElders: searchElderOptions } = useElderOptions({ pageSize: 20 })
const sourceTypeOptions = [
  { label: '手工', value: 'MANUAL' },
  { label: '商城', value: 'STORE' },
  { label: '医护', value: 'MEDICAL' },
  { label: '账单收款', value: 'BILL_PAYMENT' }
]
const categoryOptions = [
  { label: '餐饮消费', value: 'DINING' },
  { label: '床位消费', value: 'BED' },
  { label: '护理消费', value: 'NURSING' },
  { label: '押金消费', value: 'DEPOSIT' },
  { label: '药品消费', value: 'MEDICINE' },
  { label: '账单收款', value: 'BILL_PAYMENT' },
  { label: '其他消费', value: 'OTHER' }
]
const createForm = reactive({
  elderId: undefined as Id | undefined,
  consumeDate: undefined as any,
  amount: 0,
  category: '',
  sourceType: 'MANUAL',
  remark: ''
})

async function fetchData() {
  if (query.from && query.to && dayjs(query.from).isAfter(dayjs(query.to), 'day')) {
    message.error('开始日期不能晚于结束日期')
    return
  }
  loading.value = true
  try {
    const res: PageResult<ConsumptionRecordItem> = await getConsumptionPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      elderId: query.elderId,
      from: query.from ? dayjs(query.from).format('YYYY-MM-DD') : undefined,
      to: query.to ? dayjs(query.to).format('YYYY-MM-DD') : undefined,
      category: query.category || undefined,
      keyword: query.keyword || undefined
    })
    rows.value = res.list
    pagination.total = res.total || res.list.length
  } finally {
    loading.value = false
  }
}

function onReset() {
  query.pageNo = 1
  query.from = undefined
  query.to = undefined
  query.category = ''
  query.riskLevel = undefined
  query.keyword = ''
  query.elderId = undefined
  pagination.current = 1
  fetchData()
}

function quickFilterHighRisk() {
  query.riskLevel = 'HIGH'
}

function handleTableChange(pag: any) {
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

function openCreate() {
  createForm.elderId = query.elderId
  createForm.consumeDate = dayjs()
  createForm.amount = 0
  createForm.category = 'OTHER'
  createForm.sourceType = 'MANUAL'
  createForm.remark = ''
  createOpen.value = true
}

async function searchElders(keyword: string) {
  await searchElderOptions(keyword)
}

async function submitCreate() {
  if (!createForm.elderId) {
    message.error('请选择老人')
    return
  }
  if (!createForm.consumeDate) {
    message.error('请选择日期')
    return
  }
  if (!createForm.amount || createForm.amount <= 0) {
    message.error('请输入有效金额')
    return
  }
  creating.value = true
  try {
    await createConsumption({
      elderId: createForm.elderId,
      consumeDate: dayjs(createForm.consumeDate).format('YYYY-MM-DD'),
      amount: createForm.amount,
      category: createForm.category || undefined,
      sourceType: createForm.sourceType || undefined,
      remark: createForm.remark || undefined
    })
    message.success('新增成功')
    createOpen.value = false
    fetchData()
  } finally {
    creating.value = false
  }
}

function exportData() {
  exportCsv(
    filteredRows.value.map(item => ({
      老人: item.elderName || '',
      消费日期: item.consumeDate || '',
      金额: item.amount || 0,
      类别: item.category || '',
      来源类型: item.sourceType || '',
      风险等级: riskLevel(item),
      备注: item.remark || '',
      创建时间: item.createTime || ''
    })),
    `消费明细-${dayjs().format('YYYYMMDD-HHmmss')}.csv`
  )
}

function printCurrent() {
  const table = filteredRows.value.map(item => ({
    老人: item.elderName || '',
    消费日期: item.consumeDate || '',
    金额: item.amount || 0,
    类别: item.category || '',
    来源类型: item.sourceType || '',
    风险等级: riskLevel(item),
    备注: item.remark || ''
  }))
  const html = `
    <html>
      <head><meta charset="utf-8"><title>消费明细打印</title></head>
      <body>
        <h3>消费明细（${dayjs().format('YYYY-MM-DD HH:mm')}）</h3>
        <table border="1" cellspacing="0" cellpadding="6">
          <thead><tr><th>老人</th><th>消费日期</th><th>金额</th><th>类别</th><th>来源类型</th><th>风险等级</th><th>备注</th></tr></thead>
          <tbody>
            ${table.map(item => `<tr><td>${item.老人}</td><td>${item.消费日期}</td><td>${item.金额}</td><td>${item.类别}</td><td>${item.来源类型}</td><td>${item.风险等级}</td><td>${item.备注}</td></tr>`).join('')}
          </tbody>
        </table>
      </body>
    </html>
  `
  const win = window.open('', '_blank')
  if (!win) {
    message.error('请允许弹窗后重试打印')
    return
  }
  win.document.write(html)
  win.document.close()
  win.focus()
  win.print()
}

fetchData()

function riskLevel(item: ConsumptionRecordItem) {
  const amount = Number(item.amount || 0)
  const sourceType = String(item.sourceType || '').toUpperCase()
  if (!sourceType) return '高'
  if (sourceType === 'MANUAL' && amount >= 1000) return '高'
  if (amount >= 300) return '中'
  return '低'
}

function riskColor(item: ConsumptionRecordItem) {
  const level = riskLevel(item)
  if (level === '高') return 'red'
  if (level === '中') return 'orange'
  return 'green'
}
</script>
