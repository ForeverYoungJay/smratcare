import request from '../utils/request'
import type { ApiResult, PageResult, RoomItem, BedItem } from '../types/api'

export function getRoomList() {
  return request.get<any>('/api/room/list').then((res) => normalizeList<RoomItem>(res))
}

export function getBedList() {
  return request.get<any>('/api/bed/list').then((res) => normalizeList<BedItem>(res))
}

export function getBedMap() {
  return request.get<any>('/api/bed/list').then((res) => normalizeList<BedItem>(res))
}

export function getRoomPage(params: any) {
  return request.get<ApiResult<PageResult<RoomItem>>>('/api/room', { params })
}

export function getBedPage(params: any) {
  return request.get<ApiResult<PageResult<BedItem>>>('/api/bed', { params })
}

export function createRoom(data: Partial<RoomItem>) {
  return request.post<ApiResult<void>>('/api/room', data)
}

export function updateRoom(id: number, data: Partial<RoomItem>) {
  return request.put<ApiResult<void>>(`/api/room/${id}`, data)
}

export function deleteRoom(id: number) {
  return request.delete<ApiResult<void>>(`/api/room/${id}`)
}

export function createBed(data: Partial<BedItem>) {
  return request.post<ApiResult<void>>('/api/bed', data)
}

export function updateBed(id: number, data: Partial<BedItem>) {
  return request.put<ApiResult<void>>(`/api/bed/${id}`, data)
}

export function deleteBed(id: number) {
  return request.delete<ApiResult<void>>(`/api/bed/${id}`)
}

function normalizeList<T>(res: any): ApiResult<T[]> {
  if (Array.isArray(res)) {
    return { code: 0, message: 'OK', data: res }
  }
  if (res?.data && Array.isArray(res.data)) {
    return { code: 0, message: 'OK', data: res.data }
  }
  if (res?.data?.records && Array.isArray(res.data.records)) {
    return { code: 0, message: 'OK', data: res.data.records }
  }
  return { code: 0, message: 'OK', data: [] }
}
