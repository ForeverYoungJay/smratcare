<template>
  <PageContainer title="医保结算清单" subTitle="由长护险结算单生成：草稿 → 已上传 → 已回执 → 已对账（金额单位：元）">
    <SearchForm :model="query" @search="onSearch" @reset="onReset">
      <a-form-item label="长者">
        <a-select v-model:value="query.elderId" show-search allow-clear :filter-option="false" :options="elderOptions"
          placeholder="输入姓名搜索" style="width: 200px" @search="searchElders" @focus="() => !elderOptions.length && searchElders('')" />
      </a-form-item>
      <a-form-item label="结算月份">
        <a-date-picker v-model:value="query.month" picker="month" value-format="YYYYMM" placeholder="YYYYMM" />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 140px" placeholder="全部">
          <a-select-option value="DRAFT">草稿</a-select-option>
          <a-select-option value="UPLOADED">已上传</a-select-option>
          <a-select-option value="RECEIPTED">已回执</a-select-option>
          <a-select-option value="RECONCILED">已对账</a-select-option>
          <a-select-option value="REJECTED">已驳回</a-select-option>
        </a-select>
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openGenerate">生成结算清单</a-button>
      </template>
    </SearchForm>

    <DataTable rowKey="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'totalFee'">{{ yuan(record.totalFee) }}</template>
        <template v-else-if="column.key === 'fundPay'">{{ yuan(record.fundPay) }}</template>
        <template v-else-if="column.key === 'selfPay'">{{ yuan(record.selfPay) }}</template>
        <template v-else-if="column.key === 'sheetStatus'">
          <a-tag :color="statusColor(record.sheetStatus)">{{ statusText(record.sheetStatus) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <div class="row-action-links">
            <a-button type="link" size="small" @click="openDetail(record)">详情</a-button>
            <a-button
              v-if="['DRAFT','REJECTED'].includes(record.sheetStatus)"
              type="link"
              size="small"
              @click="openUpload(record)"
            >上传</a-button>
            <a-button
              v-if="record.sheetStatus === 'RECEIPTED'"
              type="link"
              size="small"
              @click="doReconcile(record)"
            >对账</a-button>
          </div>
        </template>
      </template>
    </DataTable>

    <!-- 生成清单 -->
    <a-modal v-model:open="genOpen" title="生成医保结算清单" :confirm-loading="saving" @ok="submitGenerate">
      <a-alert type="info" show-icon message="从已提交(SUBMITTED)的长护险结算单生成，金额自动带入统筹支付/个人自付。" style="margin-bottom: 12px" />
      <a-form layout="vertical">
        <a-form-item label="长者" required>
          <a-select v-model:value="genForm.elderId" show-search :filter-option="false" :options="elderOptions"
            placeholder="输入姓名搜索" @search="searchElders" @focus="() => !elderOptions.length && searchElders('')" />
        </a-form-item>
        <a-form-item label="结算月份" required>
          <a-date-picker v-model:value="genForm.month" picker="month" value-format="YYYYMM" style="width: 100%" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 上传清单 -->
    <a-modal v-model:open="uploadOpen" title="上传结算清单" :confirm-loading="saving" @ok="submitUpload">
      <a-form layout="vertical">
        <a-form-item label="上传渠道" required>
          <a-select v-model:value="uploadForm.channel" placeholder="选择医保渠道">
            <a-select-option v-for="c in channelOptions" :key="c.channel" :value="c.channel">
              {{ c.channel }}{{ c.regionCode ? `（${c.regionCode}）` : '' }}
            </a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 详情：清单 + 上报任务 -->
    <a-modal v-model:open="detailOpen" title="结算清单详情" :footer="null" width="820px">
      <a-descriptions v-if="detail" :column="2" size="small" bordered style="margin-bottom: 12px">
        <a-descriptions-item label="清单编号">{{ detail.sheetNo || '-' }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="statusColor(detail.sheetStatus)">{{ statusText(detail.sheetStatus) }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="结算月份">{{ detail.settleMonth }}</a-descriptions-item>
        <a-descriptions-item label="长护结算单ID">{{ detail.ltciSettlementId }}</a-descriptions-item>
        <a-descriptions-item label="费用合计(元)">{{ yuan(detail.totalFee) }}</a-descriptions-item>
        <a-descriptions-item label="统筹支付(元)">{{ yuan(detail.fundPay) }}</a-descriptions-item>
        <a-descriptions-item label="个人自付(元)">{{ yuan(detail.selfPay) }}</a-descriptions-item>
        <a-descriptions-item label="渠道">{{ detail.channel || '-' }}</a-descriptions-item>
        <a-descriptions-item label="回执编号">{{ detail.receiptCode || '-' }}</a-descriptions-item>
        <a-descriptions-item label="驳回原因">{{ detail.rejectReason || '-' }}</a-descriptions-item>
      </a-descriptions>

      <a-table rowKey="id" size="small" :columns="taskColumns" :data-source="tasks" :loading="taskLoading" :pagination="false">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'taskStatus'">
            <a-tag :color="taskStatusColor(record.taskStatus)">{{ taskStatusText(record.taskStatus) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <div class="row-action-links">
              <a-button type="link" size="small" @click="viewPayload(record)">报文</a-button>
              <a-button v-if="record.taskStatus === 'SENT'" type="link" size="small" @click="doQueryReceipt(record)">查回执</a-button>
              <a-button v-if="record.taskStatus === 'FAILED'" type="link" size="small" @click="doRetry(record)">重发</a-button>
            </div>
          </template>
        </template>
      </a-table>
    </a-modal>

    <!-- 报文/回执 -->
    <a-modal v-model:open="payloadOpen" title="报文快照与回执" :footer="null" width="720px">
      <a-descriptions v-if="payloadTask" :column="1" size="small" bordered style="margin-bottom: 12px">
        <a-descriptions-item label="报文指纹">{{ payloadTask.payloadHash || '-' }}</a-descriptions-item>
        <a-descriptions-item label="回执编号">{{ payloadTask.receiptCode || '-' }}</a-descriptions-item>
      </a-descriptions>
      <pre class="payload-box">{{ prettyJson(payloadTask?.payloadJson) }}</pre>
      <pre v-if="payloadTask?.receiptJson" class="payload-box" style="margin-top: 8px">{{ prettyJson(payloadTask?.receiptJson) }}</pre>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { useElderOptions } from '../../composables/useElderOptions'
import {
  generateMedinsSheet,
  getMedinsSheetPage,
  uploadMedinsSheet,
  reconcileMedinsSheet,
  getMedinsTaskPage,
  retryMedinsTask,
  queryMedinsTaskReceipt,
  getMedinsChannelOptions
} from '../../api/medins'
import type { MedinsChannelOption, MedinsSettlementSheet, MedinsUploadTask } from '../../api/medins'
import type { Id, PageResult } from '../../types'

const loading = ref(false)
const saving = ref(false)
const rows = ref<MedinsSettlementSheet[]>([])
const query = reactive({
  elderId: undefined as Id | undefined,
  month: undefined as string | undefined,
  status: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const { elderOptions, searchElders } = useElderOptions({ pageSize: 50 })

const columns = [
  { title: '清单编号', dataIndex: 'sheetNo', key: 'sheetNo', width: 150 },
  { title: '长者ID', dataIndex: 'elderId', key: 'elderId', width: 90 },
  { title: '结算月份', dataIndex: 'settleMonth', key: 'settleMonth', width: 100 },
  { title: '费用合计(元)', key: 'totalFee', width: 115 },
  { title: '统筹支付(元)', key: 'fundPay', width: 115 },
  { title: '个人自付(元)', key: 'selfPay', width: 115 },
  { title: '渠道', dataIndex: 'channel', key: 'channel', width: 90 },
  { title: '状态', key: 'sheetStatus', width: 95 },
  { title: '操作', key: 'action', width: 150 }
]

const taskColumns = [
  { title: '任务ID', dataIndex: 'id', key: 'id', width: 120 },
  { title: '渠道', dataIndex: 'channel', key: 'channel', width: 90 },
  { title: '状态', key: 'taskStatus', width: 95 },
  { title: '重试', dataIndex: 'retryCount', key: 'retryCount', width: 60 },
  { title: '最近错误', dataIndex: 'lastError', key: 'lastError', ellipsis: true },
  { title: '操作', key: 'action', width: 160 }
]

const genOpen = ref(false)
const genForm = reactive({ elderId: undefined as Id | undefined, month: undefined as string | undefined })

const uploadOpen = ref(false)
const uploadForm = reactive({ sheetId: undefined as Id | undefined, channel: undefined as string | undefined })
const channelOptions = ref<MedinsChannelOption[]>([])

const detailOpen = ref(false)
const detail = ref<MedinsSettlementSheet | null>(null)
const tasks = ref<MedinsUploadTask[]>([])
const taskLoading = ref(false)

const payloadOpen = ref(false)
const payloadTask = ref<MedinsUploadTask | null>(null)

function yuan(fen?: number) {
  return fen == null ? '0.00' : (Number(fen) / 100).toFixed(2)
}
function statusText(s?: string) {
  return ({ DRAFT: '草稿', UPLOADED: '已上传', RECEIPTED: '已回执', RECONCILED: '已对账', REJECTED: '已驳回' } as Record<string, string>)[s || ''] || s || '-'
}
function statusColor(s?: string) {
  if (s === 'RECONCILED') return 'green'
  if (s === 'RECEIPTED') return 'cyan'
  if (s === 'UPLOADED') return 'blue'
  if (s === 'REJECTED') return 'red'
  return 'orange'
}
function taskStatusText(s?: string) {
  return ({ PENDING: '待发送', SENT: '已发送', ACKED: '已回执', FAILED: '失败' } as Record<string, string>)[s || ''] || s || '-'
}
function taskStatusColor(s?: string) {
  if (s === 'ACKED') return 'green'
  if (s === 'SENT') return 'blue'
  if (s === 'FAILED') return 'red'
  return 'orange'
}
function prettyJson(text?: string | null) {
  if (!text) return '（无报文）'
  try {
    return JSON.stringify(JSON.parse(text), null, 2)
  } catch {
    return text
  }
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<MedinsSettlementSheet> = await getMedinsSheetPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      elderId: query.elderId,
      month: query.month,
      status: query.status
    })
    rows.value = res.list
    pagination.total = res.total || res.list.length
  } finally {
    loading.value = false
  }
}
function onSearch() {
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}
function onReset() {
  query.elderId = undefined
  query.month = undefined
  query.status = undefined
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}
function handleTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  fetchData()
}

function openGenerate() {
  genForm.elderId = undefined
  genForm.month = undefined
  genOpen.value = true
}
async function submitGenerate() {
  if (!genForm.elderId || !genForm.month) {
    message.error('请选择长者与结算月份')
    return
  }
  if (saving.value) return
  saving.value = true
  try {
    await generateMedinsSheet({ elderId: genForm.elderId, month: genForm.month })
    message.success('医保结算清单已生成')
    genOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function openUpload(record: MedinsSettlementSheet) {
  uploadForm.sheetId = record.id
  uploadForm.channel = record.channel || undefined
  if (!channelOptions.value.length) {
    channelOptions.value = (await getMedinsChannelOptions()) || []
  }
  if (!uploadForm.channel && channelOptions.value.length === 1) {
    uploadForm.channel = channelOptions.value[0].channel
  }
  uploadOpen.value = true
}
async function submitUpload() {
  if (!uploadForm.sheetId || !uploadForm.channel) {
    message.error('请选择上传渠道')
    return
  }
  if (saving.value) return
  saving.value = true
  try {
    const task = await uploadMedinsSheet(uploadForm.sheetId, uploadForm.channel)
    if (task.taskStatus === 'FAILED') {
      message.error(`上传失败：${task.lastError || '未知错误'}`)
    } else {
      message.success(`已上传，任务状态：${taskStatusText(task.taskStatus)}`)
    }
    uploadOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function doReconcile(record: MedinsSettlementSheet) {
  await reconcileMedinsSheet(record.id)
  message.success('清单已对账')
  fetchData()
}

async function openDetail(record: MedinsSettlementSheet) {
  detail.value = record
  detailOpen.value = true
  taskLoading.value = true
  try {
    const res = await getMedinsTaskPage({ pageNo: 1, pageSize: 50, sheetId: record.id })
    tasks.value = res.list
  } finally {
    taskLoading.value = false
  }
}

function viewPayload(record: MedinsUploadTask) {
  payloadTask.value = record
  payloadOpen.value = true
}

async function doQueryReceipt(record: MedinsUploadTask) {
  const task = await queryMedinsTaskReceipt(record.id)
  message.success(`回执状态：${taskStatusText(task.taskStatus)}`)
  if (detail.value) openDetail(detail.value)
  fetchData()
}

async function doRetry(record: MedinsUploadTask) {
  const task = await retryMedinsTask(record.id)
  if (task.taskStatus === 'FAILED') {
    message.error(`重发失败：${task.lastError || '未知错误'}`)
  } else {
    message.success(`已重发，任务状态：${taskStatusText(task.taskStatus)}`)
  }
  if (detail.value) openDetail(detail.value)
  fetchData()
}

fetchData()
searchElders('')
</script>

<style scoped>
.payload-box {
  max-height: 320px;
  overflow: auto;
  background: #0b1021;
  color: #d6e2ff;
  padding: 12px;
  border-radius: 8px;
  font-size: 12px;
  line-height: 1.6;
}
</style>
