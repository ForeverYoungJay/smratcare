import type { RouteRecordRaw } from 'vue-router'
import { routes } from '../router/routes'
import { canAccessPath } from '../utils/routeAccess'
import { getPageDescription, getPageAliases } from '../router/pageDescriptions'

export interface MenuItem {
  key: string
  label: string
  path?: string
  icon?: string
  roles?: string[]
  desc?: string
  aliases?: string[]
  children?: MenuItem[]
}

type MenuBuildOptions = {
  focused?: boolean
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
  return (
    routes
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
        const routeName = r.name ? String(r.name) : ''
        const node: MenuItem = {
          key: String(r.name || fullPath || r.path),
          label: String(r.meta?.title || r.name || r.path),
          icon: r.meta?.icon as string | undefined,
          roles: r.meta?.roles as string[] | undefined,
          desc: getPageDescription(routeName),
          aliases: getPageAliases(routeName)
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

export function isFocusedEmployeeRole(roles: string[]) {
  const normalized = roles.map((role) => String(role || '').toUpperCase())
  const hasManagementRole = normalized.some((role) => ['ADMIN', 'SYS_ADMIN', 'DIRECTOR'].includes(role))
  if (hasManagementRole) return false
  return normalized.some((role) => role.endsWith('_EMPLOYEE') || role.endsWith('_MINISTER') || role.includes('GUARD'))
}

function matchFocusedRoots(roles: string[]) {
  const normalized = roles.map((role) => String(role || '').toUpperCase())
  const roots = new Set<string>(['/portal', '/workbench'])
  if (normalized.some((role) => role.includes('NURSING') || role.includes('MEDICAL'))) {
    roots.add('/medical-care')
    roots.add('/elder')
  }
  if (normalized.some((role) => role.includes('LOGISTICS') || role.includes('GUARD'))) {
    roots.add('/logistics')
  }
  if (normalized.some((role) => role.includes('FINANCE'))) {
    roots.add('/finance')
  }
  if (normalized.some((role) => role.includes('MARKETING'))) {
    roots.add('/marketing')
  }
  if (normalized.some((role) => role.includes('HR'))) {
    roots.add('/hr')
  }
  return roots
}

const focusedChildPresets: Record<string, string[]> = {
  '/workbench': [
    '/workbench/overview',
    '/workbench/todo',
    '/workbench/attendance',
    '/workbench/approvals'
  ],
  '/elder': [
    '/elder/in-hospital-overview',
    '/elder/list',
    '/elder/assessment',
    '/elder/status-change'
  ],
  '/medical-care': [
    '/medical-care/care-task-board',
    '/medical-care/unified-task-center',
    '/medical-care/inspection',
    '/medical-care/medication-registration',
    '/medical-care/nursing-log',
    '/medical-care/handovers'
  ],
  '/marketing': [
    '/marketing/workbench',
    '/marketing/leads',
    '/marketing/interactions',
    '/marketing/contracts'
  ],
  '/finance': [
    '/finance/workbench',
    '/finance/payments',
    '/finance/bills',
    '/finance/reconcile'
  ],
  '/logistics': [
    '/logistics/workbench',
    '/logistics/task-center',
    '/logistics/assets/maintenance-record',
    '/logistics/dining/delivery-plan',
    '/logistics/storage/outbound'
  ],
  '/hr': [
    '/hr/overview',
    '/hr/staff',
    '/hr/scheduling',
    '/hr/profile'
  ]
}

function filterFocusedChildren(items: MenuItem[], parentPath?: string): MenuItem[] {
  const preset = parentPath ? focusedChildPresets[parentPath] || [] : []
  const nextItems = items
    .map((item) => {
      const nextChildren = item.children?.length ? filterFocusedChildren(item.children, item.path || item.key) : undefined
      return {
        ...item,
        children: nextChildren
      }
    })
    .filter((item) => item.path || item.children?.length)

  if (!preset.length) return nextItems
  const prioritized = nextItems.filter((item) =>
    preset.some((candidate) => item.path === candidate || candidate.startsWith(`${item.path || ''}/`))
  )
  if (prioritized.length >= 3) return prioritized
  const fallback = nextItems.filter((item) => !prioritized.includes(item)).slice(0, Math.max(0, 4 - prioritized.length))
  return [...prioritized, ...fallback]
}

function buildFocusedMenu(items: MenuItem[], roles: string[]) {
  const roots = matchFocusedRoots(roles)
  return items
    .filter((item) => item.path && roots.has(item.path))
    .map((item) => ({
      ...item,
      children: item.children?.length ? filterFocusedChildren(item.children, item.path) : item.children
    }))
}

export function getMenuTree(roles: string[], pagePermissions: string[] = [], options: MenuBuildOptions = {}) {
  const layout = routes.find((r) => r.path === '/' && r.children)
  const tree = buildMenu(layout?.children || [], roles, pagePermissions, '')
  if (options.focused && isFocusedEmployeeRole(roles)) {
    return buildFocusedMenu(tree, roles)
  }
  return tree
}
