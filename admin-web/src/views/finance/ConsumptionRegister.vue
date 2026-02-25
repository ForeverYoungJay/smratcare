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
        <a-input v-model:value="query.category" placeholder="如 餐饮/医护/商城" allow-clear />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openCreate">新增消费</a-button>
      </template>
    </SearchForm>

    <DataTable rowKey="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" @change="handleTableChange" />

    <a-modal v-model:open="createOpen" title="新增消费登记" :confirm-loading="creating" @ok="submitCreate">
      <a-form layout="vertical">
        <a-form-item label="老人" required>
          <a-select v-model:value="createForm.elderId" show-search :filter-option="false" :options="elderOptions" @search="searchElders" />
        </a-form-item>
        <a-form-item label="消费日期" required>
          <a-date-picker v-model:value="createForm.consumeDate" style="width: 100%" />
        </a-form-item>
        <a-form-item label="金额" required>
          <a-input-number v-model:value="createForm.amount" :min="0.01" :precision="2" style="width: 100%" />
        </a-form-item>
        <a-form-item label="类别">
          <a-input v-model:value="createForm.category" />
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
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { getElderPage } from '../../api/elder'
import { createConsumption, getConsumptionPage } from '../../api/financeFee'
import type { ConsumptionRecordItem, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<ConsumptionRecordItem[]>([])
const query = reactive({ pageNo: 1, pageSize: 10, from: undefined as any, to: undefined as any, category: '' })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 140 },
  { title: '消费日期', dataIndex: 'consumeDate', key: 'consumeDate', width: 120 },
  { title: '金额', dataIndex: 'amount', key: 'amount', width: 120 },
  { title: '类别', dataIndex: 'category', key: 'category', width: 120 },
  { title: '来源类型', dataIndex: 'sourceType', key: 'sourceType', width: 120 },
  { title: '备注', dataIndex: 'remark', key: 'remark' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 }
]

const createOpen = ref(false)
const creating = ref(false)
const elderOptions = ref<{ label: string; value: number }[]>([])
const sourceTypeOptions = [
  { label: '手工', value: 'MANUAL' },
  { label: '商城', value: 'STORE' },
  { label: '医护', value: 'MEDICAL' }
]
const createForm = reactive({
  elderId: undefined as number | undefined,
  consumeDate: undefined as any,
  amount: 0,
  category: '',
  sourceType: 'MANUAL',
  remark: ''
})

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<ConsumptionRecordItem> = await getConsumptionPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      from: query.from ? dayjs(query.from).format('YYYY-MM-DD') : undefined,
      to: query.to ? dayjs(query.to).format('YYYY-MM-DD') : undefined,
      category: query.category || undefined
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
  pagination.current = 1
  fetchData()
}

function handleTableChange(pag: any) {
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

function openCreate() {
  createForm.elderId = undefined
  createForm.consumeDate = dayjs()
  createForm.amount = 0
  createForm.category = ''
  createForm.sourceType = 'MANUAL'
  createForm.remark = ''
  createOpen.value = true
}

async function searchElders(keyword: string) {
  if (!keyword) {
    elderOptions.value = []
    return
  }
  const res = await getElderPage({ pageNo: 1, pageSize: 20, keyword })
  elderOptions.value = res.list.map((item: any) => ({ label: item.fullName || item.name || '未知老人', value: item.id }))
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

fetchData()
</script>
