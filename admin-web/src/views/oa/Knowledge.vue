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
        <a-button :disabled="!selectedSingleRecord || !canEditSelected" @click="editSelected">编辑</a-button>
        <a-button :disabled="!selectedSingleRecord || !canPublishSelected" @click="publishSelected">发布</a-button>
        <a-button :disabled="!selectedSingleRecord || !canExpireSelected" @click="expireSelected">设为过期</a-button>
        <a-button :disabled="!selectedSingleRecord || !canArchiveSelected" @click="archiveSelected">归档</a-button>
        <a-button :disabled="!selectedSingleRecord" danger @click="removeSelected">删除</a-button>
        <a-button :disabled="selectedPublishIds.length === 0" @click="batchPublish">批量发布</a-button>
        <a-button :disabled="selectedExpireIds.length === 0" @click="batchExpire">批量过期</a-button>
        <a-button :disabled="selectedArchiveIds.length === 0" @click="batchArchive">批量归档</a-button>
        <a-button :disabled="selectedRowKeys.length === 0" danger @click="batchRemove">批量删除</a-button>
        <a-button @click="downloadExport">导出CSV</a-button>
        <span class="selection-tip">已勾选 {{ selectedRowKeys.length }} 条，批量状态流转按当前状态自动过滤</span>
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
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="上传附件（Word/PDF）">
              <a-upload
                :show-upload-list="false"
                :before-upload="beforeUploadKnowledge"
                accept=".doc,.docx,.pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/pdf"
              >
                <a-button :loading="uploading">上传 Word / PDF</a-button>
              </a-upload>
              <div class="upload-hint">{{ uploadDisplayText }}</div>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="过期时间">
              <a-date-picker
                v-model:value="form.expiredAt"
                show-time
                style="width: 100%"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="可选，状态为已过期时自动补当前时间"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="正文（可选，正文或附件至少一项）">
          <a-textarea v-model:value="form.content" :rows="6" />
        </a-form-item>
        <a-form-item label="附件地址">
          <a-input v-model:value="form.attachmentUrl" placeholder="上传后自动填充，可手动粘贴外链" />
        </a-form-item>
        <a-form-item label="附件名称">
          <a-input v-model:value="form.attachmentName" placeholder="上传后自动带入，可手动修改" />
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
  batchExpireKnowledge,
  batchArchiveKnowledge,
  batchDeleteKnowledge,
  batchPublishKnowledge,
  createKnowledge,
  deleteKnowledge,
  expireKnowledge,
  exportKnowledge,
  getKnowledgePage,
  uploadOaFile,
  publishKnowledge,
  updateKnowledge
} from '../../api/oa'
import type { OaKnowledge, PageResult } from '../../types'

const loading = ref(false)
const saving = ref(false)
const uploading = ref(false)
const rows = ref<OaKnowledge[]>([])
const query = reactive({ keyword: '', category: '', status: undefined as string | undefined, pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const selectedRowKeys = ref<string[]>([])

const columns = [
  { title: '标题', dataIndex: 'title', key: 'title', width: 220 },
  { title: '分类', dataIndex: 'category', key: 'category', width: 120 },
  { title: '标签', dataIndex: 'tags', key: 'tags', width: 160 },
  { title: '正文摘要', dataIndex: 'content', key: 'content', width: 260 },
  { title: '作者', dataIndex: 'authorName', key: 'authorName', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '发布时间', dataIndex: 'publishedAt', key: 'publishedAt', width: 160 }
]

const statusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '已过期', value: 'EXPIRED' },
  { label: '已归档', value: 'ARCHIVED' }
]
const editableStatusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '已过期', value: 'EXPIRED' }
]

const editOpen = ref(false)
const form = reactive<Partial<OaKnowledge>>({ status: 'DRAFT' })
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: (string | number)[]) => {
    selectedRowKeys.value = keys.map((item) => String(item))
  }
}))
const selectedRecords = computed(() => rows.value.filter((item) => selectedRowKeys.value.includes(String(item.id))))
const selectedSingleRecord = computed(() => (selectedRecords.value.length === 1 ? selectedRecords.value[0] : null))
const canEditSelected = computed(() => !!selectedSingleRecord.value && selectedSingleRecord.value.status !== 'ARCHIVED')
const canPublishSelected = computed(() => ['DRAFT', 'EXPIRED'].includes(String(selectedSingleRecord.value?.status || '')))
const canExpireSelected = computed(() => !!selectedSingleRecord.value && !['ARCHIVED', 'EXPIRED'].includes(String(selectedSingleRecord.value?.status || '')))
const canArchiveSelected = computed(() => !!selectedSingleRecord.value && selectedSingleRecord.value.status !== 'ARCHIVED')
const selectedPublishIds = computed(() =>
  selectedRecords.value.filter((item) => ['DRAFT', 'EXPIRED'].includes(String(item.status || ''))).map((item) => String(item.id))
)
const selectedExpireIds = computed(() =>
  selectedRecords.value.filter((item) => !['ARCHIVED', 'EXPIRED'].includes(String(item.status || ''))).map((item) => String(item.id))
)
const selectedArchiveIds = computed(() =>
  selectedRecords.value.filter((item) => item.status !== 'ARCHIVED').map((item) => String(item.id))
)

function statusText(status?: string) {
  if (status === 'PUBLISHED') return '已发布'
  if (status === 'EXPIRED') return '已过期'
  if (status === 'ARCHIVED') return '已归档'
  return '草稿'
}

