import request, { fetchPage } from '../utils/request';
export function getServiceItemPage(params) {
    return fetchPage('/api/standard/items/page', params);
}
export function listServiceItems(params) {
    return request.get('/api/standard/items/list', { params });
}
export function createServiceItem(data) {
    return request.post('/api/standard/items', data);
}
export function updateServiceItem(id, data) {
    return request.put(`/api/standard/items/${id}`, data);
}
export function deleteServiceItem(id) {
    return request.delete(`/api/standard/items/${id}`);
}
export function getCarePackagePage(params) {
    return fetchPage('/api/standard/packages/page', params);
}
export function listCarePackages(params) {
    return request.get('/api/standard/packages/list', { params });
}
export function createCarePackage(data) {
    return request.post('/api/standard/packages', data);
}
export function updateCarePackage(id, data) {
    return request.put(`/api/standard/packages/${id}`, data);
}
export function deleteCarePackage(id) {
    return request.delete(`/api/standard/packages/${id}`);
}
export function getPackageItemPage(params) {
    return fetchPage('/api/standard/package-items/page', params);
}
export function listPackageItems(params) {
    return request.get('/api/standard/package-items/list', { params });
}
export function createPackageItem(data) {
    return request.post('/api/standard/package-items', data);
}
export function updatePackageItem(id, data) {
    return request.put(`/api/standard/package-items/${id}`, data);
}
export function deletePackageItem(id) {
    return request.delete(`/api/standard/package-items/${id}`);
}
export function getElderPackagePage(params) {
    return fetchPage('/api/standard/elder-packages/page', params);
}
export function listElderPackages(params) {
    return request.get('/api/standard/elder-packages/list', { params });
}
export function createElderPackage(data) {
    return request.post('/api/standard/elder-packages', data);
}
export function updateElderPackage(id, data) {
    return request.put(`/api/standard/elder-packages/${id}`, data);
}
export function deleteElderPackage(id) {
    return request.delete(`/api/standard/elder-packages/${id}`);
}
export function generateTasksByPackage(data) {
    return request.post('/api/standard/tasks/generate', data);
}
