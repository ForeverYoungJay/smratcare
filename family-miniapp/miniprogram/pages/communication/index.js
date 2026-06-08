const {
  bookVideoVisit,
  getCommunicationMessages,
  resolveFileUrl,
  sendCommunicationMessage,
  uploadCommunicationMedia,
  uploadVoiceMessage
} = require('../../services/family');

const ROLE_OPTIONS = [
  { value: '客服中心', label: '客服中心', desc: '咨询、投诉、通用问题' },
  { value: '责任护士', label: '责任护士', desc: '用药、体征、健康问题' },
  { value: '责任护工', label: '责任护工', desc: '起居、护理、日常照护' },
  { value: '生活管家', label: '生活管家', desc: '探视、缴费、生活安排' }
];
const DRAFT_KEY_PREFIX = 'family_comm_chat_draft_';

function nextDayVideoTime() {
  const now = new Date();
  const next = new Date(now.getTime() + 24 * 60 * 60 * 1000);
  const year = next.getFullYear();
  const month = `${next.getMonth() + 1}`.padStart(2, '0');
  const day = `${next.getDate()}`.padStart(2, '0');
  return `${year}-${month}-${day} 15:00`;
}

function resolveTypeLabel(type) {
  if (type === 'voice') return '语音';
  if (type === 'video') return '探视预约';
  if (type === 'image') return '图片';
  if (type === 'media_video') return '视频';
  return '消息';
}

function isMineMessage(item = {}) {
  const direction = String(item.direction || 'FAMILY_TO_STAFF').toUpperCase();
  return direction !== 'STAFF_TO_FAMILY';
}

function draftKey(role) {
  return `${DRAFT_KEY_PREFIX}${role || '客服中心'}`;
}

