import type { Ref } from 'vue'
import type { LocationQuery, RouteLocationNormalizedLoaded, Router } from 'vue-router'
import type { LogisticsWorkbenchSummaryQuery } from '../types'
import {
  buildLogisticsWorkbenchRouteQuery,
  normalizeLogisticsWorkbenchQuery,
  parseLogisticsWorkbenchQueryPatch
} from '../utils/logisticsWorkbenchQuery'

export type TaskCenterTab = 'cleaning' | 'maintenance' | 'delivery' | 'inventory'
export type TaskCenterViewMode = 'ALL' | 'DUTY'
export type TaskCenterDensityMode = 'normal' | 'dense'
export type TaskCenterLifecycleFocus = TaskCenterTab | ''

export const TASK_CENTER_TAB_VALUES: TaskCenterTab[] = ['cleaning', 'maintenance', 'delivery', 'inventory']
export const TASK_CENTER_VIEW_VALUES: TaskCenterViewMode[] = ['ALL', 'DUTY']
export const TASK_CENTER_DENSITY_VALUES: TaskCenterDensityMode[] = ['normal', 'dense']
export const TASK_CENTER_MANAGED_ROUTE_KEYS = new Set([
  'windowDays',
  'expiryDays',
  'overdueDays',
  'maintenanceDueDays',
  'tab',
  'view',
  'density',
  'lifecycleFocus',
  'overdueOnly'
])

export function firstTaskCenterQueryValue(value: unknown): string {
  if (Array.isArray(value)) {
    return firstTaskCenterQueryValue(value[0])
  }
  if (value == null) {
    return ''
  }
  return String(value).trim()
}

export function flattenTaskCenterRouteQuery(query: LocationQuery | Record<string, unknown>) {
  const flattened: Record<string, string> = {}
  Object.entries(query || {}).forEach(([key, value]) => {
    const text = firstTaskCenterQueryValue(value)
    if (!text) return
    flattened[key] = text
  })
  return flattened
}

export function normalizeTaskCenterTab(value: unknown): TaskCenterTab {
  const raw = firstTaskCenterQueryValue(value)
  return TASK_CENTER_TAB_VALUES.includes(raw as TaskCenterTab) ? (raw as TaskCenterTab) : 'cleaning'
}

export function normalizeTaskCenterViewMode(value: unknown): TaskCenterViewMode {
  const raw = firstTaskCenterQueryValue(value).toUpperCase()
  return TASK_CENTER_VIEW_VALUES.includes(raw as TaskCenterViewMode) ? (raw as TaskCenterViewMode) : 'ALL'
}

export function normalizeTaskCenterDensityMode(value: unknown): TaskCenterDensityMode {
  const raw = firstTaskCenterQueryValue(value).toLowerCase()
  return TASK_CENTER_DENSITY_VALUES.includes(raw as TaskCenterDensityMode) ? (raw as TaskCenterDensityMode) : 'normal'
}

export function normalizeTaskCenterLifecycleFocus(value: unknown): TaskCenterLifecycleFocus {
  const normalized = normalizeTaskCenterTab(value)
  const raw = firstTaskCenterQueryValue(value)
  return raw && TASK_CENTER_TAB_VALUES.includes(normalized) ? normalized : ''
}

export function normalizeTaskCenterOverdueOnly(value: unknown): boolean {
  const raw = firstTaskCenterQueryValue(value).toLowerCase()
  return raw === '1' || raw === 'true' || raw === 'yes'
}

export function summaryQuerySignature(query: LogisticsWorkbenchSummaryQuery) {
  if (Object.keys(query || {}).length === 0) {
    return ''
  }
  const normalized = normalizeLogisticsWorkbenchQuery(query)
  return [normalized.windowDays, normalized.expiryDays, normalized.overdueDays, normalized.maintenanceDueDays].join('-')
}

export function useLogisticsTaskCenterRouteLayer(params: {
  route: RouteLocationNormalizedLoaded
  router: Router
  summaryQuery: Ref<LogisticsWorkbenchSummaryQuery>
  activeTab: Ref<TaskCenterTab>
  viewMode: Ref<TaskCenterViewMode>
  densityMode: Ref<TaskCenterDensityMode>
  lifecycleFocus: Ref<TaskCenterLifecycleFocus>
  overdueOnly: Ref<boolean>
  syncingRouteState: Ref<boolean>
}) {
  function syncStateFromRoute(query: LocationQuery) {
    params.syncingRouteState.value = true
    try {
      params.summaryQuery.value = parseLogisticsWorkbenchQueryPatch(query)
      params.activeTab.value = normalizeTaskCenterTab(query.tab)
      params.viewMode.value = normalizeTaskCenterViewMode(query.view)
      params.densityMode.value = normalizeTaskCenterDensityMode(query.density)
      params.lifecycleFocus.value = normalizeTaskCenterLifecycleFocus(query.lifecycleFocus)
      params.overdueOnly.value = normalizeTaskCenterOverdueOnly(query.overdueOnly)
    } finally {
      params.syncingRouteState.value = false
    }
  }

  function buildTaskCenterRouteQuery() {
    const unmanagedQuery: Record<string, string> = {}
    Object.entries(flattenTaskCenterRouteQuery(params.route.query)).forEach(([key, value]) => {
      if (TASK_CENTER_MANAGED_ROUTE_KEYS.has(key)) return
      unmanagedQuery[key] = value
    })
    const extraQuery = {
      ...unmanagedQuery,
      tab: params.activeTab.value,
      view: params.viewMode.value,
      density: params.densityMode.value
    }
    if (params.lifecycleFocus.value) {
      extraQuery.lifecycleFocus = params.lifecycleFocus.value
    }
    if (params.overdueOnly.value) {
      extraQuery.overdueOnly = '1'
    }
    if (Object.keys(params.summaryQuery.value || {}).length === 0) {
      return extraQuery
    }
    return buildLogisticsWorkbenchRouteQuery(params.summaryQuery.value, extraQuery)
  }

  function hasSameRouteQuery(nextQuery: Record<string, string>) {
    const currentQuery = flattenTaskCenterRouteQuery(params.route.query)
    const currentKeys = Object.keys(currentQuery)
    const nextKeys = Object.keys(nextQuery)
    if (currentKeys.length !== nextKeys.length) {
      return false
    }
    return nextKeys.every((key) => currentQuery[key] === nextQuery[key])
  }

  async function syncRouteQueryFromState() {
    if (params.syncingRouteState.value) {
      return
    }
    const nextQuery = buildTaskCenterRouteQuery()
    if (hasSameRouteQuery(nextQuery)) {
      return
    }
    await params.router.replace({ query: nextQuery })
  }

  return {
    syncStateFromRoute,
    buildTaskCenterRouteQuery,
    hasSameRouteQuery,
    syncRouteQueryFromState
  }
}
