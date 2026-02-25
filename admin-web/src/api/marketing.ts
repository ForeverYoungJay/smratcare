import { getCrmLeadPage, createCrmLead, updateCrmLead, deleteCrmLead } from './crm'
import { getAdmissionRecords } from './elderLifecycle'
import request from '../utils/request'
import type {
  AdmissionRecordItem,
  CrmLeadItem,
  MarketingCallbackReport,
  MarketingChannelReportItem,
  MarketingConsultationTrendItem,
  MarketingConversionReport,
  MarketingDataQualityReport,
  MarketingFollowupReport,
  MarketingReportQuery,
  PageResult
} from '../types'

export { createCrmLead, updateCrmLead, deleteCrmLead }

export function getLeadPage(params: any) {
  return getCrmLeadPage(params)
}

export async function getAllLeads(pageSize = 200): Promise<CrmLeadItem[]> {
  let pageNo = 1
  let total = 0
  const result: CrmLeadItem[] = []
  do {
    const page: PageResult<CrmLeadItem> = await getCrmLeadPage({ pageNo, pageSize })
    result.push(...(page.list || []))
    total = page.total || 0
    pageNo += 1
  } while (result.length < total && pageNo <= 30)
  return result
}

export async function getAllAdmissions(pageSize = 200): Promise<AdmissionRecordItem[]> {
  let pageNo = 1
  let total = 0
  const result: AdmissionRecordItem[] = []
  do {
    const page: PageResult<AdmissionRecordItem> = await getAdmissionRecords({ pageNo, pageSize })
    result.push(...(page.list || []))
    total = page.total || 0
    pageNo += 1
  } while (result.length < total && pageNo <= 30)
  return result
}

export function getMarketingConversionReport(params?: MarketingReportQuery) {
  return request.get<MarketingConversionReport>('/api/marketing/report/conversion', { params })
}

export function getMarketingConsultationTrend(days = 14, params?: MarketingReportQuery) {
  return request.get<MarketingConsultationTrendItem[]>('/api/marketing/report/consultation', {
    params: { days, ...(params || {}) }
  })
}

export function getMarketingChannelReport(params?: MarketingReportQuery) {
  return request.get<MarketingChannelReportItem[]>('/api/marketing/report/channel', { params })
}

export function getMarketingFollowupReport(params?: MarketingReportQuery) {
  return request.get<MarketingFollowupReport>('/api/marketing/report/followup', { params })
}

export function getMarketingCallbackReport(params?: { pageNo?: number; pageSize?: number } & MarketingReportQuery) {
  return request.get<MarketingCallbackReport>('/api/marketing/report/callback', { params })
}

export function getMarketingDataQualityReport() {
  return request.get<MarketingDataQualityReport>('/api/marketing/report/data-quality')
}

export function normalizeMarketingSources() {
  return request.post<number>('/api/marketing/report/data-quality/normalize-source')
}
