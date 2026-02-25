<template>
  <PageContainer title="知识库管理" subTitle="制度规范、护理手册、操作指引管理">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="标题/标签/作者" allow-clear />
      </a-form-item>
      <a-form-item label="分类">
        <a-input v-model:value="query.category" placeholder="如：护理标准" allow-clear />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 160px" :options="statusOptions" />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openEdit()">新增文章</a-button>
        <a-button :disabled="selectedRowKeys.length === 0" @click="batchPublish">批量发布</a-button>
        <a-button :disabled="selectedRowKeys.length === 0" @click="batchArchive">批量归档</a-button>
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
          <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'content'">
          <span>{{ (record.content || '').slice(0, 40) }}{{ (record.content || '').length > 40 ? '...' : '' }}</span>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a :style="{ color: record.status === 'ARCHIVED' ? '#999' : '' }" @click="openEdit(record)">编辑</a>
            <a :style="{ color: record.status !== 'DRAFT' ? '#999' : '' }" @click="publish(record)">发布</a>
            <a :style="{ color: record.status === 'ARCHIVED' ? '#999' : '' }" @click="archive(record)">归档</a>
            <a @click="remove(record)" style="color: #ff4d4f">删除</a>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="editOpen" :title="form.id ? '编辑文章' : '新增文章'" @ok="submit" :confirm-loading="saving" width="760px">
      <a-form layout="vertical">
        <a-form-item label="标题" required>
          <a-input v-model:value="form.title" />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="分类">
              <a-input v-model:value="form.category" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="标签">
              <a-input v-model:value="form.tags" placeholder="多个标签请用逗号分隔" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="作者">
              <a-input v-model:value="form.authorName" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="状态">
              <a-select v-model:value="form.status" :options="editableStatusOptions" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="正文" required>
          <a-textarea v-model:value="form.content" :rows="8" />
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="form.remark" :rows="2" />
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
  archiveKnowledge,
  batchArchiveKnowledge,
  batchDeleteKnowledge,
  batchPublishKnowledge,
  createKnowledge,
  deleteKnowledge,
  exportKnowledge,
  getKnowledgePage,
  publishKnowledge,
  updateKnowledge
} from '../../api/oa'
import type { OaKnowledge, PageResult } from '../../types'

const loading = ref(false)
const saving = ref(false)
const rows = ref<OaKnowledge[]>([])
const query = reactive({ keyword: '', category: '', status: undefined as string | undefined, pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const selectedRowKeys = ref<number[]>([])

const columns = [
  { title: '标题', dataIndex: 'title', key: 'title', width: 220 },
  { title: '分类', dataIndex: 'category', key: 'category', width: 120 },
  { title: '标签', dataIndex: 'tags', key: 'tags', width: 160 },
  { title: '正文摘要', dataIndex: 'content', key: 'content', width: 260 },
  { title: '作者', dataIndex: 'authorName', key: 'authorName', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '发布时间', dataIndex: 'publishedAt', key: 'publishedAt', width: 160 },
  { title: '操作', key: 'action', width: 140 }
]

const statusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '已归档', value: 'ARCHIVED' }
]
const editableStatusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已发布', value: 'PUBLISHED' }
]

const editOpen = ref(false)
const form = reactive<Partial<OaKnowledge>>({ status: 'DRAFT' })
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: (string | number)[]) => {
    selectedRowKeys.value = keys.map((item) => Number(item))
  }
}))

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
    const res: PageResult<OaKnowledge> = await getKnowledgePage(query)
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
  query.category = ''
  query.status = undefined
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openEdit(record?: OaKnowledge) {
  if (record?.status === 'ARCHIVED') return
  Object.assign(form, record || { status: 'DRAFT', title: '', category: '', tags: '', content: '', authorName: '', remark: '' })
  editOpen.value = true
}

async function submit() {
  saving.value = true
  try {
    if (form.id) {
      await updateKnowledge(form.id, form)
    } else {
      await createKnowledge(form)
    }
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function remove(record: OaKnowledge) {
  await deleteKnowledge(record.id)
  fetchData()
}

async function publish(record: OaKnowledge) {
  if (record.status !== 'DRAFT') return
  await publishKnowledge(record.id)
  fetchData()
}

async function archive(record: OaKnowledge) {
  if (record.status === 'ARCHIVED') return
  await archiveKnowledge(record.id)
  fetchData()
}

async function batchPublish() {
  if (selectedRowKeys.value.length === 0) return
  const affected = await batchPublishKnowledge(selectedRowKeys.value)
  message.success(`批量发布，共处理 ${affected || 0} 条`)
  fetchData()
}

async function batchArchive() {
  if (selectedRowKeys.value.length === 0) return
  const affected = await batchArchiveKnowledge(selectedRowKeys.value)
  message.success(`批量归档，共处理 ${affected || 0} 条`)
  fetchData()
}

async function batchRemove() {
  if (selectedRowKeys.value.length === 0) return
  const affected = await batchDeleteKnowledge(selectedRowKeys.value)
  message.success(`批量删除，共处理 ${affected || 0} 条`)
  fetchData()
}

async function downloadExport() {
  const blob = await exportKnowledge({
    keyword: query.keyword || undefined,
    category: query.category || undefined,
    status: query.status
  })
  const href = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = href
  link.download = `oa-knowledge-${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(href)
}

fetchData()
</script>
