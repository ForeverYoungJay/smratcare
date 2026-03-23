import type { Id } from './common'

export interface ScheduleItem {
  id: Id
  staffId: Id
  staffName?: string
  sourceTemplateId?: Id
  sourceTemplateName?: string
  dutyDate: string
  shiftCode?: string
  startTime?: string
  endTime?: string
  calendarTaskId?: Id
  dutyTodoId?: Id
  status?: number
}

export interface ShiftSwapRequestItem {
  id: Id
  applicantStaffId?: Id
  applicantStaffName?: string
  applicantScheduleId?: Id
  applicantDutyDate?: string
  applicantShiftCode?: string
  applicantStartTime?: string
  applicantEndTime?: string
  targetStaffId?: Id
  targetStaffName?: string
  targetScheduleId?: Id
  targetDutyDate?: string
  targetShiftCode?: string
  targetStartTime?: string
  targetEndTime?: string
  status?: string
  targetConfirmStatus?: string
  approvalId?: Id
  approvalStatus?: string
  applicantRemark?: string
  targetRemark?: string
  targetConfirmedAt?: string
  approvalSubmittedAt?: string
  completedAt?: string
  createTime?: string
}

export interface AttendanceItem {
  id: Id
  staffId: Id
  staffName?: string
  workDate?: string
  checkInTime?: string
  checkOutTime?: string
  outingStartTime?: string
  outingEndTime?: string
  lunchBreakStartTime?: string
  lunchBreakEndTime?: string
  outingMinutes?: number
  reviewed?: number
  reviewedBy?: Id
  reviewedAt?: string
  reviewRemark?: string
  note?: string
  status?: string
}

export interface AttendanceDashboardDayItem {
  date: string
  weekLabel?: string
  checkInTime?: string
  checkOutTime?: string
  outingStartTime?: string
  outingEndTime?: string
  lunchBreakStartTime?: string
  lunchBreakEndTime?: string
  outingMinutes?: number
  lunchBreakMinutes?: number
  workMinutes?: number
  status?: string
  late?: boolean
  earlyLeave?: boolean
  hasLeave?: boolean
  leaveTitle?: string
  leaveType?: string
  scheduleTitles?: string
  anomalyText?: string
}

export interface AttendanceDashboardOverview {
  staffId?: Id
  staffName?: string
  month?: string
  seasonType?: 'WINTER' | 'SUMMER' | string
  expectedWorkStart?: string
  expectedWorkEnd?: string
  lateGraceMinutes?: number
  earlyLeaveGraceMinutes?: number
  outingMaxMinutes?: number
  onDutyDays?: number
  leaveDays?: number
  lateCount?: number
  earlyLeaveCount?: number
  abnormalCount?: number
  totalOutingMinutes?: number
  totalLunchBreakMinutes?: number
  todayStatus?: string
  todayStatusLabel?: string
  days?: AttendanceDashboardDayItem[]
}

export interface AttendanceSeasonRule {
  winterWorkStart?: string
  winterWorkEnd?: string
  summerWorkStart?: string
  summerWorkEnd?: string
  lateGraceMinutes?: number
  earlyLeaveGraceMinutes?: number
  outingMaxMinutes?: number
  lateEnabled?: number
  earlyLeaveEnabled?: number
  outingOvertimeEnabled?: number
  missingCheckoutEnabled?: number
  enabledStatus?: 'ACTIVE' | 'INACTIVE' | string
}

export interface AttendanceStaffAvailability {
  staffId?: Id
  staffName?: string
  available?: boolean
  status?: string
  message?: string
  contactPhone?: string
  staffNo?: string
}
