import type { LocationQuery } from 'vue-router'
import type { LogisticsWorkbenchSummaryQuery } from '../types'

type LogisticsWorkbenchQueryField = keyof LogisticsWorkbenchSummaryQuery

const QUERY_FIELDS: LogisticsWorkbenchQueryField[] = ['windowDays', 'expiryDays', 'overdueDays', 'maintenanceDueDays']

export const LOGISTICS_WORKBENCH_QUERY_LIMITS: Record<LogisticsWorkbenchQueryField, { min: number; max: number }> = {
  windowDays: { min: 1, max: 180 },
  expiryDays: { min: 1, max: 365 },
  overdueDays: { min: 1, max: 30 },
  maintenanceDueDays: { min: 1, max: 180 }
}

export const LOGISTICS_WORKBENCH_QUERY_DEFAULTS: Required<LogisticsWorkbenchSummaryQuery> = {
  windowDays: 30,
  expiryDays: 30,
  overdueDays: 2,
  maintenanceDueDays: 30
}

function firstQueryValue(value: unknown): string {
  if (Array.isArray(value)) {
    return firstQueryValue(value[0])
  }
  if (value == null) {
    return ''
  }
  return String(value).trim()
}

function clamp(value: number, min: number, max: number) {
  return Math.max(min, Math.min(max, value))
}

function normalizeField(
  field: LogisticsWorkbenchQueryField,
  value: unknown,
  fallback: number
): number {
  const parsed = Number(value)
  if (!Number.isFinite(parsed)) {
    return fallback
  }
  const limits = LOGISTICS_WORKBENCH_QUERY_LIMITS[field]
  return clamp(Math.round(parsed), limits.min, limits.max)
}

export function normalizeLogisticsWorkbenchQuery(
  query?: Partial<LogisticsWorkbenchSummaryQuery> | null,
  fallback: Required<LogisticsWorkbenchSummaryQuery> = LOGISTICS_WORKBENCH_QUERY_DEFAULTS
): Required<LogisticsWorkbenchSummaryQuery> {
  return {
    windowDays: normalizeField('windowDays', query?.windowDays, fallback.windowDays),
    expiryDays: normalizeField('expiryDays', query?.expiryDays, fallback.expiryDays),
    overdueDays: normalizeField('overdueDays', query?.overdueDays, fallback.overdueDays),
    maintenanceDueDays: normalizeField('maintenanceDueDays', query?.maintenanceDueDays, fallback.maintenanceDueDays)
  }
}

export function parseLogisticsWorkbenchQueryPatch(routeQuery: LocationQuery): LogisticsWorkbenchSummaryQuery {
  const patch: LogisticsWorkbenchSummaryQuery = {}
  QUERY_FIELDS.forEach((field) => {
    const raw = firstQueryValue(routeQuery[field])
    if (!raw) {
      return
    }
    const parsed = Number(raw)
    if (!Number.isFinite(parsed)) {
      return
    }
    patch[field] = normalizeField(field, parsed, LOGISTICS_WORKBENCH_QUERY_DEFAULTS[field])
  })
  return patch
}

export function buildLogisticsWorkbenchRouteQuery(
  query: Partial<LogisticsWorkbenchSummaryQuery>,
  extra?: Record<string, string | number | undefined | null>
) {
  const normalized = normalizeLogisticsWorkbenchQuery(query)
  const result: Record<string, string> = {
    windowDays: String(normalized.windowDays),
    expiryDays: String(normalized.expiryDays),
    overdueDays: String(normalized.overdueDays),
    maintenanceDueDays: String(normalized.maintenanceDueDays)
  }
  Object.entries(extra || {}).forEach(([key, value]) => {
    if (value == null || value === '') {
      return
    }
    result[key] = String(value)
  })
  return result
}
