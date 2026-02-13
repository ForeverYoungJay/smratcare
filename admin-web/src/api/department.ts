import { fetchPage } from '../utils/request'
import type { DepartmentItem } from '../types'

export function getDepartmentPage(params: any) {
  return fetchPage<DepartmentItem>('/api/admin/departments', params)
}
