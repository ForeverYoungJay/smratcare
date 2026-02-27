<template>
  <PageContainer title="合同签约" sub-title="营销建合同 -> 长者管理入住评估 -> 入住办理 -> 最终签署">
    <a-card class="card-elevated" :bordered="false">
      <a-row :gutter="12">
        <a-col :span="6">
          <div class="board-item">
            <div class="board-title">待评估 <a-badge :count="stageBoard.pendingAssessmentOverdue" color="#ff4d4f" /></div>
            <div class="board-value">{{ stageBoard.pendingAssessment }}</div>
          </div>
        </a-col>
        <a-col :span="6">
          <div class="board-item">
            <div class="board-title">待办理入住</div>
            <div class="board-value">{{ stageBoard.pendingBedSelect }}</div>
          </div>
        </a-col>
        <a-col :span="6">
          <div class="board-item">
            <div class="board-title">待签署 <a-badge :count="stageBoard.pendingSignOverdue" color="#ff4d4f" /></div>
            <div class="board-value">{{ stageBoard.pendingSign }}</div>
          </div>
        </a-col>
        <a-col :span="6">
          <div class="board-item">
            <div class="board-title">已签署</div>
            <div class="board-value">{{ stageBoard.signed }}</div>
          </div>
        </a-col>
      </a-row>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <a-form :model="query" layout="inline" class="search-bar">
        <a-form-item label="合同编号">
          <a-input v-model:value="query.contractNo" placeholder="请输入 合同编号" allow-clear />
        </a-form-item>
        <a-form-item label="姓名">
          <a-input v-model:value="query.elderName" placeholder="请输入 姓名" allow-clear />
        </a-form-item>
        <a-form-item label="联系电话">
          <a-input v-model:value="query.elderPhone" placeholder="请输入 联系电话" allow-clear />
        </a-form-item>
        <a-form-item label="营销人员">
          <a-input v-model:value="query.marketerName" placeholder="请输入 营销人员" allow-clear />
        </a-form-item>
        <a-form-item label="流程阶段">
          <a-select v-model:value="query.flowStage" style="width: 150px" allow-clear>
            <a-select-option value="PENDING_ASSESSMENT">待评估</a-select-option>
            <a-select-option value="PENDING_BED_SELECT">待办理入住</a-select-option>
            <a-select-option value="PENDING_SIGN">待签署</a-select-option>
            <a-select-option value="SIGNED">已签署</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="仅看我负责">
          <a-switch v-model:checked="onlyMineDept" @change="onMineSwitchChange" />
        </a-form-item>
        <a-form-item label="责任部门">
          <a-segmented
            v-model:value="mineDept"
            :options="mineDeptOptions"
            :disabled="!onlyMineDept"
            @change="onMineDeptChange"
          />
        </a-form-item>
        <a-form-item label="只看超时">
          <a-switch v-model:checked="onlyOverdue" @change="onOverdueSwitchChange" />
        </a-form-item>
        <a-form-item label="超时排序">
          <a-switch v-model:checked="sortByOverdue" @change="onSortSwitchChange" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchData">搜 索</a-button>
            <a-button @click="reset">清 空</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <div class="table-actions">
        <a-space>
          <a-button type="primary" @click="openForm()">新增合同</a-button>
          <a-button @click="batchApprove">审批</a-button>
          <a-button danger @click="batchDelete">删除</a-button>
        </a-space>
      </div>
      <a-table
        :data-source="tableRows"
        :columns="columns"
        :loading="loading"
        :pagination="false"
        row-key="id"
        :row-selection="rowSelection"
        :scroll="{ x: 2400 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'flowStage'">
            <a-tag :color="flowStageColor(normalizedFlowStage(record))">{{ flowStageText(normalizedFlowStage(record)) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'slaWarning'">
            <a-tag v-if="getOverdueLevel(record) === 'high'" color="red">{{ overdueText(record) }}</a-tag>
            <span v-else>-</span>
          </template>
          <template v-else-if="column.key === 'currentOwnerDept'">
            <a-tag>{{ ownerDeptText(normalizedOwnerDept(record)) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'operation'">
            <a-space wrap>
              <a-button type="link" @click="view(record)">查看</a-button>
              <a-button type="link" @click="openForm(record)">编辑</a-button>
              <a-button type="link" @click="openAttachment(record)">附件</a-button>
              <a-button type="link" @click="startAdmissionAssessment(record)">移交评估部</a-button>
              <a-button type="link" @click="goAdmissionProcessing(record)">入住办理</a-button>
              <a-tooltip
                v-if="normalizedFlowStage(record) !== 'SIGNED'"
                :title="isFinalizeDisabled(record) ? finalizeDisabledReason(record) : null"
              >
                <span>
                  <a-button type="link" :disabled="isFinalizeDisabled(record)" @click="openFinalize(record)">最终签署</a-button>
                </span>
              </a-tooltip>
            </a-space>
          </template>
        </template>
      </a-table>
      <a-pagination
        style="margin-top: 16px; text-align: right;"
        :current="query.pageNo"
        :page-size="query.pageSize"
        :total="displayTotal"
        show-size-changer
        @change="onPageChange"
        @showSizeChange="onPageSizeChange"
      />
    </a-card>

    <a-modal v-model:open="open" :title="form.id ? '编辑合同' : '新增合同'" width="760px" :confirm-loading="submitting" @ok="submit">
      <a-form ref="formRef" :model="form" :rules="rules" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="合同编号" name="contractNo">
              <a-input :value="form.contractNo" disabled placeholder="保存后后端自动生成（gfyy+年月日+编号）" />
            </a-form-item>
          </a-col>
          <a-col :span="12"><a-form-item label="签约房号"><a-input v-model:value="form.reservationRoomNo" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="姓名" name="elderName"><a-input v-model:value="form.elderName" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="联系电话"><a-input v-model:value="form.elderPhone" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="性别"><a-select v-model:value="form.gender" allow-clear><a-select-option :value="1">男</a-select-option><a-select-option :value="2">女</a-select-option></a-select></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="年龄"><a-input-number v-model:value="form.age" :min="0" :max="120" style="width: 100%" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="签约日期"><a-date-picker v-model:value="form.contractSignedAt" value-format="YYYY-MM-DD HH:mm:ss" show-time style="width: 100%" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="合同有效期止"><a-date-picker v-model:value="form.contractExpiryDate" value-format="YYYY-MM-DD" style="width: 100%" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="合同状态"><a-input v-model:value="form.contractStatus" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="营销人员"><a-input v-model:value="form.marketerName" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="优惠政策"><a-input v-model:value="form.orgName" placeholder="请输入优惠政策" /></a-form-item></a-col>
        </a-row>
      </a-form>
    </a-modal>

    <a-modal v-model:open="attachmentOpen" title="合同附件（病历/医保/户口等）" width="860px" :footer="null">
      <a-space style="margin-bottom: 12px;" wrap>
        <a-select v-model:value="attachmentType" style="width: 180px">
          <a-select-option value="MEDICAL_RECORD">病历资料</a-select-option>
          <a-select-option value="MEDICAL_INSURANCE">医保复印件</a-select-option>
          <a-select-option value="HOUSEHOLD">户口复印件</a-select-option>
          <a-select-option value="CONTRACT">合同附件</a-select-option>
          <a-select-option value="OTHER">其他附件</a-select-option>
        </a-select>
        <a-upload
          :show-upload-list="false"
          :custom-request="handleUpload"
          :before-upload="beforeUpload"
          :accept="uploadAccept"
        >
          <a-button type="primary" :loading="attachmentSubmitting">上传附件</a-button>
        </a-upload>
      </a-space>
      <a-table :data-source="attachments" :columns="attachmentColumns" :pagination="false" row-key="id" size="small">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'preview'">
            <a-image
              v-if="isImageAttachment(record)"
              :src="record.fileUrl"
              :width="40"
              :height="40"
              style="object-fit: cover; border-radius: 4px;"
            />
            <span v-else>-</span>
          </template>
          <template v-else-if="column.key === 'remark'">
            {{ attachmentTypeLabel(record.remark) }}
          </template>
          <template v-else-if="column.key === 'fileUrl'">
            <a :href="record.fileUrl" target="_blank">{{ record.fileName }}</a>
          </template>
          <template v-else-if="column.key === 'fileSize'">
            {{ formatFileSize(record.fileSize) }}
          </template>
          <template v-else-if="column.key === 'operation'">
            <a-button type="link" danger @click="removeAttachment(record.id)">删除</a-button>
          </template>
        </template>
      </a-table>
    </a-modal>

    <a-modal v-model:open="finalizeOpen" title="最终签署" width="620px" :confirm-loading="finalizing" @ok="submitFinalize">
      <a-form layout="vertical" :model="finalizeForm">
        <a-alert
          type="info"
          show-icon
          style="margin-bottom: 12px;"
          :message="`请先在“入住办理”完成选床和办理，再执行最终签署。`"
          :description="`当前合同：${finalizeForm.contractNo || '-'}；长者：${finalizeForm.elderName || '-'}。`"
        />
        <a-form-item label="签署备注">
          <a-input v-model:value="finalizeForm.remark" placeholder="可填写最终签署说明" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'ant-design-vue'
import { message, Modal } from 'ant-design-vue'
import dayjs from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import {
  batchDeleteLeads,
  createCrmLead,
  createLeadAttachment,
  deleteLeadAttachment,
  getLeadAttachments,
  getLeadPage,
  updateCrmLead,
  uploadMarketingFile
} from '../../api/marketing'
import { createElder, getElderPage, updateElder } from '../../api/elder'
import { getAdmissionRecords } from '../../api/elderLifecycle'
import type { ContractAttachmentItem, CrmLeadItem, ElderItem, PageResult } from '../../types'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const open = ref(false)
const formRef = ref<FormInstance>()
const rows = ref<CrmLeadItem[]>([])
const localRows = ref<CrmLeadItem[]>([])
const total = ref(0)
const selectedRowKeys = ref<number[]>([])
const onlyMineDept = ref(false)
const mineDept = ref<'MARKETING' | 'ASSESSMENT'>('MARKETING')
const onlyOverdue = ref(false)
const sortByOverdue = ref(false)

const stageBoard = reactive({
  pendingAssessment: 0,
  pendingBedSelect: 0,
  pendingSign: 0,
  signed: 0,
  pendingAssessmentOverdue: 0,
  pendingSignOverdue: 0
})

const mineDeptOptions = [
  { label: '营销部', value: 'MARKETING' },
  { label: '评估部', value: 'ASSESSMENT' }
]

const query = reactive({
  contractNo: '',
  elderName: '',
  elderPhone: '',
  marketerName: '',
  flowStage: undefined as CrmLeadItem['flowStage'] | undefined,
  pageNo: 1,
  pageSize: 10
})

const form = reactive<Partial<CrmLeadItem>>({})
const rules: FormRules = {
  elderName: [{ required: true, message: '请输入姓名' }]
}

const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: (number | string)[]) => {
    selectedRowKeys.value = keys.map((item) => Number(item))
  }
}))

