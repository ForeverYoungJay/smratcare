<template>
  <PageContainer :title="pageTitle" subTitle="统一维护老人、营销、入住、活动、社区及费用等基础字典">
    <a-card class="card-elevated" :bordered="false">
      <a-tabs v-if="showTabs" v-model:activeKey="activeGroup" @change="onGroupChange">
        <a-tab-pane v-for="item in displayGroups" :key="item.code" :tab="item.label" />
      </a-tabs>

      <a-form :model="query" layout="inline" class="search-bar">
        <a-form-item label="关键词">
          <a-input v-model:value="query.keyword" placeholder="名称/编码" allow-clear @pressEnter="fetchData" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.status" allow-clear style="width: 140px">
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">停用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchData">查询</a-button>
            <a-button @click="reset">重置</a-button>
            <a-button type="primary" ghost @click="openCreate" :disabled="!activeGroup">新增配置项</a-button>
            <a-button :disabled="selectedRowKeys.length === 0" @click="batchToggleStatus(1)">批量启用</a-button>
            <a-button :disabled="selectedRowKeys.length === 0" @click="batchToggleStatus(0)">批量停用</a-button>
            <a-button @click="downloadExport" :disabled="!activeGroup">导出CSV</a-button>
            <a-button @click="downloadImportTemplate">导入模板</a-button>
            <a-upload :before-upload="beforeImport" :show-upload-list="false" accept=".csv" :disabled="!activeGroup">
              <a-button :disabled="!activeGroup">导入CSV/错误文件</a-button>
            </a-upload>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <a-table
        :data-source="rows"
        :columns="columns"
        :loading="loading"
        :pagination="false"
        row-key="id"
        :row-selection="rowSelection"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'configGroup'">
            {{ resolveGroupLabel(record) }}
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'default'">{{ record.status === 1 ? '启用' : '停用' }}</a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" @click="toggleStatus(record)">{{ record.status === 1 ? '停用' : '启用' }}</a-button>
              <a-button type="link" @click="openEdit(record)">编辑</a-button>
              <a-button type="link" danger @click="remove(record)">删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>

      <a-pagination
        style="margin-top: 16px; text-align: right;"
        :current="query.pageNo"
        :page-size="query.pageSize"
        :total="total"
        show-size-changer
        @change="onPageChange"
        @showSizeChange="onPageSizeChange"
      />
    </a-card>

    <a-modal
      v-model:open="editorOpen"
      :title="editingId ? '编辑基础数据' : '新增基础数据'"
      @ok="submit"
      :confirm-loading="submitting"
      width="520px"
    >
      <a-form ref="formRef" :model="form" :rules="rules" layout="vertical">
        <a-form-item label="所属分组" name="configGroup">
          <a-select v-model:value="form.configGroup" :disabled="!!editingId">
            <a-select-option v-for="item in groups" :key="item.code" :value="item.code">{{ item.label }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="配置名称" name="itemName">
          <a-input v-model:value="form.itemName" placeholder="请输入名称" :maxlength="128" />
        </a-form-item>
        <a-form-item label="配置编码" name="itemCode">
          <a-input v-model:value="form.itemCode" placeholder="如 CHANNEL_WECHAT" :maxlength="64" @blur="normalizeFormCode" />
        </a-form-item>
        <a-form-item label="排序">
          <a-input-number v-model:value="form.sortNo" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="form.status">
            <a-radio :value="1">启用</a-radio>
            <a-radio :value="0">停用</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="form.remark" :rows="3" :maxlength="255" show-count />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="importPreviewOpen"
      title="导入预校验结果"
      width="680px"
      :confirm-loading="importSubmitting"
      ok-text="确认导入"
      cancel-text="关闭"
      :ok-button-props="{ disabled: (importPreviewResult?.successCount || 0) === 0 }"
      @cancel="resetImportPreview"
      @ok="confirmImport"
    >
      <a-space direction="vertical" style="width: 100%">
        <a-alert
          type="info"
          show-icon
          :message="`总计${importPreviewResult?.totalCount || 0}条，可导入${importPreviewResult?.successCount || 0}条，失败${importPreviewResult?.failCount || 0}条`"
        />
        <a-space>
          <a-tag color="blue">新增 {{ importPreviewResult?.createCount || 0 }}</a-tag>
          <a-tag color="green">更新 {{ importPreviewResult?.updateCount || 0 }}</a-tag>
          <a-button v-if="hasImportErrors" @click="downloadImportErrors">下载错误文件</a-button>
          <a-upload
            v-if="hasImportErrors"
            :before-upload="beforeRetryImport"
            :show-upload-list="false"
            accept=".csv"
          >
            <a-button>上传修复后错误文件</a-button>
          </a-upload>
        </a-space>
        <a-alert
          v-if="hasImportErrors"
          type="warning"
          show-icon
          :message="`存在${importPreviewResult?.failCount || 0}条异常，最多展示前10条`"
        />
        <a-list
          v-if="hasImportErrors"
          size="small"
          bordered
          :data-source="(importPreviewResult?.errors || []).slice(0, 10)"
          style="max-height: 260px; overflow: auto;"
        >
          <template #renderItem="{ item }">
            <a-list-item>{{ item }}</a-list-item>
          </template>
        </a-list>
      </a-space>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'ant-design-vue'
import { message, Modal } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import {
  batchUpdateBaseConfigStatus,
  createBaseConfigItem,
  deleteBaseConfigItem,
  downloadBaseConfigImportTemplate,
  exportBaseConfigItems,
  getBaseConfigGroups,
  getBaseConfigItemPage,
  importBaseConfigItems,
  previewImportBaseConfigItems,
  updateBaseConfigItem
} from '../../api/baseConfig'
import type {
  BaseConfigImportErrorItem,
  BaseConfigGroupOption,
  BaseConfigImportItem,
  BaseConfigImportResult,
  BaseConfigItem,
  BaseConfigItemPayload,
  PageResult
} from '../../types'

const props = withDefaults(defineProps<{
  title?: string
  groupCode?: string
}>(), {
  title: '基础数据配置',
  groupCode: ''
})

const loading = ref(false)
const submitting = ref(false)
const importSubmitting = ref(false)
const editorOpen = ref(false)
const importPreviewOpen = ref(false)
const formRef = ref<FormInstance>()
const editingId = ref<number>()

const groups = ref<BaseConfigGroupOption[]>([])
const activeGroup = ref('')
const rows = ref<BaseConfigItem[]>([])
const total = ref(0)
const selectedRowKeys = ref<number[]>([])
const pendingImportItems = ref<BaseConfigImportItem[]>([])
const importPreviewResult = ref<BaseConfigImportResult>()

const query = reactive({
  keyword: '',
  status: undefined as number | undefined,
  pageNo: 1,
  pageSize: 10
})

const form = reactive<BaseConfigItemPayload>({
  configGroup: '',
  itemCode: '',
  itemName: '',
  status: 1,
  sortNo: 0,
  remark: ''
})

const rules: FormRules = {
  configGroup: [{ required: true, message: '请选择分组' }],
  itemName: [{ required: true, message: '请输入配置名称' }],
  itemCode: [
    { required: true, message: '请输入配置编码' },
    { pattern: /^[A-Za-z0-9_]+$/, message: '仅支持字母/数字/下划线' }
  ],
  status: [{ required: true, message: '请选择状态' }]
}

const columns = computed(() => [
  { title: '分组', dataIndex: 'configGroup', key: 'configGroup', width: 180 },
  { title: '配置名称', dataIndex: 'itemName', key: 'itemName', width: 180 },
  { title: '配置编码', dataIndex: 'itemCode', key: 'itemCode', width: 180 },
  { title: '排序', dataIndex: 'sortNo', key: 'sortNo', width: 100 },
  { title: '状态', key: 'status', width: 100 },
  { title: '备注', dataIndex: 'remark', key: 'remark' },
  { title: '操作', key: 'action', width: 140 }
])

const pageTitle = computed(() => props.title || '基础数据配置')

const showTabs = computed(() => !props.groupCode)

const displayGroups = computed(() => {
  if (!props.groupCode) {
    return groups.value
  }
  return groups.value.filter((item) => item.code === props.groupCode)
})

const groupLabelMap = computed(() => {
  return groups.value.reduce<Record<string, string>>((result, item) => {
    result[item.code] = item.label
    return result
  }, {})
})

const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: (string | number)[]) => {
    selectedRowKeys.value = keys.map((item) => Number(item))
  }
}))

