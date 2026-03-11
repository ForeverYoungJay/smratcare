import type { Id } from './common'

export interface LogisticsNamedStatItem {
  name: string
  quantity: number
}

export interface LogisticsWorkbenchSummaryQuery {
  windowDays?: number
  expiryDays?: number
  overdueDays?: number
  maintenanceDueDays?: number
}

export type LogisticsWorkbenchRiskLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'

export interface LogisticsWorkbenchSummary {
  lowStockCount: number
  expiringCount: number
  todayOutboundQty: number
  inventoryTotalAmount: number
  weekTopConsumption: LogisticsNamedStatItem[]
  inventoryAssetQty: number
  inventoryConsumableQty: number
  inventoryFoodQty: number
  inventoryServiceQty: number
  lowStockAssetCount: number
  lowStockConsumableCount: number
  lowStockFoodCount: number
  lowStockServiceCount: number
  monthPurchaseAssetAmount: number
  monthPurchaseConsumableAmount: number
  monthPurchaseFoodAmount: number
  monthPurchaseServiceAmount: number
  todayOutboundAssetQty: number
  todayOutboundConsumableQty: number
  todayOutboundFoodQty: number
  todayOutboundServiceQty: number

  purchasePendingApprovalCount: number
  purchaseApprovedNotArrivedCount: number
  purchaseReceivedNotSettledCount: number
  monthPurchaseAmount: number

  occupiedBeds: number
  freeBeds: number
  cleaningBeds: number
  maintenanceBeds: number
  todayDischargeCount: number
  todayAdmissionCount: number

  maintenancePendingCount: number
  maintenanceProcessingCount: number
  maintenanceOverdueCount: number
  monthMaintenanceCost: number

  todayMealOrderCount: number
  personalizedMealCount: number
  deliveryCompletionRate: number
  undeliveredCount: number
  todayDiningCost: number

  diabetesMealCount: number
  lowSaltMealCount: number
  allergyAlertCount: number
  specialNutritionMealCount: number

  monthConsumeAmount: number
  monthTopConsumption: LogisticsNamedStatItem[]
  nursingConsumeAmount: number
  diningConsumeAmount: number

  todayCleaningTaskCount: number
  todayMaintenanceTaskCount: number
  todayDeliveryTaskCount: number
  todayInventoryCheckTaskCount: number

  equipmentTotalCount: number
  equipmentMaintainingCount: number
  equipmentDueSoonCount: number

  maintenanceTodoLastStatus?: 'SUCCESS' | 'FAILED'
  maintenanceTodoLastTriggerType?: 'MANUAL' | 'SCHEDULED' | 'RETRY'
  maintenanceTodoLastExecutedAt?: string
  maintenanceTodoLastCreatedCount?: number
  maintenanceTodoLastSkippedCount?: number
  maintenanceTodoLastErrorMessage?: string
  maintenanceTodoFailedCount7d: number

  configuredWindowDays?: number
  configuredExpiryDays?: number
  configuredOverdueDays?: number
  configuredMaintenanceDueDays?: number
  snapshotStartDate?: string
  snapshotEndDate?: string
  generatedAt?: string

  bedOccupancyRate?: number
  maintenanceOverdueRate?: number
  deliveryUndeliveredRate?: number
  equipmentDueSoonRate?: number

  riskIndex?: number
  riskLevel?: LogisticsWorkbenchRiskLevel
  riskTriggeredCount?: number
  riskSignals?: string[]
}

export type LogisticsEquipmentStatus = 'ENABLED' | 'DISABLED' | 'MAINTENANCE' | 'SCRAPPED'

export interface LogisticsEquipmentArchive {
  id: Id
  equipmentCode: string
  equipmentName: string
  category?: string
  brand?: string
  modelNo?: string
  serialNo?: string
  purchaseDate?: string
  warrantyUntil?: string
  location?: string
  maintainerName?: string
  lastMaintainedAt?: string
  nextMaintainedAt?: string
  status?: LogisticsEquipmentStatus
  remark?: string
}

export interface LogisticsMaintenanceTodoJobLog {
  id: Id
  orgId?: Id
  triggerType: 'MANUAL' | 'SCHEDULED' | 'RETRY'
  status: 'SUCCESS' | 'FAILED'
  days: number
  totalMatched: number
  createdCount: number
  skippedCount: number
  errorMessage?: string
  executedAt?: string
  createdBy?: Id
  createTime?: string
  updateTime?: string
}

export interface LogisticsMaintenanceTodoJobLogOverview {
  recentTotalRuns: number
  recentSuccessRuns: number
  recentFailedRuns: number
  recentWindowDays: number

  lastExecutedAt?: string
  lastStatus?: 'SUCCESS' | 'FAILED'
  lastTriggerType?: 'MANUAL' | 'SCHEDULED' | 'RETRY'
  lastDays?: number
  lastCreatedCount?: number
  lastSkippedCount?: number
  lastErrorMessage?: string

  lastFailedLogId?: Id
  lastFailedExecutedAt?: string
  lastFailedErrorMessage?: string
}
