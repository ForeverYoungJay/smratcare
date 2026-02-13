import request, { fetchPage } from '../utils/request'
import type {
  HrStaffProfile,
  StaffTrainingRecord,
  StaffPointsAccount,
  StaffPointsLog,
  StaffPerformanceSummary,
  StaffPerformanceRankItem,
  StaffPointsRule
} from '../types'

export function getHrStaffPage(params: any) {
  return fetchPage<HrStaffProfile>('/api/admin/hr/staff/page', params)
}

export function getHrProfile(staffId: number) {
  return request.get<HrStaffProfile>(`/api/admin/hr/profile/${staffId}`)
}

export function upsertHrProfile(data: Partial<HrStaffProfile>) {
  return request.post<HrStaffProfile>('/api/admin/hr/profile', data)
}

export function getHrTrainingPage(params: any) {
  return fetchPage<StaffTrainingRecord>('/api/admin/hr/training/page', params)
}

export function createHrTraining(data: Partial<StaffTrainingRecord>) {
  return request.post<StaffTrainingRecord>('/api/admin/hr/training', data)
}

export function updateHrTraining(id: number, data: Partial<StaffTrainingRecord>) {
  return request.put<StaffTrainingRecord>(`/api/admin/hr/training/${id}`, data)
}

export function deleteHrTraining(id: number) {
  return request.delete<void>(`/api/admin/hr/training/${id}`)
}

export function adjustStaffPoints(data: { staffId: number; changeType: string; changePoints: number; remark?: string }) {
  return request.post<StaffPointsAccount>('/api/admin/hr/points/adjust', data)
}

export function terminateStaff(params: { staffId: number; leaveDate?: string; leaveReason?: string }) {
  return request.post<void>('/api/admin/hr/terminate', null, { params })
}

export function reinstateStaff(params: { staffId: number }) {
  return request.post<void>('/api/admin/hr/reinstate', null, { params })
}

export function getStaffPointsLog(params: any) {
  return fetchPage<StaffPointsLog>('/api/admin/hr/points/log/page', params)
}

export function getPerformanceSummary(params: { staffId: number; dateFrom?: string; dateTo?: string }) {
  return request.get<StaffPerformanceSummary>('/api/admin/hr/performance/summary', { params })
}

export function getPerformanceRanking(params?: { dateFrom?: string; dateTo?: string; sortBy?: string }) {
  return request.get<StaffPerformanceRankItem[]>('/api/admin/hr/performance/ranking', { params })
}

export function getPointsRulePage(params: any) {
  return fetchPage<StaffPointsRule>('/api/admin/hr/points/rule/page', params)
}

export function upsertPointsRule(data: Partial<StaffPointsRule>) {
  return request.post<StaffPointsRule>('/api/admin/hr/points/rule', data)
}

export function deletePointsRule(id: number) {
  return request.delete<void>(`/api/admin/hr/points/rule/${id}`)
}
