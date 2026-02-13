export interface BillItem {
  id: number
  elderId: number
  elderName?: string
  billMonth?: string
  totalAmount: number
  paidAmount?: number
  outstandingAmount?: number
  status?: number
}

export interface PayRequest {
  amount: number
  method: string
  paidAt?: string
  remark?: string
}
