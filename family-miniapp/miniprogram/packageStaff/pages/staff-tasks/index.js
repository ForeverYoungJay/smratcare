const { getTaskList } = require('../../../services/staff');

const RENDER_PAGE_SIZE = 20;

function routeForTask(task = {}) {
  const idParam = task.id ? `&id=${encodeURIComponent(task.id)}` : '';
  if (task.module === 'CARE') return `/packageStaff/pages/staff-care-execution/index${task.id ? `?id=${encodeURIComponent(task.id)}` : ''}`;
  if (task.module === 'MEDICATION') return `/packageStaff/pages/staff-clinical/index?mode=MEDICATION${idParam}`;
  if (task.module === 'INSPECTION') return `/packageStaff/pages/staff-clinical/index?mode=INSPECTION${idParam}`;
  if (task.module === 'LOGISTICS') return `/packageStaff/pages/staff-repairs/index${task.id ? `?id=${encodeURIComponent(task.id)}` : ''}`;
  if (task.module === 'MEAL') return `/packageStaff/pages/staff-meals/index${task.id ? `?id=${encodeURIComponent(task.id)}` : ''}`;
  return task.id ? `/packageStaff/pages/staff-task-detail/index?id=${encodeURIComponent(task.id)}` : '';
}

function normalizeTask(task = {}) {
  return {
    ...task,
    route: task.route || routeForTask(task)
  };
}

// 按角色推断默认任务模块 Tab：护理类→护理，医护类→用药，后勤类→后勤。
function defaultModuleForRoles(roles = []) {
  const joined = roles.map((item) => String(item || '').toUpperCase()).join(',');
  if (joined.indexOf('NURSING') >= 0) return 'CARE';
  if (joined.indexOf('MEDICAL') >= 0 || joined.indexOf('DOCTOR') >= 0 || joined.indexOf('NURSE') >= 0) return 'MEDICATION';
  if (joined.indexOf('LOGISTICS') >= 0) return 'LOGISTICS';
  return '';
}

Page({
  data: {
    loading: false,
    loadError: '',
    active: '',
    workbenches: [
      { title: '护理执行', desc: '床号核对 / 皮肤风险', route: '/packageStaff/pages/staff-care-execution/index' },
      { title: '用药确认', desc: '三查七对 / 给药结果', route: '/packageStaff/pages/staff-clinical/index?mode=MEDICATION' },
      { title: '巡检复核', desc: '复测体征 / 风险跟进', route: '/packageStaff/pages/staff-clinical/index?mode=INSPECTION' },
      { title: '维修处理', desc: '到场处理 / 拍照验收', route: '/packageStaff/pages/staff-repairs/index' },
      { title: '送餐签收', desc: '禁忌核对 / 餐量反馈', route: '/packageStaff/pages/staff-meals/index' }
    ],
    filters: [
      { label: '全部', value: '' },
      { label: '护理', value: 'CARE' },
      { label: '用药', value: 'MEDICATION' },
      { label: '巡检', value: 'INSPECTION' },
      { label: '后勤', value: 'LOGISTICS' },
      { label: '送餐', value: 'MEAL' }
    ],
    tasks: [],
    hasMore: false
  },
  onLoad(options = {}) {
    let active = options.module || '';
    if (!active) {
      const app = getApp();
      const staffUser = (app && app.globalData && app.globalData.staffUser) || {};
      active = defaultModuleForRoles(staffUser.roles || []);
    }
    this.allTasks = [];
    this.setData({ active });
    this.loadData();
  },
  onPullDownRefresh() {
    this.loadData(true);
  },
  onReachBottom() {
    this.renderMore();
  },
  async loadData(fromPullDown = false) {
    this.setData({ loading: true, loadError: '' });
    try {
      const tasks = await getTaskList(this.data.active);
      // 接口一次性返回全部任务，前端分批渲染，避免长列表卡顿。
      this.allTasks = (tasks || []).map(normalizeTask);
      this.setData({
        tasks: this.allTasks.slice(0, RENDER_PAGE_SIZE),
        hasMore: this.allTasks.length > RENDER_PAGE_SIZE
      });
    } catch (error) {
      this.allTasks = [];
      this.setData({ tasks: [], hasMore: false, loadError: error.message || '待办任务加载失败' });
    } finally {
      this.setData({ loading: false });
      if (fromPullDown) wx.stopPullDownRefresh();
    }
  },
  renderMore() {
    const all = this.allTasks || [];
    const rendered = this.data.tasks.length;
    if (rendered >= all.length) return;
    const next = all.slice(0, rendered + RENDER_PAGE_SIZE);
    this.setData({
      tasks: next,
      hasMore: next.length < all.length
    });
  },
  switchFilter(e) {
    this.setData({ active: e.currentTarget.dataset.value || '' });
    this.loadData();
  },
  openTask(e) {
    const route = e.currentTarget.dataset.route;
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: route || `/packageStaff/pages/staff-task-detail/index?id=${id}` });
  },
  goWorkbench(e) {
    wx.navigateTo({ url: e.currentTarget.dataset.path });
  }
});
