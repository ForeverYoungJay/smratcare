import request from '../utils/request'
import type { Id } from '../types'

export interface FinanceRecentOperation {
  id: Id
  actorName?: string
  actionType?: string
  actionTypeText?: string
  entityType?: string
  detail?: string
  createTime?: string
}

export interface FinanceHomeSummary {
  billMonth: string
  residentCount: number
  bedTotal: number
  occupiedBedCount: number
  careFeeSettleRate: number
  billCount: number
  settledBillCount: number
  careFeeAmountRate: number
  billTotalAmount: number
  billOutstandingAmount: number
  depositShortfallCount: number
  depositShortfallAmount: number
  depositTotalBalance: number
  electricityUnpaidRoomCount: number
  electricityUnpaidFee: number
  electricityUnrecordedRoomCount: number
  recentOperations: FinanceRecentOperation[]
}

export function getFinanceHomeSummary(month?: string) {
  return request.get<FinanceHomeSummary>('/api/finance/home/summary', { params: { month } })
}
