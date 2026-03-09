function compactQuery(query) {
    return Object.fromEntries(Object.entries(query).filter(([, value]) => value !== undefined && value !== null && String(value) !== ''));
}
export function buildLeadRoute(entry, query) {
    return {
        path: `/marketing/leads/${entry}`,
        query: compactQuery(query || {})
    };
}
export function buildFollowupRoute(entry, query) {
    return {
        path: `/marketing/followup/${entry}`,
        query: compactQuery(query || {})
    };
}
export function buildReservationRoute(entry, query) {
    return {
        path: `/marketing/reservation/${entry}`,
        query: compactQuery(query || {})
    };
}
export function buildContractRoute(entry, query) {
    return {
        path: `/marketing/contracts/${entry}`,
        query: compactQuery(query || {})
    };
}
export function buildFunnelRoute(entry, query) {
    return {
        path: `/marketing/funnel/${entry}`,
        query: compactQuery(query || {})
    };
}
export function buildCallbackRoute(entry, query) {
    return {
        path: `/marketing/callback/${entry}`,
        query: compactQuery(query || {})
    };
}
export function routeToUnknownSourceOrAll(source) {
    const sourceText = String(source || '').trim();
    if (!sourceText || sourceText === '未标记渠道' || sourceText.includes('不明')) {
        return buildLeadRoute('unknown-source');
    }
    return buildLeadRoute('all', { source: sourceText });
}
