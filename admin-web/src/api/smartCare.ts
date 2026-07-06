import request, { fetchPage } from '../utils/request'
import type {
  SmartAlert,
  SmartAlertDispatch,
  SmartAlertRule,
  SmartAlertSummary,
  SmartDevice,
  SmartDeviceEventRequest,
  SmartDeviceHealthSummary
} from '../types'

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

export function refreshDerivedSmartAlerts() {
  return request.post<SmartAlertSummary>('/api/smart-care/alerts/derived-health/refresh')
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

// ===== 场景规则引擎 =====
export function getSmartRulePage(params: {
  pageNo?: number
  pageSize?: number
  eventType?: string
  enabled?: number
}) {
  return fetchPage<SmartAlertRule>('/api/smart/rules/page', params)
}

export function createSmartRule(data: Partial<SmartAlertRule>) {
  return request.post<SmartAlertRule>('/api/smart/rules', data)
}

export function updateSmartRule(id: string | number, data: Partial<SmartAlertRule>) {
  return request.put<SmartAlertRule>(`/api/smart/rules/${id}`, data)
}

export function setSmartRuleEnabled(id: string | number, enabled: boolean) {
  return request.put(`/api/smart/rules/${id}/enabled`, null, { params: { enabled } })
}

export function deleteSmartRule(id: string | number) {
  return request.delete(`/api/smart/rules/${id}`)
}

// ===== 告警派单闭环 =====
export function getSmartDispatchPage(params: {
  pageNo?: number
  pageSize?: number
  status?: string
  level?: string
  assigneeId?: string | number
}) {
  return fetchPage<SmartAlertDispatch>('/api/smart/dispatch/page', params)
}

export function autoSmartDispatch() {
  return request.post<number>('/api/smart/dispatch/auto')
}

export function assignSmartDispatch(data: { dispatchId: string | number; assigneeId?: string | number; assigneeName?: string }) {
  return request.post<SmartAlertDispatch>('/api/smart/dispatch/assign', data)
}

export function respondSmartDispatch(id: string | number) {
  return request.post<SmartAlertDispatch>(`/api/smart/dispatch/${id}/respond`)
}

export function onsiteSmartDispatch(id: string | number) {
  return request.post<SmartAlertDispatch>(`/api/smart/dispatch/${id}/onsite`)
}

export function handleSmartDispatch(data: {
  dispatchId: string | number
  note?: string
  incidentId?: string | number
  createIncident?: boolean
  incidentType?: string
}) {
  return request.post<SmartAlertDispatch>('/api/smart/dispatch/handle', data)
}

export function reviewSmartDispatch(data: { dispatchId: string | number; note?: string }) {
  return request.post<SmartAlertDispatch>('/api/smart/dispatch/review', data)
}

// ===== 设备健康监控 =====
export function getSmartDeviceHealthSummary() {
  return request.get<SmartDeviceHealthSummary>('/api/smart/devices/health/summary')
}

export function getSmartDeviceHealthPage(params: {
  pageNo?: number
  pageSize?: number
  keyword?: string
  deviceType?: string
  onlineStatus?: string
  lowBatteryOnly?: boolean
  weakSignalOnly?: boolean
}) {
  return fetchPage<SmartDevice>('/api/smart/devices/health/page', params)
}
