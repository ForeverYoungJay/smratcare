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
}

export function getDashboardSummary() {
  return request.get<DashboardSummary>('/api/dashboard/summary')
}
