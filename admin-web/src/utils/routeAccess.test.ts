import { describe, expect, it } from 'vitest'
import { canAccessPath } from './routeAccess'

describe('routeAccess utils', () => {
  it('allows super roles to access any guarded route', () => {
    expect(canAccessPath(['ADMIN'], ['HR_MINISTER'], '/hr/staff')).toBe(true)
    expect(canAccessPath(['SYS_ADMIN'], ['MEDICAL_EMPLOYEE'], '/health/management/archive')).toBe(true)
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

  it('treats explicit page permissions as the primary route allow rule', () => {
    expect(canAccessPath(['MARKETING_EMPLOYEE'], ['ADMIN'], '/system/site-config', ['/system/site-config'])).toBe(true)
    expect(canAccessPath(['MARKETING_EMPLOYEE'], ['ADMIN'], '/system/site-config', [])).toBe(false)
    expect(canAccessPath(['HR_MINISTER'], ['ADMIN'], '/system/site-config', ['/system/site-config'])).toBe(true)
  })

  it('keeps module fallback aligned with marketing and fire routes', () => {
    expect(canAccessPath(['MARKETING_EMPLOYEE'], [], '/crm/follow-up')).toBe(true)
    expect(canAccessPath(['GUARD'], [], '/fire/day-patrol')).toBe(true)
    expect(canAccessPath(['MARKETING_EMPLOYEE'], [], '/fire/day-patrol')).toBe(false)
  })
})
