import request, { fetchPage } from '../utils/request'
import type { Id } from '../types'

export interface MedicalFollowupPlan {
  id: Id
  elderId?: Id
  elderName?: string
  diseaseType?: string
  planName?: string
  frequencyDays?: number
  targetIndicators?: string
  nextFollowupDate?: string
  lastFollowupDate?: string
  doctorId?: Id
  doctorName?: string
  status?: string
  remark?: string
  createTime?: string
}

export interface MedicalFollowupRecord {
  id: Id
  planId?: Id
  elderId?: Id
  elderName?: string
  followupDate?: string
  vitalJson?: string
  healthDataId?: Id
  medicationCompliance?: string
  assessmentLevel?: string
  assessment?: string
  nextFollowupDate?: string
  doctorName?: string
  remark?: string
  createTime?: string
}

export function getFollowupPlanPage(params: {
  pageNo?: number
  pageSize?: number
  elderId?: Id
  diseaseType?: string
  status?: string
  dueOnly?: boolean
}) {
  return fetchPage<MedicalFollowupPlan>('/api/medical/followup/plans/page', params)
}

export function createFollowupPlan(data: Partial<MedicalFollowupPlan>) {
  return request.post<MedicalFollowupPlan>('/api/medical/followup/plans', data)
}

export function updateFollowupPlan(id: Id, data: Partial<MedicalFollowupPlan>) {
  return request.put<MedicalFollowupPlan>(`/api/medical/followup/plans/${id}`, data)
}

export function updateFollowupPlanStatus(id: Id, status: string) {
  return request.post<MedicalFollowupPlan>(`/api/medical/followup/plans/${id}/status`, null, { params: { status } })
}

export function getFollowupRecordPage(params: { pageNo?: number; pageSize?: number; planId?: Id; elderId?: Id }) {
  return fetchPage<MedicalFollowupRecord>('/api/medical/followup/records/page', params)
}

export function createFollowupRecord(data: Partial<MedicalFollowupRecord>) {
  return request.post<MedicalFollowupRecord>('/api/medical/followup/records', data)
}

export function getDueFollowupPlans(date?: string) {
  return request.get<MedicalFollowupPlan[]>('/api/medical/followup/plans/due', { params: { date } })
}

export function generateFollowupReminders() {
  return request.post<number>('/api/medical/followup/reminders/generate')
}
