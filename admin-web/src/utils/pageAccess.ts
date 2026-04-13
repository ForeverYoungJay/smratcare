import type { RouteRecordRaw } from 'vue-router'
import { routes } from '../router/routes'

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

const layout = routes.find((route) => route.path === LAYOUT_ROOT && Array.isArray(route.children))
const pagePermissionTree = buildTree((layout?.children || []) as RouteRecordRaw[])
const pagePermissionFlat: Array<{ path: string; title: string }> = []
flattenTree(pagePermissionTree, pagePermissionFlat)
const pageTitleMap = new Map(pagePermissionFlat.map((item) => [item.path, item.title]))
const knownPagePaths = new Set(pagePermissionFlat.map((item) => item.path))

const commonPersonalPaths = ['/portal', '/workbench', '/workbench/overview', '/workbench/todo', '/workbench/my-info', '/workbench/attendance', '/workbench/reports', '/workbench/approvals']
const adminManagementPaths = ['/elder', '/medical-care', '/care', '/finance', '/logistics', '/marketing', '/hr', '/oa', '/stats', '/system', '/base-config']

export const ROLE_PAGE_PRESETS: Record<string, RolePagePreset> = {
  NURSING_MINISTER: {
    label: '护理部长',
    description: '首页、医护健康服务、长者管理、护理审批、工作总结、基础数据配置',
    paths: [...commonPersonalPaths, '/medical-care', '/elder', '/oa/approval', '/base-config']
  },
  NURSING_EMPLOYEE: {
    label: '护理生活管家',
    description: '个人首页、我的信息、医护健康服务中心、我的待办和快捷发起',
    paths: [...commonPersonalPaths, '/medical-care', '/oa/approval']
  },
  HR_MINISTER: {
    label: '行政人事部部长',
    description: '工作台、人力资源、行政管理、统计分析、官网配置、角色管理、基础配置',
    paths: [...commonPersonalPaths, '/hr', '/oa', '/stats', '/system/site-config', '/system/role', '/base-config']
  },
  HR_EMPLOYEE: {
    label: '行政人事部员工',
    description: '工作台、行政协同、人资总览与社保提醒，不包含账号权限和系统设置',
    paths: [...commonPersonalPaths, '/hr/overview', '/hr/profile/social-security-reminders', '/oa']
  },
  LOGISTICS_MINISTER: {
    label: '后勤主管',
    description: '后勤保障和消防安全管理',
    paths: [...commonPersonalPaths, '/logistics', '/fire']
  },
  LOGISTICS_EMPLOYEE: {
    label: '后勤员工',
    description: '后勤保障日常工作',
    paths: [...commonPersonalPaths, '/logistics']
  },
  GUARD: {
    label: '消防员工',
    description: '仅消防安全管理',
    paths: [...commonPersonalPaths, '/fire']
  },
  MEDICAL_MINISTER: {
    label: '医务部长',
    description: '医护健康服务、长者管理、相关审批与工作总结',
    paths: [...commonPersonalPaths, '/medical-care', '/elder', '/oa/approval']
  },
  MEDICAL_EMPLOYEE: {
    label: '医务员工',
    description: '医护健康服务与长者相关工作',
    paths: [...commonPersonalPaths, '/medical-care', '/elder']
  },
  FINANCE_MINISTER: {
    label: '财务部长',
    description: '财务管理、费用审批、统计分析',
    paths: [...commonPersonalPaths, '/finance', '/oa/approval', '/stats']
  },
  FINANCE_EMPLOYEE: {
    label: '财务员工',
    description: '财务工作和相关审批',
    paths: [...commonPersonalPaths, '/finance', '/oa/approval']
  },
  MARKETING_MINISTER: {
    label: '营销部长',
    description: '营销中心、统计和工作总结',
    paths: [...commonPersonalPaths, '/marketing', '/stats']
  },
  MARKETING_EMPLOYEE: {
    label: '营销员工',
    description: '营销中心与个人工作台',
    paths: [...commonPersonalPaths, '/marketing']
  },
  ADMIN: {
    label: '管理员',
    description: '拥有全平台业务和系统设置权限',
    paths: [...commonPersonalPaths, ...adminManagementPaths]
  },
  SYS_ADMIN: {
    label: '系统超管',
    description: '最高权限，自动包含管理员全部页面和系统配置能力',
    paths: [...commonPersonalPaths, ...adminManagementPaths]
  },
  DIRECTOR: {
    label: '院长',
    description: '跨部门经营管理和全局查看权限',
    paths: [...commonPersonalPaths, ...adminManagementPaths]
  }
}

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
    if (!knownPagePaths.has(normalizedPath)) {
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

export function mergeRolePagePermissions(roleList: Array<{ routePermissionsJson?: string | null }>): string[] {
  return normalizePagePermissions(
    roleList.flatMap((item) => parseRoutePermissionsJson(item.routePermissionsJson))
  )
}
