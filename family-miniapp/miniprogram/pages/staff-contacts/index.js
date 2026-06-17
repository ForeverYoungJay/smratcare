const { getStaffContacts } = require('../../services/staff');

function roleText(codes = []) {
  const labels = {
    NURSING_MINISTER: '护理主管',
    NURSING_EMPLOYEE: '护理员',
    MEDICAL_MINISTER: '医务主管',
    MEDICAL_EMPLOYEE: '医生/医护',
    LOGISTICS_EMPLOYEE: '后勤',
    LOGISTICS_MINISTER: '后勤主管',
    HR_MINISTER: '人事',
    FINANCE_EMPLOYEE: '财务',
    DIRECTOR: '院长'
  };
  const rows = (codes || []).map((code) => labels[String(code).toUpperCase()] || code).filter(Boolean);
  return rows.length ? rows.join(' / ') : '员工';
}

function normalizeContact(item) {
  const name = item.realName || item.username || `员工${item.id || ''}`;
  return {
    ...item,
    name,
    roleText: roleText(item.roleCodes),
    departmentText: item.departmentName || (item.departmentId ? `部门 ${item.departmentId}` : '未分配部门'),
    phoneText: item.phone || '未维护手机号',
    avatarText: String(name).slice(0, 1)
  };
}

Page({
  data: {
    loading: false,
    loadError: '',
    keyword: '',
    contacts: []
  },
  onShow() {
    getApp().ensureLogin();
    this.loadData();
  },
  async loadData() {
    this.setData({ loading: true, loadError: '' });
    try {
      const rows = await getStaffContacts({ keyword: this.data.keyword.trim(), size: 120 });
      this.setData({ contacts: (rows || []).map(normalizeContact) });
    } catch (error) {
      this.setData({ loadError: error.message || '通讯录加载失败' });
    } finally {
      this.setData({ loading: false });
    }
  },
  onKeywordInput(e) {
    this.setData({ keyword: e.detail.value });
  },
  searchContacts() {
    this.loadData();
  },
  call(e) {
    const phone = e.currentTarget.dataset.phone;
    if (!phone) {
      wx.showToast({ title: '未维护手机号', icon: 'none' });
      return;
    }
    wx.makePhoneCall({ phoneNumber: phone });
  }
});
