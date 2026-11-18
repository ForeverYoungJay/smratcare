import type { Component } from 'vue'
import type { RouteLocationNormalizedLoaded, RouteRecordRaw } from 'vue-router'
import { hasRouteAccess, moduleRolesByPath } from '../utils/roleAccess'
import { toCanonicalRoutePath } from '../router/routeAliases'

type Entry = [path: string, label: string, aliases?: string[]]
/** groups：标签较多时分两级展示，[一级标签, 该组包含的原页面路径] */
export interface WorkspaceDefinition { id: string; title: string; entries: Entry[]; groups?: Array<[label: string, paths: string[]]> }
export const workspaceDefinitions: WorkspaceDefinition[] = [
  // 财务 5 个主页面 + 主管可见的规则配置；同一页面的不同视角用筛选/抽屉，不再单独占标签
  { id: 'finance-collections', title: '收费与账户', entries: [
    ['/finance/bills/detail-query', '账单', ['/finance/payments/register', '/finance/bills/in-resident', '/finance/bills/in-admission']], ['/finance/admission-fee-audit', '入住费用审核'],
    ['/finance/payments/cashier-desk', '收银台', ['/finance/payments/records', '/finance/payments/shift-close']], ['/finance/payments/refund-reversal', '退款与冲正'],
    ['/finance/accounts/list', '长者账户', ['/finance/accounts/ledger', '/finance/prepaid-recharge']], ['/finance/deposit-management', '押金'], ['/finance/consumer-voucher', '消费券'],
    ['/finance/fees/payment-and-invoice', '票据管理', ['/finance/reconcile/invoice']]
  ], groups: [
    ['账单与收银', ['/finance/bills/detail-query', '/finance/admission-fee-audit', '/finance/payments/cashier-desk', '/finance/payments/refund-reversal']],
    ['长者资金账户', ['/finance/accounts/list', '/finance/deposit-management', '/finance/consumer-voucher']],
    ['票据管理', ['/finance/fees/payment-and-invoice']]
  ] },
  { id: 'finance-consumption', title: '消费与风险', entries: [
    ['/finance/flows/consumption', '消费流水', ['/finance/flows/medical', '/finance/flows/dining', '/finance/flows/logistics', '/finance/flows/medical-errors']], ['/finance/allocation/public-cost', '公共费用分摊', ['/finance/allocation/tasks']],
    ['/finance/electricity-fee', '长者电费'], ['/finance/bills/follow-up', '欠费与催缴'],
    ['/finance/bills/auto-deduct', '自动扣费'], ['/finance/reconcile/issue-center', '异常工单', ['/finance/reconcile/exception', '/finance/reconcile/ledger-health', '/finance/bills/auto-deduct-errors']],
    ['/finance/flows/adjustments', '费用调整']
  ], groups: [
    ['消费与分摊', ['/finance/flows/consumption', '/finance/allocation/public-cost', '/finance/electricity-fee']],
    ['欠费与异常', ['/finance/bills/follow-up', '/finance/bills/auto-deduct', '/finance/reconcile/issue-center', '/finance/flows/adjustments']]
  ] },
  { id: 'finance-settlement', title: '结算与月结', entries: [
    ['/finance/discharge/settlement', '退住结算', ['/finance/discharge/print-sign', '/finance/discharge/review', '/finance/discharge/status-sync']],
    ['/finance/reconcile/center', '日对账'], ['/finance/reconcile/month-close', '月结关账'], ['/finance/payroll', '工资付款']
  ], groups: [
    ['退住与费用审核', ['/finance/discharge/settlement']],
    ['对账与月结', ['/finance/reconcile/center', '/finance/reconcile/month-close']],
    ['工资付款', ['/finance/payroll']]
  ] },
  { id: 'finance-reports', title: '财务经营分析', entries: [
    ['/finance/reports/overall', '总收支'], ['/finance/reports/revenue-structure', '营收结构'],
    ['/finance/reports/floor-room-profit', '楼层房间'], ['/finance/reports/occupancy-consumption', '入住消费'],
    ['/finance/reports/monthly-ops', '月运营'], ['/finance/reports/excel', '报表导出']
  ] },
  { id: 'finance-settings', title: '财务规则配置', entries: [
    ['/finance/config/master-data', '配置总览'], ['/finance/allocation/subjects', '费用科目'],
    ['/finance/bills/rules', '计费规则'], ['/finance/allocation/rules', '分摊规则'],
    ['/finance/config/payment-channels', '支付渠道'], ['/finance/config/approval-flow', '审批规则'],
    ['/finance/accounts/warning-rules', '余额预警'], ['/finance/config/change-log', '变更记录']
  ], groups: [
    ['收费与计费规则', ['/finance/config/master-data', '/finance/allocation/subjects', '/finance/bills/rules', '/finance/allocation/rules', '/finance/accounts/warning-rules']],
    ['渠道与审批', ['/finance/config/payment-channels', '/finance/config/approval-flow', '/finance/config/change-log']]
  ] },
  // 客户/跟进/参观预订已收敛为 /marketing/customers、/marketing/visits 两个主页面（标签切换），不再走工作区
  { id: 'marketing-contracts', title: '合同台账', entries: [
    ['/marketing/contracts/pending', '待签合同'], ['/marketing/contracts/signed', '已签合同'],
    ['/marketing/funnel/signing', '签约推进'], ['/marketing/funnel/admission', '入住办理'],
    ['/marketing/contracts/renewal', '续签提醒'], ['/marketing/contracts/change', '合同变更'],
    ['/marketing/contracts/attachments', '合同附件']
  ] },
  { id: 'marketing-reports', title: '经营报表明细', entries: [
    ['/marketing/reports/conversion', '转化漏斗'], ['/marketing/reports/consultation', '咨询趋势'],
    ['/marketing/reports/followup', '跟进统计'], ['/marketing/reports/callback', '回访统计'],
    ['/marketing/reports/channel', '渠道评估'], ['/marketing/reports/channel-rank', '渠道排名'],
    ['/marketing/reports/unknown-channel', '渠道质量'], ['/marketing/reports/sales-performance', '人员业绩'],
    ['/marketing/reports/snapshots', '经营快照']
  ] },
  { id: 'hr-recruitment', title: '招聘与入职', entries: [
    ['/hr/recruitment/needs', '招聘办理'], ['/hr/profile/onboarding', '入职向导']
  ] },
  { id: 'hr-profile', title: '员工档案与合同', entries: [
    ['/hr/profile/basic', '员工档案'], ['/hr/profile/account-access', '账号与领导'],
    ['/hr/profile/contracts', '劳动合同'], ['/hr/profile/contract-reminders', '合同临期'],
    ['/hr/profile/contract-print', '合同打印'], ['/hr/profile/contract-templates', '合同模板'],
    ['/hr/profile/attachments', '档案附件']
  ] },
  { id: 'hr-attendance', title: '考勤与排班', entries: [
    ['/hr/attendance/records', '考勤记录'], ['/hr/attendance/abnormal', '异常核验'],
    ['/hr/attendance/calendar', '排班日历'], ['/hr/attendance/schemes', '值班方案'],
    ['/hr/attendance/groups', '护理班组'], ['/hr/attendance/leave-approval', '请假与换班审批'],
    ['/hr/attendance/access-control', '门禁记录'], ['/hr/attendance/control', '核定与调整']
  ] },
  // 人事 7 个主菜单：员工宿舍并入「薪酬与员工服务」作为一组标签，不再单独占一级菜单
  { id: 'hr-compensation', title: '薪酬与员工服务', entries: [
    ['/hr/expense/records', '报销记录'], ['/hr/expense/payroll', '工资批次'], ['/hr/expense/approval-flow', '费用申请与审批'],
    ['/hr/expense/meal-fee', '员工餐费'], ['/hr/expense/electricity-fee', '员工电费'],
    ['/hr/profile/social-security-reminders', '社保公积金'],
    ['/hr/expense/dormitory', '住宿台账与配置'], ['/hr/expense/dormitory-map', '宿舍房态图']
  ], groups: [
    ['薪酬与报销', ['/hr/expense/records', '/hr/expense/payroll', '/hr/expense/approval-flow', '/hr/expense/meal-fee', '/hr/expense/electricity-fee', '/hr/profile/social-security-reminders']],
    ['员工宿舍', ['/hr/expense/dormitory', '/hr/expense/dormitory-map']]
  ] },
  { id: 'hr-training', title: '培训与证书', entries: [
    ['/hr/development/records', '培训记录'], ['/hr/development/certificates', '证书档案', ['/hr/profile/certificates']],
    ['/hr/development/certificate-reminders', '证书临期']
  ] },
  { id: 'hr-performance', title: '绩效与积分', entries: [
    ['/hr/performance/reports', '绩效与兑换'], ['/hr/performance/reward-punishment', '奖惩'], ['/hr/performance/appeals', '申诉复核'],
    ['/hr/incentive/ledger', '积分台账'], ['/hr/incentive/rules', '评分与积分规则', ['/hr/performance/scoring-rules']]
  ] },
  { id: 'organization', title: '组织与权限', entries: [
    ['/system/department', '部门'], ['/system/org-info', '机构'], ['/system/role', '角色与权限']
  ] },
  { id: 'security', title: '安全与审计', entries: [
    ['/stats/sensitive-access-audit', '敏感访问审计'], ['/stats/export-audit', '导出审计'], ['/stats/security-policy', '安全策略']
  ] },
  // 业务字典：原「入住」下的床位类型/房间类型/区域设置（实为后勤页面的跳转）并入这里，按业务分组
  { id: 'dictionary', title: '基础数据', entries: [
    ['/base-config/elder-type', '长者类别'], ['/logistics/assets/bed-type-config', '床位类型'],
    ['/logistics/assets/room-type-config', '房间类型'], ['/logistics/assets/area-config', '区域设置'],
    ['/base-config/trial-package', '试住套餐'],
    ['/base-config/marketing/customer-tag', '客户标签'], ['/base-config/marketing/source-channel', '来源渠道'],
    ['/base-config/assessment', '评估类型'], ['/base-config/activity', '活动类型'],
    ['/base-config/community/maintenance-category', '维修分类'], ['/base-config/community/task-type-settings', '任务类型'],
    ['/base-config/fee', '费用类型'], ['/base-config/refund-reason', '退款原因'], ['/base-config/discharge-fee-settings', '退住费用']
  ], groups: [
    ['长者与床位', ['/base-config/elder-type', '/logistics/assets/bed-type-config', '/logistics/assets/room-type-config', '/logistics/assets/area-config', '/base-config/trial-package']],
    ['营销', ['/base-config/marketing/customer-tag', '/base-config/marketing/source-channel']],
    ['评估与活动', ['/base-config/assessment', '/base-config/activity']],
    ['后勤服务', ['/base-config/community/maintenance-category', '/base-config/community/task-type-settings']],
    ['费用', ['/base-config/fee', '/base-config/refund-reason', '/base-config/discharge-fee-settings']]
  ] },
  { id: 'documents', title: '制度与文档', entries: [
    ['/oa/document', '文档档案夹'], ['/hr/compliance/policies', '规章制度'], ['/hr/compliance/policy-alerts', '制度更新提醒']
  ] },
  { id: 'cards', title: '长者卡务', entries: [
    ['/oa/card/account', '卡账户'], ['/oa/card/recharge', '充值记录'], ['/oa/card/consume', '消费记录'],
    ['/hr/attendance/card-sync', '卡务同步']
  ] },
  { id: 'approvals', title: '审批办理', entries: [
    ['/oa/approval', '审批事项', ['/workbench/approvals']]
  ] },
  { id: 'work-reports', title: '工作总结', entries: [
    ['/workbench/reports', '全部总结'], ['/oa/work-execution/daily-report', '日总结'],
    ['/oa/work-execution/weekly-report', '周总结'], ['/oa/work-execution/monthly-report', '月总结'],
    ['/oa/work-execution/yearly-report', '年总结']
  ] }
]

