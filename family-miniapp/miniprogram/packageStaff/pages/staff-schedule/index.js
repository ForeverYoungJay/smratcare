const { getSchedule, getAttendanceOverview, punchAttendance } = require('../../../services/staff');

function pad2(value) {
  return String(value).padStart(2, '0');
}

// 由排班列表构建当月月历：周日起始 7 列网格，标记有班次的日期与今天。
function buildCalendar(schedule = []) {
  const now = new Date();
  const year = now.getFullYear();
  const month = now.getMonth();
  const todayStr = `${year}-${pad2(month + 1)}-${pad2(now.getDate())}`;
  const shiftByDate = {};
  (schedule || []).forEach((item) => {
    const date = String(item.date || '').slice(0, 10);
    if (!date) return;
    if (!shiftByDate[date]) shiftByDate[date] = [];
    shiftByDate[date].push(item);
  });

  const firstWeekday = new Date(year, month, 1).getDay();
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const cells = [];
  for (let i = 0; i < firstWeekday; i++) cells.push({ key: `blank-${i}`, blank: true });
  for (let day = 1; day <= daysInMonth; day++) {
    const dateStr = `${year}-${pad2(month + 1)}-${pad2(day)}`;
    const items = shiftByDate[dateStr] || [];
    cells.push({
      key: dateStr,
      blank: false,
      day,
      dateStr,
      isToday: dateStr === todayStr,
      hasShift: items.length > 0,
      shiftLabel: items.length ? String(items[0].shift || '班').slice(0, 2) : ''
    });
  }
  while (cells.length % 7 !== 0) cells.push({ key: `tail-${cells.length}`, blank: true });
  return {
    monthLabel: `${year}年${month + 1}月`,
    cells,
    todayStr,
    shiftByDate
  };
}

Page({
  data: {
    schedule: [],
    viewMode: 'calendar',
    calendar: { monthLabel: '', cells: [] },
    weekdays: ['日', '一', '二', '三', '四', '五', '六'],
    selectedDate: '',
    selectedItems: [],
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
    loadError: '',
    punchError: '',
    punchSuccess: ''
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
      const calendar = buildCalendar(schedule);
      const selectedDate = this.data.selectedDate || calendar.todayStr;
      this.shiftByDate = calendar.shiftByDate;
      this.setData({
        schedule,
        attendance,
        calendar: { monthLabel: calendar.monthLabel, cells: calendar.cells },
        selectedDate,
        selectedItems: calendar.shiftByDate[selectedDate] || [],
        punchError: ''
      });
    } catch (error) {
      this.setData({ loadError: error.message || '排班加载失败' });
    } finally {
      this.setData({ loading: false });
    }
  },
  switchView(e) {
    this.setData({ viewMode: e.currentTarget.dataset.mode });
  },
  pickDay(e) {
    const dateStr = e.currentTarget.dataset.date;
    if (!dateStr) return;
    this.setData({
      selectedDate: dateStr,
      selectedItems: (this.shiftByDate && this.shiftByDate[dateStr]) || []
    });
  },
  goShiftSwap() {
    wx.navigateTo({ url: '/packageStaff/pages/staff-approval/index?type=SHIFT_CHANGE' });
  },
  async punch(e) {
    if (this.data.punching) return;
    const action = e.currentTarget.dataset.action;
    this.setData({ punching: true, punchError: '', punchSuccess: '' });
    try {
      await punchAttendance(action);
      const actionLabel = (this.data.punchActions.find((item) => item.action === action) || {}).label || '打卡';
      wx.showToast({ title: '打卡成功', icon: 'success' });
      await this.loadData();
      this.setData({ punchSuccess: `${actionLabel}已提交，可在今日考勤中查看最新状态。` });
    } catch (error) {
      this.setData({ punchError: error.message || '打卡失败，请稍后重试' });
      wx.showToast({ title: error.message || '打卡失败', icon: 'none' });
    } finally {
      this.setData({ punching: false });
    }
  }
});
