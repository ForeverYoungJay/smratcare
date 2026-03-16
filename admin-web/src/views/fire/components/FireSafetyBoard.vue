<template>
  <PageContainer :title="title" :subTitle="subTitle">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" placeholder="标题/区域/问题描述/处置措施" allow-clear style="width: 300px" />
      </a-form-item>
      <a-form-item label="负责人">
        <a-input v-model:value="query.inspectorName" placeholder="请输入检查/值班人" allow-clear style="width: 220px" />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" :options="statusOptions" allow-clear style="width: 140px" />
      </a-form-item>
      <a-form-item label="检查时间">
        <a-range-picker v-model:value="query.checkTimeRange" value-format="YYYY-MM-DDTHH:mm:ss" show-time />
      </a-form-item>
      <template #extra>
        <a-space>
          <a-button v-if="props.recordType === 'MAINTENANCE_REPORT'" @click="downloadMaintenanceLog">一键下载维护保养日志</a-button>
          <a-button v-if="viewConfig.supportsQr" @click="openScan">扫码完成</a-button>
          <a-button type="primary" @click="openCreate">新增记录</a-button>
        </a-space>
      </template>
    </SearchForm>

    <a-row :gutter="[12, 12]">
      <a-col :xs="12" :lg="4"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="记录总数" :value="summary.totalCount || 0" /></a-card></a-col>
      <a-col :xs="12" :lg="4"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="处理中" :value="summary.openCount || 0" /></a-card></a-col>
      <a-col :xs="12" :lg="4"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="已关闭" :value="summary.closedCount || 0" /></a-card></a-col>
      <a-col :xs="12" :lg="4"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="逾期项" :value="summary.overdueCount || 0" /></a-card></a-col>
      <a-col v-if="viewConfig.showNextCheck" :xs="12" :lg="4"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="7天内待检" :value="summary.nextCheckDueSoonCount || 0" /></a-card></a-col>
      <a-col v-if="viewConfig.showProductLifecycle" :xs="12" :lg="4"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="30天内到期" :value="summary.expiringSoonCount || 0" /></a-card></a-col>
    </a-row>

    <a-alert
      v-if="warningMessage"
      type="warning"
      show-icon
      style="margin-top: 12px"
      :message="warningMessage"
    />

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
          <a-tag :color="record.status === 'CLOSED' ? 'green' : 'orange'">
            {{ record.status === 'CLOSED' ? '已关闭' : '处理中' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'qrToken'">
          <a-typography-text v-if="record.qrToken" copyable>
            {{ record.qrToken.slice(0, 12) }}...
          </a-typography-text>
          <span v-else>-</span>
        </template>
        <template v-else-if="column.key === 'scanCompletedAt'">
          {{ record.scanCompletedAt || '-' }}
        </template>
        <template v-else-if="column.key === 'nextCheckDate'">
          {{ record.nextCheckDate || '-' }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space wrap>
            <a-button v-if="viewConfig.supportsQr" type="link" @click="openQr(record)">生成二维码</a-button>
            <a-button type="link" @click="openEdit(record)">编辑</a-button>
            <a-button type="link" :disabled="record.status === 'CLOSED'" @click="closeRecord(record)">关闭</a-button>
            <a-button type="link" danger @click="remove(record)">删除</a-button>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal
      v-model:open="editOpen"
      :title="form.id ? `编辑${title}` : `新增${title}`"
      @ok="submit"
      :confirm-loading="saving"
      width="860px"
    >
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="标题" required>
              <a-input v-model:value="form.title" placeholder="请输入标题" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="区域/位置">
              <a-input v-model:value="form.location" placeholder="如：A栋1层配电间" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="负责人" required>
              <a-input v-model:value="form.inspectorName" placeholder="请输入检查/值班人" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="检查时间" required>
              <a-date-picker v-model:value="form.checkTime" show-time style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row v-if="viewConfig.showDutyRecord || viewConfig.showHandoverPunch" :gutter="16">
          <a-col v-if="viewConfig.showDutyRecord" :span="12">
            <a-form-item label="值班记录">
              <a-textarea v-model:value="form.dutyRecord" :rows="2" />
            </a-form-item>
          </a-col>
          <a-col v-if="viewConfig.showHandoverPunch" :span="12">
            <a-form-item label="交接班打卡时间">
              <a-date-picker v-model:value="form.handoverPunchTime" show-time style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row v-if="viewConfig.showEquipmentBatch || viewConfig.showProductLifecycle" :gutter="16">
          <a-col v-if="viewConfig.showEquipmentBatch" :span="12">
            <a-form-item label="设备批号">
              <a-input v-model:value="form.equipmentBatchNo" placeholder="如：XF-2026-01" />
            </a-form-item>
          </a-col>
          <a-col v-if="viewConfig.showProductLifecycle" :span="12">
            <a-form-item label="产品生产时间">
              <a-date-picker v-model:value="form.productProductionDate" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row v-if="viewConfig.showProductLifecycle" :gutter="16">
          <a-col :span="12">
            <a-form-item label="产品过期时间">
              <a-date-picker v-model:value="form.productExpiryDate" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="检查周期(天)">
              <a-input-number v-model:value="form.checkCycleDays" :min="1" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row v-if="viewConfig.showEquipmentUpdateNote || viewConfig.showEquipmentAgingDisposal" :gutter="16">
          <a-col v-if="viewConfig.showEquipmentUpdateNote" :span="12">
            <a-form-item label="设备更新记录">
              <a-textarea v-model:value="form.equipmentUpdateNote" :rows="2" />
            </a-form-item>
          </a-col>
          <a-col v-if="viewConfig.showEquipmentAgingDisposal" :span="12">
            <a-form-item label="设备老化处置记录">
              <a-textarea v-model:value="form.equipmentAgingDisposal" :rows="2" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="问题描述">
          <a-textarea v-model:value="form.issueDescription" :rows="3" />
        </a-form-item>
        <a-form-item label="处置措施">
          <a-textarea v-model:value="form.actionTaken" :rows="3" />
        </a-form-item>

        <a-row :gutter="16">
          <a-col v-if="viewConfig.showNextCheck" :span="12">
            <a-form-item label="下次检查日期">
              <a-date-picker v-model:value="form.nextCheckDate" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="viewConfig.showNextCheck ? 12 : 24">
            <a-form-item label="状态">
              <a-select v-model:value="form.status" :options="statusOptions" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>

    <a-modal v-if="viewConfig.supportsQr" v-model:open="qrOpen" title="巡查二维码" :footer="null" width="520px">
      <a-space direction="vertical" style="width: 100%" align="center">
        <img v-if="qrDataUrl" :src="qrDataUrl" alt="巡查二维码" style="width: 240px; height: 240px" />
        <a-typography-text copyable>{{ qrText }}</a-typography-text>
        <a-typography-text type="secondary">扫码后自动完成该条巡查记录并闭环。</a-typography-text>
      </a-space>
    </a-modal>

    <a-modal v-if="viewConfig.supportsQr" v-model:open="scanOpen" title="扫码完成巡查" :footer="null" width="560px">
      <a-form layout="vertical">
        <a-form-item label="二维码令牌" required>
          <a-input
            ref="scanInputRef"
            v-model:value="scanForm.qrToken"
            placeholder="扫码枪扫码后会自动提交，无需手点确认"
            @pressEnter="onScannerConfirm(scanForm.qrToken)"
          />
        </a-form-item>
        <a-form-item label="执行人">
          <a-input v-model:value="scanForm.inspectorName" placeholder="可选，覆盖负责人" />
        </a-form-item>
        <a-form-item label="处置备注">
          <a-textarea v-model:value="scanForm.actionTaken" :rows="3" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" :loading="scanSaving" @click="submitScan">手动提交（备用）</a-button>
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, reactive, ref } from 'vue'
import dayjs, { Dayjs } from 'dayjs'
import QRCode from 'qrcode'
import { message } from 'ant-design-vue'
import PageContainer from '../../../components/PageContainer.vue'
import SearchForm from '../../../components/SearchForm.vue'
import DataTable from '../../../components/DataTable.vue'
import {
  closeFireSafetyRecord,
  completeFireSafetyByScan,
  createFireSafetyRecord,
  deleteFireSafetyRecord,
  exportFireSafetyMaintenanceLog,
  generateFireSafetyQr,
  getFireSafetyRecordPage,
  getFireSafetySummary,
  updateFireSafetyRecord
} from '../../../api/fire'
import type { FireSafetyRecord, FireSafetyRecordType, FireSafetyReportSummary, FireSafetyStatus } from '../../../types'

type FireBoardViewConfig = {
  showDutyRecord: boolean
  showHandoverPunch: boolean
  showEquipmentBatch: boolean
  showProductLifecycle: boolean
  showEquipmentUpdateNote: boolean
  showEquipmentAgingDisposal: boolean
  showNextCheck: boolean
  supportsQr: boolean
}

const props = defineProps<{
  title: string
  subTitle: string
  recordType: FireSafetyRecordType
}>()

const loading = ref(false)
const rows = ref<FireSafetyRecord[]>([])
const query = reactive({
  keyword: undefined as string | undefined,
  inspectorName: undefined as string | undefined,
  status: undefined as FireSafetyStatus | undefined,
  checkTimeRange: undefined as [string, string] | undefined,
  pageNo: 1,
  pageSize: 10
})
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const defaultSummary = (): FireSafetyReportSummary => ({
  totalCount: 0,
  openCount: 0,
  closedCount: 0,
  overdueCount: 0,
  dailyCompletedCount: 0,
  monthlyCompletedCount: 0,
  dutyRecordCount: 0,
  handoverPunchCount: 0,
  equipmentUpdateCount: 0,
  equipmentAgingDisposalCount: 0,
  expiringSoonCount: 0,
  nextCheckDueSoonCount: 0,
  typeStats: []
})

const summary = reactive<FireSafetyReportSummary>(defaultSummary())

const viewConfigMap: Record<FireSafetyRecordType, FireBoardViewConfig> = {
  FACILITY: {
    showDutyRecord: false,
    showHandoverPunch: false,
    showEquipmentBatch: true,
    showProductLifecycle: true,
    showEquipmentUpdateNote: true,
    showEquipmentAgingDisposal: true,
    showNextCheck: true,
    supportsQr: false
  },
  CONTROL_ROOM_DUTY: {
    showDutyRecord: true,
    showHandoverPunch: true,
    showEquipmentBatch: false,
    showProductLifecycle: false,
    showEquipmentUpdateNote: false,
    showEquipmentAgingDisposal: false,
    showNextCheck: false,
    supportsQr: false
  },
  MONTHLY_CHECK: {
    showDutyRecord: false,
    showHandoverPunch: false,
    showEquipmentBatch: false,
    showProductLifecycle: false,
    showEquipmentUpdateNote: false,
    showEquipmentAgingDisposal: false,
    showNextCheck: true,
    supportsQr: true
  },
  DAY_PATROL: {
    showDutyRecord: false,
    showHandoverPunch: false,
    showEquipmentBatch: false,
    showProductLifecycle: false,
    showEquipmentUpdateNote: false,
    showEquipmentAgingDisposal: false,
    showNextCheck: true,
    supportsQr: true
  },
  NIGHT_PATROL: {
    showDutyRecord: false,
    showHandoverPunch: false,
    showEquipmentBatch: false,
    showProductLifecycle: false,
    showEquipmentUpdateNote: false,
    showEquipmentAgingDisposal: false,
    showNextCheck: true,
    supportsQr: true
  },
  MAINTENANCE_REPORT: {
    showDutyRecord: false,
    showHandoverPunch: false,
    showEquipmentBatch: true,
    showProductLifecycle: true,
    showEquipmentUpdateNote: true,
    showEquipmentAgingDisposal: false,
    showNextCheck: false,
    supportsQr: false
  },
  FAULT_MAINTENANCE: {
    showDutyRecord: false,
    showHandoverPunch: false,
    showEquipmentBatch: true,
    showProductLifecycle: false,
    showEquipmentUpdateNote: false,
    showEquipmentAgingDisposal: false,
    showNextCheck: false,
    supportsQr: false
  }
}

const viewConfig = computed(() => viewConfigMap[props.recordType])

const warningMessage = computed(() => {
  const warnings: string[] = []
  if ((summary.overdueCount || 0) > 0) {
    warnings.push(`逾期 ${summary.overdueCount || 0} 项`)
  }
  if (viewConfig.value.showProductLifecycle && (summary.expiringSoonCount || 0) > 0) {
    warnings.push(`30天内到期 ${summary.expiringSoonCount || 0} 项`)
  }
  if (warnings.length === 0) {
    return ''
  }
  return `异常提醒：${warnings.join('，')}，请优先处理。`
})

const columns = computed(() => {
  const items: Array<Record<string, unknown>> = [
    { title: '标题', dataIndex: 'title', key: 'title', width: 180 },
    { title: '区域/位置', dataIndex: 'location', key: 'location', width: 150 },
    { title: '负责人', dataIndex: 'inspectorName', key: 'inspectorName', width: 100 },
    { title: '检查时间', dataIndex: 'checkTime', key: 'checkTime', width: 170 }
  ]

  if (viewConfig.value.showDutyRecord) {
    items.push({ title: '值班记录', dataIndex: 'dutyRecord', key: 'dutyRecord', width: 220 })
  }
  if (viewConfig.value.showHandoverPunch) {
    items.push({ title: '交接班打卡', dataIndex: 'handoverPunchTime', key: 'handoverPunchTime', width: 160 })
  }
  if (viewConfig.value.showEquipmentBatch) {
    items.push({ title: '设备批号', dataIndex: 'equipmentBatchNo', key: 'equipmentBatchNo', width: 140 })
  }
  if (viewConfig.value.showProductLifecycle) {
    items.push(
      { title: '生产时间', dataIndex: 'productProductionDate', key: 'productProductionDate', width: 120 },
      { title: '过期时间', dataIndex: 'productExpiryDate', key: 'productExpiryDate', width: 120 },
      { title: '检查周期(天)', dataIndex: 'checkCycleDays', key: 'checkCycleDays', width: 110 }
    )
  }
  if (viewConfig.value.showEquipmentUpdateNote) {
    items.push({ title: '设备更新记录', dataIndex: 'equipmentUpdateNote', key: 'equipmentUpdateNote', width: 200 })
  }
  if (viewConfig.value.showEquipmentAgingDisposal) {
    items.push({ title: '设备老化处置', dataIndex: 'equipmentAgingDisposal', key: 'equipmentAgingDisposal', width: 200 })
  }
  if (viewConfig.value.showNextCheck) {
    items.push({ title: '下次检查日期', dataIndex: 'nextCheckDate', key: 'nextCheckDate', width: 130 })
  }
  if (viewConfig.value.supportsQr) {
    items.push(
      { title: '二维码令牌', dataIndex: 'qrToken', key: 'qrToken', width: 140 },
      { title: '扫码完成时间', dataIndex: 'scanCompletedAt', key: 'scanCompletedAt', width: 160 }
    )
  }

  items.push(
    { title: '状态', dataIndex: 'status', key: 'status', width: 90 },
    { title: '操作', key: 'action', width: viewConfig.value.supportsQr ? 270 : 200 }
  )
  return items
})

const editOpen = ref(false)
const saving = ref(false)
const form = reactive({
  id: undefined as number | undefined,
  title: '',
  location: '',
  inspectorName: '',
  checkTime: dayjs(),
  status: 'OPEN' as FireSafetyStatus,
  issueDescription: '',
  actionTaken: '',
  nextCheckDate: undefined as Dayjs | undefined,
  dutyRecord: '',
  handoverPunchTime: undefined as Dayjs | undefined,
  equipmentBatchNo: '',
  productProductionDate: undefined as Dayjs | undefined,
  productExpiryDate: undefined as Dayjs | undefined,
  checkCycleDays: undefined as number | undefined,
  equipmentUpdateNote: '',
  equipmentAgingDisposal: ''
})

const qrOpen = ref(false)
const qrText = ref('')
const qrDataUrl = ref('')

const scanOpen = ref(false)
const scanSaving = ref(false)
const scanInputRef = ref<any>()
let scanKeyboardBuffer = ''
let scanBufferTimer: number | undefined
const scanForm = reactive({
  qrToken: '',
  inspectorName: '',
  actionTaken: ''
})

const statusOptions = [
  { label: '处理中', value: 'OPEN' },
  { label: '已关闭', value: 'CLOSED' }
]

function resetSummary() {
  Object.assign(summary, defaultSummary())
}

function normalizeText(value: string) {
  const trimmed = String(value || '').trim()
  return trimmed || undefined
}

async function fetchData() {
  loading.value = true
  try {
    const [checkTimeStart, checkTimeEnd] = query.checkTimeRange || []
    const [res, sum] = await Promise.all([
      getFireSafetyRecordPage({
        pageNo: query.pageNo,
        pageSize: query.pageSize,
        keyword: query.keyword,
        recordType: props.recordType,
        inspectorName: query.inspectorName,
        status: query.status,
        checkTimeStart,
        checkTimeEnd
      }),
      getFireSafetySummary({
        recordType: props.recordType,
        checkTimeStart,
        checkTimeEnd
      })
    ])
    rows.value = res.list
    pagination.current = res.pageNo || query.pageNo
    pagination.pageSize = res.pageSize || query.pageSize
    pagination.total = res.total || res.list.length
    Object.assign(summary, defaultSummary(), sum || {})
  } catch (_error) {
    rows.value = []
    pagination.total = 0
    resetSummary()
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
  query.keyword = undefined
  query.inspectorName = undefined
  query.status = undefined
  query.checkTimeRange = undefined
  query.pageNo = 1
  query.pageSize = 10
  pagination.current = 1
  pagination.pageSize = 10
  fetchData()
}

function resetForm() {
  form.id = undefined
  form.title = ''
  form.location = ''
  form.inspectorName = ''
  form.checkTime = dayjs()
  form.status = 'OPEN'
  form.issueDescription = ''
  form.actionTaken = ''
  form.nextCheckDate = undefined
  form.dutyRecord = ''
  form.handoverPunchTime = undefined
  form.equipmentBatchNo = ''
  form.productProductionDate = undefined
  form.productExpiryDate = undefined
  form.checkCycleDays = undefined
  form.equipmentUpdateNote = ''
  form.equipmentAgingDisposal = ''
}

function openCreate() {
  resetForm()
  editOpen.value = true
}

function openEdit(record: FireSafetyRecord) {
  resetForm()
  form.id = record.id
  form.title = record.title
  form.location = record.location || ''
  form.inspectorName = record.inspectorName
  form.checkTime = dayjs(record.checkTime)
  form.status = record.status
  form.issueDescription = record.issueDescription || ''
  form.actionTaken = record.actionTaken || ''
  form.nextCheckDate = record.nextCheckDate ? dayjs(record.nextCheckDate) : undefined
  form.dutyRecord = record.dutyRecord || ''
  form.handoverPunchTime = record.handoverPunchTime ? dayjs(record.handoverPunchTime) : undefined
  form.equipmentBatchNo = record.equipmentBatchNo || ''
  form.productProductionDate = record.productProductionDate ? dayjs(record.productProductionDate) : undefined
  form.productExpiryDate = record.productExpiryDate ? dayjs(record.productExpiryDate) : undefined
  form.checkCycleDays = record.checkCycleDays
  form.equipmentUpdateNote = record.equipmentUpdateNote || ''
  form.equipmentAgingDisposal = record.equipmentAgingDisposal || ''
  editOpen.value = true
}

function validateForm() {
  if (!normalizeText(form.title)) {
    message.warning('请输入标题')
    return false
  }
  if (!normalizeText(form.inspectorName)) {
    message.warning('请输入负责人')
    return false
  }
  if (!form.checkTime) {
    message.warning('请选择检查时间')
    return false
  }
  if (viewConfig.value.showProductLifecycle && form.productProductionDate && form.productExpiryDate
    && form.productExpiryDate.isBefore(form.productProductionDate, 'day')) {
    message.warning('产品过期时间不能早于生产时间')
    return false
  }
  if (viewConfig.value.showNextCheck && form.nextCheckDate
    && form.nextCheckDate.isBefore(form.checkTime.startOf('day'), 'day')) {
    message.warning('下次检查日期不能早于检查日期')
    return false
  }
  return true
}

async function submit() {
  if (!validateForm()) {
    return
  }

  const payload = {
    recordType: props.recordType,
    title: normalizeText(form.title),
    location: normalizeText(form.location),
    inspectorName: normalizeText(form.inspectorName),
    checkTime: form.checkTime ? dayjs(form.checkTime).format('YYYY-MM-DDTHH:mm:ss') : undefined,
    status: form.status,
    issueDescription: normalizeText(form.issueDescription),
    actionTaken: normalizeText(form.actionTaken),
    nextCheckDate: viewConfig.value.showNextCheck && form.nextCheckDate
      ? dayjs(form.nextCheckDate).format('YYYY-MM-DD')
      : undefined,
    dutyRecord: viewConfig.value.showDutyRecord ? normalizeText(form.dutyRecord) : undefined,
    handoverPunchTime: viewConfig.value.showHandoverPunch && form.handoverPunchTime
      ? dayjs(form.handoverPunchTime).format('YYYY-MM-DDTHH:mm:ss')
      : undefined,
    equipmentBatchNo: viewConfig.value.showEquipmentBatch ? normalizeText(form.equipmentBatchNo) : undefined,
    productProductionDate: viewConfig.value.showProductLifecycle && form.productProductionDate
      ? dayjs(form.productProductionDate).format('YYYY-MM-DD')
      : undefined,
    productExpiryDate: viewConfig.value.showProductLifecycle && form.productExpiryDate
      ? dayjs(form.productExpiryDate).format('YYYY-MM-DD')
      : undefined,
    checkCycleDays: viewConfig.value.showProductLifecycle ? form.checkCycleDays : undefined,
    equipmentUpdateNote: viewConfig.value.showEquipmentUpdateNote ? normalizeText(form.equipmentUpdateNote) : undefined,
    equipmentAgingDisposal: viewConfig.value.showEquipmentAgingDisposal ? normalizeText(form.equipmentAgingDisposal) : undefined
  }

  saving.value = true
  try {
    if (form.id) {
      await updateFireSafetyRecord(form.id, payload)
    } else {
      await createFireSafetyRecord(payload)
    }
    editOpen.value = false
    await fetchData()
  } finally {
    saving.value = false
  }
}

async function openQr(record: FireSafetyRecord) {
  const payload = await generateFireSafetyQr(record.id)
  qrText.value = payload.qrToken
  qrDataUrl.value = await QRCode.toDataURL(payload.qrContent)
  qrOpen.value = true
  message.success('二维码已生成')
  fetchData()
}

function openScan() {
  scanForm.qrToken = ''
  scanForm.inspectorName = ''
  scanForm.actionTaken = ''
  scanOpen.value = true
  scanKeyboardBuffer = ''
  if (scanBufferTimer) {
    window.clearTimeout(scanBufferTimer)
    scanBufferTimer = undefined
  }
  nextTick(() => {
    scanInputRef.value?.focus?.()
  })
}

function extractToken(rawValue: string) {
  const raw = String(rawValue || '').trim()
  if (!raw) return ''
  if (raw.includes('FIRE_PATROL:')) {
    const chunks = raw.split(':')
    return String(chunks[chunks.length - 1] || '').trim()
  }
  const tokenFromQuery = raw.match(/[?&](?:token|qrToken)=([^&]+)/i)
  if (tokenFromQuery?.[1]) {
    return decodeURIComponent(tokenFromQuery[1]).trim()
  }
  return raw
}

function onScannerConfirm(rawValue: string) {
  if (scanSaving.value || !scanOpen.value) return
  const token = extractToken(rawValue)
  if (!token) return
  scanForm.qrToken = token
  submitScan()
}

async function submitScan() {
  if (scanSaving.value) return
  const token = extractToken(scanForm.qrToken)
  if (!token) {
    message.warning('请先输入二维码令牌')
    return
  }
  scanForm.qrToken = token
  scanSaving.value = true
  try {
    await completeFireSafetyByScan({
      qrToken: token,
      inspectorName: normalizeText(scanForm.inspectorName),
      actionTaken: normalizeText(scanForm.actionTaken)
    })
    message.success('扫码完成成功')
    scanOpen.value = false
    fetchData()
  } finally {
    scanSaving.value = false
  }
}

function onScanModalKeydown(event: KeyboardEvent) {
  if (!scanOpen.value) return
  const targetTag = (event.target as HTMLElement | null)?.tagName?.toUpperCase?.()
  if (targetTag === 'INPUT' || targetTag === 'TEXTAREA') return
  if (event.key === 'Enter') {
    event.preventDefault()
    onScannerConfirm(scanKeyboardBuffer || scanForm.qrToken)
    scanKeyboardBuffer = ''
    return
  }
  if (event.key.length === 1 && !event.metaKey && !event.ctrlKey && !event.altKey) {
    scanKeyboardBuffer += event.key
    if (scanBufferTimer) {
      window.clearTimeout(scanBufferTimer)
    }
    scanBufferTimer = window.setTimeout(() => {
      scanKeyboardBuffer = ''
      scanBufferTimer = undefined
    }, 300)
  }
}

async function closeRecord(record: FireSafetyRecord) {
  await closeFireSafetyRecord(record.id)
  fetchData()
}

async function remove(record: FireSafetyRecord) {
  await deleteFireSafetyRecord(record.id)
  fetchData()
}

async function downloadMaintenanceLog() {
  const [checkTimeStart, checkTimeEnd] = query.checkTimeRange || []
  await exportFireSafetyMaintenanceLog({
    keyword: query.keyword,
    inspectorName: query.inspectorName,
    status: query.status,
    checkTimeStart,
    checkTimeEnd
  })
}

onMounted(() => {
  window.addEventListener('keydown', onScanModalKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', onScanModalKeydown)
  if (scanBufferTimer) {
    window.clearTimeout(scanBufferTimer)
  }
})

fetchData()
</script>
