const { getMessages, markAllMessagesRead } = require('../../services/family');

const FILTER_OPTIONS = {
  all: {},
  unread: { unreadOnly: true },
  urgent: { level: 'urgent' },
  payment: { type: '费用提醒' },
  health: { type: '健康提醒' }
};

function unwrapList(result) {
  if (Array.isArray(result)) return result;
  if (result && Array.isArray(result.list)) return result.list;
  if (result && Array.isArray(result.records)) return result.records;
  return [];
}

Page({
  data: {
    list: [],
    unreadCount: 0,
    loading: false,
    loadingMore: false,
    loadError: '',
    processing: false,
    emptyText: '暂无消息',
    activeFilter: 'all',
    pageNo: 1,
    pageSize: 20,
    hasMore: false,
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
    this.loadData({ reset: true });
  },
  async loadData(options = {}) {
    const reset = options.reset !== false;
    const activeFilter = this.data.activeFilter || 'all';
    const nextPageNo = reset ? 1 : Number(this.data.pageNo || 1) + 1;
    this.setData(reset ? { loading: true, loadError: '' } : { loadingMore: true, loadError: '' });
    try {
      const [listRes, unreadRes] = await Promise.all([
        getMessages(nextPageNo, this.data.pageSize, FILTER_OPTIONS[activeFilter] || {}),
        getMessages(1, 50, FILTER_OPTIONS.unread)
      ]);
      const list = unwrapList(listRes);
      const unreadList = unwrapList(unreadRes);
      const unreadCount = unreadList.filter((item) => item.unread).length;
      const emptyTextMap = {
        all: '暂无消息',
        unread: '暂无未读消息',
        urgent: '暂无紧急消息',
        payment: '暂无费用提醒',
        health: '暂无健康提醒'
      };
      this.setData({
        list: reset ? list : this.data.list.concat(list),
        unreadCount,
        pageNo: nextPageNo,
        hasMore: list.length >= this.data.pageSize,
        emptyText: emptyTextMap[activeFilter] || '暂无消息'
      });
    } catch (error) {
      this.setData({ loadError: error.message || (reset ? '消息加载失败' : '更多消息加载失败，请稍后重试') });
    } finally {
      this.setData({ loading: false, loadingMore: false });
    }
  },
  async switchFilter(e) {
    const next = e.currentTarget.dataset.filter;
    if (!next || next === this.data.activeFilter) {
      return;
    }
    this.setData({ activeFilter: next });
    await this.loadData({ reset: true });
  },
  loadMore() {
    if (this.data.loading || this.data.loadingMore || !this.data.hasMore) return;
    this.loadData({ reset: false });
  },
  onReachBottom() {
    this.loadMore();
  },
  async readAll() {
    if (this.data.processing || this.data.unreadCount <= 0) {
      return;
    }
    this.setData({ processing: true });
    try {
      await markAllMessagesRead();
      wx.showToast({ title: '已全部标记已读', icon: 'success' });
      await this.loadData({ reset: true });
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
