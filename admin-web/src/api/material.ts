import request, { fetchPage } from '../utils/request'
import type {
  MaterialWarehouseItem,
  MaterialSupplierItem,
  MaterialPurchaseOrder,
  MaterialTransferOrder,
  MaterialStockAmountItem,
  MaterialPurchaseOrderItem,
  MaterialTransferItem
} from '../types'
import type { MaterialOrderStatus } from '../utils/materialStatus'

export function getWarehousePage(params: {
  pageNo?: number
  pageSize?: number
  keyword?: string
  enabledOnly?: boolean
}) {
  return fetchPage<MaterialWarehouseItem>('/api/material/warehouse/page', params)
}

export function createWarehouse(data: Partial<MaterialWarehouseItem>) {
  return request.post<void>('/api/material/warehouse', data)
}

export function updateWarehouse(id: number, data: Partial<MaterialWarehouseItem>) {
  return request.put<void>(`/api/material/warehouse/${id}`, data)
}

export function deleteWarehouse(id: number) {
  return request.delete<void>(`/api/material/warehouse/${id}`)
}

export function getSupplierPage(params: {
  pageNo?: number
  pageSize?: number
  keyword?: string
  enabledOnly?: boolean
}) {
  return fetchPage<MaterialSupplierItem>('/api/material/supplier/page', params)
}

export function createSupplier(data: Partial<MaterialSupplierItem>) {
  return request.post<void>('/api/material/supplier', data)
}

export function updateSupplier(id: number, data: Partial<MaterialSupplierItem>) {
  return request.put<void>(`/api/material/supplier/${id}`, data)
}

export function deleteSupplier(id: number) {
  return request.delete<void>(`/api/material/supplier/${id}`)
}

export function getPurchasePage(params: {
  pageNo?: number
  pageSize?: number
  warehouseId?: number
  supplierId?: number
  status?: MaterialOrderStatus
  keyword?: string
}) {
  return fetchPage<MaterialPurchaseOrder>('/api/material/purchase/page', params)
}

export function getPurchaseDetail(id: number) {
  return request.get<MaterialPurchaseOrder>(`/api/material/purchase/${id}`)
}

export function createPurchase(data: {
  warehouseId?: number
  supplierId?: number
  orderDate?: string
  remark?: string
  items: MaterialPurchaseOrderItem[]
}) {
  return request.post<void>('/api/material/purchase', data)
}

export function updatePurchase(
  id: number,
  data: {
    warehouseId?: number
    supplierId?: number
    orderDate?: string
    remark?: string
    items: MaterialPurchaseOrderItem[]
  }
) {
  return request.put<void>(`/api/material/purchase/${id}`, data)
}

export function approvePurchase(id: number) {
  return request.post<void>(`/api/material/purchase/${id}/approve`)
}

export function completePurchase(id: number) {
  return request.post<void>(`/api/material/purchase/${id}/complete`)
}

export function cancelPurchase(id: number) {
  return request.post<void>(`/api/material/purchase/${id}/cancel`)
}

export function getTransferPage(params: {
  pageNo?: number
  pageSize?: number
  status?: MaterialOrderStatus
  keyword?: string
}) {
  return fetchPage<MaterialTransferOrder>('/api/material/transfer/page', params)
}

export function getTransferDetail(id: number) {
  return request.get<MaterialTransferOrder>(`/api/material/transfer/${id}`)
}

export function createTransfer(data: {
  fromWarehouseId: number
  toWarehouseId: number
  remark?: string
  items: MaterialTransferItem[]
}) {
  return request.post<void>('/api/material/transfer', data)
}

export function updateTransfer(
  id: number,
  data: {
    fromWarehouseId: number
    toWarehouseId: number
    remark?: string
    items: MaterialTransferItem[]
  }
) {
  return request.put<void>(`/api/material/transfer/${id}`, data)
}

export function completeTransfer(id: number) {
  return request.post<void>(`/api/material/transfer/${id}/complete`)
}

export function cancelTransfer(id: number) {
  return request.post<void>(`/api/material/transfer/${id}/cancel`)
}

export function getStockAmount(params?: {
  dimension?: 'PRODUCT' | 'WAREHOUSE' | 'CATEGORY'
  warehouseId?: number
  category?: string
}) {
  return request.get<MaterialStockAmountItem[]>('/api/material/stock/amount', { params })
}
