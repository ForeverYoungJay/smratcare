const { getStaffTodoSummary, getStaffTodos, completeStaffTodo } = require('../../services/staff');

function sourceTypeOf(item) {
  const content = item && item.content ? item.content : '';
  if (content.indexOf('[APPROVAL_FLOW:') >= 0) return 'APPROVAL';
  if (content.indexOf('[BIRTHDAY_REMINDER:') >= 0) return 'BIRTHDAY';
  if (content.indexOf('[SHIFT_SWAP_TARGET:') >= 0) return 'SHIFT_SWAP';
  if (content.indexOf('[SHIFT_DUTY:') >= 0) return 'SHIFT_DUTY';
  return 'NORMAL';
}

function sourceText(type) {
  if (type === 'APPROVAL') return '审批流';
  if (type === 'BIRTHDAY') return '生日提醒';
  if (type === 'SHIFT_SWAP') return '换班确认';
  if (type === 'SHIFT_DUTY') return '值班提醒';
  return '普通待办';
}

function normalizeTodo(item) {
  const sourceType = sourceTypeOf(item);
  return {
    ...item,
    sourceType,
    sourceText: sourceText(sourceType),
    dueText: String(item.dueTime || '').replace('T', ' '),
    createText: String(item.createTime || '').replace('T', ' '),
    doneDisabled: item.status !== 'OPEN' || sourceType === 'APPROVAL'
  };
}

Page({
  data: {
    loading: false,
    loadingMore: false,
    operatingId: '',
    loadError: '',
    activeStatus: 'OPEN',
    activeSource: '',
    keyword: '',
    summary: {},
    todos: [],
    pageNo: 1,
    pageSize: 20,
    hasMore: false,
    statusFilters: [
      { label: '待处理', value: 'OPEN' },
      { label: '已完成', value: 'DONE' },
      { label: '全部', value: '' }
    ],
    sourceFilters: [
      { label: '全部来源', value: '' },
      { label: '审批', value: 'APPROVAL' },
      { label: '普通', value: 'NORMAL' },
      { label: '生日', value: 'BIRTHDAY' }
    ]
  },
  onShow() {
    getApp().ensureLogin();
    this.loadData({ reset: true });
  },
  async onPullDownRefresh() {
    try {
      await this.loadData({ reset: true });
    } finally {
      wx.stopPullDownRefresh();
    }
  },
  async loadData(options = {}) {
    const reset = options.reset !== false;
    const nextPageNo = reset ? 1 : Number(this.data.pageNo || 1) + 1;
    const statePatch = reset
      ? { loading: true, loadError: '', pageNo: 1 }
      : { loadingMore: true, loadError: '' };
    this.setData(statePatch);
    try {
      const filter = {
        status: this.data.activeStatus,
        sourceType: this.data.activeSource,
        keyword: this.data.keyword.trim(),
        pageNo: nextPageNo,
        pageSize: this.data.pageSize
      };
      const [summary, todos] = await Promise.all([
        getStaffTodoSummary(filter),
        getStaffTodos(filter)
      ]);
      const normalized = (todos || []).map(normalizeTodo);
      const merged = reset ? normalized : this.data.todos.concat(normalized);
      this.setData({
        summary: summary || {},
        todos: merged,
        pageNo: nextPageNo,
        hasMore: normalized.length >= this.data.pageSize
      });
    } catch (error) {
      this.setData({ loadError: error.message || (reset ? '待办加载失败' : '更多待办加载失败，请稍后重试') });
    } finally {
      this.setData({ loading: false, loadingMore: false });
    }
  },
  switchStatus(e) {
    this.setData({ activeStatus: e.currentTarget.dataset.value || '' });
    this.loadData({ reset: true });
  },
  switchSource(e) {
    this.setData({ activeSource: e.currentTarget.dataset.value || '' });
    this.loadData({ reset: true });
  },
  onKeywordInput(e) {
    this.setData({ keyword: e.detail.value });
  },
  searchTodos() {
    this.loadData({ reset: true });
  },
  loadMore() {
    if (this.data.loading || this.data.loadingMore || !this.data.hasMore) return;
    this.loadData({ reset: false });
  },
  onReachBottom() {
    this.loadMore();
  },
  goApproval() {
    wx.navigateTo({ url: '/pages/staff-approval/index' });
  },
  async markDone(e) {
    const id = e.currentTarget.dataset.id;
    if (!id || this.data.operatingId) return;
    this.setData({ operatingId: id });
    try {
      await completeStaffTodo(id);
      wx.showToast({ title: '已完成待办', icon: 'success' });
      this.loadData({ reset: true });
    } catch (error) {
      wx.showToast({ title: error.message || '操作失败', icon: 'none' });
    } finally {
      this.setData({ operatingId: '' });
    }
  }
});
