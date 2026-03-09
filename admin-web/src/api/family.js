import request, { fetchPage } from '../utils/request';
export function getFamilyUserPage(params) {
    return fetchPage('/api/admin/family/users/page', params);
}
export function getFamilyRelations(elderId) {
    return request.get('/api/admin/family/relations', { params: { elderId } });
}
export function removeFamilyRelation(relationId) {
    return request.delete(`/api/admin/family/relations/${relationId}`);
}
