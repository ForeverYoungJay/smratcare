import request, { fetchPage } from '../utils/request'
import type { FamilyRelationItem, FamilyUserItem } from '../types'

export function getFamilyUserPage(params: any) {
  return fetchPage<FamilyUserItem>('/api/admin/family/users/page', params)
}

export function getFamilyRelations(elderId: number) {
  return request.get<FamilyRelationItem[]>('/api/admin/family/relations', { params: { elderId } })
}
