import request, { fetchPage } from '../utils/request';
export function getCardAccountPage(params) {
    return fetchPage('/api/card/account/page', params);
}
export function createCardAccount(data) {
    return request.post('/api/card/account', data);
}
export function updateCardAccount(id, data) {
    return request.put(`/api/card/account/${id}`, data);
}
export function deleteCardAccount(id) {
    return request.delete(`/api/card/account/${id}`);
}
export function rechargeCard(data) {
    return request.post('/api/card/account/recharge', data);
}
export function consumeCard(data) {
    return request.post('/api/card/account/consume', data);
}
export function getCardRechargePage(params) {
    return fetchPage('/api/card/account/recharge/page', params);
}
export function getCardConsumePage(params) {
    return fetchPage('/api/card/account/consume/page', params);
}
