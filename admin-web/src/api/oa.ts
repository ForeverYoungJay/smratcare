import request, { fetchPage } from '../utils/request'
import type {
  OaNotice,
  OaTodo,
  OaTodoSummary,
  OaApproval,
  OaApprovalSummary,
  OaDocument,
  OaTask,
  OaTaskSummary,
  OaPortalSummary,
  OaWorkReport,
  OaWorkReportSummary,
  OaAlbum,
  OaKnowledge,
  OaGroupSetting,
  OaActivityPlan,
  OaSuggestion
} from '../types'

export function getPortalSummary() {
  return request.get<OaPortalSummary>('/api/oa/portal/summary')
}

export function uploadOaFile(file: File, bizType = 'oa-approval-proof') {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('bizType', bizType)
  return request.post<{ fileName?: string; originalFileName?: string; fileUrl?: string; fileType?: string; fileSize?: number }>(
    '/api/files/upload',
    formData,
    { headers: { 'Content-Type': 'multipart/form-data' } }
  )
}

export function getSuggestionPage(params: any) {
  return fetchPage<OaSuggestion>('/api/oa/suggestion/page', params)
}

export function createSuggestion(data: Partial<OaSuggestion>) {
  return request.post<OaSuggestion>('/api/oa/suggestion', data)
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

export function batchPublishNotice(ids: number[]) {
  return request.put<number>('/api/oa/notice/batch/publish', { ids })
}

export function deleteNotice(id: number) {
  return request.delete<void>(`/api/oa/notice/${id}`)
}

export function batchDeleteNotice(ids: number[]) {
  return request.delete<number>('/api/oa/notice/batch', { data: { ids } })
}

export function exportNotice(params: any) {
  return request.get<Blob>('/api/oa/notice/export', { params, responseType: 'blob' })
}

export function getTodoPage(params: any) {
  return fetchPage<OaTodo>('/api/oa/todo/page', params)
}

export function getTodoSummary(params: any) {
  return request.get<OaTodoSummary>('/api/oa/todo/summary', { params })
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

export function batchCompleteTodo(ids: number[]) {
  return request.put<number>('/api/oa/todo/batch/done', { ids })
}

export function deleteTodo(id: number) {
  return request.delete<void>(`/api/oa/todo/${id}`)
}

export function batchDeleteTodo(ids: number[]) {
  return request.delete<number>('/api/oa/todo/batch', { data: { ids } })
}

export function exportTodo(params: any) {
  return request.get<Blob>('/api/oa/todo/export', { params, responseType: 'blob' })
}

export function getApprovalPage(params: any) {
  return fetchPage<OaApproval>('/api/oa/approval/page', params)
}

export function getApprovalSummary(params: any) {
  return request.get<OaApprovalSummary>('/api/oa/approval/summary', { params })
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

export function batchApproveApproval(ids: number[]) {
  return request.put<number>('/api/oa/approval/batch/approve', { ids })
}

export function rejectApproval(id: number, remark?: string) {
  return request.put<OaApproval>(`/api/oa/approval/${id}/reject`, null, { params: { remark } })
}

export function batchRejectApproval(ids: number[], remark?: string) {
  return request.put<number>('/api/oa/approval/batch/reject', { ids, remark })
}

export function deleteApproval(id: number) {
  return request.delete<void>(`/api/oa/approval/${id}`)
}

export function batchDeleteApproval(ids: number[]) {
  return request.delete<number>('/api/oa/approval/batch', { data: { ids } })
}

export function exportApproval(params: any) {
  return request.get<Blob>('/api/oa/approval/export', { params, responseType: 'blob' })
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

export function batchDeleteDocument(ids: number[]) {
  return request.delete<number>('/api/oa/document/batch', { data: { ids } })
}

export function exportDocument(params: any) {
  return request.get<Blob>('/api/oa/document/export', { params, responseType: 'blob' })
}

export function getAlbumPage(params: any) {
  return fetchPage<OaAlbum>('/api/oa/album/page', params)
}

export function createAlbum(data: Partial<OaAlbum>) {
  return request.post<OaAlbum>('/api/oa/album', data)
}

export function updateAlbum(id: number, data: Partial<OaAlbum>) {
  return request.put<OaAlbum>(`/api/oa/album/${id}`, data)
}

export function deleteAlbum(id: number) {
  return request.delete<void>(`/api/oa/album/${id}`)
}

export function getKnowledgePage(params: any) {
  return fetchPage<OaKnowledge>('/api/oa/knowledge/page', params)
}

export function createKnowledge(data: Partial<OaKnowledge>) {
  return request.post<OaKnowledge>('/api/oa/knowledge', data)
}

export function updateKnowledge(id: number, data: Partial<OaKnowledge>) {
  return request.put<OaKnowledge>(`/api/oa/knowledge/${id}`, data)
}

export function publishKnowledge(id: number) {
  return request.put<OaKnowledge>(`/api/oa/knowledge/${id}/publish`)
}

export function archiveKnowledge(id: number) {
  return request.put<OaKnowledge>(`/api/oa/knowledge/${id}/archive`)
}

export function batchPublishKnowledge(ids: number[]) {
  return request.put<number>('/api/oa/knowledge/batch/publish', { ids })
}

export function batchArchiveKnowledge(ids: number[]) {
  return request.put<number>('/api/oa/knowledge/batch/archive', { ids })
}

export function deleteKnowledge(id: number) {
  return request.delete<void>(`/api/oa/knowledge/${id}`)
}

export function batchDeleteKnowledge(ids: number[]) {
  return request.delete<number>('/api/oa/knowledge/batch', { data: { ids } })
}

export function exportKnowledge(params: any) {
  return request.get<Blob>('/api/oa/knowledge/export', { params, responseType: 'blob' })
}

export function getGroupSettingPage(params: any) {
  return fetchPage<OaGroupSetting>('/api/oa/group-setting/page', params)
}

export function createGroupSetting(data: Partial<OaGroupSetting>) {
  return request.post<OaGroupSetting>('/api/oa/group-setting', data)
}

export function updateGroupSetting(id: number, data: Partial<OaGroupSetting>) {
  return request.put<OaGroupSetting>(`/api/oa/group-setting/${id}`, data)
}

export function enableGroupSetting(id: number) {
  return request.put<OaGroupSetting>(`/api/oa/group-setting/${id}/enable`)
}

export function disableGroupSetting(id: number) {
  return request.put<OaGroupSetting>(`/api/oa/group-setting/${id}/disable`)
}

export function batchEnableGroupSetting(ids: number[]) {
  return request.put<number>('/api/oa/group-setting/batch/enable', { ids })
}

export function batchDisableGroupSetting(ids: number[]) {
  return request.put<number>('/api/oa/group-setting/batch/disable', { ids })
}

export function deleteGroupSetting(id: number) {
  return request.delete<void>(`/api/oa/group-setting/${id}`)
}

export function batchDeleteGroupSetting(ids: number[]) {
  return request.delete<number>('/api/oa/group-setting/batch', { data: { ids } })
}

export function exportGroupSetting(params: any) {
  return request.get<Blob>('/api/oa/group-setting/export', { params, responseType: 'blob' })
}

export function getActivityPlanPage(params: any) {
  return fetchPage<OaActivityPlan>('/api/oa/activity-plan/page', params)
}

export function createActivityPlan(data: Partial<OaActivityPlan>) {
  return request.post<OaActivityPlan>('/api/oa/activity-plan', data)
}

export function updateActivityPlan(id: number, data: Partial<OaActivityPlan>) {
  return request.put<OaActivityPlan>(`/api/oa/activity-plan/${id}`, data)
}

export function startActivityPlan(id: number) {
  return request.put<OaActivityPlan>(`/api/oa/activity-plan/${id}/start`)
}

export function completeActivityPlan(id: number) {
  return request.put<OaActivityPlan>(`/api/oa/activity-plan/${id}/done`)
}

export function cancelActivityPlan(id: number) {
  return request.put<OaActivityPlan>(`/api/oa/activity-plan/${id}/cancel`)
}

export function batchStartActivityPlan(ids: number[]) {
  return request.put<number>('/api/oa/activity-plan/batch/start', { ids })
}

export function batchCompleteActivityPlan(ids: number[]) {
  return request.put<number>('/api/oa/activity-plan/batch/done', { ids })
}

export function batchCancelActivityPlan(ids: number[]) {
  return request.put<number>('/api/oa/activity-plan/batch/cancel', { ids })
}

export function deleteActivityPlan(id: number) {
  return request.delete<void>(`/api/oa/activity-plan/${id}`)
}

export function batchDeleteActivityPlan(ids: number[]) {
  return request.delete<number>('/api/oa/activity-plan/batch', { data: { ids } })
}

export function exportActivityPlan(params: any) {
  return request.get<Blob>('/api/oa/activity-plan/export', { params, responseType: 'blob' })
}

export function getOaTaskPage(params: any) {
  return fetchPage<OaTask>('/api/oa/task/page', params)
}

export function getOaTaskSummary(params: any) {
  return request.get<OaTaskSummary>('/api/oa/task/summary', { params })
}

export function getOaTaskCalendar(params: any) {
  return request.get<OaTask[]>('/api/oa/task/calendar', { params })
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

export function batchCompleteOaTask(ids: number[]) {
  return request.put<number>('/api/oa/task/batch/done', { ids })
}

export function deleteOaTask(id: number) {
  return request.delete<void>(`/api/oa/task/${id}`)
}

export function batchDeleteOaTask(ids: number[]) {
  return request.delete<number>('/api/oa/task/batch', { data: { ids } })
}

export function exportOaTask(params: any) {
  return request.get<Blob>('/api/oa/task/export', { params, responseType: 'blob' })
}

export function getOaWorkReportPage(params: any) {
  return fetchPage<OaWorkReport>('/api/oa/report/page', params)
}

export function getOaWorkReportSummary(params: any) {
  return request.get<OaWorkReportSummary>('/api/oa/report/summary', { params })
}

export function getDailyWorkReportPage(params: any) {
  return fetchPage<OaWorkReport>('/api/oa/report/daily/page', params)
}

export function getWeeklyWorkReportPage(params: any) {
  return fetchPage<OaWorkReport>('/api/oa/report/weekly/page', params)
}

export function getMonthlyWorkReportPage(params: any) {
  return fetchPage<OaWorkReport>('/api/oa/report/monthly/page', params)
}

export function getYearlyWorkReportPage(params: any) {
  return fetchPage<OaWorkReport>('/api/oa/report/yearly/page', params)
}

export function createOaWorkReport(data: Partial<OaWorkReport>) {
  return request.post<OaWorkReport>('/api/oa/report', data)
}

export function updateOaWorkReport(id: number, data: Partial<OaWorkReport>) {
  return request.put<OaWorkReport>(`/api/oa/report/${id}`, data)
}

export function submitOaWorkReport(id: number) {
  return request.put<OaWorkReport>(`/api/oa/report/${id}/submit`)
}

export function deleteOaWorkReport(id: number) {
  return request.delete<void>(`/api/oa/report/${id}`)
}
