import { beforeEach, describe, expect, it, vi } from 'vitest'
import {
  listStatsPresets,
  loadStatsTargets,
  nextSubscriptionRunText,
  saveStatsPreset,
  saveStatsSubscription,
  saveStatsTargets
} from './statsWorkbench'

describe('statsWorkbench utils', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2026-03-15T09:00:00'))
  })

  it('saves and restores presets', () => {
    saveStatsPreset('check-in', {
      name: '周会版',
      summary: '近6个月',
      payload: { from: '2026-01', to: '2026-06' }
    })
    const presets = listStatsPresets('check-in')
    expect(presets).toHaveLength(1)
    expect(presets[0].payload).toEqual({ from: '2026-01', to: '2026-06' })
  })

  it('saves and restores targets', () => {
    saveStatsTargets('revenue', { totalRevenue: 1200 })
    expect(loadStatsTargets('revenue')).toEqual({ totalRevenue: 1200 })
  })

  it('computes next run text for subscription', () => {
    saveStatsSubscription('revenue', {
      name: '月报',
      frequency: 'DAILY',
      hour: 10,
      minute: 30,
      recipient: '院长',
      channel: '站内提醒',
      enabled: true,
      note: ''
    })
    const nextText = nextSubscriptionRunText({
      id: '1',
      name: '月报',
      frequency: 'DAILY',
      hour: 10,
      minute: 30,
      recipient: '院长',
      channel: '站内提醒',
      enabled: true,
      note: '',
      updatedAt: '2026-03-15T09:00:00'
    })
    expect(nextText).toBe('03-15 10:30')
  })
})
