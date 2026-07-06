const { getTaskList } = require('../../../services/staff');

function isDoneStatus(status) {
  const text = String(status || '');
  return text.indexOf('完成') >= 0 || text.indexOf('DONE') >= 0;
}

Page({
  data: {
    loading: false,
    loadError: '',
    activeTab: 'LOGISTICS',
    maintenanceTasks: [],
    mealTasks: [],
    maintenancePending: 0,
    mealPending: 0
  },
  onLoad(options = {}) {
    const module = String(options.module || '').toUpperCase();
    if (module === 'MEAL') {
      this.setData({ activeTab: 'MEAL' });
    }
  },
  onShow() {
    getApp().ensureLogin();
    this.loadData();
  },
  onPullDownRefresh() {
    this.loadData(true);
  },
  async loadData(fromPullDown = false) {
    this.setData({ loading: true, loadError: '' });
    try {
      const [maintenanceTasks, mealTasks] = await Promise.all([
        getTaskList('LOGISTICS'),
        getTaskList('MEAL')
      ]);
      this.setData({
        maintenanceTasks: maintenanceTasks || [],
        mealTasks: mealTasks || [],
        maintenancePending: (maintenanceTasks || []).filter((item) => !isDoneStatus(item.status)).length,
        mealPending: (mealTasks || []).filter((item) => !isDoneStatus(item.status)).length
      });
    } catch (error) {
      this.setData({ loadError: error.message || '后勤任务加载失败' });
    } finally {
      this.setData({ loading: false });
      if (fromPullDown) wx.stopPullDownRefresh();
    }
  },
  switchTab(e) {
    const tab = e.currentTarget.dataset.tab;
    if (!tab || tab === this.data.activeTab) return;
    this.setData({ activeTab: tab });
  },
  openRepair(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/packageStaff/pages/staff-repairs/index${id ? `?id=${encodeURIComponent(id)}` : ''}` });
  },
  openMeal(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/packageStaff/pages/staff-meals/index${id ? `?id=${encodeURIComponent(id)}` : ''}` });
  },
  goMaterial() {
    wx.navigateTo({ url: '/packageStaff/pages/staff-material/index' });
  },
  goMeals() {
    wx.navigateTo({ url: '/packageStaff/pages/staff-meals/index' });
  },
  goRepairs() {
    wx.navigateTo({ url: '/packageStaff/pages/staff-repairs/index' });
  }
});
