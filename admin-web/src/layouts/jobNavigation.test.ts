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
    ['NURSING_EMPLOYEE', '护理部业务', '长者查询'],
    ['MEDICAL_EMPLOYEE', '医务部业务', '长者查询'],
    ['FINANCE_EMPLOYEE', '财务部业务', '财务查询'],
    ['LOGISTICS_EMPLOYEE', '后勤部业务', '房态查询'],
    ['HR_EMPLOYEE', '行政人事业务', '员工查询'],
    ['MARKETING_EMPLOYEE', '市场部业务', '客户查询']
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
    ['NURSING_MINISTER', '护理部管理', '护理排班', '护理报表'],
    ['MEDICAL_MINISTER', '医务部管理', '医护任务分配', '医务报表'],
    ['FINANCE_MINISTER', '财务部管理', '财务任务分配', '财务报表'],
    ['LOGISTICS_MINISTER', '后勤部管理', '后勤任务分配', '后勤报表'],
    ['HR_MINISTER', '行政人事管理', '排班与任务分配', '人事报表'],
    ['MARKETING_MINISTER', '市场部管理', '客户任务分配', '市场报表']
  ])('%s receives minister-only management entries', (role, management, assignment, report) => {
    const labels = jobMenu(role).map((item) => item.label)
    expect(labels).toContain(management)
    expect(labels).toContain('待我审批')
    expect(labels).toContain(assignment)
    expect(labels).toContain(report)
  })

  it('gives the director a concise operating and risk navigation', () => {
    const menu = jobMenu('DIRECTOR')
    expect(menu.map((item) => item.label)).toEqual([
      '经营总览', '部门运行', '风险与异常', '长者查询', '待我审批', '经营报表', '我的信息'
    ])
    expect(paths(menu)).not.toContain('/system')
  })

  it('keeps system administration separate from daily business', () => {
    const menu = jobMenu('SYS_ADMIN')
    expect(menu.map((item) => item.label)).toEqual([
      '系统管理首页', '角色与权限', '组织与账号', '基础配置', '安全与审计', '我的信息'
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
