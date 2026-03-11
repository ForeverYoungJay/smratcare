import type { Id } from './common'

export type AssessmentType =
  | 'ADMISSION'
  | 'REGISTRATION'
  | 'CONTINUOUS'
  | 'ARCHIVE'
  | 'OTHER_SCALE'
  | 'COGNITIVE'
  | 'SELF_CARE'

export interface AssessmentRecord {
  id: number | string
  idText?: string
  elderId?: Id
  elderName: string
  gender?: number
  genderLabel?: string
  age?: number
  phone?: string
  address?: string
  orgId?: Id
  orgName?: string
  assessmentType: AssessmentType
  assessmentTypeLabel?: string
  templateId?: number
  levelCode?: string
  score?: number
  assessmentDate: string
  nextAssessmentDate?: string
  assessorId?: number
  assessorName?: string
  status?: string
  statusLabel?: string
  resultSummary?: string
  suggestion?: string
  detailJson?: string
  scoreAuto?: number
  archiveNo?: string
  source?: string
  updateTime?: string
}

export interface AssessmentScaleTemplate {
  id: number
  templateCode: string
  templateName: string
  assessmentType: AssessmentType
  description?: string
  scoreRulesJson?: string
  levelRulesJson?: string
  status?: number
}

export interface AssessmentScorePreview {
  score: number
  levelCode?: string
  reason?: string
}

export interface AssessmentRecordSummary {
  totalCount: number
  draftCount: number
  completedCount: number
  archivedCount: number
  reassessOverdueCount: number
  highRiskCount: number
}

export interface AssessmentRecordReport {
  recordId: number
  reportNo: string
  reportStatus: string
  elderName?: string
  assessmentType?: AssessmentType
  assessmentTypeLabel?: string
  assessorName?: string
  assessmentDate?: string
  completedTime?: string
  score?: number
  levelCode?: string
  resultSummary?: string
  suggestion?: string
  detailJson?: string
}

export interface AssessmentBatchFailureItem {
  id?: number
  reason?: string
}

export interface AssessmentBatchOperationResult {
  successCount: number
  failedCount: number
  successIds: number[]
  failures: AssessmentBatchFailureItem[]
}
