<template>
  <PageContainer title="入住评估" subTitle="按 GB/T 42195-2022 执行入院能力与风险评估，评估完成后进入签约与入住办理流程">
    <a-card title="评估入口" class="card-elevated" :bordered="false">
      <StatefulBlock :loading="loading" :error="errorMessage" @retry="loadLatestRecord">
        <a-alert
          v-if="contracts.length === 0"
          type="warning"
          show-icon
          message="当前未匹配到待评估合同，请先在合同签约中新增合同，系统会自动进入待评估列表。"
          style="margin-bottom: 12px"
        />
        <a-table
          :data-source="contracts"
          :columns="contractColumns"
          :pagination="false"
          row-key="contractNo"
          size="small"
          :row-class-name="contractRowClassName"
          :custom-row="contractCustomRow"
          style="margin-bottom: 12px"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'flowStage'">
              <a-tag :color="flowStageColor(record.flowStage)">{{ flowStageText(record.flowStage) }}</a-tag>
            </template>
          </template>
          <template #expandedRowRender="{ record }">
            <a-table
              :data-source="record.reports || []"
              :columns="reportColumns"
              :pagination="false"
              row-key="recordId"
              size="small"
              :locale="{ emptyText: '该合同暂未产生入住评估报告' }"
            />
          </template>
        </a-table>
        <a-alert
          v-if="(overview?.unassignedReports || []).length"
          type="warning"
          show-icon
          style="margin-bottom: 12px"
          :message="`有 ${(overview?.unassignedReports || []).length} 条评估报告尚未匹配到具体合同，已纳入长者总评估记录。`"
        />
        <a-alert
          v-if="selectedContract"
          type="success"
          show-icon
          style="margin-bottom: 12px"
          :message="`当前已选合同：${selectedContract.contractNo || '-'}，可执行一键闭环评估并自动回流入住办理。`"
        />
        <a-alert
          v-else
          type="warning"
          show-icon
          style="margin-bottom: 12px"
          message="请先在上方合同列表选择一个合同，再进行评估或入住办理。"
        />
        <LifecycleStageBar
          title="评估-入住联动阶段"
          :subject="flowSubject"
          :stage="admissionLifecycleStage"
          :hint="admissionLifecycleHint"
          style="margin-bottom: 12px"
        />
        <a-space direction="vertical" style="width: 100%">
          <a-button block type="primary" :disabled="!selectedContract" @click="go(primaryActionPath)">{{ primaryActionText }}</a-button>
          <a-button block @click="go(historyPath)">查看历史评估报告</a-button>
          <a-button block :disabled="!selectedContract" @click="go(admissionPath)">回流入住办理（闭环）</a-button>
        </a-space>
      </StatefulBlock>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import PageContainer from '../../../components/PageContainer.vue'
import LifecycleStageBar from '../../../components/LifecycleStageBar.vue'
import StatefulBlock from '../../../components/StatefulBlock.vue'
import { getAssessmentRecordPage } from '../../../api/assessment'
import { getContractAssessmentOverview, getContractPage } from '../../../api/marketing'
import type { AssessmentRecord, ContractAssessmentContractItem, ContractAssessmentOverview, CrmContractItem, PageResult } from '../../../types'

