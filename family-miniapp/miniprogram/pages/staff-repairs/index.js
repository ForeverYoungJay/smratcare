const {
  getRepairTasks,
  submitRepairReceipt,
  uploadStaffTaskEvidence
} = require('../../services/staff');

const faultOptions = ['设施五金', '水电照明', '适老设备', '呼叫设备', '门窗家具', '其他'];
const resultOptions = ['已修复', '临时处理', '需二次维修', '需采购备件', '转外部维修'];
const durationOptions = ['15 分钟内', '15-30 分钟', '30-60 分钟', '1 小时以上'];

function normalizeTask(item) {
  return {
    ...item,
    metaText: `${item.room || '位置未填'} · ${item.time || '时间待定'} · ${item.status || '待处理'}`
  };
}

Page({
  data: {
    loading: false,
    submitting: false,
    loadError: '',
    tasks: [],
    activeTaskId: '',
    activeTask: null,
    faultOptions,
    resultOptions,
    durationOptions,
    faultIndex: 0,
    resultIndex: 0,
    durationIndex: 1,
    arrived: false,
    repaired: false,
    accepted: false,
    materials: '',
    remark: '',
    followUp: '',
    scanText: '',
    photos: []
  },
  onLoad(options = {}) {
    this.setData({ activeTaskId: options.id || '' });
  },
  onShow() {
    getApp().ensureLogin();
    this.loadData();
  },
  async loadData() {
    this.setData({ loading: true, loadError: '' });
    try {
      const tasks = ((await getRepairTasks()) || []).map(normalizeTask);
      const activeTask = tasks.find((item) => item.id === this.data.activeTaskId) || tasks[0] || null;
      this.setData({
        tasks,
        activeTask,
        activeTaskId: activeTask ? activeTask.id : ''
      });
    } catch (error) {
      this.setData({ loadError: error.message || '维修工单加载失败' });
    } finally {
      this.setData({ loading: false });
    }
  },
  selectTask(e) {
    const id = e.currentTarget.dataset.id;
    const activeTask = this.data.tasks.find((item) => item.id === id) || null;
    this.setData({
      activeTaskId: id,
      activeTask,
      faultIndex: 0,
      resultIndex: 0,
      durationIndex: 1,
      arrived: false,
      repaired: false,
      accepted: false,
      materials: '',
      remark: '',
      followUp: '',
      scanText: '',
      photos: []
    });
  },
  toggleCheck(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({ [field]: !this.data[field] });
  },
  onFaultChange(e) {
    this.setData({ faultIndex: Number(e.detail.value || 0) });
  },
  onResultChange(e) {
    this.setData({ resultIndex: Number(e.detail.value || 0) });
  },
  onDurationChange(e) {
    this.setData({ durationIndex: Number(e.detail.value || 0) });
  },
  onInput(e) {
    this.setData({ [e.currentTarget.dataset.field]: e.detail.value });
  },
  scanCode() {
    wx.scanCode({
      onlyFromCamera: false,
      success: (res) => {
        this.setData({ scanText: res.result || '已扫码' });
      },
      fail: () => {
        wx.showToast({ title: '扫码已取消', icon: 'none' });
      }
    });
  },
  choosePhoto() {
    wx.chooseImage({
      count: 4,
      sizeType: ['compressed'],
      sourceType: ['camera', 'album'],
      success: (res) => {
        this.setData({ photos: this.data.photos.concat(res.tempFilePaths || []).slice(0, 8) });
      }
    });
  },
  validateForm() {
    if (!this.data.activeTask) return '请选择维修工单';
    if (!this.data.arrived) return '请确认已到场';
    if (!this.data.repaired) return '请确认已完成处理';
    if (!this.data.accepted) return '请确认现场验收';
    if (!this.data.remark.trim()) return '请填写处理说明';
    return '';
  },
  async submitRepair() {
    if (this.data.submitting) return;
    const validation = this.validateForm();
    if (validation) {
      wx.showToast({ title: validation, icon: 'none' });
      return;
    }
    this.setData({ submitting: true });
    wx.showLoading({ title: this.data.photos.length ? '上传验收照' : '提交中', mask: true });
    try {
      const uploadedPhotos = [];
      for (const photo of this.data.photos) {
        if (/^https?:\/\//i.test(photo)) {
          uploadedPhotos.push(photo);
        } else {
          const uploaded = await uploadStaffTaskEvidence(photo, { bizType: 'staff-repair-acceptance' });
          uploadedPhotos.push(uploaded.fileUrl || photo);
        }
      }
      await submitRepairReceipt(this.data.activeTask.id, {
        arrived: this.data.arrived,
        repaired: this.data.repaired,
        accepted: this.data.accepted,
        faultType: this.data.faultOptions[this.data.faultIndex],
        resultType: this.data.resultOptions[this.data.resultIndex],
        duration: this.data.durationOptions[this.data.durationIndex],
        materials: this.data.materials.trim(),
        remark: this.data.remark.trim(),
        followUp: this.data.followUp.trim(),
        scanText: this.data.scanText,
        photos: uploadedPhotos
      });
      wx.hideLoading();
      wx.showToast({ title: '维修已回执', icon: 'success' });
      this.setData({
        arrived: false,
        repaired: false,
        accepted: false,
        materials: '',
        remark: '',
        followUp: '',
        scanText: '',
        photos: []
      });
      this.loadData();
    } catch (error) {
      wx.hideLoading();
      wx.showToast({ title: error.message || '提交失败', icon: 'none' });
    } finally {
      this.setData({ submitting: false });
    }
  }
});
