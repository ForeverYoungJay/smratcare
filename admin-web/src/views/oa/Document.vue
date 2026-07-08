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
          <a-form-item label="文件夹可见性">
            <a-select v-model:value="query.folderVisibility" allow-clear style="width: 140px">
              <a-select-option value="PUBLIC">公开</a-select-option>
              <a-select-option value="PRIVATE">私有</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="区域">
            <a-input v-model:value="query.regionCode" placeholder="如：A区/护理部" allow-clear style="width: 160px" />
          </a-form-item>
          <a-form-item label="当前档案夹">
            <a-tag color="blue">{{ selectedFolderName }}</a-tag>
          </a-form-item>
          <template #extra>
            <a-button type="primary" @click="openCreate">新增文档</a-button>
            <a-button :disabled="!selectedSingleRecord" @click="editSelected">编辑</a-button>
            <a-button @click="downloadExport">导出CSV</a-button>
            <a-button :disabled="!selectedSingleRecord" danger @click="removeSelected">删除</a-button>
            <a-button :disabled="selectedRowKeys.length === 0" danger @click="batchRemove">批量删除</a-button>
            <span class="selection-tip">已勾选 {{ selectedRowKeys.length }} 条</span>
          </template>
        </SearchForm>

        <div v-if="isComplianceMode" class="compliance-panel">
          <div class="compliance-summary">
            <div>
              <strong>{{ activeArchivePreset?.title || '合规档案包' }}</strong>
              <span>{{ activeArchivePreset?.description || '按协议、告知、记录、报表等材料统一检索和补齐。' }}</span>
            </div>
            <a-button size="small" @click="openFolderCreateWithPreset">新建档案夹</a-button>
          </div>
          <a-space wrap>
            <a-button
              v-for="doc in complianceRequiredDocuments"
              :key="doc"
              size="small"
              @click="applyComplianceKeyword(doc)"
            >
              {{ doc }}
            </a-button>
            <a-button
              v-if="activeArchivePreset?.nextDocuments?.length"
              size="small"
              type="primary"
              ghost
              @click="openComplianceCreate(activeArchivePreset.nextDocuments[0])"
            >
              补齐{{ activeArchivePreset.nextDocuments[0] }}
            </a-button>
          </a-space>
        </div>

        <DataTable
          rowKey="id"
          :row-selection="rowSelection"
          :columns="columns"
          :data-source="rows"
          :loading="loading"
          :pagination="pagination"
          :empty-title="emptyStateTitle"
          :empty-description="emptyStateDescription"
          @change="handleTableChange"
        >
          <template #emptyExtra>
            <a-space wrap>
              <a-button type="primary" @click="openCreate">新增文档</a-button>
              <a-button v-if="showCreateFolderAction" @click="openFolderCreate">新增档案夹</a-button>
            </a-space>
          </template>
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
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="可见性">
              <a-select v-model:value="folderForm.visibility">
                <a-select-option value="PUBLIC">公开</a-select-option>
                <a-select-option value="PRIVATE">私有</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="区域编码">
              <a-input v-model:value="folderForm.regionCode" placeholder="如：A区/护理部" />
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
import { computed, reactive, ref, watch } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { useRoute } from 'vue-router'
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
const route = useRoute()

const loading = ref(false)
const rows = ref<OaDocument[]>([])
const folders = ref<OaDocumentFolder[]>([])
const selectedFolderKey = ref<string>(ROOT_KEY)

const query = reactive({
  keyword: '',
  folderVisibility: undefined as string | undefined,
  regionCode: '',
  pageNo: 1,
  pageSize: 10,
  folderId: ''
})
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const selectedRowKeys = ref<string[]>([])

