import { describe, expect, it } from 'vitest'
import { buildWorkbenchKpis, resolveWorkbenchProfile, workbenchAudienceLabel } from './model'

describe('workbench profile', () => {
  it.each([
    ['NURSING_EMPLOYEE', 'NURSING', 'EMPLOYEE', false],
    ['NURSING_MINISTER', 'NURSING', 'MINISTER', true],
    ['MEDICAL_EMPLOYEE', 'MEDICAL', 'EMPLOYEE', false],
    ['MEDICAL_MINISTER', 'MEDICAL', 'MINISTER', true],
    ['FINANCE_EMPLOYEE', 'FINANCE', 'EMPLOYEE', false],
    ['FINANCE_MINISTER', 'FINANCE', 'MINISTER', true],
    ['LOGISTICS_EMPLOYEE', 'LOGISTICS', 'EMPLOYEE', false],
    ['LOGISTICS_MINISTER', 'LOGISTICS', 'MINISTER', true],
    ['HR_EMPLOYEE', 'HR', 'EMPLOYEE', false],
    ['HR_MINISTER', 'HR', 'MINISTER', true],
    ['MARKETING_EMPLOYEE', 'MARKETING', 'EMPLOYEE', false],
    ['MARKETING_MINISTER', 'MARKETING', 'MINISTER', true]
  ])('maps %s to a deterministic department profile', (role, department, audience, hasManagementView) => {
    const profile = resolveWorkbenchProfile([role])
    expect(profile.department).toBe(department)
    expect(profile.audience).toBe(audience)
    expect(profile.hasManagementView).toBe(hasManagementView)
  })

  it('uses security role priority for mixed-role accounts', () => {
    expect(resolveWorkbenchProfile(['NURSING_EMPLOYEE', 'SYS_ADMIN']).audience).toBe('SYSTEM_ADMIN')
    expect(resolveWorkbenchProfile(['MARKETING_EMPLOYEE', 'DIRECTOR']).audience).toBe('DIRECTOR')
    expect(resolveWorkbenchProfile(['HR_EMPLOYEE', 'HR_MINISTER']).audience).toBe('MINISTER')
  })

  it.each([
    ['SYS_ADMIN', 'SYSTEM_ADMIN', '/system'],
    ['DIRECTOR', 'DIRECTOR', '/portal'],
    ['ADMIN', 'BUSINESS_ADMIN', '/portal']
  ])('keeps %s outside department workbench profiles', (role, audience, primaryEntry) => {
    const profile = resolveWorkbenchProfile([role])
    expect(profile.department).toBeUndefined()
    expect(profile.audience).toBe(audience)
    expect(profile.primaryEntry).toBe(primaryEntry)
  })

  it('keeps legacy marketing identities compatible', () => {
    expect(resolveWorkbenchProfile(['OPERATOR']).department).toBe('MARKETING')
    expect(resolveWorkbenchProfile(['MANAGER']).audience).toBe('MINISTER')
  })

  it('provides staff-facing audience labels', () => {
    expect(workbenchAudienceLabel(resolveWorkbenchProfile(['FINANCE_EMPLOYEE']))).toBe('员工工作台')
    expect(workbenchAudienceLabel(resolveWorkbenchProfile(['FINANCE_MINISTER']))).toBe('部长工作台')
  })
})

describe('buildWorkbenchKpis', () => {
  const roleMetric = { label: '我的待执行', value: '3', helper: '', path: '/care/today', tone: 'success' as const }
  const oa = { overdue: '7', healthRisk: '2', healthPath: '/medical-care/unified-task-center', approvals: '5' }

  it.each([
    ['nursing', 'health'],
    ['medical', 'health'],
    ['finance', 'approvals'],
    ['manager', 'approvals']
  ])('%s gets %s as the last card', (personaKey, lastKey) => {
    const kpis = buildWorkbenchKpis(personaKey, [roleMetric, roleMetric], oa)
    expect(kpis.map((item) => item.key)).toEqual(['role-0', 'role-1', 'overdue', lastKey])
  })

  it('keeps the row short when a persona has fewer role metrics', () => {
    expect(buildWorkbenchKpis('medical', [roleMetric], oa).map((item) => item.key)).toEqual(['role-0', 'overdue', 'health'])
  })
})
