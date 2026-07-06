const { getMedicalRecords } = require('../../services/family');
const { ensureSensitiveAccess } = require('../../utils/sensitive-access');

Page({
  data: {
    records: [],
    loading: false,
    loadError: '',
    denied: false
  },
  async onShow() {
    getApp().ensureLogin();
    const granted = await ensureSensitiveAccess({
      scene: 'medical-records',
      settingKey: 'verifyMedicalRecords',
      title: '就医记录访问验证'
    });
    if (!granted) {
      this.setData({ records: [], denied: true, loadError: '' });
      return;
    }
    this.setData({ denied: false });
    await this.loadRecords();
  },
  onPullDownRefresh() {
    this.loadRecords().finally(() => wx.stopPullDownRefresh());
  },
  async loadRecords() {
    this.setData({ loading: true, loadError: '' });
    try {
      const records = await getMedicalRecords();
      this.setData({ records: records || [] });
    } catch (error) {
      this.setData({ records: [], loadError: error.message || '就医记录加载失败，请稍后重试' });
    } finally {
      this.setData({ loading: false });
    }
  },
  retry() {
    this.loadRecords();
  },
  openDetail(e) {
    const id = e.currentTarget.dataset.id;
    const record = (this.data.records || []).find((item) => String(item.id) === String(id));
    if (!record) {
      return;
    }
    getApp().globalData.medicalRecordDetailCache = record;
    wx.navigateTo({ url: `/pages/medical-record-detail/index?id=${encodeURIComponent(id)}` });
  }
});
