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
    loading: false
  },
  async onShow() {
    getApp().ensureLogin();
    await this.loadFaqs();
  },
  async loadFaqs() {
    this.setData({ loading: true });
    try {
      const list = await getHelpFaqs();
      this.setData({ groups: groupFaqs(list || []) });
    } catch (error) {
      wx.showToast({ title: 'FAQ加载失败', icon: 'none' });
      this.setData({ groups: [] });
    } finally {
      this.setData({ loading: false });
    }
  },
  callService() {
    wx.makePhoneCall({ phoneNumber: '4008009000' });
  },
  goFeedback() {
    wx.navigateTo({ url: '/pages/feedback/index' });
  }
});
