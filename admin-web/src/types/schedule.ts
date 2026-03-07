export interface ScheduleItem {
  id: number
  staffId: number
  staffName?: string
  dutyDate: string
  shiftCode?: string
  startTime?: string
  endTime?: string
  status?: number
}

export interface AttendanceItem {
  id: string | number
  staffId: string | number
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
  reviewedBy?: string | number
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
  staffId?: string | number
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
  staffId?: string | number
  staffName?: string
  available?: boolean
  status?: string
  message?: string
  contactPhone?: string
  staffNo?: string
}
