import { describe, expect, it } from 'vitest'
import dayjs from 'dayjs'
import { formatRate, funnelStages, growth, monthRange, rate, recentMonths, sortTodos, toCsv, trendPoints, type TodoItem } from './marketingMetrics'

describe('marketingMetrics', () => {
  it('computes rates and treats empty denominators as unknown', () => {
    expect(rate(12, 86)).toBe(14)
    expect(formatRate(62, 86)).toBe('72.1%')
    expect(formatRate(1, 0)).toBe('--')
    expect(formatRate(undefined, 10)).toBe('--')
    expect(growth(86, 77)).toBe(12)
    expect(growth(5, 0)).toBeNull()
  })

  it('stops the current month at today and lists recent months in order', () => {
    const today = dayjs('2026-11-16')
    expect(monthRange(today, today)).toEqual({ dateFrom: '2026-11-01', dateTo: '2026-11-16', label: '11月', month: '2026-11' })
    expect(monthRange(dayjs('2026-10-05'), today).dateTo).toBe('2026-10-31')
    expect(recentMonths(6, today, today).map((m) => m.label)).toEqual(['6月', '7月', '8月', '9月', '10月', '11月'])
  })

  it('puts overdue todos first, then by time', () => {
    const base = { kind: 'followup', title: '', desc: '', actionLabel: '', path: '' } as const
    const items: TodoItem[] = [
      { ...base, key: 'a', time: '2026-11-16 14:00', overdue: false },
      { ...base, key: 'b', overdue: false },
      { ...base, key: 'c', time: '2026-11-16 09:00', overdue: false },
      { ...base, key: 'd', time: '2026-11-20', overdue: true }
    ]
    expect(sortTodos(items).map((i) => i.key)).toEqual(['d', 'c', 'a', 'b'])
  })

  it('scales trend lines on a shared axis', () => {
    const [a, b] = trendPoints([[0, 10], [5, 5]], 100, 100, 10)
    expect(a).toEqual([{ x: 10, y: 90, value: 0 }, { x: 90, y: 10, value: 10 }])
    expect(b[0].y).toBe(50)
  })

  it('builds funnel stages with drill-down targets', () => {
    const stages = funnelStages({ totalLeads: 86, intentCount: 62, visitCount: 34, reservationCount: 18, contractCount: 12 })
    expect(stages.map((s) => s.count)).toEqual([86, 62, 34, 18, 12])
    expect(stages[4]).toMatchObject({ share: '14.0%', width: 14, path: '/marketing/contracts' })
    expect(stages[1].query).toEqual({ tab: 'intent' })
    expect(funnelStages({})[0].width).toBe(0)
  })

  it('escapes CSV cells', () => {
    expect(toCsv(['a', 'b'], [['x,y', 1]])).toBe('﻿a,b\n"x,y",1')
  })
})
