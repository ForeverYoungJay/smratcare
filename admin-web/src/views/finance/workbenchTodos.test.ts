import { describe, expect, it } from 'vitest'
import type { FinanceWorkbenchOverview } from '../../types'
import { financeTodos } from './workbenchTodos'

describe('finance workbench todos', () => {
  it('lists only open todos, highest risk first', () => {
    const todos = financeTodos({ pending: { pendingRefundCount: '2', pendingDiscountCount: 0, collectionReminderCount: 3 }, reconcile: {} } as unknown as FinanceWorkbenchOverview)
    expect(todos.map((t) => [t.label, t.count])).toEqual([['待审批退款', 2], ['催缴提醒', 3]])
    expect(financeTodos(null)).toEqual([])
  })
})
