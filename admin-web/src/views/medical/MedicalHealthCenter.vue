<template>
  <PageContainer title="医护健康服务中心" subTitle="医护照护一体化 + 健康服务统一入口">
    <StatefulBlock :loading="loading" :error="errorText" :empty="false" @retry="loadSummary">
      <a-alert
        v-if="summary.overdueCareTaskCount > 0 || summary.abnormalInspectionCount > 0 || summary.todayMedicationPendingCount > 0"
        type="warning"
        show-icon
        style="margin-bottom: 12px"
        :message="`执行提醒：护理超时 ${summary.overdueCareTaskCount || 0} 条，异常巡检 ${summary.abnormalInspectionCount || 0} 条，用药待执行 ${summary.todayMedicationPendingCount || 0} 条。`"
      />
      <a-row :gutter="16" style="margin-bottom: 16px">
        <a-col :xs="24" :sm="12" :xl="6">
          <a-card :bordered="false" class="card-elevated">
            <a-statistic title="护理待办" :value="summary.pendingCareTaskCount || 0" />
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :xl="6">
          <a-card :bordered="false" class="card-elevated">
            <a-statistic title="护理超时" :value="summary.overdueCareTaskCount || 0" />
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :xl="6">
          <a-card :bordered="false" class="card-elevated">
            <a-statistic title="巡检待闭环" :value="summary.todayInspectionPendingCount || 0" />
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :xl="6">
          <a-card :bordered="false" class="card-elevated">
            <a-statistic title="用药待执行" :value="summary.todayMedicationPendingCount || 0" />
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :xs="24" :xl="12" style="margin-bottom: 16px">
          <a-card title="统一作业流" :bordered="false" class="card-elevated">
            <a-space wrap>
              <a-button type="primary" @click="go('/medical-care/care-task-board')">护理任务看板</a-button>
              <a-button @click="go('/medical-care/inspection')">健康巡检</a-button>
              <a-button @click="go('/medical-care/medication-registration')">用药登记</a-button>
              <a-button @click="go('/medical-care/nursing-log')">护理日志</a-button>
              <a-button @click="go('/medical-care/handovers')">交接班</a-button>
            </a-space>
          </a-card>
        </a-col>
        <a-col :xs="24" :xl="12" style="margin-bottom: 16px">
          <a-card title="评估与联动" :bordered="false" class="card-elevated">
            <a-space wrap>
              <a-button @click="go('/medical-care/assessment/tcm')">中医体质评估</a-button>
              <a-button @click="go('/medical-care/assessment/cvd')">心血管风险评估</a-button>
              <a-button @click="go('/medical-care/integrated-account')">医护账户联动</a-button>
              <a-button @click="go('/medical-care/workbench')">高级工作台</a-button>
            </a-space>
          </a-card>
        </a-col>
      </a-row>

      <a-card title="重点提醒" :bordered="false" class="card-elevated">
        <a-descriptions :column="2" bordered size="small">
          <a-descriptions-item label="护理任务应做/已做">
            {{ summary.careTaskShouldCount || 0 }} / {{ summary.careTaskDoneCount || 0 }}
          </a-descriptions-item>
          <a-descriptions-item label="扫码执行率">
            {{ (summary.scanExecuteRate || 0).toFixed(1) }}%
          </a-descriptions-item>
          <a-descriptions-item label="医嘱应做/已做">
            {{ summary.medicalOrderShouldCount || 0 }} / {{ summary.medicalOrderDoneCount || 0 }}
          </a-descriptions-item>
          <a-descriptions-item label="医嘱查对完成率">
            {{ (summary.orderCheckRate || 0).toFixed(1) }}%
          </a-descriptions-item>
          <a-descriptions-item label="巡检计划/已完成">
            {{ summary.todayInspectionPlanCount || 0 }} / {{ summary.todayInspectionDoneCount || 0 }}
          </a-descriptions-item>
          <a-descriptions-item label="异常巡检">
            {{ summary.abnormalInspectionCount || 0 }}
          </a-descriptions-item>
        </a-descriptions>
      </a-card>
    </StatefulBlock>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
import { getMedicalHealthCenterSummary } from '../../api/medicalCare'
import type { MedicalCareWorkbenchSummary } from '../../types'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const errorText = ref('')

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

function currentFilters() {
  const elderIdRaw = route.query.elderId
  const elderId = elderIdRaw == null ? undefined : Number(elderIdRaw)
  const date = typeof route.query.date === 'string' ? route.query.date : undefined
  const status = typeof route.query.status === 'string' ? route.query.status : undefined
  return {
    elderId: Number.isFinite(elderId) ? elderId : undefined,
    date,
    status
  }
}

function go(path: string, extra?: { elderId?: number; date?: string; status?: string }) {
  router.push({
    path,
    query: {
      ...currentFilters(),
      ...(extra || {})
    }
  })
}

async function loadSummary() {
  loading.value = true
  errorText.value = ''
  try {
    const data = await getMedicalHealthCenterSummary(currentFilters())
    Object.assign(summary, data || {})
  } catch (error: any) {
    errorText.value = error?.message || '加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

onMounted(loadSummary)
watch(() => route.query, loadSummary)
</script>