/** 标签页顶部的合规提示（按原页面路径），来自财务模块设计中的风险约束 */
export const workspaceNotes: Record<string, { message: string; type: 'info' | 'warning' | 'error' }> = {
  '/finance/payments/cashier-desk': { type: 'info', message: '实际收款须与银行到账核实；未确认到账不得标记为已确认。收据与税务发票是不同凭证，不得互相替代。' },
  '/finance/accounts/list': { type: 'warning', message: '预收服务费、押金等不同性质资金分账管理，不得在界面合并为一个“可提现余额”；实际账务以银行对账及原始凭据为准。' },
  '/finance/deposit-management': { type: 'warning', message: '押金按存管账户、可用用途与退款规则管理，不等同于可用消费资金。' },
  '/finance/fees/payment-and-invoice': { type: 'warning', message: '开票信息须与实际交易一致；金额更正、服务中止等走红字流程，不直接删除原开票记录。' },
  '/finance/flows/consumption': { type: 'warning', message: '不得因录入两次或多部门重复提交而重复扣费；审核前不自动进入应收或扣款。' },
  '/finance/allocation/public-cost': { type: 'info', message: '公共费用分摊须明示依据、对象和计算方法，审核后再入账。' },
  '/finance/bills/follow-up': { type: 'info', message: '催缴沟通应保留联系人、方式、时间、约定与跟进责任人。' },
  '/finance/bills/auto-deduct': { type: 'error', message: '自动扣费不得绕开授权、合同条款和资金用途限制。' },
  '/finance/reconcile/issue-center': { type: 'warning', message: '账单减免、核销、冲销、退款均通过受控流程并保留原始记录，不直接修改余额。' },
  '/finance/discharge/settlement': { type: 'warning', message: '试算金额不是可直接支付的退款指令；押金用途、抵扣与退款渠道须符合合同及预收费监管规定。' },
  '/finance/reconcile/month-close': { type: 'warning', message: '未匹配流水、待处理异常及未审核调整项完成后才允许月结；月结后修改走调整期和审批，不直接改历史数据。' },
  '/finance/config/master-data': { type: 'error', message: '规则变化不得追溯覆盖已审核或已归档账单；须有版本号、生效日期与审批链。' },
  '/finance/config/approval-flow': { type: 'info', message: '申请人不得自动成为最终审核人；技术管理员不默认具有账务修改权限。' },
  // 业务字典：选项被历史记录引用，删除会让旧记录显示不出含义
  '/base-config/elder-type': { type: 'info', message: '字典选项已被长者、账单等记录引用：不再使用时请停用，不要删除或改成别的含义。' },
  '/logistics/assets/bed-type-config': { type: 'info', message: '床位类型、房间类型、区域会用于床位管理和床态图；修改名称会影响所有已使用的床位，停用不影响历史记录。' },
  '/base-config/fee': { type: 'warning', message: '费用类型调整不追溯已生成的账单；新增收费项目须与收费标准和合同一致。' },
  // 人力资源模块设计中的约束
  '/hr/recruitment/needs': { type: 'info', message: '候选人与员工是不同状态：录用审批与身份核验完成后才建立或关联正式员工档案；返聘先查重，保留历史劳动关系。' },
  '/hr/profile/basic': { type: 'warning', message: '一人一档：身份证、银行卡等默认脱敏；不得删除正式员工来“修改身份”，通过更正并保留记录。' },
  '/hr/profile/contracts': { type: 'info', message: '合同到期预警不等于自动结束劳动关系；已签署合同不覆写，续签和变更保留历史版本。' },
  '/hr/attendance/records': { type: 'info', message: '夜班跨日按实际时间戳匹配打卡；已核定的考勤期修改须走有审批的调整单。' },
  '/hr/attendance/calendar': { type: 'warning', message: '护理部、医务部的专业排班是实际值班来源，人事只引用和核定，不另建冲突的第二张值班表。' },
  '/hr/attendance/leave-approval': { type: 'info', message: '请假、加班、补卡未经审批不改变考勤结果；加班不因排班超时自动计薪。' },
  '/hr/expense/records': { type: 'warning', message: '一笔费用一个业务来源：报销须有票据和审批，由财务复核并付款；同一报销单不得重复支付。' },
  '/hr/profile/social-security-reminders': { type: 'info', message: '社保公积金按真实制度和申报结果登记，不以写死比例代替实际申报。' },
  '/hr/expense/dormitory': { type: 'warning', message: '同一床位任何时点只能有一个有效入住；宿舍维修由后勤办理；宿舍费用未经依据和审批不得自动从工资扣除。' },
  '/hr/development/certificates': { type: 'warning', message: '证书有效状态以人工复核为准，员工不能自行改为“有效”；到期提醒天数是机构配置，不等同于法定期限。' },
  '/hr/performance/reports': { type: 'warning', message: '绩效和积分不自动生成工资扣减；护理、医务指标应来自真实任务与质控记录。' },
  '/hr/incentive/ledger': { type: 'info', message: '积分更正用冲正记录，不覆盖历史流水；加减分须写明依据。' }
}

