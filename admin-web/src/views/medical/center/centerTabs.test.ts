import { describe, expect, it } from 'vitest'
import { createMemoryHistory, createRouter } from 'vue-router'
import { routes } from '../../../router/routes'
import { resolveRouteAccess } from '../../../utils/routeAccess'
import { MEDICAL_CENTERS, pickTab } from './centerTabs'

const router = createRouter({ history: createMemoryHistory(), routes })

describe('medical center tabs', () => {
  it('points every tab at an existing route with a component', () => {
    for (const config of Object.values(MEDICAL_CENTERS)) {
      for (const tab of config.tabs) {
        const matched = router.resolve(tab.path).matched
        const record = matched[matched.length - 1]
        expect(record?.components?.default, tab.path).toBeTruthy()
      }
    }
  })

  it('falls back to the first visible tab', () => {
    const tabs = MEDICAL_CENTERS.alerts.tabs
    expect(pickTab(tabs, 'emergency')?.key).toBe('emergency')
    expect(pickTab(tabs, 'nope')?.key).toBe('device')
  })

  it('keeps system settings away from front-line medical staff', () => {
    expect(resolveRouteAccess(router, ['MEDICAL_EMPLOYEE'], '/medical-care/settings', [], []).canAccess).toBe(false)
    expect(resolveRouteAccess(router, ['MEDICAL_MINISTER'], '/medical-care/settings', [], []).canAccess).toBe(true)
    expect(resolveRouteAccess(router, ['MEDICAL_EMPLOYEE'], '/medical-care/clinical', [], []).canAccess).toBe(true)
  })
})
