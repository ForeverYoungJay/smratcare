import request from '../utils/request';
export function login(data) {
    return request.post('/api/auth/login', data);
}
export function logout() {
    return request.post('/api/auth/logout');
}
export function getMe() {
    return request.get('/api/auth/me');
}
export function familyLogin(data) {
    return request.post('/api/auth/family/login', data);
}
