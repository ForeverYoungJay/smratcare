const {
  getActivityAlbums,
  toggleAlbumLike,
  getActivityAlbumComments,
  addActivityAlbumComment
} = require('../../services/family');

Page({
  data: {
    list: [],
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
    const list = await getActivityAlbums();
    this.setData({ list: list || [] });
  },
  async like(e) {
    const id = Number(e.currentTarget.dataset.id);
    await toggleAlbumLike(id);
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
