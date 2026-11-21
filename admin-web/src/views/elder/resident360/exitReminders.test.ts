import dayjs from 'dayjs'
import { describe, expect, it } from 'vitest'
import type { CrmContractItem, DischargeApplyItem } from '../../../types'
import { contractExpiry, contractExpiryReminderTo, countDischargeTimeouts, dischargeTimeout } from './exitReminders'

const now = dayjs('2026-11-16T10:00:00')
const apply: DischargeApplyItem = { id: '1', elderId: '9', plannedDischargeDate: '2026-11-20', reason: '回家', status: 'PENDING' }

describe('dischargeTimeout', () => {
  it('flags pending applies created more than 3 days ago', () => {
    expect(dischargeTimeout({ ...apply, createTime: '2026-11-13T09:59:00' }, now)).toBe('REVIEW_TIMEOUT')
    expect(dischargeTimeout({ ...apply, createTime: '2026-11-13T10:01:00' }, now)).toBeNull()
    expect(dischargeTimeout({ ...apply, applyDate: '2026-11-10' }, now)).toBe('REVIEW_TIMEOUT')
    expect(dischargeTimeout(apply, now)).toBeNull()
  })

  it('flags approved, unarchived applies past the planned date', () => {
    const approved = { ...apply, status: 'APPROVED' as const, plannedDischargeDate: '2026-11-15' }
    expect(dischargeTimeout(approved, now)).toBe('HANDLING_TIMEOUT')
    expect(dischargeTimeout({ ...approved, plannedDischargeDate: '2026-11-16' }, now)).toBeNull()
    expect(dischargeTimeout({ ...approved, archiveTime: '2026-11-16T09:00:00' }, now)).toBeNull()
    expect(dischargeTimeout({ ...approved, status: 'REJECTED' }, now)).toBeNull()
  })

  it('counts both kinds', () => {
    expect(countDischargeTimeouts([
      { ...apply, createTime: '2026-11-01T00:00:00' },
      { ...apply, status: 'APPROVED', plannedDischargeDate: '2026-11-01' },
      apply
    ], now)).toEqual({ REVIEW_TIMEOUT: 1, HANDLING_TIMEOUT: 1 })
  })
})

describe('contractExpiry', () => {
  const contract: CrmContractItem = { id: 'c1', elderId: '9', status: 'SIGNED', contractExpiryDate: '2026-12-16' }

  it('marks active contracts ending within 30 days as expiring', () => {
    expect(contractExpiry(contract, [], now)).toBe('EXPIRING')
    expect(contractExpiry({ ...contract, contractExpiryDate: '2026-11-16' }, [], now)).toBe('EXPIRING')
    expect(contractExpiry({ ...contract, contractExpiryDate: '2026-12-17' }, [], now)).toBeNull()
  })

  it('marks active contracts ended before today as expired unless renewed', () => {
    const ended = { ...contract, status: 'EFFECTIVE' as const, contractExpiryDate: '2026-11-15' }
    expect(contractExpiry(ended, [], now)).toBe('EXPIRED')
    expect(contractExpiry(ended, [{ id: 'c2', elderId: '9', status: 'SIGNED', contractExpiryDate: '2027-11-15' }], now)).toBeNull()
    expect(contractExpiry(ended, [{ id: 'c2', elderId: '9', status: 'VOID', contractExpiryDate: '2027-11-15' }], now)).toBe('EXPIRED')
    expect(contractExpiry({ ...ended, status: 'VOID' }, [], now)).toBeNull()
    expect(contractExpiry({ ...contract, contractExpiryDate: undefined }, [], now)).toBeNull()
  })

  it('exposes the backend query bound', () => {
    expect(contractExpiryReminderTo(now)).toBe('2026-12-16')
  })
})
