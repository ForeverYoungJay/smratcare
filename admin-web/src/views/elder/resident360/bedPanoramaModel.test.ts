import { describe, expect, it } from 'vitest'
import type { BedItem, CareTaskItem, ChangeLogItem, MedicalOutingItem, OutingItem } from '../../../types'
import { ageFromBirthDate, bedCaregiver, bedLabel, bedRiskTags, bedStatus, countBedTransfers, filterAwayRecords, floorSortValue, groupRooms, isAwayOverdue, mergeAwayRecords, parsePanoramaQuery, primaryCaregivers } from './bedPanoramaModel'

const bed = (patch: Partial<BedItem>): BedItem => ({ id: '1', bedNo: '1', roomId: 'r1', ...patch })

describe('bedStatus', () => {
  it.each([
    [{ elderId: 'e1', status: 3 }, 'occupied'],
    [{ status: 0 }, 'locked'],
    [{ status: 1, occupancySource: 'CLEANING' }, 'cleaning'],
    [{ status: 3 }, 'maintenance'],
    [{ status: 1, occupancySource: 'RESERVATION' }, 'reserved'],
    [{ status: 1 }, 'idle']
  ] as Array<[Partial<BedItem>, string]>)('%o -> %s', (patch, expected) => {
    expect(bedStatus(bed(patch))).toBe(expected)
  })
})

describe('floorSortValue', () => {
  it('orders basement, numeric and chinese floors', () => {
    const floors = ['3层', 'B1', '十二楼', '1F', '二层']
    expect([...floors].sort((a, b) => floorSortValue(a) - floorSortValue(b))).toEqual(['B1', '1F', '二层', '3层', '十二楼'])
  })
})

describe('groupRooms', () => {
  it('groups beds by room with natural sort and occupancy count', () => {
    const rooms = groupRooms([
      bed({ id: 'a', roomNo: '310', bedNo: '2', roomId: 'r10' }),
      bed({ id: 'b', roomNo: '302', bedNo: '1', roomId: 'r2', elderId: 'e1' }),
      bed({ id: 'c', roomNo: '310', bedNo: '1', roomId: 'r10', elderId: 'e2' })
    ], { r10: 'DOUBLE' })
    expect(rooms.map((room) => room.roomNo)).toEqual(['302', '310'])
    expect(rooms[1]).toMatchObject({ roomType: '双人间', occupied: 1 })
    expect(rooms[1].beds.map((item) => item.bedNo)).toEqual(['1', '2'])
  })
})

describe('bedRiskTags', () => {
  it('shows high risk and abnormal vitals', () => {
    expect(bedRiskTags(bed({ riskLevel: 'HIGH', riskLabel: '跌倒风险', abnormalVital24hCount: 2 }))).toEqual(['跌倒风险', '体征异常'])
    expect(bedRiskTags(bed({ riskLevel: 'LOW', riskLabel: '低' }))).toEqual([])
  })
})

describe('bedLabel', () => {
  it('does not repeat the room number', () => {
    expect(bedLabel(bed({ roomNo: 'B-205', bedNo: 'B-205-A' }))).toBe('B-205-A')
    expect(bedLabel(bed({ roomNo: '301', bedNo: '1' }))).toBe('301-1')
  })
})

describe('ageFromBirthDate', () => {
  const today = new Date(2026, 10, 16)
  it('counts full years around the birthday', () => {
    expect(ageFromBirthDate('1940-11-16', today)).toBe(86)
    expect(ageFromBirthDate('1940-11-17', today)).toBe(85)
    expect(ageFromBirthDate(undefined, today)).toBeNull()
    expect(ageFromBirthDate('bad', today)).toBeNull()
  })
})

describe('countBedTransfers', () => {
  it('ignores admission assignment and discharge release', () => {
    const log = (beforeValue?: string, afterValue?: string): ChangeLogItem => ({ id: '1', elderId: '1', changeType: 'BED_CHANGE', beforeValue, afterValue })
    expect(countBedTransfers([log('301-1', '302-2'), log(undefined, '301-1'), log('301-1', undefined)])).toBe(1)
  })
})

