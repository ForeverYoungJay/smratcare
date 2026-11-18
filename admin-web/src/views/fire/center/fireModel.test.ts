import { describe, expect, it } from 'vitest'
import dayjs from 'dayjs'
import { createMemoryHistory, createRouter } from 'vue-router'
import { routes } from '../../../router/routes'
import { resolveRouteAccess } from '../../../utils/routeAccess'
import { FIRE_CENTERS, HANDOVER_ITEMS, RECORD_TYPE_PATH, buildChecklist, canCloseHazard, dailyTrend, parseChecklist, splitTemplateItems, templateIssueLine } from './fireModel'

const router = createRouter({ history: createMemoryHistory(), routes })
const access = (role: string, path: string) => resolveRouteAccess(router, [role], path, [], []).canAccess

describe('fire safety centers', () => {
  it('points every tab and hazard source at an existing page', () => {
    for (const config of Object.values(FIRE_CENTERS)) {
      for (const tab of config.tabs) {
        const matched = router.resolve(tab.path).matched
        expect(matched[matched.length - 1]?.components?.default, tab.path).toBeTruthy()
      }
    }
    for (const path of Object.values(RECORD_TYPE_PATH)) expect(router.resolve(path).matched.length, path).toBeGreaterThan(0)
  })

  it('keeps recorded video away from guards while they can still log duty shifts', () => {
    expect(access('GUARD', '/fire/control-center')).toBe(true)
    expect(access('GUARD', '/fire/video')).toBe(false)
    expect(access('GUARD', '/fire/video-wall')).toBe(false)
    expect(access('GUARD', '/fire/video-access')).toBe(false)
    expect(access('GUARD', '/fire/handover')).toBe(true)
    expect(access('GUARD', '/fire/settings')).toBe(false)
    expect(access('LOGISTICS_MINISTER', '/fire/settings')).toBe(true)
    expect(access('GUARD', '/fire/control-room-duty')).toBe(true)
    expect(access('LOGISTICS_EMPLOYEE', '/fire/video')).toBe(true)
  })

  it('lets only managers close a hazard', () => {
    expect(canCloseHazard(['GUARD'])).toBe(false)
    expect(canCloseHazard(['LOGISTICS_EMPLOYEE'])).toBe(false)
    expect(canCloseHazard(['logistics_minister'])).toBe(true)
  })

  it('counts patrols and newly found issues per day', () => {
    const today = dayjs('2026-11-18T12:00:00')
    const trend = dailyTrend([
      { recordType: 'DAY_PATROL', checkTime: '2026-11-18T09:00:00', issueDescription: '' },
      { recordType: 'NIGHT_PATROL', checkTime: '2026-11-18T02:00:00', issueDescription: '配电间门未关' },
      { recordType: 'FAULT_MAINTENANCE', checkTime: '2026-11-17T08:35:00', issueDescription: '探测器故障' },
      { recordType: 'DAY_PATROL', checkTime: '2026-11-01T09:00:00', issueDescription: '' }
    ], 3, today)
    expect(trend.map((d) => [d.label, d.patrols, d.issues])).toEqual([['11/16', 0, 0], ['11/17', 0, 1], ['11/18', 2, 1]])
  })

  it('turns unticked template items into an issue line and round-trips the handover checklist', () => {
    const items = splitTemplateItems('安全出口畅通\n\n 灭火器在位 \n疏散指示正常')
    expect(items).toEqual(['安全出口畅通', '灭火器在位', '疏散指示正常'])
    expect(templateIssueLine(items, items)).toBe('')
    expect(templateIssueLine(items, ['灭火器在位'])).toBe('检查未通过：安全出口畅通、疏散指示正常')
    const checklist = parseChecklist(buildChecklist([HANDOVER_ITEMS[0]]))
    expect(checklist.filter((c) => c.checked).map((c) => c.item)).toEqual([HANDOVER_ITEMS[0]])
    expect(parseChecklist('not json')).toEqual([])
  })
})