const router = useRouter()
const route = useRoute()
const residentId = computed(() => Number(route.query.residentId || 0))
const leadId = computed(() => Number(route.query.leadId || 0))
const latestRecord = ref<AssessmentRecord | null>(null)
const overview = ref<ContractAssessmentOverview | null>(null)
const loading = ref(false)
const errorMessage = ref('')
const fallbackContracts = ref<ContractAssessmentContractItem[]>([])
const contracts = computed<ContractAssessmentContractItem[]>(() =>
  (overview.value?.contracts && overview.value.contracts.length ? overview.value.contracts : fallbackContracts.value) || []
)
const selectedContractNo = ref('')
const pendingAssessmentCount = computed(() =>
  contracts.value.filter((item) => item.flowStage === 'PENDING_ASSESSMENT').length
)
const selectedContract = computed(() =>
  contracts.value.find((item) => item.contractNo === selectedContractNo.value)
)
const selectedLatestRecord = computed(() => {
  const contractReports = selectedContract.value?.reports || []
  const draft = contractReports.find((item) => item.status === 'DRAFT')
  return draft || contractReports[0] || latestRecord.value || null
})
const flowSubject = computed(() => {
  const contract = selectedContract.value
  if (!contract) return '请先在上方表格选择合同'
  return `合同 ${contract.contractNo || '-'} / 长者 ${contract.elderName || '-'}`
})
const admissionLifecycleStage = computed(() => {
  const contract = selectedContract.value
  if (!contract) return 'PENDING_ASSESSMENT'
  return contract.flowStage || 'PENDING_ASSESSMENT'
})
const admissionLifecycleHint = computed(() => {
  const contract = selectedContract.value
  if (!contract) return ''
  if (contract.flowStage === 'PENDING_BED_SELECT') return '评估已完成，可直接回流入住办理并选择床位'
  if (contract.flowStage === 'SIGNED') return '流程已完成闭环'
  return '按提示完成当前节点后，系统将自动推进到下一阶段'
})

const contractColumns = [
  { title: '合同编号', dataIndex: 'contractNo', key: 'contractNo', width: 170 },
  { title: '老人姓名', dataIndex: 'elderName', key: 'elderName', width: 140 },
  { title: '合同状态', dataIndex: 'contractStatus', key: 'contractStatus', width: 120 },
  { title: '流程阶段', dataIndex: 'flowStage', key: 'flowStage', width: 120 },
  { title: '签约时间', dataIndex: 'contractSignedAt', key: 'contractSignedAt', width: 170 }
]

const reportColumns = [
  { title: '评估日期', dataIndex: 'assessmentDate', key: 'assessmentDate', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '分值', dataIndex: 'score', key: 'score', width: 90 },
  { title: '等级', dataIndex: 'levelCode', key: 'levelCode', width: 90 },
  { title: '结论', dataIndex: 'resultSummary', key: 'resultSummary' }
]

const admissionPath = computed(() =>
  residentId.value
    ? `/elder/admission-processing?residentId=${residentId.value}&contractNo=${selectedContractNo.value || ''}`
    : `/elder/admission-processing?contractNo=${selectedContractNo.value || ''}`
)

const historyPath = computed(() =>
  residentId.value ? `/elder/assessment/ability/archive?residentId=${residentId.value}` : '/elder/assessment/ability/archive'
)

const isReassessOverdue = computed(() => {
  const nextDate = selectedLatestRecord.value?.nextAssessmentDate
  if (!nextDate) return false
  const today = new Date().toISOString().slice(0, 10)
  return nextDate < today
})

const primaryActionText = computed(() => {
  if (!selectedLatestRecord.value) return '一键闭环：开始入住评估'
  if (selectedLatestRecord.value.status === 'DRAFT') return '一键闭环：继续填写入住评估'
  if (isReassessOverdue.value) return '一键闭环：发起复评'
  return '一键闭环：新建入住评估'
})

const primaryActionPath = computed(() => {
  const mode = selectedLatestRecord.value?.status === 'DRAFT'
    ? 'continue'
    : isReassessOverdue.value
      ? 'reassess'
      : 'new'
  const contract = selectedContract.value
  const selectedElderId = contract?.elderId || overview.value?.elderId || residentId.value || undefined
  const params = new URLSearchParams()
  params.set('autoOpen', '1')
  params.set('closeLoop', '1')
  params.set('mode', mode)
  if (residentId.value) params.set('residentId', String(residentId.value))
  if (selectedElderId != null && String(selectedElderId) !== '0') {
    params.set('elderId', String(selectedElderId))
  }
  if (selectedContractNo.value) params.set('contractNo', selectedContractNo.value)
  if (contract?.contractId != null) params.set('contractId', String(contract.contractId))
  if (contract?.leadId != null) params.set('leadId', String(contract.leadId))
  if (contract?.elderName) params.set('elderName', contract.elderName)
  if (contract?.elderPhone) params.set('elderPhone', contract.elderPhone)
  if (contract?.idCardNo) params.set('idCardNo', contract.idCardNo)
  if (contract?.homeAddress) params.set('homeAddress', contract.homeAddress)
  return `/elder/assessment/ability/admission?${params.toString()}`
})

