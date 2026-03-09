const {
  bookVideoVisit,
  getCommunicationMessages,
  getCommunicationTemplates,
  sendCommunicationMessage,
  uploadVoiceMessage
} = require('../../services/family');

const ROLE_OPTIONS = ['责任护士', '责任护工', '生活管家', '客服中心'];
const TYPE_OPTIONS = [
  { value: 'text', label: '图文消息' },
  { value: 'voice', label: '语音留言' },
  { value: 'service', label: '服务咨询' },
  { value: 'video', label: '探视预约' }
];
const DRAFT_KEY_PREFIX = 'family_comm_draft_';

function resolveTypeLabel(type) {
  const found = TYPE_OPTIONS.find((item) => item.value === type);
  if (found) {
    return found.label;
  }
  if (type === 'video') {
    return '探视预约';
  }
  return '普通消息';
}

function nextDayVideoTime() {
  const now = new Date();
  const next = new Date(now.getTime() + 24 * 60 * 60 * 1000);
  const year = next.getFullYear();
  const month = `${next.getMonth() + 1}`.padStart(2, '0');
  const day = `${next.getDate()}`.padStart(2, '0');
  return `${year}-${month}-${day} 15:00`;
}

function resolveTypeIndex(msgType) {
  const idx = TYPE_OPTIONS.findIndex((item) => item.value === msgType);
  return idx >= 0 ? idx : 0;
}

function draftKey(role, msgType) {
  return `${DRAFT_KEY_PREFIX}${role || 'default'}_${msgType || 'text'}`;
}

