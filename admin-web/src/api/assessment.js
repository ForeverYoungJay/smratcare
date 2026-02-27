import request, { fetchPage } from '../utils/request';
export function getAssessmentRecordPage(params) {
    return fetchPage('/api/assessment/records/page', params);
}
export function getAssessmentRecordSummary(params) {
    return request.get('/api/assessment/records/summary', { params });
}
export function getAssessmentRecord(id) {
    return request.get(`/api/assessment/records/${id}`);
}
export function createAssessmentRecord(data) {
    return request.post('/api/assessment/records', data);
}
export function updateAssessmentRecord(id, data) {
    return request.put(`/api/assessment/records/${id}`, data);
}
export function deleteAssessmentRecord(id) {
    return request.delete(`/api/assessment/records/${id}`);
}
export function batchDeleteAssessmentRecord(ids) {
    return request.post('/api/assessment/records/batch-delete', ids);
}
export function exportAssessmentRecord(params) {
    return request.get('/api/assessment/records/export', {
        params,
        responseType: 'blob'
    });
}
export function getAssessmentTemplatePage(params) {
    return fetchPage('/api/assessment/templates/page', params);
}
export function getAssessmentTemplateList(params) {
    return request.get('/api/assessment/templates/list', { params });
}
export function createAssessmentTemplate(data) {
    return request.post('/api/assessment/templates', data);
}
export function updateAssessmentTemplate(id, data) {
    return request.put(`/api/assessment/templates/${id}`, data);
}
export function deleteAssessmentTemplate(id) {
    return request.delete(`/api/assessment/templates/${id}`);
}
export function previewAssessmentScore(data) {
    return request.post('/api/assessment/templates/score-preview', data);
}
