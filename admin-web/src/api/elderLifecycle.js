import request, { fetchPage } from '../utils/request';
import { exportCsvByRequest } from '../utils/export';
export function admitElder(data) {
    return request.post('/api/elder/lifecycle/admit', data);
}
export function dischargeElder(data) {
    return request.post('/api/elder/lifecycle/discharge', data);
}
export function getChangeLogs(params) {
    return fetchPage('/api/elder/lifecycle/changes', params);
}
export function getAdmissionRecords(params) {
    return fetchPage('/api/elder/lifecycle/admissions/page', params);
}
export function exportChangeLogs(params) {
    return exportCsvByRequest('/api/elder/lifecycle/changes/export', params, '变更记录.csv');
}
export function exportAdmissionRecords(params) {
    return exportCsvByRequest('/api/elder/lifecycle/admissions/export', params, '入住办理记录.csv');
}
