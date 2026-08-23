import type { RouteRecordRaw } from 'vue-router'
import { routes } from '../router/routes'
import { canAccessRoleGroups, hasExplicitPageAccess } from '../utils/routeAccess'
import { getPageDescription, getPageAliases } from '../router/pageDescriptions'
import { buildJobNavigation, supportsJobNavigation } from './jobNavigation'

export interface MenuItem {
  key: string
  label: string
  path?: string
  icon?: string
  roles?: string[]
  desc?: string
  aliases?: string[]
  navSection?: 'entry' | 'care' | 'operations' | 'compliance' | 'support' | 'system'
  navOrder?: number
  navPinned?: boolean
  children?: MenuItem[]
}

type MenuBuildOptions = {
  focused?: boolean
  permissions?: string[]
}

function joinPath(base: string, path: string) {
  if (path.startsWith('/')) return path
  if (!base || base === '/') return `/${path}`
  return `${base}/${path}`.replace(/\/+/g, '/')
}

function buildMenu(
  routes: RouteRecordRaw[],
  roles: string[],
  pagePermissions: string[],
  permissions: string[],
  basePath = '',
  ancestorRoleGroups: string[][] = [],
  ancestorPermissionGroups: string[][] = []
): MenuItem[] {
  return (
    routes
      .filter((r) => !r.meta?.hidden)
      .map((r) => {
        const routePath = joinPath(basePath, r.path || '')
        const ownRoles = (r.meta?.roles || []).filter(Boolean)
        const roleGroups = ownRoles.length > 0 ? [...ancestorRoleGroups, ownRoles] : ancestorRoleGroups
        const ownPermissions = (r.meta?.permissions || []).filter(Boolean)
        const permissionGroups = ownPermissions.length > 0 ? [...ancestorPermissionGroups, ownPermissions] : ancestorPermissionGroups
        const permissionAllowed = permissionGroups.every((group) => group.some((code) => permissions.includes(code)))
        const pageAllowed = pagePermissions.length === 0 || hasExplicitPageAccess(pagePermissions, routePath)
        const roleAllowed = roleGroups.length > 0
          ? canAccessRoleGroups(roles, roleGroups, routePath, pagePermissions)
          : permissionGroups.length > 0 ? pageAllowed : canAccessRoleGroups(roles, roleGroups, routePath, pagePermissions)
        const selfAccessible = permissionAllowed && roleAllowed
        const children = r.children?.length
          ? buildMenu(r.children, roles, pagePermissions, permissions, routePath, roleGroups, permissionGroups)
          : []
        if (!selfAccessible && children.length === 0) {
          return null
        }
        const fullPath = r.children && children.length > 0 ? undefined : joinPath(basePath, r.path || '')
        const routeName = r.name ? String(r.name) : ''
        const node: MenuItem = {
          key: String(r.name || fullPath || r.path),
          label: String(r.meta?.title || r.name || r.path),
          icon: r.meta?.icon as string | undefined,
          roles: r.meta?.roles as string[] | undefined,
          desc: getPageDescription(routeName),
          aliases: getPageAliases(routeName),
          navSection: r.meta?.navSection as MenuItem['navSection'],
          navOrder: r.meta?.navOrder,
          navPinned: r.meta?.navPinned
        }
        if (children.length > 0) {
          if (selfAccessible) {
            node.path = routePath
          }
          node.children = children
        } else if (fullPath && selfAccessible) {
          node.path = fullPath
        }
        return node
      })
      .filter(Boolean)
      .filter((m) => !m?.children || m.children.length > 0)
  ) as MenuItem[]
}

export { supportsJobNavigation }

function collectMenuPaths(items: MenuItem[], result = new Set<string>()) {
  items.forEach((item) => {
    if (item.path) result.add(item.path)
    if (item.children?.length) collectMenuPaths(item.children, result)
  })
  return result
}

export function getMenuTree(roles: string[], pagePermissions: string[] = [], options: MenuBuildOptions = {}) {
  const layout = routes.find((r) => r.path === '/' && r.children)
  const tree = buildMenu(layout?.children || [], roles, pagePermissions, options.permissions || [], '')
  if (options.focused && supportsJobNavigation(roles)) {
    return buildJobNavigation(roles, collectMenuPaths(tree))
  }
  return tree
}
