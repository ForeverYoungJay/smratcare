import request, { fetchPage } from '../utils/request';
import { exportCsvByRequest } from '../utils/export';
export function getLogisticsWorkbenchSummary() {
    return request.get('/api/logistics/workbench/summary');
}
export function getLogisticsEquipmentPage(params) {
    return fetchPage('/api/logistics/equipment/page', params);
}
export function createLogisticsEquipment(data) {
    return request.post('/api/logistics/equipment', data);
}
export function updateLogisticsEquipment(id, data) {
    return request.put(`/api/logistics/equipment/${id}`, data);
}
export function deleteLogisticsEquipment(id) {
    return request.delete(`/api/logistics/equipment/${id}`);
}
export function generateEquipmentMaintenance(id) {
    return request.put(`/api/logistics/equipment/${id}/generate-maintenance`);
}
export function generateEquipmentMaintenanceTodos(days = 30) {
    return request.post('/api/logistics/equipment/generate-maintenance-todos', null, { params: { days } });
}
export function getMaintenanceTodoJobLogPage(params) {
    return fetchPage('/api/logistics/maintenance-todo-job-log/page', params);
}
export function rerunMaintenanceTodoJobLog(id) {
    return request.post(`/api/logistics/maintenance-todo-job-log/${id}/rerun`);
}
export function rerunLatestFailedMaintenanceTodoJobLog() {
    return request.post('/api/logistics/maintenance-todo-job-log/rerun-latest-failed');
}
export function getMaintenanceTodoJobLogOverview(days = 7) {
    return request.get('/api/logistics/maintenance-todo-job-log/overview', { params: { days } });
}
export function exportMaintenanceTodoJobLog(params) {
    return exportCsvByRequest('/api/logistics/maintenance-todo-job-log/export', params, '维保待办执行日志.csv');
}
