<template>
  <PageContainer title="服务计划" subTitle="老人服务计划编排与执行人设置">
    <a-card :bordered="false" class="card-elevated">
      <a-form layout="inline" @submit.prevent>
        <a-form-item label="关键字">
          <a-input v-model:value="query.keyword" placeholder="计划名称" allow-clear />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.status" :options="statusOptions" allow-clear style="width: 140px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="search">查询</a-button>
            <a-button @click="reset">重置</a-button>
            <a-button type="primary" @click="openModal()">新增计划</a-button>
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

    <a-modal v-model:open="modalOpen" title="服务计划" :confirm-loading="submitting" width="760" @ok="submit">
      <a-form ref="formRef" layout="vertical" :model="form" :rules="rules">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="计划名称" name="planName" required>
              <a-input v-model:value="form.planName" />
            </a-form-item>
          </a-col>
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
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="服务项目" name="serviceItemId" required>
              <a-select v-model:value="form.serviceItemId" :options="serviceItemOptions" show-search option-filter-prop="label" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="护理等级" name="careLevelId">
              <a-select v-model:value="form.careLevelId" allow-clear :options="careLevelOptions" show-search option-filter-prop="label" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="周期" name="cycleType">
              <a-select v-model:value="form.cycleType" :options="cycleOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="频次" name="frequency">
              <a-input-number v-model:value="form.frequency" :min="1" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="默认护工" name="defaultStaffId">
              <a-select
                v-model:value="form.defaultStaffId"
                show-search
                allow-clear
                :filter-option="false"
                :options="staffOptions"
                @search="searchStaff"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="开始日期" name="startDate" required>
              <a-date-picker v-model:value="form.startDate" value-format="YYYY-MM-DD" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="结束日期" name="endDate">
              <a-date-picker v-model:value="form.endDate" value-format="YYYY-MM-DD" style="width: 100%" />
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
            <a-form-item label="备注" name="remark">
              <a-input v-model:value="form.remark" />
            </a-form-item>
          </a-col>
        </a-row>
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
import { createServicePlan, deleteServicePlan, getCareLevelList, getServicePlanPage, updateServicePlan } from '../../api/nursing'
import type { CareLevelItem, PageResult, ServiceItem, ServicePlanItem, StaffItem } from '../../types'

const rows = ref<ServicePlanItem[]>([])
const loading = ref(false)
const modalOpen = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const { elderOptions, searchElders, ensureSelectedElder } = useElderOptions({ pageSize: 50 })
const staffOptions = ref<Array<{ label: string; value: number }>>([])
const careLevelOptions = ref<Array<{ label: string; value: number }>>([])
const serviceItemOptions = ref<Array<{ label: string; value: number }>>([])

const query = reactive({ pageNo: 1, pageSize: 10, total: 0, keyword: undefined as string | undefined, status: undefined as string | undefined })

const form = reactive<Partial<ServicePlanItem>>({
  planName: '',
  elderId: undefined,
  careLevelId: undefined,
  serviceItemId: undefined,
  cycleType: 'DAILY',
  frequency: 1,
  startDate: '',
  endDate: undefined,
  defaultStaffId: undefined,
  status: 'ACTIVE',
  remark: ''
})

const rules = {
  planName: [{ required: true, message: '请输入计划名称', trigger: 'blur' }],
  elderId: [{ required: true, message: '请选择老人', trigger: 'change' }],
  serviceItemId: [{ required: true, message: '请选择服务项目', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }]
}

const cycleOptions = [
  { label: '每日', value: 'DAILY' },
  { label: '每周', value: 'WEEKLY' },
  { label: '每月', value: 'MONTHLY' }
]

const statusOptions = [
  { label: '生效', value: 'ACTIVE' },
  { label: '暂停', value: 'PAUSED' },
  { label: '关闭', value: 'CLOSED' }
]

const columns = [
  { title: '计划名称', dataIndex: 'planName', key: 'planName', width: 180 },
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '服务项目', dataIndex: 'serviceItemName', key: 'serviceItemName', width: 140 },
  { title: '周期', dataIndex: 'cycleType', key: 'cycleType', width: 100 },
  { title: '频次', dataIndex: 'frequency', key: 'frequency', width: 80 },
  { title: '开始', dataIndex: 'startDate', key: 'startDate', width: 110 },
  { title: '结束', dataIndex: 'endDate', key: 'endDate', width: 110 },
  { title: '状态', key: 'status', width: 100 },
  { title: '操作', key: 'actions', width: 140 }
]

const pagination = computed(() => ({ current: query.pageNo, pageSize: query.pageSize, total: query.total, showSizeChanger: true }))

function statusLabel(status?: string) {
  if (status === 'PAUSED') return '暂停'
  if (status === 'CLOSED') return '关闭'
  return '生效'
}

function statusColor(status?: string) {
  if (status === 'PAUSED') return 'orange'
  if (status === 'CLOSED') return 'default'
  return 'green'
}

function resetForm() {
  form.id = undefined
  form.planName = ''
  form.elderId = undefined
  form.careLevelId = undefined
  form.serviceItemId = undefined
  form.cycleType = 'DAILY'
  form.frequency = 1
  form.startDate = ''
  form.endDate = undefined
  form.defaultStaffId = undefined
  form.status = 'ACTIVE'
  form.remark = ''
}

function openModal(record?: ServicePlanItem) {
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
    const payload: Partial<ServicePlanItem> = {
      planName: form.planName,
      elderId: form.elderId,
      careLevelId: form.careLevelId,
      serviceItemId: form.serviceItemId,
      cycleType: form.cycleType,
      frequency: form.frequency,
      startDate: form.startDate,
      endDate: form.endDate,
      defaultStaffId: form.defaultStaffId,
      status: form.status,
      remark: form.remark
    }
    if (form.id) {
      await updateServicePlan(form.id, payload)
    } else {
      await createServicePlan(payload)
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
    title: '确认删除该服务计划？',
    onOk: async () => {
      await deleteServicePlan(id)
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
  query.keyword = undefined
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
  const [careLevels, serviceItems] = await Promise.all([
    getCareLevelList({ enabled: 1 }) as Promise<CareLevelItem[]>,
    listServiceItems({ enabled: 1 }) as Promise<ServiceItem[]>
  ])
  careLevelOptions.value = careLevels.map((item) => ({ label: `${item.levelCode} - ${item.levelName}`, value: item.id }))
  serviceItemOptions.value = serviceItems.map((item) => ({ label: item.name, value: item.id }))
  await Promise.all([searchElders(''), searchStaff('')])
}

async function load() {
  loading.value = true
  try {
    const res: PageResult<ServicePlanItem> = await getServicePlanPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      status: query.status,
      keyword: query.keyword
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
