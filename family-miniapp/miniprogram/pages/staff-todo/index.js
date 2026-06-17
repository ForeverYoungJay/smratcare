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
    operatingId: '',
    loadError: '',
    activeStatus: 'OPEN',
    activeSource: '',
    keyword: '',
    summary: {},
    todos: [],
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
    this.loadData();
  },
  async loadData() {
    this.setData({ loading: true, loadError: '' });
    try {
      const filter = {
        status: this.data.activeStatus,
        sourceType: this.data.activeSource,
        keyword: this.data.keyword.trim()
      };
      const [summary, todos] = await Promise.all([
        getStaffTodoSummary({ mineOnly: true }),
        getStaffTodos(filter)
      ]);
      this.setData({
        summary: summary || {},
        todos: (todos || []).map(normalizeTodo)
      });
    } catch (error) {
      this.setData({ loadError: error.message || '待办加载失败' });
    } finally {
      this.setData({ loading: false });
    }
  },
  switchStatus(e) {
    this.setData({ activeStatus: e.currentTarget.dataset.value || '' });
    this.loadData();
  },
  switchSource(e) {
    this.setData({ activeSource: e.currentTarget.dataset.value || '' });
    this.loadData();
  },
  onKeywordInput(e) {
    this.setData({ keyword: e.detail.value });
  },
  searchTodos() {
    this.loadData();
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
      this.loadData();
    } catch (error) {
      wx.showToast({ title: error.message || '操作失败', icon: 'none' });
    } finally {
      this.setData({ operatingId: '' });
    }
  }
});