Page({
  data: {
    roleOptions: ROLE_OPTIONS,
    typeOptions: TYPE_OPTIONS,
    roleIndex: 0,
    typeIndex: 0,
    videoTime: nextDayVideoTime(),
    content: '',
    voiceTranscript: '',
    voiceFileUrl: '',
    voiceFileName: '',
    voiceDurationSec: null,
    messages: [],
    templates: [],
    filteredTemplates: [],
    draftRestored: false,
    loading: false,
    sending: false,
    uploadingVoice: false
  },
  onLoad(query) {
    const mode = query && query.mode ? String(query.mode).toLowerCase() : '';
    const idx = resolveTypeIndex(mode === 'video' ? 'video' : mode === 'voice' ? 'voice' : 'text');
    this.setData({ typeIndex: idx });
  },
  async onShow() {
    getApp().ensureLogin();
    await Promise.all([this.loadMessages(), this.loadTemplates()]);
    this.restoreDraft();
  },
  async loadMessages() {
    this.setData({ loading: true });
    try {
      const list = await getCommunicationMessages(1, 50);
      const messages = (list || []).map((item) => ({
        ...item,
        msgTypeText: resolveTypeLabel(item.msgType || ''),
        isVoice: String(item.msgType || '').toLowerCase() === 'voice',
        voiceDurationText: item && item.mediaDurationSec ? `${item.mediaDurationSec}秒` : ''
      }));
      this.setData({ messages });
    } finally {
      this.setData({ loading: false });
    }
  },
  async loadTemplates() {
    const templates = await getCommunicationTemplates();
    this.setData({ templates: templates || [] });
    this.updateTemplateView();
  },
  updateTemplateView() {
    const role = this.data.roleOptions[this.data.roleIndex] || '';
    const type = this.data.typeOptions[this.data.typeIndex] || TYPE_OPTIONS[0];
    const list = this.data.templates || [];
    const filteredTemplates = list.filter((item) => item.targetRole === role || item.msgType === type.value);
    this.setData({
      filteredTemplates: filteredTemplates.length > 0 ? filteredTemplates : list.slice(0, 5)
    });
  },
  onRoleChange(e) {
    this.setData({ roleIndex: Number(e.detail.value), draftRestored: false });
    this.updateTemplateView();
    this.restoreDraft();
  },
  onTypeChange(e) {
    this.setData({ typeIndex: Number(e.detail.value), draftRestored: false });
    this.updateTemplateView();
    this.restoreDraft();
  },
  onContentInput(e) {
    const content = e.detail.value;
    this.setData({ content });
    this.saveDraft(content);
  },
  onTimeInput(e) {
    this.setData({ videoTime: e.detail.value });
  },
  onTranscriptInput(e) {
    this.setData({ voiceTranscript: e.detail.value });
  },
  useTemplate(e) {
    const idx = Number(e.currentTarget.dataset.index);
    const template = this.data.filteredTemplates[idx];
    if (!template) {
      return;
    }
    const roleIndex = this.data.roleOptions.findIndex((item) => item === template.targetRole);
    const typeIndex = resolveTypeIndex(template.msgType);
    this.setData({
      roleIndex: roleIndex >= 0 ? roleIndex : this.data.roleIndex,
      typeIndex,
      content: template.content,
      draftRestored: false
    });
    this.updateTemplateView();
    this.saveDraft(template.content);
  },
  currentDraftKey() {
    const role = this.data.roleOptions[this.data.roleIndex] || '';
    const type = (this.data.typeOptions[this.data.typeIndex] || TYPE_OPTIONS[0]).value;
    return draftKey(role, type);
  },
  restoreDraft() {
    const key = this.currentDraftKey();
    const draft = wx.getStorageSync(key) || '';
    if (!draft) {
      this.setData({ content: '', draftRestored: false });
      return;
    }
    this.setData({
      content: draft,
      draftRestored: true
    });
  },
  saveDraft(content) {
    const key = this.currentDraftKey();
    const trimmed = (content || '').trim();
    if (!trimmed) {
      wx.removeStorageSync(key);
      this.setData({ draftRestored: false });
      return;
    }
    wx.setStorageSync(key, content);
  },
  async chooseVoiceFile() {
    if (this.data.uploadingVoice) {
      return;
    }
    this.setData({ uploadingVoice: true });
    try {
      const chooseResult = await new Promise((resolve, reject) => {
        wx.chooseMessageFile({
          count: 1,
          type: 'file',
          extension: ['m4a', 'mp3', 'wav', 'aac', 'amr'],
          success: resolve,
          fail: reject
        });
      });
      const file = chooseResult && chooseResult.tempFiles && chooseResult.tempFiles[0];
      if (!file || !file.path) {
        throw new Error('未获取到语音文件');
      }
      const uploaded = await uploadVoiceMessage(file.path, { bizType: 'family-voice' });
      this.setData({
        voiceFileUrl: uploaded.fileUrl || '',
        voiceFileName: uploaded.originalFileName || uploaded.fileName || '语音留言',
        voiceDurationSec: file.time ? Number(file.time) : null
      });
      wx.showToast({ title: '语音文件上传成功', icon: 'none' });
    } catch (error) {
      wx.showToast({ title: error.message || '语音上传失败', icon: 'none' });
    } finally {
      this.setData({ uploadingVoice: false });
    }
  },
  clearVoiceFile() {
    this.setData({
      voiceFileUrl: '',
      voiceFileName: '',
      voiceDurationSec: null
    });
  },
  async sendMessage() {
    const targetRole = this.data.roleOptions[this.data.roleIndex] || '责任护士';
    const type = this.data.typeOptions[this.data.typeIndex] || TYPE_OPTIONS[0];
    let content = (this.data.content || '').trim();
    const payload = {
      targetRole,
      msgType: type.value
    };
    if (type.value === 'voice') {
      if (!(this.data.voiceFileUrl || '').trim()) {
        wx.showToast({ title: '请先上传语音文件', icon: 'none' });
        return;
      }
      if (!content) {
        content = '已发送语音留言，请协助播放给老人。';
      }
      payload.mediaUrl = this.data.voiceFileUrl;
      payload.mediaName = this.data.voiceFileName || 'family-voice.m4a';
      if (this.data.voiceDurationSec) {
        payload.mediaDurationSec = Number(this.data.voiceDurationSec);
      }
      const transcript = (this.data.voiceTranscript || '').trim();
      if (transcript) {
        payload.transcript = transcript;
      }
    } else if (!content) {
      wx.showToast({ title: '请输入消息内容', icon: 'none' });
      return;
    }
    payload.content = content;
    this.setData({ sending: true });
    try {
      await sendCommunicationMessage(payload);
      wx.removeStorageSync(this.currentDraftKey());
      this.setData({
        content: '',
        voiceTranscript: '',
        voiceFileUrl: '',
        voiceFileName: '',
        voiceDurationSec: null,
        draftRestored: false
      });
      wx.showToast({ title: '发送成功', icon: 'success' });
      await this.loadMessages();
    } catch (error) {
      wx.showToast({ title: '发送失败，请重试', icon: 'none' });
    } finally {
      this.setData({ sending: false });
    }
  },
  async submitVideoBooking() {
    const app = getApp();
    const elderId = app.globalData.selectedElderId;
    if (!elderId) {
      wx.showToast({ title: '请先绑定并选择老人', icon: 'none' });
      return;
    }
    if (!(this.data.videoTime || '').trim()) {
      wx.showToast({ title: '请填写预约时间', icon: 'none' });
      return;
    }
    this.setData({ sending: true });
    try {
      await bookVideoVisit({
        elderId,
        bookingTime: this.data.videoTime,
        remark: '家属预约视频探视'
      });
      await sendCommunicationMessage({
        targetRole: '生活管家',
        msgType: 'video',
        content: `已提交探视预约：${this.data.videoTime}`
      });
      wx.showToast({ title: '探视预约已提交', icon: 'success' });
      await this.loadMessages();
    } catch (error) {
      wx.showToast({ title: '预约失败，请稍后重试', icon: 'none' });
    } finally {
      this.setData({ sending: false });
    }
  }
});
