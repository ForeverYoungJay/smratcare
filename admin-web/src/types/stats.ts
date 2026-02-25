export interface MonthCountItem {
  month: string
  count: number
}

export interface MonthAmountItem {
  month: string
  amount: number
}

export interface NameCountItem {
  name: string
  count: number
}

export interface ElderAmountItem {
  elderId?: number
  elderName?: string
  amount: number
}

export interface CheckInStatsResponse {
  totalAdmissions: number
  totalDischarges: number
  netIncrease: number
  dischargeToAdmissionRate: number
  currentResidents: number
  monthlyAdmissions: MonthCountItem[]
  monthlyDischarges: MonthCountItem[]
  monthlyNetIncrease: MonthCountItem[]
}

export interface ConsumptionStatsResponse {
  totalBillConsumption: number
  totalStoreConsumption: number
  totalConsumption: number
  billConsumptionRatio: number
  storeConsumptionRatio: number
  averageMonthlyConsumption: number
  monthlyBillConsumption: MonthAmountItem[]
  monthlyStoreConsumption: MonthAmountItem[]
  monthlyTotalConsumption: MonthAmountItem[]
  topConsumerElders: ElderAmountItem[]
}

export interface ElderInfoStatsResponse {
  totalElders: number
  inHospitalCount: number
  dischargedCount: number
  genderDistribution: NameCountItem[]
  ageDistribution: NameCountItem[]
  careLevelDistribution: NameCountItem[]
  statusDistribution: NameCountItem[]
}

export interface OrgMonthlyOperationItem {
  orgId?: number
  orgName?: string
  month: string
  admissions: number
  discharges: number
  revenue: number
  totalBeds: number
  occupiedBeds: number
  occupancyRate: number
}

export interface MonthFlowItem {
  month: string
  admissions: number
  discharges: number
}

export interface BedUsageStatsResponse {
  totalBeds: number
  occupiedBeds: number
  availableBeds: number
  maintenanceBeds: number
  occupancyRate: number
  maintenanceRate: number
  availableRate: number
}

export interface MonthlyRevenueStatsResponse {
  totalRevenue: number
  averageMonthlyRevenue: number
  revenueGrowthRate: number
  monthlyRevenue: MonthAmountItem[]
}

export interface FlowReportItem {
  eventType: 'ADMISSION' | 'DISCHARGE'
  eventDate: string
  elderId?: number
  elderName?: string
  remark?: string
}

export interface FlowReportPageResponse {
  list: FlowReportItem[]
  total: number
  pageNo: number
  pageSize: number
}
