<template>
  <PageContainer title="健康服务与医护账户一体化" subTitle="医护执行 + 老人账户联动总览">
    <a-row :gutter="16" style="margin-bottom: 16px">
      <a-col :span="6"><a-card><a-statistic title="今日待执行医嘱" :value="medicalSummary.pendingMedicalOrderCount" /></a-card></a-col>
      <a-col :span="6"><a-card><a-statistic title="护理超时任务" :value="medicalSummary.overdueCareTaskCount" /></a-card></a-col>
      <a-col :span="6"><a-card><a-statistic title="余额预警人数" :value="warningCount" /></a-card></a-col>
      <a-col :span="6"><a-card><a-statistic title="今日巡查待完成" :value="medicalSummary.todayInspectionPendingCount" /></a-card></a-col>
    </a-row>

    <a-card title="账户预警与医护协同" style="margin-bottom: 16px">
      <template #extra>
        <a-space>
          <a-button @click="go('/finance/account')">老人账户</a-button>
          <a-button @click="go('/health/medication/medication-registration?date=today')">用药登记</a-button>
          <a-button type="primary" @click="go('/care/workbench/task-board?date=today')">护理任务看板</a-button>
        </a-space>
      </template>
      <a-table :columns="warningColumns" :data-source="warningRows" :pagination="false" row-key="id" size="small">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'balance'">
            <a-tag :color="Number(record.balance) <= Number(record.warnThreshold) ? 'red' : 'green'">{{ record.balance }}</a-tag>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-card title="最近账户流水（用于医护收费核对）">
      <a-table :columns="logColumns" :data-source="logRows" :pagination="false" row-key="id" size="small" />
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import { getElderAccountLogPage, getElderAccountWarnings } from '../../api/finance'
import { getMedicalCareWorkbenchSummary } from '../../api/medicalCare'
import type { ElderAccount, ElderAccountLog, MedicalCareWorkbenchSummary, PageResult } from '../../types'

const router = useRouter()

const medicalSummary = ref<MedicalCareWorkbenchSummary>({
  pendingMedicalOrderCount: 0,
  pendingReviewCount: 0,
  pendingAuditCount: 0,
  unclosedAbnormalCount: 0,
  todayInspectionTodoCount: 0,
  topRiskResidentCount: 0,
  abnormalVital24hCount: 0,
  abnormalEvent24hCount: 0,
  medicalOrderShouldCount: 0,
  medicalOrderDoneCount: 0,
  medicalOrderPendingCount: 0,
  medicalOrderAbnormalCount: 0,
  orderCheckRate: 0,
  medicationShouldCount: 0,
  medicationDoneCount: 0,
  medicationUndoneCount: 0,
  medicationLowStockCount: 0,
  medicationRequestPendingCount: 0,
  careTaskShouldCount: 0,
  careTaskDoneCount: 0,
  careTaskOverdueCount: 0,
  scanExecuteRate: 0,
  todayInspectionPlanCount: 0,
  nursingLogPendingCount: 0,
  handoverPendingCount: 0,
  handoverDoneCount: 0,
  handoverRiskCount: 0,
  handoverTodoCount: 0,
  incidentOpenCount: 0,
  incident30dCount: 0,
  incident30dRate: 0,
  lowScoreSurveyCount: 0,
  rectifyInProgressCount: 0,
  rectifyOverdueCount: 0,
  aiReportGeneratedCount: 0,
  aiReportPublishedCount: 0,
  pendingCareTaskCount: 0,
  overdueCareTaskCount: 0,
  todayInspectionPendingCount: 0,
  todayInspectionDoneCount: 0,
  abnormalInspectionCount: 0,
  todayMedicationPendingCount: 0,
  todayMedicationDoneCount: 0,
  tcmPublishedCount: 0,
  cvdHighRiskCount: 0,
  cvdNeedFollowupCount: 0,
  keyResidents: []
})

const warningRows = ref<ElderAccount[]>([])
const logRows = ref<ElderAccountLog[]>([])

const warningCount = computed(() => warningRows.value.length)

const warningColumns = [
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 140 },
  { title: '余额', dataIndex: 'balance', key: 'balance', width: 100 },
  { title: '预警阈值', dataIndex: 'warnThreshold', key: 'warnThreshold', width: 120 },
  { title: '备注', dataIndex: 'remark', key: 'remark' }
]

const logColumns = [
  { title: '时间', dataIndex: 'createTime', key: 'createTime', width: 170 },
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 140 },
  { title: '方向', dataIndex: 'direction', key: 'direction', width: 90 },
  { title: '金额', dataIndex: 'amount', key: 'amount', width: 100 },
  { title: '余额后', dataIndex: 'balanceAfter', key: 'balanceAfter', width: 110 },
  { title: '来源', dataIndex: 'sourceType', key: 'sourceType', width: 120 },
  { title: '备注', dataIndex: 'remark', key: 'remark' }
]

function go(path: string) {
  router.push(path)
}

async function load() {
  const [medicalData, warningData, logData] = await Promise.all([
    getMedicalCareWorkbenchSummary(),
    getElderAccountWarnings(),
    getElderAccountLogPage({ pageNo: 1, pageSize: 10 })
  ])
  medicalSummary.value = medicalData
  warningRows.value = warningData || []
  const page = logData as PageResult<ElderAccountLog>
  logRows.value = page?.list || []
}

onMounted(load)
</script>
