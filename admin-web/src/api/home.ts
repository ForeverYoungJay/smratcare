import { getDashboardSummary, type DashboardSummary } from './dashboard'
import { getHrWorkbenchSummary } from './hr'
import { getLogisticsEquipmentSummary, getLogisticsWorkbenchSummary } from './logistics'
import { getPortalSummary } from './oa'
import { getOperationsCapabilityMap, type OperationsCapabilityMap } from './operations'
import { getMonthlyRevenueStats } from './stats'
import type { HrWorkbenchSummary, LogisticsEquipmentArchiveSummary, LogisticsWorkbenchSummary, MonthlyRevenueStatsResponse, OaPortalSummary } from '../types'

export interface HomeOverviewBundle {
  dashboard: DashboardSummary | null
  equipment: LogisticsEquipmentArchiveSummary | null
  hr: HrWorkbenchSummary | null
  logistics: LogisticsWorkbenchSummary | null
  operations: OperationsCapabilityMap | null
  portal: OaPortalSummary | null
  revenue: MonthlyRevenueStatsResponse | null
}

const silent403Config = { silent403: true, silentError: true }

async function safelyLoad<T>(loader: () => Promise<T>) {
  try {
    return await loader()
  } catch {
    return null
  }
}

export async function getHomeOverviewBundle(): Promise<HomeOverviewBundle> {
  const [dashboard, portal, hr, logistics, equipment, revenue, operations] = await Promise.all([
    safelyLoad(() => getDashboardSummary(silent403Config)),
    safelyLoad(() => getPortalSummary(silent403Config)),
    safelyLoad(() => getHrWorkbenchSummary(undefined, silent403Config)),
    safelyLoad(() => getLogisticsWorkbenchSummary(undefined, silent403Config)),
    safelyLoad(() => getLogisticsEquipmentSummary({}, silent403Config)),
    safelyLoad(() => getMonthlyRevenueStats({ months: 6, orgId: undefined }, silent403Config)),
    safelyLoad(() => getOperationsCapabilityMap(silent403Config))
  ])

  return {
    dashboard,
    equipment,
    hr,
    logistics,
    operations,
    portal,
    revenue
  }
}
