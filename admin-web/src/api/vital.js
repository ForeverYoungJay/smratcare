import request from '../utils/request';
export function createVitalRecord(data) {
    return request.post('/api/vital/record', data);
}
export function getVitalByElder(elderId, params) {
    return request.get(`/api/vital/elder/${elderId}`, { params });
}
export function getVitalLatest(elderId) {
    return request.get(`/api/vital/elder/${elderId}/latest`);
}
export function getVitalAbnormalToday() {
    return request.get('/api/vital/abnormal/today');
}
