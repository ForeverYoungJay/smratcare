import { describe, expect, it } from 'vitest'
import { getMenuTree, type MenuItem } from './menu'
import { getRecommendedPagePermissions } from '../utils/pageAccess'
import { createMemoryHistory, createRouter, type RouteRecordRaw } from 'vue-router'
import { routes } from '../router/routes'

const router = createRouter({ history: createMemoryHistory(), routes })

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
  it('gives logistics staff the business group and keeps management for the minister', () => {
    expect(jobMenu('LOGISTICS_EMPLOYEE').map((item) => item.label)).toEqual(['后勤部业务', '数字孪生中心', '我的办公'])
    const minister = jobMenu('LOGISTICS_MINISTER')
    expect(minister.map((item) => item.label)).toEqual(['后勤部业务', '后勤部管理', '数字孪生中心', '我的办公'])
    expect(paths(minister)).toEqual(expect.arrayContaining(['/logistics/task-center', '/logistics/reports/maintenance-todo-log']))
  })

  it.each(['NURSING_EMPLOYEE', 'MEDICAL_MINISTER', 'FINANCE_EMPLOYEE', 'LOGISTICS_MINISTER', 'HR_MINISTER', 'MARKETING_EMPLOYEE', 'DIRECTOR', 'ADMIN', 'SYS_ADMIN'])(
    '%s has personal work entries in exactly one place, last', (role) => {
      const menu = jobMenu(role)
      const office = menu.filter((item) => item.navSection === 'entry')
      expect(office.map((item) => item.label)).toEqual(['我的办公'])
      expect(menu[menu.length - 1].label).toBe('我的办公')
      expect(office[0].children?.map((c) => c.label)).toEqual(['我的工作台', '我的待办', '考勤与请假', '我的日程', '工作总结', '我的工资与积分', '我的信息'])
      for (const personal of ['/workbench/todo', '/workbench/my-info']) {
        expect(paths(menu).filter((p) => p === personal)).toHaveLength(1)
        expect(paths(office)).toContain(personal)
      }
    })

  it.each([
    ['MARKETING_EMPLOYEE', '市场工作台', '/marketing/customers'],
    ['MARKETING_MINISTER', '市场工作台', '/marketing/operations'],
    ['HR_EMPLOYEE', '行政工作台', '/hr/overview'],
    ['HR_MINISTER', '行政工作台', '/hr/profile/basic']
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
      '经营总览', '部门运行', '经营报表', '长者查询', '风险与异常', '数字孪生中心', '我的办公'
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

  it('gives medical seven main pages and keeps settings for managers', () => {
    const main = ['/medical-care/workbench', '/medical-care/residents', '/medical-care/clinical', '/medical-care/medication',
      '/medical-care/monitoring', '/medical-care/coordination', '/medical-care/alerts']
    expect(paths(jobMenu('MEDICAL_MINISTER'))).toEqual(expect.arrayContaining([...main, '/medical-care/settings']))
    expect(paths(jobMenu('MEDICAL_EMPLOYEE'))).toEqual(expect.arrayContaining(main))
    expect(paths(jobMenu('MEDICAL_EMPLOYEE'))).not.toContain('/medical-care/settings')
  })

  it('gives finance five main pages and keeps rule settings for managers', () => {
    const main = ['/finance/workbench', '/finance/bills/detail-query', '/finance/flows/consumption', '/finance/discharge/settlement', '/finance/reports/overall']
    expect(jobMenu('FINANCE_MINISTER')[0].label).toBe('财务工作台')
    expect(paths(jobMenu('FINANCE_MINISTER'))).toEqual(expect.arrayContaining([...main, '/finance/config/master-data']))
    expect(paths(jobMenu('FINANCE_EMPLOYEE'))).toEqual(expect.arrayContaining(main))
    expect(paths(jobMenu('FINANCE_EMPLOYEE'))).not.toContain('/finance/config/master-data')
  })

  it('gives administration six main pages and keeps HR work in one group', () => {
    const menu = jobMenu('HR_EMPLOYEE')
    expect(menu.slice(0, 6).map((item) => item.path)).toEqual(['/oa/workbench', '/oa/family', '/oa/collaboration', '/oa/services', '/oa/culture', '/oa/library'])
    // 人事员工只有社保办理权限：落在「薪酬与员工服务」
    expect(menu.find((item) => item.label === '人事管理')?.children?.map((c) => c.path)).toContain('/hr/expense/records')
  })

  it('gives the HR minister the seven HR main menus with dormitory inside compensation', () => {
    const hr = jobMenu('HR_MINISTER').find((item) => item.label === '人事管理')
    expect(hr?.children?.map((c) => c.label)).toEqual(['人事工作台', '招聘与入职', '员工档案与合同', '考勤与排班', '薪酬与员工服务', '培训与证书', '绩效与积分'])
  })

  it('gives the logistics minister the five fire safety pages inside management', () => {
    const management = jobMenu('LOGISTICS_MINISTER').find((item) => item.label === '后勤部管理')
    expect(management?.children?.map((c) => c.path)).toEqual(expect.arrayContaining(['/fire/overview', '/fire/inspection', '/fire/facilities', '/fire/control-center', '/fire/safety']))
  })

  it('links employee navigation to medical and logistics department workbenches', () => {
    expect(paths(jobMenu('MEDICAL_EMPLOYEE'))).toContain('/medical-care/workbench')
    expect(paths(jobMenu('LOGISTICS_EMPLOYEE'))).toContain('/logistics/workbench')
  })

  it('gives the admin one base-data entry and the admin library instead of a separate website config', () => {
    const labels = jobMenu('ADMIN').flatMap((item) => [item.label, ...(item.children || []).map((c) => c.label)])
    expect(paths(jobMenu('ADMIN'))).toEqual(expect.arrayContaining(['/base-config/elder-type', '/oa/library']))
    expect(labels).not.toContain('官网内容配置')
  })

  it('keeps system administration separate from daily business', () => {
    const menu = jobMenu('SYS_ADMIN')
    expect(menu.map((item) => item.label)).toEqual([
      '系统管理首页', '组织与权限', '安全与审计', '数字孪生中心', '我的办公'
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
  ])('%s has no more than ten first-level items and no more than two levels', (role) => {
    const menu = jobMenu(role)
    // 9 个业务/办公入口 + 所有岗位共用的「数字孪生中心」
    expect(menu.length).toBeLessThanOrEqual(10)
    expect(depth(menu)).toBeLessThanOrEqual(2)
    const menuPaths = paths(menu)
    expect(new Set(menuPaths).size).toBe(menuPaths.length)
  })

  it.each([
    'NURSING_EMPLOYEE', 'NURSING_MINISTER', 'MEDICAL_EMPLOYEE', 'MEDICAL_MINISTER',
    'FINANCE_EMPLOYEE', 'FINANCE_MINISTER', 'LOGISTICS_EMPLOYEE', 'LOGISTICS_MINISTER',
    'HR_EMPLOYEE', 'HR_MINISTER', 'MARKETING_EMPLOYEE', 'MARKETING_MINISTER',
    'DIRECTOR', 'ADMIN', 'SYS_ADMIN'
  ])('%s menu links all open a real page', (role) => {
    for (const path of paths(jobMenu(role))) {
      expect(router.resolve(path).matched.length, path).toBeGreaterThan(0)
      expect(router.resolve(path).name, path).not.toBe('NotFound')
    }
  })

  it('points every string redirect in the route table at a real page', () => {
    const broken: string[] = []
    const walk = (list: RouteRecordRaw[], parent = '') => list.forEach((route) => {
      const full = route.path.startsWith('/') ? route.path : `${parent}/${route.path}`.replace(/\/+/g, '/')
      if (typeof route.redirect === 'string') {
        const target = router.resolve(route.redirect)
        if (!target.matched.length || target.name === 'NotFound') broken.push(`${full} -> ${route.redirect}`)
      }
      if (route.children) walk(route.children, full)
    })
    walk(routes)
    expect(broken).toEqual([])
  })
})
