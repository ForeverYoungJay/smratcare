const { getNotifyLogs } = require('../../services/family');

const PAGE_SIZE = 20;
const STATUS_FILTERS = [
  { key: '', label: '全部' },
  { key: 'SUCCESS', label: '已送达' },
  { key: 'FAILED', label: '发送失败' },
  { key: 'PENDING', label: '待发送' }
];

Page({
  data: {
    list: [],
    loading: false,
    loadError: '',
    statusFilters: STATUS_FILTERS,
    activeStatus: '',
    pageNo: 1,
    hasMore: true,
    loadingMore: false
  },
  async onShow() {
    getApp().ensureLogin();
    await this.reload();
  },
  async onPullDownRefresh() {
    await this.reload();
    wx.stopPullDownRefresh();
  },
  async reload() {
    this.setData({ pageNo: 1, hasMore: true });
    await this.loadData(true);
  },
  async loadData(reset = false) {
    if (this.data.loading || this.data.loadingMore) {
      return;
    }
    this.setData(reset ? { loading: true, loadError: '' } : { loadingMore: true });
    try {
      const pageNo = reset ? 1 : this.data.pageNo;
      const list = await getNotifyLogs(pageNo, PAGE_SIZE, { status: this.data.activeStatus });
      const items = list || [];
      this.setData({
        list: reset ? items : this.data.list.concat(items),
        pageNo: pageNo + 1,
        hasMore: items.length >= PAGE_SIZE,
        loadError: ''
      });
    } catch (error) {
      if (reset) {
        this.setData({ list: [], loadError: error.message || '通知记录加载失败，请稍后重试' });
      } else {
        wx.showToast({ title: '加载更多失败，请稍后重试', icon: 'none' });
      }
    } finally {
      this.setData({ loading: false, loadingMore: false });
    }
  },
  async onReachBottom() {
    if (!this.data.hasMore || this.data.loading) {
      return;
    }
    await this.loadData(false);
  },
  async switchStatus(e) {
    const status = e.currentTarget.dataset.status || '';
    if (status === this.data.activeStatus) {
      return;
    }
    this.setData({ activeStatus: status, list: [] });
    await this.reload();
  },
  retryLoad() {
    this.reload();
  },
  goNotificationSettings() {
    wx.navigateBack({
      fail: () => wx.navigateTo({ url: '/pages/notification-settings/index' })
    });
  }
});
