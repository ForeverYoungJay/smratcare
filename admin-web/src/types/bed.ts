export interface RoomItem {
  id: number
  buildingId?: number
  floorId?: number
  roomNo: string
  building?: string
  floorNo?: string
  roomType?: string
  capacity?: number
  status?: number
  roomQrCode?: string
}

export interface BedItem {
  id: number
  bedNo: string
  roomId: number
  bedType?: string
  status?: 0 | 1 | 2 | 3
  bedQrCode?: string
  elderId?: number
  elderName?: string
  careLevel?: string
  roomNo?: string
  building?: string
  floorNo?: string
  roomType?: string
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
  id: number
  name: string
  code?: string
  areaCode?: string
  areaName?: string
  status?: number
  sortNo?: number
  remark?: string
}

export interface FloorItem {
  id: number
  buildingId: number
  floorNo: string
  name?: string
  status?: number
  sortNo?: number
}

export interface AssetTreeNode {
  id: number
  type: 'BUILDING' | 'FLOOR' | 'ROOM' | 'BED'
  name: string
  status?: number
  buildingId?: number
  floorId?: number
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
