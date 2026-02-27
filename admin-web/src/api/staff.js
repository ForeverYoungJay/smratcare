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
export function updateStaffRoles(id, roleIds) {
    return request.post('/api/admin/staff-roles/assign', { staffId: id, roleIds });
}
