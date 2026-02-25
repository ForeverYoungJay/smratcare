import request, { fetchPage } from '../utils/request'
import type {
  DischargeApplyCreateRequest,
  DischargeApplyItem,
  DischargeApplyReviewRequest,
  OutingCreateRequest,
  OutingItem,
  OutingReturnRequest,
  TrialStayItem,
  TrialStayRequest
} from '../types'

export function getOutingPage(params: any) {
  return fetchPage<OutingItem>('/api/elder/lifecycle/outing/page', params)
}

export function createOuting(data: OutingCreateRequest) {
  return request.post<OutingItem>('/api/elder/lifecycle/outing', data)
}

export function returnOuting(id: number, data: OutingReturnRequest) {
  return request.put<OutingItem>(`/api/elder/lifecycle/outing/${id}/return`, data)
}

export function getTrialStayPage(params: any) {
  return fetchPage<TrialStayItem>('/api/elder/lifecycle/trial-stay/page', params)
}

export function createTrialStay(data: TrialStayRequest) {
  return request.post<TrialStayItem>('/api/elder/lifecycle/trial-stay', data)
}

export function updateTrialStay(id: number, data: TrialStayRequest) {
  return request.put<TrialStayItem>(`/api/elder/lifecycle/trial-stay/${id}`, data)
}

export function getDischargeApplyPage(params: any) {
  return fetchPage<DischargeApplyItem>('/api/elder/lifecycle/discharge-apply/page', params)
}

export function createDischargeApply(data: DischargeApplyCreateRequest) {
  return request.post<DischargeApplyItem>('/api/elder/lifecycle/discharge-apply', data)
}

export function reviewDischargeApply(id: number, data: DischargeApplyReviewRequest) {
  return request.put<DischargeApplyItem>(`/api/elder/lifecycle/discharge-apply/${id}/review`, data)
}
