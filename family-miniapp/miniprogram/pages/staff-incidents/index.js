const { getIncidents } = require('../../services/staff');

Page({
  data: {
    loading: false,
    loadError: '',
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
    incidents: []
  },
  onShow() {
    getApp().ensureLogin();
    this.loadData();
  },
  async loadData() {
    this.setData({ loading: true, loadError: '' });
    try {
      const incidents = await getIncidents({
        status: this.data.activeStatus,
        level: this.data.activeLevel
      });
      this.setData({ incidents });
    } catch (error) {
      this.setData({ loadError: error.message || '异常记录加载失败' });
    } finally {
      this.setData({ loading: false });
    }
  },
  switchStatus(e) {
    this.setData({ activeStatus: e.currentTarget.dataset.value || '' });
    this.loadData();
  },
  switchLevel(e) {
    this.setData({ activeLevel: e.currentTarget.dataset.value || '' });
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
