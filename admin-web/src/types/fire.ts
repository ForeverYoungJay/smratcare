export type FireSafetyRecordType =
  | 'FACILITY'
  | 'CONTROL_ROOM_DUTY'
  | 'MONTHLY_CHECK'
  | 'DAY_PATROL'
  | 'NIGHT_PATROL'
  | 'MAINTENANCE_REPORT'
  | 'FAULT_MAINTENANCE'

export type FireSafetyStatus = 'OPEN' | 'CLOSED'

export interface FireSafetyRecord {
  id: number
  recordType: FireSafetyRecordType
  title: string
  location?: string
  inspectorName: string
  checkTime: string
  status: FireSafetyStatus
  issueDescription?: string
  actionTaken?: string
  nextCheckDate?: string
}

export interface FireSafetyRecordQuery {
  pageNo?: number
  pageSize?: number
  keyword?: string
  recordType?: FireSafetyRecordType
  inspectorName?: string
  status?: FireSafetyStatus
  checkTimeStart?: string
  checkTimeEnd?: string
}

export interface FireSafetyTypeCount {
  recordType: FireSafetyRecordType
  count: number
}

export interface FireSafetyReportSummary {
  totalCount: number
  openCount: number
  closedCount: number
  overdueCount: number
  typeStats: FireSafetyTypeCount[]
}
