<template>
  <PageContainer title="入住评估" subTitle="按 GB/T 42195-2022 执行入院能力与风险评估，评估完成后进入签约与入住办理流程">
    <a-card title="评估入口" class="card-elevated" :bordered="false">
      <a-alert
        type="info"
        show-icon
        message="评估结果实时联动签约、入住办理与护理规则，不再使用演示数据。"
        style="margin-bottom: 12px"
      />
      <StatefulBlock :loading="loading" :error="errorMessage" @retry="loadLatestRecord">
        <a-alert
          v-if="contracts.length === 0"
          type="warning"
          show-icon
          message="当前未匹配到合同信息，建议先在合同签约页执行“移交评估部”。"
          style="margin-bottom: 12px"
        />
        <a-row :gutter="12" style="margin-bottom: 12px">
          <a-col :xs="24" :sm="8">
            <a-card size="small"><a-statistic title="合同数" :value="overview?.totalContractCount || 0" /></a-card>
          </a-col>
          <a-col :xs="24" :sm="8">
            <a-card size="small"><a-statistic title="评估报告数" :value="overview?.totalReportCount || 0" /></a-card>
          </a-col>
          <a-col :xs="24" :sm="8">
            <a-card size="small"><a-statistic title="待评估合同" :value="pendingAssessmentCount" /></a-card>
          </a-col>
        </a-row>
        <a-descriptions :column="1" size="small" bordered style="margin-bottom: 12px">
          <a-descriptions-item label="最近评估日期">{{ latestRecord?.assessmentDate || '-' }}</a-descriptions-item>
          <a-descriptions-item label="最近评估状态">{{ latestRecord?.status || '无记录' }}</a-descriptions-item>
          <a-descriptions-item label="最近评估分值">{{ latestRecord?.score ?? '-' }}</a-descriptions-item>
          <a-descriptions-item label="最近评估等级">{{ latestRecord?.levelCode || '-' }}</a-descriptions-item>
          <a-descriptions-item label="复评状态">
            {{ isReassessOverdue ? '已到复评日期' : latestRecord?.nextAssessmentDate ? `下次复评 ${latestRecord?.nextAssessmentDate}` : '未设置' }}
          </a-descriptions-item>
        </a-descriptions>
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
            <template v-else-if="column.key === 'reportCount'">
              {{ (record.reports || []).length }}
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
          :message="`当前已选合同：${selectedContract.contractNo || '-'}，可执行开始评估、下载报告、入住办理。`"
        />
        <a-alert
          v-else
          type="warning"
          show-icon
          style="margin-bottom: 12px"
          message="请先在上方合同列表选择一个合同，再进行评估或入住办理。"
        />
        <a-typography-paragraph>
          输出：评估等级、建议护理套餐/服务频次、人力配置建议、评估报告下载并归档到长者档案。
        </a-typography-paragraph>
        <a-typography-paragraph>
          联动：评估完成后自动创建“待签约/待入院办理”任务，并同步护理端生成个性化任务规则。
        </a-typography-paragraph>
        <a-space direction="vertical" style="width: 100%">
          <a-button block type="primary" :disabled="!selectedContract" @click="go(primaryActionPath)">{{ primaryActionText }}</a-button>
          <a-button block @click="go(historyPath)">查看历史评估报告</a-button>
          <a-button block :disabled="!selectedLatestRecord" @click="downloadLatestReport">下载最近评估报告</a-button>
          <a-button block :disabled="!selectedContract" @click="go(admissionPath)">进入入住办理</a-button>
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
import StatefulBlock from '../../../components/StatefulBlock.vue'
import { getAssessmentRecordPage } from '../../../api/assessment'
import { getContractAssessmentOverview, getLeadPage } from '../../../api/marketing'
import type { AssessmentRecord, ContractAssessmentContractItem, ContractAssessmentOverview, CrmLeadItem, PageResult } from '../../../types'

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

const contractColumns = [
  { title: '合同编号', dataIndex: 'contractNo', key: 'contractNo', width: 170 },
  { title: '合同状态', dataIndex: 'contractStatus', key: 'contractStatus', width: 120 },
  { title: '流程阶段', dataIndex: 'flowStage', key: 'flowStage', width: 120 },
  { title: '签约时间', dataIndex: 'contractSignedAt', key: 'contractSignedAt', width: 170 },
  { title: '到期日期', dataIndex: 'contractExpiryDate', key: 'contractExpiryDate', width: 120 },
  { title: '报告数', dataIndex: 'reportCount', key: 'reportCount', width: 90 }
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
  residentId.value ? `/assessment/ability/archive?residentId=${residentId.value}` : '/assessment/ability/archive'
)

const isReassessOverdue = computed(() => {
  const nextDate = selectedLatestRecord.value?.nextAssessmentDate
  if (!nextDate) return false
  const today = new Date().toISOString().slice(0, 10)
  return nextDate < today
})

const primaryActionText = computed(() => {
  if (!selectedLatestRecord.value) return '开始入住评估'
  if (selectedLatestRecord.value.status === 'DRAFT') return '继续填写入住评估'
  if (isReassessOverdue.value) return '发起复评'
  return '新建入住评估'
})

const primaryActionPath = computed(() => {
  const mode = selectedLatestRecord.value?.status === 'DRAFT'
    ? 'continue'
    : isReassessOverdue.value
      ? 'reassess'
      : 'new'
  if (!residentId.value) return '/assessment/ability/admission'
  return `/assessment/ability/admission?residentId=${residentId.value}&autoOpen=1&mode=${mode}&contractNo=${selectedContractNo.value || ''}`
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
  const page = await getLeadPage({
    pageNo: 1,
    pageSize: 200,
    status: 2,
    flowStage: 'PENDING_ASSESSMENT'
  })
  const list: CrmLeadItem[] = page?.list || []
  return list
    .filter((item) => item.contractNo)
    .map((item) => ({
      leadId: item.id,
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

function downloadLatestReport() {
  if (!selectedLatestRecord.value) {
    message.warning('暂无可下载评估记录')
    return
  }
  const record = selectedLatestRecord.value
  const elderName = latestRecord.value?.elderName || overview.value?.elderName || '长者'
  const suggestion = 'suggestion' in record ? record.suggestion : latestRecord.value?.suggestion
  const lines = [
    '入住评估报告',
    `老人姓名：${elderName || '-'}`,
    `评估日期：${record.assessmentDate || '-'}`,
    `评估状态：${record.status || '-'}`,
    `评估分值：${record.score ?? '-'}`,
    `评估等级：${record.levelCode || '-'}`,
    `评估结论：${record.resultSummary || '-'}`,
    `护理建议：${suggestion || '-'}`,
    `下次评估日期：${record.nextAssessmentDate || '-'}`
  ]
  const blob = new Blob([lines.join('\n')], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `入住评估报告_${elderName || '长者'}_${record.assessmentDate || new Date().toISOString().slice(0, 10)}.txt`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
  message.success('最近评估报告已下载')
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