export interface WorkspaceSource {
  title?: string
  path: string
  roleGroups: string[][]
  permissionGroups: string[][]
}
export interface WorkspaceTab {
  key: string
  path: string
  label: string
  component: () => Promise<Component>
  props: RouteRecordRaw['props']
  sources: WorkspaceSource[]
}
export interface FunctionWorkspace { id: string; title: string; path: string; tabs: WorkspaceTab[]; groups?: WorkspaceDefinition['groups'] }
export const functionWorkspaces: FunctionWorkspace[] = []
export const retainedWorkspacePermissionPaths = new Set<string>()
const legacyLocations = new Map<string, { path: string; query: Record<string, any> }>()
const workspacePage = () => import('../views/FunctionWorkspace.vue')

export function canUseWorkspaceTab(tab: WorkspaceTab, roles: string[], pages: string[], permissions: string[]) {
  return tab.sources.some((source) => {
    const canonical = toCanonicalRoutePath(source.path)
    const pageAllowed = !pages.length || pages.some((p) => {
      const granted = toCanonicalRoutePath(p)
      return canonical === granted || canonical.startsWith(`${granted}/`)
    })
    const inferred = moduleRolesByPath(source.path)
    const roleAllowed = source.roleGroups.length
      ? source.roleGroups.every((group) => hasRouteAccess(roles, group, source.path))
      : inferred.length ? hasRouteAccess(roles, inferred, source.path) : source.permissionGroups.length > 0 || pages.length > 0
    return pageAllowed && roleAllowed &&
      source.permissionGroups.every((group) => group.some((p) => permissions.includes(p)))
  })
}

