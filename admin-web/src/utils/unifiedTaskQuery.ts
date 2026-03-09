import type { LocationQuery } from 'vue-router'
import type { MedicalUnifiedTaskQuery } from '../types'

const SORT_BY_OPTIONS = new Set(['PLANNED_TIME', 'PRIORITY', 'RISK_SCORE'])
const SORT_DIRECTION_OPTIONS = new Set(['ASC', 'DESC'])

export const MEDICAL_UNIFIED_TASK_QUERY_DEFAULTS: Required<Pick<
  MedicalUnifiedTaskQuery,
  'overdueHours' | 'onlyOverdue' | 'sortBy' | 'sortDirection'
>> = {
  overdueHours: 12,
  onlyOverdue: false,
  sortBy: 'RISK_SCORE',
  sortDirection: 'DESC'
}

function firstValue(value: unknown): string {
  if (Array.isArray(value)) {
    return firstValue(value[0])
  }
  if (value == null) {
    return ''
  }
  return String(value).trim()
}

function normalizeText(value: unknown): string | undefined {
  const text = firstValue(value)
  return text ? text : undefined
}

function normalizeEnum(value: unknown): string | undefined {
  const text = normalizeText(value)
  return text ? text.toUpperCase() : undefined
}

function normalizePageValue(value: unknown): number | undefined {
  const parsed = Number(value)
  if (!Number.isFinite(parsed) || parsed <= 0) {
    return undefined
  }
  return Math.round(parsed)
}

function normalizeOverdueHours(value: unknown, fallback = MEDICAL_UNIFIED_TASK_QUERY_DEFAULTS.overdueHours): number {
  const parsed = Number(value)
  if (!Number.isFinite(parsed)) {
    return fallback
  }
  const rounded = Math.round(parsed)
  return Math.max(1, Math.min(72, rounded))
}

function normalizeOnlyOverdue(value: unknown, fallback = MEDICAL_UNIFIED_TASK_QUERY_DEFAULTS.onlyOverdue): boolean {
  if (value == null || value === '') {
    return fallback
  }
  const text = String(value).trim().toLowerCase()
  if (text === '1' || text === 'true' || text === 'yes' || text === 'y') {
    return true
  }
  if (text === '0' || text === 'false' || text === 'no' || text === 'n') {
    return false
  }
  return fallback
}

function normalizeSortBy(value: unknown, fallback = MEDICAL_UNIFIED_TASK_QUERY_DEFAULTS.sortBy): string {
  const normalized = normalizeEnum(value)
  if (!normalized || !SORT_BY_OPTIONS.has(normalized)) {
    return fallback
  }
  return normalized
}

function normalizeSortDirection(value: unknown, fallback = MEDICAL_UNIFIED_TASK_QUERY_DEFAULTS.sortDirection): string {
  const normalized = normalizeEnum(value)
  if (!normalized || !SORT_DIRECTION_OPTIONS.has(normalized)) {
    return fallback
  }
  return normalized
}

export function normalizeMedicalUnifiedTaskQuery(
  query?: Partial<MedicalUnifiedTaskQuery> | null
): MedicalUnifiedTaskQuery {
  const normalized: MedicalUnifiedTaskQuery = {
    overdueHours: normalizeOverdueHours(query?.overdueHours),
    onlyOverdue: normalizeOnlyOverdue(query?.onlyOverdue),
    sortBy: normalizeSortBy(query?.sortBy),
    sortDirection: normalizeSortDirection(query?.sortDirection)
  }
  const keyword = normalizeText(query?.keyword)
  if (keyword) {
    normalized.keyword = keyword
  }
  const module = normalizeEnum(query?.module)
  if (module) {
    normalized.module = module
  }
  const priority = normalizeEnum(query?.priority)
  if (priority) {
    normalized.priority = priority
  }
  const status = normalizeEnum(query?.status)
  if (status) {
    normalized.status = status
  }
  const elderId = normalizePageValue(query?.elderId)
  if (elderId) {
    normalized.elderId = elderId
  }
  const pageNo = normalizePageValue(query?.pageNo)
  if (pageNo) {
    normalized.pageNo = pageNo
  }
  const pageSize = normalizePageValue(query?.pageSize)
  if (pageSize) {
    normalized.pageSize = pageSize
  }
  return normalized
}

export function parseMedicalUnifiedTaskRouteQuery(routeQuery: LocationQuery): MedicalUnifiedTaskQuery {
  return normalizeMedicalUnifiedTaskQuery({
    elderId: firstValue(routeQuery.elderId || routeQuery.residentId),
    module: firstValue(routeQuery.module),
    priority: firstValue(routeQuery.priority),
    status: firstValue(routeQuery.status),
    keyword: firstValue(routeQuery.keyword),
    overdueHours: firstValue(routeQuery.overdueHours),
    onlyOverdue: firstValue(routeQuery.onlyOverdue),
    sortBy: firstValue(routeQuery.sortBy),
    sortDirection: firstValue(routeQuery.sortDirection),
    pageNo: firstValue(routeQuery.pageNo),
    pageSize: firstValue(routeQuery.pageSize)
  })
}

export function buildMedicalUnifiedTaskRouteQuery(
  query: Partial<MedicalUnifiedTaskQuery>,
  extra?: Record<string, string | number | boolean | undefined | null>
) {
  const normalized = normalizeMedicalUnifiedTaskQuery(query)
  const result: Record<string, string> = {
    overdueHours: String(normalized.overdueHours ?? MEDICAL_UNIFIED_TASK_QUERY_DEFAULTS.overdueHours),
    sortBy: String(normalized.sortBy ?? MEDICAL_UNIFIED_TASK_QUERY_DEFAULTS.sortBy),
    sortDirection: String(normalized.sortDirection ?? MEDICAL_UNIFIED_TASK_QUERY_DEFAULTS.sortDirection)
  }
  if (normalized.onlyOverdue) {
    result.onlyOverdue = '1'
  }
  if (normalized.keyword) {
    result.keyword = normalized.keyword
  }
  if (normalized.module) {
    result.module = normalized.module
  }
  if (normalized.priority) {
    result.priority = normalized.priority
  }
  if (normalized.status) {
    result.status = normalized.status
  }
  if (normalized.elderId) {
    result.elderId = String(normalized.elderId)
  }
  if (normalized.pageNo) {
    result.pageNo = String(normalized.pageNo)
  }
  if (normalized.pageSize) {
    result.pageSize = String(normalized.pageSize)
  }
  Object.entries(extra || {}).forEach(([key, value]) => {
    if (value == null || value === '') {
      return
    }
    result[key] = String(value)
  })
  return result
}
