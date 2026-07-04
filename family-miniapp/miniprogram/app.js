const { getToken, getUser, clearAuth, setAuth } = require('./utils/auth');
const offlineQueue = require('./utils/offline-queue');

const RUNTIME_FLAGS_STORAGE_KEY = 'smartcare_family_runtime_flags';
const OPENID_BIND_STORAGE_KEY = 'smartcare_family_openid_bind';
const OPENID_REBIND_INTERVAL_MS = 7 * 24 * 60 * 60 * 1000;

function normalizeRuntimeFlags(raw = {}) {
  return {
    enableMockFallback: !!raw.enableMockFallback,
    enableLocalDevBypassLogin: !!raw.enableLocalDevBypassLogin,
    allowManualRechargeFallback: !!raw.allowManualRechargeFallback,
    baseUrlOverride: raw.baseUrlOverride ? String(raw.baseUrlOverride).trim() : ''
  };
}

App({
  globalData: {
    baseUrl: 'https://gfyy.org.cn',
    currentBaseUrl: 'https://gfyy.org.cn',
    appName: '智养云',
    useMockFallback: false,
    localDevBypassLogin: false,
    localDevSession: {
      orgId: 1,
      familyUserId: 0,
      realName: '本地调试家属',
      phone: '13800138000'
    },
    localDevStaffSession: {
      id: 1,
      orgId: 1,
      username: 'nursing_emp',
      realName: '本地调试员工',
      roles: ['NURSING_EMPLOYEE']
    },
    allowManualRechargeFallback: false,
    runtimeProfile: 'develop',
    clientPlatform: 'unknown',
    runtimeReady: true,
    runtimeNotice: '',
    runtimeFlags: normalizeRuntimeFlags(),
    supportInfo: {
      organizationName: '智养云合作机构',
      supportPhone: '4008009000',
      supportHours: '工作日 09:00-18:00',
      serviceEmail: 'family-support@smartcare.example.com'
    },
    complianceInfo: {
      privacyPolicyVersion: 'v2026.05',
      serviceAgreementVersion: 'v2026.05',
      dataUsageSummary: '仅用于家属身份校验、老人照护信息展示、缴费充值、服务通知与安全审计。'
    },
    token: '',
    userType: '',
    familyUser: null,
    staffUser: null,
    selectedElderId: null,
    refreshHomeAfterBinding: false,
    pendingMessageFilter: '',
    capabilityAlertCount: 0,
    mallOrdersDirty: false,
    // 员工端订阅消息模板 ID。请在微信公众平台（小程序后台）->
    // 功能 -> 订阅消息中申请对应模板后，把模板 ID 填到这里；
    // 保持空串时 utils/subscribe.js 会静默跳过订阅请求，不影响业务。
    subscribeTemplates: {
      taskOverdue: '',
      approvalResult: '',
      noticePublish: ''
    }
  },
  onLaunch() {
    this.initRuntimeConfig();
    this.globalData.token = getToken();
    const storedUser = getUser();
    this.globalData.userType = storedUser && storedUser.userType ? storedUser.userType : (storedUser ? 'family' : '');
    if (this.globalData.userType === 'staff') {
      this.globalData.staffUser = storedUser;
    } else {
      this.globalData.familyUser = storedUser;
    }
    if (!this.globalData.token && this.canUseLocalDevBypass()) {
      this.enableLocalDevSession();
    }
    if (this.globalData.token && this.globalData.userType === 'family') {
      this.ensureWechatNotifyBind();
    }
    if (typeof wx.onNetworkStatusChange === 'function') {
      wx.onNetworkStatusChange((res) => {
        if (res && res.isConnected) {
          this.flushOfflineQueue();
        }
      });
    }
  },
  onShow() {
    this.flushOfflineQueue();
  },
  flushOfflineQueue() {
    offlineQueue.flush().catch(() => {
      // 离线队列补交失败时保留队列项，等待下次触发。
    });
  },
  // 登录态存在时自动绑定微信通知 openId：
  // - 默认按用户节流（7 天内不重复绑定），force = true 时强制重绑
  // - 绑定失败静默处理，不阻断主流程；callback(ok) 可用于页面提示
  ensureWechatNotifyBind(force = false, callback = null) {
    const done = (ok) => {
      if (typeof callback === 'function') {
        callback(!!ok);
      }
    };
    if (!this.globalData.runtimeReady || this.globalData.userType !== 'family' || !this.globalData.token) {
      done(false);
      return;
    }
    if (this.isLocalDevEnvironment() && String(this.globalData.token).indexOf('LOCAL_DEV_TOKEN_') === 0) {
      done(false);
      return;
    }
    const familyUserId = Number(this.globalData.familyUser && this.globalData.familyUser.familyUserId) || 0;
    if (!force) {
      try {
        const record = wx.getStorageSync(OPENID_BIND_STORAGE_KEY);
        if (record
          && Number(record.familyUserId) === familyUserId
          && Number(record.boundAt) > 0
          && Date.now() - Number(record.boundAt) < OPENID_REBIND_INTERVAL_MS) {
          done(true);
          return;
        }
      } catch (error) {
        // 读取失败按未绑定处理
      }
    }
    wx.login({
      success: async (loginRes) => {
        const code = loginRes && loginRes.code ? String(loginRes.code).trim() : '';
        if (!code) {
          done(false);
          return;
        }
        try {
          const { bindWechatNotifyOpenId } = require('./services/family');
          await bindWechatNotifyOpenId({ loginCode: code });
          try {
            wx.setStorageSync(OPENID_BIND_STORAGE_KEY, {
              familyUserId,
              boundAt: Date.now()
            });
          } catch (error) {
            // 写入失败不影响绑定结果
          }
          done(true);
        } catch (error) {
          // openId 绑定失败不阻断主流程
          done(false);
        }
      },
      fail: () => {
        // 忽略微信登录失败，避免影响主流程
        done(false);
      }
    });
  },
  initRuntimeConfig() {
    const runtimeProfile = this.getMiniProgramEnvVersion();
    const runtimeFlags = this.readRuntimeFlags();
    const clientPlatform = this.getClientPlatform();
    this.globalData.runtimeProfile = runtimeProfile;
    this.globalData.runtimeFlags = runtimeFlags;
    this.globalData.clientPlatform = clientPlatform;
    if (runtimeFlags.baseUrlOverride && runtimeProfile === 'develop') {
      this.globalData.baseUrl = runtimeFlags.baseUrlOverride;
    }
    this.globalData.currentBaseUrl = this.globalData.baseUrl;
    this.globalData.useMockFallback = this.isLocalDevEnvironment() && runtimeProfile === 'develop'
      && runtimeFlags.enableMockFallback;
    this.globalData.localDevBypassLogin = this.isLocalDevEnvironment() && runtimeProfile === 'develop'
      && runtimeFlags.enableLocalDevBypassLogin;
    this.globalData.allowManualRechargeFallback = this.isLocalDevEnvironment() && runtimeProfile === 'develop'
      && runtimeFlags.allowManualRechargeFallback;
    this.validateRuntimeConfig();
  },
  readRuntimeFlags() {
    try {
      const raw = wx.getStorageSync(RUNTIME_FLAGS_STORAGE_KEY);
      if (!raw || typeof raw !== 'object') {
        return normalizeRuntimeFlags();
      }
      return normalizeRuntimeFlags(raw);
    } catch (error) {
      return normalizeRuntimeFlags();
    }
  },
  getMiniProgramEnvVersion() {
    try {
      const accountInfo = wx.getAccountInfoSync();
      const envVersion = accountInfo && accountInfo.miniProgram && accountInfo.miniProgram.envVersion;
      if (envVersion) {
        return String(envVersion).toLowerCase();
      }
    } catch (error) {
      // Ignore and use develop as the safest local default.
    }
    return 'develop';
  },
  getClientPlatform() {
    try {
      const systemInfo = wx.getSystemInfoSync();
      const platform = systemInfo && systemInfo.platform ? systemInfo.platform : '';
      if (platform) {
        return String(platform).toLowerCase();
      }
    } catch (error) {
      // Ignore and use unknown.
    }
    return 'unknown';
  },
  isDevtoolsPlatform() {
    return this.globalData.clientPlatform === 'devtools';
  },
  isLocalDevEnvironment() {
    const baseUrl = String(this.globalData.baseUrl || '').toLowerCase();
    return baseUrl.includes('localhost')
      || baseUrl.includes('127.0.0.1')
      || baseUrl.includes('0.0.0.0');
  },
  isHttpsBaseUrl() {
    return String(this.globalData.baseUrl || '').trim().toLowerCase().startsWith('https://');
  },
  isPlaceholderBaseUrl() {
    const baseUrl = String(this.globalData.baseUrl || '').trim().toLowerCase();
    return !baseUrl || baseUrl.includes('your-domain.example.com');
  },
  canUseLocalDevBypass() {
    return this.globalData.localDevBypassLogin && this.isLocalDevEnvironment();
  },
  canUseManualRechargeFallback() {
    return this.globalData.allowManualRechargeFallback
      && this.globalData.useMockFallback
      && this.isLocalDevEnvironment()
      && this.globalData.runtimeProfile === 'develop';
  },
  validateRuntimeConfig() {
    const runtimeProfile = this.globalData.runtimeProfile;
    const clientPlatform = this.globalData.clientPlatform;
    const releaseLike = runtimeProfile === 'trial' || runtimeProfile === 'release';
    const usingLocalBaseUrl = this.isLocalDevEnvironment();
    const usingDevtools = this.isDevtoolsPlatform();
    const hasDevOnlyFlags = this.globalData.useMockFallback
      || this.globalData.localDevBypassLogin
      || this.globalData.allowManualRechargeFallback;
    if (this.isPlaceholderBaseUrl()) {
      this.globalData.runtimeReady = false;
      this.globalData.runtimeNotice = '当前还没有配置家属端接口地址，请先把小程序 baseUrl 切换到可访问的后端地址。';
      return;
    }
    if (!usingDevtools && usingLocalBaseUrl) {
      this.globalData.runtimeReady = false;
      this.globalData.runtimeNotice = '当前运行在真机或预览环境，`localhost` 只在微信开发者工具里可用。请改成已加入微信 request 合法域名的 HTTPS 接口地址。';
      return;
    }
    if (!usingDevtools && !this.isHttpsBaseUrl()) {
      this.globalData.runtimeReady = false;
      this.globalData.runtimeNotice = '当前运行在真机或预览环境，小程序接口必须使用 HTTPS 域名。请改成已加入微信 request 合法域名的 HTTPS 接口地址。';
      return;
    }
    if (releaseLike && usingLocalBaseUrl) {
      this.globalData.runtimeReady = false;
      this.globalData.runtimeNotice = '当前小程序仍指向本地联调地址，请切换为正式 HTTPS 接口后再提审或上线。';
      return;
    }
    if (releaseLike && hasDevOnlyFlags) {
      this.globalData.runtimeReady = false;
      this.globalData.runtimeNotice = '当前小程序仍启用了开发专用能力，请关闭 mock、免登录和人工回退后再提审或上线。';
      return;
    }
    this.globalData.runtimeReady = true;
    this.globalData.runtimeNotice = '';
  },
  assertRuntimeReady(showModal = true) {
    if (this.globalData.runtimeReady) {
      return true;
    }
    if (showModal) {
      wx.showModal({
        title: '运行配置未就绪',
        content: this.globalData.runtimeNotice || '请先完成正式环境配置后再继续。',
        showCancel: false,
        confirmText: '我知道了'
      });
    }
    return false;
  },
  enableLocalDevSession() {
    const dev = this.globalData.localDevSession || {};
    const user = {
      userType: 'family',
      orgId: Number(dev.orgId) || 1,
      familyUserId: Number(dev.familyUserId) || 0,
      realName: dev.realName || '本地调试家属',
      phone: dev.phone || '13800138000'
    };
    const token = `LOCAL_DEV_TOKEN_${Date.now()}`;
    this.globalData.token = token;
    this.globalData.userType = 'family';
    this.globalData.familyUser = user;
    setAuth(token, user);
  },
  enableLocalDevStaffSession() {
    const dev = this.globalData.localDevStaffSession || {};
    const user = {
      ...dev,
      userType: 'staff'
    };
    const token = `LOCAL_DEV_STAFF_TOKEN_${Date.now()}`;
    this.globalData.token = token;
    this.globalData.userType = 'staff';
    this.globalData.staffUser = user;
    this.globalData.familyUser = null;
    setAuth(token, user);
  },
  ensureLogin() {
    if (!this.assertRuntimeReady(false)) {
      return false;
    }
    if (!this.globalData.token) {
      if (this.canUseLocalDevBypass()) {
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
    try {
      wx.removeStorageSync(OPENID_BIND_STORAGE_KEY);
    } catch (error) {
      // 忽略清理失败
    }
    this.globalData.token = '';
    this.globalData.userType = '';
    this.globalData.familyUser = null;
    this.globalData.staffUser = null;
    this.globalData.selectedElderId = null;
    this.globalData.refreshHomeAfterBinding = false;
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
