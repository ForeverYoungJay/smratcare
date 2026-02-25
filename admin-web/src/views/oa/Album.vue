<template>
  <PageContainer title="相册管理" subTitle="活动照片与影像资料管理">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="相册标题/备注" allow-clear />
      </a-form-item>
      <a-form-item label="分类">
        <a-input v-model:value="query.category" placeholder="如：节日活动" allow-clear />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 160px" :options="statusOptions" />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openEdit()">新增相册</a-button>
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
        <template v-if="column.key === 'coverUrl'">
          <a-image v-if="record.coverUrl" :src="record.coverUrl" :width="48" :height="48" style="object-fit: cover; border-radius: 6px;" />
          <span v-else>-</span>
        </template>
        <template v-else-if="column.key === 'status'">
          <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a @click="openEdit(record)">编辑</a>
            <a @click="remove(record)" style="color: #ff4d4f">删除</a>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="editOpen" :title="form.id ? '编辑相册' : '新增相册'" @ok="submit" :confirm-loading="saving" width="640px">
      <a-form layout="vertical">
        <a-form-item label="相册标题" required>
          <a-input v-model:value="form.title" />
        </a-form-item>
        <a-form-item label="分类">
          <a-input v-model:value="form.category" />
        </a-form-item>
        <a-form-item label="封面地址">
          <a-input v-model:value="form.coverUrl" />
        </a-form-item>
        <a-form-item label="照片数量">
          <a-input-number v-model:value="form.photoCount" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="拍摄日期">
          <a-date-picker v-model:value="shootDate" style="width: 100%" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status" :options="statusOptions" />
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="form.remark" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import type { Dayjs } from 'dayjs'
import dayjs from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { createAlbum, deleteAlbum, getAlbumPage, updateAlbum } from '../../api/oa'
import type { OaAlbum, PageResult } from '../../types'

const loading = ref(false)
const saving = ref(false)
const rows = ref<OaAlbum[]>([])
const query = reactive({ keyword: '', category: '', status: undefined as string | undefined, pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '封面', dataIndex: 'coverUrl', key: 'coverUrl', width: 90 },
  { title: '相册标题', dataIndex: 'title', key: 'title', width: 220 },
  { title: '分类', dataIndex: 'category', key: 'category', width: 140 },
  { title: '照片数量', dataIndex: 'photoCount', key: 'photoCount', width: 100 },
  { title: '拍摄日期', dataIndex: 'shootDate', key: 'shootDate', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 140 }
]

const statusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '已归档', value: 'ARCHIVED' }
]

const editOpen = ref(false)
const shootDate = ref<Dayjs | undefined>()
const form = reactive<Partial<OaAlbum>>({ status: 'DRAFT', photoCount: 0 })

function statusText(status?: string) {
  if (status === 'PUBLISHED') return '已发布'
  if (status === 'ARCHIVED') return '已归档'
  return '草稿'
}

function statusColor(status?: string) {
  if (status === 'PUBLISHED') return 'green'
  if (status === 'ARCHIVED') return 'blue'
  return 'orange'
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<OaAlbum> = await getAlbumPage(query)
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
  query.category = ''
  query.status = undefined
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openEdit(record?: OaAlbum) {
  Object.assign(form, record || { status: 'DRAFT', photoCount: 0, title: '', category: '', coverUrl: '', remark: '' })
  shootDate.value = form.shootDate ? dayjs(form.shootDate) : undefined
  editOpen.value = true
}

async function submit() {
  saving.value = true
  try {
    const payload = {
      ...form,
      shootDate: shootDate.value ? shootDate.value.format('YYYY-MM-DD') : undefined
    }
    if (form.id) {
      await updateAlbum(form.id, payload)
    } else {
      await createAlbum(payload)
    }
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function remove(record: OaAlbum) {
  await deleteAlbum(record.id)
  fetchData()
}

fetchData()
</script>
