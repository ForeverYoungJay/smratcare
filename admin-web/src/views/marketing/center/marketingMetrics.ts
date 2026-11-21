import dayjs, { type Dayjs } from 'dayjs'

/** 市场工作台 / 市场运营共用的指标口径与纯计算。 */

/** 比率（百分数，保留 1 位）；分母为 0 或缺数据时返回 null，界面显示 `--`。 */
export function rate(numerator?: number | null, denominator?: number | null): number | null {
  const n = Number(numerator), d = Number(denominator)
  if (!Number.isFinite(n) || !Number.isFinite(d) || d <= 0) return null
  return Math.round((n / d) * 1000) / 10
}

export function formatRate(numerator?: number | null, denominator?: number | null) {
  const value = rate(numerator, denominator)
  return value == null ? '--' : `${value.toFixed(1)}%`
}

/** 环比变化（百分数，保留整数），上期为 0 时无法比较。 */
export function growth(current?: number | null, previous?: number | null): number | null {
  const c = Number(current), p = Number(previous)
  if (!Number.isFinite(c) || !Number.isFinite(p) || p <= 0) return null
  return Math.round(((c - p) / p) * 100)
}

export interface MonthRange { dateFrom: string; dateTo: string; label: string; month: string }

/** 某月的统计区间；当月只统计到今天（避免把未来日期算进口径）。 */
export function monthRange(month: Dayjs, today: Dayjs = dayjs()): MonthRange {
  const start = month.startOf('month')
  const end = start.isSame(today, 'month') ? today : start.endOf('month')
  return { dateFrom: start.format('YYYY-MM-DD'), dateTo: end.format('YYYY-MM-DD'), label: `${start.month() + 1}月`, month: start.format('YYYY-MM') }
}

/** 截止到 endMonth 的最近 n 个月，按时间正序。 */
export function recentMonths(n: number, endMonth: Dayjs = dayjs(), today: Dayjs = dayjs()): MonthRange[] {
  return Array.from({ length: n }, (_, i) => monthRange(endMonth.subtract(n - 1 - i, 'month'), today))
}

export interface TodoItem {
  key: string
  kind: 'followup' | 'visit' | 'contract' | 'reservation'
  title: string
  desc: string
  /** 排序与展示用的时间（YYYY-MM-DD 或 YYYY-MM-DD HH:mm） */
  time?: string
  overdue: boolean
  actionLabel: string
  path: string
  query?: Record<string, string>
}

/** 待办合并：超时的排最前，其余按时间先后，无时间的放最后。 */
export function sortTodos(items: TodoItem[]): TodoItem[] {
  return [...items].sort((a, b) => {
    if (a.overdue !== b.overdue) return a.overdue ? -1 : 1
    if (!a.time || !b.time) return a.time ? -1 : b.time ? 1 : 0
    return a.time.localeCompare(b.time)
  })
}

/** 折线坐标：多条线共用一个纵轴最大值，保证可比。 */
export function trendPoints(series: number[][], width: number, height: number, pad = 24) {
  const max = Math.max(1, ...series.flat().map((v) => Number(v) || 0))
  const count = Math.max(1, ...series.map((s) => s.length))
  const step = count > 1 ? (width - pad * 2) / (count - 1) : 0
  return series.map((values) => values.map((value, i) => ({
    x: Math.round((pad + i * step) * 10) / 10,
    y: Math.round((height - pad - ((Number(value) || 0) / max) * (height - pad * 2)) * 10) / 10,
    value: Number(value) || 0
  })))
}

export interface FunnelInput { totalLeads?: number; intentCount?: number; visitCount?: number; reservationCount?: number; contractCount?: number }
export interface FunnelStage { key: string; label: string; count: number; share: string; width: number; path: string; query: Record<string, string> }

/** 漏斗各阶段：占线索比例 + 下钻到客户页对应标签。 */
export function funnelStages(input: FunnelInput): FunnelStage[] {
  const total = Number(input.totalLeads || 0)
  const stages: Array<[string, string, number | undefined, string, Record<string, string>]> = [
    ['lead', '新增线索', input.totalLeads, '/marketing/customers', {}],
    ['intent', '有效意向', input.intentCount, '/marketing/customers', { tab: 'intent' }],
    ['visit', '实际到访', input.visitCount, '/marketing/visits', {}],
    ['reservation', '床位预订', input.reservationCount, '/marketing/customers', { tab: 'reservation' }],
    ['contract', '合同签约', input.contractCount, '/marketing/contracts', {}]
  ]
  return stages.map(([key, label, value, path, query]) => {
    const count = Number(value || 0)
    return { key, label, count, share: formatRate(count, total), width: total > 0 ? Math.min(100, Math.round((count / total) * 100)) : 0, path, query }
  })
}

/** 当前表格导出 CSV（Excel 可直接打开）。 */
export function toCsv(headers: string[], rows: Array<Array<string | number | null | undefined>>) {
  const cell = (v: string | number | null | undefined) => {
    const s = v == null ? '' : String(v)
    return /[",\n]/.test(s) ? `"${s.replace(/"/g, '""')}"` : s
  }
  return '﻿' + [headers, ...rows].map((r) => r.map(cell).join(',')).join('\n')
}
