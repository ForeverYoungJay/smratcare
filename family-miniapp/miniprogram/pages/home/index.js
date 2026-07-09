const { cleanVitalText } = require('../../utils/vital-text');
const { getHomeDashboard, getWeeklyBrief, getCapabilityStatus } = require('../../services/family');
const { requestSubscribeOncePerDay } = require('../../utils/subscribe');

const CAPABILITY_ALERT_STATUSES = new Set(['OFF', 'MOCK', 'BIND_REQUIRED', 'DEPRECATED']);

function countCapabilityAlerts(status) {
  if (!status || !Array.isArray(status.items)) {
    return 0;
  }
  return status.items.filter((item) => CAPABILITY_ALERT_STATUSES.has(String(item.status || '').toUpperCase())).length;
}

function buildEntries(capabilityAlertCount = 0) {
  const count = Math.max(0, Number(capabilityAlertCount) || 0);
  const entries = [
    { title: '预警中心', sub: '紧急提醒', path: '/pages/alert-center/index' },
    { title: '健康资料', sub: '档案/就医/评估', path: '/pages/health/index' },
    { title: '缴费充值', sub: '账单/余额/异常', path: '/pages/payment/index' },
    { title: '额外购买', sub: '护理服务商城', path: '/pages/service-mall/index' },
    { title: '在线沟通', sub: '图文语音', path: '/pages/communication/index' },
    { title: '紧急联系人', sub: '一键拨号', path: '/pages/emergency-contacts/index' },
    { title: '相册视频', sub: '活动/亲情互动', path: '/pages/activity-album/index' },
    { title: '更多服务', sub: '周报/日程/护理', path: '/pages/services/index' }
  ];
  if (count > 0) {
    entries.push({
      title: '系统提醒',
      sub: count > 0 ? `待处理 ${count} 项` : '链路巡检',
      path: '/pages/capability-status/index',
      badge: count > 0 ? (count > 99 ? '99+' : String(count)) : ''
    });
  }
  return entries;
}

function mapStatusClass(statusType) {
  if (statusType === 'warning') return 'pill-warning';
  if (statusType === 'danger') return 'pill-danger';
  return 'pill-normal';
}

function isBindingRequiredError(message) {
  const text = String(message || '').trim();
  return text.includes('请先绑定老人信息');
}

function isInvalidSelectedElderError(message) {
  const text = String(message || '').trim();
  return text.includes('无权限查看该老人信息');
}

Page({
  data: {
    loading: false,
    loadError: '',
    elderIndex: 0,
    elderOptions: [],
    elders: [],
    dashboard: {
      healthSummary: { metrics: [] },
      schedules: [],
      meal: { tags: [], breakfastText: '', lunchText: '', dinnerText: '' },
      notices: [],
      focusEvents: [],
      careTimeline: []
    },
    weeklyBrief: null,
    currentTime: '',
    entries: buildEntries(0),
    capabilityAlertCount: 0,
    runtimeNotice: ''
  },
  onLoad() {
    this.setData({ runtimeNotice: getApp().globalData.runtimeNotice || '' });
    this.loadData();
    this.setData({ currentTime: this.formatNow() });
  },
  onShow() {
    getApp().ensureLogin();
    if (getApp().globalData.userType === 'staff') {
      wx.reLaunch({ url: '/packageStaff/pages/staff-home/index' });
      return;
    }
    const app = getApp();
    if (app.globalData.refreshHomeAfterBinding) {
      app.globalData.refreshHomeAfterBinding = false;
      this.loadData();
      return;
    }
    this.loadCapabilityStatus();
    // 每天最多请求一次家属端通用提醒订阅授权（健康预警/费用/紧急事件）
    requestSubscribeOncePerDay(['familyGeneral', 'familyHealth']);
  },
  onPullDownRefresh() {
    this.loadData(true);
  },
  async loadData(fromPullDown = false, hasRetried = false) {
    this.setData({ loading: true, loadError: '' });
    try {
      const raw = await getHomeDashboard({ elderId: null });
      const elders = (raw.elders || []).map((item) => ({
        ...item,
        avatarText: item.elderName ? item.elderName.slice(0, 1) : '长',
        statusClass: mapStatusClass(item.statusType),
        dynamicPreview: cleanVitalText(item.dynamicPreview)
      }));
      const meal = raw.meal || {};
      const schedules = (raw.schedules || []).slice(0, 4);
      const notices = (raw.notices || []).slice(0, 3);
      const careTimeline = (raw.careTimeline || []).slice(0, 6).map((item) => ({
        ...item,
        content: cleanVitalText(item.content)
      }));
      const normalized = {
        ...raw,
        schedules,
        notices,
        careTimeline,
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
      const selectedElderId = elders[elderIndex] ? elders[elderIndex].elderId : null;
      app.globalData.selectedElderId = selectedElderId;
      const weeklyBrief = selectedElderId ? await getWeeklyBrief({ elderId: selectedElderId }) : null;
      this.setData({
        elders,
        elderOptions,
        dashboard: normalized,
        elderIndex,
        weeklyBrief
      });
      this.loadCapabilityStatus();
    } catch (error) {
      const message = error && error.message ? error.message : '';
      if (!hasRetried && isInvalidSelectedElderError(message)) {
        getApp().globalData.selectedElderId = null;
        this.setData({ elderIndex: 0 });
        await this.loadData(fromPullDown, true);
        return;
      }
      if (isBindingRequiredError(message)) {
        getApp().globalData.selectedElderId = null;
        this.setData({
          elders: [],
          elderOptions: [],
          elderIndex: 0,
          dashboard: {
            healthSummary: { metrics: [] },
            schedules: [],
            meal: { tags: [], breakfastText: '', lunchText: '', dinnerText: '' },
            notices: [],
            focusEvents: [],
            careTimeline: []
          },
          weeklyBrief: null,
          loadError: ''
        });
        return;
      }
      this.setData({
        loadError: message || '首页信息加载失败，请稍后重试'
      });
    } finally {
      this.setData({ loading: false });
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
    const elder = this.data.elders[this.data.elderIndex];
    const weeklyBrief = elder ? await getWeeklyBrief({ elderId: elder.elderId }) : null;
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
  retryLoad() {
    this.loadData();
  },
  formatNow() {
    const now = new Date();
    const pad = (v) => (v < 10 ? `0${v}` : `${v}`);
    return `${pad(now.getHours())}:${pad(now.getMinutes())}`;
  }
});