const columns = [
  { title: '合同编号', dataIndex: 'contractNo', key: 'contractNo', width: 170 },
  { title: '流程阶段', dataIndex: 'flowStage', key: 'flowStage', width: 120 },
  { title: '超时预警', dataIndex: 'slaWarning', key: 'slaWarning', width: 150 },
  { title: '当前责任部门', dataIndex: 'currentOwnerDept', key: 'currentOwnerDept', width: 130 },
  { title: '签约房号', dataIndex: 'reservationRoomNo', key: 'reservationRoomNo', width: 140 },
  { title: '姓名', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '联系电话', dataIndex: 'elderPhone', key: 'elderPhone', width: 140 },
  { title: '营销人员', dataIndex: 'marketerName', key: 'marketerName', width: 120 },
  { title: '合同状态', dataIndex: 'contractStatus', key: 'contractStatus', width: 130 },
  { title: '操作', key: 'operation', fixed: 'right', width: 520 }
]

const attachmentOpen = ref(false)
const attachmentSubmitting = ref(false)
const attachmentType = ref<'MEDICAL_RECORD' | 'MEDICAL_INSURANCE' | 'HOUSEHOLD' | 'CONTRACT' | 'OTHER'>('CONTRACT')
const currentAttachmentLead = ref<CrmLeadItem>()
const attachments = ref<ContractAttachmentItem[]>([])
const uploadAccept = '.jpg,.jpeg,.png,.gif,.webp,.pdf,.doc,.docx,.xls,.xlsx,.txt,.zip'
const attachmentColumns = [
  { title: '预览', dataIndex: 'preview', key: 'preview', width: 80 },
  { title: '类型', dataIndex: 'remark', key: 'remark', width: 120 },
  { title: '文件名', dataIndex: 'fileName', key: 'fileName', width: 200 },
  { title: '文件链接', dataIndex: 'fileUrl', key: 'fileUrl', width: 280 },
  { title: '文件类型', dataIndex: 'fileType', key: 'fileType', width: 120 },
  { title: '文件大小', dataIndex: 'fileSize', key: 'fileSize', width: 100 },
  { title: '上传时间', dataIndex: 'createTime', key: 'createTime', width: 160 },
  { title: '操作', key: 'operation', width: 80 }
]

