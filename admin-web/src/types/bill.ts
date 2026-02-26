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
  status?: number
}

export interface PayRequest {
  amount: number
  method: string
  paidAt?: string
  remark?: string
}
