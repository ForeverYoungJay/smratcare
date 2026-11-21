import { DEPARTMENT_ALL_ROLES, LEGACY_ROLE_ALIASES, ROLE_CODES } from '../access/policy'

const TOKEN_KEY = 'zhiyangyun_token'
const ROLES_KEY = 'zhiyangyun_roles'
const PERMISSIONS_KEY = 'zhiyangyun_permissions'
const PAGE_PERMISSIONS_KEY = 'zhiyangyun_page_permissions'
const STAFF_INFO_KEY = 'zhiyangyun_staff_info'

function safeStorage() {
  try {
    if (typeof window !== 'undefined' && window.localStorage) {
      return window.localStorage
    }
  } catch (_error) {
    return null
  }
  return null
}

export function normalizeRoles(roles: string[]): string[] {
  const normalized = new Set<string>()
  ;(roles || []).forEach((role) => {
    const code = String(role || '').trim().toUpperCase()
    if (code) normalized.add(code)
  })
  Object.entries(LEGACY_ROLE_ALIASES).forEach(([legacyRole, aliases]) => {
    if (normalized.has(legacyRole)) aliases.forEach((alias) => normalized.add(alias))
  })
  const hasDepartmentRole = DEPARTMENT_ALL_ROLES.some((code) => normalized.has(code))
  if (hasDepartmentRole) {
    normalized.add(ROLE_CODES.STAFF)
  }
  return Array.from(normalized)
}

export function getToken(): string {
  return safeStorage()?.getItem(TOKEN_KEY) || ''
}

export function setToken(token: string) {
  safeStorage()?.setItem(TOKEN_KEY, token)
}

export function clearToken() {
  safeStorage()?.removeItem(TOKEN_KEY)
}

export function getRoles(): string[] {
  const raw = safeStorage()?.getItem(ROLES_KEY)
  let roles: string[] = []
  try {
    roles = raw ? JSON.parse(raw) : []
  } catch (_error) {
    roles = []
  }
  return normalizeRoles(roles)
}

export function setRoles(roles: string[]) {
  safeStorage()?.setItem(ROLES_KEY, JSON.stringify(normalizeRoles(roles || [])))
}

export function clearRoles() {
  safeStorage()?.removeItem(ROLES_KEY)
}

export function getPermissions(): string[] {
  const raw = safeStorage()?.getItem(PERMISSIONS_KEY)
  try {
    return raw ? JSON.parse(raw) : []
  } catch (_error) {
    return []
  }
}

export function setPermissions(permissions: string[]) {
  safeStorage()?.setItem(PERMISSIONS_KEY, JSON.stringify(permissions || []))
}

export function clearPermissions() {
  safeStorage()?.removeItem(PERMISSIONS_KEY)
}

export function getPagePermissions(): string[] {
  const raw = safeStorage()?.getItem(PAGE_PERMISSIONS_KEY)
  try {
    return raw ? JSON.parse(raw) : []
  } catch (_error) {
    return []
  }
}

export function setPagePermissions(pagePermissions: string[]) {
  safeStorage()?.setItem(PAGE_PERMISSIONS_KEY, JSON.stringify(pagePermissions || []))
}

export function clearPagePermissions() {
  safeStorage()?.removeItem(PAGE_PERMISSIONS_KEY)
}

export function getStaffInfo<T = any>(): T | null {
  const raw = safeStorage()?.getItem(STAFF_INFO_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as T
  } catch {
    return null
  }
}

export function setStaffInfo(staffInfo: any) {
  safeStorage()?.setItem(STAFF_INFO_KEY, JSON.stringify(staffInfo || null))
}

export function clearStaffInfo() {
  safeStorage()?.removeItem(STAFF_INFO_KEY)
}
