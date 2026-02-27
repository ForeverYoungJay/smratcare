import request, { fetchPage } from '../utils/request';
import { exportCsvByRequest } from '../utils/export';
export function getMealPlanPage(params) {
    return fetchPage('/api/life/meal-plan/page', params);
}
export function createMealPlan(data) {
    return request.post('/api/life/meal-plan', data);
}
export function updateMealPlan(id, data) {
    return request.put(`/api/life/meal-plan/${id}`, data);
}
export function deleteMealPlan(id) {
    return request.delete(`/api/life/meal-plan/${id}`);
}
export function getActivityPage(params) {
    return fetchPage('/api/life/activity/page', params);
}
export function createActivity(data) {
    return request.post('/api/life/activity', data);
}
export function updateActivity(id, data) {
    return request.put(`/api/life/activity/${id}`, data);
}
export function deleteActivity(id) {
    return request.delete(`/api/life/activity/${id}`);
}
export function getIncidentPage(params) {
    return fetchPage('/api/life/incident/page', params);
}
export function createIncident(data) {
    return request.post('/api/life/incident', data);
}
export function updateIncident(id, data) {
    return request.put(`/api/life/incident/${id}`, data);
}
export function closeIncident(id) {
    return request.put(`/api/life/incident/${id}/close`);
}
export function deleteIncident(id) {
    return request.delete(`/api/life/incident/${id}`);
}
export function exportIncidentPage(params) {
    return exportCsvByRequest('/api/life/incident/export', params, '事故登记.csv');
}
export function getHealthBasicPage(params) {
    return fetchPage('/api/life/health-basic/page', params);
}
export function createHealthBasic(data) {
    return request.post('/api/life/health-basic', data);
}
export function updateHealthBasic(id, data) {
    return request.put(`/api/life/health-basic/${id}`, data);
}
export function deleteHealthBasic(id) {
    return request.delete(`/api/life/health-basic/${id}`);
}
export function getBirthdayPage(params) {
    return fetchPage('/api/life/birthday/page', params);
}
export function getRoomCleaningPage(params) {
    return fetchPage('/api/life/room-cleaning/page', params);
}
export function createRoomCleaning(data) {
    return request.post('/api/life/room-cleaning', data);
}
export function updateRoomCleaning(id, data) {
    return request.put(`/api/life/room-cleaning/${id}`, data);
}
export function completeRoomCleaning(id) {
    return request.put(`/api/life/room-cleaning/${id}/done`);
}
export function deleteRoomCleaning(id) {
    return request.delete(`/api/life/room-cleaning/${id}`);
}
export function getMaintenancePage(params) {
    return fetchPage('/api/life/maintenance/page', params);
}
export function createMaintenance(data) {
    return request.post('/api/life/maintenance', data);
}
export function updateMaintenance(id, data) {
    return request.put(`/api/life/maintenance/${id}`, data);
}
export function completeMaintenance(id) {
    return request.put(`/api/life/maintenance/${id}/complete`);
}
export function deleteMaintenance(id) {
    return request.delete(`/api/life/maintenance/${id}`);
}
