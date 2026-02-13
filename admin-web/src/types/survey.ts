export interface SurveyQuestion {
  id: number
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
  id: number
  templateCode: string
  templateName: string
  description?: string
  targetType: string
  status?: number
  startDate?: string | null
  endDate?: string | null
  anonymousFlag?: number
  scoreEnabled?: number
  totalScore?: number | null
}

export interface SurveyTemplateQuestionDetail {
  questionId: number
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

export interface SurveyTemplateQuestionItem {
  questionId: number
  sortNo?: number
  requiredFlag?: number
  weight?: number | null
}

export interface SurveySubmissionAnswerItem {
  questionId: number
  answerJson?: string
  answerText?: string
  score?: number
}

export interface SurveySubmissionRequest {
  templateId: number
  targetType: string
  targetId?: number
  relatedStaffId?: number
  anonymousFlag?: number
  answers: SurveySubmissionAnswerItem[]
}

export interface SurveySubmissionResponse {
  submissionId: number
  scoreTotal?: number
}

export interface SurveyStatsSummary {
  totalSubmissions: number
  averageScore: number
  scoreTotal: number
}

export interface SurveyPerformanceItem {
  staffId: number
  staffName?: string
  submissions: number
  averageScore: number
}
