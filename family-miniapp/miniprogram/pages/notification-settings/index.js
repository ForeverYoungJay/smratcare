const { getNotificationSettings, updateNotificationSettings } = require('../../services/family');

Page({
  data: {
    setting: {},
    loading: false,
    loadError: ''
  },
  async onShow() {
    getApp().ensureLogin();
    await this.loadData();
  },
  async loadData() {
    this.setData({ loading: true, loadError: '' });
    try {
      const setting = await getNotificationSettings();
      this.setData({ setting: setting || {} });
    } catch (error) {
      this.setData({ setting: {}, loadError: error.message || '通知设置加载失败，请稍后重试' });
    } finally {
      this.setData({ loading: false });
    }
  },
  retryLoad() {
    this.loadData();
  },
  async onSwitch(e) {
    const field = e.currentTarget.dataset.field;
    const value = !!e.detail.value;
    const previous = !!(this.data.setting && this.data.setting[field]);
    try {
      const next = await updateNotificationSettings({ [field]: value });
      this.setData({ setting: next || {} });
    } catch (error) {
      this.setData({ [`setting.${field}`]: previous });
      wx.showToast({ title: error.message || '设置保存失败，请稍后重试', icon: 'none' });
    }
  },
  async requestSubscribe() {
    const ids = (this.data.setting && this.data.setting.subscribeTemplateIds) || [];
    if (!ids.length) {
      wx.showToast({ title: '暂无可授权模板', icon: 'none' });
      return;
    }
    try {
      const resp = await new Promise((resolve, reject) => {
        wx.requestSubscribeMessage({
          tmplIds: ids,
          success: resolve,
          fail: reject
        });
      });
      const accepted = Object.values(resp || {}).some((item) => item === 'accept');
      const next = await updateNotificationSettings({ subscribeAuthorized: accepted });
      this.setData({ setting: next || {} });
      wx.showToast({ title: accepted ? '订阅授权成功' : '未授权订阅', icon: 'none' });
    } catch (error) {
      wx.showToast({ title: '订阅授权失败', icon: 'none' });
    }
  },
  rebindOpenId() {
    const app = getApp();
    if (typeof app.ensureWechatNotifyBind !== 'function') {
      return;
    }
    wx.showLoading({ title: '绑定中', mask: true });
    app.ensureWechatNotifyBind(true, (ok) => {
      wx.hideLoading();
      wx.showToast({
        title: ok ? '微信通知绑定成功' : '绑定失败，请稍后重试',
        icon: 'none'
      });
    });
  },
  goNotifyHistory() {
    wx.navigateTo({ url: '/pages/notify-history/index' });
  }
});
