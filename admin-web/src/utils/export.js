import { getToken } from './auth';
export function exportCsv(data, filename) {
    if (!data || data.length === 0)
        return;
    const headers = Object.keys(data[0]);
    const rows = data.map((row) => headers.map((h) => JSON.stringify(row[h] ?? '')).join(','));
    const csv = [headers.join(','), ...rows].join('\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    downloadBlob(blob, filename.endsWith('.csv') ? filename : `${filename}.csv`);
}
export function exportExcel(data, filename) {
    if (!data || data.length === 0)
        return;
    const headers = Object.keys(data[0]);
    const headHtml = headers.map((h) => `<th>${escapeHtml(String(h))}</th>`).join('');
    const rowHtml = data
        .map((row) => `<tr>${headers.map((h) => `<td>${escapeHtml(String(row[h] ?? ''))}</td>`).join('')}</tr>`)
        .join('');
    const tableHtml = `<table><thead><tr>${headHtml}</tr></thead><tbody>${rowHtml}</tbody></table>`;
    const html = `<!DOCTYPE html><html><head><meta charset="UTF-8" /></head><body>${tableHtml}</body></html>`;
    const blob = new Blob([html], { type: 'application/vnd.ms-excel;charset=utf-8;' });
    downloadBlob(blob, filename.endsWith('.xls') ? filename : `${filename}.xls`);
}
export async function exportCsvByRequest(path, params, fallbackFilename) {
    const url = new URL(path, window.location.origin);
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
    const filenameMatch = contentDisposition.match(/filename\*?=(?:UTF-8''|\"?)([^\";]+)/i);
    const filename = filenameMatch?.[1] ? decodeURIComponent(filenameMatch[1]) : (fallbackFilename || 'export.csv');
    downloadBlob(blob, filename);
}
function downloadBlob(blob, filename) {
    const link = document.createElement('a');
    const objectUrl = URL.createObjectURL(blob);
    link.href = objectUrl;
    link.download = filename;
    link.click();
    URL.revokeObjectURL(objectUrl);
}
function escapeHtml(value) {
    return value
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}
