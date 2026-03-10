const { request, uploadFile } = require('../utils/request');
const mock = require('../mocks/family-app');

async function withFallback(realCall, fallbackCall) {
  try {
    return await realCall();
  } catch (error) {
    return fallbackCall();
  }
}

function resolveCurrentElderId(inputElderId) {
  if (inputElderId) {
    return inputElderId;
  }
  const app = getApp();
  return app.globalData.selectedElderId || null;
}

function currentMonth() {
  const now = new Date();
  const month = now.getMonth() + 1;
  return `${now.getFullYear()}-${month < 10 ? `0${month}` : month}`;
}

async function familyLogin(payload) {
  return request({
    url: '/api/auth/family/login',
    method: 'POST',
    data: payload,
    auth: false
  });
}

async function sendFamilySmsCode(payload) {
  return request({
    url: '/api/auth/family/sms-code/send',
    method: 'POST',
    data: payload,
    auth: false
  });
}

async function verifyFamilySmsCode(payload) {
  return request({
    url: '/api/auth/family/sms-code/verify',
    method: 'POST',
    data: payload,
    auth: false
  });
}

async function registerFamily(payload) {
  return request({
    url: '/api/auth/family/register',
    method: 'POST',
    data: payload,
    auth: false
  });
}

async function resetFamilyPassword(payload) {
  return request({
    url: '/api/auth/family/password/reset',
    method: 'POST',
    data: payload,
    auth: false
  });
}

async function getFamilyAuthBootstrap() {
  return request({
    url: '/api/auth/family/bootstrap',
    auth: false
  });
}

async function bindElder(payload) {
  return withFallback(
    async () => request({
      url: '/api/family/bindings',
      method: 'POST',
      data: payload
    }),
    () => mock.bindFamilyElder(payload)
  );
}

async function getMyElders() {
  return withFallback(
    async () => request({ url: '/api/family/bindings' }),
    () => mock.getFamilyBindings()
  );
}

async function getHomeDashboard() {
  const elderId = resolveCurrentElderId();
  return withFallback(
    async () => request({ url: '/api/family/dashboard/home', data: { elderId } }),
    async () => {
      const fallback = mock.getHomeDashboard();
      const elders = await getMyElders();
      if (!elders || elders.length === 0) {
        return fallback;
      }
      return {
        ...fallback,
        elders: elders.map((item, idx) => {
          const base = fallback.elders[idx] || fallback.elders[0];
          return {
            ...base,
            elderId: item.elderId,
            elderName: item.elderName,
            relation: item.relation
          };
        })
      };
    }
  );
}

async function getWeeklyBrief(options = {}) {
  const elderId = resolveCurrentElderId(options.elderId);
  return withFallback(
    async () => request({ url: '/api/family/dashboard/weekly-brief', data: { elderId } }),
    () => mock.getWeeklyBrief()
  );
}

async function getWeeklyBriefHistory(options = {}) {
  const elderId = resolveCurrentElderId(options.elderId);
  const weeks = options.weeks || 8;
  return withFallback(
    async () => request({ url: '/api/family/dashboard/weekly-brief/history', data: { elderId, weeks } }),
    () => mock.getWeeklyBriefHistory(weeks)
  );
}

async function getMessages(pageNo = 1, pageSize = 20, options = {}) {
  const data = { pageNo, pageSize };
  if (options.level) {
    data.level = options.level;
  }
  if (options.type) {
    data.type = options.type;
  }
  if (options.unreadOnly !== undefined) {
    data.unreadOnly = !!options.unreadOnly;
  }
  return withFallback(
    async () => request({ url: '/api/family/messages/page', data }),
    () => mock.getMessages(options)
  );
}

async function getMessageSummary() {
  return withFallback(
    async () => request({ url: '/api/family/messages/summary' }),
    () => mock.getMessageSummary()
  );
}

async function getMessageDetail(id) {
  return withFallback(
    async () => request({ url: `/api/family/messages/${id}` }),
    () => mock.getMessageDetail(id)
  );
}

async function markMessageRead(id) {
  return withFallback(
    async () => request({ url: `/api/family/messages/${id}/read`, method: 'POST' }),
    () => {
      mock.markMessageRead(id);
      return true;
    }
  );
}

async function markAllMessagesRead() {
  return withFallback(
    async () => request({ url: '/api/family/messages/read-all', method: 'POST' }),
    () => mock.markAllMessagesRead()
  );
}

