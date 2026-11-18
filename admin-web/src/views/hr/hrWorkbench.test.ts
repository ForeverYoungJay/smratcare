import { describe, expect, it } from 'vitest'
import { hrAlerts, hrKpis } from './hrWorkbench'

const counts = { onJob: 236, recruiting: 4, contractsExpiring: 9, certificatesExpiring: 0, attendanceAbnormal: 3, leavePending: 6, expensePending: 0 }

describe('HR workbench', () => {
  it('builds six KPI cards that link to their source pages', () => {
    const kpis = hrKpis(counts)
    expect(kpis).toHaveLength(6)
    expect(kpis.find((k) => k.key === 'attendance')).toMatchObject({ value: 3, hint: '请假待审批 6 条', path: '/hr/attendance/abnormal' })
  })

  it('lists only alerts with pending items, most urgent first', () => {
    expect(hrAlerts(counts).map((a) => a.label)).toEqual(['考勤异常待核验', '合同即将到期', '请假待审批'])
  })
})
