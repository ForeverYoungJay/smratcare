const { getTaskReceipts } = require('../../services/staff');

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
    receipts: []
  },
  onShow() {
    getApp().ensureLogin();
    this.loadData();
  },
  async loadData() {
    this.setData({ loading: true, loadError: '' });
    try {
      this.setData({ receipts: await getTaskReceipts(this.data.active) });
    } catch (error) {
      this.setData({ loadError: error.message || '执行记录加载失败' });
    } finally {
      this.setData({ loading: false });
    }
  },
  switchFilter(e) {
    this.setData({ active: e.currentTarget.dataset.value || '' });
    this.loadData();
  },
  previewPhoto(e) {
    const urls = e.currentTarget.dataset.urls || [];
    const current = e.currentTarget.dataset.current || urls[0];
    if (!urls.length) return;
    wx.previewImage({ urls, current });
  }
});
