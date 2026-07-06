Page({
  data: {
    record: null,
    expired: false
  },
  onLoad(query) {
    getApp().ensureLogin();
    const id = query && query.id ? decodeURIComponent(query.id) : '';
    const cached = getApp().globalData.medicalRecordDetailCache;
    if (cached && (!id || String(cached.id) === String(id))) {
      this.setData({ record: cached, expired: false });
    } else {
      this.setData({ record: null, expired: true });
    }
  },
  onUnload() {
    // 详情缓存用完即清，避免误读到旧记录
    if (getApp().globalData) {
      getApp().globalData.medicalRecordDetailCache = null;
    }
  },
  goBack() {
    wx.navigateBack({ delta: 1 });
  }
});
