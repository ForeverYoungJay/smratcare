<template>
  <PageContainer title="公告管理" subTitle="公告发布与维护">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="标题/内容" allow-clear />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" :options="statusOptions" allow-clear style="width: 160px" />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openCreate">新增公告</a-button>
        <a-button :disabled="selectedRowKeys.length === 0" @click="batchPublish">批量发布</a-button>
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
          <a-tag :color="record.status === 'PUBLISHED' ? 'green' : 'orange'">
            {{ record.status === 'PUBLISHED' ? '已发布' : '草稿' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" :disabled="record.status !== 'DRAFT'" @click="openEdit(record)">编辑</a-button>
            <a-button type="link" :disabled="record.status !== 'DRAFT'" @click="publish(record)">发布</a-button>
            <a-button type="link" danger @click="remove(record)">删除</a-button>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="editOpen" title="公告" @ok="submit" :confirm-loading="saving" width="640px">
      <a-form layout="vertical">
        <a-form-item label="标题" required>
          <a-input v-model:value="form.title" />
        </a-form-item>
        <a-form-item label="内容" required>
          <a-textarea v-model:value="form.content" :rows="4" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status" :options="statusOptions" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import {
  getNoticePage,
  createNotice,
  updateNotice,
  publishNotice,
  deleteNotice,
  batchPublishNotice,
  batchDeleteNotice,
  exportNotice
} from '../../api/oa'
import type { OaNotice, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<OaNotice[]>([])
const query = reactive({ keyword: '', status: undefined as string | undefined, pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const selectedRowKeys = ref<number[]>([])

const columns = [
  { title: '标题', dataIndex: 'title', key: 'title', width: 240 },
  { title: '发布人', dataIndex: 'publisherName', key: 'publisherName', width: 120 },
  { title: '发布时间', dataIndex: 'publishTime', key: 'publishTime', width: 160 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 200 }
]

const editOpen = ref(false)
const saving = ref(false)
const form = reactive({ id: undefined as number | undefined, title: '', content: '', status: 'DRAFT' })
const statusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已发布', value: 'PUBLISHED' }
]
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: (string | number)[]) => {
    selectedRowKeys.value = keys.map((item) => Number(item))
  }
}))

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<OaNotice> = await getNoticePage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword,
      status: query.status
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
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openCreate() {
  form.id = undefined
  form.title = ''
  form.content = ''
  form.status = 'DRAFT'
  editOpen.value = true
}

function openEdit(record: OaNotice) {
  form.id = record.id
  form.title = record.title
  form.content = record.content
  form.status = record.status || 'DRAFT'
  editOpen.value = true
}

async function submit() {
  const payload = { title: form.title, content: form.content, status: form.status }
  saving.value = true
  try {
    if (form.id) {
      await updateNotice(form.id, payload)
    } else {
      await createNotice(payload)
    }
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function publish(record: OaNotice) {
  if (record.status !== 'DRAFT') return
  await publishNotice(record.id)
  fetchData()
}

async function remove(record: OaNotice) {
  await deleteNotice(record.id)
  fetchData()
}

async function batchPublish() {
  if (selectedRowKeys.value.length === 0) return
  const affected = await batchPublishNotice(selectedRowKeys.value)
  message.success(`批量发布完成，共处理 ${affected || 0} 条`)
  fetchData()
}

async function batchRemove() {
  if (selectedRowKeys.value.length === 0) return
  const affected = await batchDeleteNotice(selectedRowKeys.value)
  message.success(`批量删除完成，共处理 ${affected || 0} 条`)
  fetchData()
}

async function downloadExport() {
  const blob = await exportNotice({ keyword: query.keyword || undefined, status: query.status })
  const href = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = href
  link.download = `oa-notice-${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(href)
}

fetchData()
</script>
