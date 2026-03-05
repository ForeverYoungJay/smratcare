<template>
  <PageContainer title="文档管理" subTitle="文档库档案夹 + Word/PDF 上传">
    <a-row :gutter="16">
      <a-col :xs="24" :md="6">
        <a-card class="card-elevated folder-card" :bordered="false">
          <div class="folder-header">
            <div class="folder-title">文档库档案夹</div>
            <a-space size="small">
              <a-button size="small" @click="fetchFolderTree">刷新</a-button>
              <a-button size="small" type="primary" @click="openFolderCreate">新增</a-button>
            </a-space>
          </div>
          <a-tree
            :tree-data="folderTreeData"
            :selected-keys="[selectedFolderKey]"
            :field-names="{ title: 'title', key: 'key', children: 'children' }"
            show-line
            default-expand-all
            @select="onFolderSelect"
          />
          <div class="folder-actions">
            <a-space wrap>
              <a-button size="small" :disabled="!selectedFolderRecord" @click="openFolderEdit">重命名</a-button>
              <a-button size="small" danger :disabled="!selectedFolderRecord" @click="removeFolder">删除</a-button>
            </a-space>
          </div>
        </a-card>
      </a-col>

      <a-col :xs="24" :md="18">
        <SearchForm :model="query" @search="fetchData" @reset="onReset">
          <a-form-item label="关键词">
            <a-input v-model:value="query.keyword" placeholder="文件名/上传人" allow-clear />
          </a-form-item>
          <a-form-item label="当前档案夹">
            <a-tag color="blue">{{ selectedFolderName }}</a-tag>
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
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'name'">
              <a-button
                type="link"
                class="name-link"
                :disabled="!record.url"
                @click="openDocument(record.url)"
              >
                {{ record.name }}
              </a-button>
            </template>
            <template v-else-if="column.key === 'sizeBytes'">
              {{ formatSize(record.sizeBytes) }}
            </template>
            <template v-else-if="column.key === 'folder'">
              {{ record.folder || '未归档' }}
            </template>
            <template v-else>
              {{ record[column.dataIndex] || '-' }}
            </template>
          </template>
        </DataTable>
      </a-col>
    </a-row>

    <a-modal v-model:open="editOpen" title="文档" @ok="submit" :confirm-loading="saving" width="680px">
      <a-form layout="vertical">
        <a-form-item label="上传文件（Word/PDF）">
          <a-upload
            :show-upload-list="false"
            :before-upload="beforeUploadDocument"
            accept=".doc,.docx,.pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/pdf"
          >
            <a-button :loading="uploading">上传 Word / PDF</a-button>
          </a-upload>
          <div class="upload-hint">
            {{ uploadDisplayText }}
          </div>
        </a-form-item>
        <a-form-item label="文件名" required>
          <a-input v-model:value="form.name" placeholder="上传后自动带入，可手动修改" />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="档案夹">
              <a-tree-select
                v-model:value="form.folderId"
                :tree-data="folderSelectData"
                :field-names="{ label: 'title', value: 'value', children: 'children' }"
                allow-clear
                tree-default-expand-all
                placeholder="选择档案夹"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="上传人">
              <a-input v-model:value="form.uploaderName" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="文件地址">
          <a-input v-model:value="form.url" placeholder="上传后自动填充，可粘贴外链" />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="大小">
              <a-input-number v-model:value="form.sizeBytes" style="width: 100%" :min="0" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="备注">
              <a-input v-model:value="form.remark" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="folderEditOpen"
      :title="folderForm.id ? '编辑档案夹' : '新增档案夹'"
      @ok="submitFolder"
      :confirm-loading="folderSaving"
      width="520px"
    >
      <a-form layout="vertical">
        <a-form-item label="档案夹名称" required>
          <a-input v-model:value="folderForm.name" placeholder="例如：护理部/合同资料/评估报告" />
        </a-form-item>
        <a-form-item label="上级档案夹">
          <a-tree-select
            v-model:value="folderForm.parentId"
            :tree-data="folderParentSelectData"
            :field-names="{ label: 'title', value: 'value', children: 'children' }"
            allow-clear
            tree-default-expand-all
            placeholder="不选表示根目录"
          />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="排序">
              <a-input-number v-model:value="folderForm.sortNo" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="状态">
              <a-select v-model:value="folderForm.status">
                <a-select-option value="ENABLED">启用</a-select-option>
                <a-select-option value="DISABLED">停用</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="备注">
          <a-input v-model:value="folderForm.remark" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import {
  getDocumentPage,
  createDocument,
  updateDocument,
  deleteDocument,
  batchDeleteDocument,
  exportDocument,
  uploadOaFile,
  getDocumentFolderTree,
  createDocumentFolder,
  updateDocumentFolder,
  deleteDocumentFolder
} from '../../api/oa'
import type { OaDocument, OaDocumentFolder, PageResult } from '../../types'

