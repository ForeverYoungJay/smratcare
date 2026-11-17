import { ROLE_CODES, type DepartmentCode, type RoleCode } from '../access/policy'
import type { MenuItem } from './menu'
import { functionWorkspaces, workspaceLocation } from '../workspaces/catalog'

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
      { label: '护理工作台', path: '/care/workbench' },
      { label: '护理任务中心', path: '/care/tasks' },
      { label: '护理档案与评估', path: '/care/residents' },
      { label: '交接班与安全', path: '/care/safety' }
    ],
    managementLabel: '护理部管理',
    management: [
      { label: '排班与质控', path: '/care/operations' }
    ],
    assignmentLabel: '护理排班', assignmentPath: '/care/operations?tab=roster',
    reportLabel: '护理报表', reportPath: '/care/operations?tab=quality'
  },
  {
    department: 'MEDICAL', employeeRole: ROLE_CODES.MEDICAL_EMPLOYEE, ministerRole: ROLE_CODES.MEDICAL_MINISTER, section: 'care',
    businessLabel: '医务部业务', objectLabel: '长者查询', objectPath: '/medical-care/residents',
    business: [
      { label: '医务部首页', path: '/medical-care/center' },
      { label: '今日巡诊', path: '/medical-care/rounds' },
      { label: '医嘱管理', path: '/medical-care/orders' },
      { label: '用药登记', path: '/medical-care/medication-registration' },
      { label: '健康档案', path: '/medical-care/emr' },
      { label: '设备与医疗告警', path: '/medical-care/smart-alerts' },
      { label: '医护交接', path: '/medical-care/handovers' },
      { label: '长者评估', path: '/elder/assessment/ability/archive' }
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
      { label: '后勤部首页', path: '/logistics/workbench' },
      { label: '报修与维修', path: '/logistics/assets/maintenance-record' },
      { label: '库存预警', path: '/logistics/storage/alerts' },
      { label: '采购管理', path: '/logistics/storage/purchase' },
      { label: '入库管理', path: '/logistics/storage/inbound' },
      { label: '出库管理', path: '/logistics/storage/outbound' },
      { label: '送餐计划', path: '/logistics/dining/delivery-plan' },
      { label: '床位与状态', path: '/elder/bed-panorama' },
      { label: '来访登记', path: '/elder/status-change/visit-register' }
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
    businessLabel: '行政人事业务', objectLabel: '部门工作台', objectPath: '/hr/overview',
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
    businessLabel: '市场部业务', objectLabel: '客户查询', objectPath: '/marketing/customers',
    business: [
      { label: '市场工作台', path: '/marketing/workbench' },
      { label: '客户与跟进', path: '/marketing/customers' },
      { label: '参观与预订', path: '/marketing/visits' },
      { label: '合同与转入住', path: '/marketing/contracts' }
    ],
    managementLabel: '市场部管理',
    management: [
      { label: '市场运营', path: '/marketing/operations' }
    ],
    assignmentLabel: '客户任务分配', assignmentPath: '/marketing/customers',
    reportLabel: '市场报表', reportPath: '/marketing/operations'
  }
]


function uniqueEntries(items: MenuItem[]) {
  const seen = new Set<string>()
  return items.flatMap((item): MenuItem[] => {
    if (item.children) {
      const children = uniqueEntriesWithSeen(item.children, seen)
      if (children.length === 1) return [{ ...children[0], icon: item.icon || children[0].icon, navSection: item.navSection }]
      return children.length ? [{ ...item, children }] : []
    }
    if (!item.path || seen.has(item.path)) return []
    seen.add(item.path)
    return [item]
  })
}
function uniqueEntriesWithSeen(items: MenuItem[], seen: Set<string>) {
  return items.filter((item) => {
    if (!item.path || seen.has(item.path)) return false
    seen.add(item.path); return true
  })
}

function buildConsolidatedDepartmentMenu(department: string, availablePaths: Set<string>) {
  const links = (pairs: string[][]): JobLink[] => pairs.map(([label, path]) => ({ label, path }))
  const office: JobEntry = { label: '我的办公', path: '', icon: 'UserOutlined', children: links([
    ['我的待办', '/workbench/todo'], ['审批办理', '/oa/approval'], ['考勤与请假', '/workbench/attendance'],
    ['我的日程', '/workbench/schedule'], ['工作总结', '/workbench/reports'], ['我的信息', '/workbench/my-info']
  ]) }
  let entries: JobEntry[]
  if (department === 'FINANCE') entries = [
    ...links([['财务工作台', '/finance/workbench'], ['收银与票据', '/finance/payments/cashier-desk'],
      ['账单与缴费', '/finance/bills/detail-query'], ['长者资金账户', '/finance/accounts/list']]),
    { label: '日常账务', path: '', children: links([['消费与分摊', '/finance/flows/consumption'], ['欠费与扣费', '/finance/bills/follow-up'],
      ['财务异常处理', '/finance/reconcile/issue-center'], ['退住与费用审核', '/finance/discharge/settlement'], ['费用与退住', '/elder/status-change/discharge-settlement'], ['对账与月结', '/finance/reconcile/center']]) },
    { label: '报表与规则', path: '', children: links([['财务报表', '/finance/reports/overall'], ['财务规则', '/finance/config/master-data']]) }, office
  ]
  else if (department === 'NURSING') entries = [
    ...links([['护理工作台', '/care/workbench'], ['护理任务中心', '/care/tasks'], ['护理档案与评估', '/care/residents'],
      ['交接班与安全', '/care/safety'], ['排班与质控', '/care/operations'], ['医嘱执行', '/medical-care/order-executions']]), office
  ]
  else if (department === 'MARKETING') entries = [
    ...links([['市场工作台', '/marketing/workbench'], ['客户与跟进', '/marketing/customers'], ['参观与预订', '/marketing/visits'],
      ['合同与转入住', '/marketing/contracts'], ['市场运营', '/marketing/operations'], ['入住办理中心', '/elder/admission-processing']]), office
  ]
  else entries = [
    ...links([['人事工作台', '/hr/overview'], ['员工档案与合同', '/hr/profile/basic'], ['招聘与入职', '/hr/recruitment/needs'],
      ['考勤与排班', '/hr/attendance/records'], ['员工费用与报销', '/hr/expense/records'], ['员工宿舍', '/hr/expense/dormitory'],
      ['培训与证书', '/hr/development/records'], ['绩效与积分', '/hr/performance/reports']]),
    { ...office, children: [...(office.children || []), ...links([['制度与文档', '/oa/document'], ['通知公告', '/oa/notice']])] }
  ]
  return uniqueEntries(entries.map((item, index) => entry(item, availablePaths, index + 1)).filter(Boolean) as MenuItem[])
}

