const {
  familyLogin,
  registerFamily,
  resetFamilyPassword,
  sendFamilySmsCode,
  getFamilyAuthBootstrap
} = require('../../services/family');
const { staffLogin } = require('../../services/staff');
const { setAuth } = require('../../utils/auth');

const MODE_LOGIN = 'login';
const MODE_REGISTER = 'register';
const MODE_RESET = 'reset';

function trimText(value) {
  return String(value || '').trim();
}

Page({
  data: {
    loadingBootstrap: false,
    loading: false,
    sendingCode: false,
    countdown: 0,
    debugCodeHint: '',
    runtimeNotice: '',
    runtimeReady: true,
    runtimeProfile: 'develop',
    clientPlatform: 'unknown',
    currentBaseUrl: '',
    mode: MODE_LOGIN,
    identity: 'family',
    orgId: 1,
    orgName: '默认机构',
    supportInfo: {},
    complianceInfo: {},
    form: {
      phone: '',
      verifyCode: '',
      password: '',
      realName: '',
      username: '',
      staffPassword: ''
    }
  },
  timer: null,
  onLoad() {
    const app = getApp();
    this.setData({
      runtimeNotice: app.globalData.runtimeNotice || '',
      runtimeReady: app.globalData.runtimeReady !== false,
      runtimeProfile: app.globalData.runtimeProfile || 'develop',
      clientPlatform: app.globalData.clientPlatform || 'unknown',
      currentBaseUrl: app.globalData.currentBaseUrl || app.globalData.baseUrl || '',
      supportInfo: app.globalData.supportInfo || {},
      complianceInfo: app.globalData.complianceInfo || {}
    });
    this.loadBootstrap();
  },
  onShow() {
    const app = getApp();
    this.setData({
      runtimeNotice: app.globalData.runtimeNotice || '',
      runtimeReady: app.globalData.runtimeReady !== false,
      runtimeProfile: app.globalData.runtimeProfile || 'develop',
      clientPlatform: app.globalData.clientPlatform || 'unknown',
      currentBaseUrl: app.globalData.currentBaseUrl || app.globalData.baseUrl || ''
    });
    if (!app.assertRuntimeReady(false)) {
      return;
    }
    if (app.globalData.token) {
      wx.reLaunch({ url: app.globalData.userType === 'staff' ? '/pages/staff-home/index' : '/pages/home/index' });
      return;
    }
    if (app.globalData.localDevBypassLogin && app.isLocalDevEnvironment()) {
      app.enableLocalDevSession();
      wx.reLaunch({ url: '/pages/home/index' });
    }
  },
  onUnload() {
    this.clearTimer();
  },
  async loadBootstrap() {
    const app = getApp();
    if (!app.assertRuntimeReady(false)) {
      this.setData({
        orgId: 1,
        orgName: '待完成正式环境配置'
      });
      return;
    }
    this.setData({ loadingBootstrap: true });
    try {
      const resp = await getFamilyAuthBootstrap();
      this.setData({
        orgId: Number(resp && resp.orgId) || 1,
        orgName: (resp && resp.orgName) || '默认机构'
      });
    } catch (error) {
      this.setData({
        orgId: 1,
        orgName: '默认机构'
      });
    } finally {
      this.setData({ loadingBootstrap: false });
    }
  },
  clearTimer() {
    if (this.timer) {
      clearInterval(this.timer);
      this.timer = null;
    }
  },
  startCountdown(seconds) {
    const total = Number(seconds) > 0 ? Number(seconds) : 60;
    this.clearTimer();
    this.setData({ countdown: total });
    this.timer = setInterval(() => {
      const next = this.data.countdown - 1;
      if (next <= 0) {
        this.clearTimer();
        this.setData({ countdown: 0 });
        return;
      }
      this.setData({ countdown: next });
    }, 1000);
  },
  switchMode(e) {
    const mode = e.currentTarget.dataset.mode || MODE_LOGIN;
    if (mode === this.data.mode) {
      return;
    }
    this.clearTimer();
    this.setData({
      mode,
      countdown: 0,
      debugCodeHint: '',
      'form.verifyCode': '',
      'form.password': ''
    });
  },
  switchIdentity(e) {
    const identity = e.currentTarget.dataset.identity || 'family';
    if (identity === this.data.identity) {
      return;
    }
    this.clearTimer();
    this.setData({
      identity,
      mode: MODE_LOGIN,
      countdown: 0,
      debugCodeHint: '',
      'form.verifyCode': '',
      'form.password': '',
      'form.staffPassword': ''
    });
  },
  onPhoneInput(e) {
    this.setData({ 'form.phone': e.detail.value });
  },
  onCodeInput(e) {
    this.setData({ 'form.verifyCode': e.detail.value });
  },
  onPasswordInput(e) {
    this.setData({ 'form.password': e.detail.value });
  },
  onUsernameInput(e) {
    this.setData({ 'form.username': e.detail.value });
  },
  onStaffPasswordInput(e) {
    this.setData({ 'form.staffPassword': e.detail.value });
  },
  onNameInput(e) {
    this.setData({ 'form.realName': e.detail.value });
  },
  currentCodeScene() {
    return this.data.mode === MODE_REGISTER ? 'REGISTER' : 'RESET_PASSWORD';
  },
  async sendCode() {
    if (!getApp().assertRuntimeReady()) {
      return;
    }
    if (this.data.mode === MODE_LOGIN) {
      return;
    }
    if (this.data.sendingCode || this.data.countdown > 0) {
      return;
    }
    const phone = trimText(this.data.form.phone);
    if (!phone) {
      wx.showToast({ title: '请先填写手机号', icon: 'none' });
      return;
    }
    this.setData({ sendingCode: true });
    try {
      const resp = await sendFamilySmsCode({
        orgId: Number(this.data.orgId) || 1,
        phone,
        scene: this.currentCodeScene()
      });
      const retryAfterSeconds = Number(resp && resp.retryAfterSeconds) || 60;
      this.startCountdown(retryAfterSeconds);
      this.setData({ debugCodeHint: '' });
      wx.showToast({ title: '验证码已发送', icon: 'none' });
    } catch (error) {
      wx.showToast({ title: error.message || '验证码发送失败', icon: 'none' });
    } finally {
      this.setData({ sendingCode: false });
    }
  },
  async onSubmit() {
    if (!getApp().assertRuntimeReady()) {
      return;
    }
    if (this.data.loading) {
      return;
    }
    if (this.data.identity === 'staff') {
      await this.submitStaffLogin();
      return;
    }
    const phone = trimText(this.data.form.phone);
    const verifyCode = trimText(this.data.form.verifyCode);
    const password = trimText(this.data.form.password);
    const realName = trimText(this.data.form.realName);
    const orgId = Number(this.data.orgId) || 1;

    if (!phone) {
      wx.showToast({ title: '请输入手机号', icon: 'none' });
      return;
    }
    if (!password) {
      wx.showToast({ title: this.data.mode === MODE_RESET ? '请输入新密码' : '请输入密码', icon: 'none' });
      return;
    }
    if (password.length < 6) {
      wx.showToast({ title: '密码长度至少6位', icon: 'none' });
      return;
    }
    if (this.data.mode !== MODE_LOGIN && !verifyCode) {
      wx.showToast({ title: '请输入验证码', icon: 'none' });
      return;
    }

    this.setData({ loading: true });
    try {
      let loginResp = null;
      if (this.data.mode === MODE_REGISTER) {
        loginResp = await registerFamily({
          orgId,
          phone,
          verifyCode,
          password,
          realName: realName || phone
        });
      } else if (this.data.mode === MODE_RESET) {
        await resetFamilyPassword({
          orgId,
          phone,
          verifyCode,
          newPassword: password
        });
        loginResp = await familyLogin({
          orgId,
          phone,
          password
        });
      } else {
        loginResp = await familyLogin({
          orgId,
          phone,
          password
        });
      }

      const app = getApp();
      app.globalData.token = loginResp.token;
      app.globalData.userType = 'family';
      app.globalData.familyUser = {
        userType: 'family',
        familyUserId: loginResp.familyUserId,
        orgId: loginResp.orgId || orgId,
        realName: loginResp.realName,
        phone: loginResp.phone
      };
      app.globalData.staffUser = null;
      app.globalData.selectedElderId = null;
      setAuth(loginResp.token, app.globalData.familyUser);
      app.ensureWechatNotifyBind(true);
      wx.reLaunch({ url: '/pages/home/index' });
    } catch (error) {
      wx.showToast({ title: error.message || '操作失败，请稍后重试', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },
  async submitStaffLogin() {
    const username = trimText(this.data.form.username);
    const password = trimText(this.data.form.staffPassword);
    if (!username || !password) {
      wx.showToast({ title: '请输入员工账号和密码', icon: 'none' });
      return;
    }
    this.setData({ loading: true });
    try {
      const loginResp = await staffLogin({ username, password });
      const token = loginResp && loginResp.token;
      if (!token) {
        throw new Error('登录响应缺少 token');
      }
      const app = getApp();
      const staffUser = {
        ...(loginResp.staffInfo || {}),
        userType: 'staff',
        username,
        roles: loginResp.roles || []
      };
      app.globalData.token = token;
      app.globalData.userType = 'staff';
      app.globalData.staffUser = staffUser;
      app.globalData.familyUser = null;
      app.globalData.selectedElderId = null;
      setAuth(token, staffUser);
      wx.reLaunch({ url: '/pages/staff-home/index' });
    } catch (error) {
      wx.showToast({ title: error.message || '员工登录失败', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  }
});
