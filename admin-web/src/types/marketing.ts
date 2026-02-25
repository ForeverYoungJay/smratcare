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

export interface LeadBatchStatusRequest {
  ids: number[]
  status: number
  followupStatus?: string
  invalidTime?: string
}

export interface LeadBatchDeleteRequest {
  ids: number[]
}

export interface CallbackPlanItem {
  id: number
  leadId: number
  title: string
  planExecuteTime: string
  executorName?: string
  status: string
  executedTime?: string
  executeNote?: string
  createTime?: string
}

export interface CallbackPlanCreateRequest {
  title: string
  planExecuteTime: string
  executorName?: string
}

export interface CallbackExecuteRequest {
  executeNote?: string
  nextFollowDate?: string
}

export interface ContractAttachmentItem {
  id: number
  leadId: number
  contractNo?: string
  fileName: string
  fileUrl?: string
  fileType?: string
  fileSize?: number
  remark?: string
  createTime?: string
}

export interface ContractAttachmentCreateRequest {
  contractNo?: string
  fileName: string
  fileUrl?: string
  fileType?: string
  fileSize?: number
  remark?: string
}

export interface SmsTaskItem {
  id: number
  leadId: number
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
  leadIds: number[]
  templateName?: string
  content?: string
  planSendTime?: string
}

export interface UploadedFileResult {
  fileName: string
  originalFileName?: string
  fileUrl: string
  fileType?: string
  fileSize?: number
}
