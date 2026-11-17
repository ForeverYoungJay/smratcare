import type { LocationQuery } from 'vue-router'
import type { BedItem, CareTaskItem, ChangeLogItem, MedicalOutingItem, OutingItem } from '../../../types'

export type BedStatusKey = 'occupied' | 'idle' | 'reserved' | 'cleaning' | 'maintenance' | 'locked'

export const BED_STATUS_META: Record<BedStatusKey, { label: string; tagColor: string }> = {
  occupied: { label: '已入住', tagColor: 'green' },
  idle: { label: '空床', tagColor: 'default' },
  reserved: { label: '预留', tagColor: 'orange' },
  cleaning: { label: '清洁中', tagColor: 'blue' },
  maintenance: { label: '维修中', tagColor: 'red' },
  locked: { label: '锁定', tagColor: 'purple' }
}

export type PanoramaRoom = {
  key: string
  roomNo: string
  roomType: string
  beds: BedItem[]
  occupied: number
}

/** 床号本身常已带房间号（如 B-205-A），避免拼成 B-205-B-205-A */
export function bedLabel(bed: BedItem) {
  const roomNo = String(bed.roomNo || '')
  const bedNo = String(bed.bedNo || '')
  return !roomNo || bedNo.startsWith(roomNo) ? bedNo : `${roomNo}-${bedNo}`
}

export function bedStatus(bed: BedItem): BedStatusKey {
  if (bed.elderId) return 'occupied'
  if (bed.status === 0) return 'locked'
  if (bed.occupancySource === 'CLEANING') return 'cleaning'
  if (bed.status === 3 || bed.occupancySource === 'MAINTENANCE') return 'maintenance'
  if (bed.occupancySource === 'RESERVATION') return 'reserved'
  return 'idle'
}

/** 在住长者的临时去向，空字符串表示在床 */
export function residentAwayLabel(bed: BedItem) {
  const status = String(bed.elderLifecycleStatus || '').toUpperCase()
  if (status === 'OUTING' || status === 'MEDICAL_OUTING') return '外出'
  if (status === 'DISCHARGE_PENDING') return '待退住'
  return ''
}

export function bedRiskTags(bed: BedItem) {
  const tags: string[] = []
  if (bed.riskLevel === 'HIGH') tags.push(bed.riskLabel || '高风险')
  else if (bed.riskLevel === 'MEDIUM' && bed.riskLabel) tags.push(bed.riskLabel)
  if (Number(bed.abnormalVital24hCount || 0) > 0) tags.push('体征异常')
  return tags
}

const ROOM_TYPE_LABELS: Record<string, string> = {
  '1': '单人间', '2': '双人间', '3': '三人间', SINGLE: '单人间', DOUBLE: '双人间', TRIPLE: '三人间',
  STANDARD: '标准间', DELUXE: '豪华间', VIP: 'VIP房', SUITE: '套间'
}

export function roomTypeLabel(roomType?: string) {
  const raw = String(roomType || '').trim()
  return ROOM_TYPE_LABELS[raw.toUpperCase()] || raw || '标准间'
}

const CN_DIGITS: Record<string, number> = { 一: 1, 二: 2, 两: 2, 三: 3, 四: 4, 五: 5, 六: 6, 七: 7, 八: 8, 九: 9 }

function floorNumber(token: string) {
  if (/^\d+$/.test(token)) return Number(token)
  if (token.startsWith('十')) return 10 + (CN_DIGITS[token[1]] || 0)
  if (token.includes('十')) return (CN_DIGITS[token[0]] || 0) * 10 + (CN_DIGITS[token[2]] || 0)
  return CN_DIGITS[token] || 0
}

/** 楼层排序值：B1/地下一层为负数，未识别的排最后 */
export function floorSortValue(text: string) {
  const normalized = String(text || '').replace(/\s+/g, '').toUpperCase()
  const basement = normalized.match(/(?:地下|负|B)(\d+|[一二两三四五六七八九十]+)/)
  if (basement) return -floorNumber(basement[1])
  const match = normalized.match(/(\d+|[一二两三四五六七八九十]+)/)
  return match ? floorNumber(match[1]) : -999
}

