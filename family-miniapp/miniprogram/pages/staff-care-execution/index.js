const {
  getCareExecutionTasks,
  submitCareExecutionReceipt,
  uploadStaffTaskEvidence
} = require('../../services/staff');

const careItemOptions = ['晨间洗漱', '晚间护理', '翻身拍背', '协助如厕', '进食协助', '皮肤护理', '康复陪练', '其他护理'];
const skinStatusOptions = ['完整无红肿', '轻微发红', '压痕需观察', '破损已上报', '其他'];
const mentalStatusOptions = ['平稳配合', '情绪低落', '烦躁不安', '嗜睡乏力', '沟通困难', '其他'];
const riskOptions = ['无明显风险', '跌倒风险', '压疮风险', '吞咽风险', '走失风险', '感染风险'];

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
    careItemOptions,
    skinStatusOptions,
    mentalStatusOptions,
    riskOptions,
    careItemIndex: 0,
    skinStatusIndex: 0,
    mentalStatusIndex: 0,
    riskIndex: 0,
    identityChecked: false,
    careDone: false,
    riskChecked: false,
    materials: '',
    remark: '',
    handoverNote: '',
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
      const tasks = ((await getCareExecutionTasks()) || []).map(normalizeTask);
      const activeTask = tasks.find((item) => item.id === this.data.activeTaskId) || tasks[0] || null;
      this.setData({
        tasks,
        activeTask,
        activeTaskId: activeTask ? activeTask.id : ''
      });
    } catch (error) {
      this.setData({ loadError: error.message || '护理任务加载失败' });
    } finally {
      this.setData({ loading: false });
    }
  },
  selectTask(e) {
    const id = e.currentTarget.dataset.id;
    const activeTask = this.data.tasks.find((item) => item.id === id) || null;
    this.setData({ activeTaskId: id, activeTask });
    this.resetForm();
  },
  resetForm() {
    this.setData({
      careItemIndex: 0,
      skinStatusIndex: 0,
      mentalStatusIndex: 0,
      riskIndex: 0,
      identityChecked: false,
      careDone: false,
      riskChecked: false,
      materials: '',
      remark: '',
      handoverNote: '',
      scanText: '',
      photos: []
    });
  },
  toggleCheck(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({ [field]: !this.data[field] });
  },
  onCareItemChange(e) {
    this.setData({ careItemIndex: Number(e.detail.value || 0) });
  },
  onSkinStatusChange(e) {
    this.setData({ skinStatusIndex: Number(e.detail.value || 0) });
  },
  onMentalStatusChange(e) {
    this.setData({ mentalStatusIndex: Number(e.detail.value || 0) });
  },
  onRiskChange(e) {
    this.setData({ riskIndex: Number(e.detail.value || 0) });
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
    if (!this.data.activeTask) return '请选择护理任务';
    if (!this.data.identityChecked) return '请先核对床号和姓名';
    if (!this.data.careDone) return '请确认护理项目已完成';
    if (!this.data.riskChecked) return '请确认已完成风险观察';
    if (!this.data.remark.trim()) return '请填写护理结果';
    return '';
  },
  async uploadPhotos() {
    const uploadedPhotos = [];
    for (const photo of this.data.photos) {
      if (/^https?:\/\//i.test(photo)) {
        uploadedPhotos.push(photo);
      } else {
        const uploaded = await uploadStaffTaskEvidence(photo, { bizType: 'staff-care-execution' });
        uploadedPhotos.push(uploaded.fileUrl || photo);
      }
    }
    return uploadedPhotos;
  },
  async submitCare() {
    if (this.data.submitting) return;
    const validation = this.validateForm();
    if (validation) {
      wx.showToast({ title: validation, icon: 'none' });
      return;
    }
    this.setData({ submitting: true });
    wx.showLoading({ title: this.data.photos.length ? '上传留痕中' : '提交中', mask: true });
    try {
      const photos = await this.uploadPhotos();
      await submitCareExecutionReceipt(this.data.activeTask.id, {
        identityChecked: this.data.identityChecked,
        careDone: this.data.careDone,
        riskChecked: this.data.riskChecked,
        careItem: this.data.careItemOptions[this.data.careItemIndex],
        skinStatus: this.data.skinStatusOptions[this.data.skinStatusIndex],
        mentalStatus: this.data.mentalStatusOptions[this.data.mentalStatusIndex],
        riskObservation: this.data.riskOptions[this.data.riskIndex],
        materials: this.data.materials.trim(),
        remark: this.data.remark.trim(),
        handoverNote: this.data.handoverNote.trim(),
        scanText: this.data.scanText,
        photos
      });
      wx.hideLoading();
      wx.showToast({ title: '护理已回执', icon: 'success' });
      this.resetForm();
      this.loadData();
    } catch (error) {
      wx.hideLoading();
      wx.showToast({ title: error.message || '提交失败', icon: 'none' });
    } finally {
      this.setData({ submitting: false });
    }
  }
});
