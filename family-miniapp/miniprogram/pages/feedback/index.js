const { submitFeedback, getFeedbackRecords } = require('../../services/family');

Page({
  data: {
    feedbackTypes: [
      { value: 'EVALUATION', label: '服务评价' },
      { value: 'COMPLAINT', label: '投诉建议' }
    ],
    feedbackTypeIndex: 0,
    score: 5,
    serviceType: '护理服务',
    serviceTypes: ['护理服务', '送餐服务', '活动组织', '客服响应'],
    serviceTypeIndex: 0,
    content: '',
    records: []
  },
  async onShow() {
    getApp().ensureLogin();
    await this.loadRecords();
  },
  onFeedbackTypeChange(e) {
    this.setData({ feedbackTypeIndex: Number(e.detail.value) });
  },
  onScoreChange(e) {
    this.setData({ score: e.detail.value });
  },
  onServiceTypeChange(e) {
    const idx = Number(e.detail.value);
    this.setData({ serviceTypeIndex: idx, serviceType: this.data.serviceTypes[idx] });
  },
  onContentInput(e) {
    this.setData({ content: e.detail.value });
  },
  async loadRecords() {
    const records = await getFeedbackRecords(1, 20);
    this.setData({ records: records || [] });
  },
  async submit() {
    const selectedType = this.data.feedbackTypes[this.data.feedbackTypeIndex] || this.data.feedbackTypes[0];
    try {
      await submitFeedback({
        feedbackType: selectedType.value,
        serviceType: this.data.serviceType,
        score: selectedType.value === 'COMPLAINT' ? null : this.data.score,
        content: this.data.content || (selectedType.value === 'COMPLAINT' ? '投诉建议已提交' : '服务评价已提交'),
        contact: ''
      });
      wx.showToast({ title: selectedType.value === 'COMPLAINT' ? '投诉已提交，感谢反馈' : '评价已提交，感谢反馈', icon: 'none' });
      this.setData({ content: '' });
      await this.loadRecords();
    } catch (error) {
      wx.showToast({ title: '提交失败，请稍后重试', icon: 'none' });
    }
  }
});
