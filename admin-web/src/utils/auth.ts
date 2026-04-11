const TOKEN_KEY = 'zhiyangyun_token'
const ROLES_KEY = 'zhiyangyun_roles'
const PERMISSIONS_KEY = 'zhiyangyun_permissions'
const PAGE_PERMISSIONS_KEY = 'zhiyangyun_page_permissions'
const STAFF_INFO_KEY = 'zhiyangyun_staff_info'

export function normalizeRoles(roles: string[]): string[] {
  const normalized = new Set<string>()
  ;(roles || []).forEach((role) => {
    const code = String(role || '').trim().toUpperCase()
    if (code) normalized.add(code)
  })
  if (normalized.has('OPERATOR')) {
    normalized.add('MARKETING_EMPLOYEE')
  }
  if (normalized.has('MANAGER')) {
    normalized.add('MARKETING_MINISTER')
  }
  const hasDepartmentRole = Array.from(normalized).some(
    (code) => code.endsWith('_EMPLOYEE') || code.endsWith('_MINISTER')
  )
  if (hasDepartmentRole) {
    normalized.add('STAFF')
  }
  if (normalized.has('SYS_ADMIN') || normalized.has('DIRECTOR')) {
    normalized.add('ADMIN')
  }
  return Array.from(normalized)
}

export function getToken(): string {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function setToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
}

export function getRoles(): string[] {
  const raw = localStorage.getItem(ROLES_KEY)
  const roles = raw ? JSON.parse(raw) : []
  return normalizeRoles(roles)
}

export function setRoles(roles: string[]) {
  localStorage.setItem(ROLES_KEY, JSON.stringify(normalizeRoles(roles || [])))
}

export function clearRoles() {
  localStorage.removeItem(ROLES_KEY)
}

export function getPermissions(): string[] {
  const raw = localStorage.getItem(PERMISSIONS_KEY)
  return raw ? JSON.parse(raw) : []
}

export function setPermissions(permissions: string[]) {
  localStorage.setItem(PERMISSIONS_KEY, JSON.stringify(permissions || []))
}

export function clearPermissions() {
  localStorage.removeItem(PERMISSIONS_KEY)
}

export function getPagePermissions(): string[] {
  const raw = localStorage.getItem(PAGE_PERMISSIONS_KEY)
  return raw ? JSON.parse(raw) : []
}

export function setPagePermissions(pagePermissions: string[]) {
  localStorage.setItem(PAGE_PERMISSIONS_KEY, JSON.stringify(pagePermissions || []))
}

export function clearPagePermissions() {
  localStorage.removeItem(PAGE_PERMISSIONS_KEY)
}

export function getStaffInfo<T = any>(): T | null {
  const raw = localStorage.getItem(STAFF_INFO_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as T
  } catch {
    return null
  }
}

export function setStaffInfo(staffInfo: any) {
  localStorage.setItem(STAFF_INFO_KEY, JSON.stringify(staffInfo || null))
}

export function clearStaffInfo() {
  localStorage.removeItem(STAFF_INFO_KEY)
}
