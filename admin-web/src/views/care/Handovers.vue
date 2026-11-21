<template>
  <PageContainer title="交接日志" subTitle="在岗工作日志、注意事项与待办追踪">
    <a-card :bordered="false" class="card-elevated">
      <a-form layout="inline" @submit.prevent>
        <a-form-item label="日期范围">
          <a-range-picker v-model:value="query.range" value-format="YYYY-MM-DD" />
        </a-form-item>
        <a-form-item label="班次编码">
          <a-select
            v-model:value="query.shiftCode"
            style="width: 200px"
            allow-clear
            show-search
            option-filter-prop="label"
            placeholder="选择班次"
            :loading="shiftCodeLoading"
            :options="shiftCodeOptions"
          >
            <template #notFoundContent>
              <div class="shift-code-empty">
                还没有配置班次模板，
                <a @click="goShiftTemplates">前往排班方案配置</a>
              </div>
            </template>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.status" :options="statusOptions" allow-clear style="width: 140px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="search">查询</a-button>
            <a-button @click="reset">重置</a-button>
            <a-button @click="exportCsvData" :loading="exporting">导出CSV</a-button>
            <a-button @click="exportExcelData" :loading="exporting">导出Excel</a-button>
            <a-button type="primary" @click="openModal()">新增交接日志</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <!-- 行会被刷成黄/红两色，页面上必须说明这两种颜色代表什么，否则新人只会慌 -->
      <div class="handover-legend">
        <span class="handover-legend__item">
          <i class="handover-legend__swatch handover-legend__swatch--warning"></i>已交接，但对方还没确认
        </span>
        <span class="handover-legend__item">
          <i class="handover-legend__swatch handover-legend__swatch--danger"></i>备注命中风险关键词，需要重点交代
        </span>
      </div>

      <a-table
        row-key="id"
        :columns="columns"
        :data-source="rows"
        :loading="loading"
        :pagination="pagination"
        :row-class-name="resolveRowClassName"
        @change="onTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="statusColor(record.status)">{{ statusLabel(record.status) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'attachmentUrls'">
            <a-button v-if="countAttachments(record.attachmentUrls) > 0" type="link" @click="openAttachments(record.attachmentUrls)">
              {{ countAttachments(record.attachmentUrls) }}个附件
            </a-button>
            <span v-else>-</span>
          </template>
          <template v-else-if="column.key === 'actions'">
            <div class="row-action-links">
              <a-button v-if="canConfirm(record)" type="link" size="small" @click="confirm(record)">接班确认</a-button>
              <a-button v-if="record.status !== 'CONFIRMED'" type="link" size="small" @click="openModal(record)">编辑</a-button>
              <a-button v-if="!record.status || record.status === 'DRAFT'" type="link" size="small" danger @click="remove(record.id)">删除</a-button>
            </div>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal :mask-closable="false" v-model:open="modalOpen" title="交接日志" :confirm-loading="submitting" width="760" @ok="submit">
      <a-form ref="formRef" layout="vertical" :model="form" :rules="rules">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="班次日期" name="dutyDate" required>
              <a-date-picker v-model:value="form.dutyDate" value-format="YYYY-MM-DD" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="班次编码" name="shiftCode" required>
              <a-select
                v-model:value="form.shiftCode"
                allow-clear
                show-search
                option-filter-prop="label"
                placeholder="选择班次"
                :loading="shiftCodeLoading"
                :options="shiftCodeOptions"
              >
                <template #notFoundContent>
                  <div class="shift-code-empty">
                    还没有配置班次模板，
                    <a @click="goShiftTemplates">前往排班方案配置</a>
                  </div>
                </template>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="交班人" name="fromStaffId" required>
              <a-select
                v-model:value="form.fromStaffId"
                show-search
                :filter-option="false"
                placeholder="输入姓名搜索"
                :options="staffOptions"
                @search="searchStaff"
                @focus="() => !staffOptions.length && searchStaff('')"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="接班人" name="toStaffId" required>
              <a-select
                v-model:value="form.toStaffId"
                show-search
                :filter-option="false"
                placeholder="输入姓名搜索"
                :options="staffOptions"
                @search="searchStaff"
                @focus="() => !staffOptions.length && searchStaff('')"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="在岗期间做了什么" name="onDutySummary">
          <a-textarea v-model:value="form.onDutySummary" :rows="3" />
        </a-form-item>
        <a-form-item label="注意事项" name="attentionNote">
          <a-textarea v-model:value="form.attentionNote" :rows="2" />
        </a-form-item>
        <a-form-item label="待办事项" name="todoNote">
          <a-textarea v-model:value="form.todoNote" :rows="2" />
        </a-form-item>
        <a-form-item label="交接附件">
          <a-upload :show-upload-list="false" :before-upload="beforeUploadAttachment">
            <a-button :loading="uploadingAttachment">上传附件</a-button>
          </a-upload>
          <div class="upload-tip">支持图片/文档，单个不超过10MB，最多10个附件</div>
          <a-space wrap style="margin-top: 8px">
            <a-tag v-for="(file, index) in attachmentFiles" :key="`${file.url}_${index}`" closable @close="removeAttachment(index)">
              <a @click.prevent="openAttachment(file.url)">{{ file.name }}</a>
            </a-tag>
          </a-space>
        </a-form-item>

        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="状态" name="status">
              <a-select v-model:value="form.status" :options="formStatusOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="交班时间" name="handoverTime">
              <a-date-picker v-model:value="form.handoverTime" show-time value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="接班确认" extra="由接班人本人在列表点「接班确认」">
              <span>{{ form.confirmTime ? String(form.confirmTime).replace('T', ' ') : '待接班人确认' }}</span>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import dayjs from 'dayjs'
