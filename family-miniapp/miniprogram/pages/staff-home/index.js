const {
  getStaffDashboard,
  getTaskList,
  getAttendanceOverview,
  getStaffTodoSummary,
  getIncidents
} = require('../../services/staff');
const { requestSubscribeOncePerDay } = require('../../utils/subscribe');

// 角色分类：护理 / 医护 / 后勤；识别不出时返回 all（显示全部入口兜底）。
function roleCategory(roles = [], roleText = '') {
  const joined = `${roles.map((item) => String(item || '').toUpperCase()).join(',')},${String(roleText || '')}`;
  if (joined.indexOf('NURSING') >= 0 || joined.indexOf('护理') >= 0) return 'nursing';
  if (joined.indexOf('MEDICAL') >= 0 || joined.indexOf('DOCTOR') >= 0 || joined.indexOf('NURSE') >= 0
    || joined.indexOf('医') >= 0 || joined.indexOf('药师') >= 0) return 'medical';
  if (joined.indexOf('LOGISTICS') >= 0 || joined.indexOf('后勤') >= 0) return 'logistics';
  return 'all';
}

// 各角色优先靠前的快捷入口（按 route 匹配），以及与本职无关、需要隐藏的入口。
const ROLE_QUICK_ENTRY_RULES = {
  nursing: {
    priority: [
      '/pages/staff-care-execution/index',
      '/pages/staff-vitals/index',
      '/pages/staff-handover/index'
    ],
    hidden: [
      '/pages/staff-repairs/index',
      '/pages/staff-material/index'
    ]
  },
  medical: {
    priority: [
      '/pages/staff-clinical/index?mode=MEDICATION',
      '/pages/staff-clinical/index?mode=INSPECTION',
      '/pages/staff-vitals/index'
    ],
    hidden: [
      '/pages/staff-repairs/index',
      '/pages/staff-meals/index',
      '/pages/staff-material/index'
    ]
  },
  logistics: {
    priority: [
      '/pages/staff-repairs/index',
      '/pages/staff-meals/index',
      '/pages/staff-material/index'
    ],
    hidden: [
      '/pages/staff-care-execution/index',
      '/pages/staff-vitals/index',
      '/pages/staff-clinical/index?mode=MEDICATION',
      '/pages/staff-clinical/index?mode=INSPECTION'
    ]
  }
};

function personalizeQuickEntries(entries = [], category = 'all') {
  const rules = ROLE_QUICK_ENTRY_RULES[category];
  if (!rules || !Array.isArray(entries) || !entries.length) return entries || [];
  const visible = entries.filter((item) => rules.hidden.indexOf(item.route) < 0);
  const prioritized = [];
  rules.priority.forEach((route) => {
    const found = visible.find((item) => item.route === route);
    if (found) prioritized.push(found);
  });
  const rest = visible.filter((item) => rules.priority.indexOf(item.route) < 0);
  return prioritized.concat(rest);
}

function canUseMockFallback() {
  const app = getApp();
  return !!(app
    && app.globalData
    && app.globalData.useMockFallback
    && typeof app.isLocalDevEnvironment === 'function'
    && app.isLocalDevEnvironment());
}

async function loadOptional(requester, fallbackValue, label, warnings) {
  try {
    return await requester();
  } catch (error) {
    if (!canUseMockFallback() && warnings) {
      warnings.push(`${label}同步失败`);
    }
    return typeof fallbackValue === 'function' ? fallbackValue() : fallbackValue;
  }
}

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
    partialWarning: '',
    levelText: '平稳',
    levelClass: 'pill-normal'
  },
  onLoad() {
    this.loadData();
  },
  onShow() {
    getApp().ensureLogin();
    // 首页每天最多自动请求一次订阅授权；模板 ID 未配置时静默跳过，失败不阻塞业务。
    requestSubscribeOncePerDay(['taskOverdue', 'noticePublish']);
  },
  onPullDownRefresh() {
    this.loadData(true);
  },
  async loadData(fromPullDown = false) {
    this.setData({ loading: true, loadError: '', partialWarning: '' });
    try {
      const dashboard = await getStaffDashboard();
      const app = getApp();
      const staffUser = (app && app.globalData && app.globalData.staffUser) || {};
      const category = roleCategory(staffUser.roles || [], dashboard.roleText || '');
      dashboard.quickEntries = personalizeQuickEntries(dashboard.quickEntries, category);
      const warnings = [];
      const [tasks, attendance, todoSummary, incidents] = await Promise.all([
        loadOptional(() => getTaskList(), () => dashboard.tasks || [], '待办任务', warnings),
        loadOptional(() => getAttendanceOverview(), {}, '考勤状态', warnings),
        loadOptional(() => getStaffTodoSummary({ mineOnly: true }), {}, 'OA待办', warnings),
        loadOptional(() => getIncidents({}), [], '异常事件', warnings)
      ]);
      this.setData({
        dashboard,
        battle: buildBattle(dashboard, tasks, attendance, todoSummary, incidents),
        partialWarning: warnings.length ? `部分实时数据未完成同步：${warnings.join('、')}。当前先展示已获取到的内容。` : '',
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
