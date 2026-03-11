import type { Id } from './common'

export interface ProductItem {
  id: number
  idStr?: string
  productCode?: string
  productName: string
  category?: string
  businessDomain?: 'INTERNAL' | 'MALL' | 'BOTH' | string
  itemType?: 'ASSET' | 'CONSUMABLE' | 'FOOD' | 'SERVICE' | string
  mallEnabled?: number
  price?: number
  pointsPrice: number
  safetyStock?: number
  currentStock?: number
  status: number
  tags?: string[]
}

export interface OrderItem {
  id: Id
  orderNo?: string
  elderId: Id
  elderName?: string
  totalAmount?: number
  payableAmount?: number
  pointsUsed?: number
  orderStatus?: number
  payStatus?: number
  payTime?: string
  items?: OrderLineItem[]
  riskReasons?: RiskReason[]
  fifoLogs?: FifoLogItem[]
}

export interface OrderLineItem {
  productId: number
  productName?: string
  quantity: number
  unitPrice?: number
  amount?: number
}

export interface RiskReason {
  diseaseName?: string
  tagCode?: string
  tagName?: string
}

export interface FifoLogItem {
  batchNo?: string
  quantity?: number
  expireDate?: string
}

export interface StoreOrderPreviewRequest {
  elderId: Id
  productId: number
  qty: number
}

export interface StoreOrderSubmitRequest {
  elderId: Id
  productId: number
  qty: number
}

export interface OrderCancelRequest {
  orderId: number
}

export interface OrderRefundRequest {
  orderId: number
  reason?: string
}

export interface ProductTagItem {
  id: number
  tagCode: string
  tagName: string
  tagType?: string
  status?: number
}

export interface ProductCategoryItem {
  id: number
  categoryCode: string
  categoryName: string
  status?: number
  remark?: string
}

export interface DiseaseItem {
  id: number
  diseaseName: string
  diseaseCode?: string
  status?: number
}

export interface ForbiddenTagGroup {
  tagType: string
  selected: ProductTagItem[]
  unselected: ProductTagItem[]
}

export interface PointsAccountItem {
  id: Id
  orgId?: number
  elderId: Id
  elderName?: string
  pointsBalance?: number
  status?: number
  updateTime?: string
}

export interface PointsLogItem {
  id: Id
  elderId: Id
  elderName?: string
  changeType?: string
  changePoints?: number
  balanceAfter?: number
  refOrderId?: Id
  remark?: string
  createTime?: string
}

export interface PointsAdjustRequest {
  elderId: Id
  points: number
  direction: 'CREDIT' | 'DEBIT'
  remark?: string
}
