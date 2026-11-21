import type { BedItem } from '../types'

type Id = string | number

/** 本次办理自己的线索 / 合同；它们占着的床不算冲突（与后端 CrmReservationRules.bedHeldByOthers 口径一致）。 */
export interface ReservationOwner {
  leadId?: Id | null
  contractId?: Id | null
  contractNo?: string | null
}

const same = (a?: Id | null, b?: Id | null) => a != null && b != null && String(a) === String(b)

export function reservedByOther(bed: Pick<BedItem, 'occupancySource' | 'occupancyRefType' | 'occupancyRefId' | 'occupancyRefNo'>, own: ReservationOwner) {
  if (bed.occupancySource !== 'RESERVATION') return false
  if (bed.occupancyRefType === 'LEAD') return !same(bed.occupancyRefId, own.leadId)
  if (bed.occupancyRefType === 'CONTRACT') {
    return !(same(bed.occupancyRefId, own.contractId) || (Boolean(own.contractNo) && bed.occupancyRefNo === own.contractNo))
  }
  return true
}

export function isBedFree(bed: Pick<BedItem, 'elderId' | 'status'>) {
  return !bed.elderId && (bed.status === 1 || bed.status === undefined)
}

export function bedLabel(bed: Pick<BedItem, 'id' | 'building' | 'floorNo' | 'roomNo' | 'bedNo'>) {
  return [bed.building, bed.floorNo, bed.roomNo, bed.bedNo].filter(Boolean).join(' / ') || String(bed.id)
}

/** 选床下拉项：已入住/停用/维修不可选；被别人预订的显示“已被预订”并禁用。 */
export function bedSelectOption(bed: BedItem, own: ReservationOwner, label = bedLabel(bed)) {
  const held = reservedByOther(bed, own)
  return { value: bed.id, label: held ? `${label}（已被预订）` : label, disabled: !isBedFree(bed) || held }
}

/** 预订房号文字：床号已含房号（如 A-302-A）时不再重复拼接 */
export function reservationRoomText(bed: Pick<BedItem, 'roomNo' | 'bedNo'>) {
  const room = String(bed.roomNo || '').trim()
  const no = String(bed.bedNo || '').trim()
  if (!no) return room
  return !room || no.startsWith(room) ? no : `${room}-${no}`
}
