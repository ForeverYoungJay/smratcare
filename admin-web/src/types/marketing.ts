export interface MarketingConversionReport {
  totalLeads: number
  consultCount: number
  intentCount: number
  reservationCount: number
  invalidCount: number
  contractCount: number
  intentRate: number
  reservationRate: number
  contractRate: number
}

export interface MarketingConsultationTrendItem {
  date: string
  total: number
  consultCount: number
  intentCount: number
}

export interface MarketingChannelReportItem {
  source: string
  leadCount: number
  reservationCount: number
  contractCount: number
}

export interface MarketingFollowupStageItem {
  name: string
  count: number
  rate: number
}

export interface MarketingFollowupReport {
  totalLeads: number
  consultCount: number
  intentCount: number
  reservationCount: number
  invalidCount: number
  overdueCount: number
  stages: MarketingFollowupStageItem[]
}

export interface MarketingCallbackItem {
  id: number
  name: string
  phone?: string
  source?: string
  nextFollowDate?: string
  remark?: string
}

export interface MarketingCallbackReport {
  todayDue: number
  overdue: number
  completed: number
  total: number
  records: MarketingCallbackItem[]
}

export interface MarketingReportQuery {
  dateFrom?: string
  dateTo?: string
  source?: string
  staffId?: number
}

export interface MarketingDataQualityReport {
  missingSourceCount: number
  missingNextFollowDateCount: number
  nonStandardSourceCount: number
}
