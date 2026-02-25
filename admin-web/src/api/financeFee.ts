import request, { fetchPage } from '../utils/request'
import type {
  AdmissionFeeAuditItem,
  DischargeFeeAuditItem,
  DischargeSettlementItem,
  ConsumptionRecordItem,
  MonthlyAllocationItem,
  AdmissionFeeAuditCreateRequest,
  DischargeFeeAuditCreateRequest,
  FeeAuditReviewRequest,
  DischargeSettlementCreateRequest,
  DischargeSettlementConfirmRequest,
  ConsumptionRecordCreateRequest,
  MonthlyAllocationCreateRequest
} from '../types/financeFee'

export function getAdmissionFeeAuditPage(params: any) {
  return fetchPage<AdmissionFeeAuditItem>('/api/finance/fee/admission-audit/page', params)
}

export function createAdmissionFeeAudit(data: AdmissionFeeAuditCreateRequest) {
  return request.post<AdmissionFeeAuditItem>('/api/finance/fee/admission-audit', data)
}

export function reviewAdmissionFeeAudit(id: number, data: FeeAuditReviewRequest) {
  return request.put<AdmissionFeeAuditItem>(`/api/finance/fee/admission-audit/${id}/review`, data)
}

export function getDischargeFeeAuditPage(params: any) {
  return fetchPage<DischargeFeeAuditItem>('/api/finance/fee/discharge-audit/page', params)
}

export function createDischargeFeeAudit(data: DischargeFeeAuditCreateRequest) {
  return request.post<DischargeFeeAuditItem>('/api/finance/fee/discharge-audit', data)
}

export function reviewDischargeFeeAudit(id: number, data: FeeAuditReviewRequest) {
  return request.put<DischargeFeeAuditItem>(`/api/finance/fee/discharge-audit/${id}/review`, data)
}

export function getDischargeSettlementPage(params: any) {
  return fetchPage<DischargeSettlementItem>('/api/finance/fee/discharge-settlement/page', params)
}

export function createDischargeSettlement(data: DischargeSettlementCreateRequest) {
  return request.post<DischargeSettlementItem>('/api/finance/fee/discharge-settlement', data)
}

export function confirmDischargeSettlement(id: number, data?: DischargeSettlementConfirmRequest) {
  return request.post<DischargeSettlementItem>(`/api/finance/fee/discharge-settlement/${id}/confirm`, data || {})
}

export function getConsumptionPage(params: any) {
  return fetchPage<ConsumptionRecordItem>('/api/finance/fee/consumption/page', params)
}

export function createConsumption(data: ConsumptionRecordCreateRequest) {
  return request.post<ConsumptionRecordItem>('/api/finance/fee/consumption', data)
}

export function getMonthlyAllocationPage(params: any) {
  return fetchPage<MonthlyAllocationItem>('/api/finance/fee/monthly-allocation/page', params)
}

export function createMonthlyAllocation(data: MonthlyAllocationCreateRequest) {
  return request.post<MonthlyAllocationItem>('/api/finance/fee/monthly-allocation', data)
}
