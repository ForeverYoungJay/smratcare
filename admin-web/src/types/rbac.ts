export interface StaffItem {
  id: number
  username: string
  realName: string
  status: number
  departmentId?: number
  orgId?: number
  staffNo?: string
  phone?: string
  email?: string
  gender?: number
  password?: string
  roles?: RoleItem[]
}

export interface RoleItem {
  id: number
  roleName: string
  roleCode: string
  roleDesc?: string
  orgId?: number
  status?: number
}

export interface DepartmentItem {
  id: number
  deptName: string
  deptCode?: string
  parentId?: number
  sortNo?: number
  orgId?: number
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
  roleIds: number[]
}
