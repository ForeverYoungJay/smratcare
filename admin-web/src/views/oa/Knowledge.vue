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
          <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'content'">
          <span>{{ (record.content || '').slice(0, 40) }}{{ (record.content || '').length > 40 ? '...' : '' }}</span>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a @click="openEdit(record)">编辑</a>
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
              <a-select v-model:value="form.status" :options="statusOptions" />
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
import { reactive, ref } from 'vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { createKnowledge, deleteKnowledge, getKnowledgePage, updateKnowledge } from '../../api/oa'
import type { OaKnowledge, PageResult } from '../../types'

const loading = ref(false)
const saving = ref(false)
const rows = ref<OaKnowledge[]>([])
const query = reactive({ keyword: '', category: '', status: undefined as string | undefined, pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

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

const editOpen = ref(false)
const form = reactive<Partial<OaKnowledge>>({ status: 'DRAFT' })

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

fetchData()
</script>
