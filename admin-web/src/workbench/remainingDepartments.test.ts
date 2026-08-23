import { beforeEach, describe, expect, it, vi } from 'vitest'
import { AxiosHeaders, type AxiosResponse } from 'axios'
import { resolveWorkbenchProfile } from './model'
import type { HrWorkbenchSummary, LogisticsWorkbenchSummary, MarketingWorkbenchSummary } from '../types'

vi.mock('../api/logistics', () => ({ getLogisticsWorkbenchSummary: vi.fn() }))
vi.mock('../api/hr', () => ({ getHrWorkbenchSummary: vi.fn() }))
vi.mock('../api/marketing', () => ({ getMarketingWorkbenchSummary: vi.fn() }))

import { getLogisticsWorkbenchSummary } from '../api/logistics'
import { getHrWorkbenchSummary } from '../api/hr'
import { getMarketingWorkbenchSummary } from '../api/marketing'
import { buildLogisticsWorkbenchDetail, loadLogisticsWorkbench } from './logistics'
import { buildHrRoleWorkbenchDetail, loadHrRoleWorkbench } from './hr'
import { buildMarketingRoleWorkbenchDetail, loadMarketingRoleWorkbench } from './marketing'

function response<T>(data: T): AxiosResponse<T> {
  return { data, status: 200, statusText: 'OK', headers: new AxiosHeaders(), config: { headers: new AxiosHeaders() } }
}

const logistics = {
  maintenancePendingCount: 8, maintenanceOverdueCount: 2, lowStockCount: 3,
  purchasePendingApprovalCount: 4, equipmentDueSoonCount: 5, todayOutboundQty: 12,
  todayCleaningTaskCount: 7, undeliveredCount: 6, riskIndex: 18, riskTriggeredCount: 2
} as LogisticsWorkbenchSummary

const hr = {
  pendingLeaveApprovalCount: 4, attendanceAbnormalCount: 3, contractExpiringCount: 2,
  warningDays: 30, todayTrainingCount: 1, birthdayTodayCount: 2, onJobCount: 80, leftCount: 6
} as HrWorkbenchSummary

const marketing = {
  followup: { todayDue: 9, overdue: 3, highIntentCount: 4 },
  contract: { pendingSignCount: 2 },
  funnel: { todayConsultCount: 6, pendingSignCount: 2, monthDealCount: 5, monthConversionRate: 25 },
  performance: { monthDealCount: 5, monthAmount: 128000 }
} as MarketingWorkbenchSummary

describe('remaining department workbench adapters', () => {
  beforeEach(() => vi.clearAllMocks())

  it('keeps employee summaries explicitly outside personal performance scope', () => {
    const cases = [
      buildLogisticsWorkbenchDetail(resolveWorkbenchProfile(['LOGISTICS_EMPLOYEE']), logistics),
      buildHrRoleWorkbenchDetail(resolveWorkbenchProfile(['HR_EMPLOYEE']), hr),
      buildMarketingRoleWorkbenchDetail(resolveWorkbenchProfile(['MARKETING_EMPLOYEE']), marketing)
    ]
    for (const detail of cases) {
      expect(detail.scopeLabel).toMatch(/非个人(绩效|业绩)/)
      expect(detail.dataScopeNotice).toBeTruthy()
      expect(detail.personnelBreakdownAvailable).toBe(false)
      expect(detail.actions.filter((item) => item.managementOnly)).not.toEqual([])
    }
  })

  it('uses real aggregate risk fields in minister views', () => {
    const logisticsDetail = buildLogisticsWorkbenchDetail(resolveWorkbenchProfile(['LOGISTICS_MINISTER']), logistics)
    const hrDetail = buildHrRoleWorkbenchDetail(resolveWorkbenchProfile(['HR_MINISTER']), hr)
    const marketingDetail = buildMarketingRoleWorkbenchDetail(resolveWorkbenchProfile(['MARKETING_MINISTER']), marketing)
    expect(logisticsDetail.metrics.map((item) => [item.key, item.value])).toContainEqual(['overdue', 2])
    expect(hrDetail.metrics.map((item) => [item.key, item.value])).toContainEqual(['attendance', 3])
    expect(marketingDetail.metrics.map((item) => [item.key, item.value])).toContainEqual(['intent', 4])
  })

  it('calls existing aggregate APIs without inventing current-user filters', async () => {
    vi.mocked(getLogisticsWorkbenchSummary).mockResolvedValue(response(logistics))
    vi.mocked(getHrWorkbenchSummary).mockResolvedValue(response(hr))
    vi.mocked(getMarketingWorkbenchSummary).mockResolvedValue(response(marketing))

    await loadLogisticsWorkbench(resolveWorkbenchProfile(['LOGISTICS_EMPLOYEE']))
    await loadHrRoleWorkbench(resolveWorkbenchProfile(['HR_EMPLOYEE']))
    await loadMarketingRoleWorkbench(resolveWorkbenchProfile(['MARKETING_EMPLOYEE']))

    expect(getLogisticsWorkbenchSummary).toHaveBeenCalledWith(undefined, { silent403: true, silentError: true })
    expect(getHrWorkbenchSummary).toHaveBeenCalledWith(undefined, { silent403: true, silentError: true })
    expect(getMarketingWorkbenchSummary).toHaveBeenCalledWith()
  })
})
