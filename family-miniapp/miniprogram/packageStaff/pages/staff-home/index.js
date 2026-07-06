const {
  getStaffDashboard,
  getTaskList,
  getAttendanceOverview,
  getStaffTodoSummary,
  getIncidents,
  punchAttendance
} = require('../../../services/staff');
const { requestSubscribeOncePerDay } = require('../../../utils/subscribe');

// 内置完整员工功能清单：后端 actions 缺失或不全时兜底，确保工作台可达全部功能页。
const STAFF_MENU = [
  { title: '待办', desc: '通知提醒', route: '/packageStaff/pages/staff-todo/index' },
  { title: '通讯', desc: '一键联系', route: '/packageStaff/pages/staff-contacts/index' },
  { title: '日报', desc: '工作汇报', route: '/packageStaff/pages/staff-report/index' },
  { title: '公告', desc: '制度通知', route: '/packageStaff/pages/staff-notices/index' },
  { title: '建议', desc: '一线反馈', route: '/packageStaff/pages/staff-suggestions/index' },
  { title: '护理', desc: '现场照护', route: '/packageStaff/pages/staff-care-execution/index' },
  { title: '体征', desc: '现场补录', route: '/packageStaff/pages/staff-vitals/index' },
  { title: '用药', desc: '三查七对', route: '/packageStaff/pages/staff-clinical/index?mode=MEDICATION' },
  { title: '巡检', desc: '异常复核', route: '/packageStaff/pages/staff-clinical/index?mode=INSPECTION' },
  { title: '物资', desc: '申领补给', route: '/packageStaff/pages/staff-material/index' },
  { title: '送餐', desc: '签收反馈', route: '/packageStaff/pages/staff-meals/index' },
  { title: '维修', desc: '到场验收', route: '/packageStaff/pages/staff-repairs/index' },
  { title: '排班', desc: '今日班次', route: '/packageStaff/pages/staff-schedule/index' },
  { title: '考勤', desc: '打卡统计', route: '/packageStaff/pages/staff-attendance/index' },
  { title: '审批', desc: '请假调班', route: '/packageStaff/pages/staff-approval/index' },
  { title: '交接', desc: '班次重点', route: '/packageStaff/pages/staff-handover/index' },
  { title: '上报', desc: '异常事件', route: '/packageStaff/pages/staff-incident/index' },
  { title: '扫码', desc: '巡检核验', route: '/packageStaff/pages/staff-patrol/index' },
  { title: '记录', desc: '执行回执', route: '/packageStaff/pages/staff-receipts/index' },
  { title: '异常', desc: '上报追踪', route: '/packageStaff/pages/staff-incidents/index' }
];

