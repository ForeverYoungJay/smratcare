const { getTaskList } = require('../../services/staff');

Page({
  data: { maintenanceTasks: [], mealTasks: [] },
  onShow() {
    getApp().ensureLogin();
    this.loadData();
  },
  async loadData() {
    const [maintenanceTasks, mealTasks] = await Promise.all([
      getTaskList('LOGISTICS'),
      getTaskList('MEAL')
    ]);
    this.setData({ maintenanceTasks, mealTasks });
  },
  openTask(e) {
    wx.navigateTo({ url: `/pages/staff-task-detail/index?id=${e.currentTarget.dataset.id}` });
  },
  goMaterial() {
    wx.navigateTo({ url: '/pages/staff-material/index' });
  },
  goMeals() {
    wx.navigateTo({ url: '/pages/staff-meals/index' });
  },
  goRepairs() {
    wx.navigateTo({ url: '/pages/staff-repairs/index' });
  }
});
