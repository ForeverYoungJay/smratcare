import type { Id } from './common'

export interface VisitBookingItem {
  id: Id
  elderId: Id
  elderName?: string
  familyName?: string
  floorNo?: string
  roomNo?: string
  visitTime: string
  visitorCount: number
  carPlate?: string
  status?: number
}

export interface VisitBookRequest {
  elderId: Id
  familyUserId?: Id
  visitTime: string
  visitTimeSlot: string
  visitorCount: number
  carPlate?: string
  remark?: string
}

export interface VisitCheckinRequest {
  bookingId?: Id
}

export interface VisitPrintTicket {
  bookingId: Id
  ticketNo: string
  elderName?: string
  familyName?: string
  floorNo?: string
  roomNo?: string
  visitTime?: string
  visitorCount?: number
  carPlate?: string
  statusText?: string
  generatedAt?: string
}
