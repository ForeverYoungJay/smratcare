import { describe, expect, it } from 'vitest'
import type { FinanceArrearsAgingItem, FinanceWorkbenchOverview } from '../../types'
import { agingRows, financeTodos } from './workbenchTodos'

describe('finance workbench todos', () => {
  it('lists only open todos, highest risk first', () => {
    const todos = financeTodos({ pending: { pendingRefundCount: '2', pendingDiscountCount: 0, collectionReminderCount: 3 }, reconcile: {} } as unknown as FinanceWorkbenchOverview)
    expect(todos.map((t) => [t.label, t.count])).toEqual([['待审批退款', 2], ['催缴提醒', 3]])
    expect(financeTodos(null)).toEqual([])
  })

  it('scales aging bars to the largest bucket and hides them when nothing is owed', () => {
    const items = [{ key: 'CURRENT', label: '未到期', amount: '200', billCount: 1 }, { key: 'D30', label: '逾期1-30天', amount: 50, billCount: 1 }] as unknown as FinanceArrearsAgingItem[]
    expect(agingRows(items).map((r) => r.percent)).toEqual([100, 25])
    expect(agingRows([{ ...items[0], amount: 0 }])).toEqual([])
  })
})
