import request, { fetchPage } from '../utils/request'
import type { ScheduleItem, AttendanceItem } from '../types'

export function getSchedulePage(params: any) {
  return fetchPage<ScheduleItem>('/api/schedule/page', params)
}

export function createSchedule(data: Partial<ScheduleItem>) {
  return request.post<void>('/api/schedule', data)
}

export function updateSchedule(id: number, data: Partial<ScheduleItem>) {
  return request.put<void>(`/api/schedule/${id}`, data)
}

export function deleteSchedule(id: number) {
  return request.delete<void>(`/api/schedule/${id}`)
}

export function getAttendancePage(params: any) {
  return fetchPage<AttendanceItem>('/api/attendance/page', params)
}
