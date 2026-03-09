export function useReportQueryCache(cacheKey) {
    const storageKey = `marketing-report:${cacheKey}`;
    function restore(defaultValue) {
        try {
            const raw = localStorage.getItem(storageKey);
            if (!raw)
                return defaultValue || {};
            const parsed = JSON.parse(raw);
            return {
                ...(defaultValue || {}),
                ...(parsed || {})
            };
        }
        catch {
            return defaultValue || {};
        }
    }
    function persist(value) {
        try {
            localStorage.setItem(storageKey, JSON.stringify(value || {}));
        }
        catch {
            // ignore storage errors
        }
    }
    function clear() {
        try {
            localStorage.removeItem(storageKey);
        }
        catch {
            // ignore storage errors
        }
    }
    return {
        restore,
        persist,
        clear
    };
}
