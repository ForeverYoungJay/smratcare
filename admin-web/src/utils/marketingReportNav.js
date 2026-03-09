function compactQuery(input) {
    return Object.fromEntries(Object.entries(input).filter(([, value]) => value !== undefined && value !== null && String(value) !== ''));
}
export function getReportCacheQuery(cacheKey) {
    try {
        const raw = localStorage.getItem(`marketing-report:${cacheKey}`);
        if (!raw)
            return {};
        const parsed = JSON.parse(raw);
        return compactQuery(parsed || {});
    }
    catch {
        return {};
    }
}
export function hasReportCache(cacheKey) {
    const query = getReportCacheQuery(cacheKey);
    return Object.keys(query).length > 0;
}
export function buildReportRoute(path, cacheKey, extraQuery) {
    return {
        path,
        query: compactQuery({
            ...getReportCacheQuery(cacheKey),
            ...(extraQuery || {})
        })
    };
}
