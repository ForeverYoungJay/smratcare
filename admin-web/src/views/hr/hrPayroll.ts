/** 工资批次：状态说明与每个状态下谁能做什么（与后端 HrRules 一致） */
export const PAYROLL_STATUS: Record<string, { label: string; color: string }> = {
  DRAFT: { label: '核算草稿', color: 'default' },
  SUBMITTED: { label: '待审核', color: 'orange' },
  APPROVED: { label: '已审核，待财务付款', color: 'blue' },
  BANK_SUBMITTED: { label: '已提交银行', color: 'purple' },
  PAID: { label: '付款成功', color: 'green' }
}

type Action = { action: 'submit' | 'approve' | 'reject' | 'bank-submit' | 'mark-paid'; label: string; needNote?: boolean; needRef?: boolean; danger?: boolean }

const has = (roles: string[], list: string[]) => roles.some((role) => list.includes(String(role).toUpperCase()))

export function payrollActions(status: string, roles: string[]): Action[] {
  const prepare = has(roles, ['HR_EMPLOYEE', 'HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'])
  const approve = has(roles, ['HR_MINISTER', 'DIRECTOR'])
  const finance = has(roles, ['FINANCE_EMPLOYEE', 'FINANCE_MINISTER'])
  if (status === 'DRAFT' && prepare) return [{ action: 'submit', label: '提交审核' }]
  if (status === 'SUBMITTED' && approve) return [{ action: 'approve', label: '审核通过' }, { action: 'reject', label: '退回', needNote: true, danger: true }]
  if (status === 'APPROVED' && finance) return [{ action: 'bank-submit', label: '复核并提交银行' }]
  if (status === 'BANK_SUBMITTED' && finance) return [{ action: 'mark-paid', label: '登记付款成功', needRef: true }]
  return []
}

/** 前端预览实发（最终以后端两位小数计算为准）：按分计算避免浮点误差 */
export function previewNet(line: { baseAmount?: number; performanceAmount?: number; allowanceAmount?: number; overtimeAmount?: number; deductionAmount?: number; personalAmount?: number }) {
  const cents = (value?: number) => Math.round(Number(value || 0) * 100)
  const gross = cents(line.baseAmount) + cents(line.performanceAmount) + cents(line.allowanceAmount) + cents(line.overtimeAmount)
  return { gross: gross / 100, net: (gross - cents(line.deductionAmount) - cents(line.personalAmount)) / 100 }
}
