import { getCrmLeadPage, createCrmLead, updateCrmLead, deleteCrmLead } from './crm'
import { getAdmissionRecords } from './elderLifecycle'
import request, { fetchPage } from '../utils/request'
import type { Id } from '../types/common'
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
  MarketingPlanItem,
  MarketingPlanApprovalPayload,
  MarketingPlanReadConfirmPayload,
  MarketingPlanReceiptItem,
  MarketingPlanPayload,
  MarketingPlanQuery,
  MarketingPlanWorkflowSummary,
  MarketingChannelReportItem,
  MarketingConsultationTrendItem,
  MarketingConversionReport,
  MarketingDataQualityReport,
  MarketingFollowupReport,
  MarketingLeadEntrySummary,
  MarketingLeadMode,
  MarketingReportQuery,
  PageResult,
  SmsTaskCreateRequest,
  SmsTaskItem,
  UploadedFileResult,
  ContractLinkageSummary,
  ContractArchiveRuleInfo,
  ContractAssessmentOverview,
  CrmContractItem,
  CrmContractStageSummary,
  CrmContractPayload,
  ContractBatchDeleteRequest
} from '../types'

export { createCrmLead, updateCrmLead, deleteCrmLead }

export function getLeadPage(params: any, config?: Record<string, any>) {
  return getCrmLeadPage(params, config)
}

export function getContractPage(params: any, config?: Record<string, any>) {
  return fetchPage<CrmContractItem>('/api/crm/contracts/page', params, config)
}

export function getContractStageSummary(params?: { currentOwnerDept?: string }) {
  return request.get<CrmContractStageSummary>('/api/crm/contracts/stage-summary', { params })
}

export function getCrmContract(id: Id) {
  return request.get<CrmContractItem>(`/api/crm/contracts/${id}`)
}

export function createCrmContract(data: CrmContractPayload) {
  return request.post<CrmContractItem>('/api/crm/contracts', data)
}

export function updateCrmContract(id: Id, data: CrmContractPayload) {
  return request.put<CrmContractItem>(`/api/crm/contracts/${id}`, data)
}

export function deleteCrmContract(id: Id) {
  return request.delete<void>(`/api/crm/contracts/${id}`)
}

export function batchDeleteContracts(data: ContractBatchDeleteRequest) {
  return request.post<number>('/api/crm/contracts/batch/delete', data)
}

export function handoffContractToAssessment(contractId: Id) {
  return request.post<CrmContractItem>(`/api/crm/contracts/${contractId}/handoff-assessment`)
}

export function finalizeContract(contractId: Id, remark?: string) {
  return request.post<CrmContractItem>(`/api/crm/contracts/${contractId}/finalize`, { remark })
}

export function approveContract(contractId: Id, remark?: string) {
  return request.post<CrmContractItem>(`/api/crm/contracts/${contractId}/approve`, { remark })
}

export function rejectContract(contractId: Id, remark?: string) {
  return request.post<CrmContractItem>(`/api/crm/contracts/${contractId}/reject`, { remark })
}

export function voidContract(contractId: Id, remark?: string) {
  return request.post<CrmContractItem>(`/api/crm/contracts/${contractId}/void`, { remark })
}

export function batchUpdateLeadStatus(data: LeadBatchStatusRequest) {
  return request.post<number>('/api/crm/leads/batch/status', data)
}

export function batchDeleteLeads(data: LeadBatchDeleteRequest) {
  return request.post<number>('/api/crm/leads/batch/delete', data)
}

export function handoffLeadToAssessment(contractNo: string) {
  return request.post<CrmLeadItem>(`/api/crm/leads/contract/${encodeURIComponent(contractNo)}/handoff-assessment`)
}

export function createLeadCallbackPlan(leadId: Id, data: CallbackPlanCreateRequest) {
  return request.post<CallbackPlanItem>(`/api/crm/leads/${leadId}/callback-plans`, data)
}

export function getLeadCallbackPlans(leadId: Id) {
  return request.get<CallbackPlanItem[]>(`/api/crm/leads/${leadId}/callback-plans`)
}

export function executeCallbackPlan(planId: Id, data: CallbackExecuteRequest) {
  return request.post<CallbackPlanItem>(`/api/crm/leads/callback-plans/${planId}/execute`, data)
}

export function createLeadAttachment(leadId: Id, data: ContractAttachmentCreateRequest) {
  return request.post<ContractAttachmentItem>(`/api/crm/leads/${leadId}/attachments`, data)
}

export function getLeadAttachments(leadId: Id) {
  return request.get<ContractAttachmentItem[]>(`/api/crm/leads/${leadId}/attachments`)
}

export function deleteLeadAttachment(attachmentId: Id) {
  return request.delete<void>(`/api/crm/leads/attachments/${attachmentId}`)
}

export function createSmsTasks(data: SmsTaskCreateRequest) {
  return request.post<SmsTaskItem[]>('/api/crm/leads/sms/tasks', data)
}

export function getSmsTasks(leadId?: Id) {
  return request.get<SmsTaskItem[]>('/api/crm/leads/sms/tasks', { params: { leadId } })
}

export function sendSmsTask(taskId: Id) {
  return request.post<SmsTaskItem>(`/api/crm/leads/sms/tasks/${taskId}/send`)
}

