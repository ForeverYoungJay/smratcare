const { getWeeklyBrief, getWeeklyBriefHistory } = require('../../services/family');

function overallClass(level) {
  if (level === 'warning') return 'overall-warning';
  if (level === 'danger') return 'overall-danger';
  return 'overall-normal';
}

function badgeClass(status) {
  if (status === '需关注') return 'badge-warning';
  if (status === '异常') return 'badge-danger';
  return 'badge-normal';
}

Page({
  data: {
    brief: null,
    history: [],
    loading: false
  },
  onLoad(query) {
    const elderId = Number(query.elderId || 0);
    if (elderId) {
      getApp().globalData.selectedElderId = elderId;
    }
  },
  onShow() {
    getApp().ensureLogin();
    this.loadData();
  },
  onShareAppMessage() {
    const brief = this.data.brief;
    if (!brief) {
      return {
        title: '家属周报',
        path: '/pages/weekly-brief/index'
      };
    }
    return {
      title: `${brief.elderName}本周概览：异常${brief.abnormalCount}项，护理${brief.nursingLogCount}条`,
      path: `/pages/weekly-brief/index?elderId=${brief.elderId || ''}`
    };
  },
  onPullDownRefresh() {
    this.loadData(true);
  },
  async loadData(fromPullDown = false) {
    this.setData({ loading: true });
    try {
      const [brief, history] = await Promise.all([
        getWeeklyBrief(),
        getWeeklyBriefHistory({ weeks: 8 })
      ]);
      const vitalBadges = (brief.vitalBadges || []).map((item) => ({
        ...item,
        statusClass: badgeClass(item.status)
      }));
      const historyList = (history || []).map((item) => ({
        ...item,
        overallClass: overallClass(item.overallLevel)
      }));
      this.setData({
        brief: {
          ...brief,
          overallClass: overallClass(brief.overallLevel),
          vitalBadges
        },
        history: historyList
      });
    } finally {
      this.setData({ loading: false });
      if (fromPullDown) {
        wx.stopPullDownRefresh();
      }
    }
  },
  toHealth() {
    wx.navigateTo({ url: '/pages/health/index' });
  },
  toTodoCenter() {
    wx.navigateTo({ url: '/pages/todo-center/index' });
  },
  toCommunication() {
    wx.navigateTo({ url: '/pages/communication/index' });
  },
  shareBriefText() {
    const brief = this.data.brief;
    if (!brief) {
      wx.showToast({ title: '暂无周报可分享', icon: 'none' });
      return;
    }
    const lines = [
      `【${brief.elderName}家属周报】${brief.weekRange}`,
      `总体状态：${brief.overallText}`,
      `健康检测 ${brief.healthCheckCount} 次，异常 ${brief.abnormalCount} 项，护理日志 ${brief.nursingLogCount} 条，活动 ${brief.activityCount} 次`,
      `未读提醒 ${brief.unreadMessageCount} 条，建议优先处理紧急消息`,
      `建议：${(brief.suggestions || []).slice(0, 2).join('；') || '暂无'}`
    ];
    wx.setClipboardData({
      data: lines.join('\n'),
      success: () => wx.showToast({ title: '周报摘要已复制', icon: 'none' })
    });
  },
  onHistoryTap(e) {
    const idx = Number(e.currentTarget.dataset.index);
    const item = this.data.history[idx];
    if (!item) {
      return;
    }
    wx.showModal({
      title: item.weekRange,
      content: `${item.quickText}\\n健康检测 ${item.healthCheckCount} 次，异常 ${item.abnormalCount} 项，护理 ${item.nursingLogCount} 条`,
      showCancel: false
    });
  }
});
