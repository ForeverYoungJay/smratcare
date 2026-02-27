export interface MedicalTcmAssessment {
  id: number
  elderId?: number
  elderName: string
  assessmentDate: string
  assessorId?: number
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
  id: number
  elderId?: number
  elderName: string
  assessmentDate: string
  assessorId?: number
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
  elderId?: number
  elderName?: string
  riskLevel?: string
  keyRiskFactors?: string
  assessmentDate?: string
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
  todayMedicationPendingCount: number
  todayMedicationDoneCount: number
  tcmPublishedCount: number
  cvdHighRiskCount: number
  cvdNeedFollowupCount: number
  keyResidents: MedicalCareRiskResident[]
}

export interface MedicalResidentRiskCard {
  elderId?: number
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
}

export interface MedicalResidentOverview {
  elderId?: number
  elderName?: string
  currentStatus?: string
  hasUnclosedIncident?: boolean
  cards: MedicalResidentOverviewCard[]
}