const finalizeOpen = ref(false)
const finalizing = ref(false)
const finalizeLead = ref<CrmLeadItem>()
const finalizeReadyMap = ref<Record<number, boolean>>({})
const finalizeCheckingMap = ref<Record<number, boolean>>({})
const finalizeForm = reactive({
  contractNo: '',
  elderName: '',
  elderId: 0,
  remark: ''
})

const processedRows = computed(() => {
  let list = onlyOverdue.value
    ? localRows.value.filter((item) => getOverdueLevel(item) === 'high')
    : [...localRows.value]
  if (sortByOverdue.value) {
    list = list.sort((a, b) => overdueHours(b) - overdueHours(a))
  }
  return list
})

const tableRows = computed(() => {
  if (!onlyOverdue.value && !sortByOverdue.value) {
    return rows.value
  }
  const start = (query.pageNo - 1) * query.pageSize
  return processedRows.value.slice(start, start + query.pageSize)
})

const displayTotal = computed(() => {
  if (!onlyOverdue.value && !sortByOverdue.value) {
    return total.value
  }
  return processedRows.value.length
})

function normalizedFlowStage(record: CrmLeadItem): CrmLeadItem['flowStage'] {
  return (record.flowStage || 'PENDING_ASSESSMENT') as CrmLeadItem['flowStage']
}

