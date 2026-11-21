import { describe, expect, it } from 'vitest'
import { payrollActions, previewNet } from './hrPayroll'

describe('payroll', () => {
  it('lets each role act only at its own step', () => {
    expect(payrollActions('DRAFT', ['HR_EMPLOYEE']).map((a) => a.action)).toEqual(['submit'])
    expect(payrollActions('SUBMITTED', ['HR_EMPLOYEE'])).toEqual([])
    expect(payrollActions('SUBMITTED', ['HR_MINISTER']).map((a) => a.action)).toEqual(['approve', 'reject'])
    expect(payrollActions('APPROVED', ['HR_MINISTER'])).toEqual([])
    expect(payrollActions('APPROVED', ['FINANCE_EMPLOYEE']).map((a) => a.action)).toEqual(['bank-submit'])
    expect(payrollActions('BANK_SUBMITTED', ['FINANCE_MINISTER'])[0]).toMatchObject({ action: 'mark-paid', needRef: true })
  })

  it('previews net pay in cents without float drift', () => {
    expect(previewNet({ baseAmount: 0.1, performanceAmount: 0.2, deductionAmount: 0.05 })).toEqual({ gross: 0.3, net: 0.25 })
  })
})
