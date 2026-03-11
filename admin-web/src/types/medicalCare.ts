import type { Id } from './common'

export interface MedicalTcmAssessment {
  id: Id
  elderId?: Id
  elderName: string
  assessmentDate: string
  assessorId?: Id
  assessorName?: string
  assessmentScene?: 'ADMISSION' | 'REASSESSMENT' | 'SPECIAL'
  constitutionPrimary?: string
  constitutionSecondary?: string
  score?: number
  confidence?: number
  featureSummary?: string
  suggestionDiet?: string
  suggestionRoutine?: string
  suggestionExercise?: string
  suggestionEmotion?: string
  nursingTip?: string
  suggestionPoints?: string
  questionnaireJson?: string
  isReassessment?: number
  familyVisible?: number
  generateNursingTask?: number
  status?: 'DRAFT' | 'PUBLISHED'
  updateTime?: string
}

export interface MedicalCvdAssessment {
  id: Id
  elderId?: Id
  elderName: string
  assessmentDate: string
  assessorId?: Id
  assessorName?: string
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'VERY_HIGH'
  keyRiskFactors?: string
  lifestyleJson?: string
  factorJson?: string
  conclusion?: string
  medicalAdvice?: string
  nursingAdvice?: string
  followupDays?: number
  needFollowup?: number
  generateInspectionPlan?: number
  generateFollowupTask?: number
  suggestMedicalOrder?: number
  status?: 'DRAFT' | 'PUBLISHED'
  updateTime?: string
}

export interface MedicalCvdAssessmentSummary {
  totalCount: number
  draftCount: number
  publishedCount: number
  highRiskCount: number
  veryHighRiskCount: number
  needFollowupCount: number
  followupOverdueCount: number
}

export interface MedicalTcmAssessmentSummary {
  totalCount: number
  draftCount: number
  publishedCount: number
  reassessmentCount: number
  familyVisibleCount: number
  nursingTaskSuggestedCount: number
  balancedConstitutionCount: number
  biasedConstitutionCount: number
  lowConfidenceCount: number
}

export interface MedicalCareRiskResident {
  elderId?: Id
  elderName?: string
  riskLevel?: string
  keyRiskFactors?: string
  assessmentDate?: string
}

export interface MedicalCareFocusAction {
  key?: string
  title?: string
  description?: string
  route?: string
  severity?: 'HIGH' | 'MEDIUM' | 'LOW' | string
  count?: number
}

export interface MedicalCareWorkbenchSummaryQuery {
  elderId?: Id
  date?: string
  status?: string
  incidentWindowDays?: number
  overdueHours?: number
  topResidentLimit?: number
  riskResidentLookbackDays?: number
}

export interface MedicalRiskTimelinePoint {
  date?: string
  riskScore?: number
  riskLevel?: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL' | string
  medicationPendingCount?: number
  inspectionOpenCount?: number
  careOverdueCount?: number
  incidentCount?: number
  rectifyOverdueCount?: number
  overdueHours?: number
}

export interface MedicalCareWorkbenchSummary {
  pendingMedicalOrderCount: number
  pendingReviewCount: number
  pendingAuditCount: number
  unclosedAbnormalCount: number
  todayInspectionTodoCount: number
  topRiskResidentCount: number
  abnormalVital24hCount: number
  abnormalEvent24hCount: number
  medicalOrderShouldCount: number
  medicalOrderDoneCount: number
  medicalOrderPendingCount: number
  medicalOrderAbnormalCount: number
  orderCheckRate: number
  medicationShouldCount: number
  medicationDoneCount: number
  medicationUndoneCount: number
  medicationLowStockCount: number
  medicationRequestPendingCount: number
  careTaskShouldCount: number
  careTaskDoneCount: number
  careTaskOverdueCount: number
  scanExecuteRate: number
  todayInspectionPlanCount: number
  nursingLogPendingCount: number
  handoverPendingCount: number
  handoverDoneCount: number
  handoverRiskCount: number
  handoverTodoCount: number
  incidentOpenCount: number
  incident30dCount: number
  incident30dRate: number
  lowScoreSurveyCount: number
  rectifyInProgressCount: number
  rectifyOverdueCount: number
  aiReportGeneratedCount: number
  aiReportPublishedCount: number
  pendingCareTaskCount: number
  overdueCareTaskCount: number
  todayInspectionPendingCount: number
  todayInspectionDoneCount: number
  abnormalInspectionCount: number
  abnormalInspectionOpenCount?: number
  todayMedicationPendingCount: number
  todayMedicationDoneCount: number
  tcmPublishedCount: number
  cvdHighRiskCount: number
  cvdNeedFollowupCount: number
  keyResidents: MedicalCareRiskResident[]