const ROOT_KEY = 'ROOT'

const loading = ref(false)
const rows = ref<OaDocument[]>([])
const folders = ref<OaDocumentFolder[]>([])
const selectedFolderKey = ref<string>(ROOT_KEY)

const query = reactive({ keyword: '', pageNo: 1, pageSize: 10, folderId: '' })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const selectedRowKeys = ref<string[]>([])

const columns = [
  { title: '文件名', dataIndex: 'name', key: 'name', width: 260 },
  { title: '档案夹', dataIndex: 'folder', key: 'folder', width: 160 },
  { title: '大小', dataIndex: 'sizeBytes', key: 'sizeBytes', width: 120 },
  { title: '上传人', dataIndex: 'uploaderName', key: 'uploaderName', width: 120 },
  { title: '上传时间', dataIndex: 'uploadedAt', key: 'uploadedAt', width: 170 },
  { title: '备注', dataIndex: 'remark', key: 'remark' }
]

const editOpen = ref(false)
const saving = ref(false)
const uploading = ref(false)
const form = reactive({
  id: undefined as string | undefined,
  name: '',
  folderId: undefined as string | undefined,
  url: '',
  sizeBytes: undefined as number | undefined,
  uploaderName: '',
  remark: ''
})

const folderEditOpen = ref(false)
const folderSaving = ref(false)
const folderForm = reactive({
  id: undefined as string | undefined,
  name: '',
  parentId: undefined as string | undefined,
  sortNo: 0,
  status: 'ENABLED',
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

const folderNodeMap = computed(() => {
  const map = new Map<string, OaDocumentFolder>()
  const traverse = (nodes: OaDocumentFolder[]) => {
    nodes.forEach((node) => {
      map.set(String(node.id), node)
      if (node.children?.length) {
        traverse(node.children)
      }
    })
  }
  traverse(folders.value)
  return map
})

const selectedFolderRecord = computed(() => {
  if (selectedFolderKey.value === ROOT_KEY) {
    return null
  }
  return folderNodeMap.value.get(selectedFolderKey.value) || null
})

const selectedFolderName = computed(() => {
  if (selectedFolderKey.value === ROOT_KEY) {
    return '全部文档'
  }
  return selectedFolderRecord.value?.name || '全部文档'
})

const folderTreeData = computed(() => [
  {
    key: ROOT_KEY,
    title: '全部文档',
    children: transformFolderTreeToUi(folders.value)
  }
])

const folderSelectData = computed(() => transformFolderTreeToSelect(folders.value))

const folderParentSelectData = computed(() => transformFolderTreeToSelect(folders.value, folderForm.id))

const uploadDisplayText = computed(() => {
  if (form.url) {
    return `已上传：${form.name || '已自动识别文件名'}`
  }
  return '支持 doc/docx/pdf，上传后自动填充文件名、地址、大小'
})

async function fetchFolderTree() {
  const data = await getDocumentFolderTree()
  folders.value = normalizeFolderTree(data || [])
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<OaDocument> = await getDocumentPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      folderId: query.folderId || undefined,
      keyword: query.keyword || undefined
    })
    rows.value = (res.list || []).map((item) => ({
      ...item,
      id: String(item.id),
      folderId: item.folderId ? String(item.folderId) : undefined
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
  query.keyword = ''
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function onFolderSelect(keys: (string | number)[]) {
  const key = String(keys?.[0] || ROOT_KEY)
  selectedFolderKey.value = key
  query.folderId = key === ROOT_KEY ? '' : key
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openCreate() {
  form.id = undefined
  form.name = ''
  form.folderId = selectedFolderKey.value === ROOT_KEY ? undefined : selectedFolderKey.value
  form.url = ''
  form.sizeBytes = undefined
  form.uploaderName = ''
  form.remark = ''
  editOpen.value = true
}

function openEdit(record: OaDocument) {
  form.id = String(record.id)
  form.name = record.name || ''
  form.folderId = record.folderId ? String(record.folderId) : undefined
  form.url = record.url || ''
  form.sizeBytes = record.sizeBytes
  form.uploaderName = record.uploaderName || ''
  form.remark = record.remark || ''
  editOpen.value = true
}

async function submit() {
  if (!form.name?.trim() && !form.url?.trim()) {
    message.warning('请先上传文档，或至少填写文件名/文件地址')
    return
  }
  const selectedFolder = form.folderId ? folderNodeMap.value.get(String(form.folderId)) : null
  const payload = {
    name: form.name,
    folderId: form.folderId ? Number(form.folderId) : undefined,
    folder: selectedFolder?.name,
    url: form.url,
    sizeBytes: form.sizeBytes,
    uploaderName: form.uploaderName,
    remark: form.remark
  }
  saving.value = true
  try {
    if (form.id) {
      await updateDocument(form.id, payload)
      message.success('文档更新成功')
    } else {
      await createDocument(payload)
      message.success('文档创建成功')
    }
    editOpen.value = false
    await fetchData()
  } finally {
    saving.value = false
  }
}

async function beforeUploadDocument(file: File) {
  const name = file.name.toLowerCase()
  const pass = name.endsWith('.doc') || name.endsWith('.docx') || name.endsWith('.pdf')
  if (!pass) {
    message.warning('仅支持上传 Word/PDF 文件')
    return false
  }
  uploading.value = true
  try {
    const res = await uploadOaFile(file, 'oa-document')
    form.url = res.fileUrl || ''
    form.name = form.name || res.originalFileName || res.fileName || file.name
    form.sizeBytes = Number(res.fileSize || file.size || 0)
    message.success('上传成功')
  } finally {
    uploading.value = false
  }
  return false
}

async function remove(record: OaDocument) {
  await deleteDocument(String(record.id))
  message.success('删除成功')
  await fetchData()
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
  await fetchData()
}

async function downloadExport() {
  const blob = await exportDocument({
    folderId: query.folderId || undefined,
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

function openDocument(url?: string) {
  if (!url) {
    message.info('当前文档未配置地址')
    return
  }
  window.open(url, '_blank')
}

function openFolderCreate() {
  folderForm.id = undefined
  folderForm.name = ''
  folderForm.parentId = selectedFolderKey.value === ROOT_KEY ? undefined : selectedFolderKey.value
  folderForm.sortNo = 0
  folderForm.status = 'ENABLED'
  folderForm.remark = ''
  folderEditOpen.value = true
}

function openFolderEdit() {
  const folder = selectedFolderRecord.value
  if (!folder) {
    message.info('请先选择一个档案夹')
    return
  }
  folderForm.id = String(folder.id)
  folderForm.name = folder.name
  folderForm.parentId = folder.parentId ? String(folder.parentId) : undefined
  folderForm.sortNo = folder.sortNo || 0
  folderForm.status = folder.status || 'ENABLED'
  folderForm.remark = folder.remark || ''
  folderEditOpen.value = true
}

async function submitFolder() {
  if (!folderForm.name?.trim()) {
    message.warning('请输入档案夹名称')
    return
  }
  const payload = {
    name: folderForm.name.trim(),
    parentId: folderForm.parentId ? Number(folderForm.parentId) : undefined,
    sortNo: folderForm.sortNo,
    status: folderForm.status,
    remark: folderForm.remark
  }
  folderSaving.value = true
  try {
    if (folderForm.id) {
      await updateDocumentFolder(folderForm.id, payload)
      message.success('档案夹更新成功')
    } else {
      await createDocumentFolder(payload)
      message.success('档案夹创建成功')
    }
    folderEditOpen.value = false
    await fetchFolderTree()
    await fetchData()
  } finally {
    folderSaving.value = false
  }
}

function removeFolder() {
  const folder = selectedFolderRecord.value
  if (!folder) {
    message.info('请先选择一个档案夹')
    return
  }
  Modal.confirm({
    title: '确认删除档案夹？',
    content: `删除后不可恢复：${folder.name}`,
    onOk: async () => {
      await deleteDocumentFolder(String(folder.id))
      message.success('档案夹删除成功')
      selectedFolderKey.value = ROOT_KEY
      query.folderId = ''
      await fetchFolderTree()
      await fetchData()
    }
  })
}

function normalizeFolderTree(nodes: OaDocumentFolder[]): OaDocumentFolder[] {
  return (nodes || []).map((item) => ({
    ...item,
    id: String(item.id),
    parentId: item.parentId ? String(item.parentId) : undefined,
    children: normalizeFolderTree(item.children || [])
  }))
}

function transformFolderTreeToUi(nodes: OaDocumentFolder[]) {
  return (nodes || []).map((item) => ({
    key: String(item.id),
    title: `${item.name}${item.documentCount ? ` (${item.documentCount})` : ''}`,
    children: transformFolderTreeToUi(item.children || [])
  }))
}

function transformFolderTreeToSelect(nodes: OaDocumentFolder[], excludeId?: string) {
  return (nodes || [])
    .filter((item) => String(item.id) !== String(excludeId || ''))
    .map((item) => ({
      title: item.name,
      value: String(item.id),
      children: transformFolderTreeToSelect(item.children || [], excludeId)
    }))
}

function formatSize(size?: number) {
  if (!size || size <= 0) return '-'
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / (1024 * 1024)).toFixed(1)} MB`
}

Promise.all([fetchFolderTree(), fetchData()])
</script>

<style scoped>
.folder-card {
  min-height: 680px;
}

.folder-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.folder-title {
  font-weight: 600;
}

.folder-actions {
  margin-top: 12px;
}

.selection-tip {
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}

.upload-hint {
  margin-top: 8px;
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}

.name-link {
  padding-inline: 0;
}
</style>
