<template>
  <PageContainer title="公告管理" subTitle="公告发布与维护">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="标题/内容" allow-clear />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openCreate">新增公告</a-button>
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
          <a-tag :color="record.status === 'PUBLISHED' ? 'green' : 'orange'">
            {{ record.status === 'PUBLISHED' ? '已发布' : '草稿' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openEdit(record)">编辑</a-button>
            <a-button type="link" @click="publish(record)">发布</a-button>
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
import { reactive, ref } from 'vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { getNoticePage, createNotice, updateNotice, publishNotice, deleteNotice } from '../../api/oa'
import type { OaNotice, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<OaNotice[]>([])
const query = reactive({ keyword: '', pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

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

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<OaNotice> = await getNoticePage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword
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
  query.keyword = ''
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
  await publishNotice(record.id)
  fetchData()
}

async function remove(record: OaNotice) {
  await deleteNotice(record.id)
  fetchData()
}

fetchData()
</script>
