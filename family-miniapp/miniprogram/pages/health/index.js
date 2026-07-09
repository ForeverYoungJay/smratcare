const { getHealthDashboard } = require('../../services/family');

// 最新体征无值时回填趋势末条，仍无值显示 “--”：避免家属看到“血压 0/0”误以为出事
function pickValue(latestValue, trendValue) {
  const v = Number(latestValue);
  if (Number.isFinite(v) && v > 0) return latestValue;
  const t = Number(trendValue);
  if (Number.isFinite(t) && t > 0) return trendValue;
  return '--';
}

function normalizeBoard(board) {
  const safe = board || {};
  const latest = safe.latest || {};
  const trend = safe.trend || [];
  const last = trend.length ? trend[trend.length - 1] : {};
  return {
    ...safe,
    trend,
    riskTips: safe.riskTips || [],
    latest: {
      ...latest,
      sbp: pickValue(latest.sbp, last.sbp),
      dbp: pickValue(latest.dbp, last.dbp),
      hr: pickValue(latest.hr, last.hr),
      temp: pickValue(latest.temp, last.temp),
      sugar: pickValue(latest.sugar, last.sugar)
    }
  };
}
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
  async verifyAndLoad(force = false) {
    const granted = await ensureSensitiveAccess({
      scene: 'health-data',
      settingKey: 'verifyHealthData',
      title: '健康档案访问验证',
      force
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
    this.setData({ board: normalizeBoard(board) });
  },
  switchRange(e) {
    this.setData({ range: e.currentTarget.dataset.range });
    this.loadData();
  },
  retryVerify() {
    this.verifyAndLoad(true);
  },
  go(e) {
    wx.navigateTo({ url: e.currentTarget.dataset.path });
  }
});
