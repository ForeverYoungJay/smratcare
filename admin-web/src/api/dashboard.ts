import request from '../utils/request'

export interface DashboardSummary {
  careTasksToday: number
  abnormalTasksToday: number
  inventoryAlerts: number
  unpaidBills: number
}

export function getDashboardSummary() {
  return request.get<DashboardSummary>('/api/dashboard/summary')
}
