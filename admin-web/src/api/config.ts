import request, { fetchPage } from '../utils/request'
import type { BillingConfig } from '../types'

export function getBillingConfigPage(params: any) {
  return fetchPage<BillingConfig>('/api/admin/billing/config', params)
}

export function updateBillingConfig(id: number, data: Partial<BillingConfig>) {
  return request.post<void>('/api/admin/billing/config', { ...data, id })
}
