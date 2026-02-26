export interface AdmissionFeeAuditItem {
  id: number
  elderId: number
  elderName?: string
  admissionId?: number
  totalAmount: number
  depositAmount: number
  status: string
  auditRemark?: string
  reviewedBy?: number
  reviewedTime?: string
  createTime?: string
}

export interface DischargeFeeAuditItem {
  id: number
  elderId: number
  elderName?: string
  dischargeApplyId?: number
  payableAmount: number
  feeItem?: string
  dischargeFeeConfig?: string
  status: string
  auditRemark?: string
  reviewedBy?: number
  reviewedTime?: string
  createTime?: string
}

export interface DischargeSettlementItem {
  id: number
  detailNo?: string
  elderId: number
  elderName?: string
  dischargeApplyId?: number
  payableAmount: number
  feeItem?: string
  dischargeFeeConfig?: string
  fromDepositAmount: number
  refundAmount: number
  supplementAmount: number
  status: string
  frontdeskApproved?: number
  frontdeskSignerName?: string
  frontdeskSignedTime?: string
  nursingApproved?: number
  nursingSignerName?: string
  nursingSignedTime?: string
  financeRefunded?: number
  financeRefundOperatorName?: string
  financeRefundTime?: string
  remark?: string
  settledBy?: number
  settledTime?: string
  createTime?: string
}

export interface ConsumptionRecordItem {
  id: number
  elderId: number
  elderName?: string
  consumeDate: string
  amount: number
  category?: string
  sourceType?: string
  sourceId?: number
  remark?: string
  createTime?: string
}

export interface MonthlyAllocationItem {
  id: number
  allocationMonth: string
  allocationName: string
  totalAmount: number
  targetCount: number
  status: string
  remark?: string
  createTime?: string
}

export interface AdmissionFeeAuditCreateRequest {
  elderId: number
  admissionId?: number
  totalAmount: number
  depositAmount: number
  remark?: string
}

export interface DischargeFeeAuditCreateRequest {
  elderId: number
  dischargeApplyId?: number
  payableAmount: number
  feeItem?: string
  dischargeFeeConfig?: string
  remark?: string
}

export interface FeeAuditReviewRequest {
  status: string
  reviewRemark?: string
}

export interface DischargeSettlementCreateRequest {
  elderId: number
  dischargeApplyId?: number
  payableAmount: number
  feeItem?: string
  dischargeFeeConfig?: string
  remark?: string
}

export interface DischargeSettlementConfirmRequest {
  action?: 'FRONTDESK_APPROVE' | 'NURSING_APPROVE' | 'FINANCE_REFUND'
  signerName?: string
  remark?: string
}

export interface ConsumptionRecordCreateRequest {
  elderId: number
  consumeDate: string
  amount: number
  category?: string
  sourceType?: string
  sourceId?: number
  remark?: string
}

export interface MonthlyAllocationCreateRequest {
  allocationMonth: string
  allocationName: string
  totalAmount: number
  targetCount: number
  remark?: string
}
