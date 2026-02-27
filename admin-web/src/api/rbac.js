import request, { fetchPage } from '../utils/request';
export function getOrgPage(params) {
    return fetchPage('/api/admin/orgs', params);
}
export function getDepartmentPage(params) {
    return fetchPage('/api/admin/departments', params);
}
export function getRolePage(params) {
    return fetchPage('/api/admin/roles', params);
}
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
    const body = { staffId: id, roleIds };
    return request.post('/api/admin/staff-roles/assign', body);
}
export function appendStaffRole(id, roleId) {
    return request.post('/api/admin/staff-roles/add', { staffId: id, roleId });
}
export function removeStaffRole(id, roleId) {
    return request.delete('/api/admin/staff-roles/remove', { data: { staffId: id, roleId } });
}
export function createRole(data) {
    return request.post('/api/admin/roles', data);
}
export function updateRole(id, data) {
    return request.put(`/api/admin/roles/${id}`, data);
}
export function deleteRole(id) {
    return request.delete(`/api/admin/roles/${id}`);
}
export function createDepartment(data) {
    return request.post('/api/admin/departments', data);
}
export function updateDepartment(id, data) {
    return request.put(`/api/admin/departments/${id}`, data);
}
export function deleteDepartment(id) {
    return request.delete(`/api/admin/departments/${id}`);
}
export function createOrg(data) {
    return request.post('/api/admin/orgs', data);
}
export function updateOrg(id, data) {
    return request.put(`/api/admin/orgs/${id}`, data);
}
export function deleteOrg(id) {
    return request.delete(`/api/admin/orgs/${id}`);
}
