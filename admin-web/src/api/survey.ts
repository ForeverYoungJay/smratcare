import request, { fetchPage } from '../utils/request'
import type {
  SurveyQuestion,
  SurveyTemplate,
  SurveyTemplateDetail,
  SurveyTemplateQuestionItem,
  SurveySubmissionRequest,
  SurveySubmissionResponse,
  SurveyStatsSummary,
  SurveyPerformanceItem
} from '../types'

export function getSurveyQuestionPage(params: any) {
  return fetchPage<SurveyQuestion>('/api/admin/survey/questions/page', params)
}

export function getSurveyQuestion(id: number) {
  return request.get<SurveyQuestion>(`/api/admin/survey/questions/${id}`)
}

export function createSurveyQuestion(data: Partial<SurveyQuestion>) {
  return request.post<SurveyQuestion>('/api/admin/survey/questions', data)
}

export function updateSurveyQuestion(id: number, data: Partial<SurveyQuestion>) {
  return request.put<SurveyQuestion>(`/api/admin/survey/questions/${id}`, data)
}

export function deleteSurveyQuestion(id: number) {
  return request.delete<void>(`/api/admin/survey/questions/${id}`)
}

export function getSurveyTemplatePage(params: any) {
  return fetchPage<SurveyTemplate>('/api/admin/survey/templates/page', params)
}

export function getSurveyTemplateDetail(id: number) {
  return request.get<SurveyTemplateDetail>(`/api/admin/survey/templates/${id}`)
}

export function createSurveyTemplate(data: Partial<SurveyTemplate>) {
  return request.post<SurveyTemplate>('/api/admin/survey/templates', data)
}

export function updateSurveyTemplate(id: number, data: Partial<SurveyTemplate>) {
  return request.put<SurveyTemplate>(`/api/admin/survey/templates/${id}`, data)
}

export function deleteSurveyTemplate(id: number) {
  return request.delete<void>(`/api/admin/survey/templates/${id}`)
}

export function updateSurveyTemplateQuestions(id: number, questions: SurveyTemplateQuestionItem[]) {
  return request.put<void>(`/api/admin/survey/templates/${id}/questions`, { questions })
}

export function submitSurvey(data: SurveySubmissionRequest) {
  return request.post<SurveySubmissionResponse>('/api/survey/submissions', data)
}

export function getSurveySubmissionPage(params: any) {
  return fetchPage<any>('/api/survey/submissions/page', params)
}

export function getSurveyStatsSummary(params: any) {
  return request.get<SurveyStatsSummary>('/api/survey/stats/summary', { params })
}

export function getSurveyPerformance(params: any) {
  return request.get<SurveyPerformanceItem[]>('/api/survey/stats/performance', { params })
}
