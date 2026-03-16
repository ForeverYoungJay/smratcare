import request, { fetchPage } from '../utils/request';
import { getToken } from '../utils/auth';
export function getPaymentRecordPage(params) {
    return fetchPage('/api/finance/payment/page', params);
}
export function updatePaymentRecord(paymentId, data) {
    return request.put(`/api/finance/payment/${paymentId}`, data);
}
export function reconcileDaily(params) {
    return request.post('/api/finance/reconcile', null, { params });
}
export function getReconcilePage(params) {
    return fetchPage('/api/finance/reconcile/page', params);
}
export function getFinanceBillDetail(billId, config) {
    return request.get(`/api/finance/bill/${billId}`, config);
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
export function getFinanceWorkbenchOverview(config) {
    return request.get('/api/finance/workbench/overview', config);
}
export function getFinanceOpsInsights() {
    return request.get('/api/finance/workbench/ops-insights');
}
export function getFinanceLedgerHealth(params) {
    return request.get('/api/finance/workbench/ledger/health', { params });
}
export function getFinanceDischargeStatusSync(params) {
    return request.get('/api/finance/workbench/discharge/status-sync', { params });
}
export function executeFinanceDischargeStatusSync(data) {
    return request.post('/api/finance/workbench/discharge/status-sync/execute', data);
}
export async function exportFinanceLedgerHealthCsv(params) {
    const url = new URL('/api/finance/workbench/ledger/health/export', window.location.origin);
    Object.entries(params || {}).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
            url.searchParams.set(key, String(value));
        }
    });
    const response = await fetch(url.toString(), {
        method: 'GET',
        headers: { Authorization: `Bearer ${getToken()}` }
    });
    if (!response.ok) {
        throw new Error('导出失败');
    }
    const blob = await response.blob();
    const contentDisposition = response.headers.get('content-disposition') || '';
    const filenameMatch = contentDisposition.match(/filename=\"?([^\";]+)\"?/);
    const filename = filenameMatch?.[1] || `finance-ledger-health-${new Date().toISOString().slice(0, 10)}.csv`;
    const link = document.createElement('a');
    const objectUrl = URL.createObjectURL(blob);
    link.href = objectUrl;
    link.download = filename;
    link.click();
    URL.revokeObjectURL(objectUrl);
}
export function getFinanceInvoiceReceiptPage(params) {
    return fetchPage('/api/finance/workbench/invoice/page', params);
}
export function getFinancePaymentAdjustmentPage(params) {
    return fetchPage('/api/finance/workbench/payment-adjustments/page', params);
}
export async function exportFinancePaymentAdjustmentCsv(params) {
    const url = new URL('/api/finance/workbench/payment-adjustments/export', window.location.origin);
    Object.entries(params || {}).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
            url.searchParams.set(key, String(value));
        }
    });
    const response = await fetch(url.toString(), {
        method: 'GET',
        headers: { Authorization: `Bearer ${getToken()}` }
    });
    if (!response.ok) {
        throw new Error('导出失败');
    }
    const blob = await response.blob();
    const contentDisposition = response.headers.get('content-disposition') || '';
    const filenameMatch = contentDisposition.match(/filename=\"?([^\";]+)\"?/);
    const filename = filenameMatch?.[1] || `finance-payment-adjustments-${new Date().toISOString().slice(0, 10)}.csv`;
    const link = document.createElement('a');
    const objectUrl = URL.createObjectURL(blob);
    link.href = objectUrl;
    link.download = filename;
    link.click();
    URL.revokeObjectURL(objectUrl);
}
export function getFinanceIssueCenterPage(params) {
    return fetchPage('/api/finance/workbench/issue-center/page', params);
}
export async function exportFinanceIssueCenterCsv(params) {
    const url = new URL('/api/finance/workbench/issue-center/export', window.location.origin);
    Object.entries(params || {}).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
            url.searchParams.set(key, String(value));
        }
    });
    const response = await fetch(url.toString(), {
        method: 'GET',
        headers: { Authorization: `Bearer ${getToken()}` }
    });
    if (!response.ok) {
        throw new Error('导出失败');
    }
    const blob = await response.blob();
    const contentDisposition = response.headers.get('content-disposition') || '';
    const filenameMatch = contentDisposition.match(/filename=\"?([^\";]+)\"?/);
    const filename = filenameMatch?.[1] || `finance-issue-center-${new Date().toISOString().slice(0, 10)}.csv`;
    const link = document.createElement('a');
    const objectUrl = URL.createObjectURL(blob);
    link.href = objectUrl;
    link.download = filename;
    link.click();
    URL.revokeObjectURL(objectUrl);
}
export function getFinanceCollectionFollowUpPage(params) {
    return fetchPage('/api/finance/workbench/collection-follow-up/page', params);
}
export async function exportFinanceCollectionFollowUpCsv(params) {
    const url = new URL('/api/finance/workbench/collection-follow-up/export', window.location.origin);
    Object.entries(params || {}).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
            url.searchParams.set(key, String(value));
        }
    });
    const response = await fetch(url.toString(), {
        method: 'GET',
        headers: { Authorization: `Bearer ${getToken()}` }
    });
    if (!response.ok) {
        throw new Error('导出失败');
    }
    const blob = await response.blob();
    const contentDisposition = response.headers.get('content-disposition') || '';
    const filenameMatch = contentDisposition.match(/filename=\"?([^\";]+)\"?/);
    const filename = filenameMatch?.[1] || `finance-collection-follow-up-${new Date().toISOString().slice(0, 10)}.csv`;
    const link = document.createElement('a');
    const objectUrl = URL.createObjectURL(blob);
    link.href = objectUrl;
    link.download = filename;
    link.click();
    URL.revokeObjectURL(objectUrl);
}
export function getFinanceMonthCloseSummary(params) {
    return request.get('/api/finance/workbench/month-close/summary', { params });
}
export function getFinanceMonthCloseStatus(params) {
    return request.get('/api/finance/workbench/month-close/status', { params });
}
export async function exportFinanceMonthCloseCsv(params) {
    const url = new URL('/api/finance/workbench/month-close/export', window.location.origin);
    Object.entries(params || {}).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
            url.searchParams.set(key, String(value));
        }
    });
    const response = await fetch(url.toString(), {
        method: 'GET',
        headers: { Authorization: `Bearer ${getToken()}` }
    });
    if (!response.ok) {
        throw new Error('导出失败');
    }
    const blob = await response.blob();
    const contentDisposition = response.headers.get('content-disposition') || '';
    const filenameMatch = contentDisposition.match(/filename=\"?([^\";]+)\"?/);
    const filename = filenameMatch?.[1] || `finance-month-close-${new Date().toISOString().slice(0, 10)}.csv`;
    const link = document.createElement('a');
    const objectUrl = URL.createObjectURL(blob);
    link.href = objectUrl;
    link.download = filename;
    link.click();
    URL.revokeObjectURL(objectUrl);
}
export function handleFinanceIssueCenter(data) {
    return request.post('/api/finance/workbench/issue-center/handle', data);
}
export function getFinanceIssueHandleLogs(params) {
    return request.get('/api/finance/workbench/issue-center/handle-logs', { params });
}
export function handleFinanceCollectionFollowUp(data) {
    return request.post('/api/finance/workbench/collection-follow-up/handle', data);
}
export function getFinanceCollectionFollowUpLogs(params) {
    return request.get('/api/finance/workbench/collection-follow-up/logs', { params });
}
export function executeFinanceMonthClose(data) {
    return request.post('/api/finance/workbench/month-close/execute', data);
}
export function unlockFinanceMonthClose(data) {
    return request.post('/api/finance/workbench/month-close/unlock', data);
}
export function requestFinanceCrossPeriodApproval(data) {
    return request.post('/api/finance/workbench/month-close/cross-period/request', data);
}
export function requestFinancePaymentAdjustmentApproval(data) {
    return request.post('/api/finance/workbench/payment-adjustments/approval/request', data);
}
export function financeSearch(params) {
    return request.get('/api/finance/workbench/search', { params });
}
export async function exportFinanceSearchCsv(params) {
    const url = new URL('/api/finance/workbench/search/export', window.location.origin);
    Object.entries(params || {}).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
            url.searchParams.set(key, String(value));
        }
    });
    const response = await fetch(url.toString(), {
        method: 'GET',
        headers: { Authorization: `Bearer ${getToken()}` }
    });
    if (!response.ok) {
        throw new Error('导出失败');
    }
    const blob = await response.blob();
    const contentDisposition = response.headers.get('content-disposition') || '';
    const filenameMatch = contentDisposition.match(/filename=\"?([^\";]+)\"?/);
    const filename = filenameMatch?.[1] || `finance-search-${new Date().toISOString().slice(0, 10)}.csv`;
    const link = document.createElement('a');
    const objectUrl = URL.createObjectURL(blob);
    link.href = objectUrl;
    link.download = filename;
    link.click();
    URL.revokeObjectURL(objectUrl);
}
export async function exportFinanceInvoiceReceiptCsv(params) {
    const url = new URL('/api/finance/workbench/invoice/export', window.location.origin);
    Object.entries(params || {}).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
            url.searchParams.set(key, String(value));
        }
    });
    const response = await fetch(url.toString(), {
        method: 'GET',
        headers: {
            Authorization: `Bearer ${getToken()}`
        }
    });
    if (!response.ok) {
        throw new Error('导出失败');
    }
    const blob = await response.blob();
    const contentDisposition = response.headers.get('content-disposition') || '';
    const filenameMatch = contentDisposition.match(/filename=\"?([^\";]+)\"?/);
    const filename = filenameMatch?.[1] || `finance-invoice-receipt-${new Date().toISOString().slice(0, 10)}.csv`;
    const link = document.createElement('a');
    const objectUrl = URL.createObjectURL(blob);
    link.href = objectUrl;
    link.download = filename;
    link.click();
    URL.revokeObjectURL(objectUrl);
}
export function getFinanceAutoDebitExceptions(params) {
    return request.get('/api/finance/workbench/auto-deduct/exceptions', { params });
}
export async function exportFinanceAutoDebitExceptionsCsv(params) {
    const url = new URL('/api/finance/workbench/auto-deduct/exceptions/export', window.location.origin);
    Object.entries(params || {}).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
            url.searchParams.set(key, String(value));
        }
    });
    const response = await fetch(url.toString(), {
        method: 'GET',
        headers: { Authorization: `Bearer ${getToken()}` }
    });
    if (!response.ok) {
        throw new Error('导出失败');
    }
    const blob = await response.blob();
    const contentDisposition = response.headers.get('content-disposition') || '';
    const filenameMatch = contentDisposition.match(/filename=\"?([^\";]+)\"?/);
    const filename = filenameMatch?.[1] || `finance-auto-deduct-exceptions-${new Date().toISOString().slice(0, 10)}.csv`;
    const link = document.createElement('a');
    const objectUrl = URL.createObjectURL(blob);
    link.href = objectUrl;
    link.download = filename;
    link.click();
    URL.revokeObjectURL(objectUrl);
}
export function getFinanceRoomOpsDetail(params) {
    return request.get('/api/finance/workbench/room-ops/detail', { params });
}
export function getFinanceAllocationRules(params) {
    return request.get('/api/finance/workbench/allocation/rules', { params });
}
export function getFinanceAllocationResidentOptions(params) {
    return request.get('/api/finance/workbench/allocation/resident-options', { params });
}
export function initFinanceAllocationRuleTemplate(params) {
    return request.post('/api/finance/workbench/allocation/rules/template/init', null, { params });
}
export function validateFinanceAllocationMeter(data) {
    return request.post('/api/finance/workbench/allocation/meter/validate', data);
}
export function getFinanceReconcileExceptions(params) {
    return request.get('/api/finance/workbench/reconcile/exceptions', { params });
}
export async function exportFinanceReconcileExceptionsCsv(params) {
    const url = new URL('/api/finance/workbench/reconcile/exceptions/export', window.location.origin);
    Object.entries(params || {}).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
            url.searchParams.set(key, String(value));
        }
    });
    const response = await fetch(url.toString(), {
        method: 'GET',
        headers: { Authorization: `Bearer ${getToken()}` }
    });
    if (!response.ok) {
        throw new Error('导出失败');
    }
    const blob = await response.blob();
    const contentDisposition = response.headers.get('content-disposition') || '';
    const filenameMatch = contentDisposition.match(/filename=\"?([^\";]+)\"?/);
    const filename = filenameMatch?.[1] || `finance-reconcile-exceptions-${new Date().toISOString().slice(0, 10)}.csv`;
    const link = document.createElement('a');
    const objectUrl = URL.createObjectURL(blob);
    link.href = objectUrl;
    link.download = filename;
    link.click();
    URL.revokeObjectURL(objectUrl);
}
export async function exportFinanceReconcileHistoryCsv(params) {
    const url = new URL('/api/finance/reconcile/export', window.location.origin);
    Object.entries(params || {}).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
            url.searchParams.set(key, String(value));
        }
    });
    const response = await fetch(url.toString(), {
        method: 'GET',
        headers: { Authorization: `Bearer ${getToken()}` }
    });
    if (!response.ok) {
        throw new Error('导出失败');
    }
    const blob = await response.blob();
    const contentDisposition = response.headers.get('content-disposition') || '';
    const filenameMatch = contentDisposition.match(/filename=\"?([^\";]+)\"?/);
    const filename = filenameMatch?.[1] || `finance-reconcile-history-${new Date().toISOString().slice(0, 10)}.csv`;
    const link = document.createElement('a');
    const objectUrl = URL.createObjectURL(blob);
    link.href = objectUrl;
    link.download = filename;
    link.click();
    URL.revokeObjectURL(objectUrl);
}
export function getFinanceMasterDataOverview(params) {
    return request.get('/api/finance/workbench/config/overview', { params });
}
export function getFinanceConfigImpactPreview(data) {
    return request.post('/api/finance/workbench/config/impact-preview', data);
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
export function getFinanceReportEntrySummary(params) {
    return request.get('/api/finance/report/entry-summary', { params });
}
export function getFinanceCategoryConsumptionAnalysis(params) {
    return request.get('/api/finance/report/category-consumption-analysis', { params });
}
export function bindFinanceBillElder(billId, data) {
    return request.post(`/api/finance/bill/${billId}/bind-elder`, data);
}
