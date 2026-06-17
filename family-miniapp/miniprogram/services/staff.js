const { request, uploadFile } = require('../utils/request');
const mock = require('../mocks/staff-app');

function canUseMockFallback() {
  const app = getApp();
  return !!(app
    && app.globalData
    && app.globalData.useMockFallback
    && typeof app.isLocalDevEnvironment === 'function'
    && app.isLocalDevEnvironment());
}

async function withFallback(realCall, fallbackCall) {
  try {
    return await realCall();
  } catch (error) {
    if (!canUseMockFallback()) throw error;
    return fallbackCall();
  }
}

async function staffLogin(payload) {
  return request({
    url: '/api/auth/login',
    method: 'POST',
    data: payload,
    auth: false
  });
}

async function getStaffProfile() {
  return withFallback(
    async () => {
      const me = await request({ url: '/api/auth/me' });
      const user = me && (me.staffInfo || me.user || me);
      return {
        ...(user || {}),
        roles: (me && me.roles) || (user && user.roles) || []
      };
    },
    () => mock.getStaffProfile()
  );
}

async function getStaffDashboard() {
  return withFallback(
    async () => {
      const mobile = await request({ url: '/api/operations/staff-mobile' });
      const profile = await getStaffProfile();
      const tasks = await getTaskList();
      return {
        generatedAt: mobile.generatedAt,
        staffName: profile.realName || profile.username || '员工',
        shiftName: profile.shiftName || '今日班次',
        roleText: (profile.roles || []).join(' / ') || '员工',
        mobileIndex: mobile.mobileIndex || 0,
        mobileLevel: mobile.mobileLevel || 'LOW',
        notices: (mobile.actions || []).slice(0, 3).map((item) => item.title),
        taskGroups: (mobile.metrics || []).map((item) => ({
          key: item.key,
          title: item.label,
          count: Number(String(item.value || '0').split('/')[0]) || 0,
          tone: item.tone || 'normal',
          route: routeForMetric(item.key)
        })),
        quickEntries: mock.getStaffDashboard().quickEntries,
        tasks: tasks.slice(0, 4).map(normalizeStaffTask)
      };
    },
    () => mock.getStaffDashboard()
  );
}

async function getTaskList(filter = '') {
  return withFallback(
    async () => request({
      url: '/api/operations/staff-mobile/tasks',
      data: filter ? { module: filter } : {}
    }),
    () => mock.getTaskList(filter)
  );
}

async function getTaskDetail(id) {
  return withFallback(
    async () => request({ url: `/api/operations/staff-mobile/tasks/${encodeURIComponent(id)}` }),
    () => mock.getTaskDetail(id)
  );
}

async function completeTask(id, payload = {}) {
  return withFallback(
    async () => request({
      url: `/api/operations/staff-mobile/tasks/${encodeURIComponent(id)}/complete`,
      method: 'POST',
      data: payload
    }),
    () => mock.completeTask(id, payload)
  );
}

async function getMealDeliveryTasks() {
  return getTaskList('MEAL');
}

async function submitMealDeliveryReceipt(id, payload = {}) {
  const checkedMap = {
    核对餐别和姓名: !!payload.mealChecked,
    核对禁忌标签: !!payload.tabooChecked,
    送达签收: !!payload.signed
  };
  const remarkLines = [
    payload.remark,
    `餐量反馈：${payload.intake || '未记录'}`,
    `签收人：${payload.signerName || '未记录'}`,
    payload.exceptionText ? `异常说明：${payload.exceptionText}` : ''
  ].filter(Boolean);
  return completeTask(id, {
    remark: remarkLines.join('；'),
    checkedMap,
    photos: payload.photos || [],
    scanText: payload.scanText || ''
  });
}

async function getRepairTasks() {
  return getTaskList('LOGISTICS');
}

async function submitRepairReceipt(id, payload = {}) {
  const checkedMap = {
    到场确认位置: !!payload.arrived,
    完成维修处理: !!payload.repaired,
    现场验收通过: !!payload.accepted
  };
  const remarkLines = [
    payload.remark,
    `故障类型：${payload.faultType || '未记录'}`,
    `处理结果：${payload.resultType || '未记录'}`,
    `使用物料：${payload.materials || '无'}`,
    `耗时：${payload.duration || '未记录'}`,
    payload.followUp ? `后续跟进：${payload.followUp}` : ''
  ].filter(Boolean);
  return completeTask(id, {
    remark: remarkLines.join('；'),
    checkedMap,
    photos: payload.photos || [],
    scanText: payload.scanText || ''
  });
}

async function getMedicationTasks() {
  return getTaskList('MEDICATION');
}

