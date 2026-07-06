const { getMyElders, getProfile, getCapabilityStatus } = require('../../services/family');

const CAPABILITY_ALERT_STATUSES = new Set(['OFF', 'MOCK', 'BIND_REQUIRED', 'DEPRECATED']);

function canUseMockFallback() {
  const app = getApp();
  return !!(app
    && app.globalData
    && app.globalData.useMockFallback
    && typeof app.isLocalDevEnvironment === 'function'
    && app.isLocalDevEnvironment());
}

function countCapabilityAlerts(status) {
  if (!status || !Array.isArray(status.items)) {
    return 0;
  }
  return status.items.filter((item) => CAPABILITY_ALERT_STATUSES.has(String(item.status || '').toUpperCase())).length;
}

Page({
  data: {
    elderCount: 0,
    profile: null,
    capabilityAlertCount: 0,
    partialWarning: '',
    loadError: '',
    supportInfo: {}
  },
  onShow() {
    getApp().ensureLogin();
    if (getApp().globalData.userType === 'staff') {
      wx.navigateTo({ url: '/packageStaff/pages/staff-profile/index' });
      return;
    }
    this.loadData();
  },
  async loadData() {
    this.setData({ loadError: '', partialWarning: '', supportInfo: getApp().globalData.supportInfo || {} });
    try {
      const [elders, profile, capabilityStatusResult] = await Promise.all([
        getMyElders(),
        getProfile(),
        getCapabilityStatus()
          .then((status) => ({ status, error: null }))
          .catch((error) => ({ status: null, error }))
      ]);
      const capabilityStatus = capabilityStatusResult && capabilityStatusResult.status;
      const capabilityError = capabilityStatusResult && capabilityStatusResult.error;
      const capabilityAlertCount = countCapabilityAlerts(capabilityStatus);
      getApp().updateCapabilityAlerts(capabilityAlertCount);
      this.setData({
        elderCount: (elders || []).length,
        profile,
        capabilityAlertCount,
        partialWarning: capabilityError && !canUseMockFallback()
          ? '能力状态暂未刷新，支付、通知和安全链路请进入“能力状态”页后重试。'
          : ''
      });
    } catch (error) {
      this.setData({
        profile: null,
        elderCount: 0,
        loadError: error.message || '个人信息加载失败，请稍后重试'
      });
    }
  },
  go(e) {
    wx.navigateTo({ url: e.currentTarget.dataset.path });
  },
  logout() {
    getApp().logout();
  }
});
