import { getHrWorkbenchSummary } from '../api/hr'
import type { HrWorkbenchSummary } from '../types'
import type { WorkbenchProfile } from './model'
import { responseData, type RoleDepartmentWorkbenchDetail } from './operational'

export interface HrRoleWorkbenchResult { summary: HrWorkbenchSummary; detail: RoleDepartmentWorkbenchDetail }

export function buildHrRoleWorkbenchDetail(profile: WorkbenchProfile, data: HrWorkbenchSummary): RoleDepartmentWorkbenchDetail {
  const management = profile.hasManagementView
  return {
    department: 'HR',
    employeeTitle: '我的行政人事协作',
    managementTitle: '行政人事部今日运行',
    scopeLabel: management ? '行政人事部管理口径' : '机构协作提醒（非个人绩效）',
    headline: management ? '先处理请假、考勤和到期风险，再推进招聘、培训与行政协同。' : '先查看本人考勤和信息，再处理已授权的档案与社保协作。',
    description: management ? '人员与风险数量来自行政人事汇总接口。' : '机构级提醒只用于协作参考，不代表本人待办。',
    dataScopeNotice: management ? undefined : '当前员工角色可操作的人事页面较少，个人经办任务接口也尚未提供。',
    actions: [
      { label: '我的考勤', icon: '勤', path: '/workbench/attendance' },
      { label: '我的信息', icon: '我', path: '/workbench/profile' },
      { label: '社保提醒', icon: '保', path: '/hr/profile/social-security-reminders' },
      { label: '员工档案', icon: '档', path: '/hr/profile/basic', managementOnly: true },
      { label: '入离职办理', icon: '入', path: '/hr/recruitment/needs', managementOnly: true },
      { label: '合同提醒', icon: '合', path: '/hr/profile/contract-reminders', managementOnly: true },
      { label: '考勤异常', icon: '异', path: '/hr/attendance/abnormal', managementOnly: true },
      { label: '请假审批', icon: '审', path: '/hr/attendance/leave-approval', managementOnly: true }
    ],
    metrics: management ? [
      { key: 'leave', label: '请假待审批', value: data.pendingLeaveApprovalCount, suffix: '项', helper: '需要今日处理的请假流程', path: '/hr/attendance/leave-approval', tone: 'warning' },
      { key: 'attendance', label: '考勤异常', value: data.attendanceAbnormalCount, suffix: '项', helper: '迟到、缺卡和异常班次', path: '/hr/attendance/abnormal', tone: 'danger' },
      { key: 'contract', label: '合同临期', value: data.contractExpiringCount, suffix: '份', helper: `未来 ${Number(data.warningDays || 30)} 天到期`, path: '/hr/profile/contract-reminders', tone: 'warning' },
      { key: 'training', label: '今日培训', value: data.todayTrainingCount, suffix: '场', helper: '正在进行的培训安排', path: '/hr/development/records', tone: 'normal' }
    ] : [
      { key: 'training', label: '机构今日培训', value: data.todayTrainingCount, suffix: '场', helper: '机构协作参考', path: '/hr/overview', tone: 'normal' },
      { key: 'birthday', label: '今日生日关怀', value: data.birthdayTodayCount, suffix: '人', helper: '机构协作提醒', path: '/hr/overview', tone: 'pending' },
      { key: 'contract', label: '合同到期提醒', value: data.contractExpiringCount, suffix: '份', helper: '机构范围，按授权处理', path: '/hr/overview', tone: 'warning' },
      { key: 'social', label: '社保事项', helper: '进入已授权页面查看', path: '/hr/profile/social-security-reminders', tone: 'pending' }
    ],
    steps: [
      { title: '先确认本人事项', description: '查看考勤、个人资料和当天需要反馈的协作。', path: '/workbench/attendance' },
      { title: '再处理人员风险', description: '按权限核对合同、证书、社保和考勤异常。', path: management ? '/hr/profile/contract-reminders' : '/hr/profile/social-security-reminders' },
      { title: '最后推进组织协同', description: '把招聘、培训、通知和审批结果同步给相关部门。', path: management ? '/hr/overview' : '/workbench/todo' }
    ],
    managementSummary: management ? `机构在岗 ${Number(data.onJobCount || 0)} 人，离职状态 ${Number(data.leftCount || 0)} 人。` : undefined,
    personnelBreakdownAvailable: false
  }
}

export async function loadHrRoleWorkbench(profile: WorkbenchProfile): Promise<HrRoleWorkbenchResult> {
  const response = await getHrWorkbenchSummary(undefined, { silent403: true, silentError: true })
  const summary = responseData<HrWorkbenchSummary>(response)
  return { summary, detail: buildHrRoleWorkbenchDetail(profile, summary) }
}
