import request, { fetchPage } from '../utils/request'
import type { Id, MedicalOrder, MedicalOrderExecution } from '../types'

export function getMedicalOrderPage(params: { pageNo?: number; pageSize?: number; elderId?: Id; status?: string; category?: string }) {
  return fetchPage<MedicalOrder>('/api/medical/order/page', params)
}

export function createMedicalOrder(data: Partial<MedicalOrder>) {
  return request.post<Id>('/api/medical/order', data)
}

export function stopMedicalOrder(id: Id) {
  return request.post(`/api/medical/order/${id}/stop`)
}

export function getOrderExecutionPage(params: { pageNo?: number; pageSize?: number; orderId?: Id; elderId?: Id; status?: string }) {
  return fetchPage<MedicalOrderExecution>('/api/medical/order/executions/page', params)
}

export function signOrderExecution(data: { executionId: Id; status: string; result?: string; dispenseRecordId?: Id; remark?: string }) {
  return request.post<MedicalOrderExecution>('/api/medical/order/executions/sign', data)
}
