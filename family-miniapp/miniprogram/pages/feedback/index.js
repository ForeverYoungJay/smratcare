const { submitFeedback, getFeedbackRecords } = require('../../services/family');

function unwrapList(result) {
  if (Array.isArray(result)) return result;
  if (result && Array.isArray(result.list)) return result.list;
  if (result && Array.isArray(result.records)) return result.records;
  return [];
}

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
    records: [],
    loading: false,
    loadingMore: false,
    submitting: false,
    loadError: '',
    pageNo: 1,
    pageSize: 10,
    hasMore: false
  },
  async onShow() {
    getApp().ensureLogin();
    await this.loadRecords({ reset: true });
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
  async loadRecords(options = {}) {
    const reset = options.reset !== false;
    const nextPageNo = reset ? 1 : Number(this.data.pageNo || 1) + 1;
    this.setData(reset ? { loading: true, loadError: '' } : { loadingMore: true, loadError: '' });
    try {
      const recordsRes = await getFeedbackRecords(nextPageNo, this.data.pageSize);
      const records = unwrapList(recordsRes);
      this.setData({
        records: reset ? records : this.data.records.concat(records),
        pageNo: nextPageNo,
        hasMore: records.length >= this.data.pageSize
      });
    } catch (error) {
      this.setData({ loadError: error.message || (reset ? '反馈记录加载失败' : '更多反馈记录加载失败，请稍后重试') });
    } finally {
      this.setData({ loading: false, loadingMore: false });
    }
  },
  async submit() {
    if (this.data.submitting) {
      return;
    }
    const selectedType = this.data.feedbackTypes[this.data.feedbackTypeIndex] || this.data.feedbackTypes[0];
    const content = String(this.data.content || '').trim();
    if (selectedType.value === 'COMPLAINT' && !content) {
      wx.showToast({ title: '请填写投诉建议内容', icon: 'none' });
      return;
    }
    this.setData({ submitting: true });
    try {
      await submitFeedback({
        feedbackType: selectedType.value,
        serviceType: this.data.serviceType,
        score: selectedType.value === 'COMPLAINT' ? null : this.data.score,
        content: content || (selectedType.value === 'COMPLAINT' ? '投诉建议已提交' : '服务评价已提交'),
        contact: ''
      });
      wx.showToast({ title: selectedType.value === 'COMPLAINT' ? '投诉已提交，感谢反馈' : '评价已提交，感谢反馈', icon: 'none' });
      this.setData({ content: '' });
      await this.loadRecords({ reset: true });
    } catch (error) {
      wx.showToast({ title: error.message || '提交失败，请稍后重试', icon: 'none' });
    } finally {
      this.setData({ submitting: false });
    }
  },
  loadMore() {
    if (this.data.loading || this.data.loadingMore || !this.data.hasMore) return;
    this.loadRecords({ reset: false });
  },
  onReachBottom() {
    this.loadMore();
  }
});