import { message, Modal } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import { useStaffOptions } from '../../composables/useStaffOptions'
import { useShiftCodeOptions } from '../../composables/useShiftCodeOptions'
import { confirmShiftHandover, createShiftHandover, deleteShiftHandover, getShiftHandover, getShiftHandoverPage, updateShiftHandover } from '../../api/nursing'
import { handoverExportColumns, mapCareExportRows } from '../../constants/careExport'
import { exportCsv, exportExcel } from '../../utils/export'
import { getHandoverRiskKeywords, syncMedicalAlertRules } from '../../utils/medicalAlertRule'
import { uploadOaFile } from '../../api/oa'
import type { PageResult, ShiftHandoverItem } from '../../types'
import { resolveCareError } from './careError'
import { useUserStore } from '../../stores/user'

const route = useRoute()
const rows = ref<ShiftHandoverItem[]>([])
const loading = ref(false)
const exporting = ref(false)
const modalOpen = ref(false)
const submitting = ref(false)
const uploadingAttachment = ref(false)
const formRef = ref<FormInstance>()
const { staffOptions, searchStaff, ensureSelectedStaff } = useStaffOptions({ pageSize: 120 })
const { shiftCodeOptions, shiftCodeLoading, loadShiftCodes, ensureShiftCode } = useShiftCodeOptions()

const shiftTemplateRouter = useRouter()
const userStore = useUserStore()

// 字典为空时给出口，别让人卡在一个选不了也填不了的下拉上
function goShiftTemplates() {
  shiftTemplateRouter.push('/care/scheduling/shift-templates')
}
const attachmentFiles = ref<HandoverAttachmentFile[]>([])
const lastAutoOpenTaskKey = ref('')

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  total: 0,
  range: undefined as string[] | undefined,
  shiftCode: undefined as string | undefined,
  status: undefined as string | undefined
})

const form = reactive<Partial<ShiftHandoverItem>>({
  dutyDate: '',
  shiftCode: '',
  fromStaffId: undefined,
  toStaffId: undefined,
  summary: '',
  onDutySummary: '',
  riskNote: '',
  attentionNote: '',
  todoNote: '',
  status: 'DRAFT',
  handoverTime: undefined,
  confirmTime: undefined,
  attachmentUrls: ''
})

interface HandoverAttachmentFile {
  name: string
  url: string
}

const statusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已交接', value: 'HANDED_OVER' },
  { label: '已确认', value: 'CONFIRMED' }
]
// 「已确认」只能由接班人本人在列表里确认，表单里不让选
const formStatusOptions = statusOptions.filter((item) => item.value !== 'CONFIRMED')

const rules = {
  dutyDate: [{ required: true, message: '请选择班次日期', trigger: 'change' }],
  shiftCode: [{ required: true, message: '请输入班次编码', trigger: 'blur' }],
  fromStaffId: [{ required: true, message: '请选择交班人', trigger: 'change' }],
  toStaffId: [{ required: true, message: '请选择接班人', trigger: 'change' }]
}

