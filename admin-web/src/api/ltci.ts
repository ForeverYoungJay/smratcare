import request, { fetchPage } from '../utils/request'
import type { Id, LtciAssessment, LtciServicePackage, LtciSettlement } from '../types'

// ===== 失能评估 =====
export function submitLtciApply(data: {
  elderId: Id
  applyType?: string
  applySource?: string
  applicantName?: string
  applicantPhone?: string
  remark?: string
}) {
  return request.post<Id>('/api/ltci/assessments/apply', data)
}

export function acceptLtciApply(data: { applyId: Id; assessorId?: Id; remark?: string }) {
  return request.post('/api/ltci/assessments/accept', data)
}

export function scoreLtciAssessment(data: {
  applyId: Id
  elderId: Id
  templateId?: Id
  assessorId?: Id
  answers: Record<string, number>
  remark?: string
}) {
  return request.post<LtciAssessment>('/api/ltci/assessments/score', data)
}

export function notifyLtciAssessment(id: Id) {
  return request.post(`/api/ltci/assessments/${id}/notify`)
}

export function disputeLtciAssessment(data: {
  assessmentId: Id
  reason: string
  applicantName?: string
  applicantPhone?: string
}) {
  return request.post<Id>('/api/ltci/assessments/dispute', data)
}

export function getLtciAssessmentPage(params: {
  pageNo?: number
  pageSize?: number
  elderId?: Id
  status?: string
  disabilityLevel?: number
}) {
  return fetchPage<LtciAssessment>('/api/ltci/assessments/page', params)
}

export function getLtciCurrentLevel(elderId: Id) {
  return request.get<LtciAssessment>(`/api/ltci/assessments/elders/${elderId}/current`)
}

// ===== 待遇与结算 =====
export function registerLtciInsured(data: {
  elderId: Id
  insuredNo: string
  idCard?: string
  cityCode?: string
  startDate?: string
  endDate?: string
  remark?: string
}) {
  return request.post<Id>('/api/ltci/insured', data)
}

export function grantLtciBenefit(data: {
  insuredId: Id
  elderId: Id
  assessmentId?: Id
  disabilityLevel?: number
  benefitType?: string
  dailyQuota: number
  payRatio: number
  validStart?: string
  validEnd?: string
  remark?: string
}) {
  return request.post<Id>('/api/ltci/benefits', data)
}

export function getLtciPackages() {
  return request.get<LtciServicePackage[]>('/api/ltci/packages')
}

export function addLtciServiceRecord(data: {
  elderId: Id
  benefitId?: Id
  packageId?: Id
  serviceDate: string
  itemCode?: string
  itemName?: string
  quantity?: number
  fee: number
  operatorId?: Id
  signUrl?: string
  remark?: string
}) {
  return request.post<Id>('/api/ltci/service-records', data)
}

export function generateLtciSettlement(elderId: Id, month: string) {
  return request.post<LtciSettlement>('/api/ltci/settlements/generate', null, {
    params: { elderId, month }
  })
}

export function getLtciSettlementPage(params: {
  pageNo?: number
  pageSize?: number
  elderId?: Id
  month?: string
  status?: string
}) {
  return fetchPage<LtciSettlement>('/api/ltci/settlements/page', params)
}

export function submitLtciSettlement(id: Id) {
  return request.post(`/api/ltci/settlements/${id}/submit`)
}
