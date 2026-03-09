const { getMessages, markAllMessagesRead } = require('../../services/family');

const FILTER_OPTIONS = {
  all: {},
  unread: { unreadOnly: true },
  urgent: { level: 'urgent' },
  payment: { type: '费用提醒' },
  health: { type: '健康提醒' }
};

Page({
  data: {
    list: [],
    unreadCount: 0,
    processing: false,
    emptyText: '暂无消息',
    activeFilter: 'all',
    filters: [
      { id: 'all', label: '全部' },
      { id: 'unread', label: '未读' },
      { id: 'urgent', label: '紧急' },
      { id: 'payment', label: '费用' },
      { id: 'health', label: '健康' }
    ]
  },
  onShow() {
    getApp().ensureLogin();
    const app = getApp();
    const pending = app.globalData.pendingMessageFilter;
    if (pending && FILTER_OPTIONS[pending]) {
      app.globalData.pendingMessageFilter = '';
      this.setData({ activeFilter: pending });
    }
    this.loadData();
  },
  async loadData() {
    const activeFilter = this.data.activeFilter || 'all';
    const [list, all] = await Promise.all([
      getMessages(1, 50, FILTER_OPTIONS[activeFilter] || {}),
      getMessages(1, 50, FILTER_OPTIONS.unread)
    ]);
    const unreadCount = (all || []).filter((item) => item.unread).length;
    const emptyTextMap = {
      all: '暂无消息',
      unread: '暂无未读消息',
      urgent: '暂无紧急消息',
      payment: '暂无费用提醒',
      health: '暂无健康提醒'
    };
    this.setData({
      list: list || [],
      unreadCount,
      emptyText: emptyTextMap[activeFilter] || '暂无消息'
    });
  },
  async switchFilter(e) {
    const next = e.currentTarget.dataset.filter;
    if (!next || next === this.data.activeFilter) {
      return;
    }
    this.setData({ activeFilter: next });
    await this.loadData();
  },
  async readAll() {
    if (this.data.processing || this.data.unreadCount <= 0) {
      return;
    }
    this.setData({ processing: true });
    try {
      await markAllMessagesRead();
      wx.showToast({ title: '已全部标记已读', icon: 'success' });
      await this.loadData();
    } finally {
      this.setData({ processing: false });
    }
  },
  goAlertCenter() {
    wx.navigateTo({ url: '/pages/alert-center/index' });
  },
  contactNurse(e) {
    wx.makePhoneCall({ phoneNumber: e.currentTarget.dataset.phone });
  },
  goDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/message-detail/index?id=${id}` });
  }
});
