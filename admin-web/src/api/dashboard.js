import request from '../utils/request';
export function getDashboardSummary(config) {
    return request.get('/api/dashboard/summary', config);
}
export function getDashboardMetricCatalog() {
    return request.get('/api/dashboard/metric-catalog');
}
export function getDashboardThresholdDefaults() {
    return request.get('/api/dashboard/threshold-defaults');
}
