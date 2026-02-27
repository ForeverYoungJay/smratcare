import request, { fetchPage } from '../utils/request';
export function getMedicalCareWorkbenchSummary() {
    return request.get('/api/medical-care/workbench/summary');
}
export function getMedicalHealthCenterSummary(params) {
    return request.get('/api/medical-care/center/summary', { params });
}
export function getTcmAssessmentPage(params) {
    return fetchPage('/api/medical-care/tcm-assessments/page', params);
}
export function getTcmAssessmentSummary(params) {
    return request.get('/api/medical-care/tcm-assessments/summary', { params });
}
export function createTcmAssessment(data) {
    return request.post('/api/medical-care/tcm-assessments', data);
}
export function updateTcmAssessment(id, data) {
    return request.put(`/api/medical-care/tcm-assessments/${id}`, data);
}
export function publishTcmAssessment(id) {
    return request.post(`/api/medical-care/tcm-assessments/${id}/publish`);
}
export function deleteTcmAssessment(id) {
    return request.delete(`/api/medical-care/tcm-assessments/${id}`);
}
export function getCvdAssessmentPage(params) {
    return fetchPage('/api/medical-care/cvd-assessments/page', params);
}
export function getCvdAssessmentSummary(params) {
    return request.get('/api/medical-care/cvd-assessments/summary', { params });
}
export function createCvdAssessment(data) {
    return request.post('/api/medical-care/cvd-assessments', data);
}
export function updateCvdAssessment(id, data) {
    return request.put(`/api/medical-care/cvd-assessments/${id}`, data);
}
export function publishCvdAssessment(id, actions) {
    return request.post(`/api/medical-care/cvd-assessments/${id}/publish`, actions);
}
export function deleteCvdAssessment(id) {
    return request.delete(`/api/medical-care/cvd-assessments/${id}`);
}
export function getResidentRiskCard(residentId) {
    return request.get('/api/medical-care/resident360/risk-card', { params: { residentId } });
}
export function getResidentOverview(residentId) {
    return request.get('/api/medical-care/resident360/overview', { params: { residentId } });
}
