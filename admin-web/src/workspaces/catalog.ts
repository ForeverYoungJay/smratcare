import type { Component } from 'vue'
import type { RouteLocationNormalizedLoaded, RouteRecordRaw } from 'vue-router'
import { hasRouteAccess, moduleRolesByPath } from '../utils/roleAccess'
import { toCanonicalRoutePath } from '../router/routeAliases'

type Entry = [path: string, label: string, aliases?: string[]]
export interface WorkspaceDefinition { id: string; title: string; entries: Entry[] }
export const workspaceDefinitions: WorkspaceDefinition[] = [
  // 财务 5 个主页面 + 主管可见的规则配置；标签超过 7 个时页头改为「办理内容」下拉
  { id: 'finance-collections', title: '收费与账户', entries: [
    ['/finance/bills/detail-query', '账单与收银'], ['/finance/bills/in-resident', '在住账单缴费', ['/finance/payments/register']],
    ['/finance/bills/in-admission', '入住首期账单'], ['/finance/payments/cashier-desk', '今日收银'],
    ['/finance/payments/records', '收款流水'], ['/finance/accounts/list', '长者资金账户'],
    ['/finance/accounts/ledger', '资金流水'], ['/finance/deposit-management', '押金'],
    ['/finance/prepaid-recharge', '预存充值'], ['/finance/consumer-voucher', '消费券'],
    ['/finance/fees/payment-and-invoice', '票据管理', ['/finance/reconcile/invoice']], ['/finance/payments/refund-reversal', '退款与冲正']
  ] },
  { id: 'finance-consumption', title: '消费与风险', entries: [
    ['/finance/flows/consumption', '消费与分摊'], ['/finance/flows/medical', '医护费用'],
    ['/finance/flows/dining', '餐饮扣费'], ['/finance/flows/logistics', '后勤收费'],
    ['/finance/allocation/public-cost', '公共费用分摊', ['/finance/allocation/tasks']], ['/finance/electricity-fee', '长者电费'],
    ['/finance/bills/follow-up', '欠费与催缴'], ['/finance/bills/auto-deduct', '自动扣费异常', ['/finance/bills/auto-deduct-errors']],
    ['/finance/reconcile/issue-center', '财务异常工单'], ['/finance/flows/medical-errors', '医护费用异常'],
    ['/finance/flows/adjustments', '费用调整']
  ] },
  { id: 'finance-settlement', title: '结算与月结', entries: [
    ['/finance/discharge/settlement', '退住结算', ['/finance/discharge/print-sign']],
    ['/finance/discharge/review', '退住费用审核'], ['/finance/admission-fee-audit', '入住费用审核'],
    ['/finance/discharge/status-sync', '结算状态核对'], ['/finance/reconcile/center', '日对账'],
    ['/finance/reconcile/exception', '对账异常'], ['/finance/payments/shift-close', '日结交班'],
    ['/finance/reconcile/ledger-health', '一致性巡检'], ['/finance/reconcile/month-close', '月结关账']
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
    ['/hr/profile/attachments', '档案附件'], ['/hr/profile/social-security-reminders', '社保办理']
  ] },
  { id: 'hr-attendance', title: '考勤与排班', entries: [
    ['/hr/attendance/records', '考勤记录'], ['/hr/attendance/abnormal', '异常核验'],
    ['/hr/attendance/calendar', '排班日历'], ['/hr/attendance/schemes', '值班方案'],
    ['/hr/attendance/groups', '护理班组'], ['/hr/attendance/leave-approval', '请假与换班审批'],
    ['/hr/attendance/access-control', '门禁记录']
  ] },
  { id: 'hr-expenses', title: '员工费用与报销', entries: [
    ['/hr/expense/records', '报销记录'], ['/hr/expense/approval-flow', '费用申请与审批'],
    ['/hr/expense/meal-fee', '员工餐费'], ['/hr/expense/electricity-fee', '员工电费']
  ] },
  { id: 'hr-dormitory', title: '员工宿舍', entries: [
    ['/hr/expense/dormitory', '住宿台账与配置'], ['/hr/expense/dormitory-map', '宿舍房态图']
  ] },
  { id: 'hr-training', title: '培训与证书', entries: [
    ['/hr/development/records', '培训记录'], ['/hr/development/certificates', '证书档案', ['/hr/profile/certificates']],
    ['/hr/development/certificate-reminders', '证书临期']
  ] },
  { id: 'hr-performance', title: '绩效与积分', entries: [
    ['/hr/performance/reports', '绩效与兑换'], ['/hr/performance/reward-punishment', '奖惩'],
    ['/hr/incentive/ledger', '积分台账'], ['/hr/incentive/rules', '评分与积分规则', ['/hr/performance/scoring-rules']]
  ] },
  { id: 'organization', title: '组织与权限', entries: [
    ['/system/department', '部门'], ['/system/org-info', '机构'], ['/system/role', '角色与权限']
  ] },
  { id: 'security', title: '安全与审计', entries: [
    ['/stats/sensitive-access-audit', '敏感访问审计'], ['/stats/export-audit', '导出审计'], ['/stats/security-policy', '安全策略']
  ] },
  { id: 'dictionary', title: '基础数据', entries: [
    ['/base-config/elder-type', '长者类别'], ['/base-config/marketing/customer-tag', '客户标签'],
    ['/base-config/marketing/source-channel', '来源渠道'], ['/base-config/assessment', '评估类型'],
    ['/base-config/activity', '活动类型'], ['/base-config/community/maintenance-category', '维修分类'],
    ['/base-config/community/task-type-settings', '任务类型'], ['/base-config/discharge-fee-settings', '退住费用'],
    ['/base-config/fee', '费用类型'], ['/base-config/refund-reason', '退款原因'], ['/base-config/trial-package', '试住套餐']
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
export interface FunctionWorkspace { id: string; title: string; path: string; tabs: WorkspaceTab[] }
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
    functionWorkspaces.push({ id: definition.id, title: definition.title, path: tabs[0].path, tabs })
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
      if (location) return {
        ...route, component: undefined, props: undefined,
        redirect: (to) => workspaceLocation(path, to.query, to.hash)!,
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
