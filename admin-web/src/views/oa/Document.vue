<template>
  <PageContainer title="文档管理" subTitle="上传记录与目录管理">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="目录">
        <a-input v-model:value="query.folder" placeholder="输入目录" allow-clear />
      </a-form-item>
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="文件名/上传人" allow-clear />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openCreate">新增文档</a-button>
        <a-button :disabled="!selectedSingleRecord" @click="editSelected">编辑</a-button>
        <a-button :disabled="!selectedSingleRecord" danger @click="removeSelected">删除</a-button>
        <a-button :disabled="selectedRowKeys.length === 0" danger @click="batchRemove">批量删除</a-button>
        <a-button @click="downloadExport">导出CSV</a-button>
        <span class="selection-tip">已勾选 {{ selectedRowKeys.length }} 条</span>
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
    />

    <a-modal v-model:open="editOpen" title="文档" @ok="submit" :confirm-loading="saving" width="640px">
      <a-form layout="vertical">
        <a-form-item label="文件名" required>
          <a-input v-model:value="form.name" />
        </a-form-item>
        <a-form-item label="目录">
          <a-input v-model:value="form.folder" />
        </a-form-item>
        <a-form-item label="URL">
          <a-input v-model:value="form.url" />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="大小(B)">
              <a-input-number v-model:value="form.sizeBytes" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="上传人">
              <a-input v-model:value="form.uploaderName" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="备注">
          <a-input v-model:value="form.remark" />
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
  getDocumentPage,
  createDocument,
  updateDocument,
  deleteDocument,
  batchDeleteDocument,
  exportDocument
} from '../../api/oa'
import type { OaDocument, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<OaDocument[]>([])
const query = reactive({ folder: '', keyword: '', pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const selectedRowKeys = ref<string[]>([])

const columns = [
  { title: '文件名', dataIndex: 'name', key: 'name', width: 200 },
  { title: '目录', dataIndex: 'folder', key: 'folder', width: 120 },
  { title: '上传人', dataIndex: 'uploaderName', key: 'uploaderName', width: 120 },
  { title: '上传时间', dataIndex: 'uploadedAt', key: 'uploadedAt', width: 160 },
  { title: '备注', dataIndex: 'remark', key: 'remark' }
]

const editOpen = ref(false)
const saving = ref(false)
const form = reactive({
  id: undefined as string | undefined,
  name: '',
  folder: '',
  url: '',
  sizeBytes: undefined as number | undefined,
  uploaderName: '',
  remark: ''
})
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: (string | number)[]) => {
    selectedRowKeys.value = keys.map((item) => String(item))
  }
}))
const selectedRecords = computed(() => rows.value.filter((item) => selectedRowKeys.value.includes(String(item.id))))
const selectedSingleRecord = computed(() => (selectedRecords.value.length === 1 ? selectedRecords.value[0] : null))

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<OaDocument> = await getDocumentPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      folder: query.folder,
      keyword: query.keyword
    })
    rows.value = (res.list || []).map((item) => ({
      ...item,
      id: String(item.id)
    }))
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
  query.folder = ''
  query.keyword = ''
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openCreate() {
  form.id = undefined
  form.name = ''
  form.folder = ''
  form.url = ''
  form.sizeBytes = undefined
  form.uploaderName = ''
  form.remark = ''
  editOpen.value = true
}

function openEdit(record: OaDocument) {
  form.id = String(record.id)
  form.name = record.name
  form.folder = record.folder || ''
  form.url = record.url || ''
  form.sizeBytes = record.sizeBytes
  form.uploaderName = record.uploaderName || ''
  form.remark = record.remark || ''
  editOpen.value = true
}

async function submit() {
  const payload = {
    name: form.name,
    folder: form.folder,
    url: form.url,
    sizeBytes: form.sizeBytes,
    uploaderName: form.uploaderName,
    remark: form.remark
  }
  saving.value = true
  try {
    if (form.id) {
      await updateDocument(form.id, payload)
    } else {
      await createDocument(payload)
    }
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function remove(record: OaDocument) {
  await deleteDocument(String(record.id))
  fetchData()
}

function requireSingleSelection(action: string) {
  if (!selectedSingleRecord.value) {
    message.info(`请先勾选 1 条文档后再${action}`)
    return null
  }
  return selectedSingleRecord.value
}

function editSelected() {
  const record = requireSingleSelection('编辑')
  if (!record) return
  openEdit(record)
}

async function removeSelected() {
  const record = requireSingleSelection('删除')
  if (!record) return
  await remove(record)
}

async function batchRemove() {
  if (selectedRowKeys.value.length === 0) return
  const affected = await batchDeleteDocument(selectedRowKeys.value)
  message.success(`批量删除，共处理 ${affected || 0} 条`)
  fetchData()
}

async function downloadExport() {
  const blob = await exportDocument({
    folder: query.folder || undefined,
    keyword: query.keyword || undefined
  })
  const href = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = href
  link.download = `oa-document-${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(href)
}

fetchData()
</script>

<style scoped>
.selection-tip {
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}
</style>
