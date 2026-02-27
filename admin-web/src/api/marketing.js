import { getCrmLeadPage, createCrmLead, updateCrmLead, deleteCrmLead } from './crm';
import { getAdmissionRecords } from './elderLifecycle';
import request, { fetchPage } from '../utils/request';
export { createCrmLead, updateCrmLead, deleteCrmLead };
export function getLeadPage(params) {
    return getCrmLeadPage(params);
}
export function batchUpdateLeadStatus(data) {
    return request.post('/api/crm/leads/batch/status', data);
}
export function batchDeleteLeads(data) {
    return request.post('/api/crm/leads/batch/delete', data);
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
export function getMarketingConversionReport(params) {
    return request.get('/api/marketing/report/conversion', { params });
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