async function getHealthDashboard(options = {}) {
  const elderId = resolveCurrentElderId(options.elderId);
  const range = options.range || '7d';
  return withFallback(
    async () => request({ url: '/api/family/health/trend', data: { elderId, range } }),
    () => mock.getHealthDashboard(range)
  );
}

async function getMedicalRecords(options = {}) {
  const elderId = resolveCurrentElderId(options.elderId);
  return withFallback(
    async () => request({ url: '/api/family/medical-records', data: { elderId } }),
    () => mock.getMedicalRecords()
  );
}

async function getAssessmentReports(options = {}) {
  const elderId = resolveCurrentElderId(options.elderId);
  return withFallback(
    async () => request({ url: '/api/family/assessment-reports', data: { elderId } }),
    () => mock.getAssessmentReports()
  );
}

async function getSchedules(options = {}) {
  const elderId = resolveCurrentElderId(options.elderId);
  return withFallback(
    async () => request({ url: '/api/family/schedules/today', data: { elderId } }),
    () => mock.getSchedules()
  );
}

async function getMealCalendar() {
  return withFallback(
    async () => request({ url: '/api/family/meals/calendar' }),
    () => mock.getMealCalendar()
  );
}

async function getCareLogs(options = {}) {
  const elderId = resolveCurrentElderId(options.elderId);
  return withFallback(
    async () => request({ url: '/api/family/care-logs', data: { elderId } }),
    () => mock.getCareLogs()
  );
}

async function getActivityAlbums(pageNo = 1, pageSize = 20) {
  return withFallback(
    async () => request({ url: '/api/family/activity-albums', data: { pageNo, pageSize } }),
    () => mock.getActivityAlbums()
  );
}

async function toggleAlbumLike(id) {
  return withFallback(
    async () => request({ url: `/api/family/activity-albums/${id}/like`, method: 'POST' }),
    () => mock.likeAlbum(id)
  );
}

async function getActivityAlbumComments(albumId) {
  return withFallback(
    async () => request({ url: `/api/family/activity-albums/${albumId}/comments` }),
    () => mock.getActivityAlbumComments(albumId)
  );
}

async function addActivityAlbumComment(albumId, content) {
  return withFallback(
    async () => request({
      url: `/api/family/activity-albums/${albumId}/comments`,
      method: 'POST',
      data: { content }
    }),
    () => mock.addActivityAlbumComment(albumId, content)
  );
}

async function getOutingRecords(options = {}) {
  const elderId = resolveCurrentElderId(options.elderId);
  return withFallback(
    async () => request({ url: '/api/family/outing-records', data: { elderId } }),
    () => mock.getOutingRecords()
  );
}

async function getEmergencyContacts() {
  return withFallback(
    async () => request({ url: '/api/family/emergency-contacts' }),
    () => mock.getEmergencyContacts()
  );
}

async function getBillSummary(options = {}) {
  const elderId = resolveCurrentElderId(options.elderId);
  const month = options.month || currentMonth();
  return withFallback(
    async () => request({ url: '/api/family/payment/summary', data: { elderId, month } }),
    () => mock.getBillSummary()
  );
}

async function getPaymentGuard(options = {}) {
  const elderId = resolveCurrentElderId(options.elderId);
  return withFallback(
    async () => request({ url: '/api/family/payment/guard', data: { elderId } }),
    () => mock.getPaymentGuard()
  );
}

async function getBillHistory(options = {}) {
  const elderId = resolveCurrentElderId(options.elderId);
  return withFallback(
    async () => request({ url: '/api/family/payment/history', data: { elderId } }),
    () => mock.getBillHistory()
  );
}

async function toggleAutoPay(enable, authorizeConfirmed = false) {
  const elderId = resolveCurrentElderId();
  return withFallback(
    async () => request({
      url: '/api/family/payment/auto-pay',
      method: 'PUT',
      data: { enabled: !!enable, elderId, authorizeConfirmed: !!authorizeConfirmed }
    }),
    () => mock.toggleAutoPay(enable)
  );
}

async function rechargeBalance(amount, options = {}) {
  const elderId = resolveCurrentElderId(options.elderId);
  return withFallback(
    async () => request({
      url: '/api/family/payment/recharge',
      method: 'POST',
      data: {
        elderId,
        amount,
        method: options.method || '微信支付',
        remark: options.remark || '家属端在线充值'
      }
    }),
    () => mock.rechargeBalance(amount)
  );
}

