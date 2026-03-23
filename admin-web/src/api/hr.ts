import request, { fetchPage } from '../utils/request'
import type {
  HrAttendanceAbnormalItem,
  HrContractReminderItem,
  HrPolicyAlertItem,
  HrProfileContractItem,
  HrProfileDocumentItem,
  HrRecruitmentNeedItem,
  HrSocialSecurityReminderItem,
  HrSocialSecurityApplyRequest,
  HrSocialSecurityCompleteRequest,
  HrSocialSecurityBillGenerateRequest,
  HrAccessControlRecordItem,
  HrCardSyncItem,
  HrExpenseItem,
  HrExpenseApprovalRequest,
  HrGenericApprovalItem,
  HrAttendanceRecordItem,
  HrStaffBirthdayItem,
  HrStaffCertificateItem,
  HrStaffServicePlan,
  HrStaffMonthlyFeeBillItem,
  HrStaffFeeGenerateRequest,
  HrStaffElectricityImportRequest,
  HrStaffFeeSyncRequest,
  HrBatchActionSummary,
  HrSocialSecuritySummary,
  HrWorkbenchSummary,
  HrStaffProfile,
  StaffTrainingRecord,
  StaffPointsAccount,
  StaffPointsLog,
  StaffPerformanceSummary,
  StaffPerformanceRankItem,
  StaffPointsRule,
  StaffRewardPunishment
} from '../types'

export function getHrStaffPage(params: any) {
  return fetchPage<HrStaffProfile>('/api/admin/hr/staff/page', params)
}

export function getHrStaffBirthdayPage(params: any) {
  return fetchPage<HrStaffBirthdayItem>('/api/admin/hr/staff/birthday/page', params)
}

export function getHrWorkbenchSummary(params?: { warningDays?: number }, config?: Record<string, any>) {
  return request.get<HrWorkbenchSummary>('/api/admin/hr/workbench/summary', { params, ...(config || {}) })
}

export function getHrContractReminderPage(params: any) {
  return fetchPage<HrContractReminderItem>('/api/admin/hr/contract/reminder/page', params)
}

export function getHrAttendanceAbnormalPage(params: any) {
  return fetchPage<HrAttendanceAbnormalItem>('/api/admin/hr/attendance/abnormal/page', params)
}

export function getHrRecruitmentNeedPage(params: any) {
  return fetchPage<HrRecruitmentNeedItem>('/api/admin/hr/recruitment/need/page', params)
}

export function createHrRecruitmentNeed(data: Partial<HrRecruitmentNeedItem>) {
  return request.post<HrRecruitmentNeedItem>('/api/admin/hr/recruitment/need', data)
}

export function updateHrRecruitmentNeed(id: string | number, data: Partial<HrRecruitmentNeedItem>) {
  return request.put<HrRecruitmentNeedItem>(`/api/admin/hr/recruitment/need/${id}`, data)
}

export function deleteHrRecruitmentNeed(id: string | number) {
  return request.delete<void>(`/api/admin/hr/recruitment/need/${id}`)
}

export function batchUpdateHrRecruitmentNeedStatus(ids: Array<string | number>, status: string) {
  return request.put<number>('/api/admin/hr/recruitment/need/batch/status', { ids }, { params: { status } })
}

export function bootstrapHrMaterialsFolders(params: { year?: number; departmentName?: string }) {
  return request.post<{ folderPath?: string; onboardingFolderId?: string | number; offboardingFolderId?: string | number }>(
    '/api/admin/hr/recruitment/materials/folder/bootstrap',
    null,
    { params }
  )
}

export function getHrPolicyPage(params: any) {
  return fetchPage<HrProfileDocumentItem>('/api/admin/hr/policy/page', params)
}

export function createHrPolicy(data: Partial<HrProfileDocumentItem>) {
  return request.post<HrProfileDocumentItem>('/api/admin/hr/policy', data)
}

export function getHrPolicyAlertPage(params: any) {
  return fetchPage<HrPolicyAlertItem>('/api/admin/hr/policy-alert/page', params)
}

export function getHrProfileContractPage(params: any) {
  return fetchPage<HrProfileContractItem>('/api/admin/hr/profile/contract/page', params)
}

export function getHrProfileTemplatePage(params: any) {
  return fetchPage<HrProfileDocumentItem>('/api/admin/hr/profile/template/page', params)
}

export function createHrProfileTemplate(data: Partial<HrProfileDocumentItem>) {
  return request.post<HrProfileDocumentItem>('/api/admin/hr/profile/template', data)
}

