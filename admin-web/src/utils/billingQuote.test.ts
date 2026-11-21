import { expect, it } from 'vitest'
import type { FinanceBillingConfigEntry } from '../types'
import { billingQuote } from './billingQuote'

const rate = (configKey: string, configValue: number, effectiveMonth = '2026-01', status = 1) =>
  ({ configKey, configValue, effectiveMonth, status } as FinanceBillingConfigEntry)
it('inherits the latest enabled standard and prefers monthly meals to daily meals', () => {
  const result = billingQuote([
    rate('BED_FEE_MONTHLY', 1800), rate('BED_FEE_MONTHLY', 2500, '2026-12'),
    rate('BED_FEE_MONTHLY', 2000, '2026-11', 0), rate('MEAL_FEE_MONTHLY', 900), rate('MEAL_FEE_PER_DAY', 50)
  ], '2026-11', 1200, 10)
  expect(result).toMatchObject({ bed: 600, nursing: 400, meal: 300, total: 1300 })
})
it('rounds each prorated fee to cents, supports leap February and daily meals', () => {
  expect(billingQuote([rate('BED_FEE_MONTHLY', 100), rate('MEAL_FEE_PER_DAY', 0.1)], '2028-02', 100, 1))
    .toMatchObject({ bed: 3.45, nursing: 3.45, meal: 0.1, total: 7, daysInMonth: 29 })
})
it('rejects an invalid month, non-finite rates and days outside the billing month', () => {
  expect(billingQuote([], '2026-13', 100)).toBeNull()
  expect(billingQuote([], '2026-11', Number.NaN)).toBeNull()
  expect(billingQuote([], '2026-11', 100, 31)).toBeNull()
  expect(billingQuote([], '2026-11', 100, 0)).toBeNull()
})
