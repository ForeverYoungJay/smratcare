export type RecentVisitItem = {
  key: string
  path: string
  title: string
  visitedAt: number
}

const MAX_RECENT_VISITS = 12

function resolveStorageKey(scope = 'default') {
  return `smartcare_recent_visits_v1_${scope || 'default'}`
}

export function loadRecentVisits(scope?: string): RecentVisitItem[] {
  if (typeof window === 'undefined') return []
  try {
    const raw = window.localStorage.getItem(resolveStorageKey(scope))
    if (!raw) return []
    const parsed = JSON.parse(raw)
    if (!Array.isArray(parsed)) return []
    return parsed
      .map((item) => ({
        key: String(item?.key || item?.path || ''),
        path: String(item?.path || ''),
        title: String(item?.title || ''),
        visitedAt: Number(item?.visitedAt || 0)
      }))
      .filter((item) => item.path && item.title)
      .sort((left, right) => right.visitedAt - left.visitedAt)
      .slice(0, MAX_RECENT_VISITS)
  } catch {
    return []
  }
}

export function saveRecentVisit(scope: string | undefined, item: Omit<RecentVisitItem, 'visitedAt'> & { visitedAt?: number }) {
  if (typeof window === 'undefined') return
  const nextItem: RecentVisitItem = {
    key: item.key,
    path: item.path,
    title: item.title,
    visitedAt: item.visitedAt || Date.now()
  }
  const next = [nextItem, ...loadRecentVisits(scope).filter((entry) => entry.path !== item.path)].slice(0, MAX_RECENT_VISITS)
  window.localStorage.setItem(resolveStorageKey(scope), JSON.stringify(next))
}

