import { describe, expect, it } from 'vitest'
import { createMemoryHistory, createRouter } from 'vue-router'
import { routes } from '../../../router/routes'
import { resolveRouteAccess } from '../../../utils/routeAccess'
import { normalizePagePermissions } from '../../../utils/pageAccess'
import { tabPagePermissions } from '../../medical/center/centerTabs'
import { functionWorkspaces, workspaceLocation } from '../../../workspaces/catalog'
import { ADMIN_CENTERS, cardActions, cardStatus, familyScopeSummary, surveyCaliber, taskColumn, upcomingBirthdays } from './adminTabs'

const router = createRouter({ history: createMemoryHistory(), routes })

describe('administration centers', () => {
  it('points every tab at an existing page', () => {
    for (const config of Object.values(ADMIN_CENTERS)) {
      for (const tab of config.tabs) {
        const matched = router.resolve(tab.path).matched
        expect(matched[matched.length - 1]?.components?.default, tab.path).toBeTruthy()
      }
    }
  })

  it('lists birthdays within the window, soonest first', () => {
    const list = [{ elderId: '1', elderName: '甲', daysUntil: 5 }, { elderId: '2', elderName: '乙', daysUntil: 0 }, { elderId: '3', elderName: '丙', daysUntil: 9 }]
    expect(upcomingBirthdays(list, 7).map((b) => b.elderName)).toEqual(['乙', '甲'])
  })

  it('keeps saved grants of pages that are now tabs inside a main page', () => {
    expect(normalizePagePermissions(['/oa/life/maintenance', '/medical-care/rounds', '/fire/day-patrol']))
      .toEqual(['/oa/life/maintenance', '/medical-care/rounds', '/fire/day-patrol'])
  })

  it('lets a role granted only the main page use its tabs, still honouring tab role limits', () => {
    const tabs = ADMIN_CENTERS.services.tabs
    const granted = ['/oa/services']
    expect(resolveRouteAccess(router, ['STAFF'], '/oa/life/maintenance', granted, []).canAccess).toBe(false)
    expect(resolveRouteAccess(router, ['STAFF'], '/oa/life/maintenance', tabPagePermissions(granted, tabs), []).canAccess).toBe(true)
    expect(tabPagePermissions([], tabs)).toEqual([])
    // 录像页只给后勤与管理层：即使主页面授权，保安也看不到
    expect(resolveRouteAccess(router, ['GUARD'], '/fire/video', tabPagePermissions(['/fire/control-center'], [{ path: '/fire/video' }]), []).canAccess).toBe(false)
  })

  it('summarises family authorisation, flagging legacy and expired bindings', () => {
    expect(familyScopeSummary(null, null, '2026-11-18').tone).toBe('warning')
    expect(familyScopeSummary('HEALTH,PHOTO', '2026-12-31', '2026-11-18').text).toBe('健康信息、照片分享 · 至 2026-12-31')
    expect(familyScopeSummary('', null, '2026-11-18').text).toContain('无数据权限')
    expect(familyScopeSummary('HEALTH', '2026-11-17', '2026-11-18').tone).toBe('danger')
  })

  it('offers only the card actions the backend allows for each state', () => {
    expect(cardStatus({ status: 'ACTIVE', lossFlag: 1 })).toBe('LOST')
    expect(cardActions({ status: 'ACTIVE', lossFlag: 1 }).map((a) => a.action)).toEqual(['CANCEL_LOSS', 'REISSUE', 'CANCEL'])
    expect(cardActions({ status: 'PENDING' }).map((a) => a.action)).toEqual(['ACTIVATE', 'CANCEL'])
    expect(cardActions({ status: 'REPLACED' })).toEqual([])
  })

  it('puts tasks into kanban columns and states the survey denominator', () => {
    expect([{ status: 'OPEN' }, { status: 'OPEN', assigneeId: '1' }, { status: 'SUBMITTED' }, { status: 'DONE' }].map(taskColumn))
      .toEqual(['unassigned', 'doing', 'submitted', 'done'])
    expect(surveyCaliber({ scoredSubmissions: 12, unscoredSubmissions: 3 }, ['2026-11-01', '2026-11-18']))
      .toContain('已评分提交 12 份，未评分/未答复 3 份不计入')
  })

  it('keeps bed/room/area types inside base data and website content inside the admin library', () => {
    expect(workspaceLocation('/logistics/assets/bed-type-config')?.path).toBe('/base-config/elder-type')
    const dictionary = functionWorkspaces.find((w) => w.id === 'dictionary')!
    expect(dictionary.groups?.map(([label]) => label)).toEqual(['长者与床位', '营销', '评估与活动', '后勤服务', '费用'])
    expect(ADMIN_CENTERS.library.tabs.find((t) => t.key === 'site')?.path).toBe('/system/site-config')
    // 官网内容只给管理员和行政人事部长
    expect(resolveRouteAccess(router, ['HR_EMPLOYEE'], '/system/site-config', [], []).canAccess).toBe(false)
  })
})
