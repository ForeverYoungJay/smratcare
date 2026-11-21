// Detail columns remain available; status, risk and action columns are never collapsed.
const primary: Record<string, string[]> = {
  '/hr/profile/basic': ['staffNo', 'realName', 'jobTitle', 'employmentType', 'contractEndDate', 'status', 'action'],
  '/hr/profile/account-access': ['username', 'realName', 'departmentId', 'directLeaderId', 'supervisorIssue', 'status', 'action'],
  '/hr/profile/contracts': ['realName', 'contractNo', 'contractType', 'contractEndDate', 'remainingDays', 'contractStatus', 'action'],
  '/hr/profile/social-security-reminders': ['staffName', 'hireDate', 'socialSecurityStatus', 'socialSecurityWorkflowStatus', 'socialSecurityMonthlyAmount', 'reminderDate', 'actions'],
  '/hr/development/records': ['staffName', 'departmentName', 'trainingName', 'startDate', 'hours', 'status', 'certificateStatus', 'actions'],
  '/hr/performance/reports': ['rankNo', 'staffName', 'taskCount', 'successCount', 'avgScore', 'pointsBalance', 'action'],
  '/finance/accounts/list': ['elderName', 'balance', 'depositBalance', 'prepaidBalance', 'status', 'action'],
  '/finance/discharge/settlement': ['elderName', 'payableAmount', 'fromDepositAmount', 'refundAmount', 'supplementAmount', 'riskLevel', 'status', 'action']
}
export function compactWorkspaceColumns<T extends { key?: unknown; dataIndex?: unknown; title?: unknown }>(columns: T[], path: string): T[] {
  if (columns.length <= 8) return columns
  const preferred = primary[path]
  const chosen = columns.filter((column, index) => {
    const key = String(column.key || column.dataIndex || '')
    return (preferred ? preferred.includes(key) : index < 6) || /action|operation/i.test(key) || /状态|风险|异常|审批|超时/.test(String(column.title || ''))
  })
  return chosen.length >= 3 ? chosen : columns
}

export function emphasizeColumns<T extends { key?: unknown; dataIndex?: unknown; title?: unknown; className?: string; children?: T[] }>(columns: T[]): T[] {
  return columns.map(column => {
    const label = `${String(column.key || column.dataIndex || '')} ${String(column.title || '')}`
    const emphasis = /amount|balance|payable|refund|supplement|金额|应收|应付|余额|退款/i.test(label) ? 'money'
      : /状态|风险|异常|超时|到期|截止|status|risk|deadline/i.test(label) ? 'state'
        : /姓名|长者|老人|患者|staffName|elderName|realName/i.test(label) ? 'identity' : ''
    return { ...column, className: [column.className, emphasis && `business-column--${emphasis}`].filter(Boolean).join(' '),
      ...(column.children ? { children: emphasizeColumns(column.children) } : {}) }
  })
}
