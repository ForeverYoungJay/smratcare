import { describe, expect, it } from 'vitest'
import { buildTaskCenterSummaryRequest } from './useLogisticsTaskCenterRequestLayer'

describe('useLogisticsTaskCenterRequestLayer helpers', () => {
  it('returns lite request when summary query is empty', () => {
    expect(buildTaskCenterSummaryRequest({})).toEqual({ lite: true })
  })

  it('normalizes summary query and keeps lite mode', () => {
    expect(
      buildTaskCenterSummaryRequest({
        windowDays: 0,
        expiryDays: 999,
        overdueDays: 2.4,
        maintenanceDueDays: -1
      })
    ).toEqual({
      windowDays: 1,
      expiryDays: 365,
      overdueDays: 2,
      maintenanceDueDays: 1,
      lite: true
    })
  })
})
