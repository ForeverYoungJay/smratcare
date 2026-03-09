import type { MedicalCareWorkbenchSummary } from '../types'

export const MEDICAL_RISK_LEVEL_LABEL_MAP: Record<string, string> = {
  LOW: '低风险',
  MEDIUM: '中风险',
  HIGH: '高风险',
  CRITICAL: '紧急风险'
}

export const MEDICAL_RISK_LEVEL_COLOR_MAP: Record<string, string> = {
  LOW: 'green',
  MEDIUM: 'blue',
  HIGH: 'orange',
  CRITICAL: 'red'
}

export function resolveMedicalRiskLabel(level?: string) {
  const key = String(level || 'LOW').toUpperCase()
  return MEDICAL_RISK_LEVEL_LABEL_MAP[key] || MEDICAL_RISK_LEVEL_LABEL_MAP.LOW
}

export function resolveMedicalRiskColor(level?: string) {
  const key = String(level || 'LOW').toUpperCase()
  return MEDICAL_RISK_LEVEL_COLOR_MAP[key] || 'default'
}

export function normalizeRiskIndex(value?: number) {
  return Math.max(0, Math.min(100, Number(value || 0)))
}

export function formatPercent(value?: number, digits = 2) {
  return Number(value || 0).toFixed(digits)
}

export function createMedicalWorkbenchSummaryDefaults(): MedicalCareWorkbenchSummary {
  return {
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
    abnormalInspectionOpenCount: 0,
    todayMedicationPendingCount: 0,
    todayMedicationDoneCount: 0,
    tcmPublishedCount: 0,
    cvdHighRiskCount: 0,
    cvdNeedFollowupCount: 0,
    keyResidents: [],
    configuredIncidentWindowDays: 30,
    configuredOverdueHours: 12,
    configuredTopResidentLimit: 5,
    configuredRiskResidentLookbackDays: 180,
    snapshotDate: '',
    incidentWindowStartDate: '',
    incidentWindowEndDate: '',
    generatedAt: '',
    medicalOrderExecutionRate: 0,
    careTaskCompletionRate: 0,
    inspectionCompletionRate: 0,
    careTaskOverdueRate: 0,
    medicationPendingRate: 0,
    inspectionAbnormalRate: 0,
    riskIndex: 0,
    riskLevel: 'LOW',
    riskTriggeredCount: 0,
    riskSignals: [],
    focusActionCount: 0,
    focusActions: []
  }
}
