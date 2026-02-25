<template>
  <PageContainer title="维修管理" subTitle="报修受理与维修闭环">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="状态">
        <a-select v-model:value="query.status" :options="statusOptions" allow-clear style="width: 160px" />
      </a-form-item>
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="房间/类型/报修人" allow-clear />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openCreate">新增报修</a-button>
      </template>
    </SearchForm>

    <DataTable
      rowKey="id"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 'COMPLETED' ? 'green' : record.status === 'PROCESSING' ? 'blue' : 'orange'">
            {{ statusLabel(record.status) }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'priority'">
          <a-tag :color="record.priority === 'HIGH' ? 'red' : record.priority === 'LOW' ? 'blue' : 'gold'">
            {{ record.priority === 'HIGH' ? '高' : record.priority === 'LOW' ? '低' : '普通' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openEdit(record)">编辑</a-button>
            <a-button type="link" @click="complete(record)">完工</a-button>
            <a-button type="link" danger @click="remove(record)">删除</a-button>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="editOpen" :title="form.id ? '编辑报修' : '新增报修'" @ok="submit" :confirm-loading="saving" width="680px">
      <a-form layout="vertical">
        <a-form-item label="房间">
          <a-select
            v-model:value="form.roomId"
            show-search
            allow-clear
            :options="roomOptions"
            placeholder="可选"
            option-filter-prop="label"
          />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="报修人" required>
              <a-input v-model:value="form.reporterName" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="维修负责人">
              <a-input v-model:value="form.assigneeName" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="故障类型" required>
              <a-input v-model:value="form.issueType" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="优先级">
              <a-select v-model:value="form.priority" :options="priorityOptions" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="故障描述" required>
          <a-textarea v-model:value="form.description" :rows="3" />
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
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { getRoomList } from '../../api/bed'
import { getMaintenancePage, createMaintenance, updateMaintenance, completeMaintenance, deleteMaintenance } from '../../api/life'
import type { MaintenanceRequest, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<MaintenanceRequest[]>([])
const roomOptions = ref<any[]>([])
const query = reactive({ status: undefined as string | undefined, keyword: '', pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const columns = [
  { title: '房间号', dataIndex: 'roomNo', key: 'roomNo', width: 120 },
  { title: '故障类型', dataIndex: 'issueType', key: 'issueType', width: 140 },
  { title: '报修人', dataIndex: 'reporterName', key: 'reporterName', width: 120 },
  { title: '维修负责人', dataIndex: 'assigneeName', key: 'assigneeName', width: 120 },
  { title: '优先级', dataIndex: 'priority', key: 'priority', width: 90 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 110 },
  { title: '报修时间', dataIndex: 'reportedAt', key: 'reportedAt', width: 170 },
  { title: '操作', key: 'action', width: 180 }
]

const statusOptions = [
  { label: '待处理', value: 'OPEN' },
  { label: '处理中', value: 'PROCESSING' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' }
]
const priorityOptions = [
  { label: '低', value: 'LOW' },
  { label: '普通', value: 'NORMAL' },
  { label: '高', value: 'HIGH' }
]

const editOpen = ref(false)
const saving = ref(false)
const form = reactive({
  id: undefined as number | undefined,
  roomId: undefined as number | undefined,
  reporterName: '',
  assigneeName: '',
  issueType: '',
  description: '',
  priority: 'NORMAL',
  status: 'OPEN',
  remark: ''
})

function statusLabel(status?: string) {
  if (status === 'PROCESSING') return '处理中'
  if (status === 'COMPLETED') return '已完成'
  if (status === 'CANCELLED') return '已取消'
  return '待处理'
}

async function loadRooms() {
  const rooms = await getRoomList()
  roomOptions.value = rooms.map((r: any) => ({ label: `${r.roomNo}`, value: r.id }))
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<MaintenanceRequest> = await getMaintenancePage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      status: query.status,
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
  query.status = undefined
  query.keyword = ''
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openCreate() {
  form.id = undefined
  form.roomId = undefined
  form.reporterName = ''
  form.assigneeName = ''
  form.issueType = ''
  form.description = ''
  form.priority = 'NORMAL'
  form.status = 'OPEN'
  form.remark = ''
  editOpen.value = true
}

function openEdit(record: MaintenanceRequest) {
  form.id = record.id
  form.roomId = record.roomId
  form.reporterName = record.reporterName
  form.assigneeName = record.assigneeName || ''
  form.issueType = record.issueType
  form.description = record.description
  form.priority = record.priority || 'NORMAL'
  form.status = record.status || 'OPEN'
  form.remark = record.remark || ''
  editOpen.value = true
}

async function submit() {
  const payload = {
    roomId: form.roomId,
    reporterName: form.reporterName,
    assigneeName: form.assigneeName,
    issueType: form.issueType,
    description: form.description,
    priority: form.priority,
    status: form.status,
    remark: form.remark
  }
  saving.value = true
  try {
    if (form.id) {
      await updateMaintenance(form.id, payload)
    } else {
      await createMaintenance(payload)
    }
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function complete(record: MaintenanceRequest) {
  await completeMaintenance(record.id)
  fetchData()
}

async function remove(record: MaintenanceRequest) {
  await deleteMaintenance(record.id)
  fetchData()
}

loadRooms()
fetchData()
</script>
