import request from '../utils/request'
import type { ApiResult, PageResult, RoleItem } from '../types/api'

export function getRolePage(params: any) {
  return request.get<ApiResult<PageResult<RoleItem>>>('/api/admin/roles', { params })
}
