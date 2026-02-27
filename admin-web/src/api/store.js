import request, { fetchPage } from '../utils/request';
export function getProductPage(params) {
    return fetchPage('/api/store/product/page', params);
}
export function createProduct(data) {
    return request.post('/api/store/product', data);
}
export function updateProduct(id, data) {
    return request.put(`/api/store/product/${id}`, data);
}
export function getOrderPage(params) {
    return fetchPage('/api/store/order/page', params);
}
export function getOrderDetail(id) {
    return request.get(`/api/store/order/${id}`);
}
export function previewOrder(data) {
    return request.post('/api/store/order/preview', data);
}
export function submitOrder(data) {
    return request.post('/api/store/order/submit', data);
}
export function cancelOrder(data) {
    return request.post('/api/store/order/cancel', data);
}
export function refundOrder(data) {
    return request.post('/api/store/order/refund', data);
}
export function fulfillOrder(orderId) {
    return request.post('/api/store/order/fulfill', { orderId });
}
export async function getDiseaseList() {
    const res = await fetchPage('/api/admin/disease', { pageNo: 1, pageSize: 200 });
    return res.list;
}
export function createDisease(data) {
    return request.post('/api/admin/disease', data);
}
export function updateDisease(id, data) {
    return request.put(`/api/admin/disease/${id}`, data);
}
export function deleteDisease(id) {
    return request.delete(`/api/admin/disease/${id}`);
}
export async function getProductTagList() {
    const res = await fetchPage('/api/admin/product-tag', { pageNo: 1, pageSize: 500 });
    return res.list;
}
export async function getProductCategoryList(params) {
    const res = await fetchPage('/api/admin/product-category', { pageNo: 1, pageSize: 500, ...(params || {}) });
    return res.list;
}
export function createProductCategory(data) {
    return request.post('/api/admin/product-category', data);
}
export function updateProductCategory(id, data) {
    return request.put(`/api/admin/product-category/${id}`, data);
}
export function deleteProductCategory(id) {
    return request.delete(`/api/admin/product-category/${id}`);
}
export function createProductTag(data) {
    return request.post('/api/admin/product-tag', data);
}
export function updateProductTag(id, data) {
    return request.put(`/api/admin/product-tag/${id}`, data);
}
export function deleteProductTag(id) {
    return request.delete(`/api/admin/product-tag/${id}`);
}
export function getForbiddenTags(diseaseId) {
    return request.get(`/api/admin/disease/${diseaseId}/forbidden-tags`);
}
export function updateForbiddenTags(diseaseId, tagIds) {
    return request.put(`/api/admin/disease/${diseaseId}/forbidden-tags`, { tagIds });
}
export function getPointsAccountPage(params) {
    return fetchPage('/api/store/points/page', params);
}
export function getPointsLogPage(params) {
    return fetchPage('/api/store/points/log/page', params);
}
export function adjustPoints(data) {
    return request.post('/api/store/points/adjust', data);
}
