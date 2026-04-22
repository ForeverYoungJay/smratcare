import request, { fetchPage } from '../utils/request';
export function getPortalSummary(config) {
    return request.get('/api/oa/portal/summary', config);
}
export function getMyInfoSummary(params) {
    return request.get('/api/oa/my-info/summary', { params });
}
export function uploadOaFile(file, bizType = 'oa-approval-proof') {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('bizType', bizType);
    return request.post('/api/files/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } });
}
export function getSuggestionPage(params) {
    return fetchPage('/api/oa/suggestion/page', params);
}
export function createSuggestion(data) {
    return request.post('/api/oa/suggestion', data);
}
export function getNoticePage(params) {
    return fetchPage('/api/oa/notice/page', params);
}
export function getNotice(id) {
    return request.get(`/api/oa/notice/${id}`);
}
export function createNotice(data) {
    return request.post('/api/oa/notice', data);
}
export function updateNotice(id, data) {
    return request.put(`/api/oa/notice/${id}`, data);
}
export function publishNotice(id) {
    return request.put(`/api/oa/notice/${id}/publish`);
}
export function batchPublishNotice(ids) {
    return request.put('/api/oa/notice/batch/publish', { ids });
}
export function deleteNotice(id) {
    return request.delete(`/api/oa/notice/${id}`);
}
export function batchDeleteNotice(ids) {
    return request.delete('/api/oa/notice/batch', { data: { ids } });
}
export function exportNotice(params) {
    return request.get('/api/oa/notice/export', { params, responseType: 'blob' });
}
export function getTodoPage(params) {
    return fetchPage('/api/oa/todo/page', params);
}
export function getTodoSummary(params) {
    return request.get('/api/oa/todo/summary', { params });
}
export function createTodo(data) {
    return request.post('/api/oa/todo', data);
}
export function updateTodo(id, data) {
    return request.put(`/api/oa/todo/${id}`, data);
}
export function completeTodo(id) {
    return request.put(`/api/oa/todo/${id}/done`);
}
export function snoozeTodo(id, days = 1) {
    return request.put(`/api/oa/todo/${id}/snooze`, null, { params: { days } });
}
export function ignoreBirthdayTodo(id) {
    return request.put(`/api/oa/todo/${id}/ignore-birthday`);
}
export function batchSnoozeBirthdayTodo(ids, days = 1) {
    return request.put('/api/oa/todo/batch/snooze', { ids }, { params: { days } });
}
export function batchIgnoreBirthdayTodo(ids) {
    return request.put('/api/oa/todo/batch/ignore-birthday', { ids });
}
export function batchCompleteTodo(ids) {
    return request.put('/api/oa/todo/batch/done', { ids });
}
export function deleteTodo(id) {
    return request.delete(`/api/oa/todo/${id}`);
}
export function batchDeleteTodo(ids) {
    return request.delete('/api/oa/todo/batch', { data: { ids } });
}
export function exportTodo(params) {
    return request.get('/api/oa/todo/export', { params, responseType: 'blob' });
}
export function getApprovalPage(params) {
    return fetchPage('/api/oa/approval/page', params);
}
export function getApprovalSummary(params) {
    return request.get('/api/oa/approval/summary', { params });
}
export function createApproval(data) {
    return request.post('/api/oa/approval', data);
}
export function updateApproval(id, data) {
    return request.put(`/api/oa/approval/${id}`, data);
}
export function approveApproval(id, remark) {
    return request.put(`/api/oa/approval/${id}/approve`, null, { params: { remark } });
}
export function batchApproveApproval(ids) {
    return request.put('/api/oa/approval/batch/approve', { ids });
}
export function rejectApproval(id, remark) {
    return request.put(`/api/oa/approval/${id}/reject`, null, { params: { remark } });
}
export function batchRejectApproval(ids, remark) {
    return request.put('/api/oa/approval/batch/reject', { ids, remark });
}
export function deleteApproval(id) {
    return request.delete(`/api/oa/approval/${id}`);
}
export function batchDeleteApproval(ids) {
    return request.delete('/api/oa/approval/batch', { data: { ids } });
}
export function exportApproval(params) {
    return request.get('/api/oa/approval/export', { params, responseType: 'blob' });
}
export function getDocumentPage(params) {
    return fetchPage('/api/oa/document/page', normalizeDocumentParams(params));
}
export function createDocument(data) {
    return request.post('/api/oa/document', normalizeDocumentPayload(data));
}
export function getDocumentFolderTree() {
    return request.get('/api/oa/document/folder/tree');
}
export function createDocumentFolder(data) {
    return request.post('/api/oa/document/folder', data);
}
export function updateDocumentFolder(id, data) {
    return request.put(`/api/oa/document/folder/${id}`, data);
}
export function deleteDocumentFolder(id) {
    return request.delete(`/api/oa/document/folder/${id}`);
}
export function updateDocument(id, data) {
    return request.put(`/api/oa/document/${id}`, normalizeDocumentPayload(data));
}
export function deleteDocument(id) {
    return request.delete(`/api/oa/document/${id}`);
}
export function batchDeleteDocument(ids) {
    return request.delete('/api/oa/document/batch', { data: { ids } });
}
export function exportDocument(params) {
    return request.get('/api/oa/document/export', { params: normalizeDocumentParams(params), responseType: 'blob' });
}
function normalizeDocumentPayload(data) {
    const folderId = normalizePositiveId(data.folderId);
    const folder = typeof data.folder === 'string' ? data.folder.trim() : data.folder;
    return {
        ...data,
        folderId,
        folder: folderId ? folder : (folder || undefined)
    };
}
function normalizeDocumentParams(params) {
    if (!params || typeof params !== 'object')
        return params;
    return {
        ...params,
        folderId: normalizePositiveId(params.folderId)
    };
}
function normalizePositiveId(value) {
    if (value == null || value === '')
        return undefined;
    const parsed = Number(value);
    if (!Number.isFinite(parsed) || parsed <= 0)
        return undefined;
    return parsed;
}
export function getAlbumPage(params) {
    return fetchPage('/api/oa/album/page', params);
}
export function getAlbumFolders() {
    return request.get('/api/oa/album/folders');
}
export function createAlbum(data) {
    return request.post('/api/oa/album', data);
}
export function updateAlbum(id, data) {
    return request.put(`/api/oa/album/${id}`, data);
}
export function deleteAlbum(id) {
    return request.delete(`/api/oa/album/${id}`);
}
export function getKnowledgePage(params) {
    return fetchPage('/api/oa/knowledge/page', params);
}
export function createKnowledge(data) {
    return request.post('/api/oa/knowledge', data);
}
export function updateKnowledge(id, data) {
    return request.put(`/api/oa/knowledge/${id}`, data);
}
export function publishKnowledge(id) {
    return request.put(`/api/oa/knowledge/${id}/publish`);
}
export function archiveKnowledge(id) {
    return request.put(`/api/oa/knowledge/${id}/archive`);
}
export function expireKnowledge(id) {
    return request.put(`/api/oa/knowledge/${id}/expire`);
}
export function batchPublishKnowledge(ids) {
    return request.put('/api/oa/knowledge/batch/publish', { ids });
}
export function batchArchiveKnowledge(ids) {
    return request.put('/api/oa/knowledge/batch/archive', { ids });
}
export function batchExpireKnowledge(ids) {
    return request.put('/api/oa/knowledge/batch/expire', { ids });
}
export function deleteKnowledge(id) {
    return request.delete(`/api/oa/knowledge/${id}`);
}
export function batchDeleteKnowledge(ids) {
    return request.delete('/api/oa/knowledge/batch', { data: { ids } });
}
export function exportKnowledge(params) {
    return request.get('/api/oa/knowledge/export', { params, responseType: 'blob' });
}
export function getGroupSettingPage(params) {
    return fetchPage('/api/oa/group-setting/page', params);
}
export function createGroupSetting(data) {
    return request.post('/api/oa/group-setting', data);
}
export function updateGroupSetting(id, data) {
    return request.put(`/api/oa/group-setting/${id}`, data);
}
export function enableGroupSetting(id) {
    return request.put(`/api/oa/group-setting/${id}/enable`);
}
export function disableGroupSetting(id) {
    return request.put(`/api/oa/group-setting/${id}/disable`);
}
export function batchEnableGroupSetting(ids) {
    return request.put('/api/oa/group-setting/batch/enable', { ids });
}
export function batchDisableGroupSetting(ids) {
    return request.put('/api/oa/group-setting/batch/disable', { ids });
}
export function deleteGroupSetting(id) {
    return request.delete(`/api/oa/group-setting/${id}`);
}
export function batchDeleteGroupSetting(ids) {
    return request.delete('/api/oa/group-setting/batch', { data: { ids } });
}
export function exportGroupSetting(params) {
    return request.get('/api/oa/group-setting/export', { params, responseType: 'blob' });
}
export function getActivityPlanPage(params) {
    return fetchPage('/api/oa/activity-plan/page', params);
}
export function getActivityPlan(id) {
    return request.get(`/api/oa/activity-plan/${id}`);
}
export function createActivityPlan(data) {
    return request.post('/api/oa/activity-plan', data);
}
export function updateActivityPlan(id, data) {
    return request.put(`/api/oa/activity-plan/${id}`, data);
}
export function submitActivityPlanScheme(id) {
    return request.put(`/api/oa/activity-plan/${id}/submit-scheme`);
}
export function approveActivityPlan(id, opinion) {
    return request.put(`/api/oa/activity-plan/${id}/approve`, { opinion });
}
export function rejectActivityPlan(id, opinion) {
    return request.put(`/api/oa/activity-plan/${id}/reject`, { opinion });
}
export function startActivityPlan(id) {
    return request.put(`/api/oa/activity-plan/${id}/start`);
}
export function completeActivityPlan(id) {
    return request.put(`/api/oa/activity-plan/${id}/done`);
}
export function saveActivityPlanExecution(id, data) {
    return request.put(`/api/oa/activity-plan/${id}/execution`, data);
}
export function saveActivityPlanReview(id, data) {
    return request.put(`/api/oa/activity-plan/${id}/review`, data);
}
export function cancelActivityPlan(id) {
    return request.put(`/api/oa/activity-plan/${id}/cancel`);
}
export function batchStartActivityPlan(ids) {
    return request.put('/api/oa/activity-plan/batch/start', { ids });
}
export function batchCompleteActivityPlan(ids) {
    return request.put('/api/oa/activity-plan/batch/done', { ids });
}
export function batchCancelActivityPlan(ids) {
    return request.put('/api/oa/activity-plan/batch/cancel', { ids });
}
export function deleteActivityPlan(id) {
    return request.delete(`/api/oa/activity-plan/${id}`);
}
export function batchDeleteActivityPlan(ids) {
    return request.delete('/api/oa/activity-plan/batch', { data: { ids } });
}
export function exportActivityPlan(params) {
    return request.get('/api/oa/activity-plan/export', { params, responseType: 'blob' });
}
export function getOaTaskPage(params) {
    return fetchPage('/api/oa/task/page', params);
}
export function getOaTaskSummary(params) {
    return request.get('/api/oa/task/summary', { params });
}
export function getQuickChatState() {
    return request.get('/api/oa/quick-chat/state');
}
export function saveQuickChatState(stateJson) {
    return request.put('/api/oa/quick-chat/state', { stateJson });
}
export function fanoutQuickChatState(stateJson, targetStaffIds) {
    return request.post('/api/oa/quick-chat/state/fanout', { stateJson, targetStaffIds });
}
export function publishQuickChatEventBatch(events) {
    return request.post('/api/oa/quick-chat/state/event/batch', { events });
}
export function getOaTaskCalendar(params) {
    return request.get('/api/oa/task/calendar', { params });
}
export function checkOaTaskConflicts(data) {
    return request.post('/api/oa/task/conflicts/check', data);
}
export function createOaTask(data) {
    return request.post('/api/oa/task', data);
}
export function updateOaTask(id, data) {
    return request.put(`/api/oa/task/${id}`, data);
}
export function completeOaTask(id) {
    return request.put(`/api/oa/task/${id}/done`);
}
export function batchCompleteOaTask(ids) {
    return request.put('/api/oa/task/batch/done', { ids });
}
export function deleteOaTask(id) {
    return request.delete(`/api/oa/task/${id}`);
}
export function batchDeleteOaTask(ids) {
    return request.delete('/api/oa/task/batch', { data: { ids } });
}
export function exportOaTask(params) {
    return request.get('/api/oa/task/export', { params, responseType: 'blob' });
}
export function getOaWorkReportPage(params) {
    return fetchPage('/api/oa/report/page', params);
}
export function getOaWorkReportSummary(params) {
    return request.get('/api/oa/report/summary', { params });
}
export function getDailyWorkReportPage(params) {
    return fetchPage('/api/oa/report/daily/page', params);
}
export function getWeeklyWorkReportPage(params) {
    return fetchPage('/api/oa/report/weekly/page', params);
}
export function getMonthlyWorkReportPage(params) {
    return fetchPage('/api/oa/report/monthly/page', params);
}
export function getYearlyWorkReportPage(params) {
    return fetchPage('/api/oa/report/yearly/page', params);
}
export function createOaWorkReport(data) {
    return request.post('/api/oa/report', data);
}
export function updateOaWorkReport(id, data) {
    return request.put(`/api/oa/report/${id}`, data);
}
export function submitOaWorkReport(id) {
    return request.put(`/api/oa/report/${id}/submit`);
}
export function deleteOaWorkReport(id) {
    return request.delete(`/api/oa/report/${id}`);
}
