import type { RouteRecordRaw } from 'vue-router'
import { routes } from '../router/routes'
import rolePagePresets from '../../../src/main/resources/security/role-page-presets.json'
import { toCanonicalRoutePath } from '../router/routeAliases'

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
  return toCanonicalRoutePath(path)
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
      if (route.meta?.hidden || route.meta?.legacy) return null
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

export const ROLE_PAGE_PRESETS: Record<string, RolePagePreset> = rolePagePresets

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
