<template>
  <PageContainer title="护理与质量中心" subTitle="用药 / 巡查 / 护理计划 / 执行闭环 / 交接 / 药库 / 质量报表">
    <a-row :gutter="12" style="margin-bottom: 12px">
      <a-col :xs="12" :lg="4"><a-card :bordered="false" class="card-elevated"><a-statistic title="用药待执行" :value="summary.todayMedicationPendingCount || 0" /></a-card></a-col>
      <a-col :xs="12" :lg="4"><a-card :bordered="false" class="card-elevated"><a-statistic title="巡检待闭环" :value="summary.todayInspectionPendingCount || 0" /></a-card></a-col>
      <a-col :xs="12" :lg="4"><a-card :bordered="false" class="card-elevated"><a-statistic title="日志待补录" :value="summary.nursingLogPendingCount || 0" /></a-card></a-col>
      <a-col :xs="12" :lg="4"><a-card :bordered="false" class="card-elevated"><a-statistic title="交接待完成" :value="summary.handoverPendingCount || 0" /></a-card></a-col>
      <a-col :xs="12" :lg="4"><a-card :bordered="false" class="card-elevated"><a-statistic title="整改逾期" :value="summary.rectifyOverdueCount || 0" /></a-card></a-col>
      <a-col :xs="12" :lg="4"><a-card :bordered="false" class="card-elevated"><a-statistic title="异常事件未闭环" :value="summary.unclosedAbnormalCount || 0" /></a-card></a-col>
    </a-row>

    <a-tabs v-model:activeKey="activeTab" type="card">
      <a-tab-pane key="medication" tab="用药管理">
        <a-space wrap>
          <a-button type="primary" @click="go('/health/medication/medication-setting')">用药设置</a-button>
          <a-button @click="go('/medical-care/medication-registration')">用药登记</a-button>
          <a-button @click="go('/health/medication/medication-remaining')">剩余用药</a-button>
          <a-button @click="go('/health/medication/drug-deposit')">带药外出/缴存</a-button>
          <a-button @click="go('/health/medication/drug-dictionary')">过敏/禁忌/重复提醒规则</a-button>
        </a-space>
      </a-tab-pane>
      <a-tab-pane key="vital" tab="健康数据与巡查">
        <a-space wrap>
          <a-button type="primary" @click="go('/health/management/data?date=today')">生命体征采集</a-button>
          <a-button @click="go('/medical-care/inspection?date=today')">健康巡检</a-button>
          <a-button @click="go('/medical-care/nursing-log?filter=pending')">护理日志</a-button>
          <a-button @click="go('/medical-care/inspection?filter=abnormal&date=today')">异常预警清单</a-button>
        </a-space>
      </a-tab-pane>
      <a-tab-pane key="plan" tab="护理计划与等级">
        <a-space wrap>
          <a-button type="primary" @click="go('/care/service/care-levels')">护理等级规则</a-button>
          <a-button @click="go('/care/service/service-items')">护理项目库</a-button>
          <a-button @click="go('/care/service/service-plans')">服务计划（周/月）</a-button>
          <a-button @click="go('/care/care-packages')">护理套餐库</a-button>
        </a-space>
      </a-tab-pane>
      <a-tab-pane key="task" tab="护理任务与扫码执行">
        <a-space wrap>
          <a-button type="primary" @click="go('/medical-care/care-task-board?date=today&filter=all')">今日任务清单</a-button>
          <a-button @click="go('/care/workbench/qr?mode=scan')">扫码执行</a-button>
          <a-button @click="go('/medical-care/care-task-board?date=today&filter=overdue')">超时与补录</a-button>
          <a-button @click="go('/medical-care/care-task-board?date=today&filter=overdue_or_missed')">任务异常与事故入口</a-button>
        </a-space>
      </a-tab-pane>
      <a-tab-pane key="handover" tab="交接班与班组">
        <a-space wrap>
          <a-button type="primary" @click="go('/medical-care/handovers?shift=当前班次')">交接清单</a-button>
          <a-button @click="go('/medical-care/handovers?tab=records')">交接记录</a-button>
          <a-button @click="go('/care/staff/caregiver-groups')">护士/护工分组</a-button>
          <a-button @click="go('/care/scheduling/shift-calendar')">排班查看与调整申请</a-button>
        </a-space>
      </a-tab-pane>
      <a-tab-pane key="pharmacy" tab="药库与药品主数据">
        <a-space wrap>
          <a-button type="primary" @click="go('/health/medication/drug-dictionary')">药品字典</a-button>
          <a-button @click="go('/logistics/storage/warehouse')">药库库存</a-button>
          <a-button @click="go('/logistics/storage/inbound')">入库</a-button>
          <a-button @click="go('/logistics/storage/outbound')">出库</a-button>
          <a-button @click="go('/logistics/storage/supplier')">供应商</a-button>
          <a-button @click="go('/logistics/storage/purchase?filter=pending')">领药申请/发药记录</a-button>
        </a-space>
      </a-tab-pane>
      <a-tab-pane key="quality" tab="质量与报表">
        <a-space wrap>
          <a-button type="primary" @click="go('/care/service/nursing-reports')">护理报表</a-button>
          <a-button @click="go('/stats/org/monthly-operation')">医嘱执行率/巡查覆盖率</a-button>
          <a-button @click="go('/survey/stats?filter=low_score_or_complaint')">投诉/问卷关联分析</a-button>
          <a-button @click="go('/oa/work-execution/task?filter=overdue_rectify')">整改任务闭环</a-button>
          <a-button @click="go('/hr/performance')">绩效数据输出</a-button>
        </a-space>
      </a-tab-pane>
    </a-tabs>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { getMedicalHealthCenterSummary } from '../../api/medicalCare'
import type { MedicalCareWorkbenchSummary } from '../../types'
import { resolveMedicalError } from './medicalError'

const router = useRouter()
const route = useRoute()
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
const activeTab = computed({
  get: () => (typeof route.query.tab === 'string' ? route.query.tab : 'medication'),
  set: (tab: string) => {
    router.replace({ query: { ...route.query, tab } })
  }
})

function go(path: string) {
  const [pathname, search] = path.split('?')
  const parsed = new URLSearchParams(search || '')
  const query: Record<string, any> = {}
  parsed.forEach((value, key) => {
    query[key] = value
  })
  const residentId = route.query.residentId ?? route.query.elderId
  if (residentId != null && query.residentId == null && query.elderId == null) {
    query.residentId = residentId
    query.elderId = residentId
  }
  router.push({
    path: pathname,
    query
  })
}

async function loadSummary() {
  try {
    const data = await getMedicalHealthCenterSummary({
      elderId: route.query.elderId ? Number(route.query.elderId) : route.query.residentId ? Number(route.query.residentId) : undefined
    })
    Object.assign(summary, data || {})
  } catch (error) {
    message.error(resolveMedicalError(error, '加载护理与质量中心失败'))
  }
}

onMounted(loadSummary)
watch(() => route.query, loadSummary)
</script>
