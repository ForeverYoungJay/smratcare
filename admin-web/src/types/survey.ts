export interface SurveyQuestion {
  id: string | number
  questionCode: string
  title: string
  questionType: string
  optionsJson?: string | null
  requiredFlag?: number
  scoreEnabled?: number
  maxScore?: number | null
  status?: number
  sortNo?: number
}

export interface SurveyTemplate {
  id: string | number
  templateCode: string
  templateName: string
  description?: string
  content?: string
  targetType: string
  status?: number
  startDate?: string | null
  endDate?: string | null
  anonymousFlag?: number
  scoreEnabled?: number
  totalScore?: number | null
}

export interface SurveyTemplateQuestionDetail {
  questionId: string | number
  questionCode: string
  title: string
  questionType: string
  optionsJson?: string | null
  requiredFlag?: number
  scoreEnabled?: number
  maxScore?: number | null
  status?: number
  sortNo?: number
  weight?: number | null
}

export interface SurveyTemplateDetail extends SurveyTemplate {
  questions: SurveyTemplateQuestionDetail[]
}

export interface SurveyTemplateVerifyResult {
  templateId: string | number
  templateCode?: string
  templateName?: string
  targetType?: string
  status?: number
  valid: boolean
  message?: string
  hasQuestions?: boolean
}

export interface SurveyTemplateQuestionItem {
  questionId: string | number
  sortNo?: number
  requiredFlag?: number
  weight?: number | null
}

export interface SurveySubmissionAnswerItem {
  questionId: string | number
  answerJson?: string
  answerText?: string
  score?: number
}

export interface SurveySubmissionRequest {
  templateId: string | number
  templateCode?: string
  targetType: string
  targetId?: string | number
  relatedStaffId?: string | number
  anonymousFlag?: number
  answers: SurveySubmissionAnswerItem[]
}

export interface SurveySubmissionItem {
  id: string | number
  templateId?: string | number
  targetType?: string
  targetId?: string | number
  submitterName?: string
  scoreTotal?: number
  createTime?: string
}

export interface SurveySubmissionResponse {
  submissionId: string | number
  scoreTotal?: number
}

export interface SurveyStatsSummary {
  totalSubmissions: number
  scoredSubmissions?: number
  unscoredSubmissions?: number
  averageScore: number
  scoreTotal: number
}

export interface SurveyPerformanceItem {
  staffId: string | number
  staffName?: string
  submissions: number
  averageScore: number
}
