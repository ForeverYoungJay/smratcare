import type { RouteRecordRaw } from 'vue-router'
import { routes } from '../router/routes'

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

function hasAccess(route: RouteRecordRaw, roles: string[]) {
  const required = (route.meta?.roles as string[] | undefined) || []
  if (required.length === 0) return true
  return required.some((r) => roles.includes(r))
}

function buildMenu(routes: RouteRecordRaw[], roles: string[], basePath = ''): MenuItem[] {
  return routes
    .filter((r) => !r.meta?.hidden)
    .filter((r) => hasAccess(r, roles))
    .map((r) => {
      const fullPath = r.children ? undefined : joinPath(basePath, r.path || '')
      const node: MenuItem = {
        key: String(r.name || fullPath || r.path),
        label: String(r.meta?.title || r.name || r.path),
        icon: r.meta?.icon as string | undefined,
        roles: r.meta?.roles as string[] | undefined
      }
      if (r.children && r.children.length > 0) {
        const children = buildMenu(r.children, roles, joinPath(basePath, r.path || ''))
        if (children.length > 0) node.children = children
      } else if (fullPath) {
        node.path = fullPath
      }
      return node
    })
    .filter((m) => !m.children || m.children.length > 0)
}

export function getMenuTree(roles: string[]) {
  const layout = routes.find((r) => r.path === '/' && r.children)
  return buildMenu(layout?.children || [], roles, '')
}

