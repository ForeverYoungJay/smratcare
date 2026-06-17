const {
  getStaffProfile,
  getStaffReportSummary,
  getStaffReports,
  submitStaffReport,
  submitStaffReportById
} = require('../../services/staff');

const today = new Date().toISOString().slice(0, 10);

function normalizeReport(item) {
  return {
    ...item,
    statusText: item.status === 'SUBMITTED' ? '已提交' : '草稿',
    createText: String(item.createTime || '').replace('T', ' ')
  };
}

Page({
  data: {
    loading: false,
    submitting: false,
    operatingId: '',
    loadError: '',
    profile: null,
    summary: {},
    reports: [],
    activeStatus: '',
    statusFilters: [
      { label: '全部', value: '' },
      { label: '已提交', value: 'SUBMITTED' },
      { label: '草稿', value: 'DRAFT' }
    ],
    reportDate: today,
    title: '',
    contentSummary: '',
    completedWork: '',
    riskIssue: '',
    nextPlan: ''
  },
  onShow() {
    getApp().ensureLogin();
    this.loadData();
  },
  async loadData() {
    this.setData({ loading: true, loadError: '' });
    try {
      const [profile, summary, reports] = await Promise.all([
        getStaffProfile(),
        getStaffReportSummary({ reportType: 'DAY' }),
        getStaffReports({ reportType: 'DAY', status: this.data.activeStatus, pageSize: 20 })
      ]);
      this.setData({
        profile,
        summary: summary || {},
        reports: (reports || []).map(normalizeReport)
      });
      if (!this.data.title) {
        const name = (profile && (profile.realName || profile.username)) || '员工';
        this.setData({ title: `${name} ${this.data.reportDate} 工作日报` });
      }
    } catch (error) {
      this.setData({ loadError: error.message || '工作日报加载失败' });
    } finally {
      this.setData({ loading: false });
    }
  },
  switchStatus(e) {
    this.setData({ activeStatus: e.currentTarget.dataset.value || '' });
    this.loadData();
  },
  onDateChange(e) {
    const date = e.detail.value;
    const name = (this.data.profile && (this.data.profile.realName || this.data.profile.username)) || '员工';
    this.setData({
      reportDate: date,
      title: `${name} ${date} 工作日报`
    });
  },
  onInput(e) {
    this.setData({ [e.currentTarget.dataset.field]: e.detail.value });
  },
  validateForm() {
    if (!this.data.title.trim()) return '请填写日报标题';
    if (!this.data.contentSummary.trim()) return '请填写工作摘要';
    if (!this.data.completedWork.trim()) return '请填写已完成工作';
    return '';
  },
  buildPayload(status) {
    return {
      title: this.data.title.trim(),
      reportType: 'DAY',
      reportDate: this.data.reportDate,
      contentSummary: this.data.contentSummary.trim(),
      completedWork: this.data.completedWork.trim(),
      riskIssue: this.data.riskIssue.trim(),
      nextPlan: this.data.nextPlan.trim(),
      reporterName: (this.data.profile && (this.data.profile.realName || this.data.profile.username)) || '',
      status
    };
  },
  async saveDraft() {
    await this.saveReport('DRAFT');
  },
  async submitNow() {
    await this.saveReport('SUBMITTED');
  },
  async saveReport(status) {
    if (this.data.submitting) return;
    const validation = this.validateForm();
    if (validation) {
      wx.showToast({ title: validation, icon: 'none' });
      return;
    }
    this.setData({ submitting: true });
    try {
      await submitStaffReport(this.buildPayload(status));
      this.setData({
        contentSummary: '',
        completedWork: '',
        riskIssue: '',
        nextPlan: ''
      });
      wx.showToast({ title: status === 'SUBMITTED' ? '日报已提交' : '草稿已保存', icon: 'success' });
      this.loadData();
    } catch (error) {
      wx.showToast({ title: error.message || '保存失败', icon: 'none' });
    } finally {
      this.setData({ submitting: false });
    }
  },
  async submitDraft(e) {
    const id = e.currentTarget.dataset.id;
    if (!id || this.data.operatingId) return;
    this.setData({ operatingId: id });
    try {
      await submitStaffReportById(id);
      wx.showToast({ title: '草稿已提交', icon: 'success' });
      this.loadData();
    } catch (error) {
      wx.showToast({ title: error.message || '提交失败', icon: 'none' });
    } finally {
      this.setData({ operatingId: '' });
    }
  }
});
