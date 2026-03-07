export interface InventoryBatchItem {
  id: number
  productId: number
  productName?: string
  category?: string
  businessDomain?: 'INTERNAL' | 'MALL' | 'BOTH' | string
  itemType?: 'ASSET' | 'CONSUMABLE' | 'FOOD' | 'SERVICE' | string
  mallEnabled?: number
  safetyStock?: number
  warehouseId?: number
  warehouseName?: string
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
  warehouseId?: number
  batchNo?: string
  outboundNo?: string
  changeType: 'IN' | 'OUT' | 'ADJUST'
  changeQty: number
  refOrderId?: number
  refAdjustmentId?: number
  outType?: 'SALE' | 'CONSUME'
  receiverName?: string
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
  warehouseId?: number
  batchId?: number
  inventoryType?: string
  adjustType: 'GAIN' | 'LOSS'
  adjustQty: number
  reason?: string
}

export interface InventoryInboundRequest {
  productId: number | string
  warehouseId?: number
  batchNo?: string
  quantity: number
  costPrice?: number
  expireDate?: string
  warehouseLocation?: string
  remark?: string
}

export interface InventoryOutboundRequest {
  productId: number | string
  warehouseId?: number
  batchId?: number
  quantity: number
  receiverName?: string
  outboundNo?: string
  reason?: string
}

export interface InventoryOutboundSheetItemRequest {
  productId: number | string
  warehouseId?: number
  batchId?: number
  quantity: number
  reason?: string
}

export interface InventoryOutboundSheetCreateRequest {
  outboundNo?: string
  receiverName: string
  elderId?: number
  contractNo?: string
  applyDept?: string
  remark?: string
  items: InventoryOutboundSheetItemRequest[]
}

export interface InventoryOutboundSheetItem {
  id: number
  productId: number
  productName?: string
  productCode?: string
  unit?: string
  warehouseId?: number
  batchId?: number
  batchNo?: string
  quantity: number
  reason?: string
}

export interface InventoryOutboundSheet {
  id: number
  outboundNo: string
  receiverName: string
  elderId?: number
  contractNo?: string
  applyDept?: string
  operatorStaffId?: number
  operatorName?: string
  status: 'DRAFT' | 'CONFIRMED' | string
  remark?: string
  confirmStaffId?: number
  confirmTime?: string
  createTime?: string
  items: InventoryOutboundSheetItem[]
}

export interface InventoryAdjustmentItem {
  id: number
  productId: number
  productName?: string
  category?: string
  batchId?: number
  warehouseId?: number
  warehouseName?: string
  inventoryType?: string
  adjustType: 'GAIN' | 'LOSS'
  adjustQty: number
  reason?: string
  operatorStaffId?: number
  createTime?: string
}

export interface InventoryAdjustmentDiffItem {
  productId: number
  productName?: string
  category?: string
  inventoryType?: string
  warehouseId?: number
  warehouseName?: string
  gainQty: number
  lossQty: number
  diffQty: number
}