export function groupRooms(beds: BedItem[], roomTypes: Record<string, string>): PanoramaRoom[] {
  const rooms = new Map<string, PanoramaRoom>()
  beds.forEach((bed) => {
    const roomNo = String(bed.roomNo || `房间-${bed.roomId}`)
    let room = rooms.get(roomNo)
    if (!room) {
      room = { key: roomNo, roomNo, roomType: roomTypeLabel(roomTypes[String(bed.roomId)] || bed.roomType), beds: [], occupied: 0 }
      rooms.set(roomNo, room)
    }
    room.beds.push(bed)
    if (bed.elderId) room.occupied += 1
  })
  const list = Array.from(rooms.values())
  list.forEach((room) => room.beds.sort((a, b) => String(a.bedNo).localeCompare(String(b.bedNo), 'zh-CN', { numeric: true })))
  return list.sort((a, b) => a.roomNo.localeCompare(b.roomNo, 'zh-CN', { numeric: true }))
}

/** 周岁；生日缺失或格式不对时返回 null */
export function ageFromBirthDate(birthDate: string | undefined, today: Date) {
  const match = String(birthDate || '').match(/^(\d{4})-(\d{2})-(\d{2})/)
  if (!match) return null
  const [year, month, day] = match.slice(1).map(Number)
  const birthdayPassed = today.getMonth() + 1 > month || (today.getMonth() + 1 === month && today.getDate() >= day)
  const age = today.getFullYear() - year - (birthdayPassed ? 0 : 1)
  return age >= 0 && age < 130 ? age : null
}

/** BED_CHANGE 日志里入住分床（无原床）和退住释放（无新床）都不算转床 */
export function countBedTransfers(logs: ChangeLogItem[]) {
  return logs.filter((log) => log.changeType === 'BED_CHANGE' && log.beforeValue && log.afterValue).length
}

/** 每位长者今天任务最多的护理员 */
export function primaryCaregivers(tasks: CareTaskItem[]) {
  const tally = new Map<string, Map<string, number>>()
  tasks.forEach((task) => {
    if (!task.staffName || task.elderId == null) return
    const elderKey = String(task.elderId)
    const byStaff = tally.get(elderKey) ?? new Map<string, number>()
    byStaff.set(task.staffName, (byStaff.get(task.staffName) ?? 0) + 1)
    tally.set(elderKey, byStaff)
  })
  const result: Record<string, string> = {}
  tally.forEach((byStaff, elderKey) => {
    result[elderKey] = [...byStaff.entries()].sort((a, b) => b[1] - a[1])[0][0]
  })
  return result
}

/** 优先后端返回的责任护理员，没有时退回今日护理任务最多的护理员 */
export function bedCaregiver(bed: BedItem & { responsibleStaffName?: string }, caregivers: Record<string, string>) {
  if (bed.responsibleStaffName) return { label: '责任护理员', name: bed.responsibleStaffName }
  const name = bed.elderId == null ? '' : caregivers[String(bed.elderId)]
  return name ? { label: '今日护理员', name } : null
}

const ORIENTATION_LABELS: Record<string, string> = { SOUTH: '朝南', NORTH: '朝北', EAST: '朝东', WEST: '朝西' }

export function orientationLabel(value?: string) {
  const raw = String(value || '').trim()
  return ORIENTATION_LABELS[raw.toUpperCase()] || raw
}

export type AwayRecord = {
  key: string
  kind: 'outing' | 'medical'
  id: OutingItem['id']
  elderId: OutingItem['elderId']
  elderName: string
  outingDate: string
  expectedReturnTime?: string
  actualReturnTime?: string
  place: string
  reason: string
  diagnosis: string
  leaveNoteImageUrl?: string
  status: string
  statusLabel: string
  canReturn: boolean
  canApprove: boolean
}

