import type { Id } from './common'

export interface MarketingConversionReport {
  totalLeads: number
  consultCount: number
  intentCount: number
  reservationCount: number
  invalidCount: number
  contractCount: number
  intentRate: number
  reservationRate: number
  contractRate: number
}

export interface MarketingConsultationTrendItem {
  date: string
  total: number
  consultCount: number
  intentCount: number
}

export interface MarketingChannelReportItem {
  source: string
  leadCount: number
  reservationCount: number
  contractCount: number
}

export interface MarketingFollowupStageItem {
  name: string
  count: number
  rate: number
}

export interface MarketingFollowupReport {
  totalLeads: number
  consultCount: number
  intentCount: number
  reservationCount: number
  invalidCount: number
  overdueCount: number
  stages: MarketingFollowupStageItem[]
}

export interface MarketingCallbackItem {
  id: number
  name: string
  phone?: string
  source?: string
  nextFollowDate?: string
  callbackType?: MarketingCallbackType
  score?: number
  remark?: string
}

export interface MarketingCallbackReport {
  todayDue: number
  overdue: number
  completed: number
  total: number
  records: MarketingCallbackItem[]
}

export type MarketingCallbackType = 'checkin' | 'trial' | 'discharge' | 'score'

export interface CrmLeadAssignRequest {
  ownerStaffId: Id
  ownerStaffName?: string
  remark?: string
}

export interface CrmLeadAssignLogItem {
  id: Id
  leadId: Id
  fromOwnerStaffId?: Id
  fromOwnerStaffName?: string
  toOwnerStaffId?: Id
  toOwnerStaffName?: string
  assignedBy?: Id
  assignedByName?: string
  assignedAt?: string
  remark?: string
}

export interface CrmStageTransitionLogItem {
  id: Id
  entityType: 'LEAD' | 'CONTRACT' | string
  leadId?: Id
  contractId?: Id
  transitionType?: string
  source?: string
  fromStage?: string
  toStage?: string
  fromStatus?: string
  toStatus?: string
  fromOwnerDept?: string
  toOwnerDept?: string
  remark?: string
  operatedBy?: Id
  operatedByName?: string
  operatedAt?: string
}

export interface CrmContractWorkflowLogItem {
  id: Id
  contractId: Id
  leadId?: Id
  actionType?: string
  beforeStatus?: string
  afterStatus?: string
  beforeFlowStage?: string
  afterFlowStage?: string
  beforeChangeWorkflow?: string
  afterChangeWorkflow?: string
  remark?: string
  snapshotJson?: string
  operatedBy?: Id
  operatedByName?: string
  operatedAt?: string
}

export interface CrmSalesReportSnapshotItem {
  id: Id
  snapshotType: string
  snapshotDate?: string
  windowFrom?: string
  windowTo?: string
  snapshotKey?: string
  metricsJson?: string
  generatedBy?: Id
  generatedByName?: string
  generatedAt?: string
}

export interface MarketingReportQuery {
  dateFrom?: string
  dateTo?: string
  source?: string
  staffId?: number
  type?: MarketingCallbackType
}

export interface MarketingDataQualityReport {
  missingSourceCount: number
  missingNextFollowDateCount: number
  nonStandardSourceCount: number
}

