const { getStaffNotices, getStaffNoticeDetail } = require('../../services/staff');

function normalizeNotice(item) {
  return {
    ...item,
    publishText: String(item.publishTime || item.createTime || '').replace('T', ' '),
    preview: String(item.content || '').slice(0, 72)
  };
}

Page({
  data: {
    loading: false,
    detailLoading: false,
    loadError: '',
    keyword: '',
    notices: [],
    activeNotice: null
  },
  onShow() {
    getApp().ensureLogin();
    this.loadData();
  },
  async loadData() {
    this.setData({ loading: true, loadError: '' });
    try {
      const notices = await getStaffNotices({ keyword: this.data.keyword.trim(), pageSize: 30 });
      this.setData({ notices: (notices || []).map(normalizeNotice) });
    } catch (error) {
      this.setData({ loadError: error.message || '通知公告加载失败' });
    } finally {
      this.setData({ loading: false });
    }
  },
  onKeywordInput(e) {
    this.setData({ keyword: e.detail.value });
  },
  searchNotices() {
    this.loadData();
  },
  async openNotice(e) {
    const id = e.currentTarget.dataset.id;
    if (!id) return;
    this.setData({ detailLoading: true });
    try {
      const detail = await getStaffNoticeDetail(id);
      this.setData({ activeNotice: normalizeNotice(detail || {}) });
    } catch (error) {
      wx.showToast({ title: error.message || '公告详情加载失败', icon: 'none' });
    } finally {
      this.setData({ detailLoading: false });
    }
  },
  closeDetail() {
    this.setData({ activeNotice: null });
  },
  noop() {
    return false;
  }
});
