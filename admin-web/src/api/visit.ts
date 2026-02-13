import request from '../utils/request'
import type { VisitBookingItem, VisitBookRequest, VisitCheckinRequest } from '../types'

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
