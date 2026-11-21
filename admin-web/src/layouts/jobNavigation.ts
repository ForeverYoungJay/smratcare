import { ROLE_CODES, type DepartmentCode, type RoleCode } from '../access/policy'
import type { MenuItem } from './menu'
import { functionWorkspaces, workspaceLocation } from '../workspaces/catalog'

type JobLink = { label: string; path: string; desc?: string }
type Section = NonNullable<MenuItem['navSection']>
type JobEntry = JobLink & { children?: JobLink[]; icon?: string; section: Section; ministerOnly?: boolean }

const links = (pairs: string[][]): JobLink[] => pairs.map(([label, path]) => ({ label, path }))
const pages = (pairs: string[][], section: Section): JobEntry[] => links(pairs).map((link) => ({ ...link, section }))

/** 个人办公只在这一个分组里出现（所有岗位一致，7 个页面）；审批在「我的待办」里统一进入 */
const OFFICE: JobEntry = {
  label: '我的办公', path: '', icon: 'UserOutlined', section: 'entry', children: links([
    ['我的工作台', '/workbench/overview'], ['我的待办', '/workbench/todo'], ['协同消息', '/workbench/chat'], ['考勤与请假', '/workbench/attendance'],
    ['我的日程', '/workbench/schedule'], ['工作总结', '/workbench/reports'], ['我的工资与积分', '/workbench/payslips'],
    ['我的信息', '/workbench/my-info']
  ])
}

/** 数字孪生中心：所有岗位都能看园区总览，各模式的数据权限由服务端按岗位校验 */
const DIGITAL_TWIN: JobEntry = { label: '数字孪生中心', path: '/digital-twin', icon: 'GlobalOutlined', section: 'operations' }

