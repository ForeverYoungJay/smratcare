const {
  getMedicationTasks,
  submitMedicationReceipt,
  getInspectionTasks,
  submitInspectionReceipt,
  uploadStaffTaskEvidence
} = require('../../../services/staff');
const { enqueue, isNetworkError } = require('../../../utils/offline-queue');
const { saveDraft, loadDraft, clearDraft } = require('../../../utils/form-draft');

const modeMap = {
  MEDICATION: {
    title: '用药确认',
    subtitle: '三查七对逐项核对、给药结果、异常反应和扫码拍照留痕',
    emptyText: '暂无用药确认任务',
    moduleText: '用药'
  },
  INSPECTION: {
    title: '巡检复核',
    subtitle: '到房查验、复测体征、处理措施和医生/主管同步',
    emptyText: '暂无巡检复核任务',
    moduleText: '巡检'
  }
};

const medicationResultOptions = ['已服药', '拒服', '漏服', '暂缓给药', '需医生复核'];
const medicationMethodOptions = ['口服', '外用', '注射', '雾化', '其他'];
const inspectionResultOptions = ['正常', '需继续观察', '异常已上报', '转医生处理'];
const riskLevelOptions = ['一般', '关注', '紧急'];

// 用药“三查七对”核对项：必须逐项勾选后才能提交。
const MED_CHECKLIST_ITEMS = [
  '查对老人身份（床号/腕带）',
  '查对药品名称',
  '查对剂量',
  '查对用药时间',
  '查对给药途径',
  '查对药品有效期',
  '确认给药完成并观察反应'
];

const DRAFT_FIELDS = [
  'medChecklist', 'medicationResultIndex', 'medicationMethodIndex', 'adverseText',
  'inspectionResultIndex', 'riskLevelIndex', 'visited', 'vitalsChecked', 'synced',
  'vitalsSummary', 'actionTaken', 'remark', 'scanText'
];

function buildMedChecklist() {
  return MED_CHECKLIST_ITEMS.map((label) => ({ label, checked: false }));
}

function normalizeTask(item) {
  return {
    ...item,
    metaText: `${item.resident || '长者'} · ${item.room || '位置未填'} · ${item.time || '时间待定'}`
  };
}

