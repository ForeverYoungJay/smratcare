import type { Id } from './common'

export interface InventoryBatchItem {
  id: Id
  productId: Id
  productName?: string
  category?: string
  businessDomain?: 'INTERNAL' | 'MALL' | 'BOTH' | string
  itemType?: 'ASSET' | 'CONSUMABLE' | 'FOOD' | 'SERVICE' | string
  mallEnabled?: number
  safetyStock?: number
  warehouseId?: Id
  warehouseName?: string
  batchNo?: string
  quantity: number
  costPrice?: number
  expireDate?: string
  warehouseLocation?: string
  createTime?: string
}

export interface InventoryLogItem {
  id: Id
  productId: Id
  productName?: string
  batchId?: Id
  warehouseId?: Id
  batchNo?: string
  outboundNo?: string
  changeType: 'IN' | 'OUT' | 'ADJUST'
  changeQty: number
  refOrderId?: Id
  refAdjustmentId?: Id
  outType?: 'SALE' | 'CONSUME'
  receiverName?: string
  remark?: string
  createTime?: string
}

export interface InventoryAlertItem {
  productId: Id
  productName?: string
  safetyStock: number
  currentStock: number
}

export interface InventoryExpiryAlertItem {
  productId: Id
  productName?: string
  batchId: Id
  batchNo?: string
  quantity: number
  expireDate?: string
  daysToExpire?: number
}

export interface InventoryAdjustRequest {
  productId: Id
  warehouseId?: Id
  batchId?: Id
  inventoryType?: string
  adjustType: 'GAIN' | 'LOSS'
  adjustQty: number
  reason?: string
}

export interface InventoryInboundRequest {
  productId: Id
  warehouseId?: Id
  batchNo?: string
  quantity: number
  costPrice?: number
  expireDate?: string
  warehouseLocation?: string
  remark?: string
}

export interface InventoryOutboundRequest {
  productId: Id
  warehouseId?: Id
  batchId?: Id
  quantity: number
  receiverName?: string
  outboundNo?: string
  reason?: string
}

export interface InventoryOutboundSheetItemRequest {
  productId: Id
  warehouseId?: Id
  batchId?: Id
  quantity: number
  reason?: string
}

export interface InventoryOutboundSheetCreateRequest {
  outboundNo?: string
  receiverName: string
  elderId?: Id
  contractNo?: string
  applyDept?: string
  remark?: string
  items: InventoryOutboundSheetItemRequest[]
}

export interface InventoryOutboundSheetPrefill {
  elderId?: Id
  receiverName?: string
  contractNo?: string
  applyDept?: string
}

export interface InventoryOutboundSheetItem {
  id: Id
  productId: Id
  productName?: string
  productCode?: string
  unit?: string
  warehouseId?: Id
  batchId?: Id
  batchNo?: string
  quantity: number
  reason?: string
}

export interface InventoryOutboundSheet {
  id: Id
  outboundNo: string
  receiverName: string
  elderId?: Id
  contractNo?: string
  applyDept?: string
  operatorStaffId?: Id
  operatorName?: string
  status: 'DRAFT' | 'CONFIRMED' | string
  remark?: string
  confirmStaffId?: Id
  confirmTime?: string
  createTime?: string
  items: InventoryOutboundSheetItem[]
}

export interface InventoryAdjustmentItem {
  id: Id
  productId: Id
  productName?: string
  category?: string
  batchId?: Id
  warehouseId?: Id
  warehouseName?: string
  inventoryType?: string
  adjustType: 'GAIN' | 'LOSS'
  adjustQty: number
  reason?: string
  operatorStaffId?: Id
  createTime?: string
}

export interface InventoryAdjustmentDiffItem {
  productId: Id
  productName?: string
  category?: string
  inventoryType?: string
  warehouseId?: Id
  warehouseName?: string
  gainQty: number
  lossQty: number
  diffQty: number
}
