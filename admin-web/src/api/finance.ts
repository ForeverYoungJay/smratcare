import request, { fetchPage } from '../utils/request'
import type {
  FinanceBillDetail,
  FinanceReportMonthlyItem,
  FinanceArrearsItem,
  FinanceStoreSalesItem,
  ElderAccount,
  ElderAccountLog,
  ElderAccountAdjustRequest,
  ElderAccountUpdateRequest,
  PaymentRecordItem,
  ReconcileDailyItem,
  ReconcileRequest
} from '../types'

export function getPaymentRecordPage(params: any) {
  return fetchPage<PaymentRecordItem>('/api/finance/payment/page', params)
}

export function reconcileDaily(params: ReconcileRequest) {
  return request.post<ReconcileDailyItem>('/api/finance/reconcile', null, { params })
}

export function getReconcilePage(params: any) {
  return fetchPage<ReconcileDailyItem>('/api/finance/reconcile/page', params)
}

export function getFinanceBillDetail(billId: number) {
  return request.get<FinanceBillDetail>(`/api/finance/bill/${billId}`)
}

export function getFinanceMonthlyRevenue(params?: { from?: string; to?: string; months?: number }) {
  return request.get<FinanceReportMonthlyItem[]>('/api/finance/report/monthly-revenue', { params })
}

export function getFinanceArrearsTop(params?: { limit?: number }) {
  return request.get<FinanceArrearsItem[]>('/api/finance/report/arrears-top', { params })
}

export function getFinanceStoreSales(params?: { from?: string; to?: string; months?: number }) {
  return request.get<FinanceStoreSalesItem[]>('/api/finance/report/store-sales', { params })
}

export function getElderAccountPage(params: any) {
  return fetchPage<ElderAccount>('/api/finance/account/page', params)
}

export function getElderAccountLogPage(params: any) {
  return fetchPage<ElderAccountLog>('/api/finance/account/log/page', params)
}

export function adjustElderAccount(data: ElderAccountAdjustRequest) {
  return request.post<ElderAccount>('/api/finance/account/adjust', data)
}

export function updateElderAccount(data: ElderAccountUpdateRequest) {
  return request.post<ElderAccount>('/api/finance/account/update', data)
}

export function getElderAccountWarnings() {
  return request.get<ElderAccount[]>('/api/finance/account/warnings')
}
