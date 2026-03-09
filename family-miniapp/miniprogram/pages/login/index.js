const { familyLogin, registerFamily, sendFamilySmsCode, bindWechatNotifyOpenId } = require('../../services/family');
const { setAuth } = require('../../utils/auth');

Page({
  data: {
    loading: false,
    sendingCode: false,
    countdown: 0,
    debugCodeHint: '',
    mode: 'login',
    form: {
      orgId: '1',
      phone: '',
      verifyCode: '',
      realName: ''
    }
  },
  timer: null,
  onShow() {
    const app = getApp();
    if (app.globalData.token) {
      wx.reLaunch({ url: '/pages/home/index' });
      return;
    }
    if (app.globalData.localDevBypassLogin && app.isLocalDevEnvironment()) {
      app.enableLocalDevSession();
      wx.showToast({ title: '已进入本地调试模式', icon: 'none' });
      wx.reLaunch({ url: '/pages/home/index' });
    }
  },
  switchMode(e) {
    this.setData({ mode: e.currentTarget.dataset.mode });
  },
  onOrgInput(e) {
    this.setData({ 'form.orgId': e.detail.value });
  },
  onPhoneInput(e) {
    this.setData({ 'form.phone': e.detail.value });
  },
  onCodeInput(e) {
    this.setData({ 'form.verifyCode': e.detail.value });
  },
  onNameInput(e) {
    this.setData({ 'form.realName': e.detail.value });
  },
  onUnload() {
    this.clearTimer();
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
  async sendCode() {
    if (this.data.sendingCode || this.data.countdown > 0) {
      return;
    }
    const { orgId, phone } = this.data.form;
    if (!orgId || !phone) {
      wx.showToast({ title: '请先填写机构ID和手机号', icon: 'none' });
      return;
    }
    this.setData({ sendingCode: true });
    try {
      const resp = await sendFamilySmsCode({
        orgId: Number(orgId),
        phone: String(phone).trim(),
        scene: 'LOGIN'
      });
      const retryAfterSeconds = Number(resp && resp.retryAfterSeconds) || 60;
      this.startCountdown(retryAfterSeconds);
      this.setData({ debugCodeHint: resp && resp.debugCode ? `调试码：${resp.debugCode}` : '' });
      wx.showToast({ title: '验证码已发送', icon: 'none' });
    } catch (error) {
      wx.showToast({ title: error.message || '验证码发送失败', icon: 'none' });
    } finally {
      this.setData({ sendingCode: false });
    }
  },
  async onSubmit() {
    const { orgId, phone, verifyCode, realName } = this.data.form;
    if (!orgId || !phone || !verifyCode) {
      wx.showToast({ title: '请完整填写信息', icon: 'none' });
      return;
    }
    this.setData({ loading: true });
    try {
      if (this.data.mode === 'register') {
        await registerFamily({
          orgId: Number(orgId),
          phone,
          realName: realName || phone
        });
      }
      const res = await familyLogin({
        orgId: Number(orgId),
        phone,
        verifyCode
      });
      const app = getApp();
      app.globalData.token = res.token;
      app.globalData.familyUser = {
        familyUserId: res.familyUserId,
        orgId: res.orgId || Number(orgId),
        realName: res.realName,
        phone: res.phone
      };
      setAuth(res.token, app.globalData.familyUser);
      this.bindWechatNotifyOpenId();
      wx.reLaunch({ url: '/pages/home/index' });
    } catch (error) {
      wx.showToast({ title: error.message || '登录失败，请稍后重试', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },
  bindWechatNotifyOpenId() {
    wx.login({
      success: async (loginRes) => {
        const code = loginRes && loginRes.code ? String(loginRes.code).trim() : '';
        if (!code) {
          return;
        }
        try {
          await bindWechatNotifyOpenId({ loginCode: code });
        } catch (error) {
          // openId 绑定失败不阻断登录流程
        }
      },
      fail: () => {
        // 忽略微信登录失败，避免影响主登录流程
      }
    });
  }
});