describe('primaryCaregivers', () => {
  it('picks the caregiver with most tasks per elder', () => {
    const task = (elderId: string, staffName?: string) => ({ elderId, staffName }) as CareTaskItem
    expect(primaryCaregivers([task('e1', '王静'), task('e1', '李娜'), task('e1', '王静'), task('e2'), task('e2', '李娜')]))
      .toEqual({ e1: '王静', e2: '李娜' })
  })
})

describe('bedCaregiver', () => {
  it('prefers responsible staff, falls back to today caregiver', () => {
    expect(bedCaregiver({ ...bed({ elderId: 'e1' }), responsibleStaffName: '王护' }, { e1: '李护' })).toEqual({ label: '责任护理员', name: '王护' })
    expect(bedCaregiver(bed({ elderId: 'e1' }), { e1: '李护' })).toEqual({ label: '今日护理员', name: '李护' })
    expect(bedCaregiver(bed({}), { e1: '李护' })).toBeNull()
  })
})

describe('mergeAwayRecords', () => {
  it('merges outing and medical records newest first, only OUT can return', () => {
    const rows = mergeAwayRecords(
      [{ id: '1', elderId: 'e1', elderName: '张三', outingDate: '2026-11-10', status: 'RETURNED', companion: '儿子' } as OutingItem],
      [{ id: '1', elderId: 'e2', elderName: '李四', outingDate: '2026-11-12', status: 'OUT', hospitalName: '市一院', department: '心内科' } as MedicalOutingItem]
    )
    expect(rows.map((row) => row.key)).toEqual(['medical-1', 'outing-1'])
    expect(rows[0]).toMatchObject({ statusLabel: '住院中', place: '市一院 / 心内科', canReturn: true })
    expect(rows[1]).toMatchObject({ statusLabel: '已返院', place: '陪同：儿子', canReturn: false })
  })
})

describe('away record helpers', () => {
  const rows = mergeAwayRecords(
    [
      { id: '1', elderId: 'e1', elderName: '张三', outingDate: '2026-11-10', status: 'APPLIED', expectedReturnTime: '2026-11-11T10:00:00' } as OutingItem,
      { id: '2', elderId: 'e1', elderName: '张三', outingDate: '2026-11-14', status: 'OUT', expectedReturnTime: '2026-11-15T10:00:00' } as OutingItem
    ],
    [{ id: '3', elderId: 'e2', elderName: '李四', outingDate: '2026-11-12', status: 'OUT', hospitalName: '市一院', diagnosis: '肺炎' } as MedicalOutingItem]
  )
  it('only APPLIED outings can be approved', () => {
    expect(rows.filter((row) => row.canApprove).map((row) => row.key)).toEqual(['outing-1'])
  })
  it('flags OUT records past expected return as overdue', () => {
    const now = new Date('2026-11-16T00:00:00')
    expect(rows.filter((row) => isAwayOverdue(row, now)).map((row) => row.key)).toEqual(['outing-2'])
  })
  it('filters by keyword including diagnosis and by inclusive date range', () => {
    expect(filterAwayRecords(rows, { keyword: '肺炎' }).map((row) => row.key)).toEqual(['medical-3'])
    expect(filterAwayRecords(rows, { from: '2026-11-12', to: '2026-11-14' }).map((row) => row.key)).toEqual(['outing-2', 'medical-3'])
  })
})

describe('parsePanoramaQuery', () => {
  it('reads legacy redirect params and ignores junk', () => {
    expect(parsePanoramaQuery({ view: 'records', recordType: 'MEDICAL_OUTING', elderId: '9', action: 'medical' }))
      .toEqual({ view: 'records', recordType: 'MEDICAL_OUTING', status: '', elderId: '9', action: 'medical' })
    expect(parsePanoramaQuery({ view: 'x', recordType: 'X', status: 'BAD', action: 'delete' }))
      .toEqual({ view: 'grid', recordType: 'ALL', status: '', elderId: '', action: '' })
  })
})
