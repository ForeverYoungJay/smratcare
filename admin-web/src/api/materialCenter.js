import request, { fetchPage } from '../utils/request';
export function getWarehousePage(params) {
    return fetchPage('/api/material/warehouse/page', params);
}
export function createWarehouse(data) {
    return request.post('/api/material/warehouse', data);
}
export function updateWarehouse(id, data) {
    return request.put(`/api/material/warehouse/${id}`, data);
}
export function deleteWarehouse(id) {
    return request.delete(`/api/material/warehouse/${id}`);
}
export function getSupplierPage(params) {
    return fetchPage('/api/material/supplier/page', params);
}
export function createSupplier(data) {
    return request.post('/api/material/supplier', data);
}
export function updateSupplier(id, data) {
    return request.put(`/api/material/supplier/${id}`, data);
}
export function deleteSupplier(id) {
    return request.delete(`/api/material/supplier/${id}`);
}
export function getPurchasePage(params) {
    return fetchPage('/api/material/purchase/page', params);
}
export function getPurchaseDetail(id) {
    return request.get(`/api/material/purchase/${id}`);
}
export function createPurchase(data) {
    return request.post('/api/material/purchase', data);
}
export function updatePurchase(id, data) {
    return request.put(`/api/material/purchase/${id}`, data);
}
export function approvePurchase(id) {
    return request.post(`/api/material/purchase/${id}/approve`);
}
export function completePurchase(id) {
    return request.post(`/api/material/purchase/${id}/complete`);
}
export function cancelPurchase(id) {
    return request.post(`/api/material/purchase/${id}/cancel`);
}
export function getTransferPage(params) {
    return fetchPage('/api/material/transfer/page', params);
}
export function getTransferDetail(id) {
    return request.get(`/api/material/transfer/${id}`);
}
export function createTransfer(data) {
    return request.post('/api/material/transfer', data);
}
export function updateTransfer(id, data) {
    return request.put(`/api/material/transfer/${id}`, data);
}
export function completeTransfer(id) {
    return request.post(`/api/material/transfer/${id}/complete`);
}
export function cancelTransfer(id) {
    return request.post(`/api/material/transfer/${id}/cancel`);
}
export function getStockAmount(params) {
    return request.get('/api/material/stock/amount', { params });
}
export function getInventoryBatchPage(params) {
    return fetchPage('/api/inventory/batch/page', params);
}
export function getInventoryLogPage(params) {
    return fetchPage('/api/inventory/log/page', params);
}
export function getInventoryInboundPage(params) {
    return fetchPage('/api/inventory/inbound/page', params);
}
export function getInventoryOutboundPage(params) {
    return fetchPage('/api/inventory/outbound/page', params);
}
export function getInventoryAdjustmentPage(params) {
    return fetchPage('/api/inventory/adjustment/page', params);
}
export function getInventoryAdjustmentDiffReport(params) {
    return request.get('/api/inventory/adjustment/diff-report', { params });
}
export function createInbound(data) {
    return request.post('/api/inventory/inbound', data);
}
export function adjustInventory(data) {
    return request.post('/api/inventory/adjust', data);
}
export function createOutbound(data) {
    return request.post('/api/inventory/outbound', data);
}
export function getInventoryAlerts() {
    return request.get('/api/inventory/alerts');
}
export function getInventoryExpiryAlerts() {
    return request.get('/api/inventory/expiry-alerts');
}
export function getMaterialCenterOverview(params) {
    return request.get('/api/material-center/overview', { params });
}
