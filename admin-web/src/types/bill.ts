export interface BillItem {
  id: number
  elderId: number
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
  lastPaymentId?: number
  lastPaymentAmount?: number
  lastPaidAt?: string
  lastPaymentRemark?: string
  status?: number
}

export interface BillPageQuery {
  pageNo: number
  pageSize: number
  month?: string
  elderId?: number
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
