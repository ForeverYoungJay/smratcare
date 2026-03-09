const { getOutingRecords } = require('../../services/family');

Page({
  data: {
    list: []
  },
  async onShow() {
    getApp().ensureLogin();
    const list = await getOutingRecords();
    this.setData({ list: list || [] });
  }
});
