import type { MaterialEnableStatus, MaterialOrderStatus } from '../utils/materialStatus'
import type { Id } from './common'

export interface MaterialWarehouseItem {
  id: Id
  warehouseCode: string
  warehouseName: string
  managerName?: string
  managerPhone?: string
  address?: string
  status?: MaterialEnableStatus
  remark?: string
  createTime?: string
}

export interface MaterialSupplierItem {
  id: Id
  supplierCode: string
  supplierName: string
  contactName?: string
  contactPhone?: string
  address?: string
  status?: MaterialEnableStatus
  remark?: string
  createTime?: string
}

export interface MaterialPurchaseOrderItem {
  id?: Id
  productId: Id
  productName?: string
  businessDomain?: 'INTERNAL' | 'MALL' | 'BOTH' | string
  itemType?: 'ASSET' | 'CONSUMABLE' | 'FOOD' | 'SERVICE' | string
  quantity: number
  unitPrice: number
  amount?: number
}

export interface MaterialPurchaseOrder {
  id: Id
  orderNo: string
  warehouseId?: Id
  warehouseName?: string
  supplierId?: Id
  supplierName?: string
  orderDate?: string
  status?: MaterialOrderStatus
  totalAmount?: number
  source?: string
  sourceRef?: string
  assetAmount?: number
  consumableAmount?: number
  foodAmount?: number
  serviceAmount?: number
  remark?: string
  createTime?: string
  items?: MaterialPurchaseOrderItem[]
}

export interface MaterialTransferItem {
  id?: Id
  productId: Id
  productName?: string
  quantity: number
}

export interface MaterialTransferOrder {
  id: Id
  transferNo: string
  fromWarehouseId: Id
  fromWarehouseName?: string
  toWarehouseId: Id
  toWarehouseName?: string
  status?: MaterialOrderStatus
  remark?: string
  createTime?: string
  items?: MaterialTransferItem[]
}

export interface MaterialStockAmountItem {
  dimension?: 'PRODUCT' | 'WAREHOUSE' | 'CATEGORY'
  productId?: Id
  productCode?: string
  productName?: string
  warehouseId?: Id
  warehouseName?: string
  category?: string
  totalQuantity: number
  totalAmount: number
}