async function createWechatRechargePrepay(amount, options = {}) {
  const elderId = resolveCurrentElderId(options.elderId);
  return withFallback(
    async () => request({
      url: '/api/family/payment/wechat/prepay',
      method: 'POST',
      data: {
        elderId,
        amount,
        description: options.description || '家属端账户充值',
        remark: options.remark || '家属端微信充值',
        payerOpenId: options.payerOpenId,
        loginCode: options.loginCode
      }
    }),
    () => mock.createWechatRechargePrepay(amount)
  );
}

async function bindWechatNotifyOpenId(payload = {}) {
  return withFallback(
    async () => request({
      url: '/api/family/wechat/notify/bind-openid',
      method: 'POST',
      data: payload
    }),
    () => mock.bindWechatNotifyOpenId(payload)
  );
}

async function getCapabilityStatus() {
  return withFallback(
    async () => request({ url: '/api/family/capabilities/status' }),
    () => mock.getCapabilityStatus()
  );
}

async function getRechargeOrder(outTradeNo) {
  return withFallback(
    async () => request({ url: `/api/family/payment/recharge-orders/${outTradeNo}` }),
    () => mock.getRechargeOrder(outTradeNo)
  );
}

async function getRechargeOrders(options = {}) {
  const elderId = resolveCurrentElderId(options.elderId);
  const pageNo = options.pageNo || 1;
  const pageSize = options.pageSize || 20;
  return withFallback(
    async () => request({ url: '/api/family/payment/recharge-orders', data: { elderId, pageNo, pageSize } }),
    () => mock.getRechargeOrders()
  );
}

async function getServiceCatalog() {
  return withFallback(
    async () => request({ url: '/api/family/service/catalog' }),
    () => mock.getServiceCatalog()
  );
}

async function getServiceOrders(options = {}) {
  const elderId = resolveCurrentElderId(options.elderId);
  return withFallback(
    async () => request({ url: '/api/family/service/orders', data: { elderId } }),
    () => mock.getServiceOrders()
  );
}

async function createServiceOrder(serviceId, elderId, remark = '') {
  const targetElderId = resolveCurrentElderId(elderId);
  return withFallback(
    async () => request({
      url: '/api/family/service/orders',
      method: 'POST',
      data: {
        serviceId,
        elderId: targetElderId,
        remark
      }
    }),
    () => mock.createServiceOrder(serviceId)
  );
}

async function getMallProducts(options = {}) {
  const data = {
    keyword: options.keyword,
    category: options.category,
    pageNo: options.pageNo || 1,
    pageSize: options.pageSize || 20
  };
  return withFallback(
    async () => request({ url: '/api/family/mall/products', data }),
    () => mock.getMallProducts(options)
  );
}

async function previewMallOrder(payload = {}) {
  const targetElderId = resolveCurrentElderId(payload.elderId);
  return withFallback(
    async () => request({
      url: '/api/family/mall/orders/preview',
      method: 'POST',
      data: {
        productId: payload.productId,
        qty: payload.qty || 1,
        elderId: targetElderId
      }
    }),
    () => mock.previewMallOrder({ ...payload, elderId: targetElderId })
  );
}

async function submitMallOrder(payload = {}) {
  const targetElderId = resolveCurrentElderId(payload.elderId);
  return withFallback(
    async () => request({
      url: '/api/family/mall/orders/submit',
      method: 'POST',
      data: {
        productId: payload.productId,
        qty: payload.qty || 1,
        elderId: targetElderId
      }
    }),
    () => mock.submitMallOrder({ ...payload, elderId: targetElderId })
  );
}

async function getMallOrders(options = {}) {
  const targetElderId = resolveCurrentElderId(options.elderId);
  return withFallback(
    async () => request({
      url: '/api/family/mall/orders',
      data: {
        elderId: targetElderId,
        pageNo: options.pageNo || 1,
        pageSize: options.pageSize || 20
      }
    }),
    () => mock.getMallOrders({ ...options, elderId: targetElderId })
  );
}

async function getMallOrderDetail(orderId) {
  return withFallback(
    async () => request({ url: `/api/family/mall/orders/${orderId}` }),
    () => mock.getMallOrderDetail(orderId)
  );
}

async function cancelMallOrder(orderId, payload = {}) {
  return withFallback(
    async () => request({
      url: `/api/family/mall/orders/${orderId}/cancel`,
      method: 'POST',
      data: payload
    }),
    () => mock.cancelMallOrder(orderId, payload)
  );
}