const complianceArchivePresets = {
  'elder-agreement': {
    title: '长者入住合规档案',
    keyword: '入住协议',
    folderName: '长者入住合规档案',
    description: '集中补齐入住协议、知情同意、风险告知和联系人授权。',
    requiredDocuments: ['入住协议', '知情同意书', '风险告知书', '护理等级确认', '家属联系人授权'],
    nextDocuments: ['风险告知书', '知情同意书']
  },
  'medical-care': {
    title: '医疗护理记录档案',
    keyword: '护理记录',
    folderName: '医疗护理记录档案',
    description: '集中检索护理记录、医嘱、用药确认和交接班材料。',
    requiredDocuments: ['护理记录', '医嘱记录', '用药确认', '生命体征', '交接班记录'],
    nextDocuments: ['护理记录', '交接班记录']
  },
  incident: {
    title: '事故与应急档案',
    keyword: '事故报告',
    folderName: '事故与应急档案',
    description: '统一归档事故报告、家属告知、整改措施和复盘结论。',
    requiredDocuments: ['事故报告', '现场处理', '家属告知', '整改措施', '复盘结论'],
    nextDocuments: ['事故报告', '整改措施']
  },
  government: {
    title: '监管与经营报表',
    keyword: '监管报表',
    folderName: '监管与经营报表',
    description: '沉淀监管报表、月度经营报表和服务质量报告。',
    requiredDocuments: ['入住人数', '床位使用率', '月收入', '事故数量', '服务质量'],
    nextDocuments: ['监管报表', '月度服务报告']
  },
  audit: {
    title: '权限与操作日志',
    keyword: '操作日志',
    folderName: '权限与操作日志',
    description: '收拢角色权限、操作日志、制度更新和培训记录。',
    requiredDocuments: ['角色权限', '页面权限', '操作日志', '制度更新', '员工培训记录'],
    nextDocuments: ['操作日志', '制度更新']
  }
} as const

type ComplianceArchiveKey = keyof typeof complianceArchivePresets

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
  visibility: 'PUBLIC',
  regionCode: '',
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
const hasDocumentFilters = computed(() => Boolean(query.keyword || query.folderVisibility || query.regionCode))
const showCreateFolderAction = computed(() => selectedFolderKey.value === ROOT_KEY)
const emptyStateTitle = computed(() => {
  if (selectedFolderKey.value !== ROOT_KEY) {
    return `“${selectedFolderName.value}”里还没有文档`
  }
  if (hasDocumentFilters.value) {
    return '当前筛选下暂无文档'
  }
  return '文档中心还是空的'
})
const emptyStateDescription = computed(() => {
  if (selectedFolderKey.value !== ROOT_KEY) {
    return '可以直接上传第一份文档，或先整理档案夹结构再归档。'
  }
  if (hasDocumentFilters.value) {
    return '尝试放宽关键词、可见性或区域条件，或者直接新增一份文档。'
  }
  return '先建立档案夹结构，再上传 Word / PDF 文档，会更方便后续检索和归档。'
})

const folderTreeData = computed(() => [
  {
    key: ROOT_KEY,
    title: '全部文档',
    children: transformFolderTreeToUi(filteredFolders.value)
  }
])

const filteredFolders = computed(() => applyFolderFilters(folders.value))

const folderSelectData = computed(() => transformFolderTreeToSelect(folders.value))

const folderParentSelectData = computed(() => transformFolderTreeToSelect(folders.value, folderForm.id))

const uploadDisplayText = computed(() => {
  if (form.url) {
    return `已上传：${form.name || '已自动识别文件名'}`
  }
  return '支持 doc/docx/pdf，上传后自动填充文件名、地址、大小'
})
const activeArchiveKey = computed(() => {
  const key = String(route.query.archive || '')
  return key in complianceArchivePresets ? (key as ComplianceArchiveKey) : undefined
})
const activeArchivePreset = computed(() => (activeArchiveKey.value ? complianceArchivePresets[activeArchiveKey.value] : undefined))
const isComplianceMode = computed(() => route.query.compliance === '1' || Boolean(activeArchivePreset.value))
const complianceRequiredDocuments = computed(() => activeArchivePreset.value?.requiredDocuments || [])

function getRouteQueryString(value: unknown) {
  return Array.isArray(value) ? String(value[0] || '') : String(value || '')
}

function applyDocumentQueryFromRoute() {
  const archivePreset = activeArchivePreset.value
  const keyword = getRouteQueryString(route.query.keyword) || archivePreset?.keyword || ''
  const folderId = getRouteQueryString(route.query.folderId)
  const visibility = getRouteQueryString(route.query.folderVisibility)
  query.keyword = keyword
  query.folderVisibility = visibility === 'PUBLIC' || visibility === 'PRIVATE' ? visibility : undefined
  query.regionCode = getRouteQueryString(route.query.regionCode)
  query.folderId = folderId
  selectedFolderKey.value = folderId || ROOT_KEY
  query.pageNo = 1
  pagination.current = 1
}

