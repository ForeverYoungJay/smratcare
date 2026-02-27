import request, { fetchPage } from '../utils/request';
import { exportCsvByRequest } from '../utils/export';
export function getFireSafetyRecordPage(params) {
    return fetchPage('/api/fire/records/page', params);
}
export function createFireSafetyRecord(data) {
    return request.post('/api/fire/records', data);
}
export function updateFireSafetyRecord(id, data) {
    return request.put(`/api/fire/records/${id}`, data);
}
export function closeFireSafetyRecord(id) {
    return request.put(`/api/fire/records/${id}/close`);
}
export function deleteFireSafetyRecord(id) {
    return request.delete(`/api/fire/records/${id}`);
}
export function getFireSafetySummary(params) {
    return request.get('/api/fire/records/summary', { params });
}
export function generateFireSafetyQr(id) {
    return request.post(`/api/fire/records/${id}/qr/generate`);
}
export function completeFireSafetyByScan(data) {
    return request.post('/api/fire/records/scan/complete', data);
}
export function getFireSafetyReportDetail(params) {
    return request.get('/api/fire/records/report/detail', { params });
}
export function exportFireSafetyReport(params) {
    return exportCsvByRequest('/api/fire/records/report/export', params, '消防巡查报表.csv');
}
export function exportFireSafetyMaintenanceLog(params) {
    return exportCsvByRequest('/api/fire/records/maintenance/export', params, '消防设施维护保养日志.csv');
}