async function refundMallOrder(orderId, payload = {}) {
  return withFallback(
    async () => request({
      url: `/api/family/mall/orders/${orderId}/refund`,
      method: 'POST',
      data: payload
    }),
    () => mock.refundMallOrder(orderId, payload)
  );
}

async function submitFeedback(payload) {
  return withFallback(
    async () => request({
      url: '/api/family/feedback',
      method: 'POST',
      data: payload
    }),
    () => mock.submitFeedback(payload)
  );
}

async function getFeedbackRecords(pageNo = 1, pageSize = 20) {
  return withFallback(
    async () => request({
      url: '/api/family/feedback/records',
      data: { pageNo, pageSize }
    }),
    () => mock.getFeedbackRecords(pageNo, pageSize)
  );
}

async function bookVideoVisit(payload) {
  return withFallback(
    async () => request({
      url: '/api/family/visit/video/book',
      method: 'POST',
      data: payload
    }),
    () => ({ id: Date.now(), visitTime: payload.bookingTime, visitTimeSlot: '视频探视' })
  );
}

async function getAffectionMoments() {
  return withFallback(
    async () => request({ url: '/api/family/affection/moments' }),
    () => mock.getAffectionMoments()
  );
}

async function getFestivalTemplates() {
  return withFallback(
    async () => request({ url: '/api/family/affection/templates' }),
    () => mock.getFestivalTemplates()
  );
}

async function addAffectionMoment(payload = {}) {
  const data = typeof payload === 'object'
    ? payload
    : { type: 'text', title: '亲情互动', desc: String(payload || '') };
  return withFallback(
    async () => request({
      url: '/api/family/affection/moments',
      method: 'POST',
      data
    }),
    () => mock.addAffectionMoment(data)
  );
}

async function getNotificationSettings() {
  return withFallback(
    async () => request({ url: '/api/family/notification-settings' }),
    () => mock.getNotificationSettings()
  );
}

async function updateNotificationSettings(payload) {
  return withFallback(
    async () => request({
      url: '/api/family/notification-settings',
      method: 'PUT',
      data: payload
    }),
    () => mock.updateNotificationSettings(payload)
  );
}

async function getSecuritySettings() {
  return withFallback(
    async () => request({ url: '/api/family/security-settings' }),
    () => mock.getSecuritySettings()
  );
}

async function updateSecuritySettings(payload) {
  return withFallback(
    async () => request({
      url: '/api/family/security-settings',
      method: 'PUT',
      data: payload
    }),
    () => mock.updateSecuritySettings(payload)
  );
}

async function sendSecuritySmsCode(scene = 'SECURITY') {
  return request({
    url: '/api/family/security/sms-code/send',
    method: 'POST',
    data: { scene }
  });
}

async function verifySecuritySmsCode(verifyCode, scene = 'SECURITY') {
  return request({
    url: '/api/family/security/sms-code/verify',
    method: 'POST',
    data: { verifyCode, scene }
  });
}

async function setSecurityPassword(password) {
  return withFallback(
    async () => request({
      url: '/api/family/security/password/set',
      method: 'POST',
      data: { password }
    }),
    () => mock.setSecurityPassword(password)
  );
}

async function verifySecurityPassword(password, scene = 'SECURITY') {
  return withFallback(
    async () => request({
      url: '/api/family/security/password/verify',
      method: 'POST',
      data: { password, scene }
    }),
    () => mock.verifySecurityPassword({ password, scene })
  );
}

async function uploadVoiceMessage(filePath, options = {}) {
  return uploadFile({
    url: '/api/files/upload',
    filePath,
    formData: {
      bizType: options.bizType || 'family-voice'
    }
  });
}

async function uploadAffectionMedia(filePath, options = {}) {
  return withFallback(
    async () => uploadFile({
      url: '/api/files/upload',
      filePath,
      formData: {
        bizType: options.bizType || 'family-affection-media'
      }
    }),
    () => mock.uploadVoiceMessage(filePath, options)
  );
}

async function getCommunicationMessages(pageNo = 1, pageSize = 20) {
  return withFallback(
    async () => request({ url: '/api/family/communication/messages', data: { pageNo, pageSize } }),
    () => mock.getCommunicationMessages()
  );
}

