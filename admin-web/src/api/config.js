import request, { fetchPage } from '../utils/request';
export function getBillingConfigPage(params) {
    return fetchPage('/api/admin/billing/config', params);
}
export function updateBillingConfig(id, data) {
    return request.post('/api/admin/billing/config', { ...data, id });
}
