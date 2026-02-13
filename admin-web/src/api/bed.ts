import request, { fetchPage } from '../utils/request'
import type { RoomItem, BedItem, BuildingItem, FloorItem, AssetTreeNode } from '../types'

export function getRoomPage(params: any) {
  return fetchPage<RoomItem>('/api/room/page', params)
}

export function getBedPage(params: any) {
  return fetchPage<BedItem>('/api/bed/page', params)
}

export function getRoomList() {
  return request.get<RoomItem[]>('/api/room/list')
}

export function getBedList() {
  return request.get<BedItem[]>('/api/bed/list')
}

export function getBedMap() {
  return request.get<BedItem[]>('/api/bed/map')
}

export function getBuildingPage(params: any) {
  return fetchPage<BuildingItem>('/api/asset/buildings/page', params)
}

export function getBuildingList() {
  return request.get<BuildingItem[]>('/api/asset/buildings/list')
}

export function createBuilding(data: Partial<BuildingItem>) {
  return request.post<void>('/api/asset/buildings', data)
}

export function updateBuilding(id: number, data: Partial<BuildingItem>) {
  return request.put<void>(`/api/asset/buildings/${id}`, data)
}

export function deleteBuilding(id: number) {
  return request.delete<void>(`/api/asset/buildings/${id}`)
}

export function getFloorPage(params: any) {
  return fetchPage<FloorItem>('/api/asset/floors/page', params)
}

export function getFloorList(params?: any) {
  return request.get<FloorItem[]>('/api/asset/floors/list', { params })
}

export function createFloor(data: Partial<FloorItem>) {
  return request.post<void>('/api/asset/floors', data)
}

export function updateFloor(id: number, data: Partial<FloorItem>) {
  return request.put<void>(`/api/asset/floors/${id}`, data)
}

export function deleteFloor(id: number) {
  return request.delete<void>(`/api/asset/floors/${id}`)
}

export function getAssetTree() {
  return request.get<AssetTreeNode[]>('/api/asset/tree')
}

export function createRoom(data: Partial<RoomItem>) {
  return request.post<void>('/api/room', data)
}

export function updateRoom(id: number, data: Partial<RoomItem>) {
  return request.put<void>(`/api/room/${id}`, data)
}

export function deleteRoom(id: number) {
  return request.delete<void>(`/api/room/${id}`)
}

export function createBed(data: Partial<BedItem>) {
  return request.post<void>('/api/bed', data)
}

export function updateBed(id: number, data: Partial<BedItem>) {
  return request.put<void>(`/api/bed/${id}`, data)
}

export function deleteBed(id: number) {
  return request.delete<void>(`/api/bed/${id}`)
}
