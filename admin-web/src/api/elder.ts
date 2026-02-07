import request from '../utils/request'
import type { ApiResult, PageResult, ElderItem } from '../types/api'

export function getElderPage(params: any) {
  return request.get<ApiResult<PageResult<ElderItem>>>('/api/elder/page', { params })
}

export function getElderDetail(id: number) {
  return request.get<ApiResult<ElderItem>>(`/api/elder/${id}`)
}

export function createElder(data: Partial<ElderItem>) {
  return request.post<ApiResult<void>>('/api/elder', data)
}

export function updateElder(id: number, data: Partial<ElderItem>) {
  return request.put<ApiResult<void>>(`/api/elder/${id}`, data)
}

export function assignBed(elderId: number, bedId: number, startDate: string) {
  return request.post<ApiResult<void>>(`/api/elder/${elderId}/assignBed`, { bedId, startDate })
}

export function unbindBed(elderId: number, endDate?: string, reason?: string) {
  return request.post<ApiResult<void>>(`/api/elder/${elderId}/unbindBed`, null, { params: { endDate, reason } })
}