export interface MarketingWorkbenchSummary {
  funnel: {
    todayConsultCount: number
    evaluationCount: number
    pendingSignCount: number
    pendingAdmissionCount: number
    monthDealCount: number
    monthConversionRate: number
  }
  followup: {
    todayDue: number
    overdue: number
    highIntentCount: number
    lockExpiringCount: number
  }
  bedSales: {
    emptyCount: number
    lockCount: number
    reservedUnsignedCount: number
    premiumEmptyCount: number
  }
  contract: {
    pendingSignCount: number
    renewalDueCount: number
    changePendingCount: number
    monthAmount: number
  }
  callback: {
    checkinCount: number
    trialCount: number
    dischargeCount: number
    score: number
  }
  performance: {
    monthDealCount: number
    monthAmount: number
    rankNo: number
    timelyRate: number
  }
  medical: {
    todayCount: number
    referCount: number
    unassignedCount: number
  }
  plan: {
    speechCount: number
    policyCount: number
    pendingApprovalCount: number
    rejectedCount: number
    totalBudgetAmount?: number
    totalActualLeadCount?: number
    totalActualContractCount?: number
  }
  risk: {
    overdueFollowupCount: number
    lockUnsignedCount: number
    highIntentNoEvalCount: number
    channelDropCount: number
  }
  channelTop5: Array<{
    source: string
    leadCount: number
    contractRate: string
  }>
  channelUnknownCount: number
  channelMonthDeals: number
  dateFrom?: string
  dateTo?: string
  generatedAt?: string
}

export type MarketingLeadMode = 'consultation' | 'intent' | 'reservation' | 'invalid' | 'callback'

export interface MarketingLeadEntrySummary {
  totalCount: number
  modeCount: number
  consultCount: number
  intentCount: number
  reservationCount: number
  invalidCount: number
  signedContractCount: number
  unsignedReservationCount: number
  refundedReservationCount: number
  callbackDueTodayCount: number
  callbackOverdueCount: number
  callbackPendingCount: number
  missingSourceCount: number
  missingNextFollowDateCount: number
  nonStandardSourceCount: number
}

export interface LeadBatchStatusRequest {
  ids: Id[]
  status: number
  followupStatus?: string
  invalidTime?: string
}

export interface LeadBatchDeleteRequest {
  ids?: Id[]
  contractNos?: string[]
}

export interface CallbackPlanItem {
  id: Id
  leadId: Id
  title: string
  followupContent?: string
  planExecuteTime: string
  executorName?: string
  callbackType?: MarketingCallbackType
  score?: number
  status: string
  executedTime?: string
  executeNote?: string
  followupResult?: string
  createTime?: string
}

export interface CallbackPlanCreateRequest {
  title: string
  followupContent?: string
  planExecuteTime: string
  executorName?: string
  callbackType?: MarketingCallbackType
}

export interface CallbackExecuteRequest {
  executeNote?: string
  followupResult?: string
  nextFollowDate?: string
  callbackType?: MarketingCallbackType
  score?: number
}

export interface ContractAttachmentItem {
  id: Id
  leadId: Id
  contractId?: Id
  contractNo?: string
  attachmentType?: string
  fileName: string
  fileUrl?: string
  fileType?: string
  fileSize?: number
  remark?: string
  createTime?: string
}

export interface ContractAttachmentCreateRequest {
  contractId?: Id
  contractNo?: string
  attachmentType?: string
  fileName: string
  fileUrl?: string
  fileType?: string
  fileSize?: number
  remark?: string
}

export interface ContractSystemLinkageSummary {
  elderId?: Id
  elderName?: string
  elderArchiveReady?: boolean
  financeAccountReady?: boolean
  financeBillReady?: boolean
  logisticsReady?: boolean
  medicalRecordReady?: boolean
  billMonth?: string
  healthRecordDate?: string
  billId?: Id
  billItemCount?: number
  billTotalAmount?: number
  medicalInspectionReady?: boolean
  starterInspectionId?: Id
  starterInspectionDate?: string
  medicalCareTaskReady?: boolean
  starterCareTaskId?: Id
  starterCareTaskDate?: string
}

export interface SmsTaskItem {
  id: Id
  leadId: Id
  contractId?: Id
  phone?: string
  templateName?: string
  content?: string
  planSendTime?: string
  status: string
  sendTime?: string
  resultMessage?: string
  createTime?: string
}

export interface SmsTaskCreateRequest {
  leadIds?: Id[]
  contractIds?: Id[]
  templateName?: string
  content?: string
  planSendTime?: string
}

