const { getTaskDetail, completeTask, uploadStaffTaskEvidence } = require('../../services/staff');

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
  },
  onLoad(options = {}) {
    this.setData({ id: options.id || '' });
    this.initRecorder();
    this.loadData();
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
  },
  toggleCheck(e) {
    const key = e.currentTarget.dataset.key;
    this.setData({
      [`checkedMap.${key}`]: !this.data.checkedMap[key]
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
    try {
      const uploadedPhotos = [];
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
      let uploadedVoice = null;
      if (this.data.voice && this.data.voice.filePath) {
        const uploaded = await uploadStaffTaskEvidence(this.data.voice.filePath, {
          bizType: `staff-task-${this.data.task.module || 'evidence'}-voice`
        });
        uploadedVoice = {
          voiceUrl: uploaded.fileUrl || this.data.voice.filePath,
          voiceDurationSec: this.data.voice.durationSec || 0
        };
      }
      await completeTask(this.data.task.id, {
        remark: this.data.remark,
        checkedMap: this.data.checkedMap,
        photos: uploadedPhotos,
        scanText: this.data.scanText,
        ...(uploadedVoice || {})
      });
      wx.hideLoading();
      wx.showToast({ title: '已提交回执', icon: 'success' });
      setTimeout(() => wx.navigateBack(), 500);
    } catch (error) {
      wx.hideLoading();
      wx.showToast({ title: error.message || '提交失败', icon: 'none' });
    } finally {
      this.setData({ submitting: false });
    }
  }
});
