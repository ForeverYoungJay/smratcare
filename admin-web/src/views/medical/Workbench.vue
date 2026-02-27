<template>
  <PageContainer title="医护照护工作台" subTitle="Medical & Care 一体化管理">
    <a-row :gutter="16">
      <a-col :xs="24" :md="12" :xl="8" v-for="card in cards" :key="card.key" style="margin-bottom: 16px">
        <a-card :title="card.title" :bordered="false" class="card-elevated card-click" @click="go(card.route)">
          <template #extra>
            <a-tag color="blue">{{ card.badge }}</a-tag>
          </template>
          <div v-for="line in card.lines" :key="line" class="line">{{ line }}</div>
          <a-space wrap style="margin-top: 12px">
            <a-button v-for="action in card.actions" :key="action.label" size="small" @click.stop="go(action.route)">
              {{ action.label }}
            </a-button>
          </a-space>
        </a-card>
      </a-col>
    </a-row>

    <a-card title="卡片B：今日长者重点关注 Top" :bordered="false" class="card-elevated">
      <a-empty v-if="!summary.keyResidents?.length" description="暂无高风险长者" />
      <a-list v-else :data-source="summary.keyResidents">
        <template #renderItem="{ item }">
          <a-list-item>
            <a-list-item-meta
              :title="`${item.elderName || '-'}（${riskLabel(item.riskLevel)}）`"
              :description="`${item.keyRiskFactors || '暂无因子说明'} · ${item.assessmentDate || '-'}`"
            />
            <template #actions>
              <a-button type="link" @click="go(`/elder/resident-360?residentId=${item.elderId}&from=medicalCare`)">Resident360</a-button>
            </template>
          </a-list-item>
        </template>
      </a-list>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import { getMedicalCareWorkbenchSummary } from '../../api/medicalCare'
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

