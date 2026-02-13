import request, { fetchPage } from '../utils/request'
import type { BillItem, PayRequest } from '../types'

export function getBillPage(params: any) {
  return fetchPage<BillItem>('/api/bill/page', params)
}

export function getBillDetail(elderId: number, month: string) {
  return request.get<BillItem>(`/api/bill/${elderId}`, { params: { month } })
}

export function generateBill(month: string) {
  return request.post<void>('/api/bill/generate', null, { params: { month } })
}

export function payBill(billId: number, data: PayRequest) {
  return request.post<void>(`/api/bill/${billId}/pay`, data)
}
