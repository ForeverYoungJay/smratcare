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

export interface PaymentVoucherUse {
  voucherId: Id
  amount: number
}

export interface PayRequest {
  /** 实收现金，全额抵扣时可为 0 */
  amount: number
  method: string
  paidAt?: string
  remark?: string
  ltciDeductAmount?: number
  discountAmount?: number
  discountReason?: string
  voucherUses?: PaymentVoucherUse[]
}

export interface FinanceVoucherItem {
  id: Id
  elderId?: Id
  elderName?: string
  voucherNo: string
  voucherName: string
  faceAmount: number
  balanceAmount: number
  minBillAmount?: number
  allowSplit?: boolean
  validFrom?: string
  validTo?: string
  status: string
  statusText?: string
  source?: string
  issuedAt?: string
  revokeReason?: string
  remark?: string
}

export interface FinanceVoucherUsageItem {
  id: Id
  voucherId: Id
  voucherNo?: string
  elderId?: Id
  elderName?: string
  billMonthlyId?: Id
  billMonth?: string
  paymentRecordId?: Id
  amount: number
  direction: string
  directionText?: string
  remark?: string
  createTime?: string
}

export interface BillDeductionPreview {
  billId: Id
  elderId?: Id
  elderName?: string
  billMonth?: string
  totalAmount: number
  settledAmount: number
  outstandingAmount: number
  ltciAvailableAmount: number
  ltciFundPayAmount: number
  ltciUsedAmount: number
  ltciHint?: string
  usableVouchers: FinanceVoucherItem[]
}
