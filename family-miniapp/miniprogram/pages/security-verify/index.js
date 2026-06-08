const { getSecuritySettings, verifySecurityPassword } = require('../../services/family');

Page({
  data: {
    scene: 'default',
    title: '敏感信息访问验证',
    password: '',
    submitting: false,
    verified: false,
    passwordLabel: '安全密码',
    passwordHint: '请输入独立安全密码；如未设置，则输入登录密码。'
  },
  onLoad(query) {
    const title = decodeURIComponent(query.title || '敏感信息访问验证');
    const scene = decodeURIComponent(query.scene || 'default');
    this.setData({ title, scene });
    this.loadPasswordHint();
  },
  async loadPasswordHint() {
    try {
      const settings = await getSecuritySettings();
      const hasIndependentPassword = !!(settings && settings.hasIndependentPassword);
      this.setData({
        passwordLabel: hasIndependentPassword ? '独立安全密码' : '登录密码',
        passwordHint: hasIndependentPassword
          ? '请输入在“隐私与安全”中设置的独立安全密码。'
          : '尚未设置独立安全密码，请输入当前账号登录密码。'
      });
    } catch (error) {
      this.setData({
        passwordLabel: '安全密码',
        passwordHint: '请输入独立安全密码；如未设置，则输入登录密码。'
      });
    }
  },
  onUnload() {
    if (this.data.verified) {
      return;
    }
    const channel = this.getOpenerEventChannel();
    if (channel) {
      channel.emit('cancelled', { scene: this.data.scene });
    }
  },
  onPasswordInput(e) {
    this.setData({ password: e.detail.value });
  },
  async submitVerify() {
    if (this.data.submitting) {
      return;
    }
    const password = (this.data.password || '').trim();
    if (!password) {
      wx.showToast({ title: '请输入登录密码', icon: 'none' });
      return;
    }
    this.setData({ submitting: true });
    try {
      const scene = (this.data.scene || 'SECURITY').toUpperCase();
      const resp = await verifySecurityPassword(password, scene);
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
