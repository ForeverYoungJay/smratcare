import request, { fetchPage } from '../utils/request'
import type {
  InventoryAlertItem,
  InventoryExpiryAlertItem,
  InventoryAdjustRequest,
  InventoryBatchItem,
  InventoryLogItem,
  InventoryAdjustmentItem,
  InventoryInboundRequest,
  InventoryOutboundRequest
} from '../types'

export function getInventoryBatchPage(params: any) {
  return fetchPage<InventoryBatchItem>('/api/inventory/batch/page', params)
}

export function getInventoryLogPage(params: any) {
  return fetchPage<InventoryLogItem>('/api/inventory/log/page', params)
}

export function getInventoryInboundPage(params: any) {
  return fetchPage<InventoryLogItem>('/api/inventory/inbound/page', params)
}

export function getInventoryOutboundPage(params: any) {
  return fetchPage<InventoryLogItem>('/api/inventory/outbound/page', params)
}

export function getInventoryAdjustmentPage(params: any) {
  return fetchPage<InventoryAdjustmentItem>('/api/inventory/adjustment/page', params)
}

export function createInbound(data: InventoryInboundRequest) {
  return request.post<void>('/api/inventory/inbound', data)
}

export function adjustInventory(data: InventoryAdjustRequest) {
  return request.post<void>('/api/inventory/adjust', data)
}

export function createOutbound(data: InventoryOutboundRequest) {
  return request.post<void>('/api/inventory/outbound', data)
}

export function getInventoryAlerts() {
  return request.get<InventoryAlertItem[]>('/api/inventory/alerts')
}

export function getInventoryExpiryAlerts() {
  return request.get<InventoryExpiryAlertItem[]>('/api/inventory/expiry-alerts')
}
