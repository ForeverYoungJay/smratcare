export interface CaregiverGroupItem {
  id: number
  name: string
  leaderStaffId?: number
  leaderStaffName?: string
  memberStaffIds?: number[]
  enabled?: number
  remark?: string
}

export interface ShiftTemplateItem {
  id: number
  name: string
  shiftCode: string
  startTime: string
  endTime: string
  crossDay?: number
  requiredStaffCount?: number
  enabled?: number
  remark?: string
}

export interface ShiftHandoverItem {
  id: number
  dutyDate: string
  shiftCode: string
  fromStaffId: number
  fromStaffName?: string
  toStaffId: number
  toStaffName?: string
  summary?: string
  riskNote?: string
  todoNote?: string
  status?: string
  handoverTime?: string
  confirmTime?: string
}

export interface CareLevelItem {
  id: number
  levelCode: string
  levelName: string
  severity?: number
  monthlyFee?: number
  enabled?: number
  remark?: string
}

export interface ServicePlanItem {
  id: number
  elderId: number
  elderName?: string
  careLevelId?: number
  careLevelName?: string
  serviceItemId: number
  serviceItemName?: string
  planName: string
  cycleType?: string
  frequency?: number
  startDate: string
  endDate?: string
  preferredTime?: string
  defaultStaffId?: number
  defaultStaffName?: string
  status?: string
  remark?: string
}

export interface ServiceBookingItem {
  id: number
  elderId: number
  elderName?: string
  planId?: number
  planName?: string
  serviceItemId: number
  serviceItemName?: string
  bookingTime: string
  expectedDuration?: number
  assignedStaffId?: number
  assignedStaffName?: string
  source?: string
  status?: string
  cancelReason?: string
  remark?: string
}

export interface NursingRecordItem {
  recordType: string
  sourceId: number
  recordTime?: string
  elderId?: number
  elderName?: string
  staffId?: number
  staffName?: string
  serviceName?: string
  planId?: number
  planName?: string
  status?: string
  successFlag?: number
  remark?: string
}

export interface NursingStaffWorkloadItem {
  staffId?: number
  staffName?: string
  bookingCount?: number
  completedBookingCount?: number
  executeCount?: number
  completedExecuteCount?: number
  totalLoad?: number
}

export interface NursingReportSummary {
  totalServices?: number
  completedServices?: number
  completionRate?: number
  overdueCount?: number
  overdueRate?: number
  planBookingCount?: number
  planCompletedCount?: number
  planAchievementRate?: number
  staffWorkloads?: NursingStaffWorkloadItem[]
}
