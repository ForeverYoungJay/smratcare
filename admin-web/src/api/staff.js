import request, { fetchPage } from '../utils/request';
export function getStaffPage(params) {
    return fetchPage('/api/admin/staff', params);
}
export function createStaff(data) {
    return request.post('/api/admin/staff', data);
}
export function updateStaff(id, data) {
    return request.put('/api/admin/staff', { ...data, id });
}
export function getStaff(id) {
    return request.get(`/api/admin/staff/${id}`);
}
export function lockStaff(id) {
    return request.put(`/api/admin/staff/${id}/lock`);
}
export function unlockStaff(id) {
    return request.put(`/api/admin/staff/${id}/unlock`);
}
export function updateStaffRoles(id, roleIds) {
    return request.post('/api/admin/staff-roles/assign', { staffId: id, roleIds });
}
export function getStaffSupervisorAnomalies() {
    return request.get('/api/admin/staff/supervisor-anomalies');
}
