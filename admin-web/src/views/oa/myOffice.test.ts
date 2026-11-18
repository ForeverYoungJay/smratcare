import { describe, expect, it } from 'vitest'
import dayjs from 'dayjs'
import { defaultReportPeriod, draftFromStats, passwordProblem } from './myOffice'

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
})