function normalizedOwnerDept(record: CrmLeadItem): CrmLeadItem['currentOwnerDept'] {
  if (record.currentOwnerDept) return record.currentOwnerDept
  return normalizedFlowStage(record) === 'PENDING_ASSESSMENT' ? 'ASSESSMENT' : 'MARKETING'
}

function flowStageText(stage?: CrmLeadItem['flowStage']) {
  if (stage === 'PENDING_ASSESSMENT') return '待评估'
  if (stage === 'PENDING_BED_SELECT') return '待办理入住'
  if (stage === 'PENDING_SIGN') return '待签署'
  if (stage === 'SIGNED') return '已签署'
  return '待评估'
}

function flowStageColor(stage?: CrmLeadItem['flowStage']) {
  if (stage === 'PENDING_ASSESSMENT') return 'gold'
  if (stage === 'PENDING_BED_SELECT') return 'blue'
  if (stage === 'PENDING_SIGN') return 'purple'
  if (stage === 'SIGNED') return 'green'
  return 'default'
}

function ownerDeptText(dept?: CrmLeadItem['currentOwnerDept']) {
  if (dept === 'ASSESSMENT') return '评估部'
  if (dept === 'MARKETING') return '营销部'
  return '-'
}

function hoursFrom(timeText?: string) {
  if (!timeText) return 0
  const t = dayjs(timeText)
  if (!t.isValid()) return 0
  return dayjs().diff(t, 'hour')
}

function overdueHours(record: CrmLeadItem) {
  const stage = normalizedFlowStage(record)
  if (stage === 'PENDING_ASSESSMENT') {
    return hoursFrom(record.createTime)
  }
  if (stage === 'PENDING_SIGN') {
    return hoursFrom(record.updateTime || record.createTime)
  }
  return 0
}

function getOverdueLevel(record: CrmLeadItem): 'none' | 'high' {
  const stage = normalizedFlowStage(record)
  const hours = overdueHours(record)
  if (stage === 'PENDING_ASSESSMENT') {
    return hours >= 24 ? 'high' : 'none'
  }
  if (stage === 'PENDING_SIGN') {
    return hours >= 48 ? 'high' : 'none'
  }
  return 'none'
}

function overdueText(record: CrmLeadItem) {
  const stage = normalizedFlowStage(record)
  const hours = overdueHours(record)
  if (stage === 'PENDING_ASSESSMENT') return `待评估超时 ${hours}h`
  if (stage === 'PENDING_SIGN') return `待签署超时 ${hours}h`
  return ''
}

function isFinalizeDisabled(record: CrmLeadItem) {
  const id = Number(record.id || 0)
  if (!id) return true
  if (finalizeCheckingMap.value[id]) return true
  return !finalizeReadyMap.value[id]
}

