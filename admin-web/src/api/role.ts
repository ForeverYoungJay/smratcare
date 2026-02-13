import { fetchPage } from '../utils/request'
import type { RoleItem } from '../types'

export function getRolePage(params: any) {
  return fetchPage<RoleItem>('/api/admin/roles', params)
}
