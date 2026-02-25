export type DiningMealType = 'BREAKFAST' | 'LUNCH' | 'DINNER' | 'SNACK' | 'UNKNOWN'
export type DiningEnableStatus = 'ENABLED' | 'DISABLED'
export type DiningOrderStatus = 'CREATED' | 'PREPARING' | 'DELIVERING' | 'DELIVERED' | 'CANCELLED'
export type DiningDeliveryStatus = 'PENDING' | 'DELIVERED' | 'FAILED'

export interface DiningDish {
  id: number
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
  id: number
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
  id: number
  zoneCode: string
  zoneName: string
  kitchenArea?: string
  capacity?: number
  managerName?: string
  status?: DiningEnableStatus
  remark?: string
}

export interface DiningDeliveryArea {
  id: number
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
  id: number
  orderNo: string
  elderId?: number
  elderName?: string
  orderDate: string
  mealType: DiningMealType
  dishIds?: string
  dishNames: string
  totalAmount: number
  prepZoneId?: number
  prepZoneName?: string
  deliveryAreaId?: number
  deliveryAreaName?: string
  overrideId?: number
  status?: DiningOrderStatus
  remark?: string
}

export interface DiningDeliveryRecord {
  id: number
  mealOrderId: number
  orderNo: string
  deliveryAreaId?: number
  deliveryAreaName?: string
  deliveredBy?: number
  deliveredByName?: string
  deliveredAt?: string
  status?: DiningDeliveryStatus
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
  elderId: number
  elderName: string
  dishNames: string
  blockedDishNames: string[]
  reasons: DiningRiskReasonItem[]
  message: string
}

export interface DiningRiskOverride {
  id: number
  elderId: number
  elderName?: string
  dishNames: string
  applyReason: string
  reviewStatus: string
  reviewRemark?: string
  effectiveUntil?: string
}

export interface DiningRiskThresholdConfig {
  id: number
  metricCode: string
  metricName: string
  thresholdValue: number
  enabled: number
  remark?: string
}
