const { getTodoCenter, handleTodoAction } = require('../../services/family');

function priorityClass(priority) {
  if (priority === 'URGENT') return 'priority-urgent';
  if (priority === 'HIGH') return 'priority-high';
  return 'priority-normal';
}

function priorityText(priority) {
  if (priority === 'URGENT') return '紧急';
  if (priority === 'HIGH') return '优先';
  return '普通';
}

function typeText(type) {
  if (type === 'MESSAGE') return '消息';
  if (type === 'PAYMENT') return '缴费';
  if (type === 'SCHEDULE') return '日程';
  if (type === 'SECURITY') return '安全';
  return '事项';
}

Page({
  data: {
    todo: {
      summary: {
        urgentCount: 0,
        dueTodayCount: 0,
        completedHintCount: 0
      },
      items: [],
      refreshTime: ''
    },
    loading: false,
    actionLoading: false
  },
  onShow() {
    getApp().ensureLogin();
    this.loadData();
  },
  onPullDownRefresh() {
    this.loadData(true);
  },
  async loadData(fromPullDown = false) {
    this.setData({ loading: true });
    try {
      const todo = await getTodoCenter();
      const items = (todo.items || []).map((item) => ({
        ...item,
        priorityClass: priorityClass(item.priority),
        priorityText: priorityText(item.priority),
        typeText: typeText(item.type)
      }));
      this.setData({
        todo: {
          ...todo,
          items
        }
      });
    } finally {
      this.setData({ loading: false });
      if (fromPullDown) {
        wx.stopPullDownRefresh();
      }
    }
  },
  openAction(e) {
    const path = e.currentTarget.dataset.path;
    if (!path) {
      return;
    }
    wx.navigateTo({ url: path });
  },
  async markDone(e) {
    const todoKey = e.currentTarget.dataset.key;
    const actionable = e.currentTarget.dataset.actionable;
    if (!todoKey || !actionable || this.data.actionLoading) {
      return;
    }
    this.setData({ actionLoading: true });
    try {
      await handleTodoAction(todoKey, 'DONE');
      wx.showToast({ title: '已标记完成', icon: 'success' });
      await this.loadData();
    } finally {
      this.setData({ actionLoading: false });
    }
  },
  async snoozeTodo(e) {
    const todoKey = e.currentTarget.dataset.key;
    const actionable = e.currentTarget.dataset.actionable;
    if (!todoKey || !actionable || this.data.actionLoading) {
      return;
    }
    this.setData({ actionLoading: true });
    try {
      await handleTodoAction(todoKey, 'SNOOZE', { snoozeMinutes: 120 });
      wx.showToast({ title: '已稍后2小时提醒', icon: 'none' });
      await this.loadData();
    } finally {
      this.setData({ actionLoading: false });
    }
  },
  toPaymentGuard() {
    wx.navigateTo({ url: '/pages/payment-guard/index' });
  }
});
