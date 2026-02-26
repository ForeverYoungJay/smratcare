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
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" placeholder="老人/类别/备注" allow-clear />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openCreate">新增消费</a-button>
      </template>
    </SearchForm>

    <DataTable rowKey="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" @change="handleTableChange" />

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
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { useElderOptions } from '../../composables/useElderOptions'
import { createConsumption, getConsumptionPage } from '../../api/financeFee'
import type { ConsumptionRecordItem, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<ConsumptionRecordItem[]>([])
const query = reactive({ pageNo: 1, pageSize: 10, from: undefined as any, to: undefined as any, category: '', keyword: '' })
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
const { elderOptions, searchElders: searchElderOptions } = useElderOptions({ pageSize: 20 })
const sourceTypeOptions = [
  { label: '手工', value: 'MANUAL' },
  { label: '商城', value: 'STORE' },
  { label: '医护', value: 'MEDICAL' }
]
const categoryOptions = [
  { label: '餐饮消费', value: 'DINING' },
  { label: '床位消费', value: 'BED' },
  { label: '护理消费', value: 'NURSING' },
  { label: '押金消费', value: 'DEPOSIT' },
  { label: '药品消费', value: 'MEDICINE' },
  { label: '其他消费', value: 'OTHER' }
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
  if (query.from && query.to && dayjs(query.from).isAfter(dayjs(query.to), 'day')) {
    message.error('开始日期不能晚于结束日期')
    return
  }
  loading.value = true
  try {
    const res: PageResult<ConsumptionRecordItem> = await getConsumptionPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
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
  query.keyword = ''
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

fetchData()
</script>
