const { submitIncident, uploadStaffTaskEvidence } = require('../../services/staff');

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
    submitting: false
  },
  onLoad() {
    this.initRecorder();
  },
  onUnload() {
    if (this.recorderManager && this.data.recording) {
      this.recorderManager.stop();
    }
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
  onTypeChange(e) {
    this.setData({ incidentType: this.data.incidentTypes[e.detail.value] });
  },
  onLevelChange(e) {
    this.setData({ level: this.data.levels[e.detail.value] });
  },
  onFieldInput(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({ [field]: e.detail.value });
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
    this.setData({ voice: null });
  },
  scanCode() {
    wx.scanCode({
      onlyFromCamera: false,
      success: (res) => {
        this.setData({ scanText: res.result || '已扫码' });
      },
      fail: () => wx.showToast({ title: '扫码已取消', icon: 'none' })
    });
  },
  async submitForm() {
    if (!this.data.description.trim() || this.data.submitting) {
      wx.showToast({ title: '请填写异常说明', icon: 'none' });
      return;
    }
    this.setData({ submitting: true });
    wx.showLoading({ title: this.data.photos.length ? '上传证据中' : '提交中', mask: true });
    try {
      const uploadedPhotos = [];
      for (const photo of this.data.photos) {
        if (/^https?:\/\//i.test(photo)) {
          uploadedPhotos.push(photo);
        } else {
          const uploaded = await uploadStaffTaskEvidence(photo, { bizType: 'staff-incident-evidence' });
          uploadedPhotos.push(uploaded.fileUrl || photo);
        }
      }
      let uploadedVoice = null;
      if (this.data.voice && this.data.voice.filePath) {
        const uploaded = await uploadStaffTaskEvidence(this.data.voice.filePath, { bizType: 'staff-incident-voice' });
        uploadedVoice = {
          voiceUrl: uploaded.fileUrl || this.data.voice.filePath,
          voiceDurationSec: this.data.voice.durationSec || 0
        };
      }
      await submitIncident({
        incidentType: this.data.incidentType,
        level: this.data.level,
        resident: this.data.resident,
        room: this.data.room,
        description: this.data.description,
        scanText: this.data.scanText,
        photos: uploadedPhotos,
        ...(uploadedVoice || {})
      });
      wx.hideLoading();
      wx.showToast({ title: '已上报', icon: 'success' });
      setTimeout(() => wx.navigateBack(), 500);
    } catch (error) {
      wx.hideLoading();
      wx.showToast({ title: error.message || '上报失败', icon: 'none' });
    } finally {
      this.setData({ submitting: false });
    }
  }
});
