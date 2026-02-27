import request, { fetchPage } from '../utils/request';
export function getDiningDishPage(params) {
    return fetchPage('/api/life/dining/dish/page', params);
}
export function getDiningDishList(params) {
    return request.get('/api/life/dining/dish/list', { params });
}
export function createDiningDish(data) {
    return request.post('/api/life/dining/dish', data);
}
export function updateDiningDish(id, data) {
    return request.put(`/api/life/dining/dish/${id}`, data);
}
export function updateDiningDishStatus(id, status) {
    return request.put(`/api/life/dining/dish/${id}/status`, null, { params: { status } });
}
export function deleteDiningDish(id) {
    return request.delete(`/api/life/dining/dish/${id}`);
}
export function getDiningRecipePage(params) {
    return fetchPage('/api/life/dining/recipe/page', params);
}
export function createDiningRecipe(data) {
    return request.post('/api/life/dining/recipe', data);
}
export function updateDiningRecipe(id, data) {
    return request.put(`/api/life/dining/recipe/${id}`, data);
}
export function deleteDiningRecipe(id) {
    return request.delete(`/api/life/dining/recipe/${id}`);
}
export function getDiningPrepZonePage(params) {
    return fetchPage('/api/life/dining/prep-zone/page', params);
}
export function getDiningPrepZoneList(params) {
    return request.get('/api/life/dining/prep-zone/list', { params });
}
export function createDiningPrepZone(data) {
    return request.post('/api/life/dining/prep-zone', data);
}
export function updateDiningPrepZone(id, data) {
    return request.put(`/api/life/dining/prep-zone/${id}`, data);
}
export function deleteDiningPrepZone(id) {
    return request.delete(`/api/life/dining/prep-zone/${id}`);
}
export function getDiningDeliveryAreaPage(params) {
    return fetchPage('/api/life/dining/delivery-area/page', params);
}
export function getDiningDeliveryAreaList(params) {
    return request.get('/api/life/dining/delivery-area/list', { params });
}
export function createDiningDeliveryArea(data) {
    return request.post('/api/life/dining/delivery-area', data);
}
export function updateDiningDeliveryArea(id, data) {
    return request.put(`/api/life/dining/delivery-area/${id}`, data);
}
export function deleteDiningDeliveryArea(id) {
    return request.delete(`/api/life/dining/delivery-area/${id}`);
}
export function getDiningMealOrderPage(params) {
    return fetchPage('/api/life/dining/order/page', params);
}
export function createDiningMealOrder(data) {
    return request.post('/api/life/dining/order', data);
}
export function checkDiningMealOrderRisk(data) {
    return request.post('/api/life/dining/order/risk-check', data);
}
export function applyDiningRiskOverride(data) {
    return request.post('/api/life/dining/order/risk-override/apply', data);
}
export function reviewDiningRiskOverride(id, data) {
    return request.put(`/api/life/dining/order/risk-override/${id}/review`, data);
}
export function getDiningRiskOverridePage(params) {
    return fetchPage('/api/life/dining/order/risk-override/page', params);
}
export function updateDiningMealOrder(id, data) {
    return request.put(`/api/life/dining/order/${id}`, data);
}
export function updateDiningMealOrderStatus(id, status) {
    return request.put(`/api/life/dining/order/${id}/status`, null, { params: { status } });
}
export function deleteDiningMealOrder(id) {
    return request.delete(`/api/life/dining/order/${id}`);
}
export function getDiningDeliveryRecordPage(params) {
    return fetchPage('/api/life/dining/delivery-record/page', params);
}
export function createDiningDeliveryRecord(data) {
    return request.post('/api/life/dining/delivery-record', data);
}
export function updateDiningDeliveryRecord(id, data) {
    return request.put(`/api/life/dining/delivery-record/${id}`, data);
}
export function deleteDiningDeliveryRecord(id) {
    return request.delete(`/api/life/dining/delivery-record/${id}`);
}
export function getDiningStatsSummary(params) {
    return request.get('/api/life/dining/stats/summary', { params });
}
export function getDiningRiskThresholdList() {
    return request.get('/api/life/dining/risk-threshold/list');
}
export function upsertDiningRiskThreshold(data) {
    return request.post('/api/life/dining/risk-threshold/upsert', data);
}
