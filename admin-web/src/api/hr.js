import request, { fetchPage } from '../utils/request';
export function getHrStaffPage(params) {
    return fetchPage('/api/admin/hr/staff/page', params);
}
export function getHrWorkbenchSummary(params) {
    return request.get('/api/admin/hr/workbench/summary', { params });
}
export function getHrContractReminderPage(params) {
    return fetchPage('/api/admin/hr/contract/reminder/page', params);
}
export function getHrAttendanceAbnormalPage(params) {
    return fetchPage('/api/admin/hr/attendance/abnormal/page', params);
}
export function getHrRecruitmentNeedPage(params) {
    return fetchPage('/api/admin/hr/recruitment/need/page', params);
}
export function createHrRecruitmentNeed(data) {
    return request.post('/api/admin/hr/recruitment/need', data);
}
export function updateHrRecruitmentNeed(id, data) {
    return request.put(`/api/admin/hr/recruitment/need/${id}`, data);
}
export function deleteHrRecruitmentNeed(id) {
    return request.delete(`/api/admin/hr/recruitment/need/${id}`);
}
export function batchUpdateHrRecruitmentNeedStatus(ids, status) {
    return request.put('/api/admin/hr/recruitment/need/batch/status', { ids }, { params: { status } });
}
export function bootstrapHrMaterialsFolders(params) {
    return request.post('/api/admin/hr/recruitment/materials/folder/bootstrap', null, { params });
}
export function getHrPolicyPage(params) {
    return fetchPage('/api/admin/hr/policy/page', params);
}
export function getHrPolicyAlertPage(params) {
    return fetchPage('/api/admin/hr/policy-alert/page', params);
}
export function getHrProfileContractPage(params) {
    return fetchPage('/api/admin/hr/profile/contract/page', params);
}
export function getHrProfileTemplatePage(params) {
    return fetchPage('/api/admin/hr/profile/template/page', params);
}
export function createHrProfileTemplate(data) {
    return request.post('/api/admin/hr/profile/template', data);
}
export function getHrProfileAttachmentPage(params) {
    return fetchPage('/api/admin/hr/profile/attachment/page', params);
}
export function createHrProfileAttachment(data) {
    return request.post('/api/admin/hr/profile/attachment', data);
}
export function getHrProfileCertificatePage(params) {
    return fetchPage('/api/admin/hr/profile/certificate/page', params);
}
export function createHrProfileCertificate(data) {
    return request.post('/api/admin/hr/profile/certificate', data);
}
export function getHrProfileCertificateReminderPage(params) {
    return fetchPage('/api/admin/hr/profile/certificate/reminder/page', params);
}
export function getHrAccessControlPage(params) {
    return fetchPage('/api/admin/hr/integration/access-control/page', params);
}
export function getHrCardSyncPage(params) {
    return fetchPage('/api/admin/hr/integration/card-sync/page', params);
}
export function getHrMealFeePage(params) {
    return fetchPage('/api/admin/hr/expense/meal-fee/page', params);
}
export function getHrElectricityFeePage(params) {
    return fetchPage('/api/admin/hr/expense/electricity-fee/page', params);
}
export function getHrTrainingReimbursePage(params) {
    return fetchPage('/api/admin/hr/expense/training-reimburse/page', params);
}
export function getHrSubsidyPage(params) {
    return fetchPage('/api/admin/hr/expense/subsidy/page', params);
}
export function getHrSalarySubsidyPage(params) {
    return fetchPage('/api/admin/hr/expense/salary-subsidy/page', params);
}
export function getHrExpenseApprovalFlowPage(params) {
    return fetchPage('/api/admin/hr/expense/approval-flow/page', params);
}
export function createHrExpenseApprovalFlow(data) {
    return request.post('/api/admin/hr/expense/approval-flow', data);
}
export function getHrAttendanceLeavePage(params) {
    return fetchPage('/api/admin/hr/attendance/leave/page', params);
}
export function getHrAttendanceShiftChangePage(params) {
    return fetchPage('/api/admin/hr/attendance/shift-change/page', params);
}
export function getHrAttendanceOvertimePage(params) {
    return fetchPage('/api/admin/hr/attendance/overtime/page', params);
}
export function getHrAttendanceRecordPage(params) {
    return fetchPage('/api/admin/hr/attendance/record/page', params);
}
export function getHrProfile(staffId) {
    return request.get(`/api/admin/hr/profile/${staffId}`);
}
export function upsertHrProfile(data) {
    return request.post('/api/admin/hr/profile', data);
}
export function getHrTrainingPage(params) {
    return fetchPage('/api/admin/hr/training/page', params);
}
export function createHrTraining(data) {
    return request.post('/api/admin/hr/training', data);
}
export function updateHrTraining(id, data) {
    return request.put(`/api/admin/hr/training/${id}`, data);
}
export function deleteHrTraining(id) {
    return request.delete(`/api/admin/hr/training/${id}`);
}
export function adjustStaffPoints(data) {
    return request.post('/api/admin/hr/points/adjust', data);
}
export function terminateStaff(params) {
    return request.post('/api/admin/hr/terminate', null, { params });
}
export function reinstateStaff(params) {
    return request.post('/api/admin/hr/reinstate', null, { params });
}
export function getStaffPointsLog(params) {
    return fetchPage('/api/admin/hr/points/log/page', params);
}
export function getPerformanceSummary(params) {
    return request.get('/api/admin/hr/performance/summary', { params });
}
export function getPerformanceRanking(params) {
    return request.get('/api/admin/hr/performance/ranking', { params });
}
export function getPointsRulePage(params) {
    return fetchPage('/api/admin/hr/points/rule/page', params);
}
export function upsertPointsRule(data) {
    return request.post('/api/admin/hr/points/rule', data);
}
export function deletePointsRule(id) {
    return request.delete(`/api/admin/hr/points/rule/${id}`);
}
export function getRewardPunishmentPage(params) {
    return fetchPage('/api/admin/hr/reward-punishment/page', params);
}
export function createRewardPunishment(data) {
    return request.post('/api/admin/hr/reward-punishment', data);
}
export function updateRewardPunishment(id, data) {
    return request.put(`/api/admin/hr/reward-punishment/${id}`, data);
}
export function deleteRewardPunishment(id) {
    return request.delete(`/api/admin/hr/reward-punishment/${id}`);
}
