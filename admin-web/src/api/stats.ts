import request from '../utils/request'
import { getToken } from '../utils/auth'
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
  keyword?: string
  orgId?: number
}) {
  const url = new URL('/api/stats/elder-flow-report/export', window.location.origin)
  Object.entries(params || {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      url.searchParams.set(key, String(value))
    }
  })
  const response = await fetch(url.toString(), {
    method: 'GET',
    headers: {
      Authorization: `Bearer ${getToken()}`
    }
  })
  if (!response.ok) {
    throw new Error('导出失败')
  }
  const blob = await response.blob()
  const contentDisposition = response.headers.get('content-disposition') || ''
  const filenameMatch = contentDisposition.match(/filename=\"?([^\";]+)\"?/)
  const filename = filenameMatch?.[1] || `elder-flow-report-${new Date().toISOString().slice(0, 10)}.csv`
  const link = document.createElement('a')
  const objectUrl = URL.createObjectURL(blob)
  link.href = objectUrl
  link.download = filename
  link.click()
  URL.revokeObjectURL(objectUrl)
}

export async function exportOrgMonthlyOperationCsv(params?: {
  from?: string
  to?: string
  months?: number
  orgId?: number
}) {
  const url = new URL('/api/stats/org/monthly-operation/export', window.location.origin)
  Object.entries(params || {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      url.searchParams.set(key, String(value))
    }
  })
  const response = await fetch(url.toString(), {
    method: 'GET',
    headers: {
      Authorization: `Bearer ${getToken()}`
    }
  })
  if (!response.ok) {
    throw new Error('导出失败')
  }
  const blob = await response.blob()
  const contentDisposition = response.headers.get('content-disposition') || ''
  const filenameMatch = contentDisposition.match(/filename=\"?([^\";]+)\"?/)
  const filename = filenameMatch?.[1] || `org-monthly-operation-${new Date().toISOString().slice(0, 10)}.csv`
  const link = document.createElement('a')
  const objectUrl = URL.createObjectURL(blob)
  link.href = objectUrl
  link.download = filename
  link.click()
  URL.revokeObjectURL(objectUrl)
}