/** 各部门的主页面（部门员工与部长共用；没有权限的页面由 availablePaths 自动过滤） */
const DEPARTMENTS: Array<{ department: DepartmentCode; roles: RoleCode[]; minister: RoleCode; entries: JobEntry[] }> = [
  { department: 'NURSING', roles: [ROLE_CODES.NURSING_EMPLOYEE, ROLE_CODES.NURSING_MINISTER], minister: ROLE_CODES.NURSING_MINISTER, entries: pages([
    ['护理工作台', '/care/workbench'], ['护理任务中心', '/care/tasks'], ['护理档案与评估', '/care/residents'],
    ['交接班与安全', '/care/safety'], ['排班与质控', '/care/operations'], ['医嘱执行', '/medical-care/order-executions']], 'care') },
  { department: 'MEDICAL', roles: [ROLE_CODES.MEDICAL_EMPLOYEE, ROLE_CODES.MEDICAL_MINISTER], minister: ROLE_CODES.MEDICAL_MINISTER, entries: pages([
    ['医务工作台', '/medical-care/workbench'], ['长者健康档案', '/medical-care/residents'], ['诊疗与医嘱', '/medical-care/clinical'],
    ['用药与医嘱执行', '/medical-care/medication'], ['健康监测与随访', '/medical-care/monitoring'], ['医护协同与质控', '/medical-care/coordination'],
    ['告警与应急', '/medical-care/alerts'], ['医护系统配置', '/medical-care/settings']], 'care') },
  { department: 'FINANCE', roles: [ROLE_CODES.FINANCE_EMPLOYEE, ROLE_CODES.FINANCE_MINISTER], minister: ROLE_CODES.FINANCE_MINISTER, entries: pages([
    ['财务工作台', '/finance/workbench'], ['收费与账户', '/finance/bills/detail-query'], ['消费与风险', '/finance/flows/consumption'],
    ['结算与月结', '/finance/discharge/settlement'], ['财务经营分析', '/finance/reports/overall'], ['财务规则配置', '/finance/config/master-data']], 'operations') },
  { department: 'MARKETING', roles: [ROLE_CODES.MARKETING_EMPLOYEE, ROLE_CODES.MARKETING_MINISTER], minister: ROLE_CODES.MARKETING_MINISTER, entries: pages([
    ['市场工作台', '/marketing/workbench'], ['客户与跟进', '/marketing/customers'], ['参观与预订', '/marketing/visits'],
    ['合同与转入住', '/marketing/contracts'], ['市场运营', '/marketing/operations'], ['入住办理中心', '/elder/admission-processing']], 'operations') },
  { department: 'LOGISTICS', roles: [ROLE_CODES.LOGISTICS_EMPLOYEE, ROLE_CODES.LOGISTICS_MINISTER], minister: ROLE_CODES.LOGISTICS_MINISTER, entries: [
    { label: '后勤部业务', path: '', icon: 'AppstoreOutlined', section: 'support', children: links([
      ['后勤部首页', '/logistics/workbench'], ['报修与维修', '/logistics/assets/maintenance-record'], ['房态查询', '/logistics/assets/room-state-map'],
      ['床位与状态', '/elder/bed-panorama'], ['库存预警', '/logistics/storage/alerts'], ['采购管理', '/logistics/storage/purchase'],
      ['入库管理', '/logistics/storage/inbound'], ['出库管理', '/logistics/storage/outbound'], ['送餐计划', '/logistics/dining/delivery-plan'],
      ['来访登记', '/elder/status-change/visit-register']]) },
    // 管理组（设备、仓库、床位、消防、任务分配、报表）只在部长菜单里出现
    { label: '后勤部管理', path: '', icon: 'ApartmentOutlined', section: 'support', ministerOnly: true, children: links([
      ['后勤任务分配', '/logistics/task-center'], ['后勤报表', '/logistics/reports/maintenance-todo-log'],
      ['设备档案', '/logistics/maintenance/assets'], ['仓库设置', '/logistics/storage/warehouse'], ['床位管理', '/logistics/assets/bed-management'],
      ['消防安全工作台', '/fire/overview'], ['防火巡查与检查', '/fire/inspection'], ['消防设施与维保', '/fire/facilities'],
      ['监控与控制室', '/fire/control-center'], ['隐患处置与分析', '/fire/safety'], ['消防系统配置', '/fire/settings']]) }
  ] },
  { department: 'HR', roles: [ROLE_CODES.HR_EMPLOYEE, ROLE_CODES.HR_MINISTER], minister: ROLE_CODES.HR_MINISTER, entries: [
    // 行政事务 6 个主页面在一级菜单，人事 7 个主菜单收进「人事管理」组（员工宿舍在「薪酬与员工服务」内）
    ...pages([['行政工作台', '/oa/workbench'], ['家属服务', '/oa/family'], ['协同办公', '/oa/collaboration'],
      ['生活服务', '/oa/services'], ['活动与满意度', '/oa/culture'], ['制度知识与公告', '/oa/library']], 'support'),
    { label: '人事管理', path: '', icon: 'TeamOutlined', section: 'support', children: links([
      ['人事工作台', '/hr/overview'], ['招聘与入职', '/hr/recruitment/needs'], ['员工档案与合同', '/hr/profile/basic'],
      ['考勤与排班', '/hr/attendance/records'], ['薪酬与员工服务', '/hr/expense/records'], ['培训与证书', '/hr/development/records'],
      ['绩效与积分', '/hr/performance/reports']]) }
  ] }
]

const DIRECTOR_ENTRIES: JobEntry[] = [
  { label: '经营总览', path: '/portal', icon: 'HomeOutlined', section: 'operations' },
  { label: '部门运行', path: '', icon: 'ApartmentOutlined', section: 'operations', children: links([
    ['护理运行', '/care/workbench'], ['医务运行', '/medical-care/workbench'], ['财务运行', '/finance/workbench'],
    ['后勤运行', '/logistics/workbench'], ['行政人事', '/hr/overview'], ['市场运行', '/marketing/workbench']]) },
  { label: '经营报表', path: '/stats/org/monthly-operation', icon: 'BarChartOutlined', section: 'operations' },
  { label: '长者查询', path: '/elder/list', icon: 'TeamOutlined', section: 'care' },
  { label: '风险与异常', path: '', icon: 'AlertOutlined', section: 'compliance', children: links([
    ['敏感数据审计', '/stats/sensitive-access-audit'], ['导出审计', '/stats/export-audit'], ['经营驾驶舱', '/stats/executive-cockpit']]) }
]

