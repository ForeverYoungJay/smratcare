import request, { fetchPage } from '../utils/request'
import type {
  CaregiverGroupItem,
  ShiftTemplateItem,
  ShiftHandoverItem,
  CareLevelItem,
  ServicePlanItem,
  ServiceBookingItem,
  NursingRecordItem,
  NursingReportSummary
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

export function getCareLevelList(params?: any) {
  return request.get<CareLevelItem[]>('/api/nursing/care-levels/list', { params })
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
