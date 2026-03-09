const {
  getAffectionMoments,
  getFestivalTemplates,
  addAffectionMoment,
  uploadAffectionMedia
} = require('../../services/family');

Page({
  data: {
    moments: [],
    templates: [],
    uploadingMedia: false,
    mediaUploading: false,
    selectedMediaUrl: '',
    selectedMediaName: '',
    selectedMediaType: ''
  },
  async onShow() {
    getApp().ensureLogin();
    await this.loadData();
  },
  async loadData() {
    const [moments, templates] = await Promise.all([getAffectionMoments(), getFestivalTemplates()]);
    this.setData({ moments: moments || [], templates: templates || [] });
  },
  async sendVoice() {
    await addAffectionMoment({
      type: 'voice',
      title: '语音留言',
      desc: '“妈妈我想你了，周末来看你。”',
      mediaType: 'voice'
    });
    wx.showToast({ title: '语音留言已发送', icon: 'none' });
    this.loadData();
  },
  async sendGreeting(e) {
    const template = e.currentTarget.dataset.template;
    await addAffectionMoment({
      type: 'greeting',
      title: `${template}祝福`,
      desc: '已发送祝福模板，护工将协助播放。'
    });
    wx.showToast({ title: '祝福已发送', icon: 'none' });
    this.loadData();
  },
  async chooseMedia() {
    if (this.data.mediaUploading) {
      return;
    }
    this.setData({ mediaUploading: true });
    try {
      const selected = await new Promise((resolve, reject) => {
        wx.chooseMedia({
          count: 1,
          mediaType: ['image', 'video'],
          sourceType: ['album', 'camera'],
          success: resolve,
          fail: reject
        });
      });
      const file = selected && selected.tempFiles && selected.tempFiles[0];
      if (!file || !file.tempFilePath) {
        throw new Error('未选择媒体文件');
      }
      const uploaded = await uploadAffectionMedia(file.tempFilePath, { bizType: 'family-affection-media' });
      const mediaType = file.fileType === 'video' ? 'video' : 'image';
      this.setData({
        selectedMediaUrl: uploaded.fileUrl || '',
        selectedMediaName: uploaded.originalFileName || uploaded.fileName || 'family-media',
        selectedMediaType: mediaType
      });
      wx.showToast({ title: '媒体上传成功', icon: 'none' });
    } catch (error) {
      wx.showToast({ title: error.message || '媒体上传失败', icon: 'none' });
    } finally {
      this.setData({ mediaUploading: false });
    }
  },
  clearSelectedMedia() {
    this.setData({
      selectedMediaUrl: '',
      selectedMediaName: '',
      selectedMediaType: ''
    });
  },
  async sendMediaMoment() {
    if (this.data.uploadingMedia) {
      return;
    }
    if (!this.data.selectedMediaUrl) {
      wx.showToast({ title: '请先上传照片或视频', icon: 'none' });
      return;
    }
    this.setData({ uploadingMedia: true });
    try {
      await addAffectionMoment({
        type: 'album',
        title: this.data.selectedMediaType === 'video' ? '家庭视频上传' : '家庭照片上传',
        desc: '家属已上传亲情素材，护工将协助老人查看。',
        mediaType: this.data.selectedMediaType,
        mediaUrl: this.data.selectedMediaUrl,
        mediaName: this.data.selectedMediaName
      });
      this.clearSelectedMedia();
      wx.showToast({ title: '亲情素材已发送', icon: 'none' });
      await this.loadData();
    } finally {
      this.setData({ uploadingMedia: false });
    }
  }
});
