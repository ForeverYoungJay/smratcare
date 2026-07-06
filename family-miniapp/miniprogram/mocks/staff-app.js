const taskGroups = [
  { key: 'care', title: '护理执行', count: 8, tone: 'warning', route: '/packageStaff/pages/staff-care/index' },
  { key: 'medication', title: '用药确认', count: 3, tone: 'danger', route: '/packageStaff/pages/staff-medical/index?module=medication' },
  { key: 'inspection', title: '巡房巡检', count: 2, tone: 'danger', route: '/packageStaff/pages/staff-medical/index?module=inspection' },
  { key: 'logistics', title: '后勤工单', count: 5, tone: 'warning', route: '/packageStaff/pages/staff-logistics/index' }
];

const tasks = [
  { id: 'care-1', module: 'CARE', title: '协助王桂兰晨间洗漱', resident: '王桂兰', room: '3F-302-1', time: '08:20', status: '待执行', priority: 'MEDIUM', actionText: '护理执行', evidenceRequired: true, checklist: ['核对床号和姓名', '完成晨间洗漱', '记录皮肤和精神状态'], route: '/packageStaff/pages/staff-care-execution/index?id=care-1' },
  { id: 'med-1', module: 'MEDICATION', title: '张建国早餐后用药', resident: '张建国', room: '2F-215-2', time: '08:40', status: '待确认', priority: 'HIGH', actionText: '用药确认', evidenceRequired: false, checklist: ['核对老人身份', '核对药品和剂量', '记录服药情况'], route: '/packageStaff/pages/staff-clinical/index?mode=MEDICATION&id=med-1' },
  { id: 'inspect-1', module: 'INSPECTION', title: '夜间异常巡检复核', resident: '刘秀英', room: '4F-408-1', time: '09:10', status: '异常待跟进', priority: 'HIGH', actionText: '巡检复核', evidenceRequired: true, checklist: ['复测生命体征', '拍照或语音记录', '同步责任医生'], route: '/packageStaff/pages/staff-clinical/index?mode=INSPECTION&id=inspect-1' },
  { id: 'repair-1', module: 'LOGISTICS', title: '306 卫生间扶手松动', resident: '公共区域', room: '3F-306', time: '09:30', status: '待到场', priority: 'MEDIUM', actionText: '维修处理', evidenceRequired: true, checklist: ['到场确认位置', '记录处理材料', '拍照验收'], route: '/packageStaff/pages/staff-repairs/index' },
  { id: 'meal-1', module: 'MEAL', title: '糖尿病软食餐配送', resident: '陈阿姨', room: '2F-208-1', time: '11:20', status: '待配送', priority: 'MEDIUM', actionText: '送餐签收', evidenceRequired: false, checklist: ['核对餐别', '确认禁忌标签', '送达签收'], route: '/packageStaff/pages/staff-meals/index' }
];

const schedule = [
  { date: '今天', shift: '早班', time: '07:30-15:30', area: '2F / 3F 护理区', status: '进行中' },
  { date: '明天', shift: '中班', time: '15:30-22:00', area: '3F 护理区', status: '已排班' },
  { date: '后天', shift: '休息', time: '-', area: '-', status: '休息' }
];

let attendanceState = {
  todayStatus: 'NOT_CHECKED_IN',
  todayStatusLabel: '未打卡',
  expectedWorkStart: '07:30',
  expectedWorkEnd: '15:30',
  checkInTime: '',
  checkOutTime: '',
  month: '2026-06',
  onDutyDays: 12,
  leaveDays: 1,
  lateCount: 1,
  earlyLeaveCount: 0,
  abnormalCount: 1,
  totalOutingMinutes: 45,
  totalLunchBreakMinutes: 420,
  days: [
    { date: '2026-06-16', weekLabel: '周二', checkInTime: '2026-06-16 07:32:00', checkOutTime: '', workMinutes: 0, status: 'ON_TIME', anomalyText: '', scheduleTitles: '早班护理区' },
    { date: '2026-06-15', weekLabel: '周一', checkInTime: '2026-06-15 07:46:00', checkOutTime: '2026-06-15 15:35:00', workMinutes: 469, status: 'LATE', anomalyText: '迟到 16 分钟', scheduleTitles: '早班护理区' }
  ]
};

