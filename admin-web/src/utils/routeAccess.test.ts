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

  it('uses role default page templates before broad module fallback', () => {
    expect(canAccessPath(['LOGISTICS_EMPLOYEE'], [], '/stats/org/bed-usage')).toBe(false)
    expect(canAccessPath(['LOGISTICS_MINISTER'], [], '/stats/check-in')).toBe(false)
    expect(canAccessPath(['HR_EMPLOYEE'], [], '/stats/check-in')).toBe(true)
    expect(canAccessPath(['FINANCE_MINISTER'], [], '/stats/check-in')).toBe(true)
    expect(canAccessPath(['NURSING_EMPLOYEE'], [], '/stats/check-in')).toBe(false)
  })

  it('keeps explicit page permissions as the highest-priority allow rule', () => {
    expect(canAccessPath(['MARKETING_EMPLOYEE'], ['ADMIN'], '/system/site-config', ['/system/site-config'])).toBe(true)
    expect(canAccessPath(['MARKETING_EMPLOYEE'], ['ADMIN'], '/system/site-config', [])).toBe(false)
  })

  it('supports page-level default permissions inside a module', () => {
    expect(canAccessPath(['LOGISTICS_EMPLOYEE'], [], '/logistics/workbench')).toBe(true)
    expect(canAccessPath(['LOGISTICS_EMPLOYEE'], [], '/logistics/storage/warehouse')).toBe(true)
    expect(canAccessPath(['LOGISTICS_EMPLOYEE'], [], '/system/role')).toBe(false)
  })
})
