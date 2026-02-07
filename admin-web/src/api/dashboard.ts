import request from '../utils/request'
import type { ApiResult, DashboardSummary } from '../types/api'

export function getDashboardSummary() {
  return request.get<ApiResult<DashboardSummary>>('/api/dashboard/summary')
}
