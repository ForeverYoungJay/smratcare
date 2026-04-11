import request, { fetchPage } from '../utils/request'
import type { Id, StaffItem, RoleItem, DepartmentItem, OrgItem, StaffRoleAssignRequest } from '../types'

export function getOrgPage(params: any) {
  return fetchPage<OrgItem>('/api/admin/orgs', params)
}

export function getDepartmentPage(params: any) {
  return fetchPage<DepartmentItem>('/api/admin/departments', params)
}

export function getDepartmentOptionPage(params: any) {
  return fetchPage<DepartmentItem>('/api/admin/departments/options', params)
}

export function getRolePage(params: any) {
  return fetchPage<RoleItem>('/api/admin/roles', params)
}

export function getStaffPage(params: any) {
  return fetchPage<StaffItem>('/api/admin/staff', params)
}

export function getStaffOptionPage(params: any) {
  return fetchPage<StaffItem>('/api/admin/staff/options', params)
}

export function createStaff(data: Partial<StaffItem>) {
  return request.post<void>('/api/admin/staff', data)
}

export function updateStaff(id: number, data: Partial<StaffItem>) {
  return request.put<void>('/api/admin/staff', { ...data, id })
}

export function updateStaffRoles(id: Id, roleIds: Id[]) {
  const body: StaffRoleAssignRequest = { staffId: id, roleIds }
  return request.post<void>('/api/admin/staff-roles/assign', body)
}

export function getStaffRoleAssignments(staffId: Id) {
  return request.get<Array<{ roleId?: Id }>>('/api/admin/staff-roles', { params: { staffId, orgId: 0 } })
}

export function appendStaffRole(id: Id, roleId: Id) {
  return request.post<void>('/api/admin/staff-roles/add', { staffId: id, roleId })
}

export function removeStaffRole(id: Id, roleId: Id) {
  return request.delete<void>('/api/admin/staff-roles/remove', { data: { staffId: id, roleId } })
}

export function createRole(data: Partial<RoleItem>) {
  return request.post<void>('/api/admin/roles', data)
}

export function updateRole(id: Id, data: Partial<RoleItem>) {
  return request.put<void>(`/api/admin/roles/${id}`, data)
}

export function deleteRole(id: Id) {
  return request.delete<void>(`/api/admin/roles/${id}`)
}

export function createDepartment(data: Partial<DepartmentItem>) {
  return request.post<void>('/api/admin/departments', data)
}

export function updateDepartment(id: number, data: Partial<DepartmentItem>) {
  return request.put<void>(`/api/admin/departments/${id}`, data)
}

export function deleteDepartment(id: number) {
  return request.delete<void>(`/api/admin/departments/${id}`)
}

export function createOrg(data: Partial<OrgItem>) {
  return request.post<void>('/api/admin/orgs', data)
}

export function updateOrg(id: number, data: Partial<OrgItem>) {
  return request.put<void>(`/api/admin/orgs/${id}`, data)
}

export function deleteOrg(id: number) {
  return request.delete<void>(`/api/admin/orgs/${id}`)
}