const hasImportErrors = computed(() => (importPreviewResult.value?.failCount || 0) > 0)

async function loadGroups() {
  groups.value = await getBaseConfigGroups()
  if (props.groupCode) {
    activeGroup.value = props.groupCode
    return
  }
  if (!activeGroup.value && groups.value.length > 0) {
    activeGroup.value = groups.value[0].code
  }
}

async function fetchData() {
  if (!activeGroup.value) {
    rows.value = []
    total.value = 0
    return
  }
  loading.value = true
  try {
    const res: PageResult<BaseConfigItem> = await getBaseConfigItemPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      configGroup: activeGroup.value,
      keyword: query.keyword || undefined,
      status: query.status
    })
    rows.value = res.list
    total.value = res.total
    selectedRowKeys.value = []
  } finally {
    loading.value = false
  }
}

function onGroupChange() {
  query.pageNo = 1
  fetchData()
}

function reset() {
  query.keyword = ''
  query.status = undefined
  query.pageNo = 1
  fetchData()
}

function openCreate() {
  if (!activeGroup.value) {
    message.warning('请先选择分组')
    return
  }
  editingId.value = undefined
  form.configGroup = activeGroup.value
  form.itemName = ''
  form.itemCode = ''
  form.status = 1
  form.sortNo = 0
  form.remark = ''
  editorOpen.value = true
}

