import type { Id } from './common'

export interface DrugCatalog {
  id: Id
  drugCode: string
  drugName: string
  genericName?: string
  spec?: string
  unit?: string
  dosageForm?: string
  manufacturer?: string
  category?: string
  controlLevel?: string
  isHighRisk?: number
  safetyStock?: number
  price?: number
  status?: number
  remark?: string
}

export interface DrugBatch {
  id: Id
  drugId: Id
  batchNo: string
  expireDate?: string
  productionDate?: string
  quantity?: number
  purchasePrice?: number
  supplier?: string
  inboundAt?: string
  status?: string
  remark?: string
}

export interface DrugDispenseRecord {
  id: Id
  drugId: Id
  batchId?: Id
  batchNo?: string
  elderId?: Id
  orderId?: Id
  dispenseType?: string
  quantity?: number
  operatorId?: Id
  dispenseTime?: string
  remark?: string
}
