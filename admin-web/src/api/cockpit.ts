import request from '../utils/request'
import type { Id } from '../types/common'

export interface CockpitTrendItem {
  statDate: string
  occupancyRate: number
  residentCount: number
  revenueAmount: number
  collectionRate: number
  alertRespRate: number
}

export interface CockpitOverviewResponse {
  statDate: string
  totalBeds: number
  occupiedBeds: number
  availableBeds: number
  occupancyRate: number
  residentCount: number
  admissions: number
  discharges: number
  netChange: number
  turnoverRate: number
  revenueAmount: number
  paidAmount: number
  outstandingAmount: number
  collectionRate: number
  staffCount: number
  residentPerStaff: number
  alertTotal: number
  alertResolved: number
  alertTimeout: number
  alertRespRate: number
  trend: CockpitTrendItem[]
}

export function getExecutiveOverview(params?: { orgId?: Id; trendDays?: number }) {
  return request.get<CockpitOverviewResponse>('/stats/executive/overview', { params })
}

export function refreshExecutiveSnapshot(params?: { orgId?: Id; trendDays?: number }) {
  return request.post<CockpitOverviewResponse>('/stats/executive/snapshot/refresh', null, { params })
}