const columns = [
  { title: '班次日期', dataIndex: 'dutyDate', key: 'dutyDate', width: 120 },
  { title: '班次编码', dataIndex: 'shiftCode', key: 'shiftCode', width: 120 },
  { title: '交班人', dataIndex: 'fromStaffName', key: 'fromStaffName', width: 120 },
  { title: '接班人', dataIndex: 'toStaffName', key: 'toStaffName', width: 120 },
  { title: '状态', key: 'status', width: 110 },
  { title: '附件', key: 'attachmentUrls', width: 120 },
  { title: '注意事项', dataIndex: 'attentionNote', key: 'attentionNote' },
  { title: '操作', key: 'actions', width: 140 }
]

const pagination = computed(() => ({
  current: query.pageNo,
  pageSize: query.pageSize,
  total: query.total,
  showSizeChanger: true
}))

function statusLabel(status?: string) {
  if (status === 'HANDED_OVER') {
    return '已交接'
  }
  if (status === 'CONFIRMED') {
    return '已确认'
  }
  return '草稿'
}

function statusColor(status?: string) {
  if (status === 'HANDED_OVER') {
    return 'processing'
  }
  if (status === 'CONFIRMED') {
    return 'green'
  }
  return 'default'
}

function resetForm() {
  form.id = undefined
  form.dutyDate = ''
  form.shiftCode = ''
  form.fromStaffId = undefined
  form.toStaffId = undefined
  form.summary = ''
  form.riskNote = ''
  form.onDutySummary = ''
  form.attentionNote = ''
  form.todoNote = ''
  form.status = 'DRAFT'
  form.handoverTime = undefined
  form.confirmTime = undefined
  form.attachmentUrls = ''
  attachmentFiles.value = []
}

function openModal(record?: ShiftHandoverItem) {
  resetForm()
  if (record) {
    Object.assign(form, {
      ...record,
      handoverTime: toIsoDateTime(record.handoverTime),
      confirmTime: toIsoDateTime(record.confirmTime)
    })
    ensureSelectedStaff(record.fromStaffId, record.fromStaffName)
    ensureSelectedStaff(record.toStaffId, record.toStaffName)
    // 历史记录里的班次编码可能已经不在模板字典里，补进选项才不会显示成空白
    ensureShiftCode(record.shiftCode)
    attachmentFiles.value = parseAttachments(record.attachmentUrls)
  }
  modalOpen.value = true
}

function normalizeLoadedRow(item: ShiftHandoverItem) {
  return {
    ...item,
    onDutySummary: item.onDutySummary || item.summary,
    attentionNote: item.attentionNote || item.riskNote,
    handoverTime: toIsoDateTime(item.handoverTime),
    confirmTime: toIsoDateTime(item.confirmTime)
  }
}

async function autoOpenFromRoute() {
  const idRaw = Array.isArray(route.query.id) ? route.query.id[0] : route.query.id
  const taskId = Number(idRaw)
  if (!Number.isFinite(taskId) || taskId <= 0) {
    return
  }
  const routeStatus = Array.isArray(route.query.status) ? route.query.status[0] : route.query.status
  const autoOpenKey = `${taskId}|${String(routeStatus || '')}`
  if (lastAutoOpenTaskKey.value === autoOpenKey) {
    return
  }
  const matched = rows.value.find((item) => Number(item.id) === taskId)
  if (matched) {
    openModal(matched)
    lastAutoOpenTaskKey.value = autoOpenKey
    return
  }
  try {
    const detail = await getShiftHandover(taskId)
    if (detail?.id) {
      openModal(normalizeLoadedRow(detail as ShiftHandoverItem))
      lastAutoOpenTaskKey.value = autoOpenKey
    }
  } catch {
    // Keep the list usable even if the task detail cannot be preloaded.
  }
}

function syncQueryFromRoute() {
  const routeStatus = Array.isArray(route.query.status) ? route.query.status[0] : route.query.status
  query.status = typeof routeStatus === 'string' && routeStatus.trim() ? routeStatus.trim() : undefined
}

