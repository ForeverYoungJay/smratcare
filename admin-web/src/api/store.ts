import request, { fetchPage } from '../utils/request'
import type {
  ProductItem,
  OrderItem,
  ProductTagItem,
  ProductCategoryItem,
  DiseaseItem,
  ForbiddenTagGroup,
  StoreOrderPreviewRequest,
  StoreOrderSubmitRequest,
  OrderCancelRequest,
  OrderRefundRequest,
  PointsAccountItem,
  PointsLogItem,
  PointsAdjustRequest
} from '../types'

export function getProductPage(params: any) {
  return fetchPage<ProductItem>('/api/store/product/page', params)
}

export function createProduct(data: Partial<ProductItem>) {
  return request.post<void>('/api/store/product', data)
}

export function updateProduct(id: number, data: Partial<ProductItem>) {
  return request.put<void>(`/api/store/product/${id}`, data)
}

export function getOrderPage(params: any) {
  return fetchPage<OrderItem>('/api/store/order/page', params)
}

export function getOrderDetail(id: number) {
  return request.get<OrderItem>(`/api/store/order/${id}`)
}

export function previewOrder(data: StoreOrderPreviewRequest) {
  return request.post<any>('/api/store/order/preview', data)
}

export function submitOrder(data: StoreOrderSubmitRequest) {
  return request.post<any>('/api/store/order/submit', data)
}

export function cancelOrder(data: OrderCancelRequest) {
  return request.post<void>('/api/store/order/cancel', data)
}

export function refundOrder(data: OrderRefundRequest) {
  return request.post<void>('/api/store/order/refund', data)
}

export function fulfillOrder(orderId: number) {
  return request.post<void>('/api/store/order/fulfill', { orderId })
}

export async function getDiseaseList() {
  const res = await fetchPage<DiseaseItem>('/api/admin/disease', { pageNo: 1, pageSize: 200 })
  return res.list
}

export function createDisease(data: Partial<DiseaseItem>) {
  return request.post<void>('/api/admin/disease', data)
}

export function updateDisease(id: number, data: Partial<DiseaseItem>) {
  return request.put<void>(`/api/admin/disease/${id}`, data)
}

export function deleteDisease(id: number) {
  return request.delete<void>(`/api/admin/disease/${id}`)
}

export async function getProductTagList() {
  const res = await fetchPage<ProductTagItem>('/api/admin/product-tag', { pageNo: 1, pageSize: 500 })
  return res.list
}

export async function getProductCategoryList(params?: Record<string, any>) {
  const res = await fetchPage<ProductCategoryItem>('/api/admin/product-category', { pageNo: 1, pageSize: 500, ...(params || {}) })
  return res.list
}

export function createProductCategory(data: Partial<ProductCategoryItem>) {
  return request.post<void>('/api/admin/product-category', data)
}

export function updateProductCategory(id: number, data: Partial<ProductCategoryItem>) {
  return request.put<void>(`/api/admin/product-category/${id}`, data)
}

export function deleteProductCategory(id: number) {
  return request.delete<void>(`/api/admin/product-category/${id}`)
}

export function createProductTag(data: Partial<ProductTagItem>) {
  return request.post<void>('/api/admin/product-tag', data)
}

export function updateProductTag(id: number, data: Partial<ProductTagItem>) {
  return request.put<void>(`/api/admin/product-tag/${id}`, data)
}

export function deleteProductTag(id: number) {
  return request.delete<void>(`/api/admin/product-tag/${id}`)
}

export function getForbiddenTags(diseaseId: number) {
  return request.get<ForbiddenTagGroup>(`/api/admin/disease/${diseaseId}/forbidden-tags`)
}

export function updateForbiddenTags(diseaseId: number, tagIds: number[]) {
  return request.put<void>(`/api/admin/disease/${diseaseId}/forbidden-tags`, { tagIds })
}

export function getPointsAccountPage(params: any) {
  return fetchPage<PointsAccountItem>('/api/store/points/page', params)
}

export function getPointsLogPage(params: any) {
  return fetchPage<PointsLogItem>('/api/store/points/log/page', params)
}

export function adjustPoints(data: PointsAdjustRequest) {
  return request.post<void>('/api/store/points/adjust', data)
}