const handovers = [
  { id: 'handover-1', time: '07:30', title: '早班接班重点', owner: '夜班护理员', content: '302-1 夜间起夜 2 次，需关注防跌倒；215-2 早餐后用药提醒。' },
  { id: 'handover-2', time: '15:20', title: '中班交班准备', owner: '护理主管', content: '午餐特殊餐已配送，306 扶手维修需后勤验收后同步。' }
];

const patrolPoints = [
  { id: 'P-302', name: '302 房巡房点', location: '3F-302 门口', status: '待扫码' },
  { id: 'P-MED', name: '药房核验点', location: '医护站', status: '待扫码' },
  { id: 'P-DINING', name: '餐车交接点', location: '2F 餐梯口', status: '待扫码' }
];

let receipts = [
  { id: 'receipt-1', taskId: 'care-1', module: 'CARE', moduleText: '护理执行', taskTitle: '协助王桂兰晨间洗漱', resident: '王桂兰', room: '3F-302-1', remark: '晨间洗漱完成；护理项目：晨间洗漱；皮肤状态：完整无红肿；精神状态：平稳配合；风险观察：起身需陪护', scanText: 'BED-302-1', checkedItems: '核对床号和姓名=true;完成护理项目=true;完成风险观察=true', photos: [], voiceUrl: 'https://mock.smartcare.local/staff/voice/receipt-1.mp3', voiceDurationSec: 12, receiptTime: '今日 08:35', status: 'DONE' },
  { id: 'receipt-2', taskId: 'med-1', module: 'MEDICATION', moduleText: '用药确认', taskTitle: '张建国早餐后用药', resident: '张建国', room: '2F-215-2', remark: '给药结果：已服药；给药方式：口服；早餐后已服药。', scanText: 'MED-215', checkedItems: '核对老人身份=true;核对药品剂量=true;确认给药完成=true', photos: [], voiceUrl: '', voiceDurationSec: 0, receiptTime: '今日 08:48', status: 'DONE' },
  { id: 'receipt-3', taskId: 'meal-1', module: 'MEAL', moduleText: '餐食配送', taskTitle: '糖尿病软食餐配送', resident: '陈阿姨', room: '2F-208-1', remark: '餐温正常；餐量反馈：约 3/4；签收人：陈阿姨', scanText: 'MEAL-208-1', checkedItems: '核对餐别和姓名=true;核对禁忌标签=true;送达签收=true', photos: [], voiceUrl: '', voiceDurationSec: 0, receiptTime: '今日 11:28', status: 'DONE' },
  { id: 'receipt-4', taskId: 'repair-1', module: 'LOGISTICS', moduleText: '后勤工单', taskTitle: '306 卫生间扶手松动', resident: '公共区域', room: '3F-306', remark: '故障类型：设施五金；处理结果：已修复；使用物料：膨胀螺丝 2 个；耗时：25 分钟', scanText: 'ROOM-306', checkedItems: '到场确认位置=true;完成维修处理=true;现场验收通过=true', photos: [], voiceUrl: '', voiceDurationSec: 0, receiptTime: '今日 10:05', status: 'DONE' }
];

let incidents = [
  { id: 'incident-1', incidentType: '跌倒风险', level: '紧急', resident: '王桂兰', location: '3F-302-1', description: '起身时步态不稳，已陪同坐下观察。', actionTaken: '员工端已上报，待主管跟进', scanText: 'ROOM-302', photos: [], voiceUrl: 'https://mock.smartcare.local/staff/voice/incident-1.mp3', voiceDurationSec: 18, incidentTime: '今日 09:12', status: 'OPEN' },
  { id: 'incident-2', incidentType: '设备维修', level: '一般', resident: '公共区域', location: '3F-306', description: '卫生间扶手松动，已设置提醒标识。', actionTaken: '员工端已上报，待主管跟进', scanText: '', photos: [], voiceUrl: '', voiceDurationSec: 0, incidentTime: '昨日 16:40', status: 'PROCESSING' }
];