export interface CrmContractItem {
  [key: string]: any
  id: Id
  name?: string
  phone?: string
  leadId?: Id
  elderId?: Id
  idCardNo?: string
  homeAddress?: string
  contractNo?: string
  reservationRoomNo?: string
  reservationBedId?: number
  elderName?: string
  elderPhone?: string
  gender?: number
  age?: number
  marketerName?: string
  amount?: number
  contractAmount?: number
  depositAmount?: number
  reservationAmount?: number
  contractSignedAt?: string
  contractExpiryDate?: string
  contractStatus?: string
  flowStage?: 'PENDING_ASSESSMENT' | 'PENDING_BED_SELECT' | 'PENDING_SIGN' | 'SIGNED'
  currentOwnerDept?: 'MARKETING' | 'ASSESSMENT'
  orgName?: string
  status?: 'DRAFT' | 'PENDING_APPROVAL' | 'APPROVED' | 'REJECTED' | 'SIGNED' | 'EFFECTIVE' | 'VOID'
  changeWorkflowStatus?: 'NONE' | 'IN_PROGRESS' | 'PENDING_APPROVAL' | 'APPROVED' | 'REJECTED'
  changeWorkflowRemark?: string
  smsSendCount?: number
  remark?: string
  linkageSummary?: ContractSystemLinkageSummary
  createTime?: string
  updateTime?: string
}

export interface CrmContractPayload {
  [key: string]: any
  name?: string
  phone?: string
  leadId?: Id
  elderId?: Id
  idCardNo?: string
  homeAddress?: string
  contractNo?: string
  reservationRoomNo?: string
  reservationBedId?: number
  elderName?: string
  elderPhone?: string
  gender?: number
  age?: number
  marketerName?: string
  contractSignedAt?: string
  contractExpiryDate?: string
  contractStatus?: string
  flowStage?: CrmContractItem['flowStage']
  currentOwnerDept?: CrmContractItem['currentOwnerDept']
  orgName?: string
  status?: CrmContractItem['status']
  changeWorkflowStatus?: CrmContractItem['changeWorkflowStatus']
  changeWorkflowRemark?: string
  smsSendCount?: number
  remark?: string
}

export interface CrmContractStageSummary {
  pendingAssessment: number
  pendingBedSelect: number
  pendingSign: number
  signed: number
  pendingAssessmentOverdue: number
  pendingSignOverdue: number
  generatedAt?: string
}

export interface ContractBatchDeleteRequest {
  ids?: Id[]
  contractNos?: string[]
}

export interface UploadedFileResult {
  fileName: string
  originalFileName?: string
  fileUrl: string
  fileType?: string
  fileSize?: number
}

export interface ContractLinkageSummary {
  contractId?: Id
  leadId?: Id
  elderId?: Id
  elderName?: string
  elderPhone?: string
  orgName?: string
  marketerName?: string
  contractNo?: string
  contractStatus?: string
  flowStage?: 'PENDING_ASSESSMENT' | 'PENDING_BED_SELECT' | 'PENDING_SIGN' | 'SIGNED' | string
  currentOwnerDept?: 'MARKETING' | 'ASSESSMENT' | string
  contractSignedAt?: string
  contractExpiryDate?: string
  admissionDate?: string
  depositAmount?: number
  billCount?: number
  billTotalAmount?: number
  billPaidAmount?: number
  billOutstandingAmount?: number
  attachmentCount?: number
  attachments?: ContractAttachmentItem[]
  archiveScore?: number
  archiveLevel?: 'COMPLETE' | 'HIGH' | 'MEDIUM' | 'LOW' | string
  missingRequiredAttachmentTypes?: string[]
  hasContractAttachment?: boolean
  hasInvoiceAttachment?: boolean
  hasIdAttachment?: boolean
  hasAssessmentReport?: boolean
  archiveRuleVersion?: string
  archiveRuleTips?: string[]
  generatedAt?: string
}