export function createContractAttachment(contractId: Id, data: ContractAttachmentCreateRequest) {
  return request.post<ContractAttachmentItem>(`/api/crm/contracts/${contractId}/attachments`, data)
}

export function getContractAttachments(contractId: Id) {
  return request.get<ContractAttachmentItem[]>(`/api/crm/contracts/${contractId}/attachments`)
}

export function deleteContractAttachment(attachmentId: Id) {
  return request.delete<void>(`/api/crm/contracts/attachments/${attachmentId}`)
}

export function createContractSmsTasks(data: SmsTaskCreateRequest) {
  return request.post<SmsTaskItem[]>('/api/crm/contracts/sms/tasks', data)
}

export function getContractSmsTasks(contractId?: Id) {
  return request.get<SmsTaskItem[]>('/api/crm/contracts/sms/tasks', { params: { contractId } })
}

export function sendContractSmsTask(taskId: Id) {
  return request.post<SmsTaskItem>(`/api/crm/contracts/sms/tasks/${taskId}/send`)
}

export function uploadMarketingFile(file: File, bizType = 'marketing-contract') {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('bizType', bizType)
  return request.post<UploadedFileResult>('/api/files/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
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

export function getMarketingConversionReport(params?: MarketingReportQuery, config?: Record<string, any>) {
  return request.get<MarketingConversionReport>('/api/marketing/report/conversion', { params, ...(config || {}) })
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

export function getMarketingLeadEntrySummary(params: {
  mode: MarketingLeadMode
  keyword?: string
  consultantName?: string
  consultantPhone?: string
  elderName?: string
  elderPhone?: string
  consultDateFrom?: string
  consultDateTo?: string
  consultType?: string
  mediaChannel?: string
  infoSource?: string
  customerTag?: string
  marketerName?: string
}) {
  return request.get<MarketingLeadEntrySummary>('/api/marketing/report/lead-entry-summary', { params })
}

export function normalizeMarketingSources() {
  return request.post<number>('/api/marketing/report/data-quality/normalize-source')
}

export function getContractLinkageByElder(elderId: Id) {
  return request.get<ContractLinkageSummary>('/api/crm/contracts/linkage', { params: { elderId } })
}

export function getContractLinkageByLead(leadId: Id) {
  return request.get<ContractLinkageSummary>(`/api/crm/contracts/${leadId}/linkage`)
}

export function getContractLinkageByContract(contractId: Id) {
  return request.get<ContractLinkageSummary>(`/api/crm/contracts/${contractId}/linkage-by-contract`)
}

export function getContractArchiveRule() {
  return request.get<ContractArchiveRuleInfo>('/api/crm/contracts/linkage-archive-rule')
}

export function getContractAssessmentOverview(params: { elderId?: Id; leadId?: Id }) {
  return request.get<ContractAssessmentOverview>('/api/crm/contracts/assessment-overview', { params })
}

export function getMarketingPlanPage(params: MarketingPlanQuery) {
  return fetchPage<MarketingPlanItem>('/api/marketing/plans/page', params as Record<string, any>)
}

export function getMarketingPlanList(moduleType?: 'SPEECH' | 'POLICY') {
  return request.get<MarketingPlanItem[]>('/api/marketing/plans', { params: { moduleType } })
}

export function createMarketingPlan(data: MarketingPlanPayload) {
  return request.post<MarketingPlanItem>('/api/marketing/plans', data)
}

export function updateMarketingPlan(id: Id, data: MarketingPlanPayload) {
  return request.put<MarketingPlanItem>(`/api/marketing/plans/${id}`, data)
}

export function deleteMarketingPlan(id: Id) {
  return request.delete<void>(`/api/marketing/plans/${id}`)
}

export function getMarketingPlanDetail(id: Id) {
  return request.get<MarketingPlanItem>(`/api/marketing/plans/${id}`)
}

export function submitMarketingPlan(id: Id) {
  return request.post<MarketingPlanItem>(`/api/marketing/plans/${id}/submit`)
}

export function approveMarketingPlan(id: Id, data?: MarketingPlanApprovalPayload) {
  return request.post<MarketingPlanItem>(`/api/marketing/plans/${id}/approve`, data || {})
}

export function rejectMarketingPlan(id: Id, data?: MarketingPlanApprovalPayload) {
  return request.post<MarketingPlanItem>(`/api/marketing/plans/${id}/reject`, data || {})
}

export function publishMarketingPlan(id: Id) {
  return request.post<MarketingPlanItem>(`/api/marketing/plans/${id}/publish`)
}

export function deactivateMarketingPlan(id: Id) {
  return request.post<MarketingPlanItem>(`/api/marketing/plans/${id}/deactivate`)
}

export function confirmMarketingPlanRead(id: Id, data: MarketingPlanReadConfirmPayload) {
  return request.post<MarketingPlanItem>(`/api/marketing/plans/${id}/receipt`, data)
}

export function getMarketingPlanReceipts(id: Id) {
  return request.get<MarketingPlanReceiptItem[]>(`/api/marketing/plans/${id}/receipts`)
}

export function getMarketingPlanWorkflowSummary(id: Id) {
  return request.get<MarketingPlanWorkflowSummary>(`/api/marketing/plans/${id}/workflow-summary`)
}
