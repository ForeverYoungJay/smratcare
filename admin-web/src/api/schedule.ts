import request, { fetchPage } from '../utils/request'
import type { Id } from '../types/common'
import type {
  ScheduleItem,
  ShiftSwapRequestItem,
  AttendanceItem,
  AttendanceDashboardOverview,
  AttendanceSeasonRule,
  AttendanceStaffAvailability
} from '../types'

export function getSchedulePage(params: any) {
  return fetchPage<ScheduleItem>('/api/schedule/page', params)
}

export function getSwapCandidatePage(params: { pageNo?: number; pageSize?: number; targetStaffId: Id; dutyDate: string }) {
  return fetchPage<ScheduleItem>('/api/schedule/swap-candidates', params)
}

export function createSchedule(data: Partial<ScheduleItem>) {
  return request.post<void>('/api/schedule', data)
}

export function updateSchedule(id: Id, data: Partial<ScheduleItem>) {
  return request.put<void>(`/api/schedule/${id}`, data)
}

export function deleteSchedule(id: Id) {
  return request.delete<void>(`/api/schedule/${id}`)
}

export function getShiftSwapPage(params: any) {
  return fetchPage<ShiftSwapRequestItem>('/api/schedule/shift-swap/page', params)
}

export function createShiftSwap(data: { applicantScheduleId: Id; targetScheduleId: Id; applicantRemark?: string }) {
  return request.post<ShiftSwapRequestItem>('/api/schedule/shift-swap', data)
}

export function agreeShiftSwap(id: Id, data?: { remark?: string }) {
  return request.put<ShiftSwapRequestItem>(`/api/schedule/shift-swap/${id}/target-agree`, data || {})
}

export function rejectShiftSwap(id: Id, data?: { remark?: string }) {
  return request.put<ShiftSwapRequestItem>(`/api/schedule/shift-swap/${id}/target-reject`, data || {})
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

export function getAttendanceOverview(params: { staffId?: Id; month?: string }) {
  return request.get<AttendanceDashboardOverview>('/api/attendance/overview', { params })
}

export function getAttendanceSeasonRule() {
  return request.get<AttendanceSeasonRule>('/api/attendance/season-rule')
}

export function saveAttendanceSeasonRule(data: AttendanceSeasonRule) {
  return request.post<AttendanceSeasonRule>('/api/attendance/season-rule', data)
}

export function getAttendanceStaffAvailability(staffId: Id) {
  return request.get<AttendanceStaffAvailability>('/api/attendance/staff-availability', { params: { staffId } })
}

export function reviewAttendanceRecord(id: Id, data?: { reviewed?: number; reviewRemark?: string }) {
  return request.put<AttendanceItem>(`/api/attendance/${id}/review`, data || { reviewed: 1 })
}
