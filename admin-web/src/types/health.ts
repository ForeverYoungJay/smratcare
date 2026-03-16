import type { Id } from './common'

export interface HealthDrugDictionary {
  id: Id
  drugCode?: string
  drugName: string
  specification?: string
  unit?: string
  manufacturer?: string
  category?: string
  remark?: string
}

export interface HealthMedicationDeposit {
  id: Id
  elderId?: Id
  elderName?: string
  drugId?: Id
  drugName: string
  depositDate: string
  quantity: number
  unit?: string
  expireDate?: string
  depositorName?: string
  remark?: string
}

export interface HealthMedicationSetting {
  id: Id
  elderId?: Id
  elderName?: string
  drugId?: Id
  drugName: string
  dosage?: string
  frequency?: string
  medicationTime?: string
  startDate?: string
  endDate?: string
  minRemainQty?: number
  remark?: string
}

export interface HealthMedicationRegistration {
  id: Id
  elderId?: Id
  elderName?: string
  drugId?: Id
  drugName: string
  registerTime: string
  dosageTaken: number
  unit?: string
  nurseName?: string
  remark?: string
}

export interface HealthNameCountStat {
  name: string
  totalCount: number
}

export interface HealthMedicationRegistrationSummary {
  totalCount: number
  todayCount: number
  totalDosage: number
  doneTaskCount: number
  pendingTaskCount: number
  nurseStats: HealthNameCountStat[]
}

export interface HealthMedicationRemainingItem {
  elderId?: Id
  elderName?: string
  drugId?: Id
  drugName: string
  unit?: string
  depositQty: number
  usedQty: number
  remainQty: number
  minRemainQty?: number
  lowStock: number
}

export interface HealthArchive {
  id: Id
  elderId?: Id
  elderName?: string
  bloodType?: string
  allergyHistory?: string
  chronicDisease?: string
  medicalHistory?: string
  emergencyContact?: string
  emergencyPhone?: string
  remark?: string
}

export interface HealthDataRecord {
  id: Id
  elderId?: Id
  elderName?: string
  dataType: string
  dataValue: string
  measuredAt: string
  source?: string
  abnormalFlag?: number
  remark?: string
}

export interface HealthDataTypeStat {
  dataType: string
  totalCount: number
  abnormalCount: number
  abnormalRate: number
}

export interface HealthDataSummary {
  totalCount: number
  abnormalCount: number
  normalCount: number
  abnormalRate: number
  latestMeasuredAt?: string
  typeStats: HealthDataTypeStat[]
}

export interface HealthInspection {
  id: Id
  elderId?: Id
  elderName?: string
  inspectionDate: string
  inspectionItem: string
  result?: string
  status?: string
  inspectorName?: string
  followUpAction?: string
  attachmentUrls?: string
  otherNote?: string
  remark?: string
}

export interface HealthInspectionVitalThreshold {
  id?: Id
  orgId?: Id
  type: string
  metricCode?: string
  minValue?: number
  maxValue?: number
  status?: number
  remark?: string
}

export interface HealthInspectionSummary {
  totalCount: number
  abnormalCount: number
  followingCount: number
  closedCount: number
  linkedLogCount: number
  statusStats: HealthNameCountStat[]
}

export interface HealthNursingLog {
  id: Id
  elderId?: Id
  elderName?: string
  sourceInspectionId?: Id
  logTime: string
  logType: string
  content: string
  staffName?: string
  status?: string
  remark?: string
}

export interface HealthNursingLogSummary {
  totalCount: number
  pendingCount: number
  doneCount: number
  closedCount: number
  linkedInspectionCount: number
  logTypeStats: HealthNameCountStat[]
}

export interface HealthMedicationTask {
  id: Id
  settingId: Id
  elderId?: Id
  elderName?: string
  drugId?: Id
  drugName: string
  plannedTime: string
  taskDate: string
  status: string
  registrationId?: Id
  doneTime?: string
}
