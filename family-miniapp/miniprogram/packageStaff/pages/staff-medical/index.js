const { getTaskList } = require('../../../services/staff');

const PREVIEW_LIMIT = 5;

function isDoneStatus(status) {
  const text = String(status || '');
  return text.indexOf('完成') >= 0 || text.indexOf('DONE') >= 0;
}

Page({
  data: {
    loading: false,
    loadError: '',
    medicationPreview: [],
    inspectionPreview: [],
    medicationTotal: 0,
    inspectionTotal: 0,
    medicationPending: 0,
    inspectionPending: 0
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
      const med = medicationTasks || [];
      const insp = inspectionTasks || [];
      this.setData({
        medicationTotal: med.length,
        inspectionTotal: insp.length,
        medicationPreview: med.slice(0, PREVIEW_LIMIT),
        inspectionPreview: insp.slice(0, PREVIEW_LIMIT),
        medicationPending: med.filter((item) => !isDoneStatus(item.status)).length,
        inspectionPending: insp.filter((item) => !isDoneStatus(item.status)).length
      });
    } catch (error) {
      this.setData({ loadError: error.message || '医护任务加载失败' });
    } finally {
      this.setData({ loading: false });
      if (fromPullDown) wx.stopPullDownRefresh();
    }
  },
  goMedication() {
    wx.navigateTo({ url: '/packageStaff/pages/staff-clinical/index?mode=MEDICATION' });
  },
  goInspection() {
    wx.navigateTo({ url: '/packageStaff/pages/staff-clinical/index?mode=INSPECTION' });
  },
  openMedication(e) {
    wx.navigateTo({ url: `/packageStaff/pages/staff-clinical/index?mode=MEDICATION&id=${e.currentTarget.dataset.id}` });
  },
  openInspection(e) {
    wx.navigateTo({ url: `/packageStaff/pages/staff-clinical/index?mode=INSPECTION&id=${e.currentTarget.dataset.id}` });
  }
});
