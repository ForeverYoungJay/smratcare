import { buildLogisticsWorkbenchRouteQuery, normalizeLogisticsWorkbenchQuery, parseLogisticsWorkbenchQueryPatch } from '../utils/logisticsWorkbenchQuery';
export const TASK_CENTER_TAB_VALUES = ['cleaning', 'maintenance', 'delivery', 'inventory'];
export const TASK_CENTER_VIEW_VALUES = ['ALL', 'DUTY'];
export const TASK_CENTER_DENSITY_VALUES = ['normal', 'dense'];
export const TASK_CENTER_MANAGED_ROUTE_KEYS = new Set([
    'windowDays',
    'expiryDays',
    'overdueDays',
    'maintenanceDueDays',
    'tab',
    'view',
    'density'
]);
export function firstTaskCenterQueryValue(value) {
    if (Array.isArray(value)) {
        return firstTaskCenterQueryValue(value[0]);
    }
    if (value == null) {
        return '';
    }
    return String(value).trim();
}
export function flattenTaskCenterRouteQuery(query) {
    const flattened = {};
    Object.entries(query || {}).forEach(([key, value]) => {
        const text = firstTaskCenterQueryValue(value);
        if (!text)
            return;
        flattened[key] = text;
    });
    return flattened;
}
export function normalizeTaskCenterTab(value) {
    const raw = firstTaskCenterQueryValue(value);
    return TASK_CENTER_TAB_VALUES.includes(raw) ? raw : 'cleaning';
}
export function normalizeTaskCenterViewMode(value) {
    const raw = firstTaskCenterQueryValue(value).toUpperCase();
    return TASK_CENTER_VIEW_VALUES.includes(raw) ? raw : 'ALL';
}
export function normalizeTaskCenterDensityMode(value) {
    const raw = firstTaskCenterQueryValue(value).toLowerCase();
    return TASK_CENTER_DENSITY_VALUES.includes(raw) ? raw : 'normal';
}
export function summaryQuerySignature(query) {
    if (Object.keys(query || {}).length === 0) {
        return '';
    }
    const normalized = normalizeLogisticsWorkbenchQuery(query);
    return [normalized.windowDays, normalized.expiryDays, normalized.overdueDays, normalized.maintenanceDueDays].join('-');
}
export function useLogisticsTaskCenterRouteLayer(params) {
    function syncStateFromRoute(query) {
        params.syncingRouteState.value = true;
        try {
            params.summaryQuery.value = parseLogisticsWorkbenchQueryPatch(query);
            params.activeTab.value = normalizeTaskCenterTab(query.tab);
            params.viewMode.value = normalizeTaskCenterViewMode(query.view);
            params.densityMode.value = normalizeTaskCenterDensityMode(query.density);
        }
        finally {
            params.syncingRouteState.value = false;
        }
    }
    function buildTaskCenterRouteQuery() {
        const unmanagedQuery = {};
        Object.entries(flattenTaskCenterRouteQuery(params.route.query)).forEach(([key, value]) => {
            if (TASK_CENTER_MANAGED_ROUTE_KEYS.has(key))
                return;
            unmanagedQuery[key] = value;
        });
        const extraQuery = {
            ...unmanagedQuery,
            tab: params.activeTab.value,
            view: params.viewMode.value,
            density: params.densityMode.value
        };
        if (Object.keys(params.summaryQuery.value || {}).length === 0) {
            return extraQuery;
        }
        return buildLogisticsWorkbenchRouteQuery(params.summaryQuery.value, extraQuery);
    }
    function hasSameRouteQuery(nextQuery) {
        const currentQuery = flattenTaskCenterRouteQuery(params.route.query);
        const currentKeys = Object.keys(currentQuery);
        const nextKeys = Object.keys(nextQuery);
        if (currentKeys.length !== nextKeys.length) {
            return false;
        }
        return nextKeys.every((key) => currentQuery[key] === nextQuery[key]);
    }
    async function syncRouteQueryFromState() {
        if (params.syncingRouteState.value) {
            return;
        }
        const nextQuery = buildTaskCenterRouteQuery();
        if (hasSameRouteQuery(nextQuery)) {
            return;
        }
        await params.router.replace({ query: nextQuery });
    }
    return {
        syncStateFromRoute,
        buildTaskCenterRouteQuery,
        hasSameRouteQuery,
        syncRouteQueryFromState
    };
}
