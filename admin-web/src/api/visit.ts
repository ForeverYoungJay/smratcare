import request from '../utils/request'
import type { Id } from '../types/common'
import type { VisitBookingItem, VisitBookRequest, VisitCheckinRequest, VisitPrintTicket } from '../types'

export function bookVisit(data: VisitBookRequest) {
  return request.post<void>('/api/family/visit/book', data)
}

export function getFamilyVisits() {
  return request.get<VisitBookingItem[]>('/api/family/visit/my')
}

export function guardTodayVisits() {
  return request.get<VisitBookingItem[]>('/api/guard/visit/today')
}

export function guardCheckin(data: VisitCheckinRequest) {
  return request.post<void>('/api/guard/visit/checkin', data)
}

export function guardBookVisit(data: VisitBookRequest) {
  return request.post<VisitBookingItem>('/api/guard/visit/book', data)
}

export function guardUpdateVisit(id: Id, data: VisitBookRequest) {
  return request.put<VisitBookingItem>(`/api/guard/visit/${id}`, data)
}

export function guardDeleteVisit(id: Id) {
  return request.delete<void>(`/api/guard/visit/${id}`)
}

export function guardGetPrintTicket(id: Id) {
  return request.get<VisitPrintTicket>(`/api/guard/visit/${id}/print-ticket`)
}
