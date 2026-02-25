<template>
  <PageContainer title="充值记录" subTitle="一卡通充值与流水">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="老人姓名" allow-clear />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openRecharge">发起充值</a-button>
      </template>
    </SearchForm>

    <DataTable
      rowKey="id"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
    />

    <a-modal v-model:open="open" title="一卡通充值" @ok="submit" :confirm-loading="saving" width="560px">
      <a-form layout="vertical">
        <a-form-item label="卡账户" required>
          <a-select
            v-model:value="form.cardAccountId"
            show-search
            :options="cardOptions"
            placeholder="请选择卡"
            option-filter-prop="label"
          />
        </a-form-item>
        <a-form-item label="充值金额" required>
          <a-input-number v-model:value="form.amount" :min="0.01" :precision="2" style="width: 100%" />
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="form.remark" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { getCardAccountPage, getCardRechargePage, rechargeCard } from '../../api/card'
import type { CardTradeLog, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<CardTradeLog[]>([])
const cardOptions = ref<any[]>([])
const query = reactive({ keyword: '', pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const columns = [
  { title: '时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '卡号', dataIndex: 'cardNo', key: 'cardNo', width: 180 },
  { title: '充值金额', dataIndex: 'amount', key: 'amount', width: 120 },
  { title: '余额', dataIndex: 'balanceAfter', key: 'balanceAfter', width: 120 },
  { title: '备注', dataIndex: 'remark', key: 'remark' }
]

const open = ref(false)
const saving = ref(false)
const form = reactive({
  cardAccountId: undefined as number | undefined,
  amount: undefined as number | undefined,
  remark: ''
})

async function loadCards() {
  const res = await getCardAccountPage({ pageNo: 1, pageSize: 200, status: 'ACTIVE' })
  cardOptions.value = res.list.map((item: any) => ({
    label: `${item.cardNo} - ${item.elderName || item.elderId}`,
    value: item.id
  }))
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<CardTradeLog> = await getCardRechargePage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined
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
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openRecharge() {
  form.cardAccountId = undefined
  form.amount = undefined
  form.remark = ''
  open.value = true
}

async function submit() {
  saving.value = true
  try {
    await rechargeCard({
      cardAccountId: Number(form.cardAccountId),
      amount: Number(form.amount),
      remark: form.remark
    })
    open.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

loadCards()
fetchData()
</script>
