const { getHealthDashboard } = require('../../services/family');
const { ensureSensitiveAccess } = require('../../utils/sensitive-access');

Page({
  data: {
    range: '7d',
    accessGranted: true,
    ranges: [
      { key: '7d', label: '7天' },
      { key: '30d', label: '30天' },
      { key: '90d', label: '90天' }
    ],
    board: {
      latest: {},
      trend: [],
      riskTips: []
    }
  },
  async onShow() {
    getApp().ensureLogin();
    await this.verifyAndLoad();
  },
  async verifyAndLoad() {
    const granted = await ensureSensitiveAccess({
      scene: 'health-data',
      settingKey: 'verifyHealthData',
      title: '健康档案访问验证'
    });
    if (!granted) {
      this.setData({
        accessGranted: false,
        board: {
          latest: {},
          trend: [],
          riskTips: []
        }
      });
      return;
    }
    this.setData({ accessGranted: true });
    this.loadData();
  },
  async loadData() {
    const board = await getHealthDashboard({ range: this.data.range });
    this.setData({ board: board || { latest: {}, trend: [], riskTips: [] } });
  },
  switchRange(e) {
    this.setData({ range: e.currentTarget.dataset.range });
    this.loadData();
  },
  retryVerify() {
    this.verifyAndLoad();
  },
  go(e) {
    wx.navigateTo({ url: e.currentTarget.dataset.path });
  }
});