Page({
  data: {
    mode: 'MEDICATION',
    config: modeMap.MEDICATION,
    loading: false,
    submitting: false,
    loadError: '',
    submitError: '',
    submitSuccess: '',
    lastReceiptSummary: '',
    tasks: [],
    activeTaskId: '',
    activeTask: null,
    medicationResultOptions,
    medicationMethodOptions,
    inspectionResultOptions,
    riskLevelOptions,
    medicationResultIndex: 0,
    medicationMethodIndex: 0,
    inspectionResultIndex: 0,
    riskLevelIndex: 1,
    medChecklist: buildMedChecklist(),
    medAllChecked: false,
    adverseText: '',
    visited: false,
    vitalsChecked: false,
    synced: false,
    vitalsSummary: '',
    actionTaken: '',
    remark: '',
    scanText: '',
    photos: []
  },
  onLoad(options = {}) {
    const mode = options.mode === 'INSPECTION' ? 'INSPECTION' : 'MEDICATION';
    this.draftRestoredFor = '';
    this.setData({
      mode,
      config: modeMap[mode],
      activeTaskId: options.id || ''
    });
  },
  onShow() {
    getApp().ensureLogin();
    this.loadData();
  },
  onPullDownRefresh() {
    this.loadData(true);
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
      const list = this.data.mode === 'INSPECTION' ? await getInspectionTasks() : await getMedicationTasks();
      const tasks = (list || []).map(normalizeTask);
      const activeTask = tasks.find((item) => item.id === this.data.activeTaskId) || tasks[0] || null;
      this.setData({
        tasks,
        activeTask,
        activeTaskId: activeTask ? activeTask.id : ''
      });
      this.restoreFormDraft();
    } catch (error) {
      this.setData({ loadError: error.message || '医护任务加载失败' });
    } finally {
      this.setData({ loading: false });
      if (fromPullDown) wx.stopPullDownRefresh();
    }
  },
  draftPageKey() {
    return `staff-clinical-${this.data.mode}`;
  },
  collectDraftData() {
    const draft = {};
    DRAFT_FIELDS.forEach((field) => {
      draft[field] = this.data[field];
    });
    return draft;
  },
  hasFormContent() {
    if (this.data.mode === 'INSPECTION') {
      return !!(this.data.visited || this.data.vitalsChecked || this.data.synced
        || this.data.vitalsSummary.trim() || this.data.actionTaken.trim() || this.data.remark.trim() || this.data.scanText);
    }
    return !!(this.data.medChecklist.some((item) => item.checked)
      || this.data.adverseText.trim() || this.data.remark.trim() || this.data.scanText);
  },
  saveFormDraft() {
    if (!this.data.activeTaskId || !this.hasFormContent()) return;
    saveDraft(this.draftPageKey(), this.data.activeTaskId, this.collectDraftData());
  },
  scheduleDraftSave() {
    if (this._draftTimer) clearTimeout(this._draftTimer);
    this._draftTimer = setTimeout(() => {
      this.saveFormDraft();
    }, 500);
  },
  restoreFormDraft() {
    const taskId = this.data.activeTaskId;
    const restoreKey = `${this.data.mode}-${taskId}`;
    if (!taskId || this.draftRestoredFor === restoreKey) return;
    this.draftRestoredFor = restoreKey;
    const draft = loadDraft(this.draftPageKey(), taskId);
    if (!draft) return;
    const patch = {};
    DRAFT_FIELDS.forEach((field) => {
      if (draft[field] !== undefined) patch[field] = draft[field];
    });
    if (Array.isArray(patch.medChecklist)) {
      const restored = buildMedChecklist();
      patch.medChecklist.forEach((item, index) => {
        if (restored[index]) restored[index].checked = !!(item && item.checked);
      });
      patch.medChecklist = restored;
      patch.medAllChecked = restored.every((item) => item.checked);
    }
    this.setData(patch);
    wx.showToast({ title: '已恢复未提交草稿', icon: 'none' });
  },
  switchMode(e) {
    const mode = e.currentTarget.dataset.mode;
    if (!mode || mode === this.data.mode) return;
    this.saveFormDraft();
    this.draftRestoredFor = '';
    this.setData({
      mode,
      config: modeMap[mode],
      activeTaskId: '',
      activeTask: null,
      submitError: '',
      submitSuccess: ''
    });
    this.resetForm();
    this.loadData();
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
      medicationResultIndex: 0,
      medicationMethodIndex: 0,
      inspectionResultIndex: 0,
      riskLevelIndex: 1,
      medChecklist: buildMedChecklist(),
      medAllChecked: false,
      adverseText: '',
      visited: false,
      vitalsChecked: false,
      synced: false,
      vitalsSummary: '',
      actionTaken: '',
      remark: '',
      scanText: '',
      photos: [],
      submitError: '',
      submitSuccess: ''
    });
  },
  toggleMedChecklist(e) {
    const index = Number(e.currentTarget.dataset.index);
    const medChecklist = this.data.medChecklist.map((item, i) => (
      i === index ? { ...item, checked: !item.checked } : item
    ));
    this.setData({
      medChecklist,
      medAllChecked: medChecklist.every((item) => item.checked),
      submitError: '',
      submitSuccess: ''
    });
    this.scheduleDraftSave();
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
  onMedicationResultChange(e) {
    this.setData({ medicationResultIndex: Number(e.detail.value || 0), submitError: '', submitSuccess: '' });
    this.scheduleDraftSave();
  },
  onMedicationMethodChange(e) {
    this.setData({ medicationMethodIndex: Number(e.detail.value || 0), submitError: '', submitSuccess: '' });
    this.scheduleDraftSave();
  },
  onInspectionResultChange(e) {
    this.setData({ inspectionResultIndex: Number(e.detail.value || 0), submitError: '', submitSuccess: '' });
    this.scheduleDraftSave();
  },
  onRiskLevelChange(e) {
    this.setData({ riskLevelIndex: Number(e.detail.value || 0), submitError: '', submitSuccess: '' });
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
  validateMedication() {
    if (!this.data.activeTask) return '请选择用药任务';
    const firstUnchecked = this.data.medChecklist.find((item) => !item.checked);
    if (firstUnchecked) return `请完成三查七对：${firstUnchecked.label}`;
    return '';
  },
  validateInspection() {
    if (!this.data.activeTask) return '请选择巡检任务';
    if (!this.data.visited) return '请确认已到房查验';
    if (!this.data.vitalsChecked) return '请确认已复测体征';
    if (!this.data.synced) return '请确认已同步医生或主管';
    if (!this.data.vitalsSummary.trim()) return '请填写体征摘要';
    if (!this.data.actionTaken.trim()) return '请填写处理措施';
    return '';
  },
  buildPayload(photos, evidencePending) {
    if (this.data.mode === 'INSPECTION') {
      return {
        visited: this.data.visited,
        vitalsChecked: this.data.vitalsChecked,
        synced: this.data.synced,
        inspectionResult: this.data.inspectionResultOptions[this.data.inspectionResultIndex],
        riskLevel: this.data.riskLevelOptions[this.data.riskLevelIndex],
        vitalsSummary: this.data.vitalsSummary.trim(),
        actionTaken: this.data.actionTaken.trim(),
        remark: this.data.remark.trim(),
        scanText: this.data.scanText,
        photos,
        ...(evidencePending ? { evidencePending: true } : {})
      };
    }
    const checklist = {};
    this.data.medChecklist.forEach((item) => {
      checklist[item.label] = !!item.checked;
    });
    return {
      identityChecked: !!this.data.medChecklist[0].checked,
      drugChecked: this.data.medChecklist.slice(1, 6).every((item) => item.checked),
      administered: !!this.data.medChecklist[6].checked,
      checklist,
      medicationResult: this.data.medicationResultOptions[this.data.medicationResultIndex],
      medicationMethod: this.data.medicationMethodOptions[this.data.medicationMethodIndex],
      adverseText: this.data.adverseText.trim(),
      remark: this.data.remark.trim(),
      scanText: this.data.scanText,
      photos,
      ...(evidencePending ? { evidencePending: true } : {})
    };
  },
  async submitClinical() {
    if (this.data.submitting) return;
    const validation = this.data.mode === 'INSPECTION' ? this.validateInspection() : this.validateMedication();
    if (validation) {
      wx.showToast({ title: validation, icon: 'none' });
      return;
    }
    this.setData({ submitting: true, submitError: '', submitSuccess: '' });
    wx.showLoading({ title: this.data.photos.length ? '上传留痕中' : '提交中', mask: true });
    const mode = this.data.mode;
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
            const uploaded = await uploadStaffTaskEvidence(photo, {
              bizType: mode === 'INSPECTION' ? 'staff-inspection-evidence' : 'staff-medication-evidence'
            });
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
      if (mode === 'INSPECTION') {
        await submitInspectionReceipt(taskId, payload);
      } else {
        await submitMedicationReceipt(taskId, payload);
      }
      wx.hideLoading();
      clearDraft(this.draftPageKey(), taskId);
      const summary = mode === 'INSPECTION'
        ? `${this.data.inspectionResultOptions[this.data.inspectionResultIndex]} · ${this.data.activeTask.resident || this.data.activeTask.room || '巡检对象'}`
        : `${this.data.medicationResultOptions[this.data.medicationResultIndex]} · ${this.data.activeTask.resident || this.data.activeTask.room || '用药对象'}`;
      this.resetForm();
      this.setData({
        lastReceiptSummary: summary,
        submitSuccess: `${this.data.config.moduleText}回执已提交，可在执行记录和后台台账继续查看。`
      });
      wx.showToast({ title: '回执已提交', icon: 'success' });
      this.loadData();
    } catch (error) {
      wx.hideLoading();
      if (isNetworkError(error) && payload) {
        enqueue('task-complete', {
          kind: mode === 'INSPECTION' ? 'inspection' : 'medication',
          taskId,
          data: payload
        });
        clearDraft(this.draftPageKey(), taskId);
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
