<template>
  <PageContainer title="服务预定" subTitle="单次服务预约与执行状态跟踪">
    <a-card :bordered="false" class="card-elevated">
      <a-form layout="inline" @submit.prevent>
        <a-form-item label="预约时间">
          <a-range-picker v-model:value="query.range" show-time value-format="YYYY-MM-DD HH:mm:ss" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.status" :options="statusOptions" allow-clear style="width: 140px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="search">查询</a-button>
            <a-button @click="reset">重置</a-button>
            <a-button type="primary" @click="openModal()">新增预定</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <a-table row-key="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" @change="onTableChange">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="statusColor(record.status)">{{ statusLabel(record.status) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'actions'">
            <a-space>
              <a @click="openModal(record)">编辑</a>
              <a class="danger-text" @click="remove(record.id)">删除</a>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal v-model:open="modalOpen" title="服务预定" :confirm-loading="submitting" width="760" @ok="submit">
      <a-form ref="formRef" layout="vertical" :model="form" :rules="rules">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="老人" name="elderId" required>
              <a-select
                v-model:value="form.elderId"
                show-search
                :filter-option="false"
                :options="elderOptions"
                @search="searchElder"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="服务项目" name="serviceItemId" required>
              <a-select v-model:value="form.serviceItemId" :options="serviceItemOptions" show-search option-filter-prop="label" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="关联计划" name="planId">
              <a-select v-model:value="form.planId" allow-clear :options="planOptions" show-search option-filter-prop="label" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="预约时间" name="bookingTime" required>
              <a-date-picker v-model:value="form.bookingTime" show-time value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="预计时长(分钟)" name="expectedDuration">
              <a-input-number v-model:value="form.expectedDuration" :min="1" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="指派护工" name="assignedStaffId">
              <a-select
                v-model:value="form.assignedStaffId"
                show-search
                allow-clear
                :filter-option="false"
                :options="staffOptions"
                @search="searchStaff"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="来源" name="source">
              <a-select v-model:value="form.source" :options="sourceOptions" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="状态" name="status">
              <a-select v-model:value="form.status" :options="statusOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="16">
            <a-form-item label="取消原因" name="cancelReason">
              <a-input v-model:value="form.cancelReason" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="备注" name="remark">
          <a-textarea v-model:value="form.remark" :rows="2" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { useElderOptions } from '../../composables/useElderOptions'
import { getStaffPage } from '../../api/rbac'
import { listServiceItems } from '../../api/standard'
import {
  createServiceBooking,
  deleteServiceBooking,
  getServiceBookingPage,
  getServicePlanList,
  updateServiceBooking
} from '../../api/nursing'
import type { PageResult, ServiceBookingItem, ServiceItem, ServicePlanItem, StaffItem } from '../../types'

const rows = ref<ServiceBookingItem[]>([])
const loading = ref(false)
const modalOpen = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const { elderOptions, searchElders, ensureSelectedElder } = useElderOptions({ pageSize: 50 })
const staffOptions = ref<Array<{ label: string; value: number }>>([])
const serviceItemOptions = ref<Array<{ label: string; value: number }>>([])
const planOptions = ref<Array<{ label: string; value: number }>>([])

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  total: 0,
  range: undefined as string[] | undefined,
  status: undefined as string | undefined
})

const form = reactive<Partial<ServiceBookingItem>>({
  elderId: undefined,
  serviceItemId: undefined,
  planId: undefined,
  bookingTime: '',
  expectedDuration: undefined,
  assignedStaffId: undefined,
  source: 'MANUAL',
  status: 'BOOKED',
  cancelReason: '',
  remark: ''
})

const rules = {
  elderId: [{ required: true, message: '请选择老人', trigger: 'change' }],
  serviceItemId: [{ required: true, message: '请选择服务项目', trigger: 'change' }],
  bookingTime: [{ required: true, message: '请选择预约时间', trigger: 'change' }]
}

const statusOptions = [
  { label: '已预约', value: 'BOOKED' },
  { label: '服务中', value: 'IN_SERVICE' },
  { label: '已完成', value: 'DONE' },
  { label: '已取消', value: 'CANCELLED' }
]

const sourceOptions = [
  { label: '人工创建', value: 'MANUAL' },
  { label: '计划生成', value: 'PLAN' },
  { label: '家属预约', value: 'FAMILY' }
]

