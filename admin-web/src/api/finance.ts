import request, { fetchPage } from '../utils/request'
import { getToken } from '../utils/auth'
import type { Id } from '../types'
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
  FinanceOpsInsight,
  FinanceInvoiceReceiptItem,
  FinanceAutoDebitExceptionItem,
  FinanceRoomOpsDetailResponse,
  FinanceAllocationRuleItem,
  FinanceAllocationResidentSyncResponse,
  FinanceAllocationMeterValidateRequest,
  FinanceAllocationMeterValidateResponse,
  FinanceAllocationTemplateInitResponse,
  FinanceReconcileExceptionItem,
  FinanceMasterDataOverview,
  FinanceConfigImpactPreview,
  FinanceConfigImpactPreviewRequest,
  FinanceBillingConfigEntry,
  FinanceBillingConfigUpsertRequest,
  FinanceBillingConfigBatchUpsertRequest,
  FinanceConfigChangeLogItem,
  FinanceBillingConfigRollbackRequest,
  FinanceBillingConfigSnapshotItem,
  FinanceModuleEntrySummary,
  FinanceReportEntrySummary,
  FinanceCategoryConsumptionAnalysis,
  FinanceLedgerHealth,
  FinanceDischargeStatusSync,
  FinanceDischargeStatusSyncExecuteRequest,
  FinanceDischargeStatusSyncExecuteResponse
} from '../types'

export function getPaymentRecordPage(params: any) {
  return fetchPage<PaymentRecordItem>('/api/finance/payment/page', params)
}

export function updatePaymentRecord(paymentId: Id, data: { amount: number; method: string; paidAt: string; remark?: string }) {
  return request.put(`/api/finance/payment/${paymentId}`, data)
}

export function reconcileDaily(params: ReconcileRequest) {
  return request.post<ReconcileDailyItem>('/api/finance/reconcile', null, { params })
}

export function getReconcilePage(params: any) {
  return fetchPage<ReconcileDailyItem>('/api/finance/reconcile/page', params)
}

