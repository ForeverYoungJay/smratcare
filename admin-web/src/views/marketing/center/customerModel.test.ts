import { describe, expect, it } from 'vitest'
import { buildCustomerTimeline, customerTabQuery, maskPhone, resolveCustomerView } from './customerModel'

describe('resolveCustomerView', () => {
  it('defaults to all customers and keeps a passed scenario', () => {
    expect(resolveCustomerView({})).toEqual({ tab: 'all', mode: 'all', scenario: '' })
    expect(resolveCustomerView({ tab: 'invalid', scenario: 'blacklist' })).toEqual({ tab: 'invalid', mode: 'invalid', scenario: 'blacklist' })
    expect(resolveCustomerView({ tab: 'bogus' }).tab).toBe('all')
  })

  it('maps follow-up views and callback types', () => {
    expect(resolveCustomerView({ tab: 'followup', view: 'overdue' })).toMatchObject({ mode: 'callback', scenario: 'follow_overdue', followupView: 'overdue' })
    expect(resolveCustomerView({ tab: 'followup' })).toMatchObject({ followupView: 'due', scenario: 'follow_due' })
    expect(resolveCustomerView({ tab: 'followup', callbackType: 'trial' }).callbackType).toBe('trial')
    expect(resolveCustomerView({ tab: 'followup', callbackType: 'x' }).callbackType).toBeUndefined()
  })
})

describe('customerTabQuery', () => {
  it('keeps filters but drops view params of the previous tab', () => {
    expect(customerTabQuery({ tab: 'followup', view: 'today', scenario: 'follow_today', elderName: '张' }, 'intent'))
      .toEqual({ elderName: '张', tab: 'intent' })
  })
})

describe('maskPhone', () => {
  it('hides the middle digits', () => {
    expect(maskPhone('13812345210')).toBe('138****5210')
    expect(maskPhone('')).toBe('-')
  })
})

describe('buildCustomerTimeline', () => {
  it('merges plans, stage and assign logs newest first', () => {
    const items = buildCustomerTimeline(
      [
        { id: '1', leadId: '9', title: '电话回访', status: 'DONE', planExecuteTime: '2026-11-10 10:00:00', executedTime: '2026-11-10 10:12:00', followupResult: '有意向' },
        { id: '2', leadId: '9', title: '电话回访', status: 'PENDING', planExecuteTime: '2026-11-17 10:00:00' }
      ],
      [{ id: '3', entityType: 'LEAD', fromStatus: '0', toStatus: '1', operatedAt: '2026-11-12 09:00:00', remark: '转意向 | {"a":1}' }],
      [{ id: '4', leadId: '9', toOwnerStaffName: '李经理', assignedAt: '2026-11-08 09:50:00' }]
    )
    expect(items.map((item) => item.key)).toEqual(['plan-2', 'stage-3', 'plan-1', 'assign-4'])
    expect(items[0].title).toBe('待回访：电话回访')
    expect(items[1]).toMatchObject({ title: '阶段：咨询 → 意向', content: '转意向' })
    expect(items[2].title).toBe('电话回访 · 有意向')
  })
})
