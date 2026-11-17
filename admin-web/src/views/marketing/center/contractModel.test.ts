import dayjs from 'dayjs'
import { describe, expect, it } from 'vitest'
import { buildFlowSteps, contractStatusTag, handoverBlockers, handoverCounts } from './contractModel'
import type { CrmContractItem } from '../../../types'

const base: CrmContractItem = { id: '1', elderName: '张树生', elderPhone: '135', flowStage: 'PENDING_SIGN', status: 'PENDING_APPROVAL' }
const none = { finance: false as const, admitted: false }

describe('contractModel', () => {
  it('marks the first unfinished step as current', () => {
    const steps = buildFlowSteps(base, none)
    expect(steps.map((s) => s.state)).toEqual(['done', 'done', 'done', 'current', 'todo', 'todo'])
    expect(steps[3].hint).toContain('审核中')
    expect(buildFlowSteps({ ...base, flowStage: 'PENDING_ASSESSMENT' }, none)[1].state).toBe('current')
  })

  it('blocks handover until signed, data complete and finance confirmed', () => {
    expect(handoverBlockers(base, none)).toEqual(['合同未签署', '财务未完成入住费用审核'])
    const signed = { ...base, flowStage: 'SIGNED' as const, status: 'SIGNED' as const, elderPhone: '' }
    expect(handoverBlockers(signed, { finance: true, admitted: false })).toEqual(['缺少联系电话'])
    // 无权查看财务时交给入住办理接口校验
    expect(handoverBlockers({ ...signed, elderPhone: '1' }, { finance: null, admitted: false })).toEqual([])
    expect(handoverBlockers(signed, { finance: true, admitted: true })).toEqual(['已完成入住登记'])
  })

  it('prefers review states over the flow stage in the status tag', () => {
    expect(contractStatusTag(base).label).toBe('合同审核')
    expect(contractStatusTag({ ...base, status: 'APPROVED', flowStage: 'PENDING_ASSESSMENT' }).label).toBe('待评估')
  })

  it('counts waiting, incomplete and this-month handovers', () => {
    const today = dayjs('2026-11-16')
    const a = { ...base, id: 'a', flowStage: 'SIGNED' as const, status: 'SIGNED' as const }
    const b = { ...a, id: 'b', elderPhone: '' }
    const c = { ...a, id: 'c' }
    const ctx = (x: CrmContractItem) => x.id === 'c' ? { finance: true, admitted: true, admittedAt: '2026-11-03' } : { finance: true, admitted: false }
    expect(handoverCounts([a, b, c], ctx, today)).toEqual({ waiting: 1, doneThisMonth: 1, incomplete: 1 })
  })
})
