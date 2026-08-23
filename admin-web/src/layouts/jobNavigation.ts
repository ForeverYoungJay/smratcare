import { ROLE_CODES, type DepartmentCode, type RoleCode } from '../access/policy'
import type { MenuItem } from './menu'

type JobLink = { label: string; path: string; desc?: string }
type JobEntry = JobLink & { children?: JobLink[]; icon?: string; section?: MenuItem['navSection'] }
type DepartmentNavigation = {
  department: DepartmentCode
  employeeRole: RoleCode
  ministerRole: RoleCode
  section: MenuItem['navSection']
  businessLabel: string
  objectLabel: string
  objectPath: string
  business: JobLink[]
  managementLabel: string
  management: JobLink[]
  assignmentLabel: string
  assignmentPath: string
  reportLabel: string
  reportPath: string
}

const personalEntries = {
  home: { label: '我的工作台', path: '/workbench/overview', desc: '查看今天的任务、提醒和最近处理记录' },
  todo: { label: '我的待办', path: '/workbench/todo', desc: '集中处理待办和超时任务' },
  info: { label: '我的信息', path: '/workbench/my-info', desc: '查看个人资料和账号信息' },
  approvals: { label: '待我审批', path: '/workbench/approvals', desc: '处理需要本人审批的事项' }
} satisfies Record<string, JobLink>

