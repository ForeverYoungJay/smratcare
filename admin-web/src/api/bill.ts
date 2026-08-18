import request, { fetchPage } from '../utils/request'
import type { BillDeductionPreview, BillItem, BillPageQuery, Id, PayRequest } from '../types'

export function getBillPage(params: BillPageQuery) {
  return fetchPage<BillItem>('/api/bill/page', params)
}

export function getBillDetail(elderId: Id, month: string) {
  return request.get<BillItem>(`/api/bill/${elderId}`, { params: { month } })
}

export function generateBill(month: string) {
  return request.post<void>('/api/bill/generate', null, { params: { month } })
}

export function getBillDeductionPreview(billId: Id) {
  return request.get<BillDeductionPreview>(`/api/bill/${billId}/deduction-preview`)
}

export function payBill(billId: Id, data: PayRequest) {
  return request.post<void>(`/api/bill/${billId}/pay`, data)
}

export function invalidateBill(billId: Id) {
  return request.post<void>(`/api/bill/${billId}/invalidate`)
}
