import type { Id } from './common'

export interface MedicalOrder {
  id: Id
  elderId: Id
  emrId?: Id
  orderType?: string
  category?: string
  content: string
  drugId?: Id
  dosage?: string
  frequency?: string
  route?: string
  quantityPerTime?: number
  startTime?: string
  stopTime?: string
  doctorName?: string
  status?: string
  priority?: string
  remark?: string
}

export interface MedicalOrderExecution {
  id: Id
  orderId: Id
  elderId: Id
  planTime: string
  execTime?: string
  executorId?: Id
  executorName?: string
  status?: string
  result?: string
  dispenseRecordId?: Id
  remark?: string
}
