import type { Id } from './common'

export interface BillItem {
  id: Id
  elderId: Id
  elderName?: string
  careLevel?: string
  billMonth?: string
  totalAmount: number
  nursingFee?: number
  bedFee?: number
  insuranceFee?: number
  paidAmount?: number
  outstandingAmount?: number
  lastPayMethod?: string
  lastPaymentId?: Id
  lastPaymentAmount?: number
  lastPaidAt?: string
  lastPaymentRemark?: string
  status?: number
}

export interface BillPageQuery {
  pageNo: number
  pageSize: number
  month?: string
  elderId?: Id
  scene?: 'ADMISSION' | 'RESIDENT'
  keyword?: string
  payMethod?: string
}

export interface PayRequest {
  amount: number
  method: string
  paidAt?: string
  remark?: string
}
