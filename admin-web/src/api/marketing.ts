import { getCrmLeadPage, createCrmLead, updateCrmLead, deleteCrmLead } from './crm'
import { getAdmissionRecords } from './elderLifecycle'
import request from '../utils/request'
import type {
  AdmissionRecordItem,
  CallbackExecuteRequest,
  CallbackPlanCreateRequest,
  CallbackPlanItem,
  ContractAttachmentCreateRequest,
  ContractAttachmentItem,
  CrmLeadItem,
  LeadBatchDeleteRequest,
  LeadBatchStatusRequest,
  MarketingCallbackReport,
  MarketingChannelReportItem,
  MarketingConsultationTrendItem,
  MarketingConversionReport,
  MarketingDataQualityReport,
  MarketingFollowupReport,
  MarketingReportQuery,
  PageResult,
  SmsTaskCreateRequest,
  SmsTaskItem
} from '../types'

export { createCrmLead, updateCrmLead, deleteCrmLead }

export function getLeadPage(params: any) {
  return getCrmLeadPage(params)
}

export function batchUpdateLeadStatus(data: LeadBatchStatusRequest) {
  return request.post<number>('/api/crm/leads/batch/status', data)
}

export function batchDeleteLeads(data: LeadBatchDeleteRequest) {
  return request.post<number>('/api/crm/leads/batch/delete', data)
}

export function createLeadCallbackPlan(leadId: number, data: CallbackPlanCreateRequest) {
  return request.post<CallbackPlanItem>(`/api/crm/leads/${leadId}/callback-plans`, data)
}

export function getLeadCallbackPlans(leadId: number) {
  return request.get<CallbackPlanItem[]>(`/api/crm/leads/${leadId}/callback-plans`)
}

export function executeCallbackPlan(planId: number, data: CallbackExecuteRequest) {
  return request.post<CallbackPlanItem>(`/api/crm/leads/callback-plans/${planId}/execute`, data)
}

export function createLeadAttachment(leadId: number, data: ContractAttachmentCreateRequest) {
  return request.post<ContractAttachmentItem>(`/api/crm/leads/${leadId}/attachments`, data)
}

export function getLeadAttachments(leadId: number) {
  return request.get<ContractAttachmentItem[]>(`/api/crm/leads/${leadId}/attachments`)
}

export function deleteLeadAttachment(attachmentId: number) {
  return request.delete<void>(`/api/crm/leads/attachments/${attachmentId}`)
}

export function createSmsTasks(data: SmsTaskCreateRequest) {
  return request.post<SmsTaskItem[]>('/api/crm/leads/sms/tasks', data)
}

export function getSmsTasks(leadId?: number) {
  return request.get<SmsTaskItem[]>('/api/crm/leads/sms/tasks', { params: { leadId } })
}

export function sendSmsTask(taskId: number) {
  return request.post<SmsTaskItem>(`/api/crm/leads/sms/tasks/${taskId}/send`)
}

export async function getAllLeads(pageSize = 200): Promise<CrmLeadItem[]> {
  let pageNo = 1
  let total = 0
  const result: CrmLeadItem[] = []
  do {
    const page: PageResult<CrmLeadItem> = await getCrmLeadPage({ pageNo, pageSize })
    result.push(...(page.list || []))
    total = page.total || 0
    pageNo += 1
  } while (result.length < total && pageNo <= 30)
  return result
}

export async function getAllAdmissions(pageSize = 200): Promise<AdmissionRecordItem[]> {
  let pageNo = 1
  let total = 0
  const result: AdmissionRecordItem[] = []
  do {
    const page: PageResult<AdmissionRecordItem> = await getAdmissionRecords({ pageNo, pageSize })
    result.push(...(page.list || []))
    total = page.total || 0
    pageNo += 1
  } while (result.length < total && pageNo <= 30)
  return result
}

export function getMarketingConversionReport(params?: MarketingReportQuery) {
  return request.get<MarketingConversionReport>('/api/marketing/report/conversion', { params })
}

export function getMarketingConsultationTrend(days = 14, params?: MarketingReportQuery) {
  return request.get<MarketingConsultationTrendItem[]>('/api/marketing/report/consultation', {
    params: { days, ...(params || {}) }
  })
}

export function getMarketingChannelReport(params?: MarketingReportQuery) {
  return request.get<MarketingChannelReportItem[]>('/api/marketing/report/channel', { params })
}

export function getMarketingFollowupReport(params?: MarketingReportQuery) {
  return request.get<MarketingFollowupReport>('/api/marketing/report/followup', { params })
}

export function getMarketingCallbackReport(params?: { pageNo?: number; pageSize?: number } & MarketingReportQuery) {
  return request.get<MarketingCallbackReport>('/api/marketing/report/callback', { params })
}

export function getMarketingDataQualityReport() {
  return request.get<MarketingDataQualityReport>('/api/marketing/report/data-quality')
}

export function normalizeMarketingSources() {
  return request.post<number>('/api/marketing/report/data-quality/normalize-source')
}
