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
    complianceInfo: {},
    serviceEmail: ''
  },
  async onShow() {
    getApp().ensureLogin();
    const supportInfo = getApp().globalData.supportInfo || {};
    this.setData({
      supportInfo,
      complianceInfo: getApp().globalData.complianceInfo || {},
      serviceEmail: String(supportInfo.serviceEmail || '').trim()
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
    const email = this.data.serviceEmail;
    if (!email) {
      // 未配置邮箱时引导用户走电话/反馈渠道，而不是留下死按钮
      wx.showModal({
        title: '暂未配置客服邮箱',
        content: `可拨打客服热线 ${(this.data.supportInfo && this.data.supportInfo.supportPhone) || '4008009000'}，或通过“提交建议反馈”联系我们。`,
        confirmText: '拨打热线',
        cancelText: '知道了',
        success: (res) => {
          if (res.confirm) {
            this.callService();
          }
        }
      });
      return;
    }
    wx.setClipboardData({
      data: email,
      success: () => wx.showToast({ title: '邮箱已复制', icon: 'none' }),
      fail: () => wx.showToast({ title: '复制失败，请手动记录邮箱', icon: 'none' })
    });
  }
});
