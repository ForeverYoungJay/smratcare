import request, { fetchPage } from '../utils/request';
export function getBillPage(params) {
    return fetchPage('/api/bill/page', params);
}
export function getBillDetail(elderId, month) {
    return request.get(`/api/bill/${elderId}`, { params: { month } });
}
export function generateBill(month) {
    return request.post('/api/bill/generate', null, { params: { month } });
}
export function payBill(billId, data) {
    return request.post(`/api/bill/${billId}/pay`, data);
}
