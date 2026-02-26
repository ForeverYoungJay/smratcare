<template>
  <PageContainer title="交接日志" subTitle="在岗工作日志、注意事项与待办追踪">
    <a-card :bordered="false" class="card-elevated">
      <a-form layout="inline" @submit.prevent>
        <a-form-item label="日期范围">
          <a-range-picker v-model:value="query.range" value-format="YYYY-MM-DD" />
        </a-form-item>
        <a-form-item label="班次编码">
          <a-input v-model:value="query.shiftCode" placeholder="如 MORNING" allow-clear />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.status" :options="statusOptions" allow-clear style="width: 140px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="search">查询</a-button>
            <a-button @click="reset">重置</a-button>
            <a-button type="primary" @click="openModal()">新增交接日志</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <a-table
        row-key="id"
        :columns="columns"
        :data-source="rows"
        :loading="loading"
        :pagination="pagination"
        @change="onTableChange"
      >
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

    <a-modal v-model:open="modalOpen" title="交接日志" :confirm-loading="submitting" width="760" @ok="submit">
      <a-form ref="formRef" layout="vertical" :model="form" :rules="rules">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="班次日期" name="dutyDate" required>
              <a-date-picker v-model:value="form.dutyDate" value-format="YYYY-MM-DD" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="班次编码" name="shiftCode" required>
              <a-input v-model:value="form.shiftCode" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="交班人" name="fromStaffId" required>
              <a-select
                v-model:value="form.fromStaffId"
                show-search
                :filter-option="false"
                placeholder="输入姓名搜索"
                :options="staffOptions"
                @search="searchStaff"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="接班人" name="toStaffId" required>
              <a-select
                v-model:value="form.toStaffId"
                show-search
                :filter-option="false"
                placeholder="输入姓名搜索"
                :options="staffOptions"
                @search="searchStaff"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="在岗期间做了什么" name="onDutySummary">
          <a-textarea v-model:value="form.onDutySummary" :rows="3" />
        </a-form-item>
        <a-form-item label="注意事项" name="attentionNote">
          <a-textarea v-model:value="form.attentionNote" :rows="2" />
        </a-form-item>
        <a-form-item label="待办事项" name="todoNote">
          <a-textarea v-model:value="form.todoNote" :rows="2" />
        </a-form-item>

        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="状态" name="status">
              <a-select v-model:value="form.status" :options="statusOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="交班时间" name="handoverTime">
              <a-date-picker v-model:value="form.handoverTime" show-time value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="确认时间" name="confirmTime">
              <a-date-picker v-model:value="form.confirmTime" show-time value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
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
import { getStaffPage } from '../../api/rbac'
import { createShiftHandover, deleteShiftHandover, getShiftHandoverPage, updateShiftHandover } from '../../api/nursing'
import type { PageResult, ShiftHandoverItem, StaffItem } from '../../types'

const rows = ref<ShiftHandoverItem[]>([])
const loading = ref(false)
const modalOpen = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const staffOptions = ref<Array<{ label: string; value: number }>>([])

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  total: 0,
  range: undefined as string[] | undefined,
  shiftCode: undefined as string | undefined,
  status: undefined as string | undefined
})

const form = reactive<Partial<ShiftHandoverItem>>({
  dutyDate: '',
  shiftCode: '',
  fromStaffId: undefined,
  toStaffId: undefined,
  summary: '',
  onDutySummary: '',
  riskNote: '',
  attentionNote: '',
  todoNote: '',
  status: 'DRAFT',
  handoverTime: undefined,
  confirmTime: undefined
})

const statusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已交接', value: 'HANDED_OVER' },
  { label: '已确认', value: 'CONFIRMED' }
]

const rules = {
  dutyDate: [{ required: true, message: '请选择班次日期', trigger: 'change' }],
  shiftCode: [{ required: true, message: '请输入班次编码', trigger: 'blur' }],
  fromStaffId: [{ required: true, message: '请选择交班人', trigger: 'change' }],
  toStaffId: [{ required: true, message: '请选择接班人', trigger: 'change' }]
}

const columns = [
  { title: '班次日期', dataIndex: 'dutyDate', key: 'dutyDate', width: 120 },
  { title: '班次编码', dataIndex: 'shiftCode', key: 'shiftCode', width: 120 },
  { title: '交班人', dataIndex: 'fromStaffName', key: 'fromStaffName', width: 120 },
  { title: '接班人', dataIndex: 'toStaffName', key: 'toStaffName', width: 120 },
  { title: '状态', key: 'status', width: 110 },
  { title: '注意事项', dataIndex: 'attentionNote', key: 'attentionNote' },
  { title: '操作', key: 'actions', width: 140 }
]

const pagination = computed(() => ({
  current: query.pageNo,
  pageSize: query.pageSize,
  total: query.total,
  showSizeChanger: true
}))

function statusLabel(status?: string) {
  if (status === 'HANDED_OVER') {
    return '已交接'
  }
  if (status === 'CONFIRMED') {
    return '已确认'
  }
  return '草稿'
}

function statusColor(status?: string) {
  if (status === 'HANDED_OVER') {
    return 'processing'
  }
  if (status === 'CONFIRMED') {
    return 'green'
  }
  return 'default'
}

function resetForm() {
  form.id = undefined
  form.dutyDate = ''
  form.shiftCode = ''
  form.fromStaffId = undefined
  form.toStaffId = undefined
  form.summary = ''
  form.riskNote = ''
  form.onDutySummary = ''
  form.attentionNote = ''
  form.todoNote = ''
  form.status = 'DRAFT'
  form.handoverTime = undefined
  form.confirmTime = undefined
}

function openModal(record?: ShiftHandoverItem) {
  resetForm()
  if (record) {
    Object.assign(form, record)
  }
  modalOpen.value = true
}

async function loadStaffOptions(keyword?: string) {
  const res: PageResult<StaffItem> = await getStaffPage({ pageNo: 1, pageSize: 50, keyword })
  staffOptions.value = res.list.map((item) => ({ label: item.realName || item.username, value: item.id }))
}

async function searchStaff(keyword: string) {
  await loadStaffOptions(keyword)
}

async function submit() {
  try {
    await formRef.value?.validate()
    submitting.value = true
    const payload: Partial<ShiftHandoverItem> = {
      dutyDate: form.dutyDate,
      shiftCode: form.shiftCode,
      fromStaffId: form.fromStaffId,
      toStaffId: form.toStaffId,
      summary: form.onDutySummary || form.summary,
      riskNote: form.attentionNote || form.riskNote,
      todoNote: form.todoNote,
      status: form.status,
      handoverTime: form.handoverTime,
      confirmTime: form.confirmTime
    }
    if (form.id) {
      await updateShiftHandover(form.id, payload)
    } else {
      await createShiftHandover(payload)
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
    title: '确认删除该交接日志？',
    onOk: async () => {
      await deleteShiftHandover(id)
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
  query.shiftCode = undefined
  query.status = undefined
  query.pageNo = 1
  load()
}

async function load() {
  loading.value = true
  try {
    const res: PageResult<ShiftHandoverItem> = await getShiftHandoverPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      dateFrom: query.range?.[0],
      dateTo: query.range?.[1],
      shiftCode: query.shiftCode,
      status: query.status
    })
    rows.value = res.list.map((item) => ({
      ...item,
      onDutySummary: item.onDutySummary || item.summary,
      attentionNote: item.attentionNote || item.riskNote
    }))
    query.total = res.total
  } finally {
    loading.value = false
  }
}

load()
loadStaffOptions()
</script>

<style scoped>
.danger-text {
  color: #ef4444;
}
</style>