async function submit() {
  try {
    await formRef.value?.validate()
    if (submitting.value) return
    submitting.value = true
    const normalizedStatus = form.status || 'DRAFT'
    const nowIso = dayjs().format('YYYY-MM-DDTHH:mm:ss')
    const handoverTime = toIsoDateTime(form.handoverTime) || (normalizedStatus === 'HANDED_OVER' ? nowIso : undefined)
    const confirmTime = toIsoDateTime(form.confirmTime)
    const payload: Partial<ShiftHandoverItem> = {
      dutyDate: form.dutyDate,
      shiftCode: form.shiftCode,
      fromStaffId: form.fromStaffId,
      toStaffId: form.toStaffId,
      summary: form.onDutySummary || form.summary,
      riskNote: form.attentionNote || form.riskNote,
      todoNote: form.todoNote,
      status: normalizedStatus,
      handoverTime,
      confirmTime,
      attachmentUrls: serializeAttachments(attachmentFiles.value)
    }
    if (form.id) {
      await updateShiftHandover(form.id, payload)
    } else {
      await createShiftHandover(payload)
    }
    message.success('保存成功')
    modalOpen.value = false
    await load()
  } catch (error: any) {
    if (error?.errorFields) {
      return
    }
    message.error(resolveCareError(error, '保存失败'))
  } finally {
    submitting.value = false
  }
}

const currentStaffId = computed(() => String(userStore.staffInfo?.id ?? ''))

function canConfirm(record: ShiftHandoverItem) {
  return record.status === 'HANDED_OVER' && !!currentStaffId.value && String(record.toStaffId) === currentStaffId.value
}

async function confirm(record: ShiftHandoverItem) {
  try {
    await confirmShiftHandover(record.id)
    message.success('已确认接班')
    await load()
  } catch (error) {
    message.error(resolveCareError(error, '确认失败'))
  }
}

async function remove(id: number) {
  Modal.confirm({
    title: '确认删除该交接日志？',
    onOk: async () => {
      try {
        await deleteShiftHandover(id)
        message.success('删除成功')
        await load()
      } catch (error) {
        message.error(resolveCareError(error, '删除失败'))
      }
    }
  })
}

function onTableChange(pag: { current?: number; pageSize?: number }) {
  query.pageNo = pag.current || 1
  query.pageSize = pag.pageSize || 10
  load()
}

function search() {
  query.pageNo = 1
  load()
}

function reset() {
  query.range = undefined
  query.shiftCode = undefined
  query.status = undefined
  query.pageNo = 1
  load()
}

async function exportCsvData() {
  const records = await loadExportRecords()
  if (!records.length) {
    message.warning('暂无可导出数据')
    return
  }
  exportCsv(records, `交接日志-${dayjs().format('YYYYMMDD-HHmmss')}.csv`)
  message.success('CSV导出成功')
}

async function exportExcelData() {
  const records = await loadExportRecords()
  if (!records.length) {
    message.warning('暂无可导出数据')
    return
  }
  exportExcel(records, `交接日志-${dayjs().format('YYYYMMDD-HHmmss')}.xls`)
  message.success('Excel导出成功')
}

async function load() {
  loading.value = true
  try {
    const res: PageResult<ShiftHandoverItem> = await getShiftHandoverPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      dateFrom: query.range?.[0],
      dateTo: query.range?.[1],
      shiftCode: query.shiftCode,
      status: query.status
    })
    rows.value = res.list.map((item) => normalizeLoadedRow(item))
    query.total = res.total
    await autoOpenFromRoute()
  } catch (error) {
    message.error(resolveCareError(error, '加载交接日志失败'))
    rows.value = []
    query.total = 0
  } finally {
    loading.value = false
  }
}

function toIsoDateTime(value?: string) {
  if (!value) return undefined
  return value.replace(' ', 'T')
}

function formatDateTime(value?: string) {
  if (!value) return '-'
  return dayjs(toIsoDateTime(value)).format('YYYY-MM-DD HH:mm:ss')
}

function parseAttachments(value?: string) {
  if (!value) return []
  const text = String(value).trim()
  if (!text) return []
  try {
    const parsed = JSON.parse(text)
    if (!Array.isArray(parsed)) return []
    return parsed
      .map((item: any) => {
        if (typeof item === 'string') {
          return { name: item.split('/').pop() || '附件', url: item }
        }
        const url = item?.url || item?.fileUrl || ''
        if (!url) return null
        return {
          name: item?.name || item?.originalFileName || item?.fileName || url.split('/').pop() || '附件',
          url
        }
      })
      .filter((item): item is HandoverAttachmentFile => !!item?.url)
  } catch {
    return text
      .split(',')
      .map((item) => item.trim())
      .filter((item) => !!item)
      .map((url) => ({ name: url.split('/').pop() || '附件', url }))
  }
}

