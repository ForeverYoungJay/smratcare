import request from '../utils/request'
import type { ApiResult, PageResult, DepartmentItem } from '../types/api'

export function getDepartmentPage(params: any) {
  return request.get<ApiResult<PageResult<DepartmentItem>>>('/api/admin/departments', { params })
}
