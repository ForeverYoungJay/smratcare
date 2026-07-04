import request, { fetchPage } from '../utils/request'
import type { DrugBatch, DrugCatalog, DrugDispenseRecord, Id } from '../types'

export function getDrugPage(params: { pageNo?: number; pageSize?: number; keyword?: string; category?: string; status?: number }) {
  return fetchPage<DrugCatalog>('/api/pharmacy/drugs/page', params)
}

export function getDrugBatchPage(params: { pageNo?: number; pageSize?: number; drugId?: Id; status?: string }) {
  return fetchPage<DrugBatch>('/api/pharmacy/batches/page', params)
}

export function drugInbound(data: {
  drugId: Id
  batchNo: string
  expireDate?: string
  productionDate?: string
  quantity: number
  purchasePrice?: number
  supplier?: string
  remark?: string
}) {
  return request.post<Id>('/api/pharmacy/inbound', data)
}

export function drugDispense(data: { drugId: Id; quantity: number; elderId?: Id; orderId?: Id; remark?: string }) {
  return request.post<DrugDispenseRecord[]>('/api/pharmacy/dispense', data)
}

export function getDispenseRecordPage(params: { pageNo?: number; pageSize?: number; drugId?: Id; elderId?: Id }) {
  return fetchPage<DrugDispenseRecord>('/api/pharmacy/dispense-records/page', params)
}
