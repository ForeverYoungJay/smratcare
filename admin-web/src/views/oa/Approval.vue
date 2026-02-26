<template>
  <PageContainer title="审批流程" subTitle="请假/加班/报销/采购/收入证明/物资申请/用章审批">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" placeholder="标题/申请人/备注" allow-clear style="width: 240px" />
      </a-form-item>
      <a-form-item label="类型">
        <a-select v-model:value="query.type" :options="typeOptions" allow-clear style="width: 160px" />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" :options="statusOptions" allow-clear style="width: 160px" />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openCreate">新增审批</a-button>
        <a-button :disabled="selectedRowKeys.length === 0" @click="batchApprove">批量通过</a-button>
        <a-button :disabled="selectedRowKeys.length === 0" @click="batchReject">批量驳回</a-button>
        <a-button :disabled="selectedRowKeys.length === 0" danger @click="batchRemove">批量删除</a-button>
        <a-button @click="downloadExport">导出CSV</a-button>
      </template>
    </SearchForm>

    <DataTable
      rowKey="id"
      :row-selection="rowSelection"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 'APPROVED' ? 'green' : record.status === 'REJECTED' ? 'red' : 'orange'">
            {{ statusLabel(record.status) }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" :disabled="record.status !== 'PENDING'" @click="openEdit(record)">编辑</a-button>
            <a-button type="link" :disabled="record.status !== 'PENDING'" @click="approve(record)">通过</a-button>
            <a-button type="link" :disabled="record.status !== 'PENDING'" @click="reject(record)">驳回</a-button>
            <a-button type="link" danger @click="remove(record)">删除</a-button>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="editOpen" title="审批" @ok="submit" :confirm-loading="saving" width="700px">
      <a-form layout="vertical">
        <a-form-item label="类型" required>
          <a-select v-model:value="form.approvalType" :options="typeOptions" />
        </a-form-item>
        <a-form-item label="标题" required>
          <a-input v-model:value="form.title" />
        </a-form-item>
        <a-form-item label="申请人" required>
          <a-input v-model:value="form.applicantName" />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="金额">
              <a-input-number v-model:value="form.amount" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="状态">
              <a-select v-model:value="form.status" :options="editableStatusOptions" disabled />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="开始时间">
              <a-date-picker v-model:value="form.startTime" show-time style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="结束时间">
              <a-date-picker v-model:value="form.endTime" show-time style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="表单数据(JSON)">
          <a-textarea v-model:value="form.formData" :rows="3" />
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="form.remark" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import {
  getApprovalPage,
  createApproval,
  updateApproval,
  approveApproval,
  rejectApproval,
  deleteApproval,
  batchApproveApproval,
  batchRejectApproval,
  batchDeleteApproval,
  exportApproval
} from '../../api/oa'
import type { OaApproval, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<OaApproval[]>([])
const route = useRoute()
const query = reactive({
  keyword: '',
  type: undefined as string | undefined,
  status: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const selectedRowKeys = ref<number[]>([])

const columns = [
  { title: '类型', dataIndex: 'approvalType', key: 'approvalType', width: 120 },
  { title: '标题', dataIndex: 'title', key: 'title', width: 200 },
  { title: '申请人', dataIndex: 'applicantName', key: 'applicantName', width: 120 },
  { title: '金额', dataIndex: 'amount', key: 'amount', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 220 }
]

const editOpen = ref(false)
const saving = ref(false)
const form = reactive({
  id: undefined as number | undefined,
  approvalType: 'LEAVE',
  title: '',
  applicantName: '',
  amount: undefined as number | undefined,
  startTime: undefined as any,
  endTime: undefined as any,
  formData: '',
  status: 'PENDING',
  remark: ''
})

const typeOptions = [
  { label: '请假', value: 'LEAVE' },
  { label: '加班', value: 'OVERTIME' },
  { label: '报销', value: 'REIMBURSE' },
  { label: '采购', value: 'PURCHASE' },
  { label: '收入证明', value: 'INCOME_PROOF' },
  { label: '物资申领', value: 'MATERIAL_APPLY' },
  { label: '用章申请', value: 'OFFICIAL_SEAL' }
]
const statusOptions = [
  { label: '待审批', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' }
]
const editableStatusOptions = [{ label: '待审批', value: 'PENDING' }]
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: (string | number)[]) => {
    selectedRowKeys.value = keys.map((item) => Number(item))
  }
}))

function statusLabel(status?: string) {
  if (status === 'APPROVED') return '已通过'
  if (status === 'REJECTED') return '已驳回'
  return '待审批'
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<OaApproval> = await getApprovalPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      status: query.status,
      type: query.type,
      keyword: query.keyword || undefined
    })
    rows.value = res.list
    pagination.total = res.total || res.list.length
    selectedRowKeys.value = []
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
  query.status = undefined
  query.type = undefined
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openCreate() {
  form.id = undefined
  form.approvalType = (query.type || 'LEAVE') as string
  form.title = ''
  form.applicantName = ''
  form.amount = undefined
  form.startTime = undefined
  form.endTime = undefined
  form.formData = ''
  form.status = 'PENDING'
  form.remark = ''
  editOpen.value = true
}

