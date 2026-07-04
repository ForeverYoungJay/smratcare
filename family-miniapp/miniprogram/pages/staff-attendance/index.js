const { getAttendanceOverview } = require('../../services/staff');

Page({
  data: {
    loading: false,
    loadError: '',
    overview: null,
    month: ''
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
      this.setData({ overview: await getAttendanceOverview(this.data.month) });
    } catch (error) {
      this.setData({ loadError: error.message || '考勤加载失败' });
    } finally {
      this.setData({ loading: false });
      if (fromPullDown) wx.stopPullDownRefresh();
    }
  },
  onMonthChange(e) {
    this.setData({ month: e.detail.value, loadError: '' });
    this.loadData();
  },
  goSchedule() {
    wx.navigateTo({ url: '/pages/staff-schedule/index' });
  },
  goApproval() {
    wx.navigateTo({ url: '/pages/staff-approval/index' });
  }
});
