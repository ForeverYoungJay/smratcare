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
  status?: 0 | 1 | 2 | 3
  bedQrCode?: string
  elderId?: number
  elderName?: string
  careLevel?: string
  roomNo?: string
  building?: string
  floorNo?: string
  roomQrCode?: string
}

export interface BuildingItem {
  id: number
  name: string
  code?: string
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
  roomNo?: string
  bedNo?: string
  qrCode?: string
  children?: AssetTreeNode[]
}
