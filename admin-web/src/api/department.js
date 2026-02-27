import { fetchPage } from '../utils/request';
export function getDepartmentPage(params) {
    return fetchPage('/api/admin/departments', params);
}
