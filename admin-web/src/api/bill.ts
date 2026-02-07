import request from '../utils/request'
import type { ApiResult, PageResult, BillItem } from '../types/api'

export function getBillPage(params: any) {
  return request.get<ApiResult<PageResult<BillItem>>>('/api/bill/page', { params })
}

export function getBillDetail(elderId: number, month: string) {
  return request.get<ApiResult<BillItem>>(`/api/bill/${elderId}`, { params: { month } })
}

export function payBill(billId: number, data: any) {
  return request.post<ApiResult<void>>(`/api/bill/${billId}/pay`, data)
}
