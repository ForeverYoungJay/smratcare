import request, { fetchPage } from '../utils/request';
export function getSurveyQuestionPage(params) {
    return fetchPage('/api/admin/survey/questions/page', params);
}
export function getSurveyQuestion(id) {
    return request.get(`/api/admin/survey/questions/${id}`);
}
export function createSurveyQuestion(data) {
    return request.post('/api/admin/survey/questions', data);
}
export function updateSurveyQuestion(id, data) {
    return request.put(`/api/admin/survey/questions/${id}`, data);
}
export function deleteSurveyQuestion(id) {
    return request.delete(`/api/admin/survey/questions/${id}`);
}
export function getSurveyTemplatePage(params) {
    return fetchPage('/api/admin/survey/templates/page', params);
}
export function getSurveyTemplateDetail(id) {
    return request.get(`/api/admin/survey/templates/${id}`);
}
export function createSurveyTemplate(data) {
    return request.post('/api/admin/survey/templates', data);
}
export function updateSurveyTemplate(id, data) {
    return request.put(`/api/admin/survey/templates/${id}`, data);
}
export function deleteSurveyTemplate(id) {
    return request.delete(`/api/admin/survey/templates/${id}`);
}
export function updateSurveyTemplateQuestions(id, questions) {
    return request.put(`/api/admin/survey/templates/${id}/questions`, { questions });
}
export function submitSurvey(data) {
    return request.post('/api/survey/submissions', data);
}
export function getSurveySubmissionPage(params) {
    return fetchPage('/api/survey/submissions/page', params);
}
export function getSurveyStatsSummary(params) {
    return request.get('/api/survey/stats/summary', { params });
}
export function getSurveyPerformance(params) {
    return request.get('/api/survey/stats/performance', { params });
}