const approvals = [
  { id: 'approval-1', approvalType: 'LEAVE', title: '病假申请：李护理', applicantName: '李护理', startTime: '2026-06-18T08:00:00', endTime: '2026-06-18T17:30:00', status: 'PENDING', remark: '发热复诊，已同步护理主管。', createTime: '2026-06-16T09:20:00' },
  { id: 'approval-2', approvalType: 'SHIFT_CHANGE', title: '调班申请：李护理', applicantName: '李护理', startTime: '2026-06-20T07:30:00', endTime: '2026-06-20T15:30:00', status: 'APPROVED', remark: '与中班同事协商互换。', createTime: '2026-06-14T18:10:00' },
  { id: 'approval-3', approvalType: 'MATERIAL_APPLY', title: '物资申领：护理垫 2 包', applicantName: '李护理', status: 'PENDING', remark: '3F 护理区夜班补给，预计今日用完。', formData: '{"materialCategory":"CARE","materialName":"护理垫","quantity":"2 包","useArea":"3F 护理区","urgency":"URGENT","source":"staff-mobile"}', createTime: '2026-06-16T11:40:00' }
];

let todos = [
  { id: 'todo-1', title: '【换班确认】王医生 请求与你换班', content: '[SHIFT_SWAP_TARGET:1001] 发起人：王医生；目标班次：2026-06-18 夜班', dueTime: '2026-06-16T18:00:00', status: 'OPEN', assigneeName: '李护理', createTime: '2026-06-16T09:30:00' },
  { id: 'todo-2', title: '审批待办：请假申请：陈护工', content: '[APPROVAL_FLOW:2001] 类型=LEAVE 申请人=陈护工 当前节点=部门主管审批', dueTime: '2026-06-16T17:00:00', status: 'OPEN', assigneeName: '李护理', createTime: '2026-06-16T08:50:00' },
  { id: 'todo-3', title: '午餐后回访 302-1', content: '午餐软食配送后 30 分钟确认吞咽与餐量。', dueTime: '2026-06-16T12:30:00', status: 'DONE', assigneeName: '李护理', createTime: '2026-06-15T18:00:00' }
];

const contacts = [
  { id: 1, realName: '张护士长', username: 'nurse-chief', staffNo: 'N001', phone: '13800138001', departmentId: 10, roleCodes: ['NURSING_MINISTER'] },
  { id: 2, realName: '王医生', username: 'doctor-wang', staffNo: 'D002', phone: '13800138002', departmentId: 20, roleCodes: ['MEDICAL_EMPLOYEE'] },
  { id: 3, realName: '陈后勤', username: 'logistics-chen', staffNo: 'L003', phone: '13800138003', departmentId: 30, roleCodes: ['LOGISTICS_EMPLOYEE'] },
  { id: 4, realName: '刘药师', username: 'pharmacist-liu', staffNo: 'M004', phone: '13800138004', departmentId: 20, roleCodes: ['MEDICAL_MINISTER'] }
];

let workReports = [
  { id: 'report-1', title: '早班护理日报', reportType: 'DAY', reportDate: '2026-06-16', contentSummary: '早班护理任务整体完成，重点关注 302-1 防跌倒。', completedWork: '完成晨间护理 8 项、用药确认 3 项、午餐特殊餐交接 1 项。', riskIssue: '302-1 起身不稳，已加密巡房。', nextPlan: '晚班继续关注夜间起夜和扶手维修验收。', status: 'SUBMITTED', reporterName: '李护理', createTime: '2026-06-16T15:20:00' },
  { id: 'report-2', title: '中班巡检草稿', reportType: 'DAY', reportDate: '2026-06-15', contentSummary: '中班巡检记录整理中。', completedWork: '完成 2F/3F 点位巡检。', riskIssue: '', nextPlan: '补充异常跟进结果。', status: 'DRAFT', reporterName: '李护理', createTime: '2026-06-15T21:10:00' }
];