async function submitMedicationReceipt(id, payload = {}) {
  const checkedMap = {
    核对老人身份: !!payload.identityChecked,
    核对药品剂量: !!payload.drugChecked,
    确认给药完成: !!payload.administered
  };
  const remarkLines = [
    payload.remark,
    `给药结果：${payload.medicationResult || '未记录'}`,
    `给药方式：${payload.medicationMethod || '未记录'}`,
    payload.adverseText ? `不良反应/异常：${payload.adverseText}` : ''
  ].filter(Boolean);
  return completeTask(id, {
    remark: remarkLines.join('；'),
    checkedMap,
    photos: payload.photos || [],
    scanText: payload.scanText || ''
  });
}

async function getInspectionTasks() {
  return getTaskList('INSPECTION');
}

async function submitInspectionReceipt(id, payload = {}) {
  const checkedMap = {
    到房查验: !!payload.visited,
    复测生命体征: !!payload.vitalsChecked,
    同步医生或主管: !!payload.synced
  };
  const remarkLines = [
    payload.remark,
    `巡检结论：${payload.inspectionResult || '未记录'}`,
    `风险等级：${payload.riskLevel || '未记录'}`,
    payload.vitalsSummary ? `体征摘要：${payload.vitalsSummary}` : '',
    payload.actionTaken ? `处理措施：${payload.actionTaken}` : ''
  ].filter(Boolean);
  return completeTask(id, {
    remark: remarkLines.join('；'),
    checkedMap,
    photos: payload.photos || [],
    scanText: payload.scanText || ''
  });
}

async function getCareExecutionTasks() {
  return getTaskList('CARE');
}

async function submitCareExecutionReceipt(id, payload = {}) {
  const checkedMap = {
    核对床号和姓名: !!payload.identityChecked,
    完成护理项目: !!payload.careDone,
    完成风险观察: !!payload.riskChecked
  };
  const remarkLines = [
    payload.remark,
    `护理项目：${payload.careItem || '未记录'}`,
    `皮肤状态：${payload.skinStatus || '未记录'}`,
    `精神状态：${payload.mentalStatus || '未记录'}`,
    `风险观察：${payload.riskObservation || '未记录'}`,
    payload.materials ? `耗材使用：${payload.materials}` : '',
    payload.handoverNote ? `交接提示：${payload.handoverNote}` : ''
  ].filter(Boolean);
  return completeTask(id, {
    remark: remarkLines.join('；'),
    checkedMap,
    photos: payload.photos || [],
    scanText: payload.scanText || ''
  });
}

async function getTaskReceipts(filter = '') {
  return withFallback(
    async () => request({
      url: '/api/operations/staff-mobile/receipts',
      data: filter ? { module: filter } : {}
    }),
    () => mock.getTaskReceipts(filter)
  );
}

async function uploadStaffTaskEvidence(filePath, options = {}) {
  return withFallback(
    async () => uploadFile({
      url: '/api/files/upload',
      filePath,
      formData: {
        bizType: options.bizType || 'staff-task-evidence'
      }
    }),
    () => ({
      fileName: filePath,
      originalFileName: filePath,
      fileUrl: filePath,
      fileType: String(options.bizType || '').indexOf('voice') >= 0 ? 'audio/mp3' : 'image'
    })
  );
}

async function getSchedule() {
  return withFallback(
    async () => request({ url: '/api/operations/staff-mobile/schedule' }),
    () => mock.getSchedule()
  );
}

async function getAttendanceOverview(month = '') {
  return withFallback(
    async () => request({
      url: '/api/attendance/overview',
      data: month ? { month } : {}
    }),
    () => mock.getAttendanceOverview(month)
  );
}

async function punchAttendance(action) {
  return withFallback(
    async () => request({
      url: `/api/attendance/punch?action=${encodeURIComponent(action)}`,
      method: 'POST'
    }),
    () => mock.punchAttendance(action)
  );
}

async function getStaffApprovals(filter = {}) {
  return withFallback(
    async () => {
      const page = await request({
        url: '/api/oa/approval/page',
        data: {
          pageNo: filter.pageNo || 1,
          pageSize: filter.pageSize || 20,
          type: filter.type || '',
          status: filter.status || ''
        }
      });
      return page && Array.isArray(page.records) ? page.records : [];
    },
    () => mock.getStaffApprovals(filter)
  );
}

async function submitStaffApproval(payload = {}) {
  return withFallback(
    async () => request({
      url: '/api/oa/approval',
      method: 'POST',
      data: payload
    }),
    () => mock.submitStaffApproval(payload)
  );
}

