export interface MaterialWarehouseItem {
  id: number
  warehouseCode: string
  warehouseName: string
  managerName?: string
  managerPhone?: string
  address?: string
  status?: number
  remark?: string
  createTime?: string
}

export interface MaterialSupplierItem {
  id: number
  supplierCode: string
  supplierName: string
  contactName?: string
  contactPhone?: string
  address?: string
  status?: number
  remark?: string
  createTime?: string
}

export interface MaterialPurchaseOrderItem {
  id?: number
  productId: number
  productName?: string
  quantity: number
  unitPrice: number
  amount?: number
}

export interface MaterialPurchaseOrder {
  id: number
  orderNo: string
  warehouseId?: number
  warehouseName?: string
  supplierId?: number
  supplierName?: string
  orderDate?: string
  status?: string
  totalAmount?: number
  remark?: string
  createTime?: string
  items?: MaterialPurchaseOrderItem[]
}

export interface MaterialTransferItem {
  id?: number
  productId: number
  productName?: string
  quantity: number
}

export interface MaterialTransferOrder {
  id: number
  transferNo: string
  fromWarehouseId: number
  fromWarehouseName?: string
  toWarehouseId: number
  toWarehouseName?: string
  status?: string
  remark?: string
  createTime?: string
  items?: MaterialTransferItem[]
}

export interface MaterialStockAmountItem {
  dimension?: 'PRODUCT' | 'WAREHOUSE' | 'CATEGORY'
  productId?: number
  productCode?: string
  productName?: string
  warehouseId?: number
  warehouseName?: string
  category?: string
  totalQuantity: number
  totalAmount: number
}