function openEdit(row: BaseConfigItem) {
  editingId.value = row.id
  form.configGroup = row.configGroup
  form.itemName = row.itemName
  form.itemCode = row.itemCode
  form.status = row.status
  form.sortNo = row.sortNo
  form.remark = row.remark || ''
  editorOpen.value = true
}

function normalizeFormCode() {
  form.itemCode = (form.itemCode || '').trim().toUpperCase()
}

function normalizePayload(payload: BaseConfigItemPayload): BaseConfigItemPayload {
  return {
    configGroup: payload.configGroup.trim(),
    itemName: payload.itemName.trim(),
    itemCode: payload.itemCode.trim().toUpperCase(),
    status: payload.status,
    sortNo: payload.sortNo ?? 0,
    remark: payload.remark?.trim() || undefined
  }
}

function resolveGroupLabel(row: BaseConfigItem) {
  return row.configGroupLabel || groupLabelMap.value[row.configGroup] || row.configGroup
}

async function submit() {
  if (!formRef.value) return
  normalizeFormCode()
  await formRef.value.validate()
  submitting.value = true
  try {
    const payload = normalizePayload(form)
    if (editingId.value) {
      await updateBaseConfigItem(editingId.value, payload)
      message.success('更新成功')
    } else {
      await createBaseConfigItem(payload)
      message.success('创建成功')
    }
    editorOpen.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

async function toggleStatus(row: BaseConfigItem) {
  const targetStatus = row.status === 1 ? 0 : 1
  await updateBaseConfigItem(row.id, {
    configGroup: row.configGroup,
    itemCode: row.itemCode,
    itemName: row.itemName,
    status: targetStatus,
    sortNo: row.sortNo ?? 0,
    remark: row.remark
  })
  message.success(targetStatus === 1 ? '已启用' : '已停用')
  fetchData()
}

async function batchToggleStatus(status: number) {
  const ids = selectedRowKeys.value
  if (!ids.length) {
    return
  }
  Modal.confirm({
    title: status === 1 ? `确认批量启用选中的${ids.length}项吗？` : `确认批量停用选中的${ids.length}项吗？`,
    onOk: async () => {
      const updatedCount = await batchUpdateBaseConfigStatus(ids, status)
      message.success(status === 1 ? `批量启用完成（更新${updatedCount || 0}项）` : `批量停用完成（更新${updatedCount || 0}项）`)
      fetchData()
    }
  })
}

async function downloadImportTemplate() {
  const blob = await downloadBaseConfigImportTemplate()
  downloadBlob(blob, 'base-config-import-template.csv')
}

async function downloadExport() {
  if (!activeGroup.value) {
    message.warning('请先选择分组')
    return
  }
  const blob = await exportBaseConfigItems({
    configGroup: activeGroup.value,
    keyword: query.keyword || undefined,
    status: query.status
  })
  downloadBlob(blob, `base-config-${activeGroup.value}-${new Date().toISOString().slice(0, 10)}.csv`)
}

function downloadBlob(blob: Blob, filename: string) {
  const href = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = href
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(href)
}

function parseCsv(text: string): string[][] {
  const rows: string[][] = []
  let current = ''
  let row: string[] = []
  let inQuotes = false
  const source = text.replace(/^\uFEFF/, '')
  for (let index = 0; index < source.length; index++) {
    const char = source[index]
    const next = source[index + 1]
    if (char === '"') {
      if (inQuotes && next === '"') {
        current += '"'
        index++
      } else {
        inQuotes = !inQuotes
      }
      continue
    }
    if (char === ',' && !inQuotes) {
      row.push(current)
      current = ''
      continue
    }
    if ((char === '\n' || char === '\r') && !inQuotes) {
      if (char === '\r' && next === '\n') {
        index++
      }
      row.push(current)
      current = ''
      if (row.some((item) => item.trim() !== '')) {
        rows.push(row)
      }
      row = []
      continue
    }
    current += char
  }
  row.push(current)
  if (row.some((item) => item.trim() !== '')) {
    rows.push(row)
  }
  return rows
}

function normalizeHeaderName(value: string) {
  return (value || '').trim().toLowerCase().replace(/[\s_]/g, '')
}

function resolveHeaderMap(headers: string[]) {
  const normalized = headers.map((item) => normalizeHeaderName(item))
  const indexOf = (...keys: string[]) => {
    const aliasSet = new Set(keys.map((key) => normalizeHeaderName(key)))
    return normalized.findIndex((item) => aliasSet.has(item))
  }
  return {
    rowNo: indexOf('rowNo', 'row_no', '行号'),
    itemCode: indexOf('配置编码', 'itemCode'),
    itemName: indexOf('配置名称', 'itemName'),
    status: indexOf('状态', 'status'),
    sortNo: indexOf('排序', 'sortNo'),
    remark: indexOf('备注', 'remark')
  }
}

function parseStatus(value: string) {
  const text = value.trim()
  if (!text) return 1
  if (text === '启用' || text === '1') return 1
  if (text === '停用' || text === '0') return 0
  throw new Error(`状态值无效：${text}`)
}

function parseImportItems(rowsData: string[][]): BaseConfigImportItem[] {
  if (rowsData.length <= 1) {
    return []
  }
  const headerMap = resolveHeaderMap(rowsData[0])
  if (headerMap.itemCode < 0 || headerMap.itemName < 0) {
    throw new Error('导入文件缺少必要列：配置编码/配置名称')
  }
  return rowsData.slice(1)
    .map((row, index) => {
      const rowNoRaw = headerMap.rowNo >= 0 ? (row[headerMap.rowNo] || '').trim() : ''
      const itemCode = (row[headerMap.itemCode] || '').trim()
      const itemName = (row[headerMap.itemName] || '').trim()
      const hasAnyValue = row.some((cell) => (cell || '').trim() !== '')
      if (!itemCode || !itemName) {
        if (hasAnyValue) {
          throw new Error(`第${index + 2}行缺少配置编码或配置名称`)
        }
        return null
      }
      const sortRaw = headerMap.sortNo >= 0 ? (row[headerMap.sortNo] || '').trim() : ''
      const sortNoValue = sortRaw ? Number(sortRaw) : 0
      if (sortRaw && (Number.isNaN(sortNoValue) || !Number.isInteger(sortNoValue))) {
        throw new Error(`第${index + 2}行排序不是有效数字`)
      }
      return {
        itemCode,
        itemName,
        status: headerMap.status >= 0 ? parseStatus(row[headerMap.status] || '') : 1,
        sortNo: sortNoValue,
        remark: headerMap.remark >= 0 ? (row[headerMap.remark] || '').trim() : undefined,
        sourceOrder: rowNoRaw && !Number.isNaN(Number(rowNoRaw)) ? Number(rowNoRaw) : index + 2
      }
    })
    .filter((item): item is BaseConfigImportItem & { sourceOrder: number } => item !== null)
    .sort((left, right) => left.sourceOrder - right.sourceOrder)
    .map(({ sourceOrder: _sourceOrder, ...rest }) => rest)
}

function beforeImport(file: File) {
  if (!activeGroup.value) {
    message.warning('请先选择分组')
    return false
  }
  const reader = new FileReader()
  reader.onload = async () => {
    try {
      const rowsData = parseCsv(String(reader.result || ''))
      const items = parseImportItems(rowsData)
      if (!items.length) {
        message.warning('未识别到可导入数据')
        return
      }
      pendingImportItems.value = items
      importPreviewResult.value = await previewImportBaseConfigItems({ configGroup: activeGroup.value, items })
      importPreviewOpen.value = true
    } catch (error: any) {
      message.error(error?.message || '导入失败')
    }
  }
  reader.readAsText(file)
  return false
}

function beforeRetryImport(file: File) {
  resetImportPreview()
  return beforeImport(file)
}

function mapErrorStatusLabel(status?: number) {
  if (status === 0) return '停用'
  if (status === 1) return '启用'
  return ''
}

function downloadImportErrors(customErrorItems?: BaseConfigImportErrorItem[]) {
  const errorItems = customErrorItems || importPreviewResult.value?.errorItems || []
  if (!errorItems.length) {
    message.warning('暂无错误数据')
    return
  }
  const headers = ['rowNo', 'itemCode', 'itemName', 'status', 'sortNo', 'remark', 'errorMessage']
  const rows = errorItems.map((item: BaseConfigImportErrorItem) => [
    String(item.rowNo || ''),
    item.itemCode || '',
    item.itemName || '',
    mapErrorStatusLabel(item.status),
    item.sortNo == null ? '' : String(item.sortNo),
    item.remark || '',
    item.errorMessage || ''
  ])
  const csv = [headers.join(','), ...rows.map((row) => row.map(escapeCsvField).join(','))].join('\n')
  const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8;' })
  downloadBlob(blob, `base-config-import-errors-${new Date().toISOString().slice(0, 10)}.csv`)
}

function escapeCsvField(value: string) {
  const safeValue = (value || '').replace(/"/g, '""')
  if (safeValue.includes(',') || safeValue.includes('"') || safeValue.includes('\n')) {
    return `"${safeValue}"`
  }
  return safeValue
}

async function confirmImport() {
  if (!pendingImportItems.value.length || !activeGroup.value) {
    resetImportPreview()
    return
  }
  importSubmitting.value = true
  try {
    const result = await importBaseConfigItems({ configGroup: activeGroup.value, items: pendingImportItems.value })
    const detail = result.failCount
      ? `导入完成：成功${result.successCount}条，失败${result.failCount}条`
      : `导入完成：成功${result.successCount}条`
    message.success(detail)
    if (result.errorItems?.length) {
      Modal.confirm({
        title: '存在导入失败项，是否下载错误文件？',
        onOk: () => downloadImportErrors(result.errorItems || [])
      })
    }
    resetImportPreview()
    fetchData()
  } finally {
    importSubmitting.value = false
  }
}

function resetImportPreview() {
  importPreviewOpen.value = false
  pendingImportItems.value = []
  importPreviewResult.value = undefined
}

function remove(row: BaseConfigItem) {
  Modal.confirm({
    title: `确认删除【${row.itemName}】吗？`,
    onOk: async () => {
      await deleteBaseConfigItem(row.id)
      message.success('删除成功')
      if (rows.value.length === 1 && query.pageNo > 1) {
        query.pageNo -= 1
      }
      fetchData()
    }
  })
}

function onPageChange(page: number) {
  query.pageNo = page
  fetchData()
}

function onPageSizeChange(_current: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
  fetchData()
}

onMounted(async () => {
  await loadGroups()
  await fetchData()
})

watch(
  () => props.groupCode,
  (value) => {
    if (value) {
      activeGroup.value = value
      query.pageNo = 1
      fetchData()
    }
  }
)
</script>

<style scoped>
.search-bar {
  margin-bottom: 12px;
}
</style>