export interface ContractArchiveRuleInfo {
  ruleVersion?: string
  title?: string
  description?: string
  requiredItems?: string[]
  stageNotes?: string[]
  generatedAt?: string
}

export interface ContractAssessmentReportItem {
  recordId: number
  assessmentType?: string
  assessmentDate?: string
  status?: string
  score?: number
  levelCode?: string
  resultSummary?: string
  nextAssessmentDate?: string
  reportFileUrl?: string
  reportFileName?: string
}

export interface ContractAssessmentContractItem {
  contractId?: Id
  leadId?: Id
  elderId?: Id
  elderName?: string
  elderPhone?: string
  idCardNo?: string
  homeAddress?: string
  contractNo?: string
  contractStatus?: string
  flowStage?: string
  currentOwnerDept?: string
  marketerName?: string
  orgName?: string
  contractSignedAt?: string
  contractExpiryDate?: string
  reports?: ContractAssessmentReportItem[]
}

export interface ContractAssessmentOverview {
  elderId?: Id
  elderName?: string
  elderPhone?: string
  totalContractCount?: number
  totalReportCount?: number
  contracts?: ContractAssessmentContractItem[]
  unassignedReports?: ContractAssessmentReportItem[]
}

export type MarketingPlanModuleType = 'SPEECH' | 'POLICY'
export type MarketingPlanStatus =
  | 'DRAFT'
  | 'PENDING_APPROVAL'
  | 'APPROVED'
  | 'REJECTED'
  | 'PUBLISHED'
  | 'ACTIVE'
  | 'INACTIVE'

export interface MarketingPlanItem {
  id: Id
  moduleType: MarketingPlanModuleType
  title: string
  summary?: string
  content?: string
  quarterLabel?: string
  target?: string
  owner?: string
  campaignCode?: string
  sourceTag?: string
  budgetAmount?: number
  targetLeadCount?: number
  targetReservationCount?: number
  targetContractCount?: number
  actualLeadCount?: number
  actualReservationCount?: number
  actualContractCount?: number
  actualContractRate?: number
  priority: number
  status: MarketingPlanStatus
  effectiveDate?: string
  linkedDepartmentIds?: Array<string | number>
  linkedDepartmentNames?: string[]
  latestApprovalStatus?: 'APPROVED' | 'REJECTED'
  latestApprovalRemark?: string
  latestApprovalTime?: string
  totalStaffCount?: number
  readCount?: number
  unreadCount?: number
  agreeCount?: number
  improveCount?: number
  currentUserRead?: boolean
  currentUserAction?: 'AGREE' | 'IMPROVE'
  createTime?: string
  updateTime?: string
}

export interface MarketingPlanQuery {
  pageNo?: number
  pageSize?: number
  moduleType?: MarketingPlanModuleType
  status?: MarketingPlanStatus
  keyword?: string
}

export interface MarketingPlanPayload {
  moduleType: MarketingPlanModuleType
  title: string
  summary?: string
  content?: string
  quarterLabel?: string
  target?: string
  owner?: string
  campaignCode?: string
  sourceTag?: string
  budgetAmount?: number
  targetLeadCount?: number
  targetReservationCount?: number
  targetContractCount?: number
  priority?: number
  status?: MarketingPlanStatus
  effectiveDate?: string
  linkedDepartmentIds?: Array<string | number>
}

export interface MarketingPlanApprovalPayload {
  remark?: string
}

export interface MarketingPlanReadConfirmPayload {
  action: 'AGREE' | 'IMPROVE'
  actionDetail?: string
}

export interface MarketingPlanReceiptItem {
  staffId: Id
  staffName?: string
  readTime?: string
  action?: 'AGREE' | 'IMPROVE'
  actionDetail?: string
  actionTime?: string
}

export interface MarketingPlanWorkflowSummary {
  planId: Id
  totalStaffCount: number
  readCount: number
  unreadCount: number
  agreeCount: number
  improveCount: number
  receipts: MarketingPlanReceiptItem[]
}
