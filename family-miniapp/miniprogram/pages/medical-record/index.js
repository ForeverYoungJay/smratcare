const { getMedicalRecords } = require('../../services/family');
const { ensureSensitiveAccess } = require('../../utils/sensitive-access');

Page({
  data: {
    records: []
  },
  async onShow() {
    getApp().ensureLogin();
    const granted = await ensureSensitiveAccess({
      scene: 'medical-records',
      settingKey: 'verifyMedicalRecords',
      title: '就医记录访问验证'
    });
    if (!granted) {
      this.setData({ records: [] });
      return;
    }
    const records = await getMedicalRecords();
    this.setData({ records: records || [] });
  }
});
