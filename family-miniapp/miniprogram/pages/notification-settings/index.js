const { getNotificationSettings, updateNotificationSettings } = require('../../services/family');

Page({
  data: {
    setting: {}
  },
  async onShow() {
    getApp().ensureLogin();
    const setting = await getNotificationSettings();
    this.setData({ setting });
  },
  async onSwitch(e) {
    const field = e.currentTarget.dataset.field;
    const value = !!e.detail.value;
    const next = await updateNotificationSettings({ [field]: value });
    this.setData({ setting: next });
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
      this.setData({ setting: next });
      wx.showToast({ title: accepted ? '订阅授权成功' : '未授权订阅', icon: 'none' });
    } catch (error) {
      wx.showToast({ title: '订阅授权失败', icon: 'none' });
    }
  }
});
