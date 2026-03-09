import request, { fetchPage } from '../utils/request';
export function getElderPage(params) {
    return fetchPage('/api/elder/page', params);
}
export function getElderDetail(id) {
    return request.get(`/api/elder/${id}`);
}
export function createElder(data) {
    return request.post('/api/elder', data);
}
export function updateElder(id, data) {
    return request.put(`/api/elder/${id}`, data);
}
export function assignBed(elderId, bedId, startDate) {
    const body = { bedId, startDate };
    return request.post(`/api/elder/${elderId}/assignBed`, body);
}
export function unbindBed(elderId, endDate, reason) {
    const body = { endDate, reason };
    return request.post(`/api/elder/${elderId}/unbindBed`, body);
}
export function getElderDiseases(elderId) {
    return request.get(`/api/elder/${elderId}/diseases`);
}
export function updateElderDiseases(elderId, diseaseIds) {
    const body = { diseaseIds };
    return request.put(`/api/elder/${elderId}/diseases`, body);
}
export function bindFamily(data) {
    return request.post('/api/admin/family/relations/bind', data);
}
export function uploadElderFile(file, bizType = 'elder-archive') {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('bizType', bizType);
    return request.post('/api/files/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    });
}
