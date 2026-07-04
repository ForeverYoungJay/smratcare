const {
  getMealDeliveryTasks,
  submitMealDeliveryReceipt,
  uploadStaffTaskEvidence
} = require('../../services/staff');
const { enqueue, isNetworkError } = require('../../utils/offline-queue');
const { saveDraft, loadDraft, clearDraft } = require('../../utils/form-draft');

const DRAFT_PAGE = 'staff-meals';
const RENDER_PAGE_SIZE = 20;
const intakeOptions = ['全部吃完', '约 3/4', '约 1/2', '少量进食', '未进食', '拒食/异常'];
const DRAFT_FIELDS = ['intakeIndex', 'mealChecked', 'tabooChecked', 'signed', 'signerName', 'remark', 'exceptionText', 'scanText', 'deliveredAt'];

function normalizeTask(item) {
  return {
    ...item,
    metaText: `${item.resident || '长者'} · ${item.room || '位置未填'} · ${item.time || '时间待定'}`
  };
}

function pad2(value) {
  return value < 10 ? `0${value}` : String(value);
}

function nowText() {
  const now = new Date();
  return `${now.getFullYear()}-${pad2(now.getMonth() + 1)}-${pad2(now.getDate())} ${pad2(now.getHours())}:${pad2(now.getMinutes())}`;
}

// 按当前时间推断餐别：10 点前早餐，10-15 点午餐，其余晚餐。
function currentMealType() {
  const hour = new Date().getHours();
  if (hour < 10) return '早餐';
  if (hour < 15) return '午餐';
  return '晚餐';
}

