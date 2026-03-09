const { getMealCalendar } = require('../../services/family');

Page({
  data: {
    list: []
  },
  async onShow() {
    getApp().ensureLogin();
    const list = await getMealCalendar();
    this.setData({
      list: (list || []).map((item) => ({
        ...item,
        breakfastText: (item.breakfast || []).join('、'),
        lunchText: (item.lunch || []).join('、'),
        dinnerText: (item.dinner || []).join('、')
      }))
    });
  },
  previewPhoto(e) {
    const date = e.currentTarget.dataset.date;
    const url = e.currentTarget.dataset.url;
    const found = (this.data.list || []).find((item) => item.date === date);
    const photos = (found && found.photos) || [];
    if (!url || photos.length === 0) {
      return;
    }
    wx.previewImage({
      current: url,
      urls: photos
    });
  }
});
