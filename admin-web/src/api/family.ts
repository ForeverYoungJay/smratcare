import request, { fetchPage } from '../utils/request'
import type { FamilyRelationItem, FamilyUserItem, Id } from '../types'

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

export function getFamilyRelations(elderId: Id | number) {
  return request.get<FamilyRelationItem[]>('/api/admin/family/relations', { params: { elderId } })
}

export function removeFamilyRelation(relationId: number | string) {
  return request.delete<void>(`/api/admin/family/relations/${relationId}`)
}
