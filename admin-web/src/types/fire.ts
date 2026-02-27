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
  qrToken?: string
  qrGeneratedAt?: string
  scanCompletedAt?: string
  dutyRecord?: string
  handoverPunchTime?: string
  equipmentBatchNo?: string
  productProductionDate?: string
  productExpiryDate?: string
  checkCycleDays?: number
  equipmentUpdateNote?: string
  equipmentAgingDisposal?: string
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
  dailyCompletedCount: number
  monthlyCompletedCount: number
  dutyRecordCount: number
  handoverPunchCount: number
  equipmentUpdateCount: number
  equipmentAgingDisposalCount: number
  expiringSoonCount: number
  nextCheckDueSoonCount: number
  typeStats: FireSafetyTypeCount[]
}

export interface FireSafetyQrPayload {
  recordId: number
  qrToken: string
  qrContent: string
  generatedAt: string
}

export interface FireSafetyReportRecordItem {
  id: number
  recordType: FireSafetyRecordType
  title: string
  location?: string
  inspectorName?: string
  checkTime?: string
  status?: FireSafetyStatus
  scanCompletedAt?: string
  dutyRecord?: string
  handoverPunchTime?: string
  equipmentBatchNo?: string
  productProductionDate?: string
  productExpiryDate?: string
  checkCycleDays?: number
  equipmentUpdateNote?: string
  equipmentAgingDisposal?: string
  issueDescription?: string
  actionTaken?: string
}

export interface FireSafetyReportDetail {
  totalCount: number
  dailyCompletedCount: number
  monthlyCompletedCount: number
  dutyRecordCount: number
  handoverPunchCount: number
  equipmentUpdateCount: number
  equipmentAgingDisposalCount: number
  records: FireSafetyReportRecordItem[]
}
