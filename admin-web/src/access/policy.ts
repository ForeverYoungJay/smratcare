export const ROLE_CODES = {
  ADMIN: 'ADMIN', SYS_ADMIN: 'SYS_ADMIN', DIRECTOR: 'DIRECTOR', STAFF: 'STAFF', GUARD: 'GUARD',
  NURSING_EMPLOYEE: 'NURSING_EMPLOYEE', NURSING_MINISTER: 'NURSING_MINISTER',
  MEDICAL_EMPLOYEE: 'MEDICAL_EMPLOYEE', MEDICAL_MINISTER: 'MEDICAL_MINISTER',
  FINANCE_EMPLOYEE: 'FINANCE_EMPLOYEE', FINANCE_MINISTER: 'FINANCE_MINISTER',
  LOGISTICS_EMPLOYEE: 'LOGISTICS_EMPLOYEE', LOGISTICS_MINISTER: 'LOGISTICS_MINISTER',
  HR_EMPLOYEE: 'HR_EMPLOYEE', HR_MINISTER: 'HR_MINISTER',
  MARKETING_EMPLOYEE: 'MARKETING_EMPLOYEE', MARKETING_MINISTER: 'MARKETING_MINISTER'
} as const

export type RoleCode = (typeof ROLE_CODES)[keyof typeof ROLE_CODES]

export const DEPARTMENT_CODES = {
  NURSING: 'NURSING', MEDICAL: 'MEDICAL', FINANCE: 'FINANCE',
  LOGISTICS: 'LOGISTICS', HR: 'HR', MARKETING: 'MARKETING'
} as const
export type DepartmentCode = (typeof DEPARTMENT_CODES)[keyof typeof DEPARTMENT_CODES]

export const DATA_SCOPES = { SELF: 'SELF', DEPARTMENT: 'DEPARTMENT', ORGANIZATION: 'ORGANIZATION', PLATFORM: 'PLATFORM' } as const
export type DataScope = (typeof DATA_SCOPES)[keyof typeof DATA_SCOPES]
export type ActionPermission = string
export type RouteAudience = 'EMPLOYEE' | 'MINISTER' | 'DIRECTOR' | 'BUSINESS_ADMIN' | 'SYSTEM_ADMIN'

export const DEPARTMENT_ROLE_MAP: Record<DepartmentCode, { employee: RoleCode; minister: RoleCode }> = {
  NURSING: { employee: ROLE_CODES.NURSING_EMPLOYEE, minister: ROLE_CODES.NURSING_MINISTER },
  MEDICAL: { employee: ROLE_CODES.MEDICAL_EMPLOYEE, minister: ROLE_CODES.MEDICAL_MINISTER },
  FINANCE: { employee: ROLE_CODES.FINANCE_EMPLOYEE, minister: ROLE_CODES.FINANCE_MINISTER },
  LOGISTICS: { employee: ROLE_CODES.LOGISTICS_EMPLOYEE, minister: ROLE_CODES.LOGISTICS_MINISTER },
  HR: { employee: ROLE_CODES.HR_EMPLOYEE, minister: ROLE_CODES.HR_MINISTER },
  MARKETING: { employee: ROLE_CODES.MARKETING_EMPLOYEE, minister: ROLE_CODES.MARKETING_MINISTER }
}
export const DEPARTMENT_EMPLOYEE_ROLES = Object.values(DEPARTMENT_ROLE_MAP).map((item) => item.employee)
export const DEPARTMENT_MINISTER_ROLES = Object.values(DEPARTMENT_ROLE_MAP).map((item) => item.minister)
export const DEPARTMENT_ALL_ROLES = [...DEPARTMENT_EMPLOYEE_ROLES, ...DEPARTMENT_MINISTER_ROLES]

/** Historic identity aliases only; none grants a management role outside its original department. */
export const LEGACY_ROLE_ALIASES: Readonly<Record<string, readonly RoleCode[]>> = {
  OPERATOR: [ROLE_CODES.MARKETING_EMPLOYEE],
  MANAGER: [ROLE_CODES.MARKETING_MINISTER]
}

// 管理员（业务管理员、系统管理员）登录后进运营中心（数字孪生大屏）；其他岗位先进「我的工作台」，部门业务从侧栏进入
export function resolveDefaultHome(roles: string[]): string {
  return roles.some((role) => role === ROLE_CODES.ADMIN || role === ROLE_CODES.SYS_ADMIN) ? '/digital-twin' : '/workbench/overview'
}
export const isSystemAdministrator = (roles: string[]) => roles.includes(ROLE_CODES.SYS_ADMIN)
export const isBusinessAdministrator = (roles: string[]) => roles.includes(ROLE_CODES.ADMIN)
export const isInstitutionDirector = (roles: string[]) => roles.includes(ROLE_CODES.DIRECTOR)
