import request from '../utils/request';
import { getToken } from '../utils/auth';
export function getCheckInStats(params) {
    return request.get('/api/stats/check-in', { params });
}
export function getConsumptionStats(params) {
    return request.get('/api/stats/consumption', { params });
}
export function getElderInfoStats(params) {
    return request.get('/api/stats/elder-info', { params });
}
export function getOrgMonthlyOperation(params) {
    return request.get('/api/stats/org/monthly-operation', { params });
}
export function getOrgElderFlow(params) {
    return request.get('/api/stats/org/elder-flow', { params });
}
export function getOrgBedUsage(params) {
    return request.get('/api/stats/org/bed-usage', { params });
}
export function getMonthlyRevenueStats(params) {
    return request.get('/api/stats/monthly-revenue', { params });
}
export function getElderFlowReport(params) {
    return request.get('/api/stats/elder-flow-report', { params });
}
export async function exportElderFlowReportCsv(params) {
    const url = new URL('/api/stats/elder-flow-report/export', window.location.origin);
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
    const filename = filenameMatch?.[1] || `elder-flow-report-${new Date().toISOString().slice(0, 10)}.csv`;
    const link = document.createElement('a');
    const objectUrl = URL.createObjectURL(blob);
    link.href = objectUrl;
    link.download = filename;
    link.click();
    URL.revokeObjectURL(objectUrl);
}
export async function exportOrgMonthlyOperationCsv(params) {
    const url = new URL('/api/stats/org/monthly-operation/export', window.location.origin);
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
    const filename = filenameMatch?.[1] || `org-monthly-operation-${new Date().toISOString().slice(0, 10)}.csv`;
    const link = document.createElement('a');
    const objectUrl = URL.createObjectURL(blob);
    link.href = objectUrl;
    link.download = filename;
    link.click();
    URL.revokeObjectURL(objectUrl);
}
