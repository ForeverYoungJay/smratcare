import type { LocationQuery } from 'vue-router'

export type BillCenterScene = 'ADMISSION' | 'RESIDENT'

export interface BillCenterRouteState {
  month?: string
  keyword?: string
  payMethod?: string
  scene?: BillCenterScene
  pageNo: number
  pageSize: number
}

export interface BillCenterRouteOptions {
  lockScene?: boolean
  defaultScene?: BillCenterScene
  defaultMonth?: string
  defaultPageNo?: number
  defaultPageSize?: number
}

const MONTH_RE = /^\d{4}-(0[1-9]|1[0-2])$/

export const BILL_CENTER_MANAGED_ROUTE_KEYS = new Set([
  'billMonth',
  'billKeyword',
  'billPayMethod',
  'billScene',
  'billPageNo',
  'billPageSize'
])

const SORTED_BILL_CENTER_MANAGED_KEYS = [...BILL_CENTER_MANAGED_ROUTE_KEYS].sort()

function firstQueryValue(value: unknown): string {
  if (Array.isArray(value)) {
    return firstQueryValue(value[0])
  }
  if (value == null) {
    return ''
  }
  return String(value).trim()
}

function normalizePositiveInt(value: unknown, fallback: number) {
  const parsed = Number(firstQueryValue(value))
  if (!Number.isFinite(parsed) || parsed <= 0) {
    return fallback
  }
  return Math.round(parsed)
}

function normalizeMonthText(value: unknown) {
  const text = firstQueryValue(value)
  return MONTH_RE.test(text) ? text : ''
}

function normalizeScene(value: unknown): BillCenterScene | undefined {
  const text = firstQueryValue(value).toUpperCase()
  if (text === 'ADMISSION' || text === 'RESIDENT') {
    return text
  }
  return undefined
}

export function flattenBillCenterRouteQuery(query: LocationQuery | Record<string, unknown>) {
  const flattened: Record<string, string> = {}
  Object.entries(query || {}).forEach(([key, value]) => {
    const text = firstQueryValue(value)
    if (!text) {
      return
    }
    flattened[key] = text
  })
  return flattened
}

export function parseBillCenterRouteState(
  query: LocationQuery | Record<string, unknown>,
  options?: BillCenterRouteOptions
): BillCenterRouteState {
  const defaultPageNo = normalizePositiveInt(options?.defaultPageNo, 1)
  const defaultPageSize = normalizePositiveInt(options?.defaultPageSize, 10)
  const state: BillCenterRouteState = {
    pageNo: defaultPageNo,
    pageSize: defaultPageSize
  }
  const defaultMonth = normalizeMonthText(options?.defaultMonth)
  if (defaultMonth) {
    state.month = defaultMonth
  }
  const defaultScene = normalizeScene(options?.defaultScene)
  if (defaultScene) {
    state.scene = defaultScene
  }

  const month = normalizeMonthText((query as any).billMonth)
  if (month) {
    state.month = month
  }

  const keyword = firstQueryValue((query as any).billKeyword)
  if (keyword) {
    state.keyword = keyword
  }

  const payMethod = firstQueryValue((query as any).billPayMethod).toUpperCase()
  if (payMethod) {
    state.payMethod = payMethod
  }

  const pageNo = normalizePositiveInt((query as any).billPageNo, defaultPageNo)
  const pageSize = normalizePositiveInt((query as any).billPageSize, defaultPageSize)
  state.pageNo = pageNo
  state.pageSize = pageSize

  if (options?.lockScene) {
    state.scene = defaultScene
  } else {
    const scene = normalizeScene((query as any).billScene)
    state.scene = scene || defaultScene
  }
  return state
}

export function buildBillCenterRouteQuery(
  state: BillCenterRouteState,
  routeQuery: LocationQuery | Record<string, unknown>,
  options?: BillCenterRouteOptions
) {
  const query: Record<string, string> = {}
  Object.entries(flattenBillCenterRouteQuery(routeQuery)).forEach(([key, value]) => {
    if (BILL_CENTER_MANAGED_ROUTE_KEYS.has(key)) {
      return
    }
    query[key] = value
  })

  const month = normalizeMonthText(state.month)
  if (month) {
    query.billMonth = month
  }

  const keyword = firstQueryValue(state.keyword)
  if (keyword) {
    query.billKeyword = keyword
  }

  const payMethod = firstQueryValue(state.payMethod).toUpperCase()
  if (payMethod) {
    query.billPayMethod = payMethod
  }

  if (!options?.lockScene) {
    const scene = normalizeScene(state.scene)
    if (scene) {
      query.billScene = scene
    }
  }

  query.billPageNo = String(normalizePositiveInt(state.pageNo, options?.defaultPageNo || 1))
  query.billPageSize = String(normalizePositiveInt(state.pageSize, options?.defaultPageSize || 10))
  return query
}

export function buildBillCenterRouteSignature(query: LocationQuery | Record<string, unknown>) {
  const flattened = flattenBillCenterRouteQuery(query)
  return SORTED_BILL_CENTER_MANAGED_KEYS
    .map((key) => `${key}:${firstQueryValue(flattened[key])}`)
    .join('|')
}
