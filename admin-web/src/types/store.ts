export interface ProductItem {
  id: number
  idStr?: string
  productCode?: string
  productName: string
  category?: string
  price?: number
  pointsPrice: number
  safetyStock?: number
  currentStock?: number
  status: number
  tags?: string[]
}

export interface OrderItem {
  id: number
  orderNo?: string
  elderId: number
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
  elderId: number
  productId: number
  qty: number
}

export interface StoreOrderSubmitRequest {
  elderId: number
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
  id: number
  orgId?: number
  elderId: number
  elderName?: string
  pointsBalance?: number
  status?: number
  updateTime?: string
}

export interface PointsLogItem {
  id: number
  elderId: number
  elderName?: string
  changeType?: string
  changePoints?: number
  balanceAfter?: number
  refOrderId?: number
  remark?: string
  createTime?: string
}

export interface PointsAdjustRequest {
  elderId: number
  points: number
  direction: 'CREDIT' | 'DEBIT'
  remark?: string
}
