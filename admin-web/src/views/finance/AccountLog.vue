<template>
  <PageContainer title="账户流水" subTitle="按老人查询流水记录">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="老人姓名">
        <a-input v-model:value="query.keyword" placeholder="输入姓名" allow-clear />
      </a-form-item>
    </SearchForm>

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
import { reactive, ref } from 'vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { getElderAccountLogPage } from '../../api/finance'
import type { ElderAccountLog, PageResult } from '../../types'
import { useRoute } from 'vue-router'

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

fetchData()
</script>
