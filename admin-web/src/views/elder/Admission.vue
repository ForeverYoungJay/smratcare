<template>
  <PageContainer title="入住办理" subTitle="分床、押金账户、合同信息与二维码配置">
    <a-card class="card-elevated" :bordered="false">
      <h3>办理入住流程</h3>
      <LifecycleStageBar
        title="入住办理阶段"
        :subject="admissionFlowSubject"
        :stage="admissionLifecycleStage"
        :generated-at="guardContract?.updateTime"
        :hint="admissionLifecycleHint"
        style="margin-bottom: 12px"
      />
      <a-alert
        v-if="lifecycleContext.active"
        type="info"
        show-icon
        style="margin-bottom: 12px"
        :message="lifecycleContext.message"
      >
        <template #description>
          <a-space wrap>
            <a-tag color="blue">场景：入住状态变更联动</a-tag>
            <a-button type="link" size="small" @click="router.push('/elder/status-change')">返回状态变更中心</a-button>
            <a-button type="link" size="small" @click="router.push('/logistics/task-center?source=lifecycle&scene=status-change&tab=maintenance&overdueOnly=1')">
              查看后勤交接任务
            </a-button>
          </a-space>
        </template>
      </a-alert>
      <a-form ref="formRef" :model="form" :rules="rules" layout="vertical">
        <a-form-item label="老人姓名" name="elderId">
          <a-select
            v-model:value="form.elderId"
            show-search
            :filter-option="false"
            :options="elderOptions"
            :loading="elderLoading"
            placeholder="请输入老人姓名/拼音首字母"
            @search="searchElders"
            @focus="() => !elderOptions.length && searchElders('')"
            @change="onElderChange"
          />
        </a-form-item>
        <a-form-item label="收费开始日期" name="admissionDate">
          <a-date-picker v-model:value="form.admissionDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
        <a-form-item label="合同号（自动回填）" name="contractNo">
          <a-input :value="form.contractNo" disabled placeholder="选择老人后自动填入，不可修改" />
        </a-form-item>
        <a-form-item label="初始余额(押金)" name="depositAmount">
          <a-input-number v-model:value="form.depositAmount" :min="0" style="width: 100%" placeholder="请输入押金金额" />
        </a-form-item>
        <a-form-item label="楼栋" name="buildingId">
          <a-select v-model:value="assetSelect.buildingId" allow-clear placeholder="请选择楼栋">
            <a-select-option v-for="item in buildingOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="楼层" name="floorId">
          <a-select v-model:value="assetSelect.floorId" allow-clear placeholder="请选择楼层">
            <a-select-option v-for="item in floorOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="房间" name="roomId">
          <a-select v-model:value="assetSelect.roomId" allow-clear placeholder="请选择房间">
            <a-select-option v-for="item in roomOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="床位" name="bedId">
          <a-select v-model:value="form.bedId" allow-clear placeholder="请选择床位">
            <a-select-option v-for="item in bedOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="备注" name="remark">
          <a-input v-model:value="form.remark" placeholder="请输入备注" />
        </a-form-item>
      </a-form>
      <div style="text-align: right; margin-top: 8px;">
        <a-button type="primary" :loading="submitting || submitChecklistLoading" @click="openSubmitConfirm">提交入住办理</a-button>
      </div>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <a-form :model="recordQuery" layout="inline" class="search-bar">
        <a-form-item label="老人姓名">
          <a-input v-model:value="recordQuery.keyword" placeholder="请输入老人姓名" allow-clear />
        </a-form-item>
        <a-form-item label="合同号">
          <a-input v-model:value="recordQuery.contractNo" placeholder="请输入合同号" allow-clear />
        </a-form-item>
        <a-form-item label="入住状态">
          <a-select v-model:value="recordQuery.elderStatus" allow-clear style="width: 140px" placeholder="请选择入住状态">
            <a-select-option :value="1">在院</a-select-option>
            <a-select-option :value="2">请假</a-select-option>
            <a-select-option :value="3">离院</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="收费开始日期">
          <a-range-picker v-model:value="recordQuery.admissionDateRange" value-format="YYYY-MM-DD" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="runRecordSearch">搜索</a-button>
            <a-button @click="resetRecordQuery">清空</a-button>
            <a-button @click="exportRecords">导出</a-button>
            <a-button @click="copyRecordSearchLink">复制检索链接</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated admission-summary-card" :bordered="false" style="margin-top: 16px;">
      <a-spin :spinning="recordSummaryLoading">
        <div class="admission-summary-head">
          <span class="admission-summary-title">入住记录筛选统计</span>
          <span class="admission-summary-meta">更新时间：{{ recordSummaryGeneratedAtText }}</span>
        </div>
        <a-row :gutter="[12, 12]">
          <a-col v-for="item in recordSummaryCards" :key="item.key" :xs="12" :md="8" :xl="4">
            <div class="admission-summary-tile">
              <div class="admission-summary-label">{{ item.label }}</div>
              <div class="admission-summary-value">{{ item.value }}</div>
              <div class="admission-summary-hint">{{ item.hint }}</div>
            </div>
          </a-col>
        </a-row>
        <div class="admission-distribution-panel">
          <div class="admission-distribution-head">
            <span class="admission-distribution-title">入住空间分布占比</span>
            <a-space :size="8">
              <a-segmented
                v-model:value="recordDimension"
                :options="[
                  { label: '楼栋维度', value: 'BUILDING' },
                  { label: '楼层维度', value: 'FLOOR' }
                ]"
              />
              <a-tag color="blue">样本 {{ Number(recordSummary.totalCount || 0) }} 人</a-tag>
            </a-space>
          </div>
          <div v-if="activeDistributionRows.length > 0" class="admission-distribution-list">
            <div
              v-for="item in activeDistributionRows"
              :key="`${recordDimension}-${item.dimensionKey}`"
              class="admission-distribution-item"
            >
              <div class="admission-distribution-row">
                <span class="admission-distribution-label">{{ item.dimensionLabel }}</span>
                <span class="admission-distribution-meta">{{ item.count }} 人 · {{ formatDistributionRatio(item.ratio) }}</span>
              </div>
              <div class="admission-distribution-track">
                <div
                  class="admission-distribution-fill"
                  :style="{ width: `${Math.max(4, Math.round(Number(item.ratio || 0) * 100))}%` }"
                />
              </div>
            </div>
          </div>
          <a-empty v-else :image="null" description="当前筛选条件下暂无楼栋/楼层分布数据" />
        </div>
      </a-spin>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <a-table :data-source="admissionRows" :columns="columns" :loading="recordLoading" :pagination="false" row-key="id">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'elderName'">
            {{ displayElderName(record) }}
          </template>
          <template v-else-if="column.key === 'elderStatus'">
            <a-tag :color="record.elderStatus === 1 ? 'green' : record.elderStatus === 2 ? 'orange' : 'default'">
              {{ statusText(record.elderStatus) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'operatorName'">
            {{ record.operatorName || record.createByName || record.creatorName || '-' }}
          </template>
        </template>
      </a-table>
      <a-pagination
        style="margin-top: 16px; text-align: right;"
        :current="recordQuery.pageNo"
        :page-size="recordQuery.pageSize"
        :total="recordTotal"
        show-size-changer
        @change="onPageChange"
        @showSizeChange="onPageSizeChange"
      />
    </a-card>

    <a-modal
      v-model:open="submitConfirmOpen"
      title="提交确认"
      :confirm-loading="submitting"
      :ok-button-props="{ disabled: submitBlockingCount > 0 }"
      ok-text="确认提交"
      cancel-text="返回检查"
      @ok="confirmSubmit"
    >
      <a-space direction="vertical" style="width: 100%">
        <div>请确认以下信息填写及上传资料是否正确：</div>
        <a-list bordered size="small" :data-source="submitChecklist">
          <template #renderItem="{ item }">
            <a-list-item>
              <a-space>
                <a-button type="link" style="padding: 0" @click="jumpToChecklist(item.key)">• {{ item.label }}</a-button>
                <a-tag :color="item.ready ? 'green' : 'red'">{{ item.ready ? '已完善' : '待完善' }}</a-tag>
                <span style="color: #8c8c8c">{{ item.note }}</span>
              </a-space>
            </a-list-item>
          </template>
        </a-list>
        <a-alert
          type="warning"
          show-icon
          :message="`确认无误后将正式建立老人档案，后续资料修改需申请审批。当前待完善项：${submitChecklist.filter((item) => !item.ready).length}`"
        />
        <a-alert
          v-if="submitChecklist.some((item) => !item.ready)"
          type="error"
          show-icon
          message="存在待完善项，建议先点击条目跳转补全后再提交。"
        />
      </a-space>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import LifecycleStageBar from '../../components/LifecycleStageBar.vue'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'
import { useElderOptions } from '../../composables/useElderOptions'
import { copyText } from '../../utils/clipboard'
import { lifecycleStageHint, normalizeLifecycleStage } from '../../utils/lifecycleStage'
import { getFamilyRelations } from '../../api/family'
import { getBedList, getBuildingList, getFloorList, getRoomList } from '../../api/bed'
import { admitElder, exportAdmissionRecords, getAdmissionRecordSummary, getAdmissionRecords } from '../../api/elderLifecycle'
import { getElderDetail, getElderDiseases } from '../../api/elder'
import { getContractAttachments, getContractPage } from '../../api/marketing'
import { getCrmLead, updateCrmLead } from '../../api/crm'
import type {
  AdmissionRecordDimensionItem,
  AdmissionRecordItem,
  AdmissionRecordSummary,
  AdmissionRequest,
  BedItem,
  BuildingItem,
  CrmContractItem,
  FloorItem,
  PageResult,
  RoomItem
} from '../../types'

const route = useRoute()
const router = useRouter()
const lifecycleContext = computed(() => {
  const source = String(route.query.source || '').trim().toLowerCase()
  const scene = String(route.query.scene || '').trim().toLowerCase()
  const active = source === 'lifecycle' || scene === 'status-change'
  return {
    active,
    message: active ? '当前由状态变更联动进入，建议优先核对床位交接与合同资料后再提交入住办理。' : ''
  }
})
const formRef = ref<FormInstance>()
const form = reactive<AdmissionRequest>({ elderId: 0, admissionDate: '', bedId: undefined, bedStartDate: undefined })
const submitting = ref(false)
const submitChecklistLoading = ref(false)
const submitConfirmOpen = ref(false)
const linkedLeadId = ref<number>()
const guardContract = ref<CrmContractItem | null>(null)
const { elderOptions, elderLoading, searchElders, findElderName, ensureSelectedElder } = useElderOptions({ pageSize: 120 })
const buildings = ref<BuildingItem[]>([])
const floors = ref<FloorItem[]>([])
const rooms = ref<RoomItem[]>([])
const beds = ref<BedItem[]>([])

const assetSelect = reactive({
  buildingId: undefined as number | undefined,
  floorId: undefined as number | undefined,
  roomId: undefined as number | undefined
})

const buildingOptions = computed(() =>
  buildings.value.map((b) => ({ label: b.name, value: b.id }))
)
const floorOptions = computed(() =>
  floors.value
    .filter((f) => (assetSelect.buildingId ? f.buildingId === assetSelect.buildingId : true))
    .map((f) => ({ label: f.floorNo, value: f.id }))
)
const roomOptions = computed(() =>
  rooms.value
    .filter((r) => (assetSelect.floorId ? r.floorId === assetSelect.floorId : true))
    .map((r) => ({ label: r.roomNo, value: r.id }))
)
const bedOptions = computed(() =>
  beds.value
    .filter((b) => (assetSelect.roomId ? b.roomId === assetSelect.roomId : true))
    .filter((b) => !b.elderId && (b.status === 1 || b.status === undefined))
    .map((b) => ({ label: b.bedNo, value: b.id }))
)

const rules: FormRules = {
  elderId: [{ required: true, message: '请选择老人姓名' }],
  admissionDate: [{ required: true, message: '请选择收费开始日期' }],
  contractNo: [{ required: true, message: '请输入合同号' }]
}

const recordLoading = ref(false)
const recordSummaryLoading = ref(false)
const admissionRows = ref<AdmissionRecordItem[]>([])
const recordTotal = ref(0)
const recordSummary = reactive<AdmissionRecordSummary>({
  totalCount: 0,
  inHospitalCount: 0,
  leaveCount: 0,
  dischargedCount: 0,
  otherStatusCount: 0,
  withContractCount: 0,
  withoutContractCount: 0,
  todayAdmissionCount: 0,
  buildingDistribution: [],
  floorDistribution: []
})
const recordDimension = ref<'BUILDING' | 'FLOOR'>('BUILDING')
const skipNextRecordRouteWatch = ref(false)
const recordRouteSignature = ref('')
type SubmitChecklistKey = 'elder-base' | 'family' | 'disease' | 'contract' | 'attachment'
type SubmitChecklistItem = { key: SubmitChecklistKey; label: string; ready: boolean; note: string }
const submitChecklist = ref<SubmitChecklistItem[]>([
  { key: 'elder-base', label: '老人基础信息', ready: false, note: '待校验' },
  { key: 'family', label: '家属信息', ready: false, note: '待校验' },
  { key: 'disease', label: '基础疾病信息', ready: false, note: '待校验' },
  { key: 'contract', label: '合同信息', ready: false, note: '待校验' },
  { key: 'attachment', label: '身份证/资料附件', ready: false, note: '待校验' }
])
const submitBlockingCount = computed(() =>
  submitChecklist.value.filter((item) => !item.ready && item.key !== 'disease').length
)
const recordQuery = reactive({
  keyword: undefined as string | undefined,
  contractNo: undefined as string | undefined,
  elderStatus: undefined as number | undefined,
  admissionDateRange: undefined as [string, string] | undefined,
  pageNo: 1,
  pageSize: 10
})
const RECORD_ROUTE_KEYS = new Set([
  'recordKeyword',
  'recordContractNo',
  'recordElderStatus',
  'recordAdmissionDateStart',
  'recordAdmissionDateEnd',
  'recordDimension',
  'recordPageNo',
  'recordPageSize'
])
const SORTED_RECORD_ROUTE_KEYS = [...RECORD_ROUTE_KEYS].sort()

const recordSummaryGeneratedAtText = computed(() =>
  recordSummary.generatedAt ? dayjs(recordSummary.generatedAt).format('MM-DD HH:mm:ss') : '-'
)
const recordSummaryCards = computed(() => {
  const cards = [
    {
      key: 'total',
      label: '筛选结果',
      value: Number(recordSummary.totalCount || 0),
      hint: `今日办理 ${Number(recordSummary.todayAdmissionCount || 0)}`
    },
    {
      key: 'inHospital',
      label: '在院',
      value: Number(recordSummary.inHospitalCount || 0),
      hint: '当前状态 = 在院'
    },
    {
      key: 'leave',
      label: '请假',
      value: Number(recordSummary.leaveCount || 0),
      hint: '当前状态 = 请假'
    },
    {
      key: 'discharged',
      label: '离院',
      value: Number(recordSummary.dischargedCount || 0),
      hint: '当前状态 = 离院'
    },
    {
      key: 'withContract',
      label: '有合同号',
      value: Number(recordSummary.withContractCount || 0),
      hint: `缺合同 ${Number(recordSummary.withoutContractCount || 0)}`
    },
    {
      key: 'other',
      label: '其他状态',
      value: Number(recordSummary.otherStatusCount || 0),
      hint: '异常状态核查'
    }
  ]
  return cards
})

const activeDistributionRows = computed<AdmissionRecordDimensionItem[]>(() => {
  const source = recordDimension.value === 'BUILDING'
    ? recordSummary.buildingDistribution
    : recordSummary.floorDistribution
  if (!Array.isArray(source)) {
    return []
  }
  return source
    .filter((item) => Number(item?.count || 0) > 0)
    .slice(0, 12)
})

function formatDistributionRatio(value: number) {
  const ratio = Number(value || 0)
  if (!Number.isFinite(ratio) || ratio <= 0) {
    return '0.0%'
  }
  return `${(ratio * 100).toFixed(1)}%`
}

function firstQueryText(value: unknown) {
  if (Array.isArray(value)) {
    return firstQueryText(value[0])
  }
  if (value == null) return ''
  return String(value).trim()
}

function normalizeRouteQueryMap(source: Record<string, unknown>) {
  return Object.entries(source || {}).reduce<Record<string, string>>((acc, [key, value]) => {
    const text = firstQueryText(value)
    if (!text) return acc
    acc[key] = text
    return acc
  }, {})
}

function buildRecordRouteSignature(source: Record<string, unknown>) {
  return SORTED_RECORD_ROUTE_KEYS.map((key) => `${key}:${firstQueryText(source[key])}`).join('|')
}

function resetRecordQueryState() {
  recordQuery.keyword = undefined
  recordQuery.contractNo = undefined
  recordQuery.elderStatus = undefined
  recordQuery.admissionDateRange = undefined
  recordDimension.value = 'BUILDING'
  recordQuery.pageNo = 1
  recordQuery.pageSize = 10
}

function applyRecordQueryFromRoute() {
  resetRecordQueryState()
  const keyword = firstQueryText(route.query.recordKeyword)
  const contractNo = firstQueryText(route.query.recordContractNo)
  const elderStatus = Number(firstQueryText(route.query.recordElderStatus))
  const admissionDateStart = firstQueryText(route.query.recordAdmissionDateStart)
  const admissionDateEnd = firstQueryText(route.query.recordAdmissionDateEnd)
  const dimension = firstQueryText(route.query.recordDimension).toUpperCase()
  const pageNo = Number(firstQueryText(route.query.recordPageNo))
  const pageSize = Number(firstQueryText(route.query.recordPageSize))

  if (keyword) recordQuery.keyword = keyword
  if (contractNo) recordQuery.contractNo = contractNo
  if (Number.isFinite(elderStatus) && elderStatus > 0) recordQuery.elderStatus = elderStatus
  if (admissionDateStart && admissionDateEnd) {
    recordQuery.admissionDateRange = [admissionDateStart, admissionDateEnd]
  }
  if (dimension === 'BUILDING' || dimension === 'FLOOR') {
    recordDimension.value = dimension
  }
  if (Number.isFinite(pageNo) && pageNo > 0) recordQuery.pageNo = pageNo
  if (Number.isFinite(pageSize) && pageSize > 0) recordQuery.pageSize = pageSize
}

function buildRecordRouteQuery() {
  const query: Record<string, string> = {}
  Object.entries(route.query || {}).forEach(([key, value]) => {
    if (RECORD_ROUTE_KEYS.has(key)) return
    const text = firstQueryText(value)
    if (!text) return
    query[key] = text
  })
  if (recordQuery.keyword) query.recordKeyword = recordQuery.keyword
  if (recordQuery.contractNo) query.recordContractNo = recordQuery.contractNo
  if (recordQuery.elderStatus) query.recordElderStatus = String(recordQuery.elderStatus)
  if (recordQuery.admissionDateRange?.[0]) query.recordAdmissionDateStart = recordQuery.admissionDateRange[0]
  if (recordQuery.admissionDateRange?.[1]) query.recordAdmissionDateEnd = recordQuery.admissionDateRange[1]
  query.recordDimension = recordDimension.value
  query.recordPageNo = String(recordQuery.pageNo)
  query.recordPageSize = String(recordQuery.pageSize)
  return query
}

async function syncRecordQueryToRoute() {
  const nextQuery = buildRecordRouteQuery()
  const currentQuery = normalizeRouteQueryMap(route.query as Record<string, unknown>)
  const currentKeys = Object.keys(currentQuery)
  const nextKeys = Object.keys(nextQuery)
  if (currentKeys.length === nextKeys.length && nextKeys.every((key) => currentQuery[key] === nextQuery[key])) {
    return
  }
  skipNextRecordRouteWatch.value = true
  recordRouteSignature.value = buildRecordRouteSignature(nextQuery)
  await router.replace({ path: route.path, query: nextQuery })
}

const columns = [
  { title: '老人姓名', dataIndex: 'elderName', key: 'elderName', width: 140 },
  { title: '合同号', dataIndex: 'contractNo', key: 'contractNo', width: 160 },
  { title: '收费开始日期', dataIndex: 'admissionDate', key: 'admissionDate', width: 140 },
  { title: '押金（元）', dataIndex: 'depositAmount', key: 'depositAmount', width: 120 },
  { title: '入住状态', key: 'elderStatus', width: 100 },
  { title: '备注', dataIndex: 'remark', key: 'remark' },
  { title: '操作员姓名', key: 'operatorName', width: 140 },
  { title: '登记时间', dataIndex: 'createTime', key: 'createTime', width: 180 }
]
const admissionFlowSubject = computed(() => {
  if (!form.contractNo) return '请先选择老人并自动回填合同号'
  if (!guardContract.value) return `合同号 ${form.contractNo} 未匹配`
  return `合同 ${guardContract.value.contractNo || '-'} / 长者 ${guardContract.value.elderName || '-'}`
})
const admissionLifecycleStage = computed(() =>
  normalizeLifecycleStage(guardContract.value?.flowStage, guardContract.value?.contractStatus || guardContract.value?.status)
)
const admissionLifecycleHint = computed(() => {
  if (!form.contractNo) return '请先选择长者，系统会自动匹配合同号并同步流程阶段。'
  if (!guardContract.value) {
    return '未找到合同，请到合同签约核对合同号。'
  }
  if (guardContract.value.status === 'VOID') {
    return '合同已作废，不能继续办理入住。'
  }
  return lifecycleStageHint(admissionLifecycleStage.value)
})

function statusText(status?: number) {
  if (status === 1) return '在院'
  if (status === 2) return '请假'
  if (status === 3) return '离院'
  return '-'
}

function displayElderName(record: AdmissionRecordItem) {
  const name = String(record.elderName || '').trim()
  if (name) return name
  const elderId = Number(record.elderId || 0)
  if (elderId > 0) {
    const fallback = String(findElderName(elderId) || '').trim()
    if (fallback) return fallback
  }
  return '-'
}

async function submit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitting.value = true
    if (form.bedId && !form.bedStartDate) {
      form.bedStartDate = form.admissionDate || new Date().toISOString().slice(0, 10)
    }
    await admitElder(form)
    await syncLeadStageAfterAdmission()
    message.success('入住办理成功')
    await refreshAdmissionRecordPanel()
  } catch (error: any) {
    message.error(error?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

function checklistRoute(key: SubmitChecklistKey) {
  const elderId = Number(form.elderId || 0)
  const contractNo = encodeURIComponent(String(form.contractNo || '').trim())
  const toContractElderInfo = () => {
    if (contractNo) {
      return `/marketing/contract-signing?contractNo=${contractNo}&openEdit=1&focus=elder-info`
    }
    return '/marketing/contract-signing?quick=1'
  }
  if (key === 'elder-base') return toContractElderInfo()
  if (key === 'family') return toContractElderInfo()
  if (key === 'disease') return toContractElderInfo()
  if (key === 'contract') return contractNo ? `/marketing/contract-signing?contractNo=${contractNo}&openEdit=1` : '/marketing/contract-signing'
  if (key === 'attachment') {
    if (contractNo) {
      return `/marketing/contract-signing?contractNo=${contractNo}&openAttachment=1&attachmentType=CONTRACT&from=admission_checklist`
    }
    return elderId ? `/marketing/contract-signing?elderId=${elderId}&openAttachment=1&from=admission_checklist` : '/marketing/contract-signing'
  }
  return '/elder/list'
}

function jumpToChecklist(key: SubmitChecklistKey) {
  submitConfirmOpen.value = false
  router.push(checklistRoute(key))
}

async function buildSubmitChecklist() {
  const elderId = Number(form.elderId || 0)
  const contractNo = String(form.contractNo || '').trim()
  const [elderDetailRes, familyRes, diseaseRes, contractPageRes] = await Promise.all([
    elderId ? getElderDetail(elderId).catch(() => null) : Promise.resolve(null),
    elderId ? getFamilyRelations(elderId).catch(() => []) : Promise.resolve([]),
    elderId ? getElderDiseases(elderId).catch(() => []) : Promise.resolve([]),
    contractNo ? getContractPage({ pageNo: 1, pageSize: 20, contractNo }).catch(() => ({ list: [] })) : Promise.resolve({ list: [] as CrmContractItem[] })
  ])
  const contract = (contractPageRes.list || []).find((item) => String(item.contractNo || '').trim() === contractNo) || null
  const attachments = contract?.id
    ? await getContractAttachments(contract.id).catch(() => [])
    : []
  const hasIdDoc = attachments.some((item) => {
    const type = String(item.attachmentType || '').toUpperCase()
    const name = String(item.fileName || '').toUpperCase()
    return type.includes('ID') || type.includes('IDENT') || name.includes('身份证')
  })
  return [
    {
      key: 'elder-base' as const,
      label: '老人基础信息',
      ready: Boolean(elderDetailRes?.fullName && elderDetailRes?.idCardNo),
      note: elderDetailRes?.fullName && elderDetailRes?.idCardNo ? '姓名/身份证已填写' : '缺少姓名或身份证'
    },
    {
      key: 'family' as const,
      label: '家属信息',
      ready: Array.isArray(familyRes) && familyRes.length > 0,
      note: Array.isArray(familyRes) && familyRes.length > 0 ? `已绑定 ${familyRes.length} 位家属` : '未绑定家属'
    },
    {
      key: 'disease' as const,
      label: '基础疾病信息',
      ready: Array.isArray(diseaseRes) && diseaseRes.length > 0,
      note: Array.isArray(diseaseRes) && diseaseRes.length > 0 ? `已维护 ${diseaseRes.length} 项疾病信息` : '未维护疾病信息'
    },
    {
      key: 'contract' as const,
      label: '合同信息',
      ready: Boolean(contract && contract.contractNo),
      note: contract ? `合同号 ${contract.contractNo || '-'}（${contract.status || contract.flowStage || '待处理'}）` : '未找到合同'
    },
    {
      key: 'attachment' as const,
      label: '身份证/资料附件',
      ready: attachments.length > 0 && hasIdDoc,
      note: attachments.length
        ? (hasIdDoc ? `已上传 ${attachments.length} 份附件（含身份证）` : `已上传 ${attachments.length} 份附件（缺身份证类）`)
        : '未上传附件'
    }
  ]
}

async function openSubmitConfirm() {
  if (!form.elderId) {
    message.warning('请先选择老人')
    return
  }
  if (!form.contractNo) {
    message.warning('请先自动回填合同号')
    return
  }
  submitChecklistLoading.value = true
  try {
    submitChecklist.value = await buildSubmitChecklist()
    submitConfirmOpen.value = true
  } finally {
    submitChecklistLoading.value = false
  }
}

async function confirmSubmit() {
  if (submitBlockingCount.value > 0) {
    message.warning('存在关键资料未完善，请先返回检查并补齐后再提交')
    return
  }
  submitConfirmOpen.value = false
  await submit()
}

async function loadContractGuardState() {
  const contractNo = String(form.contractNo || '').trim()
  if (!contractNo) {
    guardContract.value = null
    return
  }
  try {
    const page: PageResult<CrmContractItem> = await getContractPage({
      pageNo: 1,
      pageSize: 20,
      contractNo
    })
    guardContract.value = (page.list || []).find((item) => item.contractNo === contractNo) || null
    if (guardContract.value?.elderId) {
      form.elderId = guardContract.value.elderId as any
      ensureSelectedElder(Number(guardContract.value.elderId), guardContract.value.elderName || `长者${String(guardContract.value.elderId)}`)
      if (!recordQuery.keyword && guardContract.value.elderName) {
        recordQuery.keyword = guardContract.value.elderName
      }
    }
  } catch {
    guardContract.value = null
  }
}

function contractPriority(item: CrmContractItem) {
  const status = String(item.status || '')
  const stage = String(item.flowStage || '')
  if (status === 'VOID') return -1
  if (stage === 'PENDING_BED_SELECT') return 500
  if (stage === 'PENDING_SIGN') return 450
  if (stage === 'SIGNED') return 400
  if (status === 'EFFECTIVE') return 350
  if (stage === 'PENDING_ASSESSMENT') return 300
  return 100
}

async function resolveContractNoByElder(elderIdRaw: string | number | undefined) {
  const elderId = Number(elderIdRaw || 0)
  if (!elderId) {
    form.contractNo = ''
    guardContract.value = null
    return
  }
  const elderName = findElderName(elderId)
  const page: PageResult<CrmContractItem> = await getContractPage({
    pageNo: 1,
    pageSize: 200,
    elderId,
    elderName: elderName || undefined
  })
  const list = (page.list || []).filter((item) => Number(item.elderId || 0) === elderId || !item.elderId)
  if (!list.length) {
    form.contractNo = ''
    guardContract.value = null
    message.warning('该老人暂无可用合同，请先在合同签约中创建/确认合同')
    return
  }
  const picked = [...list]
    .sort((a, b) => {
      const diff = contractPriority(b) - contractPriority(a)
      if (diff !== 0) return diff
      const ta = new Date(a.updateTime || a.createTime || 0).getTime()
      const tb = new Date(b.updateTime || b.createTime || 0).getTime()
      return tb - ta
    })[0]
  form.contractNo = String(picked.contractNo || '').trim()
  guardContract.value = picked || null
  if (picked.leadId && !linkedLeadId.value) {
    linkedLeadId.value = Number(picked.leadId)
  }
}

async function onElderChange(value: string | number | undefined) {
  form.elderId = (value as any) || 0
  await resolveContractNoByElder(value)
}

function buildAdmissionRecordFilterParams() {
  const [admissionDateStart, admissionDateEnd] = recordQuery.admissionDateRange || []
  return {
    keyword: recordQuery.keyword,
    contractNo: recordQuery.contractNo,
    elderStatus: recordQuery.elderStatus,
    admissionDateStart,
    admissionDateEnd
  }
}

async function fetchAdmissionRecords() {
  recordLoading.value = true
  try {
    const res: PageResult<AdmissionRecordItem> = await getAdmissionRecords({
      pageNo: recordQuery.pageNo,
      pageSize: recordQuery.pageSize,
      ...buildAdmissionRecordFilterParams()
    })
    admissionRows.value = res.list || []
    admissionRows.value.forEach((item) => {
      const elderId = Number(item.elderId || 0)
      if (elderId > 0) {
        ensureSelectedElder(elderId, String(item.elderName || '').trim() || undefined)
      }
    })
    recordTotal.value = Number(res.total || 0)
  } finally {
    recordLoading.value = false
  }
}

async function fetchAdmissionRecordSummary() {
  recordSummaryLoading.value = true
  try {
    const data = await getAdmissionRecordSummary(buildAdmissionRecordFilterParams())
    Object.assign(recordSummary, {
      totalCount: Number(data?.totalCount || 0),
      inHospitalCount: Number(data?.inHospitalCount || 0),
      leaveCount: Number(data?.leaveCount || 0),
      dischargedCount: Number(data?.dischargedCount || 0),
      otherStatusCount: Number(data?.otherStatusCount || 0),
      withContractCount: Number(data?.withContractCount || 0),
      withoutContractCount: Number(data?.withoutContractCount || 0),
      todayAdmissionCount: Number(data?.todayAdmissionCount || 0),
      buildingDistribution: Array.isArray(data?.buildingDistribution)
        ? data.buildingDistribution.map((item) => ({
            dimensionKey: String(item?.dimensionKey || ''),
            dimensionLabel: String(item?.dimensionLabel || item?.dimensionKey || '-'),
            count: Number(item?.count || 0),
            ratio: Number(item?.ratio || 0)
          }))
        : [],
      floorDistribution: Array.isArray(data?.floorDistribution)
        ? data.floorDistribution.map((item) => ({
            dimensionKey: String(item?.dimensionKey || ''),
            dimensionLabel: String(item?.dimensionLabel || item?.dimensionKey || '-'),
            count: Number(item?.count || 0),
            ratio: Number(item?.ratio || 0)
          }))
        : [],
      generatedAt: data?.generatedAt
    })
  } catch {
    Object.assign(recordSummary, {
      totalCount: 0,
      inHospitalCount: 0,
      leaveCount: 0,
      dischargedCount: 0,
      otherStatusCount: 0,
      withContractCount: 0,
      withoutContractCount: 0,
      todayAdmissionCount: 0,
      buildingDistribution: [],
      floorDistribution: [],
      generatedAt: undefined
    })
  } finally {
    recordSummaryLoading.value = false
  }
}

async function refreshAdmissionRecordPanel() {
  await Promise.all([fetchAdmissionRecords(), fetchAdmissionRecordSummary()])
}

function runRecordSearch() {
  recordQuery.pageNo = 1
  refreshAdmissionRecordPanel()
  syncRecordQueryToRoute().catch(() => {})
}

function resetRecordQuery() {
  resetRecordQueryState()
  refreshAdmissionRecordPanel()
  syncRecordQueryToRoute().catch(() => {})
}

async function exportRecords() {
  await exportAdmissionRecords({
    ...buildAdmissionRecordFilterParams()
  })
}

async function copyRecordSearchLink() {
  const href = router.resolve({ path: route.path, query: buildRecordRouteQuery() }).href
  const fullUrl = /^https?:\/\//i.test(href) ? href : `${window.location.origin}${href}`
  const copied = await copyText(fullUrl)
  if (copied) {
    message.success('检索链接已复制')
    return
  }
  message.warning('当前环境不支持自动复制，请手动复制地址栏链接')
}

async function loadAssets() {
  try {
    const [buildingList, floorList, roomList, bedList] = await Promise.all([
      getBuildingList(),
      getFloorList(),
      getRoomList(),
      getBedList()
    ])
    buildings.value = buildingList
    floors.value = floorList
    rooms.value = roomList
    beds.value = bedList
  } catch {
    buildings.value = []
    floors.value = []
    rooms.value = []
    beds.value = []
  }
}

function applyRoutePrefill() {
  applyRecordQueryFromRoute()
  const residentIdText = String(route.query.residentId || '').trim()
  const leadId = Number(route.query.leadId || 0)
  const contractNo = String(route.query.contractNo || '').trim()
  const elderName = String(route.query.elderName || '').trim()
  if (residentIdText && !recordQuery.keyword) {
    const residentId = Number(residentIdText)
    if (residentId > 0) {
      ensureSelectedElder(residentId, elderName || `长者${residentId}`)
      form.elderId = residentId as any
      recordQuery.keyword = elderName || findElderName(residentId) || residentIdText
    } else {
      form.elderId = residentIdText as any
      recordQuery.keyword = elderName || residentIdText
    }
  }
  if (!recordQuery.keyword && elderName) {
    recordQuery.keyword = elderName
  }
  if (leadId > 0) {
    linkedLeadId.value = leadId
  }
  if (contractNo) {
    form.contractNo = contractNo
    if (!recordQuery.contractNo) {
      recordQuery.contractNo = contractNo
    }
  }
  if (!form.admissionDate) {
    form.admissionDate = dayjs().format('YYYY-MM-DD')
  }
}

async function syncLeadStageAfterAdmission() {
  if (!linkedLeadId.value) return
  try {
    const lead = await getCrmLead(linkedLeadId.value)
    if (!lead?.id) return
    await updateCrmLead(lead.id, {
      ...lead,
      contractNo: form.contractNo || lead.contractNo,
      reservationBedId: form.bedId || lead.reservationBedId,
      flowStage: 'PENDING_SIGN',
      currentOwnerDept: 'MARKETING',
      contractStatus: '待签署',
      status: 2
    })
  } catch {
    message.warning('入住成功，但合同流程状态同步失败，请返回合同签约页重试')
  }
}

function onPageChange(page: number) {
  recordQuery.pageNo = page
  refreshAdmissionRecordPanel()
  syncRecordQueryToRoute().catch(() => {})
}

function onPageSizeChange(current: number, size: number) {
  recordQuery.pageNo = 1
  recordQuery.pageSize = size
  refreshAdmissionRecordPanel()
  syncRecordQueryToRoute().catch(() => {})
}

watch(
  () => assetSelect.buildingId,
  () => {
    assetSelect.floorId = undefined
    assetSelect.roomId = undefined
    form.bedId = undefined
  }
)
watch(
  () => assetSelect.floorId,
  () => {
    assetSelect.roomId = undefined
    form.bedId = undefined
  }
)
watch(
  () => assetSelect.roomId,
  () => {
    form.bedId = undefined
  }
)
watch(
  () => form.contractNo,
  () => {
    loadContractGuardState()
  }
)
watch(
  () => route.query,
  () => {
    const nextSignature = buildRecordRouteSignature(route.query as Record<string, unknown>)
    if (skipNextRecordRouteWatch.value) {
      skipNextRecordRouteWatch.value = false
      recordRouteSignature.value = nextSignature
      return
    }
    if (nextSignature === recordRouteSignature.value) {
      return
    }
    recordRouteSignature.value = nextSignature
    applyRecordQueryFromRoute()
    refreshAdmissionRecordPanel()
  },
  { deep: true }
)
watch(recordDimension, () => {
  syncRecordQueryToRoute().catch(() => {})
})

useLiveSyncRefresh({
  topics: ['elder', 'bed', 'lifecycle', 'finance', 'care'],
  refresh: () => {
    searchElders('')
    loadAssets()
    refreshAdmissionRecordPanel()
  },
  debounceMs: 700
})

onMounted(async () => {
  await searchElders('')
  await loadAssets()
  applyRoutePrefill()
  if (!form.contractNo && form.elderId) {
    await resolveContractNoByElder(form.elderId as any)
  }
  await refreshAdmissionRecordPanel()
  recordRouteSignature.value = buildRecordRouteSignature(route.query as Record<string, unknown>)
  await syncRecordQueryToRoute().catch(() => {})
})
</script>

<style scoped>
.search-bar {
  margin-bottom: 12px;
}

.admission-summary-card {
  border: 1px solid #e6f4ff;
  background: linear-gradient(118deg, #f8fbff 0%, #f2f8ff 45%, #f9fcff 100%);
}

.admission-summary-head {
  margin-bottom: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.admission-summary-title {
  color: #0f172a;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.2px;
}

.admission-summary-meta {
  color: #64748b;
  font-size: 12px;
}

.admission-summary-tile {
  border-radius: 10px;
  border: 1px solid rgba(148, 163, 184, 0.24);
  background: rgba(255, 255, 255, 0.8);
  padding: 10px;
  min-height: 88px;
}

.admission-summary-label {
  color: #64748b;
  font-size: 12px;
}

.admission-summary-value {
  margin-top: 6px;
  color: #0f172a;
  font-size: 24px;
  line-height: 1.1;
  font-weight: 700;
}

.admission-summary-hint {
  margin-top: 6px;
  color: #475569;
  font-size: 12px;
}

.admission-distribution-panel {
  margin-top: 14px;
  padding: 12px;
  border-radius: 12px;
  border: 1px solid rgba(148, 163, 184, 0.22);
  background: linear-gradient(130deg, rgba(255, 255, 255, 0.95) 0%, rgba(241, 245, 249, 0.84) 100%);
}

.admission-distribution-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 10px;
}

.admission-distribution-title {
  font-size: 13px;
  font-weight: 600;
  color: #0f172a;
}

.admission-distribution-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.admission-distribution-item {
  padding: 8px 10px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(148, 163, 184, 0.2);
}

.admission-distribution-row {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  align-items: center;
  margin-bottom: 6px;
}

.admission-distribution-label {
  color: #334155;
  font-size: 12px;
  font-weight: 500;
}

.admission-distribution-meta {
  color: #64748b;
  font-size: 12px;
}

.admission-distribution-track {
  height: 8px;
  border-radius: 999px;
  background: rgba(148, 163, 184, 0.2);
  overflow: hidden;
}

.admission-distribution-fill {
  height: 100%;
  min-width: 4px;
  border-radius: inherit;
  background: linear-gradient(90deg, #1677ff 0%, #36cfc9 100%);
  transition: width 0.35s ease;
}
</style>