export function getHrProfileAttachmentPage(params: any) {
  return fetchPage<HrProfileDocumentItem>('/api/admin/hr/profile/attachment/page', params)
}

export function createHrProfileAttachment(data: Partial<HrProfileDocumentItem>) {
  return request.post<HrProfileDocumentItem>('/api/admin/hr/profile/attachment', data)
}

export function getHrContractAttachmentPage(params: any) {
  return fetchPage<HrProfileDocumentItem>('/api/admin/hr/profile/contract-attachment/page', params)
}

export function createHrContractAttachment(data: Partial<HrProfileDocumentItem>) {
  return request.post<HrProfileDocumentItem>('/api/admin/hr/profile/contract-attachment', data)
}

export function getHrProfileCertificatePage(params: any) {
  return fetchPage<HrStaffCertificateItem>('/api/admin/hr/profile/certificate/page', params)
}

export function createHrProfileCertificate(data: Partial<HrStaffCertificateItem>) {
  return request.post<HrStaffCertificateItem>('/api/admin/hr/profile/certificate', data)
}

export function getHrProfileCertificateReminderPage(params: any, config?: Record<string, any>) {
  return fetchPage<HrStaffCertificateItem>('/api/admin/hr/profile/certificate/reminder/page', params, config)
}

export function getHrAccessControlPage(params: any) {
  return fetchPage<HrAccessControlRecordItem>('/api/admin/hr/integration/access-control/page', params)
}

export function getHrCardSyncPage(params: any) {
  return fetchPage<HrCardSyncItem>('/api/admin/hr/integration/card-sync/page', params)
}

export function getHrMealFeePage(params: any) {
  return fetchPage<HrStaffMonthlyFeeBillItem>('/api/admin/hr/expense/meal-fee/page', params)
}

export function getHrElectricityFeePage(params: any) {
  return fetchPage<HrStaffMonthlyFeeBillItem>('/api/admin/hr/expense/electricity-fee/page', params)
}

export function getHrStaffServicePlan(staffId: string | number) {
  return request.get<HrStaffServicePlan>(`/api/admin/hr/expense/service-plan/${staffId}`)
}

export function upsertHrStaffServicePlan(data: HrStaffServicePlan) {
  return request.post<HrStaffServicePlan>('/api/admin/hr/expense/service-plan', data)
}

export function generateHrMealFee(data: HrStaffFeeGenerateRequest) {
  return request.post<HrBatchActionSummary>('/api/admin/hr/expense/meal-fee/generate', data)
}

export function importHrElectricityFee(data: HrStaffElectricityImportRequest) {
  return request.post<HrBatchActionSummary>('/api/admin/hr/expense/electricity-fee/import', data)
}

export function syncHrMonthlyFeeToFinance(data: HrStaffFeeSyncRequest) {
  return request.post<HrBatchActionSummary>('/api/admin/hr/expense/monthly-fee/sync-finance', data)
}

export function getHrTrainingReimbursePage(params: any) {
  return fetchPage<HrExpenseItem>('/api/admin/hr/expense/training-reimburse/page', params)
}

export function getHrSubsidyPage(params: any) {
  return fetchPage<HrExpenseItem>('/api/admin/hr/expense/subsidy/page', params)
}

export function getHrSalarySubsidyPage(params: any) {
  return fetchPage<HrExpenseItem>('/api/admin/hr/expense/salary-subsidy/page', params)
}

export function getHrExpenseApprovalFlowPage(params: any) {
  return fetchPage<HrExpenseItem>('/api/admin/hr/expense/approval-flow/page', params)
}

export function createHrExpenseApprovalFlow(data: HrExpenseApprovalRequest) {
  return request.post<HrExpenseItem>('/api/admin/hr/expense/approval-flow', data)
}

export function getHrAttendanceLeavePage(params: any) {
  return fetchPage<HrGenericApprovalItem>('/api/admin/hr/attendance/leave/page', params)
}

export function getHrAttendanceShiftChangePage(params: any) {
  return fetchPage<HrGenericApprovalItem>('/api/admin/hr/attendance/shift-change/page', params)
}

export function getHrAttendanceOvertimePage(params: any) {
  return fetchPage<HrGenericApprovalItem>('/api/admin/hr/attendance/overtime/page', params)
}

export function getHrAttendanceRecordPage(params: any) {
  return fetchPage<HrAttendanceRecordItem>('/api/admin/hr/attendance/record/page', params)
}

