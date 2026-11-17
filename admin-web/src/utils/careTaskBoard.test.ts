import { describe, expect, it } from 'vitest'
import type { CareTaskItem } from '../types'
import { groupCareTasks } from './careTaskBoard'

const task = (id: number, time: string, props: Partial<CareTaskItem> = {}): CareTaskItem => ({
  taskDailyId: id, elderId: '9007199254740993', elderName: '张阿姨', bedId: '1', roomNo: '101',
  taskName: '照护', planTime: time, status: 'PENDING', ...props
})
describe('groupCareTasks', () => {
  it('keeps six-hour boundaries and unscheduled tasks in chronological groups', () => {
    const tasks = [task(1, '2026-11-16 12:00'), task(2, ''), task(3, '2026-11-16 05:59'), task(4, '2026-11-16 06:00')]
    const groups = groupCareTasks(tasks, 'time')
    expect(groups.map(g => g.key)).toEqual(['0', '1', '2', '4'])
    expect(groups.flatMap(g => g.tasks).map(t => t.taskDailyId)).toEqual([3, 4, 1, 2])
    expect(tasks[0].taskDailyId).toBe(1)
  })
  it('separates same-name residents by string ID and counts a risky task once', () => {
    const groups = groupCareTasks([
      task(1, '2026-11-16 08:00', { overdueFlag: true, suspiciousFlag: true }),
      task(2, '2026-11-16 09:00', { elderId: '9007199254740992', status: 'CANCELLED' })
    ], 'resident')
    expect(groups).toHaveLength(2)
    expect(groups.find(g => g.key === '9007199254740993')).toMatchObject({ pending: 1, risk: 1 })
    expect(groups.find(g => g.key === '9007199254740992')?.pending).toBe(0)
  })
})
