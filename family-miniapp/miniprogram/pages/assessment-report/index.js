const { getAssessmentReports, generateAiAssessmentReports } = require('../../services/family');
const { ensureSensitiveAccess } = require('../../utils/sensitive-access');

Page({
  data: {
    list: [],
    generating: false,
    loading: false,
    loadError: '',
    accessGranted: true
  },
  async onShow() {
    getApp().ensureLogin();
    await this.verifyAndLoad();
  },
  async verifyAndLoad(force = false) {
    const granted = await ensureSensitiveAccess({
      scene: 'assessment-report-list',
      settingKey: 'verifyReports',
      title: '评估报告访问验证',
      force
    });
    if (!granted) {
      this.setData({
        accessGranted: false,
        list: [],
        loadError: ''
      });
      return;
    }
    this.setData({ accessGranted: true });
    await this.loadData();
  },
  async loadData() {
    this.setData({ loading: true, loadError: '' });
    try {
      const list = await getAssessmentReports();
      this.setData({ list: list || [] });
    } catch (error) {
      this.setData({
        list: [],
        loadError: error.message || '评估报告加载失败，请稍后重试'
      });
    } finally {
      this.setData({ loading: false });
    }
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
  },
  retryVerify() {
    this.verifyAndLoad(true);
  }
});
