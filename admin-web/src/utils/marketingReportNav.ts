import type { RouteLocationRaw } from 'vue-router'

function compactQuery(input: Record<string, unknown>) {
  return Object.fromEntries(
    Object.entries(input).filter(([, value]) => value !== undefined && value !== null && String(value) !== '')
  )
}

export function getReportCacheQuery(cacheKey: string): Record<string, unknown> {
  try {
    const raw = localStorage.getItem(`marketing-report:${cacheKey}`)
    if (!raw) return {}
    const parsed = JSON.parse(raw) as Record<string, unknown>
    return compactQuery(parsed || {})
  } catch {
    return {}
  }
}

export function hasReportCache(cacheKey: string): boolean {
  const query = getReportCacheQuery(cacheKey)
  return Object.keys(query).length > 0
}

export function buildReportRoute(
  path: string,
  cacheKey: string,
  extraQuery?: Record<string, unknown>
): RouteLocationRaw {
  return {
    path,
    query: compactQuery({
      ...getReportCacheQuery(cacheKey),
      ...(extraQuery || {})
    })
  }
}