export function getFinanceBillDetail(billId: Id, config?: Record<string, any>) {
  return request.get<FinanceBillDetail>(`/api/finance/bill/${billId}`, config)
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

export function printElderAccountLogPdf(params: { elderId: Id; accountId?: Id; keyword?: string }) {
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

export function getFinanceWorkbenchOverview(config?: Record<string, any>) {
  return request.get<FinanceWorkbenchOverview>('/api/finance/workbench/overview', config)
}

export function getFinanceOpsInsights() {
  return request.get<FinanceOpsInsight>('/api/finance/workbench/ops-insights')
}

export function getFinanceLedgerHealth(params?: { limit?: number }) {
  return request.get<FinanceLedgerHealth>('/api/finance/workbench/ledger/health', { params })
}

export function getFinanceDischargeStatusSync(params?: { limit?: number }) {
  return request.get<FinanceDischargeStatusSync>('/api/finance/workbench/discharge/status-sync', { params })
}

export function executeFinanceDischargeStatusSync(data: FinanceDischargeStatusSyncExecuteRequest) {
  return request.post<FinanceDischargeStatusSyncExecuteResponse>('/api/finance/workbench/discharge/status-sync/execute', data)
}

export async function exportFinanceLedgerHealthCsv(params?: { limit?: number }) {
  const url = new URL('/api/finance/workbench/ledger/health/export', window.location.origin)
  Object.entries(params || {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      url.searchParams.set(key, String(value))
    }
  })
  const response = await fetch(url.toString(), {
    method: 'GET',
    headers: { Authorization: `Bearer ${getToken()}` }
  })
  if (!response.ok) {
    throw new Error('导出失败')
  }
  const blob = await response.blob()
  const contentDisposition = response.headers.get('content-disposition') || ''
  const filenameMatch = contentDisposition.match(/filename=\"?([^\";]+)\"?/)
  const filename = filenameMatch?.[1] || `finance-ledger-health-${new Date().toISOString().slice(0, 10)}.csv`
  const link = document.createElement('a')
  const objectUrl = URL.createObjectURL(blob)
  link.href = objectUrl
  link.download = filename
  link.click()
  URL.revokeObjectURL(objectUrl)
}

export function getFinanceInvoiceReceiptPage(params: any) {
  return fetchPage<FinanceInvoiceReceiptItem>('/api/finance/workbench/invoice/page', params)
}

export async function exportFinanceInvoiceReceiptCsv(params?: {
  date?: string
  method?: string
  invoiceStatus?: string
  keyword?: string
}) {
  const url = new URL('/api/finance/workbench/invoice/export', window.location.origin)
  Object.entries(params || {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      url.searchParams.set(key, String(value))
    }
  })
  const response = await fetch(url.toString(), {
    method: 'GET',
    headers: {
      Authorization: `Bearer ${getToken()}`
    }
  })
  if (!response.ok) {
    throw new Error('导出失败')
  }
  const blob = await response.blob()
  const contentDisposition = response.headers.get('content-disposition') || ''
  const filenameMatch = contentDisposition.match(/filename=\"?([^\";]+)\"?/)
  const filename = filenameMatch?.[1] || `finance-invoice-receipt-${new Date().toISOString().slice(0, 10)}.csv`
  const link = document.createElement('a')
  const objectUrl = URL.createObjectURL(blob)
  link.href = objectUrl
  link.download = filename
  link.click()
  URL.revokeObjectURL(objectUrl)
}

export function getFinanceAutoDebitExceptions(params?: { date?: string }) {
  return request.get<FinanceAutoDebitExceptionItem[]>('/api/finance/workbench/auto-deduct/exceptions', { params })
}

export async function exportFinanceAutoDebitExceptionsCsv(params?: { date?: string }) {
  const url = new URL('/api/finance/workbench/auto-deduct/exceptions/export', window.location.origin)
  Object.entries(params || {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      url.searchParams.set(key, String(value))
    }
  })
  const response = await fetch(url.toString(), {
    method: 'GET',
    headers: { Authorization: `Bearer ${getToken()}` }
  })
  if (!response.ok) {
    throw new Error('导出失败')
  }
  const blob = await response.blob()
  const contentDisposition = response.headers.get('content-disposition') || ''
  const filenameMatch = contentDisposition.match(/filename=\"?([^\";]+)\"?/)
  const filename = filenameMatch?.[1] || `finance-auto-deduct-exceptions-${new Date().toISOString().slice(0, 10)}.csv`
  const link = document.createElement('a')
  const objectUrl = URL.createObjectURL(blob)
  link.href = objectUrl
  link.download = filename
  link.click()
  URL.revokeObjectURL(objectUrl)
}

export function getFinanceRoomOpsDetail(params?: { period?: string; building?: string; room?: string }) {
  return request.get<FinanceRoomOpsDetailResponse>('/api/finance/workbench/room-ops/detail', { params })
}

export function getFinanceAllocationRules(params?: { month?: string }) {
  return request.get<FinanceAllocationRuleItem[]>('/api/finance/workbench/allocation/rules', { params })
}

export function getFinanceAllocationResidentOptions(params?: {
  building?: string
  floorNo?: string
  roomNo?: string
}) {
  return request.get<FinanceAllocationResidentSyncResponse>('/api/finance/workbench/allocation/resident-options', { params })
}

export function initFinanceAllocationRuleTemplate(params?: { month?: string }) {
  return request.post<FinanceAllocationTemplateInitResponse>('/api/finance/workbench/allocation/rules/template/init', null, { params })
}

export function validateFinanceAllocationMeter(data: FinanceAllocationMeterValidateRequest) {
  return request.post<FinanceAllocationMeterValidateResponse>('/api/finance/workbench/allocation/meter/validate', data)
}

export function getFinanceReconcileExceptions(params?: { date?: string; type?: string }) {
  return request.get<FinanceReconcileExceptionItem[]>('/api/finance/workbench/reconcile/exceptions', { params })
}

export async function exportFinanceReconcileExceptionsCsv(params?: { date?: string; type?: string }) {
  const url = new URL('/api/finance/workbench/reconcile/exceptions/export', window.location.origin)
  Object.entries(params || {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      url.searchParams.set(key, String(value))
    }
  })
  const response = await fetch(url.toString(), {
    method: 'GET',
    headers: { Authorization: `Bearer ${getToken()}` }
  })
  if (!response.ok) {
    throw new Error('导出失败')
  }
  const blob = await response.blob()
  const contentDisposition = response.headers.get('content-disposition') || ''
  const filenameMatch = contentDisposition.match(/filename=\"?([^\";]+)\"?/)
  const filename = filenameMatch?.[1] || `finance-reconcile-exceptions-${new Date().toISOString().slice(0, 10)}.csv`
  const link = document.createElement('a')
  const objectUrl = URL.createObjectURL(blob)
  link.href = objectUrl
  link.download = filename
  link.click()
  URL.revokeObjectURL(objectUrl)
}

export async function exportFinanceReconcileHistoryCsv(params?: { from?: string; to?: string }) {
  const url = new URL('/api/finance/reconcile/export', window.location.origin)
  Object.entries(params || {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      url.searchParams.set(key, String(value))
    }
  })
  const response = await fetch(url.toString(), {
    method: 'GET',
    headers: { Authorization: `Bearer ${getToken()}` }
  })
  if (!response.ok) {
    throw new Error('导出失败')
  }
  const blob = await response.blob()
  const contentDisposition = response.headers.get('content-disposition') || ''
  const filenameMatch = contentDisposition.match(/filename=\"?([^\";]+)\"?/)
  const filename = filenameMatch?.[1] || `finance-reconcile-history-${new Date().toISOString().slice(0, 10)}.csv`
  const link = document.createElement('a')
  const objectUrl = URL.createObjectURL(blob)
  link.href = objectUrl
  link.download = filename
  link.click()
  URL.revokeObjectURL(objectUrl)
}

export function getFinanceMasterDataOverview(params?: { month?: string }) {
  return request.get<FinanceMasterDataOverview>('/api/finance/workbench/config/overview', { params })
}

export function getFinanceConfigImpactPreview(data: FinanceConfigImpactPreviewRequest) {
  return request.post<FinanceConfigImpactPreview>('/api/finance/workbench/config/impact-preview', data)
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

export function getFinanceReportEntrySummary(params: { reportKey: string; from?: string; to?: string; top?: number }) {
  return request.get<FinanceReportEntrySummary>('/api/finance/report/entry-summary', { params })
}

export function getFinanceCategoryConsumptionAnalysis(params: {
  from: string
  to: string
  itemKeyword?: string
  building?: string
  floorNo?: string
}) {
  return request.get<FinanceCategoryConsumptionAnalysis>('/api/finance/report/category-consumption-analysis', { params })
}

export function bindFinanceBillElder(billId: Id, data: { elderId: Id; remark?: string }) {
  return request.post(`/api/finance/bill/${billId}/bind-elder`, data)
}
