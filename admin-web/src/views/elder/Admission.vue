<template>
  <PageContainer title="入住办理" subTitle="分床、押金账户、合同信息与二维码配置">
    <a-card class="card-elevated" :bordered="false">
      <h3>办理入住流程</h3>
      <FlowGuardBar
        :title="'入住办理守卫'"
        :subject="admissionFlowSubject"
        :stage-text="admissionFlowStageText"
        :stage-color="admissionFlowStageColor"
        :steps="admissionFlowSteps"
        :current-index="admissionFlowIndex"
        :blockers="admissionFlowBlockers"
        :hint="admissionFlowHint"
        @action="handleAdmissionGuardAction"
        style="margin-bottom: 12px"
      />
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
            <a-button type="primary" @click="fetchAdmissionRecords">搜索</a-button>
            <a-button @click="resetRecordQuery">清空</a-button>
            <a-button @click="exportRecords">导出</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <a-table :data-source="admissionRows" :columns="columns" :loading="recordLoading" :pagination="false" row-key="id">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'elderStatus'">
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
import FlowGuardBar from '../../components/FlowGuardBar.vue'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'
import { useElderOptions } from '../../composables/useElderOptions'
import { getFamilyRelations } from '../../api/family'
import { getBedList, getBuildingList, getFloorList, getRoomList } from '../../api/bed'
import { admitElder, exportAdmissionRecords, getAdmissionRecords } from '../../api/elderLifecycle'
import { getElderDetail, getElderDiseases } from '../../api/elder'
import { getContractAttachments, getContractPage } from '../../api/marketing'
import { getCrmLead, updateCrmLead } from '../../api/crm'
import type {
  AdmissionRecordItem,
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
const admissionRows = ref<AdmissionRecordItem[]>([])
const recordTotal = ref(0)
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
const admissionFlowSteps = ['合同完成评估', '办理入住选床', '进入待签署', '最终签署']
const admissionFlowIndex = computed(() => {
  if (!guardContract.value) return 0
  if (guardContract.value.flowStage === 'PENDING_ASSESSMENT') return 0
  if (guardContract.value.flowStage === 'PENDING_BED_SELECT') return 1
  if (guardContract.value.flowStage === 'PENDING_SIGN') return 2
  return 3
})
const admissionFlowStageText = computed(() => {
  if (!form.contractNo) return '待输入合同号'
  if (!guardContract.value) return '合同未找到'
  if (guardContract.value.flowStage === 'PENDING_ASSESSMENT') return '待评估'
  if (guardContract.value.flowStage === 'PENDING_BED_SELECT') return '待办理入住'
  if (guardContract.value.flowStage === 'PENDING_SIGN') return '待签署'
  if (guardContract.value.flowStage === 'SIGNED') return '已签署'
  return guardContract.value.flowStage || '-'
})
const admissionFlowStageColor = computed(() => {
  if (!guardContract.value) return 'default'
  if (guardContract.value.flowStage === 'PENDING_ASSESSMENT') return 'gold'
  if (guardContract.value.flowStage === 'PENDING_BED_SELECT') return 'blue'
  if (guardContract.value.flowStage === 'PENDING_SIGN') return 'purple'
  if (guardContract.value.flowStage === 'SIGNED') return 'green'
  return 'default'
})
const admissionFlowSubject = computed(() => {
  if (!form.contractNo) return '请先选择老人并自动回填合同号'
  if (!guardContract.value) return `合同号 ${form.contractNo} 未匹配`
  return `合同 ${guardContract.value.contractNo || '-'} / 长者 ${guardContract.value.elderName || '-'}`
})
const admissionFlowBlockers = computed(() => {
  if (!form.contractNo) return [{ code: 'G100', text: '未匹配到合同号，请先选择老人', actionLabel: '选择老人', actionKey: 'focus-elder' }]
  if (!guardContract.value) return [{ code: 'G102', text: '合同不存在或已删除', actionLabel: '去合同签约', actionKey: 'go-contract' }]
  const blockers: Array<{ code: string; text: string; actionLabel?: string; actionKey?: string }> = []
  if (guardContract.value.status === 'VOID') blockers.push({ code: 'G401', text: '合同已作废' })
  if (guardContract.value.flowStage === 'PENDING_ASSESSMENT') {
    blockers.push({ code: 'G201', text: '尚未完成入住评估', actionLabel: '去评估并闭环回流', actionKey: 'go-assessment' })
  }
  if (guardContract.value.flowStage === 'SIGNED' || guardContract.value.status === 'SIGNED' || guardContract.value.status === 'EFFECTIVE') {
    blockers.push({ code: 'G402', text: '合同已签署，不能重复办理入住' })
  }
  return blockers
})
const admissionFlowHint = computed(() => {
  if (!guardContract.value) return ''
  if (guardContract.value.flowStage === 'PENDING_BED_SELECT') return '当前可提交入住办理并同步推进到待签署'
  if (guardContract.value.flowStage === 'PENDING_SIGN') return '已满足入住环节，可回到合同签约执行最终签署'
  if (guardContract.value.flowStage === 'SIGNED') return '当前合同流程已完成'
  if (guardContract.value.flowStage === 'PENDING_ASSESSMENT') return '请通过“去评估并闭环回流”完成评估后自动返回本页'
  return ''
})

function handleAdmissionGuardAction(item: { actionKey?: string }) {
  if (item.actionKey === 'focus-elder') {
    message.info('请先在“老人姓名”中选择长者，系统会自动回填合同号')
    return
  }
  if (item.actionKey === 'go-contract') {
    router.push('/marketing/contract-signing')
    return
  }
  if (item.actionKey === 'go-assessment') {
    const params = new URLSearchParams()
    params.set('autoOpen', '1')
    params.set('closeLoop', '1')
    params.set('mode', 'new')
    if (form.contractNo) params.set('contractNo', String(form.contractNo))
    if (guardContract.value?.leadId) params.set('leadId', String(guardContract.value.leadId))
    if (guardContract.value?.elderName) params.set('elderName', String(guardContract.value.elderName))
    router.push(`/elder/assessment/ability/admission?${params.toString()}`)
  }
}

function statusText(status?: number) {
  if (status === 1) return '在院'
  if (status === 2) return '请假'
  if (status === 3) return '离院'
  return '-'
}

async function submit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    const canContinue = await validateAdmissionGuard()
    if (!canContinue) return
    submitting.value = true
    if (form.bedId && !form.bedStartDate) {
      form.bedStartDate = form.admissionDate || new Date().toISOString().slice(0, 10)
    }
    await admitElder(form)
    await syncLeadStageAfterAdmission()
    message.success('入住办理成功')
    await fetchAdmissionRecords()
  } catch (error: any) {
    message.error(error?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

function checklistRoute(key: SubmitChecklistKey) {
  const elderId = Number(form.elderId || 0)
  const contractNo = encodeURIComponent(String(form.contractNo || '').trim())
  if (key === 'elder-base') return elderId ? `/elder/detail/${elderId}?tab=base` : '/elder/list'
  if (key === 'family') return elderId ? `/elder/detail/${elderId}?tab=family` : '/elder/family'
  if (key === 'disease') return elderId ? `/elder/detail/${elderId}?tab=disease` : '/elder/list'
  if (key === 'contract') return contractNo ? `/marketing/contract-signing?contractNo=${contractNo}` : '/marketing/contract-signing'
  if (key === 'attachment') {
    if (elderId) return `/elder/contracts-invoices?residentId=${elderId}&tab=attachments`
    return contractNo ? `/marketing/contract-signing?contractNo=${contractNo}` : '/marketing/contract-signing'
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

async function validateAdmissionGuard() {
  const contractNo = String(form.contractNo || '').trim()
  if (!contractNo) {
    message.warning('请先选择老人并自动回填合同号，再办理入住')
    return false
  }
  try {
    const page: PageResult<CrmContractItem> = await getContractPage({
      pageNo: 1,
      pageSize: 20,
      contractNo
    })
    const contract = (page.list || []).find((item) => item.contractNo === contractNo)
    guardContract.value = contract || null
    if (!contract) {
      message.warning('未找到该合同号，请先在合同签约中确认')
      return false
    }
    if (contract.flowStage === 'PENDING_ASSESSMENT') {
      message.warning('合同尚未完成入住评估，请先完成评估后再办理入住')
      return false
    }
    if (contract.flowStage === 'SIGNED' || contract.status === 'SIGNED' || contract.status === 'EFFECTIVE') {
      message.warning('该合同已完成签署，无法重复办理入住')
      return false
    }
    if (contract.status === 'VOID') {
      message.warning('作废合同不可办理入住')
      return false
    }
    if (contract.elderId && String(form.elderId) !== String(contract.elderId)) {
      message.warning('当前选择的老人和合同绑定老人不一致，请检查后重试')
      return false
    }
    if (contract.elderId) {
      form.elderId = contract.elderId as any
      ensureSelectedElder(Number(contract.elderId), contract.elderName || `长者${String(contract.elderId)}`)
    }
    if (contract.leadId && !linkedLeadId.value) {
      linkedLeadId.value = Number(contract.leadId)
    }
    return true
  } catch {
    guardContract.value = null
    message.warning('合同流程校验失败，请稍后重试')
    return false
  }
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

async function fetchAdmissionRecords() {
  recordLoading.value = true
  try {
    const [admissionDateStart, admissionDateEnd] = recordQuery.admissionDateRange || []
    const res: PageResult<AdmissionRecordItem> = await getAdmissionRecords({
      pageNo: recordQuery.pageNo,
      pageSize: recordQuery.pageSize,
      keyword: recordQuery.keyword,
      contractNo: recordQuery.contractNo,
      elderStatus: recordQuery.elderStatus,
      admissionDateStart,
      admissionDateEnd
    })
    admissionRows.value = res.list
    recordTotal.value = res.total
  } finally {
    recordLoading.value = false
  }
}

function resetRecordQuery() {
  recordQuery.keyword = undefined
  recordQuery.contractNo = undefined
  recordQuery.elderStatus = undefined
  recordQuery.admissionDateRange = undefined
  recordQuery.pageNo = 1
  fetchAdmissionRecords()
}

async function exportRecords() {
  const [admissionDateStart, admissionDateEnd] = recordQuery.admissionDateRange || []
  await exportAdmissionRecords({
    keyword: recordQuery.keyword,
    contractNo: recordQuery.contractNo,
    elderStatus: recordQuery.elderStatus,
    admissionDateStart,
    admissionDateEnd
  })
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
  const residentIdText = String(route.query.residentId || '').trim()
  const leadId = Number(route.query.leadId || 0)
  const contractNo = String(route.query.contractNo || '').trim()
  const elderName = String(route.query.elderName || '').trim()
  if (residentIdText) {
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
    recordQuery.contractNo = contractNo
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
  fetchAdmissionRecords()
}

function onPageSizeChange(current: number, size: number) {
  recordQuery.pageNo = 1
  recordQuery.pageSize = size
  fetchAdmissionRecords()
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

useLiveSyncRefresh({
  topics: ['elder', 'bed', 'lifecycle', 'finance', 'care'],
  refresh: () => {
    searchElders('')
    loadAssets()
    fetchAdmissionRecords()
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
  await fetchAdmissionRecords()
})
</script>

<style scoped>
.search-bar {
  margin-bottom: 12px;
}
</style>
