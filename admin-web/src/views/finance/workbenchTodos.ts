import type { FinanceArrearsAgingItem, FinanceWorkbenchOverview } from '../../types'

export interface FinanceTodo {
  label: string
  count: number
  unit: string
  hint: string
  path: string
  tone: 'danger' | 'warning' | 'info'
}

/** 工作台待办：只列出数量大于 0 的事项，风险高的排前面 */
export function financeTodos(o: FinanceWorkbenchOverview | null | undefined): FinanceTodo[] {
  if (!o) return []
  const n = (value: unknown) => Number(value ?? 0)
  const items: FinanceTodo[] = [
    { label: '待审批退款', count: n(o.pending?.pendingRefundCount), unit: '笔', hint: '含押金与预收服务费，核对合同及存管规则', path: '/finance/payments/refund-reversal', tone: 'danger' },
    { label: '对账未匹配', count: n(o.reconcile?.billPaidUnmatchedCount), unit: '笔', hint: '账单已收但银行流水未匹配', path: '/finance/reconcile/center', tone: 'danger' },
    { label: '财务异常待处理', count: n(o.pending?.issueTodoCount), unit: '项', hint: '重复扣费、金额不符等走异常工单', path: '/finance/reconcile/issue-center', tone: 'danger' },
    { label: '退住结算待办', count: n(o.pending?.pendingDischargeSettlementCount), unit: '项', hint: '核对未消费预收、押金待退与未结清应收', path: '/finance/discharge/settlement', tone: 'warning' },
    { label: '减免待审核', count: n(o.pending?.pendingDiscountCount), unit: '笔', hint: '减免须有申请人、审批人与依据', path: '/oa/approval', tone: 'warning' },
    { label: '自动扣费失败', count: n(o.autoDebit?.failedCount), unit: '笔', hint: '核实余额与授权后再处理，不得强制扣款', path: '/finance/bills/auto-deduct', tone: 'warning' },
    { label: '催缴提醒', count: n(o.pending?.collectionReminderCount), unit: '项', hint: '按账龄跟进并记录沟通结果', path: '/finance/bills/follow-up', tone: 'info' },
    { label: '票据未关联收款', count: n(o.reconcile?.invoiceUnlinkedCount), unit: '张', hint: '收据、发票须与实际收款对应', path: '/finance/fees/payment-and-invoice', tone: 'info' }
  ]
  return items.filter((item) => item.count > 0)
}

/** 账龄条：按最大金额折算长度；全部为 0 时不显示 */
export function agingRows(items: FinanceArrearsAgingItem[]) {
  const rows = items.map((item) => ({ ...item, amount: Number(item.amount ?? 0) }))
  const max = Math.max(0, ...rows.map((r) => r.amount))
  return max > 0 ? rows.map((r) => ({ ...r, percent: Math.round((r.amount / max) * 100) })) : []
}