function taskMealType(task) {
  const text = `${(task && task.title) || ''}${(task && task.mealType) || ''}`;
  if (text.indexOf('早餐') >= 0) return '早餐';
  if (text.indexOf('午餐') >= 0) return '午餐';
  if (text.indexOf('晚餐') >= 0) return '晚餐';
  return '';
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
    hasMore: false,
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
    deliveredAt: '',
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
      this.allTasks = ((await getMealDeliveryTasks()) || []).map(normalizeTask);
      const activeTask = this.allTasks.find((item) => item.id === this.data.activeTaskId) || this.allTasks[0] || null;
      this.setData({
        tasks: this.allTasks.slice(0, RENDER_PAGE_SIZE),
        hasMore: this.allTasks.length > RENDER_PAGE_SIZE,
        activeTask,
        activeTaskId: activeTask ? activeTask.id : '',
        signerName: this.data.signerName || (activeTask && activeTask.resident) || ''
      });
      this.restoreFormDraft();
    } catch (error) {
      this.setData({ loadError: error.message || '送餐任务加载失败' });
    } finally {
      this.setData({ loading: false });
      if (fromPullDown) wx.stopPullDownRefresh();
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
    return !!(this.data.remark.trim() || this.data.exceptionText.trim() || this.data.scanText
      || this.data.mealChecked || this.data.tabooChecked || this.data.signed);
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
    const activeTask = (this.allTasks || []).find((item) => item.id === id) || null;
    this.draftRestoredFor = '';
    this.setData({
      activeTaskId: id,
      activeTask,
      signerName: (activeTask && activeTask.resident) || '',
      submitError: '',
      submitSuccess: '',
      remark: '',
      exceptionText: '',
      scanText: '',
      photos: [],
      mealChecked: false,
      tabooChecked: false,
      signed: false,
      intakeIndex: 1,
      deliveredAt: ''
    });
    this.restoreFormDraft();
  },
  toggleCheck(e) {
    const field = e.currentTarget.dataset.field;
    const nextValue = !this.data[field];
    const patch = {
      [field]: nextValue,
      submitError: '',
      submitSuccess: ''
    };
    // 勾选“送达签收”时自动记录送达时间。
    if (field === 'signed') {
      patch.deliveredAt = nextValue ? nowText() : '';
    }
    this.setData(patch);
    this.scheduleDraftSave();
  },
  onIntakeChange(e) {
    this.setData({ intakeIndex: Number(e.detail.value || 0), submitError: '', submitSuccess: '' });
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
      count: 3,
      sizeType: ['compressed'],
      sourceType: ['camera', 'album'],
      success: (res) => {
        this.setData({
          photos: this.data.photos.concat(res.tempFilePaths || []).slice(0, 6),
          submitError: '',
          submitSuccess: ''
        });
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
  // 餐别与任务不符时二次确认。
  async confirmMealTypeMatch() {
    const expected = taskMealType(this.data.activeTask);
    if (!expected) return true;
    const current = currentMealType();
    if (expected === current) return true;
    return new Promise((resolve) => {
      wx.showModal({
        title: '餐别核对提醒',
        content: `当前时间对应「${current}」，但任务餐别为「${expected}」。请再次确认餐食无误后继续签收。`,
        confirmText: '确认签收',
        cancelText: '再核对',
        success: (res) => resolve(!!res.confirm),
        fail: () => resolve(false)
      });
    });
  },
  resetForm() {
    this.setData({
      remark: '',
      exceptionText: '',
      scanText: '',
      photos: [],
      mealChecked: false,
      tabooChecked: false,
      signed: false,
      intakeIndex: 1,
      deliveredAt: ''
    });
  },
  buildPayload(photos, evidencePending) {
    return {
      mealChecked: this.data.mealChecked,
      tabooChecked: this.data.tabooChecked,
      signed: this.data.signed,
      intake: this.data.intakeOptions[this.data.intakeIndex],
      signerName: this.data.signerName.trim(),
      remark: this.data.remark.trim(),
      exceptionText: this.data.exceptionText.trim(),
      deliveredAt: this.data.deliveredAt || nowText(),
      scanText: this.data.scanText,
      photos,
      ...(evidencePending ? { evidencePending: true } : {})
    };
  },
  async submitMeal() {
    if (this.data.submitting) return;
    const validation = this.validateForm();
    if (validation) {
      wx.showToast({ title: validation, icon: 'none' });
      return;
    }
    const confirmed = await this.confirmMealTypeMatch();
    if (!confirmed) return;
    this.setData({ submitting: true, submitError: '', submitSuccess: '' });
    wx.showLoading({ title: this.data.photos.length ? '上传留痕中' : '提交中', mask: true });
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
            const uploaded = await uploadStaffTaskEvidence(photo, { bizType: 'staff-meal-signoff' });
            uploadedPhotos.push(uploaded.fileUrl || photo);
          }
        }
      } catch (uploadError) {
        if (!isNetworkError(uploadError)) throw uploadError;
        // 弱网下文件没传上去：只保留已上传部分，文本先行提交/暂存。
        uploadedPhotos = this.data.photos.filter((photo) => /^https?:\/\//i.test(photo));
        evidencePending = true;
      }
      payload = this.buildPayload(uploadedPhotos, evidencePending);
      await submitMealDeliveryReceipt(taskId, payload);
      wx.hideLoading();
      clearDraft(DRAFT_PAGE, taskId);
      const summary = `${this.data.intakeOptions[this.data.intakeIndex]} · ${this.data.signerName.trim() || this.data.activeTask.resident || '签收人'}`;
      this.setData({
        lastReceiptSummary: summary,
        submitSuccess: '送餐签收已提交，可在执行记录和后台台账继续查看。'
      });
      wx.showToast({ title: '送餐已签收', icon: 'success' });
      this.resetForm();
      this.loadData();
    } catch (error) {
      wx.hideLoading();
      if (isNetworkError(error) && payload) {
        enqueue('task-complete', { kind: 'meal', taskId, data: payload });
        clearDraft(DRAFT_PAGE, taskId);
        this.resetForm();
        this.setData({ submitSuccess: '', submitError: '' });
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
