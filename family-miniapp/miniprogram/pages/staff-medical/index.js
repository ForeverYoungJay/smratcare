const { getTaskList } = require('../../services/staff');

function isDoneStatus(status) {
  const text = String(status || '');
  return text.indexOf('完成') >= 0 || text.indexOf('DONE') >= 0;
}

Page({
  data: {
    loading: false,
    loadError: '',
    activeTab: 'MEDICATION',
    medicationTasks: [],
    inspectionTasks: [],
    medicationPending: 0,
    inspectionPending: 0
  },
  onLoad(options = {}) {
    const module = String(options.module || '').toUpperCase();
    if (module === 'INSPECTION') {
      this.setData({ activeTab: 'INSPECTION' });
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
      const [medicationTasks, inspectionTasks] = await Promise.all([
        getTaskList('MEDICATION'),
        getTaskList('INSPECTION')
      ]);
      this.setData({
        medicationTasks: medicationTasks || [],
        inspectionTasks: inspectionTasks || [],
        medicationPending: (medicationTasks || []).filter((item) => !isDoneStatus(item.status)).length,
        inspectionPending: (inspectionTasks || []).filter((item) => !isDoneStatus(item.status)).length
      });
    } catch (error) {
      this.setData({ loadError: error.message || '医护任务加载失败' });
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