const departmentNavigation: DepartmentNavigation[] = [
  {
    department: 'NURSING', employeeRole: ROLE_CODES.NURSING_EMPLOYEE, ministerRole: ROLE_CODES.NURSING_MINISTER, section: 'care',
    businessLabel: '护理部业务', objectLabel: '长者查询', objectPath: '/elder/list',
    business: [
      { label: '今日护理任务', path: '/care/today' },
      { label: '长者护理计划', path: '/care/workbench/plan' },
      { label: '护理记录', path: '/care/service/nursing-records' },
      { label: '交接班', path: '/medical-care/handovers' },
      { label: '医嘱执行', path: '/medical-care/order-executions' },
      { label: '异常任务', path: '/care/exception' }
    ],
    managementLabel: '护理部管理',
    management: [
      { label: '护理人员', path: '/care/staff/caregiver-info' },
      { label: '护理小组', path: '/care/staff/caregiver-groups' },
      { label: '护理质量', path: '/medical-care/nursing-quality' }
    ],
    assignmentLabel: '护理排班', assignmentPath: '/care/scheduling/shift-calendar',
    reportLabel: '护理报表', reportPath: '/care/service/nursing-reports'
  },
  {
    department: 'MEDICAL', employeeRole: ROLE_CODES.MEDICAL_EMPLOYEE, ministerRole: ROLE_CODES.MEDICAL_MINISTER, section: 'care',
    businessLabel: '医务部业务', objectLabel: '长者查询', objectPath: '/medical-care/residents',
    business: [
      { label: '今日巡诊', path: '/medical-care/rounds' },
      { label: '医嘱管理', path: '/medical-care/orders' },
      { label: '用药登记', path: '/medical-care/medication-registration' },
      { label: '健康档案', path: '/medical-care/emr' },
      { label: '设备与医疗告警', path: '/medical-care/smart-alerts' },
      { label: '医护交接', path: '/medical-care/handovers' }
    ],
    managementLabel: '医务部管理',
    management: [
      { label: '异常规则', path: '/medical-care/alert-rules' },
      { label: '医疗质量', path: '/medical-care/nursing-quality' },
      { label: '药事管理', path: '/medical-care/pharmacy' }
    ],
    assignmentLabel: '医护任务分配', assignmentPath: '/medical-care/smart-dispatch',
    reportLabel: '医务报表', reportPath: '/medical-care/ai-reports'
  },
  {
    department: 'FINANCE', employeeRole: ROLE_CODES.FINANCE_EMPLOYEE, ministerRole: ROLE_CODES.FINANCE_MINISTER, section: 'operations',
    businessLabel: '财务部业务', objectLabel: '财务查询', objectPath: '/finance/bills/detail-query',
    business: [
      { label: '今日收费', path: '/finance/workbench' },
      { label: '收费与收款', path: '/finance/payments/cashier-desk' },
      { label: '退款与冲正', path: '/finance/payments/refund-reversal' },
      { label: '欠费提醒', path: '/finance/bills/follow-up' },
      { label: '对账异常', path: '/finance/reconcile/exception' },
      { label: '退住结算', path: '/finance/discharge/settlement' }
    ],
    managementLabel: '财务部管理',
    management: [
      { label: '费用审核', path: '/finance/admission-fee-audit' },
      { label: '账务巡检', path: '/finance/reconcile/ledger-health' },
      { label: '预警规则', path: '/finance/accounts/warning-rules' }
    ],
    assignmentLabel: '财务任务分配', assignmentPath: '/finance/allocation/tasks',
    reportLabel: '财务报表', reportPath: '/finance/reports/overall'
  },
  {
    department: 'LOGISTICS', employeeRole: ROLE_CODES.LOGISTICS_EMPLOYEE, ministerRole: ROLE_CODES.LOGISTICS_MINISTER, section: 'support',
    businessLabel: '后勤部业务', objectLabel: '房态查询', objectPath: '/logistics/assets/room-state-map',
    business: [
      { label: '报修与维修', path: '/logistics/assets/maintenance-record' },
      { label: '库存预警', path: '/logistics/storage/alerts' },
      { label: '采购管理', path: '/logistics/storage/purchase' },
      { label: '入库管理', path: '/logistics/storage/inbound' },
      { label: '出库管理', path: '/logistics/storage/outbound' },
      { label: '送餐计划', path: '/logistics/dining/delivery-plan' }
    ],
    managementLabel: '后勤部管理',
    management: [
      { label: '设备档案', path: '/logistics/maintenance/assets' },
      { label: '仓库设置', path: '/logistics/storage/warehouse' },
      { label: '床位管理', path: '/logistics/assets/bed-management' }
    ],
    assignmentLabel: '后勤任务分配', assignmentPath: '/logistics/task-center',
    reportLabel: '后勤报表', reportPath: '/logistics/reports/maintenance-todo-log'
  },
  {
    department: 'HR', employeeRole: ROLE_CODES.HR_EMPLOYEE, ministerRole: ROLE_CODES.HR_MINISTER, section: 'support',
    businessLabel: '行政人事业务', objectLabel: '员工查询', objectPath: '/hr/overview',
    business: [
      { label: '通知公告', path: '/oa/notice' },
      { label: '制度与文档', path: '/oa/document' },
      { label: '我的考勤与请假', path: '/workbench/attendance' },
      { label: '合同和社保提醒', path: '/hr/profile/social-security-reminders' }
    ],
    managementLabel: '行政人事管理',
    management: [
      { label: '员工档案', path: '/hr/profile/basic' },
      { label: '招聘办理', path: '/hr/recruitment/needs' },
      { label: '培训与证书', path: '/hr/development/records' },
      { label: '制度管理', path: '/hr/compliance/policies' }
    ],
    assignmentLabel: '排班与任务分配', assignmentPath: '/hr/attendance/calendar',
    reportLabel: '人事报表', reportPath: '/hr/performance/reports'
  },
  {
    department: 'MARKETING', employeeRole: ROLE_CODES.MARKETING_EMPLOYEE, ministerRole: ROLE_CODES.MARKETING_MINISTER, section: 'operations',
    businessLabel: '市场部业务', objectLabel: '客户查询', objectPath: '/marketing/leads/all',
    business: [
      { label: '销售工作台', path: '/marketing/workbench' },
      { label: '今日跟进', path: '/marketing/interactions/today' },
      { label: '待回访客户', path: '/marketing/interactions/due' },
      { label: '到访与床位预定', path: '/marketing/reservation/records' },
      { label: '合同办理', path: '/marketing/contracts/pending' }
    ],
    managementLabel: '市场部管理',
    management: [
      { label: '渠道评估', path: '/marketing/reports/channel' },
      { label: '营销方案', path: '/marketing/plan' },
      { label: '回访质量', path: '/marketing/callback/score' }
    ],
    assignmentLabel: '客户任务分配', assignmentPath: '/marketing/funnel/consultation',
    reportLabel: '市场报表', reportPath: '/marketing/reports/conversion'
  }
]

