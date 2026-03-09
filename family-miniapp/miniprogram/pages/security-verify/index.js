const {
  sendSecuritySmsCode,
  verifySecuritySmsCode,
  verifySecurityPassword
} = require('../../services/family');

Page({
  data: {
    scene: 'default',
    title: '敏感信息访问验证',
    mode: 'sms',
    verifyCode: '',
    password: '',
    sending: false,
    submitting: false,
    verified: false,
    countdown: 0,
    debugCodeHint: ''
  },
  timer: null,
  onLoad(query) {
    const title = decodeURIComponent(query.title || '敏感信息访问验证');
    const scene = decodeURIComponent(query.scene || 'default');
    const mode = decodeURIComponent(query.mode || 'sms');
    this.setData({ title, scene, mode: mode === 'password' ? 'password' : 'sms' });
  },
  onUnload() {
    this.clearTimer();
    if (this.data.verified) {
      return;
    }
    const channel = this.getOpenerEventChannel();
    if (channel) {
      channel.emit('cancelled', { scene: this.data.scene });
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
  onCodeInput(e) {
    this.setData({ verifyCode: e.detail.value });
  },
  onPasswordInput(e) {
    this.setData({ password: e.detail.value });
  },
  switchMode() {
    const mode = this.data.mode === 'sms' ? 'password' : 'sms';
    this.setData({ mode });
  },
  async sendCode() {
    if (this.data.sending || this.data.countdown > 0) {
      return;
    }
    this.setData({ sending: true });
    try {
      const scene = (this.data.scene || 'SECURITY').toUpperCase();
      const resp = await sendSecuritySmsCode(scene);
      const retryAfterSeconds = Number(resp && resp.retryAfterSeconds) || 60;
      this.startCountdown(retryAfterSeconds);
      this.setData({ debugCodeHint: resp && resp.debugCode ? `调试码：${resp.debugCode}` : '' });
      wx.showToast({ title: '验证码已发送', icon: 'none' });
    } catch (error) {
      wx.showToast({ title: error.message || '验证码发送失败', icon: 'none' });
    } finally {
      this.setData({ sending: false });
    }
  },
  async submitVerify() {
    if (this.data.submitting) {
      return;
    }
    this.setData({ submitting: true });
    try {
      const scene = (this.data.scene || 'SECURITY').toUpperCase();
      let resp;
      if (this.data.mode === 'password') {
        const password = (this.data.password || '').trim();
        if (!password) {
          wx.showToast({ title: '请输入独立密码', icon: 'none' });
          return;
        }
        resp = await verifySecurityPassword(password, scene);
      } else {
        const code = (this.data.verifyCode || '').trim();
        if (!code) {
          wx.showToast({ title: '请输入验证码', icon: 'none' });
          return;
        }
        resp = await verifySecuritySmsCode(code, scene);
      }
      if (!resp || !resp.passed) {
        wx.showToast({ title: (resp && resp.message) || '验证失败，请重试', icon: 'none' });
        return;
      }
      this.setData({ verified: true });
      const channel = this.getOpenerEventChannel();
      if (channel) {
        channel.emit('verified', { scene: this.data.scene });
      }
      wx.showToast({ title: '验证通过', icon: 'success' });
      setTimeout(() => {
        wx.navigateBack();
      }, 200);
    } finally {
      this.setData({ submitting: false });
    }
  }
});
