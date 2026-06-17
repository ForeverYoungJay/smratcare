const {
  getStaffDashboard,
  getTaskList,
  getAttendanceOverview,
  getStaffTodoSummary,
  getIncidents
} = require('../../services/staff');

function levelText(level) {
  if (level === 'CRITICAL') return '紧急';
  if (level === 'HIGH') return '高压';
  if (level === 'MEDIUM') return '需关注';
  return '平稳';
}

function levelClass(level) {
  if (level === 'CRITICAL' || level === 'HIGH') return 'pill-danger';
  if (level === 'MEDIUM') return 'pill-warning';
  return 'pill-normal';
}

function routeForTask(task = {}) {
  const idParam = task.id ? `&id=${encodeURIComponent(task.id)}` : '';
  if (task.module === 'CARE') return `/pages/staff-care-execution/index${task.id ? `?id=${encodeURIComponent(task.id)}` : ''}`;
  if (task.module === 'MEDICATION') return `/pages/staff-clinical/index?mode=MEDICATION${idParam}`;
  if (task.module === 'INSPECTION') return `/pages/staff-clinical/index?mode=INSPECTION${idParam}`;
  if (task.module === 'LOGISTICS') return `/pages/staff-repairs/index${task.id ? `?id=${encodeURIComponent(task.id)}` : ''}`;
  if (task.module === 'MEAL') return `/pages/staff-meals/index${task.id ? `?id=${encodeURIComponent(task.id)}` : ''}`;
  return task.id ? `/pages/staff-task-detail/index?id=${encodeURIComponent(task.id)}` : '/pages/staff-tasks/index';
}

function attendanceText(status) {
  if (status === 'ON_DUTY') return '在岗';
  if (status === 'OFF_DUTY') return '已下班';
  if (status === 'LUNCH_BREAK') return '午休中';
  if (status === 'OUTING') return '外出中';
  return '未打卡';
}

function normalizeTask(task = {}) {
  return {
    ...task,
    route: task.route || routeForTask(task)
  };
}

function buildBattle(dashboard = {}, tasks = [], attendance = {}, todoSummary = {}, incidents = []) {
  const normalizedTasks = (tasks || []).map(normalizeTask);
  const urgentTasks = normalizedTasks.filter((item) => item.priority === 'HIGH' || item.priority === 'CRITICAL' || String(item.status || '').indexOf('异常') >= 0);
  const criticalTask = urgentTasks[0] || normalizedTasks[0] || null;
  const openIncidents = (incidents || []).filter((item) => item.status !== 'CLOSED');
  return {
    shiftText: dashboard.shiftName || '今日班次待确认',
    attendanceText: attendance.todayStatusLabel || attendanceText(attendance.todayStatus),
    attendanceRoute: '/pages/staff-schedule/index',
    openTodoCount: todoSummary.openCount || 0,
    dueTodayCount: todoSummary.dueTodayCount || 0,
    overdueCount: todoSummary.overdueCount || 0,
    incidentOpenCount: openIncidents.length,
    criticalTaskTitle: criticalTask ? criticalTask.title : '暂无紧急任务',
    criticalTaskMeta: criticalTask ? `${criticalTask.time || '时间待定'} · ${criticalTask.resident || '对象未填'} · ${criticalTask.room || '位置未填'}` : '现场状态平稳',
    criticalTaskRoute: criticalTask ? criticalTask.route : '/pages/staff-tasks/index',
    actionHint: criticalTask ? criticalTask.actionText || '立即处理' : '查看全部待办',
    riskTone: openIncidents.length || urgentTasks.length || todoSummary.overdueCount ? 'danger' : 'normal'
  };
}

Page({
  data: {
    loading: false,
    loadError: '',
    dashboard: {
      generatedAt: '',
      staffName: '',
      shiftName: '',
      roleText: '',
      mobileIndex: 0,
      mobileLevel: 'LOW',
      notices: [],
      taskGroups: [],
      tasks: []
    },
    battle: {
      shiftText: '今日班次待确认',
      attendanceText: '未打卡',
      attendanceRoute: '/pages/staff-schedule/index',
      openTodoCount: 0,
      dueTodayCount: 0,
      overdueCount: 0,
      incidentOpenCount: 0,
      criticalTaskTitle: '暂无紧急任务',
      criticalTaskMeta: '现场状态平稳',
      criticalTaskRoute: '/pages/staff-tasks/index',
      actionHint: '查看全部待办',
      riskTone: 'normal'
    },
    levelText: '平稳',
    levelClass: 'pill-normal'
  },
  onLoad() {
    this.loadData();
  },
  onShow() {
    getApp().ensureLogin();
  },
  onPullDownRefresh() {
    this.loadData(true);
  },
  async loadData(fromPullDown = false) {
    this.setData({ loading: true, loadError: '' });
    try {
      const dashboard = await getStaffDashboard();
      const [tasks, attendance, todoSummary, incidents] = await Promise.all([
        getTaskList().catch(() => dashboard.tasks || []),
        getAttendanceOverview().catch(() => ({})),
        getStaffTodoSummary({ mineOnly: true }).catch(() => ({})),
        getIncidents({}).catch(() => [])
      ]);
      this.setData({
        dashboard,
        battle: buildBattle(dashboard, tasks, attendance, todoSummary, incidents),
        levelText: levelText(dashboard.mobileLevel),
        levelClass: levelClass(dashboard.mobileLevel)
      });
    } catch (error) {
      this.setData({ loadError: error.message || '加载员工工作台失败' });
    } finally {
      this.setData({ loading: false });
      if (fromPullDown) wx.stopPullDownRefresh();
    }
  },
  goPage(e) {
    const path = e.currentTarget.dataset.path;
    if (path) wx.navigateTo({ url: path });
  }
});
