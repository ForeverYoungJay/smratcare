const QUERY_FIELDS = ['windowDays', 'expiryDays', 'overdueDays', 'maintenanceDueDays'];
export const LOGISTICS_WORKBENCH_QUERY_LIMITS = {
    windowDays: { min: 1, max: 180 },
    expiryDays: { min: 1, max: 365 },
    overdueDays: { min: 1, max: 30 },
    maintenanceDueDays: { min: 1, max: 180 }
};
export const LOGISTICS_WORKBENCH_QUERY_DEFAULTS = {
    windowDays: 30,
    expiryDays: 30,
    overdueDays: 2,
    maintenanceDueDays: 30
};
function firstQueryValue(value) {
    if (Array.isArray(value)) {
        return firstQueryValue(value[0]);
    }
    if (value == null) {
        return '';
    }
    return String(value).trim();
}
function clamp(value, min, max) {
    return Math.max(min, Math.min(max, value));
}
function normalizeField(field, value, fallback) {
    const parsed = Number(value);
    if (!Number.isFinite(parsed)) {
        return fallback;
    }
    const limits = LOGISTICS_WORKBENCH_QUERY_LIMITS[field];
    return clamp(Math.round(parsed), limits.min, limits.max);
}
export function normalizeLogisticsWorkbenchQuery(query, fallback = LOGISTICS_WORKBENCH_QUERY_DEFAULTS) {
    return {
        windowDays: normalizeField('windowDays', query?.windowDays, fallback.windowDays),
        expiryDays: normalizeField('expiryDays', query?.expiryDays, fallback.expiryDays),
        overdueDays: normalizeField('overdueDays', query?.overdueDays, fallback.overdueDays),
        maintenanceDueDays: normalizeField('maintenanceDueDays', query?.maintenanceDueDays, fallback.maintenanceDueDays)
    };
}
export function parseLogisticsWorkbenchQueryPatch(routeQuery) {
    const patch = {};
    QUERY_FIELDS.forEach((field) => {
        const raw = firstQueryValue(routeQuery[field]);
        if (!raw) {
            return;
        }
        const parsed = Number(raw);
        if (!Number.isFinite(parsed)) {
            return;
        }
        patch[field] = normalizeField(field, parsed, LOGISTICS_WORKBENCH_QUERY_DEFAULTS[field]);
    });
    return patch;
}
export function buildLogisticsWorkbenchRouteQuery(query, extra) {
    const normalized = normalizeLogisticsWorkbenchQuery(query);
    const result = {
        windowDays: String(normalized.windowDays),
        expiryDays: String(normalized.expiryDays),
        overdueDays: String(normalized.overdueDays),
        maintenanceDueDays: String(normalized.maintenanceDueDays)
    };
    Object.entries(extra || {}).forEach(([key, value]) => {
        if (value == null || value === '') {
            return;
        }
        result[key] = String(value);
    });
    return result;
}