const notices = [
  { id: 'notice-1', title: '端午节期间护理巡检加强通知', content: '节假日期间请各护理区加强夜间巡房、防跌倒提醒和家属沟通记录。异常情况需 10 分钟内在员工端上报。', publisherName: '护理部', publishTime: '2026-06-16T09:00:00', status: 'PUBLISHED' },
  { id: 'notice-2', title: '药品双人核对制度复训安排', content: '本周五 15:00 在医护站进行药品双人核对制度复训，请护理员、医生、药师准时参加。', publisherName: '医务部', publishTime: '2026-06-15T17:30:00', status: 'PUBLISHED' },
  { id: 'notice-3', title: '消防通道巡检点位更新', content: '3F 西侧消防通道新增巡检点位，请后勤和夜班人员按新二维码完成扫码核验。', publisherName: '后勤部', publishTime: '2026-06-14T10:20:00', status: 'PUBLISHED' }
];

let suggestions = [
  { id: 'suggestion-1', content: '【员工建议｜流程优化】建议晚班巡房点位增加“风险备注”快捷选项，便于交接时快速识别防跌倒重点。', proposerName: '李护理', contact: '13800138000', status: 'PENDING', createTime: '2026-06-16T10:20:00' },
  { id: 'suggestion-2', content: '【员工建议｜设备物资】3F 餐梯口手消补给点消耗较快，建议纳入每日后勤巡检补货清单。', proposerName: '李护理', contact: '13800138000', status: 'DONE', reply: '已加入后勤巡检清单，本周开始执行。', createTime: '2026-06-13T16:35:00' }
];

let vitalRecords = [
  { id: 'vital-1', elderName: '王桂兰', dataType: 'BP', dataValue: '138/88', measuredAt: '2026-06-16T10:30:00', source: 'staff-mobile', abnormalFlag: 0, remark: '晨间护理后复测，状态平稳。' },
  { id: 'vital-2', elderName: '刘秀英', dataType: 'TEMP', dataValue: '37.6', measuredAt: '2026-06-16T09:12:00', source: 'staff-mobile', abnormalFlag: 1, remark: '夜间异常巡检复核，已同步责任医生。' },
  { id: 'vital-3', elderName: '张建国', dataType: 'SPO2', dataValue: '96', measuredAt: '2026-06-15T18:20:00', source: 'staff-mobile', abnormalFlag: 0, remark: '餐后巡检补录。' }
];

function getStaffDashboard() {
  return {
    generatedAt: '今日 08:15',
    staffName: '李护理',
    shiftName: '早班 07:30-15:30',
    roleText: '护理员 / 医护协同',
    mobileIndex: 42,
    mobileLevel: 'MEDIUM',
    notices: ['08:40 有 3 项用药待确认', '巡检异常需在 30 分钟内跟进', '午餐特殊餐配送已生成'],
    quickEntries: [
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
      { title: '审批', desc: '请假调班', route: '/packageStaff/pages/staff-approval/index' },
      { title: '交接', desc: '班次重点', route: '/packageStaff/pages/staff-handover/index' },
      { title: '上报', desc: '异常事件', route: '/packageStaff/pages/staff-incident/index' },
      { title: '扫码', desc: '巡检核验', route: '/packageStaff/pages/staff-patrol/index' },
      { title: '记录', desc: '执行回执', route: '/packageStaff/pages/staff-receipts/index' },
      { title: '异常', desc: '上报追踪', route: '/packageStaff/pages/staff-incidents/index' }
    ],
    taskGroups,
    tasks: tasks.slice(0, 4)
  };
}

function getTaskList(filter = '') {
  if (!filter) return tasks;
  return tasks.filter((item) => item.module === filter);
}

function getTaskDetail(id) {
  return tasks.find((item) => item.id === id) || tasks[0];
}

