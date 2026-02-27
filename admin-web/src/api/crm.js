import request, { fetchPage } from '../utils/request';
export function getCrmLeadPage(params) {
    return fetchPage('/api/crm/leads/page', params);
}
export function getCrmLead(id) {
    return request.get(`/api/crm/leads/${id}`);
}
export function createCrmLead(data) {
    return request.post('/api/crm/leads', data);
}
export function updateCrmLead(id, data) {
    return request.put(`/api/crm/leads/${id}`, data);
}
export function deleteCrmLead(id) {
    return request.delete(`/api/crm/leads/${id}`);
}
