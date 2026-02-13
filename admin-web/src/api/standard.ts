import request, { fetchPage } from '../utils/request'
import type { ServiceItem, CarePackage, CarePackageItem, ElderPackage, GenerateTaskRequest } from '../types'

export function getServiceItemPage(params: any) {
  return fetchPage<ServiceItem>('/api/standard/items/page', params)
}

export function listServiceItems(params?: { enabled?: number }) {
  return request.get<ServiceItem[]>('/api/standard/items/list', { params })
}

export function createServiceItem(data: Partial<ServiceItem>) {
  return request.post<void>('/api/standard/items', data)
}

export function updateServiceItem(id: number, data: Partial<ServiceItem>) {
  return request.put<void>(`/api/standard/items/${id}`, data)
}

export function deleteServiceItem(id: number) {
  return request.delete<void>(`/api/standard/items/${id}`)
}

export function getCarePackagePage(params: any) {
  return fetchPage<CarePackage>('/api/standard/packages/page', params)
}

export function listCarePackages(params?: { enabled?: number }) {
  return request.get<CarePackage[]>('/api/standard/packages/list', { params })
}

export function createCarePackage(data: Partial<CarePackage>) {
  return request.post<void>('/api/standard/packages', data)
}

export function updateCarePackage(id: number, data: Partial<CarePackage>) {
  return request.put<void>(`/api/standard/packages/${id}`, data)
}

export function deleteCarePackage(id: number) {
  return request.delete<void>(`/api/standard/packages/${id}`)
}

export function getPackageItemPage(params: any) {
  return fetchPage<CarePackageItem>('/api/standard/package-items/page', params)
}

export function listPackageItems(params?: { packageId?: number }) {
  return request.get<CarePackageItem[]>('/api/standard/package-items/list', { params })
}

export function createPackageItem(data: Partial<CarePackageItem>) {
  return request.post<void>('/api/standard/package-items', data)
}

export function updatePackageItem(id: number, data: Partial<CarePackageItem>) {
  return request.put<void>(`/api/standard/package-items/${id}`, data)
}

export function deletePackageItem(id: number) {
  return request.delete<void>(`/api/standard/package-items/${id}`)
}

export function getElderPackagePage(params: any) {
  return fetchPage<ElderPackage>('/api/standard/elder-packages/page', params)
}

export function listElderPackages(params?: { elderId?: number }) {
  return request.get<ElderPackage[]>('/api/standard/elder-packages/list', { params })
}

export function createElderPackage(data: Partial<ElderPackage>) {
  return request.post<void>('/api/standard/elder-packages', data)
}

export function updateElderPackage(id: number, data: Partial<ElderPackage>) {
  return request.put<void>(`/api/standard/elder-packages/${id}`, data)
}

export function deleteElderPackage(id: number) {
  return request.delete<void>(`/api/standard/elder-packages/${id}`)
}

export function generateTasksByPackage(data: GenerateTaskRequest) {
  return request.post<number>('/api/standard/tasks/generate', data)
}
