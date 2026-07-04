import request, { fetchPage } from '../utils/request'
import type { GovChannelConfig, GovReportSnapshot, GovReportTask, Id } from '../types'

export function buildGovReportTask(data: {
  reportType: string
  channel: string
  period?: string
  remark?: string
}) {
  return request.post<Id>('/api/govreport/tasks/build', data)
}

export function sendGovReportTask(id: Id) {
  return request.post<GovReportTask>(`/api/govreport/tasks/${id}/send`)
}

export function getGovReportTaskPage(params: {
  pageNo?: number
  pageSize?: number
  channel?: string
  reportType?: string
  status?: string
}) {
  return fetchPage<GovReportTask>('/api/govreport/tasks/page', params)
}

export function getGovReportSnapshot(id: Id) {
  return request.get<GovReportSnapshot>(`/api/govreport/tasks/${id}/snapshot`)
}

export function getGovChannels() {
  return request.get<GovChannelConfig[]>('/api/govreport/channels')
}
