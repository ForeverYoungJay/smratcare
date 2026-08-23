import {
  DEPARTMENT_CODES,
  DEPARTMENT_ROLE_MAP,
  ROLE_CODES,
  type DepartmentCode,
  type RoleCode
} from '../access/policy'
import { normalizeRoles } from '../utils/auth'

export type WorkbenchAudience = 'EMPLOYEE' | 'MINISTER' | 'DIRECTOR' | 'BUSINESS_ADMIN' | 'SYSTEM_ADMIN' | 'STAFF'

export interface WorkbenchProfile {
  key: string
  audience: WorkbenchAudience
  department?: DepartmentCode
  departmentLabel: string
  roleLabel: string
  objectLabel: string
  primaryEntry: string
  hasManagementView: boolean
}

const DEPARTMENT_PROFILE: Record<DepartmentCode, Omit<WorkbenchProfile, 'key' | 'audience' | 'hasManagementView'>> = {
  NURSING: { department: DEPARTMENT_CODES.NURSING, departmentLabel: '护理部', roleLabel: '护理人员', objectLabel: '长者', primaryEntry: '/care/today' },
  MEDICAL: { department: DEPARTMENT_CODES.MEDICAL, departmentLabel: '医务部', roleLabel: '医务人员', objectLabel: '长者', primaryEntry: '/medical-care/workbench' },
  FINANCE: { department: DEPARTMENT_CODES.FINANCE, departmentLabel: '财务部', roleLabel: '财务人员', objectLabel: '长者账户', primaryEntry: '/finance/workbench' },
  LOGISTICS: { department: DEPARTMENT_CODES.LOGISTICS, departmentLabel: '后勤部', roleLabel: '后勤人员', objectLabel: '工单与设施', primaryEntry: '/logistics/workbench' },
  HR: { department: DEPARTMENT_CODES.HR, departmentLabel: '行政人事部', roleLabel: '行政人事人员', objectLabel: '员工', primaryEntry: '/hr/overview' },
  MARKETING: { department: DEPARTMENT_CODES.MARKETING, departmentLabel: '市场部', roleLabel: '市场人员', objectLabel: '客户', primaryEntry: '/marketing/workbench' }
}

const ROLE_PRIORITY: RoleCode[] = [
  ROLE_CODES.SYS_ADMIN,
  ROLE_CODES.DIRECTOR,
  ROLE_CODES.ADMIN,
  ...Object.values(DEPARTMENT_ROLE_MAP).map((item) => item.minister),
  ...Object.values(DEPARTMENT_ROLE_MAP).map((item) => item.employee)
]

function departmentProfile(role: RoleCode): WorkbenchProfile | undefined {
  for (const [department, rolePair] of Object.entries(DEPARTMENT_ROLE_MAP) as Array<[DepartmentCode, { employee: RoleCode; minister: RoleCode }]>) {
    if (role !== rolePair.employee && role !== rolePair.minister) continue
    const minister = role === rolePair.minister
    const base = DEPARTMENT_PROFILE[department]
    return {
      ...base,
      key: `${department.toLowerCase()}-${minister ? 'minister' : 'employee'}`,
      audience: minister ? 'MINISTER' : 'EMPLOYEE',
      roleLabel: minister ? `${base.departmentLabel}部长` : base.roleLabel,
      hasManagementView: minister
    }
  }
}

export function resolveWorkbenchProfile(inputRoles: string[]): WorkbenchProfile {
  const roles = new Set(normalizeRoles(inputRoles))
  const primaryRole = ROLE_PRIORITY.find((role) => roles.has(role))
  if (primaryRole) {
    const department = departmentProfile(primaryRole)
    if (department) return department
  }
  if (primaryRole === ROLE_CODES.SYS_ADMIN) {
    return { key: 'system-admin', audience: 'SYSTEM_ADMIN', departmentLabel: '系统管理', roleLabel: '系统管理员', objectLabel: '系统配置', primaryEntry: '/system', hasManagementView: true }
  }
  if (primaryRole === ROLE_CODES.DIRECTOR) {
    return { key: 'director', audience: 'DIRECTOR', departmentLabel: '机构经营', roleLabel: '机构负责人', objectLabel: '跨部门事项', primaryEntry: '/portal', hasManagementView: true }
  }
  if (primaryRole === ROLE_CODES.ADMIN) {
    return { key: 'business-admin', audience: 'BUSINESS_ADMIN', departmentLabel: '机构运营', roleLabel: '业务管理员', objectLabel: '运营事项', primaryEntry: '/portal', hasManagementView: true }
  }
  return { key: 'staff', audience: 'STAFF', departmentLabel: '个人工作', roleLabel: '工作人员', objectLabel: '工作事项', primaryEntry: '/workbench/overview', hasManagementView: false }
}

export function workbenchAudienceLabel(profile: WorkbenchProfile) {
  if (profile.audience === 'EMPLOYEE') return '员工工作台'
  if (profile.audience === 'MINISTER') return '部长工作台'
  if (profile.audience === 'DIRECTOR') return '负责人总览'
  if (profile.audience === 'SYSTEM_ADMIN') return '系统管理入口'
  if (profile.audience === 'BUSINESS_ADMIN') return '运营管理入口'
  return '个人工作台'
}
