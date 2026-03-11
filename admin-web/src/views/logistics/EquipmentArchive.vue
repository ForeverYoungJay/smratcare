<template>
  <PageContainer title="设备档案" subTitle="独立设备台账（序列号、维保计划、状态）">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="编码/名称/分类/位置/序列号" allow-clear />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" :options="statusOptions" allow-clear style="width: 160px" />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openCreate">新增设备</a-button>
      </template>
    </SearchForm>

    <a-card class="card-elevated" :bordered="false" style="margin-bottom: 12px">
      <a-row :gutter="12">
        <a-col :span="6"><a-statistic title="设备总数" :value="summary.total" /></a-col>
        <a-col :span="6"><a-statistic title="启用中" :value="summary.enabled" /></a-col>
        <a-col :span="6"><a-statistic title="维保中" :value="summary.maintaining" /></a-col>
        <a-col :span="6"><a-statistic title="30天内待维保" :value="summary.dueSoon" /></a-col>
      </a-row>
    </a-card>

    <DataTable rowKey="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="statusColor(record.status)">{{ statusLabel(record.status) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="createMaintenance(record)">发起维修</a-button>
            <a-button type="link" @click="openEdit(record)">编辑</a-button>
            <a-button type="link" danger @click="remove(record)">删除</a-button>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="editOpen" :title="form.id ? '编辑设备' : '新增设备'" :confirm-loading="saving" width="760px" @ok="submit">
      <a-form layout="vertical">
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="设备编码" required>
              <a-input v-model:value="form.equipmentCode" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="设备名称" required>
              <a-input v-model:value="form.equipmentName" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :span="8"><a-form-item label="分类"><a-input v-model:value="form.category" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="品牌"><a-input v-model:value="form.brand" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="状态"><a-select v-model:value="form.status" :options="statusOptions" /></a-form-item></a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :span="12"><a-form-item label="型号"><a-input v-model:value="form.modelNo" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="序列号"><a-input v-model:value="form.serialNo" /></a-form-item></a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :span="12"><a-form-item label="采购日期"><a-date-picker v-model:value="form.purchaseDate" value-format="YYYY-MM-DD" style="width: 100%" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="质保到期"><a-date-picker v-model:value="form.warrantyUntil" value-format="YYYY-MM-DD" style="width: 100%" /></a-form-item></a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :span="12"><a-form-item label="所在位置"><a-input v-model:value="form.location" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="维保负责人"><a-input v-model:value="form.maintainerName" /></a-form-item></a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="最近维保时间">
              <a-date-picker v-model:value="form.lastMaintainedAt" show-time value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="下次维保时间">
              <a-date-picker v-model:value="form.nextMaintainedAt" show-time value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="备注"><a-input v-model:value="form.remark" /></a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import {
  createLogisticsEquipment,
  deleteLogisticsEquipment,
  generateEquipmentMaintenance,
  getLogisticsEquipmentPage,
  updateLogisticsEquipment
} from '../../api/logistics'
import type { Id, LogisticsEquipmentArchive, PageResult } from '../../types'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const saving = ref(false)
const editOpen = ref(false)
const rows = ref<LogisticsEquipmentArchive[]>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const summary = reactive({ total: 0, enabled: 0, maintaining: 0, dueSoon: 0 })

const query = reactive({
  keyword: '',
  status: (route.query.status as string | undefined) || undefined,
  pageNo: 1,
  pageSize: 10
})

const form = reactive({
  id: undefined as Id | undefined,
  equipmentCode: '',
  equipmentName: '',
  category: '',
  brand: '',
  modelNo: '',
  serialNo: '',
  purchaseDate: undefined as string | undefined,
  warrantyUntil: undefined as string | undefined,
  location: '',
  maintainerName: '',
  lastMaintainedAt: undefined as string | undefined,
  nextMaintainedAt: undefined as string | undefined,
  status: 'ENABLED',
  remark: ''
})

const statusOptions = [
  { label: '启用', value: 'ENABLED' },
  { label: '停用', value: 'DISABLED' },
  { label: '维保中', value: 'MAINTENANCE' },
  { label: '报废', value: 'SCRAPPED' }
]

