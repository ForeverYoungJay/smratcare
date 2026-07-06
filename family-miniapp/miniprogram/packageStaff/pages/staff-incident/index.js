const { submitIncident, uploadStaffTaskEvidence } = require('../../../services/staff');
const { enqueue, isNetworkError } = require('../../../utils/offline-queue');
const { saveDraft, loadDraft, clearDraft } = require('../../../utils/form-draft');

const DRAFT_PAGE = 'staff-incident';
// 异常上报不绑定具体任务，草稿统一挂在 default 键下。
const DRAFT_ID = 'default';
const DRAFT_FIELDS = ['incidentType', 'level', 'resident', 'room', 'description', 'scanText'];

Page({
  data: {
    incidentTypes: ['跌倒风险', '生命体征异常', '用药异常', '设备维修', '餐食异常', '其他'],
    levels: ['一般', '紧急', '重大'],
    incidentType: '跌倒风险',
    level: '一般',
    resident: '',
    room: '',
    description: '',
    scanText: '',
    photos: [],
    voice: null,
    recording: false,
    recordStartAt: 0,
    submitting: false,
    submitError: '',
    submitSuccess: '',
    lastIncidentSummary: ''
  },
  onLoad() {
    this.initRecorder();
    this.restoreFormDraft();
  },
  onHide() {
    this.saveFormDraft();
  },
  onUnload() {
    if (this.recorderManager && this.data.recording) {
      this.recorderManager.stop();
    }
    this.saveFormDraft();
  },
  collectDraftData() {
    const draft = {};
    DRAFT_FIELDS.forEach((field) => {
      draft[field] = this.data[field];
    });
    return draft;
  },
  hasFormContent() {
    return !!(this.data.resident.trim() || this.data.room.trim()
      || this.data.description.trim() || this.data.scanText);
  },
  saveFormDraft() {
    if (!this.hasFormContent()) return;
    saveDraft(DRAFT_PAGE, DRAFT_ID, this.collectDraftData());
  },
  scheduleDraftSave() {
    if (this._draftTimer) clearTimeout(this._draftTimer);
    this._draftTimer = setTimeout(() => {
      this.saveFormDraft();
    }, 500);
  },
  restoreFormDraft() {
    const draft = loadDraft(DRAFT_PAGE, DRAFT_ID);
    if (!draft) return;
    const patch = {};
    DRAFT_FIELDS.forEach((field) => {
      if (draft[field] !== undefined) patch[field] = draft[field];
    });
    if (patch.incidentType && this.data.incidentTypes.indexOf(patch.incidentType) < 0) {
      delete patch.incidentType;
    }
    if (patch.level && this.data.levels.indexOf(patch.level) < 0) {
      delete patch.level;
    }
    this.setData(patch);
    wx.showToast({ title: '已恢复未提交草稿', icon: 'none' });
  },
  initRecorder() {
    if (!wx.getRecorderManager) return;
    this.recorderManager = wx.getRecorderManager();
    this.recorderManager.onStop((res) => {
      const durationMs = Math.max(0, Date.now() - Number(this.data.recordStartAt || 0));
      this.setData({ recording: false, recordStartAt: 0 });
      if (durationMs < 800) {
        wx.showToast({ title: '说话时间太短', icon: 'none' });
        return;
      }
      if (!res || !res.tempFilePath) {
        wx.showToast({ title: '录音文件生成失败', icon: 'none' });
        return;
      }
      const durationSec = res.duration ? Math.max(1, Math.round(res.duration / 1000)) : Math.max(1, Math.round(durationMs / 1000));
      this.setData({
        voice: {
          filePath: res.tempFilePath,
          durationSec,
          durationText: `${durationSec}"`
        },
        submitError: '',
        submitSuccess: ''
      });
    });
    this.recorderManager.onError(() => {
      this.setData({ recording: false, recordStartAt: 0 });
      wx.showToast({ title: '录音失败，请检查麦克风权限', icon: 'none' });
    });
  },
  onTypeChange(e) {
    this.setData({
      incidentType: this.data.incidentTypes[e.detail.value],
      submitError: '',
      submitSuccess: ''
    });
    this.scheduleDraftSave();
  },
  onLevelChange(e) {
    this.setData({
      level: this.data.levels[e.detail.value],
      submitError: '',
      submitSuccess: ''
    });
    this.scheduleDraftSave();
  },
  onFieldInput(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({
      [field]: e.detail.value,
      submitError: '',
      submitSuccess: ''
    });
    this.scheduleDraftSave();
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
  async startRecord() {
    if (!this.recorderManager || this.data.recording || this.data.submitting) return;
    const authorized = await new Promise((resolve) => {
      wx.authorize({
        scope: 'scope.record',
        success: () => resolve(true),
        fail: () => resolve(false)
      });
    });
    if (!authorized) {
      wx.showModal({
        title: '需要麦克风权限',
        content: '请在小程序设置中允许录音后，再按住录入异常语音说明。',
        confirmText: '知道了',
        showCancel: false
      });
      return;
    }
    this.setData({ recording: true, recordStartAt: Date.now() });
    this.recorderManager.start({
      duration: 60000,
      sampleRate: 16000,
      numberOfChannels: 1,
      encodeBitRate: 48000,
      format: 'mp3'
    });
  },
  stopRecord() {
    if (!this.recorderManager || !this.data.recording) return;
    this.recorderManager.stop();
  },
  clearVoice() {
    this.setData({ voice: null, submitError: '', submitSuccess: '' });
  },
  scanCode() {
    wx.scanCode({
      onlyFromCamera: false,
      success: (res) => {
        this.setData({
          scanText: res.result || '已扫码',
          submitError: '',
          submitSuccess: ''
        });
        this.scheduleDraftSave();
      },
      fail: () => wx.showToast({ title: '扫码已取消', icon: 'none' })
    });
  },
  buildPayload(photos, voice, evidencePending) {
    return {
      incidentType: this.data.incidentType,
      level: this.data.level,
      resident: this.data.resident,
      room: this.data.room,
      description: this.data.description,
      scanText: this.data.scanText,
      photos,
      ...(voice || {}),
      ...(evidencePending ? { evidencePending: true } : {})
    };
  },
  async submitForm() {
    if (!this.data.description.trim() || this.data.submitting) {
      wx.showToast({ title: '请填写异常说明', icon: 'none' });
      return;
    }
    this.setData({ submitting: true, submitError: '', submitSuccess: '' });
    wx.showLoading({ title: this.data.photos.length ? '上传证据中' : '提交中', mask: true });
    let payload = null;
    try {
      let uploadedPhotos = [];
      let uploadedVoice = null;
      let evidencePending = false;
      try {
        for (const photo of this.data.photos) {
          if (/^https?:\/\//i.test(photo)) {
            uploadedPhotos.push(photo);
          } else {
            const uploaded = await uploadStaffTaskEvidence(photo, { bizType: 'staff-incident-evidence' });
            uploadedPhotos.push(uploaded.fileUrl || photo);
          }
        }
        if (this.data.voice && this.data.voice.filePath) {
          const uploaded = await uploadStaffTaskEvidence(this.data.voice.filePath, { bizType: 'staff-incident-voice' });
          uploadedVoice = {
            voiceUrl: uploaded.fileUrl || this.data.voice.filePath,
            voiceDurationSec: this.data.voice.durationSec || 0
          };
        }
      } catch (uploadError) {
        if (!isNetworkError(uploadError)) throw uploadError;
        // 弱网下照片/语音没传上去：只保留已上传部分，文本先行提交/暂存。
        uploadedPhotos = this.data.photos.filter((photo) => /^https?:\/\//i.test(photo));
        uploadedVoice = null;
        evidencePending = true;
      }
      payload = this.buildPayload(uploadedPhotos, uploadedVoice, evidencePending);
      await submitIncident(payload);
      wx.hideLoading();
      clearDraft(DRAFT_PAGE, DRAFT_ID);
      const summary = `${this.data.incidentType} · ${this.data.level}${this.data.room ? ` · ${this.data.room}` : ''}`;
      this.setData({
        resident: '',
        room: '',
        description: '',
        scanText: '',
        photos: [],
        voice: null,
        submitSuccess: '异常事件已提交，后台可继续跟踪处理。',
        lastIncidentSummary: summary
      });
      wx.showToast({ title: '已上报', icon: 'success' });
    } catch (error) {
      wx.hideLoading();
      if (isNetworkError(error) && payload) {
        enqueue('incident', payload);
        clearDraft(DRAFT_PAGE, DRAFT_ID);
        this.setData({
          resident: '',
          room: '',
          description: '',
          scanText: '',
          photos: [],
          voice: null
        });
        wx.showToast({ title: '已离线暂存，网络恢复后自动提交', icon: 'none' });
      } else {
        this.setData({ submitError: error.message || '上报失败，请稍后重试' });
        wx.showToast({ title: error.message || '上报失败', icon: 'none' });
      }
    } finally {
      this.setData({ submitting: false });
    }
  }
});
