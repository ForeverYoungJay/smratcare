export interface PaymentRecordItem {
  id: number
  billMonthlyId: number
  amount: number
  payMethod: 'CASH' | 'BANK' | 'WECHAT_OFFLINE' | 'ALIPAY' | 'WECHAT' | 'QR_CODE'
  paidAt: string
  operatorStaffId?: number
  operatorStaffName?: string
  remark?: string
  externalTxnId?: string
  createTime?: string
}

export interface ReconcileDailyItem {
  id?: number
  date?: string
  reconcileDate?: string
  totalReceived: number
  mismatchFlag: boolean | number
  remark?: string
  createTime?: string
}

export interface ReconcileRequest {
  date: string
}

export interface FinanceBillDetail {
  billId: number
  elderId: number
  elderName?: string
  billMonth: string
  totalAmount: number
  paidAmount?: number
  outstandingAmount?: number
  status?: number
  items: FinanceBillItem[]
  payments: PaymentRecordItem[]
  storeOrders: StoreOrderSummary[]
}

export interface FinanceBillItem {
  itemType: string
  itemName: string
  amount: number
  basis?: string
}

export interface StoreOrderSummary {
  id: number
  orderNo: string
  elderId: number
  elderName?: string
  totalAmount: number
  payableAmount: number
  orderStatus: number
  payStatus: number
  payTime?: string
  createTime?: string
}

export interface FinanceReportMonthlyItem {
  month: string
  amount: number
}

export interface FinanceArrearsItem {
  elderId: number
  elderName?: string
  outstandingAmount: number
}

export interface FinanceStoreSalesItem {
  month: string
  amount: number
}

export interface ElderAccount {
  id: number
  elderId: number
  elderName?: string
  balance: number
  creditLimit: number
  warnThreshold: number
  status: number
  pointsBalance?: number
  pointsStatus?: number
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface ElderAccountLog {
  id: number
  accountId: number
  elderId: number
  elderName?: string
  amount: number
  balanceAfter: number
  direction: 'DEBIT' | 'CREDIT'
  sourceType: string
  sourceId?: number
  remark?: string
  createTime?: string
}

export interface ElderAccountAdjustRequest {
  elderId: number
  elderName?: string
  amount: number
  direction: 'DEBIT' | 'CREDIT'
  remark?: string
}

export interface ElderAccountUpdateRequest {
  elderId: number
  elderName?: string
  creditLimit?: number
  warnThreshold?: number
  status?: number
  remark?: string
}
