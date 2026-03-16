export interface PeriodValueItem {
  period: string
  value: number
}

export interface StatsComparisonSummary {
  latestValue: number
  previousValue?: number
  previousPeriod?: string
  latestPeriod?: string
  momRate?: number
  yoyRate?: number
  target?: number
  targetGap?: number
  anomalyText?: string
  anomalyLevel?: 'good' | 'warning' | 'danger'
}

function toSafeNumber(value: unknown) {
  const numeric = Number(value)
  return Number.isFinite(numeric) ? numeric : 0
}

export function buildPeriodSeries<T extends Record<string, any>>(
  rows: T[],
  periodKey: keyof T,
  valueKey: keyof T
): PeriodValueItem[] {
  return (rows || []).map((item) => ({
    period: String(item[periodKey] || ''),
    value: toSafeNumber(item[valueKey])
  }))
}

export function buildComparisonSummary(series: PeriodValueItem[], target?: number): StatsComparisonSummary {
  const latest = series[series.length - 1]
  const previous = series.length > 1 ? series[series.length - 2] : undefined
  const latestValue = toSafeNumber(latest?.value)
  const previousValue = toSafeNumber(previous?.value)
  const latestPeriod = latest?.period || ''
  const previousPeriod = previous?.period || ''
  const prevYearPeriod = latestPeriod && /^\d{4}-\d{2}$/.test(latestPeriod)
    ? `${String(Number(latestPeriod.slice(0, 4)) - 1)}-${latestPeriod.slice(5)}`
    : ''
  const previousYear = prevYearPeriod ? series.find((item) => item.period === prevYearPeriod) : undefined
  const yoyBase = toSafeNumber(previousYear?.value)
  const momRate = previous ? percentageDelta(latestValue, previousValue) : undefined
  const yoyRate = previousYear ? percentageDelta(latestValue, yoyBase) : undefined
  const targetValue = target == null ? undefined : toSafeNumber(target)
  const targetGap = targetValue == null ? undefined : latestValue - targetValue
  const anomaly = detectSeriesAnomaly(series)
  return {
    latestValue,
    previousValue: previous ? previousValue : undefined,
    previousPeriod,
    latestPeriod,
    momRate,
    yoyRate,
    target: targetValue,
    targetGap,
    anomalyText: anomaly.text,
    anomalyLevel: anomaly.level
  }
}

export function percentageDelta(current: number, previous: number) {
  if (!Number.isFinite(previous) || previous === 0) {
    return current > 0 ? 100 : 0
  }
  return Number((((current - previous) / previous) * 100).toFixed(2))
}

export function detectSeriesAnomaly(series: PeriodValueItem[]) {
  if (!series.length) {
    return { text: '暂无序列数据', level: 'warning' as const }
  }
  if (series.length < 3) {
    return { text: '样本不足，暂不判定异常', level: 'warning' as const }
  }
  const values = series.map((item) => toSafeNumber(item.value))
  const latest = values[values.length - 1]
  const baseline = values.slice(Math.max(0, values.length - 4), values.length - 1)
  const avg = baseline.reduce((sum, item) => sum + item, 0) / Math.max(baseline.length, 1)
  const changeRate = avg === 0 ? (latest > 0 ? 100 : 0) : ((latest - avg) / avg) * 100
  const consecutiveDrop = values.length >= 3
    && values[values.length - 3] > values[values.length - 2]
    && values[values.length - 2] > values[values.length - 1]

  if (consecutiveDrop && changeRate <= -15) {
    return { text: '连续 3 期下滑，建议排查波动来源', level: 'danger' as const }
  }
  if (changeRate <= -12) {
    return { text: '最近一期显著低于近阶段均值', level: 'danger' as const }
  }
  if (changeRate >= 18) {
    return { text: '最近一期明显抬升，可关注结构变化', level: 'good' as const }
  }
  return { text: '波动平稳，维持当前观察节奏', level: 'good' as const }
}
