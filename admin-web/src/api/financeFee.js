import request, { fetchPage } from '../utils/request';
export function getAdmissionFeeAuditPage(params) {
    return fetchPage('/api/finance/fee/admission-audit/page', params);
}
export function createAdmissionFeeAudit(data) {
    return request.post('/api/finance/fee/admission-audit', data);
}
export function reviewAdmissionFeeAudit(id, data) {
    return request.put(`/api/finance/fee/admission-audit/${id}/review`, data);
}
export function getDischargeFeeAuditPage(params) {
    return fetchPage('/api/finance/fee/discharge-audit/page', params);
}
export function createDischargeFeeAudit(data) {
    return request.post('/api/finance/fee/discharge-audit', data);
}
export function reviewDischargeFeeAudit(id, data) {
    return request.put(`/api/finance/fee/discharge-audit/${id}/review`, data);
}
export function getDischargeSettlementPage(params) {
    return fetchPage('/api/finance/fee/discharge-settlement/page', params);
}
export function createDischargeSettlement(data) {
    return request.post('/api/finance/fee/discharge-settlement', data);
}
export function confirmDischargeSettlement(id, data) {
    return request.post(`/api/finance/fee/discharge-settlement/${id}/confirm`, data || {});
}
export function getConsumptionPage(params) {
    return fetchPage('/api/finance/fee/consumption/page', params);
}
export function createConsumption(data) {
    return request.post('/api/finance/fee/consumption', data);
}
export function getMonthlyAllocationPage(params) {
    return fetchPage('/api/finance/fee/monthly-allocation/page', params);
}
export function createMonthlyAllocation(data) {
    return request.post('/api/finance/fee/monthly-allocation', data);
}
