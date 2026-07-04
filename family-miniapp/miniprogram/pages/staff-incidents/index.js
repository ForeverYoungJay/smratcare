const { getIncidents } = require('../../services/staff');

const RENDER_PAGE_SIZE = 20;

Page({
  data: {
    loading: false,
    loadError: '',
    lastFilterSummary: '',
    activeStatus: '',
    activeLevel: '',
    statusFilters: [
      { label: '全部', value: '' },
      { label: '待处理', value: 'OPEN' },
      { label: '处理中', value: 'PROCESSING' },
      { label: '已关闭', value: 'CLOSED' }
    ],
    levelFilters: [
      { label: '全部等级', value: '' },
      { label: '一般', value: '一般' },
      { label: '紧急', value: '紧急' },
      { label: '重大', value: '重大' }
    ],
    incidents: [],
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
      // 接口一次性返回全部异常记录，前端分批渲染，避免长列表卡顿。
      this.allIncidents = (await getIncidents({
        status: this.data.activeStatus,
        level: this.data.activeLevel
      })) || [];
      this.setData({
        incidents: this.allIncidents.slice(0, RENDER_PAGE_SIZE),
        hasMore: this.allIncidents.length > RENDER_PAGE_SIZE,
        lastFilterSummary: `${this.data.activeStatus || '全部状态'} · ${this.data.activeLevel || '全部等级'}`
      });
    } catch (error) {
      this.setData({ loadError: error.message || '异常记录加载失败' });
    } finally {
      this.setData({ loading: false });
      if (fromPullDown) wx.stopPullDownRefresh();
    }
  },
  renderMore() {
    const all = this.allIncidents || [];
    const rendered = this.data.incidents.length;
    if (rendered >= all.length) return;
    const next = all.slice(0, rendered + RENDER_PAGE_SIZE);
    this.setData({
      incidents: next,
      hasMore: next.length < all.length
    });
  },
  switchStatus(e) {
    this.setData({
      activeStatus: e.currentTarget.dataset.value || '',
      loadError: ''
    });
    this.loadData();
  },
  switchLevel(e) {
    this.setData({
      activeLevel: e.currentTarget.dataset.value || '',
      loadError: ''
    });
    this.loadData();
  },
  previewPhoto(e) {
    const urls = e.currentTarget.dataset.urls || [];
    const current = e.currentTarget.dataset.current || urls[0];
    if (!urls.length) return;
    wx.previewImage({ urls, current });
  },
  goReport() {
    wx.navigateTo({ url: '/pages/staff-incident/index' });
  }
});
