const { getTaskList } = require('../../../services/staff');

function statusTone(status) {
  const text = String(status || '');
  if (text.indexOf('超时') >= 0 || text.indexOf('逾期') >= 0 || text.indexOf('异常') >= 0) return 'danger';
  if (text.indexOf('完成') >= 0 || text.indexOf('DONE') >= 0) return 'done';
  return 'pending';
}

function normalizeTask(task = {}) {
  return {
    ...task,
    statusTone: statusTone(task.status)
  };
}

function buildOverview(tasks = []) {
  let doneCount = 0;
  let overdueCount = 0;
  tasks.forEach((item) => {
    if (item.statusTone === 'done') doneCount += 1;
    if (item.statusTone === 'danger') overdueCount += 1;
  });
  return {
    totalCount: tasks.length,
    pendingCount: tasks.length - doneCount,
    doneCount,
    overdueCount
  };
}

Page({
  data: {
    loading: false,
    loadError: '',
    tasks: [],
    overview: {
      totalCount: 0,
      pendingCount: 0,
      doneCount: 0,
      overdueCount: 0
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
      const tasks = ((await getTaskList('CARE')) || []).map(normalizeTask);
      this.setData({
        tasks,
        overview: buildOverview(tasks)
      });
    } catch (error) {
      this.setData({ loadError: error.message || '护理任务加载失败' });
    } finally {
      this.setData({ loading: false });
      if (fromPullDown) wx.stopPullDownRefresh();
    }
  },
  openCare(e) {
    wx.navigateTo({ url: `/packageStaff/pages/staff-care-execution/index?id=${e.currentTarget.dataset.id}` });
  },
  goCareExecution() {
    wx.navigateTo({ url: '/packageStaff/pages/staff-care-execution/index' });
  },
  goVitals() {
    wx.navigateTo({ url: '/packageStaff/pages/staff-vitals/index' });
  },
  goIncident() {
    wx.navigateTo({ url: '/packageStaff/pages/staff-incident/index' });
  },
  goHandover() {
    wx.navigateTo({ url: '/packageStaff/pages/staff-handover/index' });
  }
});
