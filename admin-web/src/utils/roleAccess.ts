import { DEPARTMENT_ALL_ROLES, DEPARTMENT_EMPLOYEE_ROLES, DEPARTMENT_MINISTER_ROLES, ROLE_CODES } from '../access/policy'

/** @deprecated Never use this group as an authorization bypass. */
export const SUPER_ROLES = [ROLE_CODES.ADMIN, ROLE_CODES.SYS_ADMIN, ROLE_CODES.DIRECTOR]
export const LEGACY_MANAGER_ROLES = ['DEPT_LEADER', 'LEADER', 'MANAGER', 'SUPERVISOR']
export { DEPARTMENT_ALL_ROLES, DEPARTMENT_EMPLOYEE_ROLES, DEPARTMENT_MINISTER_ROLES }

const moduleRoleMap: Array<{ prefixes: string[]; employeeRoles: string[]; ministerRoles: string[] }> = [
  {
    prefixes: ['/medical-care', '/health', '/medical'],
    employeeRoles: ['MEDICAL_EMPLOYEE', 'NURSING_EMPLOYEE'],
    ministerRoles: ['MEDICAL_MINISTER', 'NURSING_MINISTER']
  },
  { prefixes: ['/care'], employeeRoles: ['NURSING_EMPLOYEE'], ministerRoles: ['NURSING_MINISTER'] },
  { prefixes: ['/finance'], employeeRoles: ['FINANCE_EMPLOYEE'], ministerRoles: ['FINANCE_MINISTER'] },
  {
    prefixes: ['/logistics', '/material', '/inventory', '/store', '/bed', '/dining'],
    employeeRoles: ['LOGISTICS_EMPLOYEE'],
    ministerRoles: ['LOGISTICS_MINISTER']
  },
  { prefixes: ['/marketing', '/crm'], employeeRoles: ['MARKETING_EMPLOYEE'], ministerRoles: ['MARKETING_MINISTER'] },
  { prefixes: ['/hr', '/oa', '/schedule', '/attendance'], employeeRoles: ['HR_EMPLOYEE'], ministerRoles: ['HR_MINISTER'] },
  {
    prefixes: ['/fire'],
    employeeRoles: ['GUARD', 'LOGISTICS_EMPLOYEE'],
    ministerRoles: ['LOGISTICS_MINISTER']
  },
  {
    prefixes: ['/stats'],
    employeeRoles: ['HR_EMPLOYEE', 'FINANCE_EMPLOYEE', 'LOGISTICS_EMPLOYEE', 'MARKETING_EMPLOYEE'],
    ministerRoles: ['HR_MINISTER', 'FINANCE_MINISTER', 'LOGISTICS_MINISTER', 'MARKETING_MINISTER']
  },
  {
    prefixes: ['/elder/assessment', '/assessment'],
    employeeRoles: ['MEDICAL_EMPLOYEE', 'NURSING_EMPLOYEE'],
    ministerRoles: ['MEDICAL_MINISTER', 'NURSING_MINISTER']
  },
  {
    prefixes: ['/survey'],
    employeeRoles: ['HR_EMPLOYEE', 'MEDICAL_EMPLOYEE', 'NURSING_EMPLOYEE'],
    ministerRoles: ['HR_MINISTER', 'MEDICAL_MINISTER', 'NURSING_MINISTER']
  }
]

export function hasAnyRole(userRoles: string[], required: string[]): boolean {
  if (!required || required.length === 0) return true
  return required.some((role) => userRoles.includes(role))
}

function moduleRoleGroupByPath(path: string): { employeeRoles: string[]; ministerRoles: string[] } | null {
  const normalizedPath = path || '/'
  for (const item of moduleRoleMap) {
    if (item.prefixes.some((prefix) => normalizedPath.startsWith(prefix))) {
      return { employeeRoles: item.employeeRoles, ministerRoles: item.ministerRoles }
    }
  }
  return null
}

export function moduleRolesByPath(path: string): string[] {
  const roleGroup = moduleRoleGroupByPath(path)
  if (!roleGroup) return []
  return [...roleGroup.employeeRoles, ...roleGroup.ministerRoles]
}

export function hasSuperRole(userRoles: string[]): boolean {
  return hasAnyRole(userRoles, SUPER_ROLES)
}

export function hasMinisterOrHigher(userRoles: string[]): boolean {
  return hasAnyRole(userRoles, [...SUPER_ROLES, ...LEGACY_MANAGER_ROLES, ...DEPARTMENT_MINISTER_ROLES])
}

export function hasStaffOrHigher(userRoles: string[]): boolean {
  return hasAnyRole(userRoles, ['STAFF', ...SUPER_ROLES, ...LEGACY_MANAGER_ROLES, ...DEPARTMENT_ALL_ROLES])
}

export function hasRouteAccess(userRoles: string[], required: string[], path: string): boolean {
  if (!required || required.length === 0) return false
  if (hasAnyRole(userRoles, required)) return true
  const moduleRoles = moduleRoleGroupByPath(path)

  if (required.includes('STAFF') && hasStaffOrHigher(userRoles)) {
    return true
  }

  if (required.includes('STAFF') && moduleRoles) {
    if (hasAnyRole(userRoles, [...moduleRoles.employeeRoles, ...moduleRoles.ministerRoles])) {
      return true
    }
  }

  if (required.includes('MANAGER') && hasAnyRole(userRoles, [...LEGACY_MANAGER_ROLES, ...DEPARTMENT_MINISTER_ROLES])) {
    return true
  }

  return false
}
