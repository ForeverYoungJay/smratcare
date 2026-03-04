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
export function getFinanceWorkbenchOverview() {
    return request.get('/api/finance/workbench/overview');
}
export function getFinanceInvoiceReceiptPage(params) {
    return fetchPage('/api/finance/workbench/invoice/page', params);
}
export function getFinanceAutoDebitExceptions(params) {
    return request.get('/api/finance/workbench/auto-deduct/exceptions', { params });
}
export function getFinanceRoomOpsDetail(params) {
    return request.get('/api/finance/workbench/room-ops/detail', { params });
}
export function getFinanceAllocationRules(params) {
    return request.get('/api/finance/workbench/allocation/rules', { params });
}
export function getFinanceReconcileExceptions(params) {
    return request.get('/api/finance/workbench/reconcile/exceptions', { params });
}
export function getFinanceMasterDataOverview(params) {
    return request.get('/api/finance/workbench/config/overview', { params });
}
export function getFinanceBillingConfig(params) {
    return request.get('/api/finance/workbench/billing-config', { params });
}
export function upsertFinanceBillingConfig(data) {
    return request.post('/api/finance/workbench/billing-config', data);
}
export function batchUpsertFinanceBillingConfig(data) {
    return request.post('/api/finance/workbench/billing-config/batch', data);
}
export function getFinanceConfigChangeLogPage(params) {
    return fetchPage('/api/finance/workbench/config/change-log/page', params);
}
export function rollbackFinanceBillingConfig(data) {
    return request.post('/api/finance/workbench/billing-config/rollback', data);
}
export function getFinanceBillingConfigSnapshots() {
    return request.get('/api/finance/workbench/billing-config/snapshots');
}
export function getFinanceModuleEntrySummary(params) {
    return request.get('/api/finance/workbench/module-entry', { params });
}
