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
          <a-button v-if="canPrintSelected" :disabled="!selectedRows.length" @click="printSelectedRows">打印勾选记录</a-button>
          <a-button v-if="viewConfig.supportsQr" @click="openScan">扫码完成</a-button>
          <a-button type="primary" @click="openCreate">新增记录</a-button>
        </a-space>
      </template>
    </SearchForm>

    <a-row :gutter="[12, 12]">
      <a-col :xs="12" :lg="4"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="记录总数" :value="summary.totalCount || 0" /></a-card></a-col>
      <a-col :xs="12" :lg="4"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="未关闭项" :value="summary.openCount || 0" /></a-card></a-col>
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
      :row-selection="rowSelection"
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="statusColor(record.status)">
            {{ statusText(record.status) }}
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
        <template v-else-if="column.key === 'imageUrls'">
          <a-space v-if="record.imageUrls?.length" wrap size="small">
            <a-image
              v-for="(url, index) in record.imageUrls.slice(0, 3)"
              :key="`${record.id}-${index}`"
              :src="url"
              :width="44"
              :height="44"
              style="object-fit: cover; border-radius: 6px"
            />
            <a-tag v-if="record.imageUrls.length > 3">+{{ record.imageUrls.length - 3 }}</a-tag>
          </a-space>
          <span v-else>-</span>
        </template>
        <template v-else-if="column.key === 'thirdPartyMaintenanceFileUrl'">
          <a v-if="record.thirdPartyMaintenanceFileUrl" :href="record.thirdPartyMaintenanceFileUrl" target="_blank" rel="noopener noreferrer">查看附件</a>
          <span v-else>-</span>
        </template>
        <template v-else-if="column.key === 'purchaseContractFileUrl'">
          <a v-if="record.purchaseContractFileUrl" :href="record.purchaseContractFileUrl" target="_blank" rel="noopener noreferrer">查看合同</a>
          <span v-else>-</span>
        </template>
        <template v-else-if="column.key === 'contractPeriod'">
          {{ formatContractPeriod(record.contractStartDate, record.contractEndDate) }}
        </template>
        <template v-else-if="column.key === 'purchaseDocumentUrls'">
          <span v-if="record.purchaseDocumentUrls?.length">已上传{{ record.purchaseDocumentUrls.length }}份</span>
          <span v-else>-</span>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space wrap>
            <a-button v-if="viewConfig.supportsQr" type="link" @click="openQr(record)">{{ qrButtonText }}</a-button>
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
        <a-form-item v-if="viewConfig.showImageUpload" label="现场图片">
          <a-upload :before-upload="beforeUploadImage" :show-upload-list="false" accept="image/*" multiple>
            <a-button :loading="uploadingImage">上传图片</a-button>
          </a-upload>
          <a-space v-if="form.imageUrls.length" wrap style="margin-top: 8px">
            <a-tag
              v-for="(url, index) in form.imageUrls"
              :key="`${url}-${index}`"
              closable
              @close="removeImage(index)"
            >
              <a :href="url" target="_blank" rel="noopener noreferrer">图片{{ index + 1 }}</a>
            </a-tag>
          </a-space>
          <div
            v-if="form.imageUrls.length"
            style="display: flex; gap: 8px; flex-wrap: wrap; margin-top: 10px"
          >
            <a-image
              v-for="(url, index) in form.imageUrls"
              :key="`${url}-preview-${index}`"
              :src="url"
              :width="72"
              :height="72"
              style="object-fit: cover; border-radius: 8px"
            />
          </div>
        </a-form-item>
        <template v-if="viewConfig.showMaintenanceAttachments">
          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item label="第三方维保记录单">
                <a-upload :before-upload="beforeUploadThirdPartyFile" :show-upload-list="false">
                  <a-button :loading="uploadingThirdPartyFile">上传维保记录单</a-button>
                </a-upload>
                <a-space v-if="form.thirdPartyMaintenanceFileUrl" style="margin-top: 8px">
                  <a :href="form.thirdPartyMaintenanceFileUrl" target="_blank" rel="noopener noreferrer">查看已上传文件</a>
                  <a-button type="link" danger @click="form.thirdPartyMaintenanceFileUrl = ''">移除</a-button>
                </a-space>
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="采购合同">
                <a-upload :before-upload="beforeUploadPurchaseContract" :show-upload-list="false">
                  <a-button :loading="uploadingPurchaseContract">上传采购合同</a-button>
                </a-upload>
                <a-space v-if="form.purchaseContractFileUrl" style="margin-top: 8px">
                  <a :href="form.purchaseContractFileUrl" target="_blank" rel="noopener noreferrer">查看采购合同</a>
                  <a-button type="link" danger @click="form.purchaseContractFileUrl = ''">移除</a-button>
                </a-space>
              </a-form-item>
            </a-col>
          </a-row>
          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item label="合约开始日期">
                <a-date-picker v-model:value="form.contractStartDate" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="合约结束日期">
                <a-date-picker v-model:value="form.contractEndDate" style="width: 100%" />
              </a-form-item>
            </a-col>
          </a-row>
          <a-form-item label="采购单据">
            <a-upload :before-upload="beforeUploadPurchaseDocument" :show-upload-list="false" multiple>
              <a-button :loading="uploadingPurchaseDocument">上传单据</a-button>
            </a-upload>
            <a-space v-if="form.purchaseDocumentUrls.length" wrap style="margin-top: 8px">
              <a-tag
                v-for="(url, index) in form.purchaseDocumentUrls"
                :key="`${url}-${index}`"
                closable
                @close="removePurchaseDocument(index)"
              >
                <a :href="url" target="_blank" rel="noopener noreferrer">单据{{ index + 1 }}</a>
              </a-tag>
            </a-space>
          </a-form-item>
        </template>

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

    <a-modal v-if="viewConfig.supportsQr" v-model:open="qrOpen" :title="qrModalTitle" :footer="null" width="520px">
      <a-space direction="vertical" style="width: 100%" align="center">
        <img v-if="qrDataUrl" :src="qrDataUrl" alt="巡查二维码" style="width: 240px; height: 240px" />
        <a-typography-text copyable>{{ qrText }}</a-typography-text>
        <a-typography-text type="secondary">{{ qrHintText }}</a-typography-text>
      </a-space>
    </a-modal>

    <a-modal v-if="viewConfig.supportsQr" v-model:open="scanOpen" :title="scanModalTitle" :footer="null" width="560px">
      <a-form layout="vertical">
        <a-form-item label="二维码令牌" required>
          <a-input
            ref="scanInputRef"
            v-model:value="scanForm.qrToken"
            :placeholder="scanPlaceholder"
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
  uploadFireSafetyFile,
  uploadFireSafetyImage,
  updateFireSafetyRecord
} from '../../../api/fire'
import { printTableReport } from '../../../utils/print'
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
  showImageUpload: boolean
  showMaintenanceAttachments: boolean
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
    supportsQr: true,
    showImageUpload: false,
    showMaintenanceAttachments: false
  },
  CONTROL_ROOM_DUTY: {
    showDutyRecord: true,
    showHandoverPunch: true,
    showEquipmentBatch: false,
    showProductLifecycle: false,
    showEquipmentUpdateNote: false,
    showEquipmentAgingDisposal: false,
    showNextCheck: false,
    supportsQr: false,
    showImageUpload: true,
    showMaintenanceAttachments: false
  },
  MONTHLY_CHECK: {
    showDutyRecord: false,
    showHandoverPunch: false,
    showEquipmentBatch: false,
    showProductLifecycle: false,
    showEquipmentUpdateNote: false,
    showEquipmentAgingDisposal: false,
    showNextCheck: true,
    supportsQr: true,
    showImageUpload: false,
    showMaintenanceAttachments: false
  },
  DAY_PATROL: {
    showDutyRecord: false,
    showHandoverPunch: false,
    showEquipmentBatch: false,
    showProductLifecycle: false,
    showEquipmentUpdateNote: false,
    showEquipmentAgingDisposal: false,
    showNextCheck: true,
    supportsQr: true,
    showImageUpload: false,
    showMaintenanceAttachments: false
  },
  NIGHT_PATROL: {
    showDutyRecord: false,
    showHandoverPunch: false,
    showEquipmentBatch: false,
    showProductLifecycle: false,
    showEquipmentUpdateNote: false,
    showEquipmentAgingDisposal: false,
    showNextCheck: true,
    supportsQr: true,
    showImageUpload: false,
    showMaintenanceAttachments: false
  },
  MAINTENANCE_REPORT: {
    showDutyRecord: false,
    showHandoverPunch: false,
    showEquipmentBatch: true,
    showProductLifecycle: true,
    showEquipmentUpdateNote: true,
    showEquipmentAgingDisposal: false,
    showNextCheck: false,
    supportsQr: false,
    showImageUpload: false,
    showMaintenanceAttachments: true
  },
  FAULT_MAINTENANCE: {
    showDutyRecord: false,
    showHandoverPunch: false,
    showEquipmentBatch: true,
    showProductLifecycle: false,
    showEquipmentUpdateNote: false,
    showEquipmentAgingDisposal: false,
    showNextCheck: false,
    supportsQr: false,
    showImageUpload: false,
    showMaintenanceAttachments: false
  }
}

