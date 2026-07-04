import request, { fetchPage } from '../utils/request'
import type { EmrRecord, Id } from '../types'

export function getEmrPage(params: { pageNo?: number; pageSize?: number; elderId?: Id; recordType?: string }) {
  return fetchPage<EmrRecord>('/api/medical/emr/page', params)
}

export function getEmr(id: Id) {
  return request.get<EmrRecord>(`/api/medical/emr/${id}`)
}

export function saveEmr(data: Partial<EmrRecord>) {
  return request.post<EmrRecord>('/api/medical/emr', data)
}
