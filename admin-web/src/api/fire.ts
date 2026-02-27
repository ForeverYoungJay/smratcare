import request, { fetchPage } from '../utils/request'
import { exportCsvByRequest } from '../utils/export'
import type {
  FireSafetyQrPayload,
  FireSafetyRecord,
  FireSafetyRecordQuery,
  FireSafetyRecordType,
  FireSafetyReportDetail,
  FireSafetyReportSummary
} from '../types'

export function getFireSafetyRecordPage(params: FireSafetyRecordQuery) {
  return fetchPage<FireSafetyRecord>('/api/fire/records/page', params)
}

export function createFireSafetyRecord(data: Partial<FireSafetyRecord>) {
  return request.post<FireSafetyRecord>('/api/fire/records', data)
}

export function updateFireSafetyRecord(id: number, data: Partial<FireSafetyRecord>) {
  return request.put<FireSafetyRecord>(`/api/fire/records/${id}`, data)
}

export function closeFireSafetyRecord(id: number) {
  return request.put<FireSafetyRecord>(`/api/fire/records/${id}/close`)
}

export function deleteFireSafetyRecord(id: number) {
  return request.delete<void>(`/api/fire/records/${id}`)
}

export function getFireSafetySummary(params?: { dateFrom?: string; dateTo?: string; recordType?: FireSafetyRecordType }) {
  return request.get<FireSafetyReportSummary>('/api/fire/records/summary', { params })
}

export function generateFireSafetyQr(id: number) {
  return request.post<FireSafetyQrPayload>(`/api/fire/records/${id}/qr/generate`)
}

export function completeFireSafetyByScan(data: { qrToken: string; inspectorName?: string; actionTaken?: string }) {
  return request.post<FireSafetyRecord>('/api/fire/records/scan/complete', data)
}

export function getFireSafetyReportDetail(params?: { dateFrom?: string; dateTo?: string; limit?: number }) {
  return request.get<FireSafetyReportDetail>('/api/fire/records/report/detail', { params })
}

export function exportFireSafetyReport(params?: { dateFrom?: string; dateTo?: string }) {
  return exportCsvByRequest('/api/fire/records/report/export', params, '消防巡查报表.csv')
}

export function exportFireSafetyMaintenanceLog(params?: {
  keyword?: string
  inspectorName?: string
  status?: string
  checkTimeStart?: string
  checkTimeEnd?: string
}) {
  return exportCsvByRequest('/api/fire/records/maintenance/export', params, '消防设施维护保养日志.csv')
}
