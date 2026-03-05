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
export function guardBookVisit(data) {
    return request.post('/api/guard/visit/book', data);
}
export function guardUpdateVisit(id, data) {
    return request.put(`/api/guard/visit/${id}`, data);
}
export function guardDeleteVisit(id) {
    return request.delete(`/api/guard/visit/${id}`);
}
export function guardGetPrintTicket(id) {
    return request.get(`/api/guard/visit/${id}/print-ticket`);
}
