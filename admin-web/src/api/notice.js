import request, { fetchPage } from '../utils/request';
export function getNoticePage(params) {
    return fetchPage('/api/oa/notice/page', params);
}
export function getNoticeDetail(id) {
    return request.get(`/api/oa/notice/${id}`);
}
export function createNotice(data) {
    return request.post('/api/oa/notice', data);
}
export function updateNotice(id, data) {
    return request.put(`/api/oa/notice/${id}`, data);
}
export function deleteNotice(id) {
    return request.delete(`/api/oa/notice/${id}`);
}