function normalizeRoles(roles: string[]) {
  return new Set(roles.map((role) => String(role || '').trim().toUpperCase()))
}

function entry(item: JobEntry, availablePaths: Set<string>, order: number): MenuItem | null {
  const children = item.children
    ?.filter((child) => availablePaths.has(child.path))
    .map((child, index) => ({
      key: child.path,
      label: child.label,
      path: child.path,
      desc: child.desc,
      navOrder: index + 1
    }))
  if (children && children.length === 0) return null
  if (!children && !availablePaths.has(item.path)) return null
  return {
    key: children ? `job-${item.label}` : item.path,
    label: item.label,
    path: children ? undefined : item.path,
    desc: item.desc,
    icon: item.icon,
    navSection: item.section || 'entry',
    navOrder: order,
    children
  }
}

function buildDepartmentMenu(config: DepartmentNavigation, minister: boolean, availablePaths: Set<string>) {
  const entries: JobEntry[] = [
    { ...personalEntries.home, icon: 'HomeOutlined', section: 'entry' },
    { label: config.businessLabel, path: '', children: config.business, icon: 'AppstoreOutlined', section: config.section },
    { label: config.objectLabel, path: config.objectPath, icon: 'TeamOutlined', section: config.section },
    { ...personalEntries.todo, icon: 'CheckSquareOutlined', section: 'entry' }
  ]
  if (minister) {
    entries.push(
      { label: config.managementLabel, path: '', children: config.management, icon: 'ApartmentOutlined', section: 'support' },
      { ...personalEntries.approvals, icon: 'AuditOutlined', section: 'support' },
      { label: config.assignmentLabel, path: config.assignmentPath, icon: 'ScheduleOutlined', section: 'support' },
      { label: config.reportLabel, path: config.reportPath, icon: 'BarChartOutlined', section: 'operations' }
    )
  }
  entries.push({ ...personalEntries.info, icon: 'UserOutlined', section: 'entry' })
  return entries.map((item, index) => entry(item, availablePaths, index + 1)).filter(Boolean) as MenuItem[]
}

function buildDirectorMenu(availablePaths: Set<string>) {
  const entries: JobEntry[] = [
    { label: '经营总览', path: '/portal', icon: 'HomeOutlined' },
    {
      label: '部门运行', path: '', icon: 'ApartmentOutlined', section: 'operations', children: [
        { label: '护理运行', path: '/care/today' }, { label: '医务运行', path: '/medical-care/center' },
        { label: '财务运行', path: '/finance/workbench' }, { label: '后勤运行', path: '/logistics/workbench' },
        { label: '行政人事', path: '/hr/overview' }, { label: '市场运行', path: '/marketing/workbench' }
      ]
    },
    {
      label: '风险与异常', path: '', icon: 'AlertOutlined', section: 'compliance', children: [
        { label: '敏感数据审计', path: '/stats/sensitive-access-audit' },
        { label: '导出审计', path: '/stats/export-audit' },
        { label: '经营驾驶舱', path: '/stats/executive-cockpit' }
      ]
    },
    { label: '长者查询', path: '/elder/list', icon: 'TeamOutlined', section: 'care' },
    { ...personalEntries.approvals, icon: 'AuditOutlined', section: 'entry' },
    { label: '经营报表', path: '/stats/org/monthly-operation', icon: 'BarChartOutlined', section: 'operations' },
    { ...personalEntries.info, icon: 'UserOutlined', section: 'entry' }
  ]
  return entries.map((item, index) => entry(item, availablePaths, index + 1)).filter(Boolean) as MenuItem[]
}

