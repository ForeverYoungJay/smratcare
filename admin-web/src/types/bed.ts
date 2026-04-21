import type { Id } from './common'

export interface RoomItem {
  id: Id
  buildingId?: Id
  floorId?: Id
  roomNo: string
  building?: string
  floorNo?: string
  roomType?: string
  sortNo?: number
  capacity?: number
  status?: number
  roomQrCode?: string
  remark?: string
}

export interface RoomSortPayload {
  floorId: Id
  roomIds: Id[]
}

export interface BedItem {
  id: Id
  bedNo: string
  roomId: Id
  bedType?: string
  status?: 0 | 1 | 2 | 3
  bedQrCode?: string
  elderId?: Id
  occupancySource?: 'SELF' | 'RESERVATION' | 'MAINTENANCE' | 'FROZEN' | 'CLEANING' | string
  occupancyRefType?: 'ELDER' | 'LEAD' | 'CONTRACT' | 'SYSTEM' | string
  occupancyRefId?: Id
  occupancyRefNo?: string
  lockExpiresAt?: string
  occupancyNote?: string
  lastReleaseReason?: string
  lastReleasedAt?: string
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

export interface ResidenceGenerationFloorRule {
  floorNo?: number
  skipRoomGeneration?: boolean
  roomType?: string
  genderLimit?: string
  excludedRoomNos?: string[]
}

export interface ResidenceGenerationSpecialRoomRule {
  floorNo?: number
  roomNo?: string
  usageType?: string
  roomType?: string
  bedCount?: number
  skipBedGeneration?: boolean
  genderLimit?: string
}

export interface ResidenceBatchGenerationRequest {
  mode: 'BUILDING_ONLY' | 'FULL_INIT' | 'FLOOR_ONLY' | 'ROOM_ONLY' | 'BED_ONLY'
  strategy?: 'SKIP_EXISTING' | 'FILL_MISSING' | 'OVERWRITE_SAFE'
  buildingNames?: string[]
  buildingCount?: number
  buildingStartNo?: number
  buildingNameStyle?: string
  buildingCodePrefix?: string
  buildingId?: Id
  floorId?: Id
  roomId?: Id
  floorStartNo?: number
  floorEndNo?: number
  floorNameStyle?: string
  roomStartNo?: number
  roomEndNo?: number
  roomSeqWidth?: number
  roomType?: string
  defaultGenderLimit?: string
  defaultBedType?: string
  bedsPerRoom?: number
  bedLabelStyle?: string
  bedNoSeparator?: string
  floorRules?: ResidenceGenerationFloorRule[]
  specialRooms?: ResidenceGenerationSpecialRoomRule[]
}

export interface ResidenceBatchPreviewItem {
  entityType: string
  action: string
  identifier: string
  parentIdentifier?: string
  displayName?: string
  reason?: string
  safeOverwrite?: boolean
  payload?: Record<string, any>
}

export interface ResidenceBatchPreviewResponse {
  previewToken: string
  mode: string
  strategy: string
  createBuildingCount: number
  createFloorCount: number
  createRoomCount: number
  createBedCount: number
  overwriteSafeCount: number
  skipCount: number
  conflictCount: number
  warnings: string[]
  conflicts: string[]
  skipped: string[]
  safeOverwriteTargets: string[]
  items: ResidenceBatchPreviewItem[]
}

export interface ResidenceBatchCommitResponse {
  createdBuildingCount: number
  createdFloorCount: number
  createdRoomCount: number
  createdBedCount: number
  updatedRoomCount: number
  updatedBedCount: number
  skippedCount: number
  messages: string[]
}
