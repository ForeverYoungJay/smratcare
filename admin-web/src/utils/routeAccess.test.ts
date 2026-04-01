import { describe, expect, it } from 'vitest'
import { canAccessPath } from './routeAccess'

describe('routeAccess utils', () => {
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

  it('keeps explicit page permissions as the highest-priority allow rule', () => {
    expect(canAccessPath(['MARKETING_EMPLOYEE'], ['ADMIN'], '/system/site-config', ['/system/site-config'])).toBe(true)
    expect(canAccessPath(['MARKETING_EMPLOYEE'], ['ADMIN'], '/system/site-config', [])).toBe(false)
  })
})
