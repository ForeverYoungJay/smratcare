import type { Id } from './common'

export interface AdmissionFeeAuditItem {
  id: Id
  elderId: Id
  elderName?: string
  admissionId?: Id
  totalAmount: number
  depositAmount: number
  status: string
  auditRemark?: string
  reviewedBy?: Id
  reviewedTime?: string
  createTime?: string
}

export interface DischargeFeeAuditItem {
  id: Id
  elderId: Id
  elderName?: string
  dischargeApplyId?: Id
  payableAmount: number
  feeItem?: string
  dischargeFeeConfig?: string
  status: string
  auditRemark?: string
  reviewedBy?: Id
  reviewedTime?: string
  createTime?: string
}

export interface DischargeSettlementItem {
  id: Id
  detailNo?: string
  elderId: Id
  elderName?: string
  dischargeApplyId?: Id
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
  settledBy?: Id
  settledTime?: string
  createTime?: string
}

export interface ConsumptionRecordItem {
  id: Id
  elderId: Id
  elderName?: string
  consumeDate: string
  amount: number
  category?: string
  sourceType?: string
  sourceId?: Id
  remark?: string
  createTime?: string
}

export interface MonthlyAllocationItem {
  id: Id
  allocationMonth: string
  allocationName: string
  totalAmount: number
  targetCount: number
  elderIds?: string
  elderSnapshotJson?: string
  avgAmount?: number
  status: string
  remark?: string
  rollbackBy?: Id
  rollbackReason?: string
  rollbackTime?: string
  createTime?: string
}

export interface AdmissionFeeAuditCreateRequest {
  elderId: Id
  admissionId?: Id
  totalAmount: number
  depositAmount: number
  remark?: string
}

export interface DischargeFeeAuditCreateRequest {
  elderId: Id
  dischargeApplyId?: Id
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
  elderId: Id
  dischargeApplyId?: Id
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
  elderId: Id
  consumeDate: string
  amount: number
  category?: string
  sourceType?: string
  sourceId?: Id
  remark?: string
}

export interface MonthlyAllocationCreateRequest {
  allocationMonth: string
  allocationName: string
  totalAmount: number
  targetCount: number
  roomNo?: string
  elderIds?: Id[]
  remark?: string
}

export interface MonthlyAllocationPreviewRequest {
  allocationMonth?: string
  allocationName?: string
  totalAmount: number
  elderIds?: Id[]
  remark?: string
}

export interface MonthlyAllocationPreviewRow {
  elderId: Id
  elderName?: string
  amount: number
}

export interface MonthlyAllocationPreviewResponse {
  allocationMonth?: string
  allocationName?: string
  totalAmount: number
  targetCount: number
  avgAmount: number
  remark?: string
  rows: MonthlyAllocationPreviewRow[]
}
