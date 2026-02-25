<template>
  <PageContainer title="送餐记录" subTitle="记录送餐结果与送达状态">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="日期范围">
        <a-range-picker v-model:value="query.range" />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" :options="statusOptions" allow-clear style="width: 140px" />
      </a-form-item>
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="订单号/区域/送餐人" allow-clear />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openCreate">新增送餐记录</a-button>
      </template>
    </SearchForm>

    <DataTable rowKey="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="getDiningDeliveryStatusColor(record.status)">
            {{ statusLabel(record.status) }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openEdit(record)">编辑</a-button>
            <a-button type="link" danger @click="remove(record)">删除</a-button>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="editOpen" title="送餐记录" :confirm-loading="saving" @ok="submit">
      <a-form layout="vertical">
        <a-form-item label="点餐单" required>
          <a-select
            v-model:value="form.mealOrderId"
            show-search
            :filter-option="false"
            :options="mealOrderOptions"
            placeholder="请输入订单号或老人姓名搜索"
            @search="searchMealOrders"
            @change="onMealOrderChange"
          />
        </a-form-item>
        <a-form-item label="订单号" required>
          <a-input v-model:value="form.orderNo" readonly />
        </a-form-item>
        <a-form-item label="送餐区域">
          <a-select
            v-model:value="form.deliveryAreaId"
            :options="deliveryAreaOptions"
            allow-clear
            placeholder="请选择送餐区域"
            @change="onDeliveryAreaChange"
          />
        </a-form-item>
        <a-form-item label="送餐人">
          <a-input v-model:value="form.deliveredByName" />
        </a-form-item>
        <a-form-item label="送达时间">
          <a-date-picker v-model:value="form.deliveredAt" show-time style="width: 100%" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status" :options="statusOptions" />
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
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import type { Dayjs } from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import {
  DINING_DELIVERY_STATUS_OPTIONS,
  DINING_MESSAGES,
  DINING_STATUS,
  getDiningDeliveryStatusColor,
  getDiningDeliveryStatusLabel
} from '../../constants/dining'
import {
  getDiningDeliveryRecordPage,
  createDiningDeliveryRecord,
  updateDiningDeliveryRecord,
  deleteDiningDeliveryRecord,
  getDiningMealOrderPage,
  getDiningDeliveryAreaList
} from '../../api/dining'
import type { DiningDeliveryRecord, DiningMealOrder, PageResult } from '../../types'

const statusOptions = DINING_DELIVERY_STATUS_OPTIONS

const loading = ref(false)
const rows = ref<DiningDeliveryRecord[]>([])
const mealOrderOptions = ref<{ label: string; value: number; orderNo: string; deliveryAreaId?: number; deliveryAreaName?: string }[]>([])
const deliveryAreaOptions = ref<{ label: string; value: number; name: string }[]>([])
const query = reactive({
  range: undefined as [Dayjs, Dayjs] | undefined,
  status: undefined as string | undefined,
  keyword: '',
  pageNo: 1,
  pageSize: 10
})
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const editOpen = ref(false)
const saving = ref(false)
const form = reactive({
  id: undefined as number | undefined,
  mealOrderId: undefined as number | undefined,
  orderNo: '',
  deliveryAreaId: undefined as number | undefined,
  deliveryAreaName: '',
  deliveredByName: '',
  deliveredAt: undefined as Dayjs | undefined,
  status: DINING_STATUS.pending,
  remark: ''
})

const columns = [
  { title: '点餐单ID', dataIndex: 'mealOrderId', key: 'mealOrderId', width: 120 },
  { title: '订单号', dataIndex: 'orderNo', key: 'orderNo', width: 150 },
  { title: '送餐区域', dataIndex: 'deliveryAreaName', key: 'deliveryAreaName', width: 130 },
  { title: '送餐人', dataIndex: 'deliveredByName', key: 'deliveredByName', width: 120 },
  { title: '送达时间', dataIndex: 'deliveredAt', key: 'deliveredAt', width: 180 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '操作', key: 'action', width: 140 }
]

async function fetchData() {
  loading.value = true
  try {
    const params: any = { pageNo: query.pageNo, pageSize: query.pageSize, keyword: query.keyword, status: query.status }
    if (query.range) {
      params.dateFrom = query.range[0].format('YYYY-MM-DD')
      params.dateTo = query.range[1].format('YYYY-MM-DD')
    }
    const res: PageResult<DiningDeliveryRecord> = await getDiningDeliveryRecordPage(params)
    rows.value = res.list
    pagination.total = res.total || res.list.length
  } finally {
    loading.value = false
  }
}

function statusLabel(status?: string) {
  return getDiningDeliveryStatusLabel(status)
}

function handleTableChange(pag: any) {
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

function onReset() {
  query.range = undefined
  query.status = undefined
  query.keyword = ''
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

async function loadDeliveryAreaOptions() {
  const list = await getDiningDeliveryAreaList({})
  deliveryAreaOptions.value = (list || []).map((item: any) => ({
    label: `${item.areaCode}-${item.areaName}`,
    value: Number(item.id),
    name: item.areaName
  }))
}

async function searchMealOrders(keyword = '') {
  const page = await getDiningMealOrderPage({ pageNo: 1, pageSize: 30, keyword })
  mealOrderOptions.value = (page.list || []).map((item: DiningMealOrder) => ({
    label: `${item.orderNo} / ${item.elderName || ''}`,
    value: Number(item.id),
    orderNo: item.orderNo,
    deliveryAreaId: item.deliveryAreaId,
    deliveryAreaName: item.deliveryAreaName
  }))
}

function onMealOrderChange(value: number | undefined) {
  const selected = mealOrderOptions.value.find((item) => item.value === value)
  form.orderNo = selected?.orderNo || ''
  form.deliveryAreaId = undefined
  form.deliveryAreaName = ''
  if (selected?.deliveryAreaId) {
    form.deliveryAreaId = selected.deliveryAreaId
    form.deliveryAreaName = selected.deliveryAreaName || ''
  }
}

function onDeliveryAreaChange(value: number | undefined) {
  const selected = deliveryAreaOptions.value.find((item) => item.value === value)
  form.deliveryAreaName = selected?.name || ''
}

async function openCreate() {
  form.id = undefined
  form.mealOrderId = undefined
  form.orderNo = ''
  form.deliveryAreaId = undefined
  form.deliveryAreaName = ''
  form.deliveredByName = ''
  form.deliveredAt = undefined
  form.status = DINING_STATUS.pending
  form.remark = ''
  await Promise.all([searchMealOrders(), loadDeliveryAreaOptions()])
  editOpen.value = true
}

async function openEdit(record: DiningDeliveryRecord) {
  form.id = record.id
  form.mealOrderId = record.mealOrderId
  form.orderNo = record.orderNo
  form.deliveryAreaId = record.deliveryAreaId
  form.deliveryAreaName = record.deliveryAreaName || ''
  form.deliveredByName = record.deliveredByName || ''
  form.deliveredAt = record.deliveredAt ? dayjs(record.deliveredAt) : undefined
  form.status = record.status || DINING_STATUS.pending
  form.remark = record.remark || ''
  await Promise.all([searchMealOrders(record.orderNo || ''), loadDeliveryAreaOptions()])
  editOpen.value = true
}

async function submit() {
  if (!form.mealOrderId || !form.orderNo) {
    message.error(DINING_MESSAGES.requiredMealOrder)
    return
  }
  saving.value = true
  try {
    const payload = {
      mealOrderId: form.mealOrderId,
      orderNo: form.orderNo,
      deliveryAreaId: form.deliveryAreaId,
      deliveryAreaName: form.deliveryAreaName,
      deliveredByName: form.deliveredByName.trim() || undefined,
      deliveredAt: form.deliveredAt ? dayjs(form.deliveredAt).format('YYYY-MM-DD HH:mm:ss') : undefined,
      status: form.status,
      remark: form.remark.trim() || undefined
    }
    if (form.id) {
      await updateDiningDeliveryRecord(form.id, payload)
    } else {
      await createDiningDeliveryRecord(payload)
    }
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function remove(record: DiningDeliveryRecord) {
  await deleteDiningDeliveryRecord(record.id)
  fetchData()
}

searchMealOrders()
loadDeliveryAreaOptions()
fetchData()
</script>
