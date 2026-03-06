import request from '../utils/request';
import { exportCsvByRequest } from '../utils/export';
export function getCheckInStats(params) {
    return request.get('/api/stats/check-in', { params });
}
export function getConsumptionStats(params) {
    return request.get('/api/stats/consumption', { params });
}
export function getElderInfoStats(params) {
    return request.get('/api/stats/elder-info', { params });
}
export function getOrgMonthlyOperation(params) {
    return request.get('/api/stats/org/monthly-operation', { params });
}
export function getOrgElderFlow(params) {
    return request.get('/api/stats/org/elder-flow', { params });
}
export function getOrgBedUsage(params) {
    return request.get('/api/stats/org/bed-usage', { params });
}
export function getMonthlyRevenueStats(params) {
    return request.get('/api/stats/monthly-revenue', { params });
}
export function getElderFlowReport(params) {
    return request.get('/api/stats/elder-flow-report', { params });
}
export async function exportElderFlowReportCsv(params) {
    await exportCsvByRequest('/api/stats/elder-flow-report/export', params, `elder-flow-report-${new Date().toISOString().slice(0, 10)}.csv`);
}
export async function exportOrgMonthlyOperationCsv(params) {
    await exportCsvByRequest('/api/stats/org/monthly-operation/export', params, `org-monthly-operation-${new Date().toISOString().slice(0, 10)}.csv`);
}
export async function exportOrgElderFlowCsv(params) {
    await exportCsvByRequest('/api/stats/org/elder-flow/export', params, `org-elder-flow-${new Date().toISOString().slice(0, 10)}.csv`);
}
async function exportStatsCsv(urlPath, fallbackFilename, params) {
    await exportCsvByRequest(urlPath, params, fallbackFilename);
}
export function exportCheckInStatsCsv(params) {
    return exportStatsCsv('/api/stats/check-in/export', `check-in-stats-${new Date().toISOString().slice(0, 10)}.csv`, params);
}
export function exportConsumptionStatsCsv(params) {
    return exportStatsCsv('/api/stats/consumption/export', `consumption-stats-${new Date().toISOString().slice(0, 10)}.csv`, params);
}
export function exportElderInfoStatsCsv(params) {
    return exportStatsCsv('/api/stats/elder-info/export', `elder-info-stats-${new Date().toISOString().slice(0, 10)}.csv`, params);
}
export function exportOrgBedUsageCsv(params) {
    return exportStatsCsv('/api/stats/org/bed-usage/export', `org-bed-usage-${new Date().toISOString().slice(0, 10)}.csv`, params);
}
export function exportMonthlyRevenueStatsCsv(params) {
    return exportStatsCsv('/api/stats/monthly-revenue/export', `monthly-revenue-stats-${new Date().toISOString().slice(0, 10)}.csv`, params);
}