function normalizeRoles(roles: string[]) {
  return new Set(roles.map((role) => String(role || '').trim().toUpperCase()))
}

function entry(raw: JobEntry, availablePaths: Set<string>, order: number): MenuItem | null {
  const normalize = (link: JobLink): JobLink => {
    const location = workspaceLocation(link.path)
    if (!location) return link
    const workspace = functionWorkspaces.find((w) => w.path === location.path)!
    return { ...link, path: location.path, label: link.label === '待我审批' ? link.label : workspace.title }
  }
  const item = { ...raw, ...(raw.children ? {} : normalize(raw)), children: raw.children?.map(normalize) }
  const seenChildren = new Set<string>()
  const children = item.children
    ?.filter((child) => { if (!availablePaths.has(child.path) || seenChildren.has(child.path)) return false; seenChildren.add(child.path); return true })
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
  return uniqueEntries(entries.map((item, index) => entry(item, availablePaths, index + 1)).filter(Boolean) as MenuItem[])
}

function buildDirectorMenu(availablePaths: Set<string>) {
  const entries: JobEntry[] = [
    { label: '经营总览', path: '/portal', icon: 'HomeOutlined' },
    {
      label: '部门运行', path: '', icon: 'ApartmentOutlined', section: 'operations', children: [
        { label: '护理运行', path: '/care/workbench' }, { label: '医务运行', path: '/medical-care/center' },
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
  return uniqueEntries(entries.map((item, index) => entry(item, availablePaths, index + 1)).filter(Boolean) as MenuItem[])
}

function buildSystemMenu(availablePaths: Set<string>) {
  const entries: JobEntry[] = [
    { label: '系统管理首页', path: '/system', icon: 'SettingOutlined', section: 'system' },
    { ...personalEntries.home, icon: 'HomeOutlined', section: 'entry' },
    { ...personalEntries.todo, icon: 'CheckSquareOutlined', section: 'entry' },
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
  return uniqueEntries(entries.map((item, index) => entry(item, availablePaths, index + 1)).filter(Boolean) as MenuItem[])
}

function buildAdminMenu(availablePaths: Set<string>) {
  const entries: JobEntry[] = [
    { label: '业务总览', path: '/portal', icon: 'HomeOutlined' },
    { ...personalEntries.home, icon: 'AppstoreOutlined', section: 'entry' },
    { ...personalEntries.todo, icon: 'CheckSquareOutlined', section: 'entry' },
    {
      label: '部门业务', path: '', icon: 'ApartmentOutlined', section: 'operations', children: [
        { label: '护理业务', path: '/care/workbench' }, { label: '医务业务', path: '/medical-care/center' },
        { label: '财务业务', path: '/finance/workbench' }, { label: '后勤业务', path: '/logistics/workbench' },
        { label: '行政人事', path: '/hr/overview' }, { label: '市场业务', path: '/marketing/workbench' }
      ]
    },
    {
      label: '长者管理', path: '', icon: 'TeamOutlined', section: 'care', children: [
        { label: '长者工作台', path: '/elder/in-hospital-overview' },
        { label: '长者列表', path: '/elder/list' },
        { label: '入住办理中心', path: '/elder/admission-processing' },
        { label: '床位与状态管理', path: '/elder/bed-panorama' },
        { label: '费用与退住', path: '/elder/status-change/discharge-settlement' }
      ]
    },
    { ...personalEntries.approvals, icon: 'AuditOutlined' },
    { label: '业务基础配置', path: '/base-config', icon: 'DatabaseOutlined', section: 'system' },
    { label: '官网内容配置', path: '/system/site-config', icon: 'SettingOutlined', section: 'system' },
    { ...personalEntries.info, icon: 'UserOutlined' }
  ]
  return uniqueEntries(entries.map((item, index) => entry(item, availablePaths, index + 1)).filter(Boolean) as MenuItem[])
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
  if (['FINANCE', 'MARKETING', 'HR', 'NURSING'].includes(department.department)) return buildConsolidatedDepartmentMenu(department.department, availablePaths)
  return buildDepartmentMenu(department, normalized.has(department.ministerRole), availablePaths)
}
