import { describe, expect, it } from 'vitest'
import type { RouteRecordRaw } from 'vue-router'
import { createMemoryHistory, createRouter } from 'vue-router'
import { routes } from './routes'
import { redirectPreservingLocation, toCanonicalRoutePath } from './routeAliases'
import { getMenuTree } from '../layouts/menu'
import { getRecommendedPagePermissions } from '../utils/pageAccess'
import { resolveDefaultHome } from '../access/policy'
import { resolveRouteAccess } from '../utils/routeAccess'

function collectRoutes(routeList: RouteRecordRaw[], parent = ''): Array<{ path: string; name?: string; hidden: boolean }> {
  return routeList.flatMap((route) => {
    const path = route.path.startsWith('/') ? route.path : `${parent}/${route.path}`.replace(/\/+/g, '/')
    return [
      { path, name: route.name == null ? undefined : String(route.name), hidden: Boolean(route.meta?.hidden) },
      ...collectRoutes(route.children || [], path)
    ]
  })
}

function flattenMenu(items: ReturnType<typeof getMenuTree>): string[] {
  return items.flatMap((item) => [item.path || '', ...flattenMenu(item.children || [])]).filter(Boolean)
}

describe('route security foundation', () => {
  const router = createRouter({ history: createMemoryHistory(), routes })

  it('has unique route names and full paths', () => {
    const all = collectRoutes(routes)
    const names = all.map((item) => item.name).filter(Boolean) as string[]
    const paths = all.map((item) => item.path)
    expect(new Set(names).size).toBe(names.length)
    expect(new Set(paths).size).toBe(paths.length)
  })

  it('preserves query and hash when redirecting a legacy URL', () => {
    expect(redirectPreservingLocation('/workbench/todo')({ query: { status: 'OVERDUE' }, hash: '#mine' } as any))
      .toEqual({ path: '/workbench/todo', query: { status: 'OVERDUE' }, hash: '#mine' })
    expect(toCanonicalRoutePath('/oa/todo?status=OVERDUE#mine')).toBe('/workbench/todo')
  })

  it('does not expose hidden compatibility routes in menus', () => {
    const paths = flattenMenu(getMenuTree(['ADMIN'], getRecommendedPagePermissions('ADMIN')))
    expect(paths).not.toContain('/oa/todo')
    expect(paths).not.toContain('/medical-care/order-risk-overview')
  })

  it.each([
    ['NURSING_EMPLOYEE', '/medical-care', ['/finance', '/system']],
    ['FINANCE_MINISTER', '/finance', ['/medical-care', '/system']],
    ['HR_EMPLOYEE', '/hr', ['/finance', '/system']],
    ['MARKETING_MINISTER', '/marketing', ['/medical-care', '/system']],
    ['DIRECTOR', '/portal', ['/system', '/base-config']],
    ['SYS_ADMIN', '/system', ['/medical-care', '/finance']]
  ])('keeps %s navigation within its intended scope', (role, expectedRoot, forbiddenRoots) => {
    const paths = flattenMenu(getMenuTree([role], getRecommendedPagePermissions(role)))
    expect(paths.some((path) => path === expectedRoot || path.startsWith(`${expectedRoot}/`))).toBe(true)
    forbiddenRoots.forEach((root) => expect(paths.some((path) => path === root || path.startsWith(`${root}/`))).toBe(false))
  })

  it('uses distinct role-specific default homes', () => {
    expect(resolveDefaultHome(['NURSING_EMPLOYEE'])).toBe('/workbench/overview')
    expect(resolveDefaultHome(['DIRECTOR'])).toBe('/portal')
    expect(resolveDefaultHome(['SYS_ADMIN'])).toBe('/system')
  })

  it('does not let personal workbench presets imply the director portal', () => {
    expect(getRecommendedPagePermissions('NURSING_EMPLOYEE')).not.toContain('/portal')
    expect(getRecommendedPagePermissions('SYS_ADMIN')).not.toContain('/portal')
  })

  it('keeps legacy STAFF accounts on the personal-workbench minimum preset', () => {
    const permissions = getRecommendedPagePermissions('STAFF')
    expect(permissions).toContain('/workbench')
    expect(permissions).toContain('/workbench/todo')
    expect(permissions).not.toContain('/portal')
    expect(permissions).not.toContain('/system')
  })

  it('keeps employee, minister, director and system administration entry semantics separate', () => {
    expect(resolveRouteAccess(router, ['NURSING_EMPLOYEE'], '/workbench/approvals', getRecommendedPagePermissions('NURSING_EMPLOYEE')).canAccess).toBe(false)
    expect(resolveRouteAccess(router, ['NURSING_MINISTER'], '/workbench/approvals', getRecommendedPagePermissions('NURSING_MINISTER')).canAccess).toBe(true)
    expect(resolveRouteAccess(router, ['SYS_ADMIN'], '/system/role', getRecommendedPagePermissions('SYS_ADMIN')).canAccess).toBe(true)
    expect(resolveRouteAccess(router, ['ADMIN'], '/system/role', getRecommendedPagePermissions('ADMIN')).canAccess).toBe(false)
    expect(resolveRouteAccess(router, ['DIRECTOR'], '/stats/security-policy', getRecommendedPagePermissions('DIRECTOR')).canAccess).toBe(false)
    expect(resolveRouteAccess(router, ['DIRECTOR'], '/stats/org/monthly-operation', getRecommendedPagePermissions('DIRECTOR')).canAccess).toBe(true)
  })
})
