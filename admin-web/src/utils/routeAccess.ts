import type { Router } from 'vue-router'
import { hasRouteAccess, moduleRolesByPath } from './roleAccess'

export interface RouteAccessResult {
  canAccess: boolean
  requiredRoles: string[]
}

const legacyPagePathMap: Record<string, string> = {
  '/system/org-manage': '/system/site-config',
  '/system/org-manage/intro': '/system/site-config',
  '/system/org-manage/news': '/system/site-config',
  '/system/org-manage/life': '/system/site-config',
  '/system/app-version': '/system/site-config',
  '/system/message': '/system/site-config',
  '/system/dict': '/base-config'
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
  const effectivePath = legacyPagePathMap[normalizedPath] || normalizedPath
  const acceptablePaths = new Set([effectivePath])
  if (effectivePath === '/system/site-config') {
    acceptablePaths.add('/system/role')
    acceptablePaths.add('/system/permission-overview')
  }
  return (pagePermissions || []).some((permissionPath) => {
    const normalizedPermission = normalizePath(permissionPath)
    return Array.from(acceptablePaths).some(
      (candidatePath) => candidatePath === normalizedPermission || candidatePath.startsWith(`${normalizedPermission}/`)
    )
  })
}

export function canAccessPath(roles: string[], required: string[], path: string, pagePermissions: string[] = []): boolean {
  if (normalizePath(path) === '/403') {
    return true
  }
  if ((pagePermissions || []).length > 0) {
    return hasExplicitPageAccess(pagePermissions, path)
  }
  const inferredRequired = (required || []).length > 0 ? required : moduleRolesByPath(normalizePath(path))
  return hasRouteAccess(roles, inferredRequired, path)
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
    const inferredRoles = moduleRolesByPath(resolved.path || path)
    return {
      canAccess: canAccessPath(roles, inferredRoles, resolved.path || path, pagePermissions),
      requiredRoles: inferredRoles
    }
  }

  const requiredRoles = Array.from(new Set(roleGroups.flat()))
  const canAccess = canAccessPath(roles, requiredRoles, resolved.path || path, pagePermissions)
  return { canAccess, requiredRoles }
}
