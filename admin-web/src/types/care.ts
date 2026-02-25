export interface CareTaskItem {
  taskDailyId: number
  elderId: number
  elderName: string
  bedId: number
  roomNo: string
  staffId?: number
  staffName?: string
  taskName: string
  planTime: string
  status: string
  suspiciousFlag?: boolean
  overdueFlag?: boolean
  careLevel?: string
}

export interface CareTaskSummary {
  totalCount?: number
  pendingCount?: number
  doneCount?: number
  exceptionCount?: number
  overdueCount?: number
  suspiciousCount?: number
  assignedCount?: number
  unassignedCount?: number
  completionRate?: number
  exceptionRate?: number
}

export interface CareGenerateRequest {
  date?: string
  force?: boolean
}

export interface CareExecuteRequest {
  taskDailyId: number
  staffId: number
  bedQrCode: string
  remark?: string
}

export interface CareTaskAssignRequest {
  taskDailyId: number
  staffId: number
  force?: boolean
}

export interface CareTaskBatchAssignRequest {
  staffId: number
  dateFrom?: string
  dateTo?: string
  roomNo?: string
  floorNo?: string
  building?: string
  careLevel?: string
  status?: string
  force?: boolean
}

export interface CareTaskReviewRequest {
  taskDailyId: number
  staffId: number
  score: number
  comment?: string
  reviewerType?: string
  reviewTime?: string
}

export interface CareTaskCreateRequest {
  elderId: number
  templateId?: number
  taskName?: string
  planTime: string
  staffId?: number
  status?: string
}

export interface CareTaskTemplate {
  id: number
  taskName: string
  frequencyPerDay: number
  careLevelRequired?: string
  chargeAmount?: number
  enabled: boolean
}

export interface CareExecuteLogItem {
  id: number
  taskDailyId: number
  staffId?: number
  staffName?: string
  executeTime: string
  bedQrCode?: string
  resultStatus?: number
  suspiciousFlag?: boolean
  remark?: string
}
