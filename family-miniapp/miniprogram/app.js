const { getToken, getUser, clearAuth } = require('./utils/auth');

App({
  globalData: {
    baseUrl: 'http://localhost:8080',
    appName: '智养云家属端',
    useMockFallback: true,
    allowManualRechargeFallback: false,
    token: '',
    familyUser: null,
    selectedElderId: null,
    pendingMessageFilter: '',
    capabilityAlertCount: 0
  },
  onLaunch() {
    this.globalData.token = getToken();
    this.globalData.familyUser = getUser();
  },
  ensureLogin() {
    if (!this.globalData.token) {
      wx.reLaunch({ url: '/pages/login/index' });
      return false;
    }
    return true;
  },
  logout() {
    clearAuth();
    this.globalData.token = '';
    this.globalData.familyUser = null;
    this.globalData.selectedElderId = null;
    this.globalData.pendingMessageFilter = '';
    this.updateCapabilityAlerts(0);
    wx.reLaunch({ url: '/pages/login/index' });
  },
  updateCapabilityAlerts(count) {
    const safeCount = Math.max(0, Number(count) || 0);
    this.globalData.capabilityAlertCount = safeCount;
    if (safeCount > 0) {
      const text = safeCount > 99 ? '99+' : String(safeCount);
      wx.setTabBarBadge({
        index: 3,
        text
      });
      return;
    }
    wx.removeTabBarBadge({ index: 3 });
  }
});
