import dayjs, { type Dayjs } from 'dayjs'
import type { BedItem } from '../../../types/bed'
import type { CrmLeadItem, CrmVisitAppointmentItem, CrmVisitAppointmentStatus } from '../../../types'
import { bedStatus } from '../../elder/resident360/bedPanoramaModel'

export const VISIT_STATUS: Record<CrmVisitAppointmentStatus, { label: string; tone: string }> = {
  SCHEDULED: { label: '待到访', tone: 'info' },
  ARRIVED: { label: '已到访', tone: 'warning' },
  COMPLETED: { label: '参观完成', tone: 'success' },
  CANCELLED: { label: '已取消', tone: '' },
  NO_SHOW: { label: '未到访', tone: 'danger' }
}

export const EXPIRY_WINDOW_DAYS = 3

/** 周一为一周的第一天 */
export function weekStart(date: Dayjs | string = dayjs()) {
  const d = dayjs(date).startOf('day')
  return d.subtract((d.day() + 6) % 7, 'day')
}

export function weekDays(start: Dayjs) {
  return Array.from({ length: 7 }, (_, i) => start.add(i, 'day'))
}

/** 按 YYYY-MM-DD 分组，组内按时间升序 */
export function groupByDay(items: CrmVisitAppointmentItem[]) {
  const groups: Record<string, CrmVisitAppointmentItem[]> = {}
  for (const item of [...items].sort((a, b) => dayjs(a.appointmentTime).valueOf() - dayjs(b.appointmentTime).valueOf())) {
    const key = dayjs(item.appointmentTime).format('YYYY-MM-DD')
    ;(groups[key] ||= []).push(item)
  }
  return groups
}

/** 床位概览：只读统计，口径与长者管理的床位页一致 */
export function bedSummary(beds: BedItem[]) {
  const summary = { total: beds.length, occupied: 0, reserved: 0, available: 0 }
  for (const bed of beds) {
    const status = bedStatus(bed)
    if (status === 'occupied') summary.occupied += 1
    else if (status === 'reserved' || status === 'locked') summary.reserved += 1
    else if (status === 'idle') summary.available += 1
  }
  return summary
}

/** 可预订房型：按楼栋 + 房型聚合空闲床位，空床多的在前 */
export function availableRoomTypes(beds: BedItem[]) {
  const map = new Map<string, { building: string; roomType: string; count: number }>()
  for (const bed of beds) {
    if (bedStatus(bed) !== 'idle') continue
    const building = bed.building || '未分楼栋'
    const roomType = bed.roomType || '未设房型'
    const key = `${building}|${roomType}`
    const entry = map.get(key) || { building, roomType, count: 0 }
    entry.count += 1
    map.set(key, entry)
  }
  return [...map.values()].sort((a, b) => b.count - a.count)
}

export interface ReservationReminder {
  lead: CrmLeadItem
  bedLabel: string
  deadline: string
  daysLeft: number
}

/**
 * 预订到期提醒：截止日优先取床位锁定到期时间（床位服务为权威源），
 * 没有锁床时退回线索的下次跟进日（与原「预定到期提醒」页的口径一致）。
 */
export function reservationReminders(leads: CrmLeadItem[], beds: BedItem[], today: Dayjs = dayjs()) {
  const bedById = new Map(beds.map((bed) => [String(bed.id), bed]))
  const base = today.startOf('day')
  return leads.flatMap((lead): ReservationReminder[] => {
    const bed = lead.reservationBedId != null ? bedById.get(String(lead.reservationBedId)) : undefined
    const deadline = bed?.lockExpiresAt || (String(lead.reservationStatus || '').includes('锁') ? lead.nextFollowDate : undefined)
    if (!deadline || lead.refunded === 1) return []
    const bedLabel = bed ? [bed.building, bed.roomNo, bed.bedNo].filter(Boolean).join('-') : (lead.reservationRoomNo || '')
    return [{ lead, bedLabel, deadline: dayjs(deadline).format('YYYY-MM-DD'), daysLeft: dayjs(deadline).startOf('day').diff(base, 'day') }]
  }).sort((a, b) => a.daysLeft - b.daysLeft)
}

export function isExpiringSoon(reminder: ReservationReminder) {
  return reminder.daysLeft >= 0 && reminder.daysLeft <= EXPIRY_WINDOW_DAYS
}

/** 参观记录的下一步动作提示 */
export function visitNextStep(item: CrmVisitAppointmentItem, now: Dayjs = dayjs()) {
  if (item.status === 'SCHEDULED') return dayjs(item.appointmentTime).isBefore(now) ? '已过预约时间，确认是否到访' : '电话确认到访人数'
  if (item.status === 'ARRIVED') return '填写参观结果与意向变化'
  if (item.status === 'COMPLETED') return item.nextFollowDate ? `${item.nextFollowDate} 回访` : '安排参观后回访'
  if (item.status === 'NO_SHOW') return '联系客户重新预约'
  return item.cancelReason ? `已取消：${item.cancelReason}` : '已取消'
}
