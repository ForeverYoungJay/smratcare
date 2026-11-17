import { beforeEach, describe, expect, it } from 'vitest'
import { createMemoryHistory, createRouter, type RouteRecordRaw } from 'vue-router'
import { financeRoutes } from '../router/financeRoutes'
import { marketingRoutes } from '../router/marketingRoutes'
import { hrRoutes } from '../router/hrRoutes'
import { routes } from '../router/routes'
import { canUseWorkspaceTab, functionWorkspaces, workspaceLocation } from './catalog'
import { normalizePagePermissions, getPagePermissionOptions, getRecommendedPagePermissions } from '../utils/pageAccess'
import { resolveRouteAccess } from '../utils/routeAccess'
import { resolveDefaultHome } from '../access/policy'
import { getMenuTree, type MenuItem } from '../layouts/menu'
import { setupPermission, resolveAuthorizedLoginTarget } from '../router/permission'
import { setPagePermissions, setRoles, setToken } from '../utils/auth'

function stubPages(list: RouteRecordRaw[]): RouteRecordRaw[] {
  return list.map((route) => ({ ...route, component: route.component ? { render: () => null } : undefined,
    children: route.children ? stubPages(route.children) : undefined } as RouteRecordRaw))
}
const makeRouter = () => createRouter({ history: createMemoryHistory(), routes: stubPages(routes) })