const ADMIN_ENTRIES: JobEntry[] = [
  { label: '业务总览', path: '/portal', icon: 'HomeOutlined', section: 'operations' },
  { label: '部门业务', path: '', icon: 'ApartmentOutlined', section: 'operations', children: links([
    ['护理业务', '/care/workbench'], ['医务业务', '/medical-care/workbench'], ['财务业务', '/finance/workbench'],
    ['后勤业务', '/logistics/workbench'], ['行政人事', '/hr/overview'], ['市场业务', '/marketing/workbench']]) },
  { label: '长者管理', path: '', icon: 'TeamOutlined', section: 'care', children: links([
    ['长者工作台', '/elder/in-hospital-overview'], ['长者列表', '/elder/list'], ['入住办理中心', '/elder/admission-processing'],
    ['床位与状态管理', '/elder/bed-panorama'], ['费用与退住', '/elder/status-change/discharge-settlement']]) },
  { label: '业务基础配置', path: '/base-config/elder-type', icon: 'DatabaseOutlined', section: 'system' },
  // 业务管理员可维护岗位的页面权限；系统超管角色仍是保留角色，后端拒绝新增、修改、删除
  { label: '角色与权限', path: '/system/role', icon: 'SafetyCertificateOutlined', section: 'system' },
  { label: '制度知识与公告', path: '/oa/library', icon: 'SettingOutlined', section: 'system' }
]

const SYSTEM_ENTRIES: JobEntry[] = [
  { label: '系统管理首页', path: '/system', icon: 'SettingOutlined', section: 'system' },
  { label: '角色与权限', path: '/system/role', icon: 'SafetyCertificateOutlined', section: 'system' },
  { label: '组织与账号', path: '', icon: 'ApartmentOutlined', section: 'system', children: links([
    ['机构信息', '/system/org-info'], ['部门管理', '/system/department']]) },
  { label: '基础配置', path: '/base-config/elder-type', icon: 'DatabaseOutlined', section: 'system' },
  { label: '安全与审计', path: '', icon: 'SafetyCertificateOutlined', section: 'compliance', children: links([
    ['安全策略', '/stats/security-policy'], ['敏感数据审计', '/stats/sensitive-access-audit'], ['导出审计', '/stats/export-audit']]) }
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
    navSection: item.section,
    navOrder: order,
    children
  }
}

function buildMenu(entries: JobEntry[], availablePaths: Set<string>) {
  return uniqueEntries([...entries, DIGITAL_TWIN, OFFICE].map((item, index) => entry(item, availablePaths, index + 1)).filter(Boolean) as MenuItem[])
}

export function supportsJobNavigation(roles: string[]) {
  const normalized = normalizeRoles(roles)
  return normalized.has(ROLE_CODES.ADMIN) || normalized.has(ROLE_CODES.SYS_ADMIN) || normalized.has(ROLE_CODES.DIRECTOR) ||
    DEPARTMENTS.some((item) => item.roles.some((role) => normalized.has(role)))
}

export function buildJobNavigation(roles: string[], availablePaths: Set<string>): MenuItem[] {
  const normalized = normalizeRoles(roles)
  if (normalized.has(ROLE_CODES.SYS_ADMIN)) return buildMenu(SYSTEM_ENTRIES, availablePaths)
  if (normalized.has(ROLE_CODES.DIRECTOR)) return buildMenu(DIRECTOR_ENTRIES, availablePaths)
  if (normalized.has(ROLE_CODES.ADMIN)) return buildMenu(ADMIN_ENTRIES, availablePaths)
  const department = DEPARTMENTS.find((item) => item.roles.some((role) => normalized.has(role)))
  if (!department) return []
  const minister = normalized.has(department.minister)
  return buildMenu(department.entries.filter((item) => minister || !item.ministerOnly), availablePaths)
}
