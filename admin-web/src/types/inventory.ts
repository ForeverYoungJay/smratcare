export interface InventoryBatchItem {
  id: number
  productId: number
  productName?: string
  safetyStock?: number
  batchNo?: string
  quantity: number
  costPrice?: number
  expireDate?: string
  warehouseLocation?: string
  createTime?: string
}

export interface InventoryLogItem {
  id: number
  productId: number
  productName?: string
  batchId?: number
  batchNo?: string
  changeType: 'IN' | 'OUT' | 'ADJUST'
  changeQty: number
  refOrderId?: number
  refAdjustmentId?: number
  outType?: 'SALE' | 'CONSUME'
  remark?: string
  createTime?: string
}

export interface InventoryAlertItem {
  productId: number
  productName?: string
  safetyStock: number
  currentStock: number
}

export interface InventoryExpiryAlertItem {
  productId: number
  productName?: string
  batchId: number
  batchNo?: string
  quantity: number
  expireDate?: string
  daysToExpire?: number
}

export interface InventoryAdjustRequest {
  productId: number
  batchId?: number
  adjustType: 'GAIN' | 'LOSS'
  adjustQty: number
  reason?: string
}

export interface InventoryInboundRequest {
  productId: number | string
  batchNo?: string
  quantity: number
  costPrice?: number
  expireDate?: string
  warehouseLocation?: string
  remark?: string
}

export interface InventoryOutboundRequest {
  productId: number | string
  batchId?: number
  quantity: number
  reason?: string
}

export interface InventoryAdjustmentItem {
  id: number
  productId: number
  productName?: string
  batchId?: number
  adjustType: 'GAIN' | 'LOSS'
  adjustQty: number
  reason?: string
  operatorStaffId?: number
  createTime?: string
}