function finalizeDisabledReason(record: CrmLeadItem) {
  const id = Number(record.id || 0)
  if (!id) return '合同数据异常，无法签署'
  if (finalizeCheckingMap.value[id]) return '正在校验是否已办理入住...'
  if (!record.contractNo) return '请先保存并生成合同号'
  return '请先在“入住办理”完成办理后再签署'
}

async function checkFinalizeReady(record: CrmLeadItem) {
  if (!record.id || !record.contractNo) return false
  const admissionPage = await getAdmissionRecords({
    pageNo: 1,
    pageSize: 10,
    contractNo: record.contractNo,
    keyword: record.elderName || record.name
  })
  return (admissionPage.list || []).length > 0
}

async function refreshFinalizeReady(rowsForCheck: CrmLeadItem[]) {
  const targets = rowsForCheck.filter((item) => normalizedFlowStage(item) !== 'SIGNED')
  await Promise.all(targets.map(async (item) => {
    const id = Number(item.id || 0)
    if (!id) return
    finalizeCheckingMap.value[id] = true
    try {
      finalizeReadyMap.value[id] = await checkFinalizeReady(item)
    } catch {
      finalizeReadyMap.value[id] = false
    } finally {
      finalizeCheckingMap.value[id] = false
    }
  }))
}

async function fetchBoardSummary() {
  const stages: CrmLeadItem['flowStage'][] = ['PENDING_ASSESSMENT', 'PENDING_BED_SELECT', 'PENDING_SIGN', 'SIGNED']
  const [a, b, c, d] = await Promise.all(stages.map((flowStage) => getLeadPage({
    pageNo: 1,
    pageSize: 1,
    status: 2,
    flowStage,
    currentOwnerDept: onlyMineDept.value ? mineDept.value : undefined
  })))
  stageBoard.pendingAssessment = a.total || 0
  stageBoard.pendingBedSelect = b.total || 0
  stageBoard.pendingSign = c.total || 0
  stageBoard.signed = d.total || 0
  const [assessmentOverdue, signOverdue] = await Promise.all([
    fetchOverdueCount('PENDING_ASSESSMENT'),
    fetchOverdueCount('PENDING_SIGN')
  ])
  stageBoard.pendingAssessmentOverdue = assessmentOverdue
  stageBoard.pendingSignOverdue = signOverdue
}

async function fetchData() {
  loading.value = true
  try {
    if (onlyOverdue.value || sortByOverdue.value) {
      localRows.value = await fetchAllLeadRows()
      rows.value = []
      total.value = 0
    } else {
      const page: PageResult<CrmLeadItem> = await getLeadPage({
        pageNo: query.pageNo,
        pageSize: query.pageSize,
        status: 2,
        contractNo: query.contractNo || undefined,
        elderName: query.elderName || undefined,
        elderPhone: query.elderPhone || undefined,
        marketerName: query.marketerName || undefined,
        flowStage: query.flowStage || undefined,
        currentOwnerDept: onlyMineDept.value ? mineDept.value : undefined
      })
      rows.value = page.list || []
      total.value = page.total || 0
      localRows.value = []
    }
    await refreshFinalizeReady(tableRows.value)
    await fetchBoardSummary()
  } finally {
    loading.value = false
  }
}

async function fetchAllLeadRows() {
  const pageSize = 200
  let pageNo = 1
  let all: CrmLeadItem[] = []
  let totalRows = 0
  do {
    const page: PageResult<CrmLeadItem> = await getLeadPage({
      pageNo,
      pageSize,
      status: 2,
      contractNo: query.contractNo || undefined,
      elderName: query.elderName || undefined,
      elderPhone: query.elderPhone || undefined,
      marketerName: query.marketerName || undefined,
      flowStage: query.flowStage || undefined,
      currentOwnerDept: onlyMineDept.value ? mineDept.value : undefined
    })
    const list = page.list || []
    all = all.concat(list)
    totalRows = page.total || 0
    pageNo += 1
    if (pageNo > 50) break
  } while (all.length < totalRows)
  return all
}

async function fetchOverdueCount(flowStage: 'PENDING_ASSESSMENT' | 'PENDING_SIGN') {
  const pageSize = 200
  let pageNo = 1
  let totalRows = 0
  let count = 0
  do {
    const page: PageResult<CrmLeadItem> = await getLeadPage({
      pageNo,
      pageSize,
      status: 2,
      flowStage,
      currentOwnerDept: onlyMineDept.value ? mineDept.value : undefined
    })
    const list = page.list || []
    count += list.filter((item) => getOverdueLevel(item) === 'high').length
    totalRows = page.total || 0
    pageNo += 1
    if (pageNo > 50) break
  } while ((pageNo - 1) * pageSize < totalRows)
  return count
}

