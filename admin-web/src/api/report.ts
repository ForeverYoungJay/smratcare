import request, { fetchPage } from '../utils/request'
import type { ReportItem } from '../types'

export function getReportPage(params: any) {
  return fetchPage<ReportItem>('/api/report/page', params)
}
