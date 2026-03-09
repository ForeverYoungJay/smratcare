const { getAssessmentReports, generateAiAssessmentReports } = require('../../services/family');

Page({
  data: {
    list: [],
    generating: false
  },
  async onShow() {
    getApp().ensureLogin();
    await this.loadData();
  },
  async loadData() {
    const list = await getAssessmentReports();
    this.setData({ list: list || [] });
  },
  viewReport(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/report-viewer/index?id=${id}` });
  },
  async generateAi(e) {
    if (this.data.generating) {
      return;
    }
    const reportType = e.currentTarget.dataset.type || 'ALL';
    this.setData({ generating: true });
    try {
      const result = await generateAiAssessmentReports({ reportType });
      wx.showToast({ title: result && result.message ? result.message : 'AI评估已生成', icon: 'none' });
      await this.loadData();
    } catch (error) {
      wx.showToast({ title: error.message || '生成失败，请稍后重试', icon: 'none' });
    } finally {
      this.setData({ generating: false });
    }
  }
});
