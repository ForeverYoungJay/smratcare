import { describe, expect, it } from 'vitest'
import type { DischargeApplyItem } from '../../../types'
import { buildDischargeSteps, isDischargeInProgress, nextDischargeAction } from './dischargeProgress'

const base: DischargeApplyItem = { id: '1', elderId: '9', plannedDischargeDate: '2026-11-19', reason: '回家', status: 'APPROVED', autoDischargeStatus: 'PENDING_SETTLEMENT' }
const states = (apply: DischargeApplyItem) => buildDischargeSteps(apply).map((step) => step.stateText)

describe('dischargeProgress', () => {
  it('walks the six steps in order', () => {
    expect(nextDischargeAction({ ...base, status: 'PENDING' })).toBe('REVIEW')
    expect(states({ ...base, status: 'PENDING' })).toEqual(['待审批', '未开始', '未开始', '未开始', '未开始', '未开始'])

    expect(nextDischargeAction(base)).toBe('CARE_HANDOVER')
    expect(states(base)).toEqual(['已完成', '待办理', '未开始', '未开始', '未开始', '未开始'])

    const handed = { ...base, careHandoverTime: '2026-11-16T09:00:00', itemHandoverTime: '2026-11-16T10:00:00' }
    expect(nextDischargeAction(handed)).toBe('SETTLEMENT')
    expect(states(handed)).toEqual(['已完成', '已完成', '已完成', '待财务确认', '未开始', '未开始'])

    const settled = { ...handed, autoDischargeStatus: 'SUCCESS' as const }
    expect(nextDischargeAction(settled)).toBe('ARCHIVE')
    expect(states(settled)).toEqual(['已完成', '已完成', '已完成', '已完成', '已完成', '待办理'])
    expect(nextDischargeAction({ ...settled, archiveTime: '2026-11-17T09:00:00' })).toBeNull()
  })

  it('keeps only in-progress applies', () => {
    expect(isDischargeInProgress({ ...base, status: 'PENDING' })).toBe(true)
    expect(isDischargeInProgress(base)).toBe(true)
    expect(isDischargeInProgress({ ...base, status: 'REJECTED' })).toBe(false)
    expect(isDischargeInProgress({ ...base, archiveTime: '2026-11-17T09:00:00' })).toBe(false)
    // 分步办理上线前已结清的历史单
    expect(isDischargeInProgress({ ...base, autoDischargeStatus: 'SUCCESS' })).toBe(false)
    expect(isDischargeInProgress({ ...base, autoDischargeStatus: 'SUCCESS', itemHandoverTime: '2026-11-16T10:00:00' })).toBe(true)
  })
})
