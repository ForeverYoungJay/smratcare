import request from '../utils/request'
import type { ApiResult, PageResult, ProductItem, InventoryItem, OrderItem, InventoryAlertItem } from '../types/api'

export function getProductPage(params: any) {
  return request.get<ApiResult<PageResult<ProductItem>>>('/api/store/product/page', { params })
    .catch(() => mockProductPage())
}

export function createProduct(data: Partial<ProductItem>) {
  return request.post<ApiResult<void>>('/api/store/product', data)
}

export function updateProduct(id: number, data: Partial<ProductItem>) {
  return request.put<ApiResult<void>>(`/api/store/product/${id}`, data)
}

export function getInventoryPage(params: any) {
  return request.get<ApiResult<PageResult<InventoryItem>>>('/api/inventory/page', { params })
    .catch(() => mockInventoryPage())
}

export function getInventoryAlerts() {
  return request.get<ApiResult<InventoryAlertItem[]>>('/api/inventory/alerts')
    .catch(() => mockInventoryAlerts())
}

export function getInventoryExpiryAlerts() {
  return request.get<ApiResult<InventoryAlertItem[]>>('/api/inventory/expiry-alerts')
    .catch(() => mockInventoryExpiryAlerts())
}

export function getOrderPage(params: any) {
  return request.get<ApiResult<PageResult<OrderItem>>>('/api/store/order/page', { params })
    .catch(() => mockOrderPage())
}

export function getOrderDetail(id: number) {
  return request.get<ApiResult<OrderItem>>(`/api/store/order/${id}`)
}

function mockProductPage(): ApiResult<PageResult<ProductItem>> {
  return {
    code: 0,
    message: 'OK',
    data: {
      records: [
        { id: 6001, productName: '白砂糖', pointsPrice: 10, status: 1 },
        { id: 6003, productName: '纯牛奶', pointsPrice: 5, status: 1 }
      ],
      total: 2
    }
  }
}

function mockInventoryPage(): ApiResult<PageResult<InventoryItem>> {
  return {
    code: 0,
    message: 'OK',
    data: {
      records: [
        { id: 8001, productId: 6003, quantity: 10, expireDate: '2026-03-10' }
      ],
      total: 1
    }
  }
}

function mockInventoryAlerts(): ApiResult<InventoryAlertItem[]> {
  return {
    code: 0,
    message: 'OK',
    data: [
      { id: 1, productName: '营养奶粉', productCode: 'NP-001', alertType: 'LOW', stock: 8, safetyStock: 15, remark: '低库存' },
      { id: 2, productName: '护理手套', productCode: 'GT-102', alertType: 'LOW', stock: 30, safetyStock: 50, remark: '低库存' }
    ]
  }
}

function mockInventoryExpiryAlerts(): ApiResult<InventoryAlertItem[]> {
  return {
    code: 0,
    message: 'OK',
    data: [
      { id: 3, productName: '营养液', productCode: 'NY-008', alertType: 'EXPIRY', stock: 20, safetyStock: 10, remark: '30天内到期' }
    ]
  }
}

function mockOrderPage(): ApiResult<PageResult<OrderItem>> {
  return {
    code: 0,
    message: 'OK',
    data: {
      records: [
        { id: 9001, elderId: 4003, totalPoints: 5, status: 'PAID' }
      ],
      total: 1
    }
  }
}
