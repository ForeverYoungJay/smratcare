import request from '../utils/request';
export function bookVisit(data) {
    return request.post('/api/family/visit/book', data);
}
export function getFamilyVisits() {
    return request.get('/api/family/visit/my');
}
export function guardTodayVisits() {
    return request.get('/api/guard/visit/today');
}
export function guardCheckin(data) {
    return request.post('/api/guard/visit/checkin', data);
}
