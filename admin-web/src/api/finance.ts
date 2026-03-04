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
  ReconcileRequest,
  FinanceWorkbenchOverview,
  FinanceInvoiceReceiptItem,
  FinanceAutoDebitExceptionItem,
  FinanceRoomOpsDetailResponse,
  FinanceAllocationRuleItem,
  FinanceReconcileExceptionItem,
  FinanceMasterDataOverview,
  FinanceBillingConfigEntry,
  FinanceBillingConfigUpsertRequest,
  FinanceBillingConfigBatchUpsertRequest,
  FinanceConfigChangeLogItem,
  FinanceBillingConfigRollbackRequest,
  FinanceBillingConfigSnapshotItem,
  FinanceModuleEntrySummary
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

export function printElderAccountLogPdf(params: { elderId: number; accountId?: number; keyword?: string }) {
  return request.get<Blob>('/api/finance/account/log/print', { params, responseType: 'blob' as any })
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

export function getFinanceWorkbenchOverview() {
  return request.get<FinanceWorkbenchOverview>('/api/finance/workbench/overview')
}

export function getFinanceInvoiceReceiptPage(params: any) {
  return fetchPage<FinanceInvoiceReceiptItem>('/api/finance/workbench/invoice/page', params)
}

export function getFinanceAutoDebitExceptions(params?: { date?: string }) {
  return request.get<FinanceAutoDebitExceptionItem[]>('/api/finance/workbench/auto-deduct/exceptions', { params })
}

export function getFinanceRoomOpsDetail(params?: { period?: string; building?: string; room?: string }) {
  return request.get<FinanceRoomOpsDetailResponse>('/api/finance/workbench/room-ops/detail', { params })
}

export function getFinanceAllocationRules(params?: { month?: string }) {
  return request.get<FinanceAllocationRuleItem[]>('/api/finance/workbench/allocation/rules', { params })
}

export function getFinanceReconcileExceptions(params?: { date?: string; type?: string }) {
  return request.get<FinanceReconcileExceptionItem[]>('/api/finance/workbench/reconcile/exceptions', { params })
}

export function getFinanceMasterDataOverview(params?: { month?: string }) {
  return request.get<FinanceMasterDataOverview>('/api/finance/workbench/config/overview', { params })
}

export function getFinanceBillingConfig(params?: { month?: string; keyPrefix?: string }) {
  return request.get<FinanceBillingConfigEntry[]>('/api/finance/workbench/billing-config', { params })
}

export function upsertFinanceBillingConfig(data: FinanceBillingConfigUpsertRequest) {
  return request.post<FinanceBillingConfigEntry>('/api/finance/workbench/billing-config', data)
}

export function batchUpsertFinanceBillingConfig(data: FinanceBillingConfigBatchUpsertRequest) {
  return request.post<FinanceBillingConfigEntry[]>('/api/finance/workbench/billing-config/batch', data)
}

export function getFinanceConfigChangeLogPage(params: any) {
  return fetchPage<FinanceConfigChangeLogItem>('/api/finance/workbench/config/change-log/page', params)
}

export function rollbackFinanceBillingConfig(data: FinanceBillingConfigRollbackRequest) {
  return request.post<FinanceBillingConfigEntry>('/api/finance/workbench/billing-config/rollback', data)
}

export function getFinanceBillingConfigSnapshots() {
  return request.get<FinanceBillingConfigSnapshotItem[]>('/api/finance/workbench/billing-config/snapshots')
}

export function getFinanceModuleEntrySummary(params: { moduleKey: string }) {
  return request.get<FinanceModuleEntrySummary>('/api/finance/workbench/module-entry', { params })
}
