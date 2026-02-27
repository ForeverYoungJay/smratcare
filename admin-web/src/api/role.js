import { fetchPage } from '../utils/request';
export function getRolePage(params) {
    return fetchPage('/api/admin/roles', params);
}
