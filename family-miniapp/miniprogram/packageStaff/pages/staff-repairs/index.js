const {
  getRepairTasks,
  submitRepairReceipt,
  uploadStaffTaskEvidence
} = require('../../../services/staff');
const { enqueue, isNetworkError } = require('../../../utils/offline-queue');
const { saveDraft, loadDraft, clearDraft } = require('../../../utils/form-draft');

const DRAFT_PAGE = 'staff-repairs';
const RENDER_PAGE_SIZE = 20;
// 故障类型固定枚举。
const faultOptions = ['水电', '家具', '设备', '门窗', '其他'];
const resultOptions = ['已修复', '临时处理', '需二次维修', '需采购备件', '转外部维修'];
const durationOptions = ['15 分钟内', '15-30 分钟', '30-60 分钟', '1 小时以上'];
const DRAFT_FIELDS = ['faultIndex', 'resultIndex', 'durationIndex', 'arrived', 'arrivalMethod', 'repaired', 'accepted', 'materials', 'remark', 'followUp', 'scanText'];

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
    hasMore: false,
    activeTaskId: '',
    activeTask: null,
    faultOptions,
    resultOptions,
    durationOptions,
    faultIndex: 0,
    resultIndex: 0,
    durationIndex: 1,
    // 三步状态机：①到场确认 → ②处理登记 → ③拍照验收提交
    currentStep: 1,
    arrived: false,
    arrivalMethod: '',
    repaired: false,
    accepted: false,
    materials: '',
    remark: '',
    followUp: '',
    scanText: '',
    photos: []
  },
  onLoad(options = {}) {
    this.allTasks = [];
    this.draftRestoredFor = '';
    this.setData({ activeTaskId: options.id || '' });
  },
  onShow() {
    getApp().ensureLogin();
    this.loadData();
  },
  onPullDownRefresh() {
    this.loadData(true);
  },
  onReachBottom() {
    const all = this.allTasks || [];
    const rendered = this.data.tasks.length;
    if (rendered >= all.length) return;
    const next = all.slice(0, rendered + RENDER_PAGE_SIZE);
    this.setData({ tasks: next, hasMore: next.length < all.length });
  },
  onUnload() {
    this.saveFormDraft();
  },
  onHide() {
    this.saveFormDraft();
  },
  async loadData(fromPullDown = false) {
    this.setData({ loading: true, loadError: '' });
    try {
      this.allTasks = ((await getRepairTasks()) || []).map(normalizeTask);
      const activeTask = this.allTasks.find((item) => item.id === this.data.activeTaskId) || this.allTasks[0] || null;
      this.setData({
        tasks: this.allTasks.slice(0, RENDER_PAGE_SIZE),
        hasMore: this.allTasks.length > RENDER_PAGE_SIZE,
        activeTask,
        activeTaskId: activeTask ? activeTask.id : ''
      });
      this.restoreFormDraft();
    } catch (error) {
      this.setData({ loadError: error.message || '维修工单加载失败' });
    } finally {
      this.setData({ loading: false });
      if (fromPullDown) wx.stopPullDownRefresh();
    }
  },
  updateStep(patch = {}) {
    const merged = { ...this.data, ...patch };
    patch.currentStep = merged.arrived ? (merged.repaired ? 3 : 2) : 1;
    this.setData(patch);
    this.scheduleDraftSave();
  },
  collectDraftData() {
    const draft = {};
    DRAFT_FIELDS.forEach((field) => {
      draft[field] = this.data[field];
    });
    return draft;
  },
  hasFormContent() {
    return !!(this.data.arrived || this.data.materials.trim() || this.data.remark.trim()
      || this.data.followUp.trim() || this.data.scanText);
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
    patch.currentStep = patch.arrived ? (patch.repaired ? 3 : 2) : 1;
    this.setData(patch);
    wx.showToast({ title: '已恢复未提交草稿', icon: 'none' });
  },
  selectTask(e) {
    const id = e.currentTarget.dataset.id;
    this.saveFormDraft();
    const activeTask = (this.allTasks || []).find((item) => item.id === id) || null;
    this.draftRestoredFor = '';
    this.setData({
      activeTaskId: id,
      activeTask,
      faultIndex: 0,
      resultIndex: 0,
      durationIndex: 1,
      currentStep: 1,
      arrived: false,
      arrivalMethod: '',
      repaired: false,
      accepted: false,
      materials: '',
      remark: '',
      followUp: '',
      scanText: '',
      photos: []
    });
    this.restoreFormDraft();
  },
  // 第一步：扫码确认到场位置。
  scanArrival() {
    wx.scanCode({
      onlyFromCamera: false,
      success: (res) => {
        this.updateStep({
          scanText: res.result || '已扫码',
          arrived: true,
          arrivalMethod: '扫码确认'
        });
        wx.showToast({ title: '已确认到场', icon: 'success' });
      },
      fail: () => {
        wx.showToast({ title: '扫码已取消', icon: 'none' });
      }
    });
  },
  // 第一步：无法扫码时手动确认位置。
  confirmArrivalManually() {
    if (this.data.arrived) return;
    wx.showModal({
      title: '到场确认',
      content: `确认已到达「${(this.data.activeTask && this.data.activeTask.room) || '工单位置'}」现场？`,
      confirmText: '确认到场',
      success: (res) => {
        if (res.confirm) {
          this.updateStep({ arrived: true, arrivalMethod: '手动确认' });
          wx.showToast({ title: '已确认到场', icon: 'success' });
        }
      }
    });
  },
  onFaultChange(e) {
    if (!this.guardStep(2)) return;
    this.updateStep({ faultIndex: Number(e.detail.value || 0) });
  },
  onResultChange(e) {
    if (!this.guardStep(2)) return;
    this.updateStep({ resultIndex: Number(e.detail.value || 0) });
  },
  onDurationChange(e) {
    if (!this.guardStep(2)) return;
    this.updateStep({ durationIndex: Number(e.detail.value || 0) });
  },
  onInput(e) {
    if (!this.guardStep(2)) return;
    this.updateStep({ [e.currentTarget.dataset.field]: e.detail.value });
  },
  guardStep(step) {
    if (step >= 2 && !this.data.arrived) {
      wx.showToast({ title: '请先完成到场确认', icon: 'none' });
      return false;
    }
    if (step >= 3 && !this.data.repaired) {
      wx.showToast({ title: '请先完成处理登记', icon: 'none' });
      return false;
    }
    return true;
  },
  // 第二步完成：确认处理登记。
  confirmRepairDone() {
    if (!this.guardStep(2)) return;
    if (this.data.repaired) {
      this.updateStep({ repaired: false, accepted: false });
      return;
    }
    if (!this.data.remark.trim()) {
      wx.showToast({ title: '请先填写处理说明', icon: 'none' });
      return;
    }
    this.updateStep({ repaired: true });
    wx.showToast({ title: '处理登记完成', icon: 'success' });
  },
  choosePhoto() {
    if (!this.guardStep(3)) return;
    wx.chooseImage({
      count: 4,
      sizeType: ['compressed'],
      sourceType: ['camera', 'album'],
      success: (res) => {
        this.setData({ photos: this.data.photos.concat(res.tempFilePaths || []).slice(0, 8) });
      }
    });
  },
  toggleAccepted() {
    if (!this.guardStep(3)) return;
    this.updateStep({ accepted: !this.data.accepted });
  },
  validateForm() {
    if (!this.data.activeTask) return '请选择维修工单';
    if (!this.data.arrived) return '请先完成到场确认';
    if (!this.data.repaired) return '请先完成处理登记';
    if (!this.data.remark.trim()) return '请填写处理说明';
    if (!this.data.accepted) return '请确认现场验收通过';
    return '';
  },
  resetForm() {
    this.setData({
      faultIndex: 0,
      resultIndex: 0,
      durationIndex: 1,
      currentStep: 1,
      arrived: false,
      arrivalMethod: '',
      repaired: false,
      accepted: false,
      materials: '',
      remark: '',
      followUp: '',
      scanText: '',
      photos: []
    });
  },
  buildPayload(photos, evidencePending) {
    return {
      arrived: this.data.arrived,
      repaired: this.data.repaired,
      accepted: this.data.accepted,
      faultType: this.data.faultOptions[this.data.faultIndex],
      resultType: this.data.resultOptions[this.data.resultIndex],
      duration: this.data.durationOptions[this.data.durationIndex],
      materials: this.data.materials.trim(),
      remark: [this.data.remark.trim(), this.data.arrivalMethod ? `到场方式：${this.data.arrivalMethod}` : ''].filter(Boolean).join('；'),
      followUp: this.data.followUp.trim(),
      scanText: this.data.scanText,
      photos,
      ...(evidencePending ? { evidencePending: true } : {})
    };
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
    const taskId = this.data.activeTask.id;
    let payload = null;
    try {
      let uploadedPhotos = [];
      let evidencePending = false;
      try {
        for (const photo of this.data.photos) {
          if (/^https?:\/\//i.test(photo)) {
            uploadedPhotos.push(photo);
          } else {
            const uploaded = await uploadStaffTaskEvidence(photo, { bizType: 'staff-repair-acceptance' });
            uploadedPhotos.push(uploaded.fileUrl || photo);
          }
        }
      } catch (uploadError) {
        if (!isNetworkError(uploadError)) throw uploadError;
        // 弱网下验收照没传上去：只保留已上传部分，文本先行提交/暂存。
        uploadedPhotos = this.data.photos.filter((photo) => /^https?:\/\//i.test(photo));
        evidencePending = true;
      }
      payload = this.buildPayload(uploadedPhotos, evidencePending);
      await submitRepairReceipt(taskId, payload);
      wx.hideLoading();
      clearDraft(DRAFT_PAGE, taskId);
      wx.showToast({ title: '维修已回执', icon: 'success' });
      this.resetForm();
      this.loadData();
    } catch (error) {
      wx.hideLoading();
      if (isNetworkError(error) && payload) {
        enqueue('task-complete', { kind: 'repair', taskId, data: payload });
        clearDraft(DRAFT_PAGE, taskId);
        this.resetForm();
        wx.showToast({ title: '已离线暂存，网络恢复后自动提交', icon: 'none' });
      } else {
        wx.showToast({ title: error.message || '提交失败', icon: 'none' });
      }
    } finally {
      this.setData({ submitting: false });
    }
  }
});
