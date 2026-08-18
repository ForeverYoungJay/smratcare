import request from '../utils/request'
import type { Id } from '../types'

export interface FloorPlanResident {
  elderId: Id
  elderName?: string
  bedNo?: string
  careLevel?: string
  outstandingAmount: number
}

export interface FloorPlanRoom {
  roomId: Id
  roomNo: string
  roomType?: string
  orientation?: string
  orientationText?: string
  capacity: number
  totalBeds: number
  occupiedBeds: number
  elderCount: number
  enabled: boolean
  planStatus: 'NORMAL' | 'OVERDUE' | 'EMPTY' | 'DISABLED' | string
  planStatusText?: string
  overdueAmount: number
  electricityUnpaid: boolean
  electricityFee: number
  residents: FloorPlanResident[]
}

export interface FloorPlanFloor {
  floorNo: string
  roomCount: number
  bedCount: number
  occupiedBeds: number
  southRooms: FloorPlanRoom[]
  northRooms: FloorPlanRoom[]
  otherRooms: FloorPlanRoom[]
}

export interface FloorPlanBuilding {
  building: string
  roomCount: number
  bedCount: number
  floors: FloorPlanFloor[]
}

export interface FloorPlanResponse {
  billMonth: string
  totalRooms: number
  occupiedRooms: number
  emptyRooms: number
  disabledRooms: number
  overdueRooms: number
  totalBeds: number
  occupiedBeds: number
  bedUsageRate: number
  buildings: FloorPlanBuilding[]
}

export function getFloorPlan(month?: string) {
  return request.get<FloorPlanResponse>('/api/finance/floor-plan', { params: { month } })
}
