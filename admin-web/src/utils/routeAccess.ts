import type { Router } from 'vue-router'
import { hasRouteAccess } from './roleAccess'

export interface RouteAccessResult {
  canAccess: boolean
  requiredRoles: string[]
}

function normalizePath(path: string): string {
  const base = String(path || '').split('?')[0].split('#')[0].trim()
  if (!base) return '/'
  const normalized = base.replace(/\/+/g, '/')
  if (normalized.length > 1 && normalized.endsWith('/')) {
    return normalized.slice(0, -1)
  }
  return normalized.startsWith('/') ? normalized : `/${normalized}`
}

export function hasExplicitPageAccess(pagePermissions: string[], path: string): boolean {
  const normalizedPath = normalizePath(path)
  return (pagePermissions || []).some((permissionPath) => {
    const normalizedPermission = normalizePath(permissionPath)
    return normalizedPath === normalizedPermission || normalizedPath.startsWith(`${normalizedPermission}/`)
  })
}

export function canAccessPath(roles: string[], required: string[], path: string, pagePermissions: string[] = []): boolean {
  if (normalizePath(path) === '/403') {
    return true
  }
  if ((pagePermissions || []).length > 0) {
    return hasExplicitPageAccess(pagePermissions, path)
  }
  return hasRouteAccess(roles, required, path)
}

export function resolveRouteAccess(router: Router, roles: string[], path: string, pagePermissions: string[] = []): RouteAccessResult {
  const resolved = router.resolve(path)
  if (!resolved.matched.length) {
    return { canAccess: false, requiredRoles: [] }
  }

  const roleGroups = resolved.matched
    .map((record) => ((record.meta?.roles as string[] | undefined) || []).filter(Boolean))
    .filter((group) => group.length > 0)

  if (roleGroups.length === 0) {
    return { canAccess: canAccessPath(roles, [], resolved.path || path, pagePermissions), requiredRoles: [] }
  }

  const requiredRoles = Array.from(new Set(roleGroups.flat()))
  const canAccess = canAccessPath(roles, requiredRoles, resolved.path || path, pagePermissions)
  return { canAccess, requiredRoles }
}