function completeTask(id, payload = {}) {
  const task = getTaskDetail(id);
  const checkedItems = payload.checkedMap
    ? Object.keys(payload.checkedMap).map((key) => `${key}=${!!payload.checkedMap[key]}`).join(';')
    : '';
  const item = {
    id: `receipt-${Date.now()}`,
    taskId: id,
    module: task.module,
    moduleText: taskGroups.find((group) => group.key.toUpperCase() === task.module)?.title || task.module,
    taskTitle: task.title,
    resident: task.resident,
    room: task.room,
    remark: payload.remark || '',
    scanText: payload.scanText || '',
    checkedItems,
    photos: payload.photos || [],
    voiceUrl: payload.voiceUrl || '',
    voiceDurationSec: payload.voiceDurationSec || 0,
    receiptTime: '刚刚',
    status: 'DONE'
  };
  receipts.unshift(item);
  return { id, status: 'DONE', ...payload };
}

function getStaffProfile() {
  return {
    realName: '李护理',
    department: '护理部',
    roles: ['护理员工', '医护协同'],
    phone: '13800138000',
    orgName: '智养云合作机构',
    shiftName: '早班 07:30-15:30'
  };
}

function getSchedule() {
  return schedule;
}

// 与后端 AttendanceServiceImpl.resolveNextPunchAction 保持一致：由今日状态推导下一步打卡动作。
function nextPunchByStatus(status) {
  if (status === 'NOT_CHECKED_IN') return { nextPunchAction: 'IN', nextPunchActionLabel: '上班打卡' };
  if (status === 'ON_DUTY') return { nextPunchAction: 'OUT', nextPunchActionLabel: '下班打卡' };
  if (status === 'LUNCH_BREAK') return { nextPunchAction: 'END_LUNCH', nextPunchActionLabel: '结束午休' };
  if (status === 'OUTING') return { nextPunchAction: 'END_OUTING', nextPunchActionLabel: '外出结束' };
  return { nextPunchAction: '', nextPunchActionLabel: '' };
}

function getAttendanceOverview(month = '') {
  return {
    ...attendanceState,
    ...nextPunchByStatus(attendanceState.todayStatus),
    month: month || attendanceState.month
  };
}

function punchAttendance(action) {
  const now = '刚刚';
  if (action === 'IN') {
    attendanceState = { ...attendanceState, todayStatus: 'ON_DUTY', todayStatusLabel: '在岗', checkInTime: now };
  } else if (action === 'OUT') {
    attendanceState = { ...attendanceState, todayStatus: 'OFF_DUTY', todayStatusLabel: '已下班', checkOutTime: now };
  } else if (action === 'START_LUNCH') {
    attendanceState = { ...attendanceState, todayStatus: 'LUNCH_BREAK', todayStatusLabel: '午休中' };
  } else if (action === 'END_LUNCH') {
    attendanceState = { ...attendanceState, todayStatus: 'ON_DUTY', todayStatusLabel: '在岗' };
  } else if (action === 'START_OUTING') {
    attendanceState = { ...attendanceState, todayStatus: 'OUTING', todayStatusLabel: '外出中' };
  } else if (action === 'END_OUTING') {
    attendanceState = { ...attendanceState, todayStatus: 'ON_DUTY', todayStatusLabel: '在岗' };
  }
  return { status: attendanceState.todayStatus, ...attendanceState };
}

function getStaffApprovals(filter = {}) {
  return approvals.filter((item) => {
    const typeMatched = !filter.type || item.approvalType === filter.type;
    const statusMatched = !filter.status || item.status === filter.status;
    return typeMatched && statusMatched;
  });
}

function submitStaffApproval(payload = {}) {
  const item = {
    id: `approval-${Date.now()}`,
    applicantName: '李护理',
    status: 'PENDING',
    createTime: new Date().toISOString().slice(0, 19),
    ...payload
  };
  approvals.unshift(item);
  return item;
}

function todoSourceType(item) {
  const content = item && item.content ? item.content : '';
  if (content.indexOf('[APPROVAL_FLOW:') >= 0) return 'APPROVAL';
  if (content.indexOf('[BIRTHDAY_REMINDER:') >= 0) return 'BIRTHDAY';
  return 'NORMAL';
}

