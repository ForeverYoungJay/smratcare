import request, { fetchPage } from '../utils/request';
export function getCaregiverGroupPage(params) {
    return fetchPage('/api/nursing/groups/page', params);
}
export function getCaregiverGroupList(params) {
    return request.get('/api/nursing/groups/list', { params });
}
export function createCaregiverGroup(data) {
    return request.post('/api/nursing/groups', data);
}
export function updateCaregiverGroup(id, data) {
    return request.put(`/api/nursing/groups/${id}`, data);
}
export function deleteCaregiverGroup(id) {
    return request.delete(`/api/nursing/groups/${id}`);
}
export function getShiftTemplatePage(params) {
    return fetchPage('/api/nursing/shift-templates/page', params);
}
export function getShiftTemplateList(params) {
    return request.get('/api/nursing/shift-templates/list', { params });
}
export function createShiftTemplate(data) {
    return request.post('/api/nursing/shift-templates', data);
}
export function updateShiftTemplate(id, data) {
    return request.put(`/api/nursing/shift-templates/${id}`, data);
}
export function deleteShiftTemplate(id) {
    return request.delete(`/api/nursing/shift-templates/${id}`);
}
export function applyShiftTemplate(id, data) {
    return request.post(`/api/nursing/shift-templates/${id}/apply`, data);
}
export function getShiftHandoverPage(params) {
    return fetchPage('/api/nursing/handovers/page', params);
}
export function createShiftHandover(data) {
    return request.post('/api/nursing/handovers', data);
}
export function updateShiftHandover(id, data) {
    return request.put(`/api/nursing/handovers/${id}`, data);
}
export function deleteShiftHandover(id) {
    return request.delete(`/api/nursing/handovers/${id}`);
}
export function getCareLevelPage(params) {
    return fetchPage('/api/nursing/care-levels/page', params);
}
export function getCareLevelList(params) {
    return request.get('/api/nursing/care-levels/list', { params });
}
export function createCareLevel(data) {
    return request.post('/api/nursing/care-levels', data);
}
export function updateCareLevel(id, data) {
    return request.put(`/api/nursing/care-levels/${id}`, data);
}
export function deleteCareLevel(id) {
    return request.delete(`/api/nursing/care-levels/${id}`);
}
export function getServicePlanPage(params) {
    return fetchPage('/api/nursing/service-plans/page', params);
}
export function getServicePlanList(params) {
    return request.get('/api/nursing/service-plans/list', { params });
}
export function createServicePlan(data) {
    return request.post('/api/nursing/service-plans', data);
}
export function updateServicePlan(id, data) {
    return request.put(`/api/nursing/service-plans/${id}`, data);
}
export function deleteServicePlan(id) {
    return request.delete(`/api/nursing/service-plans/${id}`);
}
export function getServiceBookingPage(params) {
    return fetchPage('/api/nursing/service-bookings/page', params);
}
export function createServiceBooking(data) {
    return request.post('/api/nursing/service-bookings', data);
}
export function updateServiceBooking(id, data) {
    return request.put(`/api/nursing/service-bookings/${id}`, data);
}
export function deleteServiceBooking(id) {
    return request.delete(`/api/nursing/service-bookings/${id}`);
}
export function getNursingRecordPage(params) {
    return fetchPage('/api/nursing/records/page', params);
}
export function getNursingReportSummary(params) {
    return request.get('/api/nursing/reports/summary', { params });
}
