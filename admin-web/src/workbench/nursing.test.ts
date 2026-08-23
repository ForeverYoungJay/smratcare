import { describe, expect, it } from 'vitest'
import type { CareTaskItem } from '../types'
import { buildNursingQuery, buildNursingWorkbenchDetail, nursingTaskStatusLabel } from './nursing'
import { resolveWorkbenchProfile } from './model'

describe('nursing workbench adapter', () => {
  it('forces employee data to the signed-in staff id', () => {
    const profile = resolveWorkbenchProfile(['NURSING_EMPLOYEE'])
    expect(buildNursingQuery(profile, { date: '2026-08-23', staffId: '88' })).toEqual({ date: '2026-08-23', staffId: 88 })
    expect(() => buildNursingQuery(profile, { date: '2026-08-23' })).toThrow('缺少员工档案')
  })

  it('keeps minister data at department scope', () => {
    const profile = resolveWorkbenchProfile(['NURSING_MINISTER'])
    expect(buildNursingQuery(profile, { date: '2026-08-23', staffId: '88' })).toEqual({ date: '2026-08-23' })
  })

  it('orders risks first and derives related elders from real task rows', () => {
    const profile = resolveWorkbenchProfile(['NURSING_EMPLOYEE'])
    const rows: CareTaskItem[] = [
      { taskDailyId: 1, elderId: '10', elderName: '王奶奶', bedId: '1', roomNo: '201', taskName: '晨间护理', planTime: '2026-08-23T09:00:00', status: 'PENDING' },
      { taskDailyId: 2, elderId: '11', elderName: '李爷爷', bedId: '2', roomNo: '202', taskName: '巡视', planTime: '2026-08-23T08:00:00', status: 'EXCEPTION', overdueFlag: true }
    ]
    const detail = buildNursingWorkbenchDetail(profile, { assignedCount: 2 }, rows, [rows[0]])
    expect(detail.scope).toBe('SELF')
    expect(detail.upcomingTasks[0].id).toBe(2)
    expect(detail.relatedElders[0]).toMatchObject({ id: '11', riskCount: 1 })
    expect(detail.recentRecords).toHaveLength(1)
    expect(detail.hasPersonnelBreakdown).toBe(false)
  })

  it('uses explicit Chinese status labels', () => {
    expect(nursingTaskStatusLabel('DONE')).toBe('已完成')
    expect(nursingTaskStatusLabel('EXCEPTION')).toBe('异常待处理')
    expect(nursingTaskStatusLabel('UNKNOWN')).toBe('待执行')
  })
})
