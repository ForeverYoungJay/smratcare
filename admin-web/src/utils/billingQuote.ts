import dayjs from 'dayjs'
import type { FinanceBillingConfigEntry } from '../types'

// shortcut: 仅试算床位、护理和餐费；需要折扣与分摊时改用后端账单预览。
export function billingQuote(entries: FinanceBillingConfigEntry[], month: string, nursingMonthly: number, days?: number) {
  const daysInMonth = dayjs(`${month}-01`).daysInMonth()
  const chargeableDays = days ?? daysInMonth
  if (!/^\d{4}-(0[1-9]|1[0-2])$/.test(month) || !Number.isInteger(chargeableDays) || chargeableDays < 1 || chargeableDays > daysInMonth) return null
  const rate = (key: string) => {
    const row = entries.filter(item => item.configKey === key && Number(item.status) === 1 && item.effectiveMonth <= month)
      .sort((a, b) => b.effectiveMonth.localeCompare(a.effectiveMonth))[0]
    return Number(row?.configValue ?? 0)
  }
  const bedMonthly = rate('BED_FEE_MONTHLY'), mealMonthly = rate('MEAL_FEE_MONTHLY'), mealDaily = rate('MEAL_FEE_PER_DAY')
  const values = [bedMonthly, nursingMonthly, mealMonthly, mealDaily]
  if (values.some(value => !Number.isFinite(value) || value < 0 || !Number.isSafeInteger(Math.round(value * 100) * daysInMonth))) return null
  const prorate = (amount: number) => Math.round(Math.round(amount * 100) * chargeableDays / daysInMonth)
  const bed = prorate(bedMonthly), nursing = prorate(nursingMonthly)
  const meal = mealMonthly > 0 ? prorate(mealMonthly) : Math.round(mealDaily * 100) * chargeableDays
  return { bed: bed / 100, nursing: nursing / 100, meal: meal / 100, total: (bed + nursing + meal) / 100, daysInMonth, chargeableDays }
}
