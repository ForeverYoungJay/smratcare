import type { Id } from './common'

export interface StaffItem {
  id: Id
  username: string
  realName: string
  status: number
  departmentId?: number
  orgId?: Id
  staffNo?: string
  phone?: string
  email?: string
  directLeaderId?: Id
  indirectLeaderId?: Id
  gender?: number
  password?: string
  passwordPlaintextSnapshot?: string
  passwordSnapshotUpdatedAt?: string
  roleCodes?: string[]
  roles?: RoleItem[]
}

export interface StaffCredentialItem {
  staffId: Id
  staffNo?: string
  username?: string
  realName?: string
  status?: number
  passwordPlaintextSnapshot?: string
  passwordSnapshotUpdatedAt?: string
}

export interface RoleItem {
  id: number
  roleName: string
  roleCode: string
  orgId?: Id
  departmentId?: Id
  superiorRoleId?: Id
  status?: number
  routePermissionsJson?: string
}

export interface DepartmentItem {
  id: number
  deptName: string
  sortNo?: number
  orgId?: Id
  status?: number
}

export interface OrgItem {
  id: number
  orgCode?: string
  orgName: string
  orgType?: string
  contactName?: string
  contactPhone?: string
  address?: string
  status?: number
}

export interface StaffRoleAssignRequest {
  staffId?: number
  roleIds: number[]
}
