/** 人事工作台：指标与重点提醒（数值都来自真实接口，不写死演示数据） */
export interface HrCounts {
  onJob: number
  recruiting: number
  contractsExpiring: number
  certificatesExpiring: number
  attendanceAbnormal: number
  leavePending: number
  expensePending: number
}

export const HR_WARNING_DAYS = 30

export function hrKpis(c: HrCounts) {
  return [
    { key: 'onJob', label: '在职员工', value: c.onJob, unit: '人', hint: '员工档案与合同', path: '/hr/profile/basic' },
    { key: 'recruiting', label: '招聘与入职待办', value: c.recruiting, unit: '项', hint: '待处理与进行中', path: '/hr/recruitment/needs', tone: 'info' },
    { key: 'contracts', label: '合同临期', value: c.contractsExpiring, unit: '份', hint: `${HR_WARNING_DAYS} 天内到期`, path: '/hr/profile/contract-reminders', tone: 'warning' },
    { key: 'certificates', label: '证书临期', value: c.certificatesExpiring, unit: '本', hint: `${HR_WARNING_DAYS} 天内到期或已过期`, path: '/hr/development/certificate-reminders', tone: 'warning' },
    { key: 'attendance', label: '考勤异常', value: c.attendanceAbnormal, unit: '条', hint: `请假待审批 ${c.leavePending} 条`, path: '/hr/attendance/abnormal', tone: 'danger' },
    { key: 'expense', label: '报销待审批', value: c.expensePending, unit: '笔', hint: '审批后由财务复核付款', path: '/hr/expense/approval-flow' }
  ]
}

/** 只列出有待办数量的提醒，按紧急程度排序 */
export function hrAlerts(c: HrCounts) {
  return [
    { label: '考勤异常待核验', count: c.attendanceAbnormal, hint: '核实后再进入月度考勤', path: '/hr/attendance/abnormal', tone: 'danger' },
    { label: '证书临期或过期', count: c.certificatesExpiring, hint: '影响岗位准入，同步提醒用人部门', path: '/hr/development/certificate-reminders', tone: 'danger' },
    { label: '合同即将到期', count: c.contractsExpiring, hint: '到期预警不自动结束劳动关系', path: '/hr/profile/contract-reminders', tone: 'warning' },
    { label: '请假待审批', count: c.leavePending, hint: '未审批不改变考勤结果', path: '/hr/attendance/leave-approval', tone: 'warning' },
    { label: '报销待审批', count: c.expensePending, hint: '票据齐全后提交财务', path: '/hr/expense/approval-flow', tone: 'warning' }
  ].filter((item) => item.count > 0)
}
