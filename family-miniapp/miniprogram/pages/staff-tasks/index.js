const { getTaskList } = require('../../services/staff');

function routeForTask(task = {}) {
  const idParam = task.id ? `&id=${encodeURIComponent(task.id)}` : '';
  if (task.module === 'CARE') return `/pages/staff-care-execution/index${task.id ? `?id=${encodeURIComponent(task.id)}` : ''}`;
  if (task.module === 'MEDICATION') return `/pages/staff-clinical/index?mode=MEDICATION${idParam}`;
  if (task.module === 'INSPECTION') return `/pages/staff-clinical/index?mode=INSPECTION${idParam}`;
  if (task.module === 'LOGISTICS') return `/pages/staff-repairs/index${task.id ? `?id=${encodeURIComponent(task.id)}` : ''}`;
  if (task.module === 'MEAL') return `/pages/staff-meals/index${task.id ? `?id=${encodeURIComponent(task.id)}` : ''}`;
  return task.id ? `/pages/staff-task-detail/index?id=${encodeURIComponent(task.id)}` : '';
}

function normalizeTask(task = {}) {
  return {
    ...task,
    route: task.route || routeForTask(task)
  };
}

Page({
  data: {
    loading: false,
    active: '',
    workbenches: [
      { title: '护理执行', desc: '床号核对 / 皮肤风险', route: '/pages/staff-care-execution/index' },
      { title: '用药确认', desc: '三查七对 / 给药结果', route: '/pages/staff-clinical/index?mode=MEDICATION' },
      { title: '巡检复核', desc: '复测体征 / 风险跟进', route: '/pages/staff-clinical/index?mode=INSPECTION' },
      { title: '维修处理', desc: '到场处理 / 拍照验收', route: '/pages/staff-repairs/index' },
      { title: '送餐签收', desc: '禁忌核对 / 餐量反馈', route: '/pages/staff-meals/index' }
    ],
    filters: [
      { label: '全部', value: '' },
      { label: '护理', value: 'CARE' },
      { label: '用药', value: 'MEDICATION' },
      { label: '巡检', value: 'INSPECTION' },
      { label: '后勤', value: 'LOGISTICS' },
      { label: '送餐', value: 'MEAL' }
    ],
    tasks: []
  },
  onLoad(options = {}) {
    this.setData({ active: options.module || '' });
    this.loadData();
  },
  async loadData() {
    this.setData({ loading: true });
    try {
      const tasks = await getTaskList(this.data.active);
      this.setData({ tasks: (tasks || []).map(normalizeTask) });
    } finally {
      this.setData({ loading: false });
    }
  },
  switchFilter(e) {
    this.setData({ active: e.currentTarget.dataset.value || '' });
    this.loadData();
  },
  openTask(e) {
    const route = e.currentTarget.dataset.route;
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: route || `/pages/staff-task-detail/index?id=${id}` });
  },
  goWorkbench(e) {
    wx.navigateTo({ url: e.currentTarget.dataset.path });
  }
});
