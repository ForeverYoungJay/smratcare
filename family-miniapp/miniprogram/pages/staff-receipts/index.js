const { getTaskReceipts } = require('../../services/staff');

const RENDER_PAGE_SIZE = 20;

Page({
  data: {
    loading: false,
    loadError: '',
    active: '',
    filters: [
      { label: '全部', value: '' },
      { label: '护理', value: 'CARE' },
      { label: '用药', value: 'MEDICATION' },
      { label: '巡检', value: 'INSPECTION' },
      { label: '后勤', value: 'LOGISTICS' },
      { label: '送餐', value: 'MEAL' }
    ],
    receipts: [],
    hasMore: false
  },
  onShow() {
    getApp().ensureLogin();
    this.loadData();
  },
  onPullDownRefresh() {
    this.loadData(true);
  },
  onReachBottom() {
    this.renderMore();
  },
  async loadData(fromPullDown = false) {
    this.setData({ loading: true, loadError: '' });
    try {
      // 接口一次性返回全部回执，前端分批渲染，避免长列表卡顿。
      this.allReceipts = (await getTaskReceipts(this.data.active)) || [];
      this.setData({
        receipts: this.allReceipts.slice(0, RENDER_PAGE_SIZE),
        hasMore: this.allReceipts.length > RENDER_PAGE_SIZE
      });
    } catch (error) {
      this.setData({ loadError: error.message || '执行记录加载失败' });
    } finally {
      this.setData({ loading: false });
      if (fromPullDown) wx.stopPullDownRefresh();
    }
  },
  renderMore() {
    const all = this.allReceipts || [];
    const rendered = this.data.receipts.length;
    if (rendered >= all.length) return;
    const next = all.slice(0, rendered + RENDER_PAGE_SIZE);
    this.setData({
      receipts: next,
      hasMore: next.length < all.length
    });
  },
  switchFilter(e) {
    this.setData({
      active: e.currentTarget.dataset.value || '',
      loadError: ''
    });
    this.loadData();
  },
  previewPhoto(e) {
    const urls = e.currentTarget.dataset.urls || [];
    const current = e.currentTarget.dataset.current || urls[0];
    if (!urls.length) return;
    wx.previewImage({ urls, current });
  }
});
