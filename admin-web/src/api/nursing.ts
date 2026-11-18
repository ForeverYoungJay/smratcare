import request, { fetchPage } from '../utils/request'
import type {
  CaregiverGroupItem,
  ShiftTemplateItem,
  ShiftTemplateBatchSaveRequest,
  ShiftHandoverItem,
  CareLevelItem,
  ServicePlanItem,
  ServiceBookingItem,
  NursingRecordItem,
  NursingReportSummary,
  ServicePlanRevisionItem,
  CareIncidentItem,
  CareIncidentSummary,
  CareIncidentActionRequest,
  NursingRectificationItem,
  NursingQualityMetrics,
  NursingResidentItem
} from '../types'

export function getCaregiverGroupPage(params: any) {
  return fetchPage<CaregiverGroupItem>('/api/nursing/groups/page', params)
}

export function getCaregiverGroupList(params?: any) {
  return request.get<CaregiverGroupItem[]>('/api/nursing/groups/list', { params })
}

export function createCaregiverGroup(data: Partial<CaregiverGroupItem>) {
  return request.post<CaregiverGroupItem>('/api/nursing/groups', data)
}

export function updateCaregiverGroup(id: number, data: Partial<CaregiverGroupItem>) {
  return request.put<CaregiverGroupItem>(`/api/nursing/groups/${id}`, data)
}

export function deleteCaregiverGroup(id: number) {
  return request.delete<void>(`/api/nursing/groups/${id}`)
}

export function getShiftTemplatePage(params: any) {
  return fetchPage<ShiftTemplateItem>('/api/nursing/shift-templates/page', params)
}

export function getShiftTemplateList(params?: any) {
  return request.get<ShiftTemplateItem[]>('/api/nursing/shift-templates/list', { params })
}

export function createShiftTemplate(data: Partial<ShiftTemplateItem>) {
  return request.post<ShiftTemplateItem>('/api/nursing/shift-templates', data)
}

export function saveShiftTemplateBatch(data: ShiftTemplateBatchSaveRequest) {
  return request.post<ShiftTemplateItem[]>('/api/nursing/shift-templates/batch-save', data)
}

export function updateShiftTemplate(id: number, data: Partial<ShiftTemplateItem>) {
  return request.put<ShiftTemplateItem>(`/api/nursing/shift-templates/${id}`, data)
}

export function deleteShiftTemplate(id: number) {
  return request.delete<void>(`/api/nursing/shift-templates/${id}`)
}

export function applyShiftTemplate(id: number, data: { startDate: string; endDate: string }) {
  return request.post<number>(`/api/nursing/shift-templates/${id}/apply`, data)
}

export function getShiftHandoverPage(params: any) {
  return fetchPage<ShiftHandoverItem>('/api/nursing/handovers/page', params)
}

export function getShiftHandover(id: number) {
  return request.get<ShiftHandoverItem>(`/api/nursing/handovers/${id}`)
}

export function createShiftHandover(data: Partial<ShiftHandoverItem>) {
  return request.post<ShiftHandoverItem>('/api/nursing/handovers', data)
}

export function updateShiftHandover(id: number, data: Partial<ShiftHandoverItem>) {
  return request.put<ShiftHandoverItem>(`/api/nursing/handovers/${id}`, data)
}

export function deleteShiftHandover(id: number) {
  return request.delete<void>(`/api/nursing/handovers/${id}`)
}

export function getCareLevelPage(params: any) {
  return fetchPage<CareLevelItem>('/api/nursing/care-levels/page', params)
}

export function getCareLevelList(params?: any, config?: Record<string, any>) {
  return request.get<CareLevelItem[]>('/api/nursing/care-levels/list', { params, ...config })
}

/** 护理等级只认护理部「护理等级」字典的名称：长者档案、护理收费标准都从这里选，账单才能按等级取到月费 */
export async function getCareLevelNameOptions(current?: string) {
  const list = await getCareLevelList(undefined, { silent403: true }).catch(() => [] as CareLevelItem[])
  const names = (list || []).filter((item) => item.enabled !== 0).map((item) => String(item.levelName || '').trim()).filter(Boolean)
  if (current && !names.includes(current)) names.push(current)
  return Array.from(new Set(names)).map((value) => ({ value, label: value }))
}

export function createCareLevel(data: Partial<CareLevelItem>) {
  return request.post<CareLevelItem>('/api/nursing/care-levels', data)
}

