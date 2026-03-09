Page({
  onShow() {
    getApp().ensureLogin();
  },
  go(e) {
    wx.navigateTo({ url: e.currentTarget.dataset.path });
  }
});
