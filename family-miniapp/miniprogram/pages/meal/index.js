const { getMealCalendar } = require('../../services/family');

Page({
  data: {
    meal: null
  },
  async onShow() {
    getApp().ensureLogin();
    const calendar = await getMealCalendar();
    const raw = calendar && calendar.length ? calendar[0] : null;
    if (!raw) {
      this.setData({ meal: null });
      return;
    }
    this.setData({
      meal: {
        ...raw,
        breakfastText: (raw.breakfast || []).join('、'),
        lunchText: (raw.lunch || []).join('、'),
        dinnerText: (raw.dinner || []).join('、')
      }
    });
  },
  previewPhoto(e) {
    const url = e.currentTarget.dataset.url;
    const photos = (this.data.meal && this.data.meal.photos) || [];
    if (!url || photos.length === 0) {
      return;
    }
    wx.previewImage({
      current: url,
      urls: photos
    });
  }
});
