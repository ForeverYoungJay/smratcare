const { getToken, getUser, clearAuth, setAuth } = require('./utils/auth');

App({
  globalData: {
    baseUrl: 'http://localhost:8080',
    appName: '智养云家属端',
    useMockFallback: true,
    localDevBypassLogin: true,
    localDevSession: {
      orgId: 1,
      familyUserId: 0,
      realName: '本地调试家属',
      phone: '13800138000'
    },
    allowManualRechargeFallback: false,
    token: '',
    familyUser: null,
    selectedElderId: null,
    pendingMessageFilter: '',
    capabilityAlertCount: 0,
    mallOrdersDirty: false
  },
  onLaunch() {
    this.globalData.token = getToken();
    this.globalData.familyUser = getUser();
    if (!this.globalData.token && this.globalData.localDevBypassLogin && this.isLocalDevEnvironment()) {
      this.enableLocalDevSession();
    }
  },
  isLocalDevEnvironment() {
    const baseUrl = String(this.globalData.baseUrl || '').toLowerCase();
    return baseUrl.includes('localhost')
      || baseUrl.includes('127.0.0.1')
      || baseUrl.includes('0.0.0.0');
  },
  enableLocalDevSession() {
    const dev = this.globalData.localDevSession || {};
    const user = {
      orgId: Number(dev.orgId) || 1,
      familyUserId: Number(dev.familyUserId) || 0,
      realName: dev.realName || '本地调试家属',
      phone: dev.phone || '13800138000'
    };
    const token = `LOCAL_DEV_TOKEN_${Date.now()}`;
    this.globalData.token = token;
    this.globalData.familyUser = user;
    setAuth(token, user);
  },
  ensureLogin() {
    if (!this.globalData.token) {
      if (this.globalData.localDevBypassLogin && this.isLocalDevEnvironment()) {
        this.enableLocalDevSession();
        return true;
      }
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
    this.globalData.mallOrdersDirty = false;
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
