import request, { fetchPage } from '../utils/request'
import type { CrmLeadItem } from '../types'

export function getCrmLeadPage(params: any) {
  return fetchPage<CrmLeadItem>('/api/crm/leads/page', params)
}

export function createCrmLead(data: Partial<CrmLeadItem>) {
  return request.post<void>('/api/crm/leads', data)
}

export function updateCrmLead(id: number, data: Partial<CrmLeadItem>) {
  return request.put<void>(`/api/crm/leads/${id}`, data)
}

export function deleteCrmLead(id: number) {
  return request.delete<void>(`/api/crm/leads/${id}`)
}
