import request, { fetchPage } from '../utils/request'
import type { SmartAlert, SmartAlertSummary, SmartDevice, SmartDeviceEventRequest } from '../types'

export function getSmartDevicePage(params: {
  pageNo?: number
  pageSize?: number
  keyword?: string
  deviceType?: string
  onlineStatus?: string
}) {
  return fetchPage<SmartDevice>('/api/smart-care/devices/page', params)
}

export function createSmartDevice(data: Partial<SmartDevice>) {
  return request.post<SmartDevice>('/api/smart-care/devices', data)
}

export function updateSmartDevice(id: string | number, data: Partial<SmartDevice>) {
  return request.put<SmartDevice>(`/api/smart-care/devices/${id}`, data)
}

export function setSmartDeviceEnabled(id: string | number, enabled: boolean) {
  return request.put<SmartDevice>(`/api/smart-care/devices/${id}/enabled`, null, { params: { enabled } })
}

export function reportSmartDeviceEvent(data: SmartDeviceEventRequest) {
  return request.post('/api/smart-care/events', data)
}

export function getSmartAlertSummary() {
  return request.get<SmartAlertSummary>('/api/smart-care/alerts/summary')
}

export function getSmartAlertPage(params: {
  pageNo?: number
  pageSize?: number
  status?: string
  level?: string
  elderId?: string | number
  keyword?: string
}) {
  return fetchPage<SmartAlert>('/api/smart-care/alerts/page', params)
}

export function acknowledgeSmartAlert(id: string | number) {
  return request.put<SmartAlert>(`/api/smart-care/alerts/${id}/ack`)
}

export function resolveSmartAlert(id: string | number, data: { resolutionNote?: string; notifyFamily?: boolean }) {
  return request.put<SmartAlert>(`/api/smart-care/alerts/${id}/resolve`, data)
}
