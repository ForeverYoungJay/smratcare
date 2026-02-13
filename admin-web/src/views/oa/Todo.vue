<template>
  <PageContainer title="待办事项" subTitle="个人待办管理">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="状态">
        <a-select v-model:value="query.status" :options="statusOptions" allow-clear style="width: 160px" />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openCreate">新增待办</a-button>
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
          <a-tag :color="record.status === 'DONE' ? 'green' : 'orange'">
            {{ record.status === 'DONE' ? '已完成' : '待处理' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openEdit(record)">编辑</a-button>
            <a-button type="link" @click="done(record)">完成</a-button>
            <a-button type="link" danger @click="remove(record)">删除</a-button>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="editOpen" title="待办" @ok="submit" :confirm-loading="saving" width="600px">
      <a-form layout="vertical">
        <a-form-item label="标题" required>
          <a-input v-model:value="form.title" />
        </a-form-item>
        <a-form-item label="内容">
          <a-textarea v-model:value="form.content" :rows="3" />
        </a-form-item>
        <a-form-item label="截止时间">
          <a-date-picker v-model:value="form.dueTime" show-time style="width: 100%" />
        </a-form-item>
        <a-form-item label="负责人">
          <a-input v-model:value="form.assigneeName" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status" :options="statusOptions" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import dayjs from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { getTodoPage, createTodo, updateTodo, completeTodo, deleteTodo } from '../../api/oa'
import type { OaTodo, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<OaTodo[]>([])
const query = reactive({ status: undefined as string | undefined, pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '标题', dataIndex: 'title', key: 'title', width: 200 },
  { title: '负责人', dataIndex: 'assigneeName', key: 'assigneeName', width: 120 },
  { title: '截止时间', dataIndex: 'dueTime', key: 'dueTime', width: 160 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 180 }
]

const editOpen = ref(false)
const saving = ref(false)
const form = reactive({
  id: undefined as number | undefined,
  title: '',
  content: '',
  dueTime: undefined as any,
  assigneeName: '',
  status: 'OPEN'
})

const statusOptions = [
  { label: '待处理', value: 'OPEN' },
  { label: '已完成', value: 'DONE' }
]

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<OaTodo> = await getTodoPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      status: query.status
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
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openCreate() {
  form.id = undefined
  form.title = ''
  form.content = ''
  form.dueTime = undefined
  form.assigneeName = ''
  form.status = 'OPEN'
  editOpen.value = true
}

function openEdit(record: OaTodo) {
  form.id = record.id
  form.title = record.title
  form.content = record.content || ''
  form.dueTime = record.dueTime ? dayjs(record.dueTime) : undefined
  form.assigneeName = record.assigneeName || ''
  form.status = record.status || 'OPEN'
  editOpen.value = true
}

async function submit() {
  const payload = {
    title: form.title,
    content: form.content,
    dueTime: form.dueTime ? dayjs(form.dueTime).format('YYYY-MM-DDTHH:mm:ss') : undefined,
    assigneeName: form.assigneeName,
    status: form.status
  }
  saving.value = true
  try {
    if (form.id) {
      await updateTodo(form.id, payload)
    } else {
      await createTodo(payload)
    }
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function done(record: OaTodo) {
  await completeTodo(record.id)
  fetchData()
}

async function remove(record: OaTodo) {
  await deleteTodo(record.id)
  fetchData()
}

fetchData()
</script>
