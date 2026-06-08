import request, { fetchPage } from '../utils/request';
export function getSmartDevicePage(params) {
    return fetchPage('/api/smart-care/devices/page', params);
}
export function createSmartDevice(data) {
    return request.post('/api/smart-care/devices', data);
}
export function updateSmartDevice(id, data) {
    return request.put(`/api/smart-care/devices/${id}`, data);
}
export function setSmartDeviceEnabled(id, enabled) {
    return request.put(`/api/smart-care/devices/${id}/enabled`, null, { params: { enabled } });
}
export function reportSmartDeviceEvent(data) {
    return request.post('/api/smart-care/events', data);
}
export function getSmartAlertSummary() {
    return request.get('/api/smart-care/alerts/summary');
}
export function getSmartAlertPage(params) {
    return fetchPage('/api/smart-care/alerts/page', params);
}
export function acknowledgeSmartAlert(id) {
    return request.put(`/api/smart-care/alerts/${id}/ack`);
}
export function resolveSmartAlert(id, data) {
    return request.put(`/api/smart-care/alerts/${id}/resolve`, data);
}