const cards = computed(() => [
  {
    key: 'A',
    title: '卡片A：我的待办',
    badge: `${summary.pendingCareTaskCount}`,
    route: '/care/workbench/task-board?date=today',
    lines: [
      `待执行医嘱 ${summary.pendingMedicalOrderCount} · 待查对 ${summary.pendingReviewCount} · 待审核 ${summary.pendingAuditCount}`,
      `超时任务 ${summary.overdueCareTaskCount} · 未闭环异常 ${summary.unclosedAbnormalCount}`,
      `今日巡查待完成 ${summary.todayInspectionTodoCount}`
    ],
    actions: [
      { label: '待执行医嘱', route: '/health/medication/medication-registration?filter=to_execute&date=today' },
      { label: '超时任务', route: '/care/workbench/task-board?date=today&filter=overdue' },
      { label: '巡查待完成', route: '/health/inspection?filter=pending&date=today' },
      { label: '账户联动', route: '/medical-care/integrated-account' }
    ]
  },
  {
    key: 'C',
    title: '卡片C：医嘱执行概览',
    badge: `${summary.medicalOrderPendingCount}`,
    route: '/health/medication/medication-registration?date=today',
    lines: [
      `今日医嘱 应执行 ${summary.medicalOrderShouldCount} / 已执行 ${summary.medicalOrderDoneCount}`,
      `待执行 ${summary.medicalOrderPendingCount} · 异常 ${summary.medicalOrderAbnormalCount}`,
      `查对完成率 ${summary.orderCheckRate.toFixed(1)}%`
    ],
    actions: [
      { label: '待执行/异常', route: '/health/medication/medication-registration?date=today&filter=pending_or_abnormal' },
      { label: '医嘱执行单', route: '/health/medication/medication-registration?date=today&view=ward' }
    ]
  },
  {
    key: 'D',
    title: '卡片D：用药与药品预警',
    badge: `${summary.medicationUndoneCount}`,
    route: '/health/medication/medication-registration?date=today',
    lines: [
      `今日应服 ${summary.medicationShouldCount} · 已服 ${summary.medicationDoneCount} · 未服 ${summary.medicationUndoneCount}`,
      `药库预警 缺药/临期 ${summary.medicationLowStockCount}`,
      `领药申请待处理 ${summary.medicationRequestPendingCount}`
    ],
    actions: [
      { label: '用药登记', route: '/health/medication/medication-registration?date=today&filter=pending' },
      { label: '库存预警', route: '/material/alerts?filter=low_or_expiring' },
      { label: '领药申请', route: '/material/purchase?filter=pending' }
    ]
  },
  {
    key: 'E',
    title: '卡片E：护理任务执行',
    badge: `${summary.careTaskOverdueCount}`,
    route: '/care/workbench/task-board?date=today',
    lines: [
      `今日护理任务 应做 ${summary.careTaskShouldCount} · 已做 ${summary.careTaskDoneCount} · 超时 ${summary.careTaskOverdueCount}`,
      `扫码执行率 ${summary.scanExecuteRate.toFixed(1)}%`,
      `异常任务与漏做会同步质量评分`
    ],
    actions: [
      { label: '任务总览', route: '/care/workbench/task-board?date=today&filter=all' },
      { label: '扫码执行', route: '/care/workbench/qr?mode=scan' },
      { label: '异常任务', route: '/care/workbench/task-board?date=today&filter=overdue_or_missed' }
    ]
  },
  {
    key: 'F',
    title: '卡片F：巡查与生命体征',
    badge: `${summary.abnormalInspectionCount}`,
    route: '/health/inspection?date=today',
    lines: [
      `巡查计划 应巡查 ${summary.todayInspectionPlanCount} · 已巡查 ${summary.todayInspectionDoneCount} · 未巡查 ${summary.todayInspectionPendingCount}`,
      `生命体征异常 ${summary.abnormalVital24hCount}`,
      `护理日志待补录 ${summary.nursingLogPendingCount}`
    ],
    actions: [
      { label: '健康巡查', route: '/health/inspection?date=today' },
      { label: '健康数据', route: '/health/management/data?date=today' },
      { label: '护理日志', route: '/health/nursing-log?filter=pending' }
    ]
  },
  {
    key: 'G',
    title: '卡片G：交接班',
    badge: `${summary.handoverPendingCount}`,
    route: '/care/scheduling/handovers?shift=today',
    lines: [
      `当前班次 待交接 ${summary.handoverPendingCount} · 已交接 ${summary.handoverDoneCount}`,
      `重点事项 风险 ${summary.handoverRiskCount} · 未完成事项 ${summary.handoverTodoCount}`,
      `未完成交接将计入待办`
    ],
    actions: [
      { label: '交接班', route: '/care/scheduling/handovers?shift=today' },
      { label: '交接记录', route: '/care/scheduling/handovers?tab=records' }
    ]
  },
  {
    key: 'H',
    title: '卡片H：质量与安全',
    badge: `${summary.incidentOpenCount}`,
    route: '/life/incident?filter=open_cases',
    lines: [
      `事故事件 未结案 ${summary.incidentOpenCount} · 近30天发生 ${summary.incident30dCount}（${summary.incident30dRate.toFixed(1)}%）`,
      `投诉/问卷低分待整改 ${summary.lowScoreSurveyCount}`,
      `整改任务 进行中 ${summary.rectifyInProgressCount} · 逾期 ${summary.rectifyOverdueCount}`
    ],
    actions: [
      { label: '安全事件', route: '/life/incident?filter=open_cases&period=30d' },
      { label: '问卷低分', route: '/survey/stats?filter=low_score_or_complaint' },
      { label: '整改任务', route: '/oa/work-execution/task?filter=overdue_rectify' }
    ]
  },
  {
    key: 'I',
    title: '卡片I：报表速览',
    badge: `${summary.aiReportGeneratedCount}`,
    route: '/stats/org/monthly-operation',
    lines: [
      `AI健康评估报告 生成记录 ${summary.aiReportGeneratedCount} · 已发布 ${summary.aiReportPublishedCount}`,
      `高风险心血管提示 ${summary.cvdHighRiskCount} · 需随访 ${summary.cvdNeedFollowupCount}`,
      `支持一键生成巡检/随访/护理干预任务`
    ],
    actions: [
      { label: '生成巡检任务', route: '/health/inspection?filter=generated_from_ai' },
      { label: '医护待办', route: '/care/workbench/task-board?filter=generated_from_ai' },
      { label: '护理任务', route: '/care/workbench/task-board?filter=generated_from_ai' }
    ]
  }
])

function go(path: string) {
  router.push(path)
}

function riskLabel(value?: string) {
  if (value === 'VERY_HIGH') return '极高风险'
  if (value === 'HIGH') return '高风险'
  if (value === 'MEDIUM') return '中风险'
  if (value === 'LOW') return '低风险'
  return value || '未分层'
}

async function load() {
  const data = await getMedicalCareWorkbenchSummary()
  Object.assign(summary, data)
}

onMounted(load)
</script>

<style scoped>
.card-click {
  cursor: pointer;
}
.line {
  margin-bottom: 6px;
  color: #334155;
  font-size: 13px;
}
</style>
