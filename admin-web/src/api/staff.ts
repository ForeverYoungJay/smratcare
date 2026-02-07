import request from '../utils/request'
import type { ApiResult, PageResult, StaffItem } from '../types/api'

export function getStaffPage(params: any) {
  return request.get<ApiResult<PageResult<StaffItem>>>('/api/admin/staff', { params })
}

export function createStaff(data: Partial<StaffItem>) {
  return request.post<ApiResult<void>>('/api/admin/staff', data)
}

export function updateStaff(id: number, data: Partial<StaffItem>) {
  return request.put<ApiResult<void>>('/api/admin/staff', { ...data, id })
}

export function updateStaffRoles(id: number, roleIds: number[]) {
  return request.post<ApiResult<void>>('/api/admin/staff-roles/assign', { staffId: id, roleIds })
}

export function getStaffRoles(staffId: number) {
  return request.get<ApiResult<any[]>>('/api/admin/staff-roles', { params: { staffId, orgId: 0 } })
}
