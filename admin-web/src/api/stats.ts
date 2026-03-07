import request from '../utils/request'
import { exportCsvByRequest } from '../utils/export'
import type {
  BedUsageStatsResponse,
  CheckInStatsResponse,
  ConsumptionStatsResponse,
  ElderInfoStatsResponse,
  FlowReportPageResponse,
  MonthFlowItem,
  MonthlyRevenueStatsResponse,
  OrgMonthlyOperationItem
} from '../types'

export function getCheckInStats(params?: { from?: string; to?: string; months?: number; orgId?: number }) {
  return request.get<CheckInStatsResponse>('/api/stats/check-in', { params })
}

export function getConsumptionStats(params?: { from?: string; to?: string; months?: number; orgId?: number }) {
  return request.get<ConsumptionStatsResponse>('/api/stats/consumption', { params })
}

export function getElderInfoStats(params?: { orgId?: number }) {
  return request.get<ElderInfoStatsResponse>('/api/stats/elder-info', { params })
}

export function getOrgMonthlyOperation(params?: { from?: string; to?: string; months?: number; orgId?: number }) {
  return request.get<OrgMonthlyOperationItem[]>('/api/stats/org/monthly-operation', { params })
}

export function getOrgElderFlow(params?: { from?: string; to?: string; months?: number; orgId?: number }) {
  return request.get<MonthFlowItem[]>('/api/stats/org/elder-flow', { params })
}

export function getOrgBedUsage(params?: { orgId?: number }) {
  return request.get<BedUsageStatsResponse>('/api/stats/org/bed-usage', { params })
}

export function getMonthlyRevenueStats(params?: { from?: string; to?: string; months?: number; orgId?: number }) {
  return request.get<MonthlyRevenueStatsResponse>('/api/stats/monthly-revenue', { params })
}

export function getElderFlowReport(params?: {
  fromDate?: string
  toDate?: string
  eventType?: 'ADMISSION' | 'DISCHARGE'
  elderId?: number
  keyword?: string
  pageNo?: number
  pageSize?: number
  orgId?: number
}) {
  return request.get<FlowReportPageResponse>('/api/stats/elder-flow-report', { params })
}

export async function exportElderFlowReportCsv(params?: {
  fromDate?: string
  toDate?: string
  eventType?: 'ADMISSION' | 'DISCHARGE'
  elderId?: number
  keyword?: string
  orgId?: number
}) {
  await exportCsvByRequest('/api/stats/elder-flow-report/export', params, `elder-flow-report-${new Date().toISOString().slice(0, 10)}.csv`)
}

export async function exportOrgMonthlyOperationCsv(params?: {
  from?: string
  to?: string
  months?: number
  orgId?: number
}) {
  await exportCsvByRequest('/api/stats/org/monthly-operation/export', params, `org-monthly-operation-${new Date().toISOString().slice(0, 10)}.csv`)
}

export async function exportOrgElderFlowCsv(params?: {
  from?: string
  to?: string
  months?: number
  orgId?: number
}) {
  await exportCsvByRequest('/api/stats/org/elder-flow/export', params, `org-elder-flow-${new Date().toISOString().slice(0, 10)}.csv`)
}

async function exportStatsCsv(urlPath: string, fallbackFilename: string, params?: Record<string, any>) {
  await exportCsvByRequest(urlPath, params, fallbackFilename)
}

export function exportCheckInStatsCsv(params?: { from?: string; to?: string; months?: number; orgId?: number }) {
  return exportStatsCsv('/api/stats/check-in/export', `check-in-stats-${new Date().toISOString().slice(0, 10)}.csv`, params)
}

export function exportConsumptionStatsCsv(params?: { from?: string; to?: string; months?: number; orgId?: number }) {
  return exportStatsCsv('/api/stats/consumption/export', `consumption-stats-${new Date().toISOString().slice(0, 10)}.csv`, params)
}

export function exportElderInfoStatsCsv(params?: { orgId?: number }) {
  return exportStatsCsv('/api/stats/elder-info/export', `elder-info-stats-${new Date().toISOString().slice(0, 10)}.csv`, params)
}

export function exportOrgBedUsageCsv(params?: { orgId?: number }) {
  return exportStatsCsv('/api/stats/org/bed-usage/export', `org-bed-usage-${new Date().toISOString().slice(0, 10)}.csv`, params)
}

export function exportMonthlyRevenueStatsCsv(params?: { from?: string; to?: string; months?: number; orgId?: number }) {
  return exportStatsCsv('/api/stats/monthly-revenue/export', `monthly-revenue-stats-${new Date().toISOString().slice(0, 10)}.csv`, params)
}
