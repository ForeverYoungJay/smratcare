import request, { fetchPage } from '../utils/request';
import { exportCsvByRequest } from '../utils/export';
export function getOutingPage(params) {
    return fetchPage('/api/elder/lifecycle/outing/page', params);
}
export function createOuting(data) {
    return request.post('/api/elder/lifecycle/outing', data);
}
export function returnOuting(id, data) {
    return request.put(`/api/elder/lifecycle/outing/${id}/return`, data);
}
export function getMedicalOutingPage(params) {
    return fetchPage('/api/elder/lifecycle/medical-outing/page', params);
}
export function createMedicalOuting(data) {
    return request.post('/api/elder/lifecycle/medical-outing', data);
}
export function returnMedicalOuting(id, data) {
    return request.put(`/api/elder/lifecycle/medical-outing/${id}/return`, data);
}
export function getTrialStayPage(params) {
    return fetchPage('/api/elder/lifecycle/trial-stay/page', params);
}
export function createTrialStay(data) {
    return request.post('/api/elder/lifecycle/trial-stay', data);
}
export function updateTrialStay(id, data) {
    return request.put(`/api/elder/lifecycle/trial-stay/${id}`, data);
}
export function getDischargeApplyPage(params) {
    return fetchPage('/api/elder/lifecycle/discharge-apply/page', params);
}
export function createDischargeApply(data) {
    return request.post('/api/elder/lifecycle/discharge-apply', data);
}
export function reviewDischargeApply(id, data) {
    return request.put(`/api/elder/lifecycle/discharge-apply/${id}/review`, data);
}
export function getDeathRegisterPage(params) {
    return fetchPage('/api/elder/lifecycle/death-register/page', params);
}
export function createDeathRegister(data) {
    return request.post('/api/elder/lifecycle/death-register', data);
}
export function updateDeathRegister(id, data) {
    return request.put(`/api/elder/lifecycle/death-register/${id}`, data);
}
export function cancelDeathRegister(id, data) {
    return request.put(`/api/elder/lifecycle/death-register/${id}/cancel`, data || {});
}
export function exportMedicalOuting(params) {
    return exportCsvByRequest('/api/elder/lifecycle/medical-outing/export', params, '外出就医登记.csv');
}
export function exportDeathRegister(params) {
    return exportCsvByRequest('/api/elder/lifecycle/death-register/export', params, '死亡登记.csv');
}
export function exportTrialStay(params) {
    return exportCsvByRequest('/api/elder/lifecycle/trial-stay/export', params, '试住登记.csv');
}
export function exportDischargeApply(params) {
    return exportCsvByRequest('/api/elder/lifecycle/discharge-apply/export', params, '退住申请.csv');
}
export function getResidenceStatusSummary() {
    return request.get('/api/elder/lifecycle/status-summary');
}
