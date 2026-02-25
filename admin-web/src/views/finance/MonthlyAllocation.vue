<template>
  <PageContainer title="月分摊费" subTitle="按月份建立分摊项目（草稿）">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="月份">
        <a-date-picker v-model:value="query.month" picker="month" allow-clear />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 180px" :options="statusOptions" />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openCreate">新建分摊</a-button>
      </template>
    </SearchForm>

    <DataTable rowKey="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'avgAmount'">
          {{ calcAvgAmount(record.totalAmount, record.targetCount) }}
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="createOpen" title="新建月分摊" :confirm-loading="creating" @ok="submitCreate">
      <a-form layout="vertical">
        <a-form-item label="分摊月份" required>
          <a-date-picker v-model:value="createForm.allocationMonth" picker="month" style="width: 100%" />
        </a-form-item>
        <a-form-item label="分摊项目" required>
          <a-input v-model:value="createForm.allocationName" />
        </a-form-item>
        <a-form-item label="分摊总金额" required>
          <a-input-number v-model:value="createForm.totalAmount" :min="0" :precision="2" style="width: 100%" />
        </a-form-item>
        <a-form-item label="目标人数" required>
          <a-input-number v-model:value="createForm.targetCount" :min="0" :precision="0" style="width: 100%" />
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
import { createMonthlyAllocation, getMonthlyAllocationPage } from '../../api/financeFee'
import type { MonthlyAllocationItem, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<MonthlyAllocationItem[]>([])
const query = reactive({ pageNo: 1, pageSize: 10, month: undefined as any, status: undefined as string | undefined })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const statusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已确认', value: 'CONFIRMED' },
  { label: '已取消', value: 'CANCELLED' }
]

const columns = [
  { title: '月份', dataIndex: 'allocationMonth', key: 'allocationMonth', width: 120 },
  { title: '分摊项目', dataIndex: 'allocationName', key: 'allocationName', width: 160 },
  { title: '总金额', dataIndex: 'totalAmount', key: 'totalAmount', width: 120 },
  { title: '目标人数', dataIndex: 'targetCount', key: 'targetCount', width: 100 },
  { title: '人均金额', key: 'avgAmount', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '备注', dataIndex: 'remark', key: 'remark' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 }
]

const createOpen = ref(false)
const creating = ref(false)
const createForm = reactive({
  allocationMonth: undefined as any,
  allocationName: '',
  totalAmount: 0,
  targetCount: 0,
  remark: ''
})

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<MonthlyAllocationItem> = await getMonthlyAllocationPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      month: query.month ? dayjs(query.month).format('YYYY-MM') : undefined,
      status: query.status
    })
    rows.value = res.list
    pagination.total = res.total || res.list.length
  } finally {
    loading.value = false
  }
}

function onReset() {
  query.pageNo = 1
  query.month = undefined
  query.status = undefined
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
  createForm.allocationMonth = dayjs().startOf('month')
  createForm.allocationName = ''
  createForm.totalAmount = 0
  createForm.targetCount = 0
  createForm.remark = ''
  createOpen.value = true
}

function calcAvgAmount(totalAmount: number, targetCount: number) {
  if (!targetCount || targetCount <= 0) {
    return '--'
  }
  return (Number(totalAmount || 0) / targetCount).toFixed(2)
}

async function submitCreate() {
  if (!createForm.allocationMonth) {
    message.error('请选择月份')
    return
  }
  if (!createForm.allocationName) {
    message.error('请输入分摊项目')
    return
  }
  if (createForm.totalAmount > 0 && createForm.targetCount <= 0) {
    message.error('总金额大于0时，目标人数必须大于0')
    return
  }
  if (createForm.totalAmount < 0 || createForm.targetCount < 0) {
    message.error('金额/人数不能小于0')
    return
  }
  creating.value = true
  try {
    await createMonthlyAllocation({
      allocationMonth: dayjs(createForm.allocationMonth).format('YYYY-MM'),
      allocationName: createForm.allocationName,
      totalAmount: createForm.totalAmount,
      targetCount: createForm.targetCount,
      remark: createForm.remark || undefined
    })
    message.success('创建成功')
    createOpen.value = false
    fetchData()
  } finally {
    creating.value = false
  }
}

fetchData()
</script>
