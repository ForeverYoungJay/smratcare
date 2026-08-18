import request, { fetchPage } from '../utils/request'
import type { FinanceVoucherItem, FinanceVoucherUsageItem, Id } from '../types'

export interface FinanceVoucherPageQuery {
  pageNo: number
  pageSize: number
  elderId?: Id
  status?: string
  keyword?: string
}

export interface FinanceVoucherIssuePayload {
  voucherName: string
  faceAmount: number
  minBillAmount?: number
  allowSplit?: boolean
  validFrom?: string
  validTo?: string
  source?: string
  remark?: string
  elderIds?: Id[]
}

export function getVoucherPage(params: FinanceVoucherPageQuery) {
  return fetchPage<FinanceVoucherItem>('/api/finance/voucher/page', params)
}

export function getUsableVouchers(params: { elderId?: Id; billAmount?: number; onDate?: string }) {
  return request.get<FinanceVoucherItem[]>('/api/finance/voucher/usable', { params })
}

export function getVoucherUsage(voucherId?: Id) {
  return request.get<FinanceVoucherUsageItem[]>('/api/finance/voucher/usage', { params: { voucherId } })
}

export function issueVouchers(data: FinanceVoucherIssuePayload) {
  return request.post<FinanceVoucherItem[]>('/api/finance/voucher/issue', data)
}

export function revokeVoucher(voucherId: Id, reason?: string) {
  return request.post<void>(`/api/finance/voucher/${voucherId}/revoke`, null, { params: { reason } })
}
