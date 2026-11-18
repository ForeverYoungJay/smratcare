import request, { fetchPage } from '../utils/request'
import type { Id } from '../types'

/** 人事运营：工资批次、考勤锁定与调整单、积分冲正与申诉、返聘查重、离职清单 */

export interface PayrollBatch {
  id: Id
  period: string
  status: 'DRAFT' | 'SUBMITTED' | 'APPROVED' | 'BANK_SUBMITTED' | 'PAID'
  headcount?: number
  totalGross?: number
  totalNet?: number
  preparedByName?: string
  approvedByName?: string
  approvedAt?: string
  rejectReason?: string
  bankSubmittedByName?: string
  bankSubmittedAt?: string
  paymentRef?: string
  paidByName?: string
  paidAt?: string
}
export interface PayrollLine {
  id: Id
  batchId: Id
  staffId: Id
  staffName?: string
  baseAmount?: number
  performanceAmount?: number
  allowanceAmount?: number
  overtimeAmount?: number
  deductionAmount?: number
  personalAmount?: number
  grossAmount?: number
  netAmount?: number
  remark?: string
}
export interface AttendancePeriod { id: Id; period: string; status: string; lockedByName?: string; lockedAt?: string }
export interface AttendanceAdjustment {
  id: Id
  recordId: Id
  staffId?: Id
  workDate?: string
  newCheckIn?: string
  newCheckOut?: string
  newStatus?: string
  reason: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
  requestedByName?: string
  decidedByName?: string
  decidedAt?: string
  decisionNote?: string
  createTime?: string
}
export interface PerformanceAppeal {
  id: Id
  staffId: Id
  staffName?: string
  targetType: 'POINTS_LOG' | 'REWARD_PUNISHMENT' | 'PERFORMANCE'
  targetId?: Id
  reason: string
  status: 'PENDING' | 'ACCEPTED' | 'REJECTED'
  reviewerName?: string
  reviewedAt?: string
  decisionNote?: string
  createTime?: string
}
export interface PointsLog { id: Id; changeType: string; changePoints: number; balanceAfter: number; sourceType?: string; sourceId?: Id; remark?: string; createTime?: string }
export interface ChecklistItem { key: string; label: string; done: boolean; detail: string }

const admin = '/api/admin/hr'

export const getPayrollBatches = (params: Record<string, unknown>) => fetchPage<PayrollBatch>(`${admin}/payroll/batches`, params)
export const createPayrollBatch = (period: string) => request.post<PayrollBatch>(`${admin}/payroll/batches`, { period })
export const getPayrollLines = (batchId: Id) => request.get<PayrollLine[]>(`${admin}/payroll/batches/${batchId}/lines`)
export const updatePayrollLine = (line: PayrollLine) => request.put<PayrollLine>(`${admin}/payroll/lines/${line.id}`, line)
export const payrollAction = (batchId: Id, action: 'submit' | 'approve' | 'reject' | 'bank-submit' | 'mark-paid', data?: { note?: string; paymentRef?: string }) =>
  request.post<PayrollBatch>(`${admin}/payroll/batches/${batchId}/${action}`, data || {})
export const getMyPayslips = () => request.get<{ period: string; status: string; line: PayrollLine }[]>('/api/hr/payroll/my')

export const getAttendancePeriods = () => request.get<AttendancePeriod[]>(`${admin}/attendance/periods`)
export const lockAttendancePeriod = (period: string) => request.post<AttendancePeriod>(`${admin}/attendance/periods/${period}/lock`)
export const getAttendanceAdjustments = (params: Record<string, unknown>) => fetchPage<AttendanceAdjustment>(`${admin}/attendance/adjustments`, params)
export const requestAttendanceAdjustment = (data: { recordId: Id; newCheckIn?: string; newCheckOut?: string; newStatus?: string; reason: string }) =>
  request.post<AttendanceAdjustment>(`${admin}/attendance/adjustments`, data)
export const decideAttendanceAdjustment = (id: Id, approve: boolean, note?: string) =>
  request.post<AttendanceAdjustment>(`${admin}/attendance/adjustments/${id}/decide`, { approve, note })

export const reversePointsLog = (id: Id, reason: string) => request.post<void>(`${admin}/points/log/${id}/reverse`, { reason })
export const getAppeals = (params: Record<string, unknown>) => fetchPage<PerformanceAppeal>(`${admin}/appeals`, params)
export const decideAppeal = (id: Id, approve: boolean, note: string) => request.post<PerformanceAppeal>(`${admin}/appeals/${id}/decide`, { approve, note })
export const submitAppeal = (data: { targetType: string; targetId?: Id; reason: string }) => request.post<PerformanceAppeal>('/api/hr/appeals', data)
export const getMyAppeals = () => request.get<PerformanceAppeal[]>('/api/hr/appeals/my')
export const getMyPoints = () => request.get<PointsLog[]>('/api/hr/points/my')

export const checkStaffDuplicate = (params: { phone?: string; name?: string }) =>
  request.get<{ staffId: string; staffNo?: string; realName: string; onJob: boolean; samePhone: boolean }[]>(`${admin}/staff/duplicate-check`, { params })
export const getOffboardingChecklist = (staffId: Id) => request.get<ChecklistItem[]>(`${admin}/staff/${staffId}/offboarding-checklist`)
