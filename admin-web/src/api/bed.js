import request, { fetchPage } from '../utils/request';
export function getRoomPage(params) {
    return fetchPage('/api/room/page', params);
}
export function getBedPage(params) {
    return fetchPage('/api/bed/page', params);
}
export function getRoomList() {
    return request.get('/api/room/list');
}
export function getBedList() {
    return request.get('/api/bed/list');
}
export function getBedMap() {
    return request.get('/api/bed/map');
}
export function getBuildingPage(params) {
    return fetchPage('/api/asset/buildings/page', params);
}
export function getBuildingList() {
    return request.get('/api/asset/buildings/list');
}
export function createBuilding(data) {
    return request.post('/api/asset/buildings', data);
}
export function updateBuilding(id, data) {
    return request.put(`/api/asset/buildings/${id}`, data);
}
export function deleteBuilding(id) {
    return request.delete(`/api/asset/buildings/${id}`);
}
export function getFloorPage(params) {
    return fetchPage('/api/asset/floors/page', params);
}
export function getFloorList(params) {
    return request.get('/api/asset/floors/list', { params });
}
export function createFloor(data) {
    return request.post('/api/asset/floors', data);
}
export function updateFloor(id, data) {
    return request.put(`/api/asset/floors/${id}`, data);
}
export function deleteFloor(id) {
    return request.delete(`/api/asset/floors/${id}`);
}
export function getAssetTree() {
    return request.get('/api/asset/tree');
}
export function createRoom(data) {
    return request.post('/api/room', data);
}
export function updateRoom(id, data) {
    return request.put(`/api/room/${id}`, data);
}
export function deleteRoom(id) {
    return request.delete(`/api/room/${id}`);
}
export function createBed(data) {
    return request.post('/api/bed', data);
}
export function updateBed(id, data) {
    return request.put(`/api/bed/${id}`, data);
}
export function deleteBed(id) {
    return request.delete(`/api/bed/${id}`);
}