function reset() {
  query.contractNo = ''
  query.elderName = ''
  query.elderPhone = ''
  query.marketerName = ''
  query.flowStage = undefined
  onlyMineDept.value = false
  mineDept.value = 'MARKETING'
  query.pageNo = 1
  onlyOverdue.value = false
  sortByOverdue.value = false
  selectedRowKeys.value = []
  fetchData()
}

function onPageChange(page: number) {
  query.pageNo = page
  if (!onlyOverdue.value && !sortByOverdue.value) {
    fetchData()
  }
}

function onPageSizeChange(_current: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
  if (!onlyOverdue.value && !sortByOverdue.value) {
    fetchData()
  }
}

function onOverdueSwitchChange() {
  query.pageNo = 1
  fetchData()
}

function onSortSwitchChange() {
  query.pageNo = 1
  fetchData()
}

function onMineSwitchChange() {
  query.pageNo = 1
  fetchData()
}

function onMineDeptChange() {
  if (!onlyMineDept.value) return
  query.pageNo = 1
  fetchData()
}

function openForm(record?: CrmLeadItem) {
  Object.keys(form).forEach((key) => {
    delete (form as Record<string, any>)[key]
  })
  if (record) {
    Object.assign(form, record)
  } else {
    Object.assign(form, {
      id: undefined,
      contractNo: undefined,
      status: 2,
      contractSignedFlag: 0,
      contractSignedAt: dayjs().format('YYYY-MM-DD HH:mm:ss'),
      contractStatus: '待评估',
      flowStage: 'PENDING_ASSESSMENT',
      currentOwnerDept: 'ASSESSMENT',
      orgName: '新签优惠待确认'
    } as Partial<CrmLeadItem>)
  }
  open.value = true
}