function buildSystemMenu(availablePaths: Set<string>) {
  const entries: JobEntry[] = [
    { label: '系统管理首页', path: '/system', icon: 'SettingOutlined', section: 'system' },
    { label: '角色与权限', path: '/system/role', icon: 'SafetyCertificateOutlined', section: 'system' },
    {
      label: '组织与账号', path: '', icon: 'ApartmentOutlined', section: 'system', children: [
        { label: '机构信息', path: '/system/org-info' },
        { label: '部门管理', path: '/system/department' }
      ]
    },
    { label: '基础配置', path: '/base-config', icon: 'DatabaseOutlined', section: 'system' },
    {
      label: '安全与审计', path: '', icon: 'SafetyCertificateOutlined', section: 'compliance', children: [
        { label: '安全策略', path: '/stats/security-policy' },
        { label: '敏感数据审计', path: '/stats/sensitive-access-audit' },
        { label: '导出审计', path: '/stats/export-audit' }
      ]
    },
    { ...personalEntries.info, icon: 'UserOutlined', section: 'entry' }
  ]
  return entries.map((item, index) => entry(item, availablePaths, index + 1)).filter(Boolean) as MenuItem[]
}

function buildAdminMenu(availablePaths: Set<string>) {
  const entries: JobEntry[] = [
    { label: '业务总览', path: '/portal', icon: 'HomeOutlined' },
    {
      label: '部门业务', path: '', icon: 'ApartmentOutlined', section: 'operations', children: [
        { label: '护理业务', path: '/care/today' }, { label: '医务业务', path: '/medical-care/center' },
        { label: '财务业务', path: '/finance/workbench' }, { label: '后勤业务', path: '/logistics/workbench' },
        { label: '行政人事', path: '/hr/overview' }, { label: '市场业务', path: '/marketing/workbench' }
      ]
    },
    { label: '长者查询', path: '/elder/list', icon: 'TeamOutlined', section: 'care' },
    { ...personalEntries.approvals, icon: 'AuditOutlined' },
    { label: '业务基础配置', path: '/base-config', icon: 'DatabaseOutlined', section: 'system' },
    { label: '官网内容配置', path: '/system/site-config', icon: 'SettingOutlined', section: 'system' },
    { ...personalEntries.info, icon: 'UserOutlined' }
  ]
  return entries.map((item, index) => entry(item, availablePaths, index + 1)).filter(Boolean) as MenuItem[]
}

export function supportsJobNavigation(roles: string[]) {
  const normalized = normalizeRoles(roles)
  return normalized.has(ROLE_CODES.ADMIN) || normalized.has(ROLE_CODES.SYS_ADMIN) || normalized.has(ROLE_CODES.DIRECTOR) ||
    departmentNavigation.some((item) => normalized.has(item.employeeRole) || normalized.has(item.ministerRole))
}

export function buildJobNavigation(roles: string[], availablePaths: Set<string>): MenuItem[] {
  const normalized = normalizeRoles(roles)
  if (normalized.has(ROLE_CODES.SYS_ADMIN)) return buildSystemMenu(availablePaths)
  if (normalized.has(ROLE_CODES.DIRECTOR)) return buildDirectorMenu(availablePaths)
  if (normalized.has(ROLE_CODES.ADMIN)) return buildAdminMenu(availablePaths)
  const department = departmentNavigation.find((item) => normalized.has(item.ministerRole) || normalized.has(item.employeeRole))
  if (!department) return []
  return buildDepartmentMenu(department, normalized.has(department.ministerRole), availablePaths)
}
