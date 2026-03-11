import { getCrmLeadPage, createCrmLead, updateCrmLead, deleteCrmLead } from './crm';
import { getAdmissionRecords } from './elderLifecycle';
import request, { fetchPage } from '../utils/request';
export { createCrmLead, updateCrmLead, deleteCrmLead };
export function getLeadPage(params, config) {
    return getCrmLeadPage(params, config);
}
export function getContractPage(params, config) {
    return fetchPage('/api/crm/contracts/page', params, config);
}
export function getContractStageSummary(params) {
    return request.get('/api/crm/contracts/stage-summary', { params });
}
export function getCrmContract(id) {
    return request.get(`/api/crm/contracts/${id}`);
}
export function createCrmContract(data) {
    return request.post('/api/crm/contracts', data);
}
export function updateCrmContract(id, data) {
    return request.put(`/api/crm/contracts/${id}`, data);
}
export function deleteCrmContract(id) {
    return request.delete(`/api/crm/contracts/${id}`);
}
export function batchDeleteContracts(data) {
    return request.post('/api/crm/contracts/batch/delete', data);
}
export function handoffContractToAssessment(contractId) {
    return request.post(`/api/crm/contracts/${contractId}/handoff-assessment`);
}
export function finalizeContract(contractId, remark) {
    return request.post(`/api/crm/contracts/${contractId}/finalize`, { remark });
}
export function approveContract(contractId, remark) {
    return request.post(`/api/crm/contracts/${contractId}/approve`, { remark });
}
export function rejectContract(contractId, remark) {
    return request.post(`/api/crm/contracts/${contractId}/reject`, { remark });
}
export function voidContract(contractId, remark) {
    return request.post(`/api/crm/contracts/${contractId}/void`, { remark });
}
export function batchUpdateLeadStatus(data) {
    return request.post('/api/crm/leads/batch/status', data);
}
export function batchDeleteLeads(data) {
    return request.post('/api/crm/leads/batch/delete', data);
}
export function handoffLeadToAssessment(contractNo) {
    return request.post(`/api/crm/leads/contract/${encodeURIComponent(contractNo)}/handoff-assessment`);
}
export function createLeadCallbackPlan(leadId, data) {
    return request.post(`/api/crm/leads/${leadId}/callback-plans`, data);
}
export function getLeadCallbackPlans(leadId) {
    return request.get(`/api/crm/leads/${leadId}/callback-plans`);
}
export function executeCallbackPlan(planId, data) {
    return request.post(`/api/crm/leads/callback-plans/${planId}/execute`, data);
}
export function createLeadAttachment(leadId, data) {
    return request.post(`/api/crm/leads/${leadId}/attachments`, data);
}
export function getLeadAttachments(leadId) {
    return request.get(`/api/crm/leads/${leadId}/attachments`);
}
export function deleteLeadAttachment(attachmentId) {
    return request.delete(`/api/crm/leads/attachments/${attachmentId}`);
}
export function createSmsTasks(data) {
    return request.post('/api/crm/leads/sms/tasks', data);
}
export function getSmsTasks(leadId) {
    return request.get('/api/crm/leads/sms/tasks', { params: { leadId } });
}
export function sendSmsTask(taskId) {
    return request.post(`/api/crm/leads/sms/tasks/${taskId}/send`);
}
export function createContractAttachment(contractId, data) {
    return request.post(`/api/crm/contracts/${contractId}/attachments`, data);
}
export function getContractAttachments(contractId) {
    return request.get(`/api/crm/contracts/${contractId}/attachments`);
}
export function deleteContractAttachment(attachmentId) {
    return request.delete(`/api/crm/contracts/attachments/${attachmentId}`);
}
export function createContractSmsTasks(data) {
    return request.post('/api/crm/contracts/sms/tasks', data);
}
export function getContractSmsTasks(contractId) {
    return request.get('/api/crm/contracts/sms/tasks', { params: { contractId } });
}
export function sendContractSmsTask(taskId) {
    return request.post(`/api/crm/contracts/sms/tasks/${taskId}/send`);
}
export function uploadMarketingFile(file, bizType = 'marketing-contract') {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('bizType', bizType);
    return request.post('/api/files/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    });
}
export async function getAllLeads(pageSize = 200) {
    let pageNo = 1;
    let total = 0;
    const result = [];
    do {
        const page = await getCrmLeadPage({ pageNo, pageSize });
        result.push(...(page.list || []));
        total = page.total || 0;
        pageNo += 1;
    } while (result.length < total && pageNo <= 30);
    return result;
}
export async function getAllAdmissions(pageSize = 200) {
    let pageNo = 1;
    let total = 0;
    const result = [];
    do {
        const page = await getAdmissionRecords({ pageNo, pageSize });
        result.push(...(page.list || []));
        total = page.total || 0;
        pageNo += 1;
    } while (result.length < total && pageNo <= 30);
    return result;
}
export function getMarketingConversionReport(params, config) {
    return request.get('/api/marketing/report/conversion', { params, ...(config || {}) });
}
export function getMarketingConsultationTrend(days = 14, params) {
    return request.get('/api/marketing/report/consultation', {
        params: { days, ...(params || {}) }
    });
}
export function getMarketingChannelReport(params) {
    return request.get('/api/marketing/report/channel', { params });
}
export function getMarketingFollowupReport(params) {
    return request.get('/api/marketing/report/followup', { params });
}
export function getMarketingCallbackReport(params) {
    return request.get('/api/marketing/report/callback', { params });
}
export function getMarketingDataQualityReport() {
    return request.get('/api/marketing/report/data-quality');
}
export function getMarketingLeadEntrySummary(params) {
    return request.get('/api/marketing/report/lead-entry-summary', { params });
}
export function normalizeMarketingSources() {
    return request.post('/api/marketing/report/data-quality/normalize-source');
}
export function getContractLinkageByElder(elderId) {
    return request.get('/api/crm/contracts/linkage', { params: { elderId } });
}
export function getContractLinkageByLead(leadId) {
    return request.get(`/api/crm/contracts/${leadId}/linkage`);
}
export function getContractLinkageByContract(contractId) {
    return request.get(`/api/crm/contracts/${contractId}/linkage-by-contract`);
}
export function getContractArchiveRule() {
    return request.get('/api/crm/contracts/linkage-archive-rule');
}
export function getContractAssessmentOverview(params) {
    return request.get('/api/crm/contracts/assessment-overview', { params });
}
export function getMarketingPlanPage(params) {
    return fetchPage('/api/marketing/plans/page', params);
}
export function getMarketingPlanList(moduleType) {
    return request.get('/api/marketing/plans', { params: { moduleType } });
}
export function createMarketingPlan(data) {
    return request.post('/api/marketing/plans', data);
}
export function updateMarketingPlan(id, data) {
    return request.put(`/api/marketing/plans/${id}`, data);
}
export function deleteMarketingPlan(id) {
    return request.delete(`/api/marketing/plans/${id}`);
}
export function getMarketingPlanDetail(id) {
    return request.get(`/api/marketing/plans/${id}`);
}
export function submitMarketingPlan(id) {
    return request.post(`/api/marketing/plans/${id}/submit`);
}
export function approveMarketingPlan(id, data) {
    return request.post(`/api/marketing/plans/${id}/approve`, data || {});
}
export function rejectMarketingPlan(id, data) {
    return request.post(`/api/marketing/plans/${id}/reject`, data || {});
}
export function publishMarketingPlan(id) {
    return request.post(`/api/marketing/plans/${id}/publish`);
}
export function deactivateMarketingPlan(id) {
    return request.post(`/api/marketing/plans/${id}/deactivate`);
}
export function confirmMarketingPlanRead(id, data) {
    return request.post(`/api/marketing/plans/${id}/receipt`, data);
}
export function getMarketingPlanReceipts(id) {
    return request.get(`/api/marketing/plans/${id}/receipts`);
}
export function getMarketingPlanWorkflowSummary(id) {
    return request.get(`/api/marketing/plans/${id}/workflow-summary`);
}
