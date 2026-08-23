import type { Router } from 'vue-router'
import { normalizeRoutePath, toCanonicalRoutePath } from '../router/routeAliases'
import { hasRouteAccess, moduleRolesByPath } from './roleAccess'

export interface RouteAccessResult {
  canAccess: boolean
  requiredRoles: string[]
  requiredPermissions: string[]
}

function normalizePath(path: string): string {
  return normalizeRoutePath(path)
}

export function hasExplicitPageAccess(pagePermissions: string[], path: string): boolean {
  const canonicalPath = toCanonicalRoutePath(path)
  return (pagePermissions || []).some((permissionPath) => {
    const canonicalPermission = toCanonicalRoutePath(permissionPath)
    return canonicalPath === canonicalPermission || canonicalPath.startsWith(`${canonicalPermission}/`)
  })
}

export function canAccessPath(roles: string[], required: string[], path: string, pagePermissions: string[] = []): boolean {
  if (normalizePath(path) === '/403') {
    return true
  }
  const inferredRequired = (required || []).length > 0 ? required : moduleRolesByPath(normalizePath(path))
  const hasRoleConstraint = inferredRequired.length > 0
  const routeAllowed = hasRoleConstraint && hasRouteAccess(roles, inferredRequired, path)
  if ((pagePermissions || []).length > 0) {
    const pageAllowed = hasExplicitPageAccess(pagePermissions, path)
    return pageAllowed && (!hasRoleConstraint || routeAllowed)
  }
  return routeAllowed
}

export function canAccessRoleGroups(roles: string[], roleGroups: string[][], path: string, pagePermissions: string[] = []): boolean {
  if (pagePermissions.length > 0 && !hasExplicitPageAccess(pagePermissions, path)) return false
  if (roleGroups.length > 0) return roleGroups.every((group) => hasRouteAccess(roles, group, path))
  return canAccessPath(roles, [], path, pagePermissions)
}

export function resolveRouteAccess(
  router: Router,
  roles: string[],
  path: string,
  pagePermissions: string[] = [],
  permissions: string[] = []
): RouteAccessResult {
  const resolved = router.resolve(path)
  if (!resolved.matched.length) {
    return { canAccess: false, requiredRoles: [], requiredPermissions: [] }
  }

  // 未知地址命中 NotFound 兜底路由时直接放行，让用户看到 404 而不是被页面权限校验误判为 403
  if (resolved.matched.length === 1 && resolved.matched[0].name === 'NotFound') {
    return { canAccess: true, requiredRoles: [], requiredPermissions: [] }
  }

  const roleGroups = resolved.matched
    .map((record) => ((record.meta?.roles as string[] | undefined) || []).filter(Boolean))
    .filter((group) => group.length > 0)
  const permissionGroups = resolved.matched
    .map((record) => (record.meta?.permissions || []).filter(Boolean))
    .filter((group) => group.length > 0)
  const requiredPermissions = Array.from(new Set(permissionGroups.flat()))
  const permissionsAllowed = permissionGroups.every((group) => group.some((permission) => permissions.includes(permission)))

  const targetPath = resolved.path || path
  const pageAllowed = pagePermissions.length === 0 || hasExplicitPageAccess(pagePermissions, targetPath)
  if (!pageAllowed) {
    return { canAccess: false, requiredRoles: Array.from(new Set(roleGroups.flat())), requiredPermissions }
  }

  if (roleGroups.length === 0) {
    const inferredRoles = moduleRolesByPath(resolved.path || path)
    return {
      canAccess: permissionsAllowed && (inferredRoles.length > 0
        ? hasRouteAccess(roles, inferredRoles, targetPath)
        : permissionGroups.length > 0 || (pagePermissions.length > 0 && pageAllowed)),
      requiredRoles: inferredRoles,
      requiredPermissions
    }
  }

  const requiredRoles = Array.from(new Set(roleGroups.flat()))
  const canAccess = permissionsAllowed && canAccessRoleGroups(roles, roleGroups, targetPath, pagePermissions)
  return { canAccess, requiredRoles, requiredPermissions }
}
