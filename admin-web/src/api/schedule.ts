import request, { fetchPage } from '../utils/request'
import type {
  ScheduleItem,
  AttendanceItem,
  AttendanceDashboardOverview,
  AttendanceSeasonRule,
  AttendanceStaffAvailability
} from '../types'

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
  const merged = { ...(params || {}) }
  return request.get<any>('/api/attendance/page', { params: merged }).then((res) => {
    if (res && Array.isArray(res.list)) {
      return res
    }
    if (res && Array.isArray(res.records)) {
      return {
        list: res.records as AttendanceItem[],
        total: Number(res.total || 0),
        pageNo: Number(res.current || merged.pageNo || 1),
        pageSize: Number(res.size || merged.pageSize || 10)
      }
    }
    if (Array.isArray(res)) {
      return {
        list: res as AttendanceItem[],
        total: res.length,
        pageNo: Number(merged.pageNo || 1),
        pageSize: Number(merged.pageSize || res.length || 10)
      }
    }
    return {
      list: [],
      total: 0,
      pageNo: Number(merged.pageNo || 1),
      pageSize: Number(merged.pageSize || 10)
    }
  })
}

export function getAttendanceOverview(params: { staffId?: string | number; month?: string }) {
  return request.get<AttendanceDashboardOverview>('/api/attendance/overview', { params })
}

export function getAttendanceSeasonRule() {
  return request.get<AttendanceSeasonRule>('/api/attendance/season-rule')
}

export function saveAttendanceSeasonRule(data: AttendanceSeasonRule) {
  return request.post<AttendanceSeasonRule>('/api/attendance/season-rule', data)
}

export function getAttendanceStaffAvailability(staffId: string | number) {
  return request.get<AttendanceStaffAvailability>('/api/attendance/staff-availability', { params: { staffId } })
}

export function reviewAttendanceRecord(id: string | number, data?: { reviewed?: number; reviewRemark?: string }) {
  return request.put<AttendanceItem>(`/api/attendance/${id}/review`, data || { reviewed: 1 })
}
