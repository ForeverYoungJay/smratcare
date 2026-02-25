import request, { fetchPage } from '../utils/request'
import type { AssessmentRecord, AssessmentScaleTemplate, AssessmentScorePreview } from '../types'

export function getAssessmentRecordPage(params: any) {
  return fetchPage<AssessmentRecord>('/api/assessment/records/page', params)
}

export function getAssessmentRecord(id: number) {
  return request.get<AssessmentRecord>(`/api/assessment/records/${id}`)
}

export function createAssessmentRecord(data: Partial<AssessmentRecord>) {
  return request.post<AssessmentRecord>('/api/assessment/records', data)
}

export function updateAssessmentRecord(id: number, data: Partial<AssessmentRecord>) {
  return request.put<AssessmentRecord>(`/api/assessment/records/${id}`, data)
}

export function deleteAssessmentRecord(id: number) {
  return request.delete<void>(`/api/assessment/records/${id}`)
}

export function batchDeleteAssessmentRecord(ids: number[]) {
  return request.post<number>('/api/assessment/records/batch-delete', ids)
}

export function exportAssessmentRecord(params: any) {
  return request.get<Blob>('/api/assessment/records/export', {
    params,
    responseType: 'blob' as any
  })
}

export function getAssessmentTemplatePage(params: any) {
  return fetchPage<AssessmentScaleTemplate>('/api/assessment/templates/page', params)
}

export function getAssessmentTemplateList(params: any) {
  return request.get<AssessmentScaleTemplate[]>('/api/assessment/templates/list', { params })
}

export function createAssessmentTemplate(data: Partial<AssessmentScaleTemplate>) {
  return request.post<AssessmentScaleTemplate>('/api/assessment/templates', data)
}

export function updateAssessmentTemplate(id: number, data: Partial<AssessmentScaleTemplate>) {
  return request.put<AssessmentScaleTemplate>(`/api/assessment/templates/${id}`, data)
}

export function deleteAssessmentTemplate(id: number) {
  return request.delete<void>(`/api/assessment/templates/${id}`)
}

export function previewAssessmentScore(data: { templateId: number; detailJson: string }) {
  return request.post<AssessmentScorePreview>('/api/assessment/templates/score-preview', data)
}
