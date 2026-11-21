import { describe, expect, it } from 'vitest'
import { normalizeRoles } from './auth'

describe('role normalization security boundary', () => {
  it('keeps ADMIN, SYS_ADMIN and DIRECTOR distinct', () => {
    expect(normalizeRoles(['SYS_ADMIN'])).toEqual(['SYS_ADMIN'])
    expect(normalizeRoles(['DIRECTOR'])).toEqual(['DIRECTOR'])
    expect(normalizeRoles(['ADMIN'])).toEqual(['ADMIN'])
  })

  it('keeps documented legacy role aliases without cross-department expansion', () => {
    expect(normalizeRoles(['OPERATOR'])).toEqual(['OPERATOR', 'MARKETING_EMPLOYEE', 'STAFF'])
    expect(normalizeRoles(['MANAGER'])).toEqual(['MANAGER', 'MARKETING_MINISTER', 'STAFF'])
    expect(normalizeRoles(['NURSING_EMPLOYEE'])).not.toContain('NURSING_MINISTER')
    expect(normalizeRoles(['NURSING_EMPLOYEE'])).not.toContain('MEDICAL_EMPLOYEE')
  })
})
