const {
  getStaffProfile,
  getStaffSuggestions,
  submitStaffSuggestion
} = require('../../services/staff');

const categoryOptions = ['流程优化', '设备物资', '餐食服务', '护理协同', '培训排班', '其他建议'];
const statusFilters = [
  { label: '全部', value: '' },
  { label: '待处理', value: 'PENDING' },
  { label: '处理中', value: 'PROCESSING' },
  { label: '已处理', value: 'DONE' }
];

function normalizeSuggestion(item) {
  const statusTextMap = {
    PENDING: '待处理',
    PROCESSING: '处理中',
    DONE: '已处理',
    CLOSED: '已关闭'
  };
  const rawContent = String(item.content || '');
  const matched = rawContent.match(/^【员工建议｜(.+?)】([\s\S]*)$/);
  return {
    ...item,
    categoryText: matched ? matched[1] : '员工建议',
    contentText: (matched ? matched[2] : rawContent).trim(),
    statusText: statusTextMap[item.status] || item.status || '待处理',
    createText: String(item.createTime || item.updateTime || '').replace('T', ' ')
  };
}

Page({
  data: {
    loading: false,
    submitting: false,
    loadError: '',
    profile: null,
    suggestions: [],
    statusFilters,
    activeStatus: '',
    categoryOptions,
    categoryIndex: 0,
    content: '',
    contact: '',
    proposerName: ''
  },
  onShow() {
    getApp().ensureLogin();
    this.loadData();
  },
  async loadData() {
    this.setData({ loading: true, loadError: '' });
    try {
      const profile = await getStaffProfile();
      const proposerName = this.data.proposerName
        || (profile && (profile.realName || profile.username))
        || '';
      const contact = this.data.contact || (profile && profile.phone) || '';
      const suggestions = await getStaffSuggestions({
        status: this.data.activeStatus,
        keyword: proposerName,
        pageSize: 20
      });
      this.setData({
        profile,
        proposerName,
        contact,
        suggestions: (suggestions || []).map(normalizeSuggestion)
      });
    } catch (error) {
      this.setData({ loadError: error.message || '建议反馈加载失败' });
    } finally {
      this.setData({ loading: false });
    }
  },
  onCategoryChange(e) {
    this.setData({ categoryIndex: Number(e.detail.value || 0) });
  },
  onInput(e) {
    this.setData({ [e.currentTarget.dataset.field]: e.detail.value });
  },
  switchStatus(e) {
    this.setData({ activeStatus: e.currentTarget.dataset.value || '' });
    this.loadData();
  },
  validateForm() {
    if (!this.data.proposerName.trim()) return '请填写提交人';
    if (!this.data.content.trim()) return '请填写建议内容';
    if (this.data.content.trim().length < 8) return '建议内容再具体一点';
    return '';
  },
  async submitSuggestion() {
    if (this.data.submitting) return;
    const validation = this.validateForm();
    if (validation) {
      wx.showToast({ title: validation, icon: 'none' });
      return;
    }
    const category = this.data.categoryOptions[this.data.categoryIndex] || '其他建议';
    this.setData({ submitting: true });
    try {
      await submitStaffSuggestion({
        proposerName: this.data.proposerName.trim(),
        contact: this.data.contact.trim(),
        content: `【员工建议｜${category}】${this.data.content.trim()}`
      });
      wx.showToast({ title: '建议已提交', icon: 'success' });
      this.setData({ content: '', categoryIndex: 0, activeStatus: '' });
      this.loadData();
    } catch (error) {
      wx.showToast({ title: error.message || '提交失败', icon: 'none' });
    } finally {
      this.setData({ submitting: false });
    }
  }
});