function getStaffTodoSummary(filter = {}) {
  const rows = getStaffTodos(filter);
  const openRows = rows.filter((item) => item.status === 'OPEN');
  return {
    totalCount: rows.length,
    openCount: openRows.length,
    doneCount: rows.filter((item) => item.status === 'DONE').length,
    dueTodayCount: openRows.length,
    overdueCount: openRows.filter((item) => String(item.dueTime || '').slice(0, 10) <= '2026-06-16').length,
    unassignedCount: rows.filter((item) => !item.assigneeName).length,
    birthdayOpenCount: openRows.filter((item) => todoSourceType(item) === 'BIRTHDAY').length,
    approvalOpenCount: openRows.filter((item) => todoSourceType(item) === 'APPROVAL').length,
    normalOpenCount: openRows.filter((item) => todoSourceType(item) === 'NORMAL').length
  };
}

function getStaffTodos(filter = {}) {
  return todos.filter((item) => {
    const statusMatched = !filter.status || item.status === filter.status;
    const itemSourceType = todoSourceType(item);
    const sourceMatched = !filter.sourceType
      || itemSourceType === filter.sourceType
      || (filter.sourceType === 'NORMAL' && itemSourceType !== 'APPROVAL' && itemSourceType !== 'BIRTHDAY');
    const keyword = filter.keyword || '';
    const keywordMatched = !keyword || `${item.title}${item.content}${item.assigneeName}`.indexOf(keyword) >= 0;
    return statusMatched && sourceMatched && keywordMatched;
  });
}

function completeStaffTodo(id) {
  todos = todos.map((item) => (String(item.id) === String(id) ? { ...item, status: 'DONE' } : item));
  return todos.find((item) => String(item.id) === String(id));
}

function getStaffContacts(filter = {}) {
  const keyword = String(filter.keyword || '').trim();
  return contacts.filter((item) => {
    if (!keyword) return true;
    return `${item.realName}${item.username}${item.staffNo}${item.phone}${item.roleCodes.join(',')}`.indexOf(keyword) >= 0;
  });
}

function getStaffReports(filter = {}) {
  const reportType = filter.reportType || 'DAY';
  const keyword = String(filter.keyword || '').trim();
  return workReports.filter((item) => {
    const typeMatched = !reportType || item.reportType === reportType;
    const statusMatched = !filter.status || item.status === filter.status;
    const keywordMatched = !keyword || `${item.title}${item.contentSummary}${item.completedWork}${item.riskIssue}${item.nextPlan}`.indexOf(keyword) >= 0;
    return typeMatched && statusMatched && keywordMatched;
  });
}

function getStaffReportSummary(filter = {}) {
  const rows = getStaffReports({ reportType: filter.reportType || 'DAY', keyword: filter.keyword || '' });
  return {
    totalCount: rows.length,
    draftCount: rows.filter((item) => item.status === 'DRAFT').length,
    submittedCount: rows.filter((item) => item.status === 'SUBMITTED').length,
    todaySubmittedCount: rows.filter((item) => item.status === 'SUBMITTED' && item.reportDate === '2026-06-16').length,
    weekSubmittedCount: rows.filter((item) => item.status === 'SUBMITTED').length,
    monthSubmittedCount: rows.filter((item) => item.status === 'SUBMITTED').length,
    dayTypeCount: rows.filter((item) => item.reportType === 'DAY').length,
    weekTypeCount: rows.filter((item) => item.reportType === 'WEEK').length,
    monthTypeCount: rows.filter((item) => item.reportType === 'MONTH').length,
    yearTypeCount: rows.filter((item) => item.reportType === 'YEAR').length,
    missingSummaryCount: rows.filter((item) => !item.contentSummary).length
  };
}

function submitStaffReport(payload = {}) {
  const item = {
    id: `report-${Date.now()}`,
    reporterName: '李护理',
    createTime: new Date().toISOString().slice(0, 19),
    ...payload
  };
  workReports.unshift(item);
  return item;
}