function serializeAttachments(list: HandoverAttachmentFile[]) {
  if (!list.length) return ''
  return JSON.stringify(list.map((item) => ({ name: item.name, url: item.url })))
}

function countAttachments(value?: string) {
  return parseAttachments(value).length
}

function openAttachment(url: string) {
  if (!url) return
  window.open(url, '_blank')
}

function openAttachments(value?: string) {
  parseAttachments(value).forEach((item) => openAttachment(item.url))
}

function removeAttachment(index: number) {
  attachmentFiles.value.splice(index, 1)
}

function resolveRowClassName(record: ShiftHandoverItem) {
  const noteText = `${record.riskNote || record.attentionNote || ''} ${record.todoNote || ''}`
  const riskKeywords = getHandoverRiskKeywords()
  if (record.status === 'HANDED_OVER' && !record.confirmTime) {
    return 'handover-row-warning'
  }
  if (riskKeywords.some((keyword) => noteText.includes(keyword))) {
    return 'handover-row-danger'
  }
  return ''
}

async function loadExportRecords() {
  exporting.value = true
  try {
    const pageSize = 500
    let pageNo = 1
    let total = 0
    const list: ShiftHandoverItem[] = []
    do {
      const page = await getShiftHandoverPage({
        pageNo,
        pageSize,
        dateFrom: query.range?.[0],
        dateTo: query.range?.[1],
        shiftCode: query.shiftCode,
        status: query.status
      }) as PageResult<ShiftHandoverItem>
      total = page.total || 0
      list.push(...(page.list || []))
      pageNo += 1
      if (!page.list || page.list.length < pageSize) break
    } while (list.length < total && pageNo <= 20)
    return mapCareExportRows(
      list.map((item) => ({
        ...item,
        status: statusLabel(item.status),
        handoverTime: formatDateTime(item.handoverTime),
        confirmTime: formatDateTime(item.confirmTime),
        attachmentUrls: parseAttachments(item.attachmentUrls).map((file) => file.url).join('；')
      })),
      handoverExportColumns
    )
  } catch (error) {
    message.error(resolveCareError(error, '加载导出数据失败'))
    return []
  } finally {
    exporting.value = false
  }
}

async function beforeUploadAttachment(file: File) {
  const maxSizeMb = 10
  if (attachmentFiles.value.length >= 10) {
    message.warning('最多上传10个附件')
    return false
  }
  if (file.size / 1024 / 1024 > maxSizeMb) {
    message.error(`附件大小不能超过${maxSizeMb}MB`)
    return false
  }
  uploadingAttachment.value = true
  try {
    const uploaded = await uploadOaFile(file, 'nursing-handover-attachment')
    const url = uploaded?.fileUrl || ''
    if (!url) {
      message.error('上传失败：未返回附件地址')
      return false
    }
    attachmentFiles.value.push({
      name: uploaded?.originalFileName || uploaded?.fileName || file.name,
      url
    })
    message.success('附件上传成功')
  } catch (error) {
    message.error(resolveCareError(error, '附件上传失败'))
  } finally {
    uploadingAttachment.value = false
  }
  return false
}

syncQueryFromRoute()
load()
searchStaff('')
loadShiftCodes()
syncMedicalAlertRules().catch(() => {})

watch(
  () => [route.query.id, route.query.status],
  () => {
    lastAutoOpenTaskKey.value = ''
    syncQueryFromRoute()
    load()
  }
)
</script>

<style scoped>
.danger-text {
  color: var(--danger);
}
.upload-tip {
  margin-top: 6px;
  color: var(--muted);
  font-size: 12px;
}
.handover-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 10px;
  color: var(--muted);
  font-size: 12px;
}
.handover-legend__item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.handover-legend__swatch {
  width: 14px;
  height: 14px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-soft);
}
.handover-legend__swatch--warning {
  background: rgba(var(--warning-rgb), 0.35);
}
.handover-legend__swatch--danger {
  background: rgba(var(--danger-rgb), 0.24);
}
:deep(.handover-row-warning > td) {
  background: rgba(var(--warning-rgb), 0.12) !important;
}
:deep(.handover-row-danger > td) {
  background: rgba(var(--danger-rgb), 0.08) !important;
}
.shift-code-empty {
  padding: 8px 4px;
  color: var(--muted);
  font-size: 12px;
}
</style>