function statusColor(status?: string) {
  if (status === 'PUBLISHED') return 'green'
  if (status === 'EXPIRED') return 'orange'
  if (status === 'ARCHIVED') return 'blue'
  return 'orange'
}

const uploadDisplayText = computed(() => {
  if (form.attachmentUrl) {
    return `已上传：${form.attachmentName || '附件已上传'}`
  }
  return '支持 doc/docx/pdf，上传后自动填充附件名称和地址'
})

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<OaKnowledge> = await getKnowledgePage(query)
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
  query.keyword = ''
  query.category = ''
  query.status = undefined
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openEdit(record?: OaKnowledge) {
  if (record?.status === 'ARCHIVED') return
  Object.assign(
    form,
    record || {
      status: 'DRAFT',
      title: '',
      category: '',
      tags: '',
      content: '',
      attachmentName: '',
      attachmentUrl: '',
      attachmentType: '',
      attachmentSize: undefined,
      expiredAt: undefined,
      authorName: '',
      remark: ''
    }
  )
  editOpen.value = true
}

async function submit() {
  if (!String(form.content || '').trim() && !String(form.attachmentUrl || '').trim()) {
    message.warning('正文或附件至少填写一项')
    return
  }
  const payload = {
    ...form,
    title: String(form.title || '').trim(),
    category: form.category ? String(form.category).trim() : undefined,
    tags: form.tags ? String(form.tags).trim() : undefined,
    content: form.content != null ? String(form.content) : undefined,
    attachmentName: form.attachmentName ? String(form.attachmentName).trim() : undefined,
    attachmentUrl: form.attachmentUrl ? String(form.attachmentUrl).trim() : undefined,
    attachmentType: form.attachmentType ? String(form.attachmentType).trim() : undefined,
    authorName: form.authorName ? String(form.authorName).trim() : undefined,
    remark: form.remark ? String(form.remark).trim() : undefined
  }
  saving.value = true
  try {
    if (form.id) {
      await updateKnowledge(form.id, payload)
    } else {
      await createKnowledge(payload)
    }
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function remove(record: OaKnowledge) {
  await deleteKnowledge(String(record.id))
  fetchData()
}

async function publish(record: OaKnowledge) {
  if (!['DRAFT', 'EXPIRED'].includes(String(record.status || ''))) return
  await publishKnowledge(String(record.id))
  fetchData()
}

async function expire(record: OaKnowledge) {
  if (['ARCHIVED', 'EXPIRED'].includes(String(record.status || ''))) return
  await expireKnowledge(String(record.id))
  fetchData()
}

async function archive(record: OaKnowledge) {
  if (record.status === 'ARCHIVED') return
  await archiveKnowledge(String(record.id))
  fetchData()
}

function requireSingleSelection(action: string) {
  if (!selectedSingleRecord.value) {
    message.info(`请先勾选 1 条知识文章后再${action}`)
    return null
  }
  return selectedSingleRecord.value
}

function editSelected() {
  const record = requireSingleSelection('编辑')
  if (!record) return
  openEdit(record)
}

async function publishSelected() {
  const record = requireSingleSelection('发布')
  if (!record) return
  await publish(record)
}

async function archiveSelected() {
  const record = requireSingleSelection('归档')
  if (!record) return
  await archive(record)
}

async function expireSelected() {
  const record = requireSingleSelection('设为过期')
  if (!record) return
  await expire(record)
}

async function removeSelected() {
  const record = requireSingleSelection('删除')
  if (!record) return
  await remove(record)
}

async function batchPublish() {
  if (selectedPublishIds.value.length === 0) {
    message.info('勾选项中没有可发布文章')
    return
  }
  const affected = await batchPublishKnowledge(selectedPublishIds.value)
  message.success(`批量发布，共处理 ${affected || 0} 条`)
  fetchData()
}

async function batchArchive() {
  if (selectedArchiveIds.value.length === 0) {
    message.info('勾选项中没有可归档文章')
    return
  }
  const affected = await batchArchiveKnowledge(selectedArchiveIds.value)
  message.success(`批量归档，共处理 ${affected || 0} 条`)
  fetchData()
}

async function batchExpire() {
  if (selectedExpireIds.value.length === 0) {
    message.info('勾选项中没有可设为过期文章')
    return
  }
  const affected = await batchExpireKnowledge(selectedExpireIds.value)
  message.success(`批量设为过期，共处理 ${affected || 0} 条`)
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

async function beforeUploadKnowledge(file: File) {
  const name = file.name.toLowerCase()
  const pass = name.endsWith('.doc') || name.endsWith('.docx') || name.endsWith('.pdf')
  if (!pass) {
    message.warning('仅支持上传 Word/PDF 文件')
    return false
  }
  uploading.value = true
  try {
    const res = await uploadOaFile(file, 'oa-knowledge')
    form.attachmentUrl = res.fileUrl || ''
    form.attachmentName = res.originalFileName || res.fileName || file.name
    form.attachmentType = res.fileType || file.type || undefined
    form.attachmentSize = Number(res.fileSize || file.size || 0)
    if (!form.title) {
      const baseName = String(form.attachmentName || '').replace(/\.[^/.]+$/, '')
      form.title = baseName || form.title
    }
    message.success('附件上传成功')
  } finally {
    uploading.value = false
  }
  return false
}

fetchData()
</script>

<style scoped>
.selection-tip {
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}

.upload-hint {
  margin-top: 8px;
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}
</style>
