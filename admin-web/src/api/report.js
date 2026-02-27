import { fetchPage } from '../utils/request';
export function getReportPage(params) {
    return fetchPage('/api/report/page', params);
}
