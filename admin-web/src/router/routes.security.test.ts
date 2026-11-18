import { describe, expect, it } from 'vitest'
import type { RouteRecordRaw } from 'vue-router'
import { createMemoryHistory, createRouter } from 'vue-router'
import { routes } from './routes'
import { redirectPreservingLocation, toCanonicalRoutePath } from './routeAliases'
import { getMenuTree } from '../layouts/menu'
import { getRecommendedPagePermissions } from '../utils/pageAccess'
import { resolveDefaultHome } from '../access/policy'
import { resolveRouteAccess } from '../utils/routeAccess'
import { resolveAuthorizedLoginTarget } from './permission'

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

  it('keeps the application route composition stable across domain modules', () => {
    const root = routes.find((route) => route.path === '/')
    expect(root?.children?.map((route) => route.path)).toEqual([
      'portal', 'function-map', 'workbench', 'dashboard', 'elder',
      'marketing', 'bed', 'material', 'store', 'inventory',
      'elder/admission-assessment', 'elder/discharge-settlement',
      'elder/admission', 'elder/outing', 'elder/visit-register',
      'elder/discharge-apply', 'elder/trial-stay', 'elder/medical-outing',
      'elder/death-register',
      'logistics', 'care', 'finance', 'health', 'medical-care', 'fire',
      'ltci', 'stats', 'ai', 'card', 'life', 'dining', 'assessment',
      'survey', 'oa', 'hr', 'base-config', 'system', 'demo'
    ])
  })

  it('preserves query and hash when redirecting a legacy URL', () => {
    expect(redirectPreservingLocation('/workbench/todo')({ query: { status: 'OVERDUE' }, hash: '#mine' } as any))
      .toEqual({ path: '/workbench/todo', query: { status: 'OVERDUE' }, hash: '#mine' })
    expect(toCanonicalRoutePath('/oa/todo?status=OVERDUE#mine')).toBe('/workbench/todo')
  })

  it.each([
    ['/logistics/maintenance/report', '/logistics/assets/maintenance-record', undefined],
    ['/logistics/maintenance/dispatch', '/logistics/assets/maintenance-record', 'OPEN'],
    ['/logistics/maintenance/progress', '/logistics/assets/maintenance-record', 'PROCESSING']
  ])('keeps the legacy maintenance entry %s on the unified work-order page', async (legacyPath, expectedPath, status) => {
    await router.push(legacyPath)
    await router.isReady()
    expect(router.currentRoute.value.path).toBe(expectedPath)
    expect(router.currentRoute.value.query.status).toBe(status)
  })

  it('keeps the logistics bed usage report inside the permitted logistics route', async () => {
    await router.push('/logistics/reports/bed-usage')
    await router.isReady()
    expect(router.currentRoute.value.path).toBe('/logistics/reports/bed-usage')
  })

  it('shows camera recordings only under fire safety and redirects old bookmarks', async () => {
    const paths = flattenMenu(getMenuTree(['LOGISTICS_MINISTER'], getRecommendedPagePermissions('LOGISTICS_MINISTER')))
    // 录像在「监控与控制室」主页面的标签里，旧页面路由仍可直达
    expect(paths).toContain('/fire/control-center')
    expect(paths).not.toContain('/logistics/video')
    await router.push('/logistics/video?cameraId=42#recordings')
    expect(router.currentRoute.value.path).toBe('/fire/video')
    expect(router.currentRoute.value.query.cameraId).toBe('42')
    expect(router.currentRoute.value.hash).toBe('#recordings')
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

  it('ignores a redirect left by a previous account when the new role cannot open it', () => {
    const permissions = getRecommendedPagePermissions('LOGISTICS_EMPLOYEE')
    expect(resolveAuthorizedLoginTarget(router, '/portal', ['LOGISTICS_EMPLOYEE'], permissions)).toBe('/workbench/overview')
    expect(resolveAuthorizedLoginTarget(router, '/logistics/workbench', ['LOGISTICS_EMPLOYEE'], permissions)).toBe('/logistics/workbench')
    expect(resolveAuthorizedLoginTarget(router, '//example.com', ['LOGISTICS_EMPLOYEE'], permissions)).toBe('/workbench/overview')
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

  it('keeps marketing reports and planning manager-only on direct navigation', () => {
    const employeePermissions = getRecommendedPagePermissions('MARKETING_EMPLOYEE')
    const ministerPermissions = getRecommendedPagePermissions('MARKETING_MINISTER')
    expect(resolveRouteAccess(router, ['MARKETING_EMPLOYEE'], '/marketing/reports/channel', employeePermissions).canAccess).toBe(false)
    expect(resolveRouteAccess(router, ['MARKETING_EMPLOYEE'], '/marketing/plan', employeePermissions).canAccess).toBe(false)
    expect(resolveRouteAccess(router, ['MARKETING_MINISTER'], '/marketing/reports/channel', ministerPermissions).canAccess).toBe(true)
  })

  it('keeps nursing plan management separate from employee plan reading', () => {
    const employeePermissions = getRecommendedPagePermissions('NURSING_EMPLOYEE')
    const ministerPermissions = getRecommendedPagePermissions('NURSING_MINISTER')
    expect(resolveRouteAccess(router, ['NURSING_EMPLOYEE'], '/care/workbench/plan', employeePermissions).canAccess).toBe(true)
    expect(resolveRouteAccess(router, ['NURSING_EMPLOYEE'], '/care/service/service-plans', employeePermissions).canAccess).toBe(false)
    expect(resolveRouteAccess(router, ['NURSING_MINISTER'], '/care/service/service-plans', ministerPermissions).canAccess).toBe(true)
  })

  it('keeps finance reminders out of non-finance job shells', () => {
    expect(resolveRouteAccess(router, ['LOGISTICS_MINISTER'], '/finance/reminder-center', getRecommendedPagePermissions('LOGISTICS_MINISTER')).canAccess).toBe(false)
    expect(resolveRouteAccess(router, ['FINANCE_EMPLOYEE'], '/finance/reminder-center', getRecommendedPagePermissions('FINANCE_EMPLOYEE')).canAccess).toBe(true)
  })
})
