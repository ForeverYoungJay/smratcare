const { getHelpFaqs } = require('../../services/family');

function groupFaqs(list = []) {
  const grouped = {};
  list.forEach((item) => {
    const category = item.category || '常见问题';
    if (!grouped[category]) {
      grouped[category] = [];
    }
    grouped[category].push(item);
  });
  return Object.keys(grouped).map((category) => ({
    category,
    items: grouped[category]
  }));
}

Page({
  data: {
    groups: [],
    loading: false,
    loadError: '',
    supportInfo: {},
    complianceInfo: {}
  },
  async onShow() {
    getApp().ensureLogin();
    this.setData({
      supportInfo: getApp().globalData.supportInfo || {},
      complianceInfo: getApp().globalData.complianceInfo || {}
    });
    await this.loadFaqs();
  },
  async loadFaqs() {
    this.setData({ loading: true });
    try {
      const list = await getHelpFaqs();
      this.setData({ groups: groupFaqs(list || []), loadError: '' });
    } catch (error) {
      wx.showToast({ title: 'FAQ加载失败', icon: 'none' });
      this.setData({ groups: [], loadError: error.message || '帮助内容加载失败，请稍后重试' });
    } finally {
      this.setData({ loading: false });
    }
  },
  callService() {
    const phone = (this.data.supportInfo && this.data.supportInfo.supportPhone) || '4008009000';
    wx.makePhoneCall({ phoneNumber: phone });
  },
  goFeedback() {
    wx.navigateTo({ url: '/pages/feedback/index' });
  },
  copySupportEmail() {
    const email = (this.data.supportInfo && this.data.supportInfo.serviceEmail) || '';
    if (!email) {
      wx.showToast({ title: '暂未配置客服邮箱', icon: 'none' });
      return;
    }
    wx.setClipboardData({
      data: email,
      success: () => wx.showToast({ title: '邮箱已复制', icon: 'none' })
    });
  }
});
