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

export interface MedicalCareRiskResident {
  elderId?: number
  elderName?: string
  riskLevel?: string
  keyRiskFactors?: string
  assessmentDate?: string
}

export interface MedicalCareWorkbenchSummary {
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