async function sendCommunicationMessage(payload) {
  return withFallback(
    async () => request({
      url: '/api/family/communication/messages',
      method: 'POST',
      data: payload
    }),
    () => mock.sendCommunicationMessage(payload)
  );
}

async function getCommunicationTemplates() {
  return withFallback(
    async () => request({ url: '/api/family/communication/templates' }),
    () => mock.getCommunicationTemplates()
  );
}

async function getHelpFaqs() {
  return withFallback(
    async () => request({ url: '/api/family/help/faqs' }),
    () => mock.getHelpFaqs()
  );
}

async function getTodoCenter(options = {}) {
  const elderId = resolveCurrentElderId(options.elderId);
  return withFallback(
    async () => request({ url: '/api/family/todo-center', data: { elderId } }),
    () => mock.getTodoCenter()
  );
}

async function generateAiAssessmentReports(payload = {}) {
  const elderId = resolveCurrentElderId(payload.elderId);
  return withFallback(
    async () => request({
      url: '/api/family/assessment-reports/generate-ai',
      method: 'POST',
      data: {
        elderId,
        reportType: payload.reportType || 'ALL'
      }
    }),
    () => mock.generateAiAssessmentReports(payload)
  );
}

async function handleTodoAction(todoKey, action, options = {}) {
  const payload = {
    todoKey,
    action
  };
  if (options.snoozeMinutes) {
    payload.snoozeMinutes = options.snoozeMinutes;
  }
  return withFallback(
    async () => request({
      url: '/api/family/todo-center/action',
      method: 'POST',
      data: payload
    }),
    () => mock.handleTodoAction(payload)
  );
}

async function getProfile() {
  return withFallback(
    async () => request({ url: '/api/family/profile' }),
    () => mock.getProfile()
  );
}

async function updateProfile(payload) {
  return withFallback(
    async () => request({
      url: '/api/family/profile',
      method: 'PUT',
      data: payload
    }),
    () => mock.updateProfile(payload)
  );
}

async function getFamilyBindings() {
  return withFallback(
    async () => request({ url: '/api/family/bindings' }),
    () => mock.getFamilyBindings()
  );
}

async function removeFamilyBinding(elderId) {
  return withFallback(
    async () => request({
      url: `/api/family/bindings/${elderId}`,
      method: 'DELETE'
    }),
    () => mock.removeFamilyBinding(elderId)
  );
}

module.exports = {
  familyLogin,
  sendFamilySmsCode,
  verifyFamilySmsCode,
  registerFamily,
  resetFamilyPassword,
  getFamilyAuthBootstrap,
  bindElder,
  getMyElders,
  getHomeDashboard,
  getWeeklyBrief,
  getWeeklyBriefHistory,
  getMessages,
  getMessageSummary,
  getMessageDetail,
  markMessageRead,
  markAllMessagesRead,
  getHealthDashboard,
  getMedicalRecords,
  getAssessmentReports,
  getSchedules,
  getMealCalendar,
  getCareLogs,
  getActivityAlbums,
  toggleAlbumLike,
  getActivityAlbumComments,
  addActivityAlbumComment,
  getOutingRecords,
  getEmergencyContacts,
  getBillSummary,
  getPaymentGuard,
  getBillHistory,
  rechargeBalance,
  createWechatRechargePrepay,
  bindWechatNotifyOpenId,
  getCapabilityStatus,
  getRechargeOrder,
  getRechargeOrders,
  toggleAutoPay,
  getServiceCatalog,
  getServiceOrders,
  createServiceOrder,
  getMallProducts,
  previewMallOrder,
  submitMallOrder,
  getMallOrders,
  getMallOrderDetail,
  cancelMallOrder,
  refundMallOrder,
  submitFeedback,
  getFeedbackRecords,
  bookVideoVisit,
  getAffectionMoments,
  getFestivalTemplates,
  addAffectionMoment,
  getNotificationSettings,
  updateNotificationSettings,
  getSecuritySettings,
  updateSecuritySettings,
  sendSecuritySmsCode,
  verifySecuritySmsCode,
  setSecurityPassword,
  verifySecurityPassword,
  uploadVoiceMessage,
  uploadAffectionMedia,
  getCommunicationMessages,
  sendCommunicationMessage,
  getCommunicationTemplates,
  getHelpFaqs,
  getTodoCenter,
  handleTodoAction,
  generateAiAssessmentReports,
  getProfile,
  updateProfile,
  getFamilyBindings,
  removeFamilyBinding
};
