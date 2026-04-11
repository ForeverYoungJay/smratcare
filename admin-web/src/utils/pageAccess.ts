import type { RouteRecordRaw } from 'vue-router'
import { routes } from '../router/routes'
import { hasRouteAccess, moduleRolesByPath } from './roleAccess'

export interface PagePermissionNode {
  title: string
  key: string
  children?: PagePermissionNode[]
}

export interface RolePagePreset {
  label: string
  description: string
  paths: string[]
}

interface LeafPermissionRoute {
  path: string
  title: string
  requiredRoles: string[]
}

interface RolePagePresetConfig {
  label: string
  description: string
  prefixes?: string[]
  includePaths?: string[]
  excludePrefixes?: string[]
}

const LAYOUT_ROOT = '/'
const HIDDEN_PATHS = new Set(['/403'])
const LEGACY_PERMISSION_PATH_MAP: Record<string, string> = {
  '/oa/portal': '/workbench',
  '/oa/todo': '/workbench/todo',
  '/oa/my-info': '/workbench/my-info',
  '/oa/attendance-leave': '/workbench/attendance',
  '/oa/work-report': '/workbench/reports',
  '/hr/workbench': '/hr/overview',
  '/oa/activity': '/oa/activity-center/records',
  '/oa/activity-plan': '/oa/activity-center/plan',
  '/oa/survey/manage': '/oa/activity-center/survey-manage',
  '/oa/survey/stats': '/oa/activity-center/survey-stats',
  '/hr/points': '/hr/incentive/ledger',
  '/hr/points-rule': '/hr/incentive/rules',
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

function toCanonicalPermissionPath(path: string): string {
  const normalized = normalizePath(path)
  return LEGACY_PERMISSION_PATH_MAP[normalized] || normalized
}

function joinPath(base: string, path: string): string {
  if (!path) return normalizePath(base || '/')
  if (path.startsWith('/')) return normalizePath(path)
  if (!base || base === '/') return normalizePath(`/${path}`)
  return normalizePath(`${base}/${path}`)
}

function shouldIncludeRoute(route: RouteRecordRaw, fullPath: string) {
  if (route.meta?.hidden || !route.meta?.title) {
    return false
  }
  if (fullPath.includes('/:')) {
    return false
  }
  return !HIDDEN_PATHS.has(fullPath)
}

function buildTree(routeList: RouteRecordRaw[], basePath = ''): PagePermissionNode[] {
  return (routeList || [])
    .map((route) => {
      const fullPath = joinPath(basePath, String(route.path || ''))
      const children = Array.isArray(route.children) ? buildTree(route.children, fullPath) : []
      const includeSelf = shouldIncludeRoute(route, fullPath)
      if (!includeSelf && children.length === 0) {
        return null
      }
      return {
        title: String(route.meta?.title || route.name || fullPath),
        key: fullPath,
        children: children.length > 0 ? children : undefined
      }
    })
    .filter(Boolean) as PagePermissionNode[]
}

function flattenTree(nodes: PagePermissionNode[], bucket: Array<{ path: string; title: string }>) {
  nodes.forEach((node) => {
    bucket.push({ path: node.key, title: node.title })
    if (node.children?.length) {
      flattenTree(node.children, bucket)
    }
  })
}

function collectLeafRoutes(
  routeList: RouteRecordRaw[],
  bucket: LeafPermissionRoute[],
  basePath = '',
  inheritedRoles: string[] = []
) {
  ;(routeList || []).forEach((route) => {
    const fullPath = joinPath(basePath, String(route.path || ''))
    const ownRoles = ((route.meta?.roles as string[] | undefined) || []).filter(Boolean)
    const mergedRoles = Array.from(new Set([...inheritedRoles, ...ownRoles]))
    const visible = shouldIncludeRoute(route, fullPath)
    const children = Array.isArray(route.children) ? route.children : []
    if (children.length > 0) {
      collectLeafRoutes(children, bucket, fullPath, mergedRoles)
      return
    }
    if (!visible) {
      return
    }
    bucket.push({
      path: fullPath,
      title: String(route.meta?.title || route.name || fullPath),
      requiredRoles: mergedRoles
    })
  })
}

const layout = routes.find((route) => route.path === LAYOUT_ROOT && Array.isArray(route.children))
const pagePermissionTree = buildTree((layout?.children || []) as RouteRecordRaw[])
const pagePermissionFlat: Array<{ path: string; title: string }> = []
flattenTree(pagePermissionTree, pagePermissionFlat)
const pageTitleMap = new Map(pagePermissionFlat.map((item) => [item.path, item.title]))
const leafPermissionRoutes: LeafPermissionRoute[] = []
collectLeafRoutes((layout?.children || []) as RouteRecordRaw[], leafPermissionRoutes)

const commonPersonalPaths = [
  '/portal',
  '/workbench',
  '/workbench/overview',
  '/workbench/todo',
  '/workbench/my-info',
  '/workbench/attendance',
  '/workbench/reports',
  '/workbench/approvals'
]

const ROLE_PAGE_PRESET_CONFIGS: Record<string, RolePagePresetConfig> = {
  ADMIN: {
    label: '管理员',
    description: '全局管理、业务、统计与系统配置页面',
    includePaths: pagePermissionFlat.map((item) => item.path)
  },
  SYS_ADMIN: {
    label: '系统管理员',
    description: '全局管理、业务、统计与系统配置页面',
    includePaths: pagePermissionFlat.map((item) => item.path)
  },
  DIRECTOR: {
    label: '院长',
    description: '全局工作台、关键业务、统计分析与系统管理页面',
    includePaths: pagePermissionFlat.map((item) => item.path)
  },
  NURSING_MINISTER: {
    label: '护理部长',
    description: '首页、医护健康服务、长者管理、护理审批、工作总结、基础数据配置',
    prefixes: ['/medical-care', '/elder', '/oa/approval', '/base-config']
  },
  NURSING_EMPLOYEE: {
    label: '护理生活管家',
    description: '个人首页、我的信息、医护健康服务中心、我的待办和快捷发起',
    prefixes: ['/medical-care', '/oa/approval']
  },
  HR_MINISTER: {
    label: '行政人事部部长',
    description: '工作台、人力资源、行政管理、统计分析、官网配置、角色管理、权限总览、基础配置',
    prefixes: ['/hr', '/oa', '/stats', '/base-config'],
    includePaths: ['/system/site-config', '/system/role', '/system/permission-overview']
  },
  HR_EMPLOYEE: {
    label: '行政人事部员工',
    description: '工作台、人力资源、行政管理、统计分析，不包含角色和账号权限配置',
    prefixes: ['/hr', '/oa', '/stats', '/base-config']
  },
  LOGISTICS_MINISTER: {
    label: '后勤主管',
    description: '后勤保障和消防安全管理，不包含统计分析',
    prefixes: ['/logistics', '/fire']
  },
  LOGISTICS_EMPLOYEE: {
    label: '后勤员工',
    description: '后勤保障日常工作',
    prefixes: ['/logistics']
  },
  GUARD: {
    label: '消防员工',
    description: '仅消防安全管理',
    prefixes: ['/fire']
  },
  MEDICAL_MINISTER: {
    label: '医务部长',
    description: '医护健康服务、长者管理、相关审批与工作总结',
    prefixes: ['/medical-care', '/elder', '/oa/approval']
  },
  MEDICAL_EMPLOYEE: {
    label: '医务员工',
    description: '医护健康服务与长者相关工作',
    prefixes: ['/medical-care', '/elder']
  },
  FINANCE_MINISTER: {
    label: '财务部长',
    description: '财务管理、费用审批、统计分析',
    prefixes: ['/finance', '/oa/approval', '/stats']
  },
  FINANCE_EMPLOYEE: {
    label: '财务员工',
    description: '财务工作和相关审批',
    prefixes: ['/finance', '/oa/approval']
  },
  MARKETING_MINISTER: {
    label: '营销部长',
    description: '营销中心、统计分析和工作总结',
    prefixes: ['/marketing', '/stats']
  },
  MARKETING_EMPLOYEE: {
    label: '营销员工',
    description: '营销中心与个人工作台',
    prefixes: ['/marketing']
  },
  STAFF: {
    label: '员工',
    description: '个人工作台与通用待办页面',
    includePaths: commonPersonalPaths
  }
}

function roleCanAccessLeaf(roleCode: string, route: LeafPermissionRoute) {
  const requiredRoles = route.requiredRoles.length ? route.requiredRoles : moduleRolesByPath(route.path)
  if (!requiredRoles.length) {
    return false
  }
  return hasRouteAccess([roleCode], requiredRoles, route.path)
}

function collectLeafPathsForRole(roleCode: string, config: RolePagePresetConfig) {
  const prefixes = (config.prefixes || []).map((item) => normalizePath(item))
  const excludes = (config.excludePrefixes || []).map((item) => normalizePath(item))
  const scopedLeafPaths = leafPermissionRoutes
    .filter((route) => {
      if (!prefixes.length) {
        return true
      }
      return prefixes.some((prefix) => route.path === prefix || route.path.startsWith(`${prefix}/`))
    })
    .filter((route) => !excludes.some((prefix) => route.path === prefix || route.path.startsWith(`${prefix}/`)))
    .filter((route) => roleCanAccessLeaf(roleCode, route))
    .map((route) => route.path)
  return normalizePagePermissions([...(config.includePaths || []), ...scopedLeafPaths])
}

export const ROLE_PAGE_PRESETS: Record<string, RolePagePreset> = Object.fromEntries(
  Object.entries(ROLE_PAGE_PRESET_CONFIGS).map(([roleCode, config]) => [
    roleCode,
    {
      label: config.label,
      description: config.description,
      paths: collectLeafPathsForRole(roleCode, config)
    }
  ])
) as Record<string, RolePagePreset>

export function getPagePermissionTree(): PagePermissionNode[] {
  return pagePermissionTree
}

export function getPagePermissionOptions() {
  return pagePermissionFlat
}

export function getPageTitle(path: string): string {
  const canonicalPath = toCanonicalPermissionPath(path)
  return pageTitleMap.get(canonicalPath) || canonicalPath
}

export function normalizePagePermissions(paths: Array<string | null | undefined>): string[] {
  const normalized = new Set<string>()
  ;(paths || []).forEach((path) => {
    const normalizedPath = toCanonicalPermissionPath(String(path || ''))
    if (!normalizedPath || normalizedPath === '/' || HIDDEN_PATHS.has(normalizedPath)) {
      return
    }
    normalized.add(normalizedPath)
    if (normalizedPath === '/workbench' || normalizedPath === '/workbench/overview') {
      normalized.add('/portal')
    }
  })
  return Array.from(normalized)
}

export function parseRoutePermissionsJson(value?: string | null): string[] {
  if (!value) return []
  try {
    const parsed = JSON.parse(value)
    return Array.isArray(parsed) ? normalizePagePermissions(parsed as string[]) : []
  } catch {
    return []
  }
}

export function hasStoredPagePermissionsConfig(value?: string | null): boolean {
  if (value == null || String(value).trim() === '') {
    return false
  }
  try {
    return Array.isArray(JSON.parse(String(value)))
  } catch {
    return false
  }
}

export function serializeRoutePermissions(paths: Array<string | null | undefined>): string {
  return JSON.stringify(normalizePagePermissions(paths))
}

export function shouldPersistExplicitPagePermissions(
  roleCode: string | null | undefined,
  roleName: string | null | undefined,
  paths: Array<string | null | undefined>
): boolean {
  const normalizedPaths = normalizePagePermissions(paths)
  const recommendedPaths = getRecommendedPagePermissions(roleCode || roleName)
  if (normalizedPaths.length === 0) {
    return recommendedPaths.length > 0
  }
  if (normalizedPaths.length !== recommendedPaths.length) {
    return true
  }
  const normalizedSet = new Set(normalizedPaths)
  return recommendedPaths.some((path) => !normalizedSet.has(path))
}

export function getRolePagePreset(roleCode?: string | null): RolePagePreset | null {
  const code = String(roleCode || '').trim().toUpperCase()
  return ROLE_PAGE_PRESETS[code] || null
}

export function getRecommendedPagePermissions(roleCode?: string | null): string[] {
  return normalizePagePermissions(getRolePagePreset(roleCode)?.paths || [])
}

export function getRecommendedPagePermissionsByRoles(roleCodes: Array<string | null | undefined>): string[] {
  return normalizePagePermissions((roleCodes || []).flatMap((roleCode) => getRecommendedPagePermissions(roleCode)))
}

export function mergeRolePagePermissions(roleList: Array<{ routePermissionsJson?: string | null }>): string[] {
  return normalizePagePermissions(
    roleList.flatMap((item) => parseRoutePermissionsJson(item.routePermissionsJson))
  )
}
