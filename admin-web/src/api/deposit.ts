import request, { fetchPage } from '../utils/request'
import type { Id } from '../types'

export interface DepositStandardItem {
  id: Id
  standardName: string
  careLevel?: string
  amount: number
  effectiveFrom: string
  remark?: string
}

export interface DepositAccountItem {
  id: Id
  elderId: Id
  elderName?: string
  careLevel?: string
  roomNo?: string
  bedNo?: string
  standardAmount: number
  paidAmount: number
  deductedAmount: number
  refundedAmount: number
  balanceAmount: number
  shortfallAmount: number
  status: string
  statusText?: string
  lastOpAt?: string
  remark?: string
}

export interface DepositTransactionItem {
  id: Id
  elderId: Id
  elderName?: string
  txnType: string
  txnTypeText?: string
  amount: number
  balanceAfter: number
  payMethod?: string
  occurredAt?: string
  reason?: string
  remark?: string
}

export interface DepositSummary {
  accountCount: number
  shortfallCount: number
  shortfallAmount: number
  totalBalance: number
  totalPaid: number
  totalDeducted: number
  totalRefunded: number
}

export interface DepositTransactionPayload {
  elderId: Id
  amount: number
  payMethod?: string
  occurredAt?: string
  reason?: string
  remark?: string
}

export function getDepositStandards() {
  return request.get<DepositStandardItem[]>('/api/finance/deposit/standards')
}

export function saveDepositStandard(data: {
  id?: Id
  standardName: string
  careLevel?: string
  amount: number
  effectiveFrom: string
  remark?: string
}) {
  return request.post<DepositStandardItem>('/api/finance/deposit/standards', data)
}

export function deleteDepositStandard(standardId: Id) {
  return request.delete<void>(`/api/finance/deposit/standards/${standardId}`)
}

export function getDepositPage(params: {
  pageNo: number
  pageSize: number
  status?: string
  keyword?: string
}) {
  return fetchPage<DepositAccountItem>('/api/finance/deposit/page', params)
}

export function getDepositSummary() {
  return request.get<DepositSummary>('/api/finance/deposit/summary')
}

export function getDepositTransactions(elderId: Id) {
  return request.get<DepositTransactionItem[]>(`/api/finance/deposit/${elderId}/transactions`)
}

export function refreshDepositStandard(elderId: Id) {
  return request.post<DepositAccountItem>(`/api/finance/deposit/${elderId}/refresh-standard`)
}

export function registerDepositPay(data: DepositTransactionPayload) {
  return request.post<DepositAccountItem>('/api/finance/deposit/pay', data)
}

export function registerDepositDeduct(data: DepositTransactionPayload) {
  return request.post<DepositAccountItem>('/api/finance/deposit/deduct', data)
}

export function registerDepositRefund(data: DepositTransactionPayload) {
  return request.post<DepositAccountItem>('/api/finance/deposit/refund', data)
}