const columns = [
  { title: '设备编码', dataIndex: 'equipmentCode', key: 'equipmentCode', width: 130 },
  { title: '设备名称', dataIndex: 'equipmentName', key: 'equipmentName', width: 160 },
  { title: '分类', dataIndex: 'category', key: 'category', width: 110 },
  { title: '品牌/型号', key: 'brandModel', width: 160, customRender: ({ record }: any) => `${record.brand || '-'} / ${record.modelNo || '-'}` },
  { title: '序列号', dataIndex: 'serialNo', key: 'serialNo', width: 150 },
  { title: '位置', dataIndex: 'location', key: 'location', width: 120 },
  { title: '下次维保', dataIndex: 'nextMaintainedAt', key: 'nextMaintainedAt', width: 170 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 210 }
]

function statusLabel(status?: string) {
  if (status === 'DISABLED') return '停用'
  if (status === 'MAINTENANCE') return '维保中'
  if (status === 'SCRAPPED') return '报废'
  return '启用'
}

function statusColor(status?: string) {
  if (status === 'DISABLED') return 'default'
  if (status === 'MAINTENANCE') return 'orange'
  if (status === 'SCRAPPED') return 'red'
  return 'green'
}

function calcSummary() {
  summary.total = rows.value.length
  summary.enabled = rows.value.filter((item) => item.status === 'ENABLED').length
  summary.maintaining = rows.value.filter((item) => item.status === 'MAINTENANCE').length
  summary.dueSoon = rows.value.filter((item) => {
    if (!item.nextMaintainedAt) return false
    const diff = dayjs(item.nextMaintainedAt).diff(dayjs(), 'day')
    return diff >= 0 && diff <= 30
  }).length
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<LogisticsEquipmentArchive> = await getLogisticsEquipmentPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined,
      status: query.status || undefined
    })
    rows.value = res.list || []
    pagination.total = res.total || 0
    calcSummary()
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.id = undefined
  form.equipmentCode = ''
  form.equipmentName = ''
  form.category = ''
  form.brand = ''
  form.modelNo = ''
  form.serialNo = ''
  form.purchaseDate = undefined
  form.warrantyUntil = undefined
  form.location = ''
  form.maintainerName = ''
  form.lastMaintainedAt = undefined
  form.nextMaintainedAt = undefined
  form.status = 'ENABLED'
  form.remark = ''
}

function openCreate() {
  resetForm()
  editOpen.value = true
}

function openEdit(record: LogisticsEquipmentArchive) {
  form.id = record.id
  form.equipmentCode = record.equipmentCode
  form.equipmentName = record.equipmentName
  form.category = record.category || ''
  form.brand = record.brand || ''
  form.modelNo = record.modelNo || ''
  form.serialNo = record.serialNo || ''
  form.purchaseDate = record.purchaseDate
  form.warrantyUntil = record.warrantyUntil
  form.location = record.location || ''
  form.maintainerName = record.maintainerName || ''
  form.lastMaintainedAt = record.lastMaintainedAt
  form.nextMaintainedAt = record.nextMaintainedAt
  form.status = record.status || 'ENABLED'
  form.remark = record.remark || ''
  editOpen.value = true
}

async function submit() {
  const payload = {
    equipmentCode: form.equipmentCode.trim(),
    equipmentName: form.equipmentName.trim(),
    category: form.category.trim() || undefined,
    brand: form.brand.trim() || undefined,
    modelNo: form.modelNo.trim() || undefined,
    serialNo: form.serialNo.trim() || undefined,
    purchaseDate: form.purchaseDate,
    warrantyUntil: form.warrantyUntil,
    location: form.location.trim() || undefined,
    maintainerName: form.maintainerName.trim() || undefined,
    lastMaintainedAt: form.lastMaintainedAt,
    nextMaintainedAt: form.nextMaintainedAt,
    status: form.status,
    remark: form.remark.trim() || undefined
  }
  saving.value = true
  try {
    if (form.id) {
      await updateLogisticsEquipment(form.id, payload)
    } else {
      await createLogisticsEquipment(payload)
    }
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function remove(record: LogisticsEquipmentArchive) {
  await deleteLogisticsEquipment(record.id)
  fetchData()
}

async function createMaintenance(record: LogisticsEquipmentArchive) {
  await generateEquipmentMaintenance(record.id)
  message.success('已发起维修单')
  router.push('/logistics/assets/maintenance-record?status=OPEN')
}

function onReset() {
  query.keyword = ''
  query.status = undefined
  query.pageNo = 1
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

onMounted(fetchData)
</script>