function applyComplianceKeyword(keyword: string) {
  query.keyword = keyword
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function findFolderByName(name?: string) {
  if (!name) return null
  for (const folder of folderNodeMap.value.values()) {
    if (folder.name === name || folder.name.includes(name)) {
      return folder
    }
  }
  return null
}

function openComplianceCreate(documentName: string) {
  openCreate()
  const preset = activeArchivePreset.value
  const folder = findFolderByName(preset?.folderName)
  form.name = documentName
  form.folderId = folder ? String(folder.id) : form.folderId
  form.remark = preset ? `${preset.title}：${documentName}` : documentName
}

function openFolderCreateWithPreset() {
  openFolderCreate()
  const preset = activeArchivePreset.value
  if (preset) {
    folderForm.name = preset.folderName
    folderForm.remark = preset.description
  }
}

async function fetchFolderTree() {
  const data = await getDocumentFolderTree()
  folders.value = normalizeFolderTree(data || [])
  reconcileFolderSelections()
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<OaDocument> = await getDocumentPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      folderId: query.folderId || undefined,
      folderVisibility: query.folderVisibility || undefined,
      regionCode: query.regionCode || undefined,
      keyword: query.keyword || undefined
    })
    rows.value = (res.list || []).map((item) => ({
      ...item,
      id: String(item.id),
      folderId: item.folderId ? String(item.folderId) : undefined
    }))
    pagination.total = Number(res.total || 0)
    pagination.current = query.pageNo
    pagination.pageSize = query.pageSize
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
  query.folderVisibility = undefined
  query.regionCode = ''
  query.pageNo = 1
  query.pageSize = 10
  pagination.current = 1
  pagination.pageSize = 10
  if (selectedFolderKey.value !== ROOT_KEY && !folderNodeMap.value.has(selectedFolderKey.value)) {
    selectedFolderKey.value = ROOT_KEY
    query.folderId = ''
  }
  fetchData()
}

function onFolderSelect(keys: (string | number)[]) {
  const key = String(keys?.[0] || ROOT_KEY)
  selectedFolderKey.value = key
  query.folderId = resolveExistingFolderKey(key)
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openCreate() {
  form.id = undefined
  form.name = ''
  form.folderId = resolveExistingFormFolderId(selectedFolderKey.value)
  form.url = ''
  form.sizeBytes = undefined
  form.uploaderName = ''
  form.remark = ''
  editOpen.value = true
}

function openEdit(record: OaDocument) {
  form.id = String(record.id)
  form.name = record.name || ''
  form.folderId = resolveExistingFormFolderId(record.folderId ? String(record.folderId) : undefined)
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
  if (!form.name?.trim()) {
    message.warning('请填写文档名称')
    return
  }
  form.folderId = resolveExistingFormFolderId(form.folderId)
  const selectedFolder = form.folderId ? folderNodeMap.value.get(String(form.folderId)) : null
  const payload = {
    name: form.name,
    folderId: normalizeTreeNodeId(form.folderId),
    folder: selectedFolder?.name,
    url: form.url,
    sizeBytes: form.sizeBytes,
    uploaderName: form.uploaderName,
    remark: form.remark
  }
  if (saving.value) return
  saving.value = true
  try {
    const creating = !form.id
    if (form.id) {
      await updateDocument(form.id, payload)
      message.success('文档更新成功')
    } else {
      await createDocument(payload)
      message.success('文档创建成功')
    }
    editOpen.value = false
    if (creating && hasDocumentFilters.value) {
      onReset()
      return
    }
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
    folderVisibility: query.folderVisibility || undefined,
    regionCode: query.regionCode || undefined,
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
  folderForm.visibility = 'PUBLIC'
  folderForm.regionCode = ''
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
  folderForm.visibility = folder.visibility || 'PUBLIC'
  folderForm.regionCode = folder.regionCode || ''
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
    parentId: normalizeTreeNodeId(folderForm.parentId),
    sortNo: folderForm.sortNo,
    status: folderForm.status,
    visibility: folderForm.visibility,
    regionCode: folderForm.regionCode || undefined,
    remark: folderForm.remark
  }
  folderSaving.value = true
  try {
    if (folderForm.id) {
      await updateDocumentFolder(folderForm.id, payload)
      message.success('档案夹更新成功')
    } else {
      const created = await createDocumentFolder(payload)
      message.success('档案夹创建成功')
      if (created?.id != null) {
        selectedFolderKey.value = String(created.id)
        query.folderId = String(created.id)
      }
    }
    folderEditOpen.value = false
    await fetchFolderTree()
    query.pageNo = 1
    pagination.current = 1
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
    visibility: item.visibility || 'PUBLIC',
    regionCode: item.regionCode || '',
    children: normalizeFolderTree(item.children || [])
  }))
}