async function submit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitting.value = true
    const payload: Partial<CrmLeadItem> = {
      ...form,
      name: form.elderName || form.name || '签约客户',
      phone: form.elderPhone || form.phone,
      contractNo: form.id ? form.contractNo : undefined,
      status: 2,
      contractSignedFlag: 0,
      flowStage: form.flowStage || 'PENDING_ASSESSMENT',
      currentOwnerDept: form.currentOwnerDept || 'ASSESSMENT',
      contractStatus: form.contractStatus || '待评估'
    }
    let saved: CrmLeadItem
    if (form.id) {
      saved = await updateCrmLead(form.id, payload)
    } else {
      saved = await createCrmLead(payload)
    }
    message.success(`保存成功，合同号：${saved?.contractNo || '生成中'}`)
    open.value = false
    await fetchData()
  } catch (error: any) {
    message.error(error?.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

async function batchApprove() {
  const ids = selectedRowKeys.value
  if (!ids.length) {
    message.info('请先勾选合同')
    return
  }
  await Promise.all(ids.map((id) => {
    const row = rows.value.find((item) => item.id === id)
    if (!row) return Promise.resolve()
    return updateCrmLead(row.id, { ...row, contractStatus: '已审批' })
  }))
  message.success(`已审批 ${ids.length} 条合同`)
  fetchData()
}

function batchDelete() {
  const ids = selectedRowKeys.value
  if (!ids.length) {
    message.info('请先勾选要删除的合同')
    return
  }
  Modal.confirm({
    title: `确认删除 ${ids.length} 条合同记录吗？`,
    onOk: async () => {
      await batchDeleteLeads({ ids })
      selectedRowKeys.value = []
      message.success('删除成功')
      fetchData()
    }
  })
}

function view(record: CrmLeadItem) {
  Modal.info({
    title: '合同详情',
    content: `${record.contractNo || '-'} / ${record.elderName || '-'} / ${flowStageText(normalizedFlowStage(record))} / ${ownerDeptText(normalizedOwnerDept(record))}`
  })
}

async function startAdmissionAssessment(record: CrmLeadItem) {
  try {
    const lead = await ensureContractNo(record)
    const handed = await updateCrmLead(lead.id, {
      ...lead,
      flowStage: 'PENDING_ASSESSMENT',
      currentOwnerDept: 'ASSESSMENT',
      contractStatus: '待评估',
      status: 2
    })
    const elder = await ensureElderFromLead(handed)
    await fetchData()
    message.success('已移交评估部，请在长者管理完成入住评估')
    router.push(`/elder/admission-assessment?residentId=${elder.id}&leadId=${handed.id}&contractNo=${handed.contractNo || ''}`)
  } catch (error: any) {
    message.error(error?.message || '移交评估部失败')
  }
}

async function goAdmissionProcessing(record: CrmLeadItem) {
  try {
    const lead = await ensureContractNo(record)
    const elder = await ensureElderFromLead(lead)
    await updateCrmLead(lead.id, {
      ...lead,
      flowStage: 'PENDING_BED_SELECT',
      currentOwnerDept: 'MARKETING',
      contractStatus: '待办理入住'
    })
    router.push(`/elder/admission-processing?residentId=${elder.id}&leadId=${lead.id}&contractNo=${lead.contractNo || ''}`)
  } catch (error: any) {
    message.error(error?.message || '跳转入住办理失败')
  }
}

async function openFinalize(record: CrmLeadItem) {
  try {
    const lead = await ensureContractNo(record)
    const elder = await ensureElderFromLead(lead)
    const admissionPage = await getAdmissionRecords({
      pageNo: 1,
      pageSize: 20,
      keyword: elder.fullName,
      contractNo: lead.contractNo
    })
    const hasAdmission = (admissionPage.list || []).some((item) => item.elderId === elder.id)
    if (!hasAdmission) {
      message.warning('请先在“入住办理”完成办理，再执行最终签署')
      router.push(`/elder/admission-processing?residentId=${elder.id}&leadId=${lead.id}&contractNo=${lead.contractNo || ''}`)
      return
    }
    const signingLead = normalizedFlowStage(lead) === 'PENDING_SIGN'
      ? lead
      : await updateCrmLead(lead.id, {
        ...lead,
        flowStage: 'PENDING_SIGN',
        currentOwnerDept: 'MARKETING',
        contractStatus: '待签署'
      })
    finalizeLead.value = signingLead
    finalizeForm.contractNo = signingLead.contractNo || ''
    finalizeForm.elderName = elder.fullName
    finalizeForm.elderId = elder.id
    finalizeForm.remark = ''
    finalizeOpen.value = true
  } catch (error: any) {
    message.error(error?.message || '进入最终签署失败')
  }
}

async function submitFinalize() {
  if (!finalizeLead.value) return
  finalizing.value = true
  try {
    await updateCrmLead(finalizeLead.value.id, {
      ...finalizeLead.value,
      contractSignedFlag: 1,
      contractSignedAt: dayjs().format('YYYY-MM-DD HH:mm:ss'),
      contractStatus: '已签署',
      reservationStatus: '已入住',
      flowStage: 'SIGNED',
      currentOwnerDept: 'MARKETING',
      remark: finalizeForm.remark || finalizeLead.value.remark,
      status: 2
    })
    message.success('最终签署完成')
    finalizeOpen.value = false
    await fetchData()
  } catch (error: any) {
    message.error(error?.message || '最终签署失败')
  } finally {
    finalizing.value = false
  }
}

async function openAttachment(record: CrmLeadItem) {
  currentAttachmentLead.value = record
  attachments.value = await getLeadAttachments(record.id)
  attachmentOpen.value = true
}

function beforeUpload(file: File) {
  const allowExt = ['jpg', 'jpeg', 'png', 'gif', 'webp', 'pdf', 'doc', 'docx', 'xls', 'xlsx', 'txt', 'zip']
  const ext = file.name?.split('.').pop()?.toLowerCase() || ''
  if (!allowExt.includes(ext)) {
    message.error(`文件类型不支持，仅允许: ${allowExt.join(', ')}`)
    return false
  }
  const maxSize = 10 * 1024 * 1024
  if (file.size > maxSize) {
    message.error('文件大小超限，最大支持 10MB')
    return false
  }
  return true
}

async function handleUpload(option: any) {
  const file = option?.file as File
  if (!file || !currentAttachmentLead.value) {
    option?.onError?.(new Error('未选择合同或文件'))
    return
  }
  attachmentSubmitting.value = true
  try {
    const uploaded = await uploadMarketingFile(file, 'marketing-contract')
    await createLeadAttachment(currentAttachmentLead.value.id, {
      contractNo: currentAttachmentLead.value.contractNo,
      fileName: uploaded.originalFileName || uploaded.fileName,
      fileUrl: uploaded.fileUrl,
      fileType: uploaded.fileType,
      fileSize: uploaded.fileSize,
      remark: attachmentType.value
    })
    attachments.value = await getLeadAttachments(currentAttachmentLead.value.id)
    message.success('附件上传成功')
    option?.onSuccess?.(uploaded)
  } catch (error) {
    option?.onError?.(error)
  } finally {
    attachmentSubmitting.value = false
  }
}

function attachmentTypeLabel(value?: string) {
  if (value === 'MEDICAL_RECORD') return '病历资料'
  if (value === 'MEDICAL_INSURANCE') return '医保复印件'
  if (value === 'HOUSEHOLD') return '户口复印件'
  if (value === 'CONTRACT') return '合同附件'
  if (value === 'OTHER') return '其他附件'
  return value || '未分类'
}

function isImageAttachment(record: ContractAttachmentItem) {
  const type = (record.fileType || '').toLowerCase()
  if (type.startsWith('image/')) return true
  const url = (record.fileUrl || '').toLowerCase()
  return ['.jpg', '.jpeg', '.png', '.gif', '.webp'].some((suffix) => url.endsWith(suffix))
}

function formatFileSize(size?: number) {
  if (size == null || Number.isNaN(size)) return '-'
  if (size < 1024) return `${size}B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)}KB`
  return `${(size / 1024 / 1024).toFixed(2)}MB`
}

async function removeAttachment(attachmentId: number) {
  await deleteLeadAttachment(attachmentId)
  if (currentAttachmentLead.value) {
    attachments.value = await getLeadAttachments(currentAttachmentLead.value.id)
  }
  message.success('附件已删除')
}

function normalizeLeadName(lead: CrmLeadItem) {
  return (lead.elderName || lead.name || '').trim() || '未命名长者'
}

async function ensureContractNo(record: CrmLeadItem) {
  if (record.contractNo) return record
  return updateCrmLead(record.id, {
    ...record,
    status: 2,
    flowStage: normalizedFlowStage(record),
    currentOwnerDept: normalizedOwnerDept(record),
    contractStatus: record.contractStatus || '待评估'
  })
}

function pickLatestAttachment(leadAttachments: ContractAttachmentItem[], type: string) {
  return leadAttachments.find((item) => item.remark === type)
}

async function findExistingElderByLead(lead: CrmLeadItem): Promise<ElderItem | undefined> {
  const page = await getElderPage({ pageNo: 1, pageSize: 300, keyword: normalizeLeadName(lead) })
  const list = page.list || []
  const byIdCard = lead.idCardNo ? list.find((item) => item.idCardNo === lead.idCardNo) : undefined
  if (byIdCard) return byIdCard
  return list.find((item) => item.fullName === normalizeLeadName(lead) && item.phone === (lead.elderPhone || lead.phone))
}

async function ensureElderFromLead(lead: CrmLeadItem): Promise<ElderItem> {
  const leadAttachments = await getLeadAttachments(lead.id)
  const existing = await findExistingElderByLead(lead)
  const elderPayload = {
    fullName: normalizeLeadName(lead),
    idCardNo: lead.idCardNo,
    gender: lead.gender,
    phone: lead.elderPhone || lead.phone,
    homeAddress: lead.homeAddress,
    medicalRecordFileUrl: pickLatestAttachment(leadAttachments, 'MEDICAL_RECORD')?.fileUrl,
    medicalInsuranceCopyUrl: pickLatestAttachment(leadAttachments, 'MEDICAL_INSURANCE')?.fileUrl,
    householdCopyUrl: pickLatestAttachment(leadAttachments, 'HOUSEHOLD')?.fileUrl,
    remark: lead.remark
  }
  if (existing?.id) {
    await updateElder(existing.id, elderPayload)
    return { ...existing, ...elderPayload, id: existing.id }
  }
  return createElder({
    ...elderPayload,
    admissionDate: dayjs().format('YYYY-MM-DD'),
    status: 1
  })
}

onMounted(fetchData)

watch(
  () => tableRows.value.map((item) => `${item.id}-${item.contractNo || ''}`).join(','),
  () => {
    refreshFinalizeReady(tableRows.value)
  }
)
</script>

<style scoped>
.search-bar {
  margin-bottom: 12px;
}

.table-actions {
  margin-bottom: 12px;
}

.board-item {
  padding: 10px 12px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
}

.board-title {
  color: rgba(0, 0, 0, 0.65);
  font-size: 13px;
}

.board-value {
  margin-top: 6px;
  font-size: 24px;
  font-weight: 600;
}
</style>
