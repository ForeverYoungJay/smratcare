import request, { fetchPage } from '../utils/request'
import type { OaNotice, OaTodo, OaApproval, OaDocument, OaTask, OaPortalSummary } from '../types'

export function getPortalSummary() {
  return request.get<OaPortalSummary>('/api/oa/portal/summary')
}

export function getNoticePage(params: any) {
  return fetchPage<OaNotice>('/api/oa/notice/page', params)
}

export function getNotice(id: number) {
  return request.get<OaNotice>(`/api/oa/notice/${id}`)
}

export function createNotice(data: Partial<OaNotice>) {
  return request.post<OaNotice>('/api/oa/notice', data)
}

export function updateNotice(id: number, data: Partial<OaNotice>) {
  return request.put<OaNotice>(`/api/oa/notice/${id}`, data)
}

export function publishNotice(id: number) {
  return request.put<OaNotice>(`/api/oa/notice/${id}/publish`)
}

export function deleteNotice(id: number) {
  return request.delete<void>(`/api/oa/notice/${id}`)
}

export function getTodoPage(params: any) {
  return fetchPage<OaTodo>('/api/oa/todo/page', params)
}

export function createTodo(data: Partial<OaTodo>) {
  return request.post<OaTodo>('/api/oa/todo', data)
}

export function updateTodo(id: number, data: Partial<OaTodo>) {
  return request.put<OaTodo>(`/api/oa/todo/${id}`, data)
}

export function completeTodo(id: number) {
  return request.put<OaTodo>(`/api/oa/todo/${id}/done`)
}

export function deleteTodo(id: number) {
  return request.delete<void>(`/api/oa/todo/${id}`)
}

export function getApprovalPage(params: any) {
  return fetchPage<OaApproval>('/api/oa/approval/page', params)
}

export function createApproval(data: Partial<OaApproval>) {
  return request.post<OaApproval>('/api/oa/approval', data)
}

export function updateApproval(id: number, data: Partial<OaApproval>) {
  return request.put<OaApproval>(`/api/oa/approval/${id}`, data)
}

export function approveApproval(id: number) {
  return request.put<OaApproval>(`/api/oa/approval/${id}/approve`)
}

export function rejectApproval(id: number, remark?: string) {
  return request.put<OaApproval>(`/api/oa/approval/${id}/reject`, null, { params: { remark } })
}

export function deleteApproval(id: number) {
  return request.delete<void>(`/api/oa/approval/${id}`)
}

export function getDocumentPage(params: any) {
  return fetchPage<OaDocument>('/api/oa/document/page', params)
}

export function createDocument(data: Partial<OaDocument>) {
  return request.post<OaDocument>('/api/oa/document', data)
}

export function updateDocument(id: number, data: Partial<OaDocument>) {
  return request.put<OaDocument>(`/api/oa/document/${id}`, data)
}

export function deleteDocument(id: number) {
  return request.delete<void>(`/api/oa/document/${id}`)
}

export function getOaTaskPage(params: any) {
  return fetchPage<OaTask>('/api/oa/task/page', params)
}

export function createOaTask(data: Partial<OaTask>) {
  return request.post<OaTask>('/api/oa/task', data)
}

export function updateOaTask(id: number, data: Partial<OaTask>) {
  return request.put<OaTask>(`/api/oa/task/${id}`, data)
}

export function completeOaTask(id: number) {
  return request.put<OaTask>(`/api/oa/task/${id}/done`)
}

export function deleteOaTask(id: number) {
  return request.delete<void>(`/api/oa/task/${id}`)
}
