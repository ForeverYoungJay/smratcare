import { describe, expect, it } from 'vitest'
import type { BedItem } from '../types'
import { bedSelectOption, reservationRoomText } from './bedOptions'

const bed = (extra: Partial<BedItem> = {}) => ({ id: 5, bedNo: '01', roomId: 1, roomNo: 'A-101', status: 1, ...extra }) as BedItem

describe('bedSelectOption', () => {
  it('lets a free bed or my own reservation be chosen', () => {
    expect(bedSelectOption(bed(), {})).toEqual({ value: 5, label: 'A-101 / 01', disabled: false })
    expect(bedSelectOption(bed({ occupancySource: 'RESERVATION', occupancyRefType: 'LEAD', occupancyRefId: 9 }), { leadId: '9' }).disabled).toBe(false)
    expect(bedSelectOption(bed({ occupancySource: 'RESERVATION', occupancyRefType: 'CONTRACT', occupancyRefNo: 'HT-1' }), { contractNo: 'HT-1' }).disabled).toBe(false)
  })

  it('blocks beds reserved by someone else, occupied or under maintenance', () => {
    const reserved = bedSelectOption(bed({ occupancySource: 'RESERVATION', occupancyRefType: 'LEAD', occupancyRefId: 8 }), { leadId: 9 })
    expect(reserved).toEqual({ value: 5, label: 'A-101 / 01（已被预订）', disabled: true })
    expect(bedSelectOption(bed({ elderId: 3, status: 2 }), {}).disabled).toBe(true)
    expect(bedSelectOption(bed({ status: 3 }), {}).disabled).toBe(true)
  })
})

describe('reservationRoomText', () => {
  it('does not repeat the room number already in the bed number', () => {
    expect(reservationRoomText({ roomNo: 'A-302', bedNo: 'A-302-A' })).toBe('A-302-A')
    expect(reservationRoomText({ roomNo: 'A-302', bedNo: '01' })).toBe('A-302-01')
  })
})
