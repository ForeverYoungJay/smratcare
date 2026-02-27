import request, { fetchPage } from '../utils/request';
export function getBaseConfigGroups() {
    return request.get('/api/base-config/items/groups');
}
export function getBaseConfigItemPage(params) {
    return fetchPage('/api/base-config/items/page', params);
}
export function getBaseConfigItemList(params) {
    return request.get('/api/base-config/items/list', { params });
}
export function createBaseConfigItem(data) {
    return request.post('/api/base-config/items', data);
}
export function updateBaseConfigItem(id, data) {
    return request.put(`/api/base-config/items/${id}`, data);
}
export function deleteBaseConfigItem(id) {
    return request.delete(`/api/base-config/items/${id}`);
}
export function batchUpdateBaseConfigStatus(ids, status) {
    return request.put('/api/base-config/items/batch/status', { ids, status });
}
export function exportBaseConfigItems(params) {
    return request.get('/api/base-config/items/export', { params, responseType: 'blob' });
}
export function importBaseConfigItems(data) {
    return request.post('/api/base-config/items/import', data);
}
export function previewImportBaseConfigItems(data) {
    return request.post('/api/base-config/items/import/preview', data);
}
export function downloadBaseConfigImportTemplate() {
    return request.get('/api/base-config/items/import-template', { responseType: 'blob' });
}
