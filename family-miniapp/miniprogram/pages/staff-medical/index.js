const { getTaskList } = require('../../services/staff');

Page({
  data: { medicationTasks: [], inspectionTasks: [] },
  onShow() {
    getApp().ensureLogin();
    this.loadData();
  },
  async loadData() {
    const [medicationTasks, inspectionTasks] = await Promise.all([
      getTaskList('MEDICATION'),
      getTaskList('INSPECTION')
    ]);
    this.setData({ medicationTasks, inspectionTasks });
  },
  goMedication() {
    wx.navigateTo({ url: '/pages/staff-clinical/index?mode=MEDICATION' });
  },
  goInspection() {
    wx.navigateTo({ url: '/pages/staff-clinical/index?mode=INSPECTION' });
  },
  openMedication(e) {
    wx.navigateTo({ url: `/pages/staff-clinical/index?mode=MEDICATION&id=${e.currentTarget.dataset.id}` });
  },
  openInspection(e) {
    wx.navigateTo({ url: `/pages/staff-clinical/index?mode=INSPECTION&id=${e.currentTarget.dataset.id}` });
  }
});
