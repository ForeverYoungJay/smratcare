const {
  getMedicationTasks,
  submitMedicationReceipt,
  getInspectionTasks,
  submitInspectionReceipt,
  uploadStaffTaskEvidence
} = require('../../services/staff');

const modeMap = {
  MEDICATION: {
    title: '用药确认',
    subtitle: '三查七对、给药结果、异常反应和扫码拍照留痕',
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
    identityChecked: false,
    drugChecked: false,
    administered: false,
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
  async loadData() {
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
    } catch (error) {
      this.setData({ loadError: error.message || '医护任务加载失败' });
    } finally {
      this.setData({ loading: false });
    }
  },
  switchMode(e) {
    const mode = e.currentTarget.dataset.mode;
    if (!mode || mode === this.data.mode) return;
    this.setData({
      mode,
      config: modeMap[mode],
      activeTaskId: '',
      activeTask: null
    });
    this.resetForm();
    this.loadData();
  },
  selectTask(e) {
    const id = e.currentTarget.dataset.id;
    const activeTask = this.data.tasks.find((item) => item.id === id) || null;
    this.setData({ activeTaskId: id, activeTask });
    this.resetForm();
  },
  resetForm() {
    this.setData({
      medicationResultIndex: 0,
      medicationMethodIndex: 0,
      inspectionResultIndex: 0,
      riskLevelIndex: 1,
      identityChecked: false,
      drugChecked: false,
      administered: false,
      adverseText: '',
      visited: false,
      vitalsChecked: false,
      synced: false,
      vitalsSummary: '',
      actionTaken: '',
      remark: '',
      scanText: '',
      photos: []
    });
  },
  toggleCheck(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({ [field]: !this.data[field] });
  },
  onMedicationResultChange(e) {
    this.setData({ medicationResultIndex: Number(e.detail.value || 0) });
  },
  onMedicationMethodChange(e) {
    this.setData({ medicationMethodIndex: Number(e.detail.value || 0) });
  },
  onInspectionResultChange(e) {
    this.setData({ inspectionResultIndex: Number(e.detail.value || 0) });
  },
  onRiskLevelChange(e) {
    this.setData({ riskLevelIndex: Number(e.detail.value || 0) });
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
  validateMedication() {
    if (!this.data.activeTask) return '请选择用药任务';
    if (!this.data.identityChecked) return '请先核对老人身份';
    if (!this.data.drugChecked) return '请先核对药品和剂量';
    if (!this.data.administered) return '请确认给药结果';
    return '';
  },
  validateInspection() {
    if (!this.data.activeTask) return '请选择巡检任务';
    if (!this.data.visited) return '请确认已到房查验';
    if (!this.data.vitalsChecked) return '请确认已复测体征';
    if (!this.data.synced) return '请确认已同步医生或主管';
    if (!this.data.actionTaken.trim()) return '请填写处理措施';
    return '';
  },
  async uploadPhotos() {
    const uploadedPhotos = [];
    for (const photo of this.data.photos) {
      if (/^https?:\/\//i.test(photo)) {
        uploadedPhotos.push(photo);
      } else {
        const uploaded = await uploadStaffTaskEvidence(photo, {
          bizType: this.data.mode === 'INSPECTION' ? 'staff-inspection-evidence' : 'staff-medication-evidence'
        });
        uploadedPhotos.push(uploaded.fileUrl || photo);
      }
    }
    return uploadedPhotos;
  },
  async submitClinical() {
    if (this.data.submitting) return;
    const validation = this.data.mode === 'INSPECTION' ? this.validateInspection() : this.validateMedication();
    if (validation) {
      wx.showToast({ title: validation, icon: 'none' });
      return;
    }
    this.setData({ submitting: true });
    wx.showLoading({ title: this.data.photos.length ? '上传留痕中' : '提交中', mask: true });
    try {
      const photos = await this.uploadPhotos();
      if (this.data.mode === 'INSPECTION') {
        await submitInspectionReceipt(this.data.activeTask.id, {
          visited: this.data.visited,
          vitalsChecked: this.data.vitalsChecked,
          synced: this.data.synced,
          inspectionResult: this.data.inspectionResultOptions[this.data.inspectionResultIndex],
          riskLevel: this.data.riskLevelOptions[this.data.riskLevelIndex],
          vitalsSummary: this.data.vitalsSummary.trim(),
          actionTaken: this.data.actionTaken.trim(),
          remark: this.data.remark.trim(),
          scanText: this.data.scanText,
          photos
        });
      } else {
        await submitMedicationReceipt(this.data.activeTask.id, {
          identityChecked: this.data.identityChecked,
          drugChecked: this.data.drugChecked,
          administered: this.data.administered,
          medicationResult: this.data.medicationResultOptions[this.data.medicationResultIndex],
          medicationMethod: this.data.medicationMethodOptions[this.data.medicationMethodIndex],
          adverseText: this.data.adverseText.trim(),
          remark: this.data.remark.trim(),
          scanText: this.data.scanText,
          photos
        });
      }
      wx.hideLoading();
      wx.showToast({ title: '回执已提交', icon: 'success' });
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
