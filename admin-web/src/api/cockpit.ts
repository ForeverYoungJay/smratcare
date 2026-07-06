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

// ==================== 经营驾驶舱 BI（领域D：预聚合汇总表） ====================

export interface CockpitBiMetricCard {
  metricCode: string
  metricName: string
  unit: string | null
  value: number | null
  prevValue: number | null
  momRate: number | null
}

export interface CockpitBiSummaryResponse {
  orgId: Id
  month: string
  latestStatDate: string | null
  cards: CockpitBiMetricCard[]
}

export interface CockpitBiTrendItem {
  statDate: string
  occupancyRate: number | null
  residentCount: number | null
  admissions: number | null
  discharges: number | null
  revenueAmount: number | null
  paidDailyAmount: number | null
  collectionRate: number | null
  careTaskCompletionRate: number | null
  alertOnTimeRate: number | null
  satisfactionScore: number | null
}

export interface CockpitBiDistributionResponse {
  orgId: Id
  statDate: string | null
  careLevelDist: Record<string, number>
  costStructure: Record<string, number>
}

export interface CockpitBiOrgCompareItem {
  orgId: Id
  orgName: string
  occupancyRate: number | null
  residentCount: number | null
  revenueAmount: number | null
  collectionRate: number | null
  careTaskCompletionRate: number | null
  alertOnTimeRate: number | null
  satisfactionScore: number | null
}

export interface CockpitMetricDefinition {
  id: Id
  metricCode: string
  metricName: string
  metricCategory: string
  unit: string | null
  caliberDesc: string | null
  calcSqlSummary: string | null
  sourceTable: string | null
}

export function getCockpitBiSummary(params?: { orgId?: Id }) {
  return request.get<CockpitBiSummaryResponse>('/stats/executive/bi/summary', { params })
}

export function getCockpitBiTrend(params?: { orgId?: Id; days?: number }) {
  return request.get<CockpitBiTrendItem[]>('/stats/executive/bi/trend', { params })
}

export function getCockpitBiDistribution(params?: { orgId?: Id }) {
  return request.get<CockpitBiDistributionResponse>('/stats/executive/bi/distribution', { params })
}

export function getCockpitBiOrgCompare(params?: { month?: string }) {
  return request.get<CockpitBiOrgCompareItem[]>('/stats/executive/bi/org-compare', { params })
}

export function getCockpitMetricDefinitions() {
  return request.get<CockpitMetricDefinition[]>('/stats/executive/bi/metric-definitions')
}

export function recalcCockpitBiStats(params?: { date?: string; orgId?: Id }) {
  return request.post<number>('/stats/executive/bi/recalc', null, { params })
}
