import { describe, expect, it } from 'vitest'
import type { CareTaskItem } from '../../../types'
import { SAFETY_TABS, checkRoster, formatRate, ratioPercent, hourlyCompletion, incidentActions, progressByStaff, resolveTab, sortTodo } from './nursingModel'
import { buildExecution, type ExecuteForm } from './taskExecution'
import { assessmentFlowStep } from './residentCare'

const task = (over: Partial<CareTaskItem>): CareTaskItem => ({
  taskDailyId: 1, elderId: '7', elderName: '王秀兰', bedId: '1', roomNo: 'A301', taskName: '晨间洗漱', planTime: '2026-11-17 09:00:00', status: 'PENDING', ...over
})

const base: ExecuteForm = { result: 'DONE', reason: '', bedQrCode: 'QR1', observation: '', incidentType: '', severity: 'MEDIUM', checkedItems: [] }

describe('nursing model', () => {
  it('falls back to the first tab for unknown query values', () => {
    expect(resolveTab(SAFETY_TABS, { tab: 'incident' })).toBe('incident')
    expect(resolveTab(SAFETY_TABS, { tab: 'nope' })).toBe('handover')
  })

  it('only lets managers close or reopen, and always allows a correction', () => {
    expect(incidentActions('PENDING_REVIEW', false)).toEqual(['CORRECT'])
    expect(incidentActions('PENDING_REVIEW', true)).toEqual(['CLOSE', 'REOPEN', 'ASSIGN', 'CORRECT'])
    expect(incidentActions('CLOSED', true)).toEqual(['CORRECT'])
    expect(incidentActions('REPORTED', false)).toEqual(['ACCEPT', 'CORRECT'])
  })

  it('shows no-data instead of a rate when the denominator is zero', () => {
    expect(formatRate({ rate: null, denominator: 0 })).toBe('暂无数据')
    expect(formatRate({ rate: 96.2, denominator: 10 })).toBe('96.2%')
    expect(ratioPercent(0.9244)).toBe(92.4)
    expect(ratioPercent('1')).toBe(100)
    expect(ratioPercent(null)).toBeUndefined()
  })

  it('finds double-booked staff and uncovered shifts', () => {
    const days = ['2026-11-16', '2026-11-17']
    const result = checkRoster([
      { staffId: 1, staffName: '张护员', dutyDate: '2026-11-16', shiftCode: 'D' },
      { staffId: 1, staffName: '张护员', dutyDate: '2026-11-16', shiftCode: 'N' },
      { staffId: 2, dutyDate: '2026-11-17', shiftCode: 'D' }
    ], days, ['D', 'N'])
    expect(result.duplicates).toEqual([{ staffName: '张护员', dutyDate: '2026-11-16', shifts: ['D', 'N'] }])
    expect(result.gaps).toEqual([{ dutyDate: '2026-11-17', shiftCode: 'N' }])
  })

  it('puts overdue then exception tasks first and hides done ones', () => {
    const list = [
      task({ taskDailyId: 1, planTime: '2026-11-17 08:00:00' }),
      task({ taskDailyId: 2, status: 'DONE' }),
      task({ taskDailyId: 3, status: 'EXCEPTION', planTime: '2026-11-17 10:00:00' }),
      task({ taskDailyId: 4, overdueFlag: true, planTime: '2026-11-17 11:00:00' })
    ]
    expect(sortTodo(list).map((t) => t.taskDailyId)).toEqual([4, 3, 1])
    expect(progressByStaff(list)).toEqual([{ name: '待分配', total: 4, done: 1, rate: 25 }])
    expect(hourlyCompletion(list, [9, 12])).toEqual([{ label: '09:00', rate: 0 }, { label: '12:00', rate: 25 }])
  })
})

describe('task execution drawer', () => {

  it('requires the bed code to mark a task done and a reason when not fully done', () => {
    expect(buildExecution({ ...base, bedQrCode: '' }, task({})).error).toBeTruthy()
    expect(buildExecution({ ...base, result: 'PARTIAL' }, task({})).error).toBeTruthy()
    expect(buildExecution(base, task({}))).toEqual({ execute: { bedQrCode: 'QR1', remark: undefined }, incident: undefined })
  })

  it('reports a delay instead of completing when the task was not done', () => {
    const plan = buildExecution({ ...base, result: 'NOT_DONE', reason: '长者拒绝', bedQrCode: '' }, task({}))
    expect(plan.execute).toBeUndefined()
    expect(plan.incident).toMatchObject({ incidentType: 'DELAY', severity: 'LOW', relatedTaskId: 1, elderId: '7' })
    expect(plan.incident?.description).toContain('长者拒绝')
  })

  it('records the task and links a new incident when an abnormality is chosen', () => {
    expect(buildExecution({ ...base, incidentType: 'SKIN' }, task({})).error).toBeTruthy()
    const plan = buildExecution({ ...base, incidentType: 'SKIN', severity: 'HIGH', observation: '骶尾部发红' }, task({}))
    expect(plan.execute?.remark).toBe('骶尾部发红')
    expect(plan.incident).toMatchObject({ incidentType: 'SKIN', severity: 'HIGH', location: 'A301' })
  })
})

describe('service checklist', () => {
  const items = task({ serviceItems: ['洗脸', '梳头', '口腔清洁'] })

  it('only allows done when every item is ticked, and partial when something is left', () => {
    expect(buildExecution({ ...base, checkedItems: ['洗脸'] }, items).error).toContain('部分完成')
    expect(buildExecution({ ...base, result: 'PARTIAL', reason: '拒绝', checkedItems: ['洗脸', '梳头', '口腔清洁'] }, items).error).toContain('已完成')
    const all = buildExecution({ ...base, checkedItems: ['洗脸', '梳头', '口腔清洁'] }, items)
    expect(all.execute?.remark).toBe('完成：洗脸、梳头、口腔清洁')
  })

  it('writes what was and was not done into the record', () => {
    const plan = buildExecution({ ...base, result: 'PARTIAL', reason: '长者拒绝刷牙', checkedItems: ['洗脸', '梳头'], observation: '精神好' }, items)
    expect(plan.execute?.remark).toBe('【部分完成】长者拒绝刷牙；完成：洗脸、梳头；未完成：口腔清洁；精神好')
  })
})

describe('resident care', () => {
  it('tracks how far the assessment-to-plan flow has gone', () => {
    const done = { id: 1, elderName: 'a', assessmentType: 'CONTINUOUS', assessmentDate: '2026-11-01', status: 'COMPLETED' } as const
    expect(assessmentFlowStep(undefined, [])).toBe(0)
    expect(assessmentFlowStep({ ...done, status: 'DRAFT' }, [])).toBe(1)
    expect(assessmentFlowStep(done, [])).toBe(3)
    expect(assessmentFlowStep(done, [{ approvalStatus: 'PENDING' }])).toBe(4)
    expect(assessmentFlowStep(done, [{ approvalStatus: 'PUBLISHED' }])).toBe(5)
  })
})
