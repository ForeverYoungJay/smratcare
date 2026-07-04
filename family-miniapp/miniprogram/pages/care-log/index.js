const { getCareLogs } = require('../../services/family');

Page({
  data: {
    days: [],
    loading: false,
    loadError: ''
  },
  async onShow() {
    getApp().ensureLogin();
    await this.loadData();
  },
  async onPullDownRefresh() {
    await this.loadData();
    wx.stopPullDownRefresh();
  },
  async loadData() {
    this.setData({ loading: true, loadError: '' });
    try {
      const days = await getCareLogs();
      this.setData({ days: days || [] });
    } catch (error) {
      this.setData({ days: [], loadError: error.message || '护理日志加载失败，请检查网络后重试' });
    } finally {
      this.setData({ loading: false });
    }
  },
  retryLoad() {
    this.loadData();
  },
  previewPhoto(e) {
    const day = e.currentTarget.dataset.day;
    const idx = Number(e.currentTarget.dataset.index);
    const url = e.currentTarget.dataset.url;
    const targetDay = (this.data.days || []).find((item) => item.date === day);
    const targetItem = targetDay && targetDay.items && targetDay.items[idx];
    const photos = (targetItem && targetItem.photos) || [];
    if (!url || photos.length === 0) {
      return;
    }
    wx.previewImage({
      current: url,
      urls: photos
    });
  }
});
