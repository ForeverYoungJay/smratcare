import request from '../utils/request'
import { fetchPage } from '../utils/request'
import { exportCsvByRequest } from '../utils/export'
import type {
  LogisticsEquipmentArchive,
  LogisticsMaintenanceTodoJobLog,
  LogisticsMaintenanceTodoJobLogOverview,
  LogisticsWorkbenchSummary
} from '../types'

export function getLogisticsWorkbenchSummary() {
  return request.get<LogisticsWorkbenchSummary>('/api/logistics/workbench/summary')
}

export function getLogisticsEquipmentPage(params: any) {
  return fetchPage<LogisticsEquipmentArchive>('/api/logistics/equipment/page', params)
}

export function createLogisticsEquipment(data: Partial<LogisticsEquipmentArchive>) {
  return request.post<LogisticsEquipmentArchive>('/api/logistics/equipment', data)
}

export function updateLogisticsEquipment(id: number, data: Partial<LogisticsEquipmentArchive>) {
  return request.put<LogisticsEquipmentArchive>(`/api/logistics/equipment/${id}`, data)
}

export function deleteLogisticsEquipment(id: number) {
  return request.delete<void>(`/api/logistics/equipment/${id}`)
}

export function generateEquipmentMaintenance(id: number) {
  return request.put(`/api/logistics/equipment/${id}/generate-maintenance`)
}

export function generateEquipmentMaintenanceTodos(days = 30) {
  return request.post('/api/logistics/equipment/generate-maintenance-todos', null, { params: { days } })
}

export function getMaintenanceTodoJobLogPage(params: any) {
  return fetchPage<LogisticsMaintenanceTodoJobLog>('/api/logistics/maintenance-todo-job-log/page', params)
}

export function rerunMaintenanceTodoJobLog(id: number) {
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
