export interface HealthDrugDictionary {
  id: number
  drugCode?: string
  drugName: string
  specification?: string
  unit?: string
  manufacturer?: string
  category?: string
  remark?: string
}

export interface HealthMedicationDeposit {
  id: number
  elderId?: number
  elderName?: string
  drugId?: number
  drugName: string
  depositDate: string
  quantity: number
  unit?: string
  expireDate?: string
  depositorName?: string
  remark?: string
}

export interface HealthMedicationSetting {
  id: number
  elderId?: number
  elderName?: string
  drugId?: number
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
  id: number
  elderId?: number
  elderName?: string
  drugId?: number
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
  elderId?: number
  elderName?: string
  drugId?: number
  drugName: string
  unit?: string
  depositQty: number
  usedQty: number
  remainQty: number
  minRemainQty?: number
  lowStock: number
}

export interface HealthArchive {
  id: number
  elderId?: number
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
  id: number
  elderId?: number
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
  id: number
  elderId?: number
  elderName?: string
  inspectionDate: string
  inspectionItem: string
  result?: string
  status?: string
  inspectorName?: string
  followUpAction?: string
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
  id: number
  elderId?: number
  elderName?: string
  sourceInspectionId?: number
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
  id: number
  settingId: number
  elderId?: number
  elderName?: string
  drugId?: number
  drugName: string
  plannedTime: string
  taskDate: string
  status: string
  registrationId?: number
  doneTime?: string
}
