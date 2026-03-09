const { getMyElders, getProfile, getCapabilityStatus } = require('../../services/family');

const CAPABILITY_ALERT_STATUSES = new Set(['OFF', 'MOCK', 'BIND_REQUIRED', 'DEPRECATED']);

function countCapabilityAlerts(status) {
  if (!status || !Array.isArray(status.items)) {
    return 0;
  }
  return status.items.filter((item) => CAPABILITY_ALERT_STATUSES.has(String(item.status || '').toUpperCase())).length;
}

Page({
  data: {
    elderCount: 0,
    profile: null,
    capabilityAlertCount: 0
  },
  onShow() {
    getApp().ensureLogin();
    this.loadData();
  },
  async loadData() {
    const [elders, profile, capabilityStatus] = await Promise.all([
      getMyElders(),
      getProfile(),
      getCapabilityStatus().catch(() => null)
    ]);
    const capabilityAlertCount = countCapabilityAlerts(capabilityStatus);
    getApp().updateCapabilityAlerts(capabilityAlertCount);
    this.setData({
      elderCount: (elders || []).length,
      profile,
      capabilityAlertCount
    });
  },
  go(e) {
    wx.navigateTo({ url: e.currentTarget.dataset.path });
  },
  logout() {
    getApp().logout();
  }
});
