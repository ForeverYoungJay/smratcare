import request, { fetchPage } from '../utils/request'
import type { CareTaskItem, CareExecuteRequest, CareTaskTemplate, CareTaskAssignRequest, CareTaskBatchAssignRequest, CareTaskReviewRequest, CareTaskCreateRequest } from '../types'

export function getTodayTasks(params: any) {
  return fetchPage<CareTaskItem>('/api/care/tasks/today', params)
}

export function getTaskPage(params: any) {
  return fetchPage<CareTaskItem>('/api/care/tasks/page', params)
}

export function executeTask(data: CareExecuteRequest) {
  return request.post<void>('/api/care/tasks/execute', data)
}

export function generateTasks(params?: { date?: string; force?: boolean }) {
  return request.post<void>('/api/care/tasks/generate', null, { params })
}

export function assignTask(data: CareTaskAssignRequest) {
  return request.post<void>('/api/care/tasks/assign', data)
}

export function assignTaskBatch(data: CareTaskBatchAssignRequest) {
  return request.post<number>('/api/care/tasks/assign/batch', data)
}

export function reviewTask(data: CareTaskReviewRequest) {
  return request.post<void>('/api/care/tasks/review', data)
}

export function createTask(data: CareTaskCreateRequest) {
  return request.post<number>('/api/care/tasks/create', data)
}

export function getTemplatePage(params: any) {
  return fetchPage<CareTaskTemplate>('/api/care/template/page', params)
}

export function createTemplate(data: Partial<CareTaskTemplate>) {
  return request.post<void>('/api/care/template', data)
}

export function updateTemplate(id: number, data: Partial<CareTaskTemplate>) {
  return request.put<void>(`/api/care/template/${id}`, data)
}

export function deleteTemplate(id: number) {
  return request.delete<void>(`/api/care/template/${id}`)
}
