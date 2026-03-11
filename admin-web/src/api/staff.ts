import request, { fetchPage } from '../utils/request'
import type { Id, StaffItem } from '../types'

export function getStaffPage(params: any) {
  return fetchPage<StaffItem>('/api/admin/staff', params)
}

export function createStaff(data: Partial<StaffItem>) {
  return request.post<void>('/api/admin/staff', data)
}

export function updateStaff(id: Id, data: Partial<StaffItem>) {
  return request.put<void>('/api/admin/staff', { ...data, id })
}

export function updateStaffRoles(id: Id, roleIds: number[]) {
  return request.post<void>('/api/admin/staff-roles/assign', { staffId: id, roleIds })
}

export interface StaffSupervisorAnomalyItem {
  staffId: Id
  staffNo?: string
  staffName?: string
  departmentId?: number
  directLeaderId?: Id
  indirectLeaderId?: Id
  issue: string
}

export function getStaffSupervisorAnomalies() {
  return request.get<StaffSupervisorAnomalyItem[]>('/api/admin/staff/supervisor-anomalies')
}