async function getStaffTodoSummary(filter = {}) {
  return withFallback(
    async () => request({
      url: '/api/oa/todo/summary',
      data: {
        mineOnly: true,
        status: filter.status || '',
        sourceType: filter.sourceType || '',
        keyword: filter.keyword || ''
      }
    }),
    () => mock.getStaffTodoSummary(filter)
  );
}

async function getStaffTodos(filter = {}) {
  return withFallback(
    async () => {
      const page = await request({
        url: '/api/oa/todo/page',
        data: {
          pageNo: filter.pageNo || 1,
          pageSize: filter.pageSize || 30,
          mineOnly: true,
          status: filter.status || '',
          sourceType: filter.sourceType || '',
          keyword: filter.keyword || ''
        }
      });
      return page && Array.isArray(page.records) ? page.records : [];
    },
    () => mock.getStaffTodos(filter)
  );
}

async function completeStaffTodo(id) {
  return withFallback(
    async () => request({
      url: `/api/oa/todo/${encodeURIComponent(id)}/done`,
      method: 'PUT'
    }),
    () => mock.completeStaffTodo(id)
  );
}

async function getStaffContacts(filter = {}) {
  return withFallback(
    async () => {
      const page = await request({
        url: '/api/admin/staff/options',
        data: {
          page: filter.page || 1,
          size: filter.size || 100,
          keyword: filter.keyword || '',
          activeOnly: true
        }
      });
      return page && Array.isArray(page.records) ? page.records : [];
    },
    () => mock.getStaffContacts(filter)
  );
}

async function getStaffReportSummary(filter = {}) {
  return withFallback(
    async () => request({
      url: '/api/oa/report/summary',
      data: {
        mineOnly: true,
        reportType: filter.reportType || 'DAY',
        status: filter.status || '',
        keyword: filter.keyword || ''
      }
    }),
    () => mock.getStaffReportSummary(filter)
  );
}

async function getStaffReports(filter = {}) {
  return withFallback(
    async () => {
      const page = await request({
        url: '/api/oa/report/page',
        data: {
          pageNo: filter.pageNo || 1,
          pageSize: filter.pageSize || 20,
          mineOnly: true,
          reportType: filter.reportType || 'DAY',
          status: filter.status || '',
          keyword: filter.keyword || ''
        }
      });
      return page && Array.isArray(page.records) ? page.records : [];
    },
    () => mock.getStaffReports(filter)
  );
}

async function submitStaffReport(payload = {}) {
  return withFallback(
    async () => request({
      url: '/api/oa/report',
      method: 'POST',
      data: payload
    }),
    () => mock.submitStaffReport(payload)
  );
}

async function submitStaffReportById(id) {
  return withFallback(
    async () => request({
      url: `/api/oa/report/${encodeURIComponent(id)}/submit`,
      method: 'PUT'
    }),
    () => mock.submitStaffReportById(id)
  );
}

async function getStaffNotices(filter = {}) {
  return withFallback(
    async () => {
      const page = await request({
        url: '/api/oa/notice/page',
        data: {
          pageNo: filter.pageNo || 1,
          pageSize: filter.pageSize || 30,
          keyword: filter.keyword || '',
          status: 'PUBLISHED'
        }
      });
      return page && Array.isArray(page.records) ? page.records : [];
    },
    () => mock.getStaffNotices(filter)
  );
}

async function getStaffNoticeDetail(id) {
  return withFallback(
    async () => request({ url: `/api/oa/notice/${encodeURIComponent(id)}` }),
    () => mock.getStaffNoticeDetail(id)
  );
}

async function getStaffSuggestions(filter = {}) {
  return withFallback(
    async () => {
      const page = await request({
        url: '/api/oa/suggestion/page',
        data: {
          pageNo: filter.pageNo || 1,
          pageSize: filter.pageSize || 20,
          status: filter.status || '',
          keyword: filter.keyword || ''
        }
      });
      return page && Array.isArray(page.records) ? page.records : [];
    },
    () => mock.getStaffSuggestions(filter)
  );
}

async function submitStaffSuggestion(payload = {}) {
  return withFallback(
    async () => request({
      url: '/api/oa/suggestion',
      method: 'POST',
      data: payload
    }),
    () => mock.submitStaffSuggestion(payload)
  );
}

async function getStaffVitalRecords(filter = {}) {
  return withFallback(
    async () => {
      const page = await request({
        url: '/api/health/data/page',
        data: {
          pageNo: filter.pageNo || 1,
          pageSize: filter.pageSize || 20,
          elderId: filter.elderId || '',
          dataType: filter.dataType || '',
          abnormalFlag: typeof filter.abnormalFlag === 'number' ? filter.abnormalFlag : '',
          keyword: filter.keyword || ''
        }
      });
      return page && Array.isArray(page.records) ? page.records : [];
    },
    () => mock.getStaffVitalRecords(filter)
  );
}

