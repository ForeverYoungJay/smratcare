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
          <a-select v-model:value="form.elderId" placeholder="请选择老人姓名">
            <a-select-option v-for="item in elders" :key="item.id" :value="item.id">{{ item.fullName }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="收费开始日期" name="admissionDate">
          <a-date-picker v-model:value="form.admissionDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
        <a-form-item label="合同号" name="contractNo">
          <a-input v-model:value="form.contractNo" placeholder="请输入合同号" />
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
        <a-button type="primary" :loading="submitting" @click="submit">提交入住办理</a-button>
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
import { getElderPage } from '../../api/elder'
import { getBedList, getBuildingList, getFloorList, getRoomList } from '../../api/bed'
import { admitElder, exportAdmissionRecords, getAdmissionRecords } from '../../api/elderLifecycle'
import { getContractPage } from '../../api/marketing'
import { getCrmLead, updateCrmLead } from '../../api/crm'
import type {
  AdmissionRecordItem,
  AdmissionRequest,
  BedItem,
  BuildingItem,
  CrmContractItem,
  ElderItem,
  FloorItem,
  PageResult,
  RoomItem
} from '../../types'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const form = reactive<AdmissionRequest>({ elderId: 0, admissionDate: '', bedId: undefined, bedStartDate: undefined })
const submitting = ref(false)
const linkedLeadId = ref<number>()
const guardContract = ref<CrmContractItem | null>(null)
const elders = ref<ElderItem[]>([])
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
  if (!form.contractNo) return '请输入合同号后系统自动校验流程'
  if (!guardContract.value) return `合同号 ${form.contractNo} 未匹配`
  return `合同 ${guardContract.value.contractNo || '-'} / 长者 ${guardContract.value.elderName || '-'}`
})
const admissionFlowBlockers = computed(() => {
  if (!form.contractNo) return [{ code: 'G100', text: '未输入合同号，请先输入合同号', actionLabel: '填写合同号', actionKey: 'focus-contract' }]
  if (!guardContract.value) return [{ code: 'G102', text: '合同不存在或已删除', actionLabel: '去合同签约', actionKey: 'go-contract' }]
  const blockers: Array<{ code: string; text: string; actionLabel?: string; actionKey?: string }> = []
  if (guardContract.value.status === 'VOID') blockers.push({ code: 'G401', text: '合同已作废' })
  if (guardContract.value.flowStage === 'PENDING_ASSESSMENT') {
    blockers.push({ code: 'G201', text: '尚未完成入住评估', actionLabel: '去评估', actionKey: 'go-assessment' })
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
  return ''
})

function handleAdmissionGuardAction(item: { actionKey?: string }) {
  if (item.actionKey === 'go-contract') {
    router.push('/marketing/contract-signing')
    return
  }
  if (item.actionKey === 'go-assessment') {
    const params = new URLSearchParams()
    params.set('autoOpen', '1')
    params.set('mode', 'new')
    if (form.contractNo) params.set('contractNo', String(form.contractNo))
    if (guardContract.value?.leadId) params.set('leadId', String(guardContract.value.leadId))
    if (guardContract.value?.elderName) params.set('elderName', String(guardContract.value.elderName))
    router.push(`/assessment/ability/admission?${params.toString()}`)
  }
}

function statusText(status?: number) {
  if (status === 1) return '在院'
  if (status === 2) return '请假'
  if (status === 3) return '离院'
  return '-'
}

async function loadElders() {
  const res: PageResult<ElderItem> = await getElderPage({ pageNo: 1, pageSize: 200 })
  elders.value = res.list
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

async function validateAdmissionGuard() {
  const contractNo = String(form.contractNo || '').trim()
  if (!contractNo) {
    message.warning('请先填写合同号，再办理入住')
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
      const hasOption = elders.value.some((item) => String(item.id) === String(contract.elderId))
      if (!hasOption) {
        elders.value.unshift({
          id: contract.elderId as any,
          fullName: contract.elderName || `长者${String(contract.elderId)}`
        } as ElderItem)
      }
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
      const hasOption = elders.value.some((item) => String(item.id) === String(guardContract.value?.elderId))
      if (!hasOption) {
        elders.value.unshift({
          id: guardContract.value.elderId as any,
          fullName: guardContract.value.elderName || `长者${String(guardContract.value.elderId)}`
        } as ElderItem)
      }
      if (!recordQuery.keyword && guardContract.value.elderName) {
        recordQuery.keyword = guardContract.value.elderName
      }
    }
  } catch {
    guardContract.value = null
  }
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
    const matched = elders.value.find((item) => String(item.id) === residentIdText)
    if (matched) {
      form.elderId = matched.id
      recordQuery.keyword = matched.fullName
    } else {
      form.elderId = residentIdText as any
      recordQuery.keyword = elderName || residentIdText
      if (elderName) {
        elders.value.unshift({
          id: residentIdText as any,
          fullName: elderName
        } as ElderItem)
      }
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
    loadElders()
    loadAssets()
    fetchAdmissionRecords()
  },
  debounceMs: 700
})

onMounted(async () => {
  await loadElders()
  await loadAssets()
  applyRoutePrefill()
  await fetchAdmissionRecords()
})
</script>

<style scoped>
.search-bar {
  margin-bottom: 12px;
}
</style>