function submitStaffReportById(id) {
  workReports = workReports.map((item) => (String(item.id) === String(id) ? { ...item, status: 'SUBMITTED' } : item));
  return workReports.find((item) => String(item.id) === String(id));
}

function getStaffNotices(filter = {}) {
  const keyword = String(filter.keyword || '').trim();
  return notices.filter((item) => {
    if (!keyword) return item.status === 'PUBLISHED';
    return item.status === 'PUBLISHED' && `${item.title}${item.content}${item.publisherName}`.indexOf(keyword) >= 0;
  });
}

function getStaffNoticeDetail(id) {
  return notices.find((item) => String(item.id) === String(id)) || notices[0];
}

function getStaffSuggestions(filter = {}) {
  const keyword = String(filter.keyword || '').trim();
  return suggestions.filter((item) => {
    const statusMatched = !filter.status || item.status === filter.status;
    const keywordMatched = !keyword || `${item.content}${item.proposerName}${item.contact}`.indexOf(keyword) >= 0;
    return statusMatched && keywordMatched;
  });
}

function submitStaffSuggestion(payload = {}) {
  const item = {
    id: `suggestion-${Date.now()}`,
    status: 'PENDING',
    createTime: new Date().toISOString().slice(0, 19),
    ...payload
  };
  suggestions.unshift(item);
  return item;
}

function getStaffVitalRecords(filter = {}) {
  const keyword = String(filter.keyword || '').trim();
  return vitalRecords.filter((item) => {
    const typeMatched = !filter.dataType || item.dataType === filter.dataType;
    const abnormalMatched = typeof filter.abnormalFlag !== 'number' || Number(item.abnormalFlag || 0) === filter.abnormalFlag;
    const keywordMatched = !keyword || `${item.elderName}${item.dataValue}${item.remark}`.indexOf(keyword) >= 0;
    return typeMatched && abnormalMatched && keywordMatched;
  });
}

function submitStaffVitalRecord(payload = {}) {
  const item = {
    id: `vital-${Date.now()}`,
    source: 'staff-mobile',
    createTime: new Date().toISOString().slice(0, 19),
    ...payload
  };
  vitalRecords.unshift(item);
  return item;
}

function getHandovers() {
  return handovers;
}

function getPatrolPoints() {
  return patrolPoints;
}

function submitIncident(payload) {
  const item = {
    id: `incident-${Date.now()}`,
    incidentTime: '刚刚',
    actionTaken: '员工端已上报，待主管跟进',
    status: 'OPEN',
    message: '异常事件已上报',
    location: payload.room || payload.location || '',
    ...payload
  };
  incidents.unshift(item);
  return item;
}

function submitHandover(payload) {
  return {
    id: `handover-${Date.now()}`,
    ...payload
  };
}

function getTaskReceipts(filter = '') {
  if (!filter) return receipts;
  return receipts.filter((item) => item.module === filter);
}

function getIncidents(filter = {}) {
  return incidents.filter((item) => {
    const statusMatched = !filter.status || item.status === filter.status;
    const levelMatched = !filter.level || item.level === filter.level;
    return statusMatched && levelMatched;
  });
}

module.exports = {
  getStaffDashboard,
  getTaskList,
  getTaskDetail,
  completeTask,
  getStaffProfile,
  getSchedule,
  getAttendanceOverview,
  punchAttendance,
  getStaffApprovals,
  submitStaffApproval,
  getStaffTodoSummary,
  getStaffTodos,
  completeStaffTodo,
  getStaffContacts,
  getStaffReportSummary,
  getStaffReports,
  submitStaffReport,
  submitStaffReportById,
  getStaffNotices,
  getStaffNoticeDetail,
  getStaffSuggestions,
  submitStaffSuggestion,
  getStaffVitalRecords,
  submitStaffVitalRecord,
  getHandovers,
  getPatrolPoints,
  submitIncident,
  submitHandover,
  getTaskReceipts,
  getIncidents
};
