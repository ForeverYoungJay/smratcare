const THRESHOLD_FIELDS = [
    'abnormalTaskThreshold',
    'inventoryAlertThreshold',
    'bedOccupancyThreshold',
    'revenueDropThreshold'
];
const MONTH_TEXT_RE = /^\d{4}-(0[1-9]|1[0-2])$/;
export const DASHBOARD_THRESHOLD_LIMITS = {
    abnormalTaskThreshold: { min: 1, max: 999 },
    inventoryAlertThreshold: { min: 1, max: 9999 },
    bedOccupancyThreshold: { min: 1, max: 100 },
    revenueDropThreshold: { min: 1, max: 100 }
};
export const DEFAULT_THRESHOLD_SNAPSHOT = {
    abnormalTaskThreshold: 3,
    inventoryAlertThreshold: 10,
    bedOccupancyThreshold: 95,
    revenueDropThreshold: 5
};
export const DASHBOARD_THRESHOLD_PRESETS = Object.freeze([
    {
        label: '稳健',
        snapshot: { abnormalTaskThreshold: 5, inventoryAlertThreshold: 12, bedOccupancyThreshold: 92, revenueDropThreshold: 8 }
    },
    {
        label: '平衡',
        snapshot: { abnormalTaskThreshold: 3, inventoryAlertThreshold: 10, bedOccupancyThreshold: 95, revenueDropThreshold: 5 }
    },
    {
        label: '敏感',
        snapshot: { abnormalTaskThreshold: 2, inventoryAlertThreshold: 6, bedOccupancyThreshold: 90, revenueDropThreshold: 3 }
    }
]);
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
    return Math.min(max, Math.max(min, value));
}
function normalizeThresholdField(field, value, fallback) {
    const numeric = Number(value);
    if (!Number.isFinite(numeric)) {
        return fallback;
    }
    const limits = DASHBOARD_THRESHOLD_LIMITS[field];
    return clamp(Math.round(numeric), limits.min, limits.max);
}
function normalizeMonthText(value) {
    const text = firstQueryValue(value);
    return MONTH_TEXT_RE.test(text) ? text : '';
}
export function toThresholdSnapshot(snapshot, fallback = DEFAULT_THRESHOLD_SNAPSHOT) {
    return {
        abnormalTaskThreshold: normalizeThresholdField('abnormalTaskThreshold', snapshot?.abnormalTaskThreshold, fallback.abnormalTaskThreshold),
        inventoryAlertThreshold: normalizeThresholdField('inventoryAlertThreshold', snapshot?.inventoryAlertThreshold, fallback.inventoryAlertThreshold),
        bedOccupancyThreshold: normalizeThresholdField('bedOccupancyThreshold', snapshot?.bedOccupancyThreshold, fallback.bedOccupancyThreshold),
        revenueDropThreshold: normalizeThresholdField('revenueDropThreshold', snapshot?.revenueDropThreshold, fallback.revenueDropThreshold)
    };
}
export function mergeThresholdConfig(base, patch) {
    const baseSnapshot = toThresholdSnapshot(base, DEFAULT_THRESHOLD_SNAPSHOT);
    const mergedSnapshot = patch
        ? toThresholdSnapshot({ ...baseSnapshot, ...patch }, baseSnapshot)
        : baseSnapshot;
    return {
        ...base,
        ...mergedSnapshot
    };
}
export function thresholdSnapshotsEqual(left, right) {
    return THRESHOLD_FIELDS.every((field) => left[field] === right[field]);
}
export function parseThresholdQuery(query) {
    const patch = {};
    THRESHOLD_FIELDS.forEach((field) => {
        const raw = firstQueryValue(query[field]);
        if (!raw) {
            return;
        }
        const numeric = Number(raw);
        if (!Number.isFinite(numeric)) {
            return;
        }
        patch[field] = normalizeThresholdField(field, numeric, DEFAULT_THRESHOLD_SNAPSHOT[field]);
    });
    return patch;
}
export function buildThresholdQuery(snapshot) {
    const normalized = toThresholdSnapshot(snapshot, DEFAULT_THRESHOLD_SNAPSHOT);
    return THRESHOLD_FIELDS.reduce((result, field) => {
        result[field] = String(normalized[field]);
        return result;
    }, {});
}
export function buildWindowValue(from, to) {
    const safeFrom = normalizeMonthText(from);
    const safeTo = normalizeMonthText(to);
    if (!safeFrom || !safeTo) {
        return '';
    }
    return `${safeFrom}_${safeTo}`;
}
export function parseDashboardRouteFilters(query) {
    const filters = {};
    const metricVersion = firstQueryValue(query.metricVersion);
    if (metricVersion) {
        filters.metricVersion = metricVersion;
    }
    const from = normalizeMonthText(query.from);
    const to = normalizeMonthText(query.to);
    if (from && to) {
        filters.from = from;
        filters.to = to;
        return filters;
    }
    const windowValue = firstQueryValue(query.window);
    if (!windowValue || !windowValue.includes('_')) {
        return filters;
    }
    const [windowFrom, windowTo] = windowValue.split('_', 2);
    const safeFrom = normalizeMonthText(windowFrom);
    const safeTo = normalizeMonthText(windowTo);
    if (safeFrom && safeTo) {
        filters.from = safeFrom;
        filters.to = safeTo;
    }
    return filters;
}
export function buildDashboardShareQuery(payload) {
    const query = buildThresholdQuery(payload.threshold);
    const metricVersion = String(payload.metricVersion || '').trim();
    if (metricVersion) {
        query.metricVersion = metricVersion;
    }
    const windowValue = buildWindowValue(payload.from, payload.to);
    if (windowValue) {
        query.window = windowValue;
    }
    return query;
}