async function submitStaffVitalRecord(payload = {}) {
  return withFallback(
    async () => request({
      url: '/api/health/data',
      method: 'POST',
      data: {
        ...payload,
        source: payload.source || 'staff-mobile'
      }
    }),
    () => mock.submitStaffVitalRecord(payload)
  );
}

async function getHandovers() {
  return withFallback(
    async () => request({ url: '/api/operations/staff-mobile/handovers' }),
    () => mock.getHandovers()
  );
}

async function submitHandover(payload = {}) {
  return withFallback(
    async () => request({
      url: '/api/operations/staff-mobile/handovers',
      method: 'POST',
      data: payload
    }),
    () => mock.submitHandover(payload)
  );
}

async function getPatrolPoints() {
  return withFallback(
    async () => request({ url: '/api/operations/staff-mobile/patrol-points' }),
    () => mock.getPatrolPoints()
  );
}

async function submitIncident(payload = {}) {
  return withFallback(
    async () => request({
      url: '/api/operations/staff-mobile/incidents',
      method: 'POST',
      data: payload
    }),
    () => mock.submitIncident(payload)
  );
}

async function getIncidents(filter = {}) {
  return withFallback(
    async () => request({
      url: '/api/operations/staff-mobile/incidents',
      data: filter || {}
    }),
    () => mock.getIncidents(filter)
  );
}

async function submitPatrolScan(payload = {}) {
  return withFallback(
    async () => request({
      url: '/api/operations/staff-mobile/patrol-scan',
      method: 'POST',
      data: payload
    }),
    () => ({ id: payload.pointId || payload.scanText || `patrol-${Date.now()}`, status: 'DONE' })
  );
}

function routeForMetric(key) {
  if (key === 'care') return '/pages/staff-care/index';
  if (key === 'medication' || key === 'inspection') return '/pages/staff-medical/index';
  if (key === 'maintenance' || key === 'meal' || key === 'cleaning') return '/pages/staff-logistics/index';
  return '/pages/staff-tasks/index';
}

function routeForTask(task = {}) {
  const idParam = task.id ? `&id=${encodeURIComponent(task.id)}` : '';
  if (task.module === 'CARE') return `/pages/staff-care-execution/index${task.id ? `?id=${encodeURIComponent(task.id)}` : ''}`;
  if (task.module === 'MEDICATION') return `/pages/staff-clinical/index?mode=MEDICATION${idParam}`;
  if (task.module === 'INSPECTION') return `/pages/staff-clinical/index?mode=INSPECTION${idParam}`;
  if (task.module === 'LOGISTICS') return `/pages/staff-repairs/index${task.id ? `?id=${encodeURIComponent(task.id)}` : ''}`;
  if (task.module === 'MEAL') return `/pages/staff-meals/index${task.id ? `?id=${encodeURIComponent(task.id)}` : ''}`;
  return task.id ? `/pages/staff-task-detail/index?id=${encodeURIComponent(task.id)}` : '/pages/staff-tasks/index';
}

function normalizeStaffTask(task = {}) {
  return {
    ...task,
    route: task.route || routeForTask(task)
  };
}

module.exports = {
  staffLogin,
  getStaffDashboard,
  getTaskList,
  getTaskDetail,
  completeTask,
  getMealDeliveryTasks,
  submitMealDeliveryReceipt,
  getRepairTasks,
  submitRepairReceipt,
  getMedicationTasks,
  submitMedicationReceipt,
  getInspectionTasks,
  submitInspectionReceipt,
  getCareExecutionTasks,
  submitCareExecutionReceipt,
  getTaskReceipts,
  uploadStaffTaskEvidence,
  getStaffProfile,
  getSchedule,
  getAttendanceOverview,
  punchAttendance,
  getStaffApprovals,
  submitStaffApproval,
  getStaffTodoSummary,
  getStaffTodos,
  completeStaffTodo,
  getStaffContacts,
  getStaffReportSummary,
  getStaffReports,
  submitStaffReport,
  submitStaffReportById,
  getStaffNotices,
  getStaffNoticeDetail,
  getStaffSuggestions,
  submitStaffSuggestion,
  getStaffVitalRecords,
  submitStaffVitalRecord,
  getHandovers,
  submitHandover,
  getPatrolPoints,
  submitIncident,
  getIncidents,
  submitPatrolScan
};
