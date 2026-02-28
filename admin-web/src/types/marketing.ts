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
  remark?: string
}

export interface MarketingCallbackReport {
  todayDue: number
  overdue: number
  completed: number
  total: number
  records: MarketingCallbackItem[]
}

export interface MarketingReportQuery {
  dateFrom?: string
  dateTo?: string
  source?: string
  staffId?: number
}

export interface MarketingDataQualityReport {
  missingSourceCount: number
  missingNextFollowDateCount: number
  nonStandardSourceCount: number
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
  ids: Array<number | string>
  status: number
  followupStatus?: string
  invalidTime?: string
}

export interface LeadBatchDeleteRequest {
  ids?: Array<number | string>
  contractNos?: string[]
}

export interface CallbackPlanItem {
  id: number | string
  leadId: number | string
  title: string
  followupContent?: string
  planExecuteTime: string
  executorName?: string
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
}

export interface CallbackExecuteRequest {
  executeNote?: string
  followupResult?: string
  nextFollowDate?: string
}

export interface ContractAttachmentItem {
  id: number | string
  leadId: number | string
  contractId?: number
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
  contractId?: number
  contractNo?: string
  attachmentType?: string
  fileName: string
  fileUrl?: string
  fileType?: string
  fileSize?: number
  remark?: string
}

export interface SmsTaskItem {
  id: number | string
  leadId: number | string
  contractId?: number
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
  leadIds?: number[]
  contractIds?: number[]
  templateName?: string
  content?: string
  planSendTime?: string
}

export interface CrmContractItem {
  [key: string]: any
  id: number | string
  name?: string
  phone?: string
  leadId?: number
  elderId?: number
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
  flowStage?: 'PENDING_ASSESSMENT' | 'PENDING_BED_SELECT' | 'PENDING_SIGN' | 'SIGNED'
  currentOwnerDept?: 'MARKETING' | 'ASSESSMENT'
  orgName?: string
  status?: string
  smsSendCount?: number
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface CrmContractPayload {
  [key: string]: any
  name?: string
  phone?: string
  leadId?: number
  elderId?: number
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
  status?: string
  smsSendCount?: number
  remark?: string
}

export interface ContractBatchDeleteRequest {
  ids?: number[]
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
  contractId?: number
  leadId?: number
  elderId?: number
  elderName?: string
  elderPhone?: string
  orgName?: string
  marketerName?: string
  contractNo?: string
  contractStatus?: string
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
}

export interface ContractAssessmentContractItem {
  leadId?: number
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
  elderId?: number
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
  | 'INACTIVE'

export interface MarketingPlanItem {
  id: number | string
  moduleType: MarketingPlanModuleType
  title: string
  summary?: string
  content?: string
  quarterLabel?: string
  target?: string
  owner?: string
  priority: number
  status: MarketingPlanStatus
  effectiveDate?: string
  linkedDepartmentIds?: number[]
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
  priority?: number
  status?: MarketingPlanStatus
  effectiveDate?: string
  linkedDepartmentIds?: number[]
}

export interface MarketingPlanApprovalPayload {
  remark?: string
}

export interface MarketingPlanReadConfirmPayload {
  action: 'AGREE' | 'IMPROVE'
  actionDetail?: string
}

export interface MarketingPlanReceiptItem {
  staffId: number | string
  staffName?: string
  readTime?: string
  action?: 'AGREE' | 'IMPROVE'
  actionDetail?: string
  actionTime?: string
}

export interface MarketingPlanWorkflowSummary {
  planId: number | string
  totalStaffCount: number
  readCount: number
  unreadCount: number
  agreeCount: number
  improveCount: number
  receipts: MarketingPlanReceiptItem[]
}
