const {
  getOutingRecords,
  submitOutingApplication,
  cancelOutingApplication
} = require('../../services/family');

function statusClass(status) {
  const text = String(status || '');
  if (text.includes('取消') || text.includes('驳回') || text.includes('拒绝')) {
    return 'pill-danger';
  }
  if (text.includes('外出') || text.includes('进行') || text.includes('审批') || text.includes('待')) {
    return 'pill-warning';
  }
  if (text.includes('归院') || text.includes('返回') || text.includes('完成') || text.includes('通过')) {
    return 'pill-success';
  }
  return 'pill-normal';
}

function decorate(list) {
  return (list || []).map((item) => ({
    ...item,
    statusClass: statusClass(item.applicationStatus || item.status)
  }));
}

Page({
  data: {
    list: [],
    loading: false,
    loadError: '',
    showForm: false,
    submitting: false,
    form: {
      outingDate: '',
      reason: '',
      destination: '',
      companion: '',
      returnDate: '',
      returnTime: ''
    }
  },
  onShow() {
    getApp().ensureLogin();
    this.loadRecords();
  },
  onPullDownRefresh() {
    this.loadRecords().finally(() => wx.stopPullDownRefresh());
  },
  async loadRecords() {
    this.setData({ loading: true, loadError: '' });
    try {
      const list = await getOutingRecords();
      this.setData({ list: decorate(list) });
    } catch (error) {
      this.setData({ list: [], loadError: error.message || '外出记录加载失败，请稍后重试' });
    } finally {
      this.setData({ loading: false });
    }
  },
  retry() {
    this.loadRecords();
  },
  toggleForm() {
    this.setData({ showForm: !this.data.showForm });
  },
  onOutingDate(e) { this.setData({ 'form.outingDate': e.detail.value }); },
  onReturnDate(e) { this.setData({ 'form.returnDate': e.detail.value }); },
  onReturnTime(e) { this.setData({ 'form.returnTime': e.detail.value }); },
  onReasonInput(e) { this.setData({ 'form.reason': e.detail.value }); },
  onDestinationInput(e) { this.setData({ 'form.destination': e.detail.value }); },
  onCompanionInput(e) { this.setData({ 'form.companion': e.detail.value }); },
  async submitApplication() {
    if (this.data.submitting) {
      return;
    }
    const app = getApp();
    if (!app.globalData.selectedElderId) {
      wx.showToast({ title: '请先在首页绑定并选择老人', icon: 'none' });
      return;
    }
    const form = this.data.form;
    if (!form.outingDate) {
      wx.showToast({ title: '请选择外出日期', icon: 'none' });
      return;
    }
    if (!String(form.reason || '').trim()) {
      wx.showToast({ title: '请填写外出事由', icon: 'none' });
      return;
    }
    const payload = {
      outingDate: form.outingDate,
      reason: String(form.reason).trim(),
      destination: String(form.destination || '').trim(),
      companion: String(form.companion || '').trim()
    };
    if (form.returnDate && form.returnTime) {
      payload.expectedReturnTime = `${form.returnDate}T${form.returnTime}:00`;
    }
    this.setData({ submitting: true });
    try {
      await submitOutingApplication(payload);
      wx.showToast({ title: '外出申请已提交', icon: 'success' });
      this.setData({
        showForm: false,
        form: { outingDate: '', reason: '', destination: '', companion: '', returnDate: '', returnTime: '' }
      });
      await this.loadRecords();
    } catch (error) {
      wx.showToast({ title: error.message || '提交失败，请稍后重试', icon: 'none' });
    } finally {
      this.setData({ submitting: false });
    }
  },
  async cancelApplication(e) {
    const id = e.currentTarget.dataset.id;
    if (!id) {
      return;
    }
    const confirm = await new Promise((resolve) => {
      wx.showModal({
        title: '取消外出申请',
        content: '确认取消这条外出申请吗？',
        confirmText: '取消申请',
        cancelText: '再想想',
        success: (res) => resolve(!!(res && res.confirm)),
        fail: () => resolve(false)
      });
    });
    if (!confirm) {
      return;
    }
    try {
      await cancelOutingApplication(id);
      wx.showToast({ title: '已取消', icon: 'success' });
      await this.loadRecords();
    } catch (error) {
      wx.showToast({ title: error.message || '取消失败，请稍后重试', icon: 'none' });
    }
  }
});
