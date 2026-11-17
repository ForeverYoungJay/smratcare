import dayjs from 'dayjs'
import { describe, expect, it } from 'vitest'
import type { BedItem } from '../../../types/bed'
import type { CrmLeadItem, CrmVisitAppointmentItem } from '../../../types'
import { availableRoomTypes, bedSummary, groupByDay, isExpiringSoon, reservationReminders, visitNextStep, weekDays, weekStart } from './visitModel'

const bed = (over: Partial<BedItem>): BedItem => ({ id: 1, bedNo: '1', roomId: 1, ...over })
const visit = (over: Partial<CrmVisitAppointmentItem>): CrmVisitAppointmentItem => ({
  id: 1, leadId: 1, appointmentTime: '2026-11-18 10:00:00', visitorName: '王', partySize: 1, status: 'SCHEDULED', ...over
})

describe('visitModel', () => {
  it('starts weeks on Monday, including when today is Sunday', () => {
    expect(weekStart('2026-11-16').format('YYYY-MM-DD')).toBe('2026-11-16')
    expect(weekStart('2026-11-22').format('YYYY-MM-DD')).toBe('2026-11-16')
    expect(weekDays(weekStart('2026-11-18')).map((d) => d.date())).toEqual([16, 17, 18, 19, 20, 21, 22])
  })

  it('groups appointments by day in time order', () => {
    const groups = groupByDay([visit({ id: 2, appointmentTime: '2026-11-18 15:00:00' }), visit({ id: 1 }), visit({ id: 3, appointmentTime: '2026-11-19 09:30:00' })])
    expect(groups['2026-11-18'].map((v) => v.id)).toEqual([1, 2])
    expect(groups['2026-11-19']).toHaveLength(1)
  })

  it('counts beds and aggregates idle room types', () => {
    const beds = [
      bed({ id: 1, elderId: 9 }),
      bed({ id: 2, occupancySource: 'RESERVATION', status: 1 }),
      bed({ id: 3, status: 1, building: 'A栋', roomType: '双人间' }),
      bed({ id: 4, status: 1, building: 'A栋', roomType: '双人间' }),
      bed({ id: 5, status: 3 })
    ]
    expect(bedSummary(beds)).toEqual({ total: 5, occupied: 1, reserved: 1, available: 2 })
    expect(availableRoomTypes(beds)).toEqual([{ building: 'A栋', roomType: '双人间', count: 2 }])
  })

  it('uses the bed lock deadline first and skips refunded reservations', () => {
    const today = dayjs('2026-11-16')
    const leads = [
      { id: 1, name: '黄', reservationBedId: 7, nextFollowDate: '2026-12-30' },
      { id: 2, name: '陈', reservationStatus: '锁床', nextFollowDate: '2026-11-19' },
      { id: 3, name: '退', reservationStatus: '锁床', nextFollowDate: '2026-11-17', refunded: 1 },
      { id: 4, name: '无', reservationStatus: '预定' }
    ] as CrmLeadItem[]
    const reminders = reservationReminders(leads, [bed({ id: 7, building: 'A栋', roomNo: '301', bedNo: '1', lockExpiresAt: '2026-11-18 12:00:00' })], today)
    expect(reminders.map((r) => [r.lead.id, r.daysLeft, r.bedLabel])).toEqual([[1, 2, 'A栋-301-1'], [2, 3, '']])
    expect(reminders.every(isExpiringSoon)).toBe(true)
  })

  it('suggests the next action per visit status', () => {
    const now = dayjs('2026-11-18 11:00:00')
    expect(visitNextStep(visit({}), now)).toContain('已过预约时间')
    expect(visitNextStep(visit({ status: 'COMPLETED', nextFollowDate: '2026-11-20' }), now)).toBe('2026-11-20 回访')
  })
})
