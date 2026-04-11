import { describe, expect, it } from 'vitest'
import { canAccessPath } from './routeAccess'

describe('routeAccess utils', () => {
  it('denies any business page when explicit page permissions are empty', () => {
    expect(canAccessPath(['ADMIN'], ['HR_MINISTER'], '/hr/staff')).toBe(false)
    expect(canAccessPath(['SYS_ADMIN'], ['MEDICAL_EMPLOYEE'], '/health/management/archive')).toBe(false)
  })

  it('does not fall back to module roles for hidden routes', () => {
    expect(canAccessPath(['MEDICAL_EMPLOYEE'], [], '/health/management/archive')).toBe(false)
    expect(canAccessPath(['NURSING_EMPLOYEE'], [], '/health/management/archive')).toBe(false)
    expect(canAccessPath(['MARKETING_EMPLOYEE'], [], '/health/management/archive')).toBe(false)
  })

  it('denies routes without explicit page permissions even when meta is absent', () => {
    expect(canAccessPath(['LOGISTICS_EMPLOYEE'], [], '/stats/org/bed-usage')).toBe(false)
    expect(canAccessPath(['HR_EMPLOYEE'], [], '/stats/check-in')).toBe(false)
    expect(canAccessPath(['NURSING_EMPLOYEE'], [], '/stats/check-in')).toBe(false)
  })

  it('allows access only when explicit page permissions include the path', () => {
    expect(canAccessPath(['MARKETING_EMPLOYEE'], ['ADMIN'], '/system/site-config', ['/system/site-config'])).toBe(true)
    expect(canAccessPath(['MARKETING_EMPLOYEE'], ['ADMIN'], '/system/site-config', [])).toBe(false)
  })
})
