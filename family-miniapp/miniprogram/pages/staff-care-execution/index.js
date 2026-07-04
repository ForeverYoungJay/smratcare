const {
  getCareExecutionTasks,
  submitCareExecutionReceipt,
  uploadStaffTaskEvidence
} = require('../../services/staff');
const { enqueue, isNetworkError } = require('../../utils/offline-queue');
const { saveDraft, loadDraft, clearDraft } = require('../../utils/form-draft');

const DRAFT_PAGE = 'staff-care-execution';
const DRAFT_FIELDS = [
  'careItemIndex', 'skinStatusIndex', 'mentalStatusIndex', 'riskIndex',
  'identityChecked', 'careDone', 'riskChecked',
  'materials', 'remark', 'handoverNote', 'scanText'
];

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
    submitError: '',
    submitSuccess: '',
    lastReceiptSummary: '',
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
    this.draftRestoredFor = '';
    this.setData({ activeTaskId: options.id || '' });
  },
  onShow() {
    getApp().ensureLogin();
    this.loadData();
  },
  onUnload() {
    this.saveFormDraft();
  },
  onHide() {
    this.saveFormDraft();
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
      this.restoreFormDraft();
    } catch (error) {
      this.setData({ loadError: error.message || '护理任务加载失败' });
    } finally {
      this.setData({ loading: false });
    }
  },
  collectDraftData() {
    const draft = {};
    DRAFT_FIELDS.forEach((field) => {
      draft[field] = this.data[field];
    });
    return draft;
  },
  hasFormContent() {
    return !!(this.data.identityChecked || this.data.careDone || this.data.riskChecked
      || this.data.materials.trim() || this.data.remark.trim()
      || this.data.handoverNote.trim() || this.data.scanText);
  },
  saveFormDraft() {
    if (!this.data.activeTaskId || !this.hasFormContent()) return;
    saveDraft(DRAFT_PAGE, this.data.activeTaskId, this.collectDraftData());
  },
  scheduleDraftSave() {
    if (this._draftTimer) clearTimeout(this._draftTimer);
    this._draftTimer = setTimeout(() => {
      this.saveFormDraft();
    }, 500);
  },
  restoreFormDraft() {
    const taskId = this.data.activeTaskId;
    if (!taskId || this.draftRestoredFor === taskId) return;
    this.draftRestoredFor = taskId;
    const draft = loadDraft(DRAFT_PAGE, taskId);
    if (!draft) return;
    const patch = {};
    DRAFT_FIELDS.forEach((field) => {
      if (draft[field] !== undefined) patch[field] = draft[field];
    });
    this.setData(patch);
    wx.showToast({ title: '已恢复未提交草稿', icon: 'none' });
  },
  selectTask(e) {
    const id = e.currentTarget.dataset.id;
    this.saveFormDraft();
    const activeTask = this.data.tasks.find((item) => item.id === id) || null;
    this.draftRestoredFor = '';
    this.setData({
      activeTaskId: id,
      activeTask,
      submitError: '',
      submitSuccess: ''
    });
    this.resetForm();
    this.restoreFormDraft();
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
      photos: [],
      submitError: '',
      submitSuccess: ''
    });
  },
  toggleCheck(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({
      [field]: !this.data[field],
      submitError: '',
      submitSuccess: ''
    });
    this.scheduleDraftSave();
  },
  onCareItemChange(e) {
    this.setData({ careItemIndex: Number(e.detail.value || 0), submitError: '', submitSuccess: '' });
    this.scheduleDraftSave();
  },
  onSkinStatusChange(e) {
    this.setData({ skinStatusIndex: Number(e.detail.value || 0), submitError: '', submitSuccess: '' });
    this.scheduleDraftSave();
  },
  onMentalStatusChange(e) {
    this.setData({ mentalStatusIndex: Number(e.detail.value || 0), submitError: '', submitSuccess: '' });
    this.scheduleDraftSave();
  },
  onRiskChange(e) {
    this.setData({ riskIndex: Number(e.detail.value || 0), submitError: '', submitSuccess: '' });
    this.scheduleDraftSave();
  },
  onInput(e) {
    this.setData({
      [e.currentTarget.dataset.field]: e.detail.value,
      submitError: '',
      submitSuccess: ''
    });
    this.scheduleDraftSave();
  },
  scanCode() {
    wx.scanCode({
      onlyFromCamera: false,
      success: (res) => {
        this.setData({ scanText: res.result || '已扫码', submitError: '', submitSuccess: '' });
        this.scheduleDraftSave();
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
        this.setData({
          photos: this.data.photos.concat(res.tempFilePaths || []).slice(0, 8),
          submitError: '',
          submitSuccess: ''
        });
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
  buildPayload(photos, evidencePending) {
    return {
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
      photos,
      ...(evidencePending ? { evidencePending: true } : {})
    };
  },
  async submitCare() {
    if (this.data.submitting) return;
    const validation = this.validateForm();
    if (validation) {
      wx.showToast({ title: validation, icon: 'none' });
      return;
    }
    this.setData({ submitting: true, submitError: '', submitSuccess: '' });
    wx.showLoading({ title: this.data.photos.length ? '上传留痕中' : '提交中', mask: true });
    const taskId = this.data.activeTask.id;
    let payload = null;
    try {
      let photos = [];
      let evidencePending = false;
      try {
        for (const photo of this.data.photos) {
          if (/^https?:\/\//i.test(photo)) {
            photos.push(photo);
          } else {
            const uploaded = await uploadStaffTaskEvidence(photo, { bizType: 'staff-care-execution' });
            photos.push(uploaded.fileUrl || photo);
          }
        }
      } catch (uploadError) {
        if (!isNetworkError(uploadError)) throw uploadError;
        // 弱网下留痕照片没传上去：只保留已上传部分，文本先行提交/暂存。
        photos = this.data.photos.filter((photo) => /^https?:\/\//i.test(photo));
        evidencePending = true;
      }
      payload = this.buildPayload(photos, evidencePending);
      await submitCareExecutionReceipt(taskId, payload);
      wx.hideLoading();
      clearDraft(DRAFT_PAGE, taskId);
      const summary = `${this.data.careItemOptions[this.data.careItemIndex]} · ${this.data.activeTask.resident || this.data.activeTask.room || '护理对象'}`;
      this.resetForm();
      this.setData({
        lastReceiptSummary: summary,
        submitSuccess: '护理回执已提交，可在执行记录和后台台账继续查看。'
      });
      wx.showToast({ title: '护理已回执', icon: 'success' });
      this.loadData();
    } catch (error) {
      wx.hideLoading();
      if (isNetworkError(error) && payload) {
        enqueue('task-complete', { kind: 'care', taskId, data: payload });
        clearDraft(DRAFT_PAGE, taskId);
        this.resetForm();
        wx.showToast({ title: '已离线暂存，网络恢复后自动提交', icon: 'none' });
      } else {
        this.setData({ submitError: error.message || '提交失败，请稍后重试' });
        wx.showToast({ title: error.message || '提交失败', icon: 'none' });
      }
    } finally {
      this.setData({ submitting: false });
    }
  }
});