function openEdit(record: OaApproval) {
  form.id = record.id
  form.approvalType = record.approvalType
  form.title = record.title
  form.applicantName = record.applicantName
  form.amount = record.amount
  form.startTime = record.startTime ? dayjs(record.startTime) : undefined
  form.endTime = record.endTime ? dayjs(record.endTime) : undefined
  form.formData = record.formData || ''
  form.status = record.status || 'PENDING'
  form.remark = record.remark || ''
  editOpen.value = true
}

async function submit() {
  const payload = {
    approvalType: form.approvalType,
    title: form.title,
    applicantName: form.applicantName,
    amount: form.amount,
    startTime: form.startTime ? dayjs(form.startTime).format('YYYY-MM-DDTHH:mm:ss') : undefined,
    endTime: form.endTime ? dayjs(form.endTime).format('YYYY-MM-DDTHH:mm:ss') : undefined,
    formData: form.formData,
    status: form.status,
    remark: form.remark
  }
  saving.value = true
  try {
    if (form.id) {
      await updateApproval(form.id, payload)
    } else {
      await createApproval(payload)
    }
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function approve(record: OaApproval) {
  if (record.status !== 'PENDING') return
  await approveApproval(record.id)
  fetchData()
}

async function reject(record: OaApproval) {
  if (record.status !== 'PENDING') return
  const remark = window.prompt('请输入驳回原因', '未通过')
  if (remark === null) return
  await rejectApproval(record.id, remark)
  fetchData()
}

async function remove(record: OaApproval) {
  await deleteApproval(record.id)
  fetchData()
}

async function batchApprove() {
  if (selectedRowKeys.value.length === 0) return
  const affected = await batchApproveApproval(selectedRowKeys.value)
  message.success(`批量通过完成，共处理 ${affected || 0} 条`)
  fetchData()
}

async function batchReject() {
  if (selectedRowKeys.value.length === 0) return
  const remark = window.prompt('请输入批量驳回原因', '批量未通过')
  if (remark === null) return
  const affected = await batchRejectApproval(selectedRowKeys.value, remark)
  message.success(`批量驳回完成，共处理 ${affected || 0} 条`)
  fetchData()
}

async function batchRemove() {
  if (selectedRowKeys.value.length === 0) return
  const affected = await batchDeleteApproval(selectedRowKeys.value)
  message.success(`批量删除完成，共处理 ${affected || 0} 条`)
  fetchData()
}

async function downloadExport() {
  const blob = await exportApproval({
    keyword: query.keyword || undefined,
    type: query.type,
    status: query.status
  })
  const href = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = href
  link.download = `oa-approval-${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(href)
}

fetchData()

onMounted(() => {
  const type = String(route.query.type || '').toUpperCase()
  const quick = String(route.query.quick || '')
  if (type && typeOptions.some((item) => item.value === type)) {
    query.type = type
    form.approvalType = type
  }
  if (quick === '1') {
    openCreate()
  }
})
</script>