const AWAY_STATUS_LABELS: Record<string, string> = { APPLIED: '待审批', OUT: '外出中', RETURNED: '已返院', CANCELLED: '已取消' }

/** 外出请假与外出就医（住院）合成一张记录表，按外出日期倒序；只有 OUT 状态可办理返院 */
export function mergeAwayRecords(outings: OutingItem[], medicals: MedicalOutingItem[]): AwayRecord[] {
  const rows: AwayRecord[] = [
    ...outings.map((item) => ({ item, kind: 'outing' as const, place: item.companion ? `陪同：${item.companion}` : '' })),
    ...medicals.map((item) => ({ item, kind: 'medical' as const, place: [item.hospitalName, item.department].filter(Boolean).join(' / ') }))
  ].map(({ item, kind, place }) => {
    const status = String(item.status || '')
    return {
      key: `${kind}-${item.id}`,
      kind,
      id: item.id,
      elderId: item.elderId,
      elderName: item.elderName || '--',
      outingDate: item.outingDate,
      expectedReturnTime: item.expectedReturnTime,
      actualReturnTime: item.actualReturnTime,
      place,
      reason: item.reason || ('diagnosis' in item ? item.diagnosis || '' : ''),
      diagnosis: 'diagnosis' in item ? item.diagnosis || '' : '',
      leaveNoteImageUrl: 'leaveNoteImageUrl' in item ? item.leaveNoteImageUrl : undefined,
      status,
      statusLabel: kind === 'medical' && status === 'OUT' ? '住院中' : AWAY_STATUS_LABELS[status] || status || '--',
      canReturn: status === 'OUT',
      canApprove: kind === 'outing' && status === 'APPLIED'
    }
  })
  return rows.sort((a, b) => String(b.outingDate).localeCompare(String(a.outingDate)))
}

/** 外出中且已过预计返院时间 */
export function isAwayOverdue(record: AwayRecord, now: Date) {
  return record.status === 'OUT' && !!record.expectedReturnTime && new Date(record.expectedReturnTime).getTime() < now.getTime()
}

/** 关键字（长者 / 医院 / 科室 / 诊断 / 原因）与外出日期区间（含端点，YYYY-MM-DD）在前端过滤 */
export function filterAwayRecords(rows: AwayRecord[], filter: { keyword?: string; from?: string; to?: string }) {
  const text = String(filter.keyword || '').trim().toLowerCase()
  return rows.filter((row) => {
    const day = String(row.outingDate || '').slice(0, 10)
    if (filter.from && day < filter.from) return false
    if (filter.to && day > filter.to) return false
    return !text || [row.elderName, row.place, row.reason, row.diagnosis].join(' ').toLowerCase().includes(text)
  })
}

export type AwayRecordType = 'ALL' | 'OUTING' | 'MEDICAL_OUTING'
export type AwayStatusFilter = 'ALL' | 'APPLIED' | 'OUT' | 'RETURNED' | 'CANCELLED'

function queryText(value: LocationQuery[string]) {
  const raw = Array.isArray(value) ? value[0] : value
  return raw == null ? '' : String(raw).trim()
}

/** 旧外出 / 外出就医 / 变更总览页重定向过来的参数，见 router/residentRedirects.ts */
export function parsePanoramaQuery(query: LocationQuery) {
  const recordType = queryText(query.recordType)
  const status = queryText(query.status)
  const action = queryText(query.action)
  return {
    view: queryText(query.view) === 'records' ? 'records' as const : 'grid' as const,
    recordType: (['OUTING', 'MEDICAL_OUTING'].includes(recordType) ? recordType : 'ALL') as AwayRecordType,
    status: (['APPLIED', 'OUT', 'RETURNED', 'CANCELLED'].includes(status) ? status : '') as AwayStatusFilter | '',
    elderId: queryText(query.elderId),
    action: (['outing', 'medical'].includes(action) ? action : '') as 'outing' | 'medical' | ''
  }
}