const columns = [
  { title: '预约时间', dataIndex: 'bookingTime', key: 'bookingTime', width: 170 },
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '服务项目', dataIndex: 'serviceItemName', key: 'serviceItemName', width: 140 },
  { title: '关联计划', dataIndex: 'planName', key: 'planName', width: 160 },
  { title: '护工', dataIndex: 'assignedStaffName', key: 'assignedStaffName', width: 120 },
  { title: '状态', key: 'status', width: 110 },
  { title: '操作', key: 'actions', width: 140 }
]

const pagination = computed(() => ({ current: query.pageNo, pageSize: query.pageSize, total: query.total, showSizeChanger: true }))

function statusLabel(status?: string) {
  if (status === 'IN_SERVICE') return '服务中'
  if (status === 'DONE') return '已完成'
  if (status === 'CANCELLED') return '已取消'
  return '已预约'
}

function statusColor(status?: string) {
  if (status === 'IN_SERVICE') return 'processing'
  if (status === 'DONE') return 'green'
  if (status === 'CANCELLED') return 'default'
  return 'blue'
}

function resetForm() {
  form.id = undefined
  form.elderId = undefined
  form.serviceItemId = undefined
  form.planId = undefined
  form.bookingTime = ''
  form.expectedDuration = undefined
  form.assignedStaffId = undefined
  form.source = 'MANUAL'
  form.status = 'BOOKED'
  form.cancelReason = ''
  form.remark = ''
}

function openModal(record?: ServiceBookingItem) {
  resetForm()
  if (record) {
    Object.assign(form, record)
    ensureSelectedElder(record.elderId, record.elderName)
  }
  modalOpen.value = true
}

async function submit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const payload: Partial<ServiceBookingItem> = {
      elderId: form.elderId,
      serviceItemId: form.serviceItemId,
      planId: form.planId,
      bookingTime: form.bookingTime,
      expectedDuration: form.expectedDuration,
      assignedStaffId: form.assignedStaffId,
      source: form.source,
      status: form.status,
      cancelReason: form.cancelReason,
      remark: form.remark
    }
    if (form.id) {
      await updateServiceBooking(form.id, payload)
    } else {
      await createServiceBooking(payload)
    }
    message.success('保存成功')
    modalOpen.value = false
    await load()
  } finally {
    submitting.value = false
  }
}

async function remove(id: number) {
  Modal.confirm({
    title: '确认删除该服务预定？',
    onOk: async () => {
      await deleteServiceBooking(id)
      message.success('删除成功')
      await load()
    }
  })
}

function onTableChange(pag: { current?: number; pageSize?: number }) {
  query.pageNo = pag.current || 1
  query.pageSize = pag.pageSize || 10
  load()
}

function search() {
  query.pageNo = 1
  load()
}

function reset() {
  query.range = undefined
  query.status = undefined
  query.pageNo = 1
  load()
}

async function searchElder(keyword: string) {
  await searchElders(keyword)
}

async function searchStaff(keyword: string) {
  const res: PageResult<StaffItem> = await getStaffPage({ pageNo: 1, pageSize: 50, keyword })
  staffOptions.value = res.list.map((item) => ({ label: item.realName || item.username, value: item.id }))
}

async function loadBaseOptions() {
  const [serviceItems, plans] = await Promise.all([
    listServiceItems({ enabled: 1 }) as Promise<ServiceItem[]>,
    getServicePlanList({ status: 'ACTIVE' }) as Promise<ServicePlanItem[]>
  ])
  serviceItemOptions.value = serviceItems.map((item) => ({ label: item.name, value: item.id }))
  planOptions.value = plans.map((item) => ({ label: item.planName, value: item.id }))
  await Promise.all([searchElders(''), searchStaff('')])
}

async function load() {
  loading.value = true
  try {
    const res: PageResult<ServiceBookingItem> = await getServiceBookingPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      timeFrom: query.range?.[0],
      timeTo: query.range?.[1],
      status: query.status
    })
    rows.value = res.list
    query.total = res.total
  } finally {
    loading.value = false
  }
}

load()
loadBaseOptions()
</script>

<style scoped>
.danger-text { color: #ef4444; }
</style>