export function getHrProfile(staffId: string | number) {
  return request.get<HrStaffProfile>(`/api/admin/hr/profile/${staffId}`)
}

export function upsertHrProfile(data: Partial<HrStaffProfile>) {
  return request.post<HrStaffProfile>('/api/admin/hr/profile', data)
}

export function getHrSocialSecuritySummary() {
  return request.get<HrSocialSecuritySummary>('/api/admin/hr/social-security/summary')
}

export function getHrSocialSecurityReminderPage(params: any) {
  return fetchPage<HrSocialSecurityReminderItem>('/api/admin/hr/social-security/reminder/page', params)
}

export function applyHrSocialSecurity(data: HrSocialSecurityApplyRequest) {
  return request.post<HrStaffProfile>('/api/admin/hr/social-security/apply', data)
}

export function decideHrSocialSecurityByDirector(staffId: string | number, approved: boolean, remark?: string) {
  return request.put<HrStaffProfile>(`/api/admin/hr/social-security/${staffId}/director-decision`, null, {
    params: { approved, remark }
  })
}

export function completeHrSocialSecurity(staffId: string | number, data?: HrSocialSecurityCompleteRequest) {
  return request.put<HrStaffProfile>(`/api/admin/hr/social-security/${staffId}/complete`, data || {})
}

export function generateHrSocialSecurityMonthlyBill(data?: HrSocialSecurityBillGenerateRequest) {
  return request.post<HrBatchActionSummary>('/api/admin/hr/social-security/monthly-bill/generate', data || {})
}

export function getHrTrainingPage(params: any) {
  return fetchPage<StaffTrainingRecord>('/api/admin/hr/training/page', params)
}

export function createHrTraining(data: Partial<StaffTrainingRecord>) {
  return request.post<StaffTrainingRecord>('/api/admin/hr/training', data)
}

export function updateHrTraining(id: string | number, data: Partial<StaffTrainingRecord>) {
  return request.put<StaffTrainingRecord>(`/api/admin/hr/training/${id}`, data)
}

export function deleteHrTraining(id: string | number) {
  return request.delete<void>(`/api/admin/hr/training/${id}`)
}

export function adjustStaffPoints(data: { staffId: string | number; changeType: string; changePoints: number; remark?: string }) {
  return request.post<StaffPointsAccount>('/api/admin/hr/points/adjust', data)
}

export function terminateStaff(params: { staffId: string | number; leaveDate?: string; leaveReason?: string }) {
  return request.post<void>('/api/admin/hr/terminate', null, { params })
}

export function reinstateStaff(params: { staffId: string | number }) {
  return request.post<void>('/api/admin/hr/reinstate', null, { params })
}

export function getStaffPointsLog(params: any) {
  return fetchPage<StaffPointsLog>('/api/admin/hr/points/log/page', params)
}

export function getPerformanceSummary(params: { staffId: string | number; dateFrom?: string; dateTo?: string }) {
  return request.get<StaffPerformanceSummary>('/api/admin/hr/performance/summary', { params })
}

export function getPerformanceRanking(params?: { dateFrom?: string; dateTo?: string; sortBy?: string; staffCategory?: string }) {
  return request.get<StaffPerformanceRankItem[]>('/api/admin/hr/performance/ranking', { params })
}

export function getPointsRulePage(params: any) {
  return fetchPage<StaffPointsRule>('/api/admin/hr/points/rule/page', params)
}

export function upsertPointsRule(data: Partial<StaffPointsRule>) {
  return request.post<StaffPointsRule>('/api/admin/hr/points/rule', data)
}

export function deletePointsRule(id: string | number) {
  return request.delete<void>(`/api/admin/hr/points/rule/${id}`)
}

export function getRewardPunishmentPage(params: any) {
  return fetchPage<StaffRewardPunishment>('/api/admin/hr/reward-punishment/page', params)
}

export function createRewardPunishment(data: Partial<StaffRewardPunishment>) {
  return request.post<StaffRewardPunishment>('/api/admin/hr/reward-punishment', data)
}

export function updateRewardPunishment(id: string | number, data: Partial<StaffRewardPunishment>) {
  return request.put<StaffRewardPunishment>(`/api/admin/hr/reward-punishment/${id}`, data)
}

export function deleteRewardPunishment(id: string | number) {
  return request.delete<void>(`/api/admin/hr/reward-punishment/${id}`)
}