  configuredIncidentWindowDays?: number
  configuredOverdueHours?: number
  configuredTopResidentLimit?: number
  configuredRiskResidentLookbackDays?: number
  snapshotDate?: string
  incidentWindowStartDate?: string
  incidentWindowEndDate?: string
  generatedAt?: string

  medicalOrderExecutionRate?: number
  careTaskCompletionRate?: number
  inspectionCompletionRate?: number
  careTaskOverdueRate?: number
  medicationPendingRate?: number
  inspectionAbnormalRate?: number

  riskIndex?: number
  riskLevel?: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
  riskTriggeredCount?: number
  riskSignals?: string[]
  focusActionCount?: number
  focusActions?: MedicalCareFocusAction[]
}

export interface MedicalUnifiedTaskItem {
  id: Id
  module: 'ORDER' | 'INSPECTION' | 'NURSING_LOG' | 'HANDOVER' | string
  residentId?: Id
  residentName?: string
  taskTitle: string
  assignee?: string
  plannedTime?: string
  priority: 'HIGH' | 'MEDIUM' | 'LOW' | string
  status?: string
  sourceId?: Id
  overdue?: boolean
  overdueMinutes?: number
  riskScore?: number
  riskReason?: string
  suggestedRoute?: string
}

export interface MedicalUnifiedTaskQuery {
  pageNo?: number
  pageSize?: number
  elderId?: Id
  module?: string
  priority?: string
  status?: string
  keyword?: string
  overdueHours?: number
  onlyOverdue?: boolean | number | string
  sortBy?: 'PLANNED_TIME' | 'PRIORITY' | 'RISK_SCORE' | string
  sortDirection?: 'ASC' | 'DESC' | string
}

export interface MedicalAlertRuleConfig {
  medicationHighDosageThreshold: number
  overdueHoursThreshold: number
  abnormalInspectionRequirePhoto: number
  handoverAutoFillConfirmTime: number
  autoCreateNursingLogFromInspection: number
  autoRaiseTaskFromAbnormal: number
  autoCarryResidentContext: number
  handoverRiskKeywords: string
}

export interface MedicalResidentRiskCard {
  elderId?: Id
  elderName?: string
  latestTcmPrimary?: string
  latestTcmSecondary?: string
  latestTcmDate?: string
  latestTcmSuggestion?: string
  latestCvdRiskLevel?: string
  latestCvdDate?: string
  latestCvdFactors?: string
  latestCvdConclusion?: string
  abnormalVital24hCount: number
  abnormalInspectionOpenCount: number
  openIncidentCount: number
  pendingCareTaskCount: number
  tcmAssessmentRoute?: string
  cvdAssessmentRoute?: string
}

export interface MedicalResidentOverviewAction {
  label: string
  path: string
  primary?: boolean
}

export interface MedicalResidentOverviewCard {
  key: string
  title: string
  tag: string
  tagColor: string
  description?: string
  lines: string[]
  actions: MedicalResidentOverviewAction[]
  dangerCount?: number
  warningCount?: number
  alertCount?: number
}

export interface MedicalResidentOverview {
  elderId?: Id
  elderName?: string
  currentStatus?: string
  hasUnclosedIncident?: boolean
  alertCardCount?: number
  alertTotalCount?: number
  generatedAt?: string
  cards: MedicalResidentOverviewCard[]
}

export interface MedicalAiReportItem {
  id: Id
  type: 'WEEKLY' | 'MONTHLY' | 'CVD' | 'CHRONIC' | string
  status: 'GENERATING' | 'GENERATED' | 'PUBLISHED' | string
  elderId?: Id
  elderName?: string
  dateFrom?: string
  dateTo?: string
  rangeText?: string
  highRiskCount?: number
  createdAt?: string
}

export interface MedicalAiGenerateTaskResponse {
  inspectionRoute?: string
  medicalTodoRoute?: string
  nursingTaskRoute?: string
}
