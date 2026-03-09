const { getHomeDashboard, getWeeklyBrief, getCapabilityStatus } = require('../../services/family');

const CAPABILITY_ALERT_STATUSES = new Set(['OFF', 'MOCK', 'BIND_REQUIRED', 'DEPRECATED']);

function countCapabilityAlerts(status) {
  if (!status || !Array.isArray(status.items)) {
    return 0;
  }
  return status.items.filter((item) => CAPABILITY_ALERT_STATUSES.has(String(item.status || '').toUpperCase())).length;
}

function buildEntries(capabilityAlertCount = 0) {
  const count = Math.max(0, Number(capabilityAlertCount) || 0);
  return [
    { title: '家属周报', sub: '一周总览', path: '/pages/weekly-brief/index' },
    { title: '预警中心', sub: '紧急提醒', path: '/pages/alert-center/index' },
    { title: '待办中心', sub: '今日待处理', path: '/pages/todo-center/index' },
    { title: '健康档案', sub: '趋势看板', path: '/pages/health/index' },
    { title: '就医记录', sub: '门诊与医嘱', path: '/pages/medical-record/index' },
    { title: '评估报告', sub: 'PDF下载', path: '/pages/assessment-report/index' },
    { title: '在线缴费', sub: '账单与余额', path: '/pages/payment/index' },
    { title: '支付保障', sub: '充值与异常', path: '/pages/payment-guard/index' },
    { title: '服务增购', sub: '护理商城', path: '/pages/service-mall/index' },
    { title: '在线沟通', sub: '图文语音', path: '/pages/communication/index' },
    { title: '护理日志', sub: '执行追踪', path: '/pages/care-log/index' },
    { title: '活动相册', sub: '点赞互动', path: '/pages/activity-album/index' },
    { title: '膳食日历', sub: '三餐详情', path: '/pages/diet-calendar/index' },
    { title: '外出记录', sub: '安全轨迹', path: '/pages/outing-record/index' },
    { title: '紧急联系人', sub: '一键拨号', path: '/pages/emergency-contacts/index' },
    { title: '亲情互动', sub: '照片语音', path: '/pages/affection/index' },
    {
      title: '能力状态',
      sub: count > 0 ? `待处理 ${count} 项` : '链路巡检',
      path: '/pages/capability-status/index',
      badge: count > 0 ? (count > 99 ? '99+' : String(count)) : ''
    }
  ];
}

function mapStatusClass(statusType) {
  if (statusType === 'warning') return 'pill-warning';
  if (statusType === 'danger') return 'pill-danger';
  return 'pill-normal';
}

Page({
  data: {
    elderIndex: 0,
    elderOptions: [],
    elders: [],
    dashboard: {
      healthSummary: { metrics: [] },
      schedules: [],
      meal: { tags: [], breakfastText: '', lunchText: '', dinnerText: '' },
      notices: [],
      focusEvents: []
    },
    weeklyBrief: null,
    currentTime: '',
    entries: buildEntries(0),
    capabilityAlertCount: 0
  },
  onLoad() {
    this.loadData();
    this.setData({ currentTime: this.formatNow() });
  },
  onShow() {
    getApp().ensureLogin();
    this.loadCapabilityStatus();
  },
  onPullDownRefresh() {
    this.loadData(true);
  },
  async loadData(fromPullDown = false) {
    try {
      const [raw, weeklyBrief] = await Promise.all([getHomeDashboard(), getWeeklyBrief()]);
      const elders = (raw.elders || []).map((item) => ({
        ...item,
        avatarText: item.elderName ? item.elderName.slice(0, 1) : '长',
        statusClass: mapStatusClass(item.statusType)
      }));
      const meal = raw.meal || {};
      const schedules = (raw.schedules || []).slice(0, 4);
      const notices = (raw.notices || []).slice(0, 3);
      const normalized = {
        ...raw,
        schedules,
        notices,
        meal: {
          ...meal,
          breakfastText: (meal.breakfast || []).join('、'),
          lunchText: (meal.lunch || []).join('、'),
          dinnerText: (meal.dinner || []).join('、')
        }
      };
      const elderOptions = elders.map((item) => `${item.elderName} · ${item.roomNo}`);
      const app = getApp();
      let elderIndex = this.data.elderIndex;
      if (app.globalData.selectedElderId) {
        const idx = elders.findIndex((item) => item.elderId === app.globalData.selectedElderId);
        elderIndex = idx >= 0 ? idx : 0;
      }
      app.globalData.selectedElderId = elders[elderIndex] ? elders[elderIndex].elderId : null;
      this.setData({ elders, elderOptions, dashboard: normalized, elderIndex, weeklyBrief });
      this.loadCapabilityStatus();
    } finally {
      if (fromPullDown) {
        wx.stopPullDownRefresh();
      }
    }
  },
  async loadCapabilityStatus() {
    try {
      const status = await getCapabilityStatus();
      const capabilityAlertCount = countCapabilityAlerts(status);
      getApp().updateCapabilityAlerts(capabilityAlertCount);
      this.setData({
        capabilityAlertCount,
        entries: buildEntries(capabilityAlertCount)
      });
    } catch (error) {
      // Ignore capability status errors on home to avoid interrupting core data flow.
    }
  },
  async loadWeeklyBrief() {
    const weeklyBrief = await getWeeklyBrief();
    this.setData({ weeklyBrief });
  },
  onElderChange(e) {
    const index = e.detail.current;
    this.setData({ elderIndex: index });
    const elder = this.data.elders[index];
    if (elder) {
      getApp().globalData.selectedElderId = elder.elderId;
      this.loadWeeklyBrief();
    }
  },
  onElderPick(e) {
    const index = Number(e.detail.value || 0);
    this.setData({ elderIndex: index });
    const elder = this.data.elders[index];
    if (elder) {
      getApp().globalData.selectedElderId = elder.elderId;
      this.loadWeeklyBrief();
    }
  },
  goBind() {
    wx.navigateTo({ url: '/pages/bind/index' });
  },
  goPage(e) {
    const path = e.currentTarget.dataset.path;
    wx.navigateTo({ url: path });
  },
  goMessageDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/message-detail/index?id=${id}` });
  },
  formatNow() {
    const now = new Date();
    const pad = (v) => (v < 10 ? `0${v}` : `${v}`);
    return `${pad(now.getHours())}:${pad(now.getMinutes())}`;
  }
});
