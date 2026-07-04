const {
  getActivityAlbums,
  toggleAlbumLike,
  getActivityAlbumComments,
  addActivityAlbumComment,
  resolveFileUrl
} = require('../../services/family');

Page({
  data: {
    tabs: [
      { key: 'PERSONAL', title: '个人相册' },
      { key: 'GROUP', title: '集体活动相册' }
    ],
    activeScope: 'PERSONAL',
    list: [],
    loading: false,
    loadError: '',
    activeAlbumId: null,
    comments: [],
    commentInput: '',
    loadingComments: false,
    commenting: false
  },
  async onShow() {
    getApp().ensureLogin();
    await this.loadData();
  },
  async loadData() {
    this.setData({ loading: true, loadError: '' });
    try {
      const list = await getActivityAlbums(1, 20, { scope: this.data.activeScope });
      this.setData({
        list: (list || []).map((item) => {
          const mediaType = String(item.mediaType || '').toLowerCase();
          const mediaUrl = item.mediaUrl || item.videoUrl || (mediaType === 'video' ? item.coverUrl : '');
          return {
            ...item,
            resolvedCoverUrl: resolveFileUrl(item.coverUrl || ''),
            resolvedMediaUrl: resolveFileUrl(mediaUrl || ''),
            scopeText: item.albumScope === 'PERSONAL' ? '个人相册' : '集体活动',
            isVideo: mediaType === 'video',
            isPhoto: mediaType === 'photo' || mediaType === 'image'
          };
        })
      });
    } catch (error) {
      this.setData({ list: [], loadError: error.message || '相册加载失败，请检查网络后重试' });
    } finally {
      this.setData({ loading: false });
    }
  },
  retryLoad() {
    this.loadData();
  },
  async switchScope(e) {
    const scope = e.currentTarget.dataset.scope;
    if (!scope || scope === this.data.activeScope) {
      return;
    }
    this.setData({
      activeScope: scope,
      activeAlbumId: null,
      comments: [],
      commentInput: '',
      list: []
    });
    await this.loadData();
  },
  previewPhoto(e) {
    const url = e.currentTarget.dataset.url;
    if (!url) {
      return;
    }
    const urls = (this.data.list || [])
      .filter((item) => item.isPhoto && item.resolvedCoverUrl)
      .map((item) => item.resolvedCoverUrl);
    wx.previewImage({
      current: url,
      urls: urls.length ? urls : [url]
    });
  },
  async like(e) {
    const id = Number(e.currentTarget.dataset.id);
    try {
      await toggleAlbumLike(id);
    } catch (error) {
      wx.showToast({ title: error.message || '操作失败，请稍后重试', icon: 'none' });
      return;
    }
    await this.loadData();
    if (this.data.activeAlbumId === id) {
      await this.loadComments(id);
    }
  },
  async openComments(e) {
    const id = Number(e.currentTarget.dataset.id);
    if (!id) {
      return;
    }
    if (this.data.activeAlbumId === id) {
      this.setData({ activeAlbumId: null, comments: [], commentInput: '' });
      return;
    }
    this.setData({ activeAlbumId: id, commentInput: '' });
    await this.loadComments(id);
  },
  async loadComments(albumId) {
    this.setData({ loadingComments: true });
    try {
      const comments = await getActivityAlbumComments(albumId);
      this.setData({ comments: comments || [] });
    } catch (error) {
      wx.showToast({ title: '评论加载失败', icon: 'none' });
    } finally {
      this.setData({ loadingComments: false });
    }
  },
  onCommentInput(e) {
    this.setData({ commentInput: e.detail.value });
  },
  async submitComment() {
    if (this.data.commenting) {
      return;
    }
    const albumId = Number(this.data.activeAlbumId || 0);
    const content = (this.data.commentInput || '').trim();
    if (!albumId) {
      return;
    }
    if (!content) {
      wx.showToast({ title: '请输入评论内容', icon: 'none' });
      return;
    }
    this.setData({ commenting: true });
    try {
      await addActivityAlbumComment(albumId, content);
      this.setData({ commentInput: '' });
      await Promise.all([this.loadComments(albumId), this.loadData()]);
      wx.showToast({ title: '评论已发布', icon: 'none' });
    } catch (error) {
      wx.showToast({ title: error.message || '评论失败，请稍后重试', icon: 'none' });
    } finally {
      this.setData({ commenting: false });
    }
  }
});
