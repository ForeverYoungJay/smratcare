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
          <a-card title="卡片A：我的待办" :bordered="false" class="card-elevated">
            <a-space wrap>
              <a-button @click="go('/medical-care/orders', { status: 'PENDING' })">
                待执行医嘱 {{ summary.medicalOrderPendingCount || 0 }}
              </a-button>
              <a-button @click="go('/medical-care/orders', { status: 'ABNORMAL' })">
                医嘱异常 {{ summary.medicalOrderAbnormalCount || 0 }}
              </a-button>
              <a-button @click="go('/medical-care/care-task-board', { status: 'OVERDUE' })">
                超时任务 {{ summary.overdueCareTaskCount || 0 }}
              </a-button>
              <a-button @click="go('/medical-care/inspection', { status: 'FOLLOWING' })">
                巡检待完成 {{ summary.todayInspectionPendingCount || 0 }}
              </a-button>
            </a-space>
          </a-card>
        </a-col>
        <a-col :xs="24" :xl="12" style="margin-bottom: 16px">
          <a-card title="卡片B：今日重点关注" :bordered="false" class="card-elevated">
            <a-space wrap>
              <a-button @click="go('/medical-care/residents', { status: 'HIGH_RISK' })">
                高风险长者 {{ summary.topRiskResidentCount || 0 }}
              </a-button>
              <a-button @click="go('/health/management/data', { status: 'ABNORMAL' })">
                异常体征 {{ summary.abnormalVital24hCount || 0 }}
              </a-button>
              <a-button @click="go('/medical-care/nursing-log', { status: 'PENDING' })">
                日志待补录 {{ summary.nursingLogPendingCount || 0 }}
              </a-button>
              <a-button @click="go('/medical-care/handovers', { status: 'DRAFT' })">
                交接待确认 {{ summary.handoverPendingCount || 0 }}
              </a-button>
            </a-space>
            <a-list
              v-if="summary.keyResidents?.length"
              size="small"
              bordered
              style="margin-top: 12px"
              :data-source="summary.keyResidents.slice(0, 5)"
            >
              <template #renderItem="{ item }">
                <a-list-item>
                  <a @click="go('/medical-care/residents', { elderId: item.elderId })">
                    {{ item.elderName || '-' }}
                  </a>
                  <span>{{ item.riskLevel || '-' }}</span>
                </a-list-item>
              </template>
            </a-list>
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :xs="24" :xl="12" style="margin-bottom: 16px">
          <a-card title="卡片C：医嘱执行概览" :bordered="false" class="card-elevated">
            <a-space wrap>
              <a-button @click="go('/medical-care/orders')">
                应执行/已执行 {{ summary.medicalOrderShouldCount || 0 }}/{{ summary.medicalOrderDoneCount || 0 }}
              </a-button>
              <a-button @click="go('/medical-care/orders', { status: 'PENDING' })">
                待执行 {{ summary.medicalOrderPendingCount || 0 }}
              </a-button>
              <a-button danger @click="go('/medical-care/orders', { status: 'ABNORMAL' })">
                异常 {{ summary.medicalOrderAbnormalCount || 0 }}
              </a-button>
              <a-tag color="blue">查对完成率 {{ (summary.orderCheckRate || 0).toFixed(1) }}%</a-tag>
            </a-space>
          </a-card>
        </a-col>
        <a-col :xs="24" :xl="12" style="margin-bottom: 16px">
          <a-card title="卡片D：用药与药品预警" :bordered="false" class="card-elevated">
            <a-space wrap>
              <a-button @click="go('/medical-care/medication-registration')">
                今日应服/已服 {{ summary.medicationShouldCount || 0 }}/{{ summary.medicationDoneCount || 0 }}
              </a-button>
              <a-button @click="go('/medical-care/medication-registration', { status: 'PENDING' })">
                未服 {{ summary.medicationUndoneCount || 0 }}
              </a-button>
              <a-button danger @click="go('/health/medication/medication-remaining')">
                缺药预警 {{ summary.medicationLowStockCount || 0 }}
              </a-button>
              <a-button @click="go('/medical-care/nursing-quality?tab=pharmacy')">
                领药待处理 {{ summary.medicationRequestPendingCount || 0 }}
              </a-button>
            </a-space>
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :xs="24" :xl="12" style="margin-bottom: 16px">
          <a-card title="卡片E：护理任务执行" :bordered="false" class="card-elevated">
            <a-space wrap>
              <a-button @click="go('/medical-care/care-task-board')">
                应做/已做 {{ summary.careTaskShouldCount || 0 }}/{{ summary.careTaskDoneCount || 0 }}
              </a-button>
              <a-button danger @click="go('/medical-care/care-task-board', { status: 'OVERDUE' })">
                超时 {{ summary.careTaskOverdueCount || 0 }}
              </a-button>
              <a-tag color="blue">扫码执行率 {{ (summary.scanExecuteRate || 0).toFixed(1) }}%</a-tag>
            </a-space>
          </a-card>
        </a-col>
        <a-col :xs="24" :xl="12" style="margin-bottom: 16px">
          <a-card title="卡片F：巡查与生命体征" :bordered="false" class="card-elevated">
            <a-space wrap>
              <a-button @click="go('/medical-care/inspection')">
                巡检计划/已完成 {{ summary.todayInspectionPlanCount || 0 }}/{{ summary.todayInspectionDoneCount || 0 }}
              </a-button>
              <a-button danger @click="go('/medical-care/inspection', { status: 'ABNORMAL' })">
                异常巡检 {{ summary.abnormalInspectionCount || 0 }}
              </a-button>
              <a-button @click="go('/medical-care/nursing-log', { status: 'PENDING' })">
                日志待补录 {{ summary.nursingLogPendingCount || 0 }}
              </a-button>
            </a-space>
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :xs="24" :xl="8" style="margin-bottom: 16px">
          <a-card title="卡片G：交接班" :bordered="false" class="card-elevated">
            <a-space wrap>
              <a-button @click="go('/medical-care/handovers', { status: 'DRAFT' })">
                待交接 {{ summary.handoverPendingCount || 0 }}
              </a-button>
              <a-button @click="go('/medical-care/handovers', { status: 'HANDED_OVER' })">
                已交接 {{ summary.handoverDoneCount || 0 }}
              </a-button>
              <a-button danger @click="go('/medical-care/handovers')">
                风险 {{ summary.handoverRiskCount || 0 }}
              </a-button>
            </a-space>
          </a-card>
        </a-col>
        <a-col :xs="24" :xl="8" style="margin-bottom: 16px">
          <a-card title="卡片H：质量与安全" :bordered="false" class="card-elevated">
            <a-space wrap>
              <a-button @click="go('/medical-care/nursing-quality?tab=quality')">
                事故未结案 {{ summary.incidentOpenCount || 0 }}
              </a-button>
              <a-button @click="go('/medical-care/nursing-quality?tab=quality')">
                投诉低分 {{ summary.lowScoreSurveyCount || 0 }}
              </a-button>
              <a-button danger @click="go('/medical-care/nursing-quality?tab=quality')">
                整改逾期 {{ summary.rectifyOverdueCount || 0 }}
              </a-button>
            </a-space>
          </a-card>
        </a-col>
        <a-col :xs="24" :xl="8" style="margin-bottom: 16px">
          <a-card title="卡片I：AI报告速览" :bordered="false" class="card-elevated">
            <a-space wrap>
              <a-button @click="go('/medical-care/ai-reports')">
                已生成 {{ summary.aiReportGeneratedCount || 0 }}
              </a-button>
              <a-button @click="go('/medical-care/ai-reports')">
                已发布 {{ summary.aiReportPublishedCount || 0 }}
              </a-button>
              <a-button type="primary" @click="go('/medical-care/ai-reports')">查看报告中心</a-button>
            </a-space>
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :xs="24" :xl="12" style="margin-bottom: 16px">
          <a-card title="统一作业流" :bordered="false" class="card-elevated">
            <a-space wrap>
              <a-button @click="go('/medical-care/residents')">长者患者列表</a-button>
              <a-button @click="go('/medical-care/orders')">医嘱管理</a-button>
              <a-button type="primary" @click="go('/medical-care/care-task-board')">护理任务看板</a-button>
              <a-button type="primary" @click="go('/medical-care/unified-task-center')">统一任务中心</a-button>
              <a-button @click="go('/medical-care/inspection')">健康巡检</a-button>
              <a-button @click="go('/medical-care/medication-registration')">用药登记</a-button>
              <a-button @click="go('/medical-care/nursing-log')">护理日志</a-button>
              <a-button @click="go('/medical-care/handovers')">交接班</a-button>
              <a-button @click="go('/medical-care/alert-rules')">异常规则配置</a-button>
              <a-button @click="go('/medical-care/nursing-quality?tab=medication')">护理与质量中心</a-button>
            </a-space>
          </a-card>
        </a-col>
        <a-col :xs="24" :xl="12" style="margin-bottom: 16px">
          <a-card title="评估与联动" :bordered="false" class="card-elevated">
            <a-space wrap>
              <a-button @click="go('/medical-care/assessment/tcm')">中医体质评估</a-button>
              <a-button @click="go('/medical-care/assessment/cvd')">心血管风险评估</a-button>
              <a-button @click="go('/medical-care/ai-reports')">AI健康评估报告</a-button>
              <a-button @click="go('/medical-care/integrated-account')">医护账户联动</a-button>
              <a-button @click="go('/medical-care/workbench')">高级工作台</a-button>
            </a-space>
          </a-card>
        </a-col>
      </a-row>

      <a-card title="全模块入口（3~10）" :bordered="false" class="card-elevated" style="margin-bottom: 16px">
        <a-space wrap>
          <a-button type="primary" @click="go('/medical-care/orders')">3. 医嘱管理</a-button>
          <a-button type="primary" @click="go('/medical-care/unified-task-center')">统一任务中心</a-button>
          <a-button @click="go('/medical-care/nursing-quality?tab=medication')">4. 用药管理</a-button>
          <a-button @click="go('/medical-care/nursing-quality?tab=vital')">5. 健康数据与巡查</a-button>
          <a-button @click="go('/medical-care/nursing-quality?tab=plan')">6. 护理计划与等级</a-button>
          <a-button @click="go('/medical-care/nursing-quality?tab=task')">7. 护理任务与扫码执行</a-button>
          <a-button @click="go('/medical-care/nursing-quality?tab=handover')">8. 交接班与班组</a-button>
          <a-button @click="go('/medical-care/nursing-quality?tab=pharmacy')">9. 药库与药品主数据</a-button>
          <a-button @click="go('/medical-care/nursing-quality?tab=quality')">10. 质量与报表中心</a-button>
          <a-button @click="go('/medical-care/alert-rules')">异常规则配置</a-button>
          <a-button @click="go('/medical-care/ai-reports')">AI健康评估报告</a-button>
        </a-space>
      </a-card>

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
import { resolveMedicalError } from './medicalError'
import { isAutoCarryResidentContextEnabled, syncMedicalAlertRules } from '../../utils/medicalAlertRule'
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
  const carryResident = isAutoCarryResidentContextEnabled()
  const elderIdRaw = route.query.elderId ?? route.query.residentId
  const elderId = elderIdRaw == null ? undefined : Number(elderIdRaw)
  const date = typeof route.query.date === 'string' ? route.query.date : undefined
  const status = typeof route.query.status === 'string' ? route.query.status : undefined
  return {
    elderId: carryResident && Number.isFinite(elderId) ? elderId : undefined,
    date,
    status
  }
}

function go(path: string, extra?: { elderId?: number; date?: string; status?: string }) {
  const [pathname, rawQuery] = path.split('?')
  const queryFromPath: Record<string, string> = {}
  if (rawQuery) {
    const searchParams = new URLSearchParams(rawQuery)
    searchParams.forEach((value, key) => {
      if (value) {
        queryFromPath[key] = value
      }
    })
  }
  router.push({
    path: pathname,
    query: {
      ...queryFromPath,
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
    errorText.value = resolveMedicalError(error, '加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(loadSummary)
onMounted(() => {
  syncMedicalAlertRules().catch(() => {})
})
watch(() => route.query, loadSummary)
</script>
