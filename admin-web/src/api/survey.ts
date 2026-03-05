import request, { fetchPage } from '../utils/request'
import type {
  SurveyQuestion,
  SurveyTemplate,
  SurveyTemplateDetail,
  SurveyTemplateVerifyResult,
  SurveyTemplateQuestionItem,
  SurveySubmissionRequest,
  SurveySubmissionResponse,
  SurveySubmissionItem,
  SurveyStatsSummary,
  SurveyPerformanceItem
} from '../types'

export function getSurveyQuestionPage(params: any) {
  return fetchPage<SurveyQuestion>('/api/admin/survey/questions/page', params)
}

export function getSurveyQuestion(id: string | number) {
  return request.get<SurveyQuestion>(`/api/admin/survey/questions/${id}`)
}

export function createSurveyQuestion(data: Partial<SurveyQuestion>) {
  return request.post<SurveyQuestion>('/api/admin/survey/questions', data)
}

export function updateSurveyQuestion(id: string | number, data: Partial<SurveyQuestion>) {
  return request.put<SurveyQuestion>(`/api/admin/survey/questions/${id}`, data)
}

export function deleteSurveyQuestion(id: string | number) {
  return request.delete<void>(`/api/admin/survey/questions/${id}`)
}

export function getSurveyTemplatePage(params: any) {
  return fetchPage<SurveyTemplate>('/api/admin/survey/templates/page', params)
}

export function getSurveyTemplateDetail(id: string | number) {
  return request.get<SurveyTemplateDetail>(`/api/admin/survey/templates/${id}`)
}

export function getSurveyPublishedTemplatePage(params: any) {
  return fetchPage<SurveyTemplate>('/api/survey/templates/page', params)
}

export function getSurveyPublishedTemplateDetail(id: string | number) {
  return request.get<SurveyTemplateDetail>(`/api/survey/templates/${id}`)
}

export function verifySurveyPublishedTemplate(id: string | number, params?: { templateCode?: string; targetType?: string }) {
  return request.get<SurveyTemplateVerifyResult>(`/api/survey/templates/${id}/verify`, { params })
}

export function createSurveyTemplate(data: Partial<SurveyTemplate>) {
  return request.post<SurveyTemplate>('/api/admin/survey/templates', data)
}

export function updateSurveyTemplate(id: string | number, data: Partial<SurveyTemplate>) {
  return request.put<SurveyTemplate>(`/api/admin/survey/templates/${id}`, data)
}

export function publishSurveyTemplate(id: string | number) {
  return request.post<SurveyTemplate>(`/api/admin/survey/templates/${id}/publish`)
}

export function disableSurveyTemplate(id: string | number) {
  return request.post<SurveyTemplate>(`/api/admin/survey/templates/${id}/disable`)
}

export function deleteSurveyTemplate(id: string | number) {
  return request.delete<void>(`/api/admin/survey/templates/${id}`)
}

export function updateSurveyTemplateQuestions(id: string | number, questions: SurveyTemplateQuestionItem[]) {
  return request.put<void>(`/api/admin/survey/templates/${id}/questions`, { questions })
}

export function submitSurvey(data: SurveySubmissionRequest) {
  return request.post<SurveySubmissionResponse>('/api/survey/submissions', data)
}

export function getSurveySubmissionPage(params: any) {
  return fetchPage<SurveySubmissionItem>('/api/survey/submissions/page', params)
}

export function getSurveyStatsSummary(params: any) {
  return request.get<SurveyStatsSummary>('/api/survey/stats/summary', { params })
}

export function getSurveyPerformance(params: any) {
  return request.get<SurveyPerformanceItem[]>('/api/survey/stats/performance', { params })
}
