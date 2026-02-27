export interface VisitBookingItem {
  id: number
  elderId: number
  elderName?: string
  familyName?: string
  floorNo?: string
  roomNo?: string
  visitTime: string
  visitorCount: number
  carPlate?: string
  visitCode?: string
  status?: number
}

export interface VisitBookRequest {
  elderId: number
  visitTime: string
  visitorCount: number
  carPlate?: string
}

export interface VisitCheckinRequest {
  bookingId?: number
  visitCode?: string
}
