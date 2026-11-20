import { describe, expect, it } from 'vitest'
import dayjs from 'dayjs'
import { defaultReportPeriod, draftFromStats, monthGrid, overtimeHours, passwordProblem, workdaysSoFar } from './myOffice'

describe('my office helpers', () => {
  it('uses Monday-to-Sunday weeks and calendar months for report periods', () => {
    const wednesday = dayjs('2026-11-18')
    expect(defaultReportPeriod('WEEK', wednesday)).toEqual(['2026-11-16', '2026-11-22'])
    expect(defaultReportPeriod('WEEK', dayjs('2026-11-22'))).toEqual(['2026-11-16', '2026-11-22'])
    expect(defaultReportPeriod('MONTH', wednesday)).toEqual(['2026-11-01', '2026-11-30'])
    expect(defaultReportPeriod('DAY', wednesday)).toEqual(['2026-11-18', '2026-11-18'])
  })

  it('turns my own work stats into an editable draft', () => {
    expect(draftFromStats({ tasksDone: '3', todosDone: 5, attendanceDays: '4' }, '2026-11-16', '2026-11-22'))
      .toContain('完成协同任务 3 项')
  })

  it('mirrors the server password rule', () => {
    expect(passwordProblem('abc123')).toContain('8 位')
    expect(passwordProblem('abcdefgh')).toContain('字母和数字')
    expect(passwordProblem('Guifeng2026')).toBe('')
  })

  it('builds a Monday-first month grid with records', () => {
    const grid = monthGrid('2026-11', [{ date: '2026-11-03', status: 'NORMAL' }])
    expect(grid.slice(0, 6)).toEqual([null, null, null, null, null, null]) // 2026-11-01 是周日
    expect(grid[6]).toMatchObject({ date: '2026-11-01', day: 1 })
    expect(grid.find((c) => c?.date === '2026-11-03')?.record).toEqual({ date: '2026-11-03', status: 'NORMAL' })
    expect(grid.filter(Boolean)).toHaveLength(30)
  })

  it('counts weekdays so far and approved overtime hours', () => {
    expect(workdaysSoFar('2026-11', dayjs('2026-11-19'))).toBe(14)
    expect(workdaysSoFar('2026-10', dayjs('2026-11-19'))).toBe(22)
    expect(overtimeHours([{ startTime: '2026-11-03 18:00', endTime: '2026-11-03 20:30' }, { startTime: '2026-11-04 18:00' }])).toBe(2.5)
  })
})