function normalizeTreeNodeId(id?: string) {
  if (!id) return undefined
  const parsed = Number(id)
  if (!Number.isFinite(parsed) || parsed <= 0) return undefined
  return parsed
}

function resolveExistingFolderKey(id?: string) {
  if (!id || id === ROOT_KEY) return ''
  return folderNodeMap.value.has(String(id)) ? String(id) : ''
}

function resolveExistingFormFolderId(id?: string) {
  const normalized = resolveExistingFolderKey(id)
  return normalized || undefined
}

function reconcileFolderSelections() {
  if (selectedFolderKey.value !== ROOT_KEY && !folderNodeMap.value.has(selectedFolderKey.value)) {
    selectedFolderKey.value = ROOT_KEY
    query.folderId = ''
  }
  if (form.folderId && !folderNodeMap.value.has(String(form.folderId))) {
    form.folderId = undefined
  }
  if (folderForm.parentId && !folderNodeMap.value.has(String(folderForm.parentId))) {
    folderForm.parentId = undefined
  }
}

function transformFolderTreeToUi(nodes: OaDocumentFolder[]) {
  return (nodes || []).map((item) => ({
    key: String(item.id),
    title: `${item.visibility === 'PRIVATE' ? '🔒' : '🌐'} ${item.name}${item.documentCount ? ` (${item.documentCount})` : ''}`,
    children: transformFolderTreeToUi(item.children || [])
  }))
}

function transformFolderTreeToSelect(nodes: OaDocumentFolder[], excludeId?: string) {
  return (nodes || [])
    .filter((item) => String(item.id) !== String(excludeId || ''))
    .map((item) => ({
      title: `${item.visibility === 'PRIVATE' ? '🔒' : '🌐'} ${item.name}`,
      value: String(item.id),
      children: transformFolderTreeToSelect(item.children || [], excludeId)
    }))
}

function applyFolderFilters(nodes: OaDocumentFolder[]): OaDocumentFolder[] {
  const visibility = query.folderVisibility
  const region = (query.regionCode || '').trim()
  const walk = (items: OaDocumentFolder[]): OaDocumentFolder[] =>
    (items || [])
      .map((item) => {
        const children = walk(item.children || [])
        const passVisibility = !visibility || (item.visibility || 'PUBLIC') === visibility
        const passRegion = !region || String(item.regionCode || '').includes(region)
        const selfMatched = passVisibility && passRegion
        if (!selfMatched && !children.length) return null
        return { ...item, children }
      })
      .filter((item): item is OaDocumentFolder => !!item)
  return walk(nodes)
}

function formatSize(size?: number) {
  if (!size || size <= 0) return '-'
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / (1024 * 1024)).toFixed(1)} MB`
}

applyDocumentQueryFromRoute()
Promise.all([fetchFolderTree(), fetchData()])

watch(
  () => route.query,
  () => {
    applyDocumentQueryFromRoute()
    fetchData()
  }
)
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
  color: var(--muted);
  font-size: 12px;
}

.upload-hint {
  margin-top: 8px;
  color: var(--muted);
  font-size: 12px;
}

.name-link {
  padding-inline: 0;
}

.compliance-panel {
  margin-bottom: 16px;
  padding: 14px 16px;
  border: 1px solid rgba(var(--primary-rgb), 0.18);
  border-radius: 8px;
  background: var(--primary-soft);
}

.compliance-summary {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.compliance-summary strong {
  display: block;
  margin-bottom: 4px;
}

.compliance-summary span {
  color: var(--muted);
  font-size: 13px;
}
</style>
