import { describe, expect, it } from 'vitest'
import { buildComparisonSummary, detectSeriesAnomaly } from './statsInsight'

describe('statsInsight utils', () => {
  it('builds comparison summary with target gap', () => {
    const summary = buildComparisonSummary([
      { period: '2025-03', value: 80 },
      { period: '2026-02', value: 100 },
      { period: '2026-03', value: 120 }
    ], 110)

    expect(summary.latestValue).toBe(120)
    expect(summary.momRate).toBe(20)
    expect(summary.yoyRate).toBe(50)
    expect(summary.targetGap).toBe(10)
  })

  it('detects consecutive decline anomaly', () => {
    const anomaly = detectSeriesAnomaly([
      { period: '2026-01', value: 120 },
      { period: '2026-02', value: 100 },
      { period: '2026-03', value: 80 }
    ])

    expect(anomaly.level).toBe('danger')
  })
})
