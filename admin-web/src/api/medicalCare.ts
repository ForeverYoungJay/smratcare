import request, { fetchPage } from '../utils/request'
import type { MedicalCareWorkbenchSummary, MedicalCvdAssessment, MedicalTcmAssessment } from '../types'

export function getMedicalCareWorkbenchSummary() {
  return request.get<MedicalCareWorkbenchSummary>('/api/medical-care/workbench/summary')
}

export function getTcmAssessmentPage(params: any) {
  return fetchPage<MedicalTcmAssessment>('/api/medical-care/tcm-assessments/page', params)
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
  return request.post<{ inspectionRoute?: string; medicalOrderRoute?: string }>(`/api/medical-care/cvd-assessments/${id}/publish`, actions)
}

export function deleteCvdAssessment(id: number) {
  return request.delete<void>(`/api/medical-care/cvd-assessments/${id}`)
}