Page({
  data: {
    roleOptions: ROLE_OPTIONS,
    roleIndex: 0,
    activeRole: ROLE_OPTIONS[0].value,
    activeRoleDesc: ROLE_OPTIONS[0].desc,
    content: '',
    allMessages: [],
    messages: [],
    loading: false,
    loadError: '',
    sending: false,
    recording: false,
    recordStartAt: 0,
    voiceMode: false,
    playingId: '',
    runtimeNotice: '',
    videoTime: nextDayVideoTime(),
    showVisitPanel: false
  },
  onLoad(query) {
    const mode = query && query.mode ? String(query.mode).toLowerCase() : '';
    const roleIndex = mode === 'video' ? 3 : 0;
    const role = ROLE_OPTIONS[roleIndex] || ROLE_OPTIONS[0];
    this.setData({
      roleIndex,
      activeRole: role.value,
      activeRoleDesc: role.desc,
      showVisitPanel: mode === 'video'
    });
    this.initRecorder();
    this.restoreDraft();
  },
  async onShow() {
    getApp().ensureLogin();
    this.setData({ runtimeNotice: getApp().globalData.runtimeNotice || '' });
    await this.loadMessages();
  },
  onUnload() {
    if (this.recorderManager && this.data.recording) {
      this.recorderManager.stop();
    }
    this.stopVoice();
  },
  initRecorder() {
    if (!wx.getRecorderManager) {
      return;
    }
    this.recorderManager = wx.getRecorderManager();
    this.recorderManager.onStop(async (res) => {
      const durationMs = Math.max(0, Date.now() - Number(this.data.recordStartAt || 0));
      this.setData({ recording: false, recordStartAt: 0 });
      if (durationMs < 800) {
        wx.showToast({ title: '说话时间太短', icon: 'none' });
        return;
      }
      const tempFilePath = res && res.tempFilePath;
      if (!tempFilePath) {
        wx.showToast({ title: '录音文件生成失败', icon: 'none' });
        return;
      }
      await this.sendVoicePath(tempFilePath, {
        durationSec: res.duration ? Math.max(1, Math.round(res.duration / 1000)) : Math.max(1, Math.round(durationMs / 1000))
      });
    });
    this.recorderManager.onError(() => {
      this.setData({ recording: false, recordStartAt: 0 });
      wx.showToast({ title: '录音失败，请检查麦克风权限', icon: 'none' });
    });
  },
  async loadMessages() {
    this.setData({ loading: true, loadError: '' });
    try {
      const list = await getCommunicationMessages(1, 80);
      const allMessages = (list || []).map((item) => {
        const msgType = String(item.msgType || 'text').toLowerCase();
        const targetRole = item.targetRole || '客服中心';
        return {
          ...item,
          targetRole,
          idText: String(item.id || ''),
          avatarText: isMineMessage(item) ? '我' : String(targetRole).slice(0, 1),
          msgType,
          msgTypeText: resolveTypeLabel(msgType),
          mine: isMineMessage(item),
          isVoice: msgType === 'voice',
          isVisit: msgType === 'video',
          isImage: msgType === 'image',
          isMediaVideo: msgType === 'media_video',
          playableUrl: resolveFileUrl(item.mediaUrl || ''),
          voiceDurationText: item && item.mediaDurationSec ? `${item.mediaDurationSec}"` : ''
        };
      }).reverse().map((item, index, rows) => ({
        ...item,
        showTime: index === 0 || rows[index - 1].time !== item.time
      }));
      this.setData({ allMessages });
      this.applyConversationMessages();
      return allMessages;
    } catch (error) {
      this.setData({ loadError: error.message || '沟通信息加载失败，请稍后重试' });
      return [];
    } finally {
      this.setData({ loading: false });
    }
  },
  onRoleChange(e) {
    this.selectRoleByIndex(Number(e.detail.value || 0));
  },
  selectRole(e) {
    this.selectRoleByIndex(Number(e.currentTarget.dataset.index || 0));
  },
  selectRoleByIndex(index) {
    const role = ROLE_OPTIONS[index] || ROLE_OPTIONS[0];
    this.setData({
      roleIndex: index,
      activeRole: role.value,
      activeRoleDesc: role.desc,
      content: wx.getStorageSync(draftKey(role.value)) || '',
      showVisitPanel: false
    });
    this.applyConversationMessages();
  },
  applyConversationMessages() {
    const activeRole = this.data.activeRole || ROLE_OPTIONS[0].value;
    const messages = (this.data.allMessages || []).filter((item) => {
      const targetRole = item.targetRole || '客服中心';
      return targetRole === activeRole;
    }).map((item, index, rows) => ({
      ...item,
      showTime: index === 0 || rows[index - 1].time !== item.time
    }));
    this.setData({ messages });
  },
  onContentInput(e) {
    const content = e.detail.value;
    this.setData({ content });
    const trimmed = String(content || '').trim();
    if (trimmed) {
      wx.setStorageSync(draftKey(this.data.activeRole), content);
    } else {
      wx.removeStorageSync(draftKey(this.data.activeRole));
    }
  },
  restoreDraft() {
    const draft = wx.getStorageSync(draftKey(this.data.activeRole)) || '';
    if (draft) {
      this.setData({ content: draft });
    }
  },
  toggleVoiceMode() {
    this.setData({ voiceMode: !this.data.voiceMode });
  },
  async sendText() {
    const content = String(this.data.content || '').trim();
    if (!content || this.data.sending) {
      return;
    }
    await this.sendChatMessage({
      msgType: 'text',
      content
    });
    wx.removeStorageSync(draftKey(this.data.activeRole));
    this.setData({ content: '' });
  },
  async sendChatMessage(payload) {
    const targetRole = this.data.activeRole || ROLE_OPTIONS[0].value;
    this.setData({ sending: true });
    try {
      await sendCommunicationMessage({
        targetRole,
        ...payload
      });
      await this.loadMessages();
    } catch (error) {
      wx.showToast({ title: error.message || '发送失败，请重试', icon: 'none' });
    } finally {
      this.setData({ sending: false });
    }
  },
  async startRecord() {
    if (!this.recorderManager || this.data.recording || this.data.sending) {
      return;
    }
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
        content: '请在小程序设置中允许录音后，再按住发送语音。',
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
    if (!this.recorderManager || !this.data.recording) {
      return;
    }
    this.recorderManager.stop();
  },
  cancelRecord() {
    if (!this.recorderManager || !this.data.recording) {
      return;
    }
    this.setData({ recordStartAt: 0 });
    this.recorderManager.stop();
  },
  async sendVoicePath(filePath, options = {}) {
    this.setData({ sending: true });
    try {
      const uploaded = await uploadVoiceMessage(filePath, { bizType: 'family-voice' });
      const durationSec = Number(options.durationSec || 0);
      await sendCommunicationMessage({
        targetRole: this.data.activeRole || ROLE_OPTIONS[0].value,
        msgType: 'voice',
        content: '语音留言',
        mediaUrl: uploaded.fileUrl || '',
        mediaName: uploaded.originalFileName || uploaded.fileName || '语音留言.mp3',
        mediaDurationSec: durationSec > 0 ? durationSec : undefined
      });
      await this.loadMessages();
    } catch (error) {
      wx.showToast({ title: error.message || '语音发送失败', icon: 'none' });
    } finally {
      this.setData({ sending: false });
    }
  },
  chooseImage() {
    this.chooseChatMedia('image');
  },
  chooseVideo() {
    this.chooseChatMedia('video');
  },
  async chooseChatMedia(mediaType) {
    if (this.data.sending) {
      return;
    }
    try {
      const chooseResult = await new Promise((resolve, reject) => {
        wx.chooseMedia({
          count: 1,
          mediaType: [mediaType],
          sourceType: ['album', 'camera'],
          maxDuration: 60,
          camera: 'back',
          success: resolve,
          fail: reject
        });
      });
      const file = chooseResult && chooseResult.tempFiles && chooseResult.tempFiles[0];
      const filePath = file && (file.tempFilePath || file.path);
      if (!filePath) {
        throw new Error(mediaType === 'image' ? '未获取到图片' : '未获取到视频');
      }
      this.setData({ sending: true });
      wx.showLoading({ title: '正在上传', mask: true });
      const uploaded = await uploadCommunicationMedia(filePath, {
        bizType: mediaType === 'image' ? 'family-chat-image' : 'family-chat-video'
      });
      const msgType = mediaType === 'image' ? 'image' : 'media_video';
      await sendCommunicationMessage({
        targetRole: this.data.activeRole || ROLE_OPTIONS[0].value,
        msgType,
        content: mediaType === 'image' ? '图片' : '视频',
        mediaUrl: uploaded.fileUrl || '',
        mediaName: uploaded.originalFileName || uploaded.fileName || (mediaType === 'image' ? '图片' : '视频'),
        mediaDurationSec: mediaType === 'video' && file.duration ? Math.max(1, Math.round(Number(file.duration))) : undefined
      });
      this.setData({ showVisitPanel: false });
      await this.loadMessages();
    } catch (error) {
      const message = error && error.errMsg && error.errMsg.includes('cancel')
        ? ''
        : (error.message || '发送失败，请重试');
      if (message) {
        wx.showToast({ title: message, icon: 'none' });
      }
    } finally {
      wx.hideLoading();
      this.setData({ sending: false });
    }
  },
  playVoice(e) {
    const id = String(e.currentTarget.dataset.id || '');
    const url = e.currentTarget.dataset.url;
    if (!url) {
      wx.showToast({ title: '暂无可播放语音', icon: 'none' });
      return;
    }
    this.stopVoice();
    const audio = wx.createInnerAudioContext();
    this.audioContext = audio;
    audio.src = url;
    audio.onEnded(() => this.setData({ playingId: '' }));
    audio.onStop(() => this.setData({ playingId: '' }));
    audio.onError(() => {
      this.setData({ playingId: '' });
      wx.showToast({ title: '语音播放失败', icon: 'none' });
    });
    this.setData({ playingId: id });
    audio.play();
  },
  previewImage(e) {
    const url = e.currentTarget.dataset.url;
    if (!url) {
      return;
    }
    wx.previewImage({
      current: url,
      urls: [url]
    });
  },
  stopVoice() {
    if (this.audioContext) {
      this.audioContext.stop();
      this.audioContext.destroy();
      this.audioContext = null;
    }
    this.setData({ playingId: '' });
  },
  toggleVisitPanel() {
    this.setData({
      showVisitPanel: !this.data.showVisitPanel,
      voiceMode: false
    });
  },
  onTimeInput(e) {
    this.setData({ videoTime: e.detail.value });
  },
  async submitVideoBooking() {
    const app = getApp();
    const elderId = app.globalData.selectedElderId;
    if (!elderId) {
      wx.showToast({ title: '请先绑定并选择老人', icon: 'none' });
      return;
    }
    const videoTime = String(this.data.videoTime || '').trim();
    if (!videoTime) {
      wx.showToast({ title: '请填写预约时间', icon: 'none' });
      return;
    }
    this.setData({ sending: true });
    try {
      await bookVideoVisit({
        elderId,
        bookingTime: videoTime,
        remark: '家属预约视频探视'
      });
      await sendCommunicationMessage({
        targetRole: '生活管家',
        msgType: 'video',
        content: `已提交探视预约：${videoTime}`
      });
      const managerIndex = ROLE_OPTIONS.findIndex((item) => item.value === '生活管家');
      this.setData({
        showVisitPanel: false,
        roleIndex: managerIndex,
        activeRole: '生活管家',
        activeRoleDesc: ROLE_OPTIONS[managerIndex].desc
      });
      wx.showToast({ title: '探视预约已提交', icon: 'success' });
      await this.loadMessages();
    } catch (error) {
      wx.showToast({ title: error.message || '预约失败，请稍后重试', icon: 'none' });
    } finally {
      this.setData({ sending: false });
    }
  },
  retryLoad() {
    this.loadMessages();
  }
});