function go(path: string) {
  router.push(path)
}

async function loadLatestRecord() {
  errorMessage.value = ''
  loading.value = true
  try {
    await Promise.all([loadContractOverview(), loadAssessmentRecord()])
  } catch (error: any) {
    errorMessage.value = error?.message || '加载入住评估失败'
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

async function loadContractOverview() {
  fallbackContracts.value = []
  if (residentId.value || leadId.value) {
    overview.value = await getContractAssessmentOverview({
      elderId: residentId.value || undefined,
      leadId: leadId.value || undefined
    })
  } else {
    overview.value = null
    fallbackContracts.value = await loadPendingContracts()
  }
  const allContracts = contracts.value
  const contractFromQuery = String(route.query.contractNo || '')
  if (contractFromQuery && allContracts.some((item) => item.contractNo === contractFromQuery)) {
    selectedContractNo.value = contractFromQuery
    return
  }
  const pending = allContracts.find((item) => item.flowStage === 'PENDING_ASSESSMENT')
  selectedContractNo.value = pending?.contractNo || allContracts[0]?.contractNo || ''
}

async function loadAssessmentRecord() {
  if (!residentId.value) {
    latestRecord.value = null
    return
  }
  const res: PageResult<AssessmentRecord> = await getAssessmentRecordPage({
    pageNo: 1,
    pageSize: 10,
    assessmentType: 'ADMISSION',
    elderId: residentId.value
  })
  const list = res.list || []
  const draft = list.find((item) => item.status === 'DRAFT')
  latestRecord.value = draft || list[0] || null
}

async function loadPendingContracts() {
  const page: PageResult<CrmContractItem> = await getContractPage({
    pageNo: 1,
    pageSize: 200,
    flowStage: 'PENDING_ASSESSMENT',
    currentOwnerDept: 'ASSESSMENT'
  })
  const list: CrmContractItem[] = page?.list || []
  return list
    .filter((item) => item.contractNo)
    .map((item) => ({
      contractId: item.id,
      leadId: item.leadId,
      elderId: item.elderId,
      elderName: item.elderName,
      elderPhone: item.elderPhone,
      idCardNo: item.idCardNo,
      homeAddress: item.homeAddress,
      contractNo: item.contractNo,
      contractStatus: item.contractStatus,
      flowStage: item.flowStage,
      currentOwnerDept: item.currentOwnerDept,
      marketerName: item.marketerName,
      orgName: item.orgName,
      contractSignedAt: item.contractSignedAt,
      contractExpiryDate: item.contractExpiryDate,
      reports: []
    } as ContractAssessmentContractItem))
}

function flowStageText(stage?: string) {
  if (stage === 'PENDING_ASSESSMENT') return '待评估'
  if (stage === 'PENDING_BED_SELECT') return '待办理入住'
  if (stage === 'PENDING_SIGN') return '待签署'
  if (stage === 'SIGNED') return '已签署'
  return stage || '-'
}

function flowStageColor(stage?: string) {
  if (stage === 'PENDING_ASSESSMENT') return 'gold'
  if (stage === 'PENDING_BED_SELECT') return 'blue'
  if (stage === 'PENDING_SIGN') return 'purple'
  if (stage === 'SIGNED') return 'green'
  return 'default'
}

function contractCustomRow(record: ContractAssessmentContractItem) {
  return {
    onClick: () => {
      selectedContractNo.value = record.contractNo || ''
    }
  }
}

function contractRowClassName(record: ContractAssessmentContractItem) {
  return record.contractNo && record.contractNo === selectedContractNo.value ? 'selected-contract-row' : ''
}

onMounted(() => {
  loadLatestRecord()
})

watch(
  () => `${residentId.value}-${leadId.value}`,
  () => {
    loadLatestRecord()
  }
)
</script>

<style scoped>
:deep(.selected-contract-row > td) {
  background: #e6f7ff !important;
}
</style>
