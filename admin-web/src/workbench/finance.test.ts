import { beforeEach, describe, expect, it, vi } from 'vitest'
import { AxiosHeaders, type AxiosResponse } from 'axios'
import { resolveWorkbenchProfile } from './model'

vi.mock('../api/finance', () => ({ getFinanceWorkbenchOverview: vi.fn() }))

import { getFinanceWorkbenchOverview } from '../api/finance'
import type { FinanceWorkbenchOverview } from '../types'
import { buildFinanceWorkbenchDetail, loadFinanceWorkbench } from './finance'

function overview(): FinanceWorkbenchOverview {
  return {
    bizDate: '2026-08-23',
    cashier: { todayCollectedTotal: 1280, todayInvoiceAmount: 800, todayRefundAmount: 50, paymentMethods: [{ method: 'CASH', methodLabel: '现金', amount: 480 }] },
    risk: { overdueElderCount: 3, overdueAmount: 6000, lowBalanceCount: 2, expiringContractCount: 1 },
    pending: { pendingDiscountCount: 2, pendingRefundCount: 1, pendingDischargeSettlementCount: 1, issueTodoCount: 4, lockedMonthCount: 1 },
    revenueStructure: { monthRevenueTotal: 0, categories: [] },
    roomOps: { floorTop: [], floorBottom: [], roomTop10: [], emptyBedLossEstimate: 0 },
    autoDebit: { shouldDeductCount: 0, successCount: 0, failedCount: 0, pendingHandleCount: 0, failureReasons: [] },
    medicalFlow: { todayFlowCount: 0, todayFlowAmount: 0, pendingReviewCount: 0, duplicateBillingCount: 0, missingOrderLinkCount: 0 },
    allocation: { monthGeneratedCount: 0, ungeneratedRoomCount: 0, errorCount: 0 },
    reconcile: { billPaidUnmatchedCount: 2, duplicatedOrReversalPendingCount: 1, invoiceUnlinkedCount: 3 },
    quickEntries: []
  }
}

describe('finance workbench adapter', () => {
  beforeEach(() => vi.clearAllMocks())

  it('labels employee totals as institution shift data rather than personal performance', () => {
    const detail = buildFinanceWorkbenchDetail(resolveWorkbenchProfile(['FINANCE_EMPLOYEE']), overview())
    expect(detail.scope).toBe('SHIFT_OPERATION')
    expect(detail.scopeLabel).toContain('非个人业绩')
    expect(detail.personalPerformanceAvailable).toBe(false)
    expect(detail.risks).toEqual([])
  })

  it('builds manager risk and close checks from real overview fields', () => {
    const detail = buildFinanceWorkbenchDetail(resolveWorkbenchProfile(['FINANCE_MINISTER']), overview())
    expect(detail.scope).toBe('DEPARTMENT')
    expect(detail.risks.map((item) => [item.key, item.value])).toEqual([
      ['arrears', 3], ['arrearsAmount', 6000], ['lowBalance', 2], ['monthClose', 1]
    ])
    expect(detail.closeChecks[0]).toMatchObject({ label: '票据未关联', count: 3, statusLabel: '待处理 3' })
  })

  it('loads the existing overview without inventing a personal filter', async () => {
    const response: AxiosResponse<FinanceWorkbenchOverview> = {
      data: overview(),
      status: 200,
      statusText: 'OK',
      headers: new AxiosHeaders(),
      config: { headers: new AxiosHeaders() }
    }
    vi.mocked(getFinanceWorkbenchOverview).mockResolvedValue(response)
    const result = await loadFinanceWorkbench(resolveWorkbenchProfile(['FINANCE_EMPLOYEE']))
    expect(getFinanceWorkbenchOverview).toHaveBeenCalledWith({ silent403: true, silentError: true })
    expect(result.overview.cashier.todayCollectedTotal).toBe(1280)
  })
})
