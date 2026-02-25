import request, { fetchPage } from '../utils/request'
import type {
  BaseConfigGroupOption,
  BaseConfigImportItem,
  BaseConfigImportResult,
  BaseConfigItem,
  BaseConfigItemPayload
} from '../types'

export function getBaseConfigGroups() {
  return request.get<BaseConfigGroupOption[]>('/api/base-config/items/groups')
}

export function getBaseConfigItemPage(params: {
  pageNo?: number
  pageSize?: number
  configGroup?: string
  keyword?: string
  status?: number
}) {
  return fetchPage<BaseConfigItem>('/api/base-config/items/page', params)
}

export function getBaseConfigItemList(params: { configGroup?: string; status?: number }) {
  return request.get<BaseConfigItem[]>('/api/base-config/items/list', { params })
}

export function createBaseConfigItem(data: BaseConfigItemPayload) {
  return request.post<BaseConfigItem>('/api/base-config/items', data)
}

export function updateBaseConfigItem(id: number, data: BaseConfigItemPayload) {
  return request.put<BaseConfigItem>(`/api/base-config/items/${id}`, data)
}

export function deleteBaseConfigItem(id: number) {
  return request.delete<void>(`/api/base-config/items/${id}`)
}

export function batchUpdateBaseConfigStatus(ids: number[], status: number) {
  return request.put<number>('/api/base-config/items/batch/status', { ids, status })
}

export function exportBaseConfigItems(params: {
  configGroup?: string
  keyword?: string
  status?: number
}) {
  return request.get<Blob>('/api/base-config/items/export', { params, responseType: 'blob' })
}

export function importBaseConfigItems(data: { configGroup: string; items: BaseConfigImportItem[] }) {
  return request.post<BaseConfigImportResult>('/api/base-config/items/import', data)
}

export function previewImportBaseConfigItems(data: { configGroup: string; items: BaseConfigImportItem[] }) {
  return request.post<BaseConfigImportResult>('/api/base-config/items/import/preview', data)
}

export function downloadBaseConfigImportTemplate() {
  return request.get<Blob>('/api/base-config/items/import-template', { responseType: 'blob' })
}
