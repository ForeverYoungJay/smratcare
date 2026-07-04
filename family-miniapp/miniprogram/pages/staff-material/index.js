const {
  getStaffApprovals,
  getStaffProfile,
  submitStaffApproval
} = require('../../services/staff');

const categoryOptions = [
  { label: '护理耗材', value: 'CARE' },
  { label: '药品医材', value: 'MEDICAL' },
  { label: '后勤维修', value: 'LOGISTICS' },
  { label: '餐饮物资', value: 'DINING' },
  { label: '保洁洗涤', value: 'CLEANING' },
  { label: '其他物资', value: 'OTHER' }
];

const urgencyOptions = [
  { label: '普通', value: 'NORMAL' },
  { label: '加急', value: 'URGENT' },
  { label: '紧急', value: 'CRITICAL' }
];

function statusText(status) {
  if (status === 'APPROVED') return '已通过';
  if (status === 'REJECTED') return '已驳回';
  return '待审批';
}

function parseFormData(formData) {
  if (!formData) return {};
  try {
    return JSON.parse(formData);
  } catch (error) {
    return {};
  }
}

function normalizeApproval(item) {
  const form = parseFormData(item.formData);
  return {
    ...item,
    form,
    statusText: statusText(item.status),
    createText: String(item.createTime || '').replace('T', ' '),
    materialLine: `${form.materialName || item.title || '物资'} · ${form.quantity || '数量未填'}`
  };
}

Page({
  data: {
    loading: false,
    submitting: false,
    loadError: '',
    submitError: '',
    submitSuccess: '',
    approvals: [],
    profile: null,
    categoryOptions,
    urgencyOptions,
    categoryIndex: 0,
    urgencyIndex: 0,
    materialName: '',
    quantity: '',
    useArea: '',
    expectedDate: new Date().toISOString().slice(0, 10),
    reason: ''
  },
  onShow() {
    getApp().ensureLogin();
    this.loadData();
  },
  async loadData() {
    this.setData({ loading: true, loadError: '' });
    try {
      const [profile, approvals] = await Promise.all([
        getStaffProfile(),
        getStaffApprovals({ type: 'MATERIAL_APPLY', pageSize: 30 })
      ]);
      this.setData({
        profile,
        approvals: (approvals || []).map(normalizeApproval)
      });
      if (!this.data.useArea) {
        this.setData({ useArea: (profile && (profile.department || profile.orgName)) || '' });
      }
    } catch (error) {
      this.setData({ loadError: error.message || '物资申领记录加载失败' });
    } finally {
      this.setData({ loading: false });
    }
  },
  onCategoryChange(e) {
    this.setData({
      categoryIndex: Number(e.detail.value || 0),
      submitError: '',
      submitSuccess: ''
    });
  },
  onUrgencyChange(e) {
    this.setData({
      urgencyIndex: Number(e.detail.value || 0),
      submitError: '',
      submitSuccess: ''
    });
  },
  onExpectedDateChange(e) {
    this.setData({
      expectedDate: e.detail.value,
      submitError: '',
      submitSuccess: ''
    });
  },
  onInput(e) {
    this.setData({
      [e.currentTarget.dataset.field]: e.detail.value,
      submitError: '',
      submitSuccess: ''
    });
  },
  validateForm() {
    if (!this.data.materialName.trim()) return '请填写物资名称';
    if (!this.data.quantity.trim()) return '请填写申领数量';
    if (!this.data.useArea.trim()) return '请填写使用区域';
    if (!this.data.reason.trim()) return '请填写申领原因';
    return '';
  },
  buildPayload() {
    const category = this.data.categoryOptions[this.data.categoryIndex] || this.data.categoryOptions[0];
    const urgency = this.data.urgencyOptions[this.data.urgencyIndex] || this.data.urgencyOptions[0];
    const materialName = this.data.materialName.trim();
    const quantity = this.data.quantity.trim();
    const form = {
      materialCategory: category.value,
      materialCategoryText: category.label,
      materialName,
      quantity,
      useArea: this.data.useArea.trim(),
      expectedDate: this.data.expectedDate,
      urgency: urgency.value,
      urgencyText: urgency.label,
      source: 'staff-mobile'
    };
    return {
      approvalType: 'MATERIAL_APPLY',
      title: `物资申领：${materialName} ${quantity}`,
      applicantName: (this.data.profile && (this.data.profile.realName || this.data.profile.username)) || '',
      formData: JSON.stringify(form),
      status: 'PENDING',
      remark: this.data.reason.trim()
    };
  },
  async submitMaterial() {
    if (this.data.submitting) return;
    const validation = this.validateForm();
    if (validation) {
      wx.showToast({ title: validation, icon: 'none' });
      return;
    }
    this.setData({ submitting: true, submitError: '', submitSuccess: '' });
    try {
      const item = await submitStaffApproval(this.buildPayload());
      this.setData({
        approvals: [normalizeApproval(item)].concat(this.data.approvals),
        materialName: '',
        quantity: '',
        reason: '',
        urgencyIndex: 0,
        submitSuccess: '物资申领已进入 OA 审批流，可在下方查看状态变化。'
      });
      wx.showToast({ title: '申领已提交', icon: 'success' });
    } catch (error) {
      this.setData({ submitError: error.message || '提交失败，请稍后重试' });
      wx.showToast({ title: error.message || '提交失败', icon: 'none' });
    } finally {
      this.setData({ submitting: false });
    }
  }
});
