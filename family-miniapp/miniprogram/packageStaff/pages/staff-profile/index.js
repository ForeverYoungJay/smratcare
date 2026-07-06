const { getStaffProfile } = require('../../../services/staff');

Page({
  data: {
    profile: null,
    quickEntries: [
      { title: '我的待办', desc: '提醒通知', route: '/packageStaff/pages/staff-todo/index' },
      { title: '通讯录', desc: '一键联系', route: '/packageStaff/pages/staff-contacts/index' },
      { title: '工作日报', desc: '汇报复盘', route: '/packageStaff/pages/staff-report/index' },
      { title: '通知公告', desc: '制度消息', route: '/packageStaff/pages/staff-notices/index' },
      { title: '建议反馈', desc: '一线声音', route: '/packageStaff/pages/staff-suggestions/index' },
      { title: '体征补录', desc: '健康采集', route: '/packageStaff/pages/staff-vitals/index' },
      { title: '护理执行', desc: '现场照护', route: '/packageStaff/pages/staff-care-execution/index' },
      { title: '物资申领', desc: '耗材补给', route: '/packageStaff/pages/staff-material/index' },
      { title: '送餐签收', desc: '餐量反馈', route: '/packageStaff/pages/staff-meals/index' },
      { title: '维修处理', desc: '到场验收', route: '/packageStaff/pages/staff-repairs/index' },
      { title: '用药确认', desc: '三查七对', route: '/packageStaff/pages/staff-clinical/index?mode=MEDICATION' },
      { title: '巡检复核', desc: '异常跟进', route: '/packageStaff/pages/staff-clinical/index?mode=INSPECTION' },
      { title: '我的排班', desc: '班次与区域', route: '/packageStaff/pages/staff-schedule/index' },
      { title: '我的考勤', desc: '打卡记录', route: '/packageStaff/pages/staff-attendance/index' },
      { title: '请假调班', desc: '移动审批', route: '/packageStaff/pages/staff-approval/index' },
      { title: '班次交接', desc: '重点事项', route: '/packageStaff/pages/staff-handover/index' },
      { title: '异常上报', desc: '现场留痕', route: '/packageStaff/pages/staff-incident/index' },
      { title: '异常记录', desc: '处理追踪', route: '/packageStaff/pages/staff-incidents/index' },
      { title: '巡检扫码', desc: '点位核验', route: '/packageStaff/pages/staff-patrol/index' },
      { title: '执行记录', desc: '回执追溯', route: '/packageStaff/pages/staff-receipts/index' }
    ],
    loadError: ''
  },
  onShow() {
    getApp().ensureLogin();
    this.loadData();
  },
  async loadData() {
    this.setData({ loadError: '' });
    try {
      this.setData({ profile: await getStaffProfile() });
    } catch (error) {
      this.setData({ loadError: error.message || '员工信息加载失败' });
    }
  },
  goHome() {
    wx.reLaunch({ url: '/packageStaff/pages/staff-home/index' });
  },
  goPage(e) {
    wx.navigateTo({ url: e.currentTarget.dataset.path });
  },
  logout() {
    getApp().logout();
  }
});
