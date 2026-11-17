import { describe, expect, it } from 'vitest'
import { getMenuTree, type MenuItem } from './menu'
import { getRecommendedPagePermissions } from '../utils/pageAccess'

function jobMenu(role: string) {
  return getMenuTree([role], getRecommendedPagePermissions(role), { focused: true })
}

function paths(items: MenuItem[]): string[] {
  return items.flatMap((item) => [item.path || '', ...paths(item.children || [])]).filter(Boolean)
}

function depth(items: MenuItem[], current = 1): number {
  return items.reduce((max, item) => Math.max(max, item.children?.length ? depth(item.children, current + 1) : current), current)
}

describe('two-level job navigation', () => {
  it.each([
    ['MEDICAL_EMPLOYEE', '医务部业务', '长者查询'],
    ['LOGISTICS_EMPLOYEE', '后勤部业务', '房态查询'],
  ])('%s receives the five employee entry types', (role, businessLabel, objectLabel) => {
    const menu = jobMenu(role)
    expect(menu.map((item) => item.label)).toEqual([
      '我的工作台', businessLabel, objectLabel, '我的待办', '我的信息'
    ])
    expect(menu).toHaveLength(5)
    expect(depth(menu)).toBeLessThanOrEqual(2)
    expect(paths(menu)).not.toContain('/workbench/approvals')
  })

  it.each([
    ['MEDICAL_MINISTER', '医务部管理', '医护任务分配', '医务报表'],
    ['LOGISTICS_MINISTER', '后勤部管理', '后勤任务分配', '后勤报表'],
  ])('%s receives minister-only management entries', (role, management, assignment, report) => {
    const labels = jobMenu(role).map((item) => item.label)
    expect(labels).toContain(management)
    expect(labels).toContain('待我审批')
    expect(labels).toContain(assignment)
    expect(labels).toContain(report)
  })

  it.each([
    ['FINANCE_EMPLOYEE', '财务工作台', '/finance/bills/detail-query'],
    ['FINANCE_MINISTER', '财务工作台', '/finance/accounts/list'],
    ['MARKETING_EMPLOYEE', '市场工作台', '/marketing/customers'],
    ['MARKETING_MINISTER', '市场工作台', '/marketing/operations'],
    ['HR_EMPLOYEE', '人事工作台', '/hr/overview'],
    ['HR_MINISTER', '人事工作台', '/hr/profile/basic']
  ])('%s starts with its department and uses consolidated function entries', (role, title, path) => {
    const menu = jobMenu(role)
    expect(menu[0].label).toBe(title)
    expect(paths(menu)).toContain(path)
    expect(paths(menu)).not.toContain('/finance/allocation/tasks')
    expect(paths(menu)).not.toContain('/hr/profile/certificates')
    expect(paths(menu)).not.toContain('/marketing/interactions/due')
  })

  it('gives the director a concise operating and risk navigation', () => {
    const menu = jobMenu('DIRECTOR')
    expect(menu.map((item) => item.label)).toEqual([
      '经营总览', '部门运行', '风险与异常', '长者查询', '待我审批', '经营报表', '我的信息'
    ])
    expect(paths(menu)).not.toContain('/system')
  })

  it('gives marketing five main entries and keeps operations manager-only', () => {
    const main = ['/marketing/workbench', '/marketing/customers', '/marketing/visits', '/marketing/contracts', '/marketing/operations']
    expect(paths(jobMenu('MARKETING_MINISTER'))).toEqual(expect.arrayContaining(main))
    expect(paths(jobMenu('MARKETING_EMPLOYEE'))).toEqual(expect.arrayContaining(main.slice(0, 4)))
    expect(paths(jobMenu('MARKETING_EMPLOYEE'))).not.toContain('/marketing/operations')
  })

  it('gives nursing the five main pages for every nursing role', () => {
    const main = ['/care/workbench', '/care/tasks', '/care/residents', '/care/safety', '/care/operations']
    expect(paths(jobMenu('NURSING_MINISTER'))).toEqual(expect.arrayContaining(main))
    expect(paths(jobMenu('NURSING_EMPLOYEE'))).toEqual(expect.arrayContaining(main))
    expect(paths(jobMenu('NURSING_EMPLOYEE'))).not.toContain('/care/today')
  })

  it('links employee navigation to medical and logistics department workbenches', () => {
    expect(paths(jobMenu('MEDICAL_EMPLOYEE'))).toContain('/medical-care/center')
    expect(paths(jobMenu('LOGISTICS_EMPLOYEE'))).toContain('/logistics/workbench')
  })

  it('keeps system administration separate from daily business', () => {
    const menu = jobMenu('SYS_ADMIN')
    expect(menu.map((item) => item.label)).toEqual([
      '系统管理首页', '我的工作台', '我的待办', '组织与权限', '安全与审计', '我的信息'
    ])
    expect(paths(menu)).not.toContain('/portal')
    expect(paths(menu)).not.toContain('/medical-care')
    expect(paths(menu)).not.toContain('/finance')
  })

  it.each([
    'NURSING_EMPLOYEE', 'NURSING_MINISTER', 'MEDICAL_EMPLOYEE', 'MEDICAL_MINISTER',
    'FINANCE_EMPLOYEE', 'FINANCE_MINISTER', 'LOGISTICS_EMPLOYEE', 'LOGISTICS_MINISTER',
    'HR_EMPLOYEE', 'HR_MINISTER', 'MARKETING_EMPLOYEE', 'MARKETING_MINISTER',
    'DIRECTOR', 'ADMIN', 'SYS_ADMIN'
  ])('%s has no more than nine first-level items and no more than two levels', (role) => {
    const menu = jobMenu(role)
    expect(menu.length).toBeLessThanOrEqual(9)
    expect(depth(menu)).toBeLessThanOrEqual(2)
    const menuPaths = paths(menu)
    expect(new Set(menuPaths).size).toBe(menuPaths.length)
  })
})
