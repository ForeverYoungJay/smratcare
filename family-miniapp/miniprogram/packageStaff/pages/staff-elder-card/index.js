const { getElderCard } = require('../../../services/staff');

Page({
  data: {
    keyword: '',
    searched: false,
    loading: false,
    loadError: '',
    matches: [],
    card: null
  },
  onShow() {
    getApp().ensureLogin();
  },
  onKeywordInput(e) {
    this.setData({ keyword: e.detail.value });
  },
  onSearch() {
    const keyword = this.data.keyword.trim();
    if (!keyword) {
      wx.showToast({ title: '请输入姓名或扫码', icon: 'none' });
      return;
    }
    this.lookup({ keyword });
  },
  scanCode() {
    wx.scanCode({
      success: (res) => {
        const text = String(res.result || '').trim();
        if (!text) {
          wx.showToast({ title: '未识别到有效编码', icon: 'none' });
          return;
        }
        this.setData({ keyword: text });
        this.lookup({ keyword: text });
      },
      fail: () => {}
    });
  },
  pickMatch(e) {
    const elderId = e.currentTarget.dataset.id;
    if (elderId) this.lookup({ elderId });
  },
  async lookup(params) {
    if (this.data.loading) return;
    this.setData({ loading: true, loadError: '', searched: true });
    try {
      const payload = await getElderCard(params) || {};
      this.setData({
        matches: payload.card ? [] : (payload.matches || []),
        card: payload.card || null
      });
      if (!payload.card && !(payload.matches || []).length) {
        wx.showToast({ title: '未找到匹配的老人', icon: 'none' });
      }
    } catch (error) {
      this.setData({ loadError: error.message || '查询失败，请稍后重试', matches: [], card: null });
    } finally {
      this.setData({ loading: false });
    }
  },
  callEmergency() {
    const phone = this.data.card && this.data.card.emergencyPhone;
    if (!phone) {
      wx.showToast({ title: '未登记紧急联系电话', icon: 'none' });
      return;
    }
    wx.makePhoneCall({ phoneNumber: phone, fail: () => {} });
  },
  backToSearch() {
    this.setData({ card: null, matches: [], searched: false, keyword: '' });
  }
});
