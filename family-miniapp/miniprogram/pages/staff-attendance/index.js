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
  async loadData() {
    this.setData({ loading: true, loadError: '' });
    try {
      this.setData({ overview: await getAttendanceOverview(this.data.month) });
    } catch (error) {
      this.setData({ loadError: error.message || '考勤加载失败' });
    } finally {
      this.setData({ loading: false });
    }
  },
  onMonthChange(e) {
    this.setData({ month: e.detail.value });
    this.loadData();
  }
});
