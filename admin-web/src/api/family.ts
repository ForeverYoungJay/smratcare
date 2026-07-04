import request, { fetchPage } from '../utils/request'
import type { FamilyElderItem, FamilyRelationItem, FamilyUserItem, Id } from '../types'

export function getFamilyUserPage(params: any) {
  return fetchPage<FamilyUserItem>('/api/admin/family/users/page', params)
}

export function upsertFamilyUser(data: {
  realName: string
  phone: string
  idCardNo: string
  status?: number
}) {
  return request.post<FamilyUserItem>('/api/admin/family/users/upsert', data)
}

export function getFamilyRelations(elderId: Id) {
  return request.get<FamilyRelationItem[]>('/api/admin/family/relations', { params: { elderId } })
}

export function getFamilyLinkedElders(familyUserId: Id) {
  return request.get<FamilyElderItem[]>(`/api/admin/family/users/${familyUserId}/elders`)
}

export function removeFamilyRelation(relationId: Id) {
  return request.delete<void>(`/api/admin/family/relations/${relationId}`)
}