export function updateCareLevel(id: number, data: Partial<CareLevelItem>) {
  return request.put<CareLevelItem>(`/api/nursing/care-levels/${id}`, data)
}

export function deleteCareLevel(id: number) {
  return request.delete<void>(`/api/nursing/care-levels/${id}`)
}

export function getServicePlanPage(params: any) {
  return fetchPage<ServicePlanItem>('/api/nursing/service-plans/page', params)
}

export function getServicePlanList(params?: any) {
  return request.get<ServicePlanItem[]>('/api/nursing/service-plans/list', { params })
}

export function createServicePlan(data: Partial<ServicePlanItem>) {
  return request.post<ServicePlanItem>('/api/nursing/service-plans', data)
}

export function updateServicePlan(id: number, data: Partial<ServicePlanItem>) {
  return request.put<ServicePlanItem>(`/api/nursing/service-plans/${id}`, data)
}

export function deleteServicePlan(id: number) {
  return request.delete<void>(`/api/nursing/service-plans/${id}`)
}

export function getServiceBookingPage(params: any) {
  return fetchPage<ServiceBookingItem>('/api/nursing/service-bookings/page', params)
}

export function createServiceBooking(data: Partial<ServiceBookingItem>) {
  return request.post<ServiceBookingItem>('/api/nursing/service-bookings', data)
}

export function updateServiceBooking(id: number, data: Partial<ServiceBookingItem>) {
  return request.put<ServiceBookingItem>(`/api/nursing/service-bookings/${id}`, data)
}

export function deleteServiceBooking(id: number) {
  return request.delete<void>(`/api/nursing/service-bookings/${id}`)
}

export function getNursingRecordPage(params: any) {
  return fetchPage<NursingRecordItem>('/api/nursing/records/page', params)
}

export function getNursingReportSummary(params?: any) {
  return request.get<NursingReportSummary>('/api/nursing/reports/summary', { params })
}

export function confirmShiftHandover(id: number) {
  return request.post<ShiftHandoverItem>(`/api/nursing/handovers/${id}/confirm`)
}

export function approveServicePlan(id: number) {
  return request.post<ServicePlanItem>(`/api/nursing/service-plans/${id}/approve`)
}

export function rejectServicePlan(id: number, reason: string) {
  return request.post<ServicePlanItem>(`/api/nursing/service-plans/${id}/reject`, { reason })
}

export function getServicePlanRevisions(id: number) {
  return request.get<ServicePlanRevisionItem[]>(`/api/nursing/service-plans/${id}/revisions`)
}

export function getIncidentPage(params: any) {
  return fetchPage<CareIncidentItem>('/api/nursing/incidents/page', params)
}

export function getIncidentSummary(month: string) {
  return request.get<CareIncidentSummary>('/api/nursing/incidents/summary', { params: { month } })
}

export function getIncident(id: number) {
  return request.get<CareIncidentItem>(`/api/nursing/incidents/${id}`)
}

export function createIncident(data: Partial<CareIncidentItem>) {
  return request.post<CareIncidentItem>('/api/nursing/incidents', data)
}

export function actOnIncident(id: number, data: CareIncidentActionRequest) {
  return request.post<CareIncidentItem>(`/api/nursing/incidents/${id}/actions`, data)
}

export function getRectificationPage(params: any) {
  return fetchPage<NursingRectificationItem>('/api/nursing/rectifications/page', params)
}

export function createRectification(data: Partial<NursingRectificationItem>) {
  return request.post<NursingRectificationItem>('/api/nursing/rectifications', data)
}

export function updateRectification(id: number, data: Partial<NursingRectificationItem>) {
  return request.put<NursingRectificationItem>(`/api/nursing/rectifications/${id}`, data)
}

export function reviewRectification(id: number, passed: boolean, reviewResult: string) {
  return request.post<NursingRectificationItem>(`/api/nursing/rectifications/${id}/review`, { passed, reviewResult })
}

export function getQualityMetrics(params: { dateFrom: string; dateTo: string }) {
  return request.get<NursingQualityMetrics>('/api/nursing/quality/metrics', { params })
}

export function getNursingResidentPage(params: { pageNo: number; pageSize: number; keyword?: string; reassessDue?: boolean }) {
  return fetchPage<NursingResidentItem>('/api/nursing/residents/page', params)
}