describe('one page per function', () => {
  beforeEach(() => localStorage.clear())

  it('covers every original department page, retaining only workbenches and marketing plans as separate functions', () => {
    const sources = new Set(functionWorkspaces.flatMap((w) => w.tabs.flatMap((t) => t.sources.map((s) => s.path))))
    const retained = new Set(['/finance/workbench', '/marketing/workbench', '/marketing/customers', '/marketing/visits', '/marketing/contracts', '/marketing/operations', '/hr/overview'])
    function verify(list: RouteRecordRaw[], parent = '', hidden = false) {
      for (const route of list) {
        const path = (route.path.startsWith('/') ? route.path : `${parent}/${route.path}`).replace(/\/+/g, '/').replace(/\/$/, '')
        const invisible = hidden || Boolean(route.meta?.hidden)
        if (!invisible && route.component) expect(sources.has(path) || retained.has(path), path).toBe(true)
        if (route.children) verify(route.children, path, invisible)
      }
    }
    verify([...financeRoutes, ...marketingRoutes, ...hrRoutes])
  })

  it('retains explicit personal-report access without requiring a department role', () => {
    const workspace = functionWorkspaces.find(w => w.id === 'work-reports')!
    const tab = workspace.tabs.find(t => t.path === '/workbench/reports')!
    expect(canUseWorkspaceTab(tab, ['STAFF'], ['/workbench/reports'], [])).toBe(true)
    expect(canUseWorkspaceTab(tab, ['STAFF'], [], [])).toBe(false)
    expect(normalizePagePermissions(['/hr/development/plans'])).toEqual(['/hr/development/plans'])
  })

  it('keeps old feature names searchable without exposing unauthorized tabs', () => {
    const menu = getMenuTree(['HR_EMPLOYEE'], ['/hr/profile/social-security-reminders'])
    const flatten = (items: MenuItem[]): MenuItem[] => items.flatMap(item => [item, ...flatten(item.children || [])])
    const profile = flatten(menu).find(item => item.path === '/hr/profile/basic')!
    expect(profile.aliases).toContain('社保办理')
    expect(profile.aliases).not.toContain('劳动合同')
    const finance = flatten(getMenuTree(['FINANCE_MINISTER'], ['/finance']))
    expect(finance.find(item => item.path === '/finance/bills/detail-query')?.aliases).toContain('押金')
  })

  it('keeps custom narrow grants usable when the new department homepage is not authorized', async () => {
    const router = makeRouter()
    expect(resolveAuthorizedLoginTarget(router, '', ['FINANCE_EMPLOYEE'], ['/workbench', '/finance/payments/register'])).toBe('/workbench/overview')
    expect(resolveAuthorizedLoginTarget(router, '', ['FINANCE_EMPLOYEE'], ['/finance/payments/register'])).toBe('/finance/bills/detail-query')
    setToken('local-test-token'); setRoles(['FINANCE_EMPLOYEE']); setPagePermissions(['/workbench', '/finance/payments/register'])
    setupPermission(router)
    await router.push('/finance/workbench')
    expect(router.currentRoute.value.path).toBe('/workbench/overview')
  })

  it('registers every source exactly once and preserves source permissions', () => {
    const paths = functionWorkspaces.flatMap((w) => w.tabs.flatMap((t) => t.sources.map((s) => s.path)))
    expect(new Set(paths).size).toBe(paths.length)
    expect(functionWorkspaces.every((w) => w.tabs.length > 0)).toBe(true)
    expect(normalizePagePermissions(paths).length).toBe(paths.length)
    const options = getPagePermissionOptions().map((option) => option.path)
    for (const path of paths) expect(options).toContain(path)
  })

  it.each(functionWorkspaces.flatMap((workspace) => workspace.tabs.flatMap((tab) => tab.sources.map((source) => ({
    path: source.path, target: workspace.path, key: tab.key
  })))))('opens $path in its single function page without losing context', async ({ path, target, key }) => {
    const router = makeRouter()
    const query = { staffId: '88', elderId: '42', month: '2026-11' }
    const location = workspaceLocation(path, query, '#record')!
    expect(location).toEqual({ path: target, query: { ...query, view: key }, hash: '#record' })
    await router.push({ path, query: { ...query, view: key }, hash: '#record' })
    expect(router.currentRoute.value.path).toBe(target)
    expect(router.currentRoute.value.query).toEqual({ ...query, view: key })
    expect(router.currentRoute.value.hash).toBe('#record')
  })

  it('does not grant HR staff access to personnel/contract tabs through social-security access', () => {
    const router = makeRouter()
    const roles = ['HR_EMPLOYEE']
    const pages = ['/hr/profile/social-security-reminders']
    const allowed = workspaceLocation('/hr/profile/social-security-reminders')!
    const forbidden = workspaceLocation('/hr/profile/contracts')!
    expect(resolveRouteAccess(router, roles, router.resolve(allowed).fullPath, pages).canAccess).toBe(true)
    expect(resolveRouteAccess(router, roles, router.resolve(forbidden).fullPath, pages).canAccess).toBe(false)
    const workspace = functionWorkspaces.find((w) => w.id === 'hr-profile')!
    expect(workspace.tabs.filter((t) => canUseWorkspaceTab(t, roles, pages, [])).map((t) => t.path)).toEqual(pages)
  })

  it('keeps role management and security policy system-admin-only inside shared pages', () => {
    const router = makeRouter()
    for (const path of ['/system/role', '/stats/security-policy']) {
      const location = router.resolve(workspaceLocation(path)!).fullPath
      expect(resolveRouteAccess(router, ['ADMIN'], location, []).canAccess).toBe(false)
      expect(resolveRouteAccess(router, ['SYS_ADMIN'], location, getRecommendedPagePermissions('SYS_ADMIN')).canAccess).toBe(true)
    }
  })

  it('preserves exact grants on equivalent aliases instead of replacing them with a broad preset', () => {
    const pages = normalizePagePermissions(['/finance/payments/register'])
    expect(pages).toEqual(['/finance/payments/register'])
    const location = workspaceLocation('/finance/payments/register')!
    const router = makeRouter()
    expect(resolveRouteAccess(router, ['FINANCE_EMPLOYEE'], router.resolve(location).fullPath, pages).canAccess).toBe(true)
    const admission = workspaceLocation('/finance/bills/in-admission')!
    expect(resolveRouteAccess(router, ['FINANCE_EMPLOYEE'], router.resolve(admission).fullPath, pages).canAccess).toBe(false)
  })

  it('rejects forged view names and unauthorized query-only tab navigation in the real guard', async () => {
    const router = makeRouter()
    setToken('local-test-token')
    setRoles(['HR_EMPLOYEE'])
    setPagePermissions(['/hr/profile/social-security-reminders'])
    setupPermission(router)
    await router.push(workspaceLocation('/hr/profile/social-security-reminders')!)
    expect(router.currentRoute.value.path).toBe('/hr/profile/basic')
    await router.push(workspaceLocation('/hr/profile/account-access')!)
    expect(router.currentRoute.value.path).toBe('/403')
    expect(resolveRouteAccess(router, ['HR_EMPLOYEE'], '/hr/profile/basic?view=unknown', ['/hr/profile/social-security-reminders']).canAccess).toBe(false)
  })

  it('keeps report route identity and scene props for genuinely different views', () => {
    const reports = functionWorkspaces.find((w) => w.id === 'finance-reports')!
    expect(reports.tabs.map((t) => t.key)).toContain('FinanceReportsExcel')
    const contracts = functionWorkspaces.find((w) => w.id === 'marketing-contracts')!
    const pending = contracts.tabs.find((t) => t.path === '/marketing/contracts/pending')!
    const signing = contracts.tabs.find((t) => t.path === '/marketing/funnel/signing')!
    expect(pending.props).toHaveProperty('disableDefaultFlowStagePreset', true)
    expect(signing.props).not.toHaveProperty('disableDefaultFlowStagePreset')
  })

  it('lands the three departments on their own daily work page', () => {
    expect(resolveDefaultHome(['FINANCE_EMPLOYEE'])).toBe('/finance/workbench')
    expect(resolveDefaultHome(['MARKETING_MINISTER'])).toBe('/marketing/workbench')
    expect(resolveDefaultHome(['HR_EMPLOYEE'])).toBe('/hr/overview')
  })
})
