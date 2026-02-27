import request, { fetchPage } from '../utils/request';
export function getHrStaffPage(params) {
    return fetchPage('/api/admin/hr/staff/page', params);
}
export function getHrProfile(staffId) {
    return request.get(`/api/admin/hr/profile/${staffId}`);
}
export function upsertHrProfile(data) {
    return request.post('/api/admin/hr/profile', data);
}
export function getHrTrainingPage(params) {
    return fetchPage('/api/admin/hr/training/page', params);
}
export function createHrTraining(data) {
    return request.post('/api/admin/hr/training', data);
}
export function updateHrTraining(id, data) {
    return request.put(`/api/admin/hr/training/${id}`, data);
}
export function deleteHrTraining(id) {
    return request.delete(`/api/admin/hr/training/${id}`);
}
export function adjustStaffPoints(data) {
    return request.post('/api/admin/hr/points/adjust', data);
}
export function terminateStaff(params) {
    return request.post('/api/admin/hr/terminate', null, { params });
}
export function reinstateStaff(params) {
    return request.post('/api/admin/hr/reinstate', null, { params });
}
export function getStaffPointsLog(params) {
    return fetchPage('/api/admin/hr/points/log/page', params);
}
export function getPerformanceSummary(params) {
    return request.get('/api/admin/hr/performance/summary', { params });
}
export function getPerformanceRanking(params) {
    return request.get('/api/admin/hr/performance/ranking', { params });
}
export function getPointsRulePage(params) {
    return fetchPage('/api/admin/hr/points/rule/page', params);
}
export function upsertPointsRule(data) {
    return request.post('/api/admin/hr/points/rule', data);
}
export function deletePointsRule(id) {
    return request.delete(`/api/admin/hr/points/rule/${id}`);
}
export function getRewardPunishmentPage(params) {
    return fetchPage('/api/admin/hr/reward-punishment/page', params);
}
export function createRewardPunishment(data) {
    return request.post('/api/admin/hr/reward-punishment', data);
}
export function updateRewardPunishment(id, data) {
    return request.put(`/api/admin/hr/reward-punishment/${id}`, data);
}
export function deleteRewardPunishment(id) {
    return request.delete(`/api/admin/hr/reward-punishment/${id}`);
}
