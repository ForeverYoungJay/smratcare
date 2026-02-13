import request, { fetchPage } from '../utils/request'
import type { StaffItem } from '../types'

export function getStaffPage(params: any) {
  return fetchPage<StaffItem>('/api/admin/staff', params)
}

export function createStaff(data: Partial<StaffItem>) {
  return request.post<void>('/api/admin/staff', data)
}

export function updateStaff(id: number, data: Partial<StaffItem>) {
  return request.put<void>('/api/admin/staff', { ...data, id })
}

export function updateStaffRoles(id: number, roleIds: number[]) {
  return request.post<void>('/api/admin/staff-roles/assign', { staffId: id, roleIds })
}
