import request, { fetchPage } from '../utils/request'
import type {
  MedicalCareWorkbenchSummary,
  MedicalAlertRuleConfig,
  MedicalUnifiedTaskItem,
  MedicalCvdAssessment,
  MedicalCvdAssessmentSummary,
  MedicalAiGenerateTaskResponse,
  MedicalAiReportItem,
  MedicalResidentOverview,
  MedicalResidentRiskCard,
  MedicalTcmAssessment,
  MedicalTcmAssessmentSummary
} from '../types'

export function getMedicalCareWorkbenchSummary() {
  return request.get<MedicalCareWorkbenchSummary>('/api/medical-care/workbench/summary')
}

export function getMedicalHealthCenterSummary(params?: {
  elderId?: number | string
  date?: string
  status?: string
}) {
  return request.get<MedicalCareWorkbenchSummary>('/api/medical-care/center/summary', { params })
}

export function getMedicalUnifiedTaskPage(params: any) {
  return fetchPage<MedicalUnifiedTaskItem>('/api/medical-care/workbench/unified-tasks', params)
}

export function getMedicalAlertRuleConfig() {
  return request.get<MedicalAlertRuleConfig>('/api/medical-care/alert-rules')
}

export function updateMedicalAlertRuleConfig(data: Partial<MedicalAlertRuleConfig>) {
  return request.put<MedicalAlertRuleConfig>('/api/medical-care/alert-rules', data)
}

export function getTcmAssessmentPage(params: any) {
  return fetchPage<MedicalTcmAssessment>('/api/medical-care/tcm-assessments/page', params)
}

export function getTcmAssessmentSummary(params: any) {
  return request.get<MedicalTcmAssessmentSummary>('/api/medical-care/tcm-assessments/summary', { params })
}

export function createTcmAssessment(data: Partial<MedicalTcmAssessment>) {
  return request.post<MedicalTcmAssessment>('/api/medical-care/tcm-assessments', data)
}

export function updateTcmAssessment(id: number, data: Partial<MedicalTcmAssessment>) {
  return request.put<MedicalTcmAssessment>(`/api/medical-care/tcm-assessments/${id}`, data)
}

export function publishTcmAssessment(id: number) {
  return request.post<MedicalTcmAssessment>(`/api/medical-care/tcm-assessments/${id}/publish`)
}

export function deleteTcmAssessment(id: number) {
  return request.delete<void>(`/api/medical-care/tcm-assessments/${id}`)
}

export function getCvdAssessmentPage(params: any) {
  return fetchPage<MedicalCvdAssessment>('/api/medical-care/cvd-assessments/page', params)
}

export function getCvdAssessmentSummary(params: any) {
  return request.get<MedicalCvdAssessmentSummary>('/api/medical-care/cvd-assessments/summary', { params })
}

export function createCvdAssessment(data: Partial<MedicalCvdAssessment>) {
  return request.post<MedicalCvdAssessment>('/api/medical-care/cvd-assessments', data)
}

export function updateCvdAssessment(id: number, data: Partial<MedicalCvdAssessment>) {
  return request.put<MedicalCvdAssessment>(`/api/medical-care/cvd-assessments/${id}`, data)
}

export function publishCvdAssessment(
  id: number,
  actions: { generateInspectionPlan?: number; generateFollowupTask?: number; suggestMedicalOrder?: number }
) {
  return request.post<{ inspectionRoute?: string; medicalOrderRoute?: string; careTaskRoute?: string }>(
    `/api/medical-care/cvd-assessments/${id}/publish`,
    actions
  )
}

export function deleteCvdAssessment(id: number) {
  return request.delete<void>(`/api/medical-care/cvd-assessments/${id}`)
}

export function getResidentRiskCard(residentId: number | string) {
  return request.get<MedicalResidentRiskCard>('/api/medical-care/resident360/risk-card', { params: { residentId } })
}

export function getResidentOverview(residentId: number | string) {
  return request.get<MedicalResidentOverview>('/api/medical-care/resident360/overview', { params: { residentId } })
}

export function getMedicalAiReportPage(params: any) {
  return fetchPage<MedicalAiReportItem>('/api/medical-care/ai-reports/page', params)
}

export function generateMedicalAiReport(data: {
  type?: string
  dateFrom?: string
  dateTo?: string
}) {
  return request.post<MedicalAiReportItem>('/api/medical-care/ai-reports/generate', data)
}

export function publishMedicalAiReport(id: number) {
  return request.post<MedicalAiReportItem>(`/api/medical-care/ai-reports/${id}/publish`)
}

export function generateMedicalAiReportTasks(id: number) {
  return request.post<MedicalAiGenerateTaskResponse>(`/api/medical-care/ai-reports/${id}/generate-tasks`)
}
