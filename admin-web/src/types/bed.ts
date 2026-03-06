import type { Id } from './common'

export interface RoomItem {
  id: Id
  buildingId?: Id
  floorId?: Id
  roomNo: string
  building?: string
  floorNo?: string
  roomType?: string
  capacity?: number
  status?: number
  roomQrCode?: string
  remark?: string
}

export interface BedItem {
  id: Id
  bedNo: string
  roomId: Id
  bedType?: string
  status?: 0 | 1 | 2 | 3
  bedQrCode?: string
  elderId?: Id
  elderName?: string
  elderGender?: number
  careLevel?: string
  roomNo?: string
  building?: string
  buildingRemark?: string
  floorNo?: string
  roomType?: string
  roomRemark?: string
  areaCode?: string
  areaName?: string
  roomQrCode?: string
  riskLevel?: '' | 'HIGH' | 'MEDIUM' | 'LOW'
  riskLabel?: string
  riskSource?: string
  abnormalVital24hCount?: number
  latestAssessmentLevel?: string
  latestAssessmentDate?: string
}

export interface BuildingItem {
  id: Id
  name: string
  code?: string
  areaCode?: string
  areaName?: string
  status?: number
  sortNo?: number
  remark?: string
}

export interface FloorItem {
  id: Id
  buildingId: Id
  floorNo: string
  name?: string
  status?: number
  sortNo?: number
}

export interface AssetTreeNode {
  id: Id
  type: 'BUILDING' | 'FLOOR' | 'ROOM' | 'BED'
  name: string
  status?: number
  buildingId?: Id
  floorId?: Id
  roomNo?: string
  bedNo?: string
  qrCode?: string
  children?: AssetTreeNode[]
}

export interface ResidenceBootstrapRequest {
  buildingCount?: number
  floorsPerBuilding?: number
  roomsPerFloor?: number
  bedsPerRoom?: number
  startNo?: number
  buildingPrefix?: string
  floorPrefix?: string
  roomPrefix?: string
  bedPrefix?: string
  roomType?: string
  bedType?: string
  templateCode?: string
}

export interface ResidenceBootstrapResponse {
  buildingCount: number
  floorCount: number
  roomCount: number
  bedCount: number
}
