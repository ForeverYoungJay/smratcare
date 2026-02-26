import request, { fetchPage } from '../utils/request'
import type { FireSafetyRecord, FireSafetyRecordQuery, FireSafetyReportSummary } from '../types'

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

export function getFireSafetySummary(params?: { dateFrom?: string; dateTo?: string }) {
  return request.get<FireSafetyReportSummary>('/api/fire/records/summary', { params })
}