/** 两级标签：只保留当前账号可用的标签，空组不显示；没有配置 groups 时返回空数组 */
export function workspaceTabGroups(workspace: FunctionWorkspace | undefined, tabs: WorkspaceTab[]) {
  return (workspace?.groups || [])
    .map(([label, paths]) => ({ label, tabs: tabs.filter((tab) => paths.includes(tab.path)) }))
    .filter((group) => group.tabs.length > 0)
}

export function getFunctionWorkspace(id: unknown) {
  return functionWorkspaces.find((workspace) => workspace.id === id)
}

export function workspaceLocation(path: string, query: Record<string, any> = {}, hash = '') {
  for (const workspace of functionWorkspaces) {
    const tab = workspace.tabs.find((t) => t.sources.some((s) => s.path === path))
    if (tab) return { path: workspace.path, query: { ...query, view: tab.key }, hash }
  }
  const legacy = legacyLocations.get(path)
  return legacy ? { path: legacy.path, query: { ...legacy.query, ...query, view: legacy.query.view }, hash } : null
}

/** Snapshot each source's authorization before replacing its UI with the shared workspace. */
export function consolidateFunctionRoutes(routes: RouteRecordRaw[]): RouteRecordRaw[] {
  const sourceRoutes = new Map<string, { route: RouteRecordRaw; source: WorkspaceSource }>()
  function collect(list: RouteRecordRaw[], parent = '', roles: string[][] = [], permissions: string[][] = []) {
    for (const route of list) {
      const path = (route.path.startsWith('/') ? route.path : `${parent}/${route.path}`).replace(/\/+/g, '/').replace(/\/$/, '') || '/'
      const roleGroups = route.meta?.roles?.length ? [...roles, route.meta.roles] : roles
      const permissionGroups = route.meta?.permissions?.length ? [...permissions, route.meta.permissions] : permissions
      sourceRoutes.set(path, { route, source: { path, title: String(route.meta?.title || route.name || path), roleGroups, permissionGroups } })
      if (route.children) collect(route.children, path, roleGroups, permissionGroups)
    }
  }
  collect(routes)
  functionWorkspaces.splice(0)
  retainedWorkspacePermissionPaths.clear()
  legacyLocations.clear()
  for (const definition of workspaceDefinitions) {
    const tabs: WorkspaceTab[] = definition.entries.map(([path, label, aliases = []]) => {
      const found = sourceRoutes.get(path)
      if (!found?.route.component) throw new Error(`Missing workspace source: ${path}`)
      return {
        key: String(found.route.name), path, label, component: found.route.component as WorkspaceTab['component'],
        props: found.route.props,
        sources: [path, ...aliases].map((p) => {
          const entry = sourceRoutes.get(p)
          if (!entry) throw new Error(`Missing workspace alias: ${p}`)
          return entry.source
        })
      }
    })
    functionWorkspaces.push({ id: definition.id, title: definition.title, path: tabs[0].path, tabs, groups: definition.groups })
  }
  function rewrite(list: RouteRecordRaw[], parent = ''): RouteRecordRaw[] {
    return list.map((route) => {
      const path = (route.path.startsWith('/') ? route.path : `${parent}/${route.path}`).replace(/\/+/g, '/').replace(/\/$/, '') || '/'
      const workspace = functionWorkspaces.find((w) => w.path === path)
      if (workspace) return {
        ...route, redirect: undefined, component: workspacePage, props: { workspaceId: workspace.id },
        meta: { ...route.meta, title: workspace.title, workspaceId: workspace.id }
      } as RouteRecordRaw
      const location = workspaceLocation(path)
      // 合并进主页面的旧入口保留自己的预设筛选（如 ?billScene=、?panel=），再叠加调用方的 query
      const preset: Record<string, string> = {}
      if (typeof route.redirect === 'string') new URL(route.redirect, 'http://workspace.local').searchParams.forEach((value, key) => { preset[key] = value })
      if (location) return {
        ...route, component: undefined, props: undefined,
        redirect: (to) => workspaceLocation(path, { ...preset, ...to.query }, to.hash)!,
        meta: { ...route.meta, hidden: true, legacy: true, searchable: false }
      } as RouteRecordRaw
      const result = { ...route, children: route.children ? rewrite(route.children, path) : undefined } as RouteRecordRaw
      // Existing leaf aliases retain both their preset and the caller's query/hash.
      if (!route.children?.length && typeof route.redirect === 'string') {
        const url = new URL(route.redirect, 'http://workspace.local')
        if (workspaceLocation(url.pathname)) {
          const defaults: Record<string, string> = {}
          url.searchParams.forEach((value, key) => { defaults[key] = value })
          const target = workspaceLocation(url.pathname, defaults)!
          legacyLocations.set(path, { path: target.path, query: target.query })
          if (!route.meta?.hidden && !route.meta?.legacy) retainedWorkspacePermissionPaths.add(path)
          result.redirect = (to) => workspaceLocation(url.pathname, { ...defaults, ...to.query }, to.hash)!
          result.meta = { ...route.meta, hidden: true, legacy: true, searchable: false }
        }
      }
      return result
    })
  }
  return rewrite(routes)
}

export function workspacePageProps(tab: WorkspaceTab, route: RouteLocationNormalizedLoaded) {
  return typeof tab.props === 'function' ? tab.props(route) : tab.props === true ? route.params : tab.props || {}
}
