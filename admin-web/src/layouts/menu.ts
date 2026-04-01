import type { RouteRecordRaw } from 'vue-router'
import { routes } from '../router/routes'
import { canAccessPath } from '../utils/routeAccess'

export interface MenuItem {
  key: string
  label: string
  path?: string
  icon?: string
  roles?: string[]
  children?: MenuItem[]
}

function joinPath(base: string, path: string) {
  if (path.startsWith('/')) return path
  if (!base || base === '/') return `/${path}`
  return `${base}/${path}`.replace(/\/+/g, '/')
}

function hasAccess(route: RouteRecordRaw, roles: string[], pagePermissions: string[], fullPath: string) {
  const required = (route.meta?.roles as string[] | undefined) || []
  return canAccessPath(roles, required, fullPath, pagePermissions)
}

function buildMenu(routes: RouteRecordRaw[], roles: string[], pagePermissions: string[], basePath = ''): MenuItem[] {
  return routes
    .filter((r) => !r.meta?.hidden)
    .map((r) => {
      const routePath = joinPath(basePath, r.path || '')
      const selfAccessible = hasAccess(r, roles, pagePermissions, routePath)
      const children = r.children?.length
        ? buildMenu(r.children, roles, pagePermissions, routePath)
        : []
      if (!selfAccessible && children.length === 0) {
        return null
      }
      const fullPath = r.children ? undefined : joinPath(basePath, r.path || '')
      const node: MenuItem = {
        key: String(r.name || fullPath || r.path),
        label: String(r.meta?.title || r.name || r.path),
        icon: r.meta?.icon as string | undefined,
        roles: r.meta?.roles as string[] | undefined
      }
      if (children.length > 0) {
        if (children.length > 0) node.children = children
      } else if (fullPath && selfAccessible) {
        node.path = fullPath
      }
      return node
    })
    .filter(Boolean)
    .filter((m) => !m.children || m.children.length > 0)
    as MenuItem[]
}

export function getMenuTree(roles: string[], pagePermissions: string[] = []) {
  const layout = routes.find((r) => r.path === '/' && r.children)
  return buildMenu(layout?.children || [], roles, pagePermissions, '')
}
