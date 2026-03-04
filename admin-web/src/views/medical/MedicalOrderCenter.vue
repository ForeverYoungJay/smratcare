<template>
  <PageContainer title="医嘱管理中心" subTitle="开立 / 审核 / 查对 / 执行 / 停嘱">
    <a-alert
      type="info"
      show-icon
      style="margin-bottom: 12px"
      :message="`今日医嘱应执行 ${summary.medicalOrderShouldCount}，已执行 ${summary.medicalOrderDoneCount}，待执行 ${summary.medicalOrderPendingCount}，异常 ${summary.medicalOrderAbnormalCount}。`"
    />

    <a-row :gutter="16" style="margin-bottom: 12px">
      <a-col :xs="24" :md="6"><a-card :bordered="false" class="card-elevated"><a-statistic title="应执行" :value="summary.medicalOrderShouldCount" /></a-card></a-col>
      <a-col :xs="24" :md="6"><a-card :bordered="false" class="card-elevated"><a-statistic title="已执行" :value="summary.medicalOrderDoneCount" /></a-card></a-col>
      <a-col :xs="24" :md="6"><a-card :bordered="false" class="card-elevated"><a-statistic title="待执行" :value="summary.medicalOrderPendingCount" /></a-card></a-col>
      <a-col :xs="24" :md="6"><a-card :bordered="false" class="card-elevated"><a-statistic title="执行异常" :value="summary.medicalOrderAbnormalCount" /></a-card></a-col>
    </a-row>

    <a-card :bordered="false" class="card-elevated">
      <a-space wrap>
        <a-button type="primary" @click="go('/medical-care/medication-registration?filter=to_execute&assignee=me&date=today')">待执行医嘱</a-button>
        <a-button @click="go('/medical-care/medication-registration?date=today&filter=pending_or_abnormal')">待执行/异常</a-button>
        <a-button @click="go('/medical-care/medication-registration?date=today&view=ward')">医嘱执行单</a-button>
        <a-button @click="go('/medical-care/medication-registration?date=today&filter=check_pending')">医嘱查对</a-button>
        <a-button @click="go('/medical-care/medication-registration?date=today&filter=stopped')">停嘱记录</a-button>
      </a-space>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import { getMedicalHealthCenterSummary } from '../../api/medicalCare'
import type { MedicalCareWorkbenchSummary } from '../../types'

const router = useRouter()
const summary = reactive<MedicalCareWorkbenchSummary>({
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

function go(path: string) {
  router.push(path)
}

async function load() {
  const data = await getMedicalHealthCenterSummary()
  Object.assign(summary, data || {})
}

onMounted(load)
</script>
