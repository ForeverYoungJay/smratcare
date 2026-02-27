import request, { fetchPage } from '../utils/request'
import { exportCsvByRequest } from '../utils/export'
import type {
  DeathRegisterCancelRequest,
  DeathRegisterCreateRequest,
  DeathRegisterItem,
  DeathRegisterQuery,
  DeathRegisterUpdateRequest,
  DischargeApplyCreateRequest,
  DischargeApplyItem,
  DischargeApplyQuery,
  DischargeApplyReviewRequest,
  MedicalOutingCreateRequest,
  MedicalOutingItem,
  MedicalOutingQuery,
  MedicalOutingReturnRequest,
  OutingCreateRequest,
  OutingItem,
  OutingQuery,
  OutingReturnRequest,
  ResidenceStatusSummary,
  TrialStayItem,
  TrialStayQuery,
  TrialStayRequest
} from '../types'

export function getOutingPage(params: OutingQuery) {
  return fetchPage<OutingItem>('/api/elder/lifecycle/outing/page', params)
}

export function createOuting(data: OutingCreateRequest) {
  return request.post<OutingItem>('/api/elder/lifecycle/outing', data)
}

export function returnOuting(id: number, data: OutingReturnRequest) {
  return request.put<OutingItem>(`/api/elder/lifecycle/outing/${id}/return`, data)
}

export function getMedicalOutingPage(params: MedicalOutingQuery) {
  return fetchPage<MedicalOutingItem>('/api/elder/lifecycle/medical-outing/page', params)
}

export function createMedicalOuting(data: MedicalOutingCreateRequest) {
  return request.post<MedicalOutingItem>('/api/elder/lifecycle/medical-outing', data)
}

export function returnMedicalOuting(id: number, data: MedicalOutingReturnRequest) {
  return request.put<MedicalOutingItem>(`/api/elder/lifecycle/medical-outing/${id}/return`, data)
}

export function getTrialStayPage(params: TrialStayQuery) {
  return fetchPage<TrialStayItem>('/api/elder/lifecycle/trial-stay/page', params)
}

export function createTrialStay(data: TrialStayRequest) {
  return request.post<TrialStayItem>('/api/elder/lifecycle/trial-stay', data)
}

export function updateTrialStay(id: number, data: TrialStayRequest) {
  return request.put<TrialStayItem>(`/api/elder/lifecycle/trial-stay/${id}`, data)
}

export function getDischargeApplyPage(params: DischargeApplyQuery) {
  return fetchPage<DischargeApplyItem>('/api/elder/lifecycle/discharge-apply/page', params)
}

export function createDischargeApply(data: DischargeApplyCreateRequest) {
  return request.post<DischargeApplyItem>('/api/elder/lifecycle/discharge-apply', data)
}

export function reviewDischargeApply(id: number, data: DischargeApplyReviewRequest) {
  return request.put<DischargeApplyItem>(`/api/elder/lifecycle/discharge-apply/${id}/review`, data)
}

export function getDeathRegisterPage(params: DeathRegisterQuery) {
  return fetchPage<DeathRegisterItem>('/api/elder/lifecycle/death-register/page', params)
}

export function createDeathRegister(data: DeathRegisterCreateRequest) {
  return request.post<DeathRegisterItem>('/api/elder/lifecycle/death-register', data)
}

export function updateDeathRegister(id: number, data: DeathRegisterUpdateRequest) {
  return request.put<DeathRegisterItem>(`/api/elder/lifecycle/death-register/${id}`, data)
}

export function cancelDeathRegister(id: number, data?: DeathRegisterCancelRequest) {
  return request.put<DeathRegisterItem>(`/api/elder/lifecycle/death-register/${id}/cancel`, data || {})
}

export function exportMedicalOuting(params: MedicalOutingQuery) {
  return exportCsvByRequest('/api/elder/lifecycle/medical-outing/export', params, '外出就医登记.csv')
}

export function exportDeathRegister(params: DeathRegisterQuery) {
  return exportCsvByRequest('/api/elder/lifecycle/death-register/export', params, '死亡登记.csv')
}

export function exportTrialStay(params: TrialStayQuery) {
  return exportCsvByRequest('/api/elder/lifecycle/trial-stay/export', params, '试住登记.csv')
}

export function exportDischargeApply(params: DischargeApplyQuery) {
  return exportCsvByRequest('/api/elder/lifecycle/discharge-apply/export', params, '退住申请.csv')
}

export function getResidenceStatusSummary() {
  return request.get<ResidenceStatusSummary>('/api/elder/lifecycle/status-summary')
}
