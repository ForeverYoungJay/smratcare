export function useReportQueryCache<T extends Record<string, any>>(cacheKey: string) {
  const storageKey = `marketing-report:${cacheKey}`

  function restore(defaultValue?: Partial<T>): Partial<T> {
    try {
      const raw = localStorage.getItem(storageKey)
      if (!raw) return defaultValue || {}
      const parsed = JSON.parse(raw) as Partial<T>
      return {
        ...(defaultValue || {}),
        ...(parsed || {})
      }
    } catch {
      return defaultValue || {}
    }
  }

  function persist(value: Partial<T>) {
    try {
      localStorage.setItem(storageKey, JSON.stringify(value || {}))
    } catch {
      // ignore storage errors
    }
  }

  function clear() {
    try {
      localStorage.removeItem(storageKey)
    } catch {
      // ignore storage errors
    }
  }

  return {
    restore,
    persist,
    clear
  }
}
