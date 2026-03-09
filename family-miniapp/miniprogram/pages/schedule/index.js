const { getSchedules } = require('../../services/family');

Page({
  data: {
    list: []
  },
  async onShow() {
    getApp().ensureLogin();
    const list = await getSchedules();
    this.setData({ list: list || [] });
  },
  showDetail(e) {
    const idx = Number(e.currentTarget.dataset.index);
    const item = (this.data.list || [])[idx];
    if (!item) {
      return;
    }
    const lines = [
      `时间：${item.time || '--:--'}`,
      `类型：${item.type || '护理'}`,
      `状态：${item.status || '待开始'}`,
      `执行人：${item.owner || '护理组'}`,
      `预计时长：${item.durationMinutes || 0} 分钟`,
      `说明：${item.description || '暂无补充说明'}`,
      `注意事项：${item.precautions || '按护理规范执行'}`,
      `适配建议：${item.fitNote || '按当前护理等级安排'}`
    ];
    wx.showModal({
      title: item.name || '日程详情',
      content: lines.join('\n'),
      showCancel: false,
      confirmText: '知道了'
    });
  }
});
