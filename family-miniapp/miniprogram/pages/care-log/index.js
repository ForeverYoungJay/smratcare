const { getCareLogs } = require('../../services/family');

Page({
  data: {
    days: []
  },
  async onShow() {
    getApp().ensureLogin();
    const days = await getCareLogs();
    this.setData({ days: days || [] });
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
