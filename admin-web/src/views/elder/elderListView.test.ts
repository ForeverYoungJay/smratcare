import { describe, expect, it } from 'vitest'
import { avatarText, canWriteElder, ELDER_STATUS_TABS, statusPillClass } from './elderListView'

describe('elderListView', () => {
  it('maps lifecycle status to pill class', () => {
    expect(statusPillClass('IN_HOSPITAL')).toBe('rm-pill--success')
    expect(statusPillClass('intent')).toBe('rm-pill--warning')
    expect(statusPillClass('DISCHARGE_PENDING')).toBe('rm-pill--danger')
    expect(statusPillClass('DISCHARGED')).toBe('')
    expect(statusPillClass(undefined)).toBe('')
  })

  it('only lets elder-write roles edit', () => {
    expect(canWriteElder(['nursing_employee'])).toBe(true)
    expect(canWriteElder(['ADMIN'])).toBe(true)
    expect(canWriteElder(['FINANCE_EMPLOYEE'])).toBe(false)
    expect(canWriteElder(undefined)).toBe(false)
  })

  it('tabs start with 全部 and use backend status codes', () => {
    expect(ELDER_STATUS_TABS[0]).toEqual({ value: undefined, label: '全部' })
    expect(ELDER_STATUS_TABS.map((t) => t.value)).toContain('DISCHARGE_PENDING')
  })

  it('avatar uses first char', () => {
    expect(avatarText(' 王秀兰')).toBe('王')
    expect(avatarText('')).toBe('?')
  })
})
