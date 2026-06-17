const { getSchedule, getAttendanceOverview, punchAttendance } = require('../../services/staff');

Page({
  data: {
    schedule: [],
    attendance: null,
    punchActions: [
      { label: '上班打卡', action: 'IN' },
      { label: '下班打卡', action: 'OUT' },
      { label: '开始午休', action: 'START_LUNCH' },
      { label: '结束午休', action: 'END_LUNCH' },
      { label: '外出开始', action: 'START_OUTING' },
      { label: '外出结束', action: 'END_OUTING' }
    ],
    loading: true,
    punching: false,
    loadError: ''
  },
  onShow() {
    getApp().ensureLogin();
    this.loadData();
  },
  async loadData() {
    this.setData({ loading: true, loadError: '' });
    try {
      const [schedule, attendance] = await Promise.all([
        getSchedule(),
        getAttendanceOverview()
      ]);
      this.setData({ schedule, attendance });
    } catch (error) {
      this.setData({ loadError: error.message || '排班加载失败' });
    } finally {
      this.setData({ loading: false });
    }
  },
  async punch(e) {
    if (this.data.punching) return;
    const action = e.currentTarget.dataset.action;
    this.setData({ punching: true });
    try {
      await punchAttendance(action);
      wx.showToast({ title: '打卡成功', icon: 'success' });
      await this.loadData();
    } catch (error) {
      wx.showToast({ title: error.message || '打卡失败', icon: 'none' });
    } finally {
      this.setData({ punching: false });
    }
  }
});
