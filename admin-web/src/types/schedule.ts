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
  id: number
  staffId: number
  checkInTime?: string
  checkOutTime?: string
  status?: string
}