// 合并后端返回的动态入口与内置清单：优先保留后端顺序，再补齐缺失的功能项（按 route 去重）。
function mergeQuickEntries(remoteEntries = []) {
  const remote = Array.isArray(remoteEntries) ? remoteEntries.filter((item) => item && item.route) : [];
  const seen = new Set(remote.map((item) => item.route));
  const merged = remote.slice();
  STAFF_MENU.forEach((item) => {
    if (!seen.has(item.route)) {
      merged.push(item);
      seen.add(item.route);
    }
  });
  return merged;
}

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
      '/packageStaff/pages/staff-care-execution/index',
      '/packageStaff/pages/staff-vitals/index',
      '/packageStaff/pages/staff-handover/index'
    ],
    hidden: [
      '/packageStaff/pages/staff-repairs/index',
      '/packageStaff/pages/staff-material/index'
    ]
  },
  medical: {
    priority: [
      '/packageStaff/pages/staff-clinical/index?mode=MEDICATION',
      '/packageStaff/pages/staff-clinical/index?mode=INSPECTION',
      '/packageStaff/pages/staff-vitals/index'
    ],
    hidden: [
      '/packageStaff/pages/staff-repairs/index',
      '/packageStaff/pages/staff-meals/index',
      '/packageStaff/pages/staff-material/index'
    ]
  },
  logistics: {
    priority: [
      '/packageStaff/pages/staff-repairs/index',
      '/packageStaff/pages/staff-meals/index',
      '/packageStaff/pages/staff-material/index'
    ],
    hidden: [
      '/packageStaff/pages/staff-care-execution/index',
      '/packageStaff/pages/staff-vitals/index',
      '/packageStaff/pages/staff-clinical/index?mode=MEDICATION',
      '/packageStaff/pages/staff-clinical/index?mode=INSPECTION'
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
  if (task.module === 'CARE') return `/packageStaff/pages/staff-care-execution/index${task.id ? `?id=${encodeURIComponent(task.id)}` : ''}`;
  if (task.module === 'MEDICATION') return `/packageStaff/pages/staff-clinical/index?mode=MEDICATION${idParam}`;
  if (task.module === 'INSPECTION') return `/packageStaff/pages/staff-clinical/index?mode=INSPECTION${idParam}`;
  if (task.module === 'LOGISTICS') return `/packageStaff/pages/staff-repairs/index${task.id ? `?id=${encodeURIComponent(task.id)}` : ''}`;
  if (task.module === 'MEAL') return `/packageStaff/pages/staff-meals/index${task.id ? `?id=${encodeURIComponent(task.id)}` : ''}`;
  return task.id ? `/packageStaff/pages/staff-task-detail/index?id=${encodeURIComponent(task.id)}` : '/packageStaff/pages/staff-tasks/index';
}

function greetingText() {
  const hour = new Date().getHours();
  if (hour < 6) return '夜班辛苦';
  if (hour < 11) return '早上好';
  if (hour < 14) return '中午好';
  if (hour < 18) return '下午好';
  return '晚上好';
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
    attendanceRoute: '/packageStaff/pages/staff-schedule/index',
    nextPunchAction: attendance.nextPunchAction || '',
    nextPunchActionLabel: attendance.nextPunchActionLabel || '',
    openTodoCount: todoSummary.openCount || 0,
    dueTodayCount: todoSummary.dueTodayCount || 0,
    overdueCount: todoSummary.overdueCount || 0,
    incidentOpenCount: openIncidents.length,
    criticalTaskTitle: criticalTask ? criticalTask.title : '暂无紧急任务',
    criticalTaskMeta: criticalTask ? `${criticalTask.time || '时间待定'} · ${criticalTask.resident || '对象未填'} · ${criticalTask.room || '位置未填'}` : '现场状态平稳',
    criticalTaskRoute: criticalTask ? criticalTask.route : '/packageStaff/pages/staff-tasks/index',
    actionHint: criticalTask ? criticalTask.actionText || '立即处理' : '查看全部待办',
    riskTone: openIncidents.length || urgentTasks.length || todoSummary.overdueCount ? 'danger' : 'normal'
  };
}

Page({
  data: {
    loading: false,
    loadError: '',
    punching: false,
    greeting: greetingText(),
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
      attendanceRoute: '/packageStaff/pages/staff-schedule/index',
      nextPunchAction: '',
      nextPunchActionLabel: '',
      openTodoCount: 0,
      dueTodayCount: 0,
      overdueCount: 0,
      incidentOpenCount: 0,
      criticalTaskTitle: '暂无紧急任务',
      criticalTaskMeta: '现场状态平稳',
      criticalTaskRoute: '/packageStaff/pages/staff-tasks/index',
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
    this.setData({ greeting: greetingText() });
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
      dashboard.quickEntries = personalizeQuickEntries(mergeQuickEntries(dashboard.quickEntries), category);
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
  },
  // 工作台一键打卡：直接使用后端建议的下一步动作，打卡后刷新在岗状态，无需跳转到排班页。
  async punch(e) {
    if (this.data.punching) return;
    const action = (e.currentTarget.dataset.action || '').trim();
    if (!action) return;
    this.setData({ punching: true });
    try {
      await punchAttendance(action);
      wx.showToast({ title: '打卡成功', icon: 'success' });
      const attendance = await getAttendanceOverview();
      this.setData({
        'battle.attendanceText': attendance.todayStatusLabel || attendanceText(attendance.todayStatus),
        'battle.nextPunchAction': attendance.nextPunchAction || '',
        'battle.nextPunchActionLabel': attendance.nextPunchActionLabel || ''
      });
    } catch (error) {
      wx.showToast({ title: error.message || '打卡失败，请稍后重试', icon: 'none' });
    } finally {
      this.setData({ punching: false });
    }
  }
});
