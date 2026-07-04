const { getTaskDetail, completeTask, uploadStaffTaskEvidence } = require('../../services/staff');
const { enqueue, isNetworkError } = require('../../utils/offline-queue');
const { saveDraft, loadDraft, clearDraft } = require('../../utils/form-draft');

const DRAFT_PAGE = 'staff-task-detail';
const DRAFT_FIELDS = ['remark', 'checkedMap', 'scanText'];

Page({
  data: {
    id: '',
    task: null,
    remark: '',
    checkedMap: {},
    photos: [],
    voice: null,
    recording: false,
    recordStartAt: 0,
    scanText: '',
    submitting: false
  },
  onUnload() {
    if (this.recorderManager && this.data.recording) {
      this.recorderManager.stop();
    }
    this.saveFormDraft();
  },
  onHide() {
    this.saveFormDraft();
  },
  onLoad(options = {}) {
    this.setData({ id: options.id || '' });
    this.initRecorder();
    this.loadData();
    this.restoreFormDraft();
  },
  hasFormContent() {
    return !!(String(this.data.remark || '').trim() || this.data.scanText
      || Object.keys(this.data.checkedMap || {}).some((key) => this.data.checkedMap[key]));
  },
  saveFormDraft() {
    if (!this.data.id || !this.hasFormContent()) return;
    const draft = {};
    DRAFT_FIELDS.forEach((field) => {
      draft[field] = this.data[field];
    });
    saveDraft(DRAFT_PAGE, this.data.id, draft);
  },
  scheduleDraftSave() {
    if (this._draftTimer) clearTimeout(this._draftTimer);
    this._draftTimer = setTimeout(() => {
      this.saveFormDraft();
    }, 500);
  },
  restoreFormDraft() {
    if (!this.data.id) return;
    const draft = loadDraft(DRAFT_PAGE, this.data.id);
    if (!draft) return;
    const patch = {};
    DRAFT_FIELDS.forEach((field) => {
      if (draft[field] !== undefined) patch[field] = draft[field];
    });
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
        }
      });
    });
    this.recorderManager.onError(() => {
      this.setData({ recording: false, recordStartAt: 0 });
      wx.showToast({ title: '录音失败，请检查麦克风权限', icon: 'none' });
    });
  },
  async loadData() {
    const task = await getTaskDetail(this.data.id);
    this.setData({ task });
  },
  onRemarkInput(e) {
    this.setData({ remark: e.detail.value });
    this.scheduleDraftSave();
  },
  toggleCheck(e) {
    const key = e.currentTarget.dataset.key;
    this.setData({
      [`checkedMap.${key}`]: !this.data.checkedMap[key]
    });
    this.scheduleDraftSave();
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
        content: '请在小程序设置中允许录音后，再按住录入现场语音。',
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
    this.setData({ voice: null });
  },
  scanCode() {
    wx.scanCode({
      onlyFromCamera: false,
      success: (res) => {
        this.setData({ scanText: res.result || '已扫码' });
        this.scheduleDraftSave();
      },
      fail: () => {
        wx.showToast({ title: '扫码已取消', icon: 'none' });
      }
    });
  },
  async submitDone() {
    if (!this.data.task || this.data.submitting) return;
    this.setData({ submitting: true });
    wx.showLoading({ title: this.data.photos.length ? '上传证据中' : '提交中', mask: true });
    const taskId = this.data.task.id;
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
            const uploaded = await uploadStaffTaskEvidence(photo, {
              bizType: `staff-task-${this.data.task.module || 'evidence'}`
            });
            uploadedPhotos.push(uploaded.fileUrl || photo);
          }
        }
        if (this.data.voice && this.data.voice.filePath) {
          const uploaded = await uploadStaffTaskEvidence(this.data.voice.filePath, {
            bizType: `staff-task-${this.data.task.module || 'evidence'}-voice`
          });
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
      payload = {
        remark: this.data.remark,
        checkedMap: this.data.checkedMap,
        photos: uploadedPhotos,
        scanText: this.data.scanText,
        ...(uploadedVoice || {}),
        ...(evidencePending ? { evidencePending: true } : {})
      };
      await completeTask(taskId, payload);
      wx.hideLoading();
      clearDraft(DRAFT_PAGE, taskId);
      wx.showToast({ title: '已提交回执', icon: 'success' });
      setTimeout(() => wx.navigateBack(), 500);
    } catch (error) {
      wx.hideLoading();
      if (isNetworkError(error) && payload) {
        enqueue('task-complete', { taskId, data: payload });
        clearDraft(DRAFT_PAGE, taskId);
        wx.showToast({ title: '已离线暂存，网络恢复后自动提交', icon: 'none' });
        setTimeout(() => wx.navigateBack(), 500);
      } else {
        wx.showToast({ title: error.message || '提交失败', icon: 'none' });
      }
    } finally {
      this.setData({ submitting: false });
    }
  }
});
