import request, { fetchPage } from '../utils/request';
export function getTodayTasks(params) {
    return fetchPage('/api/care/tasks/today', params);
}
export function getTaskPage(params) {
    return fetchPage('/api/care/tasks/page', params);
}
export function getTaskSummary(params) {
    return request.get('/api/care/tasks/summary', { params });
}
export function getTaskExecuteLogs(taskDailyId) {
    return request.get('/api/care/tasks/logs', { params: { taskDailyId } });
}
export function executeTask(data) {
    return request.post('/api/care/tasks/execute', data);
}
export function generateTasks(params) {
    return request.post('/api/care/tasks/generate', null, { params });
}
export function assignTask(data) {
    return request.post('/api/care/tasks/assign', data);
}
export function assignTaskBatch(data) {
    return request.post('/api/care/tasks/assign/batch', data);
}
export function reviewTask(data) {
    return request.post('/api/care/tasks/review', data);
}
export function createTask(data) {
    return request.post('/api/care/tasks/create', data);
}
export function getTemplatePage(params) {
    return fetchPage('/api/care/template/page', params);
}
export function createTemplate(data) {
    return request.post('/api/care/template', data);
}
export function updateTemplate(id, data) {
    return request.put(`/api/care/template/${id}`, data);
}
export function deleteTemplate(id) {
    return request.delete(`/api/care/template/${id}`);
}
