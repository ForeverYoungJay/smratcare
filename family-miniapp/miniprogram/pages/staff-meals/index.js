const {
  getMealDeliveryTasks,
  submitMealDeliveryReceipt,
  uploadStaffTaskEvidence
} = require('../../services/staff');

const intakeOptions = ['全部吃完', '约 3/4', '约 1/2', '少量进食', '未进食', '拒食/异常'];

function normalizeTask(item) {
  return {
    ...item,
    metaText: `${item.resident || '长者'} · ${item.room || '位置未填'} · ${item.time || '时间待定'}`
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
    intakeOptions,
    intakeIndex: 1,
    mealChecked: false,
    tabooChecked: false,
    signed: false,
    signerName: '',
    remark: '',
    exceptionText: '',
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
      const tasks = ((await getMealDeliveryTasks()) || []).map(normalizeTask);
      const activeTask = tasks.find((item) => item.id === this.data.activeTaskId) || tasks[0] || null;
      this.setData({
        tasks,
        activeTask,
        activeTaskId: activeTask ? activeTask.id : '',
        signerName: this.data.signerName || (activeTask && activeTask.resident) || ''
      });
    } catch (error) {
      this.setData({ loadError: error.message || '送餐任务加载失败' });
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
      signerName: (activeTask && activeTask.resident) || '',
      remark: '',
      exceptionText: '',
      scanText: '',
      photos: [],
      mealChecked: false,
      tabooChecked: false,
      signed: false,
      intakeIndex: 1
    });
  },
  toggleCheck(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({ [field]: !this.data[field] });
  },
  onIntakeChange(e) {
    this.setData({ intakeIndex: Number(e.detail.value || 0) });
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
      count: 3,
      sizeType: ['compressed'],
      sourceType: ['camera', 'album'],
      success: (res) => {
        this.setData({ photos: this.data.photos.concat(res.tempFilePaths || []).slice(0, 6) });
      }
    });
  },
  validateForm() {
    if (!this.data.activeTask) return '请选择送餐任务';
    if (!this.data.mealChecked) return '请先核对餐别和姓名';
    if (!this.data.tabooChecked) return '请先核对禁忌标签';
    if (!this.data.signed) return '请确认已送达签收';
    if (!this.data.signerName.trim()) return '请填写签收人';
    return '';
  },
  async submitMeal() {
    if (this.data.submitting) return;
    const validation = this.validateForm();
    if (validation) {
      wx.showToast({ title: validation, icon: 'none' });
      return;
    }
    this.setData({ submitting: true });
    wx.showLoading({ title: this.data.photos.length ? '上传留痕中' : '提交中', mask: true });
    try {
      const uploadedPhotos = [];
      for (const photo of this.data.photos) {
        if (/^https?:\/\//i.test(photo)) {
          uploadedPhotos.push(photo);
        } else {
          const uploaded = await uploadStaffTaskEvidence(photo, { bizType: 'staff-meal-signoff' });
          uploadedPhotos.push(uploaded.fileUrl || photo);
        }
      }
      await submitMealDeliveryReceipt(this.data.activeTask.id, {
        mealChecked: this.data.mealChecked,
        tabooChecked: this.data.tabooChecked,
        signed: this.data.signed,
        intake: this.data.intakeOptions[this.data.intakeIndex],
        signerName: this.data.signerName.trim(),
        remark: this.data.remark.trim(),
        exceptionText: this.data.exceptionText.trim(),
        scanText: this.data.scanText,
        photos: uploadedPhotos
      });
      wx.hideLoading();
      wx.showToast({ title: '送餐已签收', icon: 'success' });
      this.setData({
        remark: '',
        exceptionText: '',
        scanText: '',
        photos: [],
        mealChecked: false,
        tabooChecked: false,
        signed: false,
        intakeIndex: 1
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
