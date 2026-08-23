import { beforeEach, describe, expect, it, vi } from 'vitest'
import { resolveWorkbenchProfile } from './model'

vi.mock('../api/care', () => ({ getTaskSummary: vi.fn(), getTaskPage: vi.fn() }))
vi.mock('../api/finance', () => ({ getFinanceWorkbenchOverview: vi.fn() }))
vi.mock('../api/hr', () => ({ getHrWorkbenchSummary: vi.fn() }))
vi.mock('../api/logistics', () => ({ getLogisticsWorkbenchSummary: vi.fn() }))
vi.mock('../api/marketing', () => ({ getMarketingWorkbenchSummary: vi.fn() }))
vi.mock('../api/medicalCare', () => ({ getMedicalCareWorkbenchSummary: vi.fn() }))

import { getTaskPage, getTaskSummary } from '../api/care'
import { getFinanceWorkbenchOverview } from '../api/finance'
import { getHrWorkbenchSummary } from '../api/hr'
import { getLogisticsWorkbenchSummary } from '../api/logistics'
import { getMarketingWorkbenchSummary } from '../api/marketing'
import { getMedicalCareWorkbenchSummary } from '../api/medicalCare'
import { loadDepartmentWorkbenchSnapshot } from './dataSources'

describe('department workbench data source', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(getTaskPage).mockResolvedValue({ list: [], total: 0, pageNo: 1, pageSize: 10 })
  })

  it.each([
    ['NURSING_EMPLOYEE', getTaskSummary],
    ['MEDICAL_EMPLOYEE', getMedicalCareWorkbenchSummary],
    ['FINANCE_EMPLOYEE', getFinanceWorkbenchOverview],
    ['LOGISTICS_EMPLOYEE', getLogisticsWorkbenchSummary],
    ['HR_EMPLOYEE', getHrWorkbenchSummary],
    ['MARKETING_EMPLOYEE', getMarketingWorkbenchSummary]
  ])('loads only the department source assigned to %s', async (role, expectedLoader) => {
    const loaders = [getTaskSummary, getMedicalCareWorkbenchSummary, getFinanceWorkbenchOverview, getLogisticsWorkbenchSummary, getHrWorkbenchSummary, getMarketingWorkbenchSummary]
    loaders.forEach((loader) => vi.mocked(loader as any).mockResolvedValue({}))
    await loadDepartmentWorkbenchSnapshot(resolveWorkbenchProfile([role]), () => true, { staffId: '88' })
    expect(expectedLoader).toHaveBeenCalledTimes(1)
    expect(loaders.reduce((total, loader) => total + vi.mocked(loader as any).mock.calls.length, 0)).toBe(1)
  })

  it('does not call an API when the route permission is unavailable', async () => {
    const result = await loadDepartmentWorkbenchSnapshot(resolveWorkbenchProfile(['NURSING_EMPLOYEE']), () => false)
    expect(result.status).toBe('forbidden')
    expect(getTaskSummary).not.toHaveBeenCalled()
  })

  it('normalizes real nursing summary fields into shared metrics', async () => {
    vi.mocked(getTaskSummary).mockResolvedValue({ pendingCount: 8, overdueCount: 2, exceptionCount: 1, completionRate: 75 } as any)
    const result = await loadDepartmentWorkbenchSnapshot(resolveWorkbenchProfile(['NURSING_EMPLOYEE']), () => true, { staffId: '88' })
    expect(result.status).toBe('ready')
    expect(result.snapshot?.metrics.map((item) => [item.key, item.value])).toEqual([
      ['pending', 8], ['overdue', 2], ['exception', 1], ['completion', 75]
    ])
  })

  it('distinguishes empty data from request failures', async () => {
    vi.mocked(getTaskSummary).mockResolvedValue({} as any)
    expect((await loadDepartmentWorkbenchSnapshot(resolveWorkbenchProfile(['NURSING_EMPLOYEE']), () => true, { staffId: '88' })).status).toBe('empty')
    vi.mocked(getTaskSummary).mockRejectedValue(new Error('network down'))
    expect((await loadDepartmentWorkbenchSnapshot(resolveWorkbenchProfile(['NURSING_EMPLOYEE']), () => true, { staffId: '88' })).status).toBe('error')
  })
})
