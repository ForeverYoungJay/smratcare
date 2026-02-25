import type { Router } from 'vue-router'

export interface RouteAccessResult {
  canAccess: boolean
  requiredRoles: string[]
}

export function resolveRouteAccess(router: Router, roles: string[], path: string): RouteAccessResult {
  const resolved = router.resolve(path)
  if (!resolved.matched.length) {
    return { canAccess: false, requiredRoles: [] }
  }

  const roleGroups = resolved.matched
    .map((record) => ((record.meta?.roles as string[] | undefined) || []).filter(Boolean))
    .filter((group) => group.length > 0)

  if (roleGroups.length === 0) {
    return { canAccess: true, requiredRoles: [] }
  }

  const canAccess = roleGroups.every((group) => group.some((role) => roles.includes(role)))
  const requiredRoles = Array.from(new Set(roleGroups.flat()))
  return { canAccess, requiredRoles }
}
