const { getCareLogs } = require('../../services/family');

// 后端历史数据可能携带内部枚举，家属端展示前统一兜底翻译（后端已同步修复，此处双保险）
const LOG_TYPE_LABELS = {
  ROUTINE: '日常护理',
  INSPECTION_FOLLOW_UP: '巡检跟进',
  INSPECTION_FOLLOW: '巡检跟进',
  INCIDENT: '异常处理',
  MEDICAL_FOLLOW_UP: '医护随访'
};
const LOG_STATUS_LABELS = { PENDING: '进行中', DONE: '已完成', COMPLETED: '已完成', CLOSED: '已关闭' };

function localizeDays(days) {
  return (days || []).map((day) => ({
    ...day,
    items: (day.items || []).map((item) => ({
      ...item,
      project: LOG_TYPE_LABELS[String(item.project || '').toUpperCase()] || item.project,
      status: LOG_STATUS_LABELS[String(item.status || '').toUpperCase()] || item.status
    }))
  }));
}

Page({
  data: {
    days: [],
    loading: false,
    loadError: ''
  },
  async onShow() {
    getApp().ensureLogin();
    await this.loadData();
  },
  async onPullDownRefresh() {
    await this.loadData();
    wx.stopPullDownRefresh();
  },
  async loadData() {
    this.setData({ loading: true, loadError: '' });
    try {
      const days = await getCareLogs();
      this.setData({ days: localizeDays(days) });
    } catch (error) {
      this.setData({ days: [], loadError: error.message || '护理日志加载失败，请检查网络后重试' });
    } finally {
      this.setData({ loading: false });
    }
  },
  retryLoad() {
    this.loadData();
  },
  previewPhoto(e) {
    const day = e.currentTarget.dataset.day;
    const idx = Number(e.currentTarget.dataset.index);
    const url = e.currentTarget.dataset.url;
    const targetDay = (this.data.days || []).find((item) => item.date === day);
    const targetItem = targetDay && targetDay.items && targetDay.items[idx];
    const photos = (targetItem && targetItem.photos) || [];
    if (!url || photos.length === 0) {
      return;
    }
    wx.previewImage({
      current: url,
      urls: photos
    });
  }
});
