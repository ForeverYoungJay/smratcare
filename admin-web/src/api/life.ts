import request, { fetchPage } from '../utils/request'
import type {
  MealPlan,
  ActivityEvent,
  IncidentReport,
  HealthBasicRecord,
  BirthdayReminder,
  RoomCleaningTask,
  MaintenanceRequest
} from '../types'

export function getMealPlanPage(params: any) {
  return fetchPage<MealPlan>('/api/life/meal-plan/page', params)
}

export function createMealPlan(data: Partial<MealPlan>) {
  return request.post<MealPlan>('/api/life/meal-plan', data)
}

export function updateMealPlan(id: number, data: Partial<MealPlan>) {
  return request.put<MealPlan>(`/api/life/meal-plan/${id}`, data)
}

export function deleteMealPlan(id: number) {
  return request.delete<void>(`/api/life/meal-plan/${id}`)
}

export function getActivityPage(params: any) {
  return fetchPage<ActivityEvent>('/api/life/activity/page', params)
}

export function createActivity(data: Partial<ActivityEvent>) {
  return request.post<ActivityEvent>('/api/life/activity', data)
}

export function updateActivity(id: number, data: Partial<ActivityEvent>) {
  return request.put<ActivityEvent>(`/api/life/activity/${id}`, data)
}

export function deleteActivity(id: number) {
  return request.delete<void>(`/api/life/activity/${id}`)
}

export function getIncidentPage(params: any) {
  return fetchPage<IncidentReport>('/api/life/incident/page', params)
}

export function createIncident(data: Partial<IncidentReport>) {
  return request.post<IncidentReport>('/api/life/incident', data)
}

export function updateIncident(id: number, data: Partial<IncidentReport>) {
  return request.put<IncidentReport>(`/api/life/incident/${id}`, data)
}

export function closeIncident(id: number) {
  return request.put<IncidentReport>(`/api/life/incident/${id}/close`)
}

export function deleteIncident(id: number) {
  return request.delete<void>(`/api/life/incident/${id}`)
}

export function getHealthBasicPage(params: any) {
  return fetchPage<HealthBasicRecord>('/api/life/health-basic/page', params)
}

export function createHealthBasic(data: Partial<HealthBasicRecord>) {
  return request.post<HealthBasicRecord>('/api/life/health-basic', data)
}

export function updateHealthBasic(id: number, data: Partial<HealthBasicRecord>) {
  return request.put<HealthBasicRecord>(`/api/life/health-basic/${id}`, data)
}

export function deleteHealthBasic(id: number) {
  return request.delete<void>(`/api/life/health-basic/${id}`)
}

export function getBirthdayPage(params: any) {
  return fetchPage<BirthdayReminder>('/api/life/birthday/page', params)
}

export function getRoomCleaningPage(params: any) {
  return fetchPage<RoomCleaningTask>('/api/life/room-cleaning/page', params)
}

export function createRoomCleaning(data: Partial<RoomCleaningTask>) {
  return request.post<RoomCleaningTask>('/api/life/room-cleaning', data)
}

export function updateRoomCleaning(id: number, data: Partial<RoomCleaningTask>) {
  return request.put<RoomCleaningTask>(`/api/life/room-cleaning/${id}`, data)
}

export function completeRoomCleaning(id: number) {
  return request.put<RoomCleaningTask>(`/api/life/room-cleaning/${id}/done`)
}

export function deleteRoomCleaning(id: number) {
  return request.delete<void>(`/api/life/room-cleaning/${id}`)
}

export function getMaintenancePage(params: any) {
  return fetchPage<MaintenanceRequest>('/api/life/maintenance/page', params)
}

export function createMaintenance(data: Partial<MaintenanceRequest>) {
  return request.post<MaintenanceRequest>('/api/life/maintenance', data)
}

export function updateMaintenance(id: number, data: Partial<MaintenanceRequest>) {
  return request.put<MaintenanceRequest>(`/api/life/maintenance/${id}`, data)
}

export function completeMaintenance(id: number) {
  return request.put<MaintenanceRequest>(`/api/life/maintenance/${id}/complete`)
}

export function deleteMaintenance(id: number) {
  return request.delete<void>(`/api/life/maintenance/${id}`)
}
