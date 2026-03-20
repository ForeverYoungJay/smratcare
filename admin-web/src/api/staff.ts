import request, { fetchPage } from '../utils/request'
import type { Id, StaffItem } from '../types'

export function getStaffPage(params: any) {
  return fetchPage<StaffItem>('/api/admin/staff', params)
}

export function createStaff(data: Partial<StaffItem>) {
  return request.post<StaffItem>('/api/admin/staff', data)
}

export function updateStaff(id: Id, data: Partial<StaffItem>) {
  return request.put<StaffItem>('/api/admin/staff', { ...data, id })
}

export function getStaff(id: Id) {
  return request.get<StaffItem>(`/api/admin/staff/${id}`)
}

export function lockStaff(id: Id) {
  return request.put<StaffItem>(`/api/admin/staff/${id}/lock`)
}

export function unlockStaff(id: Id) {
  return request.put<StaffItem>(`/api/admin/staff/${id}/unlock`)
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
