import { getFinanceWorkbenchOverview } from '../api/finance'
import type { FinancePaymentMethodAmount, FinanceWorkbenchOverview } from '../types'
import type { WorkbenchProfile } from './model'

export type FinanceWorkbenchScope = 'SHIFT_OPERATION' | 'DEPARTMENT'

export interface FinanceWorkbenchCheck {
  key: string
  label: string
  count: number
  statusLabel: string
  path: string
}

export interface FinanceWorkbenchRisk {
  key: string
  label: string
  value: number
  suffix?: string
  path: string
}

export interface FinanceWorkbenchDetail {
  scope: FinanceWorkbenchScope
  scopeLabel: string
  personalPerformanceAvailable: false
  overview: FinanceWorkbenchOverview
  paymentMethods: FinancePaymentMethodAmount[]
  closeChecks: FinanceWorkbenchCheck[]
  risks: FinanceWorkbenchRisk[]
}

export interface FinanceWorkbenchResult {
  overview: FinanceWorkbenchOverview
  detail: FinanceWorkbenchDetail
}

function responseData<T>(response: T | { data: T }): T {
  return response && typeof response === 'object' && 'data' in response
    ? (response as { data: T }).data
    : response as T
}

function closeCheck(key: string, label: string, count: number | undefined, path: string): FinanceWorkbenchCheck {
  const normalized = Number(count || 0)
  return {
    key,
    label,
    count: normalized,
    statusLabel: normalized > 0 ? `待处理 ${normalized}` : '已清零',
    path
  }
}

export function buildFinanceWorkbenchDetail(
  profile: WorkbenchProfile,
  overview: FinanceWorkbenchOverview
): FinanceWorkbenchDetail {
  const managementView = profile.hasManagementView
  return {
    scope: managementView ? 'DEPARTMENT' : 'SHIFT_OPERATION',
    scopeLabel: managementView ? '财务部经营与风险口径' : '机构当日收银口径（非个人业绩）',
    personalPerformanceAvailable: false,
    overview,
    paymentMethods: overview.cashier?.paymentMethods || [],
    closeChecks: [
      closeCheck('invoice', '票据未关联', overview.reconcile?.invoiceUnlinkedCount, '/finance/fees/payment-and-invoice'),
      closeCheck('payment', '账单收款不一致', overview.reconcile?.billPaidUnmatchedCount, '/finance/reconcile/issue-center'),
      closeCheck('reversal', '退款/冲正待复核', overview.reconcile?.duplicatedOrReversalPendingCount, '/finance/payments/refund-reversal')
    ],
    risks: managementView ? [
      { key: 'arrears', label: '欠费长者', value: Number(overview.risk?.overdueElderCount || 0), suffix: '位', path: '/finance/bills/follow-up' },
      { key: 'arrearsAmount', label: '欠费金额', value: Number(overview.risk?.overdueAmount || 0), suffix: '元', path: '/finance/bills/follow-up' },
      { key: 'lowBalance', label: '低余额账户', value: Number(overview.risk?.lowBalanceCount || 0), suffix: '户', path: '/finance/accounts/list' },
      { key: 'monthClose', label: '月结阻塞', value: Number(overview.pending?.lockedMonthCount || 0), suffix: '项', path: '/finance/reconcile/month-close' }
    ] : []
  }
}

export async function loadFinanceWorkbench(profile: WorkbenchProfile): Promise<FinanceWorkbenchResult> {
  const response = await getFinanceWorkbenchOverview({ silent403: true, silentError: true })
  const overview = responseData<FinanceWorkbenchOverview>(response)
  return { overview, detail: buildFinanceWorkbenchDetail(profile, overview) }
}
