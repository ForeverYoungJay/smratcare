import request, { fetchPage } from '../utils/request'
import type { Id } from '../types/common'
import type { CrmLeadItem } from '../types'

export function getCrmLeadPage(params: any, config?: Record<string, any>) {
  return fetchPage<CrmLeadItem>('/api/crm/leads/page', params, config)
}

export function getCrmLead(id: Id) {
  return request.get<CrmLeadItem>(`/api/crm/leads/${id}`)
}

export function createCrmLead(data: Partial<CrmLeadItem>) {
  return request.post<CrmLeadItem>('/api/crm/leads', data)
}

export function updateCrmLead(id: Id, data: Partial<CrmLeadItem>) {
  return request.put<CrmLeadItem>(`/api/crm/leads/${id}`, data)
}

export function deleteCrmLead(id: Id) {
  return request.delete<void>(`/api/crm/leads/${id}`)
}
