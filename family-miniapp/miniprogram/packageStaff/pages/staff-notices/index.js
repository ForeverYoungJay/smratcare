const { getStaffNotices, getStaffNoticeDetail } = require('../../../services/staff');

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
    loadingMore: false,
    detailLoading: false,
    loadError: '',
    keyword: '',
    notices: [],
    activeNotice: null,
    pageNo: 1,
    pageSize: 20,
    hasMore: false
  },
  onShow() {
    getApp().ensureLogin();
    this.loadData({ reset: true });
  },
  async loadData(options = {}) {
    const reset = options.reset !== false;
    const nextPageNo = reset ? 1 : Number(this.data.pageNo || 1) + 1;
    this.setData(reset ? { loading: true, loadError: '' } : { loadingMore: true, loadError: '' });
    try {
      const notices = await getStaffNotices({
        keyword: this.data.keyword.trim(),
        pageNo: nextPageNo,
        pageSize: this.data.pageSize
      });
      const normalized = (notices || []).map(normalizeNotice);
      this.setData({
        notices: reset ? normalized : this.data.notices.concat(normalized),
        pageNo: nextPageNo,
        hasMore: normalized.length >= this.data.pageSize
      });
    } catch (error) {
      this.setData({ loadError: error.message || (reset ? '通知公告加载失败' : '更多公告加载失败，请稍后重试') });
    } finally {
      this.setData({ loading: false, loadingMore: false });
    }
  },
  onKeywordInput(e) {
    this.setData({ keyword: e.detail.value });
  },
  searchNotices() {
    this.loadData({ reset: true });
  },
  loadMore() {
    if (this.data.loading || this.data.loadingMore || !this.data.hasMore) return;
    this.loadData({ reset: false });
  },
  onReachBottom() {
    this.loadMore();
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
