import request, { fetchPage } from '../utils/request';
export function getSchedulePage(params) {
    return fetchPage('/api/schedule/page', params);
}
export function createSchedule(data) {
    return request.post('/api/schedule', data);
}
export function updateSchedule(id, data) {
    return request.put(`/api/schedule/${id}`, data);
}
export function deleteSchedule(id) {
    return request.delete(`/api/schedule/${id}`);
}
export function getAttendancePage(params) {
    const merged = { ...(params || {}) };
    return request.get('/api/attendance/page', { params: merged }).then((res) => {
        if (res && Array.isArray(res.list)) {
            return res;
        }
        if (res && Array.isArray(res.records)) {
            return {
                list: res.records,
                total: Number(res.total || 0),
                pageNo: Number(res.current || merged.pageNo || 1),
                pageSize: Number(res.size || merged.pageSize || 10)
            };
        }
        if (Array.isArray(res)) {
            return {
                list: res,
                total: res.length,
                pageNo: Number(merged.pageNo || 1),
                pageSize: Number(merged.pageSize || res.length || 10)
            };
        }
        return {
            list: [],
            total: 0,
            pageNo: Number(merged.pageNo || 1),
            pageSize: Number(merged.pageSize || 10)
        };
    });
}
