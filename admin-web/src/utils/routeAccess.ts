import type { Router } from 'vue-router'
import { hasRouteAccess, moduleRolesByPath } from './roleAccess'

export interface RouteAccessResult {
  canAccess: boolean
  requiredRoles: string[]
}

const routePathAliases: Record<string, string[]> = {
  '/workbench': ['/portal', '/workbench/overview'],
  '/workbench/overview': ['/workbench', '/portal'],
  '/portal': ['/workbench', '/workbench/overview'],
  '/workbench/todo': ['/oa/todo'],
  '/oa/todo': ['/workbench/todo'],
  '/workbench/my-info': ['/oa/my-info'],
  '/oa/my-info': ['/workbench/my-info'],
  '/workbench/attendance': ['/oa/attendance-leave'],
  '/oa/attendance-leave': ['/workbench/attendance'],
  '/workbench/reports': ['/oa/work-report'],
  '/oa/work-report': ['/workbench/reports'],
  '/workbench/approvals': ['/oa/approval'],
  '/oa/approval': ['/workbench/approvals'],
  '/hr/overview': ['/hr/workbench'],
  '/hr/workbench': ['/hr/overview'],
  '/oa/activity-center/records': ['/oa/activity'],
  '/oa/activity': ['/oa/activity-center/records'],
  '/oa/activity-center/plan': ['/oa/activity-plan'],
  '/oa/activity-plan': ['/oa/activity-center/plan'],
  '/oa/activity-center/survey-manage': ['/oa/survey/manage'],
  '/oa/survey/manage': ['/oa/activity-center/survey-manage'],
  '/oa/activity-center/survey-stats': ['/oa/survey/stats'],
  '/oa/survey/stats': ['/oa/activity-center/survey-stats'],
  '/hr/incentive/ledger': ['/hr/points'],
  '/hr/points': ['/hr/incentive/ledger'],
  '/hr/incentive/rules': ['/hr/points-rule'],
  '/hr/points-rule': ['/hr/incentive/rules'],
  '/system/site-config': ['/system/org-manage', '/system/org-manage/intro', '/system/org-manage/news', '/system/org-manage/life', '/system/app-version', '/system/message'],
  '/system/org-manage': ['/system/site-config'],
  '/system/org-manage/intro': ['/system/site-config'],
  '/system/org-manage/news': ['/system/site-config'],
  '/system/org-manage/life': ['/system/site-config'],
  '/system/app-version': ['/system/site-config'],
  '/system/message': ['/system/site-config'],
  '/base-config': ['/system/dict'],
  '/system/dict': ['/base-config']
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

function collectAcceptablePaths(path: string): Set<string> {
  const normalizedPath = normalizePath(path)
  const acceptablePaths = new Set<string>([normalizedPath])
  const queue = [normalizedPath]
  while (queue.length) {
    const current = queue.shift() as string
    ;(routePathAliases[current] || []).forEach((alias) => {
      const normalizedAlias = normalizePath(alias)
      if (acceptablePaths.has(normalizedAlias)) {
        return
      }
      acceptablePaths.add(normalizedAlias)
      queue.push(normalizedAlias)
    })
  }
  const siteConfigGroup = ['/system/site-config', '/system/role', '/system/permission-overview']
  if (siteConfigGroup.some((item) => acceptablePaths.has(item))) {
    siteConfigGroup.forEach((item) => acceptablePaths.add(item))
  }
  return acceptablePaths
}

export function hasExplicitPageAccess(pagePermissions: string[], path: string): boolean {
  const acceptablePaths = collectAcceptablePaths(path)
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
  const inferredRequired = (required || []).length > 0 ? required : moduleRolesByPath(normalizePath(path))
  const roleAllowed = hasRouteAccess(roles, inferredRequired, path)
  if (!roleAllowed) {
    return false
  }
  if ((pagePermissions || []).length > 0) {
    return hasExplicitPageAccess(pagePermissions, path)
  }
  return true
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
