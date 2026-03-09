const { getMessageDetail, markMessageRead } = require('../../services/family');

Page({
  data: {
    id: null,
    detail: null
  },
  onLoad(query) {
    this.setData({ id: Number(query.id || 0) });
  },
  onShow() {
    getApp().ensureLogin();
    this.loadData();
  },
  async loadData() {
    const detail = await getMessageDetail(this.data.id);
    this.setData({ detail });
  },
  async markRead() {
    await markMessageRead(this.data.id);
    wx.showToast({ title: '已标记已读', icon: 'success' });
    this.loadData();
  },
  callNurse() {
    if (!this.data.detail || !this.data.detail.phone) {
      return;
    }
    wx.makePhoneCall({ phoneNumber: this.data.detail.phone });
  }
});
