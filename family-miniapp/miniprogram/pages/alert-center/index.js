const { getMessageSummary, markAllMessagesRead } = require('../../services/family');

function levelClass(level) {
  if (level === 'urgent') return 'lv-urgent';
  if (level === 'warning') return 'lv-warning';
  return 'lv-normal';
}

Page({
  data: {
    summary: {
      totalCount: 0,
      unreadCount: 0,
      urgentUnreadCount: 0,
      warningUnreadCount: 0,
      normalUnreadCount: 0,
      typeSummary: [],
      urgentItems: []
    },
    processing: false
  },
  onShow() {
    getApp().ensureLogin();
    this.loadData();
  },
  onPullDownRefresh() {
    this.loadData(true);
  },
  async loadData(fromPullDown = false) {
    try {
      const summary = await getMessageSummary();
      const urgentItems = (summary.urgentItems || []).map((item) => ({
        ...item,
        levelClass: levelClass(item.level)
      }));
      this.setData({
        summary: {
          ...summary,
          urgentItems
        }
      });
    } finally {
      if (fromPullDown) {
        wx.stopPullDownRefresh();
      }
    }
  },
  async markAllRead() {
    if (this.data.processing || this.data.summary.unreadCount <= 0) {
      return;
    }
    this.setData({ processing: true });
    try {
      await markAllMessagesRead();
      wx.showToast({ title: '已全部已读', icon: 'success' });
      await this.loadData();
    } finally {
      this.setData({ processing: false });
    }
  },
  goMessageList() {
    getApp().globalData.pendingMessageFilter = 'urgent';
    wx.switchTab({ url: '/pages/messages/index' });
  },
  goMessageDetail(e) {
    const id = e.currentTarget.dataset.id;
    if (!id) {
      return;
    }
    wx.navigateTo({ url: `/pages/message-detail/index?id=${id}` });
  },
  callNow(e) {
    const phone = e.currentTarget.dataset.phone;
    if (!phone) {
      return;
    }
    wx.makePhoneCall({ phoneNumber: phone });
  }
});
