import request from '../utils/request'

export interface DashboardSummary {
  careTasksToday: number
  abnormalTasksToday: number
  inventoryAlerts: number
  unpaidBills: number
  totalAdmissions: number
  totalDischarges: number
  checkInNetIncrease: number
  dischargeToAdmissionRate: number
  totalBillConsumption: number
  totalStoreConsumption: number
  totalConsumption: number
  averageMonthlyConsumption: number
  billConsumptionRatio: number
  storeConsumptionRatio: number
  inHospitalCount: number
  dischargedCount: number
  totalBeds: number
  occupiedBeds: number
  availableBeds: number
  bedOccupancyRate: number
  bedAvailableRate: number
  totalRevenue: number
  averageMonthlyRevenue: number
  revenueGrowthRate: number
  statsFromMonth?: string
  statsToMonth?: string
  metricVersion?: string
  metricEffectiveDate?: string
  dataRefreshedAt?: string
}

export interface DashboardMetricDefinition {
  metricKey: string
  metricName: string
  metricGroup: string
  formula: string
  source: string
  refreshPolicy: string
  suggestedRoute: string
  requiredRoles: string[]
}

export interface DashboardMetricCatalog {
  metricVersion: string
  effectiveDate: string
  defaultWindow: string
  definitions: DashboardMetricDefinition[]
}

export interface DashboardThresholdDefaults {
  abnormalTaskThreshold: number
  inventoryAlertThreshold: number
  bedOccupancyThreshold: number
  revenueDropThreshold: number
  configVersion: string
}

export function getDashboardSummary() {
  return request.get<DashboardSummary>('/api/dashboard/summary')
}

export function getDashboardMetricCatalog() {
  return request.get<DashboardMetricCatalog>('/api/dashboard/metric-catalog')
}

export function getDashboardThresholdDefaults() {
  return request.get<DashboardThresholdDefaults>('/api/dashboard/threshold-defaults')
}
