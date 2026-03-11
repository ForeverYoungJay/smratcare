<template>
  <PageContainer title="账户流水" subTitle="按老人查询流水记录">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="老人姓名">
        <ElderNameAutocomplete v-model:value="query.keyword" placeholder="姓名(编号)" width="220px" />
      </a-form-item>
      <template #extra>
        <a-button @click="printCurrentElderLogs">打印当前流水</a-button>
      </template>
    </SearchForm>

    <a-row :gutter="[12, 12]" style="margin-bottom: 12px;">
      <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="流水笔数" :value="rows.length" /></a-card></a-col>
      <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="收入合计" :value="creditAmount" suffix="元" :precision="2" /></a-card></a-col>
      <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="支出合计" :value="debitAmount" suffix="元" :precision="2" /></a-card></a-col>
      <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="未识别充值方式" :value="unknownMethodCount" /></a-card></a-col>
      <a-col :span="24" v-if="unknownMethodCount > 0">
        <a-alert type="warning" show-icon message="存在未识别充值方式备注，建议规范备注格式：充值方式:xxx|充值时间:yyyy-MM-dd HH:mm:ss" />
      </a-col>
    </a-row>

    <DataTable rowKey="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'rechargeMethod'">
          {{ parseRechargeMethod(record.remark) || '-' }}
        </template>
        <template v-else-if="column.key === 'rechargeTime'">
          {{ parseRechargeTime(record.remark) || record.createTime || '-' }}
        </template>
      </template>
    </DataTable>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import ElderNameAutocomplete from '../../components/ElderNameAutocomplete.vue'
import { getElderAccountLogPage, printElderAccountLogPdf } from '../../api/finance'
import type { ElderAccountLog, PageResult } from '../../types'
import { useRoute } from 'vue-router'
import { message } from 'ant-design-vue'

const route = useRoute()
const loading = ref(false)
const rows = ref<ElderAccountLog[]>([])

const query = reactive({
  keyword: '',
  elderId: route.query.elderId ? Number(route.query.elderId) : undefined,
  pageNo: 1,
  pageSize: 10
})

const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const creditAmount = computed(() => rows.value
  .filter(item => String(item.direction || '').toUpperCase() === 'CREDIT')
  .reduce((sum, item) => sum + Number(item.amount || 0), 0))
const debitAmount = computed(() => rows.value
  .filter(item => String(item.direction || '').toUpperCase() === 'DEBIT')
  .reduce((sum, item) => sum + Number(item.amount || 0), 0))
const unknownMethodCount = computed(() => rows.value
  .filter(item => String(item.direction || '').toUpperCase() === 'CREDIT')
  .filter(item => !parseRechargeMethod(item.remark))
  .length)

const columns = [
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 140 },
  { title: '方向', dataIndex: 'direction', key: 'direction', width: 80 },
  { title: '金额', dataIndex: 'amount', key: 'amount', width: 120 },
  { title: '余额', dataIndex: 'balanceAfter', key: 'balanceAfter', width: 120 },
  { title: '来源', dataIndex: 'sourceType', key: 'sourceType', width: 120 },
  { title: '充值方式', key: 'rechargeMethod', width: 120 },
  { title: '充值时间', key: 'rechargeTime', width: 180 },
  { title: '时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '备注', dataIndex: 'remark', key: 'remark', width: 200 }
]

function parseRechargeMethod(remark?: string) {
  const match = String(remark || '').match(/充值方式:([^|]+)/)
  if (!match?.[1]) return ''
  const raw = match[1].trim().toUpperCase()
  if (raw === 'ALIPAY') return '支付宝'
  if (raw === 'WECHAT') return '微信'
  if (raw === 'QR_CODE') return '扫码'
  if (raw === 'CASH') return '现金'
  if (raw === 'BANK') return '转账'
  return raw
}

function parseRechargeTime(remark?: string) {
  const match = String(remark || '').match(/充值时间:([0-9\-: ]+)/)
  return match?.[1]?.trim() || ''
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<ElderAccountLog> = await getElderAccountLogPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      elderId: query.elderId,
      keyword: query.keyword
    })
    rows.value = res.list
    pagination.total = res.total || res.list.length
  } finally {
    loading.value = false
  }
}

function handleTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  fetchData()
}

function onReset() {
  query.keyword = ''
  query.elderId = undefined
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

async function printCurrentElderLogs() {
  if (!query.elderId) {
    message.warning('请先从老人账户进入流水页，或指定某个老人后再打印')
    return
  }
  const elderName = rows.value[0]?.elderName || '姓名待完善'
  const blob = await printElderAccountLogPdf({
    elderId: query.elderId,
    keyword: query.keyword || undefined
  })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `账户流水_${elderName}_${new Date().toISOString().slice(0, 10)}.pdf`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

fetchData()
</script>
