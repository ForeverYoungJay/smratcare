import request, { fetchPage } from '../utils/request';
export function getPaymentRecordPage(params) {
    return fetchPage('/api/finance/payment/page', params);
}
export function reconcileDaily(params) {
    return request.post('/api/finance/reconcile', null, { params });
}
export function getReconcilePage(params) {
    return fetchPage('/api/finance/reconcile/page', params);
}
export function getFinanceBillDetail(billId) {
    return request.get(`/api/finance/bill/${billId}`);
}
export function getFinanceMonthlyRevenue(params) {
    return request.get('/api/finance/report/monthly-revenue', { params });
}
export function getFinanceArrearsTop(params) {
    return request.get('/api/finance/report/arrears-top', { params });
}
export function getFinanceStoreSales(params) {
    return request.get('/api/finance/report/store-sales', { params });
}
export function getElderAccountPage(params) {
    return fetchPage('/api/finance/account/page', params);
}
export function getElderAccountLogPage(params) {
    return fetchPage('/api/finance/account/log/page', params);
}
export function printElderAccountLogPdf(params) {
    return request.get('/api/finance/account/log/print', { params, responseType: 'blob' });
}
export function adjustElderAccount(data) {
    return request.post('/api/finance/account/adjust', data);
}
export function updateElderAccount(data) {
    return request.post('/api/finance/account/update', data);
}
export function getElderAccountWarnings() {
    return request.get('/api/finance/account/warnings');
}
