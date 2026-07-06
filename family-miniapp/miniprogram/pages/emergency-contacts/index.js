const { getEmergencyContacts } = require('../../services/family');

Page({
  data: {
    contacts: [],
    loading: false,
    loadError: ''
  },
  onShow() {
    getApp().ensureLogin();
    this.loadContacts();
  },
  onPullDownRefresh() {
    this.loadContacts().finally(() => wx.stopPullDownRefresh());
  },
  async loadContacts() {
    this.setData({ loading: true, loadError: '' });
    try {
      const contacts = await getEmergencyContacts();
      this.setData({ contacts: contacts || [] });
    } catch (error) {
      this.setData({ contacts: [], loadError: error.message || '联系人加载失败，请稍后重试' });
    } finally {
      this.setData({ loading: false });
    }
  },
  retry() {
    this.loadContacts();
  },
  call(e) {
    const phone = e.currentTarget.dataset.phone;
    if (!phone) {
      wx.showToast({ title: '暂无电话', icon: 'none' });
      return;
    }
    wx.makePhoneCall({ phoneNumber: String(phone) });
  }
});
