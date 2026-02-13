import request, { fetchPage } from '../utils/request'
import type { MealPlan, ActivityEvent, IncidentReport, HealthBasicRecord } from '../types'

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
