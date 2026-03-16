import request from '../utils/request'
import { fetchPage } from '../utils/request'
import { exportCsvByRequest } from '../utils/export'
import type {
  Id,
  LogisticsEquipmentArchive,
  LogisticsEquipmentArchiveSummary,
  LogisticsMaintenanceTodoJobLog,
  LogisticsMaintenanceTodoJobLogOverview,
  LogisticsWorkbenchSummary,
  LogisticsWorkbenchSummaryQuery
} from '../types'

type LogisticsWorkbenchSummaryRequest = LogisticsWorkbenchSummaryQuery & { lite?: boolean }

export function getLogisticsWorkbenchSummary(params?: LogisticsWorkbenchSummaryRequest, config?: Record<string, any>) {
  return request.get<LogisticsWorkbenchSummary>('/api/logistics/workbench/summary', { params, ...(config || {}) })
}

export function getLogisticsEquipmentPage(params: any) {
  return fetchPage<LogisticsEquipmentArchive>('/api/logistics/equipment/page', params)
}

export function getLogisticsEquipmentSummary(params: any) {
  return request.get<LogisticsEquipmentArchiveSummary>('/api/logistics/equipment/summary', { params })
}

export function createLogisticsEquipment(data: Partial<LogisticsEquipmentArchive>) {
  return request.post<LogisticsEquipmentArchive>('/api/logistics/equipment', data)
}

export function updateLogisticsEquipment(id: Id, data: Partial<LogisticsEquipmentArchive>) {
  return request.put<LogisticsEquipmentArchive>(`/api/logistics/equipment/${id}`, data)
}

export function deleteLogisticsEquipment(id: Id) {
  return request.delete<void>(`/api/logistics/equipment/${id}`)
}

export function generateEquipmentMaintenance(id: Id) {
  return request.put(`/api/logistics/equipment/${id}/generate-maintenance`)
}

export function generateEquipmentMaintenanceTodos(days = 30) {
  return request.post('/api/logistics/equipment/generate-maintenance-todos', null, { params: { days } })
}

export function getMaintenanceTodoJobLogPage(params: any) {
  return fetchPage<LogisticsMaintenanceTodoJobLog>('/api/logistics/maintenance-todo-job-log/page', params)
}

export function rerunMaintenanceTodoJobLog(id: Id) {
  return request.post(`/api/logistics/maintenance-todo-job-log/${id}/rerun`)
}

export function rerunLatestFailedMaintenanceTodoJobLog() {
  return request.post('/api/logistics/maintenance-todo-job-log/rerun-latest-failed')
}

export function getMaintenanceTodoJobLogOverview(days = 7) {
  return request.get<LogisticsMaintenanceTodoJobLogOverview>('/api/logistics/maintenance-todo-job-log/overview', { params: { days } })
}

export function exportMaintenanceTodoJobLog(params: any) {
  return exportCsvByRequest('/api/logistics/maintenance-todo-job-log/export', params, '维保待办执行日志.csv')
}
