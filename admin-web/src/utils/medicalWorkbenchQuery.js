const DATE_RE = /^\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\d|3[01])$/;
const NUMERIC_FIELDS = ['incidentWindowDays', 'overdueHours', 'topResidentLimit', 'riskResidentLookbackDays'];
export const MEDICAL_WORKBENCH_NUMERIC_LIMITS = {
    incidentWindowDays: { min: 1, max: 365 },
    overdueHours: { min: 1, max: 72 },
    topResidentLimit: { min: 1, max: 20 },
    riskResidentLookbackDays: { min: 7, max: 730 }
};
export const MEDICAL_WORKBENCH_QUERY_DEFAULTS = {
    incidentWindowDays: 30,
    overdueHours: 12,
    topResidentLimit: 5,
    riskResidentLookbackDays: 180
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
function normalizeNumeric(field, value, fallback) {
    const parsed = Number(value);
    if (!Number.isFinite(parsed)) {
        return fallback;
    }
    const limit = MEDICAL_WORKBENCH_NUMERIC_LIMITS[field];
    return clamp(Math.round(parsed), limit.min, limit.max);
}
function normalizeDateText(value) {
    const text = firstQueryValue(value);
    return DATE_RE.test(text) ? text : '';
}
function normalizeStatus(value) {
    const text = firstQueryValue(value);
    return text ? text.toUpperCase() : '';
}
function normalizeElderId(value) {
    const text = firstQueryValue(value);
    return text || undefined;
}
export function normalizeMedicalWorkbenchQuery(query, fallback = MEDICAL_WORKBENCH_QUERY_DEFAULTS) {
    const normalized = {
        incidentWindowDays: normalizeNumeric('incidentWindowDays', query?.incidentWindowDays, fallback.incidentWindowDays),
        overdueHours: normalizeNumeric('overdueHours', query?.overdueHours, fallback.overdueHours),
        topResidentLimit: normalizeNumeric('topResidentLimit', query?.topResidentLimit, fallback.topResidentLimit),
        riskResidentLookbackDays: normalizeNumeric('riskResidentLookbackDays', query?.riskResidentLookbackDays, fallback.riskResidentLookbackDays)
    };
    const date = normalizeDateText(query?.date);
    if (date) {
        normalized.date = date;
    }
    const status = normalizeStatus(query?.status);
    if (status) {
        normalized.status = status;
    }
    const elderId = normalizeElderId(query?.elderId);
    if (elderId) {
        normalized.elderId = elderId;
    }
    return normalized;
}
export function parseMedicalWorkbenchQueryPatch(routeQuery) {
    const patch = {};
    NUMERIC_FIELDS.forEach((field) => {
        const raw = firstQueryValue(routeQuery[field]);
        if (!raw) {
            return;
        }
        const parsed = Number(raw);
        if (!Number.isFinite(parsed)) {
            return;
        }
        patch[field] = normalizeNumeric(field, parsed, MEDICAL_WORKBENCH_QUERY_DEFAULTS[field]);
    });
    const date = normalizeDateText(routeQuery.date);
    if (date) {
        patch.date = date;
    }
    const status = normalizeStatus(routeQuery.status);
    if (status) {
        patch.status = status;
    }
    const elderId = normalizeElderId(routeQuery.elderId);
    if (elderId) {
        patch.elderId = elderId;
    }
    return patch;
}
export function buildMedicalWorkbenchRouteQuery(query, extra) {
    const normalized = normalizeMedicalWorkbenchQuery(query);
    const result = {
        incidentWindowDays: String(normalized.incidentWindowDays ?? MEDICAL_WORKBENCH_QUERY_DEFAULTS.incidentWindowDays),
        overdueHours: String(normalized.overdueHours ?? MEDICAL_WORKBENCH_QUERY_DEFAULTS.overdueHours),
        topResidentLimit: String(normalized.topResidentLimit ?? MEDICAL_WORKBENCH_QUERY_DEFAULTS.topResidentLimit),
        riskResidentLookbackDays: String(normalized.riskResidentLookbackDays ?? MEDICAL_WORKBENCH_QUERY_DEFAULTS.riskResidentLookbackDays)
    };
    if (normalized.date) {
        result.date = normalized.date;
    }
    if (normalized.status) {
        result.status = normalized.status;
    }
    if (normalized.elderId) {
        result.elderId = String(normalized.elderId);
    }
    Object.entries(extra || {}).forEach(([key, value]) => {
        if (value == null || value === '') {
            return;
        }
        result[key] = String(value);
    });
    return result;
}
