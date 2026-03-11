import type { Id } from './common'

export type DiningMealType = 'BREAKFAST' | 'LUNCH' | 'DINNER' | 'SNACK' | 'UNKNOWN'
export type DiningEnableStatus = 'ENABLED' | 'DISABLED'
export type DiningOrderStatus = 'CREATED' | 'PREPARING' | 'DELIVERING' | 'DELIVERED' | 'CANCELLED'
export type DiningDeliveryStatus = 'PENDING' | 'DELIVERED' | 'FAILED'

export interface DiningDish {
  id: Id
  dishName: string
  dishCategory?: string
  mealType?: DiningMealType
  unitPrice: number
  calories?: number
  nutritionInfo?: string
  allergenTags?: string
  tagIds?: string
  status?: DiningEnableStatus
  remark?: string
}

export interface DiningRecipe {
  id: Id
  recipeName: string
  mealType?: DiningMealType
  dishIds?: string
  dishNames: string
  planDate?: string
  suitableCrowd?: string
  status?: DiningEnableStatus
  remark?: string
}

export interface DiningPrepZone {
  id: Id
  zoneCode: string
  zoneName: string
  kitchenArea?: string
  capacity?: number
  managerName?: string
  status?: DiningEnableStatus
  remark?: string
}

export interface DiningDeliveryArea {
  id: Id
  areaCode: string
  areaName: string
  buildingName?: string
  floorNo?: string
  roomScope?: string
  managerName?: string
  status?: DiningEnableStatus
  remark?: string
}

export interface DiningMealOrder {
  id: Id
  orderNo: string
  elderId?: Id
  elderName?: string
  orderDate: string
  mealType: DiningMealType
  dishIds?: string
  dishNames: string
  totalAmount: number
  prepZoneId?: Id
  prepZoneName?: string
  deliveryAreaId?: Id
  deliveryAreaName?: string
  overrideId?: Id
  status?: DiningOrderStatus
  remark?: string
}

export interface DiningDeliveryRecord {
  id: Id
  mealOrderId: Id
  orderNo: string
  deliveryAreaId?: Id
  deliveryAreaName?: string
  deliveredBy?: number
  deliveredByName?: string
  deliveredAt?: string
  status?: DiningDeliveryStatus
  failureReason?: string
  redispatchStatus?: 'NONE' | 'REDISPATCHED'
  redispatchAt?: string
  redispatchByName?: string
  redispatchRemark?: string
  remark?: string
}

export interface DiningStatsMealTypeItem {
  mealType: DiningMealType
  orderCount: number
}

export interface DiningStatsSummary {
  totalOrders: number
  totalAmount: number
  deliveredOrders: number
  deliveryRate: number
  mealTypeStats: DiningStatsMealTypeItem[]
}

export interface DiningRiskReasonItem {
  reasonType: string
  reasonCode: string
  reasonDetail: string
}

export interface DiningRiskCheckResponse {
  allowed: boolean
  canOverride: boolean
  elderId: Id
  elderName: string
  dishNames: string
  blockedDishNames: string[]
  reasons: DiningRiskReasonItem[]
  message: string
}

export interface DiningRiskOverride {
  id: Id
  elderId: Id
  elderName?: string
  dishNames: string
  applyReason: string
  reviewStatus: string
  reviewRemark?: string
  effectiveUntil?: string
}

export interface DiningRiskThresholdConfig {
  id: Id
  metricCode: string
  metricName: string
  thresholdValue: number
  enabled: number
  remark?: string
}
