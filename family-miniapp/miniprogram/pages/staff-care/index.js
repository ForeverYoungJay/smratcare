const { getTaskList } = require('../../services/staff');

Page({
  data: { tasks: [] },
  onShow() {
    getApp().ensureLogin();
    this.loadData();
  },
  async loadData() {
    this.setData({ tasks: await getTaskList('CARE') });
  },
  openCare(e) {
    wx.navigateTo({ url: `/pages/staff-care-execution/index?id=${e.currentTarget.dataset.id}` });
  },
  goCareExecution() {
    wx.navigateTo({ url: '/pages/staff-care-execution/index' });
  },
  goVitals() {
    wx.navigateTo({ url: '/pages/staff-vitals/index' });
  }
});
