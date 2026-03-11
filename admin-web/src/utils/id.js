export function normalizeId(value) {
    const raw = Array.isArray(value) ? value[0] : value;
    const text = String(raw ?? '').trim();
    return text ? text : undefined;
}
export function normalizeResidentId(routeQuery) {
    return normalizeId(routeQuery.residentId ?? routeQuery.elderId);
}
