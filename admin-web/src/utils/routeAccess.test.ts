import { createMemoryHistory, createRouter } from 'vue-router'
import { describe, expect, it } from 'vitest'
import { canAccessPath, resolveRouteAccess } from './routeAccess'

describe('routeAccess utils', () => {
  it('does not treat ADMIN, SYS_ADMIN and DIRECTOR as interchangeable roles', () => {
    expect(canAccessPath(['ADMIN'], ['HR_MINISTER'], '/hr/staff', ['/hr'])).toBe(false)
    expect(canAccessPath(['SYS_ADMIN'], ['ADMIN'], '/system/business-admin', ['/system/business-admin'])).toBe(false)
    expect(canAccessPath(['DIRECTOR'], ['SYS_ADMIN'], '/system/security', ['/system/security'])).toBe(false)
  })

  it('falls back to module roles for hidden health routes', () => {
    expect(canAccessPath(['MEDICAL_EMPLOYEE'], [], '/health/management/archive')).toBe(true)
    expect(canAccessPath(['NURSING_EMPLOYEE'], [], '/health/management/archive')).toBe(true)
    expect(canAccessPath(['MARKETING_EMPLOYEE'], [], '/health/management/archive')).toBe(false)
  })

  it('restricts stats routes when no explicit route meta is present', () => {
    expect(canAccessPath(['LOGISTICS_EMPLOYEE'], [], '/stats/org/bed-usage')).toBe(true)
    expect(canAccessPath(['HR_EMPLOYEE'], [], '/stats/check-in')).toBe(true)
    expect(canAccessPath(['NURSING_EMPLOYEE'], [], '/stats/check-in')).toBe(false)
  })

  it('requires page access and route roles instead of letting either one override the other', () => {
    expect(canAccessPath(['MARKETING_EMPLOYEE'], ['ADMIN'], '/system/site-config', ['/system/site-config'])).toBe(false)
    expect(canAccessPath(['MARKETING_EMPLOYEE'], ['ADMIN'], '/system/site-config', [])).toBe(false)
    expect(canAccessPath(['ADMIN'], ['ADMIN'], '/system/site-config', ['/system/site-config'])).toBe(true)
  })

  it('does not let broad page permissions bypass stricter route roles', () => {
    expect(
      canAccessPath(
        ['HR_EMPLOYEE'],
        ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'],
        '/hr/profile/basic',
        ['/hr']
      )
    ).toBe(false)
    expect(
      canAccessPath(
        ['HR_MINISTER'],
        ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'],
        '/hr/profile/basic',
        ['/hr']
      )
    ).toBe(true)
  })

  it('keeps module fallback aligned with marketing and fire routes', () => {
    expect(canAccessPath(['MARKETING_EMPLOYEE'], [], '/crm/follow-up')).toBe(true)
    expect(canAccessPath(['GUARD'], [], '/fire/day-patrol')).toBe(true)
    expect(canAccessPath(['MARKETING_EMPLOYEE'], [], '/fire/day-patrol')).toBe(false)
  })

  it('requires every matched parent and child role constraint', () => {
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [
        {
          path: '/hr',
          component: { template: '<router-view />' },
          meta: { roles: ['HR_EMPLOYEE', 'HR_MINISTER'] },
          children: [
            {
              path: 'approval',
              component: { template: '<div />' },
              meta: { roles: ['HR_MINISTER'] }
            }
          ]
        }
      ]
    })

    expect(resolveRouteAccess(router, ['HR_EMPLOYEE'], '/hr/approval', ['/hr']).canAccess).toBe(false)
    expect(resolveRouteAccess(router, ['HR_MINISTER'], '/hr/approval', ['/hr']).canAccess).toBe(true)
  })

  it('defaults to deny when neither route metadata nor a page grant authorizes an unknown page', () => {
    expect(canAccessPath(['HR_EMPLOYEE'], [], '/unclassified-page')).toBe(false)
    expect(canAccessPath(['HR_EMPLOYEE'], [], '/unclassified-page', ['/unclassified-page'])).toBe(true)
  })

  it('allows a role-free page only when its route permission and page grant both match', () => {
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [{ path: '/action-page', component: { template: '<div />' }, meta: { permissions: ['oa.todo.view'] } }]
    })
    expect(resolveRouteAccess(router, ['HR_EMPLOYEE'], '/action-page', ['/action-page'], []).canAccess).toBe(false)
    expect(resolveRouteAccess(router, ['HR_EMPLOYEE'], '/action-page', ['/action-page'], ['oa.todo.view']).canAccess).toBe(true)
    expect(resolveRouteAccess(router, ['HR_EMPLOYEE'], '/action-page', [], ['oa.todo.view']).canAccess).toBe(true)
  })
})