const viewConfig = computed(() => viewConfigMap[props.recordType])
const canPrintSelected = computed(() => props.recordType === 'CONTROL_ROOM_DUTY')
const qrButtonText = computed(() => (props.recordType === 'FACILITY' ? '制作二维码' : '生成二维码'))
const qrModalTitle = computed(() => (props.recordType === 'FACILITY' ? '日常消防填报二维码' : '巡查二维码'))
const qrHintText = computed(() => (
  props.recordType === 'FACILITY'
    ? '消防安全员扫码后可进入日常消防安全填报与闭环处理。'
    : '扫码后自动完成该条巡查记录并闭环。'
))
const scanModalTitle = computed(() => (
  props.recordType === 'FACILITY' ? '扫码填写日常消防记录' : '扫码完成巡查'
))
const scanPlaceholder = computed(() => (
  props.recordType === 'FACILITY'
    ? '扫码后自动填充，可补充本次消防安全填报内容'
    : '扫码枪扫码后会自动提交，无需手点确认'
))

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
  if (viewConfig.value.showImageUpload) {
    items.push({ title: '现场图片', dataIndex: 'imageUrls', key: 'imageUrls', width: 180 })
  }
  if (viewConfig.value.showMaintenanceAttachments) {
    items.push(
      { title: '第三方维保单', dataIndex: 'thirdPartyMaintenanceFileUrl', key: 'thirdPartyMaintenanceFileUrl', width: 140 },
      { title: '采购合同', dataIndex: 'purchaseContractFileUrl', key: 'purchaseContractFileUrl', width: 140 },
      { title: '合约期', dataIndex: 'contractPeriod', key: 'contractPeriod', width: 190 },
      { title: '采购单据', dataIndex: 'purchaseDocumentUrls', key: 'purchaseDocumentUrls', width: 140 }
    )
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
const uploadingImage = ref(false)
const uploadingThirdPartyFile = ref(false)
const uploadingPurchaseContract = ref(false)
const uploadingPurchaseDocument = ref(false)
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
  equipmentAgingDisposal: '',
  imageUrls: [] as string[],
  thirdPartyMaintenanceFileUrl: '',
  purchaseContractFileUrl: '',
  contractStartDate: undefined as Dayjs | undefined,
  contractEndDate: undefined as Dayjs | undefined,
  purchaseDocumentUrls: [] as string[]
})
const selectedRowKeys = ref<number[]>([])
const selectedRows = computed(() => rows.value.filter((item) => selectedRowKeys.value.includes(item.id)))
const rowSelection = computed(() => {
  if (!canPrintSelected.value) {
    return undefined
  }
  return {
    selectedRowKeys: selectedRowKeys.value,
    onChange: (keys: Array<string | number>) => {
      selectedRowKeys.value = keys.map((item) => Number(item))
    }
  }
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
  { label: '运行中', value: 'RUNNING' },
  { label: '已关闭', value: 'CLOSED' }
]

function statusText(status?: FireSafetyStatus) {
  if (status === 'CLOSED') return '已关闭'
  if (status === 'RUNNING') return '运行中'
  return '处理中'
}

function statusColor(status?: FireSafetyStatus) {
  if (status === 'CLOSED') return 'green'
  if (status === 'RUNNING') return 'blue'
  return 'orange'
}

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
    selectedRowKeys.value = selectedRowKeys.value.filter((id) => rows.value.some((item) => item.id === id))
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
  form.imageUrls = []
  form.thirdPartyMaintenanceFileUrl = ''
  form.purchaseContractFileUrl = ''
  form.contractStartDate = undefined
  form.contractEndDate = undefined
  form.purchaseDocumentUrls = []
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
  form.imageUrls = Array.isArray(record.imageUrls) ? [...record.imageUrls] : []
  form.thirdPartyMaintenanceFileUrl = record.thirdPartyMaintenanceFileUrl || ''
  form.purchaseContractFileUrl = record.purchaseContractFileUrl || ''
  form.contractStartDate = record.contractStartDate ? dayjs(record.contractStartDate) : undefined
  form.contractEndDate = record.contractEndDate ? dayjs(record.contractEndDate) : undefined
  form.purchaseDocumentUrls = Array.isArray(record.purchaseDocumentUrls) ? [...record.purchaseDocumentUrls] : []
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
  if (viewConfig.value.showMaintenanceAttachments && form.contractStartDate && form.contractEndDate
    && form.contractEndDate.isBefore(form.contractStartDate, 'day')) {
    message.warning('合约结束日期不能早于开始日期')
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
    equipmentAgingDisposal: viewConfig.value.showEquipmentAgingDisposal ? normalizeText(form.equipmentAgingDisposal) : undefined,
    imageUrls: viewConfig.value.showImageUpload ? form.imageUrls : undefined,
    thirdPartyMaintenanceFileUrl: viewConfig.value.showMaintenanceAttachments ? normalizeText(form.thirdPartyMaintenanceFileUrl) : undefined,
    purchaseContractFileUrl: viewConfig.value.showMaintenanceAttachments ? normalizeText(form.purchaseContractFileUrl) : undefined,
    contractStartDate: viewConfig.value.showMaintenanceAttachments && form.contractStartDate
      ? dayjs(form.contractStartDate).format('YYYY-MM-DD')
      : undefined,
    contractEndDate: viewConfig.value.showMaintenanceAttachments && form.contractEndDate
      ? dayjs(form.contractEndDate).format('YYYY-MM-DD')
      : undefined,
    purchaseDocumentUrls: viewConfig.value.showMaintenanceAttachments ? form.purchaseDocumentUrls : undefined
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

async function beforeUploadImage(file: File) {
  uploadingImage.value = true
  try {
    const uploaded = await uploadFireSafetyImage(file, 'fire-control-room-record')
    const url = uploaded?.fileUrl || ''
    if (!url) {
      message.error('上传失败：未返回图片地址')
      return false
    }
    if (!form.imageUrls.includes(url)) {
      form.imageUrls = [...form.imageUrls, url]
    }
    message.success('图片上传成功')
  } catch (error: any) {
    message.error(error?.message || '图片上传失败')
  } finally {
    uploadingImage.value = false
  }
  return false
}

function removeImage(index: number) {
  form.imageUrls.splice(index, 1)
}

async function uploadSingleFireFile(
  file: File,
  bizType: string,
  loadingRef: { value: boolean },
  onSuccess: (url: string) => void,
  successText: string
) {
  loadingRef.value = true
  try {
    const uploaded = await uploadFireSafetyFile(file, bizType)
    const url = uploaded?.fileUrl || ''
    if (!url) {
      message.error('上传失败：未返回文件地址')
      return false
    }
    onSuccess(url)
    message.success(successText)
  } catch (error: any) {
    message.error(error?.message || '文件上传失败')
  } finally {
    loadingRef.value = false
  }
  return false
}

async function beforeUploadThirdPartyFile(file: File) {
  return uploadSingleFireFile(
    file,
    'fire-maintenance-third-party-record',
    uploadingThirdPartyFile,
    (url) => {
      form.thirdPartyMaintenanceFileUrl = url
    },
    '第三方维保记录单上传成功'
  )
}

async function beforeUploadPurchaseContract(file: File) {
  return uploadSingleFireFile(
    file,
    'fire-maintenance-purchase-contract',
    uploadingPurchaseContract,
    (url) => {
      form.purchaseContractFileUrl = url
    },
    '采购合同上传成功'
  )
}

async function beforeUploadPurchaseDocument(file: File) {
  return uploadSingleFireFile(
    file,
    'fire-maintenance-purchase-document',
    uploadingPurchaseDocument,
    (url) => {
      if (!form.purchaseDocumentUrls.includes(url)) {
        form.purchaseDocumentUrls = [...form.purchaseDocumentUrls, url]
      }
    },
    '采购单据上传成功'
  )
}

function removePurchaseDocument(index: number) {
  form.purchaseDocumentUrls.splice(index, 1)
}

function formatContractPeriod(start?: string, end?: string) {
  if (!start && !end) {
    return '-'
  }
  return `${start || '-'} ~ ${end || '-'}`
}

function printSelectedRows() {
  if (!selectedRows.value.length) {
    message.warning('请先勾选要打印的记录')
    return
  }
  try {
    printTableReport({
      title: `${props.title}打印单`,
      subtitle: `打印时间：${dayjs().format('YYYY-MM-DD HH:mm:ss')}；记录数：${selectedRows.value.length}`,
      columns: [
        { key: 'title', title: '标题' },
        { key: 'location', title: '区域/位置' },
        { key: 'inspectorName', title: '负责人' },
        { key: 'checkTime', title: '检查时间' },
        { key: 'dutyRecord', title: '值班记录' },
        { key: 'handoverPunchTime', title: '交接班打卡时间' },
        { key: 'issueDescription', title: '问题描述' },
        { key: 'actionTaken', title: '处置措施' },
        { key: 'imageSummary', title: '图片' },
        { key: 'statusText', title: '状态' }
      ],
      rows: selectedRows.value.map((item) => ({
        title: item.title || '-',
        location: item.location || '-',
        inspectorName: item.inspectorName || '-',
        checkTime: item.checkTime || '-',
        dutyRecord: item.dutyRecord || '-',
        handoverPunchTime: item.handoverPunchTime || '-',
        issueDescription: item.issueDescription || '-',
        actionTaken: item.actionTaken || '-',
        imageSummary: item.imageUrls?.length ? `已上传${item.imageUrls.length}张` : '无',
        statusText: statusText(item.status)
      })),
      signatures: ['值班人', '复核人']
    })
  } catch (error: any) {
    message.error(error?.message || '打印失败')
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
